package dk.kb.bitrepository.mediator.filehandler;

import dk.kb.bitrepository.mediator.crypto.CryptoStrategy;
import dk.kb.bitrepository.mediator.database.DatabaseDAO;
import dk.kb.bitrepository.mediator.filehandler.exception.MismatchingChecksumsException;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import static dk.kb.bitrepository.mediator.filehandler.FileUtils.*;
import static dk.kb.bitrepository.mediator.utils.configurations.ConfigConstants.ENCRYPTED_FILES_PATH;
import static dk.kb.bitrepository.mediator.utils.configurations.ConfigConstants.UNENCRYPTED_FILES_PATH;
import static org.bitrepository.common.utils.ChecksumUtils.generateChecksum;

public class PutFileHandler {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final String collectionID;
    private final String fileID;
    private final Path unencryptedFilePath;
    private final Path encryptedFilePath;
    private final byte[] unencryptedBytes;
    private final ChecksumSpecTYPE checksumSpec;
    private final String expectedChecksum;
    private final OffsetDateTime receivedTimestamp;
    private final DatabaseDAO dao;
    private final CryptoStrategy crypto;

    public PutFileHandler(String collectionID, String fileID, byte[] unencryptedBytes, ChecksumDataForFileTYPE checksumData,
                          OffsetDateTime receivedTimestamp, DatabaseDAO dao, CryptoStrategy crypto) {
        this.collectionID = collectionID;
        this.fileID = fileID;
        this.unencryptedFilePath = getFilePath(UNENCRYPTED_FILES_PATH, collectionID, fileID);
        this.encryptedFilePath = getFilePath(ENCRYPTED_FILES_PATH, collectionID, fileID);
        this.unencryptedBytes = unencryptedBytes;
        this.checksumSpec = checksumData.getChecksumSpec();
        this.expectedChecksum = new String(checksumData.getChecksumValue(), Charset.defaultCharset());
        this.receivedTimestamp = receivedTimestamp;
        this.dao = dao;
        this.crypto = crypto;
    }

    /**
     * The main method that is called to handle the PutFile operation.
     */
    public void performPutFile() throws MismatchingChecksumsException {
        if (fileExists(ENCRYPTED_FILES_PATH, collectionID, fileID)) {
            log.debug("Using existing encrypted file");

            try {
                BasicFileAttributes attributes = Files.readAttributes(encryptedFilePath, BasicFileAttributes.class);
                OffsetDateTime encryptedTimestamp = OffsetDateTime.ofInstant(attributes.creationTime().toInstant(), ZoneId.systemDefault());
                handleEncryptedFileExists(encryptedTimestamp);
            } catch (IOException e) {
                log.error("An error occurred when trying to read attributes from path {}", unencryptedFilePath);
            }
        } else if (fileExists(UNENCRYPTED_FILES_PATH, collectionID, fileID)) {
            log.debug("Using existing unencrypted file");
            handleUnencryptedFile();
        } else {
            if (writeBytesToFile(unencryptedBytes, UNENCRYPTED_FILES_PATH, collectionID, fileID)) {
                handleUnencryptedFile();
            }
        }
    }

    /**
     * This method handles the process that happens after a local unencrypted file exists.
     * <p/>
     * The checksum of the local file is compared to the expected checksum.
     * If the checksums do not match, an exception is thrown. If the checksums match the data
     * of the unencrypted file (as a byte[]) is delegated to the
     * {@link #encryptAndCompareChecksums(byte[])} method.
     */
    private void handleUnencryptedFile() throws MismatchingChecksumsException {
        byte[] unencryptedFileData = readBytesFromFile(unencryptedFilePath);

        if (!compareChecksums(unencryptedFileData, checksumSpec, expectedChecksum)) {
            log.error("Checksums of unencrypted file did not match. Try PutFile again.");
            deleteFileLocally(unencryptedFilePath);
            throw new MismatchingChecksumsException("Checksums of unencrypted file did not match. Try PutFile again.");
        } else {
            encryptAndCompareChecksums(unencryptedFileData);
        }
    }

    /**
     * This method is called when an encrypted file exists locally, but
     * was not created in this run of the {@link #performPutFile()}.
     * Given an encrypted timestamp it calls
     * {@link #assertEncryptedChecksumMatch(OffsetDateTime)}
     * to further ensure the encryption is correct, and update the local database.
     *
     * @param encryptedTimestamp The created timestamp of the encrypted local file.
     */
    private void handleEncryptedFileExists(OffsetDateTime encryptedTimestamp) throws MismatchingChecksumsException {
        assertEncryptedChecksumMatch(encryptedTimestamp);
        handleStateAndJobDoneHandler();
    }

    /**
     * Encrypts the given byte[] data using the given CryptoStrategy, then proceeds to write the encrypted bytes to a local file.
     * Once the file has been written, the method {@link #handleEncryptedFileExists(OffsetDateTime enryptedTimestamp)} is called.
     *
     * @param unencryptedFileData The unencrypted data, read from a file, as a byte[].
     */
    private void encryptAndCompareChecksums(byte[] unencryptedFileData) throws MismatchingChecksumsException {
        byte[] encryptedBytes = crypto.encrypt(unencryptedFileData);
        boolean encryptedFileCreated = writeBytesToFile(encryptedBytes, ENCRYPTED_FILES_PATH, collectionID, fileID);

        if (encryptedFileCreated) {
            OffsetDateTime encryptedTimestamp = OffsetDateTime.now(Clock.systemUTC());
            handleEncryptedFileExists(encryptedTimestamp);
        }
    }

    /**
     * Compares the checksums of the decrypted bytes with the expected checksum to ensure correct encryption.
     * If the checksums match, then a checksum of the encrypted data will be generated, together with a timestamp.
     * <p/>
     * Afterwards the local database will be updated to include information about the file and the encryption parameters.
     *
     * @param encryptedTimestamp The timestamp for when the file was encrypted.
     */
    private void assertEncryptedChecksumMatch(OffsetDateTime encryptedTimestamp) throws MismatchingChecksumsException {
        byte[] encryptedFileData = readBytesFromFile(encryptedFilePath);

        if (compareChecksums(crypto.decrypt(encryptedFileData), checksumSpec, expectedChecksum)) {
            String encryptedChecksum = generateChecksum(new ByteArrayInputStream(encryptedFileData), checksumSpec);
            OffsetDateTime checksumTimestamp = OffsetDateTime.now(Clock.systemUTC());

            updateLocalDatabase(encryptedTimestamp, encryptedChecksum, checksumTimestamp);
        } else {
            log.error("Checksum of encrypted file did not match. Try PutFile again.");
            deleteFileLocally(encryptedFilePath);
            throw new MismatchingChecksumsException("Checksum of encrypted file did not match. Try PutFile again.");
        }
    }

    /**
     * Updates the local database to include the information about the file, for easy access and decrypting at a later state by storing
     * the encryption parameters.
     *
     * @param encryptedTimestamp The timestamp of when the file was encrypted.
     * @param encryptedChecksum  The checksum of the encrypted file.
     * @param checksumTimestamp  The timestamp of when the encrypted checksum was successfully compared.
     */
    private void updateLocalDatabase(OffsetDateTime encryptedTimestamp, String encryptedChecksum, OffsetDateTime checksumTimestamp) {
        //FIXME: Only do this after we know the data is added to the pillar (?)
        dao.insertIntoEncParams(collectionID, fileID, crypto.getSalt(), crypto.getIV().getIV(), crypto.getIterations());
        dao.insertIntoFiles(collectionID, fileID, receivedTimestamp, encryptedTimestamp, expectedChecksum, encryptedChecksum,
                checksumTimestamp);
        log.debug("Local database updated to include: {}/{}", collectionID, fileID);
    }

    /**
     * Computes the checksum from byte[] and compare it to some expected checksum.
     *
     * @param bytesFromFile    Byte[] to compute checksum of.
     * @param checksumSpec     The checksum Spec, used when creating a checksum.
     * @param expectedChecksum The expected checksum.
     * @return Returns true if the two checksums match.
     */
    private boolean compareChecksums(byte[] bytesFromFile, ChecksumSpecTYPE checksumSpec, String expectedChecksum) {
        String newChecksum = generateChecksum(new ByteArrayInputStream(bytesFromFile), checksumSpec);

        return newChecksum.equals(expectedChecksum);
    }

    private void handleStateAndJobDoneHandler() {
        // TODO: Implement JobDoneHandler (sending encrypted file to encrypted pillar) and state updates
    }
}

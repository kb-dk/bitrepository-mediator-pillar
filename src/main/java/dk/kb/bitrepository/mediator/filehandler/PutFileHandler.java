package dk.kb.bitrepository.mediator.filehandler;

import dk.kb.bitrepository.mediator.crypto.AESCryptoStrategy;
import dk.kb.bitrepository.mediator.database.DatabaseDAO;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.time.Clock;
import java.time.OffsetDateTime;

import static dk.kb.bitrepository.mediator.filehandler.FileUtils.*;
import static dk.kb.bitrepository.mediator.utils.configurations.ConfigConstants.ENCRYPTED_FILES_PATH;
import static dk.kb.bitrepository.mediator.utils.configurations.ConfigConstants.UNENCRYPTED_FILES_PATH;
import static org.bitrepository.common.utils.ChecksumUtils.generateChecksum;

public class PutFileHandler {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final DatabaseDAO dao;
    private final AESCryptoStrategy aes;
    private final String collectionID;
    private final String fileID;
    private final byte[] unencryptedBytes;
    private final ChecksumDataForFileTYPE checksumData;
    private final OffsetDateTime receivedTimestamp;

    public PutFileHandler(String collectionID, String fileID, byte[] unencryptedBytes, ChecksumDataForFileTYPE checksumData,
                          OffsetDateTime receivedTimestamp, DatabaseDAO dao, String cryptoPassword) {
        this.collectionID = collectionID;
        this.fileID = fileID;
        this.unencryptedBytes = unencryptedBytes;
        this.checksumData = checksumData;
        this.receivedTimestamp = receivedTimestamp;
        this.dao = dao;
        // FIXME: Reusing same instance of PutFileHandler will NOT create new salt and IV.
        //  is there another way to store this info, maybe in STATE?
        this.aes = new AESCryptoStrategy(cryptoPassword);
    }

    /**
     * The main method that is called to handle the PutFile operation.
     */
    public void performPutFile() {
        if (fileExists(ENCRYPTED_FILES_PATH, collectionID, fileID)) {
            log.debug("Using existing encrypted file");
            handleEncryptedFileAlreadyExists();
        } else if (fileExists(UNENCRYPTED_FILES_PATH, collectionID, fileID)) {
            log.debug("Using existing unencrypted file");
            handleUnencryptedFile();
        } else {
            if (writeBytesToFile(unencryptedBytes, UNENCRYPTED_FILES_PATH, collectionID, fileID)) {
                handleUnencryptedFile();
            }
        }
    }

    private void handleUnencryptedFile() {
        String expectedChecksum = new String(checksumData.getChecksumValue(), Charset.defaultCharset());
        Path unencryptedFilePath = getFilePath(UNENCRYPTED_FILES_PATH, collectionID, fileID);
        byte[] unencryptedFileData = readBytesFromFile(unencryptedFilePath);

        if (!compareChecksums(unencryptedFileData, checksumData.getChecksumSpec(), expectedChecksum)) {
            log.error("Checksums did not match. Try PutFile again.");
            deleteFileLocally(unencryptedFilePath);
            //TODO: Throw checksums-did-not-match-exception
        } else {
            encryptAndCompareChecksums(unencryptedFileData);
        }
    }

    private void handleEncryptedFileAlreadyExists() {
        String expectedChecksum = new String(checksumData.getChecksumValue(), Charset.defaultCharset());
        OffsetDateTime encryptedTimestamp = OffsetDateTime.now(Clock.systemUTC());
        assertEncryptedChecksumAndUpdateLocalDatabase(collectionID, fileID, expectedChecksum, checksumData.getChecksumSpec(),
                receivedTimestamp, encryptedTimestamp);
        handleStateAndJobDoneHandler();
    }

    private void encryptAndCompareChecksums(byte[] unencryptedFileData) {
        byte[] encryptedBytes = aes.encrypt(unencryptedFileData);
        OffsetDateTime encryptedTimestamp = OffsetDateTime.now(Clock.systemUTC());

        boolean encryptedFileCreated = writeBytesToFile(encryptedBytes, ENCRYPTED_FILES_PATH, collectionID, fileID);

        if (encryptedFileCreated) {
            String expectedChecksum = new String(checksumData.getChecksumValue(), Charset.defaultCharset());
            assertEncryptedChecksumAndUpdateLocalDatabase(collectionID, fileID, expectedChecksum, checksumData.getChecksumSpec(),
                    receivedTimestamp, encryptedTimestamp);
            handleStateAndJobDoneHandler();
        }
    }

    private void assertEncryptedChecksumAndUpdateLocalDatabase(String collectionID, String fileID, String expectedChecksum,
                                                               ChecksumSpecTYPE checksumSpec, OffsetDateTime receivedTimestamp,
                                                               OffsetDateTime encryptedTimestamp) {
        Path encryptedFilePath = getFilePath(ENCRYPTED_FILES_PATH, collectionID, fileID);
        byte[] encryptedFileData = readBytesFromFile(encryptedFilePath);

        if (compareChecksums(aes.decrypt(encryptedFileData), checksumSpec, expectedChecksum)) {
            String encryptedChecksum = generateChecksum(new ByteArrayInputStream(encryptedFileData), checksumSpec);
            OffsetDateTime checksumTimestamp = OffsetDateTime.now(Clock.systemUTC());

            dao.insertIntoEncParams(collectionID, fileID, aes.getSalt(), aes.getIV().getIV(), aes.getIterations());
            dao.insertIntoFiles(collectionID, fileID, receivedTimestamp, encryptedTimestamp, expectedChecksum, encryptedChecksum,
                    checksumTimestamp);
            log.debug("Local database updated to include: {}/{}", collectionID, fileID);
        } else {
            log.error("Checksum of encrypted file did not match. Try PutFile again.");
            deleteFileLocally(encryptedFilePath);
            //TODO: Throw checksums-did-not-match-exception
        }
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

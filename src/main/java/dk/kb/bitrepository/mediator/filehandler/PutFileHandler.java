package dk.kb.bitrepository.mediator.filehandler;

import dk.kb.bitrepository.mediator.MediatorPillarComponentFactory;
import dk.kb.bitrepository.mediator.crypto.CryptoStrategy;
import dk.kb.bitrepository.mediator.database.DatabaseDAO;
import dk.kb.bitrepository.mediator.filehandler.exception.MismatchingChecksumsException;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.commandline.eventhandler.CompleteEventAwaiter;
import org.bitrepository.commandline.eventhandler.PutFileEventHandler;
import org.bitrepository.commandline.output.DefaultOutputHandler;
import org.bitrepository.commandline.output.OutputHandler;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.modify.ModifyComponentFactory;
import org.bitrepository.modify.putfile.PutFileClient;
import org.bitrepository.protocol.security.SecurityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.spec.IvParameterSpec;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;

import static dk.kb.bitrepository.mediator.MediatorPillarComponentFactory.getSecurityManager;
import static dk.kb.bitrepository.mediator.database.DatabaseConstants.ENC_PARAMS_TABLE;
import static dk.kb.bitrepository.mediator.database.DatabaseData.EncryptedParametersData;
import static dk.kb.bitrepository.mediator.filehandler.FileUtils.*;
import static dk.kb.bitrepository.mediator.utils.configurations.ConfigConstants.ENCRYPTED_FILES_PATH;
import static dk.kb.bitrepository.mediator.utils.configurations.ConfigConstants.UNENCRYPTED_FILES_PATH;
import static org.bitrepository.common.utils.ChecksumUtils.generateChecksum;

public class PutFileHandler {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final JobContext context;
    private final String collectionID;
    private final String fileID;
    private final Path unencryptedFilePath;
    private final Path encryptedFilePath;
    private final byte[] fileBytes;
    private final ChecksumSpecTYPE checksumSpec;
    private final String expectedChecksum;
    private final OffsetDateTime receivedTimestamp;
    private final DatabaseDAO dao;
    private final CryptoStrategy crypto;
    private final ChecksumDataForFileTYPE checksumDataForFileTYPE;
    private String encryptedChecksum;
    private OffsetDateTime encryptedTimestamp;

    public PutFileHandler(JobContext context, OffsetDateTime receivedTimestamp) {
        this.context = context;
        this.collectionID = context.getCollectionID();
        this.fileID = context.getFileID();
        this.unencryptedFilePath = createFilePath(UNENCRYPTED_FILES_PATH, collectionID, fileID);
        this.encryptedFilePath = createFilePath(ENCRYPTED_FILES_PATH, collectionID, fileID);
        this.fileBytes = context.getFileBytes();
        this.checksumDataForFileTYPE = context.getChecksumDataForFileTYPE();
        this.checksumSpec = context.getChecksumDataForFileTYPE().getChecksumSpec();
        this.expectedChecksum = Base16Utils.decodeBase16(context.getChecksumDataForFileTYPE().getChecksumValue());
        this.receivedTimestamp = receivedTimestamp;
        this.crypto = context.getCrypto();
        this.dao = MediatorPillarComponentFactory.getDAO();
    }

    /**
     * The main method that is called to handle the PutFile operation.
     */
    public void performPutFile() throws MismatchingChecksumsException {
        if (fileExists(ENCRYPTED_FILES_PATH, collectionID, fileID)) {
            updateCryptoParameters();
            log.debug("Using existing encrypted file");

            try {
                BasicFileAttributes attributes = Files.readAttributes(encryptedFilePath, BasicFileAttributes.class);
                encryptedTimestamp = OffsetDateTime.ofInstant(attributes.creationTime().toInstant(), ZoneId.systemDefault());
                setCalculationTimestamp();
                assertEncryptedChecksumMatch();
            } catch (IOException e) {
                log.error("An error occurred when trying to read attributes from path {}", unencryptedFilePath);
            }
        } else if (fileExists(UNENCRYPTED_FILES_PATH, collectionID, fileID)) {
            log.debug("Using existing unencrypted file");
            handleUnencryptedFile();
        } else {
            log.debug("No local files found so creating it");
            if (writeBytesToFile(fileBytes, UNENCRYPTED_FILES_PATH, collectionID, fileID)) {
                handleUnencryptedFile();
            }
        }

        if (putFileOnPillar().getEventType() == OperationEvent.OperationEventType.COMPLETE) {
            //TODO: Let JobScheduler know status
            log.info("File Put Successfully");
            updateLocalDatabase();
            handleStateAndJobDoneHandler();
        } else {
            //TODO: AuditTrail + Alarm
            log.error("File was not put");
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
            log.error("Checksums of unencrypted file did not match. Try 'PutFile' operation again.");
            deleteFileLocally(unencryptedFilePath);
            throw new MismatchingChecksumsException();
        } else {
            encryptAndCompareChecksums(unencryptedFileData);
        }
    }

    /**
     * Encrypts the given byte[] data using the given CryptoStrategy, then proceeds to write the encrypted bytes to a local file.
     * Once the file has been written, the method {@link #assertEncryptedChecksumMatch()} is called.
     *
     * @param unencryptedFileData The unencrypted data, read from a file, as a byte[].
     */
    private void encryptAndCompareChecksums(byte[] unencryptedFileData) throws MismatchingChecksumsException {
        byte[] encryptedBytes = crypto.encrypt(unencryptedFileData);
        boolean encryptedFileCreated = writeBytesToFile(encryptedBytes, ENCRYPTED_FILES_PATH, collectionID, fileID);

        if (encryptedFileCreated) {
            encryptedTimestamp = OffsetDateTime.now(Clock.systemUTC());
            setCalculationTimestamp();
            assertEncryptedChecksumMatch();
        }
    }

    /**
     * Compares the checksums of the decrypted bytes with the expected checksum to ensure correct encryption.
     * If the checksums match, then a checksum of the encrypted data will be generated, together with a timestamp.
     */
    private void assertEncryptedChecksumMatch() throws MismatchingChecksumsException {
        byte[] encryptedFileData = readBytesFromFile(encryptedFilePath);

        if (compareChecksums(crypto.decrypt(encryptedFileData), checksumSpec, expectedChecksum)) {
            encryptedChecksum = generateChecksum(new ByteArrayInputStream(encryptedFileData), checksumSpec);
            checksumDataForFileTYPE.setChecksumValue(Base16Utils.encodeBase16(encryptedChecksum));
        } else {
            log.error("Checksum of encrypted file did not match. Try PutFile again.");
            deleteFileLocally(encryptedFilePath);
            throw new MismatchingChecksumsException("Checksum of encrypted file did not match. Try PutFile again.");
        }
    }

    private OperationEvent putFileOnPillar() {
        Settings settings = context.getSettings();
        String auditTrailInformation = "AuditTrailInfo for PutFileHandler.";
        SecurityManager securityManager = getSecurityManager();
        OutputHandler output = new DefaultOutputHandler(getClass());
        boolean printChecksums = false;
        CompleteEventAwaiter eventHandler = new PutFileEventHandler(settings, output, printChecksums);

        log.debug("Attempting to put file on FileExchange.");
        URL fileURL = context.getFileExchange().putFile(new File(encryptedFilePath.toString()));

        PutFileClient client = ModifyComponentFactory.getInstance().retrievePutClient(settings, securityManager, settings.getComponentID());
        client.putFile(collectionID, fileURL, fileID, getFileSize(encryptedFilePath), checksumDataForFileTYPE, checksumSpec, eventHandler,
                auditTrailInformation);

        return eventHandler.getFinish();
    }

    /**
     * Sets the Calculation Timestamp property of the checksumDataForFileTYPE object as type XMLGregorianCalendar.
     */
    private void setCalculationTimestamp() {
        checksumDataForFileTYPE.setCalculationTimestamp(
                CalendarUtils.getXmlGregorianCalendar(new Date(encryptedTimestamp.toInstant().toEpochMilli())));
    }

    /**
     * Updates the encryption parameters, so that they match the ones used to encrypt the file that is currently being worked on.
     */
    private void updateCryptoParameters() {
        EncryptedParametersData paramData = (EncryptedParametersData) dao.select(collectionID, fileID, ENC_PARAMS_TABLE);
        crypto.setSalt(paramData.getSalt());
        crypto.setIV(new IvParameterSpec(paramData.getIv()));
    }

    /**
     * Updates the local database to include the information about the file, for easy access and decrypting at a later state by storing
     * the encryption parameters.
     */
    private void updateLocalDatabase() {
        dao.insertIntoEncParams(collectionID, fileID, crypto.getSalt(), crypto.getIV().getIV(), crypto.getIterations());
        dao.insertIntoFiles(collectionID, fileID, receivedTimestamp, encryptedTimestamp, expectedChecksum, encryptedChecksum,
                OffsetDateTime.now(Clock.systemUTC()));
        log.debug("Local database updated to include: {}/{}", collectionID, fileID);
    }

    private void handleStateAndJobDoneHandler() {
        // TODO: Implement JobDoneHandler (sending encrypted file to encrypted pillar) and state updates
    }
}

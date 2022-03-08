package dk.kb.bitrepository.mediator.filehandler;

import dk.kb.bitrepository.mediator.crypto.CryptoStrategy;
import dk.kb.bitrepository.mediator.database.DatabaseDAO;
import dk.kb.bitrepository.mediator.filehandler.context.PutFileContext;
import dk.kb.bitrepository.mediator.filehandler.exception.MismatchingChecksumsException;
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
import org.bitrepository.protocol.FileExchange;
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

import static dk.kb.bitrepository.mediator.MediatorPillarComponentFactory.*;
import static dk.kb.bitrepository.mediator.database.DatabaseData.EncryptedParametersData;
import static dk.kb.bitrepository.mediator.filehandler.FileUtils.*;
import static dk.kb.bitrepository.mediator.utils.configurations.ConfigConstants.ENCRYPTED_FILES_PATH;
import static dk.kb.bitrepository.mediator.utils.configurations.ConfigConstants.UNENCRYPTED_FILES_PATH;
import static org.bitrepository.common.utils.ChecksumUtils.generateChecksum;

public class PutFileHandler {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final PutFileContext context;
    private final Path unencryptedFilePath;
    private final Path encryptedFilePath;
    private final String expectedChecksum;
    private final CryptoStrategy crypto;
    private String encryptedChecksum;

    public PutFileHandler(PutFileContext context) {
        this.context = context;
        this.unencryptedFilePath = createFilePath(UNENCRYPTED_FILES_PATH, context.getCollectionID(), context.getFileID());
        this.encryptedFilePath = createFilePath(ENCRYPTED_FILES_PATH, context.getCollectionID(), context.getFileID());
        this.expectedChecksum = Base16Utils.decodeBase16(context.getChecksumDataForFileTYPE().getChecksumValue());
        this.crypto = context.getCrypto();
    }

    /**
     * Performs the PutFile operation.
     */
    public void performPutFile() throws MismatchingChecksumsException {
        DatabaseDAO dao = getDAO();
        if (fileExists(ENCRYPTED_FILES_PATH, context.getCollectionID(), context.getFileID())) {
            updateCryptoParameters(dao);
            log.debug("Using existing encrypted file");

            try {
                BasicFileAttributes attributes = Files.readAttributes(encryptedFilePath, BasicFileAttributes.class);
                setCalculatedChecksumTimestamp(OffsetDateTime.ofInstant(attributes.creationTime().toInstant(), ZoneId.systemDefault()));
                assertEncryptedChecksumMatch();
            } catch (IOException e) {
                log.error("An error occurred when trying to read attributes from path {}", unencryptedFilePath);
            }
        } else if (fileExists(UNENCRYPTED_FILES_PATH, context.getCollectionID(), context.getFileID())) {
            log.debug("Using existing unencrypted file");
            handleUnencryptedFile();
        } else {
            log.debug("Creating file locally");
            if (writeBytesToFile(context.getFileBytes(), UNENCRYPTED_FILES_PATH, context.getCollectionID(), context.getFileID())) {
                handleUnencryptedFile();
            }
        }

        if (putFileOnPillar().getEventType() == OperationEvent.OperationEventType.COMPLETE) {
            //TODO: Let JobScheduler know status
            log.info("File Put Successfully");
            updateLocalDatabase(dao);
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

        if (!compareChecksums(unencryptedFileData, context.getChecksumDataForFileTYPE().getChecksumSpec(), expectedChecksum)) {
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
        boolean encryptedFileCreated = writeBytesToFile(encryptedBytes, ENCRYPTED_FILES_PATH, context.getCollectionID(),
                context.getFileID());

        if (encryptedFileCreated) {
            setCalculatedChecksumTimestamp(OffsetDateTime.now(Clock.systemUTC()));
            assertEncryptedChecksumMatch();
        }
    }

    /**
     * Compares the checksums of the decrypted bytes with the expected checksum to ensure correct encryption.
     * If the checksums match, then a checksum of the encrypted data will be generated, together with a timestamp.
     */
    private void assertEncryptedChecksumMatch() throws MismatchingChecksumsException {
        byte[] encryptedFileData = readBytesFromFile(encryptedFilePath);

        if (compareChecksums(crypto.decrypt(encryptedFileData), context.getChecksumDataForFileTYPE().getChecksumSpec(), expectedChecksum)) {
            encryptedChecksum = generateChecksum(new ByteArrayInputStream(encryptedFileData),
                    context.getChecksumDataForFileTYPE().getChecksumSpec());
            context.getChecksumDataForFileTYPE().setChecksumValue(Base16Utils.encodeBase16(encryptedChecksum));
        } else {
            log.error("Checksum of encrypted file did not match. Try PutFile again.");
            deleteFileLocally(encryptedFilePath);
            throw new MismatchingChecksumsException("Checksum of encrypted file did not match. Try PutFile again.");
        }
    }

    /**
     * Puts the file on the File Exchange and contacts the pillar to have it pull the file from File Exchange to its own storage.
     *
     * @return The finishing event of the EventHandler. That is whether the operation is COMPLETE or FAILED.
     */
    private OperationEvent putFileOnPillar() {
        Settings settings = context.getSettings();
        String auditTrailInformation = "AuditTrailInfo for PutFileHandler.";
        SecurityManager securityManager = getSecurityManager();
        OutputHandler output = new DefaultOutputHandler(getClass());
        boolean printChecksums = false;
        CompleteEventAwaiter eventHandler = new PutFileEventHandler(settings, output, printChecksums);

        log.debug("Attempting to put file on FileExchange.");
        FileExchange fileExchange = getInstance().getFileExchange(settings);
        URL fileURL = fileExchange.putFile(new File(encryptedFilePath.toString()));

        PutFileClient client = ModifyComponentFactory.getInstance().retrievePutClient(settings, securityManager, settings.getComponentID());
        client.putFile(context.getCollectionID(), fileURL, context.getFileID(), getFileSize(encryptedFilePath),
                context.getChecksumDataForFileTYPE(), context.getChecksumDataForFileTYPE().getChecksumSpec(), eventHandler,
                auditTrailInformation);

        return eventHandler.getFinish();
    }

    /**
     * Sets the Calculation Timestamp property of the checksumDataForFileTYPE object as type XMLGregorianCalendar.
     */
    private void setCalculatedChecksumTimestamp(OffsetDateTime encryptedTimestamp) {
        context.getChecksumDataForFileTYPE()
                .setCalculationTimestamp(CalendarUtils.getXmlGregorianCalendar(new Date(encryptedTimestamp.toInstant().toEpochMilli())));
    }

    /**
     * Updates the encryption parameters, so that they match the ones used to encrypt the file that is currently being worked on.
     */
    private void updateCryptoParameters(DatabaseDAO dao) {
        EncryptedParametersData paramData = dao.getEncParams(context.getCollectionID(), context.getFileID());
        crypto.setSalt(paramData.getSalt());
        crypto.setIV(new IvParameterSpec(paramData.getIv()));
    }

    /**
     * Updates the local database to include the information about the file, for easy access and decrypting at a later state by storing
     * the encryption parameters.
     */
    private void updateLocalDatabase(DatabaseDAO dao) {
        dao.insertIntoEncParams(context.getCollectionID(), context.getFileID(), crypto.getSalt(), crypto.getIV().getIV(),
                crypto.getIterations());
        OffsetDateTime encryptedTimestamp = context.getChecksumDataForFileTYPE().getCalculationTimestamp().toGregorianCalendar()
                .toZonedDateTime().toOffsetDateTime();
        dao.insertIntoFiles(context.getCollectionID(), context.getFileID(), context.getReceivedTimestamp(), encryptedTimestamp,
                expectedChecksum, encryptedChecksum, OffsetDateTime.now(Clock.systemUTC()));
    }

    private void handleStateAndJobDoneHandler() {
        // TODO: Implement JobDoneHandler (sending encrypted file to encrypted pillar) and state updates
    }
}

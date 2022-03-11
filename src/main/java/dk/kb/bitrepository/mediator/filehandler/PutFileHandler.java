package dk.kb.bitrepository.mediator.filehandler;

import dk.kb.bitrepository.mediator.database.DatabaseDAO;
import dk.kb.bitrepository.mediator.filehandler.context.PutFileContext;
import dk.kb.bitrepository.mediator.filehandler.exception.MismatchingChecksumsException;
import org.bitrepository.client.eventhandler.OperationEvent;
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
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Date;

import static dk.kb.bitrepository.mediator.MediatorPillarComponentFactory.getInstance;
import static dk.kb.bitrepository.mediator.MediatorPillarComponentFactory.getSecurityManager;
import static dk.kb.bitrepository.mediator.filehandler.FileUtils.*;
import static dk.kb.bitrepository.mediator.utils.configurations.ConfigConstants.ENCRYPTED_FILES_PATH;
import static dk.kb.bitrepository.mediator.utils.configurations.ConfigConstants.UNENCRYPTED_FILES_PATH;
import static org.bitrepository.common.utils.ChecksumUtils.generateChecksum;

public class PutFileHandler extends OperationHandler<PutFileContext> {
    private String encryptedChecksum;
    private OffsetDateTime receivedTimestamp;

    public PutFileHandler(PutFileContext context) {
        super(context, context.getCrypto(), Base16Utils.decodeBase16(context.getChecksumDataForFileTYPE().getChecksumValue()));
        log = LoggerFactory.getLogger(this.getClass());
    }

    /**
     * Perform the PutFile operation.
     *
     * @throws MismatchingChecksumsException Throws an exception if the checksum of the file to work on does not match.
     */
    public void performOperation() throws MismatchingChecksumsException {
        switch (checkLocalStorageForFile(unencryptedFilePath, encryptedFilePath)) {
            case ENCRYPTED:
                try {
                    updateCryptoParameters(context.getCollectionID(), context.getFileID(), dao);
                    assertEncryptedChecksumMatch(readFileCreationDate(encryptedFilePath));
                } catch (IOException e) {
                    log.error("An error occurred when trying to read attributes from path {}", unencryptedFilePath);
                }
                break;
            case UNENCRYPTED:
                handleUnencryptedFile();
                break;
            case NONE:
                if (writeBytesToFile(context.getFileBytes(), UNENCRYPTED_FILES_PATH, context.getCollectionID(), context.getFileID())) {
                    log.debug("Using newly created local file");
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
        if (compareChecksums(unencryptedFileData, context.getChecksumDataForFileTYPE().getChecksumSpec(), expectedChecksum)) {
            encryptAndCompareChecksums(unencryptedFileData);
        } else {
            log.error("Checksums of unencrypted file did not match. Try 'PutFile' operation again.");
            deleteFileLocally(unencryptedFilePath);
            throw new MismatchingChecksumsException();
        }
    }

    /**
     * Encrypts the given byte[] data using the given CryptoStrategy, then proceeds to write the encrypted bytes to a local file.
     * Once the file has been written, the method {@link #assertEncryptedChecksumMatch(OffsetDateTime)} is called.
     *
     * @param unencryptedFileData The unencrypted data, read from a file, as a byte[].
     */
    private void encryptAndCompareChecksums(byte[] unencryptedFileData) throws MismatchingChecksumsException {
        byte[] encryptedBytes = crypto.encrypt(unencryptedFileData);
        boolean encryptedFileCreated = writeBytesToFile(encryptedBytes, ENCRYPTED_FILES_PATH, context.getCollectionID(),
                context.getFileID());

        if (encryptedFileCreated) {
            assertEncryptedChecksumMatch(OffsetDateTime.now(Clock.systemUTC()));
        }
    }

    /**
     * Compares the checksums of the decrypted bytes with the expected checksum to ensure correct encryption.
     * If the checksums match, then a checksum of the encrypted data will be generated, together with a timestamp.
     */
    private void assertEncryptedChecksumMatch(OffsetDateTime checksumCalculatedTimestamp) throws MismatchingChecksumsException {
        byte[] encryptedFileData = readBytesFromFile(encryptedFilePath);
        if (compareChecksums(crypto.decrypt(encryptedFileData), context.getChecksumDataForFileTYPE().getChecksumSpec(), expectedChecksum)) {
            log.debug("Checksums Matched");
            encryptedChecksum = generateChecksum(new ByteArrayInputStream(encryptedFileData),
                    context.getChecksumDataForFileTYPE().getChecksumSpec());
            context.getChecksumDataForFileTYPE().setChecksumValue(Base16Utils.encodeBase16(encryptedChecksum));
            setCalculatedChecksumTimestamp(checksumCalculatedTimestamp);
            receivedTimestamp = checksumCalculatedTimestamp;
            waitForPillarToHandleRequest();
        } else {
            log.error("Checksum of encrypted file did not match. Try PutFile again.");
            deleteFileLocally(encryptedFilePath);
            throw new MismatchingChecksumsException();
        }
    }

    /**
     * Runs the {@link #putFileOnPillar()} method, and awaits a completion event.
     */
    private void waitForPillarToHandleRequest() {
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
        eventHandler = new PutFileEventHandler(settings, output, printChecksums);

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
     * Sets the Calculation Timestamp property of the {@link org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE}
     * object as type {@link javax.xml.datatype.XMLGregorianCalendar}.
     */
    private void setCalculatedChecksumTimestamp(OffsetDateTime encryptedTimestamp) {
        context.getChecksumDataForFileTYPE()
                .setCalculationTimestamp(CalendarUtils.getXmlGregorianCalendar(new Date(encryptedTimestamp.toInstant().toEpochMilli())));
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
        dao.insertIntoFiles(context.getCollectionID(), context.getFileID(), receivedTimestamp, encryptedTimestamp, expectedChecksum,
                encryptedChecksum, OffsetDateTime.now(Clock.systemUTC()));
    }

    protected void handleStateAndJobDoneHandler() {
        // TODO: Implement JobDoneHandler (sending encrypted file to encrypted pillar) and state updates
    }
}

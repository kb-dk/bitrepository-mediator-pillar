package dk.kb.bitrepository.mediator.filehandler;

import dk.kb.bitrepository.mediator.crypto.CryptoStrategy;
import org.bitrepository.access.AccessComponentFactory;
import org.bitrepository.access.getfile.GetFileClient;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.commandline.eventhandler.CompleteEventAwaiter;
import org.bitrepository.commandline.eventhandler.GetFileEventHandler;
import org.bitrepository.commandline.output.DefaultOutputHandler;
import org.bitrepository.commandline.output.OutputHandler;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.protocol.FileExchange;
import org.bitrepository.protocol.security.SecurityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Path;

import static dk.kb.bitrepository.mediator.MediatorPillarComponentFactory.getSecurityManager;
import static dk.kb.bitrepository.mediator.filehandler.FileUtils.*;
import static dk.kb.bitrepository.mediator.utils.configurations.ConfigConstants.ENCRYPTED_FILES_PATH;
import static dk.kb.bitrepository.mediator.utils.configurations.ConfigConstants.UNENCRYPTED_FILES_PATH;
import static org.bitrepository.client.eventhandler.OperationEvent.OperationEventType;

public class GetFileHandler {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final JobContext context;
    private final Path unencryptedFilePath;
    private final Path encryptedFilePath;
    private final ChecksumDataForFileTYPE checksumData;
    private final CryptoStrategy crypto;
    private final FileExchange fileExchange;
    private CompleteEventAwaiter eventHandler;

    public GetFileHandler(JobContext context) {
        this.context = context;
        this.unencryptedFilePath = createFilePath(UNENCRYPTED_FILES_PATH, context.getCollectionID(), context.getFileID());
        this.encryptedFilePath = createFilePath(ENCRYPTED_FILES_PATH, context.getCollectionID(), context.getFileID());
        this.checksumData = context.getChecksumDataForFileTYPE();
        this.crypto = context.getCrypto();
        this.fileExchange = context.getFileExchange();
    }

    public void performGetFile() {
        log.debug("Attempting to find file locally.");
        File file = checkLocalStorageForFile();
        if (file == null) {
            log.debug("Attempting to get file from pillar.");
            getFileFromPillar();
            if (waitForPillarToHandleRequest()) {
                try {
                    ensureDirectoryExists(createFileDir(ENCRYPTED_FILES_PATH, context.getCollectionID()));
                    fileExchange.getFile(new File(encryptedFilePath.toString()), fileExchange.getURL(context.getFileID()).toString());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                file = decryptAndCreateFile();
            } else {
                log.error("Error occurred on pillar.");
                return;
            }
        }
        if (context.getFilePart() != null) {
            log.debug("Getting file part");
            file = getFilePart(file);
        }
        //TODO: handle the bytes either as a Response or delegate to JobHandler?
    }

    private boolean waitForPillarToHandleRequest() {
        OperationEventType eventType = eventHandler.getFinish().getEventType();

        return eventType.equals(OperationEventType.COMPLETE);
    }

    private File checkLocalStorageForFile() {
        File localFile = null;
        if (fileExists(unencryptedFilePath)) {
            log.debug("Using local unencrypted file.");
            localFile = new File(unencryptedFilePath.toString());

        } else if (fileExists(encryptedFilePath)) {
            log.debug("Using local encrypted file.");
            localFile = decryptAndCreateFile();
        }
        if (localFile != null) {
            String expectedChecksum = Base16Utils.decodeBase16(checksumData.getChecksumValue());
            compareChecksums(readBytesFromFile(unencryptedFilePath), checksumData.getChecksumSpec(), expectedChecksum);
        }
        return localFile;
    }

    /**
     * Decrypts the bytes of file found at the encrypted file path.
     * An unencrypted file is created in the unencrypted file path. This is done by writing the bytes of the file at the encrypted file
     * path to a temp folder, and only moving the file to the unencrypted path once all the bytes has been successfully written to the file.
     *
     * @return A File object from the unencrypted file path. If the file is not written successfully returns null.
     */
    private File decryptAndCreateFile() {
        byte[] encryptedBytes = readBytesFromFile(encryptedFilePath);
        if (writeBytesToFile(crypto.decrypt(encryptedBytes), UNENCRYPTED_FILES_PATH, context.getCollectionID(), context.getFileID())) {
            return new File(unencryptedFilePath.toString());
        }
        return null;
    }

    private void getFileFromPillar() {
        Settings settings = context.getSettings();
        String auditTrailInformation = "AuditTrailInfo for getFileFromPillar.";
        SecurityManager securityManager = getSecurityManager();
        OutputHandler output = new DefaultOutputHandler(getClass());
        eventHandler = new GetFileEventHandler(settings, output);
        GetFileClient client = AccessComponentFactory.getInstance()
                .createGetFileClient(settings, securityManager, settings.getComponentID());

        if (context.getContributors().size() > 1) {
            client.getFileFromFastestPillar(context.getCollectionID(), context.getFileID(), context.getFilePart(),
                    context.getUrlForResult(), eventHandler, auditTrailInformation);
        } else {
            client.getFileFromSpecificPillar(context.getCollectionID(), context.getFileID(), context.getFilePart(),
                    context.getUrlForResult(), context.getContributors().iterator().next(), eventHandler, auditTrailInformation);
        }
    }

    private File getFilePart(File fileBytes) {
        return null;
    }

    private void handleStateAndJobDoneHandler() {
        // TODO: Implement : update state, let JobHandler know the job is done (?)
    }
}

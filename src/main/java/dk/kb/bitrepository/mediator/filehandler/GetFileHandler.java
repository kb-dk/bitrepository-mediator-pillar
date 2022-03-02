package dk.kb.bitrepository.mediator.filehandler;

import dk.kb.bitrepository.mediator.crypto.CryptoStrategy;
import org.bitrepository.access.AccessComponentFactory;
import org.bitrepository.access.getfile.GetFileClient;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.FilePart;
import org.bitrepository.commandline.eventhandler.CompleteEventAwaiter;
import org.bitrepository.commandline.eventhandler.GetFileEventHandler;
import org.bitrepository.commandline.output.DefaultOutputHandler;
import org.bitrepository.commandline.output.OutputHandler;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.FileExchange;
import org.bitrepository.protocol.security.SecurityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
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
        ArgumentValidator.checkNotNull(context.getFileID(), "File ID");
        ArgumentValidator.checkNotNull(context.getChecksumDataForFileTYPE(), "Checksum Data for FileType");
        ArgumentValidator.checkNotNull(context.getFileExchange(), "FileExchange");
        this.context = context;
        this.unencryptedFilePath = getFilePath(UNENCRYPTED_FILES_PATH, context.getCollectionID(), context.getFileID());
        this.encryptedFilePath = getFilePath(ENCRYPTED_FILES_PATH, context.getCollectionID(), context.getFileID());
        this.checksumData = context.getChecksumDataForFileTYPE();
        this.crypto = context.getCrypto();
        this.fileExchange = context.getFileExchange();
    }

    public void performGetFile() {
        FilePart filePart = context.getFilePart();

        log.debug("Attempting to find file locally.");
        byte[] fileBytes = checkLocalStorageForFile();
        if (fileBytes == null) {
            log.debug("Attempting to get file from pillar.");
            getFileFromPillar();
            if (waitForPillarToHandleRequest()) {
                Path filePath = getFilePath(ENCRYPTED_FILES_PATH, context.getCollectionID(), context.getFileID());
                try {
                    fileExchange.getFile(new File(filePath.toString()), fileExchange.getURL(context.getFileID()).toExternalForm());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                fileBytes = readBytesFromFile(filePath);
            } else {
                log.error("Error occurred on pillar.");
                return;
            }
        }
        if (filePart != null) {
            log.debug("Getting file part");
            fileBytes = getFilePart(fileBytes);
        }
        //TODO: handle the bytes either as a Response or delegate to JobHandler?
    }

    private boolean waitForPillarToHandleRequest() {
        OperationEventType eventType = eventHandler.getFinish().getEventType();

        return eventType.equals(OperationEventType.COMPLETE);
    }

    private byte[] checkLocalStorageForFile() {
        byte[] localBytes = null;
        if (fileExists(UNENCRYPTED_FILES_PATH, context.getCollectionID(), context.getFileID())) {
            log.debug("Using local unencrypted file.");
            localBytes = readBytesFromFile(unencryptedFilePath);

        } else if (fileExists(ENCRYPTED_FILES_PATH, context.getCollectionID(), context.getFileID())) {
            //TODO: What if encrypted file is locally, but DAO doesn't have salt and IV yet => can't decrypt
            log.debug("Using local encrypted file.");
            byte[] encryptedBytes = readBytesFromFile(encryptedFilePath);
            localBytes = crypto.decrypt(encryptedBytes);
        }
        if (localBytes != null) {
            if (compareChecksums(localBytes, checksumData.getChecksumSpec(),
                    new String(checksumData.getChecksumValue(), Charset.defaultCharset()))) return localBytes;
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

    private byte[] getFilePart(byte[] fileBytes) {
        return null;
    }

    private void handleStateAndJobDoneHandler() {
        // TODO: Implement : update state, let JobHandler know the job is done (?)
    }
}

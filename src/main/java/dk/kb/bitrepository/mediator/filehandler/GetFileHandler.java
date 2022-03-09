package dk.kb.bitrepository.mediator.filehandler;

import dk.kb.bitrepository.mediator.filehandler.context.GetFileContext;
import org.bitrepository.access.AccessComponentFactory;
import org.bitrepository.access.getfile.GetFileClient;
import org.bitrepository.commandline.eventhandler.GetFileEventHandler;
import org.bitrepository.commandline.output.DefaultOutputHandler;
import org.bitrepository.commandline.output.OutputHandler;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.protocol.FileExchange;
import org.bitrepository.protocol.security.SecurityManager;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;

import static dk.kb.bitrepository.mediator.MediatorPillarComponentFactory.getInstance;
import static dk.kb.bitrepository.mediator.MediatorPillarComponentFactory.getSecurityManager;
import static dk.kb.bitrepository.mediator.filehandler.FileUtils.*;
import static dk.kb.bitrepository.mediator.utils.configurations.ConfigConstants.ENCRYPTED_FILES_PATH;
import static org.bitrepository.client.eventhandler.OperationEvent.OperationEventType;

public class GetFileHandler extends OperationHandler<GetFileContext> {
    public GetFileHandler(GetFileContext context) {
        super(context, context.getCrypto(), Base16Utils.decodeBase16(context.getChecksumDataForFileTYPE().getChecksumValue()));
        log = LoggerFactory.getLogger(this.getClass());
    }

    public void performOperation() {
        log.debug("Attempting to find file locally.");
        File file = checkLocalStorageForFile();
        if (file == null) {
            log.debug("Attempting to get file from pillar.");
            getFileFromPillar();
            if (waitForPillarToHandleRequest()) {
                try {
                    ensureDirectoryExists(createFileDir(ENCRYPTED_FILES_PATH, context.getCollectionID()));
                    FileExchange fileExchange = getInstance().getFileExchange(context.getSettings());
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
            compareChecksums(readBytesFromFile(unencryptedFilePath), context.getChecksumDataForFileTYPE().getChecksumSpec(),
                    expectedChecksum);
        }
        return localFile;
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

    private boolean waitForPillarToHandleRequest() {
        OperationEventType eventType = eventHandler.getFinish().getEventType();

        return eventType.equals(OperationEventType.COMPLETE);
    }

    private File getFilePart(File fileBytes) {
        return null;
    }

    @Override
    protected void handleStateAndJobDoneHandler() {
        // TODO: Implement : update state, let JobHandler know the job is done (?)
    }
}

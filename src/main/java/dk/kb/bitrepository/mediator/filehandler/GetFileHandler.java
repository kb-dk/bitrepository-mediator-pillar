package dk.kb.bitrepository.mediator.filehandler;

import dk.kb.bitrepository.mediator.filehandler.context.GetFileContext;
import org.bitrepository.access.AccessComponentFactory;
import org.bitrepository.access.getfile.GetFileClient;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.commandline.eventhandler.CompleteEventAwaiter;
import org.bitrepository.commandline.eventhandler.GetFileEventHandler;
import org.bitrepository.commandline.output.DefaultOutputHandler;
import org.bitrepository.commandline.output.OutputHandler;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.protocol.FileExchange;
import org.bitrepository.protocol.security.SecurityManager;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.nio.file.Path;

import static dk.kb.bitrepository.mediator.MediatorPillarComponentFactory.getInstance;
import static dk.kb.bitrepository.mediator.MediatorPillarComponentFactory.getSecurityManager;
import static dk.kb.bitrepository.mediator.filehandler.FileUtils.*;
import static dk.kb.bitrepository.mediator.utils.configurations.ConfigConstants.ENCRYPTED_FILES_PATH;

public class GetFileHandler extends OperationHandler<GetFileContext> {
    public GetFileHandler(GetFileContext context) {
        super(context, context.getCrypto(), Base16Utils.decodeBase16(context.getChecksumDataForFileTYPE().getChecksumValue()));
        log = LoggerFactory.getLogger(this.getClass());
    }

    public void performOperation() {
        File file = getLocalFile();
        if (context.getFilePart() != null) {
            log.debug("Getting file part");
            byte[] filePart = getFilePart(file);
            handleStateAndJobDoneHandler(filePart);
        } else {
            handleStateAndJobDoneHandler();
        }
        //TODO: handle the bytes either as a Response or delegate to JobHandler?
    }

    /**
     * Runs {@link FileUtils#checkLocalStorageForFile(Path, Path)} and handles the returned {@link FileUtils.FileStatus} appropriately.
     * <p/>
     * If the files does not exist locally, then {@link #requestFileFromPillar()} is called, to attempt to get the file from an
     * encrypted pillar.
     *
     * @return Returns a {@link File} object that has been created from either the local files or from the received file from the pillar.
     */
    private File getLocalFile() {
        File localFile = null;
        switch (checkLocalStorageForFile(unencryptedFilePath, encryptedFilePath)) {
            case UNENCRYPTED:
                localFile = new File(unencryptedFilePath.toString());
                break;
            case ENCRYPTED:
                localFile = decryptAndGetFile();
                break;
            case NONE:
                localFile = requestFileFromPillar();
        }
        if (localFile != null) {
            compareChecksums(readBytesFromFile(unencryptedFilePath), context.getChecksumDataForFileTYPE().getChecksumSpec(),
                    expectedChecksum);
        }
        return localFile;
    }

    /**
     * Calls {@link #pillarRequestGetFile()} and waits for an answer from the event handler.
     *
     * @return Returns a {@link File} object of the unencrypted version of the file retrieved from the pillar.
     */
    private File requestFileFromPillar() {
        log.debug("Attempting to get file from pillar.");
        ensureDirectoryExists(createFileDirPath(ENCRYPTED_FILES_PATH, context.getCollectionID()));
        if (pillarRequestGetFile().getEventType() == OperationEvent.OperationEventType.COMPLETE) {
            try {
                FileExchange fileExchange = getInstance().getFileExchange(context.getSettings());
                fileExchange.getFile(new File(encryptedFilePath.toString()), fileExchange.getURL(context.getFileID()).toString());
                return decryptAndGetFile();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            //TODO: Let JobScheduler know status
            log.info("File Received Successfully");
        } else {
            //TODO: AuditTrail + Alarm
            log.error("File was not received");
        }
        return null;
    }

    /**
     * Creates a {@link GetFileClient} to communicate with an encrypted pillar.
     * <p/>
     * If more than one pillar is set as contributor, the method chooses the fastest one.
     *
     * @return Returns the {@link OperationEvent} returned by {@link CompleteEventAwaiter#getFinish()}.
     */
    private OperationEvent pillarRequestGetFile() {
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
        return eventHandler.getFinish();
    }

    private byte[] getFilePart(File file) {
        int offset = context.getFilePart().getPartOffSet().intValue();
        int size = context.getFilePart().getPartLength().intValue();
        ByteBuffer bytes = ByteBuffer.allocateDirect(size);

        FileInputStream inputStream;
        try {
            inputStream = new FileInputStream(file);
            inputStream.getChannel().read(bytes, offset);
        } catch (IOException e) {
            log.error("A problem occurred trying to get partial bytes from file: {}", file);
        }

        return bytes.array();
    }

//    private InputStream getFilePart(File file) throws IOException {
//        int offset = context.getFilePart().getPartOffSet().intValue();
//        int size = context.getFilePart().getPartLength().intValue();
//        byte[] partOfFile = new byte[size];
//        InputStream inputStream = null;
//        try {
//            log.debug("Extracting " + size + " bytes with offset " + offset + " from " + context.getFileID());
//            inputStream = new FileInputStream(file);
//            inputStream.read(new byte[offset]);
//            inputStream.read(partOfFile);
//        } finally {
//            if (inputStream != null) {
//                inputStream.close();
//            }
//        }
//        return new ByteArrayInputStream(partOfFile);
//    }

    protected void handleStateAndJobDoneHandler(byte[] filePart) {
        // TODO: Implement : update state, let JobHandler know the job is done (?)
    }

    protected void handleStateAndJobDoneHandler() {
        handleStateAndJobDoneHandler(null);
    }
}

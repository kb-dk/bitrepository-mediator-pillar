package dk.kb.bitrepository.mediator.filehandler;

import dk.kb.bitrepository.mediator.crypto.CryptoStrategy;
import dk.kb.bitrepository.mediator.pillaraccess.AccessPillarFactory;
import dk.kb.bitrepository.mediator.pillaraccess.clients.GetFileClient;
import org.bitrepository.access.getfile.conversation.GetFileConversationContext;
import org.bitrepository.bitrepositoryelements.FilePart;
import org.bitrepository.commandline.eventhandler.CompleteEventAwaiter;
import org.bitrepository.commandline.eventhandler.GetFileEventHandler;
import org.bitrepository.commandline.output.DefaultOutputHandler;
import org.bitrepository.commandline.output.OutputHandler;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.security.SecurityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

import static dk.kb.bitrepository.mediator.filehandler.FileUtils.*;
import static dk.kb.bitrepository.mediator.utils.configurations.ConfigConstants.ENCRYPTED_FILES_PATH;
import static dk.kb.bitrepository.mediator.utils.configurations.ConfigConstants.UNENCRYPTED_FILES_PATH;

public class GetFileHandler {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final GetFileConversationContext context;
    private final FilePart filePart;
    private final Path unencryptedFilePath;
    private final Path encryptedFilePath;
    private final CryptoStrategy crypto;

    public GetFileHandler(GetFileConversationContext context, CryptoStrategy crypto) {
        this.context = context;
        this.filePart = context.getFilePart();
        this.unencryptedFilePath = getFilePath(UNENCRYPTED_FILES_PATH, context.getCollectionID(), context.getFileID());
        this.encryptedFilePath = getFilePath(ENCRYPTED_FILES_PATH, context.getCollectionID(), context.getFileID());
        // Crypto needs to be initialized with the IV and Salt that was used to decrypt the file
        this.crypto = crypto;
    }

    public void performGetFile() {
        byte[] fileBytes = null;
        if (filePart == null) {
            fileBytes = checkLocalStorageForFile();
        }
        if (fileBytes == null) {
            fileBytes = getFileFromPillar();
        }
        // TODO: Remember getPartialFile
        // TODO: Check local files, assume we have checked local database before calling this method (?)
    }

    private byte[] checkLocalStorageForFile() {
        byte[] localBytes = null;
        if (fileExists(UNENCRYPTED_FILES_PATH, context.getCollectionID(), context.getFileID())) {
            localBytes = readBytesFromFile(unencryptedFilePath);

        } else if (fileExists(ENCRYPTED_FILES_PATH, context.getCollectionID(), context.getFileID())) {
            byte[] encryptedBytes = readBytesFromFile(encryptedFilePath);
            localBytes = crypto.decrypt(encryptedBytes);
        }
        return localBytes;
    }

    private byte[] getFileFromPillar() {
        Settings settings = context.getSettings();
        AccessPillarFactory pillarAccess = AccessPillarFactory.getInstance();
        OutputHandler output = new DefaultOutputHandler(getClass());
        CompleteEventAwaiter eventHandler = new GetFileEventHandler(settings, output);
        String auditTrailInformation = "AuditTrailInfo for getFileFromSpecificPillarTest";

        SecurityManager securityManager = null;
        /* FIXME: Missing securityManager
        String id = settings.getReferenceSettings().getIntegrityServiceSettings().getID();
        securityManager = SecurityManagerUtil.getSecurityManager(settings, Paths.get(privateKeyFile), id); */
        //TODO: Use this: fileUrl = extractUrl(fileID); instead of getUrlForResult(); ?

        GetFileClient client = pillarAccess.createGetFileClient(settings, securityManager, context.getClientID());
        client.getFileFromEncryptedPillar(context.getCollectionID(), context.getFileID(), context.getFilePart(),
                context.getUrlForResult(), eventHandler, auditTrailInformation);
        // TODO: Get response from the URL 
        return new byte[0];
    }

    public void handleStateAndJobDoneHandler() {
        // TODO: Implement JobDoneHandler (sending encrypted file to encrypted pillar) and state updates
    }
}

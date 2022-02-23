package dk.kb.bitrepository.mediator.filehandler;

import dk.kb.bitrepository.mediator.MediatorComponentFactory;
import dk.kb.bitrepository.mediator.crypto.CryptoStrategy;
import org.bitrepository.access.AccessComponentFactory;
import org.bitrepository.access.getfile.GetFileClient;
import org.bitrepository.access.getfile.conversation.GetFileConversationContext;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.FilePart;
import org.bitrepository.commandline.eventhandler.CompleteEventAwaiter;
import org.bitrepository.commandline.eventhandler.GetFileEventHandler;
import org.bitrepository.commandline.output.DefaultOutputHandler;
import org.bitrepository.commandline.output.OutputHandler;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.security.SecurityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
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
    private final ChecksumDataForFileTYPE checksumData;
    private final CryptoStrategy crypto;

    public GetFileHandler(GetFileConversationContext context, ChecksumDataForFileTYPE checksumData, CryptoStrategy crypto) {
        this.context = context;
        this.filePart = context.getFilePart();
        this.unencryptedFilePath = getFilePath(UNENCRYPTED_FILES_PATH, context.getCollectionID(), context.getFileID());
        this.encryptedFilePath = getFilePath(ENCRYPTED_FILES_PATH, context.getCollectionID(), context.getFileID());
        this.checksumData = checksumData;
        // Crypto needs to be initialized with the IV and Salt that was used to decrypt the file
        this.crypto = crypto;
    }

    public void performGetFile() {
        log.debug("Attempting to find file locally.");
        byte[] fileBytes = checkLocalStorageForFile();
        // TODO: Upload to URL (?)
        if (fileBytes == null) {
            log.debug("Getting file from pillar.");
            fileBytes = getFileFromPillar();
        }
        if (filePart != null) {
            log.debug("Getting file part");
            fileBytes = getFilePart(fileBytes);
        }
        // TODO: Check local files, assume we have checked local database before calling this method
    }

    private byte[] checkLocalStorageForFile() {
        byte[] localBytes = null;
        if (fileExists(UNENCRYPTED_FILES_PATH, context.getCollectionID(), context.getFileID())) {
            log.debug("Using local unencrypted file.");
            localBytes = readBytesFromFile(unencryptedFilePath);

        } else if (fileExists(ENCRYPTED_FILES_PATH, context.getCollectionID(), context.getFileID())) {
            log.debug("Using local encrypted file.");
            byte[] encryptedBytes = readBytesFromFile(encryptedFilePath);
            localBytes = crypto.decrypt(encryptedBytes);
        }
        if (compareChecksums(localBytes, checksumData.getChecksumSpec(),
                new String(checksumData.getChecksumValue(), Charset.defaultCharset()))) {
            return localBytes;
        }
        return null;
    }

    private byte[] getFileFromPillar() {
        Settings settings = context.getSettings();
        AccessComponentFactory pillarAccess = AccessComponentFactory.getInstance();
        OutputHandler output = new DefaultOutputHandler(getClass());
        CompleteEventAwaiter eventHandler = new GetFileEventHandler(settings, output);
        String auditTrailInformation = "AuditTrailInfo for getFileFromPillar.";
        SecurityManager securityManager = MediatorComponentFactory.getSecurityManager();

        GetFileClient client = pillarAccess.createGetFileClient(settings, securityManager, context.getClientID());

        if (context.getContributors().size() > 1) {
            client.getFileFromFastestPillar(context.getCollectionID(), context.getFileID(), context.getFilePart(),
                    context.getUrlForResult(), eventHandler, auditTrailInformation);
        } else {
            client.getFileFromSpecificPillar(context.getCollectionID(), context.getFileID(), context.getFilePart(),
                    context.getUrlForResult(), context.getContributors().iterator().next(), eventHandler, auditTrailInformation);
        }

        // TODO: Get response from the URL AND decrypt it before returning the bytes? (If locally written then run decrypt file)
        return new byte[0];
    }

    private byte[] getFilePart(byte[] fileBytes) {
        return null;
    }

    private void handleStateAndJobDoneHandler() {
        // TODO: Implement : update state, let JobHandler know the job is done (?)
    }
}

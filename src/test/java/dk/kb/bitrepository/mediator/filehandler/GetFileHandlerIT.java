package dk.kb.bitrepository.mediator.filehandler;

import dk.kb.bitrepository.mediator.IntegrationFileHandlerTest;
import dk.kb.bitrepository.mediator.TestingSetup;
import dk.kb.bitrepository.mediator.crypto.AESCryptoStrategy;
import dk.kb.bitrepository.mediator.database.DatabaseDAO;
import dk.kb.bitrepository.mediator.filehandler.exception.MismatchingChecksumsException;
import org.bitrepository.access.getfile.conversation.GetFileConversationContext;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.client.conversation.mediator.CollectionBasedConversationMediator;
import org.bitrepository.client.conversation.mediator.ConversationMediatorManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Collections;

import static dk.kb.bitrepository.mediator.TestingUtilities.cleanupFiles;
import static dk.kb.bitrepository.mediator.database.DatabaseConstants.*;
import static dk.kb.bitrepository.mediator.utils.configurations.ConfigConstants.ENCRYPTED_FILES_PATH;
import static dk.kb.bitrepository.mediator.utils.configurations.ConfigConstants.UNENCRYPTED_FILES_PATH;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@DisplayName("Test #GetFileHandler")
public class GetFileHandlerIT extends IntegrationFileHandlerTest {
    private static byte[] fileBytes;
    private static ChecksumDataForFileTYPE checksumDataForFileTYPE;
    private static DatabaseDAO dao;
    private static AESCryptoStrategy crypto;

    @BeforeAll
    static void setup() throws IOException {
        TestingSetup setup = new TestingSetup();
        setupSettingsAndFileExchange();
        setupMessageBus(settings, securityManager);

        fileBytes = setup.getFileBytes();
        checksumDataForFileTYPE = setup.getChecksumDataForFileTYPE();
        dao = setup.getDao();
        crypto = new AESCryptoStrategy(setup.getCryptoConfigurations().getPassword());
    }

    @AfterEach
    public void cleanup() {
        teardownMessageBus();
        cleanupFiles(UNENCRYPTED_FILES_PATH);
        cleanupFiles(ENCRYPTED_FILES_PATH);
        dao.delete(COLLECTION_ID, FILE_ID, ENC_PARAMS_TABLE);
        dao.delete(COLLECTION_ID, FILE_ID, FILES_TABLE);
    }

    @Test
    @DisplayName("Test #GetFileHandler using existing file")
    public void testGetFileUsingExistingFile() throws MismatchingChecksumsException {
        OffsetDateTime receivedTimestamp = OffsetDateTime.now(Clock.systemUTC());
        PutFileHandler putFileHandler =
                new PutFileHandler(COLLECTION_ID, FILE_ID, fileBytes, checksumDataForFileTYPE, receivedTimestamp, dao, crypto);

        putFileHandler.performPutFile();

        //Test with both files (will use unencrypted file)
        GetFileConversationContext context =
                new GetFileConversationContext(COLLECTION_ID, FILE_ID, null, null, null, settings, null, settings.getComponentID(), null,
                        "GetFileHandler Test");
        GetFileHandler getFileHandler = new GetFileHandler(context, checksumDataForFileTYPE, crypto, null);
        assertDoesNotThrow(getFileHandler::performGetFile);

        //Test with only encrypted file
        cleanupFiles(UNENCRYPTED_FILES_PATH);
        assertDoesNotThrow(getFileHandler::performGetFile);
    }

    @Test
    @DisplayName("Test #GetFileHandler using file on pillar")
    public void testGetFileHandlerUsingPillarFile() throws IOException {
        putFileLocally(fileExchange);


        GetFileConversationContext context = new GetFileConversationContext(COLLECTION_ID, FILE_ID, fileExchange.getURL(FILE_ID), null,
                Collections.singleton("TestPillar1"), settings, null, settings.getComponentID(), null, "");
        GetFileHandler handler = new GetFileHandler(context, checksumDataForFileTYPE, crypto, fileExchange);
        handler.performGetFile();
    }
}

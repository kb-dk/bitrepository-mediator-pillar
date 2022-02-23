package dk.kb.bitrepository.mediator.filehandler;

import dk.kb.bitrepository.mediator.TestingSetup;
import dk.kb.bitrepository.mediator.crypto.AESCryptoStrategy;
import dk.kb.bitrepository.mediator.database.DatabaseDAO;
import dk.kb.bitrepository.mediator.filehandler.exception.MismatchingChecksumsException;
import org.bitrepository.access.getfile.conversation.GetFileConversationContext;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.common.settings.Settings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Clock;
import java.time.OffsetDateTime;

import static dk.kb.bitrepository.mediator.TestingUtilities.cleanupFiles;
import static dk.kb.bitrepository.mediator.TestingUtilities.loadIncorrectChecksumData;
import static dk.kb.bitrepository.mediator.database.DatabaseConstants.*;
import static dk.kb.bitrepository.mediator.utils.configurations.ConfigConstants.ENCRYPTED_FILES_PATH;
import static dk.kb.bitrepository.mediator.utils.configurations.ConfigConstants.UNENCRYPTED_FILES_PATH;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@DisplayName("Test #GetFileHandler")
public class GetFileHandlerIT {
    private static byte[] fileBytes;
    private static ChecksumDataForFileTYPE checksumDataForFileTYPE;
    private static DatabaseDAO dao;
    private static AESCryptoStrategy crypto;
    private static ChecksumDataForFileTYPE checksumDataWithWrongChecksum;
    private static Settings settings;

    @BeforeAll
    static void setup() throws IOException {
        TestingSetup setup = new TestingSetup();
        settings = setup.getConfigurations().getRefPillarSettings();
        fileBytes = setup.getFileBytes();
        checksumDataForFileTYPE = setup.getChecksumDataForFileTYPE();
        dao = setup.getDao();
        crypto = new AESCryptoStrategy(setup.getCryptoConfigurations().getPassword());
        checksumDataWithWrongChecksum = loadIncorrectChecksumData();
    }

    @AfterEach
    public void cleanup() {
        cleanupFiles(UNENCRYPTED_FILES_PATH);
        cleanupFiles(ENCRYPTED_FILES_PATH);
        dao.delete(COLLECTION_ID, FILE_ID, ENC_PARAMS_TABLE);
        dao.delete(COLLECTION_ID, FILE_ID, FILES_TABLE);
    }

    @Test
    @DisplayName("Test #GetFileHandler for existing file")
    public void testGetFileHandlerForExistingFile() throws MismatchingChecksumsException {
        OffsetDateTime receivedTimestamp = OffsetDateTime.now(Clock.systemUTC());
        PutFileHandler putFileHandler = new PutFileHandler(COLLECTION_ID, FILE_ID, fileBytes, checksumDataForFileTYPE, receivedTimestamp,
                dao, crypto);

        putFileHandler.performPutFile();

        //Test with both files (will use unencrypted file)
        GetFileConversationContext context = new GetFileConversationContext(COLLECTION_ID, FILE_ID, null, null,
                null, settings, null,
                settings.getComponentID(), null, "GetFileHandler Test");
        GetFileHandler getFileHandler = new GetFileHandler(context, checksumDataForFileTYPE, crypto, null);
        assertDoesNotThrow(getFileHandler::performGetFile);

        //Test with only encrypted file
        cleanupFiles(UNENCRYPTED_FILES_PATH);
        assertDoesNotThrow(getFileHandler::performGetFile);
    }


}

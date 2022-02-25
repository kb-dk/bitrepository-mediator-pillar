package dk.kb.bitrepository.mediator.filehandler;

import dk.kb.bitrepository.mediator.IntegrationFileHandlerTest;
import dk.kb.bitrepository.mediator.filehandler.exception.MismatchingChecksumsException;
import org.bitrepository.access.getfile.conversation.GetFileConversationContext;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Collections;

import static dk.kb.bitrepository.mediator.TestingUtilities.cleanupFiles;
import static dk.kb.bitrepository.mediator.database.DatabaseConstants.COLLECTION_ID;
import static dk.kb.bitrepository.mediator.database.DatabaseConstants.FILE_ID;
import static dk.kb.bitrepository.mediator.utils.configurations.ConfigConstants.UNENCRYPTED_FILES_PATH;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Test #GetFileHandler")
public class GetFileHandlerIT extends IntegrationFileHandlerTest {
    @Test
    @DisplayName("Test #GetFileHandler using existing file")
    public void testGetFileUsingExistingFile() throws MismatchingChecksumsException {
        OffsetDateTime receivedTimestamp = OffsetDateTime.now(Clock.systemUTC());
        PutFileHandler putFileHandler = new PutFileHandler(COLLECTION_ID, FILE_ID, fileBytes, checksumDataForFileTYPE, receivedTimestamp,
                dao, crypto);

        putFileHandler.performPutFile();

        //Test with both files (will use unencrypted file)
        GetFileConversationContext context = new GetFileConversationContext(COLLECTION_ID, FILE_ID, null, null, null, settings, null,
                settings.getComponentID(), null, "GetFileHandler Test");
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
                Collections.singleton(encryptedPillarID), settings, null, settings.getComponentID(), null, "");
        GetFileHandler handler = new GetFileHandler(context, checksumDataForFileTYPE, crypto, fileExchange);
        handler.performGetFile();

        IdentifyPillarsForGetFileRequest receivedIdentifyRequestMessage = collectionReceiver.waitForMessage(
                IdentifyPillarsForGetFileRequest.class);
        assertEquals(collectionID, receivedIdentifyRequestMessage.getCollectionID());
        assertEquals(FILE_ID, receivedIdentifyRequestMessage.getFileID());
    }
}

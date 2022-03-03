package dk.kb.bitrepository.mediator.filehandler;

import dk.kb.bitrepository.mediator.IntegrationFileHandlerTest;
import dk.kb.bitrepository.mediator.filehandler.exception.MismatchingChecksumsException;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Collections;

import static dk.kb.bitrepository.mediator.TestingUtilities.cleanupFiles;
import static dk.kb.bitrepository.mediator.database.DatabaseConstants.COLLECTION_ID;
import static dk.kb.bitrepository.mediator.database.DatabaseConstants.FILE_ID;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Test #GetFileHandler")
public class GetFileHandlerIT extends IntegrationFileHandlerTest {
    @BeforeAll
    protected static void setup() {
        startRealMessageBus();
        startEmbeddedPillar();
    }

    @AfterAll
    protected static void cleanup() {
        stopEmbeddedPillar();
    }

    @Test
    @DisplayName("Test #GetFileHandler using existing file")
    public void testGetFileUsingExistingFile() throws MismatchingChecksumsException {
        OffsetDateTime receivedTimestamp = OffsetDateTime.now(Clock.systemUTC());

        JobContext context = new JobContext(COLLECTION_ID, FILE_ID, fileBytes, null, checksumDataForFileTYPE, settings, fileURL,
                Collections.singleton(encryptedPillarID), crypto, fileExchange);
        PutFileHandler putFileHandler = new PutFileHandler(context, receivedTimestamp);

        putFileHandler.performPutFile();

        //Test with both files (will use unencrypted file)
        GetFileHandler getFileHandler = new GetFileHandler(context);
        assertDoesNotThrow(getFileHandler::performGetFile);

        //Test with only encrypted file
        cleanupFiles(UNENCRYPTED_FILES_PATH);
        assertDoesNotThrow(getFileHandler::performGetFile);
    }

    @Disabled
    @Test
    @DisplayName("Test #GetFileHandler using file on pillar")
    public void testGetFileHandlerUsingPillarFile() throws IOException {
        putFileLocally(fileExchange);

        JobContext context = new JobContext(COLLECTION_ID, FILE_ID, null, null, checksumDataForFileTYPE, settings, fileURL,
                Collections.singleton(encryptedPillarID), crypto, fileExchange);
        GetFileHandler handler = new GetFileHandler(context);
        handler.performGetFile();

        IdentifyPillarsForGetFileRequest receivedIdentifyRequestMessage = collectionReceiver.waitForMessage(
                IdentifyPillarsForGetFileRequest.class);
        assertEquals(collectionID, receivedIdentifyRequestMessage.getCollectionID());
        assertEquals(FILE_ID, receivedIdentifyRequestMessage.getFileID());
//
//        TestGetFileMessageFactory messageFactory = new TestGetFileMessageFactory(settings.getComponentID());
//        IdentifyPillarsForGetFileResponse receivedIdentifyResponse = messageFactory.createIdentifyPillarsForGetFileResponse(
//                receivedIdentifyRequestMessage, encryptedPillarID, pillarDestinationId);
//        messageBus.sendMessage(receivedIdentifyResponse);
//
//        GetFileRequest receivedGetFileRequest = pillarReceiver.waitForMessage(GetFileRequest.class);
//        GetFileProgressResponse getFileProgressResponse = messageFactory.createGetFileProgressResponse(receivedGetFileRequest,
//                encryptedPillarID, pillarDestinationId);
//        messageBus.sendMessage(getFileProgressResponse);
//
//        GetFileFinalResponse completeMsg = messageFactory.createGetFileFinalResponse(receivedGetFileRequest, encryptedPillarID,
//                pillarDestinationId);
//        messageBus.sendMessage(completeMsg);

//        fileExchange.getFile(new File(ENCRYPTED_FILES_PATH + "/" + FILE_ID), fileURL.toExternalForm());
//        assertEquals("lorem ipsum", Files.readString(Path.of(ENCRYPTED_FILES_PATH + "/" + FILE_ID), Charset.defaultCharset()));
    }
}

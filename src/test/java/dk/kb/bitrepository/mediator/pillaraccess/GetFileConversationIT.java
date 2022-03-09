package dk.kb.bitrepository.mediator.pillaraccess;

import dk.kb.bitrepository.mediator.IntegrationFileHandlerTest;
import dk.kb.bitrepository.mediator.pillaraccess.factories.GetFileClientTestWrapper;
import dk.kb.bitrepository.mediator.pillaraccess.factories.TestGetFileMessageFactory;
import org.bitrepository.access.AccessComponentFactory;
import org.bitrepository.access.getfile.ConversationBasedGetFileClient;
import org.bitrepository.access.getfile.GetFileClient;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.*;
import org.bitrepository.client.conversation.mediator.ConversationMediator;
import org.bitrepository.commandline.eventhandler.CompleteEventAwaiter;
import org.bitrepository.commandline.eventhandler.GetFileEventHandler;
import org.bitrepository.commandline.output.DefaultOutputHandler;
import org.bitrepository.commandline.output.OutputHandler;
import org.bitrepository.protocol.FileExchange;
import org.bitrepository.protocol.LocalFileExchange;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import static dk.kb.bitrepository.mediator.database.DatabaseConstants.FILE_ID;
import static org.junit.jupiter.api.Assertions.*;

public class GetFileConversationIT extends IntegrationFileHandlerTest {
    @Test
    @DisplayName("Test #AccessPillarFactory.createGetFileClient returns a GetFileConversation")
    public void verifyGetFileClientFromFactory() {
        assertTrue(AccessComponentFactory.getInstance()
                        .createGetFileClient(settings, securityManager, settings.getComponentID()) instanceof ConversationBasedGetFileClient,
                "The default GetFileClient from the Access factory should be of the type '" +
                        ConversationBasedGetFileClient.class.getName() + "'.");
    }

    @Test
    @DisplayName("Test local fileExchange")
    public void testLocalFileExchange() throws IOException {
        FileExchange fileExchange = new LocalFileExchange(BASE_FILE_EXCHANGE_DIR);
        putFileLocally(fileExchange);

        URL fileURL = fileExchange.getURL(FILE_ID);
        fileExchange.getFile(new File(ENCRYPTED_FILES_PATH + "/" + FILE_ID), fileURL.toExternalForm());
        assertEquals("lorem ipsum", Files.readString(Path.of(ENCRYPTED_FILES_PATH + "/" + FILE_ID), Charset.defaultCharset()));

        File actualFile = new File(fileURL.getFile());
        assertTrue(actualFile.delete());
    }

    @Test
    @DisplayName("Test #GetFileConversation")
    public void testIdentifyPillarGetFileRequest() throws IOException {
        putFileLocally(fileExchange);

        GetFileClient client = createGetFileClient(conversationMediator);
        OutputHandler output = new DefaultOutputHandler(getClass());
        CompleteEventAwaiter eventHandler = new GetFileEventHandler(settings, output);

        client.getFileFromSpecificPillar(collectionID, FILE_ID, null, fileURL, encryptedPillarID, eventHandler,
                "AuditTrailInfo for getFileFromSpecificPillarTest");
        IdentifyPillarsForGetFileRequest receivedIdentifyRequestMessage = collectionReceiver.waitForMessage(
                IdentifyPillarsForGetFileRequest.class);
        assertEquals(collectionID, receivedIdentifyRequestMessage.getCollectionID());
        assertEquals(FILE_ID, receivedIdentifyRequestMessage.getFileID());
        assertNotNull(receivedIdentifyRequestMessage.getCorrelationID());
        assertEquals(settings.getReceiverDestinationID(), receivedIdentifyRequestMessage.getReplyTo());
        assertEquals(settings.getComponentID(), receivedIdentifyRequestMessage.getFrom());
        assertEquals(settings.getCollectionDestination(), receivedIdentifyRequestMessage.getDestination());
        //assertEquals(eventHandler.getFinish().getEventType(), OperationEvent.OperationEventType.IDENTIFY_REQUEST_SENT);

        TestGetFileMessageFactory messageFactory = new TestGetFileMessageFactory(settings.getComponentID());
        //IdentifyPillarsForGetFileResponse tes = clientReceiver.waitForMessage(IdentifyPillarsForGetFileResponse.class);
        IdentifyPillarsForGetFileResponse receivedIdentifyResponse = messageFactory.createIdentifyPillarsForGetFileResponse(
                receivedIdentifyRequestMessage, encryptedPillarID, pillarDestinationId);
        assertEquals(ResponseCode.IDENTIFICATION_POSITIVE, receivedIdentifyResponse.getResponseInfo().getResponseCode());
        assertEquals(encryptedPillarID, receivedIdentifyResponse.getPillarID());
        assertEquals(FILE_ID, receivedIdentifyResponse.getFileID());
        messageBus.sendMessage(receivedIdentifyResponse);

        GetFileRequest receivedGetFileRequest = pillarReceiver.waitForMessage(GetFileRequest.class);
        //assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEvent.OperationEventType.COMPONENT_IDENTIFIED);
        //assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEvent.OperationEventType.IDENTIFICATION_COMPLETE);
        //assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEvent.OperationEventType.REQUEST_SENT);
        GetFileProgressResponse getFileProgressResponse = messageFactory.createGetFileProgressResponse(receivedGetFileRequest,
                encryptedPillarID, pillarDestinationId);
        messageBus.sendMessage(getFileProgressResponse);
        GetFileFinalResponse completeMsg = messageFactory.createGetFileFinalResponse(receivedGetFileRequest, encryptedPillarID,
                pillarDestinationId);
        messageBus.sendMessage(completeMsg);
        assertEquals(fileURL.toExternalForm(), completeMsg.getFileAddress());
        fileExchange.getFile(new File(ENCRYPTED_FILES_PATH + "/" + FILE_ID), fileURL.toExternalForm());
        assertEquals("lorem ipsum", Files.readString(Path.of(ENCRYPTED_FILES_PATH + "/" + FILE_ID), Charset.defaultCharset()));

        //assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_COMPLETE);
        //assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPLETE);
    }

    private GetFileClient createGetFileClient(ConversationMediator conversationMediator) {
        return new GetFileClientTestWrapper(
                new ConversationBasedGetFileClient(messageBus, conversationMediator, settings, settings.getComponentID()));
    }
}

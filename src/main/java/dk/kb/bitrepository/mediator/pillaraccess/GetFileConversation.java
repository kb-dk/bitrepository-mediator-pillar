package dk.kb.bitrepository.mediator.pillaraccess;

import dk.kb.bitrepository.mediator.pillaraccess.clients.GetFileClient;
import org.bitrepository.access.getfile.conversation.GetFileConversationContext;
import org.bitrepository.bitrepositoryelements.FilePart;
import org.bitrepository.client.AbstractClient;
import org.bitrepository.client.conversation.mediator.ConversationMediator;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Collections;

public class GetFileConversation extends AbstractClient implements GetFileClient {
    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * @param settings             The settings.
     * @param conversationMediator The mediator.
     * @param messageBus           The messageBus.
     * @param clientID             The id of the client.
     * @see AbstractClient
     */
    public GetFileConversation(MessageBus messageBus, ConversationMediator conversationMediator, Settings settings, String clientID) {
        super(settings, conversationMediator, messageBus, clientID);
        ArgumentValidator.checkNotNull(messageBus, "messageBus");
        ArgumentValidator.checkNotNull(settings, "settings");
    }

    public void getFileFromEncryptedPillar(String collectionID, String fileID, FilePart filePart, URL uploadUrl,
                                           EventHandler eventHandler, String auditTrailInformation) {
        log.info("Requesting the file '" + fileID + "' from pillar '" + settings.getComponentID() + "'.");
        GetFileConversationContext context = new GetFileConversationContext(collectionID, fileID, uploadUrl, filePart,
                Collections.singleton(settings.getComponentID()), settings, messageBus, clientID, eventHandler, auditTrailInformation);
        startConversation(context, new EncryptedPillarGetFileRequest(context));
    }

}

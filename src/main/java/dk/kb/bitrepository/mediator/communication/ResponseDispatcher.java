package dk.kb.bitrepository.mediator.communication;

import dk.kb.bitrepository.mediator.MediatorConfiguration;
import org.bitrepository.bitrepositorymessages.MessageRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.protocol.ProtocolVersionLoader;
import org.bitrepository.protocol.messagebus.MessageSender;

public class ResponseDispatcher {
    private final MediatorConfiguration config;
    private final MessageSender sender;

    public ResponseDispatcher(MediatorConfiguration config, MessageSender sender) {
        this.config = config;
        this.sender = sender;
    }

    public void completeAndSendResponseToRequest(MessageRequest request, MessageResponse response) {
        completeResponse(request, response);
        sender.sendMessage(response);
    }

    private void completeResponse(MessageRequest request, MessageResponse response) {
        // TODO will probably move this block to template method in a parent class as in ref-code
        response.setFrom(config.getComponentID());
        response.setMinVersion(ProtocolVersionLoader.loadProtocolVersion().getMinVersion());
        response.setVersion(ProtocolVersionLoader.loadProtocolVersion().getVersion());

        response.setCorrelationID(request.getCorrelationID());
        response.setCollectionID(request.getCollectionID());
        response.setReplyTo(config.getPrivateMessageDestination());
        response.setDestination(request.getReplyTo());
        response.setTo(request.getFrom());
    }
}

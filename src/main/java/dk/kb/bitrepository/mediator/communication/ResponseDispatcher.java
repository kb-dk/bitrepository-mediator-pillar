package dk.kb.bitrepository.mediator.communication;

import dk.kb.bitrepository.mediator.utils.configurations.PillarSettings;
import org.bitrepository.bitrepositorymessages.MessageRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.protocol.ProtocolVersionLoader;
import org.bitrepository.protocol.messagebus.MessageSender;

public class ResponseDispatcher {
    private final PillarSettings config;
    private final MessageSender sender;
    private final String privateMessageDestination;

    public ResponseDispatcher(PillarSettings config, String privateMessageDestination, MessageSender sender) {
        this.config = config;
        this.privateMessageDestination = privateMessageDestination;
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
        response.setReplyTo(privateMessageDestination);
        response.setDestination(request.getReplyTo());
        response.setTo(request.getFrom());
    }
}

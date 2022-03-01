package dk.kb.bitrepository.mediator.communication;

import org.bitrepository.bitrepositorymessages.MessageRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.ProtocolVersionLoader;
import org.bitrepository.protocol.messagebus.MessageSender;

public class ResponseDispatcher {
    private final Settings refPillarSettings;
    private final MessageSender sender;

    public ResponseDispatcher(Settings refPillarSettings, MessageSender sender) {
        this.refPillarSettings = refPillarSettings;
        this.sender = sender;
    }

    public void completeAndSendResponseToRequest(MessageRequest request, MessageResponse response) {
        completeResponse(request, response);
        sender.sendMessage(response);
    }

    private void completeResponse(MessageRequest request, MessageResponse response) {
        // TODO will probably move this block to template method in a parent class as in ref-code
        response.setFrom(refPillarSettings.getComponentID());
        response.setMinVersion(ProtocolVersionLoader.loadProtocolVersion().getMinVersion());
        response.setVersion(ProtocolVersionLoader.loadProtocolVersion().getVersion());

        response.setCorrelationID(request.getCorrelationID());
        response.setCollectionID(request.getCollectionID());
        response.setReplyTo(refPillarSettings.getContributorDestinationID());
        response.setDestination(request.getReplyTo());
        response.setTo(request.getFrom());
    }
}

package dk.kb.bitrepository.mediator.communication;

import org.bitrepository.bitrepositorymessages.MessageRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.ProtocolVersionLoader;
import org.bitrepository.protocol.messagebus.MessageSender;

/**
 * Class for sending responses over the message bus.
 */
public class ResponseDispatcher {
    private final Settings refPillarSettings;
    private final MessageSender sender;
    private final String privateMessageDestination;

    public ResponseDispatcher(MessageSender sender, Settings refPillarSettings, String privateMessageDestination) {
        this.sender = sender;
        this.refPillarSettings = refPillarSettings;
        this.privateMessageDestination = privateMessageDestination;
    }

    /**
     * Adds the final configurations to a response and sends it.
     *
     * @param request The request that the response should match.
     * @param response The response to configure and send.
     */
    public void completeAndSendResponseToRequest(MessageRequest request, MessageResponse response) {
        completeResponse(request, response);
        sender.sendMessage(response);
    }

    /**
     * Finalizes the provided MessageResponse by adding the baseline configurations and configures it to
     * match the arguments of the request.
     *
     * @param request The request to use for configuring the response.
     * @param response The response to configure.
     */
    private void completeResponse(MessageRequest request, MessageResponse response) {
        // TODO will probably move this block to template method in a parent class as in ref-code
        response.setFrom(refPillarSettings.getComponentID());
        response.setMinVersion(ProtocolVersionLoader.loadProtocolVersion().getMinVersion());
        response.setVersion(ProtocolVersionLoader.loadProtocolVersion().getVersion());

        response.setCorrelationID(request.getCorrelationID());
        response.setCollectionID(request.getCollectionID());
        response.setReplyTo(privateMessageDestination);
        response.setDestination(request.getReplyTo());
        response.setTo(request.getFrom());
    }
}

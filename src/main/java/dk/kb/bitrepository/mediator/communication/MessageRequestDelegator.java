package dk.kb.bitrepository.mediator.communication;

import dk.kb.bitrepository.mediator.PillarContext;
import dk.kb.bitrepository.mediator.communication.exception.RequestHandlerException;
import dk.kb.bitrepository.mediator.communication.messagehandling.GetFileRequestHandler;
import dk.kb.bitrepository.mediator.communication.messagehandling.IdentifyGetFileRequestHandler;
import dk.kb.bitrepository.mediator.utils.configurations.PillarConfigurations;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.bitrepositorymessages.MessageRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.MessageContext;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.messagebus.MessageListener;
import org.bitrepository.protocol.utils.MessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageRequestDelegator implements MessageListener {
    private static final Logger log = LoggerFactory.getLogger(MessageRequestDelegator.class);
    private final Map<String, RequestHandler<? extends MessageRequest>> handlerMap;
    private final MessageBus messageBus;
    private final PillarContext context;
    private final Settings refPillarSettings;
    private final PillarConfigurations mediatorPillarConfig;

    public MessageRequestDelegator(MessageBus messageBus, PillarContext context) {
        this.messageBus = messageBus;
        this.context = context;
        this.refPillarSettings = context.getConfigurations().getRefPillarSettings();
        this.mediatorPillarConfig = context.getConfigurations().getPillarConfig();

        handlerMap = new HashMap<>();
        for (RequestHandler<? extends MessageRequest> handler : createMessageHandlers()) {
            handlerMap.put(handler.getRequestClass().getSimpleName(), handler);
        }
    }

    private List<RequestHandler<? extends MessageRequest>> createMessageHandlers() {
        // TODO probably put handlers in some kind of factory instead
        List<RequestHandler<? extends MessageRequest>> handlers = new ArrayList<>();
        handlers.add(new IdentifyGetFileRequestHandler(context));
        handlers.add(new GetFileRequestHandler(context));
        return handlers;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void onMessage(Message message, MessageContext messageContext) {
        if (message instanceof MessageRequest) {
            MessageRequest request = (MessageRequest) message;
            RequestHandler handler = handlerMap.get(message.getClass().getSimpleName());
            if (handler != null) {
                try {
                    handler.processRequest(request, messageContext);
                } catch (IllegalArgumentException e) {
                    log.debug("Stack trace for illegal argument", e);
                    ResponseInfo responseInfo = new ResponseInfo();
                    responseInfo.setResponseCode(ResponseCode.REQUEST_NOT_UNDERSTOOD_FAILURE);
                    responseInfo.setResponseText(e.getMessage());
                    sendFailedResponse(request, handler, responseInfo);
                    // TODO alarm?
                } catch (RequestHandlerException e) {
                    log.debug("Failed to handle request '" + message + "'", e);
                    sendFailedResponse(request, handler, e.getResponseInfo());
                    // TODO alarm?
                }
            } else {
                if (MessageUtils.isIdentifyRequest(message)) {
                    log.trace("Received unhandled identity request: \n{}", message);
                } else
                    log.warn("Unable to handle messages of this type: \n{}", message);
            }
        } else {
            log.trace("Can only handle message requests, but received: \n{}", message);
        }
    }

    /**
     * Method to send the corresponding failed response when an exception occurs in the handling of a request.
     *
     * @param request The request that is being handled.
     * @param handler The corresponding handler.
     * @param responseInfo ResponseInfo that should be modified to the specific failed scenario.
     * @param <T> The type of the request that failed.
     */
    private <T extends MessageRequest> void sendFailedResponse(T request, RequestHandler<T> handler, ResponseInfo responseInfo) {
        log.info("Cannot perform operation. Sending failed response. Cause: " + responseInfo.getResponseText());
        MessageResponse response = handler.generateFailedResponse(request);
        response.setResponseInfo(responseInfo);
        context.getResponseDispatcher().completeAndSendResponseToRequest(request, response);
    }

    public void startListening() {
        messageBus.addListener(mediatorPillarConfig.getPrivateMessageDestination(), this);
        messageBus.addListener(refPillarSettings.getCollectionDestination(), this);
    }

    public void stop() {
        messageBus.removeListener(mediatorPillarConfig.getPrivateMessageDestination(), this);
        messageBus.removeListener(refPillarSettings.getCollectionDestination(), this);
    }
}

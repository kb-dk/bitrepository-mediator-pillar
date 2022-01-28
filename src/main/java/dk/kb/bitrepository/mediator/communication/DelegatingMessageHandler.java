package dk.kb.bitrepository.mediator.communication;

import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.bitrepositorymessages.MessageRequest;
import org.bitrepository.protocol.MessageContext;
import org.bitrepository.protocol.messagebus.MessageListener;
import org.bitrepository.protocol.utils.MessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("rawtypes")
public class DelegatingMessageHandler implements MessageListener { // TODO should maybe be inside mediator as in reference code?
    private static final Logger log = LoggerFactory.getLogger(DelegatingMessageHandler.class);
    private final Map<String, RequestHandler<? extends MessageRequest>> handlerMap;

    public DelegatingMessageHandler(List<RequestHandler<? extends MessageRequest>> handlers) {
        handlerMap = new HashMap<>();
        for (RequestHandler<? extends MessageRequest> handler : handlers) {
            handlerMap.put(handler.getRequestClass().getSimpleName(), handler);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onMessage(Message message, MessageContext messageContext) {
        if (message instanceof MessageRequest) {
            RequestHandler handler = handlerMap.get(message.getClass().getSimpleName());
            if (handler != null) {
                try {
                    handler.processRequest((MessageRequest) message, messageContext);
                } catch (Exception e) { // TODO handle different exceptions and dispatch negative response.
                    // Do stuff
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
}

package dk.kb.bitrepository.mediator.communication;

import dk.kb.bitrepository.mediator.PillarContext;
import dk.kb.bitrepository.mediator.utils.configurations.PillarConfigurations;
import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.bitrepositorymessages.MessageRequest;
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
    private final PillarConfigurations configs;

    public MessageRequestDelegator(MessageBus messageBus, PillarContext context, PillarConfigurations configs) {
        this.messageBus = messageBus;
        this.context = context;
        this.configs = configs;

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
            RequestHandler handler = handlerMap.get(message.getClass().getSimpleName());
            if (handler != null) {
                try {
                    handler.processRequest((MessageRequest) message, messageContext);
                } catch (Exception e) { // TODO handle different exceptions and dispatch negative response.
                    log.error("Something went wrong!", e);
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

    public void startListening() {
        messageBus.addListener(configs.getPrivateMessageDestination(), this);
        messageBus.addListener(context.getConfigurations().getRefPillarSettings().getCollectionDestination(), this);
    }

    public void stop() {
        messageBus.removeListener(configs.getPrivateMessageDestination(), this);
        messageBus.removeListener(context.getConfigurations().getRefPillarSettings().getCollectionDestination(), this);
    }
}

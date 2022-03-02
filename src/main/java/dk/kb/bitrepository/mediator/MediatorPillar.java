package dk.kb.bitrepository.mediator;

import dk.kb.bitrepository.mediator.communication.MessageRequestDelegator;
import dk.kb.bitrepository.mediator.communication.RequestHandler;
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
import org.bitrepository.settings.repositorysettings.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The mediator pillar class.
 */
public class MediatorPillar implements MessageListener {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final MessageBus messageBus;
    private final Settings settings;
    private final PillarContext pillarContext;
    private final PillarConfigurations mediatorPillarConfig;
    private final Map<String, RequestHandler<? extends MessageRequest>> handlerMap;

    /**
     * Constructor instantiating the mediator pillar and registering it as a listener on the message bus
     * @param settings The Settings object containing both the Repository- and ReferenceSettings (TODO replace with just Configurations?)
     * @param pillarContext The pillar context containing necessary components.
     * @param messageBus The message bus.
     */
    public MediatorPillar(Settings settings, PillarContext pillarContext, MessageBus messageBus) {
        log.debug("Creating mediator pillar");
        this.settings = settings;
        this.pillarContext = pillarContext;
        this.messageBus = messageBus;
        this.mediatorPillarConfig = pillarContext.getConfigurations().getPillarConfig();
        messageBus.setCollectionFilter(getPillarCollectionIDs());
        startListening();

        handlerMap = new HashMap<>();
        for (RequestHandler<? extends MessageRequest> handler : createMessageHandlers()) {
            handlerMap.put(handler.getRequestClass().getSimpleName(), handler);
        }
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
     * Helper method for creating MessageRequest-handlers
     *
     * @return List of handlers to register.
     */
    private List<RequestHandler<? extends MessageRequest>> createMessageHandlers() {
        // TODO probably put handlers in some kind of factory instead
        List<RequestHandler<? extends MessageRequest>> handlers = new ArrayList<>();
        handlers.add(new IdentifyGetFileRequestHandler(pillarContext));
        handlers.add(new GetFileRequestHandler(pillarContext));
        return handlers;
    }

    /**
     * Method to send the corresponding failed response when an exception occurs in the handling of a request.
     *
     * @param request The request that is being handled.
     * @param handler The corresponding handler.
     * @param responseInfo ResponseInfo that should be modified to the specific failed scenario.
     * @param <T> The type of the request that failed.
     */
    private <T extends MessageRequest> void sendFailedResponse(T request, RequestHandler<T> handler,
                                                               ResponseInfo responseInfo) {
        log.info("Cannot perform operation. Sending failed response. Cause: " + responseInfo.getResponseText());
        MessageResponse response = handler.generateFailedResponse(request);
        response.setResponseInfo(responseInfo);
        pillarContext.getResponseDispatcher().completeAndSendResponseToRequest(request, response);
    }

    /**
     * Registers this MessageRequestDelegator as an observer on the message bus provided through the constructor.
     */
    public void startListening() {
        messageBus.addListener(mediatorPillarConfig.getPrivateMessageDestination(), this);
        messageBus.addListener(settings.getCollectionDestination(), this);
    }


    /**
     * TODO should use this in the future when nearing a more complete mediator pillar
     * Shuts down all the components of the mediator pillar.
     */
    public void shutdown() {
        try {
            messageBus.removeListener(mediatorPillarConfig.getPrivateMessageDestination(), this);
            messageBus.removeListener(settings.getCollectionDestination(), this);
            messageBus.close();
        } catch (JMSException e) {
            log.warn("Could not close the messagebus.", e);
        }
    }

    /**
     * TODO consider moving to utils file
     * Helper method to grab the collection IDs relevant for this/the underlying pillar from the settings.
     *
     * @return List of collection IDs relevant for the pillar (collections contained in this pillar)
     */
    private List<String> getPillarCollectionIDs() {
        String pillarID = settings.getComponentID();
        List<Collection> collections = settings.getCollections();
        List<String> relevantCollectionIDs = new ArrayList<>();
        for (Collection collection : collections) {
            for (String pillar : collection.getPillarIDs().getPillarID()) {
                if (pillarID.equals(pillar)) {
                    relevantCollectionIDs.add(collection.getID());
                    break;
                }
            }
        }
        return relevantCollectionIDs;
    }
}
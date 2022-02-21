package dk.kb.bitrepository.mediator.communication;

import dk.kb.bitrepository.mediator.communication.exception.RequestHandlerException;
import org.bitrepository.bitrepositorymessages.MessageRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.protocol.MessageContext;

/**
 * Provides functionality for handling a single type of request.
 * @param <T> the type request handled by this handler
 */
public interface RequestHandler<T extends MessageRequest> {
    /**
     * Return the request class which is handled by this handler.
     * @return the class of requests handled by this RequestHandler
     * */
    Class<T> getRequestClass();

    /**
     * Implements the concrete handling of a received request.
     * @param request The request to handle.
     * @param messageContext the message context
     */
    void processRequest(T request, MessageContext messageContext) throws RequestHandlerException;

    /**
     * Used for creating responses signaling general failures to handle the request.
     * The response is missing the response info field.
     * @param request the request to create a response for
     * @return The failure response.
     */
    MessageResponse generateFailedResponse(T request);
}

package dk.kb.bitrepository.mediator.communication;

import org.bitrepository.bitrepositorymessages.MessageRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.protocol.MessageContext;

public abstract class IdentifyRequestHandler<T extends MessageRequest> implements RequestHandler<T> {
    @Override
    public abstract Class<T> getRequestClass();

    @Override
    public void processRequest(T request, MessageContext messageContext) {
        validateRequest(request, messageContext);
        sendPositiveResponse(request, messageContext);
    }

    @Override
    public abstract MessageResponse generateFailedResponse(T request);

    /**
     * Validate both that the given request it is possible to perform and that it is allowed.
     * @param request The request to validate.
     * @param requestContext The context for the request.
     * @throws RequestHandlerException If something in the request is inconsistent with the possibilities of the pillar.
     */
    protected abstract void validateRequest(T request, MessageContext requestContext);
            //throws RequestHandlerException;

    /**
     * Sends a identification response.
     * @param request The request to respond to.
     * @param requestContext The context for the request.
     * @throws RequestHandlerException If the positive response could not be created.
     */
    protected abstract void sendPositiveResponse(T request, MessageContext requestContext);
            //throws RequestHandlerException;
}

package dk.kb.bitrepository.mediator.communication;

import dk.kb.bitrepository.mediator.PillarContext;
import dk.kb.bitrepository.mediator.communication.exception.RequestHandlerException;
import org.bitrepository.bitrepositorymessages.MessageRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.MessageContext;

public abstract class ActionRequestHandler<T extends MessageRequest> implements RequestHandler<T> {
    private PillarContext context;
    private Settings settings;

    public ActionRequestHandler(PillarContext context) {
        this.context = context;
        settings = context.getPillarSettings();
    }

    @Override
    public abstract Class<T> getRequestClass();

    @Override
    public void processRequest(T request, MessageContext messageContext) throws RequestHandlerException {
        validateRequest(request, messageContext);
        sendProgressResponse();
        performAction();
    }

    @Override
    public abstract MessageResponse generateFailedResponse(T request);

    /**
     * Validate both that the given request is possible to perform and that it is allowed.
     *
     * @param request        The request to validate.
     * @param requestContext The context for the request.
     * @throws RequestHandlerException If something in the request is inconsistent with the possibilities of the pillar.
     */
    protected abstract void validateRequest(T request, MessageContext requestContext) throws RequestHandlerException;

    protected abstract void performAction();

    protected abstract void sendProgressResponse();
}

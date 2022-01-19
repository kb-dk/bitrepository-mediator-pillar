package dk.kb.bitrepository.mediator.communication;

import dk.kb.bitrepository.mediator.PillarContext;
import dk.kb.bitrepository.mediator.communication.exception.RequestHandlerException;
import dk.kb.bitrepository.mediator.utils.RequestValidator;
import org.bitrepository.bitrepositorymessages.MessageRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.MessageContext;

public abstract class IdentifyRequestHandler<T extends MessageRequest> implements RequestHandler<T> {
    protected static final String RESPONSE_FOR_POSITIVE_IDENTIFICATION = "Operation acknowledged and accepted.";
    private PillarContext context;
    private Settings settings;
    protected RequestValidator requestValidator;

    public IdentifyRequestHandler(PillarContext context) {
        this.context = context;
        settings = context.getPillarSettings();
        requestValidator = new RequestValidator(settings);
    }

    @Override
    public abstract Class<T> getRequestClass();

    @Override
    public void processRequest(T request, MessageContext messageContext) throws RequestHandlerException {
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
    protected abstract void validateRequest(T request, MessageContext requestContext) throws RequestHandlerException;

    /**
     * Sends a identification response.
     * @param request The request to respond to.
     * @param requestContext The context for the request.
     * @throws RequestHandlerException If the positive response could not be created.
     */
    protected abstract void sendPositiveResponse(T request, MessageContext requestContext) throws RequestHandlerException;

    public Settings getSettings() {
        return settings;
    }
}

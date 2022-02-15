package dk.kb.bitrepository.mediator.communication.messagehandling;

import dk.kb.bitrepository.mediator.PillarContext;
import dk.kb.bitrepository.mediator.communication.RequestHandler;
import dk.kb.bitrepository.mediator.communication.exception.RequestHandlerException;
import dk.kb.bitrepository.mediator.utils.RequestValidator;
import dk.kb.bitrepository.mediator.utils.configurations.Configurations;
import org.bitrepository.bitrepositorymessages.MessageRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.protocol.MessageContext;

/**
 * Parent class for <X>IdentifyRequestHandlers that initializes variables common to all Identify-handlers
 * and defines the template method {@link #processRequest} describing how to process these requests.
 * @param <T> The type of request that is handled.
 */
public abstract class IdentifyRequestHandler<T extends MessageRequest> implements RequestHandler<T> {
    protected static final String RESPONSE_FOR_POSITIVE_IDENTIFICATION = "Operation acknowledged and accepted.";
    protected final Configurations configurations;
    protected PillarContext context;
    protected RequestValidator requestValidator;
    protected String pillarID;

    public IdentifyRequestHandler(PillarContext context) {
        this.context = context;
        configurations = context.getConfigurations();
        requestValidator = new RequestValidator(configurations.getRefPillarSettings(), context.getDAO());
        pillarID = configurations.getPillarConfig().getMediatorPillarID();
    }

    @Override
    public abstract Class<T> getRequestClass();

    @Override
    public void processRequest(T request, MessageContext messageContext) throws RequestHandlerException {
        validateRequest(request);
        sendPositiveResponse(request, messageContext);
    }

    @Override
    public abstract MessageResponse generateFailedResponse(T request);

    /**
     * Validate both that the given request is possible to perform and that it is allowed.
     * @param request The request to validate.
     * @throws RequestHandlerException If something in the request is inconsistent with the possibilities of the pillar.
     */
    protected abstract void validateRequest(T request) throws RequestHandlerException;

    /**
     * Sends an identification response.
     * @param request The request to respond to.
     * @param requestContext The context for the request.
     * @throws RequestHandlerException If the positive response could not be created.
     */
    protected abstract void sendPositiveResponse(T request, MessageContext requestContext) throws RequestHandlerException;
}

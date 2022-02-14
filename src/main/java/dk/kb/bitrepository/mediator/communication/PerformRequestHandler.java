package dk.kb.bitrepository.mediator.communication;

import dk.kb.bitrepository.mediator.PillarContext;
import dk.kb.bitrepository.mediator.communication.exception.RequestHandlerException;
import dk.kb.bitrepository.mediator.utils.RequestValidator;
import dk.kb.bitrepository.mediator.utils.configurations.Configurations;
import org.bitrepository.bitrepositorymessages.MessageRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.protocol.MessageContext;

public abstract class PerformRequestHandler<T extends MessageRequest> implements RequestHandler<T> {
    protected final Configurations configurations;
    protected PillarContext context;
    protected RequestValidator requestValidator;
    protected String pillarID;

    public PerformRequestHandler(PillarContext context) {
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
        sendProgressResponse(request);
        performAction(request, messageContext);
    }

    @Override
    public abstract MessageResponse generateFailedResponse(T request);

    /**
     * Validate both that the given request is possible to perform and that it is allowed.
     *
     * @param request        The request to validate.
     * @throws RequestHandlerException If something in the request is inconsistent with the possibilities of the pillar.
     */
    protected abstract void validateRequest(T request) throws RequestHandlerException;

    protected abstract void performAction(T request, MessageContext context);

    protected abstract void sendProgressResponse(T request);
}

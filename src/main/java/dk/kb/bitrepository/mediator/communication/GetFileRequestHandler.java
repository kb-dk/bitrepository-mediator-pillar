package dk.kb.bitrepository.mediator.communication;

import dk.kb.bitrepository.mediator.PillarContext;
import dk.kb.bitrepository.mediator.communication.exception.RequestHandlerException;
import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.protocol.MessageContext;

public class GetFileRequestHandler extends ActionRequestHandler<GetFileRequest> {
    public GetFileRequestHandler(PillarContext context) {
        super(context);
    }

    @Override
    public Class<GetFileRequest> getRequestClass() {
        return GetFileRequest.class;
    }

    @Override
    public MessageResponse generateFailedResponse(GetFileRequest request) {
        return null;
    }

    @Override
    protected void validateRequest(GetFileRequest request, MessageContext requestContext) throws RequestHandlerException {
        requestValidator.validate(request);
    }

    @Override
    protected void performAction() {
        // Check DAO
        // If file does not exist send failed response
        // Else start client towards underlying pillar
        // and return progress response and results in final response
    }

    @Override
    protected void sendProgressResponse() {

    }
}

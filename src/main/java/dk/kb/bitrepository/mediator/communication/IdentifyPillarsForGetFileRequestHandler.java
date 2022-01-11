package dk.kb.bitrepository.mediator.communication;

import dk.kb.bitrepository.mediator.PillarContext;
import dk.kb.bitrepository.mediator.utils.RequestValidator;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.common.utils.FileIDValidator;
import org.bitrepository.protocol.MessageContext;

public class IdentifyPillarsForGetFileRequestHandler extends IdentifyRequestHandler<IdentifyPillarsForGetFileRequest> {
    RequestValidator validator;

    public IdentifyPillarsForGetFileRequestHandler(PillarContext context) {
        validator = new RequestValidator();
    }

    @Override
    public Class<IdentifyPillarsForGetFileRequest> getRequestClass() {
        return IdentifyPillarsForGetFileRequest.class;
    }

    @Override
    protected void validateRequest(IdentifyPillarsForGetFileRequest request, MessageContext requestContext) {
        validator.validateCollectionIdIsSet(request);
        validator.validateFileIDFormat(request.getFileID());
    }

    @Override
    protected void sendPositiveResponse(IdentifyPillarsForGetFileRequest request, MessageContext requestContext) {

    }

    @Override
    public MessageResponse generateFailedResponse(IdentifyPillarsForGetFileRequest request) {
        return null;
    }
}

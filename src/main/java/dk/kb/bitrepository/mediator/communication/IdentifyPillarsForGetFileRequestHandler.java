package dk.kb.bitrepository.mediator.communication;

import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.common.utils.FileIDValidator;
import org.bitrepository.protocol.MessageContext;

public class IdentifyPillarsForGetFileRequestHandler extends IdentifyRequestHandler<IdentifyPillarsForGetFileRequest> {
    FileIDValidator
    @Override
    public Class<IdentifyPillarsForGetFileRequest> getRequestClass() {
        return IdentifyPillarsForGetFileRequest.class;
    }

    @Override
    protected void validateRequest(IdentifyPillarsForGetFileRequest request, MessageContext requestContext) {
        if(!request.isSetCollectionID()) {
            throw new IllegalArgumentException(request.getClass().getSimpleName() +
                    "'s requires a CollectionID");
        }
    }

    @Override
    protected void sendPositiveResponse(IdentifyPillarsForGetFileRequest request, MessageContext requestContext) {

    }

    @Override
    public MessageResponse generateFailedResponse(IdentifyPillarsForGetFileRequest request) {
        return null;
    }
}

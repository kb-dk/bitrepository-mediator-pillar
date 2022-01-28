package dk.kb.bitrepository.mediator.communication;

import dk.kb.bitrepository.mediator.PillarContext;
import dk.kb.bitrepository.mediator.communication.exception.RequestHandlerException;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileResponse;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.common.utils.TimeMeasurementUtils;
import org.bitrepository.protocol.MessageContext;

public class IdentifyPillarsForGetFileRequestHandler extends IdentifyRequestHandler<IdentifyPillarsForGetFileRequest> {
    public IdentifyPillarsForGetFileRequestHandler(PillarContext context) {
        super(context);
    }

    @Override
    public Class<IdentifyPillarsForGetFileRequest> getRequestClass() {
        return IdentifyPillarsForGetFileRequest.class;
    }

    @Override
    protected void validateRequest(IdentifyPillarsForGetFileRequest request, MessageContext requestContext) throws RequestHandlerException {
        requestValidator.validate(request);
    }

    @Override
    protected void sendPositiveResponse(IdentifyPillarsForGetFileRequest request, MessageContext requestContext) {
        IdentifyPillarsForGetFileResponse response = new IdentifyPillarsForGetFileResponse();
        response.setFileID(request.getFileID());
        response.setPillarID(getSettings().getComponentID());
        response.setTimeToDeliver(
                TimeMeasurementUtils.getTimeMeasurementFromMiliseconds(
                        getSettings().getReferenceSettings().getPillarSettings().getTimeToStartDeliver()));

        ResponseInfo irInfo = new ResponseInfo();
        irInfo.setResponseCode(ResponseCode.IDENTIFICATION_POSITIVE);
        irInfo.setResponseText(RESPONSE_FOR_POSITIVE_IDENTIFICATION);
        response.setResponseInfo(irInfo);
        context.getResponseDispatcher().completeAndSendResponseToRequest(request, response);
    }

    @Override
    public MessageResponse generateFailedResponse(IdentifyPillarsForGetFileRequest request) { // TODO
        return null;
    }
}

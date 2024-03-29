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

/**
 * Class for handling IdentifyPillarsForGetFileRequests.
 */
public class IdentifyGetFileRequestHandler extends IdentifyRequestHandler<IdentifyPillarsForGetFileRequest> {
    public IdentifyGetFileRequestHandler(PillarContext context) {
        super(context);
    }

    @Override
    public Class<IdentifyPillarsForGetFileRequest> getRequestClass() {
        return IdentifyPillarsForGetFileRequest.class;
    }

    @Override
    protected void validateRequest(IdentifyPillarsForGetFileRequest request) throws RequestHandlerException {
        requestValidator.validate(request);
    }

    @Override
    protected void sendPositiveResponse(IdentifyPillarsForGetFileRequest request, MessageContext requestContext) {
        IdentifyPillarsForGetFileResponse response = createPartlyConfiguredResponse(request);
        response.setTimeToDeliver(
                TimeMeasurementUtils.getTimeMeasurementFromMilliseconds(
                        configurations.getRefPillarSettings().getReferenceSettings().getPillarSettings().getTimeToStartDeliver()));

        ResponseInfo irInfo = new ResponseInfo();
        irInfo.setResponseCode(ResponseCode.IDENTIFICATION_POSITIVE);
        irInfo.setResponseText(RESPONSE_FOR_POSITIVE_IDENTIFICATION);
        response.setResponseInfo(irInfo);
        context.getResponseDispatcher().completeAndSendResponseToRequest(request, response);
    }

    /**
     * Creates a partly configured IdentifyPillarsForGetFileResponse based on am IdentifyPillarsForGetFileRequest.
     * @param request The IdentifyPillarsForGetFileRequest to base the response on.
     * @return The IdentifyPillarsForGetFileResponse based on the request.
     */
    private IdentifyPillarsForGetFileResponse createPartlyConfiguredResponse(IdentifyPillarsForGetFileRequest request) {
        IdentifyPillarsForGetFileResponse response = new IdentifyPillarsForGetFileResponse();
        response.setFileID(request.getFileID());
        response.setPillarID(pillarID);
        return response;
    }

    @Override
    public MessageResponse generateFailedResponse(IdentifyPillarsForGetFileRequest request) {
        return createPartlyConfiguredResponse(request);
    }
}

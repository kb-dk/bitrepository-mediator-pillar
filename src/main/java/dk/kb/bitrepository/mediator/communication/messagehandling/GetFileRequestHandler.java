package dk.kb.bitrepository.mediator.communication.messagehandling;

import dk.kb.bitrepository.mediator.PillarContext;
import dk.kb.bitrepository.mediator.communication.exception.RequestHandlerException;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositorymessages.GetFileFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileProgressResponse;
import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.protocol.MessageContext;

/**
 * Class for initial handling of GetFileRequests.
 */
public class GetFileRequestHandler extends OperationRequestHandler<GetFileRequest> {
    public GetFileRequestHandler(PillarContext context) {
        super(context);
    }

    @Override
    public Class<GetFileRequest> getRequestClass() {
        return GetFileRequest.class;
    }

    @Override
    public MessageResponse generateFailedResponse(GetFileRequest request) {
        return createPartlyConfiguredFinalResponse(request);
    }

    @Override
    protected void validateRequest(GetFileRequest request) throws RequestHandlerException {
        requestValidator.validate(request);
    }

    @Override
    protected void scheduleOperation(GetFileRequest request, MessageContext context) {
        // TODO insert jobscheduler.queue(actionThingy) here or something along those lines
    }

    @Override
    protected void sendProgressResponse(GetFileRequest request) {
        GetFileProgressResponse progressResponse = createGetFileProgressResponse(request);
        ResponseInfo responseInfo = new ResponseInfo();
        responseInfo.setResponseCode(ResponseCode.OPERATION_ACCEPTED_PROGRESS);
        responseInfo.setResponseText("Request accepted");
        progressResponse.setResponseInfo(responseInfo);
        context.getResponseDispatcher().completeAndSendResponseToRequest(request, progressResponse);
    }

    /**
     * Creates a partly configured GetFileProgressResponse based on a GetFileRequest.
     * @param request The GetFileRequest to base the progress response on.
     * @return The GetFileProgressResponse based on the request.
     */
    private GetFileProgressResponse createGetFileProgressResponse(GetFileRequest request) {
        GetFileProgressResponse res = new GetFileProgressResponse();
        res.setFileAddress(request.getFileAddress());
        res.setFileID(request.getFileID());
        res.setFilePart(request.getFilePart());
        res.setPillarID(configurations.getPillarConfig().getMediatorPillarID());

        return res;
    }

    /**
     * Creates a GetFileFinalResponse based on a GetFileRequest.
     * @param request The GetFileRequest to base the final response on.
     * @return The GetFileFinalResponse based on the request.
     */
    public GetFileFinalResponse createPartlyConfiguredFinalResponse(GetFileRequest request) {
        GetFileFinalResponse finalResponse = new GetFileFinalResponse();
        finalResponse.setFileAddress(request.getFileAddress());
        finalResponse.setFileID(request.getFileID());
        finalResponse.setFilePart(request.getFilePart());
        finalResponse.setPillarID(configurations.getPillarConfig().getMediatorPillarID());

        return finalResponse;
    }
}

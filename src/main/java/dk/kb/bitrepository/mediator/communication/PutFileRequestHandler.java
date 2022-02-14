package dk.kb.bitrepository.mediator.communication;

import dk.kb.bitrepository.mediator.PillarContext;
import dk.kb.bitrepository.mediator.communication.exception.RequestHandlerException;
import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.protocol.MessageContext;

public class PutFileRequestHandler extends PerformRequestHandler<GetFileRequest> {
    public PutFileRequestHandler(PillarContext context) {
        super(context);
    }

    @Override
    public Class<GetFileRequest> getRequestClass() {
        return GetFileRequest.class;
    }

    @Override
    public MessageResponse generateFailedResponse(GetFileRequest request) { // TODO
        return null;
    }

    @Override
    protected void validateRequest(GetFileRequest request) throws RequestHandlerException {
        requestValidator.validate(request);
    }

    @Override
    protected void performAction(GetFileRequest request, MessageContext context) { // TODO
        System.out.println("Performing action!");
        // Check DAO
        // If file does not exist send failed response
        // Else start client towards underlying pillar
        // and return progress response and results in final response
    }

    @Override
    protected void sendProgressResponse(GetFileRequest request) { // TODO
        // In refpillar checks that collection has an archive for the collection and
        // gets back a DefaultFileInfo from there with the File object - this is used to set the file size on the response
        // Not sure if we should keep entries of file sizes in database? Ask Kim.
        // For now ignore file size - it is not required for the Response object.
        System.out.println("Sending progress response!");
    }
}

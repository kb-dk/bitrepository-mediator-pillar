package dk.kb.bitrepository.mediator.pillaraccess;

import org.bitrepository.access.getfile.conversation.GetFileConversationContext;
import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.client.conversation.ConversationContext;
import org.bitrepository.client.conversation.PerformingOperationState;
import org.bitrepository.client.conversation.selector.SelectedComponentInfo;
import org.bitrepository.client.eventhandler.ContributorCompleteEvent;
import org.bitrepository.common.exceptions.UnableToFinishException;

public class GettingFile extends PerformingOperationState {
    private final GetFileConversationContext context;
    private final String encryptedPillarID;
    private final SelectedComponentInfo encryptedPillar;

    public GettingFile(GetFileConversationContext context, SelectedComponentInfo encryptedPillar) {
        super(encryptedPillar.getID());
        this.context = context;
        this.encryptedPillar = encryptedPillar;
        this.encryptedPillarID = encryptedPillar.getID();
    }

    @Override
    protected void sendRequest() {
        //TODO: Use the request we already have?
        GetFileRequest msg = new GetFileRequest();
        initializeMessage(msg);
        msg.setFileAddress(context.getUrlForResult().toExternalForm());
        msg.setFileID(context.getFileID());
        msg.setFilePart(context.getFilePart());
        msg.setPillarID(encryptedPillarID);
        msg.setDestination(encryptedPillar.getDestination());
        context.getMonitor().requestSent("Sending GetFileRequest to ", encryptedPillar.toString());
        context.getMessageSender().sendMessage(msg);
    }

    @Override
    protected boolean handleFailureResponse(MessageResponse response) throws UnableToFinishException {
        getContext().getMonitor().contributorFailed(
                response.getResponseInfo().getResponseText(), response.getFrom(), response.getResponseInfo().getResponseCode());
        throw new UnableToFinishException("Failed to get file from " + response.getFrom() +
                ", " + response.getResponseInfo());
    }

    @Override
    protected void generateContributorCompleteEvent(MessageResponse response) {
        getContext().getMonitor().contributorComplete(new ContributorCompleteEvent(response.getFrom(), response.getCollectionID()));
    }

    @Override
    protected ConversationContext getContext() {
        return context;
    }

    @Override
    protected String getPrimitiveName() {
        return "GetFile";
    }
}

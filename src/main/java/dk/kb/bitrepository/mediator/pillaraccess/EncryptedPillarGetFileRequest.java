package dk.kb.bitrepository.mediator.pillaraccess;

import org.bitrepository.access.getfile.conversation.GetFileConversationContext;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.client.conversation.ConversationContext;
import org.bitrepository.client.conversation.GeneralConversationState;
import org.bitrepository.client.conversation.IdentifyingState;
import org.bitrepository.client.conversation.selector.SelectedComponentInfo;

public class EncryptedPillarGetFileRequest extends IdentifyingState {
    private final GetFileConversationContext context;


    protected EncryptedPillarGetFileRequest(GetFileConversationContext context) {
        super(context.getContributors());
        this.context = context;
        context.getMonitor().markAsFailedOnContributorFailure(false);
    }

    @Override
    public GeneralConversationState getOperationState() {
        return new GettingFile(context, getEncryptedPilar());
    }

    @Override
    protected void sendRequest() {
        IdentifyPillarsForGetFileRequest msg = new IdentifyPillarsForGetFileRequest();
        initializeMessage(msg);
        msg.setDestination(context.getSettings().getCollectionDestination());
        msg.setFileID(context.getFileID());
        context.getMessageSender().sendMessage(msg);
        context.getMonitor().identifyRequestSent("Identifying pillar from GetFile");
    }

    @Override
    protected ConversationContext getContext() {
        return context;
    }

    @Override
    protected String getPrimitiveName() {
        return "IdentifyPillarsForGetFile";
    }

    /**
     * @return the single selected pillar from the generic selector's SelectedComponents list.
     */
    private SelectedComponentInfo getEncryptedPilar() {
        return getSelector().getSelectedComponents().iterator().next();
    }
}

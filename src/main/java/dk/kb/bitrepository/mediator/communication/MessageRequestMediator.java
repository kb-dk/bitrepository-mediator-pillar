package dk.kb.bitrepository.mediator.communication;

import dk.kb.bitrepository.mediator.PillarContext;
import org.bitrepository.bitrepositorymessages.MessageRequest;
import org.bitrepository.protocol.messagebus.MessageBus;

public class MessageRequestMediator implements MessageMediator {

    private final MessageBus messageBus;
    private final PillarContext context;
    //private List<RequestHandler<? extends MessageRequest>> handlers;

    public MessageRequestMediator(MessageBus messageBus, PillarContext context) {
        this.messageBus = messageBus;
        this.context = context;
    }

    @Override
    public void startListening() {
        //messageBus.addListener(context.getPillarSettings().getContributorDestinationID(), somedelegatinghandler);
        //messageBus.addListener(context.getPillarSettings().getCollectionDestination(), somedelegatinghandler);
    }

    @Override
    public void close() {
        //messageBus.removeListener(context.getPillarSettings().getContributorDestinationID(), somedelegatinghandler);
        //messageBus.removeListener(context.getPillarSettings().getCollectionDestination(), somedelegatinghandler);
    }
}

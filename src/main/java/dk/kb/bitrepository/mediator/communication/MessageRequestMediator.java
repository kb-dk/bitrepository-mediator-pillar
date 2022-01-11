package dk.kb.bitrepository.mediator.communication;

import dk.kb.bitrepository.mediator.PillarContext;
import org.bitrepository.bitrepositorymessages.MessageRequest;
import org.bitrepository.protocol.messagebus.MessageBus;

import java.util.ArrayList;
import java.util.List;

public class MessageRequestMediator implements MessageMediator {

    private final MessageBus messageBus;
    private final PillarContext context;
    private final DelegatingMessageHandler messageHandler;

    public MessageRequestMediator(MessageBus messageBus, PillarContext context) {
        this.messageBus = messageBus;
        this.context = context;
        messageHandler = new DelegatingMessageHandler(createMessageHandlers());
    }

    public List<RequestHandler<? extends MessageRequest>> createMessageHandlers() {
        // TODO probably put handlers in some kind of factory instead
        List<RequestHandler<? extends MessageRequest>> handlers = new ArrayList<>();
        handlers.add(new IdentifyPillarsForGetFileRequestHandler(context));
        return handlers;
    }

    @Override
    public void startListening() {
        messageBus.addListener(context.getPillarSettings().getContributorDestinationID(), messageHandler);
        messageBus.addListener(context.getPillarSettings().getCollectionDestination(), messageHandler);
    }

    @Override
    public void close() {
        messageBus.removeListener(context.getPillarSettings().getContributorDestinationID(), messageHandler);
        messageBus.removeListener(context.getPillarSettings().getCollectionDestination(), messageHandler);
    }
}

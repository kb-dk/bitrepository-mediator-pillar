package dk.kb.bitrepository.mediator.communication;

import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.protocol.MessageContext;
import org.bitrepository.protocol.messagebus.MessageListener;

public class ConversationMediator implements MessageListener { // TODO probably extend as in reference code for being more specific
    @Override
    public void onMessage(Message message, MessageContext messageContext) {

    }
}

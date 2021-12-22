package dk.kb.bitrepository.mediator.communication;

import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.protocol.MessageContext;

public class StateBasedConversation implements Conversation {
    @Override
    public void startConversation() {

    }

    @Override
    public void onMessage(Message message, MessageContext messageContext) {
        System.out.println("Heyho");
    }
}

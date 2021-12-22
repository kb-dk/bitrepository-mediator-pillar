package dk.kb.bitrepository.mediator.communication;

import org.bitrepository.protocol.messagebus.MessageListener;

public interface Conversation extends MessageListener {
    void startConversation();
}

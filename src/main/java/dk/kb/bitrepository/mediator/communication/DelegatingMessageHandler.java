package dk.kb.bitrepository.mediator.communication;

import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.bitrepositorymessages.MessageRequest;
import org.bitrepository.protocol.MessageContext;
import org.bitrepository.protocol.messagebus.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DelegatingMessageHandler implements MessageListener { // TODO should maybe be inside mediator as in reference code?
    private static Logger log = LoggerFactory.getLogger(DelegatingMessageHandler.class);
    public DelegatingMessageHandler() {

    }

    @Override
    public void onMessage(Message message, MessageContext messageContext) {
        if (message instanceof MessageRequest) {

        } else {
            log.trace("Can only handle message requests, but received: \n{}", message);
        }
    }
}

package dk.kb.bitrepository.mediator;

import dk.kb.bitrepository.mediator.communication.MessageMediator;
import dk.kb.bitrepository.mediator.communication.MessageRequestMediator;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;

public class MediatorPillar {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final MessageBus messageBus;
    private final Configuration config;
    private final MessageMediator messageMediator;
    private final PillarContext context;

    /**
     * Rough sketch:
     * Should initialize all necessary stuff so that we listen for new requests on the messagebus (or initialize something that does it)
     * Initialize database etc. (collection_id, file_id, file_receival_timestamp, file_encryption_timestamp, crypto_algo, checksum, crypto_checksum)
     * Listen for incoming messages and delegate them to appropriate handlers (spawn new thread for each?)
     * - GetFile: check dao if file exists, propagate message to pillar ("client-side"), and get response back to original client
     * - PutFile: check dao if file already exists (name can't be same right?), put stuff in db, propagate message to pillar, and get response back to original client
     * - GetFileIDs: Just check dao and respond with file IDs from there no?
     * - DeleteFile: Check dao if file exists, propagate message to pillar, and get response back from pillar to client
     */
    public MediatorPillar(MessageBus messageBus, Configuration config) {
        log.debug("Creating mediator pillar");
        this.messageBus = messageBus;
        this.config = config;
        context = new PillarContext(config, messageBus);
        messageMediator = new MessageRequestMediator(messageBus, context);
        messageMediator.startListening();
    }

    public void start() {
        System.out.println(config.getCollections());
    }

    public void shutdown() {
        try {
            messageMediator.close();
            messageBus.close();
        } catch (JMSException e) {
            log.warn("Could not close the messagebus.", e);
        }
    }
}
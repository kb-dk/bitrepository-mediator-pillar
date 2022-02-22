package dk.kb.bitrepository.mediator;

import dk.kb.bitrepository.mediator.communication.MessageRequestDelegator;
import dk.kb.bitrepository.mediator.utils.configurations.PillarConfigurations;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.settings.repositorysettings.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import java.util.ArrayList;
import java.util.List;

public class MediatorPillar {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final MessageBus messageBus;
    private final Settings settings;
    private final MessageRequestDelegator messageRequestDelegator;

    /**
     * Rough sketch:
     * Should initialize all necessary stuff so that we listen for new requests on the messageBus (or initialize something that does it)
     * Initialize database etc. (collection_id, file_id, file_received_timestamp, file_encryption_timestamp, crypto_algo, checksum,
     * crypto_checksum)
     * Listen for incoming messages and delegate them to appropriate handlers (spawn new thread for each?)
     * - GetFile: check dao if file exists, propagate message to pillar ("client-side"), and get response back to original client
     * - PutFile: check dao if file already exists (name can't be same right?), put stuff in db, propagate message to pillar, and get
     * response back to original client
     * - GetFileIDs: Just check dao and respond with file IDs from there no?
     * - DeleteFile: Check dao if file exists, propagate message to pillar, and get response back from pillar to client
     */
    public MediatorPillar(Settings settings, PillarContext pillarContext, PillarConfigurations configs, MessageBus messageBus) {
        log.debug("Creating mediator pillar");
        this.messageBus = messageBus;
        this.settings = settings;
        messageBus.setCollectionFilter(getPillarCollectionIDs());
        messageRequestDelegator = new MessageRequestDelegator(messageBus, pillarContext, configs);
        messageRequestDelegator.startListening();
    }

    public void shutdown() {
        try {
            messageRequestDelegator.stop();
            messageBus.close();
        } catch (JMSException e) {
            log.warn("Could not close the messagebus.", e);
        }
    }

    /**
     * TODO consider moving to utils file
     * Helper method to grab the collection IDs relevant for this/the underlying pillar from the settings.
     *
     * @return List of collection IDs relevant for the pillar (collections contained in this pillar)
     */
    private List<String> getPillarCollectionIDs() {
        String pillarID = settings.getComponentID();
        List<Collection> collections = settings.getCollections();
        List<String> relevantCollectionIDs = new ArrayList<>();
        for (Collection collection : collections) {
            for (String pillar : collection.getPillarIDs().getPillarID()) {
                if (pillarID.equals(pillar)) {
                    relevantCollectionIDs.add(collection.getID());
                    break;
                }
            }
        }
        return relevantCollectionIDs;
    }
}
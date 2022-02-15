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

/**
 * The mediator pillar class.
 */
public class MediatorPillar {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final MessageBus messageBus;
    private final Settings settings;
    private final MessageRequestDelegator messageRequestDelegator;

    /**
     * Constructor instantiating the mediator pillar and registering it as a listener on the message bus
     * @param settings The Settings object containing both the Repository- and ReferenceSettings (TODO replace with just Configurations?)
     * @param pillarContext The pillar context containing necessary components.
     * @param configs The configurations specific to the mediator pillar (TODO see above TODO)
     * @param messageBus The message bus.
     */
    public MediatorPillar(Settings settings, PillarContext pillarContext, PillarConfigurations configs, MessageBus messageBus) {
        log.debug("Creating mediator pillar");
        this.messageBus = messageBus;
        this.settings = settings;
        messageBus.setCollectionFilter(getPillarCollectionIDs());
        messageRequestDelegator = new MessageRequestDelegator(messageBus, pillarContext, configs);
        messageRequestDelegator.startListening();
    }

    /**
     * TODO should use this in the future when nearing a more complete mediator pillar
     * Shuts down all the components of the mediator pillar.
     */
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
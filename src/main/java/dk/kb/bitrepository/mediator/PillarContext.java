package dk.kb.bitrepository.mediator;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.messagebus.MessageBus;

public class PillarContext {
    private final Configuration configuration;
    private final MessageBus messageBus;
    private final Settings pillarSettings;

    public PillarContext(Configuration configuration, MessageBus messageBus) {
        this.configuration = configuration;
        this.pillarSettings = configuration.getPillarSpecificConfiguration();
        this.messageBus = messageBus;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public Settings getPillarSettings() {
        return pillarSettings;
    }
}

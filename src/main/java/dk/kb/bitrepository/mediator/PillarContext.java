package dk.kb.bitrepository.mediator;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.messagebus.MessageBus;

public class PillarContext {
    private final MediatorConfiguration configuration;
    private final MessageBus messageBus;
    private final Settings pillarSettings;

    public PillarContext(MediatorConfiguration configuration, MessageBus messageBus) {
        this.configuration = configuration;
        this.pillarSettings = configuration.getPillarSettings();
        this.messageBus = messageBus;
    }

    public MediatorConfiguration getConfiguration() {
        return configuration;
    }

    public Settings getPillarSettings() {
        return pillarSettings;
    }
}

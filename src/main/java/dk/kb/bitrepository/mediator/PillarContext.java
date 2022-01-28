package dk.kb.bitrepository.mediator;

import dk.kb.bitrepository.mediator.communication.ResponseDispatcher;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.messagebus.MessageBus;

public class PillarContext {
    private final MediatorConfiguration configuration;
    private final MessageBus messageBus;
    private final Settings pillarSettings;
    private final ResponseDispatcher responseDispatcher;

    public PillarContext(MediatorConfiguration configuration, MessageBus messageBus, ResponseDispatcher responseDispatcher) {
        this.configuration = configuration;
        this.pillarSettings = configuration.getPillarSettings();
        this.messageBus = messageBus;
        this.responseDispatcher = responseDispatcher;
    }

    public MediatorConfiguration getConfiguration() {
        return configuration;
    }

    public Settings getPillarSettings() {
        return pillarSettings;
    }

    public ResponseDispatcher getResponseDispatcher() {
        return responseDispatcher;
    }
}

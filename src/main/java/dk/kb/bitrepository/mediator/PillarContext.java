package dk.kb.bitrepository.mediator;

import dk.kb.bitrepository.mediator.communication.ResponseDispatcher;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.messagebus.MessageBus;

public class PillarContext {
    private final MessageBus messageBus;
    private final Settings settings;
    private final ResponseDispatcher responseDispatcher;

    public PillarContext(Settings settings, MessageBus messageBus, ResponseDispatcher responseDispatcher) {
        this.settings = settings;
        this.messageBus = messageBus;
        this.responseDispatcher = responseDispatcher;
    }

    public Settings getRefPillarSettings() {
        return settings;
    }

    public ResponseDispatcher getResponseDispatcher() {
        return responseDispatcher;
    }
}

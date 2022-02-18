package dk.kb.bitrepository.mediator;

import dk.kb.bitrepository.mediator.communication.ResponseDispatcher;
import dk.kb.bitrepository.mediator.database.DatabaseDAO;
import dk.kb.bitrepository.mediator.utils.configurations.Configurations;
import org.bitrepository.protocol.  messagebus.MessageBus;

public class PillarContext {
    private final MessageBus messageBus;
    private final Configurations configurations;
    private final ResponseDispatcher responseDispatcher;
    private final DatabaseDAO dao;

    public PillarContext(Configurations configurations, MessageBus messageBus, ResponseDispatcher responseDispatcher, DatabaseDAO dao) {
        this.configurations = configurations;
        this.messageBus = messageBus;
        this.responseDispatcher = responseDispatcher;
        this.dao = dao;
    }

    public Configurations getConfigurations() {
        return configurations;
    }

    public MessageBus getMessageBus() {
        return messageBus;
    }

    public ResponseDispatcher getResponseDispatcher() {
        return responseDispatcher;
    }

    public DatabaseDAO getDAO() {
        return dao;
    }
}

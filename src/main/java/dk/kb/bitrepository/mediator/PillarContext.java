package dk.kb.bitrepository.mediator;

import dk.kb.bitrepository.mediator.communication.ResponseDispatcher;
import dk.kb.bitrepository.mediator.database.DatabaseDAO;
import dk.kb.bitrepository.mediator.utils.configurations.Configurations;
import org.bitrepository.protocol.messagebus.MessageBus;

/**
 * Aggregate class for containing objects necessary throughout the workflow of handling and performing operations
 * against the underlying pillar.
 */
public class PillarContext {
    private final MessageBus messageBus;
    private final Configurations configurations;
    private final ResponseDispatcher responseDispatcher;
    private final JobScheduler jobScheduler;
    private final DatabaseDAO dao;

    // TODO consider removing message bus - otherwise remove it from other places using context
    public PillarContext(Configurations configurations, MessageBus messageBus, ResponseDispatcher responseDispatcher, JobScheduler jobScheduler, DatabaseDAO dao) {
        this.configurations = configurations;
        this.messageBus = messageBus;
        this.responseDispatcher = responseDispatcher;
        this.jobScheduler = jobScheduler;
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

    public JobScheduler getJobScheduler() {
        return jobScheduler;
    }

    public DatabaseDAO getDAO() {
        return dao;
    }
}

package dk.kb.bitrepository.mediator;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JobScheduler {
    private ExecutorService threadPool;

    public JobScheduler(int threadPoolSize) {
        threadPool = Executors.newFixedThreadPool(threadPoolSize);
    }

    // TODO handle deferring jobs
}

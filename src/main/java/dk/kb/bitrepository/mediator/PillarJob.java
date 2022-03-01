package dk.kb.bitrepository.mediator;

import org.bitrepository.bitrepositorymessages.MessageRequest;

public interface PillarJob {
    /**
     * Execute the actual job.
     */
    void executeJob();

    /**
     * Indicate whether the job has actually finished.
     * @return boolean indicating finished state.
     */
    boolean isFinished();

    /**
     * Get the request matching the job.
     * @return The request for the job.
     */
    MessageRequest getRequest();
}

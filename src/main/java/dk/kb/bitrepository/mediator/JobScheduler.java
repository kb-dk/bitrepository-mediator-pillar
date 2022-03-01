package dk.kb.bitrepository.mediator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Class for scheduling jobs to carry out operations on the mediator-/underlying pillar.
 */
public class JobScheduler {
    private static final Logger log = LoggerFactory.getLogger(JobScheduler.class);
    private final ExecutorService threadPool;
    private final BlockingQueue<PillarJob> jobQueue;
    private int jobCount;

    /**
     * Constructor.
     * @param threadPoolSize Size of the thread pool, i.e. how many threads to keep running for carrying out jobs.
     */
    public JobScheduler(int threadPoolSize) {
        threadPool = Executors.newFixedThreadPool(threadPoolSize);
        jobQueue = new LinkedBlockingQueue<>();
        jobCount = 0;
        startThreads(threadPoolSize);
    }

    /**
     * Start the threads in the thread pool.
     * @param threadPoolSize How many threads to start.
     */
    private void startThreads(int threadPoolSize) {
        for (int i = 0; i < threadPoolSize; i++) {
            threadPool.execute(new JobProcessor());
        }
    }

    /**
     * Submit a job to the job queue.
     * @param job The job to submit
     */
    public void submitJob(PillarJob job) {
        boolean submitted = jobQueue.offer(job);
        if (submitted) {
            incrementJobCount();
            log.debug("Submitted job {} ({})", job.getClass().getSimpleName(), job.getRequest().getCorrelationID());
        } else {
            // TODO as the blocking queue has no set capacity, submissions should never fail - this implementation follows sbpillar's
            throw new IllegalStateException("Work queue full");
        }
    }

    /**
     * Get the number of currently queued and active jobs.
     * @return the sum of jobs queued and active.
     */
    public int getJobCount() {
        synchronized(this) {
            return jobCount;
        }
    }

    /**
     * Method to increment the job count when a new job is added.
     */
    private void incrementJobCount() {
        synchronized(this) {
            jobCount++;
        }
    }

    /**
     * Method to decrement the job count when a job has finished its work.
     */
    private void decrementJobCount() {
        synchronized(this) {
            jobCount--;
        }
    }

    /**
     * Inner class that handles the actual execution of jobs in the work queue.
     */
    private class JobProcessor implements Runnable {
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    PillarJob job = jobQueue.take();
                    job.executeJob();
                    if (!job.isFinished()) { // TODO check state?
                        // TODO handle deferring jobs (or just resubmit?)
                        System.out.println("Deferring job " + job.getRequest().getCorrelationID());
                    } else {
                        decrementJobCount();
                        log.debug("Finished job {} ({})", job.getClass().getSimpleName(), job.getRequest().getCorrelationID());
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

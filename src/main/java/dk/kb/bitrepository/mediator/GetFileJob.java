package dk.kb.bitrepository.mediator;

import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.bitrepositorymessages.MessageRequest;

import java.util.concurrent.ThreadLocalRandom;

public class GetFileJob implements PillarJob {
    private boolean jobDone = false;
    private final GetFileRequest request;

    public GetFileJob(GetFileRequest request) {
        this.request = request;
    }

    public void executeJob() {
        // TODO do actual getfile stuff
        try {
            Thread.sleep(5000);
            /*if (ThreadLocalRandom.current().nextInt(2) == 0) {
                throw new InterruptedException();
            }*/
            System.out.println("Done getting file");
            jobDone = true;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isFinished() {
        return jobDone;
    }

    @Override
    public MessageRequest getRequest() {
        return request;
    }
}

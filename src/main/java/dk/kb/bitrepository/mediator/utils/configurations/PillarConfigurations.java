package dk.kb.bitrepository.mediator.utils.configurations;

public final class PillarConfigurations {
    private String privateMessageDestination;
    private String mediatorPillarID;
    private int jobSchedulerThreadCount;

    public String getMediatorPillarID() {
        return mediatorPillarID;
    }

    public String getPrivateMessageDestination() {
        return privateMessageDestination;
    }

    public void setMediatorPillarID(String mediatorPillarID) {
        this.mediatorPillarID = mediatorPillarID;
    }

    public void setPrivateMessageDestination(String privateMessageDestination) {
        this.privateMessageDestination = privateMessageDestination;
    }

    public int getJobSchedulerThreadCount() {
        return jobSchedulerThreadCount;
    }

    public void setJobSchedulerThreadCount(int jobSchedulerThreadCount) {
        this.jobSchedulerThreadCount = jobSchedulerThreadCount;
    }

    @Override
    public String toString() {
        return "PillarConfigurations{" +
                "mediatorPillarID='" + mediatorPillarID + "'" +
                "privateMessageDestination='" + privateMessageDestination + "'" +
                '}';
    }
}

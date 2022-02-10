package dk.kb.bitrepository.mediator.utils.configurations;

public final class PillarConfigurations {
    private String privateMessageDestination;
    private String mediatorPillarID;

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

    @Override
    public String toString() {
        return "PillarConfigurations{" +
                "mediatorPillarID='" + mediatorPillarID + "'" +
                "privateMessageDestination='" + privateMessageDestination + "'" +
                '}';
    }
}

package dk.kb.bitrepository.mediator.utils.configurations;

public final class PillarConfigurations {
    private String privateMessageDestination;

    public String getPrivateMessageDestination() {
        return privateMessageDestination;
    }

    public void setPrivateMessageDestination(String privateMessageDestination) {
        this.privateMessageDestination = privateMessageDestination;
    }

    @Override
    public String toString() {
        return "PillarConfigurations{" +
                "privateMessageDestination='" + privateMessageDestination + '\'' +
                '}';
    }
}

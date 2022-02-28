package dk.kb.bitrepository.mediator.utils.configurations;

public final class PillarConfigurations {
    private String privateMessageDestination;
    private String mediatorPillarID;
    private String unencryptedFilesPath;
    private String encryptedFilesPath;

    public String getMediatorPillarID() {
        return mediatorPillarID;
    }

    public String getPrivateMessageDestination() {
        return privateMessageDestination;
    }

    public String getUnencryptedFilesPath() {
        return unencryptedFilesPath;
    }

    public String getEncryptedFilesPath() {
        return encryptedFilesPath;
    }

    public void setMediatorPillarID(String mediatorPillarID) {
        this.mediatorPillarID = mediatorPillarID;
    }

    public void setPrivateMessageDestination(String privateMessageDestination) {
        this.privateMessageDestination = privateMessageDestination;
    }

    public void setUnencryptedFilesPath(String unencryptedFilesPath) {
        this.unencryptedFilesPath = unencryptedFilesPath;
    }

    public void setEncryptedFilesPath(String encryptedFilesPath) {
        this.encryptedFilesPath = encryptedFilesPath;
    }

    @Override
    public String toString() {
        return "PillarConfigurations{" +
                "privateMessageDestination='" + privateMessageDestination + '\'' +
                ", mediatorPillarID='" + mediatorPillarID + '\'' +
                ", unencryptedFilesPath='" + unencryptedFilesPath + '\'' +
                ", encryptedFilesPath='" + encryptedFilesPath + '\'' +
                '}';
    }
}

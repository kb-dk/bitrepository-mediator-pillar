package dk.kb.bitrepository.mediator.utils.configurations;

public final class CryptoConfigurations {
    private String algorithm;
    private String password;

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "CryptoConfigurations{" +
                "algorithm='" + algorithm + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}

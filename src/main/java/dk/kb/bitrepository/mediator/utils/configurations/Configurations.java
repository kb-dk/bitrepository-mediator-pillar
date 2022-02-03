package dk.kb.bitrepository.mediator.utils.configurations;

public final class Configurations {
    private PillarConfigurations pillarConfig;
    private DatabaseConfigurations databaseConfig;
    private CryptoConfigurations cryptoConfig;

    public PillarConfigurations getPillarConfig() {
        return pillarConfig;
    }

    public DatabaseConfigurations getDatabaseConfig() {
        return databaseConfig;
    }

    public CryptoConfigurations getCryptoConfig() {
        return cryptoConfig;
    }

    public void setPillarConfig(PillarConfigurations pillarConfigurations) {
        this.pillarConfig = pillarConfigurations;
    }

    public void setDatabaseConfig(DatabaseConfigurations dbConfig) {
        this.databaseConfig = dbConfig;
    }

    public void setCryptoConfig(CryptoConfigurations cryptoConfig) {
        this.cryptoConfig = cryptoConfig;
    }

    @Override
    public String toString() {
        return "Configurations{" +
                "pillarConfig=" + pillarConfig +
                ", databaseConfig=" + databaseConfig +
                ", cryptoConfig=" + cryptoConfig +
                '}';
    }
}

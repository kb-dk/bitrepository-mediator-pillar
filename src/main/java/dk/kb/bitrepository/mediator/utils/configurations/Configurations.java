package dk.kb.bitrepository.mediator.utils.configurations;

import org.bitrepository.common.settings.Settings;

public final class Configurations {
    private PillarConfigurations pillarConfig;
    private DatabaseConfigurations databaseConfig;
    private CryptoConfigurations cryptoConfig;
    private Settings pillarSettings;

    public PillarConfigurations getPillarConfig() {
        return pillarConfig;
    }

    public DatabaseConfigurations getDatabaseConfig() {
        return databaseConfig;
    }

    public CryptoConfigurations getCryptoConfig() {
        return cryptoConfig;
    }

    public Settings getPillarSettings() {
        return pillarSettings;
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

    public void setPillarSettings(Settings pillarSettings) {
        this.pillarSettings = pillarSettings;
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

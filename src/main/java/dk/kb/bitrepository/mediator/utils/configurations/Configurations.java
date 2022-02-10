package dk.kb.bitrepository.mediator.utils.configurations;

import org.bitrepository.common.settings.Settings;

public final class Configurations {
    private PillarConfigurations pillarConfig;
    private DatabaseConfigurations databaseConfig;
    private CryptoConfigurations cryptoConfig;
    private Settings refPillarSettings;

    public PillarConfigurations getPillarConfig() {
        return pillarConfig;
    }

    public DatabaseConfigurations getDatabaseConfig() {
        return databaseConfig;
    }

    public CryptoConfigurations getCryptoConfig() {
        return cryptoConfig;
    }

    public Settings getRefPillarSettings() {
        return refPillarSettings;
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

    public void setRefPillarSettings(Settings refPillarSettings) {
        this.refPillarSettings = refPillarSettings;
    }

    @Override
    public String toString() {
        return "Configurations{" +
                "pillarConfig=" + pillarConfig +
                ", databaseConfig=" + databaseConfig +
                ", cryptoConfig=" + cryptoConfig +
                ", refPillarSettings=" + refPillarSettings +
                '}';
    }
}

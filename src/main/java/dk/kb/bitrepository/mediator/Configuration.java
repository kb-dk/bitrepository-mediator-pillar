package dk.kb.bitrepository;

import dk.kb.util.yaml.YAML;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.settings.referencesettings.ReferenceSettings;
import org.bitrepository.settings.repositorysettings.Collection;
import org.bitrepository.settings.repositorysettings.RepositorySettings;

import java.util.List;

public class Configuration {
    private final YAML intermediatorConf;
    private final Settings pillarSpecificConfiguration;

    public Configuration(YAML intermediatorConf, Settings pillarSpecificConfiguration) {
        this.intermediatorConf = intermediatorConf;
        this.pillarSpecificConfiguration = pillarSpecificConfiguration;
    }

    public String getComponentID() {
        return intermediatorConf.getString(ConfigConstants.COMPONENT_ID);
    }

    public String getCryptoAlgorithm() {
        return intermediatorConf.getString(ConfigConstants.CRYPTO_ALGORITHM);
    }

    /**
     * @return the first Collections ID as a list of Collections.
     * TODO singular vs. plural
     */
    public List<Collection> getCollections() {
        return pillarSpecificConfiguration.getCollections();
    }

    /**
     * Wraps the {@link org.bitrepository.settings.repositorysettings.ProtocolSettings#getAlarmDestination()} method.
     * @return the alarm destination
     */
    public String getAlarmDestination() {
        return getRepositorySettings().getProtocolSettings().getAlarmDestination();
    }

    public Settings getPillarSpecificConfiguration() {
        return pillarSpecificConfiguration;
    }

    /**
     * @return The settings specific to the reference code for a collection.
     */
    public ReferenceSettings getReferenceSettings() {
        return pillarSpecificConfiguration.getReferenceSettings();
    }

    /**
     * @return The standard settings for a collection.
     */
    public RepositorySettings getRepositorySettings() {
        return pillarSpecificConfiguration.getRepositorySettings();
    }
}

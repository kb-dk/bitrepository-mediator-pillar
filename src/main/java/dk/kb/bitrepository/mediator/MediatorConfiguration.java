package dk.kb.bitrepository.mediator;

import dk.kb.util.yaml.YAML;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.settings.referencesettings.ReferenceSettings;
import org.bitrepository.settings.repositorysettings.Collection;
import org.bitrepository.settings.repositorysettings.RepositorySettings;

import java.util.List;

public class MediatorConfiguration {
    private final YAML intermediatorConf;
    private final Settings pillarSettings;

    public MediatorConfiguration(YAML intermediatorConf, Settings pillarSettings) {
        this.intermediatorConf = intermediatorConf;
        this.pillarSettings = pillarSettings;
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
        return pillarSettings.getCollections();
    }

    /**
     * Wraps the {@link org.bitrepository.settings.repositorysettings.ProtocolSettings#getAlarmDestination()} method.
     * @return the alarm destination
     */
    public String getAlarmDestination() {
        return getRepositorySettings().getProtocolSettings().getAlarmDestination();
    }

    public Settings getPillarSettings() {
        return pillarSettings;
    }

    /**
     * @return The settings specific to the reference code for a collection.
     */
    public ReferenceSettings getReferenceSettings() {
        return pillarSettings.getReferenceSettings();
    }

    /**
     * @return The standard settings for a collection.
     */
    public RepositorySettings getRepositorySettings() {
        return pillarSettings.getRepositorySettings();
    }
}

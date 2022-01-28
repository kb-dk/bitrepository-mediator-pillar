package dk.kb.bitrepository.mediator;

import dk.kb.util.yaml.YAML;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.settings.referencesettings.ReferenceSettings;
import org.bitrepository.settings.repositorysettings.Collection;
import org.bitrepository.settings.repositorysettings.RepositorySettings;

import java.util.List;

public class MediatorConfiguration {
    private final YAML mediatorYAML;
    private final Settings pillarSettings;

    public MediatorConfiguration(YAML mediatorYAML, Settings pillarSettings) {
        this.mediatorYAML = mediatorYAML;
        this.pillarSettings = pillarSettings;
    }

    public String getComponentID() {
        return pillarSettings.getComponentID();
    }

    public String getPrivateMessageDestination() {
        return mediatorYAML.getString(ConfigConstants.PRIVATE_MESSAGE_DESTINATION);
    }

    public String getRepositoryMessageDestination() {
        return pillarSettings.getCollectionDestination();
    }

    public String getCryptoAlgorithm() {
        return mediatorYAML.getString(ConfigConstants.CRYPTO_ALGORITHM);
    }

    /**
     * @return the first Collections ID as a list of Collections.
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

package dk.kb.bitrepository.mediator.utils.configurations;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.settings.referencesettings.ReferenceSettings;
import org.bitrepository.settings.repositorysettings.Collection;
import org.bitrepository.settings.repositorysettings.RepositorySettings;

import java.util.List;

public class PillarSettings {
    private final Settings pillarSettings;

    public PillarSettings(Settings pillarSettings) {
        this.pillarSettings = pillarSettings;
    }

    public String getComponentID() {
        return pillarSettings.getComponentID();
    }

    public String getRepositoryMessageDestination() {
        return pillarSettings.getCollectionDestination();
    }

    /**
     * @return the first Collections ID as a list of Collections.
     */
    public List<Collection> getCollections() {
        return pillarSettings.getCollections();
    }

    /**
     * Wraps the {@link org.bitrepository.settings.repositorysettings.ProtocolSettings#getAlarmDestination()} method.
     *
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

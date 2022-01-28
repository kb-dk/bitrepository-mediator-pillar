package dk.kb.bitrepository.mediator.utils;

import dk.kb.bitrepository.mediator.MediatorConfiguration;
import dk.kb.util.yaml.YAML;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.SettingsProvider;
import org.bitrepository.common.settings.XMLFileSettingsLoader;

import java.io.IOException;

public class TestUtils {
    public static MediatorConfiguration loadConfiguration(String pillarID, String pathToConfiguration) throws IOException {
        YAML mediatorConfig = new YAML(pathToConfiguration + "/mediatorConfig.yaml");
        SettingsProvider settingsProvider = new SettingsProvider(new XMLFileSettingsLoader(pathToConfiguration), pillarID);
        Settings settings = settingsProvider.getSettings();
        return new MediatorConfiguration(mediatorConfig, settings);
    }
}

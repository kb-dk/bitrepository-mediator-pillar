package dk.kb.bitrepository.mediator.utils;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.SettingsProvider;
import org.bitrepository.common.settings.XMLFileSettingsLoader;

import java.io.IOException;

public class TestUtils {
    public static Settings loadRefPillarSettings(String pillarID, String pathToConfiguration) throws IOException {
        SettingsProvider settingsProvider = new SettingsProvider(new XMLFileSettingsLoader(pathToConfiguration), pillarID);
        return settingsProvider.getSettings();
    }
}

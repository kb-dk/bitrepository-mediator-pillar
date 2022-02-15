package dk.kb.bitrepository.mediator.utils.configurations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;

/**
 * Class for loading a Configurations object from a YAML file.
 */
public class ConfigurationsLoader {
    private static Configurations config = null;
    private static final Logger log = LoggerFactory.getLogger(ConfigurationsLoader.class);

    /**
     * Constructor initializing the Configurations instance.
     *
     * @throws IOException If the Configurations can not be loaded from the provided path.
     */
    public ConfigurationsLoader(String configPath) throws IOException {
        loadYAMLConfigurations(configPath);
        log.info("Config initialized with path: {}", configPath);
    }

    /**
     * @return The Configurations.
     */
    public Configurations getConfigurations() {
        if (config == null) {
            log.error("The Configurations have not been setup.");
        }
        return config;
    }

    /**
     * Creates a configurations object using the data from the configs file.
     * @throws IOException If configuration file does not exist or otherwise fails to load from disk.
     */
    private void loadYAMLConfigurations(String configPath) throws IOException {
        Yaml yaml = new Yaml();

        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(configPath)) {
            config = yaml.loadAs(stream, Configurations.class);
        }
    }
}

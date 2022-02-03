package dk.kb.bitrepository.mediator.utils.configurations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ConfigurationHandler {
    private final String configPath;
    private static Configurations config = null;
    private static final Logger log = LoggerFactory.getLogger(ConfigurationHandler.class);

    public ConfigurationHandler(String configPath) {
        this.configPath = configPath;
        ensureConfigsExist();
        setConfigurations();
        log.info("Config initialized with path: {}", configPath);
    }

    /**
     * @return The Configurations.
     */
    public static Configurations getConfigurations() {
        if (config == null) {
            log.error("The Configurations have not been setup.");
        }
        return config;
    }

    /**
     * Creates a configurations object using the data from the configs file.
     */
    private void setConfigurations() {
        Yaml yaml = new Yaml();

        try {
            InputStream stream = new FileInputStream(configPath);
            config = yaml.loadAs(stream, Configurations.class);
        } catch (FileNotFoundException e) {
            log.error("Could not read config file.", e);
            System.exit(0);
        }
    }

    /**
     * Used to check if the configuration file exists, if it doesn't it will be created.
     */
    private void ensureConfigsExist() {
        if (!Files.exists(Paths.get(configPath))) {
            log.info("Config file doesn't exist - creating it now.");
            try {
                Files.createFile(Paths.get(configPath));
            } catch (IOException e) {
                log.error("Couldn't create the config file at the given path." + e);
            }
            log.info("Config file has been created.");
        }
    }

}

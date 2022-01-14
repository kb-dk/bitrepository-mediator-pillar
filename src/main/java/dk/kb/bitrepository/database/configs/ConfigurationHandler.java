package dk.kb.bitrepository.database.configs;

import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.iv.RandomIvGenerator;
import org.jasypt.properties.EncryptableProperties;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class ConfigurationHandler {
    private final String configPath = "src/main/java/dk/kb/bitrepository/database/configs/configurations.properties";
    private final PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
    private Properties properties = null;
    // TODO: Missing logger

    /**
     * Initializes the config file. If the file doesn't exist, it will be created.
     * When the file is sure to exist, information like Database Name, URL, Port and Driver
     * will be stored in the config file.
     *
     * @param dbName The name of the Database.
     * @param dbURL  The URL path of the Database.
     * @param port   The port to use with the URL to connect to the Database.
     */
    public void initConfig(String dbName, String dbURL, String port) {
        boolean configExists = Files.exists(Paths.get(configPath));
        if (!configExists) {
            System.out.println("Config file doesn't exist - creating it now.");
            try {
                Files.createFile(Paths.get(configPath));
            } catch (IOException e) {
                System.out.println("Couldn't create the config file at the given path." + e);
            }
            System.out.println("Config file has been created.");
        }

        if (!encryptor.isInitialized()) {
            initEncryptor();
        }

        System.out.println("Trying to set driver info, database name and URL.");
        storeProperty("db.driver", "org.postgresql.Driver");
        storeProperty("db.name", dbName);
        storeProperty("db.url", dbURL);
        storeProperty("db.port", port);
        System.out.println("Database name and URL has been added to the config file.");
    }

    /**
     * Initializes the encryptor that will be used to encrypt data before it is stored in the config file.
     */
    private void initEncryptor() {
        encryptor.setAlgorithm("PBEWithHmacSHA512AndAES_256");
        encryptor.setIvGenerator(new RandomIvGenerator());
        encryptor.setPoolSize(4);
        //TODO: private String PBES_PWD = System.getenv("JASYPT_PWD"); //(load from env)
        String ENC_PW = "testtesttest";
        encryptor.setPassword(ENC_PW);
        properties = new EncryptableProperties(encryptor);
        try {
            properties.load(new FileInputStream(configPath));
        } catch (IOException e) {
            System.out.println("An error occurred trying to load the config file. " + e);
        }
    }

    /**
     * This method encrypts the 'username' and 'password' using Jasypt, and stores it in the config file.
     *
     * @param username Username to encrypt and store.
     * @param password Password to encrypt and store.
     */
    public void encryptLoginInformation(String username, String password) {
        if (!encryptor.isInitialized()) {
            initEncryptor();
        }
        String encryptedUsername = "ENC(" + encryptor.encrypt(username) + ")";
        String encryptedPassword = "ENC(" + encryptor.encrypt(password) + ")";
        storeProperty("db.username", encryptedUsername);
        storeProperty("db.password", encryptedPassword);
    }

    /**
     * Used to store a property in the config file.
     *
     * @param key   The key to store under, e.g. 'db.username'.
     * @param input The input to be stored under the chosen key.
     */
    private void storeProperty(String key, String input) {
        properties.setProperty(key, input);
        try {
            properties.store(new FileWriter(configPath), "Configurations for the JDBC database.");
        } catch (IOException e) {
            System.out.println("An error occurred trying to write to the config file. " + e);
        }
    }

    /**
     * Called to get a specific property from the configurations file, omit "db." in the key parameter.
     * The most common properties would be "username" or "password".
     * Check the configurations.properties file for all the options.
     *
     * @param key Is the key to get, i.e. username
     * @return Returns the value of the property.
     */
    public String getProperty(final String key) throws IOException {
        if (!encryptor.isInitialized()) {
            initEncryptor();
        }
        return properties.getProperty("db." + key);
    }

}

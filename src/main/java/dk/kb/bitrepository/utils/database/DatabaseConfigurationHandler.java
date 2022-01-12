package dk.kb.bitrepository.utils.database;

import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.iv.RandomIvGenerator;
import org.jasypt.properties.EncryptableProperties;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class DatabaseConfigurationHandler {
    private final String path = "src/main/java/dk/kb/bitrepository/utils/database/configurations.properties";
    PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
    Properties properties = null;
    // TODO: Missing logger + documentation

    public void initConfig(String dbName, String dbURL) {
        boolean configExists = Files.exists(Paths.get(path));
        if (!configExists) {
            System.out.println("Config file doesn't exist - creating it now.");
            try {
                Files.createFile(Paths.get(path));
            } catch (IOException e) {
                System.out.println("Couldn't create the config file at the given path." + e);
            }
            System.out.println("Config file has been created.");
        }

        if (!encryptor.isInitialized()) {
            initEncryptor();
        }

        System.out.println("Trying to set database name and URL.");
        storeProperty("db.name", dbName);
        storeProperty("db.url", dbURL);
        System.out.println("Database name and URL has been added to the config file.");
    }

    private void initEncryptor() {
        encryptor.setAlgorithm("PBEWithHmacSHA512AndAES_256");
        encryptor.setIvGenerator(new RandomIvGenerator());
        encryptor.setPoolSize(4);
        //TODO: private String PBES_PWD = System.getenv("JASYPT_PWD"); //(load from env)
        String ENC_PW = "testtesttest";
        encryptor.setPassword(ENC_PW);
        properties = new EncryptableProperties(encryptor);
        try {
            properties.load(new FileInputStream(path));
        } catch (IOException e) {
            System.out.println("An error occurred trying to load the config file. " + e);
        }
    }

    public void encryptLoginInformation(String username, String password) {
        if (!encryptor.isInitialized()) {
            initEncryptor();
        }
        String encryptedUsername = "ENC(" + encryptor.encrypt(username) + ")";
        String encryptedPassword = "ENC(" + encryptor.encrypt(password) + ")";
        storeProperty("db.username", encryptedUsername);
        storeProperty("db.password", encryptedPassword);
    }

    private void storeProperty(String key, String input) {
        properties.setProperty(key, input);
        try {
            properties.store(new FileWriter(path), "Configurations for the JDBC database.");
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

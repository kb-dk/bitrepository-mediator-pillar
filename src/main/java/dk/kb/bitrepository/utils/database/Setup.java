package dk.kb.bitrepository.utils.database;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Setup {
    private static final ConfigurationHandler configs = new ConfigurationHandler();
    private static final Logger log = LoggerFactory.getLogger(Setup.class);

    public static void main(String @NotNull [] args) {
        String dbName = args[0];
        String dbURL = args[1];
        configs.initConfig(dbName, dbURL);
        String username = args[2];
        String password = args[3];
        configs.encryptLoginInformation(username, password);
        System.out.println("Username and Password saved to configs successfully.");
    }
}

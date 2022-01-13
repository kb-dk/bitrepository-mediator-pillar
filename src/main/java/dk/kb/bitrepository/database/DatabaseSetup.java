package dk.kb.bitrepository.database;

import dk.kb.bitrepository.database.configs.ConfigurationHandler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseSetup {
    private static final ConfigurationHandler configs = new ConfigurationHandler();
    //TODO: Use the logger + documentation
    private static final Logger log = LoggerFactory.getLogger(DatabaseSetup.class);

    public static void main(String @NotNull [] args) {
        if (args.length < 5) {
            System.out.println("Takes the following 5 arguments:\n\tName URL Port Username Password");
            System.exit(0);
        }

        configs.initConfig(args[0], args[1], args[2]);
        configs.encryptLoginInformation(args[3], args[4]);
        System.out.println("Configuration file has been created successfully.");

        DatabaseUtils.createTables();
    }
}

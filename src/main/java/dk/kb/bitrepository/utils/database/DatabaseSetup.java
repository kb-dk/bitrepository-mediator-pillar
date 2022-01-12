package dk.kb.bitrepository.utils.database;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseSetup {
    private static final ConfigurationHandler configs = new ConfigurationHandler();
    //TODO: Use the logger + documentation
    private static final Logger log = LoggerFactory.getLogger(DatabaseSetup.class);

    public static void main(String @NotNull [] args) {
        if (args.length < 4) {
            System.out.println("Takes the following 4 arguments:\n\tDB-Name DB-URL Username Password");
            System.exit(0);
        }

        String dbName = args[0];
        String dbURL = args[1];
        String username = args[2];
        String password = args[3];

        configs.initConfig(dbName, dbURL);
        configs.encryptLoginInformation(username, password);
        System.out.println("Configuration file has been created successfully.");

        DatabaseUtils.createTables();

    }
}

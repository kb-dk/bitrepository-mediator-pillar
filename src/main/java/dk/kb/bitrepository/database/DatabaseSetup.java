package dk.kb.bitrepository.database;

import dk.kb.bitrepository.database.configs.ConfigurationHandler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseSetup {
    private static final ConfigurationHandler configs = new ConfigurationHandler();
    private static final Logger log = LoggerFactory.getLogger(DatabaseSetup.class);

    /**
     * Run this main method with the following 5 arguments:
     * Database-name, URL, Port, Username, and Password.
     * </p>
     * This will initialize the configurations file with encrypted username, and password,
     * while the other information stays as cleartext.
     * </p
     * If the main method is run with 0 arguments, and a configurations file exists,
     * then it will simply create the tables 'files' and 'enc_parameters'.
     *
     * @param args Takes either 0 or these 5 arguments : Name, Url, Port, Username, and Password.
     */
    public static void main(String @NotNull [] args) {
        if (args.length != 0 && args.length < 5) {
            System.out.println("Takes the following 5 arguments:\n\tName URL Port Username Password");
            System.exit(0);
        }
        if (args.length >= 5) {
            configs.initConfig(args[0], args[1], args[2]);
            configs.encryptLoginInformation(args[3], args[4]);
            System.out.println("Configurations file has been created successfully.");
            log.info("Configurations file has been created successfully.");
        }

        if (configs.configExists()) {
            DatabaseUtils.createTables();
            System.out.println("Tables have been created.");
            log.info("Tables 'files' and 'enc_parameters' created successfully.");
        }
    }
}

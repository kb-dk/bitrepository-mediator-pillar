package dk.kb.bitrepository.mediator.database;

import dk.kb.bitrepository.mediator.database.configs.ConfigurationHandler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;

public class DatabaseSetup {
    private static final Logger log = LoggerFactory.getLogger(DatabaseSetup.class);

    /**
     * Run this main method with the following 5 arguments:
     * Database-name, URL, Port, Username, Password, and AESPassword.
     * </p>
     * This will initialize the configurations file with encrypted username and passwords,
     * while the other information stays as cleartext.
     * Example of parameters:
     * </p>
     * testdb jdbc:postgresql://localhost 5432 testuser testpw testcryptopw
     * </p
     * If the main method is run with 0 arguments, and a configurations file exists,
     * then it will simply create the tables 'files' and 'enc_parameters'.
     *
     * @param args Takes either 0 or these 6 arguments : Name, Url, Port, Username, Password, and AESPassword.
     */
    public static void main(String @NotNull [] args) {
        if (args.length != 0 && args.length < 5) {
            System.out.println("Takes the following 6 arguments:\n\tName URL Port Username Password AESPassword");
            System.exit(0);
        }
        if (args.length >= 6) {
            ConfigurationHandler configs = new ConfigurationHandler(args[0], args[1], args[2]);
            configs.encryptLoginInformation(args[3], args[4], args[5]);
            System.out.println("Configurations file has been created successfully.");
            log.info("Configurations file has been created successfully.");
        }

        if (Files.exists(Path.of("src/main/java/dk/kb/bitrepository/mediator/database/configs/configurations.properties"))) {
            DatabaseUtils.createTables();
            System.out.println("Tables have been created.");
            log.info("Tables 'files' and 'enc_parameters' created successfully.");
        }
    }
}

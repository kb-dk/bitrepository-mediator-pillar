package dk.kb.bitrepository.mediator.database;

import dk.kb.bitrepository.mediator.MediatorPillarComponentFactory;
import dk.kb.bitrepository.mediator.utils.configurations.Configurations;

import java.io.IOException;
import java.sql.SQLException;

public class DatabaseSetup {
    public static void main(String[] args) throws IOException, SQLException {
        if (args.length < 1 || args.length > 2) {
            System.out.println("Error: Expected 1-2 arguments:\n\tpathToConfigurationDir pathToCreationScript (optional)");
            System.exit(0);
        }
        Configurations configs = MediatorPillarComponentFactory.getMediatorConfigurations(args[0]);
        if (args.length == 1) {
            DatabaseUtils.runSqlFromFile(configs.getDatabaseConfig(), DatabaseConstants.DEFAULT_DATABASE_CREATION_SCRIPT);
        }
        if (args.length == 2) {
            DatabaseUtils.runSqlFromFile(configs.getDatabaseConfig(), args[1]);
        }
    }
}

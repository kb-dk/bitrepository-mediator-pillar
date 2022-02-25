package dk.kb.bitrepository.mediator.database;

import dk.kb.bitrepository.mediator.utils.configurations.DatabaseConfigurations;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseTestUtils {
    /**
     * Delete tables 'enc_parameters' and 'files'. Used for testing purposes.
     *
     * @throws SQLException Throws a SQLException when failing to connect to the database server.
     */
    public static void dropTables(DatabaseConfigurations testConfig) throws SQLException {
        try (Connection connection = DatabaseUtils.getNonPooledConnection(testConfig)) {
            Statement statement = connection.createStatement();
            statement.executeUpdate("DROP TABLE IF EXISTS enc_parameters");
            statement.executeUpdate("DROP TABLE IF EXISTS files");
        }
    }

    /**
     * Creates the tables by running the script 'create_tables.sql'.
     *
     * @param testConfig An instance of Configurations, can be created using the {@link dk.kb.bitrepository.mediator.TestingDAO} class.
     * @throws SQLException An exception that the SQL was malformed or otherwise corrupt.
     * @throws IOException  An exception thrown if the file was not found, or damaged.
     */
    public static void createTables(DatabaseConfigurations testConfig) throws SQLException, IOException {
        DatabaseUtils.runSqlFromFile(testConfig, DatabaseConstants.DEFAULT_DATABASE_CREATION_SCRIPT);
    }
}

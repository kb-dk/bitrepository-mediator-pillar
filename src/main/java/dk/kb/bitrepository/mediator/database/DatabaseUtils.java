package dk.kb.bitrepository.mediator.database;

import dk.kb.bitrepository.mediator.MediatorConfiguration;
import dk.kb.bitrepository.mediator.database.configs.ConfigurationHandler;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.OffsetDateTime;

public class DatabaseUtils {
    private static final Logger log = LoggerFactory.getLogger(DatabaseUtils.class);
    private static String databaseURL;
    private static String password;
    private static String username;
    private static ConfigurationHandler configs = null;

    private DatabaseUtils() {
    }

    /**
     * Initializes the configurations in the code, by extracting them from the configurations file.
     */
    private static void initDatabaseConfigurations() {
        configs = new ConfigurationHandler();
        try {
            username = configs.getUsername();
            password = configs.getPassword();
            databaseURL = configs.getDatabaseURL();

        } catch (IOException e) {
            log.error("Could not load the configurations from the configurations file.", e);
        }
    }

    /**
     * Runs an SQL file against the database specified by the pillar configuration.
     */
    public static void runSqlFromFile(MediatorConfiguration pillarConfig, String pathToSqlFile) throws IOException, SQLException {
        String[] queries = parseSqlFile(pathToSqlFile);
        try (Connection connection = getNonPooledConnection(pillarConfig)) {
            executeSQLFromStringArray(connection, queries);
        }
    }

    /**
     * Parses SQL queries from a given .sql file to a String array.
     *
     * @param pathToCreationScript The .sql files path.
     * @return A string array containing the queries parsed from the file.
     */
    @NotNull
    private static String[] parseSqlFile(String pathToCreationScript) throws IOException {
        String[] queries;
        try (InputStream is = DatabaseUtils.class.getClassLoader().getResourceAsStream(pathToCreationScript)) {
            if (is == null) {
                throw new FileNotFoundException("Didn't find any file in classpath corresponding to '" +
                        pathToCreationScript + "'");
            }
            // Splitting the string on ";" to separate requests.
            queries = IOUtils.toString(is, StandardCharsets.UTF_8).split(";");
        }
        return queries;
    }

    public static Connection getNonPooledConnection(MediatorConfiguration pillarConfig) throws SQLException {
        return DriverManager.getConnection(pillarConfig.getDatabaseURL(), pillarConfig.getDatabaseUsername(), pillarConfig.getDatabasePassword());
    }

    /**
     * Connects to the Database through the DriveManager using the configurations set by {@link #initDatabaseConfigurations() initConfigs}.
     *
     * @return The connection established.
     * @throws SQLException Throws an error if the connection could not be established.
     */
    static Connection connect() throws SQLException {
        if (configs == null) {
            log.info("Initializing configurations.");
            initDatabaseConfigurations();
        }
        return DriverManager.getConnection(databaseURL, username, password);
    }

    /**
     * Executes all queries provided in a String array.
     *
     * @param query String array that contains the queries to be executed.
     */
    private static void executeSQLFromStringArray(Connection connection, String[] query) {
        try (Statement statement = connection.createStatement()) {
            log.info("Connection established to the database; trying to execute query.");
            for (String s : query) {
                //Remove spaces to not execute empty statements
                if (!s.trim().equals("")) {
                    //FIXME: Detect "table already exists"?
                    log.info("Executing Query >>>{}", s);
                    statement.executeUpdate(s);
                }
            }
            log.info("Query executed successfully.");
        } catch (SQLException e) {
            log.error("Error in executing SQL query: ", e);
        }
    }

    /**
     * Prepare a statement given a query string and some args.
     * <p>
     * NB: the provided connection is not closed.
     *
     * @param dbConnection The connection to the database.
     * @param query        The query to run.
     * @param args         The args to insert into this query string.
     * @return a prepared statement
     * @throws SQLException          If unable to prepare a statement
     * @throws IllegalStateException if any of the args is of an unknown type
     */
    static PreparedStatement createPreparedStatement(@NotNull Connection dbConnection, String query, Object @NotNull ... args) throws SQLException {
        //log.trace("Preparing the statement: '" + query + "' with arguments '" + Arrays.asList(args) + "'");
        PreparedStatement s = dbConnection.prepareStatement(query);
        int i = 1;
        for (Object arg : args) {
            if (arg instanceof String) {
                s.setString(i, (String) arg);
            } else if (arg instanceof Integer) {
                s.setInt(i, (Integer) arg);
            } else if (arg instanceof Long) {
                s.setLong(i, (Long) arg);
            } else if (arg instanceof Boolean) {
                s.setBoolean(i, (Boolean) arg);
            } else if (arg instanceof OffsetDateTime) {
                s.setObject(i, arg);
            } else if (arg instanceof byte[]) {
                s.setBytes(i, (byte[]) arg);
            } else {
                if (arg == null) {
                    throw new IllegalStateException("Cannot handle a null as argument for SQL query. We can only " + "handle string, int, long, date or boolean args for query: " + query);
                } else {
                    throw new IllegalStateException("Cannot handle type '" + arg.getClass().getName() + "'. We can only " + "handle string, int, long, date or boolean args for query: " + query);
                }
            }
            i++;
        }
        return s;
    }
}

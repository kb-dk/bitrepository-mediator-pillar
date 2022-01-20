package dk.kb.bitrepository.database;

import dk.kb.bitrepository.database.configs.ConfigurationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.time.OffsetDateTime;

public class DatabaseUtils {
    private static String databaseURL;
    private static String password;
    private static String username;
    private static ConfigurationHandler configs = null;
    private static final Logger log = LoggerFactory.getLogger(DatabaseUtils.class);

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
     * Creates the tables as defined in the 'create_tables.sql' file, using the
     * {@link #parseSQL(String filePath) parseSQL} method.
     */
    public static void createTables() {
        String filePath = "src/main/java/dk/kb/bitrepository/database/create_tables.sql";
        String[] queries = parseSQL(filePath);
        executeSQLFromStringArray(queries);
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
     * Parses SQL queries from a given .sql file to a String array.
     *
     * @param filePath The .sql files path.
     * @return A string array containing the queries parsed from the file.
     */
    private static String[] parseSQL(String filePath) {
        String[] queries = new String[0];

        try {
            File file = new File(filePath);
            FileReader reader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(reader);
            StringBuilder stringBuilder = new StringBuilder();
            String currentLine;

            while ((currentLine = bufferedReader.readLine()) != null) {
                stringBuilder.append(currentLine);
            }
            reader.close();

            // Splitting the string on ";" to separate requests.
            queries = stringBuilder.toString().split(";");
        } catch (IOException e) {
            log.error("No file exists at {}", filePath, e);
        }
        return queries;
    }

    /**
     * Executes all queries provided in a String array.
     *
     * @param query String array that contains the queries to be executed.
     */
    private static void executeSQLFromStringArray(String[] query) {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            log.info("Connection established to the database; trying to execute query.");
            for (String s : query) {
                //Remove spaces to not execute empty statements
                if (!s.trim().equals("")) {
                    //FIXME: Detect "table already exists"?
                    statement.executeUpdate(s);
                }
            }
            statement.close();
            log.info("Query executed successfully.");
        } catch (SQLException e) {
            log.error("Error in executing SQL query: ", e);
        }
    }

    /**
     * Delete tables 'enc_parameters' and 'files'. Used for testing purposes.
     *
     * @throws SQLException Throws a SQLException when failing to connect to the database server.
     */
    static void dropTables() throws SQLException {
        Connection connection = connect();
        Statement statement = connection.createStatement();
        statement.executeUpdate("DROP TABLE IF EXISTS enc_parameters");
        statement.executeUpdate("DROP TABLE IF EXISTS files");
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
    static PreparedStatement createPreparedStatement(Connection dbConnection, String query, Object... args) throws SQLException {
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

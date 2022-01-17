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
    private final Logger log = LoggerFactory.getLogger(getClass());
    // TODO: Missing logger + documentation + automatic tests

    public DatabaseUtils() {
    }

    public static void initConfigs() {
        ConfigurationHandler configs = new ConfigurationHandler();
        try {
            username = configs.getProperty("username");
            password = configs.getProperty("password");
            databaseURL = configs.getProperty("url") + ":" + configs.getProperty("port") + "/" + configs.getProperty("name");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void createTables() {
        String filePath = "src/main/java/dk/kb/bitrepository/database/create_tables.sql";
        String[] queries = parseSQL(filePath);
        executeSQLFromFile(queries);
    }

    static Connection connect() throws SQLException {
        if (databaseURL == null) {
            initConfigs();
        }
        return DriverManager.getConnection(databaseURL, username, password);
    }

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
            //log.error("Could not find file {}.", filePath, e);
            System.out.println("No file exists at the provided path.");
        }
        return queries;
    }

    private static void executeSQLFromFile(String[] query) {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            System.out.println("Connection established to the database; trying to execute query.");
            for (String s : query) {
                //Remove spaces to not execute empty statements
                if (!s.trim().equals("")) {
                    // Execute the query
                    //TODO: Detect "table already exists"?
                    statement.executeUpdate(s);
                }
            }
            statement.close();
            System.out.println("Query executed successfully.");
        } catch (SQLException e) {
            //log.error("Error in executing SQL query: ", e);
            System.out.println("Error in executing SQL query: " + e);
        }
    }

    /**
     * Delete tables 'enc_parameters' and 'files'.
     * Used in setup for testing.
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

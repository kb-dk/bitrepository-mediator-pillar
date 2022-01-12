package dk.kb.bitrepository.utils.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Initializer {
    private final Logger log = LoggerFactory.getLogger(Initializer.class);
    private final String dbName;

    public Initializer(String databaseName, ) {
        this.dbName = databaseName;
    }

    private String[] parseSQL(String filePath) {
        String[] queries = new String[0];

        try {
            File file = new File(filePath);
            FileReader reader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(reader);
            StringBuilder stringBuilder = new StringBuilder();
            String currentLine;

            while ((currentLine = bufferedReader.readLine()) != null)
                stringBuilder.append(currentLine);
            reader.close();

            // Splitting the string on ";" to separate requests.
            queries = stringBuilder.toString().split(";");
        } catch (IOException e) {
            log.error("Could not find file {}.", filePath, e);
            System.out.println("No file exists at the provided path.");
        }
        return queries;
    }

    public void executeSQL(String[] query) {
        try (Connection connection = connect();
             Statement statement = connection.createStatement()) {
            System.out.println("Connection established to the database; trying to execute query.");
            for (String s : query) {
                //Remove spaces to not execute empty statements
                if (!s.trim().equals("")) {
                    // Execute the query
                    statement.executeUpdate(s);
                    System.out.println(">>" + s);
                }
            }
            statement.close();
            System.out.println("Query executed successfully.");
        } catch (SQLException e) {
            //log.error("Something went wrong: ", e);
            System.out.println("Something went wrong: " + e);
        }
    }

    public Connection connect() throws SQLException {
        String databaseURL = "jdbc:postgresql://localhost:5432/" + dbName;
        //TODO: Not hardcoded
        String password = "masj";
        //TODO: Not hardcoded
        String username = "masj";
        return DriverManager.getConnection(databaseURL, username, password);
    }

    public void createTables(String query) {
        String filePath = "src/main/java/dk/kb/bitrepository/utils/database/create_tables.sql";
        String[] queries = parseSQL(filePath);
        executeSQL(queries);
    }
}

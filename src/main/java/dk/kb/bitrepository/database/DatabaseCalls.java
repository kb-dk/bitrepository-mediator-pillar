package dk.kb.bitrepository.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static dk.kb.bitrepository.database.DatabaseUtils.connect;

public class DatabaseCalls {
    private static final Logger log = LoggerFactory.getLogger(DatabaseCalls.class);
    //TODO: Missing logger

    public DatabaseCalls() {
    }

    /**
     * This method will insert either into the enc_parameter or files table.
     * <p>
     * The query values are 'collectionID, fileID, salt, iv, iterations' for the enc_parameters table.
     * The query values are 'collectionID, fileID, receivedTimestamp, encryptedTimestamp, checksum,
     * encChecksum, checksumTimestamp' for the files table.
     *
     * @param values A list of values to insert into the table. Must fit the above-mentioned values.
     */
    public static void insertInto(String... values) {
        String query = "";
        if (values.length == 5) {
            query = String.format("INSERT INTO %s VALUES(/?/?)", DatabaseConstants.ENC_PARAMS_TABLE);
        } else if (values.length == 7) {
            query = String.format("INSERT INTO %s VALUES(/?/?)", DatabaseConstants.FILES_TABLE);
        } else {
            System.out.println("All columns must have a valid value.");
            System.exit(0);
        }
        query = query.replace("/?/", "?, ".repeat(values.length - 1));
        System.out.println(query);
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(query)) {
            System.out.println("Connection established to the database; trying to execute query.");
            for (int i = 0; i < values.length; i++) {
                statement.setString(i + 1, values[i]);
            }
            statement.executeUpdate();
        } catch (SQLException e) {
            //log.error("Error in executing SQL query: ", e);
            System.out.println("Error in executing SQL query:\n" + e);
        }
    }

    /**
     * @param collectionID The collection ID which is part of the primary key
     * @param fileID       The file ID which is part of the primary key
     * @param table        Should be either DatabaseConstants.ENC_PARAMS_TABLE or DatabaseConstants.FILES_TABLE
     * @param conditions   Unused atm. //TODO: Introduce additional conditions
     * @return Returns a DatabaseData object containing the data found in the ResultSet that is received from the query.
     */
    public static List<DatabaseData> getFile(String collectionID, String fileID, String table, String... conditions) {
        List<DatabaseData> resultList = new ArrayList<>();
        String query = String.format("SELECT * FROM %s WHERE collection_id = '?' AND file_id = '@'", table);
        //query = query.replace("*", specifics[0]);
        query = query.replace("?", collectionID);
        query = query.replace("@", fileID);

        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            System.out.println("Connection established to the database.");
            System.out.println("Executing >>" + query);
            ResultSet result = statement.executeQuery(query);

            while (result.next()) {
                EncParameters data = new EncParameters();

                data.setCollectionID(result.getString(1));
                data.setFileID(result.getString(2));
                data.setSalt(result.getString(3));
                data.setIv(result.getString(4));
                data.setIterations(result.getString(5));

                resultList.add(data);
            }
            result.close();

            if (resultList.isEmpty()) {
                throw new IllegalStateException("No results from " + statement);
            }

        } catch (SQLException e) {
            //log.error("Error in executing SQL query: ", e);
            System.out.println("Error in executing SQL query:\n" + e);
        }

        return resultList;
    }
}

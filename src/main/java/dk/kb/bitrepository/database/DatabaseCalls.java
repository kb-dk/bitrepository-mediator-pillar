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
     * Performs the INSERT query into the enc_parameter table.
     *
     * @param collection_id The Collection ID, part of the primary key.
     * @param file_id       The File ID, part of the primary key.
     * @param salt          The Salt used in the encryption.
     * @param iv            The initialization vector used in the encryption.
     * @param iterations    The number of iterations.
     */
    public static void insertInto(String collection_id, String file_id, String salt, String iv, String iterations) {
        String query = String.format("INSERT INTO %s VALUES(?, ?, ?, ?, ?)", DatabaseConstants.FILES_TABLE);
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(query)) {
            System.out.println("Connection established to the database; readying statement.");
            statement.setString(1, collection_id);
            statement.setString(2, file_id);
            statement.setString(3, salt);
            statement.setString(4, iv);
            statement.setString(5, iterations);
            System.out.println("Executing the query.");
            statement.executeUpdate();
            System.out.println("Query has been executed successfully!");
        } catch (SQLException e) {
            //log.error("Error in executing SQL query: ", e);
            System.out.println("Error in executing SQL query:\n" + e);
        }
    }

    /**
     * Performs the INSERT query into the 'files' table.
     *
     * @param collection_id       The Collection ID, part of the primary key.
     * @param file_id             The File ID, part of the primary key.
     * @param received_timestamp  The timestamp for when the file was received.
     * @param encrypted_timestamp The timestamp for when the file was encrypted.
     * @param checksum            The checksum of the un-encrypted file.
     * @param enc_checksum        The checksum of the encrypted file.
     * @param checksum_timestamp  The timestamp for when the checksum was computed.
     */
    public static void insertInto(String collection_id, String file_id, String received_timestamp,
                                  String encrypted_timestamp, String checksum, String enc_checksum, String checksum_timestamp) {
        String query = String.format("INSERT INTO %s VALUES(?, ?, ?, ?, ?, ?, ?)", DatabaseConstants.FILES_TABLE);
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(query)) {
            System.out.println("Connection established to the database; readying statement.");
            statement.setString(1, collection_id);
            statement.setString(2, file_id);
            statement.setString(3, received_timestamp);
            statement.setString(4, encrypted_timestamp);
            statement.setString(5, checksum);
            statement.setString(6, enc_checksum);
            statement.setString(7, checksum_timestamp);
            System.out.println("Executing the query.");
            statement.executeUpdate();
            System.out.println("Query has been executed successfully!");
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
                //TODO: Can only create enc_parameter data, needs to also do files
                // Maybe overload the method?
                // Then maybe DatabaseData abstract method is not needed
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

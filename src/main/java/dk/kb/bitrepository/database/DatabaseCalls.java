package dk.kb.bitrepository.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static dk.kb.bitrepository.database.DatabaseConstants.ENC_PARAMS_TABLE;
import static dk.kb.bitrepository.database.DatabaseConstants.FILES_TABLE;
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
        String query = String.format("INSERT INTO %s VALUES(?, ?, ?, ?, ?)", ENC_PARAMS_TABLE);
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
    public static void insertInto(String collection_id, String file_id, OffsetDateTime received_timestamp,
                                  OffsetDateTime encrypted_timestamp, String checksum, String enc_checksum,
                                  OffsetDateTime checksum_timestamp) {
        String query = String.format("INSERT INTO %s VALUES(?, ?, ?, ?, ?, ?, ?)", FILES_TABLE);
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(query)) {
            System.out.println("Connection established to the database; readying statement.");
            statement.setString(1, collection_id);
            statement.setString(2, file_id);
            statement.setObject(3, received_timestamp);
            statement.setObject(4, encrypted_timestamp);
            statement.setString(5, checksum);
            statement.setString(6, enc_checksum);
            statement.setObject(7, checksum_timestamp);
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
     * @return Returns a DatabaseData object containing the data found in the ResultSet that is received from the query.
     */
    public static List<DatabaseData> select(String collectionID, String fileID, String table) {
        List<DatabaseData> resultList = new ArrayList<>();
        String query = String.format("SELECT * FROM %s WHERE collection_id = '?' AND file_id = '@'", table);
        query = query.replace("?", collectionID);
        query = query.replace("@", fileID);

        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            System.out.println("Connection established to the database.");
            System.out.println("Executing >>" + query);
            ResultSet result = statement.executeQuery(query);
            System.out.println("Query executed successfully!");

            while (result.next()) {
                if (table.equals(ENC_PARAMS_TABLE)) {
                    EncParametersData data = new EncParametersData();

                    data.setCollectionID(result.getString(1));
                    data.setFileID(result.getString(2));
                    data.setSalt(result.getString(3));
                    data.setIv(result.getString(4));
                    data.setIterations(result.getString(5));

                    resultList.add(data);
                } else if (table.equals(FILES_TABLE)) {
                    FilesData data = new FilesData();

                    data.setCollectionID(result.getString(1));
                    data.setFileID(result.getString(2));
                    data.setReceivedTimestamp(result.getObject(3, OffsetDateTime.class));
                    data.setEncryptedTimestamp(result.getObject(4, OffsetDateTime.class));
                    data.setChecksum(result.getString(5));
                    data.setEncryptedChecksum(result.getString(6));
                    data.setChecksumTimestamp(result.getObject(7, OffsetDateTime.class));

                    resultList.add(data);
                }
            }
            result.close();

            if (resultList.isEmpty()) {
                System.out.println("No results found.");
                return resultList;
            }
        } catch (SQLException e) {
            //log.error("Error in executing SQL query: ", e);
            System.out.println("Error in executing SQL query:\n" + e);
        }

        return resultList;
    }

    /**
     * Performs the DELETE query in the chosen table where collection_id and file_id fits the chosen parameters.
     *
     * @param collectionID The CollectionID of the index to be deleted.
     * @param fileID       The FileID of the index to be deleted.
     * @param table        The table to delete from.
     */
    public static void delete(String collectionID, String fileID, String table) {
        String query = String.format("DELETE FROM %s WHERE collection_id = ? AND file_id = ?", table);
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(query)) {
            System.out.println("Connection established to the database; readying statement.");
            statement.setString(1, collectionID);
            statement.setString(2, fileID);
            System.out.println("Executing >>" + query);
            statement.executeUpdate();
            System.out.println("Query has been executed successfully!");
        } catch (SQLException e) {
            //log.error("Error in executing SQL query: ", e);
            System.out.println("Error in executing SQL query:\n" + e);
        }
    }

    public static void updateTimestamp(String collectionID, String fileID, String timestampColumn, OffsetDateTime new_enc_timestamp) {
        String query = String.format("UPDATE %s SET %s = ? WHERE collection_id = ? AND file_id = ?", FILES_TABLE, timestampColumn);

        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(query)) {
            System.out.println("Connection established to the database; readying statement.");
            statement.setObject(1, new_enc_timestamp);
            statement.setString(2, collectionID);
            statement.setString(3, fileID);
            System.out.println("Executing >>" + statement);
            statement.executeUpdate();
            System.out.println("Query has been executed successfully!");
        } catch (SQLException e) {
            //log.error("Error in executing SQL query: ", e);
            System.out.println("Error in executing SQL query:\n" + e);
        }
    }
}

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
import static dk.kb.bitrepository.database.DatabaseUtils.createPreparedStatement;

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
        try (Connection connection = connect()) {
            prepareStatement(query, connection, collection_id, file_id, salt, iv, iterations);
        } catch (SQLException e) {
            //log.error("Error in executing SQL query: ", e);
            System.out.println("Error in executing 'insert' SQL query:\n" + e);
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
    public static void insertInto(String collection_id, String file_id, OffsetDateTime received_timestamp, OffsetDateTime encrypted_timestamp, String checksum, String enc_checksum, OffsetDateTime checksum_timestamp) {
        String query = String.format("INSERT INTO %s VALUES(?, ?, ?, ?, ?, ?, ?)", FILES_TABLE);
        try (Connection connection = connect()) {
            prepareStatement(query, connection, collection_id, file_id, received_timestamp, encrypted_timestamp, checksum, enc_checksum, checksum_timestamp);
        } catch (SQLException e) {
            //log.error("Error in executing SQL query: ", e);
            System.out.println("Error in executing 'insert' SQL query:\n" + e);
        }
    }

    /**
     * Performs a select query, and creates the appropriate object containing the resulting data.
     *
     * @param collectionID The collection ID which is part of the primary key.
     * @param fileID       The file ID which is part of the primary key.
     * @param table        Should be either DatabaseConstants.ENC_PARAMS_TABLE or DatabaseConstants.FILES_TABLE.
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
            System.out.println("Error in executing 'select' SQL query:\n" + e);
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
        try (Connection connection = connect()) {
            prepareStatement(query, connection, collectionID, fileID);
        } catch (SQLException e) {
            //log.error("Error in executing SQL query: ", e);
            System.out.println("Error in executing 'delete' SQL query:\n" + e);
        }
    }

    /**
     * Updates the any timestamp column in the 'files' table in the Database.
     *
     * @param collectionID    The collection id, part of the primary key.
     * @param fileID          The file id, part of the primary key.
     * @param timestampColumn The timestamp column to update, use e.g. 'FILES_ENCRYPTED_TIMESTAMP_NAME' etc.
     * @param new_timestamp   The new timestamp that will replace the old one.
     */
    public static void updateTimestamp(String collectionID, String fileID, String timestampColumn, OffsetDateTime new_timestamp) {
        String query = String.format("UPDATE %s SET %s = ? WHERE collection_id = ? AND file_id = ?", FILES_TABLE, timestampColumn);

        try (Connection connection = connect()) {
            prepareStatement(query, connection, new_timestamp, collectionID, fileID);
        } catch (SQLException e) {
            //log.error("Error in executing SQL query: ", e);
            System.out.println("Error in executing 'update' SQL query:\n" + e);
        }
    }

    /**
     * Used to prepare the statement and execute the given query.
     *
     * @param query      The query to be executed.
     * @param connection The database connect, used to prepare the statement.
     * @param args       The arguments to be set in the statement.
     * @throws SQLException Throws an exception if the connection to the Database fails.
     */
    private static void prepareStatement(String query, Connection connection, Object... args) throws SQLException {
        System.out.println("Connection established to the database; readying statement.");
        PreparedStatement statement = createPreparedStatement(connection, query, args);
        System.out.println("Executing >>" + statement);
        statement.executeUpdate();
        System.out.println("Query has been executed successfully!");
        statement.close();
    }
}

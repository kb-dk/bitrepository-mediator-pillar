package dk.kb.bitrepository.mediator.database;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.Locale;

import static dk.kb.bitrepository.mediator.database.DatabaseConstants.ENC_PARAMS_TABLE;
import static dk.kb.bitrepository.mediator.database.DatabaseConstants.FILES_TABLE;
import static dk.kb.bitrepository.mediator.database.DatabaseData.EncryptedParametersData;
import static dk.kb.bitrepository.mediator.database.DatabaseData.FilesData;

public class DatabaseDAO {
    private static final Logger log = LoggerFactory.getLogger(DatabaseDAO.class);
    private final DatabaseConnectionManager connectionManager;

    public DatabaseDAO(DatabaseConnectionManager connectionManager) { // name subject to change
        this.connectionManager = connectionManager;
    }

    /**
     * Performs the INSERT query into the enc_parameter table.
     *
     * @param collectionID The Collection ID, part of the primary key.
     * @param fileID       The File ID, part of the primary key.
     * @param salt         The Salt used in the encryption.
     * @param iv           The initialization vector used in the encryption.
     * @param iterations   The number of iterations.
     */
    public void insertIntoEncParams(String collectionID, String fileID, String salt, byte[] iv, int iterations) {
        String query = String.format(Locale.getDefault(), "INSERT INTO %s VALUES(?, ?, ?, ?, ?) ON CONFLICT DO NOTHING", ENC_PARAMS_TABLE);
        executeQuery(query, collectionID, fileID, salt, iv, iterations);
    }

    /**
     * Performs the INSERT query into the 'files' table.
     *
     * @param collectionID       The Collection ID, part of the primary key.
     * @param fileID             The File ID, part of the primary key.
     * @param receivedTimestamp  The timestamp for when the file was received.
     * @param encryptedTimestamp The timestamp for when the file was encrypted.
     * @param checksum           The checksum of the un-encrypted file.
     * @param encChecksum        The checksum of the encrypted file.
     * @param checksumTimestamp  The timestamp for when the checksum was computed.
     */
    public void insertIntoFiles(String collectionID, String fileID, OffsetDateTime receivedTimestamp, OffsetDateTime encryptedTimestamp,
                                String checksum, String encChecksum, OffsetDateTime checksumTimestamp) {
        String query = String.format(Locale.getDefault(), "INSERT INTO %s VALUES(?, ?, ?, ?, ?, ?, ?) ON CONFLICT DO NOTHING",
                FILES_TABLE);
        executeQuery(query, collectionID, fileID, receivedTimestamp, encryptedTimestamp, checksum, encChecksum, checksumTimestamp);
    }

    public boolean hasFile(String collectionID, String fileID) {
        String sql = "SELECT COUNT(*) FROM " + FILES_TABLE + " WHERE file_id = ? AND collection_id = ?";
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = DatabaseUtils.createPreparedStatement(connection, sql, fileID, collectionID)) {
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getInt(1) == 1;
            }
        } catch (SQLException e) {
            log.error("Error occurred when trying to connect to the database.", e);
        }
        return false;
    }

    /**
     * Performs a SELECT query, and creates the appropriate object containing the resulting data.
     *
     * @param collectionID The collection ID which is part of the primary key.
     * @param fileID       The file ID which is part of the primary key.
     * @param table        Should be either DatabaseConstants.ENC_PARAMS_TABLE or DatabaseConstants.FILES_TABLE.
     * @return Returns a DatabaseData object containing the data found in the ResultSet that is received from the query.
     */
    public DatabaseData select(String collectionID, String fileID, String table) {
        String query = String.format(Locale.getDefault(), "SELECT * FROM %s WHERE collection_id = '?' AND file_id = '@'", table);
        query = query.replace("?", collectionID);
        query = query.replace("@", fileID);

        DatabaseData out = null;
        try (Connection connection = connectionManager.getConnection(); Statement statement = connection.createStatement()) {
            log.info("Executing >>" + query);
            ResultSet result = statement.executeQuery(query);
            while (result.next()) {
                if (table.equals(ENC_PARAMS_TABLE)) {
                    out = new EncryptedParametersData(result.getString(1), result.getString(2), result.getString(3),
                            result.getBytes(4), result.getInt(5));
                } else if (table.equals(FILES_TABLE)) {
                    out = new FilesData(result.getString(1), result.getString(2), result.getObject(3, OffsetDateTime.class),
                            result.getObject(4, OffsetDateTime.class), result.getString(5), result.getString(6),
                            result.getObject(7, OffsetDateTime.class));
                }
            }
            result.close();

            if (out == null) {
                log.warn("No results were found.");
            }
        } catch (SQLException e) {
            log.error("Error occurred when trying to connect to the database.", e);
        }

        return out;
    }

    /**
     * Performs the DELETE query in the chosen table where collection_id and file_id fits the chosen parameters.
     *
     * @param collectionID The CollectionID of the index to be deleted.
     * @param fileID       The FileID of the index to be deleted.
     * @param table        The table to delete from.
     */
    public void delete(String collectionID, String fileID, String table) {
        String query = String.format(Locale.getDefault(), "DELETE FROM %s WHERE collection_id = ? AND file_id = ?", table);
        executeQuery(query, collectionID, fileID);
    }

    /**
     * Deletes every column in the chosen table.
     *
     * @param table The table to delete all columns from.
     */
    public void delete(String table) {
        String query = String.format(Locale.getDefault(), "DELETE FROM %s", table);
        executeQuery(query);
    }

    /**
     * Updates the any timestamp column in the 'files' table in the Database.
     *
     * @param collectionID    The collection id, part of the primary key.
     * @param fileID          The file id, part of the primary key.
     * @param timestampColumn The timestamp column to update, use e.g. 'FILES_ENCRYPTED_TIMESTAMP_NAME' etc.
     * @param newTimestamp    The new timestamp that will replace the old one.
     */
    public void updateTimestamp(String collectionID, String fileID, String timestampColumn, OffsetDateTime newTimestamp) {
        String query = String.format(Locale.getDefault(), "UPDATE %s SET %s = ? WHERE collection_id = ? AND file_id = ?", FILES_TABLE,
                timestampColumn);
        executeQuery(query, newTimestamp, collectionID, fileID);
    }

    /**
     * Performs UPDATE on all values matching the chosen primary key.
     * Used to update a column in the table when replacing a file on the encrypted pillar.
     *
     * @param collectionID       The collection ID, part of the primary key.
     * @param fileID             The file ID, part of the primary key.
     * @param receivedTimestamp  The new timestamp for when the file was received.
     * @param encryptedTimestamp The new timestamp for when the file was encrypted.
     * @param checksum           The checksum of the new file.
     * @param encryptedChecksum  The encrypted checksum of the new encrypted file.
     * @param checksumTimestamp  The timestamp for when the checksum was computed.
     */
    public void updateFilesTable(String collectionID, String fileID, OffsetDateTime receivedTimestamp, OffsetDateTime encryptedTimestamp,
                                 String checksum, String encryptedChecksum, OffsetDateTime checksumTimestamp) {
        String query = String.format(Locale.getDefault(),
                "UPDATE %s SET " + "received_timestamp = ?, " + "encrypted_timestamp = ?, " + "checksum = ?, " +
                        "encrypted_checksum = ?, " + "checksum_timestamp = ? WHERE collection_id = ? AND file_id = ?", FILES_TABLE);
        executeQuery(query, receivedTimestamp, encryptedTimestamp, checksum, encryptedChecksum, checksumTimestamp, collectionID, fileID);

    }

    /**
     * Performs UPDATE on all values matching the chosen primary key.
     * Used to update a column in the table when replacing a file on the encrypted pillar.
     *
     * @param collectionID The collection ID, part of the primary key.
     * @param fileID       The file ID, part of the primary key.
     * @param salt         The salt used to encrypt the new file.
     * @param iv           The IV used to encrypt the new file.
     * @param iterations   The number of iterations used in the encryption of the new file.
     */
    public void updateEncryptionParametersTable(String collectionID, String fileID, String salt, byte[] iv, int iterations) {
        String query = String.format(Locale.getDefault(),
                "UPDATE %s SET " + "salt = ?, " + "iv = ?, " + "iterations = ? WHERE collection_id = ? AND file_id = ?", ENC_PARAMS_TABLE);
        executeQuery(query, salt, iv, iterations, collectionID, fileID);

    }

    /**
     * Helper method to execute the given query with the given arguments.
     *
     * @param query The query to execute.
     * @param args  The arguments to put in the given query.
     */
    private void executeQuery(String query, @NotNull Object... args) {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = DatabaseUtils.createPreparedStatement(connection, query, args)) {

            String finalQuery = statement.toString();
            log.debug("Executing >>" +
                    finalQuery.substring(finalQuery.indexOf("wrapping")).replace("wrapping ", ""));
            statement.executeUpdate();
        } catch (SQLException e) {
            log.error("Error in executing SQL query:\n", e);
        }
    }
}

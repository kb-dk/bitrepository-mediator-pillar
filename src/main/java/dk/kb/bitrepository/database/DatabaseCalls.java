package dk.kb.bitrepository.database;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static dk.kb.bitrepository.database.DatabaseConstants.ENC_PARAMS_TABLE;
import static dk.kb.bitrepository.database.DatabaseConstants.FILES_TABLE;
import static dk.kb.bitrepository.database.DatabaseData.EncryptedParametersData;
import static dk.kb.bitrepository.database.DatabaseData.FilesData;
import static dk.kb.bitrepository.database.DatabaseUtils.connect;
import static dk.kb.bitrepository.database.DatabaseUtils.createPreparedStatement;

public class DatabaseCalls {
    private static final Logger log = LoggerFactory.getLogger(DatabaseCalls.class);

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
    public static void insertInto(String collection_id, String file_id, String salt, byte[] iv, int iterations) {
        String query = String.format(Locale.getDefault(), "INSERT INTO %s VALUES(?, ?, ?, ?, ?)", ENC_PARAMS_TABLE);
        executeQuery(query, true, collection_id, file_id, salt, iv, iterations);
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
        String query = String.format(Locale.getDefault(), "INSERT INTO %s VALUES(?, ?, ?, ?, ?, ?, ?)", FILES_TABLE);
        executeQuery(query, true, collection_id, file_id, received_timestamp, encrypted_timestamp,
                checksum, enc_checksum, checksum_timestamp);
    }

    /**
     * Performs a SELECT query, and creates the appropriate object containing the resulting data.
     *
     * @param collectionID The collection ID which is part of the primary key.
     * @param fileID       The file ID which is part of the primary key.
     * @param table        Should be either DatabaseConstants.ENC_PARAMS_TABLE or DatabaseConstants.FILES_TABLE.
     * @return Returns a DatabaseData object containing the data found in the ResultSet that is received from the query.
     */
    public static List<DatabaseData> select(String collectionID, String fileID, String table) {
        List<DatabaseData> resultList = new ArrayList<>();
        String query = String.format(Locale.getDefault(), "SELECT * FROM %s WHERE collection_id = '?' AND file_id = '@'", table);
        query = query.replace("?", collectionID);
        query = query.replace("@", fileID);

        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            log.info("Executing >>" + query);
            ResultSet result = statement.executeQuery(query);
            while (result.next()) {
                if (table.equals(ENC_PARAMS_TABLE)) {
                    EncryptedParametersData data = new EncryptedParametersData(
                            result.getString(1),
                            result.getString(2),
                            result.getString(3),
                            result.getBytes(4),
                            result.getInt(5)
                    );

                    resultList.add(data);
                } else if (table.equals(FILES_TABLE)) {
                    FilesData data = new FilesData(
                            result.getString(1),
                            result.getString(2),
                            result.getObject(3, OffsetDateTime.class),
                            result.getObject(4, OffsetDateTime.class),
                            result.getString(5),
                            result.getString(6),
                            result.getObject(7, OffsetDateTime.class)
                    );

                    resultList.add(data);
                }
            }
            result.close();

            if (resultList.isEmpty()) {
                log.warn("No results found.");
                return resultList;
            } else {
                log.info("{} result(s) have been processed.", resultList.size());
            }
        } catch (SQLException e) {
            log.error("Error occurred when trying to connect to the database.", e);
        }

        return resultList;
    }

    /**
     * Performs the DELETE query in the chosen table where collection_id and file_id fits the chosen parameters.
     *
     * @param collectionID The CollectionID of the index to be deleted.
     * @param fileID       The FileID of the index to be deleted.
     * @param table        The table to delete from.
     * @param verbose      Used to turn informative logging off or on.
     */
    public static void delete(String collectionID, String fileID, String table, boolean verbose) {
        String query = String.format(Locale.getDefault(), "DELETE FROM %s WHERE collection_id = ? AND file_id = ?", table);

        executeQuery(query, verbose, collectionID, fileID);
    }

    /**
     * Performs the DELETE query in the chosen table where collection_id and file_id fits the chosen parameters.
     * Informative logging is turned on by default. Use the overloaded method to turn logging off,
     * primarily used for clean in tests.
     *
     * @param collectionID The CollectionID of the index to be deleted.
     * @param fileID       The FileID of the index to be deleted.
     * @param table        The table to delete from.
     */
    public static void delete(String collectionID, String fileID, String table) {
        delete(collectionID, fileID, table, true);
    }

    /**
     * Updates the any timestamp column in the 'files' table in the Database.
     *
     * @param collectionID    The collection id, part of the primary key.
     * @param fileID          The file id, part of the primary key.
     * @param timestampColumn The timestamp column to update, use e.g. 'FILES_ENCRYPTED_TIMESTAMP_NAME' etc.
     * @param new_timestamp   The new timestamp that will replace the old one.
     */
    public static void updateTimestamp(String collectionID, String fileID, String timestampColumn,
                                       OffsetDateTime new_timestamp) {
        String query = String.format(Locale.getDefault(), "UPDATE %s SET %s = ? WHERE collection_id = ? AND file_id = ?", FILES_TABLE,
                timestampColumn);
        executeQuery(query, true, new_timestamp, collectionID, fileID);
    }

    /**
     * Helper method to execute the given query with the given arguments.
     *
     * @param query The query to execute.
     * @param args  The arguments to put in the given query.
     */
    private static void executeQuery(String query, boolean verbose, @NotNull Object... args) {
        try (Connection connection = connect()) {
            prepareStatement(query, connection, verbose, args);
        } catch (SQLException e) {
            log.error("Error in executing SQL query:\n", e);
            System.out.println("Error in executing SQL query:\n" + e);
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
    private static void prepareStatement(String query, Connection connection, boolean verbose, @NotNull Object... args) throws SQLException {
        PreparedStatement statement = createPreparedStatement(connection, query, args);
        if (verbose) {
            log.info("Executing >>" + statement);
        }
        statement.executeUpdate();
        statement.close();
    }
}

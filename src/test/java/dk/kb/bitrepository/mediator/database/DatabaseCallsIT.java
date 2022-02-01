package dk.kb.bitrepository.mediator.database;

import dk.kb.bitrepository.mediator.database.configs.ConfigurationHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

import static dk.kb.bitrepository.mediator.database.DatabaseCalls.delete;
import static dk.kb.bitrepository.mediator.database.DatabaseCalls.insertInto;
import static dk.kb.bitrepository.mediator.database.DatabaseCalls.select;
import static dk.kb.bitrepository.mediator.database.DatabaseCalls.updateEncryptionParametersTable;
import static dk.kb.bitrepository.mediator.database.DatabaseCalls.updateFilesTable;
import static dk.kb.bitrepository.mediator.database.DatabaseCalls.updateTimestamp;
import static dk.kb.bitrepository.mediator.database.DatabaseConstants.COLLECTION_ID;
import static dk.kb.bitrepository.mediator.database.DatabaseConstants.ENC_PARAMS_ITERATIONS;
import static dk.kb.bitrepository.mediator.database.DatabaseConstants.ENC_PARAMS_IV;
import static dk.kb.bitrepository.mediator.database.DatabaseConstants.ENC_PARAMS_SALT;
import static dk.kb.bitrepository.mediator.database.DatabaseConstants.ENC_PARAMS_TABLE;
import static dk.kb.bitrepository.mediator.database.DatabaseConstants.FILES_CHECKSUM;
import static dk.kb.bitrepository.mediator.database.DatabaseConstants.FILES_CHECKSUM_TIMESTAMP;
import static dk.kb.bitrepository.mediator.database.DatabaseConstants.FILES_CHECKSUM_TIMESTAMP_MOCKUP;
import static dk.kb.bitrepository.mediator.database.DatabaseConstants.FILES_ENCRYPTED_TIMESTAMP;
import static dk.kb.bitrepository.mediator.database.DatabaseConstants.FILES_ENCRYPTED_TIMESTAMP_MOCKUP;
import static dk.kb.bitrepository.mediator.database.DatabaseConstants.FILES_ENC_CHECKSUM;
import static dk.kb.bitrepository.mediator.database.DatabaseConstants.FILES_RECEIVED_TIMESTAMP;
import static dk.kb.bitrepository.mediator.database.DatabaseConstants.FILES_RECEIVED_TIMESTAMP_MOCKUP;
import static dk.kb.bitrepository.mediator.database.DatabaseConstants.FILES_TABLE;
import static dk.kb.bitrepository.mediator.database.DatabaseConstants.FILE_ID;
import static dk.kb.bitrepository.mediator.database.DatabaseData.EncryptedParametersData;
import static dk.kb.bitrepository.mediator.database.DatabaseData.FilesData;
import static dk.kb.bitrepository.mediator.database.DatabaseUtils.dropTables;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Test Database Calls")
public class DatabaseCallsIT {
    private static final ConfigurationHandler configs = new ConfigurationHandler();

    @BeforeAll
    static void setUp() throws Exception {
        DatabaseSetup.main(new String[] {"testdb", "jdbc:postgresql://localhost", "5432", "testuser", "testpw", "testcryppw"});
        // Create tables anew
        if (configs.configExists()) {
            // Drop tables
            System.out.println("Database tables has been dropped.");
            dropTables();
            DatabaseUtils.createTables();
            System.out.println("Tables have been created.");
        } else {
            System.out.println("Config has not been set up. Run DatabaseSetup before running any tests.");
            System.exit(0);
        }
    }

    @AfterEach
    public void afterEach() {
        delete(COLLECTION_ID, FILE_ID, ENC_PARAMS_TABLE);
        delete(COLLECTION_ID, FILE_ID, FILES_TABLE);
    }

    @Test
    @DisplayName("Test Insert, Select, and Delete for 'encrypted_parameters' table")
    public void TestInsertSelectAndDeleteForEncParametersTable() {
        String table = ENC_PARAMS_TABLE;

        // Insert some information
        insertInto(COLLECTION_ID, FILE_ID, ENC_PARAMS_SALT, ENC_PARAMS_IV, ENC_PARAMS_ITERATIONS);

        // Get the information from the table with a SELECT query
        EncryptedParametersData result = (EncryptedParametersData) select(COLLECTION_ID, FILE_ID, table);

        // Assert that there is now a result
        assertNotNull(result);

        // Check that the information is correct
        assertEquals(COLLECTION_ID, result.getCollectionID());
        assertEquals(FILE_ID, result.getFileID());
        assertEquals(ENC_PARAMS_SALT, result.getSalt());
        assertEquals(Arrays.toString(ENC_PARAMS_IV), Arrays.toString(result.getIv()));
        assertEquals(ENC_PARAMS_ITERATIONS, result.getIterations());

        // Delete the information using the composite key (collection_id, file_id)
        delete(COLLECTION_ID, FILE_ID, table);

        // Perform a SELECT query again
        result = (EncryptedParametersData) select(COLLECTION_ID, FILE_ID, table);

        // Assert that the query returned null since it should have been deleted
        assertNull(result);
    }

    @Test
    @DisplayName("Test Insert, Select, and Delete for 'files' table")
    public void TestInsertSelectAndDeleteForFilesTable() {
        insertInto(COLLECTION_ID, FILE_ID, FILES_RECEIVED_TIMESTAMP_MOCKUP, FILES_ENCRYPTED_TIMESTAMP_MOCKUP,
                FILES_CHECKSUM, FILES_ENC_CHECKSUM, FILES_CHECKSUM_TIMESTAMP_MOCKUP);

        FilesData result = (FilesData) select(COLLECTION_ID, FILE_ID, FILES_TABLE);

        assertNotNull(result, "Result should be of 'FilesData' type.");

        assertEquals(COLLECTION_ID, result.getCollectionID());
        assertEquals(FILE_ID, result.getFileID());
        assertEquals(FILES_RECEIVED_TIMESTAMP_MOCKUP, result.getReceivedTimestamp());
        assertEquals(FILES_ENCRYPTED_TIMESTAMP_MOCKUP, result.getEncryptedTimestamp());
        assertEquals(FILES_CHECKSUM, result.getChecksum());
        assertEquals(FILES_ENC_CHECKSUM, result.getEncryptedChecksum());
        assertEquals(FILES_CHECKSUM_TIMESTAMP_MOCKUP, result.getChecksumTimestamp());
    }

    @Test
    @DisplayName("Test Update of Encrypted Timestamp in 'files' table")
    public void TestUpdateEncryptedTimestampInFilesTable() {
        String table = FILES_TABLE;

        insertInto(COLLECTION_ID, FILE_ID, FILES_RECEIVED_TIMESTAMP_MOCKUP, FILES_ENCRYPTED_TIMESTAMP_MOCKUP,
                FILES_CHECKSUM, FILES_ENC_CHECKSUM, FILES_CHECKSUM_TIMESTAMP_MOCKUP);

        FilesData result = (FilesData) select(COLLECTION_ID, FILE_ID, table);

        OffsetDateTime oldTimestamp = result.getEncryptedTimestamp();
        OffsetDateTime newTimestamp = OffsetDateTime.now(ZoneOffset.UTC);

        updateTimestamp(COLLECTION_ID, FILE_ID, FILES_ENCRYPTED_TIMESTAMP, newTimestamp);
        result = (FilesData) select(COLLECTION_ID, FILE_ID, table);

        assertEquals(result.getEncryptedTimestamp(), newTimestamp);
        assertNotEquals(oldTimestamp, result.getEncryptedTimestamp());
    }

    @Test
    @DisplayName("Test Update of Checksum Timestamp in 'files' table")
    public void TestUpdateChecksumTimestampInFilesTable() {
        String table = FILES_TABLE;

        insertInto(COLLECTION_ID, FILE_ID, FILES_RECEIVED_TIMESTAMP_MOCKUP, FILES_ENCRYPTED_TIMESTAMP_MOCKUP,
                FILES_CHECKSUM, FILES_ENC_CHECKSUM, FILES_CHECKSUM_TIMESTAMP_MOCKUP);

        FilesData result = (FilesData) select(COLLECTION_ID, FILE_ID, table);

        OffsetDateTime oldTimestamp = result.getChecksumTimestamp();
        OffsetDateTime newTimestamp = OffsetDateTime.now(ZoneOffset.UTC);

        updateTimestamp(COLLECTION_ID, FILE_ID, FILES_CHECKSUM_TIMESTAMP, newTimestamp);
        result = (FilesData) select(COLLECTION_ID, FILE_ID, table);

        assertEquals(result.getChecksumTimestamp(), newTimestamp);
        assertNotEquals(oldTimestamp, result.getChecksumTimestamp());
    }

    @Test
    @DisplayName("Test Update of Received Timestamp in 'files' table")
    public void TestUpdateReceivedTimestampInFilesTable() {
        String table = FILES_TABLE;

        insertInto(COLLECTION_ID, FILE_ID, FILES_RECEIVED_TIMESTAMP_MOCKUP, FILES_ENCRYPTED_TIMESTAMP_MOCKUP,
                FILES_CHECKSUM, FILES_ENC_CHECKSUM, FILES_CHECKSUM_TIMESTAMP_MOCKUP);

        FilesData result = (FilesData) select(COLLECTION_ID, FILE_ID, table);

        OffsetDateTime oldTimestamp = result.getReceivedTimestamp();
        OffsetDateTime newTimestamp = OffsetDateTime.now(ZoneOffset.UTC);

        updateTimestamp(COLLECTION_ID, FILE_ID, FILES_RECEIVED_TIMESTAMP, newTimestamp);
        result = (FilesData) select(COLLECTION_ID, FILE_ID, table);

        assertEquals(result.getReceivedTimestamp(), newTimestamp);
        assertNotEquals(oldTimestamp, result.getReceivedTimestamp());
    }

    @Test
    @DisplayName("Test that updating 'files' table works")
    public void testUpdatingFilesTable() {
        insertInto(COLLECTION_ID, FILE_ID, FILES_RECEIVED_TIMESTAMP_MOCKUP, FILES_ENCRYPTED_TIMESTAMP_MOCKUP,
                FILES_CHECKSUM, FILES_ENC_CHECKSUM, FILES_CHECKSUM_TIMESTAMP_MOCKUP);
        updateFilesTable(COLLECTION_ID, FILE_ID, OffsetDateTime.now(ZoneOffset.UTC), OffsetDateTime.now(ZoneOffset.UTC),
                FILES_CHECKSUM + "_new", FILES_ENC_CHECKSUM + "_new", OffsetDateTime.now(ZoneOffset.UTC));
        FilesData result = (FilesData) select(COLLECTION_ID, FILE_ID, FILES_TABLE);

        assertNotEquals(FILES_RECEIVED_TIMESTAMP_MOCKUP, result.getReceivedTimestamp());
        assertNotEquals(FILES_ENCRYPTED_TIMESTAMP_MOCKUP, result.getEncryptedTimestamp());
        assertNotEquals(FILES_CHECKSUM, result.getChecksum());
        assertNotEquals(FILES_ENC_CHECKSUM, result.getEncryptedChecksum());
        assertNotEquals(FILES_CHECKSUM_TIMESTAMP_MOCKUP, result.getChecksumTimestamp());
        assertEquals(FILES_CHECKSUM + "_new", result.getChecksum());
        assertEquals(FILES_ENC_CHECKSUM + "_new", result.getEncryptedChecksum());
    }

    @Test
    @DisplayName("Test that updating 'enc_parameters' table works")
    public void testUpdatingEncryptionParametersTable() {
        insertInto(COLLECTION_ID, FILE_ID, ENC_PARAMS_SALT, ENC_PARAMS_IV, ENC_PARAMS_ITERATIONS);
        updateEncryptionParametersTable(COLLECTION_ID, FILE_ID, ENC_PARAMS_SALT + "_new", ENC_PARAMS_IV, 1234);
        EncryptedParametersData result = (EncryptedParametersData) select(COLLECTION_ID, FILE_ID, ENC_PARAMS_TABLE);

        assertNotEquals(ENC_PARAMS_SALT, result.getSalt());
        assertNotEquals(ENC_PARAMS_ITERATIONS, result.getIterations());
        assertEquals(Arrays.toString(ENC_PARAMS_IV), Arrays.toString(result.getIv()));
        assertEquals(ENC_PARAMS_SALT + "_new", result.getSalt());
        assertEquals(1234, result.getIterations());
    }

    @Test
    @DisplayName("Test updating 'enc_parameters' table for non-existing index")
    public void testUpdatingEncryptionParametersTableForNonExistingIndex() {
        insertInto(COLLECTION_ID, FILE_ID, ENC_PARAMS_SALT, ENC_PARAMS_IV, ENC_PARAMS_ITERATIONS);
        updateEncryptionParametersTable(COLLECTION_ID + "test", FILE_ID + "test", ENC_PARAMS_SALT
                + "_new", ENC_PARAMS_IV, 1234);
        EncryptedParametersData result = (EncryptedParametersData) select(COLLECTION_ID, FILE_ID, ENC_PARAMS_TABLE);

        assertEquals(COLLECTION_ID, result.getCollectionID());
        assertEquals(FILE_ID, result.getFileID());
        assertEquals(ENC_PARAMS_SALT, result.getSalt());
        assertEquals(Arrays.toString(ENC_PARAMS_IV), Arrays.toString(result.getIv()));
        assertEquals(ENC_PARAMS_ITERATIONS, result.getIterations());
        assertNotEquals(ENC_PARAMS_SALT + "_new", result.getSalt());
        assertNotEquals(1234, result.getIterations());
    }
}

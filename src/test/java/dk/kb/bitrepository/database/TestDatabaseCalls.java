package dk.kb.bitrepository.database;

import dk.kb.bitrepository.database.configs.ConfigurationHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

import static dk.kb.bitrepository.database.DatabaseCalls.*;
import static dk.kb.bitrepository.database.DatabaseConstants.*;
import static dk.kb.bitrepository.database.DatabaseData.EncryptedParametersData;
import static dk.kb.bitrepository.database.DatabaseData.FilesData;
import static dk.kb.bitrepository.database.DatabaseUtils.dropTables;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test Database Calls")
public class TestDatabaseCalls {
    private static final ConfigurationHandler configs = new ConfigurationHandler();

    @BeforeAll
    static void setUp() throws Exception {
        // Drop tables
        dropTables();
        System.out.println("Database tables has been dropped.");
        // Create tables anew
        if (configs.configExists()) {
            DatabaseUtils.createTables();
            System.out.println("Tables have been created.");
        } else {
            System.out.println("Config has not been set up. Run DatabaseSetup before running any tests.");
        }
    }

    @AfterEach
    public void afterEach() {
        delete(COLLECTION_ID, FILE_ID, ENC_PARAMS_TABLE, false);
        delete(COLLECTION_ID, FILE_ID, FILES_TABLE, false);
    }

    @Test
    @DisplayName("Test Insert, Select, and Delete for 'encrypted_parameters' table")
    public void TestInsertSelectAndDeleteForEncParametersTable() {
        String table = ENC_PARAMS_TABLE;

        // Insert some information
        insertInto(COLLECTION_ID, FILE_ID, ENC_PARAMS_SALT, ENC_PARAMS_IV, ENC_PARAMS_ITERATIONS);

        // Get the information from the table with a SELECT query
        List<DatabaseData> result = select(COLLECTION_ID, FILE_ID, table);

        // Assert that there is now a result
        assertFalse(result.isEmpty());

        // Check that the information is correct
        EncryptedParametersData result0 = (EncryptedParametersData) result.get(0);

        if (result0 != null) {
            assertEquals(COLLECTION_ID, result0.getCollectionID());
            assertEquals(FILE_ID, result0.getFileID());
            assertEquals(ENC_PARAMS_SALT, result0.getSalt());
            assertEquals(Arrays.toString(ENC_PARAMS_IV), Arrays.toString(result0.getIv()));
            assertEquals(ENC_PARAMS_ITERATIONS, result0.getIterations());
        }

        // Delete the information using the composite key (collection_id, file_id)
        delete(COLLECTION_ID, FILE_ID, table);

        // Perform a SELECT query again
        result = select(COLLECTION_ID, FILE_ID, table);

        // Assert that the query returned an empty object since it should have been deleted
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Test Insert, Select, and Delete for 'files' table")
    public void TestInsertSelectAndDeleteForFilesTable() {
        insertInto(COLLECTION_ID, FILE_ID, FILES_RECEIVED_TIMESTAMP_MOCKUP, FILES_ENCRYPTED_TIMESTAMP_MOCKUP, FILES_CHECKSUM, FILES_ENC_CHECKSUM, FILES_CHECKSUM_TIMESTAMP_MOCKUP);

        List<DatabaseData> result = select(COLLECTION_ID, FILE_ID, FILES_TABLE);

        assertFalse(result.isEmpty());

        FilesData firstResult = (FilesData) result.get(0);
        assertNotNull(firstResult, "Result should be of 'FilesData' type.");

        assertEquals(COLLECTION_ID, firstResult.getCollectionID());
        assertEquals(FILE_ID, firstResult.getFileID());
        assertEquals(FILES_RECEIVED_TIMESTAMP_MOCKUP, firstResult.getReceivedTimestamp());
        assertEquals(FILES_ENCRYPTED_TIMESTAMP_MOCKUP, firstResult.getEncryptedTimestamp());
        assertEquals(FILES_CHECKSUM, firstResult.getChecksum());
        assertEquals(FILES_ENC_CHECKSUM, firstResult.getEncryptedChecksum());
        assertEquals(FILES_CHECKSUM_TIMESTAMP_MOCKUP, firstResult.getChecksumTimestamp());
    }

    @Test
    @DisplayName("Test Update of Encrypted Timestamp in 'files' table")
    public void TestUpdateEncryptedTimestampInFilesTable() {
        String table = FILES_TABLE;

        insertInto(COLLECTION_ID, FILE_ID, FILES_RECEIVED_TIMESTAMP_MOCKUP, FILES_ENCRYPTED_TIMESTAMP_MOCKUP, FILES_CHECKSUM, FILES_ENC_CHECKSUM, FILES_CHECKSUM_TIMESTAMP_MOCKUP);

        List<DatabaseData> result = select(COLLECTION_ID, FILE_ID, table);

        OffsetDateTime oldTimestamp = ((FilesData) result.get(0)).getEncryptedTimestamp();
        OffsetDateTime newTimestamp = OffsetDateTime.now(ZoneOffset.UTC);

        updateTimestamp(COLLECTION_ID, FILE_ID, FILES_ENCRYPTED_TIMESTAMP, newTimestamp);
        result = select(COLLECTION_ID, FILE_ID, table);
        FilesData firstResult = (FilesData) result.get(0);

        assertEquals(firstResult.getEncryptedTimestamp(), newTimestamp);
        assertNotEquals(oldTimestamp, firstResult.getEncryptedTimestamp());
    }

    @Test
    @DisplayName("Test Update of Checksum Timestamp in 'files' table")
    public void TestUpdateChecksumTimestampInFilesTable() {
        String table = FILES_TABLE;

        insertInto(COLLECTION_ID, FILE_ID, FILES_RECEIVED_TIMESTAMP_MOCKUP, FILES_ENCRYPTED_TIMESTAMP_MOCKUP, FILES_CHECKSUM, FILES_ENC_CHECKSUM, FILES_CHECKSUM_TIMESTAMP_MOCKUP);

        List<DatabaseData> result = select(COLLECTION_ID, FILE_ID, table);

        OffsetDateTime oldTimestamp = ((FilesData) result.get(0)).getChecksumTimestamp();
        OffsetDateTime newTimestamp = OffsetDateTime.now(ZoneOffset.UTC);

        updateTimestamp(COLLECTION_ID, FILE_ID, FILES_CHECKSUM_TIMESTAMP, newTimestamp);
        result = select(COLLECTION_ID, FILE_ID, table);
        FilesData firstResult = (FilesData) result.get(0);

        assertEquals(firstResult.getChecksumTimestamp(), newTimestamp);
        assertNotEquals(oldTimestamp, firstResult.getChecksumTimestamp());
    }

    @Test
    @DisplayName("Test Update of Received Timestamp in 'files' table")
    public void TestUpdateReceivedTimestampInFilesTable() {
        String table = FILES_TABLE;

        insertInto(COLLECTION_ID, FILE_ID, FILES_RECEIVED_TIMESTAMP_MOCKUP, FILES_ENCRYPTED_TIMESTAMP_MOCKUP, FILES_CHECKSUM, FILES_ENC_CHECKSUM, FILES_CHECKSUM_TIMESTAMP_MOCKUP);

        List<DatabaseData> result = select(COLLECTION_ID, FILE_ID, table);

        OffsetDateTime oldTimestamp = ((FilesData) result.get(0)).getReceivedTimestamp();
        OffsetDateTime newTimestamp = OffsetDateTime.now(ZoneOffset.UTC);

        updateTimestamp(COLLECTION_ID, FILE_ID, FILES_RECEIVED_TIMESTAMP, newTimestamp);
        result = select(COLLECTION_ID, FILE_ID, table);
        FilesData firstResult = (FilesData) result.get(0);

        assertEquals(firstResult.getReceivedTimestamp(), newTimestamp);
        assertNotEquals(oldTimestamp, firstResult.getReceivedTimestamp());
    }
}

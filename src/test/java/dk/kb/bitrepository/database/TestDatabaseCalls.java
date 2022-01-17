package dk.kb.bitrepository.database;

import dk.kb.bitrepository.database.configs.ConfigurationHandler;
import org.junit.Before;
import org.junit.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static dk.kb.bitrepository.database.DatabaseCalls.*;
import static dk.kb.bitrepository.database.DatabaseConstants.*;
import static dk.kb.bitrepository.database.DatabaseUtils.dropTables;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

public class TestDatabaseCalls {
    private final ConfigurationHandler configs = new ConfigurationHandler();

    @Before
    public void setUp() throws Exception {
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

    @Test
    public void TestInsertSelectAndDeleteForEncParametersTable() {
        String table = ENC_PARAMS_TABLE;

        // Insert some information
        insertInto(COLLECTION_ID, FILE_ID, ENC_PARAMS_SALT, ENC_PARAMS_IV, ENC_PARAMS_ITERATIONS);

        // Get the information from the table with a SELECT query
        List<DatabaseData> result = select(COLLECTION_ID, FILE_ID, table);

        // Assert that there is now a result
        assertFalse(result.isEmpty());

        // Check that the information is correct
        if (result.get(0) instanceof EncParametersData) {
            assertThat(result.get(0).getCollectionID(), is(COLLECTION_ID));
            assertThat(result.get(0).getFileID(), is(FILE_ID));
            assertThat(((EncParametersData) result.get(0)).getSalt(), is(ENC_PARAMS_SALT));
            assertThat(((EncParametersData) result.get(0)).getIv(), is(ENC_PARAMS_IV));
            assertThat(((EncParametersData) result.get(0)).getIterations(), is(ENC_PARAMS_ITERATIONS));
        }

        // Delete the information using the composite key (collection_id, file_id)
        delete(COLLECTION_ID, FILE_ID, table);

        // Perform a SELECT query again
        result = select(COLLECTION_ID, FILE_ID, table);

        // Assert that the query returned an empty object since it should have been deleted
        assertTrue(result.isEmpty());
    }

    @Test
    public void TestInsertSelectAndDeleteForFilesTable() {
        String table = FILES_TABLE;

        insertInto(COLLECTION_ID, FILE_ID, FILES_RECEIVED_TIMESTAMP_MOCKUP, FILES_ENCRYPTED_TIMESTAMP_MOCKUP, FILES_CHECKSUM, FILES_ENC_CHECKSUM, FILES_CHECKSUM_TIMESTAMP_MOCKUP);

        List<DatabaseData> result = select(COLLECTION_ID, FILE_ID, table);

        assertFalse(result.isEmpty());

        DatabaseData firstResult = result.get(0);
        assertTrue("Result should be of EncParametersData type.", firstResult instanceof FilesData);

        assertThat(firstResult.getCollectionID(), is(COLLECTION_ID));
        assertThat(firstResult.getFileID(), is(FILE_ID));
        assertThat(((FilesData) firstResult).getReceivedTimestamp(), is(FILES_RECEIVED_TIMESTAMP_MOCKUP));
        assertThat(((FilesData) firstResult).getEncryptedTimestamp(), is(FILES_ENCRYPTED_TIMESTAMP_MOCKUP));
        assertThat(((FilesData) firstResult).getChecksum(), is(FILES_CHECKSUM));
        assertThat(((FilesData) firstResult).getEncryptedChecksum(), is(FILES_ENC_CHECKSUM));
        assertThat(((FilesData) firstResult).getChecksumTimestamp(), is(FILES_CHECKSUM_TIMESTAMP_MOCKUP));

        delete(COLLECTION_ID, FILE_ID, table);
    }

    @Test
    public void TestUpdateEncryptedTimestampInFilesTable() {
        String table = FILES_TABLE;

        insertInto(COLLECTION_ID, FILE_ID, FILES_RECEIVED_TIMESTAMP_MOCKUP, FILES_ENCRYPTED_TIMESTAMP_MOCKUP, FILES_CHECKSUM, FILES_ENC_CHECKSUM, FILES_CHECKSUM_TIMESTAMP_MOCKUP);

        List<DatabaseData> result = select(COLLECTION_ID, FILE_ID, table);

        OffsetDateTime oldTimestamp = ((FilesData) result.get(0)).getEncryptedTimestamp();
        OffsetDateTime newTimestamp = OffsetDateTime.now(ZoneOffset.UTC);

        updateTimestamp(COLLECTION_ID, FILE_ID, FILES_ENCRYPTED_TIMESTAMP, newTimestamp);
        result = select(COLLECTION_ID, FILE_ID, table);
        FilesData firstResult = (FilesData) result.get(0);

        assertThat(firstResult.getEncryptedTimestamp(), is(newTimestamp));
        assertNotEquals(oldTimestamp, firstResult.getEncryptedTimestamp());

        delete(COLLECTION_ID, FILE_ID, table);
    }

    @Test
    public void TestUpdateChecksumTimestampInFilesTable() {
        String table = FILES_TABLE;

        insertInto(COLLECTION_ID, FILE_ID, FILES_RECEIVED_TIMESTAMP_MOCKUP, FILES_ENCRYPTED_TIMESTAMP_MOCKUP, FILES_CHECKSUM, FILES_ENC_CHECKSUM, FILES_CHECKSUM_TIMESTAMP_MOCKUP);

        List<DatabaseData> result = select(COLLECTION_ID, FILE_ID, table);

        OffsetDateTime oldTimestamp = ((FilesData) result.get(0)).getChecksumTimestamp();
        OffsetDateTime newTimestamp = OffsetDateTime.now(ZoneOffset.UTC);

        updateTimestamp(COLLECTION_ID, FILE_ID, FILES_CHECKSUM_TIMESTAMP, newTimestamp);
        result = select(COLLECTION_ID, FILE_ID, table);
        FilesData firstResult = (FilesData) result.get(0);

        assertThat(firstResult.getChecksumTimestamp(), is(newTimestamp));
        assertNotEquals(oldTimestamp, firstResult.getChecksumTimestamp());

        delete(COLLECTION_ID, FILE_ID, table);
    }

    @Test
    public void TestUpdateReceivedTimestampInFilesTable() {
        String table = FILES_TABLE;

        insertInto(COLLECTION_ID, FILE_ID, FILES_RECEIVED_TIMESTAMP_MOCKUP, FILES_ENCRYPTED_TIMESTAMP_MOCKUP, FILES_CHECKSUM, FILES_ENC_CHECKSUM, FILES_CHECKSUM_TIMESTAMP_MOCKUP);

        List<DatabaseData> result = select(COLLECTION_ID, FILE_ID, table);

        OffsetDateTime oldTimestamp = ((FilesData) result.get(0)).getReceivedTimestamp();
        OffsetDateTime newTimestamp = OffsetDateTime.now(ZoneOffset.UTC);

        updateTimestamp(COLLECTION_ID, FILE_ID, FILES_RECEIVED_TIMESTAMP, newTimestamp);
        result = select(COLLECTION_ID, FILE_ID, table);
        FilesData firstResult = (FilesData) result.get(0);

        assertThat(firstResult.getReceivedTimestamp(), is(newTimestamp));
        assertNotEquals(oldTimestamp, firstResult.getReceivedTimestamp());

        delete(COLLECTION_ID, FILE_ID, table);
    }
}

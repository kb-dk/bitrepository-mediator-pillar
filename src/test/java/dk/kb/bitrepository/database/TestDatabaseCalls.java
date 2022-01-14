package dk.kb.bitrepository.database;

import org.junit.Before;
import org.junit.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static dk.kb.bitrepository.database.DatabaseCalls.*;
import static dk.kb.bitrepository.database.DatabaseConstants.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

public class TestDatabaseCalls {
    @Before
    public void setUp() throws Exception {
        // Drop tables
        // Run database setup
    }

    @Test
    public void TestInsertSelectAndDeleteForEncParametersTable() {
        // Perform a SELECT query
        String table = ENC_PARAMS_TABLE;
        List<DatabaseData> result = select(COLLECTION_ID, FILE_ID, table);

        // Assert that the query returned an empty object - since it should not already exist
        assertTrue(result.isEmpty());

        // Insert some information
        insertInto(COLLECTION_ID, FILE_ID, ENC_PARAMS_SALT, ENC_PARAMS_IV, ENC_PARAMS_ITERATIONS);

        // Get the information from the table with a SELECT query
        result = select(COLLECTION_ID, FILE_ID, table);

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
        // Perform a SELECT query
        List<DatabaseData> result = select(COLLECTION_ID, FILE_ID, table);

        // Assert that the query returned an empty object - since it should not already exist
        assertTrue(result.isEmpty());

        // Insert some information
        insertInto(COLLECTION_ID, FILE_ID, FILES_RECEIVED_TIMESTAMP, FILES_ENCRYPTED_TIMESTAMP,
                FILES_CHECKSUM, FILES_ENC_CHECKSUM, FILES_CHECKSUM_TIMESTAMP);

        // Get the information from the table with a SELECT query
        result = select(COLLECTION_ID, FILE_ID, table);

        // Assert that there is now a result
        assertFalse(result.isEmpty());

        // Assert that we get the correct data type
        DatabaseData firstResult = result.get(0);
        assertTrue("Result should be of EncParametersData type.", firstResult instanceof FilesData);

        // Check that the information is correct
        assertThat(firstResult.getCollectionID(), is(COLLECTION_ID));
        assertThat(firstResult.getFileID(), is(FILE_ID));
        assertThat(((FilesData) firstResult).getReceivedTimestamp(), is(FILES_RECEIVED_TIMESTAMP));
        assertThat(((FilesData) firstResult).getEncryptedTimestamp(), is(FILES_ENCRYPTED_TIMESTAMP));
        assertThat(((FilesData) firstResult).getChecksum(), is(FILES_CHECKSUM));
        assertThat(((FilesData) firstResult).getEncryptedChecksum(), is(FILES_ENC_CHECKSUM));
        assertThat(((FilesData) firstResult).getChecksumTimestamp(), is(FILES_CHECKSUM_TIMESTAMP));

        cleanUp(COLLECTION_ID, FILE_ID, table);
        result = select(COLLECTION_ID, FILE_ID, table);
        assertTrue(result.isEmpty());
    }

    @Test
    public void TestUpdateEncryptedTimestampInFilesTable() {
        String table = FILES_TABLE;

        insertInto(COLLECTION_ID, FILE_ID, FILES_RECEIVED_TIMESTAMP, FILES_ENCRYPTED_TIMESTAMP,
                FILES_CHECKSUM, FILES_ENC_CHECKSUM, FILES_CHECKSUM_TIMESTAMP);

        List<DatabaseData> result = select(COLLECTION_ID, FILE_ID, table);

        OffsetDateTime oldTimestamp = ((FilesData) result.get(0)).getEncryptedTimestamp();
        System.out.println(oldTimestamp);
        OffsetDateTime newTimestamp = OffsetDateTime.now(ZoneOffset.UTC);
        System.out.println(newTimestamp);

        updateTimestamp(COLLECTION_ID, FILE_ID, FILES_ENCRYPTED_TIMESTAMP_NAME, newTimestamp);
        result = select(COLLECTION_ID, FILE_ID, table);
        FilesData firstResult = (FilesData) result.get(0);

        assertThat(firstResult.getEncryptedTimestamp(), is(newTimestamp));
        assertNotEquals(oldTimestamp, firstResult.getEncryptedTimestamp());

        cleanUp(COLLECTION_ID, FILE_ID, table);
        result = select(COLLECTION_ID, FILE_ID, table);
        assertTrue(result.isEmpty());
    }

    private void cleanUp(String collectionID, String fileID, String table) {
        delete(collectionID, fileID, table);
    }
}

package dk.kb.bitrepository.database;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static dk.kb.bitrepository.database.DatabaseCalls.*;
import static dk.kb.bitrepository.database.DatabaseConstants.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public class TestDatabaseCalls {
    @Before
    public void setUp() throws Exception {
        // Drop tables
        // Run database setup
    }

    @Test
    public void TestInsertSelectAndDeleteForEncParametersTable() {
        // Perform a SELECT query
        List<DatabaseData> result = select(COLLECTION_ID, FILE_ID, ENC_PARAMS_TABLE);

        // Assert that the query returned an empty object - since it should not already exist
        assertTrue(result.isEmpty());

        // Insert some information
        insertInto(COLLECTION_ID, FILE_ID, ENC_PARAMS_SALT, ENC_PARAMS_IV, ENC_PARAMS_ITERATIONS);

        // Get the information from the table with a SELECT query
        result = select(COLLECTION_ID, FILE_ID, ENC_PARAMS_TABLE);

        // Check that the information is correct
        if (!result.isEmpty()) {
            if (result.get(0) instanceof EncParameters) {
                assertThat(result.get(0).getCollectionID(), is(COLLECTION_ID));
                assertThat(result.get(0).getFileID(), is(FILE_ID));
                assertThat(((EncParameters) result.get(0)).getSalt(), is(ENC_PARAMS_SALT));
                assertThat(((EncParameters) result.get(0)).getIv(), is(ENC_PARAMS_IV));
                assertThat(((EncParameters) result.get(0)).getIterations(), is(ENC_PARAMS_ITERATIONS));
            }
        }
        // Delete the information using the composite key (collection_id, file_id)
        delete(COLLECTION_ID, FILE_ID, ENC_PARAMS_TABLE);

        // Perform a SELECT query again
        result = select(COLLECTION_ID, FILE_ID, ENC_PARAMS_TABLE);

        // Assert that the query returned an empty object since it should have been deleted
        assertTrue(result.isEmpty());
    }

}

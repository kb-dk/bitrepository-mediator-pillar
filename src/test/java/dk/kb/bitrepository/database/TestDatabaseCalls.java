package dk.kb.bitrepository.database;

import org.junit.jupiter.api.Test;

import java.util.List;

import static dk.kb.bitrepository.database.DatabaseConstants.ENC_PARAMS_TABLE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestDatabaseCalls {

    @Test
    void TestGetFile() {
        List<DatabaseData> result = DatabaseCalls.getFile("collection1", "file1", ENC_PARAMS_TABLE);
        if (!result.isEmpty()) {
            if (result.get(0) instanceof EncParameters) {
                assertThat(result.get(0).getFileID(), is("file1"));
                assertThat(((EncParameters) result.get(0)).getIv(), is("iv"));
                assertEquals("collection1", result.get(0).getCollectionID(), "Expected collectionID to be 'collection1'");
            }
        }
    }
}

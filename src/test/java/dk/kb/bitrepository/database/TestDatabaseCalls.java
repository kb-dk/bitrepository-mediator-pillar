package dk.kb.bitrepository.database;

import org.junit.jupiter.api.Test;

import java.util.List;

import static dk.kb.bitrepository.database.DatabaseCalls.getFile;
import static dk.kb.bitrepository.database.DatabaseCalls.insertInto;
import static dk.kb.bitrepository.database.DatabaseConstants.ENC_PARAMS_TABLE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestDatabaseCalls {
    private static final String collection_id = "collection_id_test";
    private static final String file_id = "file_id_test";
    private static final String salt = "salt_test";
    private static final String iv = "iv_test";
    private static final String iterations = "iterations_test";

    private static final String received_ts = "received_ts_test";
    private static final String encrypted_ts = "encrypted_ts_test";
    private static final String checksum = "checksum_test";
    private static final String encrypted_checksum = "enc_checksum_test";
    private static final String checksum_ts = "checksum_ts_test";

    @Test
    void TestInsertIntoEncParameters() {
        insertInto(collection_id, file_id, salt, iv, iterations);
    }

    @Test
    void TestGetFile() {
        // Insert

        List<DatabaseData> result = getFile("collection1", "file1", ENC_PARAMS_TABLE);
        if (!result.isEmpty()) {
            if (result.get(0) instanceof EncParameters) {
                assertThat(result.get(0).getFileID(), is("file1"));
                assertThat(((EncParameters) result.get(0)).getIv(), is("iv"));
                assertEquals("collection1", result.get(0).getCollectionID(), "Expected collectionID to be 'collection1'");
            }
        }

        // Clean-up
    }
}

package dk.kb.bitrepository.mediator.filehandler;

import dk.kb.bitrepository.mediator.IntegrationFileHandlerTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.file.Path;

import static dk.kb.bitrepository.mediator.TestingUtilities.cleanupFiles;
import static dk.kb.bitrepository.mediator.database.DatabaseConstants.COLLECTION_ID;
import static dk.kb.bitrepository.mediator.database.DatabaseConstants.FILE_ID;
import static dk.kb.bitrepository.mediator.filehandler.FileUtils.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test File Utilities")
public class FileUtilsIT extends IntegrationFileHandlerTest {
    @BeforeAll
    static void setup() {
        initTestingDAO(false);
    }

    @AfterEach
    void clean() {
        cleanupFiles(UNENCRYPTED_FILES_PATH);
        cleanupFiles(ENCRYPTED_FILES_PATH);
    }

    @Test
    @DisplayName("Test Writing Bytes to File")
    public void testThatFileIsWrittenLocally() {
        assertDoesNotThrow(() -> writeBytesToFile(fileBytes, UNENCRYPTED_FILES_PATH, COLLECTION_ID, FILE_ID));
        assertTrue(fileExists(Path.of(UNENCRYPTED_FILES_PATH + "/" + COLLECTION_ID + "/" + FILE_ID)));
        assertEquals(fileContent,
                new String(readBytesFromFile(UNENCRYPTED_FILES_PATH + "/" + COLLECTION_ID + "/" + FILE_ID), Charset.defaultCharset()));
    }

    @Test
    @DisplayName("Test Writing Encrypted Bytes to File")
    public void testThatEncryptedFileIsWrittenLocally() {
        byte[] encryptedString = crypto.encrypt(fileBytes);
        assertDoesNotThrow(() -> writeBytesToFile(encryptedString, ENCRYPTED_FILES_PATH, COLLECTION_ID, FILE_ID));
        assertTrue(fileExists(ENCRYPTED_FILES_PATH, COLLECTION_ID, FILE_ID));
        assertEquals(fileContent,
                new String(crypto.decrypt(readBytesFromFile(ENCRYPTED_FILES_PATH + "/" + COLLECTION_ID + "/" + FILE_ID)),
                        Charset.defaultCharset()));
    }

    @Test
    @DisplayName("Test deletion of local file")
    public void testDeleteFileLocally() {
        assertDoesNotThrow(() -> writeBytesToFile(fileBytes, UNENCRYPTED_FILES_PATH, COLLECTION_ID, FILE_ID));
        deleteFileLocally(UNENCRYPTED_FILES_PATH, COLLECTION_ID, FILE_ID);
        assertFalse(fileExists(UNENCRYPTED_FILES_PATH, COLLECTION_ID, FILE_ID));
    }
}

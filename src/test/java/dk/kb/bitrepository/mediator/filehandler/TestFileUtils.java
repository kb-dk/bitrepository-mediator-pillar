package dk.kb.bitrepository.mediator.filehandler;

import dk.kb.bitrepository.mediator.IntegrationFileHandlerTest;
import dk.kb.bitrepository.mediator.crypto.AESCryptoStrategy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static dk.kb.bitrepository.mediator.TestingUtilities.cleanupFiles;
import static dk.kb.bitrepository.mediator.database.DatabaseConstants.COLLECTION_ID;
import static dk.kb.bitrepository.mediator.database.DatabaseConstants.FILE_ID;
import static dk.kb.bitrepository.mediator.filehandler.FileUtils.writeBytesToFile;
import static dk.kb.bitrepository.mediator.utils.configurations.ConfigConstants.ENCRYPTED_FILES_PATH;
import static dk.kb.bitrepository.mediator.utils.configurations.ConfigConstants.UNENCRYPTED_FILES_PATH;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Test File Utilities")
public class TestFileUtils extends IntegrationFileHandlerTest {
    // TODO: relative path in method call
    private static byte[] testFileBytes;

    @BeforeAll
    static void setup() throws IOException {
        initTestingDAO(false);
        testFileBytes = fileBytes;
    }

    @AfterEach
    void clean() {
        cleanupFiles(UNENCRYPTED_FILES_PATH);
        cleanupFiles(ENCRYPTED_FILES_PATH);
    }

    @Test
    @DisplayName("Test Writing Bytes to File")
    public void testWritingBytesToFile() {
        assertDoesNotThrow(() -> writeBytesToFile(testFileBytes, UNENCRYPTED_FILES_PATH, COLLECTION_ID, FILE_ID));
        assertTrue(Files.exists(Path.of(UNENCRYPTED_FILES_PATH + "/" + COLLECTION_ID + "/" + FILE_ID)));
    }

    @Test
    @DisplayName("Test Encrypting File is Being Written.")
    public void testThatEncryptedFileIsWrittenToDisk() {
        byte[] encryptedString = new AESCryptoStrategy(encryptionPassword).encrypt(testFileBytes);
        assertDoesNotThrow(() -> writeBytesToFile(encryptedString, ENCRYPTED_FILES_PATH, COLLECTION_ID, FILE_ID));
        assertTrue(Files.exists(Path.of(ENCRYPTED_FILES_PATH + "/" + COLLECTION_ID + "/" + FILE_ID)));
    }
}

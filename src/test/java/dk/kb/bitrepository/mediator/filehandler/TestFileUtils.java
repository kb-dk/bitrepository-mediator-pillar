package dk.kb.bitrepository.mediator.filehandler;

import dk.kb.bitrepository.mediator.TestingSetup;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Test File Utilities")
public class TestFileUtils {
    // TODO: relative path in method call
    private final String fileDirectory = "src/test/resources/files";
    private final String encryptedFileDirectory = "src/test/resources/encrypted-files";
    private static String encryptionPassword;
    private static byte[] testFileBytes;

    @BeforeAll
    static void setup() throws IOException {
        TestingSetup setup = new TestingSetup(false);
        encryptionPassword = setup.getCryptoConfigurations().getPassword();
        testFileBytes = setup.getFileBytes();
    }

    @AfterEach
    public void afterEach() {
        cleanupFiles(fileDirectory);
        cleanupFiles(encryptedFileDirectory);
    }

    @Test
    @DisplayName("Test Writing Bytes to File")
    public void testWritingBytesToFile() {
        writeBytesToFile(testFileBytes, fileDirectory, COLLECTION_ID, FILE_ID);
        assertTrue(Files.exists(Path.of(fileDirectory + "/" + COLLECTION_ID + "/" + FILE_ID)));
    }

    @Test
    @DisplayName("Test Encrypting File is Being Written.")
    public void testThatEncryptedFileIsWrittenToDisk() {
        byte[] encryptedString = new AESCryptoStrategy(encryptionPassword).encrypt(testFileBytes);
        writeBytesToFile(encryptedString, encryptedFileDirectory, COLLECTION_ID, FILE_ID);
        assertTrue(Files.exists(Path.of(encryptedFileDirectory + "/" + COLLECTION_ID + "/" + FILE_ID)));
    }
}

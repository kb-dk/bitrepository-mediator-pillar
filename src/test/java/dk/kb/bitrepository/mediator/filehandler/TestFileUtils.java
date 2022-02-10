package dk.kb.bitrepository.mediator.filehandler;

import dk.kb.bitrepository.mediator.MediatorComponentFactory;
import dk.kb.bitrepository.mediator.crypto.AESCryptoStrategy;
import dk.kb.bitrepository.mediator.utils.configurations.Configurations;
import dk.kb.bitrepository.mediator.utils.configurations.CryptoConfigurations;
import org.apache.commons.io.FileExistsException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import static dk.kb.bitrepository.mediator.database.DatabaseConstants.COLLECTION_ID;
import static dk.kb.bitrepository.mediator.database.DatabaseConstants.FILE_ID;
import static dk.kb.bitrepository.mediator.filehandler.FileUtils.writeBytesToFile;
import static org.apache.commons.io.FileUtils.cleanDirectory;
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
        Configurations testConfig = MediatorComponentFactory.loadMediatorConfigurations("conf");
        CryptoConfigurations cryptoConfigurations = testConfig.getCryptoConfig();
        encryptionPassword = cryptoConfigurations.getPassword();
        testFileBytes = "teststring".getBytes(Charset.defaultCharset());
    }

    @AfterEach
    public void afterEach() {
        cleanupFiles(fileDirectory);
        cleanupFiles(encryptedFileDirectory);
    }

    @Test
    @DisplayName("Test Writing Bytes to File")
    public void testWritingBytesToFile() {
        try {
            writeBytesToFile(testFileBytes, fileDirectory, COLLECTION_ID, FILE_ID);
        } catch (FileExistsException e) {
            System.out.println("File already exists.");
        }

        assertTrue(Files.exists(Path.of(fileDirectory + "/" + COLLECTION_ID + "/" + FILE_ID)));
    }

    @Test
    @DisplayName("Test Encrypting File is Being Written.")
    public void testThatEncryptedFileIsWrittenToDisk() {
        byte[] encryptedString = new AESCryptoStrategy(encryptionPassword).encrypt(testFileBytes);

        try {
            writeBytesToFile(encryptedString, encryptedFileDirectory, COLLECTION_ID, FILE_ID);
        } catch (FileExistsException e) {
            System.out.println("File already exists.");
        }

        assertTrue(Files.exists(Path.of(encryptedFileDirectory + "/" + COLLECTION_ID + "/" + FILE_ID)));
    }

    public void cleanupFiles(String path) {
        File dir = new File(path);
        if (dir.exists()) {
            try {
                cleanDirectory(dir);
            } catch (IOException e) {
                System.out.println("Something went wrong trying to clean up /files/ directory." + e);
            }
        }
    }
}

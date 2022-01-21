package dk.kb.bitrepository.crypto;

import dk.kb.bitrepository.utils.crypto.AESCryptoStrategy;
import dk.kb.bitrepository.utils.crypto.CryptoStrategy;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test AES Cryptographic Strategy")
public class TestAESCryptoStrategy {
    private String testPassword = "12345678";
    private static String testFilePath = TestAESCryptoStrategy.class.getClassLoader().getResource("crypto/fileToEncrypt.txt").getPath();
    private static Path inputFile;

    @TempDir
    static Path tempDir;

    @BeforeAll
    static void setup() throws IOException {
        Path testFileResource = Path.of(testFilePath);
        inputFile = tempDir.resolve(testFileResource);
        Files.copy(testFileResource, inputFile);
    }

    @Test
    @DisplayName("Test that encrypting and decrypting results in the correct output.")
    public void testEncryptionDecryptionGivesOriginalOutput() throws IOException {
        Path encryptedFile = tempDir.resolve("encryptedFile");
        Path decryptedFile = tempDir.resolve("decryptedFile");

        CryptoStrategy aes = new AESCryptoStrategy(testPassword);
        aes.encrypt(inputFile, encryptedFile);
        aes.decrypt(encryptedFile, decryptedFile);

        assertEquals(Files.readString(inputFile), Files.readString(decryptedFile));
    }

    @Test
    public void testCantReadContentOfEncryptedFile() throws IOException {
        String inputContent = Files.readString(inputFile);
        Path encryptedFile = tempDir.resolve("encryptedFile");
        CryptoStrategy aes = new AESCryptoStrategy(testPassword);

        aes.encrypt(inputFile, encryptedFile);
        String encryptedContent;
        try (InputStream is = Files.newInputStream(encryptedFile)) { // Bit scuffed to read the bytes directly - base64 instead?
            encryptedContent = IOUtils.toString(is, StandardCharsets.UTF_8);
        }

        assertNotEquals(inputContent, encryptedContent);
    }

    @Test
    public void testWrongPasswordGivesBadDecryption() {
        Path encryptedFile = tempDir.resolve("encryptedFile");
        Path decryptedFile = tempDir.resolve("decryptedFile");
        AESCryptoStrategy encryption = new AESCryptoStrategy(testPassword);
        AESCryptoStrategy decryption = new AESCryptoStrategy("87654321");

        encryption.encrypt(inputFile, encryptedFile);
        assertThrows(IllegalStateException.class, () -> decryption.decrypt(encryptedFile, decryptedFile));
    }
}

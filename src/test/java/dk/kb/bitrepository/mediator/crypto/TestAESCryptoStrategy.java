package dk.kb.bitrepository.mediator.crypto;

import dk.kb.bitrepository.utils.crypto.AESCryptoStrategy;
import dk.kb.bitrepository.utils.crypto.CryptoStrategy;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestAESCryptoStrategy {
    private String testPassword = "12345678";
    private String testFilePath = getClass().getClassLoader().getResource("crypto/fileToEncrypt.txt").getPath();
    private Path inputFile;

    @TempDir
    Path tempDir;

    @BeforeEach
    public void setupTest() throws IOException {
        Path testFileResource = Path.of(testFilePath);
        inputFile = tempDir.resolve(testFileResource.getFileName());
        Files.copy(testFileResource, inputFile);
    }

    @Test
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

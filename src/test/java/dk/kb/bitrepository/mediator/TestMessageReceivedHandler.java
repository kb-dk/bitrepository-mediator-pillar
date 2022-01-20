package dk.kb.bitrepository.mediator;

import dk.kb.bitrepository.database.DatabaseData;
import dk.kb.bitrepository.database.configs.ConfigurationHandler;
import dk.kb.bitrepository.mediator.communication.MessageReceivedHandler;
import dk.kb.bitrepository.mediator.communication.MockupMessageObject;
import dk.kb.bitrepository.mediator.communication.MockupResponse;
import dk.kb.bitrepository.utils.crypto.AESCryptoStrategy;
import dk.kb.bitrepository.utils.crypto.CryptoStrategy;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static dk.kb.bitrepository.database.DatabaseCalls.delete;
import static dk.kb.bitrepository.database.DatabaseCalls.select;
import static dk.kb.bitrepository.database.DatabaseConstants.*;
import static dk.kb.bitrepository.database.DatabaseData.EncryptedParametersData;
import static dk.kb.bitrepository.database.DatabaseData.FilesData;
import static dk.kb.bitrepository.mediator.communication.MessageReceivedHandler.*;
import static dk.kb.bitrepository.mediator.communication.MockupMessageType.GET_FILE;
import static dk.kb.bitrepository.mediator.communication.MockupMessageType.PUT_FILE;
import static org.bitrepository.common.utils.ChecksumUtils.generateChecksum;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class TestMessageReceivedHandler {
    private MockupMessageObject message;
    private String encryptionPassword;
    private String testString;
    private String encryptedFilePath;
    private String filePath;
    private MessageReceivedHandler handler;
    private String decryptedFilePath;
    private byte[] payload;


    @Before
    public void setup() throws IOException {
        ConfigurationHandler config = new ConfigurationHandler();
        handler = new MessageReceivedHandler(config);
        encryptionPassword = config.getEncryptionPassword();

        filePath = getFilePath(COLLECTION_ID, FILE_ID);
        encryptedFilePath = getEncryptedFilePath(COLLECTION_ID, FILE_ID);
        decryptedFilePath = getDecryptedFilePath(COLLECTION_ID, FILE_ID);

        testString = "test string";
        payload = testString.getBytes();
    }

    @After
    public void cleanup() {
        delete(COLLECTION_ID, FILE_ID, ENC_PARAMS_TABLE);
        delete(COLLECTION_ID, FILE_ID, FILES_TABLE);
        handler.cleanupFiles();
    }

    @Test
    public void testPutFile() throws IOException {
        message = new MockupMessageObject(PUT_FILE, COLLECTION_ID, FILE_ID, payload);
        boolean handled = (boolean) handler.handleReceivedMessage(message);
        assertTrue(handled);

        // Get the used encryption parameters from the 'enc_parameters' table
        List<DatabaseData> result = select(COLLECTION_ID, FILE_ID, ENC_PARAMS_TABLE);
        EncryptedParametersData firstEncParamResult = (EncryptedParametersData) result.get(0);
        String salt = firstEncParamResult.getSalt();
        byte[] iv = firstEncParamResult.getIv();
        CryptoStrategy AES = new AESCryptoStrategy(encryptionPassword, salt, iv);

        // Decrypt the file
        AES.decrypt(Paths.get(encryptedFilePath), Paths.get(decryptedFilePath));

        // Assert that Checksums match
        {
            result = select(COLLECTION_ID, FILE_ID, FILES_TABLE);
            FilesData firstFilesResult = (FilesData) result.get(0);
            String newChecksum = generateChecksum(new File(decryptedFilePath), ChecksumType.MD5);
            String newEncryptedChecksum = generateChecksum(new File(encryptedFilePath), ChecksumType.MD5);

            assertEquals(newChecksum, firstFilesResult.getChecksum());
            assertEquals(newEncryptedChecksum, firstFilesResult.getEncryptedChecksum());
        }

        // Assert that the decrypted file is equal to the originally created file,
        // and that the decrypted file contains the chosen string
        {
            assertEquals(Files.readAllLines(Paths.get(decryptedFilePath)), Files.readAllLines(Paths.get(filePath)));
            assertThat(Files.readString(Paths.get(decryptedFilePath)), is(testString));
        }
    }

    @Test
    public void testGetFile() throws IOException {
        message = new MockupMessageObject(PUT_FILE, COLLECTION_ID, FILE_ID, payload);
        // Put file
        boolean handled = (boolean) handler.handleReceivedMessage(message);
        assertTrue(handled);

        // Get salt and IV
        List<DatabaseData> filesResult = select(COLLECTION_ID, FILE_ID, ENC_PARAMS_TABLE);
        EncryptedParametersData firstFilesResult = (EncryptedParametersData) filesResult.get(0);
        AESCryptoStrategy AES = new AESCryptoStrategy(encryptionPassword, firstFilesResult.getSalt(), firstFilesResult.getIv());
        // Write payload to local file
        writeBytesToFile(payload, Path.of(filePath));
        // Encrypt the file
        AES.encrypt(Path.of(filePath), Path.of(encryptedFilePath));
        byte[] encryptedPayload = new byte[0];
        // Read the encrypted file, and created encrypted payload
        try {
            encryptedPayload = Files.readAllBytes(Path.of(encryptedFilePath));
        } catch (IOException e) {
            System.out.println("Couldn't read the encrypted file." + e);
        }

        // Create a mockup message object with a mockup response containing the encrypted payload.
        message = new MockupMessageObject(GET_FILE, COLLECTION_ID, FILE_ID, payload, new MockupResponse(encryptedPayload));
        // Get file using the mockup
        byte[] bytesReceived = (byte[]) handler.handleReceivedMessage(message);
        writeBytesToFile(bytesReceived, Path.of(filePath + ":received"));

        assertTrue(bytesReceived.length > 0);
        assertEquals(Files.readAllLines(Path.of(filePath)), Files.readAllLines(Path.of(filePath + ":received")));
    }
}

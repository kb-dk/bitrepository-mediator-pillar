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
    private MessageReceivedHandler handler;
    private Path filePath;
    private Path encryptedFilePath;
    private Path decryptedFilePath;
    private byte[] payload;


    @Before
    public void setup() throws IOException {
        ConfigurationHandler config = new ConfigurationHandler();
        handler = new MessageReceivedHandler(config);
        encryptionPassword = config.getEncryptionPassword();

        filePath = Path.of(getFilePath(COLLECTION_ID, FILE_ID));
        encryptedFilePath = Path.of(getEncryptedFilePath(COLLECTION_ID, FILE_ID));
        decryptedFilePath = Path.of(getDecryptedFilePath(COLLECTION_ID, FILE_ID));

        testString = "test string";
        payload = testString.getBytes();
    }

    @After
    public void cleanup() {
        delete(COLLECTION_ID, FILE_ID, ENC_PARAMS_TABLE);
        delete(COLLECTION_ID, FILE_ID, FILES_TABLE);
        cleanupFiles();
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
        AES.decrypt(encryptedFilePath, decryptedFilePath);

        // Assert that Checksums match
        {
            result = select(COLLECTION_ID, FILE_ID, FILES_TABLE);
            FilesData firstFilesResult = (FilesData) result.get(0);
            String newChecksum = generateChecksum(new File(decryptedFilePath.toString()), ChecksumType.MD5);
            String newEncryptedChecksum = generateChecksum(new File(encryptedFilePath.toString()), ChecksumType.MD5);

            assertEquals(newChecksum, firstFilesResult.getChecksum());
            assertEquals(newEncryptedChecksum, firstFilesResult.getEncryptedChecksum());
        }

        // Assert that the decrypted file is equal to the originally created file,
        // and that the decrypted file contains the chosen string
        {
            assertEquals(Files.readAllLines(decryptedFilePath), Files.readAllLines(filePath));
            assertThat(Files.readString(decryptedFilePath), is(testString));
        }
        cleanupFiles();
    }

    @Test
    public void testGetFile() throws IOException {
        // Put file using MockupMessage with the payload
        message = new MockupMessageObject(PUT_FILE, COLLECTION_ID, FILE_ID, payload);
        boolean handled = (boolean) handler.handleReceivedMessage(message);
        assertTrue(handled);

        // Read the encrypted file, and created encrypted payload
        byte[] encryptedPayload = new byte[0];
        try {
            encryptedPayload = Files.readAllBytes(encryptedFilePath);
        } catch (IOException e) {
            System.out.println("Couldn't read the encrypted file." + e);
        }

        // Running a cleanup here, as that is what you'd do after a PUT_FILE request
        cleanupFiles();

        // Create a mockup message object with a mockup response containing the encrypted payload.
        message = new MockupMessageObject(GET_FILE, COLLECTION_ID, FILE_ID, payload, new MockupResponse(encryptedPayload));

        // Get file using the mockup request
        byte[] bytesReceived = (byte[]) handler.handleReceivedMessage(message);
        writeBytesToFile(bytesReceived, Path.of(filePath + ":received"));

        assertTrue(bytesReceived.length > 0);
        assertEquals(testString, Files.readString(Path.of(filePath.toString() + ":received")));
    }
}

package dk.kb.bitrepository.mediator;

import dk.kb.bitrepository.database.DatabaseData;
import dk.kb.bitrepository.database.configs.ConfigurationHandler;
import dk.kb.bitrepository.mediator.communication.MessageReceivedHandler;
import dk.kb.bitrepository.mediator.communication.MockupMessageObject;
import dk.kb.bitrepository.mediator.communication.MockupResponse;
import dk.kb.bitrepository.utils.crypto.AESCryptoStrategy;
import dk.kb.bitrepository.utils.crypto.CryptoStrategy;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static dk.kb.bitrepository.database.DatabaseCalls.delete;
import static dk.kb.bitrepository.database.DatabaseCalls.select;
import static dk.kb.bitrepository.database.DatabaseConstants.*;
import static dk.kb.bitrepository.database.DatabaseData.EncryptedParametersData;
import static dk.kb.bitrepository.database.DatabaseData.FilesData;
import static dk.kb.bitrepository.mediator.communication.MockupMessageType.*;
import static org.bitrepository.common.utils.ChecksumUtils.generateChecksum;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test MessageReceivedHandler")
public class TestMessageReceivedHandler {
    private MockupMessageObject message;
    private static String encryptionPassword;
    private static String testString;
    private static MessageReceivedHandler handler;
    private static byte[] payload;
    private static ChecksumSpecTYPE checksumSpecTYPE;


    @BeforeAll
    static void setup() throws IOException {
        ConfigurationHandler config = new ConfigurationHandler();
        handler = new MessageReceivedHandler(config);
        encryptionPassword = config.getEncryptionPassword();

        testString = "test string";
        payload = testString.getBytes(Charset.defaultCharset());

        checksumSpecTYPE = new ChecksumSpecTYPE();
        checksumSpecTYPE.setChecksumType(ChecksumType.MD5);
    }

    @AfterEach
    public void cleanup() {
        delete(COLLECTION_ID, FILE_ID, ENC_PARAMS_TABLE, true);
        delete(COLLECTION_ID, FILE_ID, FILES_TABLE, true);
    }

    @Test
    @DisplayName("Test #PutFile()")
    public void testPutFile() {
        message = new MockupMessageObject(PUT_FILE, COLLECTION_ID, FILE_ID, payload);
        boolean handled = (boolean) handler.handleReceivedMessage(message);
        assertTrue(handled);

        // Decrypt the file
        CryptoStrategy AES = setupCryptoStrategy();
        byte[] encryptedPayload = AES.encrypt(payload);

        // Compute new checksums
        String newChecksum = generateChecksum(new ByteArrayInputStream(payload), checksumSpecTYPE);
        String newEncryptedChecksum = generateChecksum(new ByteArrayInputStream(encryptedPayload), checksumSpecTYPE);

        // Assert that checksums match
        List<DatabaseData> result = select(COLLECTION_ID, FILE_ID, FILES_TABLE);
        FilesData firstFilesResult = (FilesData) result.get(0);
        assertEquals(firstFilesResult.getChecksum(), newChecksum);
        assertEquals(firstFilesResult.getEncryptedChecksum(), newEncryptedChecksum);

        // Assert that the decrypted bytes are equal to the original payload bytes
        assertEquals(testString, new String(AES.decrypt(encryptedPayload), StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("Test #GetFile()")
    public void testGetFile() {
        // Put file using MockupMessage with the payload
        message = new MockupMessageObject(PUT_FILE, COLLECTION_ID, FILE_ID, payload);
        boolean handled = (boolean) handler.handleReceivedMessage(message);
        assertTrue(handled);

        // Created encrypted payload from PutFile payload
        CryptoStrategy AES = setupCryptoStrategy();
        byte[] encryptedPayload = AES.encrypt(payload);

        // Create a mockup message object with a mockup response containing the encrypted payload.
        message = new MockupMessageObject(GET_FILE, COLLECTION_ID, FILE_ID, null, new MockupResponse(encryptedPayload));

        // Get file using the mockup request
        byte[] bytesReceived = (byte[]) handler.handleReceivedMessage(message);

        assertTrue(bytesReceived.length > 0);
        assertEquals(testString, new String(bytesReceived));
    }

    @Test
    @DisplayName("Test of #GetFile() to return an empty byte[] when it can't find the file")
    public void testGetFileEmptyResponse() {
        message = new MockupMessageObject(GET_FILE, COLLECTION_ID, FILE_ID, payload, new MockupResponse(payload));
        byte[] result = (byte[]) handler.handleReceivedMessage(message);
        assertEquals(0, result.length);
    }

    @Test
    @DisplayName("Test of #GetFile() to return an empty byte[] when it gets no response from the encrypted pillar")
    public void testGetFileNoResponseFromPillar() {
        //Mockup Message with no Response, so it defaults to null
        message = new MockupMessageObject(GET_FILE, COLLECTION_ID, FILE_ID, payload);
        byte[] result = (byte[]) handler.handleReceivedMessage(message);
        assertEquals(0, result.length);
    }

    @Test
    @DisplayName("Test #DeleteFile()")
    public void testDeleteFile() {
        message = new MockupMessageObject(PUT_FILE, COLLECTION_ID, FILE_ID, payload);
        // Put file using MockupMessage with the payload
        boolean putFileHandled = (boolean) handler.handleReceivedMessage(message);
        assertTrue(putFileHandled);

        message = new MockupMessageObject(DELETE_FILE, COLLECTION_ID, FILE_ID);
        // Compute the checksum of the payload bytes
        String payloadChecksum = generateChecksum(new ByteArrayInputStream(payload), checksumSpecTYPE);

        // Delete file and assert that the checksum of the deleted file is equal to the checksum of the payload
        String checksumDeleted = (String) handler.handleReceivedMessage(message);
        assertNotNull(checksumDeleted);
        assertEquals(payloadChecksum, checksumDeleted);

        // Assert that the local database indexes are deleted
        List<DatabaseData> filesData = select(COLLECTION_ID, FILE_ID, FILES_TABLE);
        List<DatabaseData> encryptionParameterData = select(COLLECTION_ID, FILE_ID, ENC_PARAMS_TABLE);
        assertTrue(filesData.isEmpty());
        assertTrue(encryptionParameterData.isEmpty());
    }

    @Test
    @DisplayName("Test of #DeteleFile() to return null when it can't find the file")
    public void testDeleteFileEmptyResponse() {
        // Concatenating "test" to the end of the primary key strings to not hit a correct index
        message = new MockupMessageObject(DELETE_FILE, COLLECTION_ID, FILE_ID + "test");
        assertNull(handler.handleReceivedMessage(message));
    }

    @Test
    @DisplayName("Test of #UpdateFile()")
    public void testUpdateFile() {

    }

    @NotNull
    private CryptoStrategy setupCryptoStrategy() {
        // Get the used encryption parameters from the 'enc_parameters' table
        List<DatabaseData> result = select(COLLECTION_ID, FILE_ID, ENC_PARAMS_TABLE);
        EncryptedParametersData firstEncParamResult = (EncryptedParametersData) result.get(0);
        String salt = firstEncParamResult.getSalt();
        byte[] iv = firstEncParamResult.getIv();
        
        return new AESCryptoStrategy(encryptionPassword, salt, iv);
    }
}

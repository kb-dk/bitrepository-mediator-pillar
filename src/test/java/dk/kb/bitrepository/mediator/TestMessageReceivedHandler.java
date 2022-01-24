package dk.kb.bitrepository.mediator;

import dk.kb.bitrepository.database.DatabaseData;
import dk.kb.bitrepository.database.configs.ConfigurationHandler;
import dk.kb.bitrepository.mediator.communication.MessageReceivedHandler;
import dk.kb.bitrepository.mediator.communication.MockupMessageObject;
import dk.kb.bitrepository.mediator.communication.MockupResponse;
import dk.kb.bitrepository.utils.crypto.AESCryptoStrategy;
import dk.kb.bitrepository.utils.crypto.CryptoStrategy;
import org.apache.commons.io.FileExistsException;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static dk.kb.bitrepository.database.DatabaseCalls.delete;
import static dk.kb.bitrepository.database.DatabaseCalls.select;
import static dk.kb.bitrepository.database.DatabaseConstants.*;
import static dk.kb.bitrepository.database.DatabaseData.EncryptedParametersData;
import static dk.kb.bitrepository.database.DatabaseData.FilesData;
import static dk.kb.bitrepository.mediator.communication.MessageReceivedHandler.*;
import static dk.kb.bitrepository.mediator.communication.MockupMessageType.*;
import static org.bitrepository.common.utils.ChecksumUtils.generateChecksum;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test MessageReceivedHandler")
public class TestMessageReceivedHandler {
    private MockupMessageObject message;
    private static String encryptionPassword;
    private static String testString;
    private static MessageReceivedHandler handler;
    private static Path filePath;
    private static Path encryptedFilePath;
    private static Path decryptedFilePath;
    private static byte[] payload;


    @BeforeAll
    static void setup() throws IOException {
        ConfigurationHandler config = new ConfigurationHandler();
        handler = new MessageReceivedHandler(config);
        encryptionPassword = config.getEncryptionPassword();

        filePath = Path.of(getFilePath(COLLECTION_ID, FILE_ID));
        encryptedFilePath = Path.of(getEncryptedFilePath(COLLECTION_ID, FILE_ID));
        decryptedFilePath = Path.of(getDecryptedFilePath(COLLECTION_ID, FILE_ID));

        testString = "test string";
        payload = testString.getBytes(Charset.defaultCharset());
    }

    @AfterEach
    public void cleanup() {
        delete(COLLECTION_ID, FILE_ID, ENC_PARAMS_TABLE, false);
        delete(COLLECTION_ID, FILE_ID, FILES_TABLE, false);
        cleanupFiles();
    }

    @Test
    public void testPutFile() throws IOException {
        message = new MockupMessageObject(PUT_FILE, COLLECTION_ID, FILE_ID, payload);
        boolean handled = (boolean) handler.handleReceivedMessage(message);
        assertTrue(handled);


        // Decrypt the file
        CryptoStrategy AES = setupCryptoStrategy();
        AES.decrypt(encryptedFilePath, decryptedFilePath);

        List<DatabaseData> result;
        // Assert that Checksums match
        {
            result = select(COLLECTION_ID, FILE_ID, FILES_TABLE);
            FilesData firstFilesResult = (FilesData) result.get(0);
            String newChecksum = generateChecksum(new File(decryptedFilePath.toString()), ChecksumType.MD5);
            String newEncryptedChecksum = generateChecksum(new File(encryptedFilePath.toString()), ChecksumType.MD5);

            assertEquals(firstFilesResult.getChecksum(), newChecksum);
            assertEquals(firstFilesResult.getEncryptedChecksum(), newEncryptedChecksum);
        }

        // Assert that the decrypted file is equal to the originally created file,
        // and that the decrypted file contains the chosen string
        {
            assertEquals(Files.readAllLines(decryptedFilePath), Files.readAllLines(filePath));
            assertEquals(testString, Files.readString(decryptedFilePath));
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

    @DisplayName("Test of #GetFile() to return an empty byte[] when it can't find the file")
    @Test
    public void testGetFileEmptyResponse() {
        message = new MockupMessageObject(GET_FILE, COLLECTION_ID, FILE_ID, payload, new MockupResponse(payload));
        byte[] result = (byte[]) handler.handleReceivedMessage(message);
        assertEquals(0, result.length);
    }

    @DisplayName("Test of #GetFile() to return an empty byte[] when it gets no response from the encrypted pillar")
    @Test
    public void testGetFileNoResponseFromPillar() {
        message = new MockupMessageObject(GET_FILE, COLLECTION_ID, FILE_ID, payload);
        byte[] result = (byte[]) handler.handleReceivedMessage(message);
        assertEquals(0, result.length);
    }

    @Test
    public void testDeleteFile() throws FileExistsException {
        message = new MockupMessageObject(PUT_FILE, COLLECTION_ID, FILE_ID, payload);
        // Put file using MockupMessage with the payload
        boolean putFileHandled = (boolean) handler.handleReceivedMessage(message);
        assertTrue(putFileHandled);
        cleanupFiles();

        message = new MockupMessageObject(DELETE_FILE, COLLECTION_ID, FILE_ID);
        // Compute the checksum of the payload bytes
        writeBytesToFile(payload, filePath);
        String payloadChecksum = generateChecksum(new File(String.valueOf(filePath)), ChecksumType.MD5);

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

    @DisplayName("Test of #DeteleFile() to return null when it can't find the file")
    @Test
    public void testDeleteFileEmptyResponse() {
        message = new MockupMessageObject(DELETE_FILE, COLLECTION_ID, FILE_ID);
        assertNull(handler.handleReceivedMessage(message));
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

package dk.kb.bitrepository.mediator;

import dk.kb.bitrepository.database.DatabaseData;
import dk.kb.bitrepository.database.configs.ConfigurationHandler;
import dk.kb.bitrepository.mediator.communication.EncryptedPillarData;
import dk.kb.bitrepository.mediator.communication.MessageReceivedHandler;
import dk.kb.bitrepository.mediator.communication.MockupMessageObject;
import dk.kb.bitrepository.mediator.communication.MockupResponse;
import dk.kb.bitrepository.utils.crypto.AESCryptoStrategy;
import dk.kb.bitrepository.utils.crypto.CryptoStrategy;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
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
    private static MockupMessageObject message;
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

    @BeforeEach
    public void putFile() {
        message = new MockupMessageObject(PUT_FILE, COLLECTION_ID, FILE_ID, payload);
        boolean handled = (boolean) handler.handleReceivedMessage(message);
        assertTrue(handled);
    }

    @AfterEach
    public void cleanup() {
        delete(ENC_PARAMS_TABLE, true);
        delete(FILES_TABLE, true);
    }

    @Test
    @DisplayName("Test #PutFile()")
    public void testPutFile() {
        // Encrypt the payload
        CryptoStrategy AES = setupCryptoStrategy();
        byte[] encryptedPayload = AES.encrypt(payload);

        // Compute new checksums
        String newChecksum = generateChecksum(new ByteArrayInputStream(payload), checksumSpecTYPE);
        String newEncryptedChecksum = generateChecksum(new ByteArrayInputStream(encryptedPayload), checksumSpecTYPE);

        // Assert that checksums match
        FilesData result = (FilesData) select(COLLECTION_ID, FILE_ID, FILES_TABLE).get(0);
        assertEquals(result.getChecksum(), newChecksum);
        assertEquals(result.getEncryptedChecksum(), newEncryptedChecksum);

        // Assert that the decrypted bytes are equal to the original payload bytes
        assertEquals(testString, new String(AES.decrypt(encryptedPayload), StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("Test #GetFile()")
    public void testGetFile() {
        // Created encrypted payload from PutFile payload
        CryptoStrategy AES = setupCryptoStrategy();
        byte[] encryptedPayload = AES.encrypt(payload);

        // Create a mockup message object with a mockup response containing the encrypted payload.
        message = new MockupMessageObject(GET_FILE, COLLECTION_ID, FILE_ID, null, new MockupResponse(encryptedPayload));

        // Get file using the mockup request
        byte[] bytesReceived = (byte[]) handler.handleReceivedMessage(message);

        assertTrue(bytesReceived.length > 0);
        assertEquals(testString, new String(bytesReceived, Charset.defaultCharset()));
    }

    @Test
    @DisplayName("Test of #GetFile() to return an empty byte[] when it can't find the file")
    public void testGetFileEmptyResponse() {
        message = new MockupMessageObject(GET_FILE, "not_a_real_id", FILE_ID, payload, new MockupResponse(payload));
        byte[] result = (byte[]) handler.handleReceivedMessage(message);
        assertEquals(0, result.length);
    }

    @Test
    @DisplayName("Test of #GetFile() to return an empty byte[] when it gets no response from the encrypted pillar")
    public void testGetFileNoResponseFromPillar() {
        //Mockup Message with no Response, so it defaults to null
        message = new MockupMessageObject(GET_FILE, COLLECTION_ID, FILE_ID, payload, null);
        byte[] result = (byte[]) handler.handleReceivedMessage(message);
        assertEquals(0, result.length);
    }

    @Test
    @DisplayName("Test #DeleteFile()")
    public void testDeleteFile() {
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
        message = new MockupMessageObject(DELETE_FILE, COLLECTION_ID, "not_a_real_id");
        assertNull(handler.handleReceivedMessage(message));
    }

    @Test
    @DisplayName("Test of #ReplaceFile()")
    public void testReplaceFile() {
        FilesData res = (FilesData) select(COLLECTION_ID, FILE_ID, FILES_TABLE).get(0);
        String oldChecksum = res.getChecksum();
        String oldEncryptedChecksum = res.getEncryptedChecksum();
        OffsetDateTime oldReceivedTimestamp = res.getReceivedTimestamp();

        byte[] newPayload = "test_new_payload".getBytes(Charset.defaultCharset());
        byte[] encryptedPayload = setupCryptoStrategy().encrypt(payload);
        message = new MockupMessageObject(REPLACE_FILE, COLLECTION_ID, FILE_ID, newPayload, new MockupResponse(encryptedPayload));
        boolean handled = (boolean) handler.handleReceivedMessage(message);
        assertTrue(handled);

        String checksum = generateChecksum(new ByteArrayInputStream(newPayload), checksumSpecTYPE);
        byte[] encryptedNewPayload = setupCryptoStrategy().encrypt(newPayload);
        String encryptedChecksum = generateChecksum(new ByteArrayInputStream(encryptedNewPayload), checksumSpecTYPE);

        // Update 'select' result
        res = (FilesData) select(COLLECTION_ID, FILE_ID, FILES_TABLE).get(0);
        // Assert that the table was updated
        assertNotEquals(oldChecksum, res.getChecksum());
        assertNotEquals(oldEncryptedChecksum, res.getEncryptedChecksum());
        assertNotEquals(oldReceivedTimestamp, res.getReceivedTimestamp());
        // Assert that the table was updated with the correct data
        assertEquals(checksum, res.getChecksum());
        assertEquals(encryptedChecksum, res.getEncryptedChecksum());
    }

    @Test
    @DisplayName("Test #GetChecksums()")
    public void testGetChecksums() {
        // Cleanup the AfterEach block
        delete(COLLECTION_ID, FILE_ID, FILES_TABLE);
        delete(COLLECTION_ID, FILE_ID, ENC_PARAMS_TABLE);
        // Performing extra PUT_FILE operations
        List<byte[]> extraPayloads = putExtraFiles(3);

        // Create the MockupResponse from the encrypted pillar
        List<EncryptedPillarData> encryptedPillarData = generateEncryptedPillarDataForMockupResponse(extraPayloads);

        // Use the newly created Mockup Response data to perform GET_CHECKSUMS
        message = new MockupMessageObject(GET_CHECKSUMS, new MockupResponse(encryptedPillarData));
        List<?> matchingChecksumsList = (ArrayList<?>) handler.handleReceivedMessage(message);

        for (int i = 0; i < matchingChecksumsList.size(); i++) {
            EncryptedPillarData e = ((EncryptedPillarData) matchingChecksumsList.get(i));
            FilesData res = (FilesData) select(COLLECTION_ID + i, FILE_ID + i, FILES_TABLE).get(0);
            assertEquals(COLLECTION_ID + i, e.getCollectionID());
            assertEquals(FILE_ID + i, e.getFileID());
            assertEquals(res.getChecksum(), e.getChecksum());
        }
    }

    @Test
    @DisplayName("Test #GetChecksums() with one mismatching checksum")
    public void testGetChecksumsOneWrongChecksum() {
        // Cleanup the AfterEach block
        delete(COLLECTION_ID, FILE_ID, FILES_TABLE);
        delete(COLLECTION_ID, FILE_ID, ENC_PARAMS_TABLE);
        // Performing extra PUT_FILE operations
        List<byte[]> extraPayloads = putExtraFiles(4);

        // Change a payload to be incorrect to simulate bits flitting or other form of corruption
        extraPayloads.remove(3);
        extraPayloads.add("test-string-wrong".getBytes(Charset.defaultCharset()));

        // Create the MockupResponse from the encrypted pillar
        List<EncryptedPillarData> encryptedPillarData = generateEncryptedPillarDataForMockupResponse(extraPayloads);

        // Use the newly created Mockup Response data to perform GET_CHECKSUMS
        message = new MockupMessageObject(GET_CHECKSUMS, new MockupResponse(encryptedPillarData));
        List<?> matchingChecksumsList = (ArrayList<?>) handler.handleReceivedMessage(message);

        // Assert that the resulting checksums are 1 less than the payloads, since exactly 1 should be corrupt
        assertEquals(extraPayloads.size() - 1, matchingChecksumsList.size());

        // Assert that collection id, file id, and checksums of decrypted bytes match
        for (int i = 0; i < matchingChecksumsList.size(); i++) {
            EncryptedPillarData e = (EncryptedPillarData) matchingChecksumsList.get(i);
            FilesData res = (FilesData) select(COLLECTION_ID + i, FILE_ID + i, FILES_TABLE).get(0);

            assertEquals(COLLECTION_ID + i, e.getCollectionID());
            assertEquals(FILE_ID + i, e.getFileID());
            assertEquals(res.getChecksum(), e.getChecksum());
        }
    }

    /**
     * Used to for easy set up of the AES crypto strategy, with parameters from the 'enc_parameters' table.
     *
     * @return AESCryptStrategy with parameters used to encrypt a given file.
     */
    @NotNull
    private CryptoStrategy setupCryptoStrategy() {
        List<DatabaseData> result = select(COLLECTION_ID, FILE_ID, ENC_PARAMS_TABLE);
        EncryptedParametersData firstEncParamResult = (EncryptedParametersData) result.get(0);
        String salt = firstEncParamResult.getSalt();
        byte[] iv = firstEncParamResult.getIv();

        return new AESCryptoStrategy(encryptionPassword, salt, iv);
    }

    /**
     * Used to perform extra PutFile operations.
     *
     * @param howMany How many extra PutFile operations to perform.
     * @return Returns a list of the payloads that were used.
     */
    @NotNull
    private List<byte[]> putExtraFiles(int howMany) {
        List<byte[]> out = new ArrayList<>();
        for (int i = 0; i < howMany; i++) {
            byte[] extraPayload = ("test string " + i).getBytes(Charset.defaultCharset());
            message = new MockupMessageObject(PUT_FILE, COLLECTION_ID + i, FILE_ID + i, extraPayload);
            handler.handleReceivedMessage(message);
            out.add(extraPayload);
        }
        return out;
    }

    /**
     * Generates encryptedPillarData, used in the MockupResponse, for every payload that is given.
     *
     * @param payloads The payloads to be added to the encryptedPillarData, should be encrypted payloads.
     * @return The mockup encryptedPillarData.
     */
    @NotNull
    private List<EncryptedPillarData> generateEncryptedPillarDataForMockupResponse(List<byte[]> payloads) {
        List<EncryptedPillarData> encryptedPillarData = new ArrayList<>();

        // Insert data for every extra PutFile that has been run through the #putExtraFiles() method.
        for (int i = 0; i < payloads.size(); i++) {
            String collectionID = COLLECTION_ID + i;
            String fileID = FILE_ID + i;

            FilesData res = (FilesData) select(collectionID, fileID, FILES_TABLE).get(0);
            EncryptedParametersData cry = (EncryptedParametersData) select(collectionID, fileID, ENC_PARAMS_TABLE).get(0);

            encryptedPillarData.add(new EncryptedPillarData(
                    collectionID, fileID,
                    new AESCryptoStrategy(encryptionPassword, cry.getSalt(), cry.getIv()).encrypt(payloads.get(i)),
                    res.getEncryptedChecksum(),
                    res.getChecksumTimestamp()));
        }

        return encryptedPillarData;
    }
}

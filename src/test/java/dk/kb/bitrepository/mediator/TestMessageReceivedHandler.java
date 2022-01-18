package dk.kb.bitrepository.mediator;

import dk.kb.bitrepository.database.DatabaseData;
import dk.kb.bitrepository.mediator.communication.MockupMessageObject;
import dk.kb.bitrepository.utils.crypto.AESCryptoStrategy;
import dk.kb.bitrepository.utils.crypto.CryptoStrategy;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static dk.kb.bitrepository.database.DatabaseCalls.delete;
import static dk.kb.bitrepository.database.DatabaseCalls.select;
import static dk.kb.bitrepository.database.DatabaseConstants.*;
import static dk.kb.bitrepository.database.DatabaseData.*;
import static dk.kb.bitrepository.database.DatabaseData.EncryptedParametersData;
import static dk.kb.bitrepository.mediator.communication.MessageReceivedHandler.cleanupFiles;
import static dk.kb.bitrepository.mediator.communication.MessageReceivedHandler.putFile;
import static org.bitrepository.common.utils.ChecksumUtils.generateChecksum;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class TestMessageReceivedHandler {
    private MockupMessageObject message;
    private String password;
    private String testString;
    private String encryptedFilePath;
    private String filePath;

    @Before
    public void setup() {
        password = "testPassword";
        encryptedFilePath = "src/main/java/dk/kb/bitrepository/mediator/files/test_encrypted:"
                + COLLECTION_ID + ":" + FILE_ID;
        filePath = "src/main/java/dk/kb/bitrepository/mediator/files/test:"
                + COLLECTION_ID + ":" + FILE_ID;
        testString = "test string";
        byte[] payload = testString.getBytes();
        message = new MockupMessageObject("PUT_FILE", COLLECTION_ID, FILE_ID, payload);
    }

    @After
    public void cleanup() {
        delete(COLLECTION_ID, FILE_ID, ENC_PARAMS_TABLE);
        delete(COLLECTION_ID, FILE_ID, FILES_TABLE);
        cleanupFiles();
    }

    @Test
    public void testPutFile() throws IOException {
        putFile(message.getPayload(), message.getCollectionID(), message.getFileID());

        // Get the used encryption parameters from the 'enc_parameters' table
        List<DatabaseData> result = select(COLLECTION_ID, FILE_ID, ENC_PARAMS_TABLE);
        EncryptedParametersData firstEncParamResult = (EncryptedParametersData) result.get(0);

        String salt = firstEncParamResult.getSalt();
        byte[] iv = firstEncParamResult.getIv();
        CryptoStrategy AES = new AESCryptoStrategy(password, salt, iv);

        // Decrypt the file
        String decryptedFilePath = "src/main/java/dk/kb/bitrepository/mediator/files/test_decrypt";
        AES.decrypt(Paths.get(encryptedFilePath), Paths.get(decryptedFilePath));

        // Assert that Checksums match
        result = select(COLLECTION_ID, FILE_ID, FILES_TABLE);
        FilesData firstFilesResult = (FilesData) result.get(0);
        String newChecksum = generateChecksum(new File(decryptedFilePath), ChecksumType.MD5);
        String newEncryptedChecksum = generateChecksum(new File(encryptedFilePath), ChecksumType.MD5);

        assertEquals(newChecksum, firstFilesResult.getChecksum());
        assertEquals(newEncryptedChecksum, firstFilesResult.getEncryptedChecksum());

        // Assert that the decrypted file is equal to the originally created file
        assertEquals(Files.readAllLines(Paths.get(decryptedFilePath)),
                Files.readAllLines(Paths.get(filePath)));
        // Assert that the decrypted file contains the chosen string
        assertThat(Files.readString(Paths.get(decryptedFilePath)), is(testString));
    }
}

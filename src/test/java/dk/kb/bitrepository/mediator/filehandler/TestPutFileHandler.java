package dk.kb.bitrepository.mediator.filehandler;

import dk.kb.bitrepository.mediator.TestingSetup;
import dk.kb.bitrepository.mediator.crypto.AESCryptoStrategy;
import dk.kb.bitrepository.mediator.database.DatabaseDAO;
import dk.kb.bitrepository.mediator.filehandler.exception.MismatchingChecksumsException;
import dk.kb.bitrepository.mediator.utils.configurations.CryptoConfigurations;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.OffsetDateTime;

import static dk.kb.bitrepository.mediator.TestingUtilities.cleanupFiles;
import static dk.kb.bitrepository.mediator.database.DatabaseConstants.*;
import static dk.kb.bitrepository.mediator.utils.configurations.ConfigConstants.ENCRYPTED_FILES_PATH;
import static dk.kb.bitrepository.mediator.utils.configurations.ConfigConstants.UNENCRYPTED_FILES_PATH;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test #PutFileHandler")
public class TestPutFileHandler {
    private static byte[] fileBytes;
    private static ChecksumDataForFileTYPE checksumDataForFileTYPE;
    private static CryptoConfigurations cryptoConfigurations;
    private static DatabaseDAO dao;

    @BeforeAll
    static void setup() throws IOException {
        TestingSetup setup = new TestingSetup();
        fileBytes = setup.getFileBytes();
        checksumDataForFileTYPE = setup.getChecksumDataForFileTYPE();
        cryptoConfigurations = setup.getCryptoConfigurations();
        dao = setup.getDao();
    }

    @AfterEach
    public void cleanup() {
        cleanupFiles(UNENCRYPTED_FILES_PATH);
        cleanupFiles(ENCRYPTED_FILES_PATH);
        dao.delete(COLLECTION_ID, FILE_ID, ENC_PARAMS_TABLE);
        dao.delete(COLLECTION_ID, FILE_ID, FILES_TABLE);
    }

    @Test
    @DisplayName("Test PutFile method")
    public void testPutFile() throws MismatchingChecksumsException {
        OffsetDateTime receivedTimestamp = OffsetDateTime.now(Clock.systemUTC());
        PutFileHandler handler = new PutFileHandler(COLLECTION_ID, FILE_ID, fileBytes, checksumDataForFileTYPE,
                receivedTimestamp, dao, new AESCryptoStrategy(cryptoConfigurations.getPassword()));

        handler.performPutFile();

        assertTrue(Files.exists(Path.of(ENCRYPTED_FILES_PATH + "/" + COLLECTION_ID + "/" + FILE_ID)));
        assertTrue(Files.exists(Path.of(UNENCRYPTED_FILES_PATH + "/" + COLLECTION_ID + "/" + FILE_ID)));
        assertTrue(dao.hasFile(COLLECTION_ID, FILE_ID));
        assertNotNull(dao.select(COLLECTION_ID, FILE_ID, ENC_PARAMS_TABLE));
    }

    @Test
    @DisplayName("Test PutFile method using already existing files")
    public void testPutFileUsingExistingFile() throws MismatchingChecksumsException {
        OffsetDateTime receivedTimestamp = OffsetDateTime.now(Clock.systemUTC());
        PutFileHandler handler = new PutFileHandler(COLLECTION_ID, FILE_ID, fileBytes, checksumDataForFileTYPE,
                receivedTimestamp, dao, new AESCryptoStrategy(cryptoConfigurations.getPassword()));
        handler.performPutFile();
        assertTrue(Files.exists(Path.of(ENCRYPTED_FILES_PATH + "/" + COLLECTION_ID + "/" + FILE_ID)));
        assertTrue(Files.exists(Path.of(UNENCRYPTED_FILES_PATH + "/" + COLLECTION_ID + "/" + FILE_ID)));

        dao.delete(COLLECTION_ID, FILE_ID, ENC_PARAMS_TABLE);
        dao.delete(COLLECTION_ID, FILE_ID, FILES_TABLE);
        cleanupFiles(ENCRYPTED_FILES_PATH);

        // Tests using the unencrypted existing file
        handler.performPutFile();
        assertTrue(dao.hasFile(COLLECTION_ID, FILE_ID));
        assertNotNull(dao.select(COLLECTION_ID, FILE_ID, ENC_PARAMS_TABLE));

        dao.delete(COLLECTION_ID, FILE_ID, ENC_PARAMS_TABLE);
        dao.delete(COLLECTION_ID, FILE_ID, FILES_TABLE);
        cleanupFiles(UNENCRYPTED_FILES_PATH);

        // Test using the encrypted existing file
        handler.performPutFile();
        assertTrue(dao.hasFile(COLLECTION_ID, FILE_ID));
        assertNotNull(dao.select(COLLECTION_ID, FILE_ID, ENC_PARAMS_TABLE));
    }

    @Test
    @DisplayName("Test PutFile throws MismatchingChecksumsException when checksums does not match")
    public void testPutFileChecksumsDoesntMatch() {
        OffsetDateTime receivedTimestamp = OffsetDateTime.now(Clock.systemUTC());
        ChecksumDataForFileTYPE checksumDataWithWrongChecksum = checksumDataForFileTYPE;
        checksumDataWithWrongChecksum.setChecksumValue(new byte[12]);
        PutFileHandler handler = new PutFileHandler(COLLECTION_ID, FILE_ID, fileBytes, checksumDataWithWrongChecksum,
                receivedTimestamp, dao, new AESCryptoStrategy(cryptoConfigurations.getPassword()));

        Assertions.assertThrows(MismatchingChecksumsException.class, handler::performPutFile);
        assertFalse(Files.exists(Path.of(ENCRYPTED_FILES_PATH + "/" + COLLECTION_ID + "/" + FILE_ID)));
        assertFalse(Files.exists(Path.of(UNENCRYPTED_FILES_PATH + "/" + COLLECTION_ID + "/" + FILE_ID)));
        assertFalse(dao.hasFile(COLLECTION_ID, FILE_ID));
    }
}

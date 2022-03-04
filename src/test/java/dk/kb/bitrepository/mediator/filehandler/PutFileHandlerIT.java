package dk.kb.bitrepository.mediator.filehandler;

import dk.kb.bitrepository.mediator.IntegrationFileHandlerTest;
import dk.kb.bitrepository.mediator.filehandler.exception.MismatchingChecksumsException;
import org.junit.jupiter.api.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Collections;

import static dk.kb.bitrepository.mediator.TestingUtilities.cleanupFiles;
import static dk.kb.bitrepository.mediator.database.DatabaseConstants.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test #PutFileHandler")
public class PutFileHandlerIT extends IntegrationFileHandlerTest {
    @BeforeAll
    protected static void setup() {
        startRealMessageBus();
        startEmbeddedPillar();
    }

    @BeforeEach
    protected void cleanupBefore() {
        cleanUpDatabase();
        cleanUpAfterEach();
    }

    @AfterAll
    protected static void cleanup() {
        stopEmbeddedPillar();
    }

    @Test
    @DisplayName("Test PutFile method")
    public void testPutFile() throws MismatchingChecksumsException {
        OffsetDateTime receivedTimestamp = OffsetDateTime.now(Clock.systemUTC());
        JobContext context = new JobContext(COLLECTION_ID, FILE_ID, fileBytes, null, checksumDataForFileTYPE, settings, fileURL,
                Collections.singleton(encryptedPillarID), crypto, fileExchange);
        PutFileHandler handler = new PutFileHandler(context, receivedTimestamp);

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
        JobContext context = new JobContext(COLLECTION_ID, FILE_ID, fileBytes, null, checksumDataForFileTYPE, settings, fileURL,
                Collections.singleton(encryptedPillarID), crypto, fileExchange);
        PutFileHandler handler = new PutFileHandler(context, receivedTimestamp);
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
        JobContext context = new JobContext(COLLECTION_ID, FILE_ID, fileBytes, null, checksumDataWithWrongChecksum, settings, fileURL,
                Collections.singleton(encryptedPillarID), crypto, fileExchange);
        PutFileHandler handler = new PutFileHandler(context, receivedTimestamp);

        Assertions.assertThrows(MismatchingChecksumsException.class, handler::performPutFile);
        assertFalse(Files.exists(Path.of(ENCRYPTED_FILES_PATH + "/" + COLLECTION_ID + "/" + FILE_ID)));
        assertFalse(Files.exists(Path.of(UNENCRYPTED_FILES_PATH + "/" + COLLECTION_ID + "/" + FILE_ID)));
        assertFalse(dao.hasFile(COLLECTION_ID, FILE_ID));
    }

    @Test
    @DisplayName("Test PutFile method using already existing unencrypted file with MismatchingChecksum")
    public void testPutFileUsingExistingUnencryptedFileMismatchingChecksums() throws MismatchingChecksumsException {
        OffsetDateTime receivedTimestamp = OffsetDateTime.now(Clock.systemUTC());
        JobContext context = new JobContext(COLLECTION_ID, FILE_ID, fileBytes, null, checksumDataForFileTYPE, settings, fileURL,
                Collections.singleton(encryptedPillarID), crypto, fileExchange);
        PutFileHandler handler = new PutFileHandler(context, receivedTimestamp);
        handler.performPutFile();
        assertTrue(Files.exists(Path.of(ENCRYPTED_FILES_PATH + "/" + COLLECTION_ID + "/" + FILE_ID)));
        assertTrue(Files.exists(Path.of(UNENCRYPTED_FILES_PATH + "/" + COLLECTION_ID + "/" + FILE_ID)));

        dao.delete(COLLECTION_ID, FILE_ID, ENC_PARAMS_TABLE);
        dao.delete(COLLECTION_ID, FILE_ID, FILES_TABLE);
        cleanupFiles(UNENCRYPTED_FILES_PATH);

        // Tests using the unencrypted existing file
        context = new JobContext(COLLECTION_ID, FILE_ID, fileBytes, null, checksumDataWithWrongChecksum, settings, fileURL,
                Collections.singleton(encryptedPillarID), crypto, fileExchange);
        handler = new PutFileHandler(context, receivedTimestamp);
        Assertions.assertThrows(MismatchingChecksumsException.class, handler::performPutFile);
        assertFalse(Files.exists(Path.of(ENCRYPTED_FILES_PATH + "/" + COLLECTION_ID + "/" + FILE_ID)));
        assertFalse(Files.exists(Path.of(UNENCRYPTED_FILES_PATH + "/" + COLLECTION_ID + "/" + FILE_ID)));
        assertFalse(dao.hasFile(COLLECTION_ID, FILE_ID));
    }

    @Test
    @DisplayName("Test PutFile method using already existing encrypted file with MismatchingChecksum")
    public void testPutFileUsingExistingEncryptedFileMismatchingChecksums() throws MismatchingChecksumsException {
        OffsetDateTime receivedTimestamp = OffsetDateTime.now(Clock.systemUTC());
        JobContext context = new JobContext(COLLECTION_ID, FILE_ID, fileBytes, null, checksumDataForFileTYPE, settings, fileURL,
                Collections.singleton(encryptedPillarID), crypto, fileExchange);
        PutFileHandler handler = new PutFileHandler(context, receivedTimestamp);
        handler.performPutFile();
        assertTrue(Files.exists(Path.of(ENCRYPTED_FILES_PATH + "/" + COLLECTION_ID + "/" + FILE_ID)));
        assertTrue(Files.exists(Path.of(UNENCRYPTED_FILES_PATH + "/" + COLLECTION_ID + "/" + FILE_ID)));

        dao.delete(COLLECTION_ID, FILE_ID, ENC_PARAMS_TABLE);
        dao.delete(COLLECTION_ID, FILE_ID, FILES_TABLE);
        cleanupFiles(ENCRYPTED_FILES_PATH);

        // Tests using the encrypted existing file
        context = new JobContext(COLLECTION_ID, FILE_ID, fileBytes, null, checksumDataWithWrongChecksum, settings, fileURL,
                Collections.singleton(encryptedPillarID), crypto, fileExchange);
        handler = new PutFileHandler(context, receivedTimestamp);
        Assertions.assertThrows(MismatchingChecksumsException.class, handler::performPutFile);
        assertFalse(Files.exists(Path.of(ENCRYPTED_FILES_PATH + "/" + COLLECTION_ID + "/" + FILE_ID)));
        assertFalse(Files.exists(Path.of(UNENCRYPTED_FILES_PATH + "/" + COLLECTION_ID + "/" + FILE_ID)));
        assertFalse(dao.hasFile(COLLECTION_ID, FILE_ID));
    }
}

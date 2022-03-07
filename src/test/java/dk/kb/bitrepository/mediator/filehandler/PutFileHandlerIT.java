package dk.kb.bitrepository.mediator.filehandler;

import dk.kb.bitrepository.mediator.IntegrationFileHandlerTest;
import dk.kb.bitrepository.mediator.crypto.AESCryptoStrategy;
import dk.kb.bitrepository.mediator.crypto.CryptoStrategy;
import dk.kb.bitrepository.mediator.filehandler.context.PutFileContext;
import dk.kb.bitrepository.mediator.filehandler.exception.MismatchingChecksumsException;
import org.bitrepository.common.utils.Base16Utils;
import org.junit.jupiter.api.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Collections;

import static dk.kb.bitrepository.mediator.TestingUtilities.cleanupFiles;
import static dk.kb.bitrepository.mediator.database.DatabaseConstants.*;
import static dk.kb.bitrepository.mediator.database.DatabaseData.EncryptedParametersData;
import static dk.kb.bitrepository.mediator.filehandler.FileUtils.compareChecksums;
import static dk.kb.bitrepository.mediator.filehandler.FileUtils.readBytesFromFile;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test #PutFileHandler")
public class PutFileHandlerIT extends IntegrationFileHandlerTest {
    private static final String pillarFilesDir = "target/test/fileArchive/collection_id/fileDir/";
    private static final Path pillarFilePath = Path.of("target/test/fileArchive/collection_id/fileDir/file_id");

    @BeforeAll
    protected static void setup() {
        startRealMessageBus();
        startEmbeddedPillar();
    }

    @AfterAll
    protected static void cleanup() {
        stopEmbeddedPillar();
    }

    @BeforeEach
    protected void cleanupBefore() {
        cleanUpDatabase();
        cleanUpAfterEach();
    }

    @AfterEach
    protected void afterEach() {
        resetPillarData(pillarFilePath, pillarFilesDir);
    }

    @Test
    @DisplayName("Test PutFile method")
    public void testPutFile() throws MismatchingChecksumsException {
        PutFileContext context = getJobContext(PutFileContext.class);
        PutFileHandler handler = new PutFileHandler(context);

        handler.performPutFile();

        assertTrue(Files.exists(Path.of(ENCRYPTED_FILES_PATH + "/" + COLLECTION_ID + "/" + FILE_ID)));
        assertTrue(Files.exists(Path.of(UNENCRYPTED_FILES_PATH + "/" + COLLECTION_ID + "/" + FILE_ID)));
        assertTrue(dao.hasFile(COLLECTION_ID, FILE_ID));
        assertNotNull(dao.getEncParams(COLLECTION_ID, FILE_ID));

        assertTrue(Files.exists(pillarFilePath));
        assertTrue(compareChecksums(readBytesFromFile(pillarFilePath), checksumDataForFileTYPE.getChecksumSpec(),
                Base16Utils.decodeBase16(checksumDataForFileTYPE.getChecksumValue())));
        assertNotEquals(fileContent, new String(readBytesFromFile(pillarFilePath), StandardCharsets.UTF_8));
        EncryptedParametersData params = dao.getEncParams(COLLECTION_ID, FILE_ID);
        CryptoStrategy aes = new AESCryptoStrategy(encryptionPassword, params.getSalt(), params.getIv());
        assertEquals(fileContent, new String(aes.decrypt(readBytesFromFile(pillarFilePath)), StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("Test PutFile method using already existing files")
    public void testPutFileUsingExistingFile() throws MismatchingChecksumsException {
        PutFileContext context = getJobContext(PutFileContext.class);
        PutFileHandler handler = new PutFileHandler(context);
        handler.performPutFile();
        assertTrue(Files.exists(Path.of(ENCRYPTED_FILES_PATH + "/" + COLLECTION_ID + "/" + FILE_ID)));
        assertTrue(Files.exists(Path.of(UNENCRYPTED_FILES_PATH + "/" + COLLECTION_ID + "/" + FILE_ID)));
        assertTrue(Files.exists(pillarFilePath));

        // Have to remove this to continue testing using unencrypted local file
        cleanupFiles(ENCRYPTED_FILES_PATH);
        resetPillarData(pillarFilePath, pillarFilesDir);

        // Tests using the unencrypted existing file
        handler.performPutFile();
        assertTrue(dao.hasFile(COLLECTION_ID, FILE_ID));
        assertNotNull(dao.getEncParams(COLLECTION_ID, FILE_ID));

        assertTrue(Files.exists(pillarFilePath));
        assertTrue(compareChecksums(readBytesFromFile(pillarFilePath), checksumDataForFileTYPE.getChecksumSpec(),
                Base16Utils.decodeBase16(checksumDataForFileTYPE.getChecksumValue())));
        assertNotEquals(fileContent, new String(readBytesFromFile(pillarFilePath), StandardCharsets.UTF_8));
        EncryptedParametersData params = dao.getEncParams(COLLECTION_ID, FILE_ID);
        CryptoStrategy aes = new AESCryptoStrategy(encryptionPassword, params.getSalt(), params.getIv());
        assertEquals(fileContent, new String(aes.decrypt(readBytesFromFile(pillarFilePath)), StandardCharsets.UTF_8));

        // Have to remove this to continue testing using encrypted local file
        cleanupFiles(UNENCRYPTED_FILES_PATH);
        resetPillarData(pillarFilePath, pillarFilesDir);

        // Test using the encrypted existing file
        handler.performPutFile();
        assertTrue(dao.hasFile(COLLECTION_ID, FILE_ID));
        assertNotNull(dao.getEncParams(COLLECTION_ID, FILE_ID));

        // Assert that the file could be fetched from FileExchange which has been provided by the pillar
        assertTrue(Files.exists(pillarFilePath));
        assertTrue(compareChecksums(readBytesFromFile(pillarFilePath), checksumDataForFileTYPE.getChecksumSpec(),
                Base16Utils.decodeBase16(checksumDataForFileTYPE.getChecksumValue())));
        assertNotEquals(fileContent, new String(readBytesFromFile(pillarFilePath), StandardCharsets.UTF_8));
        assertEquals(fileContent, new String(aes.decrypt(readBytesFromFile(pillarFilePath)), StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("Test PutFile throws MismatchingChecksumsException when checksums does not match")
    public void testPutFileChecksumsDoesntMatch() {
        OffsetDateTime receivedTimestamp = OffsetDateTime.now(Clock.systemUTC());
        PutFileContext context = new PutFileContext(COLLECTION_ID, FILE_ID, fileBytes, receivedTimestamp,
                checksumDataWithWrongChecksum, settings, fileURL, Collections.singleton(encryptedPillarID), crypto);
        PutFileHandler handler = new PutFileHandler(context);

        Assertions.assertThrows(MismatchingChecksumsException.class, handler::performPutFile);
        assertFalse(Files.exists(Path.of(ENCRYPTED_FILES_PATH + "/" + COLLECTION_ID + "/" + FILE_ID)));
        assertFalse(Files.exists(Path.of(UNENCRYPTED_FILES_PATH + "/" + COLLECTION_ID + "/" + FILE_ID)));
        assertFalse(dao.hasFile(COLLECTION_ID, FILE_ID));
    }

    @Test
    @DisplayName("Test PutFile method using already existing unencrypted file with MismatchingChecksum")
    public void testPutFileUsingExistingUnencryptedFileMismatchingChecksums() throws MismatchingChecksumsException {
        OffsetDateTime receivedTimestamp = OffsetDateTime.now(Clock.systemUTC());
        PutFileContext context = new PutFileContext(COLLECTION_ID, FILE_ID, fileBytes, receivedTimestamp, checksumDataForFileTYPE,
                settings, fileURL, Collections.singleton(encryptedPillarID), crypto);
        PutFileHandler handler = new PutFileHandler(context);
        handler.performPutFile();
        assertTrue(Files.exists(Path.of(ENCRYPTED_FILES_PATH + "/" + COLLECTION_ID + "/" + FILE_ID)));
        assertTrue(Files.exists(Path.of(UNENCRYPTED_FILES_PATH + "/" + COLLECTION_ID + "/" + FILE_ID)));

        dao.delete(COLLECTION_ID, FILE_ID, FILES_TABLE);
        cleanupFiles(UNENCRYPTED_FILES_PATH);

        // Tests using the unencrypted existing file
        context = new PutFileContext(COLLECTION_ID, FILE_ID, fileBytes, receivedTimestamp, checksumDataWithWrongChecksum, settings,
                fileURL, Collections.singleton(encryptedPillarID), crypto);
        handler = new PutFileHandler(context);
        Assertions.assertThrows(MismatchingChecksumsException.class, handler::performPutFile);
        assertFalse(Files.exists(Path.of(ENCRYPTED_FILES_PATH + "/" + COLLECTION_ID + "/" + FILE_ID)));
        assertFalse(Files.exists(Path.of(UNENCRYPTED_FILES_PATH + "/" + COLLECTION_ID + "/" + FILE_ID)));
        assertFalse(dao.hasFile(COLLECTION_ID, FILE_ID));
    }

    @Test
    @DisplayName("Test PutFile method using already existing encrypted file with MismatchingChecksum")
    public void testPutFileUsingExistingEncryptedFileMismatchingChecksums() throws MismatchingChecksumsException {
        OffsetDateTime receivedTimestamp = OffsetDateTime.now(Clock.systemUTC());
        PutFileContext context = new PutFileContext(COLLECTION_ID, FILE_ID, fileBytes, receivedTimestamp, checksumDataForFileTYPE,
                settings, fileURL, Collections.singleton(encryptedPillarID), crypto);
        PutFileHandler handler = new PutFileHandler(context);
        handler.performPutFile();
        assertTrue(Files.exists(Path.of(ENCRYPTED_FILES_PATH + "/" + COLLECTION_ID + "/" + FILE_ID)));
        assertTrue(Files.exists(Path.of(UNENCRYPTED_FILES_PATH + "/" + COLLECTION_ID + "/" + FILE_ID)));

        dao.delete(COLLECTION_ID, FILE_ID, FILES_TABLE);
        cleanupFiles(ENCRYPTED_FILES_PATH);

        // Tests using the encrypted existing file
        context = new PutFileContext(COLLECTION_ID, FILE_ID, fileBytes, receivedTimestamp, checksumDataWithWrongChecksum, settings,
                fileURL, Collections.singleton(encryptedPillarID), crypto);
        handler = new PutFileHandler(context);
        Assertions.assertThrows(MismatchingChecksumsException.class, handler::performPutFile);
        assertFalse(Files.exists(Path.of(ENCRYPTED_FILES_PATH + "/" + COLLECTION_ID + "/" + FILE_ID)));
        assertFalse(Files.exists(Path.of(UNENCRYPTED_FILES_PATH + "/" + COLLECTION_ID + "/" + FILE_ID)));
        assertFalse(dao.hasFile(COLLECTION_ID, FILE_ID));
    }
}

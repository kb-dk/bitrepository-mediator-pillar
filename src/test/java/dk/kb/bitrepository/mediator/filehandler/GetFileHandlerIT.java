package dk.kb.bitrepository.mediator.filehandler;

import dk.kb.bitrepository.mediator.IntegrationFileHandlerTest;
import dk.kb.bitrepository.mediator.crypto.AESCryptoStrategy;
import dk.kb.bitrepository.mediator.crypto.CryptoStrategy;
import dk.kb.bitrepository.mediator.database.DatabaseData;
import dk.kb.bitrepository.mediator.filehandler.exception.MismatchingChecksumsException;
import org.bitrepository.common.utils.Base16Utils;
import org.junit.jupiter.api.*;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Collections;

import static dk.kb.bitrepository.mediator.TestingUtilities.cleanupFiles;
import static dk.kb.bitrepository.mediator.database.DatabaseConstants.*;
import static dk.kb.bitrepository.mediator.filehandler.FileUtils.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test #GetFileHandler")
public class GetFileHandlerIT extends IntegrationFileHandlerTest {
    private static final String pillarFilesDir = "target/test/fileArchive/collection_id/fileDir/";
    private static final Path pillarFilePath = Path.of("target/test/fileArchive/collection_id/fileDir/file_id");

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

    @AfterEach
    protected void afterEach() {
        resetPillarData(pillarFilePath, pillarFilesDir);
    }

    @AfterAll
    protected static void cleanup() {
        stopEmbeddedPillar();
    }

    @Test
    @DisplayName("Test #GetFileHandler using existing file")
    public void testGetFileUsingExistingFile() throws MismatchingChecksumsException {
        OffsetDateTime receivedTimestamp = OffsetDateTime.now(Clock.systemUTC());

        JobContext context = new JobContext(COLLECTION_ID, FILE_ID, fileBytes, null, checksumDataForFileTYPE, settings, fileURL,
                Collections.singleton(encryptedPillarID), crypto, fileExchange);
        PutFileHandler putFileHandler = new PutFileHandler(context, receivedTimestamp);

        putFileHandler.performPutFile();

        //Test with both files (will use unencrypted file)
        GetFileHandler getFileHandler = new GetFileHandler(context);
        assertDoesNotThrow(getFileHandler::performGetFile);

        //Test with only encrypted file
        cleanupFiles(UNENCRYPTED_FILES_PATH);
        assertDoesNotThrow(getFileHandler::performGetFile);
    }

    @Test
    @DisplayName("Test #GetFileHandler using file on pillar")
    public void testGetFileHandlerUsingPillarFile() throws MismatchingChecksumsException, MalformedURLException {
        OffsetDateTime receivedTimestamp = OffsetDateTime.now(Clock.systemUTC());
        JobContext putFileContext = new JobContext(COLLECTION_ID, FILE_ID, fileBytes, null, checksumDataForFileTYPE, settings, fileURL,
                Collections.singleton(encryptedPillarID), crypto, fileExchange);
        PutFileHandler putFileHandler = new PutFileHandler(putFileContext, receivedTimestamp);
        putFileHandler.performPutFile();

        assertEquals(new URL("file:" + new File(BASE_FILE_EXCHANGE_DIR).getAbsolutePath() + "/" + FILE_ID), fileURL);

        //Remove the local files created by PutFile
        cleanupFiles(ENCRYPTED_FILES_PATH);
        cleanupFiles(UNENCRYPTED_FILES_PATH);

        JobContext getFileContext = new JobContext(COLLECTION_ID, FILE_ID, null, null, checksumDataForFileTYPE, settings, fileURL,
                Collections.singleton(encryptedPillarID), crypto, fileExchange);
        GetFileHandler getFileHandler = new GetFileHandler(getFileContext);
        getFileHandler.performGetFile();

        Path encryptedPath = createFilePath(ENCRYPTED_FILES_PATH, COLLECTION_ID, FILE_ID);
        assertTrue(Files.exists(encryptedPath));
        DatabaseData.EncryptedParametersData params = (DatabaseData.EncryptedParametersData) dao.select(COLLECTION_ID, FILE_ID,
                ENC_PARAMS_TABLE);
        CryptoStrategy aes = new AESCryptoStrategy(encryptionPassword, params.getSalt(), params.getIv());
        assertEquals(fileContent, new String(aes.decrypt(readBytesFromFile(encryptedPath)),
                StandardCharsets.UTF_8));
        assertTrue(compareChecksums(readBytesFromFile(encryptedPath), checksumDataForFileTYPE.getChecksumSpec(),
                Base16Utils.decodeBase16(checksumDataForFileTYPE.getChecksumValue())));
    }
}
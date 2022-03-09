package dk.kb.bitrepository.mediator.filehandler;

import dk.kb.bitrepository.mediator.IntegrationFileHandlerTest;
import dk.kb.bitrepository.mediator.crypto.AESCryptoStrategy;
import dk.kb.bitrepository.mediator.crypto.CryptoStrategy;
import dk.kb.bitrepository.mediator.filehandler.context.GetFileContext;
import dk.kb.bitrepository.mediator.filehandler.context.PutFileContext;
import dk.kb.bitrepository.mediator.filehandler.exception.MismatchingChecksumsException;
import org.bitrepository.common.utils.Base16Utils;
import org.junit.jupiter.api.*;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import static dk.kb.bitrepository.mediator.TestingUtilities.cleanupFiles;
import static dk.kb.bitrepository.mediator.database.DatabaseConstants.*;
import static dk.kb.bitrepository.mediator.database.DatabaseData.*;
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
    @DisplayName("Test #GetFileHandler using existing file")
    public void testGetFileUsingExistingFile() throws MismatchingChecksumsException {
        PutFileContext context = new PutFileContext(COLLECTION_ID, FILE_ID, fileBytes, checksumDataForFileTYPE,
                settings, fileURL, Collections.singleton(encryptedPillarID), crypto);
        PutFileHandler putFileHandler = new PutFileHandler(context);

        putFileHandler.performOperation();

        //Test with both files (will use unencrypted file)
        GetFileContext getFileContext = getJobContext(GetFileContext.class);
        GetFileHandler getFileHandler = new GetFileHandler(getFileContext);
        assertDoesNotThrow(getFileHandler::performOperation);

        //Test with only encrypted file
        cleanupFiles(UNENCRYPTED_FILES_PATH);
        assertDoesNotThrow(getFileHandler::performOperation);
    }

    @Test
    @DisplayName("Test #GetFileHandler using file on pillar")
    public void testGetFileHandlerUsingPillarFile() throws MismatchingChecksumsException, MalformedURLException {
        PutFileContext putFileContext = getJobContext(PutFileContext.class);
        PutFileHandler putFileHandler = new PutFileHandler(putFileContext);
        putFileHandler.performOperation();

        assertEquals(new URL("file:" + new File(BASE_FILE_EXCHANGE_DIR).getAbsolutePath() + "/" + FILE_ID), fileURL);

        //Remove the local files created by PutFile
        cleanupFiles(ENCRYPTED_FILES_PATH);
        cleanupFiles(UNENCRYPTED_FILES_PATH);

        GetFileContext getFileContext = getJobContext(GetFileContext.class);
        GetFileHandler getFileHandler = new GetFileHandler(getFileContext);
        getFileHandler.performOperation();

        // Assert that the file could be created locally using the file given to FileExchange by the EmbeddedPillar
        Path encryptedPath = createFilePath(ENCRYPTED_FILES_PATH, COLLECTION_ID, FILE_ID);
        assertTrue(Files.exists(encryptedPath));
        EncryptedParametersData params = dao.getEncParams(COLLECTION_ID, FILE_ID);
        CryptoStrategy aes = new AESCryptoStrategy(encryptionPassword, params.getSalt(), params.getIv());
        assertEquals(fileContent, new String(aes.decrypt(readBytesFromFile(encryptedPath)), StandardCharsets.UTF_8));
        assertTrue(compareChecksums(readBytesFromFile(encryptedPath), checksumDataForFileTYPE.getChecksumSpec(),
                Base16Utils.decodeBase16(checksumDataForFileTYPE.getChecksumValue())));
    }
}
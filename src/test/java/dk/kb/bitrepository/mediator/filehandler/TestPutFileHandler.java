package dk.kb.bitrepository.mediator.filehandler;

import dk.kb.bitrepository.mediator.TestingSetup;
import dk.kb.bitrepository.mediator.database.DatabaseDAO;
import dk.kb.bitrepository.mediator.utils.configurations.CryptoConfigurations;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.OffsetDateTime;

import static dk.kb.bitrepository.mediator.TestingUtilities.cleanupFiles;
import static dk.kb.bitrepository.mediator.database.DatabaseConstants.*;
import static dk.kb.bitrepository.mediator.utils.configurations.ConfigConstants.ENCRYPTED_FILES_PATH;
import static dk.kb.bitrepository.mediator.utils.configurations.ConfigConstants.UNENCRYPTED_FILES_PATH;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    public void testPutFile() {
        OffsetDateTime receivedTimestamp = OffsetDateTime.now(Clock.systemUTC());
        PutFileHandler handler = new PutFileHandler(COLLECTION_ID, FILE_ID, fileBytes, checksumDataForFileTYPE,
                receivedTimestamp, dao, cryptoConfigurations.getPassword());
        handler.performPutFile();

        assertTrue(Files.exists(Path.of(ENCRYPTED_FILES_PATH + "/" + COLLECTION_ID + "/" + FILE_ID)));
        assertTrue(Files.exists(Path.of(UNENCRYPTED_FILES_PATH + "/" + COLLECTION_ID + "/" + FILE_ID)));
        assertTrue(dao.hasFile(COLLECTION_ID, FILE_ID));
        assertNotNull(dao.select(COLLECTION_ID, FILE_ID, ENC_PARAMS_TABLE));
    }

    @Test
    @DisplayName("Test PutFile method using already existing files")
    public void testPutFileUsingExistingFile() {
        OffsetDateTime receivedTimestamp = OffsetDateTime.now(Clock.systemUTC());
        PutFileHandler handler = new PutFileHandler(COLLECTION_ID, FILE_ID, fileBytes, checksumDataForFileTYPE,
                receivedTimestamp, dao, cryptoConfigurations.getPassword());
        handler.performPutFile();
        dao.delete(COLLECTION_ID, FILE_ID, ENC_PARAMS_TABLE);
        dao.delete(COLLECTION_ID, FILE_ID, FILES_TABLE);

        handler.performPutFile();
        assertTrue(Files.exists(Path.of(ENCRYPTED_FILES_PATH + "/" + COLLECTION_ID + "/" + FILE_ID)));
        assertTrue(Files.exists(Path.of(UNENCRYPTED_FILES_PATH + "/" + COLLECTION_ID + "/" + FILE_ID)));
        assertTrue(dao.hasFile(COLLECTION_ID, FILE_ID));
        assertNotNull(dao.select(COLLECTION_ID, FILE_ID, ENC_PARAMS_TABLE));
    }

    @Test
    @DisplayName("Test PutFile doesn't work when checksums does not match")
    public void testPutFileChecksumsDoesntMatch() {
        //TODO: Implement
    }
}

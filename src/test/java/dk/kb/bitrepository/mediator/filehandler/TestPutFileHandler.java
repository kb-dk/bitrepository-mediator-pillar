package dk.kb.bitrepository.mediator.filehandler;

import dk.kb.bitrepository.mediator.MediatorComponentFactory;
import dk.kb.bitrepository.mediator.database.DatabaseDAO;
import dk.kb.bitrepository.mediator.utils.configurations.Configurations;
import dk.kb.bitrepository.mediator.utils.configurations.CryptoConfigurations;
import dk.kb.bitrepository.mediator.utils.configurations.DatabaseConfigurations;
import org.apache.commons.io.FileExistsException;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.junit.jupiter.api.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import static dk.kb.bitrepository.mediator.database.DatabaseConstants.*;
import static dk.kb.bitrepository.mediator.utils.configurations.ConfigConstants.ENCRYPTED_FILES_PATH;
import static dk.kb.bitrepository.mediator.utils.configurations.ConfigConstants.UNENCRYPTED_FILES_PATH;
import static org.apache.commons.io.FileUtils.cleanDirectory;
import static org.bitrepository.common.utils.ChecksumUtils.generateChecksum;
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
        Configurations testConfig = MediatorComponentFactory.loadMediatorConfigurations("conf");
        DatabaseConfigurations databaseConfig = testConfig.getDatabaseConfig();
        dao = MediatorComponentFactory.getDAO(databaseConfig);
        cryptoConfigurations = testConfig.getCryptoConfig();

        fileBytes = "test-string".getBytes(Charset.defaultCharset());

        ChecksumSpecTYPE checksumSpecTYPE = new ChecksumSpecTYPE();
        checksumSpecTYPE.setChecksumType(ChecksumType.MD5);
        String checksum = generateChecksum(new ByteArrayInputStream(fileBytes), checksumSpecTYPE);
        checksumDataForFileTYPE = new ChecksumDataForFileTYPE();
        checksumDataForFileTYPE.setChecksumSpec(checksumSpecTYPE);
        checksumDataForFileTYPE.setChecksumValue(checksum.getBytes(Charset.defaultCharset()));
    }

    @AfterEach
    public void afterEach() {
        cleanupFiles(UNENCRYPTED_FILES_PATH);
        cleanupFiles(ENCRYPTED_FILES_PATH);
        dao.delete(COLLECTION_ID, FILE_ID, ENC_PARAMS_TABLE);
        dao.delete(COLLECTION_ID, FILE_ID, FILES_TABLE);
    }

    @BeforeEach
    public void beforeEach() {
    }

    @Test
    @DisplayName("Test PutFile method")
    public void testPutFile() {
        PutFileHandler handler = new PutFileHandler(COLLECTION_ID, FILE_ID, fileBytes, checksumDataForFileTYPE,
                cryptoConfigurations, dao);
        try {
            handler.performPutFile();
        } catch (FileExistsException e) {
            System.out.println("File already exists.");
            return;
        }
        assertTrue(Files.exists(Path.of(ENCRYPTED_FILES_PATH + "/" + COLLECTION_ID + "/" + FILE_ID)));
        assertTrue(Files.exists(Path.of(UNENCRYPTED_FILES_PATH + "/" + COLLECTION_ID + "/" + FILE_ID)));
        assertNotNull(dao.select(COLLECTION_ID, FILE_ID, FILES_TABLE));
        assertNotNull(dao.select(COLLECTION_ID, FILE_ID, ENC_PARAMS_TABLE));
    }

    public void cleanupFiles(String path) {
        File dir = new File(path);
        if (dir.exists()) {
            try {
                cleanDirectory(dir);
            } catch (IOException e) {
                System.out.println("Something went wrong trying to clean up /files/ directory." + e);
            }
        }
    }
}

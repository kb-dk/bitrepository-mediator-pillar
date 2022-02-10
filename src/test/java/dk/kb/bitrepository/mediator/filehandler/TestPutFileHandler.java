package dk.kb.bitrepository.mediator.filehandler;

import dk.kb.bitrepository.mediator.utils.configurations.ConfigurationHandler;
import dk.kb.bitrepository.mediator.utils.configurations.Configurations;
import dk.kb.bitrepository.mediator.utils.configurations.CryptoConfigurations;
import org.apache.commons.io.FileExistsException;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.junit.jupiter.api.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import static dk.kb.bitrepository.mediator.TestConstants.CONFIG_PATH_TEST;
import static dk.kb.bitrepository.mediator.database.DatabaseCalls.delete;
import static dk.kb.bitrepository.mediator.database.DatabaseCalls.select;
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

    @BeforeAll
    static void setup() throws FileNotFoundException {
        new ConfigurationHandler(CONFIG_PATH_TEST);
        Configurations config = ConfigurationHandler.getConfigurations();
        cryptoConfigurations = config.getCryptoConfig();

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
    }

    @BeforeEach
    public void beforeEach() {
        delete(COLLECTION_ID, FILE_ID, ENC_PARAMS_TABLE);
        delete(COLLECTION_ID, FILE_ID, FILES_TABLE);
    }

    @Test
    @DisplayName("Test PutFile method")
    public void testPutFile() {
        PutFileHandler handler = new PutFileHandler(COLLECTION_ID, FILE_ID, fileBytes, checksumDataForFileTYPE, cryptoConfigurations);
        try {
            handler.putFile();
        } catch (FileExistsException e) {
            System.out.println("File already exists.");
            return;
        }
        assertTrue(Files.exists(Path.of(ENCRYPTED_FILES_PATH + "/" + COLLECTION_ID + "/" + FILE_ID)));
        assertTrue(Files.exists(Path.of(UNENCRYPTED_FILES_PATH + "/" + COLLECTION_ID + "/" + FILE_ID)));
        assertNotNull(select(COLLECTION_ID, FILE_ID, FILES_TABLE));
        assertNotNull(select(COLLECTION_ID, FILE_ID, ENC_PARAMS_TABLE));
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

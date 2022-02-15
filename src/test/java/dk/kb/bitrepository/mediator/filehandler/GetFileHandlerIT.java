package dk.kb.bitrepository.mediator.filehandler;

import dk.kb.bitrepository.mediator.TestingSetup;
import dk.kb.bitrepository.mediator.crypto.AESCryptoStrategy;
import dk.kb.bitrepository.mediator.database.DatabaseDAO;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;

import java.io.IOException;

import static dk.kb.bitrepository.mediator.TestingUtilities.cleanupFiles;
import static dk.kb.bitrepository.mediator.TestingUtilities.loadIncorrectChecksumData;
import static dk.kb.bitrepository.mediator.database.DatabaseConstants.*;
import static dk.kb.bitrepository.mediator.utils.configurations.ConfigConstants.ENCRYPTED_FILES_PATH;
import static dk.kb.bitrepository.mediator.utils.configurations.ConfigConstants.UNENCRYPTED_FILES_PATH;

@DisplayName("Test #GetFileHandler")
public class GetFileHandlerIT {
    private static byte[] fileBytes;
    private static ChecksumDataForFileTYPE checksumDataForFileTYPE;
    private static DatabaseDAO dao;
    private static AESCryptoStrategy crypto;
    private static ChecksumDataForFileTYPE checksumDataWithWrongChecksum;

    @BeforeAll
    static void setup() throws IOException {
        TestingSetup setup = new TestingSetup();
        fileBytes = setup.getFileBytes();
        checksumDataForFileTYPE = setup.getChecksumDataForFileTYPE();
        dao = setup.getDao();
        crypto = new AESCryptoStrategy(setup.getCryptoConfigurations().getPassword());
        checksumDataWithWrongChecksum = loadIncorrectChecksumData();
    }

    @AfterEach
    public void cleanup() {
        cleanupFiles(UNENCRYPTED_FILES_PATH);
        cleanupFiles(ENCRYPTED_FILES_PATH);
        dao.delete(COLLECTION_ID, FILE_ID, ENC_PARAMS_TABLE);
        dao.delete(COLLECTION_ID, FILE_ID, FILES_TABLE);
    }
}

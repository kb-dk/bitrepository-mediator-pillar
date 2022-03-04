package dk.kb.bitrepository.mediator;

import dk.kb.bitrepository.mediator.crypto.AESCryptoStrategy;
import dk.kb.bitrepository.mediator.crypto.CryptoStrategy;
import dk.kb.bitrepository.mediator.database.DatabaseDAO;
import dk.kb.bitrepository.mediator.utils.configurations.Configurations;
import dk.kb.bitrepository.mediator.utils.configurations.CryptoConfigurations;
import dk.kb.bitrepository.mediator.utils.configurations.DatabaseConfigurations;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.nio.charset.Charset;

import static dk.kb.bitrepository.mediator.TestingUtilities.loadChecksumData;
import static dk.kb.bitrepository.mediator.database.DatabaseConstants.*;

public class TestingDAO {
    protected static String fileContent = "test-string";
    protected static byte[] fileBytes = fileContent.getBytes(Charset.defaultCharset());
    protected static ChecksumDataForFileTYPE checksumDataForFileTYPE;
    protected static CryptoConfigurations cryptoConfigurations;
    protected static String encryptionPassword;
    protected static CryptoStrategy crypto;
    protected static DatabaseDAO dao = null;
    protected static DatabaseConfigurations databaseConfigurations = null;
    protected static Configurations configurations;

    protected static void initTestingDAO() throws IOException {
        setup(true);
    }

    protected static void initTestingDAO(boolean setupDatabase) throws IOException {
        setup(setupDatabase);
    }

    @BeforeEach
    protected void initChecksumData() {
        checksumDataForFileTYPE = loadChecksumData(fileBytes);
    }

    @AfterEach
    protected void cleanUpDatabase() {
        if (dao != null) {
            dao.delete(COLLECTION_ID, FILE_ID, ENC_PARAMS_TABLE);
            dao.delete(COLLECTION_ID, FILE_ID, FILES_TABLE);
        }
    }

    private static void setup(boolean setupDatabase) throws IOException {
        configurations = MediatorPillarComponentFactory.loadMediatorConfigurations("conf");
        cryptoConfigurations = configurations.getCryptoConfig();
        encryptionPassword = cryptoConfigurations.getPassword();
        crypto = new AESCryptoStrategy(encryptionPassword); // Can change the encryption strategy here for testing

        checksumDataForFileTYPE = loadChecksumData(fileBytes);

        if (setupDatabase) {
            if (dao == null) {
                databaseConfigurations = configurations.getDatabaseConfig();
                dao = MediatorPillarComponentFactory.getDAO(databaseConfigurations);
            }
        }
    }
}

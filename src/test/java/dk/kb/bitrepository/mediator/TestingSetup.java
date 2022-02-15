package dk.kb.bitrepository.mediator;

import dk.kb.bitrepository.mediator.database.DatabaseDAO;
import dk.kb.bitrepository.mediator.utils.configurations.Configurations;
import dk.kb.bitrepository.mediator.utils.configurations.CryptoConfigurations;
import dk.kb.bitrepository.mediator.utils.configurations.DatabaseConfigurations;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;

import java.io.IOException;
import java.nio.charset.Charset;

import static dk.kb.bitrepository.mediator.TestingUtilities.loadChecksumData;

public class TestingSetup {
    private byte[] fileBytes;
    private ChecksumDataForFileTYPE checksumDataForFileTYPE;
    private CryptoConfigurations cryptoConfigurations;
    private DatabaseDAO dao = null;
    private DatabaseConfigurations databaseConfigurations = null;
    private Configurations configurations;

    public TestingSetup(boolean setupDatabase) throws IOException {
        setup(setupDatabase);
    }

    /**
     * No parameter defaults setting up the database configurations to true. Should only be used in integration tests.
     */
    public TestingSetup() throws IOException {
        setup(true);
    }

    private void setup(boolean setupDatabase) throws IOException {
        configurations = MediatorComponentFactory.loadMediatorConfigurations("conf");
        cryptoConfigurations = configurations.getCryptoConfig();

        fileBytes = "test-string".getBytes(Charset.defaultCharset());
        checksumDataForFileTYPE = loadChecksumData(fileBytes);

        if (setupDatabase) {
            databaseConfigurations = configurations.getDatabaseConfig();
            dao = MediatorComponentFactory.getDAO(databaseConfigurations);
        }
    }

    public byte[] getFileBytes() {
        return fileBytes;
    }

    public ChecksumDataForFileTYPE getChecksumDataForFileTYPE() {
        return checksumDataForFileTYPE;
    }

    public CryptoConfigurations getCryptoConfigurations() {
        return cryptoConfigurations;
    }

    public DatabaseDAO getDao() {
        return dao;
    }

    public DatabaseConfigurations getDatabaseConfigurations() {
        return databaseConfigurations;
    }

    public Configurations getConfigurations() {
        return configurations;
    }
}

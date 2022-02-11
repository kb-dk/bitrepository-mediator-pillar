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
    private final byte[] fileBytes;
    private final ChecksumDataForFileTYPE checksumDataForFileTYPE;
    private final CryptoConfigurations cryptoConfigurations;
    private final DatabaseDAO dao;
    private final DatabaseConfigurations databaseConfigurations;
    private final Configurations configurations;

    public TestingSetup() throws IOException {
        configurations = MediatorComponentFactory.loadMediatorConfigurations("conf");
        databaseConfigurations = configurations.getDatabaseConfig();
        cryptoConfigurations = configurations.getCryptoConfig();
        dao = MediatorComponentFactory.getDAO(databaseConfigurations);

        fileBytes = "test-string".getBytes(Charset.defaultCharset());

        checksumDataForFileTYPE = loadChecksumData(fileBytes);
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

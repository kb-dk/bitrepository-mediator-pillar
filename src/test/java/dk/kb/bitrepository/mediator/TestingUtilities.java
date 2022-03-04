package dk.kb.bitrepository.mediator;

import dk.kb.bitrepository.mediator.pillaraccess.communication.DummySecurityManager;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.SettingsProvider;
import org.bitrepository.common.settings.XMLFileSettingsLoader;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.protocol.security.SecurityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import static org.apache.commons.io.FileUtils.cleanDirectory;
import static org.bitrepository.common.utils.ChecksumUtils.generateChecksum;

public class TestingUtilities {
    private static final Logger log = LoggerFactory.getLogger(TestingUtilities.class);

    public static Settings loadRefPillarSettings(String pillarID, String pathToConfiguration) throws IOException {
        SettingsProvider settingsProvider = new SettingsProvider(new XMLFileSettingsLoader(pathToConfiguration), pillarID);
        return settingsProvider.getSettings();
    }

    /**
     * Sets up the ChecksumData for given file data as bytes.
     *
     * @param bytes The file data as bytes.
     * @return Returns a ChecksumDataForFileType that contains the checksum type and checksum of the file data.
     */
    public static ChecksumDataForFileTYPE loadChecksumData(byte[] bytes) {
        ChecksumSpecTYPE checksumSpecTYPE = new ChecksumSpecTYPE();
        checksumSpecTYPE.setChecksumType(ChecksumType.MD5);
        String checksum = generateChecksum(new ByteArrayInputStream(bytes), checksumSpecTYPE);
        ChecksumDataForFileTYPE checksumDataForFileTYPE = new ChecksumDataForFileTYPE();
        checksumDataForFileTYPE.setChecksumSpec(checksumSpecTYPE);
        checksumDataForFileTYPE.setChecksumValue(Base16Utils.encodeBase16(checksum));

        return checksumDataForFileTYPE;
    }

    public static ChecksumDataForFileTYPE loadIncorrectChecksumData() {
        ChecksumDataForFileTYPE checksumDataWithWrongChecksum = new ChecksumDataForFileTYPE();
        checksumDataWithWrongChecksum.setChecksumValue(new byte[12]);
        ChecksumSpecTYPE checksumSpecType = new ChecksumSpecTYPE();
        checksumSpecType.setChecksumType(ChecksumType.MD5);
        checksumDataWithWrongChecksum.setChecksumSpec(checksumSpecType);

        return checksumDataWithWrongChecksum;
    }

    /**
     * Removes all files in a given directory.
     *
     * @param path The path to the directory to clean up.
     */
    public static void cleanupFiles(String path) {
        File dir = new File(path);
        if (dir.exists()) {
            try {
                cleanDirectory(dir);
                log.info("Cleaned up directory {}", dir);
            } catch (IOException e) {
                log.error("Something went wrong trying to clean up /files/ directory.");
            }
        }
    }

    public static SecurityManager createSecurityManager() {
        return new DummySecurityManager();
    }
}

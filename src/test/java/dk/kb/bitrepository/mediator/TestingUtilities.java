package dk.kb.bitrepository.mediator;

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.SettingsProvider;
import org.bitrepository.common.settings.XMLFileSettingsLoader;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import static org.apache.commons.io.FileUtils.cleanDirectory;
import static org.bitrepository.common.utils.ChecksumUtils.generateChecksum;

public class TestingUtilities {
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
        checksumDataForFileTYPE.setChecksumValue(checksum.getBytes(Charset.defaultCharset()));

        return checksumDataForFileTYPE;
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
            } catch (IOException e) {
                System.out.println("Something went wrong trying to clean up /files/ directory." + e);
            }
        }
    }
}

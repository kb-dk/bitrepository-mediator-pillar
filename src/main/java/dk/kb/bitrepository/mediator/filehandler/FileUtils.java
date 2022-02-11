package dk.kb.bitrepository.mediator.filehandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import static java.lang.String.format;

public class FileUtils {
    private static final Logger log = LoggerFactory.getLogger(FileUtils.class);

    public FileUtils() {
    }

    /**
     * Writes the given bytes to a file.
     * The file will be located at ".../directory/collectionID/fileID".
     *
     * @param bytes     The bytes to write.
     * @param directory The string representation of the directory to write the file to.
     */
    protected static boolean writeBytesToFile(byte[] bytes, String directory, String collectionID, String fileID) {
        Locale locale = Locale.getDefault();
        String fileDirectory = format(locale, "%s/%s", directory, collectionID);
        String tempFileDirectory = format(locale, "%s/temp/%s", directory, collectionID);
        Path filePath = getFilePath(directory, collectionID, fileID);
        Path tempFilePath = Path.of(format(locale, "%s/%s/", tempFileDirectory, fileID));

        boolean fileExists = Files.exists(filePath);

        if (fileExists) {
            log.warn("File already exists.");
        } else {
            ensureDirectoryExists(fileDirectory, tempFileDirectory);

            try {
                OutputStream output = Files.newOutputStream(tempFilePath);
                output.write(bytes);
                output.close();
                log.debug("Bytes has successfully been written to temp file {}", tempFilePath);

                Files.move(tempFilePath, filePath);
                log.debug("File was moved from {} to {} folder.", tempFilePath, filePath);
                return true;
            } catch (IOException e) {
                log.error("Something went wrong with the file.", e);
            }
        }
        return false;
    }

    /**
     * Returns a complete file path given the directory, collectionID and fileID of a file.
     *
     * @param directory    The directory, files or encrypted-files.
     * @param collectionID The collectionID to find the file under.
     * @param fileID       The fileID of the file.
     * @return Returns the complete path created from the above information.
     */
    protected static Path getFilePath(String directory, String collectionID, String fileID) {
        return Path.of(format(Locale.getDefault(), "%s/%s/%s", directory, collectionID, fileID));
    }

    /**
     * Performs a check to see if the given file directories exists.
     * If this is not the case, then the directories will be created.
     */
    private static void ensureDirectoryExists(String... directories) {
        for (String directory : directories) {
            if (!Files.exists(Path.of(directory))) {
                if (new File(directory).mkdirs()) {
                    log.debug("Created directory {}", directory);
                }
            }
        }
    }

    /**
     * Reads bytes from a local file.
     *
     * @param path The path to the local file.
     * @return Returns the file data as a byte array.
     */
    protected static byte[] readBytesFromFile(Path path) {
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            log.error("Error occurred when trying to read bytes from file {}.", path);
        }
        return new byte[0];
    }
}

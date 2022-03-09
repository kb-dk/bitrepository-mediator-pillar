package dk.kb.bitrepository.mediator.filehandler;

import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Locale;

import static java.lang.String.format;
import static org.bitrepository.common.utils.ChecksumUtils.generateChecksum;

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
        Path filePath = createFilePath(directory, collectionID, fileID);
        Path tempFilePath = Path.of(format(locale, "%s/%s/", tempFileDirectory, fileID));

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
            log.error("Could not write to the specified file.", e);
            return false;
        }
    }

    /**
     * Used to check if the file at the given path exists.
     *
     * @param directory    The directory path.
     * @param collectionID The collections ID.
     * @param fileID       The files ID.
     * @return Whether there exists a file at the path directory/collectionID/fileID/ .
     */
    public static boolean fileExists(String directory, String collectionID, String fileID) {
        return Files.exists(createFilePath(directory, collectionID, fileID));
    }

    /**
     * Used to check if the file at the given path exists.
     *
     * @param path The path to check for the file.
     * @return Whether there exists a file at the given path.
     */
    public static boolean fileExists(Path path) {
        return Files.exists(path);
    }

    /**
     * Returns a complete file path given the directory, collectionID and fileID of a file.
     *
     * @param directory    The directory, files or encrypted-files.
     * @param collectionID The collectionID to find the file under.
     * @param fileID       The fileID of the file.
     * @return Returns the complete path created from the above information.
     */
    protected static Path createFilePath(String directory, String collectionID, String fileID) {
        return Path.of(format(Locale.getDefault(), "%s/%s/%s", directory, collectionID, fileID));
    }

    protected static Path createFileDir(String directory, String collectionID) {
        return Path.of(format(Locale.getDefault(), "%s/%s", directory, collectionID));
    }

    /**
     * Performs a check to see if the given file directories exists.
     * If this is not the case, then the directories will be created.
     */
    protected static void ensureDirectoryExists(Path... directories) {
        for (Path dir : directories) {
            if (!Files.exists(dir)) {
                try {
                    Files.createDirectories(dir);
                    log.debug("Created directory {}", dir);
                } catch (IOException e) {
                    log.error("Could not create directory {}", dir, e);
                }
            }
        }
    }

    /**
     * See {@link #ensureDirectoryExists(Path...)} for more information.
     */
    protected static void ensureDirectoryExists(String... directories) {
        Arrays.stream(directories).forEach((dir) -> ensureDirectoryExists(Path.of(dir)));
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

    /**
     * Reads bytes from a local file.
     *
     * @param path The string value of the path to the local file.
     * @return Returns the file data as a byte array.
     */
    protected static byte[] readBytesFromFile(String path) {
        return readBytesFromFile(Path.of(path));
    }

    /**
     * Deletes a local file.
     *
     * @param path The path to the file to delete.
     */
    protected static void deleteFileLocally(Path path) {
        try {
            Files.delete(path);
            log.info("File {} deleted", path);
        } catch (IOException e) {
            log.error("Could no delete file {}.", path);
        }
    }

    /**
     * Computes the checksum from byte[] and compare it to some expected checksum.
     *
     * @param bytesFromFile    Byte[] to compute checksum of.
     * @param checksumSpec     The checksum Spec, used when creating a checksum.
     * @param expectedChecksum The expected checksum.
     * @return Returns true if the two checksums match.
     */
    protected static boolean compareChecksums(byte[] bytesFromFile, ChecksumSpecTYPE checksumSpec, String expectedChecksum) {
        log.debug("Comparing checksums");
        String newChecksum = generateChecksum(new ByteArrayInputStream(bytesFromFile), checksumSpec);

        return newChecksum.equals(expectedChecksum);
    }

    /**
     * Reads the attribute for when the file was created.
     *
     * @param path The path of the file.
     * @return Returns an {@link OffsetDateTime} object for when the chosen file was created.
     * @throws IOException Throws an exception if the file could not be read or wasn't found.
     */
    protected static OffsetDateTime readFileCreationDate(Path path) throws IOException {
        BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
        return OffsetDateTime.ofInstant(attributes.creationTime().toInstant(), ZoneId.systemDefault());
    }

    /**
     * Computes the size in long of the file to be put on the pillar.
     *
     * @param filePath The Path to the file as String.
     * @return The size of the file in Long.
     */
    protected static long getFileSize(String filePath) {
        File file = new File(filePath);
        if (!file.isFile()) {
            throw new IllegalArgumentException("The file '" + filePath + "' is invalid. It does not exists or it " + "is a directory.");
        }

        return file.length();
    }

    /**
     * Computes the size in long of the file to be put on the pillar.
     *
     * @param filePath The Path to the file as Path type.
     * @return The size of the file in Long.
     */
    protected static long getFileSize(Path filePath) {
        return getFileSize(filePath.toString());
    }
}

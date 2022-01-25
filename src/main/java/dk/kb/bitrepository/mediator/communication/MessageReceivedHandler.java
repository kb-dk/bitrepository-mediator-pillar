package dk.kb.bitrepository.mediator.communication;

import dk.kb.bitrepository.database.configs.ConfigurationHandler;
import dk.kb.bitrepository.utils.crypto.AESCryptoStrategy;
import dk.kb.bitrepository.utils.crypto.CryptoStrategy;
import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class MessageReceivedHandler {
    private static final Logger log = LoggerFactory.getLogger(MessageReceivedHandler.class);
    private ConfigurationHandler config = null;

    public MessageReceivedHandler(ConfigurationHandler configurationHandler) {
        this.config = configurationHandler;
    }

    //TODO: Must be asynchronous to not block up the mediator pillar
    public <T> Object handleReceivedMessage(MockupMessageObject message) {
        switch (message.getType()) {
            case PUT_FILE:
                return new PutFile(config, message).execute();
            case GET_FILE:
                return new GetFile(config, message).execute();
            case DELETE_FILE:
                return new DeleteFile(message).execute();
            case REPLACE_FILE:
                return new ReplaceFile(config, message).execute();
            case GET_CHECKSUMS:
                //
                break;
            default:
                log.error("Unsupported message type.");
        }
        return null;
    }

    /**
     * Writes the given bytes to a file.
     *
     * @param bytes    The bytes to write.
     * @param filePath The path to the file.
     */
    public static void writeBytesToFile(byte[] bytes, Path filePath) throws FileExistsException {
        boolean fileExist = Files.exists(filePath);
        if (fileExist) {
            log.warn("File already exists at {}.", filePath);
            throw new FileExistsException();
        } else {
            try {
                OutputStream output = Files.newOutputStream(filePath);
                output.write(bytes);
                output.close();
                log.info("Bytes has successfully been written to file {}", filePath);
            } catch (IOException e) {
                log.error("Something went wrong writing to the file.", e);
            }
        }
    }

    /**
     * Standard initialization of an AESCryptoStrategy.
     *
     * @param password The password to use in the encryption.
     * @return Returns a standard AESCryptoStrategy instance.
     */
    @NotNull
    static CryptoStrategy initAES(String password) {
        return new AESCryptoStrategy(password);
    }

    /**
     * Using the overloaded constructor, in order to pass pre-determined salt and IV.
     * Mainly used to create an AES instance for decryption.
     *
     * @param password The password that was previously used to encrypt.
     * @param salt     The pre-determined salt.
     * @param iv       The pre-determined IV.
     * @return Returns an AESCryptoStrategy instance using pre-determined values.
     */
    @NotNull
    static CryptoStrategy initAES(String password, String salt, byte[] iv) {
        return new AESCryptoStrategy(password, salt, iv);
    }

    /**
     * Given a crypto strategy, write the given bytes to a local file, and create an encrypted file.
     *
     * @param crypto            An instance of a crypto protocol, e.g. AES.
     * @param bytes             The bytes to be written to a local file.
     * @param filePath          The path to where the file will be created.
     * @param encryptedFilePath The path to where the encrypted file will be created.
     */
    public static boolean createAndEncryptFileFromBytes(CryptoStrategy crypto, byte[] bytes, Path filePath, Path encryptedFilePath) {
        try {
            writeBytesToFile(bytes, filePath);
        } catch (FileExistsException e) {
            log.error("The file already exists.");
        }

        // TODO: Check encryption is done correctly from time to time, perhaps decrypt and compare checksums?
        crypto.encrypt(filePath, encryptedFilePath);
        boolean encryptedFileExists = Files.exists(encryptedFilePath);

        if (!encryptedFileExists) {
            log.error("Something went wrong during encryption, and the encrypted file does not exist.");
            return false;
        }
        return true;
    }

    /**
     * Returns a string that represents the path to store the unencrypted file at.
     *
     * @param collectionID Collection ID of the file.
     * @param fileID       File ID of the file.
     * @return The string representation of the path, created from the unique collection ID and file ID.
     */
    @Deprecated
    public static String getFilePath(String collectionID, String fileID) {
        return "src/main/java/dk/kb/bitrepository/mediator/files/file:" + collectionID + ":" + fileID;
    }

    /**
     * Returns a string that represents the path to store the encrypted file at.
     *
     * @param collectionID Collection ID of the file.
     * @param fileID       File ID of the file.
     * @return The string representation of the path, created from the unique collection ID and file ID.
     */
    @Deprecated
    public static String getEncryptedFilePath(String collectionID, String fileID) {
        return "src/main/java/dk/kb/bitrepository/mediator/files/file_encrypted:" + collectionID + ":" + fileID;
    }

    /**
     * Returns a string that represents the path to store the decrypted file at.
     *
     * @param collectionID Collection ID of the file.
     * @param fileID       File ID of the file.
     * @return The string representation of the path, created from the unique collection ID and file ID.
     */
    @Deprecated
    public static String getDecryptedFilePath(String collectionID, String fileID) {
        return "src/main/java/dk/kb/bitrepository/mediator/files/file_decrypted:" + collectionID + ":" + fileID;
    }

    /**
     * Helper method that is used to delete local files.
     */
    @Deprecated
    public static void cleanupFiles() {
        File dir = new File("src/main/java/dk/kb/bitrepository/mediator/files/");
        try {
            FileUtils.cleanDirectory(dir);
        } catch (IOException e) {
            log.error("Something went wrong trying to clean up /files/ directory.", e);
        }
    }
}

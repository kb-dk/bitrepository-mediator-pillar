package dk.kb.bitrepository.mediator.communication;

import dk.kb.bitrepository.database.configs.ConfigurationHandler;
import dk.kb.bitrepository.utils.crypto.AESCryptoStrategy;
import dk.kb.bitrepository.utils.crypto.CryptoStrategy;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;

public class MessageReceivedHandler {
    private static final Logger log = LoggerFactory.getLogger(MessageReceivedHandler.class);
    private final ConfigurationHandler config;
    private byte[] output;

    public MessageReceivedHandler(ConfigurationHandler configurationHandler) {
        this.config = configurationHandler;
    }

    //TODO: Must be asynchronous to not block up the mediator pillar
    public void handleReceivedMessage(MockupMessageObject message) {
        switch (message.getType()) {
            case PUT_FILE:
                new PutFile(config).putFile(message.getPayload(), message.getCollectionID(), message.getFileID());
                break;
            case GET_FILE:
                GetFile get = new GetFile(config, message);
                output = get.execute();
                break;
            case DELETE_FILE:
                //
                break;
            case REPLACE_FILE:
                //
                break;
            case GET_CHECKSUMS:
                //
                break;
            default:
                log.error("Unsupported message type.");
                break;
        }
    }

    public byte[] getMessageOutput() {
        return output;
    }

    /**
     * Writes the given bytes to a file.
     *
     * @param input    The bytes to write.
     * @param filePath The path to the file.
     */
    static void writeBytesToFile(byte[] input, Path filePath) {
        try {
            File fileOut = new File(String.valueOf(filePath));
            OutputStream output = new FileOutputStream(fileOut);
            output.write(input);
            output.close();
            log.info("Object has successfully been written to a file.");
        } catch (FileNotFoundException e) {
            log.error("The file could not be found.");
        } catch (IOException e) {
            log.error("Something went wrong writing to the file.", e);
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
     * Returns a string that represents the path to store the unencrypted file at.
     *
     * @param collectionID Collection ID of the file.
     * @param fileID       File ID of the file.
     * @return The string representation of the path, created from the unique collection ID and file ID.
     */
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
    public static String getDecryptedFilePath(String collectionID, String fileID) {
        return "src/main/java/dk/kb/bitrepository/mediator/files/file_decrypted:" + collectionID + ":" + fileID;
    }

    /**
     * Helper method that is used to delete local files.
     */
    public void cleanupFiles() {
        File dir = new File("src/main/java/dk/kb/bitrepository/mediator/files/");
        try {
            FileUtils.cleanDirectory(dir);
        } catch (IOException e) {
            log.error("Something went wrong trying to clean up /files/ directory.", e);
        }
    }
}

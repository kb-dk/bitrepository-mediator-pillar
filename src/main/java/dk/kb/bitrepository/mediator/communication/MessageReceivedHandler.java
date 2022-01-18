package dk.kb.bitrepository.mediator.communication;

import dk.kb.bitrepository.utils.crypto.AESCryptoStrategy;
import dk.kb.bitrepository.utils.crypto.CryptoStrategy;
import org.apache.commons.io.FileUtils;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.OffsetDateTime;

import static dk.kb.bitrepository.database.DatabaseCalls.insertInto;
import static org.bitrepository.common.utils.ChecksumUtils.generateChecksum;

public class MessageReceivedHandler {
    private static final Logger log = LoggerFactory.getLogger(MessageReceivedHandler.class);

    public MessageReceivedHandler() {
    }

    //TODO: Must be asynchronous to not block up the mediator pillar
    public void handleReceivedMessage(MockupMessageObject message) {
        switch (message.getType()) {
            case "PUT_FILE":
                putFile(message.getPayload(), message.getCollectionID(), message.getFileID());
                break;
            case "GET_FILE":
                getFile(message.getCollectionID(), message.getFileID());
                break;
            // TODO:... Rest of the cases here
            default:
                log.error("Unsupported message type.");
                break;
        }
    }

    public static void putFile(byte[] file, String collectionID, String fileID, Object... encryptionParameters) {
        OffsetDateTime fileReceivedTimestamp = OffsetDateTime.now(Clock.systemUTC());
        CryptoStrategy AES;
        //FIXME: Put password in ENV?
        String password = "testPassword";

        if (encryptionParameters.length > 0) {
            String salt = (String) encryptionParameters[0];
            byte[] iv = (byte[]) encryptionParameters[1];
            AES = initAES(password, salt, iv);
        } else {
            AES = initAES(password);
        }

        Path filePath = Paths.get("src/main/java/dk/kb/bitrepository/mediator/files/test:"
                + collectionID + ":" + fileID);
        Path encryptedFilePath = Paths.get("src/main/java/dk/kb/bitrepository/mediator/files/test_encrypted:"
                + collectionID + ":" + fileID);

        boolean fileExist = Files.exists(filePath);
        if (fileExist) {
            log.warn("File already exists at {}.", filePath);
            System.exit(0);
        }

        writeBytesToFile(file, filePath);

        // TODO: Check encryption is done correctly from time to time, perhaps decrypt and compare checksums?
        AES.encrypt(filePath, encryptedFilePath);
        boolean encryptedFileExists = Files.exists(encryptedFilePath);
        OffsetDateTime encryptedTimestamp = OffsetDateTime.now(Clock.systemUTC());

        if (!encryptedFileExists) {
            log.error("Something went wrong during encryption, and the encrypted file does not exist.");
            System.exit(0);
        }

        // FIXME:
        //  Is this the correct way of computing checksum?
        //  Load in the encrypted file and relay message to the encrypted pillar WITH the encrypted file
        String checksum = generateChecksum(new File(String.valueOf(filePath)), ChecksumType.MD5);
        String encryptedChecksum = generateChecksum(new File(String.valueOf(encryptedFilePath)), ChecksumType.MD5);
        OffsetDateTime checksumTimestamp = OffsetDateTime.now(Clock.systemUTC());

        insertInto(collectionID, fileID, AES.getSalt(), AES.getIV().getIV(), "1");
        insertInto(collectionID, fileID, fileReceivedTimestamp, encryptedTimestamp, checksum, encryptedChecksum, checksumTimestamp);
        //cleanUpFiles();
    }

    public static void getFile(String collectionID, String filID) {

    }

    @NotNull
    private static CryptoStrategy initAES(String password) {
        return new AESCryptoStrategy(password);
    }

    @NotNull
    private static CryptoStrategy initAES(String password, String salt, byte[] iv) {
        return new AESCryptoStrategy(password, salt, iv);
    }

    private static void writeBytesToFile(byte[] file, Path filePath) {
        try {
            File fileOut = new File(String.valueOf(filePath));
            OutputStream output = new FileOutputStream(fileOut);
            output.write(file);
            output.close();
            log.info("Object has successfully been written to a file.");
        } catch (FileNotFoundException e) {
            log.error("The file could not be found.");
        } catch (IOException e) {
            log.error("Something went wrong writing to the file.", e);
        }
    }

    /**
     * Helper method that is used to delete local files.
     */
    public static void cleanupFiles() {
        File dir = new File("src/main/java/dk/kb/bitrepository/mediator/files/");
        try {
            FileUtils.cleanDirectory(dir);
        } catch (IOException e) {
            log.error("Something went wrong trying to clean up /files/ directory.", e);
        }
    }
}

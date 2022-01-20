package dk.kb.bitrepository.mediator.communication;

import dk.kb.bitrepository.database.configs.ConfigurationHandler;
import dk.kb.bitrepository.utils.crypto.CryptoStrategy;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.OffsetDateTime;

import static dk.kb.bitrepository.database.DatabaseCalls.insertInto;
import static dk.kb.bitrepository.mediator.communication.MessageReceivedHandler.*;
import static org.bitrepository.common.utils.ChecksumUtils.generateChecksum;

public class PutFile extends MessageResult<Boolean> {
    private final ConfigurationHandler config;
    private final byte[] file;
    private final String collectionID;
    private final String fileID;
    private final Logger log = LoggerFactory.getLogger(MessageReceivedHandler.class);

    public PutFile(ConfigurationHandler config, MockupMessageObject message) {
        this.config = config;
        this.file = message.getPayload();
        this.collectionID = message.getCollectionID();
        this.fileID = message.getFileID();
    }

    @Override
    public Boolean execute() {
        OffsetDateTime fileReceivedTimestamp = OffsetDateTime.now(Clock.systemUTC());
        CryptoStrategy AES;
        String password = null;
        try {
            password = config.getEncryptionPassword();
        } catch (IOException e) {
            log.error("An error occurred when trying to get encryption password from configs.", e);
        }

        AES = initAES(password);

        Path filePath = Paths.get(getFilePath(collectionID, fileID));
        Path encryptedFilePath = Paths.get(getEncryptedFilePath(collectionID, fileID));

        boolean fileExist = Files.exists(filePath);
        if (fileExist) {
            log.warn("File already exists at {}.", filePath);
            System.exit(0);
        }

        MessageReceivedHandler.writeBytesToFile(file, filePath);

        // TODO: Check encryption is done correctly from time to time, perhaps decrypt and compare checksums?
        AES.encrypt(filePath, encryptedFilePath);
        boolean encryptedFileExists = Files.exists(encryptedFilePath);
        OffsetDateTime encryptedTimestamp = OffsetDateTime.now(Clock.systemUTC());

        if (!encryptedFileExists) {
            log.error("Something went wrong during encryption, and the encrypted file does not exist.");
            return Boolean.FALSE;
        }

        // FIXME:
        //  Is this the correct way of computing checksum?
        //  Relay message to the encrypted pillar w. the encrypted file
        String checksum = generateChecksum(new File(String.valueOf(filePath)), ChecksumType.MD5);
        String encryptedChecksum = generateChecksum(new File(String.valueOf(encryptedFilePath)), ChecksumType.MD5);

        AES.encrypt(filePath, encryptedFilePath);

        OffsetDateTime checksumTimestamp = OffsetDateTime.now(Clock.systemUTC());

        insertInto(collectionID, fileID, AES.getSalt(), AES.getIV().getIV(), "1");
        insertInto(collectionID, fileID, fileReceivedTimestamp, encryptedTimestamp, checksum, encryptedChecksum, checksumTimestamp);
        //cleanUpFiles();

        return Boolean.TRUE;
    }
}

package dk.kb.bitrepository.mediator.communication;

import dk.kb.bitrepository.database.configs.ConfigurationHandler;
import dk.kb.bitrepository.utils.crypto.CryptoStrategy;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Clock;
import java.time.OffsetDateTime;

import static dk.kb.bitrepository.database.DatabaseCalls.insertInto;
import static dk.kb.bitrepository.mediator.communication.MessageReceivedHandler.cleanupFiles;
import static dk.kb.bitrepository.mediator.communication.MessageReceivedHandler.initAES;
import static org.bitrepository.common.utils.ChecksumUtils.generateChecksum;

public class PutFile extends MessageResult<Boolean> {
    private final ConfigurationHandler config;
    private final byte[] bytes;
    private final String collectionID;
    private final String fileID;
    private final ChecksumSpecTYPE checksumSpecTYPE;
    private final Logger log = LoggerFactory.getLogger(MessageReceivedHandler.class);

    public PutFile(ConfigurationHandler config, @NotNull MockupMessageObject message) {
        this.config = config;
        this.bytes = message.getPayload();
        this.collectionID = message.getCollectionID();
        this.fileID = message.getFileID();
        checksumSpecTYPE = new ChecksumSpecTYPE();
        checksumSpecTYPE.setChecksumType(ChecksumType.MD5);
    }

    @Override
    public Boolean execute() {
        OffsetDateTime fileReceivedTimestamp = OffsetDateTime.now(Clock.systemUTC());
        CryptoStrategy AES = null;
        try {
            AES = initAES(config.getEncryptionPassword());
        } catch (IOException e) {
            log.error("An error occurred when trying to get encryption password from configs.", e);
        }

        if (AES != null) {
            byte[] encryptedBytes = AES.encrypt(bytes);
            if (encryptedBytes.length > 0) {
                OffsetDateTime encryptedTimestamp = OffsetDateTime.now(Clock.systemUTC());
                {
                    // TODO IMPLEMENT: Relay message to the encrypted pillar w. the encrypted file
                }
                //FIXME: Is this the correct way of computing checksum?
                String checksum = generateChecksum(new ByteArrayInputStream(bytes), checksumSpecTYPE);
                String encryptedChecksum = generateChecksum(new ByteArrayInputStream(encryptedBytes), checksumSpecTYPE);

                OffsetDateTime checksumTimestamp = OffsetDateTime.now(Clock.systemUTC());

                insertInto(collectionID, fileID, AES.getSalt(), AES.getIV().getIV(), AES.getIterations());
                insertInto(collectionID, fileID, fileReceivedTimestamp, encryptedTimestamp, checksum, encryptedChecksum, checksumTimestamp);

                return Boolean.TRUE;
            } else {
                log.error("There was an error encrypting the file.");
            }
        }

        return Boolean.FALSE;
    }
}
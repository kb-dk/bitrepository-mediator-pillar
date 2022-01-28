package dk.kb.bitrepository.mediator.communication;

import dk.kb.bitrepository.database.DatabaseData;
import dk.kb.bitrepository.database.configs.ConfigurationHandler;
import dk.kb.bitrepository.crypto.CryptoStrategy;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static dk.kb.bitrepository.database.DatabaseCalls.*;
import static dk.kb.bitrepository.database.DatabaseConstants.FILES_TABLE;
import static dk.kb.bitrepository.database.DatabaseData.FilesData;
import static dk.kb.bitrepository.mediator.communication.MessageReceivedHandler.initAES;
import static org.bitrepository.common.utils.ChecksumUtils.generateChecksum;

public class ReplaceFile extends MessageResult<Boolean> {
    private final ConfigurationHandler config;
    private final String collectionID;
    private final String fileID;
    private final ChecksumSpecTYPE checksumSpecTYPE;
    private final byte[] payload;
    private final MockupResponse response;
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public ReplaceFile(ConfigurationHandler config, @NotNull MockupMessageObject message) {
        this.config = config;
        this.collectionID = message.getCollectionID();
        this.fileID = message.getFileID();
        this.payload = message.getPayload();
        this.response = message.getMockupResponse();
        checksumSpecTYPE = new ChecksumSpecTYPE();
        checksumSpecTYPE.setChecksumType(ChecksumType.MD5);
    }

    @Override
    public Boolean execute() {
        MockupResponse response = relayMessageToEncryptedPillar(collectionID, fileID);
        byte[] responseEncryptedBytes = response.getPayload(); //FIXME: Real call gives payload + checksum?
        OffsetDateTime receivedTimestamp = OffsetDateTime.now(ZoneOffset.UTC);

        // Retrieve old and compute new checksums
        List<DatabaseData> result = select(collectionID, fileID, FILES_TABLE);
        String storedEncryptedChecksum = ((FilesData) result.get(0)).getEncryptedChecksum();
        String receivedEncryptedChecksum = generateChecksum(new ByteArrayInputStream(responseEncryptedBytes), checksumSpecTYPE);

        // Check that the encrypted checksums match
        if (!storedEncryptedChecksum.equals(receivedEncryptedChecksum)) {
            log.error("The checksums did not match.");
            // TODO: Alarm / Warning that checksums did not match
            return Boolean.FALSE;
        }

        // Encrypt the new file
        byte[] newEncryptedBytes;
        CryptoStrategy AES;
        OffsetDateTime newEncryptedTimestamp;
        try {
            AES = initAES(config.getEncryptionPassword());
            newEncryptedBytes = AES.encrypt(payload);
            newEncryptedTimestamp = OffsetDateTime.now(ZoneOffset.UTC);
        } catch (IOException e) {
            log.error("An error occurred during encryption", e);
            return Boolean.FALSE;
        }

        // Get new values that are to be saved to the mediator pillars internal database
        String newChecksum = generateChecksum(new ByteArrayInputStream(payload), checksumSpecTYPE);
        String newEncryptedChecksum = generateChecksum(new ByteArrayInputStream(newEncryptedBytes), checksumSpecTYPE);
        OffsetDateTime checksumTimestamp = OffsetDateTime.now(ZoneOffset.UTC);
        String newSalt = AES.getSalt();
        byte[] newIV = AES.getIV().getIV();
        int newIterations = AES.getIterations();

        // Updated the internal tables in mediator pillar
        updateFilesTable(collectionID, fileID, receivedTimestamp, newEncryptedTimestamp, newChecksum, newEncryptedChecksum, checksumTimestamp);
        updateEncryptionParametersTable(collectionID, fileID, newSalt, newIV, newIterations);

        // Relay the new encrypted file and checksum to the encrypted pillar
        sendNewEncryptedFileToEncryptedPillar(newEncryptedBytes, newEncryptedChecksum);

        return Boolean.TRUE;
    }

    private MockupResponse relayMessageToEncryptedPillar(String collectionID, String fileID) {
        //TODO: Implement
        return response;
    }

    private void sendNewEncryptedFileToEncryptedPillar(byte[] newEncryptedFileBytes, String newEncryptedChecksum) {
        //TODO:IMPLEMENT Send new checksum + file to encrypted pillar
    }
}

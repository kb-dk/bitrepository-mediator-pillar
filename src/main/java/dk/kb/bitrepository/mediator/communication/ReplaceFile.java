package dk.kb.bitrepository.mediator.communication;

import dk.kb.bitrepository.database.DatabaseData;
import dk.kb.bitrepository.database.configs.ConfigurationHandler;
import dk.kb.bitrepository.utils.crypto.CryptoStrategy;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import static dk.kb.bitrepository.database.DatabaseCalls.select;
import static dk.kb.bitrepository.database.DatabaseConstants.ENC_PARAMS_TABLE;
import static dk.kb.bitrepository.database.DatabaseConstants.FILES_TABLE;
import static dk.kb.bitrepository.database.DatabaseData.EncryptedParametersData;
import static dk.kb.bitrepository.database.DatabaseData.FilesData;
import static dk.kb.bitrepository.mediator.communication.MessageReceivedHandler.initAES;
import static org.bitrepository.common.utils.ChecksumUtils.generateChecksum;

public class ReplaceFile extends MessageResult<Boolean> {
    private final ConfigurationHandler config;
    private final String collectionID;
    private final String fileID;

    private final byte[] payload;
    private final MockupResponse response;
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public ReplaceFile(ConfigurationHandler config, MockupMessageObject message) {
        this.config = config;
        this.collectionID = message.getCollectionID();
        this.fileID = message.getFileID();
        this.payload = message.getPayload();
        this.response = message.getMockupResponse();
    }

    @Override
    public Boolean execute() {
        MockupResponse response = relayMessageToEncryptedPillar(collectionID, fileID);
        byte[] responseEncryptedBytes = response.getPayload();

        List<DatabaseData> result = select(collectionID, fileID, FILES_TABLE);
        String storedEncryptedChecksum = ((FilesData) result.get(0)).getEncryptedChecksum();
        String receivedEncryptedChecksum = generateChecksum(new ByteArrayInputStream(responseEncryptedBytes), new ChecksumSpecTYPE());

        // Check that the encrypted checksums match
        if (!storedEncryptedChecksum.equals(receivedEncryptedChecksum)) {
            log.error("The checksums did not match.");
            // TODO: Alarm / Warning that checksums did not match
            return Boolean.FALSE;
        }

        byte[] encryptedNewBytes = new byte[0];
        // Encrypt the new file using the parameters.
        try {
            result = select(collectionID, fileID, ENC_PARAMS_TABLE);
            EncryptedParametersData firstResult = (EncryptedParametersData) result.get(0);
            CryptoStrategy AES = initAES(config.getEncryptionPassword(), firstResult.getSalt(), firstResult.getIv());
            encryptedNewBytes = AES.encrypt(payload);
        } catch (IOException e) {
            log.error("An error occurred during encryption", e);
        }

        // Compute new checksums
        if (encryptedNewBytes.length > 0) {
            String newChecksum = generateChecksum(new ByteArrayInputStream(payload), new ChecksumSpecTYPE());
            String newEncryptedChecksum = generateChecksum(new ByteArrayInputStream(encryptedNewBytes), new ChecksumSpecTYPE());


        }

        return Boolean.FALSE;
    }

    private MockupResponse relayMessageToEncryptedPillar(String collectionID, String fileID) {
        //TODO: Implement
        return response;
    }
}

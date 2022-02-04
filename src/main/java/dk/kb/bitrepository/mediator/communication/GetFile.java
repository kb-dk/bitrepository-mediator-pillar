package dk.kb.bitrepository.mediator.communication;

import dk.kb.bitrepository.mediator.database.DatabaseData.EncryptedParametersData;
import dk.kb.bitrepository.mediator.database.configs.ConfigurationHandler;
import dk.kb.bitrepository.mediator.crypto.CryptoStrategy;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.common.utils.ChecksumUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static dk.kb.bitrepository.mediator.database.DatabaseDAO.select;
import static dk.kb.bitrepository.mediator.database.DatabaseConstants.*;
import static dk.kb.bitrepository.mediator.database.DatabaseData.FilesData;
import static dk.kb.bitrepository.mediator.communication.MessageReceivedHandler.initAES;

public class GetFile extends MessageResult<byte[]> {
    private final MockupResponse response;
    private final String collectionID;
    private final String fileID;
    private final ConfigurationHandler config;
    private final ChecksumSpecTYPE checksumSpecTYPE;
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public GetFile(ConfigurationHandler config, @NotNull MockupMessageObject message) {
        this.config = config;
        this.collectionID = message.getCollectionID();
        this.fileID = message.getFileID();
        this.response = message.getMockupResponse();
        checksumSpecTYPE = new ChecksumSpecTYPE();
        checksumSpecTYPE.setChecksumType(ChecksumType.MD5);
    }

    @Override
    public byte[] execute() {
        // TODO: Identify => Relay message to Encrypted Pillar
        //  and get response from Encrypted Pillar
        MockupResponse response = relayMessageToEncryptedPillar(collectionID, fileID);
        byte[] encryptedPayload = (response != null) ? response.getPayload() : new byte[0];
        byte[] out = new byte[0];

        if (encryptedPayload.length == 0) {
            log.error("Received no response from the encrypted pillar.");
            return out;
        }

        FilesData filesResult = (FilesData) select(collectionID, fileID, FILES_TABLE);
        if (filesResult != null) {
            // Get old encrypted checksum, and generate new encrypted checksum
            String newEncryptedChecksum = ChecksumUtils.generateChecksum(new ByteArrayInputStream(encryptedPayload), checksumSpecTYPE);
            String oldEncryptedChecksum = filesResult.getEncryptedChecksum();

            // Compared checksum of file from encrypted pillar with the encrypted checksum in local table.
            if (newEncryptedChecksum.equals(oldEncryptedChecksum)) {
                // Get the used encryption parameters from the 'enc_parameters' table.
                EncryptedParametersData encParams = (EncryptedParametersData) select(COLLECTION_ID, FILE_ID, ENC_PARAMS_TABLE);

                // Decrypt the file using the parameters.
                try {
                    CryptoStrategy aes = initAES(config.getEncryptionPassword(), encParams.getSalt(), encParams.getIv());
                    out = aes.decrypt(encryptedPayload);
                } catch (IOException e) {
                    log.error("An error occurred when fetching the AES password from the configs.", e);
                }
            } else {
                // TODO: RETURN DECRYPTED BYTES TO CLIENT IF NOTHING GOES WRONG
                //  ELSE THROW EXCEPTION / ALARM
                log.error("Checksums did not match.");
            }
        }
        log.error("Received no results for the collection- and file-ids: [{}, {}}", collectionID, fileID);
        return out;
    }

    private MockupResponse relayMessageToEncryptedPillar(String collectionID, String fileID) {
        // TODO: Implement : Relay message to Encrypted Pillar
        return response;
    }
}

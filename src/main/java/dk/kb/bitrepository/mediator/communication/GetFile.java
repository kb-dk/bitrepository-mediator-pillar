package dk.kb.bitrepository.mediator.communication;

import dk.kb.bitrepository.database.DatabaseData;
import dk.kb.bitrepository.database.DatabaseData.EncryptedParametersData;
import dk.kb.bitrepository.database.configs.ConfigurationHandler;
import dk.kb.bitrepository.crypto.CryptoStrategy;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.common.utils.ChecksumUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import static dk.kb.bitrepository.database.DatabaseCalls.select;
import static dk.kb.bitrepository.database.DatabaseConstants.*;
import static dk.kb.bitrepository.database.DatabaseData.FilesData;
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

        List<DatabaseData> filesResult = select(collectionID, fileID, FILES_TABLE);
        if (!filesResult.isEmpty()) {
            // Get old encrypted checksum, and generate new encrypted checksum
            FilesData firstFilesResult = (FilesData) filesResult.get(0);
            String newEncryptedChecksum = ChecksumUtils.generateChecksum(new ByteArrayInputStream(encryptedPayload), checksumSpecTYPE);
            String oldEncryptedChecksum = firstFilesResult.getEncryptedChecksum();

            // Compared checksum of file from encrypted pillar with the encrypted checksum in local table.
            if (newEncryptedChecksum.equals(oldEncryptedChecksum)) {
                // Get the used encryption parameters from the 'enc_parameters' table.
                List<DatabaseData> result = select(COLLECTION_ID, FILE_ID, ENC_PARAMS_TABLE);
                EncryptedParametersData firstEncParamResult = (EncryptedParametersData) result.get(0);

                // Decrypt the file using the parameters.
                try {
                    CryptoStrategy AES = initAES(config.getEncryptionPassword(), firstEncParamResult.getSalt(), firstEncParamResult.getIv());
                    out = AES.decrypt(encryptedPayload);
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

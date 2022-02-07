package dk.kb.bitrepository.mediator.communication;

import dk.kb.bitrepository.mediator.PillarContext;
import dk.kb.bitrepository.mediator.crypto.CryptoStrategy;
import dk.kb.bitrepository.mediator.database.DatabaseData.EncryptedParametersData;
import dk.kb.bitrepository.mediator.utils.configurations.Configurations;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.common.utils.ChecksumUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;

import static dk.kb.bitrepository.mediator.communication.MessageReceivedHandler.initAES;
import static dk.kb.bitrepository.mediator.database.DatabaseConstants.COLLECTION_ID;
import static dk.kb.bitrepository.mediator.database.DatabaseConstants.ENC_PARAMS_TABLE;
import static dk.kb.bitrepository.mediator.database.DatabaseConstants.FILES_TABLE;
import static dk.kb.bitrepository.mediator.database.DatabaseConstants.FILE_ID;
import static dk.kb.bitrepository.mediator.database.DatabaseData.FilesData;

public class GetFile extends MessageResult<byte[]> {
    private final MockupResponse response;
    private final String collectionID;
    private final String fileID;
    private final Configurations config;
    private final ChecksumSpecTYPE checksumSpecTYPE;
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public GetFile(PillarContext context, @NotNull MockupMessageObject message) {
        this.context = context;
        this.config = context.getConfigurations();
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

        FilesData filesResult = (FilesData) context.getDAO().select(collectionID, fileID, FILES_TABLE);
        if (filesResult != null) {
            // Get old encrypted checksum, and generate new encrypted checksum
            String newEncryptedChecksum = ChecksumUtils.generateChecksum(new ByteArrayInputStream(encryptedPayload), checksumSpecTYPE);
            String oldEncryptedChecksum = filesResult.getEncryptedChecksum();

            // Compared checksum of file from encrypted pillar with the encrypted checksum in local table.
            if (newEncryptedChecksum.equals(oldEncryptedChecksum)) {
                // Get the used encryption parameters from the 'enc_parameters' table.
                EncryptedParametersData encParams = (EncryptedParametersData) context.getDAO().select(COLLECTION_ID, FILE_ID, ENC_PARAMS_TABLE);

                // Decrypt the file using the parameters.
                CryptoStrategy aes = initAES(config.getCryptoConfig().getPassword(), encParams.getSalt(), encParams.getIv());
                out = aes.decrypt(encryptedPayload);
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

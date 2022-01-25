package dk.kb.bitrepository.mediator.communication;

import dk.kb.bitrepository.database.DatabaseData;
import dk.kb.bitrepository.database.DatabaseData.EncryptedParametersData;
import dk.kb.bitrepository.database.configs.ConfigurationHandler;
import dk.kb.bitrepository.utils.crypto.CryptoStrategy;
import org.apache.commons.io.FileExistsException;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.common.utils.ChecksumUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static dk.kb.bitrepository.database.DatabaseCalls.select;
import static dk.kb.bitrepository.database.DatabaseConstants.*;
import static dk.kb.bitrepository.database.DatabaseData.FilesData;
import static dk.kb.bitrepository.mediator.communication.MessageReceivedHandler.*;

public class GetFile extends MessageResult<byte[]> {
    private final MockupResponse response;
    private final String collectionID;
    private final String fileID;
    private final ConfigurationHandler config;
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public GetFile(ConfigurationHandler config, @NotNull MockupMessageObject message) {
        this.config = config;
        this.collectionID = message.getCollectionID();
        this.fileID = message.getFileID();
        this.response = message.getMockupResponse();
    }

    @Override
    public byte[] execute() {
        // TODO: Identify => Relay message to Encrypted Pillar
        //  and get response from Encrypted Pillar
        MockupResponse response = relayMessageToEncryptedPillar(collectionID, fileID);
        byte[] fileBytes = (response != null) ? response.getPayload() : new byte[0];
        byte[] out = new byte[0];

        if (fileBytes.length == 0) {
            log.error("Received no response from the encrypted pillar.");
            return out;
        }

        // Create the received file locally
        Path encryptedFilePath = Paths.get(getEncryptedFilePath(collectionID, fileID));
        try {
            writeBytesToFile(fileBytes, encryptedFilePath);
        } catch (FileExistsException e) {
            log.error("The file already exists.", e);
        }

        List<DatabaseData> filesResult = select(collectionID, fileID, FILES_TABLE);
        if (!filesResult.isEmpty()) {
            // Get old encrypted checksum, and generate new encrypted checksum
            FilesData firstFilesResult = (FilesData) filesResult.get(0);
            String newEncryptedChecksum = ChecksumUtils.generateChecksum(new File(String.valueOf(encryptedFilePath)), ChecksumType.MD5);
            String oldEncryptedChecksum = firstFilesResult.getEncryptedChecksum();

            // Compared checksum of file from encrypted pillar with the encrypted checksum in local table.
            Path decryptedFilePath = Paths.get(getDecryptedFilePath(collectionID, fileID));

            if (newEncryptedChecksum.equals(oldEncryptedChecksum)) {
                // Get the used encryption parameters from the 'enc_parameters' table.
                List<DatabaseData> result = select(COLLECTION_ID, FILE_ID, ENC_PARAMS_TABLE);
                EncryptedParametersData firstEncParamResult = (EncryptedParametersData) result.get(0);

                // Decrypt the file using the parameters.
                try {
                    CryptoStrategy AES = initAES(config.getEncryptionPassword(), firstEncParamResult.getSalt(), firstEncParamResult.getIv());
                    AES.decrypt(encryptedFilePath, decryptedFilePath);
                } catch (IOException e) {
                    log.error("An error occurred when fetching the AES password from the configs.", e);
                }
                // FIXME: RETURN DECRYPTED FILE TO CLIENT IF NOTHING GOES WRONG
                //  ELSE THROW EXCEPTION / ALARM
                try {
                    out = Files.readAllBytes(decryptedFilePath);
                } catch (IOException e) {
                    log.error("Error occurred when trying to read file.", e);
                }
            } else {
                // TODO: Handle this in a proper way
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

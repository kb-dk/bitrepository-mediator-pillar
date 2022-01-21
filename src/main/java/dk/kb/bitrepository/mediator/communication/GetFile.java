package dk.kb.bitrepository.mediator.communication;

import dk.kb.bitrepository.database.DatabaseData;
import dk.kb.bitrepository.database.DatabaseData.EncryptedParametersData;
import dk.kb.bitrepository.database.configs.ConfigurationHandler;
import dk.kb.bitrepository.utils.crypto.CryptoStrategy;
import org.apache.commons.io.FileExistsException;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.common.utils.ChecksumUtils;
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

    public GetFile(ConfigurationHandler config, MockupMessageObject message) {
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
        byte[] fileBytes = response.getFile();

        // Create the received file locally
        Path encryptedFilePath = Paths.get(getEncryptedFilePath(collectionID, fileID));
        try {
            writeBytesToFile(fileBytes, encryptedFilePath);
        } catch (FileExistsException e) {
            log.error("The file already exists.", e);
        }

        // Get old - and generate new encrypted checksum
        List<DatabaseData> filesResult = select(collectionID, fileID, FILES_TABLE);
        FilesData firstFilesResult = (FilesData) filesResult.get(0);
        String newEncryptedChecksum = ChecksumUtils.generateChecksum(new File(String.valueOf(encryptedFilePath)), ChecksumType.MD5);
        String oldEncryptedChecksum = firstFilesResult.getEncryptedChecksum();

        // Compared checksum of file from encrypted pillar with the encrypted checksum in local table.
        Path decryptedFilePath = Paths.get(getDecryptedFilePath(collectionID, fileID));
        byte[] out = new byte[0];
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
            // TODO: RETURN DECRYPTED FILE TO BitClient IF NOTHING GOES WRONG
            //  ELSE THROW EXCEPTION / ALARM
            try {
                out = Files.readAllBytes(decryptedFilePath);
            } catch (IOException e) {
                log.error("Error occurred when trying to read file.", e);
            }
        } else {
            // TODO: Handle this in a proper way
            log.error("Checksums did not match!");
        }
        return out;
    }

    private MockupResponse relayMessageToEncryptedPillar(String collectionID, String fileID) {
        // Relay message to Encrypted Pillar
        return response;
    }
}

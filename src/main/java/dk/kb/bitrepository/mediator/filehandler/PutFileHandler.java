package dk.kb.bitrepository.mediator.filehandler;

import dk.kb.bitrepository.mediator.PillarContext;
import dk.kb.bitrepository.mediator.crypto.AESCryptoStrategy;
import dk.kb.bitrepository.mediator.crypto.CryptoStrategy;
import dk.kb.bitrepository.mediator.database.DatabaseDAO;
import dk.kb.bitrepository.mediator.database.DatabaseData;
import dk.kb.bitrepository.mediator.utils.configurations.CryptoConfigurations;
import org.apache.commons.io.FileExistsException;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.time.Clock;
import java.time.OffsetDateTime;

import static dk.kb.bitrepository.mediator.filehandler.FileUtils.*;
import static dk.kb.bitrepository.mediator.utils.configurations.ConfigConstants.ENCRYPTED_FILES_PATH;
import static dk.kb.bitrepository.mediator.utils.configurations.ConfigConstants.UNENCRYPTED_FILES_PATH;
import static org.bitrepository.common.utils.ChecksumUtils.generateChecksum;

public class PutFileHandler {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final String fileID;
    private final String collectionID;
    private final byte[] fileData;
    private final ChecksumDataForFileTYPE checksumData;
    private final String cryptoPassword;
    private final DatabaseDAO dao;

    public PutFileHandler(String collectionID, String fileID, byte[] fileData, ChecksumDataForFileTYPE checksumData,
                          CryptoConfigurations crypto, DatabaseDAO dao) {
        this.collectionID = collectionID;
        this.fileID = fileID;
        this.fileData = fileData;
        this.checksumData = checksumData;
        this.cryptoPassword = crypto.getPassword();
        this.dao = dao;
    }

    public void performPutFile() throws FileExistsException {
        OffsetDateTime receivedTimestamp = OffsetDateTime.now(Clock.systemUTC());
        if (writeBytesToFile(fileData, UNENCRYPTED_FILES_PATH, collectionID, fileID)) {
            Path unencryptedFilePath = getFilePath(UNENCRYPTED_FILES_PATH, collectionID, fileID);
            byte[] unencryptedBytes = readBytesFromFile(unencryptedFilePath);

            String expectedChecksum = new String(checksumData.getChecksumValue());
            if (!compareChecksums(unencryptedBytes, expectedChecksum)) {
                log.error("Checksums did not match.");
                return;
            }
            //TODO: Init AES somewhere else?
            CryptoStrategy aes = new AESCryptoStrategy(cryptoPassword);
            if (writeBytesToFile(aes.encrypt(unencryptedBytes), ENCRYPTED_FILES_PATH, collectionID, fileID)) {
                updateLocalDatabase(receivedTimestamp, expectedChecksum, aes);
                // TODO: Implement JobDoneHandler (sending encrypted file to encrypted pillar) and state updates
            }
        }
    }

    private void updateLocalDatabase(OffsetDateTime receivedTimestamp, String expectedChecksum, CryptoStrategy aes) {
        OffsetDateTime encryptedTimestamp = OffsetDateTime.now(Clock.systemUTC());
        byte[] encryptedBytes = readBytesFromFile(getFilePath(ENCRYPTED_FILES_PATH, collectionID, fileID));

        if (compareChecksums(aes.decrypt(encryptedBytes), expectedChecksum)) {
            String encryptedChecksum = generateChecksum(new ByteArrayInputStream(encryptedBytes), checksumData.getChecksumSpec());
            OffsetDateTime checksumTimestamp = OffsetDateTime.now(Clock.systemUTC());

            dao.insertIntoEncParams(collectionID, fileID, aes.getSalt(), aes.getIV().getIV(), aes.getIterations());
            dao.insertIntoFiles(collectionID, fileID, receivedTimestamp, encryptedTimestamp, expectedChecksum,
                    encryptedChecksum, checksumTimestamp);
        }
    }

    private boolean compareChecksums(byte[] bytesFromFile, String expectedChecksum) {
        String newChecksum = generateChecksum(new ByteArrayInputStream(bytesFromFile), checksumData.getChecksumSpec());

        return newChecksum.equals(expectedChecksum);
    }
}

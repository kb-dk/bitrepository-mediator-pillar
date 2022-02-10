package dk.kb.bitrepository.mediator.filehandler;

import dk.kb.bitrepository.mediator.crypto.AESCryptoStrategy;
import dk.kb.bitrepository.mediator.crypto.CryptoStrategy;
import org.apache.commons.io.FileExistsException;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.util.Arrays;

import static dk.kb.bitrepository.mediator.filehandler.FileUtils.*;
import static dk.kb.bitrepository.mediator.utils.configurations.ConfigConstants.UNENCRYPTED_FILES_PATH;
import static org.bitrepository.common.utils.ChecksumUtils.generateChecksum;

public class PutFileHandler {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final String fileID;
    private final String collectionID;
    private final byte[] fileData;
    private final ChecksumDataForFileTYPE checksumData;

    public PutFileHandler(String fileID, String collectionID, byte[] fileData, ChecksumDataForFileTYPE checksumData) {
        this.fileID = fileID;
        this.collectionID = collectionID;
        this.fileData = fileData;
        this.checksumData = checksumData;
    }

    public void putFile() {
        try {
            writeBytesToFile(fileData, UNENCRYPTED_FILES_PATH, collectionID, fileID);
        } catch (FileExistsException e) {
            log.error("File {} already exists.", collectionID + "/" + fileID);
        }

        byte[] bytesFromFile = readBytesFromFile(getFilePath(UNENCRYPTED_FILES_PATH, collectionID, fileID));
        String newChecksum = generateChecksum(new ByteArrayInputStream(bytesFromFile), checksumData.getChecksumSpec());
        String expectedChecksum = Arrays.toString(checksumData.getChecksumValue());

        if (!newChecksum.equals(expectedChecksum)) {
            log.error("Checksums did not match.");
        } else {
            encryptAndWriteBytes();
        }
    }

    private void encryptAndWriteBytes() {
        //CryptoStrategy aes = new AESCryptoStrategy();
    }
}

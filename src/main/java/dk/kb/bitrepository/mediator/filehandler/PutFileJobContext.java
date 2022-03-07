package dk.kb.bitrepository.mediator.filehandler;

import dk.kb.bitrepository.mediator.crypto.CryptoStrategy;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.common.settings.Settings;

import java.net.URL;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collection;

public class PutFileJobContext extends JobContext {
    private final byte[] fileBytes;
    private final OffsetDateTime receivedTimestamp;

    public PutFileJobContext(String collectionID, String fileID, byte[] fileBytes,
                             OffsetDateTime receivedTimestamp,
                             ChecksumDataForFileTYPE checksumDataForFileTYPE,
                             Settings settings, URL urlForResult,
                             Collection<String> contributors, CryptoStrategy crypto) {
        super(collectionID, fileID, checksumDataForFileTYPE, settings, urlForResult, contributors, crypto);
        this.fileBytes = fileBytes;
        this.receivedTimestamp = receivedTimestamp;
    }

    public byte[] getFileBytes() {
        return fileBytes;
    }

    public OffsetDateTime getReceivedTimestamp() {
        return receivedTimestamp;
    }
}

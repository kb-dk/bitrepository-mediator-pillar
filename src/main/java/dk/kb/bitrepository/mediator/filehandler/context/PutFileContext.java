package dk.kb.bitrepository.mediator.filehandler.context;

import dk.kb.bitrepository.mediator.crypto.CryptoStrategy;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.common.settings.Settings;

import java.net.URL;
import java.util.Collection;

public class PutFileContext extends JobContext {
    private final byte[] fileBytes;

    public PutFileContext(String collectionID, String fileID, byte[] fileBytes,
                          ChecksumDataForFileTYPE checksumDataForFileTYPE,
                          Settings settings, URL urlForResult,
                          Collection<String> contributors, CryptoStrategy crypto) {
        super(collectionID, fileID, checksumDataForFileTYPE, settings, urlForResult, contributors, crypto);
        this.fileBytes = fileBytes;
    }

    public byte[] getFileBytes() {
        return fileBytes;
    }
}

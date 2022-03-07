package dk.kb.bitrepository.mediator.filehandler;

import dk.kb.bitrepository.mediator.crypto.CryptoStrategy;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.FilePart;
import org.bitrepository.common.settings.Settings;

import java.net.URL;
import java.util.Collection;

public class GetFileJobContext extends JobContext {
    private final FilePart filePart;

    public GetFileJobContext(String collectionID, String fileID, FilePart filePart,
                             ChecksumDataForFileTYPE checksumDataForFileTYPE,
                             Settings settings, URL urlForResult,
                             Collection<String> contributors, CryptoStrategy crypto) {
        super(collectionID, fileID, checksumDataForFileTYPE, settings, urlForResult, contributors, crypto);
        this.filePart = filePart;
    }

    public FilePart getFilePart() {
        return filePart;
    }
}

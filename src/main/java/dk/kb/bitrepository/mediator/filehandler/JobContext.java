package dk.kb.bitrepository.mediator.filehandler;

import dk.kb.bitrepository.mediator.crypto.CryptoStrategy;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.FilePart;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.FileExchange;

import java.net.URL;
import java.util.Collection;

public class JobContext {
    private final String collectionID;
    private final String fileID;
    private final FilePart filePart;
    private final ChecksumDataForFileTYPE checksumDataForFileTYPE;
    private final Settings settings;
    private final URL urlForResult;
    private final Collection<String> contributors;
    private final FileExchange fileExchange;
    private final CryptoStrategy crypto;
    private final byte[] fileBytes;

    public JobContext(String collectionID, String fileID, byte[] fileBytes, FilePart filePart,
                      ChecksumDataForFileTYPE checksumDataForFileTYPE, Settings settings, URL urlForResult, Collection<String> contributors,
                      CryptoStrategy crypto, FileExchange fileExchange) {
        ArgumentValidator.checkNotNull(collectionID, "Collection ID");
        ArgumentValidator.checkNotNull(settings, "Settings");
        ArgumentValidator.checkNotNull(crypto, "Crypto Strategy");
        ArgumentValidator.checkNotNullOrEmpty(contributors, "Contributors");
        this.collectionID = collectionID;
        this.fileID = fileID;
        this.fileBytes = fileBytes;
        this.filePart = filePart;
        this.checksumDataForFileTYPE = checksumDataForFileTYPE;
        this.settings = settings;
        this.urlForResult = urlForResult;
        this.contributors = contributors;
        // Crypto needs to be initialized with the IV and Salt that was used to encrypt the file (Get this from DAO) when a file is
        // queued that comes from either the local storage or a pillar.
        this.crypto = crypto;
        this.fileExchange = fileExchange;
    }

    public String getCollectionID() {
        return collectionID;
    }

    public String getFileID() {
        return fileID;
    }

    public byte[] getFileBytes() {
        return fileBytes;
    }

    public FilePart getFilePart() {
        return filePart;
    }

    public ChecksumDataForFileTYPE getChecksumDataForFileTYPE() {
        return checksumDataForFileTYPE;
    }

    public Settings getSettings() {
        return settings;
    }

    public URL getUrlForResult() {
        return urlForResult;
    }

    public Collection<String> getContributors() {
        return contributors;
    }

    public CryptoStrategy getCrypto() {
        return crypto;
    }

    public FileExchange getFileExchange() {
        return fileExchange;
    }
}

package dk.kb.bitrepository.mediator.filehandler.context;

import dk.kb.bitrepository.mediator.crypto.CryptoStrategy;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.common.settings.Settings;

import java.net.URL;
import java.util.Collection;

public class JobContext {
    private final String collectionID;
    private final String fileID;
    private final ChecksumDataForFileTYPE checksumDataForFileTYPE;
    private final Settings settings;
    private final URL urlForResult;
    private final Collection<String> contributors;
    private final CryptoStrategy crypto;

    public JobContext(String collectionID, String fileID, ChecksumDataForFileTYPE checksumDataForFileTYPE,
                      Settings settings, URL urlForResult, Collection<String> contributors, CryptoStrategy crypto) {
        this.collectionID = collectionID;
        this.fileID = fileID;
        this.checksumDataForFileTYPE = checksumDataForFileTYPE;
        this.settings = settings;
        this.urlForResult = urlForResult;
        this.contributors = contributors;
        // Crypto needs to be initialized with the IV and Salt that was used to encrypt the file (Get this from DAO) when a file is
        // queued that comes from either the local storage or a pillar.
        this.crypto = crypto;
    }

    public String getCollectionID() {
        return collectionID;
    }

    public String getFileID() {
        return fileID;
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
}

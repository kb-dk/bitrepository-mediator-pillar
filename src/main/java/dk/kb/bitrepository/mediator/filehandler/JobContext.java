package dk.kb.bitrepository.mediator.filehandler;

import dk.kb.bitrepository.mediator.crypto.CryptoStrategy;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.FilePart;
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

    public JobContext(String collectionID, String fileID, FilePart filePart, ChecksumDataForFileTYPE checksumDataForFileTYPE,
                      Settings settings, URL urlForResult, Collection<String> contributors,
                      CryptoStrategy crypto, FileExchange fileExchange) {
        this.collectionID = collectionID;
        this.fileID = fileID;
        this.filePart = filePart;
        this.checksumDataForFileTYPE = checksumDataForFileTYPE;
        this.settings = settings;
        this.urlForResult = urlForResult;
        this.contributors = contributors;
        this.crypto = crypto;
        this.fileExchange = fileExchange;
    }

    public String getCollectionID() {
        return collectionID;
    }

    public String getFileID() {
        return fileID;
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

package dk.kb.bitrepository.database;

import java.sql.Timestamp;
import java.time.OffsetDateTime;

public class FilesData extends DatabaseData {
    private String collectionID;
    private String fileID;
    private OffsetDateTime receivedTimestamp;
    private OffsetDateTime encryptedTimestamp;
    private String checksum;
    private String encryptedChecksum;
    private OffsetDateTime checksumTimestamp;

    @Override
    String getCollectionID() {
        return collectionID;
    }

    @Override
    void setCollectionID(String collectionID) {
        this.collectionID = collectionID;
    }

    @Override
    String getFileID() {
        return fileID;
    }

    @Override
    void setFileID(String fileID) {
        this.fileID = fileID;
    }

    public OffsetDateTime getReceivedTimestamp() {
        return receivedTimestamp;
    }

    public void setReceivedTimestamp(OffsetDateTime receivedTimestamp) {
        this.receivedTimestamp = receivedTimestamp;
    }

    public OffsetDateTime getEncryptedTimestamp() {
        return encryptedTimestamp;
    }

    public void setEncryptedTimestamp(OffsetDateTime encryptedTimestamp) {
        this.encryptedTimestamp = encryptedTimestamp;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public String getEncryptedChecksum() {
        return encryptedChecksum;
    }

    public void setEncryptedChecksum(String encryptedChecksum) {
        this.encryptedChecksum = encryptedChecksum;
    }

    public OffsetDateTime getChecksumTimestamp() {
        return checksumTimestamp;
    }

    public void setChecksumTimestamp(OffsetDateTime checksumTimestamp) {
        this.checksumTimestamp = checksumTimestamp;
    }
}

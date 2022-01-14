package dk.kb.bitrepository.database;

import java.sql.Timestamp;

public class FilesData extends DatabaseData {
    private String collectionID;
    private String fileID;
    private Timestamp receivedTimestamp;
    private Timestamp encryptedTimestamp;
    private String checksum;
    private String encChecksum;
    private Timestamp checksumTimestamp;

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

    public Timestamp getReceivedTimestamp() {
        return receivedTimestamp;
    }

    public void setReceivedTimestamp(Timestamp receivedTimestamp) {
        this.receivedTimestamp = receivedTimestamp;
    }

    public Timestamp getEncryptedTimestamp() {
        return encryptedTimestamp;
    }

    public void setEncryptedTimestamp(Timestamp encryptedTimestamp) {
        this.encryptedTimestamp = encryptedTimestamp;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public String getEncChecksum() {
        return encChecksum;
    }

    public void setEncChecksum(String encChecksum) {
        this.encChecksum = encChecksum;
    }

    public Timestamp getChecksumTimestamp() {
        return checksumTimestamp;
    }

    public void setChecksumTimestamp(Timestamp checksumTimestamp) {
        this.checksumTimestamp = checksumTimestamp;
    }
}

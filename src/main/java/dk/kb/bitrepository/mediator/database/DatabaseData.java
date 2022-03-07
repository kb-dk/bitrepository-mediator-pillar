package dk.kb.bitrepository.mediator.database;

import java.time.OffsetDateTime;

public abstract class DatabaseData {
    String collectionID;
    String fileID;
    String salt;
    byte[] iv;
    int iterations;

    OffsetDateTime receivedTimestamp;
    OffsetDateTime encryptedTimestamp;
    String checksum;
    String encryptedChecksum;
    OffsetDateTime checksumTimestamp;

    DatabaseData(String collectionID, String fileID, String salt, byte[] iv, int iterations) {
        this.collectionID = collectionID;
        this.fileID = fileID;
        this.salt = salt;
        this.iv = iv;
        this.iterations = iterations;
    }

    DatabaseData(String collectionID, String fileID, OffsetDateTime receivedTimestamp, OffsetDateTime encryptedTimestamp, String checksum,
                 String encryptedChecksum, OffsetDateTime checksumTimestamp) {
        this.collectionID = collectionID;
        this.fileID = fileID;
        this.receivedTimestamp = receivedTimestamp;
        this.encryptedTimestamp = encryptedTimestamp;
        this.checksum = checksum;
        this.encryptedChecksum = encryptedChecksum;
        this.checksumTimestamp = checksumTimestamp;
    }

    public String getCollectionID() {
        return collectionID;
    }

    public String getFileID() {
        return fileID;
    }

    public static class EncryptedParametersData extends DatabaseData {
        public EncryptedParametersData(String collectionID, String fileID, String salt, byte[] iv, int iterations) {
            super(collectionID, fileID, salt, iv, iterations);
        }

        public String getSalt() {
            return super.salt;
        }

        public byte[] getIv() {
            return super.iv;
        }

        public int getIterations() {
            return super.iterations;
        }
    }

    public static class FilesData extends DatabaseData {
        public FilesData(String collectionID, String fileID, OffsetDateTime receivedTimestamp, OffsetDateTime encryptedTimestamp,
                         String checksum, String encryptedChecksum, OffsetDateTime checksumTimestamp) {
            super(collectionID, fileID, receivedTimestamp, encryptedTimestamp, checksum, encryptedChecksum, checksumTimestamp);
        }

        public OffsetDateTime getReceivedTimestamp() {
            return super.receivedTimestamp;
        }

        public OffsetDateTime getEncryptedTimestamp() {
            return encryptedTimestamp;
        }

        public String getChecksum() {
            return super.checksum;
        }

        public String getEncryptedChecksum() {
            return super.encryptedChecksum;
        }

        public OffsetDateTime getChecksumTimestamp() {
            return checksumTimestamp;
        }
    }
}

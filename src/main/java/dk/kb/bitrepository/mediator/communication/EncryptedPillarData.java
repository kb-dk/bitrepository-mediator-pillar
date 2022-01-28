package dk.kb.bitrepository.mediator.communication;

import java.time.OffsetDateTime;

public class EncryptedPillarData {
    private final String collectionID;
    private final String fileID;
    private final String checksum;
    private final byte[] payload;
    private final OffsetDateTime checksumTimestamp;

    public EncryptedPillarData(String collectionID, String fileID, byte[] payload, String checksum, OffsetDateTime checksumTimestamp) {
        this.collectionID = collectionID;
        this.fileID = fileID;
        this.payload = payload;
        this.checksum = checksum;
        this.checksumTimestamp = checksumTimestamp;
    }

    public String getCollectionID() {
        return collectionID;
    }

    public String getFileID() {
        return fileID;
    }

    public byte[] getPayload() {
        return payload;
    }

    public String getChecksum() {
        return checksum;
    }

    public OffsetDateTime getChecksumTimestamp() {
        return checksumTimestamp;
    }
}

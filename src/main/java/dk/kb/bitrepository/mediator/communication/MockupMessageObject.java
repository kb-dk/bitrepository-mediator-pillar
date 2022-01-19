package dk.kb.bitrepository.mediator.communication;

public class MockupMessageObject {
    private final MockupMessageType type;
    private final String collectionID;
    private final String fileID;
    private final byte[] payload;

    public MockupMessageObject(MockupMessageType type, String collectionID, String fileID, byte[] payload) {
        this.type = type;
        this.payload = payload;
        this.collectionID = collectionID;
        this.fileID = fileID;
    }

    public byte[] getPayload() {
        return payload;
    }

    public MockupMessageType getType() {
        return type;
    }

    public String getCollectionID() {
        return collectionID;
    }

    public String getFileID() {
        return fileID;
    }
}

package dk.kb.bitrepository.database;

public abstract class DatabaseData {
    String collectionID = "";
    String fileID = "";

    abstract String getCollectionID();

    abstract void setCollectionID(String collectionID);

    abstract String getFileID();

    abstract void setFileID(String fileID);
}

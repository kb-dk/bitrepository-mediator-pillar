package dk.kb.bitrepository.database;

public class EncParametersData extends DatabaseData {
    private String collectionID;
    private String fileID;
    private String salt;
    private String iv;
    private String iterations;

    public EncParametersData() {
    }

    @Override
    public String getCollectionID() {
        return collectionID;
    }

    @Override
    public void setCollectionID(String collectionID) {
        this.collectionID = collectionID;
    }

    @Override
    public String getFileID() {
        return fileID;
    }

    @Override
    public void setFileID(String fileID) {
        this.fileID = fileID;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getIv() {
        return iv;
    }

    public void setIv(String iv) {
        this.iv = iv;
    }

    public String getIterations() {
        return iterations;
    }

    public void setIterations(String iterations) {
        this.iterations = iterations;
    }
}

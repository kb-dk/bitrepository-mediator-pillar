package dk.kb.bitrepository.mediator.communication;

public class MockupResponse {
    private static byte[] file = null;

    public MockupResponse() {
    }

    public MockupResponse(byte[] file) {
        MockupResponse.file = file;
    }

    public byte[] getFile() {
        return file;
    }
}

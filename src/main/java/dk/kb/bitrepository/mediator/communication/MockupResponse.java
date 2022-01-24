package dk.kb.bitrepository.mediator.communication;

public class MockupResponse {
    private static byte[] payload = null;

    public MockupResponse(byte[] payload) {
        MockupResponse.payload = payload;
    }

    public byte[] getPayload() {
        return payload;
    }
}

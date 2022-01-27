package dk.kb.bitrepository.mediator.communication;

import java.util.List;

public class MockupResponse {
    private static byte[] payload = null;
    private static List<EncryptedPillarData> encryptedPillarData = null;

    public MockupResponse(byte[] payload) {
        MockupResponse.payload = payload;
    }

    public MockupResponse(List<EncryptedPillarData> encryptedPillarData) {
        MockupResponse.encryptedPillarData = encryptedPillarData;
    }

    public byte[] getPayload() {
        return payload;
    }

    public List<EncryptedPillarData> getInfo() {
        return encryptedPillarData;
    }
}

package dk.kb.bitrepository.mediator.utils;

import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.protocol.ProtocolVersionLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RequestValidatorTest {
    private static IdentifyPillarsForGetFileRequest request;

    @BeforeEach
    public void setup() {
        request = new IdentifyPillarsForGetFileRequest();
        request.setMinVersion(ProtocolVersionLoader.loadProtocolVersion().getMinVersion());
        request.setVersion(ProtocolVersionLoader.loadProtocolVersion().getVersion());
        request.setFrom("testSomething");
        request.setCorrelationID("12345");
        request.setDestination("Testination");
        request.setReplyTo("Something");
        request.setFileID("test");
    }

    @Test
    public void testNegativeValidateCollectionID() {
        RequestValidator validator = new RequestValidator();
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            validator.validateCollectionIdIsSet(request);
        });
    }

    @Test
    public void testPositiveValidateCollectionID() {
        RequestValidator validator = new RequestValidator();
        request.setCollectionID("testCollection");
        Assertions.assertEquals("testCollection", request.getCollectionID());
        Assertions.assertDoesNotThrow(() -> {
            validator.validateCollectionIdIsSet(request);
        });
    }
}

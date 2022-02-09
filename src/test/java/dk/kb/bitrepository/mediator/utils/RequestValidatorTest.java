package dk.kb.bitrepository.mediator.utils;

import dk.kb.bitrepository.mediator.communication.exception.RequestHandlerException;
import dk.kb.bitrepository.mediator.database.DatabaseDAO;
import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.ProtocolVersionLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;

public class RequestValidatorTest {
    private GetFileRequest request;
    private static Settings conf;
    private static RequestValidator validator;
    private static String pillarID = "test-pillar";
    private static DatabaseDAO daoMock;

    @BeforeAll
    public static void setup() throws IOException {
        daoMock = Mockito.mock(DatabaseDAO.class);
        conf = TestUtils.loadRefPillarSettings(pillarID, "conf");
        validator = new RequestValidator(conf, daoMock);
    }

    @BeforeEach
    public void setupEach() {
        request = new GetFileRequest();
        request.setMinVersion(ProtocolVersionLoader.loadProtocolVersion().getMinVersion());
        request.setVersion(ProtocolVersionLoader.loadProtocolVersion().getVersion());
        request.setFrom("testSomething");
        request.setCorrelationID("12345");
        request.setDestination("Testination");
        request.setReplyTo("Something");
        request.setFileID("test");
    }

    @Test
    public void negativeValidateCollectionID() {
        // Mocking might be better
        request.setCollectionID(null);
        Assertions.assertFalse(request.isSetCollectionID());
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            validator.validateCollectionIdIsSet(request);
        });
    }

    @Test
    public void positiveValidateCollectionID() {
        request.setCollectionID("testCollection");
        Assertions.assertTrue(request.isSetCollectionID());
        Assertions.assertEquals("testCollection", request.getCollectionID());
        Assertions.assertDoesNotThrow(() -> {
            validator.validateCollectionIdIsSet(request);
        });
    }

    @Test
    public void validateBadPathFileID() {
        request.setFileID("../test.txt");
        Assertions.assertThrows(RequestHandlerException.class, () -> {
            validator.validateFileIDFormat(request.getFileID());
        });
    }

    @Test
    public void validateInvalidFileID() {
        request.setFileID("#bad|name.txt");
        Assertions.assertThrows(RequestHandlerException.class, () -> {
            validator.validateFileIDFormat(request.getFileID());
        });
    }

    @Test
    public void validateProperFileID() {
        request.setFileID("test.txt");
        Assertions.assertDoesNotThrow(() -> {
            validator.validateFileIDFormat(request.getFileID());
        });
    }

    @Test
    public void validateIncorrectPillarID() {
        request.setPillarID("my-pillar");
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            validator.validatePillarID(request.getPillarID());
        });
    }

    @Test
    public void validateCorrectPillarID() {
        request.setPillarID(pillarID);
        Assertions.assertDoesNotThrow(() -> {
            validator.validatePillarID(request.getPillarID());
        });
    }

    @Test
    public void validateFileExists() {
        request.setCollectionID("testCollection");
        Mockito.when(daoMock.hasFile(request.getFileID(), request.getCollectionID()))
                .thenReturn(true);
        Assertions.assertDoesNotThrow(() -> {
            validator.validateFileExists(request.getFileID(), request.getCollectionID());
        });
    }

    @Test
    public void validateFileDoesNotExist() {
        Mockito.when(daoMock.hasFile(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(false);
        Assertions.assertThrows(RequestHandlerException.class, () -> {
            validator.validateFileExists("non-existing-file.txt", "wrong-collection");
        });
    }
}

package dk.kb.bitrepository.mediator.communication.pillar;

import dk.kb.bitrepository.mediator.TestSettingsProvider;
import dk.kb.bitrepository.mediator.TestingSetup;
import dk.kb.bitrepository.mediator.pillaraccess.AccessPillarFactory;
import dk.kb.bitrepository.mediator.pillaraccess.GetFileConversation;
import org.bitrepository.client.conversation.mediator.ConversationMediator;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.security.DummySecurityManager;
import org.bitrepository.protocol.security.SecurityManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestGetFileConversation {
    private static Settings pillarSettings;
    private static ConversationMediator conversationMediator;
    private static final SecurityManager securityManager = createSecurityManager();
    private static Settings settingsForTestClient;

    @BeforeAll
    public static void setup() throws IOException {
        TestingSetup setup = new TestingSetup();
        pillarSettings = setup.getConfigurations().getRefPillarSettings();
        settingsForTestClient = TestSettingsProvider.reloadSettings("TestSuiteInitializer");
    }

    @Test
    @DisplayName("Test #AccessPillarFactory.createGetFileClient returns a GetFileConversation")
    public void verifyGetFileClientFromFactory() throws Exception {
        assertTrue(AccessPillarFactory.getInstance().createGetFileClient(
                        settingsForTestClient, securityManager, settingsForTestClient.getComponentID())
                        instanceof GetFileConversation,
                "The default GetFileClient from the Access factory should be of the type '" +
                        GetFileConversation.class.getName() + "'.");
    }

    @Test
    @DisplayName("Test #GetFileConversation")
    public void testIdentifyPillarGetFileRequest() {

    }

    private static SecurityManager createSecurityManager() {
        return new DummySecurityManager();
    }
}

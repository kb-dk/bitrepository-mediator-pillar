package dk.kb.bitrepository.mediator.pillaraccess;

import dk.kb.bitrepository.mediator.PillarMain;
import dk.kb.bitrepository.mediator.TestSettingsProvider;
import org.bitrepository.client.conversation.mediator.ConversationMediator;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.security.DummySecurityManager;
import org.bitrepository.protocol.security.SecurityManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestGetFileConversation {
    private static ConversationMediator conversationMediator;
    private static final SecurityManager securityManager = createSecurityManager();
    private static Settings settingsForTestClient;
    private static MessageBus messageBus;

    @BeforeAll
    public static void setup() {
        settingsForTestClient = TestSettingsProvider.reloadSettings("TestSuiteInitializer");
        PillarMain.main();
        messageBus = null;
    }

    @Test
    @DisplayName("Test #AccessPillarFactory.createGetFileClient returns a GetFileConversation")
    public void verifyGetFileClientFromFactory() {
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

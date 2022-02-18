package dk.kb.bitrepository.mediator.communication.pillar;

import dk.kb.bitrepository.mediator.TestingSetup;
import org.bitrepository.client.conversation.mediator.ConversationMediator;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.security.SecurityManager;
import org.jaccept.TestEventManager;
import org.jaccept.structure.ExtendedTestCase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class TestGetFileConversation extends ExtendedTestCase {
    private static Settings pillarSettings;
    private static ConversationMediator conversationMediator;
    private static SecurityManager securityManager;
    private static final TestEventManager testEventManager = TestEventManager.getInstance();

    @BeforeAll
    public static void setup() throws IOException {
        TestingSetup setup = new TestingSetup();
        pillarSettings = setup.getConfigurations().getRefPillarSettings();
    }

    @Test
    @DisplayName("Test #GetFileConversation")
    public void testIdentifyPillarGetFileRequest() {

    }
}

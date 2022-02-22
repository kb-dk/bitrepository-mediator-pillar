package dk.kb.bitrepository.mediator.pillaraccess;

import dk.kb.bitrepository.mediator.LocalActiveMQBroker;
import dk.kb.bitrepository.mediator.MessageBusConfigurationFactory;
import dk.kb.bitrepository.mediator.MessageBusWrapper;
import dk.kb.bitrepository.mediator.TestSettingsProvider;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.messagebus.MessageBusManager;
import org.bitrepository.protocol.messagebus.SimpleMessageBus;
import org.bitrepository.protocol.security.DummySecurityManager;
import org.bitrepository.protocol.security.SecurityManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.jms.JMSException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

//@Disabled("Disabled until MessageBus and SecurityManager works.")
public class GetFileConversationIT {
    private static final SecurityManager securityManager = createSecurityManager();
    private static Settings settingsForTestClient;
    private static LocalActiveMQBroker broker;
    private static MessageBus messageBus;

    @BeforeAll
    public static void setup() {
        //settingsForTestClient = TestSettingsProvider.reloadSettings("TestSuiteInitializer");
        settingsForTestClient = TestSettingsProvider.getSettings(GetFileConversationIT.class.getSimpleName());
        settingsForTestClient.getRepositorySettings().getProtocolSettings().setMessageBusConfiguration(
                MessageBusConfigurationFactory.createEmbeddedMessageBusConfiguration()
        );
        messageBus = new SimpleMessageBus();
        messageBus.setCollectionFilter(List.of());
        messageBus.setComponentFilter(List.of());
        setupMessageBus();
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

    private static void setupMessageBus() {
        if (broker == null) {
            broker = new LocalActiveMQBroker(settingsForTestClient.getMessageBusConfiguration());
            broker.start();
        }
        messageBus = new MessageBusWrapper(ProtocolComponentFactory.getInstance().getMessageBus(
                settingsForTestClient, securityManager));

    }

    private void teardownMessageBus() {
        MessageBusManager.clear();
        if (messageBus != null) {
            try {
                messageBus.close();
                messageBus = null;
            } catch (JMSException e) {
                throw new RuntimeException(e);
            }
        }

        if (broker != null) {
            try {
                broker.stop();
                broker = null;
            } catch (Exception e) {
                // No reason to pollute the test output with this
            }
        }
    }

}

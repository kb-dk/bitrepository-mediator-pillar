package dk.kb.bitrepository.mediator.pillaraccess;

import dk.kb.bitrepository.mediator.*;
import dk.kb.bitrepository.mediator.pillaraccess.clients.GetFileClient;
import org.bitrepository.commandline.eventhandler.CompleteEventAwaiter;
import org.bitrepository.commandline.eventhandler.GetFileEventHandler;
import org.bitrepository.commandline.output.DefaultOutputHandler;
import org.bitrepository.commandline.output.OutputHandler;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.FileExchange;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.messagebus.MessageBusManager;
import org.bitrepository.protocol.security.DummySecurityManager;
import org.bitrepository.protocol.security.SecurityManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.jms.JMSException;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import static dk.kb.bitrepository.mediator.database.DatabaseConstants.COLLECTION_ID;
import static dk.kb.bitrepository.mediator.database.DatabaseConstants.FILE_ID;
import static dk.kb.bitrepository.mediator.utils.configurations.ConfigConstants.ENCRYPTED_FILES_PATH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestGetFileConversation {
    private static final SecurityManager securityManager = createSecurityManager();
    private static Settings settings;
    private static LocalActiveMQBroker broker;
    private static MessageBus messageBus;

    @BeforeAll
    public static void setup() {
        settings = TestSettingsProvider.getSettings(TestGetFileConversation.class.getSimpleName());
        settings.getRepositorySettings().getProtocolSettings().setMessageBusConfiguration(
                MessageBusConfigurationFactory.createEmbeddedMessageBusConfiguration()
        );
        setupMessageBus();
    }

    @AfterAll
    public static void cleanup() {
        teardownMessageBus();
    }

    @Test
    @DisplayName("Test #AccessPillarFactory.createGetFileClient returns a GetFileConversation")
    public void verifyGetFileClientFromFactory() {
        assertTrue(AccessPillarFactory.getInstance().createGetFileClient(
                        settings, securityManager, settings.getComponentID())
                        instanceof GetFileConversation,
                "The default GetFileClient from the Access factory should be of the type '" +
                        GetFileConversation.class.getName() + "'.");
    }

    @Test
    @DisplayName("Test #GetFileConversation")
    public void testIdentifyPillarGetFileRequest() throws MalformedURLException {
        String auditTrailInformation = "AuditTrailInfo for getFileFromSpecificPillarTest";
        GetFileClient client = AccessPillarFactory.getInstance().createGetFileClient(settings, securityManager,
                settings.getComponentID());
        OutputHandler output = new DefaultOutputHandler(getClass());
        CompleteEventAwaiter eventHandler = new GetFileEventHandler(settings, output);
        FileExchange fileexchange = ProtocolComponentFactory.getInstance().getFileExchange(settings);
        URL fileURL = fileexchange.getURL(FILE_ID);
        MessageReceiver collectionReceiver = new MessageReceiver(settings.getCollectionDestination());


        client.getFileFromEncryptedPillar(COLLECTION_ID, FILE_ID, null, fileURL, eventHandler, auditTrailInformation);
        EncryptedPillarGetFileRequest receivedIdentifyRequestMessage =
                collectionReceiver.waitForMessage(EncryptedPillarGetFileRequest.class);
        assertEquals(receivedIdentifyRequestMessage.getContext().getCollectionID(), COLLECTION_ID);

        fileexchange.getFile(new File("src/test/" + ENCRYPTED_FILES_PATH + "/" + FILE_ID), fileURL.toExternalForm());
    }

    private static SecurityManager createSecurityManager() {
        return new DummySecurityManager();
    }

    private static void setupMessageBus() {
        if (broker == null) {
            broker = new LocalActiveMQBroker(settings.getMessageBusConfiguration());
            broker.start();
        }
        messageBus = new MessageBusWrapper(ProtocolComponentFactory.getInstance().getMessageBus(
                settings, securityManager));
    }

    private static void teardownMessageBus() {
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

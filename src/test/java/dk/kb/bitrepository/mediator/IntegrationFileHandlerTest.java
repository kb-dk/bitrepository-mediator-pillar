package dk.kb.bitrepository.mediator;

import dk.kb.bitrepository.mediator.pillaraccess.communication.LocalActiveMQBroker;
import dk.kb.bitrepository.mediator.pillaraccess.communication.MessageBusConfigurationFactory;
import dk.kb.bitrepository.mediator.pillaraccess.communication.MessageBusWrapper;
import org.bitrepository.client.conversation.mediator.CollectionBasedConversationMediator;
import org.bitrepository.client.conversation.mediator.ConversationMediatorManager;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.FileExchange;
import org.bitrepository.protocol.LocalFileExchange;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.messagebus.MessageBusManager;
import org.bitrepository.protocol.security.SecurityManager;

import javax.jms.JMSException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static dk.kb.bitrepository.mediator.TestingUtilities.createSecurityManager;
import static dk.kb.bitrepository.mediator.database.DatabaseConstants.FILE_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class IntegrationFileHandlerTest {
    protected static LocalActiveMQBroker broker;
    protected static MessageBus messageBus;
    protected static Settings settings;
    protected static String collectionID;
    protected static String BASE_FILE_EXCHANGE_DIR;
    protected static String encryptedPillarID;
    protected static String pillarDestinationId;
    protected static LocalFileExchange fileExchange;
    protected static URL fileURL;
    protected static SecurityManager securityManager;

    protected static void setupSettingsAndFileExchange() throws IOException {
        encryptedPillarID = "TestPillar1";
        settings = TestSettingsProvider.getSettings(encryptedPillarID);
        settings.getRepositorySettings().getProtocolSettings()
                .setMessageBusConfiguration(MessageBusConfigurationFactory.createEmbeddedMessageBusConfiguration());
        settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().clear();
        settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().add(encryptedPillarID);

        pillarDestinationId = settings.getContributorDestinationID();
        collectionID = settings.getCollections().get(0).getID();
        BASE_FILE_EXCHANGE_DIR = "target/fileExchange/" + collectionID + "/";
        Path dir = Paths.get(BASE_FILE_EXCHANGE_DIR);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        fileExchange = new LocalFileExchange(BASE_FILE_EXCHANGE_DIR);
        fileURL = fileExchange.getURL(FILE_ID);
        securityManager = createSecurityManager();
        MediatorComponentFactory.setSecurityManager(securityManager);
    }

    protected static void setupMessageBus(Settings settings, SecurityManager securityManager) {
        if (broker == null) {
            broker = new LocalActiveMQBroker(settings.getMessageBusConfiguration());
            broker.start();
        }
        messageBus = new MessageBusWrapper(ProtocolComponentFactory.getInstance().getMessageBus(settings, securityManager));
        CollectionBasedConversationMediator conversationMediator = new CollectionBasedConversationMediator(settings, securityManager);
        ConversationMediatorManager.injectCustomConversationMediator(conversationMediator);
    }

    protected static void teardownMessageBus() {
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

    protected void putFileLocally(FileExchange fileExchange) throws IOException {
        String testFileName = FILE_ID;
        String testFileLocation = "target/" + testFileName;
        String testFileContent = "lorem ipsum";
        File testFile = createTestFile(testFileLocation, testFileContent);

        File basedir = new File(BASE_FILE_EXCHANGE_DIR);
        URL expectedUrl = new URL("file:" + basedir.getAbsolutePath() + "/" + testFileName);

        URL fileExchangeUrl = fileExchange.putFile(testFile);
        assertEquals(fileExchangeUrl, expectedUrl);
    }

    private File createTestFile(String filename, String content) throws IOException {
        Files.write(Paths.get(filename), content.getBytes(StandardCharsets.UTF_8));
        File f = Paths.get(filename).toFile();
        f.deleteOnExit();
        return f;
    }
}

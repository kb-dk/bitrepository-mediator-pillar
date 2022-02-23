package dk.kb.bitrepository.mediator.pillaraccess;

import dk.kb.bitrepository.mediator.*;
import dk.kb.bitrepository.mediator.pillaraccess.clients.GetFileClient;
import org.bitrepository.client.conversation.mediator.CollectionBasedConversationMediator;
import org.bitrepository.client.conversation.mediator.ConversationMediator;
import org.bitrepository.commandline.eventhandler.CompleteEventAwaiter;
import org.bitrepository.commandline.eventhandler.GetFileEventHandler;
import org.bitrepository.commandline.output.DefaultOutputHandler;
import org.bitrepository.commandline.output.OutputHandler;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.FileExchange;
import org.bitrepository.protocol.LocalFileExchange;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.messagebus.MessageBusManager;
import org.bitrepository.protocol.security.DummySecurityManager;
import org.bitrepository.protocol.security.SecurityManager;
import org.bitrepository.settings.referencesettings.FileExchangeSettings;
import org.bitrepository.settings.referencesettings.ProtocolType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.jms.JMSException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static dk.kb.bitrepository.mediator.TestingUtilities.cleanupFiles;
import static dk.kb.bitrepository.mediator.database.DatabaseConstants.FILE_ID;
import static dk.kb.bitrepository.mediator.utils.configurations.ConfigConstants.ENCRYPTED_FILES_PATH;
import static dk.kb.bitrepository.mediator.utils.configurations.ConfigConstants.UNENCRYPTED_FILES_PATH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestGetFileConversation {
    private static final SecurityManager securityManager = createSecurityManager();
    private static TestingSetup setup;
    private static Settings settings;
    private static LocalActiveMQBroker broker;
    private static MessageBus messageBus;
    private static String collectionID;
    private static String BASE_FILE_EXCHANGE_DIR;


    @BeforeAll
    public static void setup() throws IOException {
        setup = new TestingSetup();
        settings = TestSettingsProvider.getSettings("TestPillar1");
        settings.getRepositorySettings().getProtocolSettings()
                .setMessageBusConfiguration(MessageBusConfigurationFactory.createEmbeddedMessageBusConfiguration());
        setupMessageBus();
        collectionID = settings.getCollections().get(0).getID();
        BASE_FILE_EXCHANGE_DIR = "target/fileExchange/" + collectionID + "/";
        Path dir = Paths.get(BASE_FILE_EXCHANGE_DIR);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
    }

    @AfterAll
    public static void cleanup() {
        teardownMessageBus();
        cleanupFiles("src/test/" + UNENCRYPTED_FILES_PATH);
        cleanupFiles("src/test/" + ENCRYPTED_FILES_PATH);
        cleanupFiles(BASE_FILE_EXCHANGE_DIR);
    }

    @Test
    @DisplayName("Test #AccessPillarFactory.createGetFileClient returns a GetFileConversation")
    public void verifyGetFileClientFromFactory() {
        assertTrue(AccessPillarFactory.getInstance()
                        .createGetFileClient(settings, securityManager, settings.getComponentID()) instanceof GetFileConversation,
                "The default GetFileClient from the Access factory should be of the type '" + GetFileConversation.class.getName() + "'.");
    }

    @Test
    @DisplayName("Test local fileExchange")
    public void testLocalFileExchange() throws IOException {
        FileExchange fileExchange = new LocalFileExchange(BASE_FILE_EXCHANGE_DIR);
        putFileLocally(fileExchange);

        URL fileURL = fileExchange.getURL(FILE_ID);
        fileExchange.getFile(new File("src/test/" + ENCRYPTED_FILES_PATH + "/" + FILE_ID), fileURL.toExternalForm());
        assertEquals("lorem ipsum",
                Files.readString(Path.of("src/test/" + ENCRYPTED_FILES_PATH + "/" + FILE_ID), Charset.defaultCharset()));

        File actualFile = new File(fileURL.getFile());
        assertTrue(actualFile.delete());
    }

    @Test
    @DisplayName("Test #GetFileConversation")
    public void testIdentifyPillarGetFileRequest() throws IOException {
        String encryptedPillarID = "TestPillar1";
        settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().clear();
        settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().add(encryptedPillarID);

        FileExchangeSettings fileExchangeSettings = settings.getReferenceSettings().getFileExchangeSettings();
        fileExchangeSettings.setPath(BASE_FILE_EXCHANGE_DIR);
        fileExchangeSettings.setProtocolType(ProtocolType.FILE);
        FileExchange fileExchange = new LocalFileExchange(BASE_FILE_EXCHANGE_DIR);
        putFileLocally(fileExchange);
        URL fileURL = fileExchange.getURL(FILE_ID);

        //HttpServerConfiguration httpServerConfiguration = new HttpServerConfiguration(fileExchangeSettings);
        //fileURL = httpServerConfiguration.getURL(FILE_ID);

        CollectionBasedConversationMediator conversationMediator = new CollectionBasedConversationMediator(settings, securityManager);

        GetFileClient client = createGetFileClient(conversationMediator);
        OutputHandler output = new DefaultOutputHandler(getClass());
        CompleteEventAwaiter eventHandler = new GetFileEventHandler(settings, output);

        MessageReceiverManager receiverManager = new MessageReceiverManager(messageBus);
        MessageReceiver pillarReceiver = new MessageReceiver(settings.getCollectionDestination());
        receiverManager.addReceiver(pillarReceiver);
        receiverManager.startListeners();

        client.getFileFromEncryptedPillar(collectionID, FILE_ID, null, fileURL, eventHandler,
                "AuditTrailInfo for getFileFromSpecificPillarTest");
        EncryptedPillarGetFileRequest receivedIdentifyRequestMessage = pillarReceiver.waitForMessage(EncryptedPillarGetFileRequest.class);
        assertEquals(receivedIdentifyRequestMessage.getContext().getCollectionID(), collectionID);
    }

    private void putFileLocally(FileExchange fileExchange) throws IOException {
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

    private GetFileClient createGetFileClient(ConversationMediator conversationMediator) {
        return new GetFileClientTestWrapper(new GetFileConversation(messageBus, conversationMediator, settings, settings.getComponentID()));
    }

    private static SecurityManager createSecurityManager() {
        return new DummySecurityManager();
    }

    private static void setupMessageBus() {
        if (broker == null) {
            broker = new LocalActiveMQBroker(settings.getMessageBusConfiguration());
            broker.start();
        }
        messageBus = new MessageBusWrapper(ProtocolComponentFactory.getInstance().getMessageBus(settings, securityManager));
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

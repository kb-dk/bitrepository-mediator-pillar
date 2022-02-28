package dk.kb.bitrepository.mediator;

import dk.kb.bitrepository.mediator.crypto.AESCryptoStrategy;
import dk.kb.bitrepository.mediator.pillaraccess.communication.*;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.client.conversation.mediator.CollectionBasedConversationMediator;
import org.bitrepository.client.conversation.mediator.ConversationMediatorManager;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.FileExchange;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.messagebus.MessageBusManager;
import org.bitrepository.protocol.security.SecurityManager;
import org.bitrepository.settings.referencesettings.ProtocolType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;

import javax.jms.JMSException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static dk.kb.bitrepository.mediator.TestingUtilities.*;
import static dk.kb.bitrepository.mediator.database.DatabaseConstants.FILE_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class IntegrationFileHandlerTest extends TestingDAO {
    protected static LocalActiveMQBroker broker;
    protected static MessageBus messageBus;
    protected static Settings settings;
    protected static String collectionID;
    protected static String BASE_FILE_EXCHANGE_DIR;
    protected static String encryptedPillarID;
    protected static String pillarDestinationId;
    protected static FileExchange fileExchange;
    protected static URL fileURL;
    protected static SecurityManager securityManager;
    protected static MessageReceiverManager receiverManager;
    protected static MessageReceiver collectionReceiver;
    protected static MessageReceiver pillarReceiver;
    protected static MessageReceiver clientReceiver;
    protected static ChecksumDataForFileTYPE checksumDataWithWrongChecksum;
    protected static String ENCRYPTED_FILES_PATH;
    protected static String UNENCRYPTED_FILES_PATH;

    @BeforeAll
    protected static void initSuite() throws IOException {
        initTestingDAO();
        setupSettingsEtc();
        setupMessageBus(settings, securityManager);
        setupMessageReceiverManager();
        crypto = new AESCryptoStrategy(cryptoConfigurations.getPassword());
        checksumDataWithWrongChecksum = loadIncorrectChecksumData();
        ENCRYPTED_FILES_PATH = configurations.getPillarConfig().getEncryptedFilesPath();
        UNENCRYPTED_FILES_PATH = configurations.getPillarConfig().getUnencryptedFilesPath();
    }

    @AfterEach
    protected void cleanUpAfterEach() {
        cleanupFiles(UNENCRYPTED_FILES_PATH);
        cleanupFiles(ENCRYPTED_FILES_PATH);
        cleanupFiles(BASE_FILE_EXCHANGE_DIR);
    }

    @AfterAll
    protected static void cleanUpSuite() {
        teardownMessageBus();
    }

    private static void setupSettingsEtc() throws IOException {
        encryptedPillarID = "TestPillar1";
        settings = TestSettingsProvider.getSettings(encryptedPillarID);
        settings.getRepositorySettings().getProtocolSettings()
                .setMessageBusConfiguration(MessageBusConfigurationFactory.createEmbeddedMessageBusConfiguration());
        settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().clear();
        settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().add(encryptedPillarID);

        collectionID = settings.getCollections().get(0).getID();
        pillarDestinationId = settings.getContributorDestinationID();
        BASE_FILE_EXCHANGE_DIR = "target/fileExchange/" + collectionID + "/";

        settings.getReferenceSettings().getFileExchangeSettings().setPath(BASE_FILE_EXCHANGE_DIR);
        settings.getReferenceSettings().getFileExchangeSettings().setProtocolType(ProtocolType.FILE);
        Path dir = Paths.get(BASE_FILE_EXCHANGE_DIR);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        fileExchange = ProtocolComponentFactory.getInstance().getFileExchange(settings);
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

    private static void setupMessageReceiverManager() {
        receiverManager = new MessageReceiverManager(messageBus);
        messageBus.setCollectionFilter(List.of());
        messageBus.setComponentFilter(List.of());

        collectionReceiver = new MessageReceiver(settings.getCollectionDestination());
        receiverManager.addReceiver(collectionReceiver);

        pillarReceiver = new MessageReceiver(pillarDestinationId);
        receiverManager.addReceiver(pillarReceiver);

        clientReceiver = new MessageReceiver(settings.getReceiverDestinationID());
        receiverManager.addReceiver(clientReceiver);

        receiverManager.startListeners();
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

package dk.kb.bitrepository.mediator;

import dk.kb.bitrepository.mediator.crypto.AESCryptoStrategy;
import dk.kb.bitrepository.mediator.pillaraccess.EmbeddedPillar;
import dk.kb.bitrepository.mediator.pillaraccess.communication.*;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.client.conversation.mediator.CollectionBasedConversationMediator;
import org.bitrepository.client.conversation.mediator.ConversationMediatorManager;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.pillar.store.checksumdatabase.ChecksumDAO;
import org.bitrepository.pillar.store.checksumdatabase.ChecksumDatabaseManager;
import org.bitrepository.protocol.FileExchange;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.messagebus.MessageBusManager;
import org.bitrepository.protocol.security.SecurityManager;
import org.bitrepository.service.database.DatabaseManager;
import org.bitrepository.settings.referencesettings.PillarType;
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
import static dk.kb.bitrepository.mediator.database.DatabaseConstants.COLLECTION_ID;
import static dk.kb.bitrepository.mediator.database.DatabaseConstants.FILE_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class IntegrationFileHandlerTest extends TestingDAO {
    protected static LocalActiveMQBroker broker;
    protected static MessageBus messageBus = null;
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
    protected static String PILLAR_FILE_DIR = "target/test/fileArchive/collection_id/fileDir/";
    protected static String componentID;
    protected static EmbeddedPillar embeddedPillar;
    protected static CollectionBasedConversationMediator conversationMediator;

    @BeforeAll
    protected static void initSuite() throws IOException {
        initTestingDAO();
        setupSettingsEtc();
        //TODO: Class<?> crypto = Class.forName("dk.kb.bitrepository.mediator.crypto.SpecificCrypto.class");
        crypto = new AESCryptoStrategy(cryptoConfigurations.getPassword());
        checksumDataWithWrongChecksum = loadIncorrectChecksumData();
        ENCRYPTED_FILES_PATH = configurations.getPillarConfig().getEncryptedFilesPath();
        UNENCRYPTED_FILES_PATH = configurations.getPillarConfig().getUnencryptedFilesPath();
        componentID = settings.getComponentID() + "-test-client";

        initImportantServices(false);
    }

    private static void initImportantServices(boolean useRealMessageBus) {
        setupMessageBus(settings, securityManager, useRealMessageBus);
        setupMessageReceiverManager();
    }

    @AfterEach
    protected void cleanUpAfterEach() {
        cleanupFiles(UNENCRYPTED_FILES_PATH);
        cleanupFiles(ENCRYPTED_FILES_PATH);
        cleanupFiles(BASE_FILE_EXCHANGE_DIR);
        cleanupFiles(PILLAR_FILE_DIR);
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
        settings.getReferenceSettings().getGeneralSettings().setReceiverDestinationIDFactoryClass(null);
        //settings.getReferenceSettings().getPillarSettings().setFileStoreClass("org.bitrepository.pillar.store.filearchive" +
        //".CollectionArchiveManager");

        collectionID = settings.getCollections().get(0).getID();
        pillarDestinationId = settings.getContributorDestinationID();
        BASE_FILE_EXCHANGE_DIR = "target/test/fileExchange/" + collectionID + "/";

        settings.getReferenceSettings().getFileExchangeSettings().setPath(BASE_FILE_EXCHANGE_DIR);
        settings.getReferenceSettings().getFileExchangeSettings().setProtocolType(ProtocolType.FILE);
        settings.getReferenceSettings().getPillarSettings().setPillarType(PillarType.FILE);
        Path dir = Paths.get(BASE_FILE_EXCHANGE_DIR);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        fileExchange = ProtocolComponentFactory.getInstance().getFileExchange(settings);
        fileURL = fileExchange.getURL(FILE_ID);
        securityManager = createSecurityManager();
        MediatorPillarComponentFactory.setSecurityManager(securityManager);
    }

    private static void setupMessageBus(Settings settings, SecurityManager securityManager, boolean useRealMessageBus) {
        if (broker == null) {
            broker = new LocalActiveMQBroker(settings.getMessageBusConfiguration());
            broker.start();
        }

        if (messageBus == null) {
            if (useRealMessageBus) {
                MessageBusManager.clear();
                messageBus = MessageBusManager.getMessageBus(settings, securityManager);
            } else {
                //messageBus = new SimpleMessageBus();
                //MessageBusManager.injectCustomMessageBus(MessageBusManager.DEFAULT_MESSAGE_BUS, messageBus);
                messageBus = new MessageBusWrapper(ProtocolComponentFactory.getInstance().getMessageBus(settings, securityManager));
            }
        }
        conversationMediator = new CollectionBasedConversationMediator(settings, securityManager);
        ConversationMediatorManager.injectCustomConversationMediator(conversationMediator);
    }

    protected static void startRealMessageBus() {
        teardownMessageBus();
        initImportantServices(true);
    }

    protected static void startEmbeddedPillar() {
        SettingsUtils.initialize(settings); // TODO: What does it do?
        embeddedPillar = EmbeddedPillar.createReferencePillar(settings);
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
            } catch (JMSException e) {
                throw new RuntimeException(e);
            }
            messageBus = null;
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

    protected static void stopEmbeddedPillar() {
        if (embeddedPillar != null) {
            embeddedPillar.shutdown();
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

    /**
     * May be used by by concrete tests for general verification when the test method has finished. Will only be run
     * if the test has passed (so far).
     */
    protected void afterMethodVerification() {
        receiverManager.checkNoMessagesRemainInReceivers();
    }

    /**
     * Purges all messages from the receivers.
     */
    protected void clearReceivers() {
        receiverManager.clearMessagesInReceivers();
    }

    protected static void resetPillarData(Path filePath, String parentDir) {
        if (Files.exists(filePath)) {
            DatabaseManager checksumDatabaseManager = new ChecksumDatabaseManager(settings);
            new ChecksumDAO(checksumDatabaseManager).deleteEntry(FILE_ID, COLLECTION_ID);
            cleanupFiles(parentDir);
        }
    }

}

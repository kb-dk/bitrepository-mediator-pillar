package dk.kb.bitrepository.mediator;

import dk.kb.util.yaml.YAML;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.SettingsProvider;
import org.bitrepository.common.settings.XMLFileSettingsLoader;
import org.bitrepository.protocol.activemq.ActiveMQMessageBus;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.security.BasicMessageAuthenticator;
import org.bitrepository.protocol.security.BasicMessageSigner;
import org.bitrepository.protocol.security.BasicOperationAuthorizor;
import org.bitrepository.protocol.security.MessageAuthenticator;
import org.bitrepository.protocol.security.MessageSigner;
import org.bitrepository.protocol.security.OperationAuthorizor;
import org.bitrepository.protocol.security.PermissionStore;
import org.bitrepository.protocol.security.SecurityManager;

import java.io.IOException;

public class MediatorComponentFactory {
    private static MediatorComponentFactory instance;

    private MediatorComponentFactory() {}

    public static MediatorComponentFactory getInstance() {
        if (instance == null) {
            synchronized (MediatorComponentFactory.class) {
                if (instance == null) {
                    instance = new MediatorComponentFactory();
                }
            }
        }
        return instance;
    }

    public MediatorPillar createPillar(String pathToConfiguration, String pathToKeyFile, String pillarID) throws IOException {
        Configuration configuration = loadConfiguration(pillarID, pathToConfiguration);
        SecurityManager securityManager = loadSecurityManager(pathToKeyFile, configuration.getPillarSpecificConfiguration());
        MessageBus messageBus = new ActiveMQMessageBus(configuration.getPillarSpecificConfiguration(), securityManager);

        return new MediatorPillar(messageBus, configuration);
    }

    private Configuration loadConfiguration(String pillarID, String pathToConfiguration) throws IOException {
        YAML mediatorConfig = new YAML(pathToConfiguration + "/mediatorConfig.yaml"); // TODO probably move
        SettingsProvider settingsProvider = new SettingsProvider(new XMLFileSettingsLoader(pathToConfiguration), pillarID);
        Settings settings = settingsProvider.getSettings();
        return new Configuration(mediatorConfig, settings);
    }

    /**
     * Instantiates the security manager based on the settings and the path to the key file.
     * @param pathToPrivateKeyFile The path to the key file.
     * @param settings The settings.
     * @return The security manager.
     */
    private static SecurityManager loadSecurityManager(String pathToPrivateKeyFile, Settings settings) {
        PermissionStore permissionStore = new PermissionStore();
        MessageAuthenticator authenticator = new BasicMessageAuthenticator(permissionStore);
        MessageSigner signer = new BasicMessageSigner();
        OperationAuthorizor authorizer = new BasicOperationAuthorizor(permissionStore);
        return new NoOpSecurityManager();
        /*return new BasicSecurityManager(settings.getRepositorySettings(), pathToPrivateKeyFile,
                authenticator, signer, authorizer, permissionStore,
                settings.getComponentID());*/
    }
}

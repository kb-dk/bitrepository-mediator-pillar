package dk.kb.bitrepository.mediator;

import dk.kb.bitrepository.mediator.communication.ResponseDispatcher;
import dk.kb.util.yaml.YAML;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.SettingsProvider;
import org.bitrepository.common.settings.XMLFileSettingsLoader;
import org.bitrepository.protocol.activemq.ActiveMQMessageBus;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.security.BasicMessageAuthenticator;
import org.bitrepository.protocol.security.BasicMessageSigner;
import org.bitrepository.protocol.security.BasicOperationAuthorizor;
import org.bitrepository.protocol.security.BasicSecurityManager;
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

    public MediatorPillar createPillar(String pathToConfiguration, String pathToKeyFile) throws IOException {
        MediatorConfiguration configuration = loadConfiguration(pathToConfiguration);
        SecurityManager securityManager = loadSecurityManager(pathToKeyFile, configuration);
        MessageBus messageBus = new ActiveMQMessageBus(configuration.getPillarSettings(), securityManager);
        ResponseDispatcher responseDispatcher = new ResponseDispatcher(configuration, messageBus);
        PillarContext pillarContext = new PillarContext(configuration, messageBus, responseDispatcher);

        return new MediatorPillar(configuration, pillarContext, messageBus);
    }

    private MediatorConfiguration loadConfiguration(String pathToConfiguration) throws IOException {
        YAML mediatorConfig = new YAML(pathToConfiguration + "/mediatorConfig.yaml");
        String pillarID = mediatorConfig.getString(ConfigConstants.PILLAR_ID);
        SettingsProvider settingsProvider = new SettingsProvider(new XMLFileSettingsLoader(pathToConfiguration), pillarID);
        Settings settings = settingsProvider.getSettings();
        return new MediatorConfiguration(mediatorConfig, settings);
    }

    /**
     * Instantiates the security manager based on the configuration and the path to the key file.
     * @param pathToPrivateKeyFile The path to the key file.
     * @param configuration The configuration.
     * @return The security manager.
     */
    private SecurityManager loadSecurityManager(String pathToPrivateKeyFile, MediatorConfiguration configuration) {
        PermissionStore permissionStore = new PermissionStore();
        MessageAuthenticator authenticator = new BasicMessageAuthenticator(permissionStore);
        MessageSigner signer = new BasicMessageSigner();
        OperationAuthorizor authorizer = new BasicOperationAuthorizor(permissionStore);
        return new BasicSecurityManager(configuration.getRepositorySettings(), pathToPrivateKeyFile,
                authenticator, signer, authorizer, permissionStore,
                configuration.getMediatorPillarID());
    }
}

package dk.kb.bitrepository.mediator;

import dk.kb.bitrepository.mediator.communication.ResponseDispatcher;
import dk.kb.bitrepository.mediator.utils.configurations.ConfigurationHandler;
import dk.kb.bitrepository.mediator.utils.configurations.Configurations;
import dk.kb.bitrepository.mediator.utils.configurations.PillarSettings;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.SettingsProvider;
import org.bitrepository.common.settings.XMLFileSettingsLoader;
import org.bitrepository.protocol.activemq.ActiveMQMessageBus;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.security.SecurityManager;
import org.bitrepository.protocol.security.*;

import java.io.IOException;

public class MediatorComponentFactory {
    private static MediatorComponentFactory instance;

    private MediatorComponentFactory() {
    }

    public static MediatorComponentFactory getInstance() {
        // FIXME: double-checked locking
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
        new ConfigurationHandler(pathToConfiguration + "/mediatorConfig.yaml");
        Configurations configs = ConfigurationHandler.getConfigurations();
        PillarSettings pillarSettings = loadPillarSettings(pillarID, pathToConfiguration);

        SecurityManager securityManager = loadSecurityManager(pathToKeyFile, pillarSettings);
        MessageBus messageBus = new ActiveMQMessageBus(pillarSettings.getPillarSettings(), securityManager);
        ResponseDispatcher responseDispatcher = new ResponseDispatcher(
                pillarSettings,
                configs.getPillarConfig().getPrivateMessageDestination(),
                messageBus);
        PillarContext pillarContext = new PillarContext(pillarSettings, messageBus, responseDispatcher);

        return new MediatorPillar(pillarSettings, pillarContext, configs.getPillarConfig(), messageBus);
    }

    private PillarSettings loadPillarSettings(String pillarID, String pathToConfiguration) {
        SettingsProvider settingsProvider = new SettingsProvider(new XMLFileSettingsLoader(pathToConfiguration), pillarID);
        Settings settings = settingsProvider.getSettings();
        return new PillarSettings(settings);
    }

    /**
     * Instantiates the security manager based on the configuration and the path to the key file.
     *
     * @param pathToPrivateKeyFile The path to the key file.
     * @param configuration        The configuration.
     * @return The security manager.
     */
    private SecurityManager loadSecurityManager(String pathToPrivateKeyFile, PillarSettings configuration) {
        PermissionStore permissionStore = new PermissionStore();
        MessageAuthenticator authenticator = new BasicMessageAuthenticator(permissionStore);
        MessageSigner signer = new BasicMessageSigner();
        OperationAuthorizor authorizer = new BasicOperationAuthorizor(permissionStore);
        return new BasicSecurityManager(configuration.getRepositorySettings(), pathToPrivateKeyFile,
                authenticator, signer, authorizer, permissionStore,
                configuration.getComponentID());
    }
}

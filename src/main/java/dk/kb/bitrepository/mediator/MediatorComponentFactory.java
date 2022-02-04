package dk.kb.bitrepository.mediator;

import dk.kb.bitrepository.mediator.communication.ResponseDispatcher;
import dk.kb.bitrepository.mediator.utils.configurations.ConfigurationsProvider;
import dk.kb.bitrepository.mediator.utils.configurations.Configurations;
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
        Configurations configs = loadMediatorConfigurations(pillarID, pathToConfiguration);
        Settings refPillarSettings = configs.getRefPillarSettings();

        SecurityManager securityManager = loadSecurityManager(pathToKeyFile, refPillarSettings);
        MessageBus messageBus = new ActiveMQMessageBus(refPillarSettings, securityManager);
        ResponseDispatcher responseDispatcher = new ResponseDispatcher(
                refPillarSettings,
                configs.getPillarConfig().getPrivateMessageDestination(),
                messageBus);
        PillarContext pillarContext = new PillarContext(refPillarSettings, messageBus, responseDispatcher);

        return new MediatorPillar(refPillarSettings, pillarContext, configs.getPillarConfig(), messageBus);
    }

    private Configurations loadMediatorConfigurations(String pillarID, String pathToConfiguration) throws IOException {
        SettingsProvider settingsProvider = new SettingsProvider(new XMLFileSettingsLoader(pathToConfiguration), pillarID);
        Settings refPillarSettings = settingsProvider.getSettings();
        ConfigurationsProvider configProvider = new ConfigurationsProvider(pathToConfiguration + "/mediatorConfig.yaml");
        Configurations configs = configProvider.getConfigurations();
        configs.setRefPillarSettings(refPillarSettings);
        return configs;
    }

    /**
     * Instantiates the security manager based on the configuration and the path to the key file.
     *
     * @param pathToPrivateKeyFile The path to the key file.
     * @param refPillarSettings    The configuration.
     * @return The security manager.
     */
    private SecurityManager loadSecurityManager(String pathToPrivateKeyFile, Settings refPillarSettings) {
        PermissionStore permissionStore = new PermissionStore();
        MessageAuthenticator authenticator = new BasicMessageAuthenticator(permissionStore);
        MessageSigner signer = new BasicMessageSigner();
        OperationAuthorizor authorizer = new BasicOperationAuthorizor(permissionStore);
        return new BasicSecurityManager(refPillarSettings.getRepositorySettings(), pathToPrivateKeyFile,
                authenticator, signer, authorizer, permissionStore,
                refPillarSettings.getComponentID());
    }
}

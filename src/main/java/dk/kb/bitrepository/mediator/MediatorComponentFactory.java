package dk.kb.bitrepository.mediator;

import dk.kb.bitrepository.mediator.communication.ResponseDispatcher;
import dk.kb.bitrepository.mediator.database.DatabaseConnectionManager;
import dk.kb.bitrepository.mediator.database.DatabaseDAO;
import dk.kb.bitrepository.mediator.utils.configurations.Configurations;
import dk.kb.bitrepository.mediator.utils.configurations.ConfigurationsLoader;
import dk.kb.bitrepository.mediator.utils.configurations.DatabaseConfigurations;
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
    private static SecurityManager securityManager = null;

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
        Configurations configs = loadMediatorConfigurations(pathToConfiguration);
        Settings refPillarSettings = configs.getRefPillarSettings();

        securityManager = loadSecurityManager(pathToKeyFile, refPillarSettings);
        MessageBus messageBus = new ActiveMQMessageBus(refPillarSettings, securityManager);
        ResponseDispatcher responseDispatcher = new ResponseDispatcher(
                refPillarSettings,
                configs.getPillarConfig().getPrivateMessageDestination(),
                messageBus);
        DatabaseDAO dao = getDAO(configs.getDatabaseConfig());
        PillarContext pillarContext = new PillarContext(configs, messageBus, responseDispatcher, dao);

        return new MediatorPillar(refPillarSettings, pillarContext, configs.getPillarConfig(), messageBus);
    }

    public static Configurations loadMediatorConfigurations(String pathToConfiguration) throws IOException {
        ConfigurationsLoader configLoader = new ConfigurationsLoader(pathToConfiguration + "/mediatorConfig.yaml");
        Configurations configs = configLoader.getConfigurations();
        String mediatorPillarID = configs.getPillarConfig().getMediatorPillarID();
        SettingsProvider settingsProvider = new SettingsProvider(new XMLFileSettingsLoader(pathToConfiguration), mediatorPillarID);
        Settings refPillarSettings = settingsProvider.getSettings();
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

    public static DatabaseDAO getDAO(DatabaseConfigurations dbConfig) {
        DatabaseConnectionManager connectionManager = new DatabaseConnectionManager(dbConfig);
        return new DatabaseDAO(connectionManager);
    }

    public static SecurityManager getSecurityManager() {
        return securityManager;
    }

    public static void setSecurityManager(SecurityManager securityManager) {
        MediatorComponentFactory.securityManager = securityManager;
    }
}

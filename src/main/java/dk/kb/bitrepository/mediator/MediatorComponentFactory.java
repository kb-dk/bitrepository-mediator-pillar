package dk.kb.bitrepository.mediator;

import dk.kb.bitrepository.mediator.communication.ResponseDispatcher;
import dk.kb.bitrepository.mediator.database.DatabaseConnectionManager;
import dk.kb.bitrepository.mediator.database.DatabaseDAO;
import dk.kb.bitrepository.mediator.utils.configurations.ConfigurationsLoader;
import dk.kb.bitrepository.mediator.utils.configurations.Configurations;
import dk.kb.bitrepository.mediator.utils.configurations.DatabaseConfigurations;
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

/**
 * Component factory for the mediator pillar.
 */
public class MediatorComponentFactory {
    private static MediatorComponentFactory instance;

    /**
     * Singleton so private constructor.
     */
    private MediatorComponentFactory() {}

    /**
     * Get the instance of this singleton.
     *
     * @return The MediatorComponentFactory instance.
     */
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

    /**
     * Creates a pillar from the configurations located at the provided configuration paths.
     *
     * @param pathToConfiguration Path to the directory containing the mediator pillar settings,
     *                            ReferenceSettings and RepositorySettings
     * @param pathToKeyFile Path to private key
     *                      - optional if required authorization/authentication is disabled in RepositorySettings
     * @return A new mediator pillar
     * @throws IOException If something fails when loading configurations.
     */
    public MediatorPillar createPillar(String pathToConfiguration, String pathToKeyFile) throws IOException {
        Configurations configs = loadMediatorConfigurations(pathToConfiguration);
        Settings refPillarSettings = configs.getRefPillarSettings();

        SecurityManager securityManager = loadSecurityManager(pathToKeyFile, refPillarSettings);
        MessageBus messageBus = new ActiveMQMessageBus(refPillarSettings, securityManager);
        ResponseDispatcher responseDispatcher = new ResponseDispatcher(
                messageBus, refPillarSettings,
                configs.getPillarConfig().getPrivateMessageDestination()
        );
        DatabaseDAO dao = getDAO(configs.getDatabaseConfig());
        PillarContext pillarContext = new PillarContext(configs, messageBus, responseDispatcher, dao);

        return new MediatorPillar(refPillarSettings, pillarContext, messageBus);
    }

    /**
     * Load a Configurations instance from the configuration files located at the provided configuration path.
     *
     * @param pathToConfiguration Path to directory containing configuration files.
     * @return A new Configurations instance.
     * @throws IOException If the Configurations can not be loaded from the provided path.
     */
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

    /**
     * Instantiates a new DAO from the provided database configurations.
     *
     * @param dbConfig The database configurations.
     * @return A new DAO to make database operations on.
     */
    public static DatabaseDAO getDAO(DatabaseConfigurations dbConfig) {
        DatabaseConnectionManager connectionManager = new DatabaseConnectionManager(dbConfig);
        return new DatabaseDAO(connectionManager);
    }
}

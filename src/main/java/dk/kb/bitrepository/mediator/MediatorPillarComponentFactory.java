package dk.kb.bitrepository.mediator;

import dk.kb.bitrepository.mediator.communication.ResponseDispatcher;
import dk.kb.bitrepository.mediator.database.DatabaseConnectionManager;
import dk.kb.bitrepository.mediator.database.DatabaseDAO;
import dk.kb.bitrepository.mediator.utils.configurations.Configurations;
import dk.kb.bitrepository.mediator.utils.configurations.ConfigurationsLoader;
import dk.kb.bitrepository.mediator.utils.configurations.DatabaseConfigurations;
import dk.kb.bitrepository.mediator.utils.configurations.PillarConfigurations;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.SettingsProvider;
import org.bitrepository.common.settings.XMLFileSettingsLoader;
import org.bitrepository.protocol.FileExchange;
import org.bitrepository.protocol.LocalFileExchange;
import org.bitrepository.protocol.activemq.ActiveMQMessageBus;
import org.bitrepository.protocol.http.HttpFileExchange;
import org.bitrepository.protocol.http.HttpsFileExchange;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.security.SecurityManager;
import org.bitrepository.protocol.security.*;
import org.bitrepository.settings.referencesettings.ProtocolType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class MediatorPillarComponentFactory {
    private static final Logger log = LoggerFactory.getLogger(MediatorPillarComponentFactory.class);
    private static SecurityManager securityManager = null;
    private static FileExchange fileExchange = null;
    private static PillarConfigurations pillarConfigurations = null;
    private static Configurations configs = null;

    private MediatorPillarComponentFactory() {}

    public static MediatorPillarComponentFactory getInstance() {
        return InstanceHolder.instance;
    }

    public MediatorPillar createPillar(String pathToConfiguration, String pathToKeyFile) {
        configs = getMediatorConfigurations(pathToConfiguration);
        Settings refPillarSettings = configs.getRefPillarSettings();
        securityManager = loadSecurityManager(pathToKeyFile, refPillarSettings);
        MessageBus messageBus = new ActiveMQMessageBus(refPillarSettings, securityManager);
        ResponseDispatcher responseDispatcher = new ResponseDispatcher(refPillarSettings, messageBus);
        DatabaseDAO dao = getDAO(configs.getDatabaseConfig());
        PillarContext pillarContext = new PillarContext(configs, messageBus, responseDispatcher, dao);

        return new MediatorPillar(refPillarSettings, pillarContext, configs.getPillarConfig(), messageBus);
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
        OperationAuthorizer authorizer = new BasicOperationAuthorizer(permissionStore);
        return new BasicSecurityManager(refPillarSettings.getRepositorySettings(), pathToPrivateKeyFile, authenticator, signer, authorizer,
                permissionStore, refPillarSettings.getComponentID());
    }

    /**
     * Instantiates the mediator configurations based on the path to the configurations.
     *
     * @param pathToConfiguration The path to the configurations.
     * @return The configurations object.
     * @throws IOException Throws an exception if the file could not be read or found.
     */
    private static Configurations loadMediatorConfigurations(String pathToConfiguration) throws IOException {
        ConfigurationsLoader configLoader = new ConfigurationsLoader(pathToConfiguration + "/mediatorConfig.yaml");
        configs = configLoader.getConfigurations();
        pillarConfigurations = configs.getPillarConfig();

        String mediatorPillarID = configs.getPillarConfig().getMediatorPillarID();
        SettingsProvider settingsProvider = new SettingsProvider(new XMLFileSettingsLoader(pathToConfiguration), mediatorPillarID);
        Settings refPillarSettings = settingsProvider.getSettings();
        configs.setRefPillarSettings(refPillarSettings);

        return configs;
    }

    public static Configurations getMediatorConfigurations(String pathToConfiguration) {
        if (configs == null) {
            try {
                configs = loadMediatorConfigurations(pathToConfiguration);
            } catch (IOException e) {
                log.error("Not readable configurations found at the path {}", pathToConfiguration);
            }
        }
        return getMediatorConfigurations();
    }

    public static Configurations getMediatorConfigurations() {
        if (configs == null) {
            log.error("Mediator configurations have not been setup yet.");
        }
        return configs;
    }

    public static DatabaseDAO getDAO(DatabaseConfigurations dbConfig) {
        DatabaseConnectionManager connectionManager = new DatabaseConnectionManager(dbConfig);
        return new DatabaseDAO(connectionManager);
    }

    public static DatabaseDAO getDAO() {
        return getDAO(configs.getDatabaseConfig());
    }

    public static SecurityManager getSecurityManager() {
        if (securityManager == null) {
            log.error("Security manager has not been initialized yet.");
        }
        return securityManager;
    }

    public static void setSecurityManager(SecurityManager securityManager) {
        MediatorPillarComponentFactory.securityManager = securityManager;
    }

    public static PillarConfigurations getPillarConfigurations(String pathToConfiguration) {
        if (pillarConfigurations == null) {
            getMediatorConfigurations(pathToConfiguration);
        }
        return getPillarConfigurations();
    }

    public static PillarConfigurations getPillarConfigurations() {
        if (pillarConfigurations == null) {
            log.error("Pillar configurations has not been initialized yet.");
        }
        return pillarConfigurations;
    }

    /**
     * Either returns an existing file exchange instance, or creates one appropriate to the given settings.
     *
     * @param settings The settings which includes the file exchange settings.
     * @return A file exchange object.
     */
    public FileExchange getFileExchange(Settings settings) {
        if (fileExchange == null) {
            if ((settings.getReferenceSettings().getFileExchangeSettings() != null)) {
                ProtocolType protocolType = settings.getReferenceSettings().getFileExchangeSettings().getProtocolType();
                if (protocolType == ProtocolType.HTTP) {
                    fileExchange = new HttpFileExchange(settings);
                } else if (protocolType == ProtocolType.HTTPS) {
                    fileExchange = new HttpsFileExchange(settings);
                } else if (protocolType == ProtocolType.FILE) {
                    fileExchange = new LocalFileExchange(settings.getReferenceSettings().getFileExchangeSettings().getPath());
                }
            } else {
                fileExchange = new HttpFileExchange(settings);
            }
        }
        return fileExchange;
    }

    private static final class InstanceHolder {
        private static final MediatorPillarComponentFactory instance = new MediatorPillarComponentFactory();
    }
}

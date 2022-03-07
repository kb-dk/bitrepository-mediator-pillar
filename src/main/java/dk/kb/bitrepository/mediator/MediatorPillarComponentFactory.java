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

import java.io.IOException;

public class MediatorPillarComponentFactory {
    private static SecurityManager securityManager = null;
    private static FileExchange fileExchange = null;
    private static PillarConfigurations pillarConfigurations;
    private static Configurations configs;

    private MediatorPillarComponentFactory() {}

    public static MediatorPillarComponentFactory getInstance() {
        return InstanceHolder.instance;
    }

    public static Configurations loadMediatorConfigurations(String pathToConfiguration) throws IOException {
        ConfigurationsLoader configLoader = new ConfigurationsLoader(pathToConfiguration + "/mediatorConfig.yaml");
        configs = configLoader.getConfigurations();
        pillarConfigurations = configs.getPillarConfig();

        String mediatorPillarID = configs.getPillarConfig().getMediatorPillarID();
        SettingsProvider settingsProvider = new SettingsProvider(new XMLFileSettingsLoader(pathToConfiguration), mediatorPillarID);
        Settings refPillarSettings = settingsProvider.getSettings();
        configs.setRefPillarSettings(refPillarSettings);

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
        return securityManager;
    }

    public static void setSecurityManager(SecurityManager securityManager) {
        MediatorPillarComponentFactory.securityManager = securityManager;
    }

    public static PillarConfigurations getPillarConfigurations() {
        return pillarConfigurations;
    }

    public MediatorPillar createPillar(String pathToConfiguration, String pathToKeyFile) throws IOException {
        configs = loadMediatorConfigurations(pathToConfiguration);
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

    public FileExchange getFileExchange(Settings settings) {
        if (fileExchange == null) {
            if ((settings.getReferenceSettings().getFileExchangeSettings() != null)) {
                ProtocolType protocolType = settings.getReferenceSettings().getFileExchangeSettings().getProtocolType();
                if (protocolType == ProtocolType.HTTP) {
                    fileExchange = new HttpFileExchange(settings);
                } else if (protocolType == ProtocolType.HTTPS) {
                    fileExchange = new HttpsFileExchange(settings);
                } else if (protocolType == ProtocolType.FILE) {
                    fileExchange = new LocalFileExchange(
                            settings.getReferenceSettings().getFileExchangeSettings().getPath());
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

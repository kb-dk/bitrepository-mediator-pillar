package dk.kb.bitrepository.mediator.utils.configurations;

import dk.kb.bitrepository.mediator.MediatorComponentFactory;

public class ConfigConstants {
    public static final String CONFIGS_PATH = "src/main/resources/conf/mediatorConfigs.yml";
    public static final String PRIVATE_KEY_FILE = "org.bitrepository.audit-trail-service.privateKeyFile";
    public static final String ENCRYPTED_FILES_PATH = MediatorComponentFactory.getPillarConfigurations().getEncryptedFilesPath();
    public static final String UNENCRYPTED_FILES_PATH = MediatorComponentFactory.getPillarConfigurations().getUnencryptedFilesPath();
}

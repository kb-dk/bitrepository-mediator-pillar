package dk.kb.bitrepository.mediator.messagehandling;

import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.SettingsProvider;
import org.bitrepository.common.settings.XMLFileSettingsLoader;
import org.bitrepository.protocol.ProtocolComponentFactory;
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
import org.junit.jupiter.api.Test;

public class Something {
    @Test
    public void testSomething() throws Exception {
        SettingsProvider provider = new SettingsProvider(new XMLFileSettingsLoader("src/test/resources/conf"), null);
        Settings settings = provider.getSettings();
        PermissionStore permissionStore = new PermissionStore();
        MessageAuthenticator authenticator = new BasicMessageAuthenticator(permissionStore);
        MessageSigner signer = new BasicMessageSigner();
        OperationAuthorizor authorizer = new BasicOperationAuthorizor(permissionStore);
        SecurityManager securityManager = new BasicSecurityManager(settings.getRepositorySettings(), null, authenticator, signer, authorizer, permissionStore, settings.getComponentID());

        MessageBus bus = ProtocolComponentFactory.getInstance().getMessageBus(settings, securityManager);
        IdentifyPillarsForGetFileRequest request = new IdentifyPillarsForGetFileRequest();
        bus.sendMessage(request);
    }
}

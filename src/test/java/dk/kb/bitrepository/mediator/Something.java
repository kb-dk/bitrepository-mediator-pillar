package dk.kb.bitrepository.mediator;

import dk.kb.bitrepository.mediator.communication.DelegatingMessageHandler;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.SettingsProvider;
import org.bitrepository.common.settings.XMLFileSettingsLoader;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.protocol.ProtocolVersionLoader;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.security.SecurityManager;
import org.junit.jupiter.api.Test;

public class Something {
    @Test
    public void testSomething() throws Exception {
        SettingsProvider provider = new SettingsProvider(new XMLFileSettingsLoader("src/test/resources/conf"), null);
        Settings settings = provider.getSettings();
        /*PermissionStore permissionStore = new PermissionStore();
        MessageAuthenticator authenticator = new BasicMessageAuthenticator(permissionStore);
        MessageSigner signer = new BasicMessageSigner();
        OperationAuthorizor authorizer = new BasicOperationAuthorizor(permissionStore);*/
        SecurityManager securityManager = new NoOpSecurityManager();
        MessageBus bus = ProtocolComponentFactory.getInstance().getMessageBus(settings, securityManager);
        bus.addListener("Testination", new DelegatingMessageHandler());
        IdentifyPillarsForGetFileRequest request = new IdentifyPillarsForGetFileRequest();
        request.setMinVersion(ProtocolVersionLoader.loadProtocolVersion().getMinVersion());
        request.setVersion(ProtocolVersionLoader.loadProtocolVersion().getVersion());
        request.setFrom("testSomething");
        request.setCorrelationID("12345");
        request.setDestination("Testination");
        request.setReplyTo("Something");
        request.setFileID("test");
        bus.sendMessage(request);
        Thread.sleep(2000);
        bus.close();
    }
}

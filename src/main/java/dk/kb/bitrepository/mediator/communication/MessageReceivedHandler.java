package dk.kb.bitrepository.mediator.communication;

import dk.kb.bitrepository.mediator.PillarContext;
import dk.kb.bitrepository.mediator.crypto.AESCryptoStrategy;
import dk.kb.bitrepository.mediator.crypto.CryptoStrategy;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageReceivedHandler {
    private static final Logger log = LoggerFactory.getLogger(MessageReceivedHandler.class);
    private PillarContext context;

    public MessageReceivedHandler(PillarContext context) {
        this.context = context;
    }

    //TODO: Must be asynchronous to not block up the mediator pillar
    public <T> Object handleReceivedMessage(MockupMessageObject message) {
        switch (message.getType()) {
            case PUT_FILE:
                return new PutFile(context, message).execute();
            case GET_FILE:
                return new GetFile(context, message).execute();
            case DELETE_FILE:
                return new DeleteFile(context, message).execute();
            case REPLACE_FILE:
                return new ReplaceFile(context, message).execute();
            case GET_CHECKSUMS:
                return new GetChecksums(context, message).execute();
            default:
                log.error("Unsupported message type.");
        }
        return null;
    }

    /**
     * Standard initialization of an AESCryptoStrategy.
     *
     * @param password The password to use in the encryption.
     * @return Returns a standard AESCryptoStrategy instance.
     */
    @NotNull
    static CryptoStrategy initAES(String password) {
        return new AESCryptoStrategy(password);
    }

    /**
     * Using the overloaded constructor, in order to pass pre-determined salt and IV.
     * Mainly used to create an AES instance for decryption.
     *
     * @param password The password that was previously used to encrypt.
     * @param salt     The pre-determined salt.
     * @param iv       The pre-determined IV.
     * @return Returns an AESCryptoStrategy instance using pre-determined values.
     */
    @NotNull
    static CryptoStrategy initAES(String password, String salt, byte[] iv) {
        return new AESCryptoStrategy(password, salt, iv);
    }
}

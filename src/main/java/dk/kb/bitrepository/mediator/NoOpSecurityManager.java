package dk.kb.bitrepository;

import org.bitrepository.protocol.security.SecurityManager;
import org.bitrepository.protocol.security.exception.CertificateUseException;
import org.bitrepository.protocol.security.exception.MessageAuthenticationException;
import org.bitrepository.protocol.security.exception.MessageSigningException;
import org.bitrepository.protocol.security.exception.OperationAuthorizationException;
import org.bitrepository.protocol.security.exception.UnregisteredPermissionException;
import org.bouncycastle.cms.SignerId;


/**
 * Class containing empty / safe implementation of the SecurityManager interface.
 * It is intended to be used in tests, or where the functionality of a real SecurityManager implementation is not
 * needed.
 */
public class NoOpSecurityManager implements SecurityManager {

    @Override
    public SignerId authenticateMessage(String message, String signature) throws MessageAuthenticationException {
        return null;
    }

    @Override
    public String signMessage(String message) throws MessageSigningException {
        // Safe empty implementation
        return null;
    }

    @Override
    public void authorizeOperation(String operationType, String messageData, String signature, String collectionID)
            throws OperationAuthorizationException {
        // Safe empty implementation
    }

    @Override
    public void authorizeCertificateUse(String certificateUser, String messageData, String signature)
            throws CertificateUseException {
        // Safe empty implementation
    }

    @Override
    public String getCertificateFingerprint(SignerId signer) throws UnregisteredPermissionException {
        return null;
    }
}

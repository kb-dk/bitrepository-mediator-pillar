package dk.kb.bitrepository.mediator.communication.exception;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;

/**
 * Exception which wraps bad response information for the validation of the operation requests.
 */
public class InvalidMessageException extends RequestHandlerException {
    /**
     * Constructor.
     * @param responseCode The response code.
     * @param responseText The text for the response info.
     */
    public InvalidMessageException(ResponseCode responseCode, String responseText) {
        super(responseCode, responseText);
    }

    /**
     * Constructor.
     * @param responseCode The response code.
     * @param responseText The text for the response info.
     * @param e The exception to wrap into the StackTrace.
     */
    public InvalidMessageException(ResponseCode responseCode, String responseText, Exception e) {
        super(responseCode, responseText, e);
    }

    /**
     * Constructor.
     * @param responseInfo The response info.
     */
    public InvalidMessageException(ResponseInfo responseInfo) {
        super(responseInfo);
    }
}


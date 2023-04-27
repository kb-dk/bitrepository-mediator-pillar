package dk.kb.bitrepository.mediator.communication.exception;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;

/** The exception for the request handlers. */
public class RequestHandlerException extends Exception {
    /** The ResponseInfo wrapped by this exception. Tells the reason for the exception. */
    private final ResponseInfo responseInfo;
    private static final long serialVersionUID = 1283942385;

    /**
     * Constructor.
     *
     * @param responseCode The response code.
     * @param responseText The text for the response info.
     */
    public RequestHandlerException(ResponseCode responseCode, String responseText) {
        super(responseText);
        this.responseInfo = new ResponseInfo();
        this.responseInfo.setResponseCode(responseCode);
        this.responseInfo.setResponseText(responseText);
    }

    /**
     * Constructor.
     *
     * @param responseCode The response code.
     * @param responseText The text for the response info.
     * @param e            The exception to wrap into the StackTrace.
     */
    public RequestHandlerException(ResponseCode responseCode, String responseText, Exception e) {
        super(responseText, e);
        this.responseInfo = new ResponseInfo();
        this.responseInfo.setResponseCode(responseCode);
        this.responseInfo.setResponseText(responseText);
    }

    /**
     * Constructor.
     *
     * @param rInfo The response info.
     */
    public RequestHandlerException(ResponseInfo rInfo) {
        super(rInfo.getResponseText());
        this.responseInfo = rInfo;
    }

    /**
     * @return The wrapped ResponseInfo.
     */
    public ResponseInfo getResponseInfo() {
        return responseInfo;
    }

    @Override
    public String toString() {
        return super.toString() + ", " + responseInfo.toString();
    }

}

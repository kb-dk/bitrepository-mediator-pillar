package dk.kb.bitrepository.mediator.communication.exception;

import org.bitrepository.bitrepositoryelements.ResponseCode;

/**
 * Exception for telling, that a given operation is illegal.
 * This might involve: deleting with an invalid checksum, performing the 'Get' operation on a ChecksumPillar, etc.
 */
public class IllegalOperationException extends RequestHandlerException {
    private String fileID;

    /**
     * Constructor.
     * @param responseCode The response code.
     * @param responseText The text for the response info.
     * @param fileID The id of the file regarding the illegal operation. Use null, if no file.
     */
    public IllegalOperationException(ResponseCode responseCode, String responseText, String fileID) {
        super(responseCode, responseText);
        this.fileID = fileID;
    }

    /**
     * Constructor.
     * @param responseCode The response code.
     * @param responseText The text for the response info.
     * @param fileID The id of the file regarding the illegal operation. Use null, if no file.
     * @param e The exception to wrap into the StackTrace.
     */
    public IllegalOperationException(ResponseCode responseCode, String responseText, String fileID, Exception e) {
        super(responseCode, responseText, e);
        this.fileID = fileID;
    }

    /**
     * @return fileID field.
     */
    public String getFileID() {
        return fileID;
    }
}

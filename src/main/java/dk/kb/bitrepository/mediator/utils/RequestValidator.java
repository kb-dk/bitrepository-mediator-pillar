package dk.kb.bitrepository.mediator.utils;

import dk.kb.bitrepository.mediator.communication.exception.InvalidMessageException;
import dk.kb.bitrepository.mediator.communication.exception.RequestHandlerException;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.bitrepositorymessages.MessageRequest;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.FileIDValidator;

public class RequestValidator {
    private final FileIDValidator fileIDValidator;
    private final Settings pillarSettings;

    public RequestValidator(Settings pillarSettings) {
        this.pillarSettings = pillarSettings;
        fileIDValidator = new FileIDValidator(pillarSettings);
    }

    /**
     * Validates that the collection ID has been set.
     * @param request The request to check the collection ID for.
     */
    public void validateCollectionIdIsSet(MessageRequest request) {
        if(!request.isSetCollectionID()) {
            throw new IllegalArgumentException(request.getClass().getSimpleName() +
                    "'s requires a CollectionID");
        }
    }

    /**
     * Uses the FileIDValidator to validate the format of a given file id.
     * Also validates, that the file-id does not start with illegal 'path' characters (e.g. '..' or starts with a '/').
     * @param fileID The id to validate.
     * @throws RequestHandlerException If the id of the file was invalid.
     */
    public void validateFileIDFormat(String fileID) throws RequestHandlerException {
        ResponseInfo ri = fileIDValidator.validateFileID(fileID);
        if (ri == null) {
            if (fileID.contains("/..") || fileID.contains("../")) {
                ri = new ResponseInfo();
                ri.setResponseCode(ResponseCode.REQUEST_NOT_UNDERSTOOD_FAILURE);
                ri.setResponseText("Invalid");
            }
        }

        if (ri != null) {
            throw new InvalidMessageException(ri);
        }
    }

    /**
     * Validates that the request has the correct pillar ID.
     * @param pillarID The pillar ID.
     */
    public void validatePillarID(String pillarID) {
        if(!pillarID.equals(pillarSettings.getComponentID())) {
            throw new IllegalArgumentException("The request had a wrong PillarID: "
                    + "Expected '" + pillarSettings.getComponentID()
                    + "' but was '" + pillarID + "'.");
        }
    }

    public void validate(IdentifyPillarsForGetFileRequest request) throws RequestHandlerException {
        String fileID = request.getFileID();
        validateCollectionIdIsSet(request);
        validateFileIDFormat(fileID);
        //pillarDAO.checkFileExists(fileID, request.getCollectionID());
    }

    public void validate(GetFileRequest request) throws RequestHandlerException {
        validateCollectionIdIsSet(request);
        validatePillarID(request.getPillarID());
        validateFileIDFormat(request.getFileID());
        //pillarDAO.checkFileExists(fileID, request.getCollectionID());
    }

    // TODO validate protocol version on messages?
}

package dk.kb.bitrepository.mediator.utils;

import dk.kb.bitrepository.mediator.communication.exception.InvalidMessageException;
import dk.kb.bitrepository.mediator.communication.exception.RequestHandlerException;
import dk.kb.bitrepository.mediator.database.DatabaseDAO;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.bitrepositorymessages.MessageRequest;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.FileIDValidator;

/**
 * Class for validating received requests. Intended for use in the handlers handling the requests.
 */
public class RequestValidator {
    private final FileIDValidator fileIDValidator;
    private final Settings pillarSettings;
    private final DatabaseDAO dao;

    public RequestValidator(Settings pillarSettings, DatabaseDAO dao) {
        this.pillarSettings = pillarSettings;
        fileIDValidator = new FileIDValidator(pillarSettings);
        this.dao = dao;
    }

    /**
     * Validates that the collection ID has been set.
     * @param request The request to check the collection ID for.
     */
    public void validateCollectionIdIsSet(MessageRequest request) { // TODO validate we actually have the collection?
        if(!request.isSetCollectionID()) {
            throw new IllegalArgumentException(request.getClass().getSimpleName() +
                    "'s requires a CollectionID");
        }
    }

    /**
     * Uses the FileIDValidator to validate the format of a given file ID.
     * Also validates, that the file ID does not start with illegal 'path' characters (e.g. '..' or starts with a '/').
     * @param fileID The ID to validate.
     * @throws RequestHandlerException If the ID of the file was invalid.
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

    // TODO (?) add verifyChecksumAlgorithm - not sure

    /**
     * Validates that the file exists by looking for an entry in the database.
     * @param fileID File ID to look for
     * @param collectionID Collection to look for file in
     * @throws InvalidMessageException If file does not exist
     */
    public void validateFileExists(String fileID, String collectionID) throws InvalidMessageException {
        if (!dao.hasFile(fileID, collectionID)) {
            throw new InvalidMessageException(ResponseCode.FILE_NOT_FOUND_FAILURE,
                    "File '" + fileID + "' not found in collection '" + collectionID + "'");
        }
    }

    /**
     * Validates IdentifyPillarsForGetFileRequests
     * @param request Request to validate
     * @throws RequestHandlerException If the request is wrongly configured or contains demands that can't be met.
     */
    public void validate(IdentifyPillarsForGetFileRequest request) throws RequestHandlerException {
        String fileID = request.getFileID();
        validateCollectionIdIsSet(request);
        validateFileIDFormat(fileID);
        validateFileExists(fileID, request.getCollectionID());
    }

    /**
     * Validates GetFileRequests
     * @param request Request to validate
     * @throws RequestHandlerException If the request is wrongly configured or contains demands that can't be met.
     */
    public void validate(GetFileRequest request) throws RequestHandlerException {
        String fileID = request.getFileID();
        validateCollectionIdIsSet(request);
        validatePillarID(request.getPillarID());
        validateFileIDFormat(fileID);
        validateFileExists(fileID, request.getCollectionID());
    }

    // TODO validate protocol version on messages?
}

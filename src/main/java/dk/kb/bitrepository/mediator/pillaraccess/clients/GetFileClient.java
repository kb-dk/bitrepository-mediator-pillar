package dk.kb.bitrepository.mediator.pillaraccess.clients;

import org.bitrepository.bitrepositoryelements.FilePart;
import org.bitrepository.client.BitrepositoryClient;
import org.bitrepository.client.eventhandler.EventHandler;

import java.net.URL;

public interface GetFileClient extends BitrepositoryClient {

    /**
     * Method for retrieving a file from the pillar able to deliver the file fastest.
     * <p>
     * The method will return as soon as the communication has been set up.
     *
     * @param collectionID          Identifies the collection the file should be retrieved from.
     * @param fileID                The id of the file to retrieve.
     * @param filePart              The part of the file, which is wanted. If null, then the whole file is retrieved.
     * @param uploadUrl             The url the pillar should upload the file to.
     * @param eventHandler          The handler which should receive notifications of the progress events.
     * @param auditTrailInformation Additional information to add to the audit trail created because of this operation.
     */
    void getFileFromEncryptedPillar(String collectionID, String fileID, FilePart filePart, URL uploadUrl,
                                    EventHandler eventHandler, String auditTrailInformation);
}

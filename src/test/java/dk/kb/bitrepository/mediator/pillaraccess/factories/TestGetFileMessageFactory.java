/*
 * #%L
 * Bitrepository Access
 * %%
 * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
/*
 * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package dk.kb.bitrepository.mediator.pillaraccess.factories;

import org.bitrepository.bitrepositoryelements.FilePart;
import org.bitrepository.bitrepositorymessages.*;

/**
 * Constructs the GetFile specific messages.
 * <p>
 * ToDo based on example messages.
 */
public class TestGetFileMessageFactory extends ClientTestMessageFactory {

    public TestGetFileMessageFactory(String clientID) {
        super(clientID);
    }

    public IdentifyPillarsForGetFileRequest createIdentifyPillarsForGetFileRequest() {
        IdentifyPillarsForGetFileRequest identifyPillarsForGetFileRequest = new IdentifyPillarsForGetFileRequest();
        initializeMessageDetails(identifyPillarsForGetFileRequest);
        identifyPillarsForGetFileRequest.setCorrelationID(CORRELATION_ID_DEFAULT);
        identifyPillarsForGetFileRequest.setFileID(FILE_ID_DEFAULT);
        return identifyPillarsForGetFileRequest;
    }

    /**
     * Creates a reference <code>IdentifyPillarsForGetFileRequest</code> message for comparing against a received
     * request.
     *
     * @param receivedIdentifyRequestMessage The request to compare against. Any attributes which can't be determined
     *                                       prior to receiving the request are copied from the supplied request to the returned message.
     *                                       Attributes copied
     *                                       include <code>correlationId</code>.
     * @return A reference <code>IdentifyPillarsForGetFileRequest</code> message.
     */
    public IdentifyPillarsForGetFileRequest createIdentifyPillarsForGetFileRequest(
            IdentifyPillarsForGetFileRequest receivedIdentifyRequestMessage, String toTopic, String from) {
        IdentifyPillarsForGetFileRequest identifyPillarsForGetFileRequest = createIdentifyPillarsForGetFileRequest();
        identifyPillarsForGetFileRequest.setCorrelationID(receivedIdentifyRequestMessage.getCorrelationID());
        identifyPillarsForGetFileRequest.setReplyTo(receivedIdentifyRequestMessage.getReplyTo());
        identifyPillarsForGetFileRequest.setDestination(toTopic);
        identifyPillarsForGetFileRequest.setFrom(from);
        return identifyPillarsForGetFileRequest;
    }

    public IdentifyPillarsForGetFileResponse createIdentifyPillarsForGetFileResponse(
            IdentifyPillarsForGetFileRequest receivedIdentifyRequestMessage,
            String pillarID,
            String pillarDestinationId) {
        IdentifyPillarsForGetFileResponse identifyPillarsForGetFileResponse = new IdentifyPillarsForGetFileResponse();
        initializeMessageDetails(identifyPillarsForGetFileResponse);
        identifyPillarsForGetFileResponse.setDestination(receivedIdentifyRequestMessage.getReplyTo());
        identifyPillarsForGetFileResponse.setCorrelationID(receivedIdentifyRequestMessage.getCorrelationID());
        identifyPillarsForGetFileResponse.setCollectionID(
                receivedIdentifyRequestMessage.getCollectionID());
        identifyPillarsForGetFileResponse.setReplyTo(pillarDestinationId);
        identifyPillarsForGetFileResponse.setPillarID(pillarID);
        identifyPillarsForGetFileResponse.setFileID(receivedIdentifyRequestMessage.getFileID());
        identifyPillarsForGetFileResponse.setTimeToDeliver(TIME_TO_DELIVER_DEFAULT);
        identifyPillarsForGetFileResponse.setFrom(pillarID);
        identifyPillarsForGetFileResponse.setResponseInfo(createPositiveIdentificationResponseInfo());
        return identifyPillarsForGetFileResponse;
    }

    public GetFileRequest createGetFileRequest(String pillarID, String toTopic, String from) {
        GetFileRequest getFileRequest = new GetFileRequest();
        initializeMessageDetails(getFileRequest);
        getFileRequest.setCorrelationID(CORRELATION_ID_DEFAULT);
        getFileRequest.setFileID(FILE_ID_DEFAULT);
        getFileRequest.setPillarID(pillarID);
        getFileRequest.setDestination(toTopic);
        getFileRequest.setFrom(from);
        return getFileRequest;
    }

    public GetFileRequest createGetFileRequest(GetFileRequest receivedGetFileRequest,
                                               FilePart filePart, String pillarID, String toTopic, String from) {
        GetFileRequest getFileRequest = createGetFileRequest(pillarID, toTopic, from);
        getFileRequest.setCorrelationID(receivedGetFileRequest.getCorrelationID());
        getFileRequest.setFileAddress(receivedGetFileRequest.getFileAddress());
        getFileRequest.setReplyTo(receivedGetFileRequest.getReplyTo());
        getFileRequest.setFilePart(filePart);
        return getFileRequest;
    }

    public GetFileProgressResponse createGetFileProgressResponse(
            GetFileRequest receivedGetFileRequest, String pillarID, String pillarDestinationId) {
        GetFileProgressResponse getFileProgressResponse = new GetFileProgressResponse();
        initializeMessageDetails(getFileProgressResponse);
        getFileProgressResponse.setDestination(receivedGetFileRequest.getReplyTo());
        getFileProgressResponse.setCorrelationID(receivedGetFileRequest.getCorrelationID());
        getFileProgressResponse.setCollectionID(receivedGetFileRequest.getCollectionID());
        getFileProgressResponse.setReplyTo(pillarDestinationId);
        getFileProgressResponse.setPillarID(pillarID);
        getFileProgressResponse.setFileID(receivedGetFileRequest.getFileID());
        getFileProgressResponse.setResponseInfo(PROGRESS_INFO_DEFAULT);
        getFileProgressResponse.setFrom(pillarID);
        getFileProgressResponse.setFileAddress(receivedGetFileRequest.getFileAddress());
        return getFileProgressResponse;
    }

    public GetFileFinalResponse createGetFileFinalResponse(
            GetFileRequest receivedGetFileRequest, String pillarID, String pillarDestinationId) {
        GetFileFinalResponse getFileFinalResponse = new GetFileFinalResponse();
        initializeMessageDetails(getFileFinalResponse);
        getFileFinalResponse.setDestination(receivedGetFileRequest.getReplyTo());
        getFileFinalResponse.setCorrelationID(receivedGetFileRequest.getCorrelationID());
        getFileFinalResponse.setCollectionID(receivedGetFileRequest.getCollectionID());
        getFileFinalResponse.setReplyTo(pillarDestinationId);
        getFileFinalResponse.setPillarID(pillarID);
        getFileFinalResponse.setFileID(receivedGetFileRequest.getFileID());
        getFileFinalResponse.setResponseInfo(createCompleteResponseInfo());
        getFileFinalResponse.setFrom(pillarID);
        getFileFinalResponse.setFileAddress(receivedGetFileRequest.getFileAddress());
        return getFileFinalResponse;
    }
}

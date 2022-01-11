package dk.kb.bitrepository.mediator.utils;

import org.bitrepository.bitrepositorymessages.MessageRequest;

public class RequestValidator { // TODO probably make as interface so more specific validating can be made (maybe make generic???)
    public void validateCollectionIdIsSet(MessageRequest request) {
        if(!request.isSetCollectionID()) {
            throw new IllegalArgumentException(request.getClass().getSimpleName() +
                    "'s requires a CollectionID");
        }
    }


    public void validateFileIDFormat(String fileID) {
        System.out.println("FIX ME!");
    }
}

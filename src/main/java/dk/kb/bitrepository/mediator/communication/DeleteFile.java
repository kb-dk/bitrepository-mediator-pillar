package dk.kb.bitrepository.mediator.communication;

import dk.kb.bitrepository.mediator.PillarContext;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static dk.kb.bitrepository.mediator.database.DatabaseConstants.ENC_PARAMS_TABLE;
import static dk.kb.bitrepository.mediator.database.DatabaseConstants.FILES_TABLE;
import static dk.kb.bitrepository.mediator.database.DatabaseData.FilesData;

public class DeleteFile extends MessageResult<String> {
    private final String collectionID;
    private final String fileID;
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public DeleteFile(PillarContext context, @NotNull MockupMessageObject message) {
        this.context = context;
        this.collectionID = message.getCollectionID();
        this.fileID = message.getFileID();
    }

    @Override
    public String execute() {
        FilesData filesData = (FilesData) context.getDAO().select(collectionID, fileID, FILES_TABLE);

        if (filesData != null) {
            String encryptedChecksum = filesData.getEncryptedChecksum();

            // TODO: Relay 'delete' call to Encrypted Pillar - and get response (Enc_Checksum or boolean?)
            boolean deletedFromEncryptedPillar = relayMessageToEncryptedPillar(collectionID, fileID, encryptedChecksum);

            if (deletedFromEncryptedPillar) {
                String checksum = filesData.getChecksum();
                context.getDAO().delete(collectionID, fileID, FILES_TABLE);
                context.getDAO().delete(collectionID, fileID, ENC_PARAMS_TABLE);

                return checksum;
            } else {
                //TODO: Throw a warning that checksums need to be recalculated
                return null;
            }
        }
        log.warn("No results were found for collection- and file-id : [{}, {}]", collectionID, fileID);
        return null;
    }

    private boolean relayMessageToEncryptedPillar(String collectionID, String fileID, String encryptedChecksum) {
        //TODO: implement
        return true;
    }
}

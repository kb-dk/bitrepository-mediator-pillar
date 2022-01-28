package dk.kb.bitrepository.mediator.communication;

import dk.kb.bitrepository.database.DatabaseData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static dk.kb.bitrepository.database.DatabaseCalls.delete;
import static dk.kb.bitrepository.database.DatabaseCalls.select;
import static dk.kb.bitrepository.database.DatabaseConstants.ENC_PARAMS_TABLE;
import static dk.kb.bitrepository.database.DatabaseConstants.FILES_TABLE;
import static dk.kb.bitrepository.database.DatabaseData.FilesData;

public class DeleteFile extends MessageResult<String> {
    private final String collectionID;
    private final String fileID;
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public DeleteFile(@NotNull MockupMessageObject message) {
        this.collectionID = message.getCollectionID();
        this.fileID = message.getFileID();
    }

    @Override
    public String execute() {
        List<DatabaseData> filesData = select(collectionID, fileID, FILES_TABLE);

        if (!filesData.isEmpty()) {
            FilesData firstResult = (FilesData) filesData.get(0);
            String encryptedChecksum = firstResult.getEncryptedChecksum();

            // TODO: Relay 'delete' call to Encrypted Pillar - and get response (Enc_Checksum or boolean?)
            boolean deletedFromEncryptedPillar = relayMessageToEncryptedPillar(collectionID, fileID, encryptedChecksum);

            if (deletedFromEncryptedPillar) {
                String checksum = firstResult.getChecksum();
                delete(collectionID, fileID, FILES_TABLE);
                delete(collectionID, fileID, ENC_PARAMS_TABLE);

                return checksum;
            } else {
                //TODO: Throw a warning that checksums need to be recalculated
                return null;
            }
        }
        log.error("No results were found for collection- and file-id : [{}, {}]", collectionID, fileID);
        return null;
    }

    private boolean relayMessageToEncryptedPillar(String collectionID, String fileID, String encryptedChecksum) {
        //TODO: implement
        return true;
    }
}

package dk.kb.bitrepository.mediator.communication;

import dk.kb.bitrepository.mediator.PillarContext;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static dk.kb.bitrepository.mediator.database.DatabaseConstants.FILES_TABLE;
import static dk.kb.bitrepository.mediator.database.DatabaseData.FilesData;
import static org.bitrepository.common.utils.ChecksumUtils.generateChecksum;

public class GetChecksums extends MessageResult<List<EncryptedPillarData>> {
    private final ChecksumSpecTYPE checksumSpecTYPE;
    private final MockupResponse response;
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public GetChecksums(PillarContext context, MockupMessageObject message) {
        this.context = context;
        this.response = message.getMockupResponse();
        checksumSpecTYPE = new ChecksumSpecTYPE();
        checksumSpecTYPE.setChecksumType(ChecksumType.MD5);
    }

    @Override
    public List<EncryptedPillarData> execute() {
        //FIXME: Encrypted pillar responds with byte[] data or checksum of that data?
        return relayMessageToEncryptedPillar().getInfo().stream()
                .map((e) -> createChecksumDataIfChecksumsMatch(
                        e.getCollectionID(),
                        e.getFileID(),
                        generateChecksum(new ByteArrayInputStream(e.getPayload()), checksumSpecTYPE)
                ))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private EncryptedPillarData createChecksumDataIfChecksumsMatch(String collectionID, String fileID, String encryptedChecksumPillar) {
        FilesData result = (FilesData) context.getDAO().select(collectionID, fileID, FILES_TABLE);

        if (result.getEncryptedChecksum().equals(encryptedChecksumPillar)) {
            //FIXME: Correct information returned?
            return new EncryptedPillarData(collectionID, fileID, null, result.getChecksum(), result.getChecksumTimestamp());
        } else {
            //TODO Throw warning that checksums doesn't match
            log.error("The checksum [{}] of file [{}, {}] did not match the one on pillar.", result.getChecksum(), collectionID, fileID);
            return null;
        }
    }

    private MockupResponse relayMessageToEncryptedPillar() {
        //TODO: Implement
        return response;
    }
}

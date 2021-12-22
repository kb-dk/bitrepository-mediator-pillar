package dk.kb.bitrepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MediatorPillar {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private Configuration config;

    /**
     * Rough sketch:
     * Should initialize all necessary stuff so that we listen for new requests on the messagebus (or initialize something that does it)
     * Initialize database etc. (collection_id, file_id, receival_timestamp, crypto_pillar_receival_timestamp(?), crypto_algo, checksum, crypto_checksum)
     * Listen for incoming messages and delegate them to appropriate handlers (spawn new thread for each?)
     * - GetFile: check dao if file exists, propagate message to pillar ("client-side"), and get response back to original client
     * - PutFile: check dao if file already exists (name can't be same right?), put stuff in db, propagate message to pillar, and get response back to original client
     * - GetFileIDs: Just check dao and respond with file IDs from there no?
     * - DeleteFile: Check dao if file exists, propagate message to pillar, and get response back from pillar to client
     * - ReplaceFile:
     * - GetChecksums:
     *
     */
    public MediatorPillar(Configuration config) {
        log.debug("Running constructor HelloWorld()");
        this.config = config;

    }

    public void start() {
        System.out.println(config.getCollections());
    }
}
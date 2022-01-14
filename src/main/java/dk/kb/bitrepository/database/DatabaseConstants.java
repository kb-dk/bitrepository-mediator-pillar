package dk.kb.bitrepository.database;

import java.time.OffsetDateTime;

/**
 * Container for the constants of the database.
 * All the names of the tables and the fields in these tables.
 */
public final class DatabaseConstants {
    // Shared Constants
    /**
     * The name of the collection id field in both tables.
     */
    public final static String COLLECTION_ID = "collection_id";
    /**
     * The name of the file id field in both tables.
     */
    public final static String FILE_ID = "file_id";
    // Encrypted Parameters Constants
    /**
     * The name of the enc parameters table.
     */
    public final static String ENC_PARAMS_TABLE = "enc_parameters";
    /**
     * The name of the salt field in the enc parameters table.
     */
    public final static String ENC_PARAMS_SALT = "salt";
    /**
     * The name of the initialization vector field in the enc parameters table.
     */
    public final static String ENC_PARAMS_IV = "iv";
    /**
     * The name of the iterations' field in the enc parameters table.
     */
    public final static String ENC_PARAMS_ITERATIONS = "iterations";

    // Files Constants
    /**
     * The name of the file table.
     */
    public final static String FILES_TABLE = "files";
    /**
     * The name of the received timestamp field in the file table.
     */
    public final static String FILES_RECEIVED_TIMESTAMP_NAME = "received_timestamp";
    /**
     * A mock-up received timestamp, used for testing.
     */
    public final static OffsetDateTime FILES_RECEIVED_TIMESTAMP = OffsetDateTime.MIN;
    /**
     * A mock-up encrypted timestamp, used for testing.
     */
    public final static OffsetDateTime FILES_ENCRYPTED_TIMESTAMP = OffsetDateTime.MIN;
    /**
     * The name of the encrypted timestamp field in the file table.
     */
    public final static String FILES_ENCRYPTED_TIMESTAMP_NAME = "encrypted_timestamp";
    /**
     * The name of the checksum field in the file table.
     */
    public final static String FILES_CHECKSUM = "checksum";
    /**
     * The name of the encrypted checksum field in the file table.
     */
    public final static String FILES_ENC_CHECKSUM = "encrypted_checksum";
    /**
     * The name of the checksum timestamp field in the file table.
     */
    public final static OffsetDateTime FILES_CHECKSUM_TIMESTAMP = OffsetDateTime.MIN;

    /**
     * Private constructor to prevent instantiation of this constants class.
     */
    private DatabaseConstants() {
    }
}
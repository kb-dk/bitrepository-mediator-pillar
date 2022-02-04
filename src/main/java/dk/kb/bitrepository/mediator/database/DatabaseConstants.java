package dk.kb.bitrepository.mediator.database;

import java.time.OffsetDateTime;

import static ch.qos.logback.core.encoder.ByteArrayUtil.hexStringToByteArray;

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
     * Mock-up of the initialization vector, used for testing.
     */
    public final static byte[] ENC_PARAMS_IV = hexStringToByteArray("e04fd020ea3a6910a2d808002b30309d");
    /**
     * The name of the iterations' field in the enc parameters table.
     */
    public final static int ENC_PARAMS_ITERATIONS = 1;

    // Files Constants
    /**
     * The name of the files table.
     */
    public final static String FILES_TABLE = "files";
    /**
     * The name of the received timestamp field in the files table.
     */
    public final static String FILES_RECEIVED_TIMESTAMP = "received_timestamp";
    /**
     * A mock-up received timestamp, used for testing.
     */
    public final static OffsetDateTime FILES_RECEIVED_TIMESTAMP_MOCKUP = OffsetDateTime.MIN;
    /**
     * A mock-up encrypted timestamp, used for testing.
     */
    public final static OffsetDateTime FILES_ENCRYPTED_TIMESTAMP_MOCKUP = OffsetDateTime.MIN;
    /**
     * The name of the encrypted timestamp field in the files table.
     */
    public final static String FILES_ENCRYPTED_TIMESTAMP = "encrypted_timestamp";
    /**
     * The name of the checksum field in the file table.
     */
    public final static String FILES_CHECKSUM = "checksum";
    /**
     * The name of the encrypted checksum field in the files table.
     */
    public final static String FILES_ENC_CHECKSUM = "encrypted_checksum";
    /**
     * The name of the checksum timestamp field in the files table.
     */
    public final static String FILES_CHECKSUM_TIMESTAMP = "checksum_timestamp";
    /**
     * A mock-up checksum timestamp, used for testing.
     */
    public final static OffsetDateTime FILES_CHECKSUM_TIMESTAMP_MOCKUP = OffsetDateTime.MIN;

    public static final String DEFAULT_DATABASE_CREATION_SCRIPT = "sql/create_tables.sql";


    /**
     * Private constructor to prevent instantiation of this constants class.
     */
    private DatabaseConstants() {
    }
}
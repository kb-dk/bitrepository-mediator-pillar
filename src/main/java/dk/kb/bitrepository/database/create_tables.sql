CREATE TABLE IF NOT EXISTS files(
    collection_id VARCHAR(255) NOT NULL,
    file_id VARCHAR(255) NOT NULL,
    received_timestamp TIMESTAMP NOT NULL,
    encrypted_timestamp TIMESTAMP NOT NULL,
    checksum VARCHAR(255) NOT NULL,
    enc_checksum VARCHAR(255) NOT NULL,
    checksum_timestamp TIMESTAMP NOT NULL,
    PRIMARY KEY (collection_id, file_id)
);

CREATE TABLE IF NOT EXISTS enc_parameters(
    collection_id VARCHAR(255) NOT NULL,
    file_id VARCHAR(255) NOT NULL,
    salt VARCHAR(255) NOT NULL,
    iv VARCHAR(255) NOT NULL,
    iterations VARCHAR(255) NOT NULL,
    PRIMARY KEY (collection_id, file_id)
);

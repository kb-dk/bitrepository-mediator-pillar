package dk.kb.bitrepository.mediator.crypto;

import java.nio.file.Path;

public interface CryptoStrategy {
    void encrypt(Path inputFile, Path encryptedOutputFile);
    void decrypt(Path encryptedInputFile, Path decryptedOutputFile);
}

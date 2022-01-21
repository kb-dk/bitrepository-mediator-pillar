package dk.kb.bitrepository.utils.crypto;

import javax.crypto.spec.IvParameterSpec;
import java.nio.file.Path;

public interface CryptoStrategy {
    void encrypt(Path inputFile, Path encryptedOutputFile);

    void decrypt(Path encryptedInputFile, Path decryptedOutputFile);

    String getSalt();

    IvParameterSpec getIV();

    int getIterations();
}

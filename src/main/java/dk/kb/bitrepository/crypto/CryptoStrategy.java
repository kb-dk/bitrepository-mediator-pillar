package dk.kb.bitrepository.crypto;

import javax.crypto.spec.IvParameterSpec;
import java.nio.file.Path;

public interface CryptoStrategy {
    void encrypt(Path inputFile, Path encryptedOutputFile);

    byte[] encrypt(byte[] bytes);

    void decrypt(Path encryptedInputFile, Path decryptedOutputFile);

    byte[] decrypt(byte[] bytes);

    String getSalt();

    IvParameterSpec getIV();

    int getIterations();
}

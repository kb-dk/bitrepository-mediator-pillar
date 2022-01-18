package dk.kb.bitrepository.utils.crypto;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public class AESCryptoStrategy implements CryptoStrategy {
    private final Logger log = LoggerFactory.getLogger(AESCryptoStrategy.class);
    private final String SECRET_KEY_ALGO = "PBKDF2WithHmacSHA256"; // TODO figure out which of these to move to settings
    private final String CIPHER_ALGO = "AES/CBC/PKCS5Padding";
    private final int HASHING_ITERATIONS = 100_000;
    private final int KEY_LENGTH = 256;
    private String salt;
    private SecretKey secretKey; // TODO consider base64-encoding to string
    private IvParameterSpec iv;
    private Cipher cipher;

    public AESCryptoStrategy(String password) {
        this.salt = generateSalt();
        this.secretKey = getKeyFromPassword(password, salt);
        this.iv = generateIv();
        this.cipher = initCipher();
    }

    public AESCryptoStrategy(String password, String salt, byte[] iv) {
        this.salt = salt;
        this.secretKey = getKeyFromPassword(password, salt);
        this.iv = generateIv(iv);
        this.cipher = initCipher();
    }

    @Override
    public void encrypt(Path inputFile, Path encryptedOutputFile) {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
        } catch (InvalidKeyException e) {
            throw new IllegalStateException("Invalid key provided for encryption", e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new IllegalStateException("Bad parameters provided for encryption", e);
        }
        doTranscipher(inputFile, encryptedOutputFile);
    }

    @Override
    public void decrypt(Path encryptedInputFile, Path decryptedOutputFile) {
        try {
            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
        } catch (InvalidKeyException e) {
            throw new IllegalStateException("Invalid key provided for decryption", e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new IllegalStateException("Bad parameters provided for decryption", e);
        }
        doTranscipher(encryptedInputFile, decryptedOutputFile);
    }

    private void doTranscipher(Path inputFile, Path encryptedOutputFile) {
        try (InputStream inputStream = Files.newInputStream(inputFile);
             OutputStream outputStream = Files.newOutputStream(encryptedOutputFile)) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byte[] output = cipher.update(buffer, 0, bytesRead);
                if (output != null) {
                    outputStream.write(output);
                }
            }

            byte[] outputBytes;
            try {
                outputBytes = cipher.doFinal();
            } catch (IllegalBlockSizeException e) {
                throw new IllegalStateException("Bad data length provided to AES", e);
            } catch (BadPaddingException e) {
                throw new IllegalStateException("Bad padding.. did you use correct key for cipher?", e);
            }
            if (outputBytes != null) {
                outputStream.write(outputBytes);
            }
        } catch (IOException e) {
            log.error("Failed reading/writing input file '{}' or output file '{}'..", inputFile, encryptedOutputFile, e);
        }
    }

    private Cipher initCipher() {
        try {
            return Cipher.getInstance(CIPHER_ALGO);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Bad algorithm/padding '" + CIPHER_ALGO + "'", e);
        }
    }

    private SecretKey getKeyFromPassword(String password, String salt) {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(SECRET_KEY_ALGO);
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(StandardCharsets.UTF_8), HASHING_ITERATIONS, KEY_LENGTH);
            SecretKey secret = new SecretKeySpec(factory.generateSecret(spec)
                    .getEncoded(), "AES");
            return secret;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("AES should always be present in a Java SE runtime", e);
        } catch (InvalidKeySpecException e) {
            throw new IllegalStateException("Invalid parameters provided for creating encryption key", e);
        }
    }

    private String generateSalt() {
        byte[] salt = new byte[8];
        new SecureRandom().nextBytes(salt);
        return Hex.encodeHexString(salt);
    }

    private IvParameterSpec generateIv() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    private IvParameterSpec generateIv(byte[] iv) {
        return new IvParameterSpec(iv);
    }

    @Override
    public String getSalt() {
        return salt;
    }

    @Override
    public IvParameterSpec getIV() {
        return iv;
    }
}

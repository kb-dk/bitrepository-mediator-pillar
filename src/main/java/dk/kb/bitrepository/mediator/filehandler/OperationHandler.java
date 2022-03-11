package dk.kb.bitrepository.mediator.filehandler;

import dk.kb.bitrepository.mediator.crypto.CryptoStrategy;
import dk.kb.bitrepository.mediator.database.DatabaseDAO;
import dk.kb.bitrepository.mediator.database.DatabaseData.EncryptedParametersData;
import dk.kb.bitrepository.mediator.filehandler.context.JobContext;
import dk.kb.bitrepository.mediator.filehandler.exception.MismatchingChecksumsException;
import org.bitrepository.commandline.eventhandler.CompleteEventAwaiter;
import org.slf4j.Logger;

import javax.crypto.spec.IvParameterSpec;
import java.io.File;
import java.nio.file.Path;

import static dk.kb.bitrepository.mediator.MediatorPillarComponentFactory.getDAO;
import static dk.kb.bitrepository.mediator.filehandler.FileUtils.*;
import static dk.kb.bitrepository.mediator.utils.configurations.ConfigConstants.ENCRYPTED_FILES_PATH;
import static dk.kb.bitrepository.mediator.utils.configurations.ConfigConstants.UNENCRYPTED_FILES_PATH;

public abstract class OperationHandler<T extends JobContext> {
    protected Logger log;
    protected final T context;
    protected final Path unencryptedFilePath;
    protected final Path encryptedFilePath;
    protected CryptoStrategy crypto;
    protected String expectedChecksum;
    protected CompleteEventAwaiter eventHandler;
    protected DatabaseDAO dao;

    protected OperationHandler(T context, CryptoStrategy crypto, String expectedChecksum) {
        this.context = context;
        this.crypto = crypto;
        this.expectedChecksum = expectedChecksum;
        this.unencryptedFilePath = createFilePath(UNENCRYPTED_FILES_PATH, context.getCollectionID(), context.getFileID());
        this.encryptedFilePath = createFilePath(ENCRYPTED_FILES_PATH, context.getCollectionID(), context.getFileID());
        this.dao = getDAO();
    }

    /**
     * Override this method in the subclass to perform the correct corresponding operation.
     *
     * @throws MismatchingChecksumsException Throws an exception if the checksum of the file to work on does not match.
     */
    public abstract void performOperation() throws MismatchingChecksumsException;

    /**
     * Updates the encryption parameters, so that they match the ones used to encrypt the file that is currently being worked on.
     */
    protected void updateCryptoParameters(String collectionID, String fileID, DatabaseDAO dao) {
        EncryptedParametersData paramData = dao.getEncParams(collectionID, fileID);
        crypto.setSalt(paramData.getSalt());
        crypto.setIV(new IvParameterSpec(paramData.getIv()));
    }

    /**
     * Decrypts the bytes of file found at the encrypted file path.
     * An unencrypted file is created in the unencrypted file path. This is done by writing the bytes of the file at the encrypted file
     * path to a temp folder, and only moving the file to the unencrypted path once all the bytes has been successfully written to the file.
     *
     * @return A File object from the unencrypted file path. If the file is not written successfully returns null.
     */
    protected File decryptAndGetFile() {
        byte[] encryptedBytes = readBytesFromFile(encryptedFilePath);
        if (writeBytesToFile(crypto.decrypt(encryptedBytes), UNENCRYPTED_FILES_PATH, context.getCollectionID(), context.getFileID())) {
            return new File(unencryptedFilePath.toString());
        }
        return null;
    }

    /**
     * Handle this in the respective operation handler.
     */
    protected abstract void handleStateAndJobDoneHandler();
}

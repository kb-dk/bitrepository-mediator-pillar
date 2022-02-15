package dk.kb.bitrepository.mediator.filehandler.exception;

public class MismatchingChecksumsException extends Exception {
    public MismatchingChecksumsException() {
    }

    public MismatchingChecksumsException(String message) {
        super(message);
    }

    public MismatchingChecksumsException(String message, Throwable cause) {
        super(message, cause);
    }

    public MismatchingChecksumsException(Throwable cause) {
        super(cause);
    }

    public MismatchingChecksumsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

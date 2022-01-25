package dk.kb.bitrepository.mediator.communication;

import java.io.FileNotFoundException;

abstract class MessageResult<T> {
    public MessageResult() {
    }

    public abstract T execute() throws FileNotFoundException;
}

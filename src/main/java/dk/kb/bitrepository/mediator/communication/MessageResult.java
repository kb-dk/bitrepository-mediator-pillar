package dk.kb.bitrepository.mediator.communication;

import dk.kb.bitrepository.mediator.PillarContext;

import java.io.FileNotFoundException;

abstract class MessageResult<T> { // TODO any reason this is not an interface (with my added context there is)?
    protected PillarContext context;
    public MessageResult() {
    }

    public abstract T execute() throws FileNotFoundException;
}

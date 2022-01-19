package dk.kb.bitrepository.mediator.communication;

abstract class MessageResult<T> {
    public MessageResult() {
    }

    public abstract T execute();
}

package de.grobmeier.lionbeast;

/**
 * Exception which might be thrown when an exception occurs while initialization of the server
 */
public class ServerInitializationException extends Exception {
    private static final long serialVersionUID = 4921226152616196464L;

    public ServerInitializationException(String s, Throwable throwable) {
        super(s, throwable);
    }
}

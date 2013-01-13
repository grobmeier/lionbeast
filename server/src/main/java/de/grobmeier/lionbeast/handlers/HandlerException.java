package de.grobmeier.lionbeast.handlers;

import de.grobmeier.lionbeast.StatusCode;

/**
 * TODO: JavaDoc
 * <p/>
 * (c) 2013 Christian Grobmeier Software
 * All rights reserved.
 * mailto:cg@grobmeier.de
 */
public class HandlerException extends Exception {

    private StatusCode statusCode;

    public HandlerException(StatusCode statusCode, String s) {
        super(s);
        this.statusCode = statusCode;
    }

    public HandlerException(StatusCode statusCode) {
        super(statusCode.getReasonPhrase());
        this.statusCode = statusCode;
    }

    public HandlerException(StatusCode statusCode, String s, Throwable throwable) {
        super(s, throwable);
        this.statusCode = statusCode;
    }

    public StatusCode getStatusCode() {
        return statusCode;
    }
}

package de.grobmeier.lionbeast;

/**
 * TODO: JavaDoc
 * <p/>
 * (c) 2013 Christian Grobmeier Software
 * All rights reserved.
 * mailto:cg@grobmeier.de
 */
public enum HTTPHeader {

    CONTENT_TYPE("Content-Type"),
    CONNECTION("Connection"),
    CONTENT_LENGTH("Content-Length"),

    /* Lionbeast HTTP headers */
    LIONBEAST_REQUEST_URI("request-uri"),
    LIONBEAST_METHOD("method"),
    LIONBEAST_HTTP_VERSION("http-version"),
    LIONBEAST_STARTLINE("start-line");


    String value;

    private HTTPHeader(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}

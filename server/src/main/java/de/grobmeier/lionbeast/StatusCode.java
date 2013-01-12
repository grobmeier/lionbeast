package de.grobmeier.lionbeast;

import java.nio.ByteBuffer;

/**
 * Enumeration of HTTP status codes as defined here:
 * http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html
 */
public enum StatusCode {
    OK(200, "OK"),
    BAD_REQUEST(400, "Bad Request"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not Found");

    int code;
    String reasonPhrase;
    ByteBuffer statusLine;

    private StatusCode(int code, String reasonPhrase) {
        this.code = code;
        this.reasonPhrase = reasonPhrase;

        String s = new StringBuilder().append(this.code).append(" ").append(this.reasonPhrase).toString();
        statusLine = ByteBuffer.wrap(s.getBytes());
    }

    public int asInt() {
        return code;
    }

    public ByteBuffer getStatusLine() {
        return statusLine;
    }
}

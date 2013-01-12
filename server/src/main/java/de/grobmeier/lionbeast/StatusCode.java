package de.grobmeier.lionbeast;

/**
 * Enumeration of HTTP status codes as defined here:
 * http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html
 */
public enum StatusCode {

    OK(200),
    BAD_REQUEST(400),
    FORBIDDEN(403),
    NOT_FOUND(404);

    int code;

    private StatusCode(int code) {
        this.code = code;
    }

    public int asInt() {
        return code;
    }
}

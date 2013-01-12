package de.grobmeier.lionbeast;

import java.util.HashMap;
import java.util.Map;

/**
 * Representation of a HTTP request
 */
public class Request {
    private Map<String, String> headers = new HashMap<String, String>();

    public Request() {
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }
}

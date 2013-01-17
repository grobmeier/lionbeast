package de.grobmeier.lionbeast;

/**
 * A view, incomplete list for common values when working with HTTP headers
 */
public enum HTTPHeaderValues {

    KEEP_ALIVE("Keep-Alive"),
    CLOSE("close");

    String value;

    private HTTPHeaderValues(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}

package de.grobmeier.lionbeast;

/**
 * A list of content types.
 */
public enum ContentType {

    TEXT_PLAIN("text/plain");

    String type;

    private ContentType(String type) {
        this.type = type;
    }

    public String asString() {
        return type;
    }
}

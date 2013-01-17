package de.grobmeier.lionbeast.configuration;

/**
 * The definition of a Matcher. A matcher is holding the information which is necessary
 * to evaluate a request and decide which handler should take care of it. The matcher object
 * also holds the information which content type is to use.
 *
 * Currently matchers are available in two flavors: matching by file extension (html, txt etc)
 * or matching by complete path. First, the path will be evaluated and might override file extension
 * settings.
 */
public class Matcher {
    /* the type of this matcher (fileending, path) */
    private Type type;
    /* the name of the handler */
    private String ref;
    /* the expression to evaluate, f. e. html or /hellworld */
    private String expression;
    /* the content type of the result */
    private String defaultContentType;

    /**
     * returns the default content type
     * @return the default content type
     */
    public String getDefaultContentType() {
        return defaultContentType;
    }

    /**
     * Sets the default content type, for example "text/plain" or "text/html"
     * @param defaultContentType the default content type
     */
    public void setDefaultContentType(String defaultContentType) {
        this.defaultContentType = defaultContentType;
    }

    /**
     * returns the reference to the handler
     * @return the handler reference
     */
    public String getRef() {
        return ref;
    }

    /**
     * Sets the reference to a handler (references are the "names" as defined in the handler configuration file,
     * most likely lionbeast-handlers.xml
     *
     * @param ref the reference
     */
    public void setRef(String ref) {
        this.ref = ref;
    }

    /**
     * returns the type of this matcher
     * @return the type of this matcher
     */
    public Type getType() {
        return type;
    }

    /**
     * Sets the type of this matcher. The value will be transformed into a matching {@link Matcher.Type}
     * @param type the type as string value
     */
    public void setType(String type) {
        this.type = Type.valueOf(type);
    }

    /**
     * returns the expression to evaluate
     * @return the expression
     */
    public String getExpression() {
        return expression;
    }

    /**
     * sets the expression to evaluate
     * @param expression the expression
     */
    public void setExpression(String expression) {
        this.expression = expression;
    }

    /**
     * Possible types of a matcher
     */
    enum Type {
        FILEENDING,
        PATH;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Matcher matcher = (Matcher) o;

        if (!expression.equals(matcher.expression)) return false;
        if (!ref.equals(matcher.ref)) return false;
        if (type != matcher.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + ref.hashCode();
        result = 31 * result + expression.hashCode();
        return result;
    }
}

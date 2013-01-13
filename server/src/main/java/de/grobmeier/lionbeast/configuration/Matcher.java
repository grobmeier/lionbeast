package de.grobmeier.lionbeast.configuration;

/**
 * The definition of a Matcher
 */
public class Matcher {
    private Type type;
    private String ref;
    private String expression;
    private String defaultContentType;

    public String getDefaultContentType() {
        return defaultContentType;
    }

    public void setDefaultContentType(String defaultContentType) {
        this.defaultContentType = defaultContentType;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setType(String type) {
        this.type = Type.valueOf(type);
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
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

    enum Type {
        FILEENDING,
        PATH;
    }
}

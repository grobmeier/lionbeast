package de.grobmeier.lionbeast.configuration;

/**
 * The definition of a handler which at least contains of name
 * and full qualified class name.
 */
public class HandlerDefinition {
    private String className;
    private String name;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

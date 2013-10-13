package de.grobmeier.lionbeast.configuration;

/**
 * The definition of a handler which at least consists of a name
 * and full qualified class name. The class referenced as a Handler needs
 * to implement the {@link de.grobmeier.lionbeast.handlers.Handler} interface.
 *
 * Optional, the {@link de.grobmeier.lionbeast.handlers.AbstractHandler} can be implemented.
 *
 * The name will be referenced as "ref" in {@link Matcher}.
 */
public class HandlerDefinition {
    /* the full qualified class name of a class implementing the Handler interface */
    private String className;

    /* the name which should be used to reference this handler definition*/
    private String name;

    /**
     * returns the full qualified class name of a class implementing {@link de.grobmeier.lionbeast.handlers.Handler}
     * or extending {@link de.grobmeier.lionbeast.handlers.AbstractHandler}
     * @return the class name
     */
    public String getClassName() {
        return className;
    }

    /**
     * Sets the class name
     * @param className the full qualified class name
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * Returns the reference name of this handler definition
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this handler definition
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }
}

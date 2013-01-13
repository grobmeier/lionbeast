package de.grobmeier.lionbeast.configuration;

import de.grobmeier.lionbeast.ServerInitializationException;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.ConfigurationNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The matcher configuration
 */
public class MatcherConfiguration {
    private List<Matcher> matchers = new ArrayList<Matcher>();

    private Map<String, Matcher> fileEndingMatcher = new HashMap<String, Matcher>();
    private Map<String, Matcher> pathMatcher = new HashMap<String, Matcher>();

    /**
     * Initializes the matcher configuration
     * @return the ready to use matcher configuration
     * @throws ServerInitializationException if the configuration could not be loaded
     */
    MatcherConfiguration init() throws ServerInitializationException {
        XMLConfiguration config;
        try {
            config = new XMLConfiguration("lionbeast-matchers.xml");
        } catch (ConfigurationException e) {
            throw new ServerInitializationException("Could not load lionbeast-matchers.xml", e);
        }

        initMatchers(config);
        initMatcherMaps();

        return this;
    }

    /*
     * Commons Configuration should support dynamic bean creation, but it
     * a) seems somehow broken
     * b) makes the xml look ugly until one creates a custom DefaultConfigurationBuilder
     * Due to the lack of time I am going barefoot.
     */
    private void initMatchers(XMLConfiguration config) {
        List<HierarchicalConfiguration> matchers = config.configurationsAt("matchers.match");

        for (HierarchicalConfiguration matcher : matchers) {
            HierarchicalConfiguration.Node root = matcher.getRoot();

            Matcher entry = new Matcher();
            String expression = root.getValue().toString();
            entry.setExpression(expression);

            List<ConfigurationNode> attributes = root.getAttributes();
            for (ConfigurationNode attribute : attributes) {
                String value = attribute.getValue().toString();

                if ("type".equalsIgnoreCase(attribute.getName())) {
                    entry.setType(value);
                } else if ("ref".equalsIgnoreCase(attribute.getName())) {
                    entry.setRef(value);
                }
            }

            this.matchers.add(entry);
        }
    }

    private void initMatcherMaps() {
        for (Matcher matcher : matchers) {
            if(Matcher.Type.FILEENDING == matcher.getType()) {
                this.fileEndingMatcher.put(matcher.getExpression(), matcher);
            } else if(Matcher.Type.PATH == matcher.getType()) {
                this.pathMatcher.put(matcher.getExpression(), matcher);
            }
        }
    }

    /**
     * Returns a list of all Matchers
     * @return the matchers
     */
    public List<Matcher> getMatchers() {
        return matchers;
    }

    public Map<String, Matcher> getFileEndingMatcher() {
        return fileEndingMatcher;
    }

    public Map<String, Matcher> getPathMatcher() {
        return pathMatcher;
    }
}

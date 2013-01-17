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
    private static final String DEFAULT_CONFIGURATION = "lionbeast-matchers.xml";
    private static final String NODE_MATCH = "matchers.match";
    private static final String FIELD_TYPE = "type";
    private static final String FIELD_REF = "ref";
    private static final String FIELD_CONTENT_TYPE = "contentType";

    private List<Matcher> matchers = new ArrayList<Matcher>();

    private Map<String, Matcher> fileEndingMatcher = new HashMap<String, Matcher>();
    private Map<String, Matcher> pathMatcher = new HashMap<String, Matcher>();

    /* Only the configurator should create this object */
    MatcherConfiguration() {
    }

    /**
     * Initializes the matcher configuration
     *
     * @return the ready to use matcher configuration
     * @throws ServerInitializationException if the configuration could not be loaded
     */
    MatcherConfiguration init() throws ServerInitializationException {
        XMLConfiguration config;
        try {
            config = new XMLConfiguration(DEFAULT_CONFIGURATION);
        } catch (ConfigurationException e) {
            throw new ServerInitializationException("Could not load matchers configuration file", e);
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
        List<HierarchicalConfiguration> matchers = config.configurationsAt(NODE_MATCH);

        for (HierarchicalConfiguration matcher : matchers) {
            HierarchicalConfiguration.Node root = matcher.getRoot();

            Matcher entry = new Matcher();
            String expression = root.getValue().toString();
            entry.setExpression(expression);

            List<ConfigurationNode> attributes = root.getAttributes();
            for (ConfigurationNode attribute : attributes) {
                String value = attribute.getValue().toString();

                if (FIELD_TYPE.equalsIgnoreCase(attribute.getName())) {
                    entry.setType(value);
                } else if (FIELD_REF.equalsIgnoreCase(attribute.getName())) {
                    entry.setRef(value);
                } else if (FIELD_CONTENT_TYPE.equalsIgnoreCase(attribute.getName())) {
                    entry.setDefaultContentType(value);
                }
            }

            this.matchers.add(entry);
        }
    }

    /**
     * Divides the matchers into patch-matchers and fileending-matchers.
     */
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

    /**
     * returns the file ending matchers
     * @return the file ending matchers
     */
    public Map<String, Matcher> getFileEndingMatcher() {
        return fileEndingMatcher;
    }

    /**
     * returns the path matchers
     * @return the path matchers
     */
    public Map<String, Matcher> getPathMatcher() {
        return pathMatcher;
    }
}

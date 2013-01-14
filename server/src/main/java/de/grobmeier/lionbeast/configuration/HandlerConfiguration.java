package de.grobmeier.lionbeast.configuration;

import de.grobmeier.lionbeast.ServerInitializationException;
import de.grobmeier.lionbeast.handlers.Handler;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.ConfigurationNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads and maintains handler configurations
 */
public class HandlerConfiguration {
    private Map<String, HandlerDefinition> handlers = new HashMap<String, HandlerDefinition>();

    /* Only the configurator should create this object */
    HandlerConfiguration() {
    }

    /**
     * Initializes the handler configuration with lionbeast-handlers.xml
     *
     * @return the ready to use HandlerConfiguration
     * @throws ServerInitializationException if the configuration could not be loaded
     */
    HandlerConfiguration init() throws ServerInitializationException {
        XMLConfiguration config;
        try {
            config = new XMLConfiguration("lionbeast-handlers.xml");
        } catch (ConfigurationException e) {
            throw new ServerInitializationException("Could not load lionbeast-matchers.xml", e);
        }

        initHandlers(config);
        return this;
    }

    /**
     * Returns a HandlerDefinition by its name as given in lionbeast-handlers.xml.
     *
     * @param name the name of the handler
     * @return the HandlerDefinition, or null, if none has been found
     */
    public HandlerDefinition getDefinitionByName(String name) {
       return this.handlers.get(name);
    }

    /**
     * Returns a map of all handler definitions
     * @return the map of handler definitions
     */
    public Map<String, HandlerDefinition> getHandlers() {
        return handlers;
    }

    /**
     * Initializes all ahndlers
     * @param config the configuration file (as xml)
     */
    private void initHandlers(XMLConfiguration config) {
        List<HierarchicalConfiguration> handlers = config.configurationsAt("handlers.handler");
        for (HierarchicalConfiguration handler : handlers) {
            List<ConfigurationNode> attributes = handler.getRoot().getAttributes();

            HandlerDefinition definition = new HandlerDefinition();

            for (ConfigurationNode attribute : attributes) {
                String value = attribute.getValue().toString();

                if ("name".equalsIgnoreCase(attribute.getName())) {
                    definition.setName(value);
                } else if ("className".equalsIgnoreCase(attribute.getName())) {
                    definition.setClassName(value);
                }
            }

            this.handlers.put(definition.getName(), definition);
        }
    }
}

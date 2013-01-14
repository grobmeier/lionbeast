package de.grobmeier.lionbeast.configuration;

import de.grobmeier.lionbeast.ServerInitializationException;
import org.apache.commons.configuration.XMLConfiguration;

/**
 * Configurator holds the configurations necessary by the server. There should only be one Configurator object
 * which is ideally created at server start time.
 */
public class Configurator {
    private HandlerConfiguration handlerConfiguration = new HandlerConfiguration();
    private MatcherConfiguration matcherConfiguration = new MatcherConfiguration();
    private ServerConfiguration serverConfiguration = new ServerConfiguration();

    /* Singleton instance, created at server start */
    private static Configurator instance;

    /* Private to prevent others to create this object */
    private Configurator() {}

    /**
     * Loads the configurations.
     *
     * This method is not thread safe.
     *
     * @throws ServerInitializationException when the configuration could not be loaded
     */
    public static void configure() throws ServerInitializationException {
        instance = new Configurator();
        instance.handlerConfiguration.init();
        instance.matcherConfiguration.init();
        instance.serverConfiguration.init();
    }

    /**
     * Returns the instance of the Configurator.
     * @return the Configurator
     */
    public static Configurator getInstance() {
        if(instance == null) {
            throw new IllegalStateException("Configurator has not been initialized. Run configure() first.");
        }
        return instance;
    }

    /**
     * Returns the handler configuration
     * @return the handler configuration
     */
    public HandlerConfiguration getHandlerConfiguration() {
        return handlerConfiguration;
    }

    /**
     * Returns the matcher configuration
     * @return the matcher configuration
     */
    public MatcherConfiguration getMatcherConfiguration() {
        return matcherConfiguration;
    }

    /**
     * Returns the server configuration
     * @return the server configuration
     */
    public ServerConfiguration getServerConfiguration() {
        return serverConfiguration;
    }
}

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

    private static Configurator instance;

    private Configurator() {}

    /**
     * Loads the configurations.
     *
     * This method is not threadsafe.
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

    public HandlerConfiguration getHandlerConfiguration() {
        return handlerConfiguration;
    }

    public MatcherConfiguration getMatcherConfiguration() {
        return matcherConfiguration;
    }

    public ServerConfiguration getServerConfiguration() {
        return serverConfiguration;
    }
}

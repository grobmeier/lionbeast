package de.grobmeier.lionbeast.configuration;

import de.grobmeier.lionbeast.ServerInitializationException;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

/**
 * The main configuration of the server, holding important facts like port, binding address etc.
 */
public class ServerConfiguration {

    private XMLConfiguration xmlConfiguration = null;

    /* Only the configurator should create this object */
    ServerConfiguration() {
    }

    /**
     * Initializes the configuration
     * @return the initialized server configuration
     * @throws ServerInitializationException if the server could not be configured
     */
    ServerConfiguration init() throws ServerInitializationException {
        try {
            xmlConfiguration = new XMLConfiguration("lionbeast-server.xml");
        } catch (ConfigurationException e) {
            throw new ServerInitializationException("Cannot load lionbeast-server.xml", e);
        }
        return this;
    }

    /**
     * returns the port of this application
     * @return the port of this application
     */
    public int port() {
        return xmlConfiguration.getInt("port");
    }

    /**
     * returns the bind to address
     * @return the bind to address
     */
    public String bindTo() {
        return xmlConfiguration.getString("bindTo");
    }

    /**
     * returns the document root of the web content
     * @return the document root of the web content
     */
    public String documentRoot() {
        return xmlConfiguration.getString("documentRoot");
    }

    /**
     * returns the name of the welcome file, if no concrete document name is given
     * @return the name of the welcome file, if no concrete document name is given
     */
    public String welcomeFile() {
        return xmlConfiguration.getString("welcomeFile");
    }

    /**
     * returns the size of the worker pool
     * @return the size of the worker pool
     */
    public int workerThreadPoolSize() {
        return xmlConfiguration.getInt("workerThreadPoolSize");
    }

    /**
     * returns the size of the handler pool
     * @return the size of the handler pool
     */
    public int handlerThreadPoolSize() {
        return xmlConfiguration.getInt("handlerThreadPoolSize");
    }
}

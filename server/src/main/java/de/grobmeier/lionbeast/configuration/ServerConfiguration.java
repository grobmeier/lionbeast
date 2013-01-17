package de.grobmeier.lionbeast.configuration;

import de.grobmeier.lionbeast.ServerInitializationException;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

/**
 * The main configuration of the server, holding important facts like port, binding address etc.
 */
public class ServerConfiguration {
    private static final String DEFAULT_CONFIGURATION = "lionbeast-server.xml";
    private static final String FIELD_PORT = "port";
    private static final String FIELD_BIND_TO = "bindTo";
    private static final String FIELD_DOCUMENT_ROOT = "documentRoot";
    private static final String FIELD_WELCOME_FILE = "welcomeFile";
    private static final String FIELD_WORKER_THREAD_POOL_SIZE = "workerThreadPoolSize";
    private static final String FILD_HANDLER_THREAD_POOL_SIZE = "handlerThreadPoolSize";

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
            xmlConfiguration = new XMLConfiguration(DEFAULT_CONFIGURATION);
        } catch (ConfigurationException e) {
            throw new ServerInitializationException("Cannot load server configuration file", e);
        }
        return this;
    }

    /**
     * returns the port of this application
     * @return the port of this application
     */
    public int port() {
        return xmlConfiguration.getInt(FIELD_PORT);
    }

    /**
     * returns the bind to address
     * @return the bind to address
     */
    public String bindTo() {
        return xmlConfiguration.getString(FIELD_BIND_TO);
    }

    /**
     * returns the document root of the web content
     * @return the document root of the web content
     */
    public String documentRoot() {
        return xmlConfiguration.getString(FIELD_DOCUMENT_ROOT);
    }

    /**
     * returns the name of the welcome file, if no concrete document name is given
     * @return the name of the welcome file, if no concrete document name is given
     */
    public String welcomeFile() {
        return xmlConfiguration.getString(FIELD_WELCOME_FILE);
    }

    /**
     * returns the size of the worker pool
     * @return the size of the worker pool
     */
    public int workerThreadPoolSize() {
        return xmlConfiguration.getInt(FIELD_WORKER_THREAD_POOL_SIZE);
    }

    /**
     * returns the size of the handler pool
     * @return the size of the handler pool
     */
    public int handlerThreadPoolSize() {
        return xmlConfiguration.getInt(FILD_HANDLER_THREAD_POOL_SIZE);
    }
}

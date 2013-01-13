package de.grobmeier.lionbeast.configuration;

import de.grobmeier.lionbeast.ServerInitializationException;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

/**
 * TODO: JavaDoc
 * <p/>
 * (c) 2013 Christian Grobmeier Software
 * All rights reserved.
 * mailto:cg@grobmeier.de
 */
public class ServerConfiguration {

    private XMLConfiguration xmlConfiguration = null;

    ServerConfiguration init() throws ServerInitializationException {
        try {
            xmlConfiguration = new XMLConfiguration("lionbeast-server.xml");
        } catch (ConfigurationException e) {
            throw new ServerInitializationException("Cannot load lionbeast-server.xml", e);
        }
        return this;
    }

    public int port() {
        return xmlConfiguration.getInt("port");
    }

    public String bindTo() {
        return xmlConfiguration.getString("bindTo");
    }

    public String documentRoot() {
        return xmlConfiguration.getString("documentRoot");
    }
}

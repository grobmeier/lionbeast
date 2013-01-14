/*
 *   Copyright 2013 Christian Grobmeier
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package de.grobmeier.lionbeast;

import de.grobmeier.lionbeast.configuration.Configurator;
import de.grobmeier.lionbeast.configuration.ServerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Configures and launches the HTTP Server
 */
public class Launcher {
    private static final Logger logger = LoggerFactory.getLogger(Launcher.class);

    public void launch() throws IOException, ServerInitializationException {
        Configurator.configure();

        if ( Configurator.getInstance().getHandlerConfiguration().getHandlers().size() == 0 ) {
            logger.warn("No handlers specified in lionbeast-handlers.xml");
        }

        if ( Configurator.getInstance().getMatcherConfiguration().getMatchers().size() == 0 ) {
            logger.warn("No matchers specified in lionbeast-matchers.xml");
        }
        ServerConfiguration config = Configurator.getInstance().getServerConfiguration();

        Dispatcher dispatcher =
            new Dispatcher(
                config.bindTo(), config.port(), config.workerThreadPoolSize(), config.handlerThreadPoolSize());

        dispatcher.listen();
    }

    public static void main(String[] args) throws Exception {
        logger.info("Starting Lionbeast");
        new Launcher().launch();
        logger.info("Shutdown Lionbeast");
    }
}

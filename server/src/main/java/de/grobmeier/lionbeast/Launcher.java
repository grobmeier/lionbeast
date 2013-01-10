package de.grobmeier.lionbeast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Lionbeast HTTP Server
 */
public class Launcher {
    private static final Logger logger = LoggerFactory.getLogger(Launcher.class);

    public void launch() throws IOException {
        Dispatcher dispatcher = new Dispatcher();
        // TODO: read from configuration
        dispatcher.listen("localhost", 10000);
    }

    public static void main(String[] args) throws Exception {
        logger.info("Starting Lionbeast");
        new Launcher().launch();
        logger.info("Shutdown Lionbeast");
    }
}

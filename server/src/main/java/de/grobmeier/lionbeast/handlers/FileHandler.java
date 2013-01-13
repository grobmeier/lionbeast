package de.grobmeier.lionbeast.handlers;

import de.grobmeier.lionbeast.StatusCode;
import de.grobmeier.lionbeast.configuration.Configurator;

import java.io.IOException;

/**
 * Hello World Handler prints just... hello world.
 */
public class FileHandler extends AbstractHandler {
    @Override
    public void process() throws IOException {
        this.streamStatusCode(StatusCode.OK);
        this.streamDefaultContentType();

        this.streamHeaders("Connection", "close"); // TODO

        String requestUri = this.request.getHeaders().get("request-uri");
        String root = Configurator.getInstance().getServerConfiguration().documentRoot();

        this.streamFile(root + requestUri);
        this.finish();
    }
}

package de.grobmeier.lionbeast.handlers;

import de.grobmeier.lionbeast.StatusCode;
import de.grobmeier.lionbeast.configuration.Configurator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Hello World Handler prints just... hello world.
 */
public class FileHandler extends AbstractHandler {
    @Override
    public Boolean call() throws HandlerException {
        FileInputStream fis;

        try {
            String requestUri = this.request.getHeaders().get("request-uri");
            String root = Configurator.getInstance().getServerConfiguration().documentRoot();

            fis = new FileInputStream(root + requestUri);

            this.streamStatusCode(StatusCode.OK);
            this.streamDefaultContentType();

            this.streamHeaders("Connection", "close"); // TODO

            this.streamFile(fis);
            this.finish();
        }  catch (FileNotFoundException e) {
            throw new HandlerException(StatusCode.NOT_FOUND);
        } catch (IOException e) {
            throw new HandlerException(StatusCode.INTERNAL_SERVER_ERROR, "Could not stream to client", e);
        }
        return Boolean.TRUE;
    }
}

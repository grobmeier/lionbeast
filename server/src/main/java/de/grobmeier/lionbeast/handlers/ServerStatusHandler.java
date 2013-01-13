package de.grobmeier.lionbeast.handlers;

import de.grobmeier.lionbeast.StatusCode;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Reports a server status
 */
public class ServerStatusHandler extends AbstractHandler {

    private HandlerException handlerException;

    @Override
    public void process() throws HandlerException {
        try {
            this.streamStatusCode(handlerException.getStatusCode());
            this.streamHeaders("Content-Type", "text/html");
            this.streamHeaders("Connection", "close"); // TODO

            StringBuilder builder = new StringBuilder();
            builder
                .append("<html><head></head><body><h1>")
                .append(handlerException.getStatusCode().asInt())
                .append(" - ")
                .append(handlerException.getStatusCode().getReasonPhrase())
                .append("</h1>");

            if(handlerException != null) {
                builder.append("<pre>").append(handlerException.toString()).append("</pre>");
            }

            builder.append(("</body></html>"));

            this.streamData(
                    ByteBuffer.wrap(builder.toString().getBytes()));

            this.finish();
        }  catch (FileNotFoundException e) {
            throw new HandlerException(StatusCode.NOT_FOUND);
        } catch (IOException e) {
            throw new HandlerException(StatusCode.INTERNAL_SERVER_ERROR, "Could not stream to client", e);
        }
    }

    public void setHandlerException(HandlerException handlerException) {
        this.handlerException = handlerException;
    }
}

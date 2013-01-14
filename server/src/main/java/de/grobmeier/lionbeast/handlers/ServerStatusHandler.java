package de.grobmeier.lionbeast.handlers;

import de.grobmeier.lionbeast.StatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Reports a server status
 */
public class ServerStatusHandler extends AbstractHandler {
    private static final Logger logger = LoggerFactory.getLogger(ServerStatusHandler.class);

    private HandlerException handlerException;
    private boolean keepAlive;

    @Override
    public Boolean call() throws HandlerException {
        try {
            if(handlerException == null) {
                logger.error("ServerStatusHandler is called without an exception.");
                handlerException = new HandlerException(StatusCode.INTERNAL_SERVER_ERROR);
            }

            this.streamStatusCode(handlerException.getStatusCode());

            if(keepAlive) {
                this.streamHeaders("Connection", "keep-alive");
            } else {
                this.streamHeaders("Connection", "close");
            }

            this.streamHeaders("Content-Type", "text/html");

            StringBuilder builder = new StringBuilder();
            builder
                .append("<html><head></head><body><h1>")
                .append(handlerException.getStatusCode().asInt())
                .append(" - ")
                .append(handlerException.getStatusCode().getReasonPhrase())
                .append("</h1>");


            builder.append("<pre>").append(handlerException.toString()).append("</pre>");
            builder.append(("</body></html>"));

            this.streamHeaders("Content-Length", Integer.toString(builder.length()));

            this.streamData(ByteBuffer.wrap(builder.toString().getBytes()));
        }  finally {
            try {
                this.finish();
            } catch (IOException e) {
                throw new HandlerException(StatusCode.INTERNAL_SERVER_ERROR, "Could not close pipe");
            }
        }
        return Boolean.TRUE;
    }

    public void setHandlerException(HandlerException handlerException) {
        this.handlerException = handlerException;
    }

    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }
}

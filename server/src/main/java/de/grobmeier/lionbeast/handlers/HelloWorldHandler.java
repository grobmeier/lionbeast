package de.grobmeier.lionbeast.handlers;

import de.grobmeier.lionbeast.StatusCode;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Hello World Handler prints just... hello world.
 */
public class HelloWorldHandler extends AbstractHandler {
    @Override
    public Boolean call() throws HandlerException {
        try {
            this.streamStatusCode(StatusCode.OK);
            this.streamDefaultKeepAlive();
            this.streamHeaders("Content-Type", "text/html");

            // Get Data
            String result = "Hello <b>World</b>, what's up?";

            byte[] bytes = result.getBytes();
            this.streamHeaders("Content-Length", Long.toString(bytes.length));

            this.streamData(ByteBuffer.wrap(bytes));
        } finally {
            try {
                this.finish();
            } catch (IOException e) {
                throw new HandlerException(StatusCode.INTERNAL_SERVER_ERROR, "Could not close pipe");
            }
        }
        return Boolean.TRUE;
    }
}

package de.grobmeier.lionbeast.handlers;

import de.grobmeier.lionbeast.ContentType;
import de.grobmeier.lionbeast.StatusCode;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;

/**
 * Hello World Handler prints just... hello world.
 */
public class HelloWorldHandler extends AbstractHandler {
    @Override
    public void process() throws IOException {
        this.streamStatusCode(StatusCode.OK);
        this.streamHeaders("Content-Type", ContentType.TEXT_HTML.asString());
        this.streamHeaders("Connection", "close");

        // Get Data
        String result = "Hello <b>World</b>, what's up?";

        this.streamData(ByteBuffer.wrap(result.getBytes()));
        this.finish();
    }


}

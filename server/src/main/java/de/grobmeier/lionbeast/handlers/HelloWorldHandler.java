package de.grobmeier.lionbeast.handlers;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;

/**
 * Hello World Handler prints just... hello world.
 */
public class HelloWorldHandler extends AbstractHandler {
    @Override
    public void content(Pipe.SinkChannel sinkChannel) throws IOException {
        String result = "Hello <b>World</b>, what's up?";
        sinkChannel.write(ByteBuffer.wrap(result.getBytes()));
        sinkChannel.close();
    }
}

package de.grobmeier.lionbeast.handlers;

import de.grobmeier.lionbeast.StatusCode;

import java.io.IOException;
import java.nio.channels.ByteChannel;
import java.nio.channels.Channel;
import java.nio.channels.Pipe;
import java.nio.channels.SocketChannel;
import java.util.Map;

/**
 * TODO: JavaDoc
 * <p/>
 * (c) 2013 Christian Grobmeier Software
 * All rights reserved.
 * mailto:cg@grobmeier.de
 */
public interface Handler {

    void process() throws IOException;
    void setChannel(Pipe.SinkChannel sinkChannel);

}

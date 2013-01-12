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

    StatusCode prepare() throws IOException;

    void content(Pipe.SinkChannel sinkChannel) throws IOException;

    /**
     * Returns the content type provided by this handler.
     *
     * Content types are provided as Strings, despite the existence of {ContentType}.
     * If a third party handler wants to return an exotic content type he should be allowed to do so easily
     * and without dealing with internal classes.
     *
     * @return the content type as text representation
     * @see de.grobmeier.lionbeast.ContentType
     */
    String getContentType();

    Map<String, String> getAdditionalHeaders();

}

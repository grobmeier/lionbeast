package de.grobmeier.lionbeast;

import de.grobmeier.lionbeast.handlers.HelloWorldHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.Channel;
import java.nio.channels.Pipe;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Iterator;
import java.util.concurrent.Callable;

/**
 * TODO: JavaDoc
 * <p/>
 * (c) 2013 Christian Grobmeier Software
 * All rights reserved.
 * mailto:cg@grobmeier.de
 */
class Worker implements Callable {
    private static final Logger logger = LoggerFactory.getLogger(Worker.class);

    private Iterator<java.nio.channels.SelectionKey> keys;
    private SelectionKey key;

    Worker(Iterator<SelectionKey> keys, SelectionKey key) {
        this.keys = keys;
        this.key = key;
    }

    @Override
    public Object call() throws Exception {
        logger.debug("Running worker with name: {}", Thread.currentThread().getName());

        logger.debug("WRITING");
        SocketChannel channel = (SocketChannel) key.channel();

        CharsetEncoder charsetEncoder = Charset.forName("UTF-8").newEncoder();

        // TODO Serialize handlers
        String headers = "HTTP/1.1 200 OK\r\n" + "Content-type: text/html\r\nConnection: close\r\n\r\n";
        channel.write(ByteBuffer.wrap(headers.getBytes()));

        // TODO chose between handler
        // HandlerPipeline: on file ending
        HelloWorldHandler handler = new HelloWorldHandler();
        StatusCode prepare = handler.prepare();
        String contentType = handler.getContentType();

        Pipe pipe = Pipe.open();
        handler.content( pipe.sink() );
        Pipe.SourceChannel source = pipe.source();

        ByteBuffer allocate = ByteBuffer.allocate(1000);
        while (source.read(allocate) != -1) {
            allocate.flip();
            channel.write(allocate);
            allocate.clear();
        }
        source.close();

        // TODO if keepalive
        //        Socket socket = channel.socket();
        //        socket.setKeepAlive(true);
        //        socket.setSoTimeout(200);
        // TODO register new readable

        // Close, if not kept alive
        channel.close();

        return null;
    }
}

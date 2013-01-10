package de.grobmeier.lionbeast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Iterator;
import java.util.Set;

public class Dispatcher {
    private static final Logger logger = LoggerFactory.getLogger(Dispatcher.class);

    Selector selector;

    void listen(String host, int port) throws IOException {

        selector = Selector.open();

        // Create at least one worker
        ServerSocketChannel worker = ServerSocketChannel.open();
        worker.socket().bind(new InetSocketAddress(host, port));
        worker.configureBlocking(false);

        worker.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            logger.debug("Selecting");
            int ready = selector.select();

            logger.debug("Selected a channel with event");

            Set<SelectionKey> selectionKeySet = selector.selectedKeys();
            Iterator<SelectionKey> selectionKeys = selectionKeySet.iterator();

            while (selectionKeys.hasNext()) {
                SelectionKey key = selectionKeys.next();

                if (!key.isValid()) {
                    logger.debug("REQEUST IS INVALID");
                    continue;
                }

                if (key.isAcceptable()) {
                    // a connection was accepted by a ServerSocketChannel.

                    logger.debug("REQEUST IS ACCEPTABLE");
                    accept((ServerSocketChannel) key.channel());

                } else if (key.isConnectable()) {
                    // a connection was established with a remote server.

                    logger.debug("REQEUST IS CONNECTABLE");
                } else if (key.isReadable()) {
                    // a channel is ready for reading
                    logger.debug("REQEUST IS READABLE");
                    read(selectionKeys, key);

                } else if (key.isWritable()) {
                    // a channel is ready for writing

                    logger.debug("REQEUST IS WRITABLE");
                }
            }
        }
    }

    void accept(ServerSocketChannel channel) throws IOException {
        SocketChannel socketChannel = channel.accept();

        if (socketChannel == null) {
            return;
        }

        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ, new RequestParser());
    }

    void read(Iterator<SelectionKey> keys, SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        RequestParser parser = (RequestParser) key.attachment();

        boolean endOfStream = false;
        while (channel.read(buffer) != -1 && !endOfStream) {
            buffer.flip();
            endOfStream = parser.onRead(buffer);
            buffer.clear();
        }

        keys.remove();
        key.interestOps(0);


        CharsetEncoder charsetEncoder = Charset.forName("UTF-8").newEncoder();



        String headers = "HTTP/1.1 200 OK\r\n" + "Content-type: text/html\r\n"
                + "Connection: close\r\n\r\n";

        String body = "Hello World";

        channel.write(ByteBuffer.wrap(headers.getBytes()));
        channel.write(ByteBuffer.wrap(body.getBytes()));

        channel.close();
    }
}
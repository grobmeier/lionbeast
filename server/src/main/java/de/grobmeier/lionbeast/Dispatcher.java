package de.grobmeier.lionbeast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
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

    private String host;
    private int port;

    private Selector selector;

    public Dispatcher(String host, int port) throws IOException {
        this.host = host;
        this.port = port;

        selector = Selector.open();
    }

    /**
     * Starts to listen to the outside world and deals with events.
     *
     * @throws IOException if binding to port/ip or selecting messages fail
     */
    void listen() throws IOException {
        createWorker(host, port);

        while (true) {
            logger.debug("Selecting");
            selector.select();
            logger.debug("Selected a channel with an event");

            Set<SelectionKey> selectionKeySet = selector.selectedKeys();
            Iterator<SelectionKey> selectionKeys = selectionKeySet.iterator();

            while (selectionKeys.hasNext()) {
                SelectionKey key = selectionKeys.next();

                if (!key.isValid()) {
                    logger.debug("Invalid request, skipping");
                    continue;
                }

                if (key.isAcceptable()) {
                    logger.debug("Request is acceptable");
                    accept(key);
                } else if (key.isReadable()) {
                    logger.debug("Request is readable");
                    read(selectionKeys, key);
                } else if(key.isWritable()) {
                    logger.debug("Request is writeable");
                    write(selectionKeys, key);
                }
            }
        }
    }

    /**
     * Creates a new ServerSocketChannel waiting for evens at given port and host.
     * It registers non-blocking on OP_ACCEPT.
     *
     * @param host the host to bind
     * @param port the port to bind
     * @throws IOException if the channel could not be established
     */
    private void createWorker(String host, int port) throws IOException {
        ServerSocketChannel worker = ServerSocketChannel.open();
        worker.socket().bind(new InetSocketAddress(host, port));
        worker.configureBlocking(false);
        worker.register(selector, SelectionKey.OP_ACCEPT);
    }

    /**
     * Accepts an event on a specific channel and registers the channel to catch the OP_READ event.
     * @param key the selection key
     * @throws IOException if accepting didn't work
     */
    void accept(SelectionKey key) throws IOException {
        logger.debug("ACCEPTING");
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel)key.channel();

        SocketChannel accepted = serverSocketChannel.accept();

        // can return null in non-blocking mode
        if (accepted == null) {
            return;
        }

        accepted.configureBlocking(false);
        accepted.register(selector, SelectionKey.OP_READ);
    }

    /**
     * Write back to the current channel
     * @param keys iterator to the currently selected keys
     * @param key the current key
     * @throws IOException if writing failed
     */
    void write(Iterator<SelectionKey> keys, SelectionKey key) throws IOException {
        logger.debug("WRITING");
        SocketChannel channel = (SocketChannel) key.channel();

        CharsetEncoder charsetEncoder = Charset.forName("UTF-8").newEncoder();

        String headers = "HTTP/1.1 200 OK\r\n" + "Content-type: text/html\r\nConnection: close\r\n\r\n";
        String body = "Hello World";

        channel.write(ByteBuffer.wrap(headers.getBytes()));
        channel.write(ByteBuffer.wrap(body.getBytes()));

        // TODO if keepalive
//        Socket socket = channel.socket();
//        socket.setKeepAlive(true);
//        socket.setSoTimeout(200);
        // TODO register new readable

        keys.remove();
        key.interestOps(0);

        // Close, if not kept alive
        channel.close();
        key.cancel();
    }

    /**
     * Reading from the selected channel
     *
     * @param keys iterator to the currently selected keys
     * @param key the current key
     * @throws IOException if writing failed
     */
    void read(Iterator<SelectionKey> keys, SelectionKey key) throws IOException {
        logger.debug("READING");
        SocketChannel channel = (SocketChannel) key.channel();

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        RequestParser parser = new RequestParser();

        boolean endOfStream = false;
        while (channel.read(buffer) != -1 && !endOfStream) {
            buffer.flip();
            endOfStream = parser.onRead(buffer);
            buffer.clear();
        }

        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_WRITE);
    }
}
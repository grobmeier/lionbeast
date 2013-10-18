/*
 *   Copyright 2013 Christian Grobmeier
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package de.grobmeier.lionbeast;

import de.grobmeier.lionbeast.handlers.HandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The dispatcher is the "main loop" of the server. It is a single thread which takes all incoming requests
 * from the Selector and and takes care the are accepted, read or processed.
 *
 * The processing of a request is done multithreaded, while reading and accepting done in the dispatcher thread.
 *
 * The dispatcher is reusing the port addresses; it will result in quicker startup after a server crash.
 */
public class Dispatcher {
    private static final Logger logger = LoggerFactory.getLogger(Dispatcher.class);

    /* the bound host */
    private String host;

    /* the port this server listens */
    private int port;

    /* the selector channel which listens to incoming requests */
    private Selector selector;

    /* the threadpool for workers */
    private ExecutorService executorService;

    /* the threadpool for handlers*/
    private ExecutorService handlerExecutorService;

    /* The handler factors has all handler definition in place and creates handler which process the request*/
    private HandlerFactory handlerFactory = new HandlerFactory();

    /**
     * Creates a new Dispatcher.
     *
     * @param host the host to bind to
     * @param port the port to listen
     * @param workerSize the size of the worker thread pool
     * @param handlerSize the size of the handler thread pool
     * @throws IOException if the Selector could not be opened
     */
    public Dispatcher(String host, int port, int workerSize, int handlerSize) throws IOException {
        this.host = host;
        this.port = port;

        selector = Selector.open();

        executorService = Executors.newFixedThreadPool(workerSize, new WorkerThreadFactory());
        handlerExecutorService = Executors.newFixedThreadPool(handlerSize, new WorkerThreadFactory());
    }

    /**
     * Starts to listen to the outside world and deals with events.
     *
     * @throws IOException if binding to port/ip or selecting messages fail
     */
    void listen() throws IOException {
        createChannel(host, port);

        // no graceful shutdown supported
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
                    selectionKeys.remove();
                    continue;
                }

                if (key.isAcceptable()) {
                    accept(key);
                } else if (key.isReadable()) {
                    logger.debug("Request is readable");
                    read(selectionKeys, key);
                } else if(key.isWritable()) {
                    logger.debug("Request is writeable");
                    process(selectionKeys, key);
                }
            }
        }
    }

    /**
     * Creates a new ServerSocketChannel waiting for evens at given port and host.
     * It registers non-blocking on OP_ACCEPT.
     *
     * Reusing addresses is on, as described here:
     * @see <a href="http://meteatamel.wordpress.com/2010/12/01/socket-reuseaddress-property-and-linux/">Meteatamel</a>
     *
     * @param host the host to bind
     * @param port the port to bind
     * @throws IOException if the channel could not be established
     */
    private void createChannel(String host, int port) throws IOException {
        ServerSocketChannel channel = ServerSocketChannel.open();
        channel.socket().setReuseAddress(true);
        channel.socket().bind(new InetSocketAddress(host, port));
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_ACCEPT);
    }

    /**
     * Accepts an event on a specific channel and registers the channel to catch the OP_READ event.
     *
     * @param key the selected key
     * @throws IOException if accepting didn't work
     */
    void accept(SelectionKey key) throws IOException {
        logger.debug("ACCEPTING");

        ServerSocketChannel serverSocketChannel = (ServerSocketChannel)key.channel();
        SocketChannel channel = serverSocketChannel.accept();

        // can return null in non-blocking mode
        if (channel == null) {
            return;
        }

        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);
    }

    /**
     * Process a response and write back the result to the client channel.
     * Runs asynchronous using a thread from the handler pool
     *
     * @param keys iterator to the currently selected keys
     * @param key the current key
     */
    void process(Iterator<SelectionKey> keys, SelectionKey key) {
        keys.remove();
        key.interestOps(0); // necessary TODO document why this is needed

        executorService.submit(
            new Worker(key, handlerFactory, handlerExecutorService));
    }

    /**
     * Reads the response from the selected channel.
     *
     * Currently only GET requests are supported. If POST (or other) data would have been sent, it should not be
     * read here. In case of large uploads the main thread would be blocked for a long time. A better place is to just
     * read the headers and leave the data block reading to the Worker. In this case: if there has been to much
     * bytes from the stream, the channel must be rewinded to the position of the incomming data block.
     *
     * @param key the selected key
     * @throws IOException if writing failed
     */
    void read(Iterator<SelectionKey> keys, SelectionKey key) throws IOException {
        logger.debug("READING");
        logger.debug("Request listens: {}", ((SocketChannel)key.channel()).socket().getPort());

        SocketChannel channel = (SocketChannel) key.channel();

        ByteBuffer buffer = ByteBuffer.allocate(100);
        RequestParser parser = new RequestParser();

        boolean endOfStream = false;
        while (channel.read(buffer) != -1 && !endOfStream) {
            buffer.flip();
            try {
                endOfStream = parser.onRead(buffer);
            } catch (ServerException e) {
                logger.error("The request did cause problems with the header. Stopping.");
                key.cancel();
                keys.remove();
                channel.close();
                return;
            }
            buffer.clear();
        }

        final RequestHeaders requestHeaders = parser.getRequestHeaders();
        if (requestHeaders == null) {
            logger.debug("Keep-Alive Socket/Request timed out, nothing to do. Discarding");
            key.cancel();
            keys.remove();
            channel.close();
            return;
        }

        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_WRITE, requestHeaders);
    }
}
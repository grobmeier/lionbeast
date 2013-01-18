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

import de.grobmeier.lionbeast.handlers.Handler;
import de.grobmeier.lionbeast.handlers.HandlerException;
import de.grobmeier.lionbeast.handlers.HandlerFactory;
import de.grobmeier.lionbeast.handlers.ServerStatusHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * The worker processes the actual request. It is expected the headers are already read, but
 * the body of the request has not been touched yet. This is beneficial when it comes to bigger uploads,
 * as they are not blocking the main thread until the process is read.
 *
 * Every request gets it's own worker which is ultimately running in an ExecutorService.
 */
class Worker implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(Worker.class);

    private SelectionKey key;
    private HandlerFactory handlerFactory;
    private ExecutorService executorService;

    /**
     * Constructs a worker with dependencies. The handler factory is instantiated only one time by the Dispatcher.
     * The keys are related to the actual request.
     *
     * The worker gets dependencies injected on constructor level.
     * @param key the selected key
     * @param handlerFactory the handler factory
     * @param executorService Executor service for executing resource reading
     */
    Worker(SelectionKey key, HandlerFactory handlerFactory, ExecutorService executorService) {
        this.key = key;
        this.handlerFactory = handlerFactory;
        this.executorService = executorService;
    }

    /**
     * The worker run which runs a specific handler for a request.
     * The worker also decides on keep alive or not (closing the channel or not).
     */
    @Override
    public void run() {
        logger.debug("WRITING worker with name: {}", Thread.currentThread().getName());

        RequestHeaders requestHeaders = (RequestHeaders)key.attachment();
        SocketChannel channel = (SocketChannel) key.channel();

        requestHeaders.normalizeWelcomeFile();

        try {
            handleKeepAlive(requestHeaders, channel);
            Handler handler = handlerFactory.createHandler(requestHeaders);
            write(handler, channel, requestHeaders);
        } catch (HandlerException e) {
            handleException(channel, e);
        } finally {
            Socket socket = channel.socket();

            // Close, if not keeping alive
            try {
                if (socket.getKeepAlive()) {
                    logger.debug("Keep alive detected, push BACK TO READ");
                    key.channel().register(key.selector(), SelectionKey.OP_READ);
                } else {
                    logger.debug("Closing session");
                    channel.close();
                }
            } catch (IOException e) {
                logger.error("Could not close client channel.", e);
            }
        }
    }

    /**
     * Writes the bytes from a handler back to the client.
     *
     * This operation will create another thread or use one from the executor service thread pool.
     * This is necessary as the used Pipe will block when an OS dependent size of bytes have been put into
     * the sink but not read.
     *
     * As it is not a good idea to read all files into memory (some might be huge) decision was to keep the Pipe
     * implementation and make the read asynchronous.
     *
     * This will cause problems if the read has been interrupted: if in the middle of a read the operation is
     * not longer possible, the headers for this request might have been sent already. With headers sent, it
     * is not longer possible to send a different status code and show an error message (like Internal Server Error).
     *
     * This might be acceptable as this is an unlikely case. The win is quicker streams to the client.
     *
     * This operation does not close the SocketChannel to the client. But it takes care of its own resources,
     * except of the sink, which needs to be closed by the resource reading thread when the read process is finished.
     *
     * @param handler the handler which will perform the data gathering
     * @param channel the client channel to write the data
     * @param requestHeaders the original requestHeaders
     * @throws HandlerException if the operation could not complete normally
     */
    private void write(Handler handler, SocketChannel channel, RequestHeaders requestHeaders) throws HandlerException {
        boolean streamingStarted = false;
        Pipe.SourceChannel source = null;
        try {
            Pipe pipe = openPipe();
            source = pipe.source();

            handler.setChannel(pipe.sink());
            handler.setRequestHeaders(requestHeaders);

            Future<Boolean> future = executorService.submit(handler);

            ByteBuffer buffer = ByteBuffer.allocate(1000);
            while (source.read(buffer) != -1) {
                buffer.flip();
                // this is the actual write to the client
                int write = channel.write(buffer);
                if(!streamingStarted && write != 0) {
                    streamingStarted = true;
                }
                buffer.clear();
            }

            future.get(); // not interested in the result object, but causes the exceptions to show up
        } catch (InterruptedException e) {
            logger.error("Reader threw exception which cannot be recovered.", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (!streamingStarted) {
                if (cause instanceof HandlerException) {
                    throw (HandlerException) cause;
                }
            }
            logger.error("Reader threw exception which cannot be recovered.", e);
        } catch (IOException e) {
            logger.error("Cannot read from source or write to out. Cannot recover", e);
        } finally {
            try {
                if (source != null) {
                    source.close();
                }
            } catch (IOException e) {
                logger.error("Cannot close source channel. Please check OS for to many open file.", e);
            }
        }
    }

    /**
     * Opens a pipe
     *
     * @return the pipe
     * @throws HandlerException if the pipe could not be opened
     */
    private Pipe openPipe() throws HandlerException {
        try {
            return Pipe.open();
        } catch (IOException e) {
            throw new HandlerException(StatusCode.INTERNAL_SERVER_ERROR, "Cannot open pipe");
        }
    }

    /**
     * Takes care of an HandlerException and writes the appropriate Server status code to the client
     * @param channel the channel to the client
     * @param exception the exception which should be reported
     */
    private void handleException(SocketChannel channel, HandlerException exception) {

        boolean keepAlive = false;
        try {
            keepAlive = channel.socket().getKeepAlive();
        } catch (SocketException e) {
            logger.error("Could not read keep alive flag from socket, better close it");
        }

        try {
            ServerStatusHandler handler = (ServerStatusHandler)handlerFactory.createHandler("serverStatus", "text/plain");
            handler.setKeepAlive(keepAlive);
            handler.setHandlerException(exception);
            write(handler, channel, null);
        } catch (HandlerException e) {
            logger.error("Cannot output server status. Game over.");
        }
    }

    /**
     * Handles the keep alive header
     * @param requestHeaders the requestHeaders containing the header
     * @param channel the channel to take care of
     * @throws HandlerException if the socket keep alive could not be set
     */
    private void handleKeepAlive(RequestHeaders requestHeaders, SocketChannel channel) throws HandlerException {
        String connection = requestHeaders.getHeader(HTTPHeader.CONNECTION);
        if (connection != null && HTTPHeaderValues.KEEP_ALIVE.toString().equalsIgnoreCase(connection)) {
            try {
                logger.debug("Marking keep alive connection");
                Socket socket = channel.socket();
                socket.setKeepAlive(true);
                socket.setSoTimeout(100);
            } catch (SocketException e) {
                throw new HandlerException(
                        StatusCode.INTERNAL_SERVER_ERROR, "Could not set keep alive flag");
            }
        }
    }
}

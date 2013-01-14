package de.grobmeier.lionbeast;

import de.grobmeier.lionbeast.configuration.Configurator;
import de.grobmeier.lionbeast.handlers.Handler;
import de.grobmeier.lionbeast.handlers.HandlerException;
import de.grobmeier.lionbeast.handlers.HandlerFactory;
import de.grobmeier.lionbeast.handlers.ServerStatusHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.Callable;
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

    private Iterator<java.nio.channels.SelectionKey> keys;
    private SelectionKey key;
    private HandlerFactory handlerFactory;
    private ExecutorService executorService;

    /**
     * Constructs a worker with dependencies. The handler factory is instantiated only one time by the Dispatcher.
     * The keys are related to the actual request.
     *
     * The worker gets dependencies injected on constructor level.
     * @param keys the selected keys
     * @param key the selected key
     * @param handlerFactory the handler factory
     * @param executorService Executor service for executing resource reading
     */
    Worker(Iterator<SelectionKey> keys, SelectionKey key, HandlerFactory handlerFactory, ExecutorService executorService) {
        this.keys = keys;
        this.key = key;
        this.handlerFactory = handlerFactory;
        this.executorService = executorService;
    }

    @Override
    public void run() {
        logger.debug("WRITING worker with name: {}", Thread.currentThread().getName());

        SocketChannel channel = (SocketChannel) key.channel();
        Request request = (Request) key.attachment();

        if ("/".equals(request.getHeaders().get("request-uri"))) {
            logger.debug("Overwriting request-uri with welcome file (leaving original status line intact)");
            request.getHeaders().put("request-uri",
                Configurator.getInstance().getServerConfiguration().welcomeFile());
        }

        try {
            Handler handler = handlerFactory.createHandler(request);
            write(handler, channel, request);
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
     * @param request the original request
     * @throws HandlerException if the operation could not complete normally
     */
    private void write(Handler handler, SocketChannel channel, Request request) throws HandlerException {
        boolean streamingStarted = false;
        Pipe.SourceChannel source = null;
        try {
            Pipe pipe = openPipe();
            source = pipe.source();

            handler.setChannel(pipe.sink());
            handler.setRequest(request);

            Future<Boolean> future = executorService.submit(handler);

            ByteBuffer allocate = ByteBuffer.allocate(1000);
            while (source.read(allocate) != -1) {
                allocate.flip();
                int write = channel.write(allocate);
                if(!streamingStarted && write != 0) {
                    streamingStarted = true;
                }
                allocate.clear();
            }

            future.get();
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
        try {
            ServerStatusHandler handler = (ServerStatusHandler)handlerFactory.createHandler("serverStatus", "text/plain");
            handler.setHandlerException(exception);
            write(handler, channel, null);
        } catch (HandlerException e) {
            logger.error("Cannot output server status. Game over.");
        }
    }
}

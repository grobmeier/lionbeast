package de.grobmeier.lionbeast;

import de.grobmeier.lionbeast.configuration.Configurator;
import de.grobmeier.lionbeast.handlers.Handler;
import de.grobmeier.lionbeast.handlers.HandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.Callable;

/**
 * The worker processes the actual request. It is expected the headers are already read, but
 * the body of the request has not been touched yet. This is beneficial when it comes to bigger uploads,
 * as they are not blocking the main thread until the process is read.
 *
 * Every request gets it's own worker which is ultimately running in an ExecutorService.
 */
class Worker implements Callable {
    private static final Logger logger = LoggerFactory.getLogger(Worker.class);

    private Iterator<java.nio.channels.SelectionKey> keys;
    private SelectionKey key;
    private HandlerFactory handlerFactory;

    /**
     * Constructs a worker with dependencies. The handler factory is instantiated only one time by the Dispatcher.
     * The keys are related to the actual request.
     *
     * The worker gets dependencies injected on constructor level.
     * @param keys the selected keys
     * @param key the selected key
     * @param handlerFactory the handler factory
     */
    Worker(Iterator<SelectionKey> keys, SelectionKey key, HandlerFactory handlerFactory) {
        this.keys = keys;
        this.key = key;
        this.handlerFactory = handlerFactory;
    }

    // TODO: do not throw exception
    @Override
    public Object call() throws Exception {
        logger.debug("WRITING worker with name: {}", Thread.currentThread().getName());

        SocketChannel channel = (SocketChannel) key.channel();
        Request request = (Request) key.attachment();

        if ("/".equals(request.getHeaders().get("request-uri"))) {
            logger.debug("Overwriting request-uri with welcome file (leaving original status line intact)");
            request.getHeaders().put("request-uri",
                Configurator.getInstance().getServerConfiguration().welcomeFile());
        }

        Handler handler = handlerFactory.createHandler(request);

        Pipe pipe = Pipe.open();
        Pipe.SourceChannel source = pipe.source();

        handler.setChannel(pipe.sink());
        handler.setRequest(request);
        handler.process();

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

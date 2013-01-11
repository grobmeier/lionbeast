package de.grobmeier.lionbeast;

import java.util.concurrent.ThreadFactory;

import static java.lang.String.format;

/**
 * Worker Factory for use with the Executor Service
 */
public class WorkerThreadFactory implements ThreadFactory {

    @Override
    public Thread newThread(Runnable runnable) {
        Thread thread = new Thread( runnable );
        thread.setName( Long.toString( System.currentTimeMillis() ) );
        thread.setDaemon( true );
        return thread;
    }
}

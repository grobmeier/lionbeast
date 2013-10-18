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

import de.grobmeier.lionbeast.configuration.Configurator;
import de.grobmeier.lionbeast.handlers.HandlerFactory;
import de.grobmeier.lionbeast.handlers.MiniHandler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WorkerTest {

    private ExecutorService executorService;

    @Before
    public void setUp() throws Exception {
        executorService = Executors.newFixedThreadPool(5, new WorkerThreadFactory());
        Configurator.configure();
    }

    @Test
    public void testRun() throws Exception {

        HandlerFactory factory = new HandlerFactory();

        RequestHeaders headers = new RequestHeaders();
        headers.addHeader(HTTPHeader.LIONBEAST_REQUEST_URI, "/mini");
        headers.addHeader(HTTPHeader.CONNECTION, HTTPHeaderValues.CLOSE.toString());

        SocketChannel socketChannel = mock(SocketChannel.class);

        ArgumentCaptor<ByteBuffer> argument = ArgumentCaptor.forClass(ByteBuffer.class);

        Socket socket = mock(Socket.class);

        when(socketChannel.socket()).thenReturn(socket);
        when(socket.getKeepAlive()).thenReturn(false);

        MySelectionKey selectionKey = new MySelectionKey(socketChannel);
        selectionKey.attach(headers);

        Worker worker = new Worker(selectionKey, factory, executorService);
        worker.run();

        verify(socketChannel).write(argument.capture());
        ByteBuffer value = argument.getValue();
        byte[] bytes = new byte[5];
        value.get(bytes);

        Assert.assertEquals(MiniHandler.MESSAGE, new String(bytes));
    }


    /**
     * Since did not succeed in mocking final method attachment()...
     *
     * I expected this to work with Powermock:
     *
     * SelectionKey selectionKey = PowerMockito.mock(SelectionKey.class);
     * when(selectionKey.attachment()).thenReturn(headers);
     * when(selectionKey.channel()).thenReturn(socketChannel);
     *
     * But it does only work for channel(), which is non-final. Therefore I have removed PowerMockito again.
     *
     * Note: The idea has been seen on Stack Overflow, but the following implementation
     * is taken from here:
     *
     * http://mail-archives.apache.org/mod_mbox/incubator-deft-commits/201109.mbox/%3C20110911183457.D82B523889E3@eris.apache.org%3E
     */
    class MySelectionKey extends SelectionKey {
        SelectableChannel channel;

        MySelectionKey(SelectableChannel channel) {
            super();
            this.channel = channel;
        }

        @Override
        public SelectableChannel channel() {
            return channel;
        }

        @Override
        public Selector selector() {
            return null;
        }

        @Override
        public boolean isValid() {
            return false;
        }

        @Override
        public void cancel() {
            // Nothing Todo
        }

        @Override
        public int interestOps() {
            return 0;
        }

        @Override
        public SelectionKey interestOps(int i) {
            return this;
        }

        @Override
        public int readyOps() {
            return 0;
        }
    }
}

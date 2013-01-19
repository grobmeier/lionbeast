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
package de.grobmeier.lionbeast.handlers;

import de.grobmeier.lionbeast.HTTPHeader;
import de.grobmeier.lionbeast.HTTPHeaderValues;
import de.grobmeier.lionbeast.RequestHeaders;
import de.grobmeier.lionbeast.StatusCode;
import de.grobmeier.lionbeast.configuration.Configurator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.Pipe;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Testing server status
 */
public class ServerStatusHandlerTest {
    private ExecutorService executorService;

    @Before
    public void setUp() throws Exception {
        executorService = Executors.newSingleThreadExecutor();
        Configurator.configure();
    }

    @Test
    public void testCall() throws Exception {
        String expected =
                new StringBuilder()
                        .append("HTTP/1.1 404 Not Found\r\n")
                        .append("Connection: close\r\n")
                        .append("Content-Type: text/html\r\n")
                        .append("Content-Length: 133\r\n")
                        .append("\r\n")
                        .append("<html><head></head><body><h1>404 - Not Found</h1>")
                        .append("<pre>de.grobmeier.lionbeast.handlers.HandlerException: Not Found</pre></body></html>")
                        .toString();

        Pipe pipe = Pipe.open();
        Pipe.SourceChannel source = pipe.source();

        RequestHeaders headers = new RequestHeaders();
        headers.addHeader(HTTPHeader.LIONBEAST_REQUEST_URI, "/test.txt");
        headers.addHeader(HTTPHeader.CONNECTION, HTTPHeaderValues.CLOSE.toString());

        HandlerException exception = new HandlerException(StatusCode.NOT_FOUND);

        ServerStatusHandler handler = new ServerStatusHandler();
        handler.setChannel(pipe.sink());
        handler.setDefaultContentType("text/plain");
        handler.setHandlerException(exception);

        Future<Boolean> future = executorService.submit(handler);

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        ByteBuffer buffer = ByteBuffer.allocate(1);
        while (source.read(buffer) != -1) {
            buffer.flip();
            out.write(buffer.array());
            buffer.clear();
        }

        future.get(); // not interested

        byte[] bytes = out.toByteArray();

        CharBuffer decode = Charset.forName("UTF-8").decode(ByteBuffer.wrap(bytes));
        Assert.assertEquals(expected, decode.toString());
    }
}

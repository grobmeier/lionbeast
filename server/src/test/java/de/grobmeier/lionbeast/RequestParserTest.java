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
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.ByteBuffer;

public class RequestParserTest {

    @Before
    public void setUp() throws Exception {
        Configurator.configure();
    }

    @Test
    public void testOnRead() throws Exception {
        String source =
                        new StringBuilder()
                                .append("GET /index.html HTTP/1.1\r\n")
                                .append("Accept-Encoding: utf-8\r\n")
                                .append("Connection: keep-alive\r\n")
                                .append("\r\n")
                                .append("HELLO").toString();

        ByteBuffer buffer = ByteBuffer.wrap(source.getBytes());

        RequestParser parser = new RequestParser();
        parser.onRead(buffer);
        RequestHeaders requestHeaders = parser.getRequestHeaders();

        Assert.assertEquals(
                "GET /index.html HTTP/1.1",
                requestHeaders.getHeader(HTTPHeader.LIONBEAST_STARTLINE));

        Assert.assertEquals("utf-8", requestHeaders.getHeaders().get("Accept-Encoding"));
        Assert.assertEquals("keep-alive", requestHeaders.getHeader(HTTPHeader.CONNECTION));
        Assert.assertEquals("/index.html", requestHeaders.getHeader(HTTPHeader.LIONBEAST_REQUEST_URI));
        Assert.assertEquals("HTTP/1.1", requestHeaders.getHeader(HTTPHeader.LIONBEAST_HTTP_VERSION));
        Assert.assertEquals("GET", requestHeaders.getHeader(HTTPHeader.LIONBEAST_METHOD));
    }
}

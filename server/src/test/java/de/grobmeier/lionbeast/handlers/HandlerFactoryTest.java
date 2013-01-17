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

import de.grobmeier.lionbeast.RequestHeaders;
import de.grobmeier.lionbeast.configuration.Configurator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Handler factory test
 */
public class HandlerFactoryTest {
    @Before
    public void setUp() throws Exception {
        Configurator.configure();
    }

    @Test
    public void testCreateHandlerByEnding() throws Exception {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("request-uri", "/index.html");
        headers.put("method", "GET");

        RequestHeaders requestHeaders = new RequestHeaders();
        requestHeaders.setHeaders(headers);

        HandlerFactory factory = new HandlerFactory();
        Handler handler = factory.createHandler(requestHeaders);

        Assert.assertNotNull(handler);
        Assert.assertEquals("de.grobmeier.lionbeast.handlers.HelloWorldHandler", handler.getClass().getCanonicalName());
    }

    @Test
    public void testCreateHandlerByPath() throws Exception {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("request-uri", "/helloworld");
        headers.put("method", "GET");

        RequestHeaders requestHeaders = new RequestHeaders();
        requestHeaders.setHeaders(headers);

        HandlerFactory factory = new HandlerFactory();
        Handler handler = factory.createHandler(requestHeaders);

        Assert.assertNotNull(handler);
        Assert.assertEquals("de.grobmeier.lionbeast.handlers.HelloWorldHandler", handler.getClass().getCanonicalName());
    }
}

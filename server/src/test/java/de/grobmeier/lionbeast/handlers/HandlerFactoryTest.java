package de.grobmeier.lionbeast.handlers;

import de.grobmeier.lionbeast.Request;
import de.grobmeier.lionbeast.configuration.Configurator;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO: JavaDoc
 * <p/>
 * (c) 2013 Christian Grobmeier Software
 * All rights reserved.
 * mailto:cg@grobmeier.de
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

        Request request = new Request();
        request.setHeaders(headers);

        HandlerFactory factory = new HandlerFactory();
        Handler handler = factory.createHandler(request);

        Assert.assertNotNull(handler);
        Assert.assertEquals("de.grobmeier.lionbeast.handlers.HelloWorldHandler", handler.getClass().getCanonicalName());
    }

    @Test
    public void testCreateHandlerByPath() throws Exception {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("request-uri", "/helloworld");
        headers.put("method", "GET");

        Request request = new Request();
        request.setHeaders(headers);

        HandlerFactory factory = new HandlerFactory();
        Handler handler = factory.createHandler(request);

        Assert.assertNotNull(handler);
        Assert.assertEquals("de.grobmeier.lionbeast.handlers.HelloWorldHandler", handler.getClass().getCanonicalName());
    }
}

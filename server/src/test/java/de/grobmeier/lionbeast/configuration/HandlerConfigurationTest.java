package de.grobmeier.lionbeast.configuration;

import junit.framework.Assert;
import org.junit.Test;

/**
 * Testing Handler Configurations
 */
public class HandlerConfigurationTest {
    @Test
    public void testHandlerConfiguration() throws Exception {
        HandlerConfiguration config = new HandlerConfiguration().init();

        HandlerDefinition helloworld = config.getDefinitionByName("helloworld");
        Assert.assertEquals("helloworld", helloworld.getName());
        Assert.assertEquals("de.grobmeier.lionbeast.handlers.HelloWorldHandler", helloworld.getClassName());

        HandlerDefinition goodbyeworld = config.getDefinitionByName("goodbyeworld");
        Assert.assertEquals("goodbyeworld", goodbyeworld.getName());
        Assert.assertEquals("de.grobmeier.lionbeast.handlers.GoodbyeWorldHandler", goodbyeworld.getClassName());
    }
}

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
package de.grobmeier.lionbeast.configuration;

import org.junit.Assert;
import org.junit.Test;

public class ServerConfigurationTest {

    @Test
    public void testMatcherConfiguration() throws Exception {
        ServerConfiguration config = new ServerConfiguration().init();

        Assert.assertEquals("UTF-8", config.serverEncoding());
        Assert.assertEquals(15, config.handlerThreadPoolSize());
        Assert.assertEquals(10, config.workerThreadPoolSize());
        Assert.assertEquals("localhost", config.bindTo());
        Assert.assertEquals("server/src/test/webdir", config.documentRoot());
        Assert.assertEquals(10000, config.port());
        Assert.assertEquals("/index.html", config.welcomeFile());
    }
}

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

import java.util.ArrayList;
import java.util.List;

/**
 * Testing Matcher Configurations
 */
public class MatcherConfigurationTest {

    @Test
    public void testMatcherConfiguration() throws Exception {
        MatcherConfiguration config = new MatcherConfiguration().init();

        List<Matcher> expected = new ArrayList<Matcher>();

        {
            Matcher matcher = new Matcher();
            matcher.setRef("helloworld");
            matcher.setType("FILEENDING");
            matcher.setExpression("html");
            matcher.setDefaultContentType("text/plain");

            expected.add(matcher);
        }

        {
            Matcher matcher = new Matcher();
            matcher.setRef("helloworld");
            matcher.setType("FILEENDING");
            matcher.setExpression("shtml");
            matcher.setDefaultContentType("text/plain");
            expected.add(matcher);
        }

        {
            Matcher matcher = new Matcher();
            matcher.setRef("helloworld");
            matcher.setType("FILEENDING");
            matcher.setExpression("htm");
            matcher.setDefaultContentType("text/plain");
            expected.add(matcher);
        }

        {
            Matcher matcher = new Matcher();
            matcher.setRef("helloworld");
            matcher.setType("PATH");
            matcher.setExpression("/helloworld");
            matcher.setDefaultContentType("text/plain");
            expected.add(matcher);
        }

        List<Matcher> matchers = config.getMatchers();
        Assert.assertEquals(4, matchers.size());

        // I don't care on the order of elements, otherwise
        // Assert.assertThat(matchers, Is.is(expected));
        Assert.assertTrue(matchers.containsAll(expected));
    }
}

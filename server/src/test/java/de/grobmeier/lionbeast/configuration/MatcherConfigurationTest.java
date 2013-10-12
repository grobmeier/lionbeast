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

import org.hamcrest.collection.IsMapContaining;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Testing Matcher Configurations
 */
public class MatcherConfigurationTest {

    private MatcherConfiguration config;

    @Before
    public void setUp() throws Exception {
        config = new MatcherConfiguration().init();
    }

    @Test
    public void testMatcherConfiguration() throws Exception {
        List<Matcher> expected = createExpectedMatchers();

        List<Matcher> matchers = config.getMatchers();
        Assert.assertEquals(6, matchers.size());

        // I don't care on the order of elements, otherwise
        // Assert.assertThat(matchers, Is.is(expected));
        Assert.assertTrue(matchers.containsAll(expected));
    }

    private List<Matcher> createExpectedMatchers() {
        List<Matcher> expected = new ArrayList<Matcher>();

        {
            Matcher matcher = new Matcher();
            matcher.setRef("helloworld");
            matcher.setType("FILEENDING");
            matcher.setExpression(".html");
            matcher.setDefaultContentType("text/plain");

            expected.add(matcher);
        }

        {
            Matcher matcher = new Matcher();
            matcher.setRef("helloworld");
            matcher.setType("FILEENDING");
            matcher.setExpression(".shtml");
            matcher.setDefaultContentType("text/plain");
            expected.add(matcher);
        }

        {
            Matcher matcher = new Matcher();
            matcher.setRef("helloworld");
            matcher.setType("FILEENDING");
            matcher.setExpression(".htm");
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

        {
            Matcher matcher = new Matcher();
            matcher.setRef("mini");
            matcher.setType("PATH");
            matcher.setExpression("/mini");
            matcher.setDefaultContentType("text/plain");
            expected.add(matcher);
        }

        {
            Matcher matcher = new Matcher();
            matcher.setRef("file");
            matcher.setType("FILEENDING");
            matcher.setExpression(".txt");
            matcher.setDefaultContentType("text/plain");
            expected.add(matcher);
        }
        return expected;
    }

    @Test
    public void testMatchers() throws Exception {
        Map<String,Matcher> fileEndingMatchers = config.getFileEndingMatcher();

        for (Matcher matcher : createExpectedMatchers()) {
            if (matcher.getType().equals(Matcher.Type.FILEENDING)) {
                Assert.assertThat(fileEndingMatchers, IsMapContaining.hasEntry(matcher.getExpression(), matcher));
            }
        }
    }
}

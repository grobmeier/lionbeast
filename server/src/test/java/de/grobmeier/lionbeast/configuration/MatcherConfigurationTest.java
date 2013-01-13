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

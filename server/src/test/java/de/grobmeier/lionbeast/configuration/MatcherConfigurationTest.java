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
            matcher.setExpression(".html");
            expected.add(matcher);
        }

        {
            Matcher matcher = new Matcher();
            matcher.setRef("helloworld");
            matcher.setType("FILEENDING");
            matcher.setExpression(".shtml");
            expected.add(matcher);
        }

        {
            Matcher matcher = new Matcher();
            matcher.setRef("helloworld");
            matcher.setType("FILEENDING");
            matcher.setExpression(".htm");
            expected.add(matcher);
        }

        List<Matcher> matchers = config.getMatchers();
        Assert.assertEquals(3, matchers.size());
        Assert.assertTrue(matchers.containsAll(expected));
    }
}

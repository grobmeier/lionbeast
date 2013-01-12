package de.grobmeier.lionbeast.configuration;

import junit.framework.Assert;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationFactory;
import org.apache.commons.configuration.DefaultConfigurationBuilder;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.beanutils.BeanDeclaration;
import org.apache.commons.configuration.beanutils.BeanHelper;
import org.apache.commons.configuration.beanutils.XMLBeanDeclaration;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.junit.Test;

import java.io.File;
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

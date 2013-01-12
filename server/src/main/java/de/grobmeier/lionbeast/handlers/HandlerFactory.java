package de.grobmeier.lionbeast.handlers;

import de.grobmeier.lionbeast.configuration.Configurator;
import de.grobmeier.lionbeast.configuration.HandlerConfiguration;
import de.grobmeier.lionbeast.configuration.HandlerDefinition;
import de.grobmeier.lionbeast.configuration.Matcher;
import de.grobmeier.lionbeast.configuration.MatcherConfiguration;

import java.util.Map;
import java.util.Set;

/**
 * TODO: JavaDoc
 * <p/>
 * (c) 2013 Christian Grobmeier Software
 * All rights reserved.
 * mailto:cg@grobmeier.de
 */
public class HandlerFactory {
    private HandlerConfiguration handlerConfiguration = Configurator.getInstance().getHandlerConfiguration();
    private MatcherConfiguration matcherConfiguration = Configurator.getInstance().getMatcherConfiguration();

    public Handler createHandler(Map<String, String> headers) {
        String ref = checkMatchingPath(headers);
        if (ref == null) {
            ref = checkFileEnding(headers);
        }

        if (ref == null) {
            // TODO: throw checked server exception to be catched with showing BADREQUEST
        }

        HandlerDefinition definition = handlerConfiguration.getDefinitionByName(ref);

        String className = definition.getClassName();

        try {
            Object o = Class.forName(className).newInstance();
            if (!(o instanceof Handler)) {
                // TODO throw Serverexception: INTERNAL SERVER ERROR
            }
            return (Handler)o;
        } catch (InstantiationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ClassNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        // TODO throw Serverexception: INTERNAL SERVER ERROR
        return null;
    }

    /**
     * File-Ending Strategy. Checks the file ending to match with the ending supported by a specific handler.
     * @param headers the headers of this request
     * @return the handler name
     */
    private String checkFileEnding(Map<String, String> headers) {
        Map<String, Matcher> fileEndingMatcher = matcherConfiguration.getFileEndingMatcher();
        Set<String> fileEndings = fileEndingMatcher.keySet();

        String uri = headers.get("request-uri");
        for (String fileEnding : fileEndings) {
            if(uri.endsWith(fileEnding)) {
                return fileEndingMatcher.get(fileEnding).getRef();
            }
        }
        return null;
    }

    /**
     * Path Strategy. Checks for the complete path to match. Makes sense for specific handling of a few paths.
     * @param headers the headers of this request
     * @return the handler name
     */
    private String checkMatchingPath(Map<String, String> headers) {
        Map<String, Matcher> pathMatcher = matcherConfiguration.getPathMatcher();
        Set<String> paths = pathMatcher.keySet();

        for (String path : paths) {
            if (path.equals(headers.get("request-uri"))) {
                return pathMatcher.get(path).getRef();
            }
        }
        return null;
    }
}

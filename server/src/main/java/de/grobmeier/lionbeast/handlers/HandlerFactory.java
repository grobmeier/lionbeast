package de.grobmeier.lionbeast.handlers;

import de.grobmeier.lionbeast.Request;
import de.grobmeier.lionbeast.StatusCode;
import de.grobmeier.lionbeast.configuration.Configurator;
import de.grobmeier.lionbeast.configuration.HandlerConfiguration;
import de.grobmeier.lionbeast.configuration.HandlerDefinition;
import de.grobmeier.lionbeast.configuration.Matcher;
import de.grobmeier.lionbeast.configuration.MatcherConfiguration;

import java.util.Map;
import java.util.Set;

/**
 * Class which creates handlers, either based on request or manually. Handlers are specified
 * in lionbeast-handlers.xml.
 */
public class HandlerFactory {
    private HandlerConfiguration handlerConfiguration = Configurator.getInstance().getHandlerConfiguration();
    private MatcherConfiguration matcherConfiguration = Configurator.getInstance().getMatcherConfiguration();

    /**
     * Creates a handler based on the (header) information from the request.
     * @param request the Request for which a handler needs to be created
     * @return the ready to use handler
     * @throws HandlerException if the handler could not be created or has not been found
     */
    public Handler createHandler(Request request) throws HandlerException {
        Map<String, String> headers = request.getHeaders();

        Matcher matcher = checkMatchingPath(headers);
        if (matcher == null) {
            matcher = checkFileEnding(headers);
        }

        if (matcher == null) {
            throw new HandlerException(StatusCode.INTERNAL_SERVER_ERROR, "No matcher found for this request");
        }

        return createHandler(matcher.getRef(), matcher.getDefaultContentType());
    }

    /**
     * Creates a handler from reference. The reference is a unique key specified in lionbeast-handlers.xml.
     *
     * @param reference the reference for the handler
     * @param defaultContentType the default content type of this handler
     * @return the ready to use handler
     * @throws HandlerException if the handler could not be created or has not been found
     */
    public Handler createHandler(String reference, String defaultContentType) throws HandlerException {
        HandlerDefinition definition = handlerConfiguration.getDefinitionByName(reference);

        if(definition == null) {
            throw new HandlerException(StatusCode.INTERNAL_SERVER_ERROR, "No handler matching for this request");
        }

        String className = definition.getClassName();

        try {
            Object o = Class.forName(className).newInstance();
            if (!(o instanceof Handler)) {
                throw new HandlerException(
                        StatusCode.INTERNAL_SERVER_ERROR, "Handler class does not implement the Handler interface");
            }
            Handler handler = (Handler)o;
            handler.setDefaultContentType(defaultContentType);
            return handler;
        } catch (InstantiationException e) {
            throw new HandlerException(
                    StatusCode.INTERNAL_SERVER_ERROR, "Cannot create handler instance. Null arg constructor given?", e);
        } catch (IllegalAccessException e) {
            throw new HandlerException(StatusCode.INTERNAL_SERVER_ERROR, "No access to this handler.", e);
        } catch (ClassNotFoundException e) {
            throw new HandlerException(StatusCode.INTERNAL_SERVER_ERROR, "Handler class found");
        }
    }

    /**
     * File-Ending Strategy. Checks the file ending to match with the ending supported by a specific handler.
     * @param headers the headers of this request
     * @return the handler name
     */
    private Matcher checkFileEnding(Map<String, String> headers) {
        Map<String, Matcher> fileEndingMatcher = matcherConfiguration.getFileEndingMatcher();
        Set<String> fileEndings = fileEndingMatcher.keySet();

        String uri = headers.get("request-uri");
        for (String fileEnding : fileEndings) {
            if(uri.endsWith(fileEnding)) {
                return fileEndingMatcher.get(fileEnding);
            }
        }
        return null;
    }

    /**
     * Path Strategy. Checks for the complete path to match. Makes sense for specific handling of a few paths.
     * @param headers the headers of this request
     * @return the handler name
     */
    private Matcher checkMatchingPath(Map<String, String> headers) {
        Map<String, Matcher> pathMatcher = matcherConfiguration.getPathMatcher();
        Set<String> paths = pathMatcher.keySet();

        for (String path : paths) {
            if (path.equals(headers.get("request-uri"))) {
                return pathMatcher.get(path);
            }
        }
        return null;
    }
}

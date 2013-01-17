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
package de.grobmeier.lionbeast.handlers;

import de.grobmeier.lionbeast.HTTPHeader;
import de.grobmeier.lionbeast.RequestHeaders;
import de.grobmeier.lionbeast.StatusCode;
import de.grobmeier.lionbeast.configuration.Configurator;
import de.grobmeier.lionbeast.configuration.HandlerConfiguration;
import de.grobmeier.lionbeast.configuration.HandlerDefinition;
import de.grobmeier.lionbeast.configuration.Matcher;
import de.grobmeier.lionbeast.configuration.MatcherConfiguration;

import java.util.Map;
import java.util.Set;

/**
 * Class which creates handlers, either based on request or manually.
 *
 * Handlers are specified in lionbeast-handlers.xml.
 */
public class HandlerFactory {
    private HandlerConfiguration handlerConfiguration = Configurator.getInstance().getHandlerConfiguration();
    private MatcherConfiguration matcherConfiguration = Configurator.getInstance().getMatcherConfiguration();

    /**
     * Creates a handler based on the (header) information from the request.
     *
     * @param requestHeaders the RequestHeaders for which a handler needs to be created
     * @return the ready to use handler
     * @throws HandlerException if the handler could not be created or has not been found
     */
    public Handler createHandler(RequestHeaders requestHeaders) throws HandlerException {
        Map<String, String> headers = requestHeaders.getHeaders();

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

        // TODO get by type
        String uri = headers.get(HTTPHeader.LIONBEAST_REQUEST_URI.toString());
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
            // TODO get by type
            if (path.equals(headers.get(HTTPHeader.LIONBEAST_REQUEST_URI.toString()))) {
                return pathMatcher.get(path);
            }
        }
        return null;
    }
}

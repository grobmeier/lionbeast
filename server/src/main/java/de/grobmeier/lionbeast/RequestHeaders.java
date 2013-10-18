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
package de.grobmeier.lionbeast;

import de.grobmeier.lionbeast.configuration.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds the of HTTP Request-Headers.
 */
public class RequestHeaders {
    private static final Logger logger = LoggerFactory.getLogger(RequestHeaders.class);

    private Map<String, String> headers = new HashMap<String, String>();

    /**
     * Checks and (if it matches) replaces the request-uri with the uri of the welcome file.
     *
     * For example, if domain.tld is given, it might become domain.tld/index.html
     */
    public void normalizeWelcomeFile() {
        if ("/".equals(this.getHeader(HTTPHeader.LIONBEAST_REQUEST_URI))) {
            logger.debug("Overwriting request-uri with welcome file (leaving original status line intact)");
            this.addHeader(
                HTTPHeader.LIONBEAST_REQUEST_URI,
                Configurator.getInstance().getServerConfiguration().welcomeFile());
        }
    }

    /**
     * Sets the header map
     * @param headers the headers
     */
    public void setHeaders(Map<String, String> headers) {
        if(headers == null) {
            headers = new HashMap<String, String>();
            logger.warn("Setting null value to RequestHeaders. Creating empty header map instead.");
        }
        this.headers = headers;
    }

    /**
     * Returns a header by its {@link HTTPHeader} type
     * @param headerType the header type
     * @return the header value
     */
    public String getHeader(HTTPHeader headerType) {
        return this.headers.get(headerType.toString());
    }

    /**
     * Adds a header
     * @param key the header key, like "Content-Type"
     * @param value the header value, like "keep-alive", "close", etc
     */
    public void addHeader(String key, String value) {
        this.headers.put(key, value);
    }

    /**
     * Adds a header
     * @param headerType the header type
     * @param value the header value, like "keep-alive", "close", etc
     */
    public void addHeader(HTTPHeader headerType, String value) {
        this.headers.put(headerType.toString(), value);
    }

    /**
     * Returns the header map
     * @return the headers
     */
    public Map<String, String> getHeaders() {
        return headers;
    }
}

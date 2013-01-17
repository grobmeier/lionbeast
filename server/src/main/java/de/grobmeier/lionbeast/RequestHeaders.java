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
     * Returns the header map
     * @return the headers
     */
    public Map<String, String> getHeaders() {
        return headers;
    }
}

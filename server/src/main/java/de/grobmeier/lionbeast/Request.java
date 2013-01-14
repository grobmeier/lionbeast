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

import java.util.HashMap;
import java.util.Map;

/**
 * Representation of a HTTP request.
 *
 * Actually this request is very basic; no data is included, just headers.
 */
public class Request {
    private Map<String, String> headers = new HashMap<String, String>();

    /**
     * Sets the header map
     * @param headers the headers
     */
    public void setHeaders(Map<String, String> headers) {
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

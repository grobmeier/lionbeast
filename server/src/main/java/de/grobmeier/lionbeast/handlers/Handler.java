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

import de.grobmeier.lionbeast.RequestHeaders;

import java.nio.channels.Pipe;
import java.util.concurrent.Callable;

/**
 * A handler is an object which actually works on the resource selected by the url.
 * It is not necessary selecting a file to operate with; it can dynamically create its own content,
 * run scripting machines and so on.
 */
public interface Handler extends Callable<Boolean> {

    /**
     * Sets the sink channel to which handlers should write
     * @param sinkChannel the channel to write
     */
    void setChannel(Pipe.SinkChannel sinkChannel);

    /**
     * Sets the requestHeaders to the channel. The requestHeaders headers will be used for some operations.
     * @param requestHeaders the requestHeaders
     */
    void setRequestHeaders(RequestHeaders requestHeaders);

    /**
     * sets the default content type. The Handler can choose to not use the default.
     * @param contentType the content type
     */
    void setDefaultContentType(String contentType);

}

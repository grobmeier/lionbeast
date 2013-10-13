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
import de.grobmeier.lionbeast.StatusCode;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Hello World Handler prints just... hello world.
 * An example for not selecting from hard disc.
 */
public class HelloWorldHandler extends AbstractHandler {

    protected boolean doCall() throws HandlerException {
        this.streamStatusCode(StatusCode.OK);
        this.streamDefaultKeepAlive();
        this.streamHeader(HTTPHeader.CONTENT_TYPE, "text/html");

        // Get Data
        String result = "Hello <b>World</b>, what's up?";

        byte[] bytes = result.getBytes();
        this.streamHeader(HTTPHeader.CONTENT_LENGTH, Long.toString(bytes.length));

        this.streamData(ByteBuffer.wrap(bytes));

        return true;
    }
}

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Reports a server status.
 *
 * This is a very special handler, as it reports exceptions as final chance to communicate
 * to the client. No other handlers are expected to get information injected from the worker.
 */
public class ServerStatusHandler extends AbstractHandler {
    private static final Logger logger = LoggerFactory.getLogger(ServerStatusHandler.class);

    private HandlerException handlerException;
    private boolean keepAlive;

    @Override
    public Boolean call() throws HandlerException {
        try {
            if(handlerException == null) {
                logger.error("ServerStatusHandler is called without an exception.");
                handlerException = new HandlerException(StatusCode.INTERNAL_SERVER_ERROR);
            }

            this.streamStatusCode(handlerException.getStatusCode());

            if(keepAlive) {
                this.streamHeader(HTTPHeader.CONNECTION, "keep-alive");
            } else {
                this.streamHeader(HTTPHeader.CONNECTION, "close");
            }

            this.streamHeader(HTTPHeader.CONTENT_TYPE, "text/html");

            StringBuilder builder = new StringBuilder();
            builder
                .append("<html><head></head><body><h1>")
                .append(handlerException.getStatusCode().asInt())
                .append(" - ")
                .append(handlerException.getStatusCode().getReasonPhrase())
                .append("</h1>");


            builder.append("<pre>").append(handlerException.toString()).append("</pre>");
            builder.append(("</body></html>"));

            this.streamHeader(HTTPHeader.CONTENT_LENGTH, Integer.toString(builder.length()));

            this.streamData(ByteBuffer.wrap(builder.toString().getBytes()));
        }  finally {
            try {
                this.finish();
            } catch (IOException e) {
                throw new HandlerException(StatusCode.INTERNAL_SERVER_ERROR, "Could not close pipe");
            }
        }
        return Boolean.TRUE;
    }

    public void setHandlerException(HandlerException handlerException) {
        this.handlerException = handlerException;
    }

    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }
}

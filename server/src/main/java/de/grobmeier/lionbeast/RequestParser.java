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

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Parses the incoming HTTP request.
 *
 * Its a line wise parser which stops on a blank line ended with CRLF.
 * Client data should not be processed here, a better place is the Worker thread.
 *
 * This class is not thread safe.
 */
class RequestParser {
    private static final Logger logger = LoggerFactory.getLogger(RequestParser.class);

    /* the internal state of this parser */
    private enum State {
        NOT_STARTED, STARTED, HEADER_COMPLETED;
    }

    /* the current state */
    private State current = State.NOT_STARTED;

    private final static Charset UTF8_CHARSET = Charset.forName("UTF-8");

    private final static char CARRIAGE_RETURN = '\r';
    private final static char LINEFEED = '\n';

    private StringBuilder line = new StringBuilder();
    private Map<String, String> headers;

    /**
     * Returns a RequestHeaders object which contains the headers.
     *
     * @return the RequestHeaders
     */
    RequestHeaders request() {
        if (current == State.NOT_STARTED || current == State.STARTED) {
            logger.warn("RequestHeaders has not fully read yet");
        }

        RequestHeaders result = new RequestHeaders();
        result.setHeaders(headers);

        return result;
    }

    /**
     * Reads from a byte buffer and reasembles lines.
     *
     * @param buffer the received data from the client
     * @return true, if the end of headers have been reached
     */
    boolean onRead(ByteBuffer buffer) throws ServerException {
        CharBuffer charBuffer = UTF8_CHARSET.decode(buffer);

        while (charBuffer.hasRemaining()) {
            char c = charBuffer.get();

            // As per recommendation, ignore the first \r\n if buggy HTTP 1.0 clients send them
            if (current == State.NOT_STARTED && (c == CARRIAGE_RETURN || c == LINEFEED)) {
                continue;
            }

            current = State.STARTED;

            //  Linefeeds are not allowed inside datablocks and can be used as line terminators (without CR)
            if (c == LINEFEED) {
                if (line.length() == 0) {
                    // Header ends here
                    if (charBuffer.hasRemaining()) {
                        logger.warn("CharBuffer has remaining chars, but the end of the header has been reached");
                    }
                    logger.debug("Reached the end of header");
                    current = State.HEADER_COMPLETED;

                    if (logger.isDebugEnabled()) {
                        Set<String> keys = headers.keySet();
                        for (String key : keys) {
                            logger.debug("Found header: {} -> {}", key, headers.get(key));
                        }
                    }

                    return true;
                }
                interpretLine();
            }

            if (c != CARRIAGE_RETURN && c != LINEFEED) {
                line.append(c);
            }
        }
        return false;
    }

    /**
     * Interprets the currently read line, puts the information into the headers map
     * and resets the line field.
     *
     * Start-Line will be split by SP, according to:
     *
     * http://www.w3.org/Protocols/rfc2616/rfc2616-sec5.html#sec5
     * Request-Line = Method SP Request-URI SP HTTP-Version CRLF
     *
     * Example:
     *
     * GET /favicon.ico HTTP/1.1
     *
     * will be available as "start-line". The split parts will be available
     * as "method", "request-uri" and "http-version".
     *
     * Other headers are split by colon and stored as key/value pairs without further
     * processing.
     */
    private void interpretLine() throws ServerException {
        if (headers == null) {
            headers = new HashMap<String, String>();
            String startLine = line.toString();
            headers.put(HTTPHeader.LIONBEAST_STARTLINE.toString(), startLine);

            String[] split = startLine.split(" ");

            if(split.length != 3) {
                logger.error("Start-Line has not the expected format");
                throw new ServerException("Start-Line has not the expected format");
            }

            headers.put(HTTPHeader.LIONBEAST_METHOD.toString(), split[0]);
            headers.put(HTTPHeader.LIONBEAST_REQUEST_URI.toString(), split[1]);
            headers.put(HTTPHeader.LIONBEAST_HTTP_VERSION.toString(), split[2]);
        } else {
            String[] split = line.toString().split(":", 2);

            if(split.length != 2) {
                logger.error("Header has not the expected format");
                throw new ServerException("Header has not the expected format");
            }

            headers.put(split[0], split[1].trim());
        }

        // once the line has been interpreted, delete it
        line = new StringBuilder();
    }
}

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
 * Parses the incoming HTTP request headers.
 *
 * Its a line wise parser which stops on a blank line ended with CRLF.
 * Client data should not be processed here, a better place is the Worker thread.
 *
 * This class is not thread safe.
 */
class RequestParser {
    private static final Logger logger = LoggerFactory.getLogger(RequestParser.class);

    private static final String BLANK = " ";
    // TODO: also used in AbstractHandler
    private static final String HEADER_SEPARATOR = ":";

    /* the internal state of this parser */
    private enum State {
        NOT_STARTED, STARTED, HEADER_COMPLETED;
    }

    /* the current state */
    private State current = State.NOT_STARTED;

    private final static char CARRIAGE_RETURN = '\r';
    private final static char LINEFEED = '\n';

    private StringBuilder line = new StringBuilder();

    private RequestHeaders requestHeaders = null;

    /**
     * Returns a RequestHeaders object which contains the headers.
     *
     * @return the RequestHeaders
     */
    RequestHeaders getRequestHeaders() {
        if (current == State.NOT_STARTED || current == State.STARTED) {
            logger.warn("RequestHeaders has not fully read yet");
        }
        return requestHeaders;
    }

    /**
     * Reads from a byte buffer and reassembles lines.
     *
     * @param buffer the received data from the client
     * @return true, if the end of headers have been reached
     */
    boolean onRead(ByteBuffer buffer) throws ServerException {
        CharBuffer charBuffer = BufferUtils.decode(buffer);

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
                        final Map<String, String> headers = requestHeaders.getHeaders();
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
        if (requestHeaders == null) {
            requestHeaders = new RequestHeaders();

            String startLine = line.toString();

            requestHeaders.addHeader(HTTPHeader.LIONBEAST_STARTLINE, startLine);

            String[] split = startLine.split(BLANK);

            if(split.length != 3) {
                logger.error("Start-Line has not the expected format");
                throw new ServerException("Start-Line has not the expected format");
            }

            requestHeaders.addHeader(HTTPHeader.LIONBEAST_METHOD, split[0]);
            requestHeaders.addHeader(HTTPHeader.LIONBEAST_REQUEST_URI, split[1]);
            requestHeaders.addHeader(HTTPHeader.LIONBEAST_HTTP_VERSION, split[2]);
        } else {
            String[] split = line.toString().split(HEADER_SEPARATOR, 2);

            if(split.length != 2) {
                logger.error("Header has not the expected format");
                throw new ServerException("Header has not the expected format");
            }
            requestHeaders.addHeader(split[0], split[1].trim());
        }

        // once the line has been interpreted, delete it
        line = new StringBuilder();
    }
}

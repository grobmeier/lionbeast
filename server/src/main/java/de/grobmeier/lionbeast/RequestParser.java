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
 */
class RequestParser {
    private static final Logger logger = LoggerFactory.getLogger(RequestParser.class);

    enum State {
        NOT_STARTED, STARTED, HEADER_COMPLETED;
    }

    State current = State.NOT_STARTED;

    private final static Charset UTF8_CHARSET = Charset.forName("UTF-8");

    private final static char CARRIAGE_RETURN = '\r';
    private final static char LINEFEED = '\n';

    private StringBuilder sink = new StringBuilder();
    private StringBuilder line = new StringBuilder();

    private Map<String, String> headers;

    Request request() {
        if (current == State.NOT_STARTED || current == State.STARTED) {
            logger.warn("Request has not fully read yet");
        }

        Request result = new Request();
        result.setHeaders(headers);

        return result;
    }

    boolean onRead(ByteBuffer buffer) {
        CharBuffer charBuffer = UTF8_CHARSET.decode(buffer);
        sink.append( charBuffer.toString() );

        while(charBuffer.hasRemaining()) {
            char c = charBuffer.get();

            // As per recommendation, ignore the first \r\n if buggy HTTP 1.0 clients send them
            if (current == State.NOT_STARTED && (c == CARRIAGE_RETURN || c == LINEFEED)) {
                continue;
            }

            current = State.STARTED;

            // Assuming LINEFEEDs do not come as part of the header data, I can use them as line terminator for headers
            if (c == LINEFEED) {
                if(line.length() == 0) {
                    // Header ends here
                    if(charBuffer.hasRemaining()) {
                        logger.warn("CharBuffer has remaining chars, but the end of the header has been reached");
                    }
                    logger.debug("Reached the end of header");
                    current = State.HEADER_COMPLETED;

                    if(logger.isDebugEnabled()) {
                        Set<String> keys = headers.keySet();
                        for (String key : keys) {
                            logger.debug("Found header: {} -> {}", key, headers.get(key));
                        }
                    }

                    return true;
                }
                interpretLine();
            }

            if(c != CARRIAGE_RETURN && c != LINEFEED) {
                line.append(c);
            }
        }
        return false;
    }

    private void interpretLine() {
        logger.debug(line.toString());

        if(headers == null) {
            headers = new HashMap<String, String>();
            String startLine = line.toString();
            headers.put("start-line", startLine);

            // Startlines look like that: GET /favicon.ico HTTP/1.1
            // http://www.w3.org/Protocols/rfc2616/rfc2616-sec5.html#sec5
            // Request-Line = Method SP Request-URI SP HTTP-Version CRLF
            String[] startLineParts = startLine.split(" ");

            if(startLineParts.length != 3) {
                logger.error("Start-Line has not the expected size");
                // TODO: server can come done because of this. Throw exception, catch it in the main loop
                // throw new HttpException(400);
            }

            headers.put("method", startLineParts[0]);
            headers.put("request-uri", startLineParts[1]);
            headers.put("http-version", startLineParts[2]);

        } else {
            String[] split = line.toString().split(":");
            headers.put(split[0], split[1].trim());
        }


        line = new StringBuilder();
    }
}

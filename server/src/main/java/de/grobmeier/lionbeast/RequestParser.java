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
public class RequestParser {
    private static final Logger logger = LoggerFactory.getLogger(RequestParser.class);

    public enum State {
        NOT_STARTED, STARTED, HEADER_COMPLETED;
    }

    State current = State.NOT_STARTED;

    private final static Charset UTF8_CHARSET = Charset.forName("UTF-8");

    private final static char CARRIAGE_RETURN = '\r';
    private final static char LINEFEED = '\n';

    private StringBuilder sink = new StringBuilder();
    private StringBuilder line = new StringBuilder();

    private Map<String, String> headers;

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
            headers.put("start-line", line.toString());
        } else {
            String[] split = line.toString().split(":");
            headers.put(split[0], split[1].trim());
        }


        line = new StringBuilder();
    }
}

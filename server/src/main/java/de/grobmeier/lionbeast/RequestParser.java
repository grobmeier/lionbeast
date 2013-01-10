package de.grobmeier.lionbeast;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

/**
 * Parses the incoming HTTP request.
 */
public class RequestParser {

    public enum State {
        NOT_STARTED, COMPLETED, READING;
    }

    State current = State.NOT_STARTED;

    private final static Charset UTF8_CHARSET = Charset.forName("UTF-8");

    private StringBuilder sink = new StringBuilder();

    public boolean onRead(ByteBuffer buffer) {
        CharBuffer charBuffer = UTF8_CHARSET.decode(buffer);
        sink.append( charBuffer.toString() );

        CharSequence last = charBuffer.subSequence((charBuffer.length() - 4), charBuffer.length());

        return isTerminator(last);
    }

    private final static char CARRIAGE_RETURN = '\r';
    private final static char LINEFEED = '\n';

    private boolean isTerminator(CharSequence chars) {
        if(chars.length() != 4) {
            return false;
        }

        return (chars.charAt(0) == CARRIAGE_RETURN && chars.charAt(1) == LINEFEED &&
                chars.charAt(2) == CARRIAGE_RETURN && chars.charAt(3) == LINEFEED);
    }


}

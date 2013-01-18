package de.grobmeier.lionbeast;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

/**
 * A simple class for some common buffer operations
 */
public class BufferUtils {

    private final static Charset UTF8_CHARSET = Charset.forName("UTF-8");

    public static CharBuffer decode(ByteBuffer buffer) {
        return UTF8_CHARSET.decode(buffer);
    }

    public static ByteBuffer encode(String value) {
        return Charset.forName("UTF-8").encode(value.toString());
    }
}

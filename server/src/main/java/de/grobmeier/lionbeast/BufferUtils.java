package de.grobmeier.lionbeast;

import de.grobmeier.lionbeast.configuration.Configurator;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

/**
 * A simple class for some common buffer operations
 */
public class BufferUtils {

    private final static Charset CHARSET = Charset.forName(
            Configurator.getInstance().getServerConfiguration().serverEncoding());

    /**
     * Decodes a byte buffer to a char buffer with this servers encoding
     * @param buffer the buffer to decode
     * @return a char buffer decoded with the selected charset
     */
    public static CharBuffer decode(ByteBuffer buffer) {
        return CHARSET.decode(buffer);
    }

    /**
     * Encodes a string value to a byte buffer with this servers encoding
     * @param value the string to encode
     * @return the ByteBuffer
     */
    public static ByteBuffer encode(String value) {
        return CHARSET.encode(value);
    }
}

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
import de.grobmeier.lionbeast.Request;
import de.grobmeier.lionbeast.StatusCode;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.Pipe;

/**
 * An abstract handler implementation, providing serveral methods to write protocol,
 * CRLF, status code and more.
 * <p/>
 * This class intention is some kind of utility class, providing common operations for handlers.
 * It is not necessary to extend from this class; it is enough to implement the Handler interface
 * to become a full fledged handler.
 */
abstract class AbstractHandler implements Handler {
    protected static final String HEADER_SEPARATOR = ": ";

    /* must not be static to avoid multithreading problems - needs to rewind */
    protected ByteBuffer protocol = ByteBuffer.wrap("HTTP/1.1 ".getBytes());
    /* must not be static to avoid multithreading problems - needs to rewind */
    protected final ByteBuffer CRLF = ByteBuffer.wrap("\r\n".getBytes());

    /* are the headers streaming? */
    private boolean streamingHeaders = false;

    /* the place to write the content to */
    protected Pipe.SinkChannel sinkChannel;
    /* the request  */
    protected Request request;
    /* the default content type as defined in lionbeast-matchers.xml*/
    protected String defaultContentType;

    /**
     * Streams the status code (the whole line, which is protocol, status and phrase).
     * Please note: only HTTP 1.1 is supported.
     *
     * @param statusCode the status code to stream
     * @throws HandlerException if the output could not be written
     */
    protected void streamStatusCode(StatusCode statusCode) throws HandlerException {
        ByteBuffer statusLine = statusCode.getStatusLine();
        try {
            streamingHeaders = true;
            sinkChannel.write(protocol);
            sinkChannel.write(statusLine);
            sinkChannel.write(CRLF);
        } catch (IOException e) {
            throw new HandlerException(StatusCode.INTERNAL_SERVER_ERROR, "Cannot write to output channel");
        } finally {
            protocol.rewind();
            statusLine.rewind();
            CRLF.rewind();
        }
    }

    /**
     * streams the default keep alive for this request, based on request header information.
     *
     * @throws HandlerException if the output could not be written
     */
    protected void streamDefaultKeepAlive() throws HandlerException {
        this.streamHeader(HTTPHeader.CONNECTION,
                request.getHeaders().get(HTTPHeader.CONNECTION.toString()));
    }

    /**
     * streams the content type, base on lionbeast-matchers.xml
     *
     * @throws HandlerException if the output could not be written
     */
    protected void streamDefaultContentType() throws HandlerException {
        this.streamHeader(HTTPHeader.CONTENT_TYPE, this.defaultContentType);
    }

    /**
     * Streams a header (key : value)
     *
     * @param header  the header name
     * @param headerValue the header value
     * @throws HandlerException if the output could not be written
     */
    protected void streamHeader(HTTPHeader header, String headerValue) throws HandlerException {
        this.streamHeader(header.toString(), headerValue);
    }

    /**
     * Streams a header (key : value)
     *
     * @param headerName  the header name
     * @param headerValue the header value
     * @throws HandlerException if the output could not be written
     */
    protected void streamHeader(String headerName, String headerValue) throws HandlerException {
        if (!streamingHeaders) {
            throw new IllegalStateException("Need to stream status code before streaming headers");
        }

        String headerLine =
                new StringBuilder().append(headerName).append(HEADER_SEPARATOR).append(headerValue).toString();

        try {
            sinkChannel.write(ByteBuffer.wrap(headerLine.getBytes()));
            sinkChannel.write(CRLF);
        } catch (IOException e) {
            throw new HandlerException(StatusCode.INTERNAL_SERVER_ERROR, "Cannot write to output channel");
        } finally {
            CRLF.rewind();
        }
    }

    /**
     * Streams a buffer of data to the sink
     *
     * @param buffer the data to stream
     * @throws HandlerException if the output could not be written
     */
    protected void streamData(ByteBuffer buffer) throws HandlerException {
        try {
            if (streamingHeaders) {
                sinkChannel.write(CRLF);
            }
            sinkChannel.write(buffer);
        } catch (IOException e) {
            throw new HandlerException(StatusCode.INTERNAL_SERVER_ERROR, "Cannot write to output channel");
        } finally {
            if (streamingHeaders) {
                CRLF.rewind();
                streamingHeaders = false;
            }
        }
    }

    /**
     * streams a whole file to the sink and closes the input stream after reading.
     *
     * @param fis the file input stream to read.
     * @throws HandlerException if the output could not be written
     */
    protected void streamFile(FileInputStream fis) throws HandlerException {
        try {
            FileChannel fileChannel = fis.getChannel();
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

            while (fileChannel.read(byteBuffer) != -1) {
                byteBuffer.flip();
                this.streamData(byteBuffer);
                byteBuffer.clear();
            }
        } catch (IOException e) {
            throw new HandlerException(StatusCode.INTERNAL_SERVER_ERROR, "Cannot write to output channel");
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    throw new HandlerException(StatusCode.INTERNAL_SERVER_ERROR, "Cannot close file input stream");
                }
            }
        }
    }

    /**
     * Finishes the sink write
     *
     * @throws IOException if the sink could not be closed
     */
    protected void finish() throws IOException {
        sinkChannel.close();
    }

    @Override
    public void setChannel(Pipe.SinkChannel sinkChannel) {
        this.sinkChannel = sinkChannel;
    }

    @Override
    public void setRequest(Request request) {
        this.request = request;
    }

    @Override
    public void setDefaultContentType(String contentType) {
        this.defaultContentType = contentType;
    }
}

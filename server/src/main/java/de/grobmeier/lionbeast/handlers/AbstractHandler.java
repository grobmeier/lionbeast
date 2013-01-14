package de.grobmeier.lionbeast.handlers;

import de.grobmeier.lionbeast.Request;
import de.grobmeier.lionbeast.StatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.Pipe;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO: JavaDoc
 * <p/>
 * (c) 2013 Christian Grobmeier Software
 * All rights reserved.
 * mailto:cg@grobmeier.de
 */
abstract class AbstractHandler implements Handler {
    protected ByteBuffer protocol = ByteBuffer.wrap("HTTP/1.1 ".getBytes());
    private ByteBuffer CRLF = ByteBuffer.wrap("\r\n".getBytes());

    private boolean streamingHeaders = false;

    protected Pipe.SinkChannel sinkChannel;
    protected Request request;
    protected String defaultContentType;

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

    protected void streamDefaultContentType() throws HandlerException {
        this.streamHeaders("Content-Type", this.defaultContentType);
    }

    protected void streamHeaders(String headerName, String headerValue) throws HandlerException {
        if(!streamingHeaders) {
            throw new IllegalStateException("Need to stream status code before streaming headers");
        }

        String s = new StringBuilder().append(headerName).append(": ").append(headerValue).toString();

        try {
            sinkChannel.write(ByteBuffer.wrap(s.getBytes()));
            sinkChannel.write(CRLF);
        } catch (IOException e) {
            throw new HandlerException(StatusCode.INTERNAL_SERVER_ERROR, "Cannot write to output channel");
        } finally {
            CRLF.rewind();
        }
    }

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

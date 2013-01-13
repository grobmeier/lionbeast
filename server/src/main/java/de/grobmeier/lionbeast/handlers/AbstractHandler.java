package de.grobmeier.lionbeast.handlers;

import de.grobmeier.lionbeast.ContentType;
import de.grobmeier.lionbeast.Request;
import de.grobmeier.lionbeast.StatusCode;

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

    private Map<String, String> emptyHeaders = new HashMap<String, String>();

    protected ByteBuffer protocol = ByteBuffer.wrap("HTTP/1.1 ".getBytes());
    private ByteBuffer CRLF = ByteBuffer.wrap("\r\n".getBytes());

    private boolean streamingHeaders = false;
    protected Pipe.SinkChannel sinkChannel;
    protected Request request;
    protected String defaultContentType;

    protected void streamStatusCode(StatusCode statusCode) throws IOException {
        sinkChannel.write(protocol);
        protocol.rewind();
        ByteBuffer statusLine = statusCode.getStatusLine();
        sinkChannel.write(statusLine);
        statusLine.rewind();
        sinkChannel.write(CRLF);
        CRLF.rewind();
        streamingHeaders = true;
    }

    protected void streamDefaultContentType() throws IOException {
        this.streamHeaders("Content-Type", this.defaultContentType);
    }

    protected void streamHeaders(String headerName, String headerValue) throws IOException {
        if(!streamingHeaders) {
            throw new IllegalStateException("Need to stream status code before streaming headers");
        }

        String s = new StringBuilder().append(headerName).append(": ").append(headerValue).toString();
        sinkChannel.write(ByteBuffer.wrap(s.getBytes()));
        sinkChannel.write(CRLF);
        CRLF.rewind();
    }

    protected void streamData(ByteBuffer buffer) throws IOException {
        if(streamingHeaders) {
            sinkChannel.write(CRLF);
            CRLF.rewind();
            streamingHeaders = false;
        }
        sinkChannel.write(buffer);
    }

    protected void streamFile(String path) throws IOException {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(path);
            FileChannel fileChannel = fis.getChannel();
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

            while (fileChannel.read(byteBuffer) != -1) {
                byteBuffer.flip();
                this.streamData( byteBuffer );
                byteBuffer.clear();
            }
        } catch (FileNotFoundException e) {
            // TODO
            e.printStackTrace();
        } catch (IOException e) {
            // TODO
            e.printStackTrace();
        } finally {
            if (fis != null) {
                fis.close();
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

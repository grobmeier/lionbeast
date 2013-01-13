package de.grobmeier.lionbeast.handlers;

import de.grobmeier.lionbeast.StatusCode;
import de.grobmeier.lionbeast.configuration.Configurator;
import org.jruby.embed.ScriptingContainer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.nio.charset.Charset;

/**
 * Hello World Handler prints just... hello world.
 */
public class JRubyFileHandler extends AbstractHandler {
    @Override
    public Boolean call() throws HandlerException {
        FileInputStream fis;

        try {
            String requestUri = this.request.getHeaders().get("request-uri");
            String root = Configurator.getInstance().getServerConfiguration().documentRoot();

            fis = new FileInputStream(root + requestUri);

            this.streamStatusCode(StatusCode.OK);
            this.streamDefaultContentType();
            this.streamHeaders("Connection", "close"); // TODO

            ScriptingContainer container = new ScriptingContainer();
            container.setWriter(new WriterWrapper());
            container.runScriptlet(fis, requestUri);

        }  catch (FileNotFoundException e) {
            throw new HandlerException(StatusCode.NOT_FOUND);
        } finally {
            try {
                this.finish();
            } catch (IOException e) {
                throw new HandlerException(StatusCode.INTERNAL_SERVER_ERROR, "Could not close pipe");
            }
        }
        return Boolean.TRUE;
    }

    private class WriterWrapper extends Writer {
        @Override
        public void write(char[] chars, int i, int i2) throws IOException {
            String str = new String(chars);

            ByteBuffer buffer = Charset.forName("UTF-8").encode(str);
            try {
                streamData(buffer);
            } catch (HandlerException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void flush() throws IOException {
        }

        @Override
        public void close() throws IOException {
        }
    }
}

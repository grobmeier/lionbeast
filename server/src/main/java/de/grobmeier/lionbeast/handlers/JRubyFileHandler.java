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

import de.grobmeier.lionbeast.StatusCode;
import de.grobmeier.lionbeast.configuration.Configurator;
import org.jruby.embed.ScriptingContainer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;

/**
 * JRuby Handler.
 *
 * This class is not optimized, its just there for demonstration purposes.
 *
 * The ScriptingEngine creation and the setting of the writerWrapper does take a lot of time.
 * For example, in local tests call would take around 1.45 seconds.
 *
 * When initializing the ScriptingContainer only one time it would take the half of the time. Before doing so,
 * one needs to check if the JRuby Scripting Container would work in a multithreaded environment.
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
            this.streamDefaultKeepAlive();
            this.streamDefaultContentType();

            // Creation of the ScriptingContainer uses a lot of time. Should be optimized
            WriterWrapper writerWrapper = new WriterWrapper();

            ScriptingContainer container = new ScriptingContainer();
            container.setWriter(writerWrapper);
            container.runScriptlet(fis, requestUri);

            String result = writerWrapper.builder.toString();
            this.streamHeaders("Content-Length", Long.toString(result.length()));

            streamData(
                Charset.forName("UTF-8").encode(result.toString()));

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

    /*
     * Dear reader, I am sorry for this.
     */
    private class WriterWrapper extends Writer {
        StringBuilder builder = new StringBuilder();

        @Override
        public void write(char[] chars, int i, int i2) throws IOException {
            builder.append(chars);
        }

        @Override
        public void flush() throws IOException {
        }

        @Override
        public void close() throws IOException {
        }
    }
}

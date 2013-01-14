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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Hello World Handler prints just... hello world.
 */
public class FileHandler extends AbstractHandler {
    private static final Logger logger = LoggerFactory.getLogger(FileHandler.class);

    @Override
    public Boolean call() throws HandlerException {
        FileInputStream fis;

        try {
            String requestUri = this.request.getHeaders().get("request-uri");
            String root = Configurator.getInstance().getServerConfiguration().documentRoot();

            File file = new File(root + requestUri);
            long fileLength = file.length();
            fis = new FileInputStream(file);

            this.streamStatusCode(StatusCode.OK);

            logger.debug("Streaming file with content-length: {}", fileLength);
            this.streamHeaders("Content-Length", Long.toString(fileLength));

            this.streamDefaultKeepAlive();
            this.streamDefaultContentType();

            this.streamFile(fis);
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
}

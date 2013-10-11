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
import de.grobmeier.lionbeast.StatusCode;
import de.grobmeier.lionbeast.configuration.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * The file handler takes a file based on the request uri and searches it in the web folder.
 * If the file is not found, 404 will be delivered to the client.
 * If the file is found, first the content-length will be delivered, followed by the file.
 */
public class FileHandler extends AbstractHandler {
    private static final Logger logger = LoggerFactory.getLogger(FileHandler.class);

    @Override
    protected boolean doCall() throws HandlerException {
        try {
            String requestUri = this.requestHeaders.getHeader(HTTPHeader.LIONBEAST_REQUEST_URI);
            String root = Configurator.getInstance().getServerConfiguration().documentRoot();

            File file = new File(root + requestUri);
            if (!file.exists()) {
                throw new HandlerException(StatusCode.NOT_FOUND);
            }
            this.streamStatusCode(StatusCode.OK); // File has been found

            long fileLength = file.length();
            logger.debug("Streaming file with content-length: {}", fileLength);
            this.streamHeader(HTTPHeader.CONTENT_LENGTH, Long.toString(fileLength));

            this.streamDefaultKeepAlive();
            this.streamDefaultContentType();

            this.streamFile( new FileInputStream(file) );
        }  catch (FileNotFoundException e) {
            throw new HandlerException(StatusCode.NOT_FOUND);
        }
        return true;
    }
}

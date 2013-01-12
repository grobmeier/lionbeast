package de.grobmeier.lionbeast.handlers;

import de.grobmeier.lionbeast.ContentType;
import de.grobmeier.lionbeast.StatusCode;

import java.io.IOException;
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

    @Override
    public StatusCode prepare() throws IOException {
        return StatusCode.OK;
    }

    @Override
    public String getContentType() {
        return ContentType.TEXT_PLAIN.asString();
    }

    @Override
    public Map<String, String> getAdditionalHeaders() {
        return emptyHeaders;
    }
}

package de.grobmeier.lionbeast.handlers;

import java.io.IOException;

/**
 * TODO: JavaDoc
 * <p/>
 * (c) 2013 Christian Grobmeier Software
 * All rights reserved.
 * mailto:cg@grobmeier.de
 */
public interface Handler {

    enum State {
        OK, ERROR;
    }

    State handle() throws IOException;


}

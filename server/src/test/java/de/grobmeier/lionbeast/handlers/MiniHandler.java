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

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Just writes a few bytes. Enables argument capturing in WorkerTest
 */
public class MiniHandler extends AbstractHandler {
    public static final String MESSAGE = "HELLO";

    @Override
    protected boolean doCall() throws HandlerException {
        ByteBuffer bb = ByteBuffer.wrap(MESSAGE.getBytes());
        this.streamData(bb);
        return true;
    }
}

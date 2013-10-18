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
package de.grobmeier.lionbeast;

import java.nio.ByteBuffer;

/**
 * Enumeration of HTTP status codes as defined here:
 * http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html
 */
public enum StatusCode {
    OK(200, "OK"),
    BAD_REQUEST(400, "Bad Request"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not Found"),
    INTERNAL_SERVER_ERROR(505, "Internal Server Error");
    // many more missing

    private int code;
    private String reasonPhrase;
    private ByteBuffer statusLine;

    private StatusCode(int code, String reasonPhrase) {
        this.code = code;
        this.reasonPhrase = reasonPhrase;

        String s = new StringBuilder().append(this.code).append(" ").append(this.reasonPhrase).toString();
        statusLine = ByteBuffer.wrap(s.getBytes());
    }

    public int asInt() {
        return code;
    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }

    public ByteBuffer getStatusLine() {
        return statusLine;
    }
}

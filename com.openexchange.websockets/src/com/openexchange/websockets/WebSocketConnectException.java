/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.websockets;

/**
 * This exception is meant to be thrown during {@link WebSocketListener#onWebSocketConnect(WebSocket)}
 * to abort the websocket handshake between server and client.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.4
 */
public class WebSocketConnectException extends WebSocketRuntimeException {

    private static final long serialVersionUID = -167921331329598079L;

    private final int code;

    /**
     * Initializes a new {@link WebSocketConnectException}.
     *
     * @param message The message
     */
    public WebSocketConnectException(String message) {
        this(500, message);
    }

    /**
     * Initializes a new {@link WebSocketConnectException}.
     *
     * @param code The HTTP status code to abort the handshake with
     * @param message The message
     */
    public WebSocketConnectException(int code, String message) {
        this(code, message, null);
    }

    /**
     * Initializes a new {@link WebSocketConnectException}.
     *
     * @param code The HTTP status code to abort the handshake with
     * @param message The message
     * @param cause The cause
     */
    public WebSocketConnectException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    /**
     * Get the error code.
     *
     * @return the error code.
     */
    public int getCode() {
        return code;
    }

}

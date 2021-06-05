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
 * {@link WebSocketListener} - A Web Socket listener for receiving various call-backs on certain Web Socket events.
 * <p>
 * Listeners simply need to be OSGi-wise registered.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public interface WebSocketListener {

    /**
     * Invoked when a new session-bound Web Socket gets connected
     *
     * @param socket The connected Web Socket
     * @throws WebSocketConnectException to abort the client handshake with an HTTP error
     */
    void onWebSocketConnect(WebSocket socket);

    /**
     * Invoked when an existing session-bound Web Socket is about to be closed
     *
     * @param socket The socket to close
     */
    void onWebSocketClose(WebSocket socket);

    /**
     * Invoked when {@link WebSocket#onMessage(String)} has been called on a  particular {@link WebSocket} instance.
     *
     * @param socket The {@link WebSocket} that received a message.
     * @param text The message received.
     */
    void onMessage(WebSocket socket, String text);

}

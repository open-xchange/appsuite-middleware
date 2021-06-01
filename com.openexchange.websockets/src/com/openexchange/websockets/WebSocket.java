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

import java.util.Map;
import com.openexchange.exception.OXException;

/**
 * {@link WebSocket} - The representation of a session-bound Web Socket to send and receive data through a {@link WebSocketListener#onMessage(WebSocket, String) onMessage() call-back}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public interface WebSocket {

    /**
     * Gets the path that was used while this Web Socket was created; e.g. <code>"/websockets/foo/bar"</code>.
     *
     * @return The path
     */
    String getPath();

    /**
     * Gets the immutable map view for the available query parameters while this Web Socket was created; e.g. <code>"param1=foo&amp;param2=bar"</code>.
     *
     * @return The parameters (as immutable map)
     */
    Map<String, String> getParameters();

    /**
     * Gets the value for the denoted query parameter.
     *
     * @param parameterName The parameter name
     * @return The parameters value or <code>null</code> (if no such parameter was available while this Web Socket was created)
     */
    String getParameter(String parameterName);

    /**
     * gets this Web Socket's connection identifier.
     *
     * @return The connection identifier
     */
    ConnectionId getConnectionId();

    /**
     * Gets the identifier of the session currently associated with this Web Socket.
     *
     * @return The session identifier
     */
    String getSessionId();

    /**
     * Gets the user identifier
     *
     * @return The user identifier
     */
    int getUserId();

    /**
     * Gets the context identifier
     *
     * @return The context identifier
     */
    int getContextId();

    /**
     * Gets the Web Socket session to store states.
     *
     * @return The Web Socket session
     */
    WebSocketSession getWebSocketSession();

    /**
     * Applies a certain message transcoder to this Web Socket.
     * <p>
     * Every inbound and outbound messages are routed through that transcoder.
     *
     * @param transcoder The transcode to set
     */
    void setMessageTranscoder(MessageTranscoder transcoder);

    /**
     * Gets the scheme identifier for the currently active message transcoder.
     *
     * @return The scheme identifier or <code>null</code> if no transcoder is in place
     */
    String getMessageTranscoderScheme();

    /**
     * Sends a message to the remote end-point.
     * <p>
     * A previously set {@link MessageTranscoder transcoder} kicks-in.
     *
     * @param message The message to be sent
     * @return The handler which will be notified of progress
     * @throws OXException If there is a problem delivering the message.
     */
    SendControl sendMessage(String message) throws OXException;

    /**
     * Sends a message to the remote end-point.
     * <p>
     * No {@link MessageTranscoder transcoder} kicks-in.
     *
     * @param message The message to be sent
     * @return The handler which will be notified of progress
     * @throws OXException If there is a problem delivering the message.
     */
    SendControl sendMessageRaw(String message) throws OXException;

    /**
     * Closes this {@link WebSocket}.
     */
    void close();

}

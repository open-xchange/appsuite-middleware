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
 * {@link MessageTranscoder} - Transcodes inbound/outbound Web Socket messages.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public interface MessageTranscoder {

    /**
     * Gets the identifier for this transcoder.
     *
     * @return The identifier
     */
    String getId();

    /**
     * Invoked when a message has arrived on the {@link WebSocket} instance associated with this transcoder.
     *
     * @param socket The Web Socket that received the message
     * @param message The message received.
     * @return The transcoded message to forward to listeners or <code>null</code> to forward nothing
     */
    String onInboundMessage(WebSocket socket, String message);

    /**
     * Invoked when a message is supposed to be sent on the {@link WebSocket} instance associated with this transcoder.
     *
     * @param socket The Web Socket that is supposed to send the message
     * @param message The message to send.
     * @return The transcoded message to send to remote end-point or <code>null</code> to send nothing
     */
    String onOutboundMessage(WebSocket socket, String message);

}

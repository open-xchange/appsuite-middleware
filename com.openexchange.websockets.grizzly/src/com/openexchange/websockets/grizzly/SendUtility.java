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

package com.openexchange.websockets.grizzly;

import org.glassfish.grizzly.websockets.DataFrame;
import org.glassfish.grizzly.websockets.ProtocolHandler;
import com.openexchange.exception.OXException;
import com.openexchange.websockets.SendControl;
import com.openexchange.websockets.WebSocketExceptionCodes;

/**
 * {@link SendUtility} - Utility class for sending messages via a given Web Socket.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class SendUtility {

    /**
     * Initializes a new {@link SendUtility}.
     */
    private SendUtility() {
        super();
    }

    /**
     * Sends specified message via given Web Socket.
     *
     * @param message The message to send
     * @param webSocket The Web Socket to send by
     * @return The send-control
     * @throws OXException On illegal arguments or if given Web Socket is currently not connected
     */
    public static SendControl sendMessage(String message, SessionBoundWebSocket webSocket) throws OXException {
        if (null == message) {
            throw WebSocketExceptionCodes.UNEXPECTED_ERROR.create(new IllegalArgumentException("Message must not be null"));
        }
        if (null == webSocket) {
            throw WebSocketExceptionCodes.UNEXPECTED_ERROR.create(new IllegalArgumentException("Web Socket must not be null"));
        }

        if (false == webSocket.isConnected()) {
            throw WebSocketExceptionCodes.NOT_CONNECTED.create();
        }

        // Yield data-frame for given text message
        ProtocolHandler protocolHandler = webSocket.getProtocolHandler();
        DataFrame frameToSend = protocolHandler.toDataFrame(message);
        frameToSend.getBytes(); // Pre-generate bytes to prevent possible NPE in DataFrame.toString()

        // Perform the send
        return new FutureBackedSendControl(protocolHandler.send(frameToSend));
    }

}

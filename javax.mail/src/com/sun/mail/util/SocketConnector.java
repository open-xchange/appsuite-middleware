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

package com.sun.mail.util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * {@link SocketConnector} - Connects a given socket.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.4
 */
public interface SocketConnector {

    /**
     * Connects given socket using specified arguments.
     * 
     * @param socket The socket to connect
     * @param socketAddress The socket address to connect against
     * @param connectTimeout The connect timeout or <code>-1</code>
     * @param protocolInfo The protocol info
     * @throws IOException If an I/O error occurs
     */
    default void connectSocket(Socket socket, InetSocketAddress socketAddress, int connectTimeout, ProtocolInfo protocolInfo) throws IOException {
        if (connectTimeout >= 0) {
            socket.connect(socketAddress, connectTimeout);
        } else {
            socket.connect(socketAddress);
        }
    }
    
}

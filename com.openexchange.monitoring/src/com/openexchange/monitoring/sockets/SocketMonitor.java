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

package com.openexchange.monitoring.sockets;

import java.io.IOException;
import java.net.Socket;
import com.openexchange.monitoring.sockets.failure.ConnectFailure;
import com.openexchange.monitoring.sockets.failure.IOFailure;
import com.openexchange.monitoring.sockets.failure.TimeoutFailure;

/**
 * {@link SocketMonitor} - Receives call-backs whenever bytes are read from / written to socket.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public interface SocketMonitor {

    /**
     * Invoked when a single byte is written to socket.
     *
     * @param socket The socket to which is written
     * @param data The written byte
     * @throws IOException If an I/O error occurs
     */
    void write(Socket socket, int data) throws IOException;

    /**
     * Invoked when bytes are written to socket.
     *
     * @param socket The socket to which is written
     * @param data The byte chunk which is written
     * @param off The offset
     * @param len The number of written bytes
     * @throws IOException If an I/O error occurs
     */
    void write(Socket socket, byte[] data, int off, int len) throws IOException;

    /**
     * Invoked when a single byte is read from socket
     *
     * @param socket The socket from which is read
     * @param data The byte that is read
     * @throws IOException If an I/O error occurs
     */
    void read(Socket socket, int data) throws IOException;

    /**
     * Invoked when bytes are read from socket
     *
     * @param socket The socket from which is read
     * @param data The bytes that are read
     * @param off The offset
     * @param len The number of read bytes
     * @throws IOException If an I/O error occurs
     */
    void read(Socket socket, byte[] data, int off, int len) throws IOException;

    /**
     * Invoked when EOF is reached for specified socket.
     *
     * @param socket The socket
     * @throws IOException If an I/O error occurs
     */
    void eof(Socket socket) throws IOException;

    /**
     * Invoked when specified socket gets closed
     *
     * @param socket The socket which is closed
     * @throws IOException If an I/O error occurs
     */
    void closed(Socket socket) throws IOException;

    /**
     * Invoked when specified socket is connected
     *
     * @param socket The socket which is connected
     * @throws IOException If an I/O error occurs
     */
    void connected(Socket socket) throws IOException;

    /**
     * Invoked when specified socket could not be connected to remote address and port.
     *
     * @param socket The socket which could not be connected
     * @param failure The connect failure that occurred
     * @throws IOException If an I/O error occurs
     */
    void connectError(Socket socket, ConnectFailure failure) throws IOException;

    /**
     * Invoked when a read timeout occurs for specified socket.
     *
     * @param socket The socket
     * @param failure The timeout failure that occurred
     * @throws IOException If an I/O error occurs
     */
    void readTimedOut(Socket socket, TimeoutFailure failure) throws IOException;

    /**
     * Invoked when a read encountered an error for specified socket.
     *
     * @param socket The socket
     * @param failure The I/O failure that occurred
     * @throws IOException If an I/O error occurs
     */
    void readError(Socket socket, IOFailure failure) throws IOException;

}

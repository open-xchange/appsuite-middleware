/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.monitoring.sockets;

import java.io.IOException;
import java.net.Socket;

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

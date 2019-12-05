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

package com.openexchange.monitoring.impl.sockets;

import static com.openexchange.java.Autoboxing.I;
import java.io.IOException;
import java.net.Socket;
import java.util.Optional;
import org.slf4j.Logger;
import com.openexchange.logging.Constants;
import com.openexchange.monitoring.sockets.SocketLoggerUtil;
import com.openexchange.monitoring.sockets.SocketMonitor;
import com.openexchange.monitoring.sockets.failure.ConnectFailure;
import com.openexchange.monitoring.sockets.failure.IOFailure;
import com.openexchange.monitoring.sockets.failure.TimeoutFailure;
import com.openexchange.server.ServiceLookup;

/**
 * {@link LoggingSocketMonitor}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.3
 */
public class LoggingSocketMonitor implements SocketMonitor {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link LoggingSocketMonitor}.
     *
     * @param services The {@link ServiceLookup} instance
     */
    public LoggingSocketMonitor(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public void write(Socket socket, int data) throws IOException {
        logSocketData(data);
    }

    @Override
    public void write(Socket socket, byte[] data, int off, int len) throws IOException {
        logSocketData(data, off, len);
    }

    @Override
    public void read(Socket socket, int data) throws IOException {
        logSocketData(data);
    }

    @Override
    public void read(Socket socket, byte[] data, int off, int len) throws IOException {
        logSocketData(data, off, len);
    }

    @Override
    public void eof(Socket socket) throws IOException {
        logSocketAction("Encountered EOF from {}:{}", socket);
    }

    @Override
    public void closed(Socket socket) throws IOException {
        logSocketAction("Closed connection to {}:{}", socket);
    }

    @Override
    public void connected(Socket socket) throws IOException {
        logSocketAction("Opened connection to {}:{}", socket);
    }

    @Override
    public void connectError(Socket socket, ConnectFailure failure) throws IOException {
        logSocketAction("Connection failed to {}:{}", socket);
    }

    @Override
    public void readTimedOut(Socket socket, TimeoutFailure failure) throws IOException {
        logSocketAction("Read timed out from {}:{}", socket);
    }

    @Override
    public void readError(Socket socket, IOFailure failure) throws IOException {
        logSocketAction("Failed to read from {}:{}", socket);
    }

    /**
     * Logs the specified data
     *
     * @param data The data to log
     * @param off The starting offset
     * @param len The length
     */
    private void logSocketData(byte[] data, int off, int len) {
        Optional<Logger> logger = SocketLoggerUtil.getLoggerForPlainSocket(services);
        logger.ifPresent((l) -> l.trace(Constants.DROP_MDC_MARKER, SocketLoggerUtil.prepareForLogging(data, off, len)));
    }

    /**
     * Logs the specified data
     *
     * @param data The data to log
     */
    private void logSocketData(int data) {
        Optional<Logger> logger = SocketLoggerUtil.getLoggerForPlainSocket(services);
        logger.ifPresent((l) -> l.trace(Constants.DROP_MDC_MARKER, new String(new char[] { (char) data })));
    }

    /**
     * Logs the specified {@link Socket} action
     *
     * @param pattern The message pattern
     * @param socket The {@link Socket}
     */
    private void logSocketAction(String pattern, Socket socket) {
        Optional<Logger> logger = SocketLoggerUtil.getLoggerForPlainSocket(services);
        logger.ifPresent((l) -> l.trace(Constants.DROP_MDC_MARKER, pattern, socket.getInetAddress().toString(), I(socket.getPort())));
    }
}

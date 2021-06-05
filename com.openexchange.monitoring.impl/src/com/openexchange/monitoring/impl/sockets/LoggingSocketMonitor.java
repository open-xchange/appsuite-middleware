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

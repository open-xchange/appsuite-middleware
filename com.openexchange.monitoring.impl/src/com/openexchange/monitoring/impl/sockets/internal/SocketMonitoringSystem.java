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

package com.openexchange.monitoring.impl.sockets.internal;

import static com.openexchange.monitoring.impl.sockets.internal.MonitoringSocketFactory.isDisabled;
import java.io.IOException;
import java.net.Socket;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;
import com.openexchange.monitoring.sockets.SocketMonitor;
import com.openexchange.monitoring.sockets.SocketMonitoringService;
import com.openexchange.monitoring.sockets.failure.ConnectFailure;
import com.openexchange.monitoring.sockets.failure.IOFailure;
import com.openexchange.monitoring.sockets.failure.TimeoutFailure;

/**
 * {@link SocketMonitoringSystem}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class SocketMonitoringSystem implements SocketMonitoringService {

    private final static SocketMonitoringSystem INSTANCE = new SocketMonitoringSystem();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static SocketMonitoringSystem getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes the socket monitoring.
     * <p>
     * <div style="margin-left: 0.1in; margin-right: 0.5in; margin-bottom: 0.1in; background-color:#FFDDDD;">
     * This should only be called when using the reflection approach (<code>com.openexchange.java.delegate.Delegator</code>).
     * </div>
     */
    public static void initForDelegator() throws IOException {
        MonitoringSocketFactory.initMonitoringSocketFactory();
    }

    /**
     * Shuts-down the socket monitoring.
     * <p>
     * <div style="margin-left: 0.1in; margin-right: 0.5in; margin-bottom: 0.1in; background-color:#FFDDDD;">
     * This should only be called when using the reflection approach (<code>com.openexchange.java.delegate.Delegator</code>).
     * </div>
     */
    public static void shutDown() {
        MonitoringSocketFactory.stopMonitoringSocketFactory();
    }

    // -------------------------------------------------------------------------------------------------------------------

    private final Collection<SocketMonitor> monitors = new CopyOnWriteArraySet<SocketMonitor>();

    private SocketMonitoringSystem() {
        super();
    }

    @Override
    public void add(SocketMonitor monitor) {
        monitors.add(monitor);
    }

    @Override
    public void remove(SocketMonitor monitor) {
        monitors.remove(monitor);
    }

    void write(Socket socket, int data) throws IOException {
        if (isDisabled()) {
            return;
        }

        for (SocketMonitor monitor : monitors) {
            monitor.write(socket, data);
        }
    }

    void write(Socket socket, byte[] data, int offset, int length) throws IOException {
        if (isDisabled()) {
            return;
        }

        for (SocketMonitor monitor : monitors) {
            monitor.write(socket, data, offset, length);
        }
    }

    void read(Socket socket, int data) throws IOException {
        if (isDisabled()) {
            return;
        }

        for (SocketMonitor monitor : monitors) {
            monitor.read(socket, data);
        }
    }

    void read(Socket socket, byte[] data, int offset, int length) throws IOException {
        if (isDisabled()) {
            return;
        }

        for (SocketMonitor monitor : monitors) {
            monitor.read(socket, data, offset, length);
        }
    }

    void closed(Socket socket) throws IOException {
        if (isDisabled()) {
            return;
        }

        for (SocketMonitor monitor : monitors) {
            monitor.closed(socket);
        }
    }

    void connected(Socket socket) throws IOException {
        if (isDisabled()) {
            return;
        }

        for (SocketMonitor monitor : monitors) {
            monitor.connected(socket);
        }
    }

    void readTimedOut(Socket socket, TimeoutFailure e) throws IOException {
        if (isDisabled()) {
            return;
        }

        for (SocketMonitor monitor : monitors) {
            monitor.readTimedOut(socket, e);
        }
    }

    void readError(Socket socket, IOFailure e) throws IOException {
        if (isDisabled()) {
            return;
        }

        for (SocketMonitor monitor : monitors) {
            monitor.readError(socket, e);
        }
    }

    void eof(Socket socket) throws IOException {
        if (isDisabled()) {
            return;
        }

        for (SocketMonitor monitor : monitors) {
            monitor.eof(socket);
        }
    }

    void connectError(Socket socket, ConnectFailure e) throws IOException {
        if (isDisabled()) {
            return;
        }

        for (SocketMonitor monitor : monitors) {
            monitor.connectError(socket, e);
        }
    }
}

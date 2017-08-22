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

package com.openexchange.monitoring.sockets.internal;

import static com.openexchange.monitoring.sockets.internal.MonitoringSocketFactory.isDisabled;
import java.io.IOException;
import java.net.Socket;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;
import com.openexchange.monitoring.sockets.ConnectFailure;
import com.openexchange.monitoring.sockets.IOFailure;
import com.openexchange.monitoring.sockets.SocketMonitor;
import com.openexchange.monitoring.sockets.SocketMonitoringService;
import com.openexchange.monitoring.sockets.TimeoutFailure;

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

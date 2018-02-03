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

import static com.openexchange.java.delegate.Delegator.Options.options;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketImpl;
import com.openexchange.java.delegate.DelegationExecutionException;
import com.openexchange.java.delegate.Delegator;
import com.openexchange.monitoring.sockets.ConnectFailure;

/**
 * {@link MonitoringSocketImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class MonitoringSocketImpl extends SocketImpl {

    private final Delegator<SocketImpl> delegator;
    private final Field socket;

    /**
     * Initializes a new {@link MonitoringSocketImpl}.
     *
     * @throws SecurityException If access violates security policies
     * @throws NoSuchFieldException If <code>"socket"</code> field cannot be found in class {@link SocketImpl}
     */
    public MonitoringSocketImpl() throws NoSuchFieldException, SecurityException {
        super();
        this.delegator = new Delegator<SocketImpl>(this, SocketImpl.class, "java.net.SocksSocketImpl");
        socket = SocketImpl.class.getDeclaredField("socket");
        socket.setAccessible(true);
    }

    private Socket getSocket0() throws IOException {
        try {
            return (Socket) socket.get(this);
        } catch (Exception e) {
            throw new IOException("Could not discover real socket", e);
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        InputStream real = delegator.invoke(options().withMethodName("getInputStream"));
        return new SocketMonitoringInputStream(getSocket0(), real);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        OutputStream real = delegator.invoke(options().withMethodName("getOutputStream"));
        return new SocketMonitoringOutputStream(getSocket0(), real);
    }

    // ---------------------------------------- Rest of the class is plain delegation to real SocketImpl ----------------------------------------

    @Override
    public void create(boolean stream) throws IOException {
        try {
            delegator.invoke(options().withMethodName("create").withArgs(Boolean.valueOf(stream)));
        } catch (DelegationExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw (IOException) cause;
            }
            throw new IOException(cause.getMessage(), cause);
        }
    }

    @Override
    public void connect(String host, int port) throws IOException {
        long st = System.currentTimeMillis();
        try {
            delegator.invoke(options().withMethodName("connect").withArgs(host, Integer.valueOf(port)));
            SocketMonitoringSystem.getInstance().connected(getSocket0());
        } catch (DelegationExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof java.net.ConnectException) {
                // An error occurred while attempting to connect a socket to a remote address and port.
                java.net.ConnectException connectException = (java.net.ConnectException) cause;
                long millis = System.currentTimeMillis() - st;
                SocketMonitoringSystem.getInstance().connectError(getSocket0(), new ConnectFailure(connectException, millis));
                throw connectException;
            }
            if (cause instanceof IOException) {
                throw (IOException) cause;
            }
            throw new IOException(cause.getMessage(), cause);
        }
    }

    @Override
    public void connect(InetAddress address, int port) throws IOException {
        long st = System.currentTimeMillis();
        try {
            delegator.invoke(options().withMethodName("connect").withArgs(address, Integer.valueOf(port)));
            SocketMonitoringSystem.getInstance().connected(getSocket0());
        } catch (DelegationExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof java.net.ConnectException) {
                // An error occurred while attempting to connect a socket to a remote address and port.
                java.net.ConnectException connectException = (java.net.ConnectException) cause;
                long millis = System.currentTimeMillis() - st;
                SocketMonitoringSystem.getInstance().connectError(getSocket0(), new ConnectFailure(connectException, millis));
                throw connectException;
            }
            if (cause instanceof IOException) {
                throw (IOException) cause;
            }
            throw new IOException(cause.getMessage(), cause);
        }
    }

    @Override
    public void connect(SocketAddress address, int timeout) throws IOException {
        long st = System.currentTimeMillis();
        try {
            delegator.invoke(options().withMethodName("connect").withArgs(address, Integer.valueOf(timeout)));
            SocketMonitoringSystem.getInstance().connected(getSocket0());
        } catch (DelegationExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof java.net.ConnectException) {
                // An error occurred while attempting to connect a socket to a remote address and port.
                java.net.ConnectException connectException = (java.net.ConnectException) cause;
                long millis = System.currentTimeMillis() - st;
                SocketMonitoringSystem.getInstance().connectError(getSocket0(), new ConnectFailure(connectException, millis));
                throw connectException;
            }
            if (cause instanceof IOException) {
                throw (IOException) cause;
            }
            throw new IOException(cause.getMessage(), cause);
        }
    }

    @Override
    public void bind(InetAddress host, int port) throws IOException {
        try {
            delegator.invoke(options().withMethodName("bind").withArgs(host, Integer.valueOf(port)));
        } catch (DelegationExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw (IOException) cause;
            }
            throw new IOException(cause.getMessage(), cause);
        }
    }

    @Override
    public void listen(int backlog) throws IOException {
        try {
            delegator.invoke(options().withMethodName("listen").withArgs(Integer.valueOf(backlog)));
        } catch (DelegationExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw (IOException) cause;
            }
            throw new IOException(cause.getMessage(), cause);
        }
    }

    @Override
    public void accept(SocketImpl s) throws IOException {
        try {
            delegator.invoke(options().withMethodName("accept").withArgs(s));
        } catch (DelegationExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw (IOException) cause;
            }
            throw new IOException(cause.getMessage(), cause);
        }
    }

    @Override
    public int available() throws IOException {
        try {
            Integer result = delegator.invoke(options().withMethodName("available"));
            return result.intValue();
        } catch (DelegationExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw (IOException) cause;
            }
            throw new IOException(cause.getMessage(), cause);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            Socket socket = getSocket0();
            delegator.invoke(options().withMethodName("close"));
            SocketMonitoringSystem.getInstance().closed(socket);
        } catch (DelegationExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw (IOException) cause;
            }
            throw new IOException(cause.getMessage(), cause);
        }
    }

    @Override
    public void shutdownInput() throws IOException {
        try {
            delegator.invoke(options().withMethodName("shutdownInput"));
        } catch (DelegationExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw (IOException) cause;
            }
            throw new IOException(cause.getMessage(), cause);
        }
    }

    @Override
    public void shutdownOutput() throws IOException {
        try {
            delegator.invoke(options().withMethodName("shutdownOutput"));
        } catch (DelegationExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw (IOException) cause;
            }
            throw new IOException(cause.getMessage(), cause);
        }
    }

    @Override
    public FileDescriptor getFileDescriptor() {
        return delegator.invoke(options().withMethodName("getFileDescriptor"));
    }

    @Override
    public InetAddress getInetAddress() {
        return delegator.invoke(options().withMethodName("getInetAddress"));
    }

    @Override
    public int getPort() {
        Integer result = delegator.invoke(options().withMethodName("getPort"));
        return result.intValue();
    }

    @Override
    public boolean supportsUrgentData() {
        Boolean result = delegator.invoke(options().withMethodName("supportsUrgentData"));
        return result.booleanValue();
    }

    @Override
    public void sendUrgentData(int data) throws IOException {
        try {
            delegator.invoke(options().withMethodName("sendUrgentData").withArgs(Integer.valueOf(data)));
        } catch (DelegationExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw (IOException) cause;
            }
            throw new IOException(cause.getMessage(), cause);
        }
    }

    @Override
    public int getLocalPort() {
        Integer result = delegator.invoke(options().withMethodName("getLocalPort"));
        return result.intValue();
    }

    @Override
    public String toString() {
        return delegator.invoke(options().withMethodName("toString"));
    }

    @Override
    public void setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
        delegator.invoke(options().withMethodName("setPerformancePreferences").withArgs(Integer.valueOf(connectionTime), Integer.valueOf(latency), Integer.valueOf(bandwidth)));
    }

    @Override
    public void setOption(int optID, Object value) throws SocketException {
        try {
            delegator.invoke(options().withMethodName("setOption").withArgs(Integer.valueOf(optID), value));
        } catch (DelegationExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof SocketException) {
                throw (SocketException) cause;
            }
            SocketException se = new SocketException(cause.getMessage());
            se.initCause(cause);
            throw se;
        }
    }

    @Override
    public Object getOption(int optID) throws SocketException {
        try {
            return delegator.invoke(options().withMethodName("getOption").withArgs(Integer.valueOf(optID)));
        } catch (DelegationExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof SocketException) {
                throw (SocketException) cause;
            }
            SocketException se = new SocketException(cause.getMessage());
            se.initCause(cause);
            throw se;
        }
    }

}

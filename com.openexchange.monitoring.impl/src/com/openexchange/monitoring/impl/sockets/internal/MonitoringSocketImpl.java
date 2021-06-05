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
import java.net.SocketTimeoutException;
import com.openexchange.java.delegate.DelegationExecutionException;
import com.openexchange.java.delegate.Delegator;
import com.openexchange.monitoring.sockets.failure.ConnectFailure;
import com.openexchange.monitoring.sockets.failure.TimeoutFailure;

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
            if (cause instanceof SocketTimeoutException) {
                SocketTimeoutException timeoutException = (SocketTimeoutException) cause;
                long millis = System.currentTimeMillis() - st;
                SocketMonitoringSystem.getInstance().readTimedOut(getSocket0(), new TimeoutFailure(timeoutException, millis));
                throw timeoutException;
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
            if (cause instanceof SocketTimeoutException) {
                SocketTimeoutException timeoutException = (SocketTimeoutException) cause;
                long millis = System.currentTimeMillis() - st;
                SocketMonitoringSystem.getInstance().readTimedOut(getSocket0(), new TimeoutFailure(timeoutException, millis));
                throw timeoutException;
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
            if (cause instanceof SocketTimeoutException) {
                SocketTimeoutException timeoutException = (SocketTimeoutException) cause;
                long millis = System.currentTimeMillis() - st;
                SocketMonitoringSystem.getInstance().readTimedOut(getSocket0(), new TimeoutFailure(timeoutException, millis));
                throw timeoutException;
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

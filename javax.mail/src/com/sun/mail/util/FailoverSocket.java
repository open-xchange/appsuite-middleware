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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.SocketChannel;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.logging.Level;

/**
 * {@link FailoverSocket} - Delegates calls to a possibly changing instance of {@link Socket}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class FailoverSocket extends Socket {

    private static MailLogger logger = new MailLogger(
        FailoverSocket.class,
        "socket",
        "DEBUG FailoverSocket",
        PropUtil.getBooleanSystemProperty("mail.socket.debug", false),
        System.out);

    // ----------------------------------------------------------------------------------------------------

    private final Socket socket;

    private final AddressSelector selector;
    private final String host;
    private final int port;
    private final Properties props;
    private final String prefix;
    private final boolean useSSL;
    private final long backoffBaseMillis;

    /**
     * Initializes a new {@link FailoverSocket}.
     *
     * @throws IOException If socket cannot be created
     */
    public FailoverSocket(AddressSelector selector, String host, int port, Properties props, String prefix, boolean useSSL) throws IOException {
        super();
        this.host = host;
        this.port = port;
        this.props = props;
        this.prefix = prefix;
        this.useSSL = useSSL;
        this.selector = selector;

        // Get configured connect timeout from properties
        int connectTimeout = PropUtil.getIntProperty(props, prefix + ".connectiontimeout", -1);
        backoffBaseMillis = connectTimeout > 1000 ? 1000L : 100L;

        // Determine max. retry attempts
        int maxRetries = PropUtil.getIntProperty(props, prefix + ".multiAddress.maxRetries", 3);

        // Establish socket (with fail-over behavior)
        this.socket = connectToNext(0, Math.min(maxRetries, selector.length()), null);
    }

    private Socket connectToNext(int retryCount, int maxRetries, com.sun.mail.util.SocketConnectException previousConnectError) throws IOException {
        // Grab the IP address to use
        InetAddress addressToUse = selector.currentAddress();
        if (null == addressToUse) {
            if (null != previousConnectError) {
                // Selector provides no further IP addresses to use
                throw previousConnectError;
            }

            // Selector provides no IP address at all
            InetAddress address = InetAddress.getAllByName(host)[0];
            return SocketFetcher.getSocket(address, host, port, props, prefix, useSSL);
        }

        // Try to establish a socket connection
        try {
            return SocketFetcher.getSocket(addressToUse, host, port, props, prefix, useSSL);
        } catch (com.sun.mail.util.SocketConnectException e) {
            // Connect attempt to IP address failed. Notify address selector
            selector.failoverAddress(addressToUse);

            // Retry using exponential back-off.
            int retry = retryCount + 1;
            if (retry >= maxRetries) {
                // Redeemed all retry attempts. Giving up...
                throw e;
            }
            long backoffNanos = TimeUnit.NANOSECONDS.convert((retry * backoffBaseMillis) + ((long) (Math.random() * backoffBaseMillis)), TimeUnit.MILLISECONDS);
            if (logger.isLoggable(Level.FINER)) {
                logger.finer("Failed to connect to " + e.getHost() + " (address " + e.getAddress() + "), port " + e.getPort() + ". Retrying in " + TimeUnit.NANOSECONDS.toMicros(backoffNanos) + " microseconds");
            }
            LockSupport.parkNanos(backoffNanos);
            return connectToNext(retry, maxRetries, e);
        }
    }

    @Override
    public void connect(SocketAddress endpoint) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void connect(SocketAddress endpoint, int timeout) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void bind(SocketAddress bindpoint) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public InetAddress getInetAddress() {
        return socket.getInetAddress();
    }

    @Override
    public InetAddress getLocalAddress() {
        return socket.getLocalAddress();
    }

    @Override
    public int getPort() {
        return socket.getPort();
    }

    @Override
    public int getLocalPort() {
        return socket.getLocalPort();
    }

    @Override
    public SocketAddress getRemoteSocketAddress() {
        return socket.getRemoteSocketAddress();
    }

    @Override
    public SocketAddress getLocalSocketAddress() {
        return socket.getLocalSocketAddress();
    }

    @Override
    public SocketChannel getChannel() {
        return socket.getChannel();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return socket.getInputStream();
    }

    @Override
    public synchronized OutputStream getOutputStream() throws IOException {
        return socket.getOutputStream();
    }

    @Override
    public void setTcpNoDelay(boolean on) throws SocketException {
        socket.setTcpNoDelay(on);
    }

    @Override
    public boolean getTcpNoDelay() throws SocketException {
        return socket.getTcpNoDelay();
    }

    @Override
    public void setSoLinger(boolean on, int linger) throws SocketException {
        socket.setSoLinger(on, linger);
    }

    @Override
    public int getSoLinger() throws SocketException {
        return socket.getSoLinger();
    }

    @Override
    public void sendUrgentData(int data) throws IOException {
        socket.sendUrgentData(data);
    }

    @Override
    public void setOOBInline(boolean on) throws SocketException {
        socket.setOOBInline(on);
    }

    @Override
    public boolean getOOBInline() throws SocketException {
        return socket.getOOBInline();
    }

    @Override
    public void setSoTimeout(int timeout) throws SocketException {
        socket.setSoTimeout(timeout);
    }

    @Override
    public int getSoTimeout() throws SocketException {
        return socket.getSoTimeout();
    }

    @Override
    public void setSendBufferSize(int size) throws SocketException {
        socket.setSendBufferSize(size);
    }

    @Override
    public int getSendBufferSize() throws SocketException {
        return socket.getSendBufferSize();
    }

    @Override
    public void setReceiveBufferSize(int size) throws SocketException {
        socket.setReceiveBufferSize(size);
    }

    @Override
    public int getReceiveBufferSize() throws SocketException {
        return socket.getReceiveBufferSize();
    }

    @Override
    public void setKeepAlive(boolean on) throws SocketException {
        socket.setKeepAlive(on);
    }

    @Override
    public boolean getKeepAlive() throws SocketException {
        return socket.getKeepAlive();
    }

    @Override
    public void setTrafficClass(int tc) throws SocketException {
        socket.setTrafficClass(tc);
    }

    @Override
    public int getTrafficClass() throws SocketException {
        return socket.getTrafficClass();
    }

    @Override
    public void setReuseAddress(boolean on) throws SocketException {
        socket.setReuseAddress(on);
    }

    @Override
    public boolean getReuseAddress() throws SocketException {
        return socket.getReuseAddress();
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }

    @Override
    public void shutdownInput() throws IOException {
        socket.shutdownInput();
    }

    @Override
    public void shutdownOutput() throws IOException {
        socket.shutdownOutput();
    }

    @Override
    public String toString() {
        return socket.toString();
    }

    @Override
    public boolean isConnected() {
        return socket.isConnected();
    }

    @Override
    public boolean isBound() {
        return socket.isBound();
    }

    @Override
    public boolean isClosed() {
        return socket.isClosed();
    }

    @Override
    public boolean isInputShutdown() {
        return socket.isInputShutdown();
    }

    @Override
    public boolean isOutputShutdown() {
        return socket.isOutputShutdown();
    }

    @Override
    public void setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
        socket.setPerformancePreferences(connectionTime, latency, bandwidth);
    }


}

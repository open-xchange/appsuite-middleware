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
 *    trademarks of the OX Software GmbH group of companies.
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

package com.sun.mail.util;

import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Properties;

/**
 * {@link RoundRobinSocket}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class RoundRobinSocket extends Socket {

    private static class WrappingInputStream extends FilterInputStream {

        private final RoundRobinSocket roundRobinSocket;
        private final WrappingOutputStream out;

        WrappingInputStream(InputStream in, WrappingOutputStream out, RoundRobinSocket roundRobinSocket) {
            super(in);
            this.out = out;
            this.roundRobinSocket = roundRobinSocket;
        }

        void applyNew(InputStream newIn) {
            InputStream in = this.in;
            if (null != in) {
                try {
                    in.close();
                } catch (Exception e) {
                    // Ignore
                }
            }

            this.in = newIn;
        }

        @Override
        public int read() throws IOException {
            InputStream in = this.in;
            try {
                return in.read();
            } catch (java.net.SocketTimeoutException e) {
                if (this.in == in) {
                    Socket socket = roundRobinSocket.initNext();
                    applyNew(socket.getInputStream());
                    out.applyNew(socket.getOutputStream());
                }
                throw e;
            }
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            InputStream in = this.in;
            try {
                return in.read(b, off, len);
            } catch (java.net.SocketTimeoutException e) {
                if (this.in == in) {
                    Socket socket = roundRobinSocket.initNext();
                    applyNew(socket.getInputStream());
                    out.applyNew(socket.getOutputStream());
                }
                throw e;
            }
        }

        @Override
        public int read(byte[] b) throws IOException {
            return read(b, 0, b.length);
        }

    }

    private static class WrappingOutputStream extends FilterOutputStream {

        WrappingOutputStream(OutputStream out) {
            super(out);
        }

        void applyNew(OutputStream newOut) {
            OutputStream out = this.out;
            if (null != out) {
                try {
                    out.close();
                } catch (Exception e) {
                    // Ignore
                }
            }

            this.out = newOut;
        }

    }

    // ----------------------------------------------------------------------------------------------------

    private volatile Socket socket;

    private final InfiniteIterator<InetAddress> addresses;
    private final String host;
    private final int port;
    private final Properties props;
    private final String prefix;
    private final boolean useSSL;

    private WrappingOutputStream out;
    private WrappingInputStream in;

    /**
     * Initializes a new {@link RoundRobinSocket}.
     *
     * @throws IOException If socket cannot be created
     */
    public RoundRobinSocket(InetAddress[] addresses, InetAddress initialAddress, String host, int port, Properties props, String prefix, boolean useSSL) throws IOException {
        super();
        this.host = host;
        this.port = port;
        this.props = props;
        this.prefix = prefix;
        this.useSSL = useSSL;
        this.addresses = new InfiniteIterator<>(Arrays.asList(addresses));
        socket = SocketFetcher.getSocket(initialAddress, host, port, props, prefix, useSSL);
    }

    /**
     * Initializes the next socket
     *
     * @return The next available socket
     * @throws IOException If next socket cannot be initialized
     */
    public Socket initNext() throws IOException {
        Socket socket = this.socket;
        if (null != socket) {
            try {
                socket.close();
            } catch (IOException e) {
                // Ignore
            }
        }

        socket = SocketFetcher.getSocket(this.addresses.next(), host, port, props, prefix, useSSL);
        this.socket = socket;
        return socket;
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
    public synchronized InputStream getInputStream() throws IOException {
        if (in == null) {
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();

            out = new WrappingOutputStream(outputStream);
            in = new WrappingInputStream(inputStream, out, this);

        }
        return in;
    }

    @Override
    public synchronized OutputStream getOutputStream() throws IOException {
        if (out == null) {
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();

            out = new WrappingOutputStream(outputStream);
            in = new WrappingInputStream(inputStream, out, this);
        }

        return out;
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

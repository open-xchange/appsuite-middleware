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

package com.openexchange.proxy.servlet;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import javax.net.ssl.SSLSocketFactory;
import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;
import com.openexchange.net.ssl.SSLSocketFactoryProvider;

/**
 * {@link TrustAdapter} - A trust-all {@link ProtocolSocketFactory protocol socket factory}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class TrustAdapter implements SecureProtocolSocketFactory {

    private final SSLSocketFactory delegate = SSLSocketFactoryProvider.getDefault();

    /**
     * Initializes a new {@link TrustAdapter}.
     */
    public TrustAdapter() {
        super();
    }

    @Override
    public Socket createSocket(final String host, final int port) throws IOException, UnknownHostException {
        return delegate.createSocket(host, port);
    }

    @Override
    public Socket createSocket(final String host, final int port, final InetAddress localAddress, final int localPort) throws IOException, UnknownHostException {
        return delegate.createSocket(host, port, localAddress, localPort);
    }

    @Override
    public Socket createSocket(final String host, final int port, final InetAddress localAddress, final int localPort, final HttpConnectionParams params) throws IOException, UnknownHostException, ConnectTimeoutException {
        Socket socket;
        final int timeout = params.getConnectionTimeout();
        if (timeout == 0) {
            socket = createSocket(host, port, localAddress, localPort);
        } else {
            socket = delegate.createSocket();
            final SocketAddress localaddr = new InetSocketAddress(localAddress, localPort);
            final SocketAddress remoteaddr = new InetSocketAddress(host, port);
            socket.bind(localaddr);
            socket.connect(remoteaddr, timeout);
            return socket;
        }

        final int linger = params.getLinger();
        if (linger == 0) {
            socket.setSoLinger(false, 0);
        } else if (linger > 0) {
            socket.setSoLinger(true, linger);
        }

        socket.setSoTimeout(params.getSoTimeout());
        socket.setTcpNoDelay(params.getTcpNoDelay());

        return socket;
    }

    @Override
    public Socket createSocket(final Socket socket, final String host, final int port, final boolean autoClose) throws IOException, UnknownHostException {
        return delegate.createSocket(socket, host, port, autoClose);
    }

}

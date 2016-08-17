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

package com.openexchange.management.internal;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;
import com.openexchange.tools.ssl.TrustAllSSLSocketFactory;

/**
 * {@link CustomSslRmiServerSocketFactory}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CustomSslRmiServerSocketFactory extends SslRMIServerSocketFactory {

    private final InetAddress bindAddress;

    /**
     * Initializes a new {@link CustomSslRmiServerSocketFactory}.
     *
     * @param bindAddr The optional bind address or <code>"*"</code> for unbounded
     * @throws IllegalArgumentException  If bindAddr is unknown
     */
    public CustomSslRmiServerSocketFactory(final String bindAddr) {
        super();
        try {
            bindAddress = bindAddr.charAt(0) == '*' ? null : InetAddress.getByName(bindAddr);
        } catch (final UnknownHostException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Initializes a new {@link CustomSslRmiServerSocketFactory}.
     *
     * @param bindAddr The optional bind address or <code>"*"</code> for unbounded
     * @param enabledCipherSuites Names of all the cipher suites to enable on SSL connections accepted by server sockets created by this
     *            factory, or <code>null</code> to use the cipher suites that are enabled by default
     * @param enabledProtocols Names of all the protocol versions to enable on SSL connections accepted by server sockets created by this
     *            factory, or <code>null</code> to use the protocol versions that are enabled by default
     * @param needClientAuth <code>true</code> to require client authentication on SSL connections accepted by server sockets created by
     *            this factory; <code>false</code> to not require client authentication
     * @throws IllegalArgumentException If one or more of the cipher suites named by the <code>enabledCipherSuites</code> parameter is not
     *             supported, when one or more of the protocols named by the <code>enabledProtocols</code> parameter is not supported or
     *             when a problem is encountered while trying to check if the supplied cipher suites and protocols to be enabled are
     *             supported.
     * @see SSLSocket#setEnabledCipherSuites
     * @see SSLSocket#setEnabledProtocols
     * @see SSLSocket#setNeedClientAuth
     */
    public CustomSslRmiServerSocketFactory(final String bindAddr, final String[] enabledCipherSuites, final String[] enabledProtocols, final boolean needClientAuth) throws IllegalArgumentException {
        super(enabledCipherSuites, enabledProtocols, needClientAuth);
        try {
            bindAddress = bindAddr.charAt(0) == '*' ? null : InetAddress.getByName(bindAddr);
        } catch (final UnknownHostException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public ServerSocket createServerSocket(final int port) throws IOException {
        final SSLSocketFactory sslSocketFactory = TrustAllSSLSocketFactory.getDefault();
        return new ServerSocket(port, 0/*use default backlog*/, bindAddress) {

            @Override
            public Socket accept() throws IOException {
                final Socket socket = super.accept();
                final SSLSocket sslSocket =
                    (SSLSocket) sslSocketFactory.createSocket(socket, socket.getInetAddress().getHostName(), socket.getPort(), true);
                sslSocket.setUseClientMode(false);
                final String[] enabledCipherSuites = getEnabledCipherSuites();
                if (enabledCipherSuites != null) {
                    sslSocket.setEnabledCipherSuites(enabledCipherSuites);
                }
                final String[] enabledProtocols = getEnabledProtocols();
                if (enabledProtocols != null) {
                    sslSocket.setEnabledProtocols(enabledProtocols);
                }
                final boolean needClientAuth = getNeedClientAuth();
                sslSocket.setNeedClientAuth(needClientAuth);
                return sslSocket;
            }
        };

        // If we do not instantiate the server socket class, but
        // instead must layer on top of an arbitrary server socket,
        // then this implementation would become uglier, like this
        // (given "serverSocket" to layer on top of):

        /*-
         *
         return new ForwardingServerSocket(serverSocket) {
            public Socket accept() throws IOException {
              Socket socket = serverSocket.accept();
              SSLSocket sslSocket =
                  (SSLSocket) sslSocketFactory.createSocket(
                      socket,
                      socket.getInetAddress().getHostName(),
                      socket.getPort(),
                      true);
              sslSocket.setUseClientMode(false);
              if (enabledProtocols != null) {
                  sslSocket.setEnabledProtocols(enabledProtocols);
              }
              if (enabledCipherSuites != null) {
                  sslSocket.setEnabledCipherSuites(enabledCipherSuites);
              }
              sslSocket.setNeedClientAuth(needClientAuth);
              return sslSocket;
            }
            public ServerSocketChannel getChannel() {
              return null;
            }
            public String toString() {
              return serverSocket.toString();
            }
         };
         *
         */
    }

}

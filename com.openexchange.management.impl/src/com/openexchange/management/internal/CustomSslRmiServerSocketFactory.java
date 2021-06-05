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

package com.openexchange.management.internal;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;
import com.openexchange.java.Streams;
import com.openexchange.management.services.ManagementServiceRegistry;
import com.openexchange.net.ssl.SSLSocketFactoryProvider;

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
        } catch (UnknownHostException e) {
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
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public ServerSocket createServerSocket(final int port) throws IOException {
        SSLSocketFactoryProvider factoryProvider = ManagementServiceRegistry.getServiceRegistry().getService(SSLSocketFactoryProvider.class);
        if (null == factoryProvider) {
            throw new IOException("Missing " + SSLSocketFactoryProvider.class.getSimpleName() + " service. Bundle \"com.openexchange.net.ssl\" not started?");
        }
        final SSLSocketFactory sslSocketFactory = factoryProvider.getDefault();
        return new ServerSocket(port, 0/*use default backlog*/, bindAddress) {

            @Override
            public Socket accept() throws IOException {
                final Socket socket = super.accept();
                try {
                    SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(socket, socket.getInetAddress().getHostName(), socket.getPort(), true);
                    sslSocket.setUseClientMode(false);

                    String[] enabledCipherSuites = getEnabledCipherSuites();
                    if (enabledCipherSuites != null) {
                        sslSocket.setEnabledCipherSuites(enabledCipherSuites);
                    }

                    String[] enabledProtocols = getEnabledProtocols();
                    if (enabledProtocols != null) {
                        sslSocket.setEnabledProtocols(enabledProtocols);
                    }

                    boolean needClientAuth = getNeedClientAuth();
                    sslSocket.setNeedClientAuth(needClientAuth);

                    SSLSocket returnMe = sslSocket;
                    sslSocket = null; // AVOid premature closing
                    return returnMe;
                } finally {
                    Streams.close(socket);
                }
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

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

package com.openexchange.tools.ssl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import com.google.common.collect.ImmutableList;

/**
 * This SSL socket factory creates an SSL context that trusts all certificates and
 * then uses that context to create an SSL socket factory, which will trust all certificates.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class TrustAllSSLSocketFactory extends SSLSocketFactory {

    /**
     * Gets a new trust-all SSL socket factory.
     *
     * @return A new trust-all SSL socket factory
     */
    public static SSLSocketFactory getDefault() {
        return new TrustAllSSLSocketFactory();
    }

    /**
     * Gets a new trust-all SSL socket factory.
     *
     * @return A new trust-all SSL socket factory
     */
    public static SSLContext getCreatingDefaultContext() {
        return getSSLContext();
    }

    // --------------------------------------------------------------------------------------------------------------------------------------------

    /** This factory will trust all certificates. */
    private final SSLSocketFactory factory;

    /**
     * This constructor creates an SSL context with the <tt>TrustAllManager</tt> and uses the SSL socket factory from that SSL context.
     */
    protected TrustAllSSLSocketFactory() {
        super();
        factory = getSSLContext().getSocketFactory();
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return factory.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return factory.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket() throws IOException {
        return checkProtocols(factory.createSocket());
    }

    @Override
    public Socket createSocket(final Socket s, final String host, final int port, final boolean autoClose) throws IOException {
        return checkProtocols(factory.createSocket(s, host, port, autoClose));
    }

    @Override
    public Socket createSocket(final String host, final int port) throws IOException, UnknownHostException {
        return checkProtocols(factory.createSocket(host, port));
    }

    @Override
    public Socket createSocket(final String host, final int port, final InetAddress localAddress, final int localPort) throws IOException, UnknownHostException {
        return checkProtocols(factory.createSocket(host, port, localAddress, localPort));
    }

    @Override
    public Socket createSocket(final InetAddress host, final int port) throws IOException {
        return checkProtocols(factory.createSocket(host, port));
    }

    @Override
    public Socket createSocket(final InetAddress address, final int port, final InetAddress localAddress, final int localPort) throws IOException {
        return checkProtocols(factory.createSocket(address, port, localAddress, localPort));
    }

    private static final List<String> PROTOCOLS_TO_ADD = ImmutableList.of("SSLv3", "SSLv2", "TLSv1", "TLSv1.1", "TLSv1.2", "SSLv2.3");

    private static Socket checkProtocols(Socket socket) {
        if (!(socket instanceof SSLSocket)) {
            return socket;
        }

        SSLSocket sslSocket = (SSLSocket) socket;

        // Get socket's enabled protocols
        Set<String> enabledProtocols = new LinkedHashSet<String>(Arrays.asList(sslSocket.getEnabledProtocols()));

        // Add the protocols, which should be added, with respect to already enabled ones
        boolean somethingAdded = false;
        for (String protocol : PROTOCOLS_TO_ADD) {
            somethingAdded |= enabledProtocols.add(protocol);
        }
        if (somethingAdded) {
            try {
                sslSocket.setEnabledProtocols(enabledProtocols.toArray(new String[enabledProtocols.size()]));
            } catch (IllegalArgumentException e) {
                // Unable to add specified protocols
            }
        }

        return new DelegatingSSLSocket(sslSocket);
    }

    private static SSLContext getSSLContext() throws IllegalStateException {
        try {
            final SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new TrustManager[] { new TrustAllManager() }, new SecureRandom());
            return context;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } catch (KeyManagementException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}

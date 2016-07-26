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
import java.util.Set;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

/**
 * This ssl socket factory creates a ssl context that trusts all certificates and uses then this context to create a ssl socket factory that
 * will trust all certificates.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 * @deprecated use com.openexchange.ssl.TrustedSSLSocketFactory instead
 */
@Deprecated
public final class TrustAllSSLSocketFactory extends SSLSocketFactory {

    /**
     * This factory will trust all certificates.
     */
    private final SSLSocketFactory factory;

    /**
     * This constructor creates a ssl context with the TrustAllManager and uses the ssl socket factory from this ssl context.
     */
    protected TrustAllSSLSocketFactory() {
        super();
        try {
            final SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new TrustManager[] { new TrustAllManager() }, new SecureRandom());
            factory = context.getSocketFactory();
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } catch (final KeyManagementException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    /**
     * Gets a new trust-all SSL socket factory.
     *
     * @return A new trust-all SSL socket factory
     */
    public static SSLSocketFactory getDefault() {
        return new TrustAllSSLSocketFactory();
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

    private static Socket checkProtocols(final Socket socket) {
        if (socket instanceof SSLSocket) {
            final SSLSocket sslSocket = (SSLSocket) socket;

            tryAddProtocol("SSLv3", sslSocket);
            tryAddProtocol("SSLv2", sslSocket);
            tryAddProtocol("TLSv1", sslSocket);
            tryAddProtocol("SSLv23", sslSocket);
        }
        return socket;
    }

    private static boolean tryAddProtocol(final String protocol, final SSLSocket sslSocket) {
        final Set<String> protocols = new LinkedHashSet<String>(Arrays.asList(sslSocket.getEnabledProtocols()));
        if (protocols.add(protocol)) {
            try {
                sslSocket.setEnabledProtocols(protocols.toArray(new String[0]));
            } catch (final IllegalArgumentException e) {
                // Unable to add specified protocol
                return false;
            }
        }
        // Already included or has been successfully added
        return true;
    }

}

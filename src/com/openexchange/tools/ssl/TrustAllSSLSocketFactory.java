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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

/**
 * This ssl socket factory creates a ssl context that trusts all certificates
 * and uses then this context to create a ssl socket factory that will trust all
 * certificates.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class TrustAllSSLSocketFactory extends SSLSocketFactory {

    /**
     * This factory will trust all certificates.
     */
    private SSLSocketFactory factory;

    /**
     * This constructor creates a ssl context with the TrustAllManager and uses
     * the ssl socket factory from this ssl context.
     */
    protected TrustAllSSLSocketFactory() {
        super();
        try {
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new TrustManager[] {new TrustAllManager()},
                new SecureRandom());
            factory = context.getSocketFactory();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see javax.net.ssl.SSLSocketFactory#getDefault()
     */
    public static SocketFactory getDefault() {
        return new TrustAllSSLSocketFactory();
    }

    /**
     * @see javax.net.ssl.SSLSocketFactory#getDefaultCipherSuites()
     */
    public String[] getDefaultCipherSuites() {
        return factory.getDefaultCipherSuites();
    }

    /**
     * @see javax.net.ssl.SSLSocketFactory#getSupportedCipherSuites()
     */
    public String[] getSupportedCipherSuites() {
        return factory.getSupportedCipherSuites();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Socket createSocket() throws IOException {
        return factory.createSocket();
    }

    /**
     * @see javax.net.ssl.SSLSocketFactory#createSocket(java.net.Socket,
     *        java.lang.String, int, boolean)
     */
    public Socket createSocket(final Socket s, final String host, final int port,
        final boolean autoClose) throws IOException {
        return factory.createSocket(s, host, port, autoClose);
    }

    /**
     * @see javax.net.SocketFactory#createSocket(java.lang.String, int)
     */
    public Socket createSocket(final String host, final int port)
        throws IOException, UnknownHostException {
        return factory.createSocket(host, port);
    }

    /**
     * @see javax.net.SocketFactory#createSocket(java.lang.String, int,
     *        java.net.InetAddress, int)
     */
    public Socket createSocket(final String host, final int port,
        final InetAddress localAddress, final int localPort) throws IOException,
        UnknownHostException {
        return factory.createSocket(host, port, localAddress, localPort);
    }

    /**
     * @see javax.net.SocketFactory#createSocket(java.net.InetAddress, int)
     */
    public Socket createSocket(final InetAddress host, final int port)
        throws IOException {
        return factory.createSocket(host, port);
    }

    /**
     * @see javax.net.SocketFactory#createSocket(java.net.InetAddress, int,
     *        java.net.InetAddress, int)
     */
    public Socket createSocket(final InetAddress address, final int port,
        final InetAddress localAddress, final int localPort) throws IOException {
        return factory.createSocket(address, port, localAddress, localPort);
    }

}

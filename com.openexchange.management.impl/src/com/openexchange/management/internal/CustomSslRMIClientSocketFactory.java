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
import java.net.Socket;
import java.util.StringTokenizer;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import com.openexchange.management.services.ManagementServiceRegistry;
import com.openexchange.net.ssl.SSLSocketFactoryProvider;

/**
 * {@link CustomSslRMIClientSocketFactory}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CustomSslRMIClientSocketFactory extends SslRMIClientSocketFactory {

    /**
     *
     */
    private static final long serialVersionUID = 7337209808828143732L;

    /*-
     * ------------- Member stuff ----------------
     */

    /**
     * Initializes a new {@link CustomSslRMIClientSocketFactory}.
     */
    public CustomSslRMIClientSocketFactory() {
        super();
    }

    @Override
    public Socket createSocket(final String host, final int port) throws IOException {
        // Retrieve the SSLSocketFactory
        //
        SSLSocketFactoryProvider factoryProvider = ManagementServiceRegistry.getServiceRegistry().getService(SSLSocketFactoryProvider.class);
        final SocketFactory sslSocketFactory = factoryProvider.getDefault();
        // Create the SSLSocket
        //
        final SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(host, port);
        // Set the SSLSocket Enabled Cipher Suites
        //
        final String enabledCipherSuites = System.getProperty("javax.rmi.ssl.client.enabledCipherSuites");
        if (enabledCipherSuites != null) {
            final StringTokenizer st = new StringTokenizer(enabledCipherSuites, ",");
            final int tokens = st.countTokens();
            final String enabledCipherSuitesList[] = new String[tokens];
            for (int i = 0; i < tokens; i++) {
                enabledCipherSuitesList[i] = st.nextToken();
            }
            try {
                sslSocket.setEnabledCipherSuites(enabledCipherSuitesList);
            } catch (final IllegalArgumentException e) {
                throw (IOException) new IOException(e.getMessage()).initCause(e);
            }
        }
        // Set the SSLSocket Enabled Protocols
        //
        final String enabledProtocols = System.getProperty("javax.rmi.ssl.client.enabledProtocols");
        if (enabledProtocols != null) {
            final StringTokenizer st = new StringTokenizer(enabledProtocols, ",");
            final int tokens = st.countTokens();
            final String enabledProtocolsList[] = new String[tokens];
            for (int i = 0; i < tokens; i++) {
                enabledProtocolsList[i] = st.nextToken();
            }
            try {
                sslSocket.setEnabledProtocols(enabledProtocolsList);
            } catch (final IllegalArgumentException e) {
                throw (IOException) new IOException(e.getMessage()).initCause(e);
            }
        }
        // Return the preconfigured SSLSocket
        //
        return sslSocket;
    }

}

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
import java.net.Socket;
import java.util.StringTokenizer;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import com.openexchange.management.services.ManagementServiceRegistry;
import com.openexchange.net.ssl.SSLSocketFactoryProvider;
import com.openexchange.systemproperties.SystemPropertiesUtils;

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
        final String enabledCipherSuites = SystemPropertiesUtils.getProperty("javax.rmi.ssl.client.enabledCipherSuites");
        if (enabledCipherSuites != null) {
            final StringTokenizer st = new StringTokenizer(enabledCipherSuites, ",");
            final int tokens = st.countTokens();
            final String enabledCipherSuitesList[] = new String[tokens];
            for (int i = 0; i < tokens; i++) {
                enabledCipherSuitesList[i] = st.nextToken();
            }
            try {
                sslSocket.setEnabledCipherSuites(enabledCipherSuitesList);
            } catch (IllegalArgumentException e) {
                throw (IOException) new IOException(e.getMessage()).initCause(e);
            }
        }
        // Set the SSLSocket Enabled Protocols
        //
        final String enabledProtocols = SystemPropertiesUtils.getProperty("javax.rmi.ssl.client.enabledProtocols");
        if (enabledProtocols != null) {
            final StringTokenizer st = new StringTokenizer(enabledProtocols, ",");
            final int tokens = st.countTokens();
            final String enabledProtocolsList[] = new String[tokens];
            for (int i = 0; i < tokens; i++) {
                enabledProtocolsList[i] = st.nextToken();
            }
            try {
                sslSocket.setEnabledProtocols(enabledProtocolsList);
            } catch (IllegalArgumentException e) {
                throw (IOException) new IOException(e.getMessage()).initCause(e);
            }
        }
        // Return the preconfigured SSLSocket
        //
        return sslSocket;
    }

}

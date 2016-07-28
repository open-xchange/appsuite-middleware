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

package com.openexchange.ssl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import com.openexchange.java.Strings;
import com.openexchange.ssl.internal.CustomTrustManager;
import com.openexchange.ssl.internal.DefaultTrustManager;

/**
 * {@link TrustedSSLSocketFactory}
 * 
 * This implementation has to be placed within an exported package as it will be accessed per reflection.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.3
 */
public class TrustedSSLSocketFactory extends SSLSocketFactory {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TrustedSSLSocketFactory.class);

    /** String-array of trusted hosts */
    private String[] trustedHosts = null;

    /** Holds a SSLContext to get SSLSocketFactories from */
    private SSLContext sslcontext;

    /** Holds the TrustManager array to use */
    private TrustManager[] trustManagers;

    /** Holds a SSLSocketFactory to pass all API-method-calls to */
    private SSLSocketFactory adapteeFactory = null;

    private TrustedSSLSocketFactory() {
        this.trustManagers = initTrustManagers();

        try {
            this.sslcontext = SSLContext.getInstance("TLS");
            // Assemble a default SSLSocketFactory to delegate all API-calls to.
            newAdapteeFactory();
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (KeyManagementException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private TrustManager[] initTrustManagers() {
        List<X509TrustManager> lTrustManagers = new ArrayList<>();
        DefaultTrustManager defaultTrustManager = new DefaultTrustManager();
        if (defaultTrustManager.isInitialized()) {
            lTrustManagers.add(defaultTrustManager);
        }

        CustomTrustManager customTrustManager = new CustomTrustManager();
        if (customTrustManager.isInitialized()) {
            lTrustManagers.add(customTrustManager);
        }
        TrustManager[] trustManagersArray = new TrustManager[lTrustManagers.size()];
        lTrustManagers.toArray(trustManagersArray);
        return trustManagersArray;
    }

    /**
     * Gets an SSLSocketFactory based on the given (or default)
     * KeyManager array, TrustManager array and SecureRandom and
     * sets it to the instance var adapteeFactory.
     *
     * @throws KeyManagementException for key manager errors
     */
    private synchronized void newAdapteeFactory() throws KeyManagementException {
        this.sslcontext.init(null, this.trustManagers, null);

        this.adapteeFactory = this.sslcontext.getSocketFactory();
    }

    /**
     * Returns the {@link TrustedSSLSocketFactory} if desired to use only trusted connections
     * 
     * @return an instance of {@link TrustedSSLSocketFactory}
     */
    public static SocketFactory getDefault() {
        return new TrustedSSLSocketFactory();
    }

    @Override
    public Socket createSocket() throws IOException {
        Socket createSocket = this.adapteeFactory.createSocket();
        logProtocols(createSocket);
        return createSocket;
    }

    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        Socket createSocket = this.adapteeFactory.createSocket(s, host, port, autoClose);
        logProtocols(createSocket);
        return createSocket;
    }

    private static void logProtocols(Socket socket) {
        if (socket instanceof SSLSocket) {
            final SSLSocket sslSocket = (SSLSocket) socket;
            String url = "unknown";
            if (sslSocket.getInetAddress() != null) {
                url = sslSocket.getInetAddress().toString();
            }
            LOG.debug("Supported protocols for socket {}: {}", url, Strings.join(sslSocket.getEnabledProtocols(), ", "));
        }
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return this.adapteeFactory.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return this.adapteeFactory.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        return this.adapteeFactory.createSocket(host, port);
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return this.adapteeFactory.createSocket(host, port);
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
        return this.adapteeFactory.createSocket(host, port, localHost, localPort);
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localHost, int localPort) throws IOException {
        return this.adapteeFactory.createSocket(address, port, localHost, localPort);
    }
}

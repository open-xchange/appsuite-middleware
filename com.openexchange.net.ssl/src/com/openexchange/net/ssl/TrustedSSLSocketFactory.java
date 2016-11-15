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

package com.openexchange.net.ssl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import com.google.common.collect.ImmutableList;
import com.openexchange.net.ssl.config.SSLConfigurationService;
import com.openexchange.net.ssl.internal.CustomTrustManager;
import com.openexchange.net.ssl.internal.DefaultTrustManager;
import com.openexchange.net.ssl.osgi.Services;
import com.openexchange.tools.ssl.DelegatingSSLSocket;

/**
 * {@link TrustedSSLSocketFactory}
 *
 * This implementation has to be placed within an exported package as it will be accessed per reflection.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.3
 */
public class TrustedSSLSocketFactory extends SSLSocketFactory implements HandshakeCompletedListener {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TrustedSSLSocketFactory.class);

    /** Holds the TrustManager array to use */
    private static final AtomicReference<List<TrustManager>> TRUST_MANAGERS = new AtomicReference<>();

    /**
     * Initializes the trust managers.
     */
    public static void init() {
        TRUST_MANAGERS.set(initTrustManagers());
    }

    private static List<TrustManager> initTrustManagers() {
        ImmutableList.Builder<TrustManager> lTrustManagers = ImmutableList.builder();
        DefaultTrustManager defaultTrustManager = new DefaultTrustManager();
        if (defaultTrustManager.isInitialized()) {
            lTrustManagers.add(defaultTrustManager);
        }

        CustomTrustManager customTrustManager = new CustomTrustManager();
        if (customTrustManager.isInitialized()) {
            lTrustManagers.add(customTrustManager);
        }
        return lTrustManagers.build();
    }

    // -------------------------------------------------------------------------------------------

    /** Holds a SSLSocketFactory to pass all API-method-calls to */
    private final SSLSocketFactory adapteeFactory;

    /**
     * Initializes a new {@link TrustedSSLSocketFactory}.
     */
    protected TrustedSSLSocketFactory() {
        super();
        SSLSocketFactory adapteeFactory = null;
        try {
            // Get the SSLContext to get SSLSocketFactories from
            SSLContext sslcontext = SSLContext.getInstance("TLS");
            adapteeFactory = newAdapteeFactory(sslcontext);
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Unable to retrieve SSLContext.", e);
        } catch (KeyManagementException e) {
            LOG.error("Unable to initialize SSLContext.", e);
        }
        this.adapteeFactory = adapteeFactory;
    }

    /**
     * Gets an SSLSocketFactory based on the given (or default)
     * KeyManager array, TrustManager array and SecureRandom and
     * sets it to the instance var adapteeFactory.
     *
     * @throws KeyManagementException for key manager errors
     */
    private static SSLSocketFactory newAdapteeFactory(SSLContext sslcontext) throws KeyManagementException {
        List<TrustManager> trustManagers = TRUST_MANAGERS.get();
        if (trustManagers == null || trustManagers.isEmpty()) {
            LOG.error("No trust managers available, maybe configuration error. Going to use default one for now. Please enable default or custom trust store.");
            sslcontext.init(null, null, null);
            return sslcontext.getSocketFactory();
        }

        sslcontext.init(null, trustManagers.toArray(new TrustManager[trustManagers.size()]), null);
        return sslcontext.getSocketFactory();
    }

    /**
     * Returns the {@link TrustedSSLSocketFactory} if desired to use only trusted connections
     *
     * @return an instance of {@link TrustedSSLSocketFactory}
     */
    public static SSLSocketFactory getDefault() {
        return new TrustedSSLSocketFactory();
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return merge();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return Services.getService(SSLConfigurationService.class).getSupportedCipherSuites();
    }

    /**
     * Merges the suites configured to be supported (by the administrator) into the supported ones (by the factory).
     *
     * @return
     */
    private String[] merge() {
        String[] supportedCipherSuites = this.adapteeFactory.getSupportedCipherSuites();
        String[] configuredSuites = Services.getService(SSLConfigurationService.class).getSupportedCipherSuites();
        Set<String> configuredCipherSuites = new HashSet<>(Arrays.asList(configuredSuites));

        Set<String> suites = new HashSet<>();
        for (String supportedCipher : supportedCipherSuites) {
            if (configuredCipherSuites.contains(supportedCipher)) {
                suites.add(supportedCipher);
            }
        }
        return suites.toArray(new String[suites.size()]);
    }

    @Override
    public Socket createSocket() throws IOException {
        Socket socket = this.adapteeFactory.createSocket();
        return setProperties(socket);
    }

    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        Socket socket = this.adapteeFactory.createSocket(s, host, port, autoClose);
        return setProperties(socket);
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        Socket socket = this.adapteeFactory.createSocket(host, port);
        return setProperties(socket);
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        Socket socket = this.adapteeFactory.createSocket(host, port);
        return setProperties(socket);
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
        Socket socket = this.adapteeFactory.createSocket(host, port, localHost, localPort);
        return setProperties(socket);
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localHost, int localPort) throws IOException {
        Socket socket = this.adapteeFactory.createSocket(address, port, localHost, localPort);
        return setProperties(socket);
    }

    private Socket setProperties(Socket socket) {
        if (!(socket instanceof SSLSocket)) {
            return socket;
        }

        SSLSocket sslSocket = (SSLSocket) socket;
//            String[] supportedProtocols = sslSocket.getSupportedProtocols();
//            String[] supportedCipherSuites = sslSocket.getSupportedCipherSuites();
        SSLConfigurationService sslConfigService = Services.getService(SSLConfigurationService.class);
        sslSocket.setEnabledProtocols(sslConfigService.getSupportedProtocols());
        sslSocket.setEnabledCipherSuites(sslConfigService.getSupportedCipherSuites());
        sslSocket.setUseClientMode(true);
        sslSocket.addHandshakeCompletedListener(this);
        return new DelegatingSSLSocket(sslSocket);
    }

    @Override
    public void handshakeCompleted(HandshakeCompletedEvent event) {
        LOG.debug("Successfully handshaked with host {}", event.getSocket().getInetAddress().getHostAddress());
    }
}

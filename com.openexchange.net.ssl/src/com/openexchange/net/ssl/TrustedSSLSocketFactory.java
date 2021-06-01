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

package com.openexchange.net.ssl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
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
import javax.net.ssl.HostnameVerifier;
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

        // Default trust manager
        DefaultTrustManager defaultTrustManager = DefaultTrustManager.newInstance();
        if (null != defaultTrustManager) {
            lTrustManagers.add(defaultTrustManager);
        }

        // Custom trust manager
        CustomTrustManager customTrustManager = CustomTrustManager.newInstance();
        if (null != customTrustManager) {
            lTrustManagers.add(customTrustManager);
        }

        return lTrustManagers.build();
    }

    /**
     * Returns the {@link TrustedSSLSocketFactory} if desired to use only trusted connections
     *
     * @return an instance of {@link TrustedSSLSocketFactory}
     */
    public static SSLSocketFactory getDefault() {
        return new TrustedSSLSocketFactory();
    }

    /**
     * Returns the {@link SSLContext} if desired to use only trusted connections
     *
     * @return An instance of {@link SSLContext} or <code>null</code>
     */
    public static SSLContext getCreatingDefaultContext() {
        return getSSLContext();
    }

    /**
     * Gets an SSLSocketFactory based on the given (or default)
     * KeyManager array, TrustManager array and SecureRandom and
     * sets it to the instance var adapteeFactory.
     */
    private static SSLSocketFactory newAdapteeFactory() {
        SSLContext sslcontext = getSSLContext();
        return sslcontext == null ? null : sslcontext.getSocketFactory();
    }

    private static SSLContext getSSLContext() {
        try {
            SSLContext sslcontext = SSLContext.getInstance("TLS");
            initializeSSLContext(sslcontext);
            return sslcontext;
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Unable to retrieve SSLContext.", e);
        } catch (KeyManagementException e) {
            LOG.error("Unable to initialize SSLContext.", e);
        }

        return null;
    }

    private static void initializeSSLContext(SSLContext sslcontext) throws KeyManagementException {
        List<TrustManager> trustManagers = TRUST_MANAGERS.get();
        if (trustManagers == null || trustManagers.isEmpty()) {
            LOG.error("No trust managers available, maybe configuration error. Going to use default one for now. Please enable default or custom trust store.");
            sslcontext.init(null, null, null);
        } else {
            sslcontext.init(null, trustManagers.toArray(new TrustManager[trustManagers.size()]), null);
        }
    }

    // -------------------------------------------------------------------------------------------

    /** Holds a SSLSocketFactory to pass all API-method-calls to */
    private final SSLSocketFactory adapteeFactory;

    /**
     * Initializes a new {@link TrustedSSLSocketFactory}.
     */
    protected TrustedSSLSocketFactory() {
        super();
        this.adapteeFactory = newAdapteeFactory();
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
        Socket socket = adapteeFactory.createSocket();
        return setProperties(socket, null);
    }

    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        Socket socket = adapteeFactory.createSocket(s, host, port, autoClose);
        return setProperties(socket, new InetSocketAddress(host, port));
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        Socket socket = adapteeFactory.createSocket(host, port);
        return setProperties(socket, new InetSocketAddress(host, port));
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        Socket socket = adapteeFactory.createSocket(host, port);
        return setProperties(socket, new InetSocketAddress(host, port));
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
        Socket socket = adapteeFactory.createSocket(host, port, localHost, localPort);
        return setProperties(socket, new InetSocketAddress(host, port));
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localHost, int localPort) throws IOException {
        Socket socket = adapteeFactory.createSocket(address, port, localHost, localPort);
        return setProperties(socket, new InetSocketAddress(address, port));
    }

    private Socket setProperties(Socket socket, InetSocketAddress endpoint) {
        if (!(socket instanceof SSLSocket)) {
            return socket;
        }

        SSLSocket sslSocket = (SSLSocket) socket;
        // String[] supportedProtocols = sslSocket.getSupportedProtocols();
        // String[] supportedCipherSuites = sslSocket.getSupportedCipherSuites();
        SSLConfigurationService sslConfigService = Services.getService(SSLConfigurationService.class);
        sslSocket.setEnabledProtocols(sslConfigService.getSupportedProtocols());
        sslSocket.setEnabledCipherSuites(sslConfigService.getSupportedCipherSuites());
        sslSocket.setUseClientMode(true);
        if (LOG.isDebugEnabled()) {
            sslSocket.addHandshakeCompletedListener(this);
        }
        HostnameVerifier hostnameVerifier = sslConfigService.isVerifyHostname() ? new com.openexchange.net.ssl.apache.DefaultHostnameVerifier() : null;
        return new DelegatingSSLSocket(sslSocket, endpoint, hostnameVerifier);
    }

    @Override
    public void handshakeCompleted(final HandshakeCompletedEvent event) {
        Object arg = new Object() { @Override public String toString() { return event.getSocket().getInetAddress().getHostAddress(); }};
        LOG.debug("Successfully handshaked with host {}", arg);
    }

}

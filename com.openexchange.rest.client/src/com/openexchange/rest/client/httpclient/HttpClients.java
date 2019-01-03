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

package com.openexchange.rest.client.httpclient;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.ProtocolException;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.DateUtils;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.ConnectionRequest;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.util.PublicSuffixMatcher;
import org.apache.http.conn.util.PublicSuffixMatcherLoader;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.CookieSpecRegistries;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.cookie.DefaultCookieSpec;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.pool.PoolStats;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.java.InetAddresses;
import com.openexchange.java.Streams;
import com.openexchange.net.ssl.SSLSocketFactoryProvider;
import com.openexchange.net.ssl.config.SSLConfigurationService;
import com.openexchange.rest.client.httpclient.internal.WrappedClientsRegistry;
import com.openexchange.rest.client.httpclient.ssl.EasySSLSocketFactory;
import com.openexchange.rest.client.osgi.RestClientServices;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link HttpClients} - Utility class for HTTP client.
 * <p>
 * See <a href="http://svn.apache.org/repos/asf/httpcomponents/httpclient/branches/4.0.x/httpclient/src/examples/org/apache/http/examples/client/">here</a> for several examples.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.1
 */
public final class HttpClients {

    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(HttpClients.class);

    /**
     * Initializes a new {@link HttpClients}.
     */
    private HttpClients() {
        super();
    }

    /** The default timeout for client connections. */
    private static final int DEFAULT_TIMEOUT_MILLIS = 30000;

    /** How long connections are kept alive. */
    private static final int KEEP_ALIVE_DURATION_SECS = 20;

    /** How often the monitoring thread checks for connections to close. */
    private static final int KEEP_ALIVE_MONITOR_INTERVAL_SECS = 5;

    /** Maximum total connections available for the connection manager */
    private static final int MAX_TOTAL_CONNECTIONS = 20;

    /** Maximum connections per route */
    private static final int MAX_CONNECTIONS_PER_ROUTE = 10;

    /** Default socket buffer size */
    private static final int DEFAULT_SOCKET_BUFFER_SIZE = 8192;

    /**
     * Creates a {@link CloseableHttpClient} instance.
     *
     * @param userAgent The optional user agent identifier
     * @return A newly created {@link CloseableHttpClient} instance
     */
    public static CloseableHttpClient getHttpClient(String userAgent) {
        return getHttpClient(new ClientConfig().setUserAgent(userAgent));
    }

    /**
     * Creates a {@link CloseableHttpClient} instance.
     *
     * @param config The configuration settings for the client
     * @return A newly created {@link CloseableHttpClient} instance
     */
    public static CloseableHttpClient getHttpClient(final ClientConfig config) {
        SSLSocketFactoryProvider factoryProvider = RestClientServices.getOptionalService(SSLSocketFactoryProvider.class);
        if (null != factoryProvider) {
            SSLConfigurationService sslConfig = RestClientServices.getOptionalService(SSLConfigurationService.class);
            if (null != sslConfig) {
                return getHttpClient(config, factoryProvider, sslConfig);
            }
        }

        return WrappedClientsRegistry.getInstance().createWrapped(config);
    }

    /**
     * Creates a {@link CloseableHttpClient} instance.
     *
     * @param config The configuration settings for the client
     * @param factoryProvider The provider for the appropriate <code>SSLSocketFactory</code> instance to use
     * @param sslConfig The SSL configuration service to use
     * @return A newly created {@link CloseableHttpClient} instance
     */
    public static CloseableHttpClient getHttpClient(ClientConfig config, SSLSocketFactoryProvider factoryProvider, SSLConfigurationService sslConfig) {
        // Initialize ClientConnectionManager
        ClientConnectionManager ccm = initializeClientConnectionManagerUsing(config, factoryProvider, sslConfig);

        // Initialize CloseableHttpClient using the ClientConnectionManager and client configuration
        return initializeHttpClientUsing(config, ccm);
    }

    /**
     * Creates a fall-back {@link CloseableHttpClient} instance.
     * <p>
     * <div style="margin-left: 0.1in; margin-right: 0.5in; margin-bottom: 0.1in; background-color:#FFDDDD;">Exclusively invoked internally! Do not use!</div>
     * <p>
     *
     * @param config The configuration settings for the client
     * @return A newly created {@link CloseableHttpClient} instance
     */
    public static CloseableHttpClient getFallbackHttpClient(ClientConfig config) {
        // Initialize ClientConnectionManager
        ClientConnectionManager ccm = initializeFallbackClientConnectionManagerUsing(config);

        // Initialize CloseableHttpClient using the ClientConnectionManager and client configuration
        return initializeHttpClientUsing(config, ccm);
    }

    private static ClientConnectionManager initializeClientConnectionManagerUsing(ClientConfig config, SSLSocketFactoryProvider factoryProvider, SSLConfigurationService sslConfig) {
        // Host name verification is done implicitly (if enabled through configuration) through com.openexchange.tools.ssl.DelegatingSSLSocket
        //javax.net.ssl.HostnameVerifier hostnameVerifier = sslConfig.isVerifyHostname() ? new DefaultHostnameVerifier() : NoopHostnameVerifier.INSTANCE;
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory> create()
            .register("http", PlainConnectionSocketFactory.getSocketFactory())
            .register("https", new SSLConnectionSocketFactory(factoryProvider.getDefault(), sslConfig.getSupportedProtocols(), sslConfig.getSupportedCipherSuites(), NoopHostnameVerifier.INSTANCE))
            .build();
        return initializeClientConnectionManagerUsing(config, socketFactoryRegistry);
    }

    private static ClientConnectionManager initializeFallbackClientConnectionManagerUsing(ClientConfig config) {
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory> create()
            .register("http", PlainConnectionSocketFactory.getSocketFactory())
            .register("https", EasySSLSocketFactory.getInstance())
            .build();
        return initializeClientConnectionManagerUsing(config, socketFactoryRegistry);
    }

    private static ClientConnectionManager initializeClientConnectionManagerUsing(ClientConfig config, Registry<ConnectionSocketFactory> socketFactoryRegistry) {
        ClientConnectionManager ccm = new ClientConnectionManager(socketFactoryRegistry, config.keepAliveMonitorInterval);
        ccm.setDefaultMaxPerRoute(config.maxConnectionsPerRoute);
        ccm.setMaxTotal(config.maxTotalConnections);
        ccm.setIdleConnectionCloser(new IdleConnectionCloser(ccm, config.keepAliveDuration));
        ccm.setDefaultSocketConfig(SocketConfig.custom().setSoTimeout(config.socketReadTimeout).setRcvBufSize(config.socketBufferSize).setSndBufSize(config.socketBufferSize).build());
        return ccm;
    }

    private static CloseableHttpClient initializeHttpClientUsing(final ClientConfig config, HttpClientConnectionManager ccm) {
        HttpClientBuilder clientBuilder = org.apache.http.impl.client.HttpClients.custom()
            .setConnectionManager(ccm)
            .setDefaultRequestConfig(RequestConfig.custom().setConnectTimeout(config.connectionTimeout).setSocketTimeout(config.socketReadTimeout).setProxy(config.proxy).setCookieSpec("lenient").build());

        if (config.keepAliveStrategy == null) {
            clientBuilder.setKeepAliveStrategy(new KeepAliveStrategy(config.keepAliveDuration));
        } else {
            clientBuilder.setKeepAliveStrategy(config.keepAliveStrategy);
        }

        if(config.connectionReuseStrategy != null) {
            clientBuilder.setConnectionReuseStrategy(config.connectionReuseStrategy);
        }

        if (config.denyLocalRedirect) {
            clientBuilder.setRedirectStrategy(DenyLocalRedirectStrategy.DENY_LOCAL_INSTANCE);
        }
        if (null != config.userAgent) {
            clientBuilder.setUserAgent(config.userAgent);
        }
        CredentialsProvider credentialsProvider = null;
        if (null != config.credentials) {
            credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, config.credentials);
            clientBuilder.addInterceptorFirst(new PreemptiveAuth());
        }
        HttpHost proxy = config.proxy;
        if (null != proxy) {
            clientBuilder.setProxy(proxy);
            if (null != config.proxyLogin && null != config.proxyPassword) {
                Credentials credentials = new UsernamePasswordCredentials(config.proxyLogin, config.proxyPassword);
                if (null == credentialsProvider) {
                    credentialsProvider = new BasicCredentialsProvider();
                }
                credentialsProvider.setCredentials(new AuthScope(proxy.getHostName(), proxy.getPort()), credentials);
            }
        }
        if (null != credentialsProvider) {
            clientBuilder.setDefaultCredentialsProvider(credentialsProvider);
        }
        if (null != config.cookieStore) {
            clientBuilder.setDefaultCookieStore(config.cookieStore);
        }
        PublicSuffixMatcher publicSuffixMatcher = PublicSuffixMatcherLoader.getDefault();
        clientBuilder.setPublicSuffixMatcher(publicSuffixMatcher);
        {
            LenientCookieSpecProvider lenientCookieSpecProvider = new LenientCookieSpecProvider();
            RegistryBuilder<CookieSpecProvider> builder = CookieSpecRegistries.createDefaultBuilder(publicSuffixMatcher);
            builder.register(CookieSpecs.DEFAULT, lenientCookieSpecProvider);
            builder.register("lenient", lenientCookieSpecProvider);
            clientBuilder.setDefaultCookieSpecRegistry(builder.build());
        }

        if (false == config.contentCompressionDisabled) {
            // Support content compression
            clientBuilder.addInterceptorLast(new HttpResponseInterceptor() {

                @Override
                public void process(HttpResponse response, HttpContext context) throws HttpException, IOException {
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        final Header ceheader = entity.getContentEncoding();
                        if (ceheader != null) {
                            final HeaderElement[] codecs = ceheader.getElements();
                            for (final HeaderElement codec : codecs) {
                                if (codec.getName().equalsIgnoreCase("gzip")) {
                                    response.setEntity(new GzipDecompressingEntity(response.getEntity()));
                                    return;
                                }
                            }
                        }
                    }
                }
            });

            clientBuilder.addInterceptorLast(new HttpRequestInterceptor() {

                @Override
                public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
                    if (!request.containsHeader("Accept-Encoding")) {
                        request.addHeader("Accept-Encoding", "gzip");
                    }
                }
            });
        }
        clientBuilder.useSystemProperties();
        return clientBuilder.build();
    }

    /**
     * A container for HTTP client settings. All settings are pre-set to default values.
     * However those probably don't fit your use case, so you should adjust them accordingly.
     * Settings can be applied in a builder-like way, e.g.:
     * <pre>
     * ClientConfig config = ClientConfig.newInstance()
     *     .setConnectionTimeout(10000)
     *     .setSocketReadTimeout(10000);
     * </pre>
     */
    public static final class ClientConfig {

        int socketReadTimeout = DEFAULT_TIMEOUT_MILLIS;
        int connectionTimeout = DEFAULT_TIMEOUT_MILLIS;
        int maxTotalConnections = MAX_TOTAL_CONNECTIONS;
        int maxConnectionsPerRoute = MAX_CONNECTIONS_PER_ROUTE;
        int keepAliveDuration = KEEP_ALIVE_DURATION_SECS;
        int keepAliveMonitorInterval = KEEP_ALIVE_MONITOR_INTERVAL_SECS;
        int socketBufferSize = DEFAULT_SOCKET_BUFFER_SIZE;
        String userAgent;
        HttpHost proxy;
        String proxyLogin;
        String proxyPassword;
        Credentials credentials;
        CookieStore cookieStore;
        boolean denyLocalRedirect;

        ConnectionKeepAliveStrategy keepAliveStrategy;
        ConnectionReuseStrategy connectionReuseStrategy;
        boolean contentCompressionDisabled = false;

        ClientConfig() {
            super();
            denyLocalRedirect = false;
        }

        /**
         * Creates a new {@link ClientConfig} instance.
         */
        public static ClientConfig newInstance() {
            return new ClientConfig();
        }

        /**
         * Sets whether to deny redirect attempts to a local address.
         * <p>
         * If set to <code>true</code> the host of every redirect URL is checked if its IP address is resolvable to a network-internal address by checking:
         * <pre>
         *    InetAddress inetAddress = InetAddress.getByName(extractedHost);
         *    if (inetAddress.isAnyLocalAddress() || inetAddress.isSiteLocalAddress() || inetAddress.isLoopbackAddress() || inetAddress.isLinkLocalAddress()) {
         *       // Deny!
         *    }
         * </pre>
         *
         * @param denyLocalRedirect <code>true</code> to deny redirect attempts to a local address; otherwise <code>false</code>
         */
        public ClientConfig setDenyLocalRedirect(boolean denyLocalRedirect) {
            this.denyLocalRedirect = denyLocalRedirect;
            return this;
        }

        /**
         * Sets the cookie store
         *
         * @param cookieStore The cookie store to set
         */
        public ClientConfig setCookieStore(CookieStore cookieStore) {
            this.cookieStore = cookieStore;
            return this;
        }

        /**
         * Sets the credentials
         *
         * @param login The login to set
         * @param password The password to set
         */
        public ClientConfig setCredentials(String login, String password) {
            this.credentials = new UsernamePasswordCredentials(login, password);
            return this;
        }

        /**
         * Sets the proxy information.
         *
         * @param proxy The proxy to set
         */
        public ClientConfig setProxy(URI proxyUrl, String proxyLogin, String proxyPassword) {
            boolean isHttps = proxyUrl.getScheme().equalsIgnoreCase("https");
            int prxyPort = proxyUrl.getPort();
            if (prxyPort == -1) {
                prxyPort = isHttps ? 443 : 80;
            }
            return setProxy(new HttpHost(proxyUrl.getHost(), prxyPort, proxyUrl.getScheme()), proxyLogin, proxyPassword);
        }

        /**
         * Sets the proxy information.
         *
         * @param proxy The proxy to set
         */
        public ClientConfig setProxy(HttpHost proxy, String proxyLogin, String proxyPassword) {
            this.proxy = proxy;
            this.proxyLogin = proxyLogin;
            this.proxyPassword = proxyPassword;
            return this;
        }

        /**
         * Sets the socket read timeout in milliseconds. A timeout value of zero
         * is interpreted as an infinite timeout.
         * Default: {@value #DEFAULT_TIMEOUT_MILLIS}
         *
         * @param socketReadTimeout The timeout
         */
        public ClientConfig setSocketReadTimeout(int socketReadTimeout) {
            this.socketReadTimeout = socketReadTimeout;
            return this;
        }

        /**
         * Sets the connection timeout in milliseconds. A timeout value of zero
         * is interpreted as an infinite timeout.
         * Default: {@value #DEFAULT_TIMEOUT_MILLIS}
         *
         * @param connectionTimeout The timeout
         */
        public ClientConfig setConnectionTimeout(int connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
            return this;
        }

        /**
         * Sets the max. number of concurrent connections that can be opened by the
         * client instance.
         * Default: {@value #MAX_TOTAL_CONNECTIONS}
         *
         * @param maxTotalConnections The number of connections
         */
        public ClientConfig setMaxTotalConnections(int maxTotalConnections) {
            this.maxTotalConnections = maxTotalConnections;
            return this;
        }

        /**
         * Sets the max. number of concurrent connections that can be opened by the
         * client instance per route.
         * Default: {@value #MAX_CONNECTIONS_PER_ROUTE}
         *
         * @param maxTotalConnections The number of connections
         */
        public ClientConfig setMaxConnectionsPerRoute(int maxConnectionsPerRoute) {
            this.maxConnectionsPerRoute = maxConnectionsPerRoute;
            return this;
        }

        /**
         * Sets the number of seconds that connections shall be kept alive.
         * Default: {@value #KEEP_ALIVE_DURATION_SECS}.
         *
         * @param keepAliveDuration The keep alive duration
         */
        public ClientConfig setKeepAliveDuration(int keepAliveDuration) {
            this.keepAliveDuration = keepAliveDuration;
            return this;
        }

        /**
         * Instead of a duration a own KeepAliveStrategy can be set.
         * Default: null
         *
         * @param strategy The {@link KeepAliveStrategy}
         */
        public ClientConfig setKeepAliveStrategy(ConnectionKeepAliveStrategy strategy) {
            this.keepAliveStrategy = strategy;
            return this;
        }

        /**
         * Sets the {@link ConnectionReuseStrategy}
         * Default: null
         *
         * @param strategy The {@link ConnectionReuseStrategy}
         */
        public ClientConfig setConnectionReuseStrategy(ConnectionReuseStrategy strategy) {
            this.connectionReuseStrategy = strategy;
            return this;
        }

        /**
         * Sets the contentCompressionDisabled flag
         * Default: false
         *
         * @param contentCompressionDisabled
         */
        public ClientConfig setContentCompressionDisabled(boolean contentCompressionDisabled) {
            this.contentCompressionDisabled = contentCompressionDisabled;
            return this;
        }

        /**
         * The interval in seconds between two monitoring runs that close stale connections
         * which exceeded the keep-alive duration.
         * Default: {@value #KEEP_ALIVE_MONITOR_INTERVAL_SECS}
         *
         * @param keepAliveMonitorInterval The interval
         */
        public ClientConfig setKeepAliveMonitorInterval(int keepAliveMonitorInterval) {
            this.keepAliveMonitorInterval = keepAliveMonitorInterval;
            return this;
        }

        /**
         * Sets the socket buffer size in bytes.
         * Default: {@value #DEFAULT_SOCKET_BUFFER_SIZE}
         *
         * @param socketBufferSize The buffer size.
         */
        public ClientConfig setSocketBufferSize(int socketBufferSize) {
            this.socketBufferSize = socketBufferSize;
            return this;
        }

        /**
         * Sets the user agent identifier to be used.
         * Default: <code>null</code>
         *
         * @param userAgent The user agent
         */
        public ClientConfig setUserAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder(256);
            builder.append("[");
            builder.append("socketReadTimeout=").append(socketReadTimeout);
            builder.append(", connectionTimeout=").append(connectionTimeout);
            builder.append(", maxTotalConnections=").append(maxTotalConnections);
            builder.append(", maxConnectionsPerRoute=").append(maxConnectionsPerRoute);
            builder.append(", keepAliveDuration=").append(keepAliveDuration);
            builder.append(", keepAliveMonitorInterval=").append(keepAliveMonitorInterval);
            builder.append(", socketBufferSize=").append(socketBufferSize);
            if (userAgent != null) {
                builder.append(", userAgent=").append(userAgent);
            }
            builder.append("]");
            return builder.toString();
        }
    }

    /**
     * Applies the default timeout of 30sec to given HTTP request.
     *
     * @param request The HTTP request
     */
    public static void setDefaultRequestTimeout(HttpRequestBase request) {
        if (null == request) {
            return;
        }
        request.setConfig(RequestConfig.custom().setConnectTimeout(DEFAULT_TIMEOUT_MILLIS).setSocketTimeout(DEFAULT_TIMEOUT_MILLIS).build());
    }

    /**
     * Applies the specified timeout to given HTTP request.
     *
     * @param timeoutMillis The timeout in milliseconds to apply
     * @param request The HTTP request
     */
    public static void setRequestTimeout(int timeoutMillis, HttpRequestBase request) {
        if (null == request || timeoutMillis <= 0) {
            return;
        }
        request.setConfig(RequestConfig.custom().setConnectTimeout(timeoutMillis).setSocketTimeout(timeoutMillis).build());
    }

    /**
     * Shuts-down given <code>HttpClient</code> instance
     *
     * @param httpclient The <code>HttpClient</code> instance to shut-down
     */
    public static void shutDown(HttpClient httpclient) {
        if (null != httpclient) {
            // When HttpClient instance is no longer needed,
            // shut down the connection manager to ensure
            // immediate deallocation of all system resources
            httpclient.getConnectionManager().shutdown();
            if (httpclient instanceof CloseableHttpClient) {
                Streams.close((CloseableHttpClient) httpclient);
            }
        }
    }

    /**
     * Closes the supplied HTTP request / response resources silently.
     * <p>
     * <ul>
     * <li>Resets internal state of the HTTP request making it reusable.</li>
     * <li>Ensures that the response's content is fully consumed and the content stream, if exists, is closed</li>
     * </ul>
     *
     * @param request The HTTP request to reset
     * @param response The HTTP response to consume and close
     */
    public static void close(HttpRequestBase request, HttpResponse response) {
        if (null != response) {
            HttpEntity entity = response.getEntity();
            if (null != entity) {
                try {
                    EntityUtils.consumeQuietly(entity);
                } catch (Exception e) {
                    LOG.trace("Failed to ensure that the entity content is fully consumed and the content stream, if exists, is closed.", e);
                }
            }
        }
        if (null != request) {
            try {
                request.reset();
            } catch (Exception e) {
                LOG.trace("Failed to reset request for making it reusable.", e);
            }
        }
    }

    /*------------------------------------------------------ CLASSES ------------------------------------------------------*/

    private static class ClientConnectionManager extends PoolingHttpClientConnectionManager {

        private volatile IdleConnectionCloser idleConnectionCloser;
        private final int keepAliveMonitorInterval;

        ClientConnectionManager(Registry<ConnectionSocketFactory> socketFactoryRegistry, int keepAliveMonitorInterval) {
            super(socketFactoryRegistry);
            this.keepAliveMonitorInterval = keepAliveMonitorInterval;
        }

        /**
         * Sets the associated {@link IdleConnectionCloser} instance
         *
         * @param idleConnectionCloser The instance to set
         */
        void setIdleConnectionCloser(IdleConnectionCloser idleConnectionCloser) {
            this.idleConnectionCloser = idleConnectionCloser;
        }

        @Override
        public ConnectionRequest requestConnection(HttpRoute route, Object state) {
            IdleConnectionCloser idleConnectionClose = this.idleConnectionCloser;
            if (null != idleConnectionClose) {
                idleConnectionClose.ensureRunning(keepAliveMonitorInterval);
            }
            return super.requestConnection(route, state);
        }

        @Override
        public void shutdown() {
            IdleConnectionCloser idleConnectionClose = this.idleConnectionCloser;
            if (null != idleConnectionClose) {
                idleConnectionClose.stop();
                this.idleConnectionCloser = null;
            }
            super.shutdown();
        }
    }

    private static class IdleConnectionCloser implements Runnable {

        private final static Logger LOGGER = LoggerFactory.getLogger(IdleConnectionCloser.class);

        private final ClientConnectionManager manager;
        private final int idleTimeoutSeconds;
        private volatile ScheduledTimerTask timerTask;

        IdleConnectionCloser(ClientConnectionManager manager, int idleTimeoutSeconds) {
            super();
            this.manager = manager;
            this.idleTimeoutSeconds = idleTimeoutSeconds;
        }

        void ensureRunning(int checkIntervalSeconds) {
            ScheduledTimerTask tmp = timerTask;
            if (null == tmp) {
                synchronized (IdleConnectionCloser.class) {
                    tmp = timerTask;
                    if (null == tmp) {
                        TimerService service = ServerServiceRegistry.getInstance().getService(TimerService.class);
                        if (null == service) {
                            LOGGER.error("{} is missing. Can't execute run()", TimerService.class.getSimpleName());
                        } else {
                            tmp = service.scheduleWithFixedDelay(this, checkIntervalSeconds, checkIntervalSeconds, TimeUnit.SECONDS);
                            timerTask = tmp;
                        }
                    }
                }
            }
        }

        void stop() {
            ScheduledTimerTask tmp = timerTask;
            if (null != tmp) {
                synchronized (IdleConnectionCloser.class) {
                    tmp = timerTask;
                    if (null != tmp) {
                        tmp.cancel();
                        TimerService service = ServerServiceRegistry.getInstance().getService(TimerService.class);
                        if (null == service) {
                            LOGGER.error("{} is missing. Can't remove canceled tasks", TimerService.class.getSimpleName());
                        } else {
                            service.purge();
                            timerTask = null;
                        }
                    }
                }
            }
        }

        @Override
        public void run() {
            try {
                manager.closeExpiredConnections();
                manager.closeIdleConnections(idleTimeoutSeconds, TimeUnit.SECONDS);
                PoolStats totalStats = manager.getTotalStats();
                if (totalStats.getLeased() == 0 && totalStats.getPending() == 0  && totalStats.getAvailable() == 0) {
                    stop();
                }
            } catch (final Exception e) {
                stop();
            }
        }
    }

    private static final class KeepAliveStrategy implements ConnectionKeepAliveStrategy {

        private final int keepAliveSeconds;

        KeepAliveStrategy(int keepAliveSeconds) {
            super();
            this.keepAliveSeconds = keepAliveSeconds;
        }

        @Override
        public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
            // Keep-alive for the shorter of 20 seconds or what the server specifies.
            long timeout = keepAliveSeconds * 1000L;

            final HeaderElementIterator i = new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));
            while (i.hasNext()) {
                final HeaderElement element = i.nextElement();
                if ("timeout".equalsIgnoreCase(element.getName())) {
                    final String value = element.getValue();
                    if (value != null) {
                        try {
                            long b = Long.parseLong(value) * 1000;
                            timeout = (timeout <= b) ? timeout : b;
                        } catch (final NumberFormatException e) {
                            // Ignore
                        }
                    }
                }
            }

            return timeout;
        }
    }

    private static class GzipDecompressingEntity extends HttpEntityWrapper {

        /*
         * From Apache HttpClient Examples. ==================================================================== Licensed to the Apache
         * Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE file distributed with this work for
         * additional information regarding copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0
         * (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
         * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software distributed under
         * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
         * License for the specific language governing permissions and limitations under the License.
         * ==================================================================== This software consists of voluntary contributions made by
         * many individuals on behalf of the Apache Software Foundation. For more information on the Apache Software Foundation, please see
         * <http://www.apache.org/>.
         */

        public GzipDecompressingEntity(HttpEntity entity) {
            super(entity);
        }

        @Override
        public InputStream getContent() throws IOException, IllegalStateException {

            // the wrapped entity's getContent() decides about repeatability
            final InputStream wrappedin = wrappedEntity.getContent();

            return new GZIPInputStream(wrappedin);
        }

        @Override
        public long getContentLength() {
            // length of ungzipped content is not known
            return -1;
        }
    }

    private static class LenientCookieSpecProvider implements CookieSpecProvider {

        private final CookieSpec cookieSpec;

        LenientCookieSpecProvider() {
            super();
            this.cookieSpec = new LenientCookieSpec();
        }

        @Override
        public CookieSpec create(HttpContext context) {
            return cookieSpec;
        }
    }

    private static class LenientCookieSpec extends DefaultCookieSpec {

        // See org.apache.http.impl.cookie.BrowserCompatSpec.DEFAULT_DATE_PATTERNS
        private static final String[] DEFAULT_DATE_PATTERNS = new String[] {
            DateUtils.PATTERN_RFC1123,
            DateUtils.PATTERN_RFC1036,
            DateUtils.PATTERN_ASCTIME,
            "EEE, dd-MMM-yyyy HH:mm:ss z",
            "EEE, dd-MMM-yyyy HH-mm-ss z",
            "EEE, dd MMM yy HH:mm:ss z",
            "EEE dd-MMM-yyyy HH:mm:ss z",
            "EEE dd MMM yyyy HH:mm:ss z",
            "EEE dd-MMM-yyyy HH-mm-ss z",
            "EEE dd-MMM-yy HH:mm:ss z",
            "EEE dd MMM yy HH:mm:ss z",
            "EEE,dd-MMM-yy HH:mm:ss z",
            "EEE,dd-MMM-yyyy HH:mm:ss z",
            "EEE, dd-MM-yyyy HH:mm:ss z",
            "EEE, dd MMM yyyy HH:mm:ss Z",
        };

        /** Initializes a new {@link LenientCookieSpec}. */
        LenientCookieSpec() {
            super(DEFAULT_DATE_PATTERNS, false);
        }
    }

    private static class PreemptiveAuth implements HttpRequestInterceptor {

        PreemptiveAuth() {
            super();
        }

        @Override
        public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
            AuthState authState = (AuthState) context.getAttribute(HttpClientContext.TARGET_AUTH_STATE);

            // If no auth scheme available yet, try to initialize it preemptively
            if (authState.getAuthScheme() == null) {
                AuthScheme authScheme = (AuthScheme) context.getAttribute("preemptive-auth");
                CredentialsProvider credsProvider = (CredentialsProvider) context.getAttribute(HttpClientContext.CREDS_PROVIDER);
                HttpHost targetHost = (HttpHost) context.getAttribute(HttpCoreContext.HTTP_TARGET_HOST);
                if (authScheme != null) {
                    Credentials creds = credsProvider.getCredentials(new AuthScope(targetHost.getHostName(), targetHost.getPort()));
                    if (creds == null) {
                        LOG.warn("No credentials for preemptive authentication against {}", targetHost);
                    } else {
                        authState.update(authScheme, creds);
                    }
                }
            }
        }
    }

    private static class DenyLocalRedirectStrategy extends DefaultRedirectStrategy {

        static final DenyLocalRedirectStrategy DENY_LOCAL_INSTANCE = new DenyLocalRedirectStrategy();

        private DenyLocalRedirectStrategy() {
            super();
        }

        @Override
        protected URI createLocationURI(String location) throws ProtocolException {
            try {
                URI locationURI = super.createLocationURI(location);
                InetAddress inetAddress = InetAddress.getByName(locationURI.getHost());
                if (InetAddresses.isInternalAddress(inetAddress)) {
                    throw new ProtocolException("Invalid redirect URI: " + location + ". No redirect to local address allowed.");
                }
                return locationURI;
            } catch (UnknownHostException e) {
                throw new ProtocolException("Invalid redirect URI: " + location + ". Unknown host.", e);
            }
        }
    }

}

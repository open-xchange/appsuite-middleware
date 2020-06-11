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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.rest.client.httpclient.internal;

import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.I;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.util.PublicSuffixMatcher;
import org.apache.http.conn.util.PublicSuffixMatcherLoader;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.CookieSpecRegistries;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.openexchange.annotation.NonNull;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.ForcedReloadable;
import com.openexchange.config.Interests;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.net.ssl.SSLSocketFactoryProvider;
import com.openexchange.net.ssl.config.SSLConfigurationService;
import com.openexchange.rest.client.httpclient.HttpBasicConfig;
import com.openexchange.rest.client.httpclient.HttpClientBuilderModifier;
import com.openexchange.rest.client.httpclient.HttpClientConfigProvider;
import com.openexchange.rest.client.httpclient.HttpClientProperty;
import com.openexchange.rest.client.httpclient.HttpClientService;
import com.openexchange.rest.client.httpclient.ManagedHttpClient;
import com.openexchange.rest.client.httpclient.SpecificHttpClientConfigProvider;
import com.openexchange.rest.client.httpclient.WildcardHttpClientConfigProvider;
import com.openexchange.rest.client.httpclient.internal.cookiestore.RejectAllCookieStore;
import com.openexchange.server.ServiceLookup;
import com.openexchange.timer.TimerService;

/**
 * {@link HttpClientServiceImpl}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.4
 */
public class HttpClientServiceImpl implements HttpClientService, ServiceTrackerCustomizer<Object, Object>, ForcedReloadable {

    private static final String DEFAULT_COOKIE_SPEC_NAME = "lenient";
    private static final CookieStore REJECT_ALL_COOKIE_STORE = new RejectAllCookieStore();

    static final Logger LOGGER = LoggerFactory.getLogger(HttpClientServiceImpl.class);

    /** Dummy wild-card provider to signal absent provider */
    static final WildcardHttpClientConfigProvider ABSENT = new WildcardHttpClientConfigProvider() {

        @Override
        public void modify(HttpClientBuilder builder) {
            // Nothing
        }

        @Override
        public @NonNull String getClientIdPattern() {
            return "noop";
        }
    };

    /** A boolean value that indicates whether the service is shutting down or not */
    private final AtomicBoolean isShutdown;

    private final BundleContext context;
    private final ServiceLookup serviceLookup;

    final LeftoverClientConfigProvider leftoverProvider;
    final Cache<String, ManagedHttpClientImpl> httpClients;
    final ConcurrentMap<String, SpecificHttpClientConfigProvider> specificProviders = new ConcurrentHashMap<>(30, 0.9f); // Core knows about 18
    final ConcurrentMap<String, PatternEnhancedWildcardHttpClientConfigProvider> wildcardProviders = new ConcurrentHashMap<>(16, 0.9F, 1);
    private final LoadingCache<String, WildcardHttpClientConfigProvider> wildcardProvidersCache;

    /**
     * Initializes a new {@link HttpClientServiceImpl}.
     *
     * @param context The bundle context
     * @param serviceLookup The {@link ServiceLookup}
     */
    public HttpClientServiceImpl(BundleContext context, ServiceLookup serviceLookup) {
        super();
        this.context = context;
        this.serviceLookup = serviceLookup;
        this.isShutdown = new AtomicBoolean(false);
        this.leftoverProvider = new LeftoverClientConfigProvider();
        //@formatter:off
        httpClients = CacheBuilder.newBuilder()
            .initialCapacity(30)
            .expireAfterAccess(2, TimeUnit.DAYS)
            .removalListener((RemovalListener<String, ManagedHttpClientImpl>) notification -> {
                if (notification.wasEvicted()) {
                    close(notification.getKey(), notification.getValue(), false);
                }})
            .build();
        //@formatter:on
        //@formatter:off
        wildcardProvidersCache = CacheBuilder.newBuilder()
            .initialCapacity(10)
            .expireAfterAccess(30, TimeUnit.MINUTES)
            .build(new CacheLoader<String, WildcardHttpClientConfigProvider>() {

                @Override
                public WildcardHttpClientConfigProvider load(String clientId) throws Exception {
                    WildcardHttpClientConfigProvider wildcardProvider = doGetWildcardProvider(clientId);
                    return wildcardProvider == null ? ABSENT : wildcardProvider;
                }
            });
        //@formatter:on
    }

    @Override
    public ManagedHttpClient getHttpClient(String httpClientId) {
        if (Strings.isEmpty(httpClientId)) {
            throw new IllegalArgumentException("The argument must not be empty!");
        }
        checkShutdownStatus();

        LOGGER.trace("Getting client with ID {}", httpClientId);
        try {
            return httpClients.get(httpClientId, () -> create(httpClientId));
        } catch (ExecutionException e) {
            throw new IllegalStateException("Error getting or intitializing HTTP client for ID: " + httpClientId, e);
        }

    }

    @Override
    public void destroyHttpClient(String httpClientId) {
        if (Strings.isEmpty(httpClientId)) {
            throw new IllegalArgumentException("The argument must not be empty!");
        }
        checkShutdownStatus();

        ManagedHttpClientImpl client = httpClients.getIfPresent(httpClientId);
        if (client != null) {
            LOGGER.debug("Explicit closing HTTP client for id {} via internal API.", httpClientId);
            close(httpClientId, client, true);
        }
    }

    // ------------------------------------------------ Tracking of providers --------------------------------------------------------------

    @Override
    public synchronized Object addingService(ServiceReference<Object> reference) {
        checkShutdownStatus();

        // Acquire provider instance
        Object service = context.getService(reference);
        if (service instanceof SpecificHttpClientConfigProvider) {
            SpecificHttpClientConfigProvider provider = (SpecificHttpClientConfigProvider) service;
            String clientId = provider.getClientId();
            if (specificProviders.putIfAbsent(clientId, provider) == null) {
                ManagedHttpClientImpl existing = httpClients.getIfPresent(clientId);
                if (existing != null) {
                    /*
                     * Some class has already created a client under this ID, reload client with provider specific configuration
                     */
                    reloadClient(clientId, adjustConfig(clientId, provider.configureHttpBasicConfig(createNewDefaultConfig())), provider, false);
                }
                return provider;
            }

            // Such a provider already contained
            LOGGER.error("Unable to add provider {}. Already have a provider for {}", provider.getClass().getSimpleName(), clientId);
            context.ungetService(reference);
            return null;
        }

        if (service instanceof WildcardHttpClientConfigProvider) {
            WildcardHttpClientConfigProvider provider = (WildcardHttpClientConfigProvider) service;
            String clientIdPattern = provider.getClientIdPattern();
            if (wildcardProviders.putIfAbsent(clientIdPattern, new PatternEnhancedWildcardHttpClientConfigProvider(provider)) == null) {
                LOGGER.trace("Added provider for pattern {}", clientIdPattern);
                reloadClientsForWildcardProvider(wildcardProviders.get(clientIdPattern));
                wildcardProvidersCache.invalidateAll();
                return provider;
            }

            LOGGER.error("Already registered a provider with pattern {}", clientIdPattern);
            context.ungetService(reference);
            return null;
        }

        // Neither SpecificHttpClientConfigProvider nor WildcardHttpClientConfigProvider
        context.ungetService(reference);
        return null;
    }

    @Override
    public void modifiedService(ServiceReference<Object> reference, Object service) {
        // Nothing to do
    }

    @Override
    public synchronized void removedService(ServiceReference<Object> reference, Object service) {
        if (service instanceof SpecificHttpClientConfigProvider) {
            SpecificHttpClientConfigProvider provider = (SpecificHttpClientConfigProvider) service;
            String clientId = provider.getClientId();
            if (specificProviders.remove(clientId) != null) {
                close(clientId, httpClients.getIfPresent(clientId), true);
                LOGGER.trace("Removed provider for {}", clientId);
            }
        } else if (service instanceof WildcardHttpClientConfigProvider) {
            WildcardHttpClientConfigProvider provider = (WildcardHttpClientConfigProvider) service;
            PatternEnhancedWildcardHttpClientConfigProvider removed = wildcardProviders.remove(provider.getClientIdPattern());
            if (null == removed) {
                LOGGER.warn("Unable to find provider for {}. Won't remove any HTTP client for provided pattern.", provider.getClientIdPattern());
            } else {
                wildcardProvidersCache.invalidateAll();

                /*
                 * Remove HTTP clients, which matches the regex and are not provided by a SpecificHttpClientConfigProvider
                 */
                Pattern pattern = removed.getRegularExpressionPattern();
                for (Map.Entry<String, ManagedHttpClientImpl> entry : httpClients.asMap().entrySet()) {
                    if (false == specificProviders.containsKey(entry.getKey()) && pattern.matcher(entry.getKey()).matches()) {
                        close(entry.getKey(), entry.getValue(), true);
                    }
                }
            }
        }

        context.ungetService(reference);
    }

    // ------------------------------------------------------ Service shutdown -------------------------------------------------------------

    /**
     * Closes all HTTP clients and shuts down the service
     * See {@link #close(String, CloseableHttpClient)} for details
     */
    public synchronized void shutdown() {
        if (isShutdown.compareAndSet(false, true)) {
            for (Map.Entry<String, ManagedHttpClientImpl> entry : httpClients.asMap().entrySet()) {
                close(entry.getKey(), entry.getValue(), true);
            }
        }
    }

    /*-
     * -------------------------------------------------------------------------------------------------------------------------------------
     * --------------------------------------------------------- Private helpers -----------------------------------------------------------
     * -------------------------------------------------------------------------------------------------------------------------------------
     */

    /**
     * Initializes a new {@link ClientConnectionManager} managing monitoring for the HTTP client
     *
     * @param clientId The client identifier of the HTTP client. Will be used as dimension within the monitoring, too.
     * @param config The HTTP configuration
     * @throws OXException In case of missing SSL services
     */
    ClientConnectionManager initializeClientConnectionManager(String clientId, HttpBasicConfig config) throws OXException {
        SSLSocketFactoryProvider factoryProvider = serviceLookup.getServiceSafe(SSLSocketFactoryProvider.class);
        SSLConfigurationService sslConfig = serviceLookup.getServiceSafe(SSLConfigurationService.class);
        return initializeClientConnectionManagerUsing(clientId, config, factoryProvider, sslConfig);
    }

    /**
     * Initializes a new {@link ClientConnectionManager} managing monitoring for the HTTP client
     *
     * @param clientId The client identifier of the HTTP client. Will be used as dimension within the monitoring, too.
     * @param config The HTTP configuration
     * @param factoryProvider The {@link SSLConnectionSocketFactory} to use for the connections
     * @param sslConfig The {@link SSLConfigurationService} to use for the connections
     * @return A {@link ClientConnectionManager}
     */
    // By Thorben from HttpClients
    private ClientConnectionManager initializeClientConnectionManagerUsing(String clientId, HttpBasicConfig config, SSLSocketFactoryProvider factoryProvider, SSLConfigurationService sslConfig) {
        // Host name verification is done implicitly (if enabled through configuration) through com.openexchange.tools.ssl.DelegatingSSLSocket
        //javax.net.ssl.HostnameVerifier hostnameVerifier = sslConfig.isVerifyHostname() ? new DefaultHostnameVerifier() : NoopHostnameVerifier.INSTANCE;
        //@formatter:off
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory> create()
            .register("http", PlainConnectionSocketFactory.getSocketFactory())
            .register("https", new SSLConnectionSocketFactory(factoryProvider.getDefault(), sslConfig.getSupportedProtocols(), sslConfig.getSupportedCipherSuites(), NoopHostnameVerifier.INSTANCE))
            .build();
        //@formatter:on

        ClientConnectionManager ccm = new ClientConnectionManager(clientId, config.getConnectTimeout(), config.getConnectionRequestTimeout(), config.getKeepAliveMonitorInterval(), socketFactoryRegistry);
        ccm.setDefaultMaxPerRoute(config.getMaxConnectionsPerRoute());
        ccm.setMaxTotal(config.getMaxTotalConnections());
        ccm.setIdleConnectionCloser(new IdleConnectionCloser(ccm, config.getKeepAliveDuration()));
        ccm.setDefaultSocketConfig(SocketConfig.custom().setSoTimeout(config.getSocketReadTimeout()).build());
        ccm.setDefaultConnectionConfig(ConnectionConfig.custom().setBufferSize(config.getSocketBufferSize()).build());
        return ccm;
    }

    /**
     * Prepares the HTTP client in terms of configuration. Via the modifier, gives a callback to implementations for additional changes.
     *
     * @param clientId The client identifier
     * @param modifier The modifier to adjust the HTTP client before creating
     * @param confi The HTTP basic configuration to build up on
     * @param ccm The connection manager
     * @return A {@link CloseableHttpClient}
     */
    CloseableHttpClient initializeHttpClient(String clientId, HttpClientBuilderModifier modifier, HttpBasicConfig config, HttpClientConnectionManager ccm) {
        /*
         * Prepare client builder
         */
        //@formatter:off
        HttpClientBuilder builder = HttpClientBuilder.create()
            .setConnectionManager(ccm)
            .setDefaultRequestConfig(RequestConfig.custom()
                .setConnectTimeout(config.getConnectTimeout())
                .setSocketTimeout(config.getSocketReadTimeout())
                .setConnectionRequestTimeout(config.getConnectionRequestTimeout())
                .setCookieSpec(DEFAULT_COOKIE_SPEC_NAME)
                .build())
            .setRequestExecutor(new MeteredHttpRequestExecutor(clientId));
        //@formatter:on

        /*
         * Add common settings
         */
        builder.setKeepAliveStrategy(new KeepAliveStrategy(config.getKeepAliveDuration()));
        builder.setRedirectStrategy(DenyLocalRedirectStrategy.DENY_LOCAL_INSTANCE);

        PublicSuffixMatcher publicSuffixMatcher = PublicSuffixMatcherLoader.getDefault();
        builder.setPublicSuffixMatcher(publicSuffixMatcher);
        {
            LenientCookieSpecProvider lenientCookieSpecProvider = new LenientCookieSpecProvider();
            RegistryBuilder<CookieSpecProvider> registryBuilder = CookieSpecRegistries.createDefaultBuilder(publicSuffixMatcher);
            registryBuilder.register(CookieSpecs.DEFAULT, lenientCookieSpecProvider);
            registryBuilder.register(DEFAULT_COOKIE_SPEC_NAME, lenientCookieSpecProvider);
            builder.setDefaultCookieSpecRegistry(registryBuilder.build());
        }

        builder.addInterceptorLast(new HttpRequestInterceptor() {

            @Override
            public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
                if (!request.containsHeader("Accept-Encoding")) {
                    request.addHeader("Accept-Encoding", "gzip");
                }
            }
        });

        builder.setDefaultCookieStore(REJECT_ALL_COOKIE_STORE);
        builder.useSystemProperties();

        /*
         * Finally adjust the client and build it.
         */
        modifier.modify(builder);
        return builder.build();
    }

    HttpBasicConfigImpl createNewDefaultConfig() {
        LeanConfigurationService leanConfigurationService = serviceLookup.getService(LeanConfigurationService.class);
        return createNewDefaultConfig(leanConfigurationService);
    }

    HttpBasicConfigImpl createNewDefaultConfig(LeanConfigurationService leanConfigurationService) {
        return new HttpBasicConfigImpl(Optional.ofNullable(leanConfigurationService));
    }

    /**
     * Adjusts configuration based on properties that start with
     * {@value HttpClientProperty#PREFIX} and the given identifier.
     * <p>
     * If a value for a specified property is set, this will overwrite
     * the set value in the configuration.
     *
     * @param clientId The identifier
     * @param httpBasicConfig The configuration to adjust
     * @return The adjusted configuration
     */
    HttpBasicConfig adjustConfig(String clientId, HttpBasicConfig httpBasicConfig) {
        ConfigurationService configService = serviceLookup.getOptionalService(ConfigurationService.class);
        if (null != configService) {
            Map<String, String> optionals = Collections.singletonMap(HttpClientProperty.SERVICE_IDENTIFIER, clientId);
            for (HttpClientProperty property : HttpClientProperty.values()) {
                adjustConfig(property, httpBasicConfig, optionals, configService);
            }
        }
        return httpBasicConfig;
    }

    private static void adjustConfig(HttpClientProperty property, HttpBasicConfig httpBasicConfig, Map<String, String> optionals, ConfigurationService configService) {
        String propertyName = property.getFQPropertyName(optionals);
        String value = configService.getProperty(propertyName);
        if (Strings.isNotEmpty(value)) {
            try {
                property.setInConfig(httpBasicConfig, Integer.valueOf(value.trim()));
            } catch (NumberFormatException e) {
                LOGGER.info("Unable to parse value {} for property {}", value, propertyName, e);
            }
        }
    }

    /**
     * Closes a HTTP client.
     *
     * @param clientId The client identifier
     * @param managedClient The client to close
     * @param removeFromCache <code>true</code> to remove the client from cache,
     *            <code>false</code> otherwise
     */
    void close(String clientId, ManagedHttpClientImpl managedClient, boolean removeFromCache) {
        /*
         * The ClientConnectionManager will be closed implicit by the
         * HTTP client itself.
         */
        if (null != managedClient) {
            LOGGER.debug("Closing HTTP client for service {}", clientId);
            if (removeFromCache) {
                httpClients.invalidate(clientId);
            }
            closeWithDelay(clientId, managedClient.getConfigHash(), managedClient.unset());
        }
    }

    /**
     * Closes the given HTTP client with a delay of 10 seconds, so that all
     * classes using the specified client have time to finish their operation.
     *
     * @param httpClientId The identifier of the HTTP client
     * @param hashCode The hash code of the client configuration for logging
     * @param httpClient The HTTP client to close
     */
    void closeWithDelay(String httpClientId, int hashCode, CloseableHttpClient httpClient) {
        if (null == httpClient) {
            return;
        }

        TimerService timerService = serviceLookup.getService(TimerService.class);
        if (null == timerService) {
            LOGGER.debug("Timer service is unavailable. Closing client now.");
            Streams.close(httpClient);
            return;
        }
        timerService.schedule(new Runnable() {

            @Override
            public void run() {
                LOGGER.trace("Closing HTTP client for {} with client config hash {} now.", httpClientId, I(hashCode));
                Streams.close(httpClient);
            }
        }, 10, TimeUnit.SECONDS);

    }

    /**
     * Checks whether the service is currently shutting down or not.
     *
     * @throws IllegalStateException If the service is shut-down
     */
    private void checkShutdownStatus() {
        if (isShutdown.get()) {
            throw new IllegalStateException("Service is shutting down.");
        }
    }

    /*
     * -----------------------------------------------------------------
     * -------------------- Reloadable functions -----------------------
     * -----------------------------------------------------------------
     */

    @Override
    public synchronized void reloadConfiguration(ConfigurationService notNeeded) {
        LeanConfigurationService configService = serviceLookup.getService(LeanConfigurationService.class);
        if (configService == null) {
            LOGGER.warn("Cannot reload HTTP clients configuration because of absent LeanConfigurationService.");
            return;
        }

        /*
         * Reload affected clients
         */
        for (Entry<String, SpecificHttpClientConfigProvider> entry : specificProviders.entrySet()) {
            String clientId = entry.getKey();
            if (null != httpClients.getIfPresent(clientId)) {
                SpecificHttpClientConfigProvider provider = entry.getValue();
                HttpBasicConfig newClientConfig = adjustConfig(clientId, provider.configureHttpBasicConfig(createNewDefaultConfig(configService)));
                reloadClient(clientId, newClientConfig, provider, true);
            }
        }

        for (Entry<String, PatternEnhancedWildcardHttpClientConfigProvider> entry : wildcardProviders.entrySet()) {
            PatternEnhancedWildcardHttpClientConfigProvider wildcardProvider = entry.getValue();
            reloadClientsForWildcardProvider(wildcardProvider, configService);
        }

        wildcardProvidersCache.invalidateAll();
    }

    /**
     * Reloads all clients that belongs to the given providers domain
     *
     * @param wildcardProvider The {@link WildcardHttpClientConfigProvider}
     */
    private void reloadClientsForWildcardProvider(PatternEnhancedWildcardHttpClientConfigProvider wildcardProvider) {
        reloadClientsForWildcardProvider(wildcardProvider, serviceLookup.getService(LeanConfigurationService.class));
    }

    /**
     * Reloads all clients that belongs to the given providers domain
     *
     * @param wildcardProvider The {@link WildcardHttpClientConfigProvider}
     * @param configService The {@link LeanConfigurationService}
     */
    private void reloadClientsForWildcardProvider(PatternEnhancedWildcardHttpClientConfigProvider wildcardProvider, LeanConfigurationService configService) {
        List<String> activeClientIds = new ArrayList<>(httpClients.asMap().keySet());
        activeClientIds.removeAll(specificProviders.keySet());
        if (activeClientIds.isEmpty()) {
            return;
        }
        activeClientIds.stream().filter(id -> wildcardProvider.getRegularExpressionPattern().matcher(id).matches()).forEach(id -> {
            HttpBasicConfig newClientConfig = wildcardProvider.configureHttpBasicConfig(id, createNewDefaultConfig(configService));
            newClientConfig = adjustConfig(id, newClientConfig);
            reloadClient(id, newClientConfig, wildcardProvider, true);
        });
    }

    /**
     * Closes the old HTTP client and replaces it with the a new HTTP client, if the client exists and the configuration has changed
     *
     * @param clientId The identifier
     * @param config The configuration to check. Can be <code>null</code>
     * @param modifier The modifier to use
     * @param checkHash <code>true</code> to check if and stop processing on equal hash codes of the configuration, <code>false</code> to avoid the check
     * @return <code>true</code> if the client has been reloaded, <code>false</code> otherwise
     */
    synchronized boolean reloadClient(String clientId, HttpBasicConfig config, HttpClientBuilderModifier modifier, boolean checkHash) {
        checkShutdownStatus();
        if (null == config) {
            return false;
        }

        ManagedHttpClientImpl managedHttpClient = httpClients.getIfPresent(clientId);
        if (managedHttpClient == null) {
            LOGGER.error("No HTTP client with id {} found to reload.", clientId);
            return false;
        }
        if (checkHash) {
            if (config.hashCode() == managedHttpClient.getConfigHash()) {
                return false;
            }
            LOGGER.trace("Configuration for client {} has changed.", clientId);
        }

        /*
         * Create new client and replace it in managed object
         */
        boolean close = true;
        CloseableHttpClient newHttpClient = null;
        ClientConnectionManager ccm = null;
        try {
            final UnmodifiableHttpBasicConfig unmodifiableConfig = new UnmodifiableHttpBasicConfig(config);
            ccm = initializeClientConnectionManager(clientId, unmodifiableConfig);
            newHttpClient = initializeHttpClient(clientId, modifier, unmodifiableConfig, ccm);
            int configHash = managedHttpClient.getConfigHash();
            closeWithDelay(clientId, configHash, managedHttpClient.reload(newHttpClient, ccm, unmodifiableConfig.hashCode()));
            LOGGER.trace("Replaced HTTP client for ID {}. Original configuration had the hashCode {}. New configuration has the hash code {}", I(configHash), I(unmodifiableConfig.hashCode()));
            close = false;
            return true;
        } catch (OXException e) {
            LOGGER.error("Unable to reload HTTP client for {}", clientId, e);
            return false;
        } finally {
            if (close) {
                Streams.close(ccm, newHttpClient);
            }
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static class PatternEnhancedWildcardHttpClientConfigProvider implements WildcardHttpClientConfigProvider {

        private final WildcardHttpClientConfigProvider provider;
        private final Pattern pattern;

        PatternEnhancedWildcardHttpClientConfigProvider(WildcardHttpClientConfigProvider provider) {
            super();
            this.provider = provider;
            String regex = Strings.wildcardToRegex(provider.getClientIdPattern());
            this.pattern = Pattern.compile(regex);
        }

        @Override
        public void modify(HttpClientBuilder builder) {
            provider.modify(builder);
        }

        @Override
        public Interests getAdditionalInterests() {
            return provider.getAdditionalInterests();
        }

        @Override
        public @NonNull String getClientIdPattern() {
            return provider.getClientIdPattern();
        }

        @Override
        public HttpBasicConfig configureHttpBasicConfig(String clientId, HttpBasicConfig config) {
            return provider.configureHttpBasicConfig(clientId, config);
        }

        Pattern getRegularExpressionPattern() {
            return pattern;
        }
    }

    /**
     * Creates a new {@link ManagedHttpClient} instance
     *
     * @param httpClientId The http client id
     * @return The new {@link ManagedHttpClient}
     * @throws OXException in case of errors
     */
    public ManagedHttpClientImpl create(String httpClientId) throws OXException {

        /*
         * Load configuration for the client
         */
        final Supplier<HttpBasicConfig> configSupplier;
        final HttpClientConfigProvider provider;
        {
            SpecificHttpClientConfigProvider specificProvider = specificProviders.get(httpClientId);
            if (specificProvider != null) {
                provider = specificProvider;
                configSupplier = () -> adjustConfig(httpClientId, specificProvider.configureHttpBasicConfig(createNewDefaultConfig()));
            } else {
                WildcardHttpClientConfigProvider tmp = getWildcardProvider(httpClientId);
                if (tmp == null) {
                    tmp = leftoverProvider;
                }
                final WildcardHttpClientConfigProvider wildcardProvider = tmp;
                provider = wildcardProvider;
                configSupplier = () -> adjustConfig(httpClientId, wildcardProvider.configureHttpBasicConfig(httpClientId, createNewDefaultConfig()));
            }
        }
        /*
         * Ensure the configuration isn't changed unless the client is reloaded
         */
        final UnmodifiableHttpBasicConfig unmodifiableConfig = new UnmodifiableHttpBasicConfig(configSupplier.get());

        ClientConnectionManager ccm = initializeClientConnectionManager(httpClientId, unmodifiableConfig);
        CloseableHttpClient httpClient = initializeHttpClient(httpClientId, provider, unmodifiableConfig, ccm);
        Supplier<Boolean> reloadCallback = () -> B(reloadClient(httpClientId, configSupplier.get(), provider, false));
        ManagedHttpClientImpl managedHttpClient = new ManagedHttpClientImpl(httpClientId, unmodifiableConfig.hashCode(), httpClient, ccm, reloadCallback);

        LOGGER.trace("Initialized HTTP client for {} and put it into cache", httpClientId);
        return managedHttpClient;
    }


    WildcardHttpClientConfigProvider getWildcardProvider(String clientId) {
        WildcardHttpClientConfigProvider wildcardProvider = wildcardProvidersCache.getUnchecked(clientId);
        return ABSENT == wildcardProvider ? null : wildcardProvider;
    }

    WildcardHttpClientConfigProvider doGetWildcardProvider(String clientId) { // Invoked from CacheLoader
        for (PatternEnhancedWildcardHttpClientConfigProvider provider : wildcardProviders.values()) {
            if (provider.getRegularExpressionPattern().matcher(clientId).matches()) {
                wildcardProvidersCache.put(clientId, provider);
                return provider;
            }
        }
        return null;
    }

}

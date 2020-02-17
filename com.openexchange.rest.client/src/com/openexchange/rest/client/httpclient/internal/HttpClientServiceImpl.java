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

import static com.openexchange.java.Autoboxing.I;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
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
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.annotation.NonNull;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.DefaultInterests;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.net.ssl.SSLSocketFactoryProvider;
import com.openexchange.net.ssl.config.SSLConfigurationService;
import com.openexchange.rest.client.httpclient.HttpBasicConfig;
import com.openexchange.rest.client.httpclient.HttpClientBuilderModifier;
import com.openexchange.rest.client.httpclient.HttpClientProperty;
import com.openexchange.rest.client.httpclient.HttpClientService;
import com.openexchange.rest.client.httpclient.ManagedHttpClient;
import com.openexchange.rest.client.httpclient.SpecificHttpClientConfigProvider;
import com.openexchange.rest.client.httpclient.WildcardHttpClientConfigProvider;
import com.openexchange.rest.client.httpclient.ssl.EasySSLSocketFactory;
import com.openexchange.server.ServiceLookup;
import com.openexchange.timer.TimerService;

/**
 * {@link HttpClientServiceImpl}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.4
 */
public class HttpClientServiceImpl implements HttpClientService, ServiceTrackerCustomizer<Object, Object> {

    static final Logger LOGGER = LoggerFactory.getLogger(HttpClientServiceImpl.class);

    /** A boolean value that indicates whether the service is shutting down or not */
    private final AtomicBoolean isShutdown;

    private final BundleContext context;
    private final ServiceLookup serviceLookup;

    final ConcurrentMap<String, ManagedHttpClientImpl> httpClients = new ConcurrentHashMap<>(30, 0.9f); // Core knows about 15 providers, 1 generic
    final ConcurrentMap<String, SpecificHttpClientConfigProvider> providers = new ConcurrentHashMap<>(20, 0.9f); // Core knows about 15
    final ConcurrentMap<String, PatternEnhancedWildcardHttpClientConfigProvider> wildcardProviders = new ConcurrentHashMap<>(16, 0.9F, 1);
    final Map<String, ServiceRegistration<Reloadable>> reloadableRegistrations = new ConcurrentHashMap<>(30, 0.9f);

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
    }

    @Override
    public ManagedHttpClient getHttpClient(String httpClientId) throws OXException {
        if (Strings.isEmpty(httpClientId)) {
            throw HttpClientExceptionCodes.MISSING_ID.create();
        }
        checkShutdownStatus();

        /*
         * Get HTTP client from cache
         */
        ManagedHttpClientImpl optHttpClient = httpClients.get(httpClientId);
        if (optHttpClient != null) {
            LOGGER.trace("Getting client with ID {} from cache", httpClientId);
            return optHttpClient;
        }

        /*
         * Create HTTP client
         */
        HttpBasicConfigImpl httpBasicConfigImpl = createNewDefaultConfig();

        {
            SpecificHttpClientConfigProvider provider = providers.get(httpClientId);
            if (provider != null) {
                HttpBasicConfig config = adjustConfig(httpClientId, provider.configureHttpBasicConfig(httpBasicConfigImpl));
                return putIntoCache(httpClientId, createHttpClient(httpClientId, provider, config), config, forProvider(httpClientId, provider));
            }
        }

        WildcardHttpClientConfigProvider provider = getWildcardProvider(httpClientId);
        if (null != provider) {
            HttpBasicConfig config = adjustConfig(httpClientId, provider.configureHttpBasicConfig(httpClientId, httpBasicConfigImpl));
            return putIntoCache(httpClientId, createHttpClient(httpClientId, provider, config), config, forProvider(httpClientId, provider));
        }

        throw HttpClientExceptionCodes.MISSING_PROVIDER.create(httpClientId);
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
            if (providers.putIfAbsent(clientId, provider) == null) {
                return provider;
            }

            // Such a provider already contained
            LOGGER.error("Unable to add provider {}. Already have a provider for {}", provider.getClass().getSimpleName(), clientId);
            context.ungetService(reference);
            return null;
        }

        if (service instanceof WildcardHttpClientConfigProvider) {
            WildcardHttpClientConfigProvider provider = (WildcardHttpClientConfigProvider) service;
            if (wildcardProviders.putIfAbsent(provider.getClientIdPattern(), new PatternEnhancedWildcardHttpClientConfigProvider(provider)) == null) {
                LOGGER.trace("Added provider for pattern {}", provider.getClientIdPattern());
                return provider;
            }

            LOGGER.error("Already registered a provider with pattern {}", provider.getClientIdPattern());
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
            if (providers.remove(clientId) != null) {
                close(clientId, httpClients.get(clientId));
                LOGGER.trace("Removed provider for {}", clientId);
            }
        } else if (service instanceof WildcardHttpClientConfigProvider) {
            WildcardHttpClientConfigProvider provider = (WildcardHttpClientConfigProvider) service;
            PatternEnhancedWildcardHttpClientConfigProvider removed = wildcardProviders.remove(provider.getClientIdPattern());
            if (null == removed) {
                LOGGER.warn("Unable to find provider for {} in cache. Won't remove any HTTP client for provided pattern.", provider.getClientIdPattern());
                return;
            }

            /*
             * Remove HTTP clients, which matches the regex and are not provided by a SpecificHttpClientConfigProvider
             */
            Pattern pattern = removed.getRegularExpressionPattern();
            for (Map.Entry<String, ManagedHttpClientImpl> entry : httpClients.entrySet()) {
                if (false == providers.containsKey(entry.getKey()) && pattern.matcher(entry.getKey()).matches()) {
                    close(entry.getKey(), entry.getValue());
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
            for (Map.Entry<String, ManagedHttpClientImpl> entry : httpClients.entrySet()) {
                close(entry.getKey(), entry.getValue());
            }
        }
    }

    /*-
     * -------------------------------------------------------------------------------------------------------------------------------------
     * --------------------------------------------------------- Private helpers -----------------------------------------------------------
     * -------------------------------------------------------------------------------------------------------------------------------------
     */

    /**
     * Creates a HTTP client based on the given configuration.
     *
     * @param clientId The identifier of the client
     * @param modifier The modifier to adjust the HTTP builder
     * @param config The basic HTTP configuration
     * @return A {@link CloseableHttpClient}
     * @throws OXException
     */
    private CloseableHttpClient createHttpClient(String clientId, HttpClientBuilderModifier modifier, HttpBasicConfig config) throws OXException {
        SSLSocketFactoryProvider factoryProvider = serviceLookup.getServiceSafe(SSLSocketFactoryProvider.class);
        SSLConfigurationService sslConfig = serviceLookup.getServiceSafe(SSLConfigurationService.class);

        return initializeHttpClient(clientId, modifier, config, initializeClientConnectionManagerUsing(clientId, config, factoryProvider, sslConfig));
    }

    /**
     * Puts a HTTP client into the cache.
     *
     * @param httpClientId The HTTP client identifier. Will be used as cache key.
     * @param closeableHttpClient The HTTP client
     * @param config The basic configuration.
     * @param reloadable The {@link Reloadable} to register for the HTTP client
     * @return A {@link ManagedHttpClient}
     */
    private ManagedHttpClient putIntoCache(String httpClientId, CloseableHttpClient closeableHttpClient, HttpBasicConfig config, Reloadable reloadable) {
        ManagedHttpClientImpl managedClient = new ManagedHttpClientImpl(httpClientId, config.hashCode(), closeableHttpClient);

        ManagedHttpClientImpl previous = httpClients.putIfAbsent(httpClientId, managedClient);
        if (previous != null) {
            // Another thread alread put client into map
            LOGGER.trace("Closing redundant HTTP client.");
            close(httpClientId, managedClient);
            return previous;
        }

        removeReloadable(reloadableRegistrations.put(httpClientId, context.registerService(Reloadable.class, reloadable, null)));
        LOGGER.trace("Initialized HTTP client for {} and put it into cache", httpClientId);
        return managedClient;
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
            .register("https", null == factoryProvider || null == sslConfig ?
                EasySSLSocketFactory.getInstance()
                : new SSLConnectionSocketFactory(factoryProvider.getDefault(), sslConfig.getSupportedProtocols(), sslConfig.getSupportedCipherSuites(), NoopHostnameVerifier.INSTANCE))
            .build();
        //@formatter:on

        ClientConnectionManager ccm = new ClientConnectionManager(clientId, config.getKeepAliveMonitorInterval(), socketFactoryRegistry);
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
    private CloseableHttpClient initializeHttpClient(String clientId, HttpClientBuilderModifier modifier, HttpBasicConfig config, HttpClientConnectionManager ccm) {
        /*
         * Prepare client builder
         */
        //@formatter:off
        HttpClientBuilder builder = new ExtendedBuilder(clientId)
            .setConnectionManager(ccm)
            .setDefaultRequestConfig(RequestConfig.custom()
                .setConnectTimeout(config.getConnectionTimeout())
                .setSocketTimeout(config.getSocketReadTimeout())
                .setConnectionRequestTimeout(config.getConnectionRequestTimeout())
                .setCookieSpec("lenient")
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
            RegistryBuilder<CookieSpecProvider> regestryBuilder = CookieSpecRegistries.createDefaultBuilder(publicSuffixMatcher);
            regestryBuilder.register(CookieSpecs.DEFAULT, lenientCookieSpecProvider);
            regestryBuilder.register("lenient", lenientCookieSpecProvider);
            builder.setDefaultCookieSpecRegistry(regestryBuilder.build());
        }

        builder.addInterceptorLast(new HttpRequestInterceptor() {

            @Override
            public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
                if (!request.containsHeader("Accept-Encoding")) {
                    request.addHeader("Accept-Encoding", "gzip");
                }
            }
        });
        builder.useSystemProperties();

        /*
         * Finally adjust the client and build it.
         */
        modifier.modify(builder);
        return builder.build();
    }

    HttpBasicConfigImpl createNewDefaultConfig() {
        LeanConfigurationService leanConfigurationService = serviceLookup.getService(LeanConfigurationService.class);
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
     * @param httpBasicConfig The config to adjust
     * @return The adjusted config
     */
    HttpBasicConfig adjustConfig(String clientId, HttpBasicConfig httpBasicConfig) {
        ConfigurationService configService = serviceLookup.getOptionalService(ConfigurationService.class);
        if (null != configService) {
            for (HttpClientProperty property : HttpClientProperty.values()) {
                adjustConfig(configService, clientId, property, httpBasicConfig);
            }
        }
        return httpBasicConfig;
    }

    private void adjustConfig(ConfigurationService configService, String clientId, HttpClientProperty property, HttpBasicConfig httpBasicConfig) {
        Map<String, String> map = Collections.singletonMap(HttpClientProperty.SERVICE_IDENTIFIER, clientId);
        String propertyName = property.getFQPropertyName(map);
        String value = configService.getProperty(propertyName);

        if (Strings.isNotEmpty(value)) {
            try {
                property.setInConfig(httpBasicConfig, Integer.valueOf(value));
            } catch (NumberFormatException e) {
                LOGGER.info("Unable to parse value {} for property {}", value, propertyName, e);
            }
        }
    }

    /**
     * Get a {@link WildcardHttpClientConfigProvider} for the provided client identifier
     *
     * @param clientId The identifier to match to a provider
     * @return A provider or <code>null</code>
     */
    private WildcardHttpClientConfigProvider getWildcardProvider(String clientId) {
        for (PatternEnhancedWildcardHttpClientConfigProvider provider : wildcardProviders.values()) {
            if (provider.getRegularExpressionPattern().matcher(clientId).matches()) {
                return provider;
            }
        }
        return null;
    }

    /**
     * Closes a HTTP client
     *
     * @param clientId The client identifier
     * @param managedClient The client to close
     */
    private void close(String clientId, ManagedHttpClientImpl managedClient) {
        /*
         * The ClientConnectionManager will be closed implicit by the
         * HTTP client itself.
         */
        if (null != managedClient) {
            removeReloadable(reloadableRegistrations.remove(clientId));
            LOGGER.debug("Closing HTTP client for service {}", clientId);
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

    void removeReloadable(ServiceRegistration<Reloadable> reloadable) {
        if (null != reloadable) {
            context.ungetService(reloadable.getReference());
            reloadable.unregister();
        }
    }

    /**
     * Checks whether the service is currently shutting down or not.
     *
     * @throws RuntimeException If the service is shut-down
     */
    private void checkShutdownStatus() throws RuntimeException {
        if (isShutdown.get()) {
            throw new RuntimeException("Service is shutting down.");
        }
    }

    private class CloseableListener implements Closeable {

        private final String clientId;

        public CloseableListener(String clientId) {
            super();
            this.clientId = clientId;
        }

        @Override
        public void close() throws IOException {
            LOGGER.debug("Client has been closed. Remove client for {} from cache.", clientId);
            ManagedHttpClientImpl client = httpClients.remove(clientId);
            if (null != client) {
                removeReloadable(reloadableRegistrations.remove(clientId));
                client.unset();
            }
        }
    }

    private class ExtendedBuilder extends HttpClientBuilder {

        @SuppressWarnings("resource")
        public ExtendedBuilder(String clientId) {
            super();
            /*
             * Add the listener to remove the client when it is closed outside this service
             */
            addCloseable(new CloseableListener(clientId));
        }
    }

    /*
     * -----------------------------------------------------------------
     * -------------------- Reloadable functions -----------------------
     * -----------------------------------------------------------------
     */

    /**
     * Creates a reloadable for the given client ID and the given provider
     *
     * @param httpClientId The HTTP client ID
     * @param provider THe provider to get configuration from. This is needed on reload, to avoid unnecessary closing of HTTP clients
     * @return The {@link Reloadable}
     */
    private Reloadable forProvider(final String httpClientId, final SpecificHttpClientConfigProvider provider) {
        return new Reloadable() {

            @Override
            public void reloadConfiguration(ConfigurationService configService) {
                reloadClient(httpClientId, adjustConfig(httpClientId, provider.configureHttpBasicConfig(createNewDefaultConfig())), provider);
            }

            @Override
            public Interests getInterests() {
                return getAllInterests(provider.getAdditionalInterests(), httpClientId);
            }
        };
    }

    /**
     * Creates a reloadable for the given client ID and the given provider
     *
     * @param httpClientId The HTTP client ID
     * @param provider THe provider to get configuration from. This is needed on reload, to avoid unnecessary closing of HTTP clients
     * @return The {@link Reloadable}
     */
    private Reloadable forProvider(final String httpClientId, final WildcardHttpClientConfigProvider provider) {
        return new Reloadable() {

            @Override
            public void reloadConfiguration(ConfigurationService configService) {
                reloadClient(httpClientId, adjustConfig(httpClientId, provider.configureHttpBasicConfig(httpClientId, createNewDefaultConfig())), provider);
            }

            @Override
            public Interests getInterests() {
                return getAllInterests(provider.getAdditionalInterests(), httpClientId);
            }
        };
    }

    Interests getAllInterests(Interests additional, String clientId) {
        ArrayList<String> interests = new ArrayList<>();
        if (null != additional) {
            for (String interest : additional.getPropertiesOfInterest()) {
                interests.add(interest);
            }
        }
        interests.add(HttpClientProperty.PREFIX + clientId + ".*");
        return DefaultInterests.builder().propertiesOfInterest(interests.toArray(new String[interests.size()])).build();
    }

    /**
     * Closes the old HTTP client and replaces it with the a new HTTP client, if the client exists and the configuration has changed
     *
     * @param clientId The identifier
     * @param config The configuration to check. Can be <code>null</code>
     */
    synchronized void reloadClient(String clientId, HttpBasicConfig config, HttpClientBuilderModifier modifier) {
        checkShutdownStatus();
        if (null == config) {
            return;
        }

        ManagedHttpClientImpl managedHttpClient = httpClients.get(clientId);
        if (managedHttpClient == null) {
            LOGGER.error("No HTTP client to reload found", HttpClientExceptionCodes.MISSING_HTTP_CLIENT.create(clientId));
            return;
        }
        if (config.hashCode() == managedHttpClient.getConfigHash()) {
            return;
        }

        LOGGER.trace("Configuration for client {} has changed.", clientId);
        /*
         * Create new client and replace it in managed object
         */
        boolean close = true;
        CloseableHttpClient newHttpClient = null;
        try {
            newHttpClient = createHttpClient(clientId, modifier, config);
            int configHash = managedHttpClient.getConfigHash();
            closeWithDelay(clientId, configHash, managedHttpClient.reload(newHttpClient, config.hashCode()));
            LOGGER.trace("Replaced HTTP client for ID {}. Original configuration had the hashCode {}. New configuration has the hash code {}", I(configHash), I(config.hashCode()));
            close = false;
        } catch (OXException e) {
            LOGGER.error("Unable to reload HTTP client for {}", clientId, e);
        } finally {
            if (close) {
                Streams.close(newHttpClient);
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

}

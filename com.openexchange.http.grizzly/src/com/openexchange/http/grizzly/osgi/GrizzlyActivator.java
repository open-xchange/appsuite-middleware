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

package com.openexchange.http.grizzly.osgi;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import javax.servlet.Filter;
import org.glassfish.grizzly.comet.CometAddOn;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.server.OXHttpServer;
import org.glassfish.grizzly.http.server.OXSessionManager;
import org.glassfish.grizzly.http.server.ServerConfiguration;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.nio.transport.TCPNIOTransportBuilder;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.grizzly.strategies.SameThreadIOStrategy;
import org.glassfish.grizzly.websockets.WebSocketAddOn;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;
import com.google.common.collect.ImmutableList;
import com.openexchange.config.ConfigurationService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.exception.OXException;
import com.openexchange.http.grizzly.GrizzlyConfig;
import com.openexchange.http.grizzly.GrizzlyExceptionCode;
import com.openexchange.http.grizzly.service.comet.CometContextService;
import com.openexchange.http.grizzly.service.comet.impl.CometContextServiceImpl;
import com.openexchange.http.grizzly.service.http.HttpServiceFactory;
import com.openexchange.http.grizzly.service.websocket.WebApplicationService;
import com.openexchange.http.grizzly.service.websocket.impl.WebApplicationServiceImpl;
import com.openexchange.http.grizzly.servletfilter.RequestReportingFilter;
import com.openexchange.http.grizzly.servletfilter.WrappingFilter;
import com.openexchange.http.grizzly.threadpool.GrizzlOXExecutorService;
import com.openexchange.http.requestwatcher.osgi.services.RequestWatcherService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.startup.SignalStartedService;
import com.openexchange.startup.ThreadControlService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.timer.TimerService;

/**
 * {@link GrizzlyActivator}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class GrizzlyActivator extends HousekeepingActivator {

    private OXSessionManager sessionManager;

    /**
     * Initializes a new {@link GrizzlyActivator}.
     */
    public GrizzlyActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { ConfigurationService.class, RequestWatcherService.class, ThreadPoolService.class, TimerService.class };
    }

    @Override
    protected boolean stopOnServiceUnavailability() {
        return true;
    }

    /**
     * Initialize server side SSL configuration.
     *
     * @return server side {@link SSLEngineConfigurator}.
     */
    private static SSLEngineConfigurator createSslConfiguration(GrizzlyConfig grizzlyConfig) {
        // Initialize SSLContext configuration
        SSLContextConfigurator sslContextConfig = new SSLContextConfigurator();

        // Set key store
        // http://www.sslshopper.com/article-most-common-java-keytool-keystore-commands.html
        sslContextConfig.setKeyStoreFile(grizzlyConfig.getKeystorePath());
        sslContextConfig.setKeyStorePass(grizzlyConfig.getKeystorePassword());
        // Create SSLEngine configurator
        SSLEngineConfigurator sslEngineConfigurator = new SSLEngineConfigurator(sslContextConfig.createSSLContext(), false, false, false);
        List<String> enabledCipherSuites = grizzlyConfig.getEnabledCiphers();
        if (null != enabledCipherSuites && !enabledCipherSuites.isEmpty()) {
            sslEngineConfigurator.setEnabledCipherSuites(enabledCipherSuites.toArray(new String[enabledCipherSuites.size()]));
        }
        return sslEngineConfigurator;
    }

    @Override
    protected synchronized void startBundle() throws OXException {
        final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GrizzlyActivator.class);
        try {
            Services.setServiceLookup(this);
            ConfigurationService configurationService = getService(ConfigurationService.class);
            trackService(DispatcherPrefixService.class);

            log.info("Starting Grizzly server.");

            // Initialize Grizzly configuration
            GrizzlyConfig grizzlyConfig;
            {
                GrizzlyConfig.Builder builder = GrizzlyConfig.builder();
                builder.initializeFrom(configurationService);
                grizzlyConfig = builder.build();
            }

            // Initialize HTTP session manager
            OXSessionManager sessionManager = new OXSessionManager(grizzlyConfig, getService(TimerService.class));
            this.sessionManager = sessionManager;

            // Create, configure and start server
            OXHttpServer grizzly = new OXHttpServer(grizzlyConfig);

            ServerConfiguration serverConfiguration = grizzly.getServerConfiguration();
            serverConfiguration.setMaxRequestParameters(grizzlyConfig.getMaxRequestParameters());
            serverConfiguration.setAllowPayloadForUndefinedHttpMethods(true);

            final NetworkListener networkListener = new NetworkListener("http-listener", grizzlyConfig.getHttpHost(), grizzlyConfig.getHttpPort());

            /*
             * Set the maximum size of the PUT/POST body generated by an HTML form to honor "com.openexchange.servlet.maxBodySize"
             */
            int maxBodySize = grizzlyConfig.getMaxBodySize();
            networkListener.setMaxFormPostSize(maxBodySize);
            networkListener.setMaxBufferedPostSize(maxBodySize);
            networkListener.setMaxHttpHeaderSize(grizzlyConfig.getMaxHttpHeaderSize());
            networkListener.setSessionManager(sessionManager);

            // Set the transport
            {
                TCPNIOTransport configuredTcpNioTransport = buildTcpNioTransport(configurationService);
                networkListener.setTransport(configuredTcpNioTransport);
            }

            // Configure keep-alive
            /*-
             * Keep default settings for now.
             * Otherwise alter KeepAlive instance.
             *
             * {
             *     final KeepAlive keepAlive = networkListener.getKeepAlive();
             *     keepAlive.setIdleTimeoutInSeconds(-1);
             *     keepAlive.setMaxRequestsCount(-1);
             * }
             *
             */

            if (grizzlyConfig.isJMXEnabled()) {
                grizzly.getServerConfiguration().setJmxEnabled(true);
                log.info("Enabled JMX for Grizzly server.");
            }

            if (grizzlyConfig.isWebsocketsEnabled()) {
                networkListener.registerAddOn(new WebSocketAddOn());
                registerService(WebApplicationService.class, new WebApplicationServiceImpl());
                log.info("Enabled WebSockets for Grizzly server.");
            }

            if (grizzlyConfig.isCometEnabled()) {
                networkListener.registerAddOn(new CometAddOn());
                registerService(CometContextService.class, new CometContextServiceImpl());
                log.info("Enabled Comet for Grizzly server.");
            }

            grizzly.addListener(networkListener);
            log.info("Prepared Grizzly HttpNetworkListener on host: {} and port: {}, but not yet started...", grizzlyConfig.getHttpHost(), Integer.valueOf(grizzlyConfig.getHttpPort()));

            if (grizzlyConfig.isSslEnabled()) {
                NetworkListener networkSslListener = new NetworkListener("https-listener", grizzlyConfig.getHttpHost(), grizzlyConfig.getHttpsPort());
                networkSslListener.setMaxFormPostSize(maxBodySize);
                networkSslListener.setMaxBufferedPostSize(maxBodySize);
                networkSslListener.setMaxHttpHeaderSize(grizzlyConfig.getMaxHttpHeaderSize());
                networkSslListener.setSessionManager(sessionManager);
                networkSslListener.setSSLEngineConfig(createSslConfiguration(grizzlyConfig));
                networkSslListener.setSecure(true);
                TCPNIOTransport configuredTcpNioTransportSsl = buildTcpNioTransport(configurationService);
                networkSslListener.setTransport(configuredTcpNioTransportSsl);
                if (grizzlyConfig.isWebsocketsEnabled()) {
                    networkSslListener.registerAddOn(new WebSocketAddOn());
                }
                if (grizzlyConfig.isCometEnabled()) {
                    networkSslListener.registerAddOn(new CometAddOn());
                }
                log.info("Enabled SSL for Grizzly server.");
                grizzly.addListener(networkSslListener);
                log.info("Prepared secure Grizzly HttpNetworkListener on host: {} and port: {}, but not yet started...", grizzlyConfig.getHttpHost(), Integer.valueOf(grizzlyConfig.getHttpsPort()));
            }

            // Start Grizzly server
            grizzly.start();

            // The HttpService factory
            HttpServiceFactory httpServiceFactory;
            {
                // Build default list of filters
                ImmutableList.Builder<Filter> builder = ImmutableList.builder();
                builder.add(new WrappingFilter(grizzlyConfig));
                boolean isFilterEnabled = configurationService.getBoolProperty("com.openexchange.server.requestwatcher.isEnabled", true);
                if (isFilterEnabled) {
                    builder.add(new RequestReportingFilter(getService(RequestWatcherService.class), configurationService));
                }

                // Create the HttpService factory. Each distinct bundle will get its own instance of HttpServiceImpl.
                httpServiceFactory = new HttpServiceFactory(grizzly, builder.build(), context.getBundle());
            }

            // Initialize the filter tracker
            {
                ServiceTracker<Filter, Filter> tracker = new ServiceTracker<Filter, Filter>(context, Filter.class, new ServletFilterTracker(httpServiceFactory.getMainHttpHandler(), context));
                rememberTracker(tracker);
            }

            if (grizzlyConfig.isShutdownFast()) {
                registerService(HttpService.class.getName(), httpServiceFactory);
                log.info("Registered OSGi HttpService for Grizzly server.");
            }

            // Track the thread control
            track(ThreadControlService.class, new ThreadControlTracker(context));

            // Finally start listeners if server start-up is completed
            track(SignalStartedService.class, new StartUpTracker(httpServiceFactory, grizzly, grizzlyConfig, context));
            openTrackers();
        } catch (Exception e) {
            throw GrizzlyExceptionCode.GRIZZLY_SERVER_NOT_STARTED.create(e, new Object[] {});
        }
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        org.slf4j.LoggerFactory.getLogger(GrizzlyActivator.class).info("Unregistering services.");
        super.stopBundle();

        OXSessionManager sessionManager = this.sessionManager;
        if (null != sessionManager) {
            this.sessionManager = null;
            sessionManager.destroy();
        }

        Services.setServiceLookup(null);
    }

    /**
     * Builds a TCPNIOTransport using {c.o}.threadpool
     *
     * @param configurationService The configuration service to use to read settings for TCP NIO connections
     * @return The configured <code>TCPNIOTransport</code> instance
     * @throws OXException If the transport cannot be build
     */
    private TCPNIOTransport buildTcpNioTransport(ConfigurationService configurationService) throws OXException {
        if (getService(ThreadPoolService.class) == null) {
            throw GrizzlyExceptionCode.NEEDED_SERVICE_MISSING.create(ThreadPoolService.class.getSimpleName());
        }

        // Determine settings for TCP NIO connections
        boolean keepAlive = configurationService.getBoolProperty("com.openexchange.http.grizzly.keepAlive", true);
        boolean tcpNoDelay = configurationService.getBoolProperty("com.openexchange.http.grizzly.tcpNoDelay", true);
        int readTimeoutMillis = configurationService.getIntProperty("com.openexchange.http.grizzly.readTimeoutMillis", org.glassfish.grizzly.Transport.DEFAULT_READ_TIMEOUT * 1000);
        int writeTimeoutMillis = configurationService.getIntProperty("com.openexchange.http.grizzly.writeTimeoutMillis", org.glassfish.grizzly.Transport.DEFAULT_WRITE_TIMEOUT * 1000);

        // Build up the TCPNIOTransport to use
        TCPNIOTransportBuilder builder = TCPNIOTransportBuilder.newInstance();
        TCPNIOTransport transport = builder
                                    .setIOStrategy(SameThreadIOStrategy.getInstance())
                                    .setMemoryManager(builder.getMemoryManager())

                                    .setKeepAlive(keepAlive)
                                    .setTcpNoDelay(tcpNoDelay)
                                    .setClientSocketSoTimeout(readTimeoutMillis)

                                    .setReadTimeout(readTimeoutMillis, TimeUnit.MILLISECONDS)
                                    .setWriteTimeout(writeTimeoutMillis, TimeUnit.MILLISECONDS)

                                    .build();

        // Apply ExecutorService backed by {c.o}.threadpool
        ExecutorService executor = GrizzlOXExecutorService.createInstance();
        transport.setWorkerThreadPool(executor);
        return transport;
    }

}

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
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionRequest;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import com.openexchange.rest.client.httpclient.ssl.EasySSLSocketFactory;
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
     * Creates a {@link DefaultHttpClient} instance.
     *
     * @param userAgent The optional user agent identifier
     * @return A newly created {@link DefaultHttpClient} instance
     */
    public static DefaultHttpClient getHttpClient(String userAgent) {
        return getHttpClient(new ClientConfig().setUserAgent(userAgent));
    }

    /**
     * Creates a {@link DefaultHttpClient} instance.
     *
     * @param config The configuration settings for the client
     * @return A newly created {@link DefaultHttpClient} instance
     */
    public static DefaultHttpClient getHttpClient(final ClientConfig config) {
        final SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
        schemeRegistry.register(new Scheme("https", 443, EasySSLSocketFactory.getInstance()));

        ClientConnectionManager ccm = new ClientConnectionManager(schemeRegistry, config.maxConnectionsPerRoute, config.maxTotalConnections, config.keepAliveMonitorInterval);
        ccm.setIdleConnectionCloser(new IdleConnectionCloser(ccm, config.keepAliveDuration));

        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, config.connectionTimeout);
        HttpConnectionParams.setSoTimeout(httpParams, config.socketReadTimeout);
        HttpConnectionParams.setSocketBufferSize(httpParams, config.socketBufferSize);
        if (null != config.userAgent) {
            HttpProtocolParams.setUserAgent(httpParams, config.userAgent);
        }

        final DefaultHttpClient c = new DefaultHttpClient(ccm, httpParams) {

            @Override
            protected ConnectionKeepAliveStrategy createConnectionKeepAliveStrategy() {
                return new KeepAliveStrategy(config.keepAliveDuration);
            }

            @Override
            protected ConnectionReuseStrategy createConnectionReuseStrategy() {
                return new DefaultConnectionReuseStrategy();
            }
        };

        c.addResponseInterceptor(new HttpResponseInterceptor() {

            @Override
            public void process(final HttpResponse response, final HttpContext context) throws HttpException, IOException {
                final HttpEntity entity = response.getEntity();
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

        return c;
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

        private int socketReadTimeout = DEFAULT_TIMEOUT_MILLIS;

        private int connectionTimeout = DEFAULT_TIMEOUT_MILLIS;

        private int maxTotalConnections = MAX_TOTAL_CONNECTIONS;

        private int maxConnectionsPerRoute = MAX_CONNECTIONS_PER_ROUTE;

        private int keepAliveDuration = KEEP_ALIVE_DURATION_SECS;

        private int keepAliveMonitorInterval = KEEP_ALIVE_MONITOR_INTERVAL_SECS;

        private int socketBufferSize = DEFAULT_SOCKET_BUFFER_SIZE;

        private String userAgent;

        ClientConfig() {
            super();
        }

        /**
         * Creates a new {@link ClientConfig} instance.
         */
        public static ClientConfig newInstance() {
            return new ClientConfig();
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

    }

    /**
     * Applies the default timeout of 30sec to given HTTP request.
     *
     * @param request The HTTP request
     */
    public static void setDefaultRequestTimeout(HttpUriRequest request) {
        if (null == request) {
            return;
        }
        final HttpParams reqParams = request.getParams();
        HttpConnectionParams.setSoTimeout(reqParams, DEFAULT_TIMEOUT_MILLIS);
        HttpConnectionParams.setConnectionTimeout(reqParams, DEFAULT_TIMEOUT_MILLIS);
    }

    /**
     * Applies the specified timeout to given HTTP request.
     *
     * @param timeoutMillis The timeout in milliseconds to apply
     * @param request The HTTP request
     */
    public static void setRequestTimeout(int timeoutMillis, HttpUriRequest request) {
        if (null == request || timeoutMillis <= 0) {
            return;
        }
        final HttpParams reqParams = request.getParams();
        HttpConnectionParams.setSoTimeout(reqParams, timeoutMillis);
        HttpConnectionParams.setConnectionTimeout(reqParams, timeoutMillis);
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
        }
    }

    /*------------------------------------------------------ CLASSES ------------------------------------------------------*/

    private static class ClientConnectionManager extends PoolingClientConnectionManager {

        private volatile IdleConnectionCloser idleConnectionCloser;
        private final int keepAliveMonitorInterval;

        ClientConnectionManager(SchemeRegistry registry, int maxPerRoute, int maxTotal, int keepAliveMonitorInterval) {
            super(registry);
            setMaxTotal(maxTotal);
            setDefaultMaxPerRoute(maxPerRoute);
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
        public ClientConnectionRequest requestConnection(HttpRoute route, Object state) {
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
                        tmp = ServerServiceRegistry.getInstance().getService(TimerService.class).scheduleWithFixedDelay(this, checkIntervalSeconds, checkIntervalSeconds, TimeUnit.SECONDS);
                        timerTask = tmp;
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
                        ServerServiceRegistry.getInstance().getService(TimerService.class).purge();
                        timerTask = null;
                    }
                }
            }
        }

        @Override
        public void run() {
            try {
                manager.closeExpiredConnections();
                manager.closeIdleConnections(idleTimeoutSeconds, TimeUnit.SECONDS);
                if (manager.getTotalStats().getLeased() == 0) {
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
            long timeout = keepAliveSeconds * 1000;

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

}

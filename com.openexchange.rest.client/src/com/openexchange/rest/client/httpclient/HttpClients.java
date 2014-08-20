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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

    /**
     * Creates a {@link DefaultHttpClient} instance.
     *
     * @param userAgent The optional user agent identifier
     * @return A newly created {@link DefaultHttpClient} instance
     */
    public static DefaultHttpClient getHttpClient(String userAgent) {
        final SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
        schemeRegistry.register(new Scheme("https", 443, EasySSLSocketFactory.getInstance()));

        final ClientConnectionManager ccm = new ClientConnectionManager(schemeRegistry, MAX_CONNECTIONS_PER_ROUTE, MAX_TOTAL_CONNECTIONS);

        final HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, DEFAULT_TIMEOUT_MILLIS);
        HttpConnectionParams.setSoTimeout(httpParams, DEFAULT_TIMEOUT_MILLIS);
        HttpConnectionParams.setSocketBufferSize(httpParams, 8192);
        if (null != userAgent) {
            HttpProtocolParams.setUserAgent(httpParams, userAgent);
        }

        final DefaultHttpClient c = new DefaultHttpClient(ccm, httpParams) {

            @Override
            protected ConnectionKeepAliveStrategy createConnectionKeepAliveStrategy() {
                return new KeepAliveStrategy();
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
     * Applies the timeout to given HTTP request.
     *
     * @param request The HTTP request
     */
    public static void setRequestTimeout(HttpUriRequest request) {
        if (null == request) {
            return;
        }
        final HttpParams reqParams = request.getParams();
        HttpConnectionParams.setSoTimeout(reqParams, DEFAULT_TIMEOUT_MILLIS);
        HttpConnectionParams.setConnectionTimeout(reqParams, DEFAULT_TIMEOUT_MILLIS);
    }

    /*------------------------------------------------------ CLASSES ------------------------------------------------------*/

    private static class ClientConnectionManager extends PoolingClientConnectionManager {

        public ClientConnectionManager(final SchemeRegistry registry, final int maxPerRoute, final int maxTotal) {
            super(registry);
            setMaxTotal(maxTotal);
            setDefaultMaxPerRoute(maxPerRoute);
        }

        @Override
        public ClientConnectionRequest requestConnection(final HttpRoute route, final Object state) {
            IdleConnectionCloser.ensureRunning(this, KEEP_ALIVE_DURATION_SECS, KEEP_ALIVE_MONITOR_INTERVAL_SECS);
            return super.requestConnection(route, state);
        }
    }

    private static class IdleConnectionCloser implements Runnable {

        private final ClientConnectionManager manager;

        private final int idleTimeoutSeconds;

        private volatile static ScheduledTimerTask timerTask;

        public IdleConnectionCloser(final ClientConnectionManager manager, final int idleTimeoutSeconds) {
            super();
            this.manager = manager;
            this.idleTimeoutSeconds = idleTimeoutSeconds;
        }

        public static void ensureRunning(final ClientConnectionManager manager, final int idleTimeoutSeconds, final int checkIntervalSeconds) {
            ScheduledTimerTask tmp = timerTask;
            if (null == tmp) {
                synchronized (IdleConnectionCloser.class) {
                    tmp = timerTask;
                    if (null == tmp) {
                        final IdleConnectionCloser task = new IdleConnectionCloser(manager, idleTimeoutSeconds);
                        tmp = ServerServiceRegistry.getInstance().getService(TimerService.class).scheduleWithFixedDelay(task, checkIntervalSeconds, checkIntervalSeconds, TimeUnit.SECONDS);
                        timerTask = tmp;
                    }
                }
            }
        }

        private static void stop() {
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

        KeepAliveStrategy() {
            super();
        }

        @Override
        public long getKeepAliveDuration(final HttpResponse response, final HttpContext context) {
            // Keep-alive for the shorter of 20 seconds or what the server specifies.
            long timeout = KEEP_ALIVE_DURATION_SECS * 1000;

            final HeaderElementIterator i = new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));
            while (i.hasNext()) {
                final HeaderElement element = i.nextElement();
                final String name = element.getName();
                final String value = element.getValue();
                if (value != null && name.equalsIgnoreCase("timeout")) {
                    try {
                        timeout = Math.min(timeout, Long.parseLong(value) * 1000);
                    } catch (final NumberFormatException e) {
                        // Ignore
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

        public GzipDecompressingEntity(final HttpEntity entity) {
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

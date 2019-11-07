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

import java.io.IOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.http.HttpClientConnection;
import org.apache.http.config.Registry;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.conn.ConnectionRequest;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.pool.PoolStats;
import org.apache.http.protocol.HttpContext;
import com.openexchange.metrics.MetricService;
import com.openexchange.metrics.MetricType;
import com.openexchange.metrics.noop.NoopCounter;
import com.openexchange.metrics.types.Counter;
import com.openexchange.rest.client.osgi.RestClientServices;

public class ClientConnectionManager extends PoolingHttpClientConnectionManager {

    private final MonitoringId monitoringId;
    private final int keepAliveMonitorInterval;
    private final AtomicBoolean shuttingDown;
    private final AtomicBoolean metricsInitialized;
    private volatile IdleConnectionCloser idleConnectionCloser;

    public ClientConnectionManager(MonitoringId monitoringId, Registry<ConnectionSocketFactory> socketFactoryRegistry, int keepAliveMonitorInterval) {
        super(socketFactoryRegistry);
        this.monitoringId = monitoringId;
        this.keepAliveMonitorInterval = keepAliveMonitorInterval;
        shuttingDown = new AtomicBoolean(false);
        metricsInitialized = new AtomicBoolean(false);
    }

    /**
     * Sets the associated {@link IdleConnectionCloser} instance
     *
     * @param idleConnectionCloser The instance to set
     */
    public void setIdleConnectionCloser(IdleConnectionCloser idleConnectionCloser) {
        this.idleConnectionCloser = idleConnectionCloser;
    }

    @Override
    public ConnectionRequest requestConnection(HttpRoute route, Object state) {
        IdleConnectionCloser idleConnectionClose = this.idleConnectionCloser;
        if (null != idleConnectionClose) {
            idleConnectionClose.ensureRunning(keepAliveMonitorInterval);
        }
        if (shuttingDown.get()) {
            /*
             * In case the connection pool is shutting down return a ConnectionReuest which always throws a ExecutionException for the get method.
             * This is required to prevent an IllegalStateException.
             */
            return new ConnectionRequest() {

                @Override
                public boolean cancel() {
                    return true;
                }

                @Override
                public HttpClientConnection get(final long timeout, final TimeUnit tunit) throws ExecutionException {
                    throw new ExecutionException("Connection pool is shutting down", null);
                }

            };
        }

        if (!metricsInitialized.get()) {
            MetricService metrics = RestClientServices.getOptionalService(MetricService.class);
            if (metrics != null && metricsInitialized.compareAndSet(false, true)) {
                initPoolMetrics(metrics);
            }
        }

        final ConnectionRequest connectionRequest = super.requestConnection(route, state);
        return new ConnectionRequest() {

            @Override
            public boolean cancel() {
                return connectionRequest.cancel();
            }

            @Override
            public HttpClientConnection get(long timeout, TimeUnit timeUnit) throws InterruptedException, ExecutionException, ConnectionPoolTimeoutException {
                try {
                    return connectionRequest.get(timeout, timeUnit);
                } catch (ConnectionPoolTimeoutException e) {
                    getErrorCounter("POOL_TIMEOUT").incement();
                    throw e;
                } catch (InterruptedException e) {
                    getErrorCounter("INTERRUPTED").incement();
                    throw e;
                }
            }
        };
    }

    @Override
    public void connect(HttpClientConnection managedConn, HttpRoute route, int connectTimeout, HttpContext context) throws IOException {
        try {
            super.connect(managedConn, route, connectTimeout, context);
        } catch (ConnectTimeoutException e) {
            getErrorCounter("TIMEOUT").incement();
            throw e;
        } catch (ConnectException | NoRouteToHostException e) {
            getErrorCounter("REFUSED").incement();
            throw e;
        }
    }

    @Override
    public void shutdown() {
        try {
            IdleConnectionCloser idleConnectionClose = this.idleConnectionCloser;
            if (null != idleConnectionClose) {
                idleConnectionClose.stop();
                this.idleConnectionCloser = null;
            }
            shuttingDown.set(true);
            super.shutdown();
        } finally {
            MonitoringRegistry.getInstance().unregisterInstance(monitoringId);
        }
    }

    private void initPoolMetrics(MetricService metrics) {
        if (MonitoringRegistry.getInstance().hasInstance(monitoringId)) {
            // need to unregister gauges first and then re-register
        }
        metrics.getGauge(monitoringId.newMetricBuilder("httpclient", "Pool.Max", MetricType.GAUGE)
            .withDescription("The configured maximum number of allowed persistent connections for all routes.")
            .withMetricSupplier(() -> getTotalStats().getMax())
            .build());

        metrics.getGauge(monitoringId.newMetricBuilder("httpclient", "Pool.Route.Max", MetricType.GAUGE)
            .withDescription("The configured maximum number of allowed persistent connections per route.")
            .withMetricSupplier(() -> getDefaultMaxPerRoute())
            .build());

        metrics.getGauge(monitoringId.newMetricBuilder("httpclient", "Pool.Available", MetricType.GAUGE)
            .withDescription("The number of available persistent connections for all routes.")
            .withMetricSupplier(() -> getTotalStats().getAvailable())
            .build());

        metrics.getGauge(monitoringId.newMetricBuilder("httpclient", "Pool.Leased", MetricType.GAUGE)
            .withDescription("The number of leased persistent connections for all routes.")
            .withMetricSupplier(() -> getTotalStats().getLeased())
            .build());

        metrics.getGauge(monitoringId.newMetricBuilder("httpclient", "Pool.Pending", MetricType.GAUGE)
            .withDescription("The number of pending threads waiting for a connection.")
            .withMetricSupplier(() -> getTotalStats().getPending())
            .build());

        metrics.getGauge(monitoringId.newMetricBuilder("httpclient", "Pool.Total", MetricType.GAUGE)
            .withDescription("The total number of pooled connections for all routes.")
            .withMetricSupplier(() -> {
                PoolStats stats = getTotalStats();
                return stats.getLeased() + stats.getAvailable();
            })
            .build());
    }

    private Counter getErrorCounter(String reason) {
        MetricService metrics = RestClientServices.getOptionalService(MetricService.class);
        if (metrics == null) {
            return NoopCounter.getInstance();
        }

        return metrics.getCounter(monitoringId.newMetricBuilder("httpclient", "ConnectErrors", MetricType.COUNTER)
            .withDescription("Number of errors while establishing connections")
            .addDimension("reason", reason)
            .build());
    }


}
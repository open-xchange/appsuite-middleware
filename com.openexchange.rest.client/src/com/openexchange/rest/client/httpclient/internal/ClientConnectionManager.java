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

package com.openexchange.rest.client.httpclient.internal;

import static com.openexchange.rest.client.httpclient.internal.HttpClientMetrics.getInterruptedCounter;
import static com.openexchange.rest.client.httpclient.internal.HttpClientMetrics.getPoolTimeoutCounter;
import static com.openexchange.rest.client.httpclient.internal.HttpClientMetrics.getRefusedCounter;
import static com.openexchange.rest.client.httpclient.internal.HttpClientMetrics.getConnectTimeoutCounter;
import static com.openexchange.rest.client.httpclient.internal.HttpClientMetrics.initPoolMetrics;
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
import org.apache.http.protocol.HttpContext;

/**
 *
 * {@link ClientConnectionManager} - Wraps the {@link PoolingHttpClientConnectionManager} and adds monitoring functionality to it
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.3
 */
public class ClientConnectionManager extends PoolingHttpClientConnectionManager {

    protected final String clientName;

    private final int keepAliveMonitorInterval;
    private final AtomicBoolean shuttingDown;
    private final AtomicBoolean metricsInitialized;
    private final int defaultConnectTimeout;
    private final int defaultConnectionRequestTimeout;
    private volatile IdleConnectionCloser idleConnectionCloser;

    /**
     * Initializes a new {@link ClientConnectionManager}.
     *
     * @param clientName A unique client name
     * @param defaultConnectTimeout The default connect timeout to apply or <code>0</code> (zero) to have no connect timeout
     * @param defaultConnectionRequestTimeout
     * @param keepAliveMonitorInterval The amount of time to periodically run the {@link IdleConnectionCloser}
     * @param socketFactoryRegistry For super class
     */
    public ClientConnectionManager(String clientName, int defaultConnectTimeout, int defaultConnectionRequestTimeout, int keepAliveMonitorInterval, Registry<ConnectionSocketFactory> socketFactoryRegistry) {
        super(socketFactoryRegistry);
        this.clientName = clientName;
        this.keepAliveMonitorInterval = keepAliveMonitorInterval;
        this.defaultConnectTimeout = defaultConnectTimeout;
        this.defaultConnectionRequestTimeout = defaultConnectionRequestTimeout;
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

    /**
     * Gets a value whether this connection manager has been shut down or not.
     *
     * @return <code>true</code> if the manager is shutting down, <code>false</code> if not
     */
    public boolean isShutdown() {
        return shuttingDown.get();
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

        if (metricsInitialized.compareAndSet(false, true)) {
            initPoolMetrics(clientName, this);
        }

        final ConnectionRequest connectionRequest = super.requestConnection(route, state);
        final int defaultConnectionRequestTimeout = this.defaultConnectionRequestTimeout;
        return new ConnectionRequest() {

            @Override
            public boolean cancel() {
                return connectionRequest.cancel();
            }

            @Override
            public HttpClientConnection get(long timeout, TimeUnit timeUnit) throws InterruptedException, ExecutionException, ConnectionPoolTimeoutException {
                try {
                    return connectionRequest.get(timeout > 0 ? timeout : defaultConnectionRequestTimeout, timeUnit);
                } catch (ConnectionPoolTimeoutException e) {
                    getPoolTimeoutCounter(clientName).increment();
                    throw e;
                } catch (InterruptedException e) {
                    getInterruptedCounter(clientName).increment();
                    throw e;
                }
            }
        };
    }

    @Override
    public void connect(HttpClientConnection managedConn, HttpRoute route, int connectTimeout, HttpContext context) throws IOException {
        try {
            super.connect(managedConn, route, connectTimeout > 0 ? connectTimeout : defaultConnectTimeout, context);
        } catch (ConnectTimeoutException e) {
            getConnectTimeoutCounter(clientName).increment();
            throw e;
        } catch (ConnectException | NoRouteToHostException e) {
            getRefusedCounter(clientName).increment();
            throw e;
        }
    }

    @Override
    public void shutdown() {
        IdleConnectionCloser idleConnectionClose = this.idleConnectionCloser;
        if (null != idleConnectionClose) {
            idleConnectionClose.stop();
            this.idleConnectionCloser = null;
        }
        shuttingDown.set(true);
        super.shutdown();
    }

}

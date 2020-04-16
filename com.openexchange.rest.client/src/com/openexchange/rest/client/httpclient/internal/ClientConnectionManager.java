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
    private volatile IdleConnectionCloser idleConnectionCloser;

    /**
     * Initializes a new {@link ClientConnectionManager}.
     * 
     * @param clientName A unique client name
     * @param keepAliveMonitorInterval The amount of time to periodically run the {@link IdleConnectionCloser}
     * @param socketFactoryRegistry For super class
     */
    public ClientConnectionManager(String clientName, int keepAliveMonitorInterval, Registry<ConnectionSocketFactory> socketFactoryRegistry) {
        super(socketFactoryRegistry);
        this.clientName = clientName;
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
            super.connect(managedConn, route, connectTimeout, context);
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

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
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;

/**
 * {@link HttpClientMetrics} - Utilities to capture metrics for HTTP clients
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.4
 */
class HttpClientMetrics {

    /**
     * The HttpClientMetrics.java.
     */
    private static final String REQUESTS = "requests";
    private final static String CLIENT_KEY = "client";
    private final static String PREFIX = "appsuite.httpclient.";
    private static final String CONNECTION_ERRORS = "connectionerrors.total";
    private static final String CONNECTIONS = "connections.";

    private HttpClientMetrics() {}

    /**
     * Gets the counter which counts {@link ConnectionPoolTimeoutException} errors for
     * the HTTP client identified by the clientName
     *
     * @param clientName The unique HTTP client identifier
     * @return The counter
     */
    public static Counter getPoolTimeoutCounter(String clientName) {
        return getCounter(clientName, "pooltimeout");
    }

    /**
     * Gets the counter which counts {@link InterruptedException} errors for
     * the HTTP client identified by the clientName
     *
     * @param clientName The unique HTTP client identifier
     * @return The counter
     */
    public static Counter getInterruptedCounter(String clientName) {
        return getCounter(clientName, "interrupted");
    }

    /**
     * Gets the counter which counts {@link ConnectTimeoutException} errors for
     * the HTTP client identified by the clientName
     *
     * @param clientName The unique HTTP client identifier
     * @return The counter
     */
    public static Counter getConnectTimeoutCounter(String clientName) {
        return getCounter(clientName, "timeout");
    }

    /**
     * Gets the counter which counts {@link ConnectException} and {@link NoRouteToHostException}
     * errors for the HTTP client identified by the clientName
     *
     * @param clientName The unique HTTP client identifier
     * @return The counter
     */
    public static Counter getRefusedCounter(String clientName) {
        return getCounter(clientName, "refused");
    }

    private static Counter getCounter(String clientName, String reason) {
        // @formatter:off
        return Counter
            .builder(PREFIX + CONNECTION_ERRORS)
            .tag(CLIENT_KEY, clientName)
            .tag("reason", reason)
            .description("Number of errors while establishing connections")
            .register(Metrics.globalRegistry);
        // @formatter:on
    }

    /**
     * Initializes metrics for the connection pool.
     * Metrics are available for each client identified by its unique name
     *
     * @param clientName The unique HTTP client identifier
     * @param pool The {@link PoolingHttpClientConnectionManager}
     */
    public static void initPoolMetrics(String clientName, PoolingHttpClientConnectionManager pool) {
        // @formatter:off
        Gauge.builder(PREFIX + CONNECTIONS + "max", () -> I(pool.getTotalStats().getMax()) )
            .tag(CLIENT_KEY, clientName)
            .description("The configured maximum number of allowed persistent connections for all routes.")
            .register(Metrics.globalRegistry);

        Gauge.builder(PREFIX + CONNECTIONS + "route.max", () -> I(pool.getDefaultMaxPerRoute()))
            .tag(CLIENT_KEY, clientName)
            .description("The configured maximum number of allowed persistent connections per route.")
            .register(Metrics.globalRegistry);

        Gauge.builder(PREFIX + CONNECTIONS + "available", () -> I(pool.getTotalStats().getAvailable()))
            .tag(CLIENT_KEY, clientName)
            .description("The number of available persistent connections for all routes.")
            .register(Metrics.globalRegistry);

        Gauge.builder(PREFIX + CONNECTIONS + "leased", () -> I(pool.getTotalStats().getLeased()))
            .tag(CLIENT_KEY, clientName)
            .description("The number of leased persistent connections for all routes.")
            .register(Metrics.globalRegistry);

        Gauge.builder(PREFIX + CONNECTIONS + "pending", () -> I(pool.getTotalStats().getPending()))
            .tag(CLIENT_KEY, clientName)
            .description("The number of pending threads waiting for a connection.")
            .register(Metrics.globalRegistry);

        Gauge.builder(PREFIX + CONNECTIONS + "total", () -> I(pool.getTotalStats().getLeased() + pool.getTotalStats().getAvailable()))
            .tag(CLIENT_KEY, clientName)
            .description("The total number of pooled connections for all routes.")
            .register(Metrics.globalRegistry);
        // @formatter:on
    }

    /**
     * Get a {@link Timer} for a HTTP client, a dedicated method and a specific result.
     *
     * @param clientName The HTTP client identifier
     * @param method The HTTP method
     * @param status The response status
     * @return A {@link Timer} for the combination of all tree arguments
     */
    public static Timer getRequestTimer(String clientName, String method, String status) {
        // @formatter:off
        return Timer.builder(PREFIX + REQUESTS)
            .description("Duration of executed HTTP requests")
            .tag(CLIENT_KEY, clientName)
            .tag("method", method)
            .tag("status", status)
            .register(Metrics.globalRegistry);
        // @formatter:on
    }
}

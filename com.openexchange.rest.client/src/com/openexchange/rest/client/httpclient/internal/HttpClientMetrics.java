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

import static com.openexchange.java.Autoboxing.I;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.util.function.Supplier;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Meter.Id;
import io.micrometer.core.instrument.Meter.Type;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tags;
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
        register(PREFIX + CONNECTIONS + "max",
            () -> I(pool.getTotalStats().getMax()),
            clientName,
            "The configured maximum number of allowed persistent connections for all routes.");

        register(PREFIX + CONNECTIONS + "route.max",
            () -> I(pool.getDefaultMaxPerRoute()),
            clientName,
            "The configured maximum number of allowed persistent connections per route.");

        register(PREFIX + CONNECTIONS + "available",
            () -> I(pool.getTotalStats().getAvailable()),
            clientName,
            "The number of available persistent connections for all routes.");

        register(PREFIX + CONNECTIONS + "leased",
            () -> I(pool.getTotalStats().getLeased()),
            clientName,
            "The number of leased persistent connections for all routes.");

        register(PREFIX + CONNECTIONS + "pending",
            () -> I(pool.getTotalStats().getPending()),
            clientName,
            "The number of pending threads waiting for a connection.");

        register(PREFIX + CONNECTIONS + "total",
            () -> I(pool.getTotalStats().getLeased() + pool.getTotalStats().getAvailable()),
            clientName,
            "The total number of pooled connections for all routes.");
        // @formatter:on
    }

    /**
     * Ensures the registration of a specific {@link Gauge}. This includes the removal of an already created gauge.
     *
     * @param gaugeName The name of the {@link Gauge}
     * @param f The function to supply the value for the gauge
     * @param clientName The name of the client
     * @param description The description for the gauge
     */
    private static void register(String gaugeName, Supplier<Number> f, String clientName, String description) {
        /*
         * Remove data that may reference to an old client
         */
        Tags tags = Tags.of(CLIENT_KEY, clientName);
        Id id = new Meter.Id(gaugeName, tags, null, description, Type.GAUGE);
        Metrics.globalRegistry.remove(id);

        /*
         * Register new gauge
         */
        // @formatter:off
        Gauge.builder(gaugeName, f)
            .tags(tags)
            .description(description)
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

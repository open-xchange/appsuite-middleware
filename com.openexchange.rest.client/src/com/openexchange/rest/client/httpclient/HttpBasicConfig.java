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

package com.openexchange.rest.client.httpclient;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * {@link HttpBasicConfig} - Represents the basic configuration for an HTTP client.
 * <p>
 * Contains only values an administrator can modify.
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.4
 */
@NotThreadSafe
public interface HttpBasicConfig {

    /**
     * Gets the socket timeout ({@code SO_TIMEOUT}) in milliseconds, which is the timeout for waiting for data or, put differently,
     * a maximum period inactivity between two consecutive data packets).
     *
     * @return The read timeout
     */
    int getSocketReadTimeout();

    /**
     * Gets the timeout in milliseconds until a connection is established.
     *
     * @return The connect timeout
     */
    int getConnectTimeout();

    /**
     * Gets the timeout in milliseconds used when requesting a connection from the connection manager.
     *
     * @return The connection request timeout
     */
    int getConnectionRequestTimeout();

    /**
     * Gets the max. number of total connections being concurrently managed in connection manager.
     *
     * @return The max. number of total connections
     */
    int getMaxTotalConnections();

    /**
     * Gets the max. number of connections per route being concurrently managed in connection manager.
     *
     * @return The max. number of connections per route
     */
    int getMaxConnectionsPerRoute();

    /**
     * Gets the keep-alive duration in seconds.
     *
     * @return The keep-alive duration in seconds
     */
    int getKeepAliveDuration();

    /**
     * Gets the keep-alive monitor interval in seconds.
     *
     * @return The keep-alive monitor interval
     */
    int getKeepAliveMonitorInterval();

    /**
     * Gets the socket buffer size in bytes.
     *
     * @return The socket buffer size
     */
    int getSocketBufferSize();

    /**
     * Sets the socket read timeout in milliseconds. A timeout value of zero
     * is interpreted as an infinite timeout.
     * Default: {@link HttpClientProperty#SOCKET_READ_TIMEOUT_MILLIS}
     *
     * @param socketReadTimeout The timeout
     * @return This instance for chaining
     */
    HttpBasicConfig setSocketReadTimeout(int socketReadTimeout);

    /**
     * Sets the connect timeout in milliseconds. A timeout value of zero
     * is interpreted as an infinite timeout.
     * Default: {@link HttpClientProperty#CONNECT_TIMEOUT_MILLIS}
     *
     * @param connectTimeout The timeout
     * @return This instance for chaining
     */
    HttpBasicConfig setConnectTimeout(int connectTimeout);

    /**
     * Sets the connection request timeout in milliseconds defining the maximum time to wait for a connection from the pool. A timeout
     * value of zero is interpreted as an infinite timeout.
     * <p/>
     * Default: {@link HttpClientProperty#CONNECTION_REQUEST_TIMEOUT_MILLIS}
     *
     * @param connectionRequestTimeout The timeout in milliseconds
     * @return This instance for chaining
     */
    HttpBasicConfig setConnectionRequestTimeout(int connectionRequestTimeout);

    /**
     * Sets the max. number of concurrent connections that can be opened by the
     * client instance.
     * Default: {@link HttpClientProperty#MAX_TOTAL_CONNECTIONS}
     *
     * @param maxTotalConnections The number of connections
     * @return This instance for chaining
     */
    HttpBasicConfig setMaxTotalConnections(int maxTotalConnections);

    /**
     * Sets the max. number of concurrent connections that can be opened by the
     * client instance per route.
     * Default: {@link HttpClientProperty#MAX_CONNECTIONS_PER_ROUTE}
     *
     * @param maxConnectionsPerRoute The number of connections
     * @return This instance for chaining
     */
    HttpBasicConfig setMaxConnectionsPerRoute(int maxConnectionsPerRoute);

    /**
     * Sets the number of seconds that connections shall be kept alive.
     * Default: {@link HttpClientProperty#KEEP_ALIVE_DURATION_SECS}.
     *
     * @param keepAliveDuration The keep alive duration
     * @return This instance for chaining
     */
    HttpBasicConfig setKeepAliveDuration(int keepAliveDuration);

    /**
     * The interval in seconds between two monitoring runs that close stale connections
     * which exceeded the keep-alive duration.
     * Default: {@link HttpClientProperty#KEEP_ALIVE_MONITOR_INTERVAL_SECS}
     *
     * @param keepAliveMonitorInterval The interval
     * @return This instance for chaining
     */
    HttpBasicConfig setKeepAliveMonitorInterval(int keepAliveMonitorInterval);

    /**
     * Sets the socket buffer size in bytes.
     * Default: {@link HttpClientProperty#DEFAULT_SOCKET_BUFFER_SIZE}
     *
     * @param socketBufferSize The buffer size.
     * @return This instance for chaining
     */
    HttpBasicConfig setSocketBufferSize(int socketBufferSize);

}

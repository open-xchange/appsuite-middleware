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
     * @return The connection timeout
     */
    int getConnectionTimeout();

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
     * Sets the connection timeout in milliseconds. A timeout value of zero
     * is interpreted as an infinite timeout.
     * Default: {@link HttpClientProperty#CONNTECTION_TIMEOUT_MILLIS}
     *
     * @param connectionTimeout The timeout
     * @return This instance for chaining
     */
    HttpBasicConfig setConnectionTimeout(int connectionTimeout);

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

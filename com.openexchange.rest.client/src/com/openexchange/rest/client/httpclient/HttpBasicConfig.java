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

import static com.openexchange.java.Autoboxing.I;
import javax.annotation.concurrent.NotThreadSafe;
import com.openexchange.config.lean.LeanConfigurationService;

/**
 * {@link HttpBasicConfig} - Represents the basic configuration for a HTTP client.
 * <p>
 * Contains only values a administrator can modify.
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.4
 */
@NotThreadSafe
public class HttpBasicConfig {

    private int socketReadTimeout;
    private int connectionTimeout;
    private int connectionRequestTimeout;
    private int maxTotalConnections;
    private int maxConnectionsPerRoute;
    private int keepAliveDuration;
    private int keepAliveMonitorInterval;
    private int socketBufferSize;

    /**
     * Initializes a new {@link HttpBasicConfig}.
     * 
     * @param leanService The {@link LeanConfigurationService} to obtain the default configuration from
     */
    public HttpBasicConfig(LeanConfigurationService leanService) {
        super();
        for (HttpClientProperty property : HttpClientProperty.values()) {
            if (null == leanService) {
                // Apply defaults
                property.setInConfig(this, null);
            } else {
                // Read from default configuration
                property.setInConfig(this, I(leanService.getIntProperty(property.getProperty())));

            }
        }
    }

    /**
     * Sets the socket read timeout in milliseconds. A timeout value of zero
     * is interpreted as an infinite timeout.
     * Default: {@link HttpClientProperty#SOCKET_READ_TIMEOUT_MILLIS}
     *
     * @param socketReadTimeout The timeout
     * @return This instance for chaining
     */
    public HttpBasicConfig setSocketReadTimeout(int socketReadTimeout) {
        this.socketReadTimeout = socketReadTimeout;
        return this;
    }

    /**
     * Sets the connection timeout in milliseconds. A timeout value of zero
     * is interpreted as an infinite timeout.
     * Default: {@link HttpClientProperty#CONNTECTION_TIMEOUT_MILLIS}
     *
     * @param connectionTimeout The timeout
     * @return This instance for chaining
     */
    public HttpBasicConfig setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        return this;
    }

    /**
     * Sets the connection request timeout in milliseconds defining the maximum time to wait for a connection from the pool. A timeout
     * value of zero is interpreted as an infinite timeout.
     * <p/>
     * Default: {@link HttpClientProperty#CONNECTION_REQUEST_TIMEOUT_MILLIS}
     *
     * @param connectionRequestTimeout The timeout in milliseconds
     * @return This instance for chaining
     */
    public HttpBasicConfig setConnectionRequestTimeout(int connectionRequestTimeout) {
        this.connectionRequestTimeout = connectionRequestTimeout;
        return this;
    }

    /**
     * Sets the max. number of concurrent connections that can be opened by the
     * client instance.
     * Default: {@link HttpClientProperty#MAX_TOTAL_CONNECTIONS}
     *
     * @param maxTotalConnections The number of connections
     * @return This instance for chaining
     */
    public HttpBasicConfig setMaxTotalConnections(int maxTotalConnections) {
        this.maxTotalConnections = maxTotalConnections;
        return this;
    }

    /**
     * Sets the max. number of concurrent connections that can be opened by the
     * client instance per route.
     * Default: {@link HttpClientProperty#MAX_CONNECTIONS_PER_ROUTE}
     *
     * @param maxConnectionsPerRoute The number of connections
     * @return This instance for chaining
     */
    public HttpBasicConfig setMaxConnectionsPerRoute(int maxConnectionsPerRoute) {
        this.maxConnectionsPerRoute = maxConnectionsPerRoute;
        return this;
    }

    /**
     * Sets the number of seconds that connections shall be kept alive.
     * Default: {@link HttpClientProperty#KEEP_ALIVE_DURATION_SECS}.
     *
     * @param keepAliveDuration The keep alive duration
     * @return This instance for chaining
     */
    public HttpBasicConfig setKeepAliveDuration(int keepAliveDuration) {
        this.keepAliveDuration = keepAliveDuration;
        return this;
    }

    /**
     * The interval in seconds between two monitoring runs that close stale connections
     * which exceeded the keep-alive duration.
     * Default: {@link HttpClientProperty#KEEP_ALIVE_MONITOR_INTERVAL_SECS}
     *
     * @param keepAliveMonitorInterval The interval
     * @return This instance for chaining
     */
    public HttpBasicConfig setKeepAliveMonitorInterval(int keepAliveMonitorInterval) {
        this.keepAliveMonitorInterval = keepAliveMonitorInterval;
        return this;
    }

    /**
     * Sets the socket buffer size in bytes.
     * Default: {@link HttpClientProperty#DEFAULT_SOCKET_BUFFER_SIZE}
     *
     * @param socketBufferSize The buffer size.
     * @return This instance for chaining
     */
    public HttpBasicConfig setSocketBufferSize(int socketBufferSize) {
        this.socketBufferSize = socketBufferSize;
        return this;
    }

    /**
     * Gets the socketReadTimeout
     *
     * @return The socketReadTimeout
     */
    public int getSocketReadTimeout() {
        return socketReadTimeout;
    }

    /**
     * Gets the connectionTimeout
     *
     * @return The connectionTimeout
     */
    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    /**
     * Gets the connectionRequestTimeout
     *
     * @return The connectionRequestTimeout
     */
    public int getConnectionRequestTimeout() {
        return connectionRequestTimeout;
    }

    /**
     * Gets the maxTotalConnections
     *
     * @return The maxTotalConnections
     */
    public int getMaxTotalConnections() {
        return maxTotalConnections;
    }

    /**
     * Gets the maxConnectionsPerRoute
     *
     * @return The maxConnectionsPerRoute
     */
    public int getMaxConnectionsPerRoute() {
        return maxConnectionsPerRoute;
    }

    /**
     * Gets the keepAliveDuration
     *
     * @return The keepAliveDuration
     */
    public int getKeepAliveDuration() {
        return keepAliveDuration;
    }

    /**
     * Gets the keepAliveMonitorInterval
     *
     * @return The keepAliveMonitorInterval
     */
    public int getKeepAliveMonitorInterval() {
        return keepAliveMonitorInterval;
    }

    /**
     * Gets the socketBufferSize
     *
     * @return The socketBufferSize
     */
    public int getSocketBufferSize() {
        return socketBufferSize;
    }

    @Override
    public int hashCode() {
        final int prime = 131;
        int result = 1;
        result = prime * result + connectionRequestTimeout;
        result = prime * result + connectionTimeout;
        result = prime * result + keepAliveDuration;
        result = prime * result + keepAliveMonitorInterval;
        result = prime * result + maxConnectionsPerRoute;
        result = prime * result + maxTotalConnections;
        result = prime * result + socketBufferSize;
        result = prime * result + socketReadTimeout;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof HttpBasicConfig)) {
            return false;
        }
        HttpBasicConfig other = (HttpBasicConfig) obj;
        if (connectionRequestTimeout != other.connectionRequestTimeout) {
            return false;
        }
        if (connectionTimeout != other.connectionTimeout) {
            return false;
        }
        if (keepAliveDuration != other.keepAliveDuration) {
            return false;
        }
        if (keepAliveMonitorInterval != other.keepAliveMonitorInterval) {
            return false;
        }
        if (maxConnectionsPerRoute != other.maxConnectionsPerRoute) {
            return false;
        }
        if (maxTotalConnections != other.maxTotalConnections) {
            return false;
        }
        if (socketBufferSize != other.socketBufferSize) {
            return false;
        }
        if (socketReadTimeout != other.socketReadTimeout) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        //@formatter:off
        return "HttpBasicConfig [socketReadTimeout=" + socketReadTimeout + ", connectionTimeout=" + connectionTimeout + ", connectionRequestTimeout=" + connectionRequestTimeout 
            + ", maxTotalConnections=" + maxTotalConnections + ", maxConnectionsPerRoute=" + maxConnectionsPerRoute + ", keepAliveDuration=" + keepAliveDuration 
            + ", keepAliveMonitorInterval=" + keepAliveMonitorInterval + ", socketBufferSize=" + socketBufferSize + "]";
        //@formatter:on
    }

}

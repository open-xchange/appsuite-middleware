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

package com.openexchange.dovecot.doveadm.client.internal;

/**
 * {@link ClientConfig} - The client configuration.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.4
 */
public class ClientConfig {

    private final int totalConnections;
    private final int maxConnectionsPerRoute;
    private final int readTimeout;
    private final int connectTimeout;

    /**
     * Initializes a new {@link ClientConfig}.
     *
     * @param totalConnections The total number of connections to be managed in pool
     * @param maxConnectionsPerRoute The max. number of connections per route
     * @param readTimeout The socket read timeout
     * @param connectTimeout The connect timeout
     */
    public ClientConfig(int totalConnections, int maxConnectionsPerRoute, int readTimeout, int connectTimeout) {
        super();
        this.totalConnections = totalConnections;
        this.maxConnectionsPerRoute = maxConnectionsPerRoute;
        this.readTimeout = readTimeout;
        this.connectTimeout = connectTimeout;
    }

    /**
     * Gets the total number of connections to be managed in pool.
     *
     * @return The total number of connections to be managed in pool
     */
    public int getTotalConnections() {
        return totalConnections;
    }

    /**
     * Gets the max. number of connections per route.
     *
     * @return The max. number of connections per route
     */
    public int getMaxConnectionsPerRoute() {
        return maxConnectionsPerRoute;
    }

    /**
     * Gets the read timeout.
     *
     * @return The read timeout
     */
    public int getReadTimeout() {
        return readTimeout;
    }

    /**
     * Gets the connect timeout.
     *
     * @return The connect timeout
     */
    public int getConnectTimeout() {
        return connectTimeout;
    }

}
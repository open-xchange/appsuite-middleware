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

package com.openexchange.oauth;

/**
 * {@link HostInfo} - Provides name/IP address as well as the JVM route for the current host.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class HostInfo {

    private final String host;
    private final String route;

    /**
     * Initializes a new {@link HostInfo}.
     *
     * @param host The name or IP address of the current host
     * @param route The JVM route for current host
     */
    public HostInfo(String host, String route) {
        super();
        this.host = host;
        this.route = route;
    }

    /**
     * Gets the name or IP address of the host
     *
     * @return The name or IP address of the host
     */
    public String getHost() {
        return host;
    }

    /**
     * Gets the JVM route
     *
     * @return The JVM route
     */
    public String getRoute() {
        return route;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((host == null) ? 0 : host.hashCode());
        result = prime * result + ((route == null) ? 0 : route.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        HostInfo other = (HostInfo) obj;
        if (host == null) {
            if (other.host != null) {
                return false;
            }
        } else if (!host.equals(other.host)) {
            return false;
        }
        if (route == null) {
            if (other.route != null) {
                return false;
            }
        } else if (!route.equals(other.route)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("HostInfo [");
        if (host != null) {
            builder.append("host=").append(host).append(", ");
        }
        if (route != null) {
            builder.append("route=").append(route);
        }
        builder.append("]");
        return builder.toString();
    }

}

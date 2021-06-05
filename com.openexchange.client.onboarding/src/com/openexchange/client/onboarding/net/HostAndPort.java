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

package com.openexchange.client.onboarding.net;

import com.openexchange.java.Strings;

/**
 * {@link HostAndPort} - An immutable representation of a host and port.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class HostAndPort {

    private final String host;
    private final int port;
    private final int hashCode;

    /**
     * Initializes a new {@link HostAndPort} w/o a port.
     *
     * @param host The host name; e.g. <code>"dav.example.com"</code>
     */
    public HostAndPort(String host) {
        this(host, 0);
    }

    /**
     * Initializes a new {@link HostAndPort}.
     *
     * @param host The host name; e.g. <code>"dav.example.com"</code>
     * @param port The port; e.g. <code>8843</code>
     */
    public HostAndPort(String host, int port) {
        super();
        if (port < 0 || port > 0xFFFF) {
            throw new IllegalArgumentException("port out of range:" + port);
        }
        if (host == null) {
            throw new IllegalArgumentException("hostname can't be null");
        }
        this.host = host;
        this.port = port;
        hashCode = (Strings.asciiLowerCase(host).hashCode()) ^ port;
    }

    /**
     * Gets the host name
     *
     * @return The host name
     */
    public String getHost() {
        return host;
    }

    /**
     * Gets the port number
     *
     * @return The port number or <code>0</code> if not set
     */
    public int getPort() {
        return port;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof HostAndPort)) {
            return false;
        }
        HostAndPort other = (HostAndPort) obj;
        if (port != other.port) {
            return false;
        }
        if (host == null) {
            if (other.host != null) {
                return false;
            }
        } else if (!host.equals(other.host)) {
            return false;
        }
        return true;
    }

}

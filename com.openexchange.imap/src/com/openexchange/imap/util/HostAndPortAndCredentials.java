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

package com.openexchange.imap.util;

/**
 * Simple (immutable) class to hold host and port as well as credentials for an IMAP end-point
 */
public final class HostAndPortAndCredentials {

    /**
     * Gets the appropriate instance for specified URL string.
     *
     * @param user The user identifier
     * @param password The password or secret
     * @param serverUrl The URL string
     * @return The appropriate instance
     */
    public static HostAndPortAndCredentials instanceFor(String user, String password, String serverUrl) {
        if (null == serverUrl) {
            return null;
        }

        int pos = serverUrl.lastIndexOf(':');
        int port;
        if (pos > 0) {
            try {
                port = Integer.parseInt(serverUrl.substring(pos + 1).trim());
            } catch (NumberFormatException e) {
                port = 143;
            }
        } else {
            port = 143;
        }

        return pos == -1 ? new HostAndPortAndCredentials(user, password, serverUrl.trim(), port) : new HostAndPortAndCredentials(user, password, serverUrl.substring(0, pos).trim(), port);
    }

    // ----------------------------------------------------------------------------------

    private final String user;
    private final String password;
    private final String host;
    private final int port;
    private final int hashCode;

    /**
     * Initializes a new {@link HostAndPortAndCredentials}.
     *
     * @param user The user identifier
     * @param password The password or secret
     * @param host The host name or IP address of the IMAP server
     * @param port The port
     */
    public HostAndPortAndCredentials(String user, String password, String host, int port) {
        super();
        if (port < 0 || port > 0xFFFF) {
            throw new IllegalArgumentException("port out of range:" + port);
        }
        if (host == null) {
            throw new IllegalArgumentException("host name can't be null");
        }
        if (user == null) {
            throw new IllegalArgumentException("user can't be null");
        }
        if (password == null) {
            throw new IllegalArgumentException("password can't be null");
        }
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;

        int prime = 31;
        int result = 1;
        result = prime * result + host.hashCode();
        result = prime * result + port;
        result = prime * result + user.hashCode();
        result = prime * result + password.hashCode();
        hashCode = result;
    }

    /**
     * Gets the host name or IP address of the IMAP server
     *
     * @return The host name or IP address of the IMAP server
     */
    public String getHost() {
        return host;
    }

    /**
     * Gets the port
     *
     * @return The port
     */
    public int getPort() {
        return port;
    }

    /**
     * Gets the user
     *
     * @return The user
     */
    public String getUser() {
        return user;
    }

    /**
     * Gets the password
     *
     * @return The password
     */
    public String getPassword() {
        return password;
    }

    @Override
    public int hashCode() {
        return hashCode;
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
        HostAndPortAndCredentials other = (HostAndPortAndCredentials) obj;
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
        if (user == null) {
            if (other.user != null) {
                return false;
            }
        } else if (!user.equals(other.user)) {
            return false;
        }
        if (password == null) {
            if (other.password != null) {
                return false;
            }
        } else if (!password.equals(other.password)) {
            return false;
        }
        return true;
    }

}
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

package com.openexchange.mail.config;

import javax.mail.internet.idn.IDNA;
import com.openexchange.java.Strings;
import com.openexchange.tools.net.URIDefaults;

/**
 * {@link ConfiguredServer} - Represents a statically configured server end-point.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class ConfiguredServer {

    /**
     * Parses the appropriate instance from given server string representation.
     *
     * @param server The server string representation
     * @param defaults The URI defaults to consider
     * @return The parsed instance
     */
    public static ConfiguredServer parseFrom(String server, URIDefaults defaults) {
        if (null == server) {
            return null;
        }

        boolean secure = false;
        String protocol = null;
        int pos = server.indexOf("://");
        if (pos >= 0) {
            protocol = server.substring(0, pos).trim();
            if (Strings.isEmpty(protocol)) {
                protocol = null;
            } else if (protocol.length() > 1 && protocol.endsWith("s")) {
                secure = true;
                protocol = protocol.substring(0, protocol.length() - 1);
            }
            pos = pos + 3;
        } else {
            pos = 0;
        }

        int port = 0;
        String host = null;
        int colonPos = server.lastIndexOf(':');
        if (colonPos >= 0 && colonPos > pos) {
            host = server.substring(pos, colonPos).trim();
            if (Strings.isEmpty(host)) {
                host = null;
            }

            try {
                port = Integer.parseInt(server.substring(colonPos + 1).trim());
            } catch (NumberFormatException e) {
                port = 0;
            }
        } else {
            host = server.substring(pos).trim();
        }

        if (null == host) {
            host = "localhost";
        }

        if (null != defaults) {
            if (null == protocol) {
                protocol = defaults.getProtocol();
            }
            if (0 == port) {
                port = secure ? defaults.getSSLPort() : defaults.getPort();
            }
        }

        return new ConfiguredServer(protocol, host, port, secure);
    }

    // --------------------------------------------------------------------------------------------------------------------------------

    private final String protocol;
    private final String hostName;
    private final int port;
    private final boolean secure;

    /**
     * Initializes a new {@link ConfiguredServer} from given arguments.
     *
     * @param protocol The protocol identifier; e.g. <code>"imap"</code>
     * @param hostName The host name or IP address; e.g. <code>"localhost"</code>
     * @param port The port number; an integer between 1 and 65536
     * @param secure <code>true</code> to indicate a secure connection is established; otherwise <code>false</code>
     * @throws IllegalArgumentException If given host name is <code>null</code> or port is illegal
     */
    public ConfiguredServer(String protocol, String hostName, int port, boolean secure) {
        super();
        if (null == hostName) {
            throw new IllegalArgumentException("Host must not be null");
        }
        if (port > 65536) {
            throw new IllegalArgumentException("Port must not be greater than 65536");
        }
        this.protocol = protocol;
        this.hostName = hostName;
        this.port = port <= 0 ? 0 : port;
        this.secure = secure;
    }

    /**
     * Gets the protocol
     *
     * @return The protocol
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * Gets the host name
     *
     * @return The host name
     */
    public String getHostName() {
        return hostName;
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
     * Gets the secure flag
     *
     * @return <code>true</code> to indicate a secure connection is established; otherwise <code>false</code>
     */
    public boolean isSecure() {
        return secure;
    }

    /**
     * Gets the URL string for this instance; e.g. <code>"imap://localhost:143"</code>
     *
     * @param punycode Whether to use puny-code encoding for the host name
     * @return The URL string
     */
    public String getUrlString(boolean punycode) {
        StringBuilder builder = new StringBuilder(48);
        if (null != protocol) {
            builder.append(protocol);
            if (secure) {
                builder.append('s');
            }
            builder.append("://");
        }
        builder.append(punycode ? IDNA.toASCII(hostName) : hostName);
        if (port > 0) {
            builder.append(':').append(port);
        }
        return builder.toString();
    }

    @Override
    public String toString() {
        return getUrlString(false);
    }

}

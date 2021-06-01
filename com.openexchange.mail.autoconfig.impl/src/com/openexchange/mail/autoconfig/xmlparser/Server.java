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

package com.openexchange.mail.autoconfig.xmlparser;

/**
 * {@link Server}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public abstract class Server {

    public static enum SocketType {
        PLAIN(Server.PLAIN_STRING), STARTTLS(Server.STARTTLS_STRING), SSL(Server.SSL_STRING);

        private final String keyword;

        private SocketType(String keyword) {
            this.keyword = keyword;
        }

        public static SocketType getSocketType(String keyword) {
            if (keyword.equalsIgnoreCase(Server.PLAIN_STRING)) {
                return PLAIN;
            }
            if (keyword.equalsIgnoreCase(Server.STARTTLS_STRING)) {
                return STARTTLS;
            }
            if (keyword.equalsIgnoreCase(Server.SSL_STRING)) {
                return SSL;
            }
            return null;
        }

        /**
         * Gets the keyword
         *
         * @return The keyword
         */
        public String getKeyword() {
            return keyword;
        }

    }

    // ---------------------------------------------- Constants -----------------------------------------------

    public static final String TYPE = "type";

    public static final String POP3_STRING = "pop3";

    public static final String IMAP_STRING = "imap";

    public static final String SMTP_STRING = "smtp";

    public static final String HOSTNAME = "hostname";

    public static final String PORT = "port";

    public static final String SOCKET_TYPE = "socketType";

    public static final String PLAIN_STRING = "plain";

    public static final String STARTTLS_STRING = "STARTTLS";

    public static final String SSL_STRING = "SSL";

    public static final String USERNAME = "username";

    public static final String EMAILADDRESS = "%EMAILADDRESS%";

    public static final String MAILLOCALPART = "%EMAILLOCALPART%";

    public static final String EMAILDOMAIN = "%EMAILDOMAIN%";

    public static final String AUTHENTICATION = "authentication";

    public static final String PASSWORD_CLEAR = "password-cleartext";

    public static final String PASSWORD_ENC = "password-encrypted";

    public static final String NTLM = "NTLM";

    public static final String GSSAPI = "GSSAPI";

    public static final String IP_BASED = "client-IP-address";

    public static final String TLS = "TLS-client-cert";

    public static final String NONE = "none";

    // ---------------------------------------------- Members -----------------------------------------------

    private String hostname;
    private int port;
    private SocketType socketType;
    private String username;
    private String authentication;

    /**
     * Initializes a new {@link Server}.
     */
    protected Server() {
        super();
    }

    /**
     * Gets the host name
     *
     * @return The host name
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * Sets the host name
     *
     * @param hostname The host name to set
     */
    public void setHostname(String hostname) {
        this.hostname = hostname;
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
     * Sets the port
     *
     * @param port The port to set
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Gets the socketType
     *
     * @return The socketType
     */
    public SocketType getSocketType() {
        return socketType;
    }

    /**
     * Sets the socketType
     *
     * @param socketType The socketType to set
     */
    public void setSocketType(String socketType) {
        this.socketType = SocketType.getSocketType(socketType);
    }

    /**
     * Gets the user name
     *
     * @return The user name
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the user name
     *
     * @param username The user name to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the authentication
     *
     * @return The authentication
     */
    public String getAuthentication() {
        return authentication;
    }

    /**
     * Sets the authentication
     *
     * @param authentication The authentication to set
     */
    public void setAuthentication(String authentication) {
        this.authentication = authentication;
    }

    public abstract void setType(String setType);

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(1024);
        builder.append("Server [");
        if (hostname != null) {
            builder.append("hostname=").append(hostname).append(", ");
        }
        builder.append("port=").append(port).append(", ");
        if (socketType != null) {
            builder.append("socketType=").append(socketType.getKeyword()).append(", ");
        }
        if (username != null) {
            builder.append("username=").append(username).append(", ");
        }
        if (authentication != null) {
            builder.append("authentication=").append(authentication);
        }
        builder.append("]");
        return builder.toString();
    }

}

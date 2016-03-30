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
 *    trademarks of the OX Software GmbH group of companies.
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

package com.openexchange.mail.autoconfig.xmlparser;

/**
 * {@link Server}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public abstract class Server {

    public static final String TYPE = "type";

    public static final String POP3 = "pop3";

    public static final String IMAP = "imap";

    public static final String SMTP = "smtp";

    public static final String HOSTNAME = "hostname";

    public static final String PORT = "port";

    public static final String SOCKET_TYPE = "socketType";

    public static final String PLAIN = "plain";

    public static final String STARTTLS = "STARTTLS";

    public static final String SSL = "SSL";

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

    private String hostname;

    private int port;

    private SocketType socketType;

    private String username;

    private String authentication;

    public enum SocketType {
        PLAIN(Server.PLAIN), STARTTLS(Server.STARTTLS), SSL(Server.SSL);

        private final String keyword;

        private SocketType(String keyword) {
            this.keyword = keyword;
        }

        public static SocketType getSocketType(String keyword) {
            if (keyword.equalsIgnoreCase(Server.PLAIN)) {
                return PLAIN;
            }
            if (keyword.equalsIgnoreCase(Server.STARTTLS)) {
                return STARTTLS;
            }
            if (keyword.equalsIgnoreCase(Server.SSL)) {
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

    /**
     * Gets the hostname
     *
     * @return The hostname
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * Sets the hostname
     *
     * @param hostname The hostname to set
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
     * Gets the username
     *
     * @return The username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username
     *
     * @param username The username to set
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

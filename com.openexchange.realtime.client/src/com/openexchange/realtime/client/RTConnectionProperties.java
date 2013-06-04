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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.realtime.client;

/**
 * {@link RTConnectionProperties} are used to configure a {@link RTConnection}.
 * 
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class RTConnectionProperties {

    public static enum RTConnectionType {
        LONG_POLLING, WEBSOCKET
    }

    private String user;

    private String password;

    private RTConnectionType type;

    private String protocol;

    private String host;

    private int port;

    private RTConnectionProperties() {
        super();
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

    /**
     * Gets the type
     * 
     * @return The type
     */
    public RTConnectionType getType() {
        return type;
    }

    /**
     * Gets the host
     * 
     * @return The host
     */
    public String getHost() {
        return host;
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
     * Gets the port
     * 
     * @return The port
     */
    public int getPort() {
        return port;
    }

    /**
     * Gets the connection type.
     * 
     * @return The connection type.
     */
    public RTConnectionType getConnectionType() {
        return type;
    }

    /**
     * Factory method for creating a new {@link Builder}.
     * 
     * @param user The OX user.
     * @param password The password.
     * @return
     */
    public static Builder newBuilder(String user, String password) {
        return new Builder(user, password);
    }

    public static class Builder {

        private final RTConnectionProperties properties;

        private Builder(String user, String password) {
            super();
            properties = new RTConnectionProperties();
            properties.user = user;
            properties.password = password;
        }

        /**
         * Sets the connection type.
         * 
         * @param type The connection type.
         */
        public Builder setConnectionType(RTConnectionType type) {
            properties.type = type;
            return this;
        }

        /**
         * Sets the OX host.
         * 
         * @param host The host.
         */
        public Builder setHost(String host) {
            properties.host = host;
            return this;
        }

        /**
         * Sets the port.
         * 
         * @param port The port.
         */
        public Builder setPort(int port) {
            properties.port = port;
            return this;
        }

        /**
         * Sets the protocol.
         * 
         * @param protocol The protocol.
         */
        public Builder setProtocol(String protocol) {
            properties.protocol = protocol;
            return this;
        }

        /**
         * @return A valid {@link RTConnectionProperties} instance.
         */
        public RTConnectionProperties build() {
            if (properties.user == null) {
                throw new IllegalStateException("A user must be set!");
            }

            if (properties.password == null) {
                throw new IllegalStateException("A password must be set!");
            }

            if (properties.type == null) {
                throw new IllegalStateException("A connection type must be set!");
            }

            if (properties.host == null) {
                throw new IllegalStateException("A host must be set!");
            }

            if (properties.protocol == null) {
                throw new IllegalStateException("A protocol must be set!");
            }

            if (properties.port <= 0) {
                throw new IllegalStateException("Port must be a valid port number!");
            }

            return properties;
        }

    }

}

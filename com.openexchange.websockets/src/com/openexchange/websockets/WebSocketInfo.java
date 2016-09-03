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

package com.openexchange.websockets;

/**
 * {@link WebSocketInfo} - Provides basic Web Socket information.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class WebSocketInfo {

    /**
     * Creates a new builder.
     *
     * @return The new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /** The builder for an instance of <code>WebSocketInfo</code> */
    public static final class Builder {

        private int userId;
        private int contextId;
        private String address;
        private ConnectionId connectionId;
        private String path;

        Builder() {
            super();
        }

        /**
         * Sets the user identifier
         *
         * @param userId The user identifier
         */
        public Builder userId(int userId) {
            this.userId = userId;
            return this;
        }

        /**
         * Sets the context identifier
         *
         * @param contextId The context identifier
         */
        public Builder contextId(int contextId) {
            this.contextId = contextId;
            return this;
        }

        /**
         * Sets the path
         *
         * @param path The path
         */
        public Builder path(String path) {
            this.path = path;
            return this;
        }

        /**
         * Sets the member address
         *
         * @param address The member address
         */
        public Builder address(String address) {
            this.address = address;
            return this;
        }

        /**
         * Sets the connection identifier
         *
         * @param memberUuid The connection identifier
         */
        public Builder connectionId(ConnectionId connectionId) {
            this.connectionId = connectionId;
            return this;
        }

        /**
         * Creates the <code>WebSocketInfo</code> instance from this builder's properties.
         *
         * @return The <code>WebSocketInfo</code> instance
         */
        public WebSocketInfo build() {
            return new WebSocketInfo(userId, contextId, address, connectionId, path);
        }
    }

    // -------------------------------------------------------------------------------------------

    private final int userId;
    private final int contextId;
    private final String address;
    private final ConnectionId connectionId;
    private final String path;

    /**
     * Initializes a new {@link WebSocketInfo}.
     */
    WebSocketInfo(int userId, int contextId, String address, ConnectionId connectionId, String path) {
        super();
        this.userId = userId;
        this.contextId = contextId;
        this.address = address;
        this.connectionId = connectionId;
        this.path = path;
    }

    /**
     * Gets the user identifier
     *
     * @return The user identifier
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Gets the context identifier
     *
     * @return The context identifier
     */
    public int getContextId() {
        return contextId;
    }

    /**
     * Gets the member address
     *
     * @return The member address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Gets the connection identifier
     *
     * @return The connection identifier
     */
    public ConnectionId getConnectionId() {
        return connectionId;
    }

    /**
     * Gets the path
     *
     * @return The path
     */
    public String getPath() {
        return path;
    }

}

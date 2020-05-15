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

package com.openexchange.authentication.application.storage.history;

/**
 * {@link AppPasswordLogin}
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.4
 */
public class AppPasswordLogin {

    /**
     * Creates a new builder instance.
     *
     * @return The new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /** The builder for a <code>AppPasswordLogin/code> instance */
    public static final class Builder {

        /**
         * Initializes a new {@link Builder}.
         */
        Builder() {
            super();
        }

        private long timestamp;
        private String userAgent;
        private String client;
        private String ipAddress;

        /**
         * Set the last login time in ticks for the event
         * setLastLogin
         *
         * @param timestamp
         * @return Builder
         */
        public Builder setTimestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        /**
         * Set the last device used to login
         * setLastDevice
         *
         * @param client
         * @return Builder
         */
        public Builder setClient(String client) {
            this.client = client;
            return this;
        }

        /**
         * Set the user agent used during login.
         *
         * @param userAgent The user agent
         * @return The builder
         */
        public Builder setUserAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        /**
         * Set the IP of the event
         * setLastIp
         *
         * @param ipAddress
         * @return Builder
         */
        public Builder setIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        /**
         * Build the app password login history event
         * build
         *
         * @return Builder
         */
        public AppPasswordLogin build() {
            return new AppPasswordLogin(timestamp, ipAddress, client, userAgent);
        }

    }

    private final long timestamp;
    private final String client;
    private final String ipAddress;
    private final String userAgent;

    AppPasswordLogin(long timestamp, String ipAddress, String client, String userAgent) {
        super();
        this.timestamp = timestamp;
        this.ipAddress = ipAddress;
        this.client = client;
        this.userAgent = userAgent;
    }

    /**
     * Get the ticks of the event
     * getLastLogin
     *
     * @return Long representation of last login time.
     */
    public long getTimestamp() {
        return timestamp;

    }

    /**
     * Get the IP address of the event
     * getLastIp
     *
     * @return String of IP used
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * Get the device used during the event
     * getLastDevice
     *
     * @return String last device type
     */
    public String getClient() {
        return client;
    }

    /**
     * Gets the user agent used during login, if available.
     *
     * @return The user agent, or <code>null</code> if not available
     */
    public String getUserAgent() {
        return userAgent;
    }

}

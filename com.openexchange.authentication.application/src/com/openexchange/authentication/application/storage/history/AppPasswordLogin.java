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

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

package com.openexchange.antiabuse;

/**
 * {@link ReportParameters} - The parameters to use when performing the <code>"report"</code> request.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class ReportParameters {

    /**
     * Creates a new builder instance.
     *
     * @return The builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /** The builder for an instance of <code>ReportParameters</code> */
    public static class Builder {

        private ReportValue reportValue;
        private String login;
        private String password;
        private String remoteAddress;
        private String userAgent;
        private Protocol protocol;

        /**
         * Initializes a new {@link ReportParameters.Builder}.
         */
        Builder() {
            super();
        }

        /**
         * Sets the userAgent
         *
         * @param userAgent The userAgent to set
         * @return This builder
         */
        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        /**
         * Sets the protocol that was used to access the authority.
         *
         * @param protocol The protocol to set
         * @return This builder
         */
        public Builder protocol(Protocol protocol) {
            this.protocol = protocol;
            return this;
        }

        /**
         * Sets the report value to advertise to Anti-Abuse service
         *
         * @param reportValue The report value to advertise to Anti-Abuse service
         * @return This builder
         */
        public Builder reportValue(ReportValue reportValue) {
            this.reportValue = reportValue;
            return this;
        }

        /**
         * Sets the login
         *
         * @param login The login to set
         * @return This builder
         */
        public Builder login(String login) {
            this.login = login;
            return this;
        }

        /**
         * Sets the password
         *
         * @param password The password to set
         * @return This builder
         */
        public Builder password(String password) {
            this.password = password;
            return this;
        }

        /**
         * Sets the remoteAddress
         *
         * @param remoteAddress The remoteAddress to set
         * @return This builder
         */
        public Builder remoteAddress(String remoteAddress) {
            this.remoteAddress = remoteAddress;
            return this;
        }

        /**
         * Creates the <code>ReportParameters</code> instance from this builder's arguments.
         *
         * @return The <code>ReportParameters</code> instance
         */
        public ReportParameters build() {
            return new ReportParameters(reportValue, login, password, remoteAddress, userAgent, protocol);
        }
    }

    // ----------------------------------------------------------------------------------------------------------------------------

    private final ReportValue reportValue;
    private final String login;
    private final String password;
    private final String remoteAddress;
    private final String userAgent;
    private final Protocol protocol;

    ReportParameters(ReportValue reportValue, String login, String password, String remoteAddress, String userAgent, Protocol protocol) {
        super();
        this.reportValue = reportValue;
        this.login = login;
        this.password = password;
        this.remoteAddress = remoteAddress;
        this.userAgent = userAgent;
        this.protocol = protocol;
    }

    /**
     * Gets the login string
     *
     * @return The login string
     */
    public String getLogin() {
        return login;
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
     * Gets the remote address
     *
     * @return The remote address
     */
    public String getRemoteAddress() {
        return remoteAddress;
    }

    /**
     * Gets the report value to advertise to Anti-Abuse service
     *
     * @return The report value to advertise to Anti-Abuse service
     */
    public ReportValue getReportValue() {
        return reportValue;
    }

    /**
     * Gets the User-Agent string.
     *
     * @return The User-Agent string
     */
    public String getUserAgent() {
        return userAgent;
    }

    /**
     * Gets the protocol
     *
     * @return The protocol
     */
    public Protocol getProtocol() {
        return protocol;
    }

}

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

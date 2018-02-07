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

package com.openexchange.client.onboarding.mail;

/**
 * {@link Configuration}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class Configuration {

    /**
     * Creates a new builder instance.
     *
     * @return The new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /** The builder for an instance of <code>Configuration</code> */
    public static class Builder {

        private String host;
        private int port;
        private boolean secure;
        private String login;
        private String password;
        private boolean needsAuthentication;

        /**
         * Initializes a new {@link Configuration.Builder}.
         */
        Builder() {
            super();
            // Authentication needed by default
            needsAuthentication = true;
        }

        /**
         * Sets the host
         *
         * @param host The host to set
         * @return This builder
         */
        public Builder withHost(String host) {
            this.host = host;
            return this;
        }

        /**
         * Sets the port
         *
         * @param port The port to set
         * @return This builder
         */
        public Builder withPort(int port) {
            this.port = port;
            return this;
        }

        /**
         * Sets the secure flag.
         *
         * @param secure The secure flag to set
         * @return This builder
         */
        public Builder withSecure(boolean secure) {
            this.secure = secure;
            return this;
        }

        /**
         * Sets the login.
         *
         * @param login The login to set
         * @return This builder
         */
        public Builder withLogin(String login) {
            this.login = login;
            return this;
        }

        /**
         * Sets the password.
         *
         * @param password The password to set
         * @return This builder
         */
        public Builder withPassword(String password) {
            this.password = password;
            return this;
        }

        /**
         * Sets whether authentication is needed or not (default is <code>true</code>).
         *
         * @param needsAuthentication <code>true</code> to signal that authentication is needed; otherwise <code>false</code> if authentication-less access is permitted
         */
        public Builder withNeedsAuthentication(boolean needsAuthentication) {
            this.needsAuthentication = needsAuthentication;
            return this;
        }

        /**
         * Builds the <code>Configuration</code> instance from this builder's arguments
         *
         * @return The <code>Configuration</code> instance
         */
        public Configuration build() {
            return new Configuration(host, port, secure, login, password, needsAuthentication);
        }
    } // End of Builder class

    // -----------------------------------------------------------------------------------------------------

    /** The host name */
    public final String host;

    /** The port */
    public final int port;

    /** Whether secure connection needs to be established */
    public final boolean secure;

    /** The login */
    public final String login;

    /** The password */
    public final String password;

    /** <code>true</code> to signal that authentication is needed; otherwise <code>false</code> if authentication-less access is permitted */
    public final boolean needsAuthentication;

    /**
     * Initializes a new {@link Configuration}.
     *
     * @param host The host name
     * @param port The port
     * @param secure Whether secure connection needs to be established
     * @param login The login
     * @param password The password
     */
    Configuration(String host, int port, boolean secure, String login, String password, boolean needsAuthentication) {
        super();
        this.host = host;
        this.port = port;
        this.secure = secure;
        this.login = needsAuthentication ? login : null;
        this.password = needsAuthentication ? password : null;
        this.needsAuthentication = needsAuthentication;
    }

    /**
     * Signals whether this configuration does not require authentication.
     *
     * @return <code>true</code> if no authentication is supposed to be performed; otherwise <code>false</code>
     */
    public boolean noAuthentication() {
        return false == needsAuthentication;
    }

}

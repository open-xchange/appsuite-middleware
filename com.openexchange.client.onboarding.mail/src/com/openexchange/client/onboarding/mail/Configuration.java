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

}

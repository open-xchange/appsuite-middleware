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

import java.util.Collections;
import java.util.Map;
import com.google.common.collect.ImmutableMap;

/**
 * {@link AllowParameters} - The parameters to use when performing the <code>"allow"</code> request.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class AllowParameters {

    /**
     * Creates a new builder instance.
     *
     * @return The builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /** The builder for an instance of <code>AllowParameters</code> */
    public static class Builder {

        private String login;
        private String password;
        private String remoteAddress;
        private String userAgent;
        private Protocol protocol;
        private Map<String, String> attributes;

        /**
         * Initializes a new {@link AllowParameters.Builder}.
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
         * Sets the attributes
         *
         * @param attributes The attributes to set
         * @return This builder
         */
        public Builder attributes(Map<String, String> attributes) {
            this.attributes = attributes;
            return this;
        }

        /**
         * Creates the <code>AllowParameters</code> instance from this builder's arguments.
         *
         * @return The <code>AllowParameters</code> instance
         */
        public AllowParameters build() {
            return new AllowParameters(login, password, remoteAddress, userAgent, protocol, attributes);
        }
    }

    // ----------------------------------------------------------------------------------------------------------------------------

    private final String login;
    private final String password;
    private final String remoteAddress;
    private final Map<String, String> attributes;
    private final String userAgent;
    private final Protocol protocol;

    AllowParameters(String login, String password, String remoteAddress, String userAgent, Protocol protocol, Map<String, String> attributes) {
        super();
        this.login = login;
        this.password = password;
        this.remoteAddress = remoteAddress;
        this.userAgent = userAgent;
        this.protocol = protocol;
        this.attributes = null == attributes ? Collections.<String, String> emptyMap() : ImmutableMap.<String, String> copyOf(attributes);
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

    /**
     * Gets the optional attributes
     *
     * @return The optional attributes
     */
    public Map<String, String> getAttributes() {
        return attributes;
    }

}

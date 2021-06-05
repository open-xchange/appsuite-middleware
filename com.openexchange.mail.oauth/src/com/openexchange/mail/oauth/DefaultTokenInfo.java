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

package com.openexchange.mail.oauth;

/**
 * {@link DefaultTokenInfo} - The default token info implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class DefaultTokenInfo implements TokenInfo {

    /**
     * The XOAUTH2 authentication mechanism.
     */
    public static final String AUTH_MECH_XOAUTH2 = "XOAUTH2";

    /**
     * The OAUTHBEARER authentication mechanism.
     */
    public static final String AUTH_MECH_OAUTHBEARER = "OAUTHBEARER";

    /**
     * Creates a new XOAUTH2 token info for specified token string
     *
     * @param token The token string
     * @return The XOAUTH2 token info
     */
    public static DefaultTokenInfo newXOAUTH2TokenInfoFor(String token) {
        return null == token ? null : builder().setAuthMechanism(AUTH_MECH_XOAUTH2).setToken(token).build();
    }

    /**
     * Creates a new OAUTHBEARER token info for specified token string
     *
     * @param token The token string
     * @return The OAUTHBEARER token info
     */
    public static DefaultTokenInfo newOAUTHBEARERTokenInfoFor(String token) {
        return null == token ? null : builder().setAuthMechanism(AUTH_MECH_OAUTHBEARER).setToken(token).build();
    }

    /**
     * Creates a new builder instance.
     *
     * @return The builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /** The builder for an <code>DefaultTokenInfo</code> instance */
    public static class Builder {

        private String token;
        private String authMechanism;

        Builder() {
            super();
        }

        /**
         * Sets the token
         *
         * @param token The token to set
         * @return This builder
         */
        public Builder setToken(String token) {
            this.token = token;
            return this;
        }

        /**
         * Sets the authMechanism
         *
         * @param authMechanism The authMechanism to set
         * @return This builder
         */
        public Builder setAuthMechanism(String authMechanism) {
            this.authMechanism = authMechanism;
            return this;
        }

        /**
         * Creates the <code>DefaultTokenInfo</code> instance from this builder's arguments.
         *
         * @return The <code>DefaultTokenInfo</code> instance
         */
        public DefaultTokenInfo build() {
            return new DefaultTokenInfo(token, authMechanism);
        }
    }

    // ----------------------------------------------------------------

    private final String token;
    private final String authMechanism;

    /**
     * Initializes a new {@link DefaultTokenInfo}.
     */
    DefaultTokenInfo(String token, String authMechanism) {
        super();
        this.token = token;
        this.authMechanism = authMechanism;
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public String getAuthMechanism() {
        return authMechanism;
    }

}

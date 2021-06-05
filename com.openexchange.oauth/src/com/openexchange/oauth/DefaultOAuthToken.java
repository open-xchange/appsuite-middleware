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

package com.openexchange.oauth;

/**
 * {@link DefaultOAuthToken} - The default OAuth token implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class DefaultOAuthToken implements OAuthToken {

    private String token;
    private String secret;
    private long expiry;

    /**
     * Initializes a new {@link DefaultOAuthToken}.
     */
    public DefaultOAuthToken() {
        super();
    }

    /**
     * Initializes a new {@link DefaultOAuthToken}.
     *
     * @param token The token string
     * @param secret The secret string
     * @param expiry The expiration time stamp
     */
    public DefaultOAuthToken(final String token, final String secret, long expiry) {
        super();
        this.token = token;
        this.secret = secret;
        this.expiry = expiry;
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public String getSecret() {
        return secret;
    }

    @Override
    public long getExpiration() {
        return expiry;
    }

    /**
     * Sets the token
     *
     * @param token The token to set
     */
    public void setToken(final String token) {
        this.token = token;
    }

    /**
     * Sets the secret
     *
     * @param secret The secret to set
     */
    public void setSecret(final String secret) {
        this.secret = secret;
    }

    /**
     * Sets the expiration time stamp
     *
     * @param expiry The expiration time stamp to set
     */
    public void setExpiration(long expiry) {
        this.expiry = expiry;
    }
}

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

package com.openexchange.saml.oauth.service;

/**
 * {@link OAuthAccessToken} - Represents an OAuth access token.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.4
 */
public class OAuthAccessToken {

    private final String accessToken;
    private final String refreshToken;
    private final String type;
    private final int expiresIn;

    /**
     * Initializes a new {@link OAuthAccessToken}.
     * 
     * @param accessToken The access token string
     * @param refreshToken The refresh token string
     * @param type The token type; e.g. <code>"Bearer"</code>
     * @param expiresIn The lifetime in seconds of the access token
     */
    public OAuthAccessToken(String accessToken, String refreshToken, String type, int expiresIn) {
        super();
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.type = type;
        this.expiresIn = expiresIn;
    }

    /**
     * Gets the access token
     *
     * @return The access token
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * Gets the refresh token
     *
     * @return The refresh token
     */
    public String getRefreshToken() {
        return refreshToken;
    }

    /**
     * Gets the type of the token
     *
     * @return The type of the token
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the lifetime in seconds of the access token
     *
     * @return The lifetime in seconds of the access token
     */
    public int getExpiresIn() {
        return expiresIn;
    }

}

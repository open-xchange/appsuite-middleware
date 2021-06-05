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

package com.openexchange.authentication.oauth.impl;

import java.util.Date;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.RefreshToken;
import com.nimbusds.oauth2.sdk.token.Tokens;
import com.openexchange.session.oauth.OAuthTokens;

/**
 * {@link OAuthRequestIssuer}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.3
 */
public abstract class OAuthRequestIssuer {

    /** The authentication configuration */
    protected final OAuthAuthenticationConfig config;

    /**
     * Initializes a new {@link OAuthRequestIssuer}.
     *
     * @param config The authentication configuration
     */
    protected OAuthRequestIssuer(OAuthAuthenticationConfig config) {
        super();
        this.config = config;
    }

    /**
     * Get the client authentication based on the authentication configuration.
     *
     * @return The {@link ClientAuthentication}
     */
    protected ClientAuthentication getClientAuthentication() {
        ClientID clientID = new ClientID(config.getClientId());
        Secret clientSecret = new Secret(config.getClientSecret());
        return new ClientSecretBasic(clientID, clientSecret);
    }

    /**
     * Converts tokens to OAuth tokens.
     *
     * @param tokens The tokens to convert
     * @return The {@link OAuthTokens}
     */
    protected OAuthTokens convertNimbusTokens(Tokens tokens) {
        AccessToken accessToken = tokens.getAccessToken();
        long lifetime = accessToken.getLifetime();
        Date expiryDate = null;
        if (lifetime > 0) {
            long expiryDateMillis = System.currentTimeMillis();
            expiryDateMillis += lifetime * 1000;
            expiryDate = new Date(expiryDateMillis);
        }

        RefreshToken refreshToken = tokens.getRefreshToken();
        String refreshTokenValue = null;
        if (refreshToken != null) {
            refreshTokenValue = refreshToken.getValue();
        }

        return new OAuthTokens(tokens.getAccessToken().getValue(), expiryDate, refreshTokenValue);
    }

}

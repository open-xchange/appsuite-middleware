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

    protected final OAuthAuthenticationConfig config;

    protected OAuthRequestIssuer(OAuthAuthenticationConfig config) {
        super();
        this.config = config;
    }

    /**
     * Get the client authentication based on the config 
     *
     * @return The {@link ClientAuthentication}
     */
    protected ClientAuthentication getClientAuthentication() {
        ClientID clientID = new ClientID(config.getClientId());
        Secret clientSecret = new Secret(config.getClientSecret());
        return new ClientSecretBasic(clientID, clientSecret);
    }

    /**
     * Converts tokens to OAuth tokens 
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

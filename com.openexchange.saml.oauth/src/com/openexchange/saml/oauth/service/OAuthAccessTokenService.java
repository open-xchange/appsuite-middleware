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

import com.openexchange.exception.OXException;

/**
 * {@link OAuthAccessTokenService} - A service to obtain an access token from user-sensitive token end-point.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.4
 */
public interface OAuthAccessTokenService {

    /**
     * {@link OAuthGrantType} describes possible grant types for {@link OAuthAccessTokenService#getAccessToken(OAuthGrantType, String, int, int)}
     */
    public enum OAuthGrantType {
        /**
         * The SAML assertions as authorization grant
         */
        SAML,

        /**
         * The refresh token grant
         */
        REFRESH_TOKEN,

        ;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Retrieves an appropriate {@link OAuthAccessToken} for specified grant type.
     *
     * @param type The request type
     * @param data The data needed for the corresponding {@link OAuthGrantType}. E.g. a SAML response for {@link OAuthGrantType.SAML} or a refresh token for {@link OAuthGrantType.REFRESH_TOKEN}
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param scope An optional scope
     * @return The {@link OAuthAccessToken access token}
     * @throws OXException If the token couldn't be retrieved.
     */
    OAuthAccessToken getAccessToken(OAuthGrantType type, String data, int userId, int contextId, String scope) throws OXException;

    /**
     * Checks whether OAuth is configured for the given user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if it is configured, <code>false</code> otherwise
     * @throws OXException If test for OAuth availability fails
     */
    boolean isConfigured(int userId, int contextId) throws OXException;

}

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

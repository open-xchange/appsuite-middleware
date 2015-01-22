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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.oauth.provider;

import com.openexchange.exception.OXException;

/**
 * {@link OAuthProviderService} - The OAuth provider service in addition to <a href="http://oauth.googlecode.com/">Google's OAuth Java
 * library</a>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface OAuthProviderService extends OAuthProviderConstants {

    /**
     *
     * @param accessToken
     * @return
     * @throws OXException
     */
    OAuthToken validate(String accessToken) throws OXException;

    Client getClient(OAuthToken token) throws OXException;

    Client getClientByID(String clientID) throws OXException;

    // ------------------------------------------------------------------------------------------------------

    String generateToken(int contextId, int userId, Scope scope);

    String generateAuthToken(int contextId, int userId);

    /**
     * Validates given client identifier
     *
     * @param clientId The client identifier
     * @return <code>true</code> if client identifier is valid/known; otherwise <code>false</code>
     * @throws OXException If operation fails
     */
    boolean validateClientId(String clientId) throws OXException;

    /**
     * Validates given client identifier and redirect URI pair
     *
     * @param clientId The client identifier
     * @param redirectUri The redirect URI
     * @return <code>true</code> if given client identifier and redirect URI pair is invalid; e.g. client identifier unknown or redirect URI mismatch; otherwise <code>false</code>
     * @throws OXException If operation fails
     */
    boolean validateRedirectUri(String clientId, String redirectUri) throws OXException;

    /**
     * Validates given scope string if it might be invalid, unknown, or malformed.
     *
     * @param scope The scope to check
     * @return A valid {@link Scope} instance if valid; otherwise <code>null</code> if invalid, unknown, or malformed
     * @throws OXException If operation fails
     */
    Scope validateScope(String scope) throws OXException;

}

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

package com.openexchange.oauth;

import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.session.Session;

/**
 * {@link OAuthService} - The OAuth service.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@SingletonService
public interface OAuthService {

    /**
     * Gets the meta data registry.
     *
     * @return The meta data registry
     */
    OAuthServiceMetaDataRegistry getMetaDataRegistry();

    /**
     * Gets all accounts belonging to specified user.
     *
     * @param session The session
     * @param user The user identifier
     * @param contextId The context identifier
     * @throws OXException If accounts cannot be returned
     * @return The accounts
     */
    List<OAuthAccount> getAccounts(Session session, int user, int contextId) throws OXException;

    /**
     * Gets all accounts belonging to specified user with given service identifier.
     *
     * @param serviceMetaData The identifier of service meta data
     * @param session The session
     * @param user The user identifier
     * @param contextId The context identifier
     * @throws OXException If accounts cannot be returned
     * @return The accounts
     */
    List<OAuthAccount> getAccounts(String serviceMetaData, Session session, int user, int contextId) throws OXException;

    /**
     * Initializes a new OAuth account.
     *
     * @param serviceMetaData The identifier of service meta data
     * @param callbackUrl The optional call-back URL
     * @param currentHost The name of this host
     * @throws OXException If initialization fails
     * @return The OAuth interaction providing needed steps
     */
    OAuthInteraction initOAuth(String serviceMetaData, String callbackUrl, String currentHost, Session session) throws OXException;

    /**
     * Creates a new OAuth account completely from specified arguments.
     *
     * @param serviceMetaData The identifier of service meta data
     * @param arguments The arguments providing {@link OAuthConstants#ARGUMENT_TOKEN}, {@link OAuthConstants#ARGUMENT_SECRET}, {@link OAuthConstants#ARGUMENT_PASSWORD}, and optional {@link OAuthConstants#ARGUMENT_DISPLAY_NAME}
     * @param user The user identifier
     * @param contextId The context identifier
     * @return The newly created account
     * @throws OXException If creation fails
     */
    OAuthAccount createAccount(String serviceMetaData, Map<String, Object> arguments, int user, int contextId) throws OXException;

    /**
     * Create a new OAuth account from specified arguments.
     * <p>
     * The arguments should provide:
     * <ul>
     * <li>display name; {@link OAuthConstants#ARGUMENT_DISPLAY_NAME}</li>
     * <li>pin; {@link OAuthConstants#ARGUMENT_PIN}</li>
     * <li>request token; {@link OAuthConstants#ARGUMENT_REQUEST_TOKEN}</li>
     * <li>user password; {@link OAuthConstants#ARGUMENT_PASSWORD}</li>
     * </ul>
     *
     * @param serviceMetaData The identifier of service meta data
     * @param type The interaction type
     * @param arguments The arguments appropriate for interaction type
     * @param user The user identifier
     * @param contextId The context identifier
     * @throws OXException If creation fails
     * @return The newly created account
     */
    OAuthAccount createAccount(String serviceMetaData, OAuthInteractionType type, Map<String, Object> arguments, int user, int contextId) throws OXException;

    /**
     * Deletes the specified account.
     *
     * @param accountId The account identifier
     * @param user The user identifier
     * @param contextId The context identifier
     * @throws OXException If deletion fails
     */
    void deleteAccount(int accountId, int user, int contextId) throws OXException;

    /**
     * Update the specified account.
     * <p>
     * The arguments may provide:
     * <ul>
     * <li>display name; {@link OAuthConstants#ARGUMENT_DISPLAY_NAME}</li>
     * <li>request token; {@link OAuthConstants#ARGUMENT_REQUEST_TOKEN}</li>
     * <li>user password is <b>mandatory</b> if request token shall be updated; {@link OAuthConstants#ARGUMENT_PASSWORD}</li>
     * </ul>
     *
     * @param accountId The account identifier
     * @param arguments The arguments to update
     * @param user The user identifier
     * @param contextId The context identifier
     * @throws OXException If deletion fails
     */
    void updateAccount(int accountId, Map<String, Object> arguments, int user, int contextId) throws OXException;

    OAuthAccount updateAccount(int accountId, String serviceMetaData, OAuthInteractionType type, Map<String, Object> arguments, int user, int contextId) throws OXException;

    /**
     * Gets the specified account.
     *
     * @param accountId The account identifier
     * @param session The session
     * @param user The user identifier
     * @param contextId The context identifier
     * @throws OXException If account cannot be returned
     * @return The account
     */
    OAuthAccount getAccount(int accountId, Session session, int user, int contextId) throws OXException;

    /**
     * Gets a default account for the given API type. Throws an exception if no account for the API can be found.
     * @param api The API type
     * @param session The session
     * @return The default account for this API type
     * @throws OXException
     */
	OAuthAccount getDefaultAccount(API api, Session session) throws OXException;

}

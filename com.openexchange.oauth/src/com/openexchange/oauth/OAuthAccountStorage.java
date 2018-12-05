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
import com.openexchange.session.Session;

/**
 * {@link OAuthAccountStorage}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public interface OAuthAccountStorage {
    
    /**
     * Stores the specified {@link OAuthAccount} in the storage
     * 
     * @param session The {@link Session}
     * @param account The {@link OAuthAccount} to store
     * @return The identifier of the stored account
     * @throws OXException if the account cannot be stored
     */
    int storeAccount(Session session, OAuthAccount account) throws OXException;

    /**
     * Gets the specified account.
     *
     * @param session The session
     * @param accountId The account identifier
     * @return The account
     * @throws OXException If account does not exist, or if any other error is occurred
     */
    OAuthAccount getAccount(Session session, int accountId) throws OXException;

    /**
     * Deletes the specified account.
     * @param session The session
     * @param accountId The account identifier
     *
     * @throws OXException If deletion fails
     */
    void deleteAccount(Session session, int accountId) throws OXException;

    /**
     * Updates the specified account
     *
     * @param session The {@link Session}
     * @param account the {@link OAuthAccount} to update
     * @throws OXException if the update fails
     */
    void updateAccount(Session session, OAuthAccount account) throws OXException;

    /**
     * Update the specified account.
     * <p>
     * The arguments may provide:
     * <ul>
     * <li>display name; {@link OAuthConstants#ARGUMENT_DISPLAY_NAME}</li>
     * <li>request token; {@link OAuthConstants#ARGUMENT_REQUEST_TOKEN}</li>
     * <li>enabled scopes; {@link OAuthConstants#ARGUMENT_SCOPES}</li>
     * <li>user password is <b>mandatory</b> if request token shall be updated; {@link OAuthConstants#ARGUMENT_PASSWORD}</li>
     * </ul>
     * @param session The session
     * @param accountId The account identifier
     * @param arguments The arguments to update
     *
     * @throws OXException If update fails
     */
    void updateAccount(Session session, int accountId, Map<String, Object> arguments) throws OXException;
    
    /**
     * Searches for an {@link OAuthAccount} with the specified user identity for the specified provider
     * 
     * @param session the {@link Session}
     * @param userIdentity The user identity
     * @param serviceId The service provider id
     * @return The {@link OAuthAccount} or <code>null</code> if no account is found
     * @throws OXException if an error is occurred
     */
    OAuthAccount findByUserIdentity(Session session, String userIdentity, String serviceId) throws OXException;

    /**
     * Returns <code>true</code> if the specified account of the specified provider has a user identity
     * 
     * @param session The {@link Session}
     * @param accountId The account identifier
     * @param serviceId The service identifier
     * @return <code>true</code> if the specified account of the specified provider has a user identity;
     *         <code>false</code> otherwise
     * @throws OXException if an error is occurred
     */
    boolean hasUserIdentity(Session session, int accountId, String serviceId) throws OXException;

    /**
     * Gets all accounts belonging to specified user.
     * 
     * @param session The {@link Session}
     * @return A {@link List} with all {@link OAuthAccount}s, or an empty {@link List}
     * @throws OXException if the {@link OAuthAccount}s cannot be returned
     */
    List<OAuthAccount> getAccounts(Session session) throws OXException;

    /**
     * Gets all accounts belonging to specified user with given service identifier.
     * 
     * @param session The {@link Session}
     * @param serviceMetaData The identifier of service meta data
     * @return A {@link List} with all {@link OAuthAccount}s, or an empty {@link List}
     * @throws OXException if the {@link OAuthAccount}s cannot be returned
     */
    List<OAuthAccount> getAccounts(Session session, String serviceMetaData) throws OXException;

}

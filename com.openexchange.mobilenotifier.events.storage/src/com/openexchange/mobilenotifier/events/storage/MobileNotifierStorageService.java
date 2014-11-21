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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.mobilenotifier.events.storage;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.mobilenotifier.MobileNotifierProviders;
import com.openexchange.session.Session;

/**
 * {@link MobileNotifierStorageService}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public interface MobileNotifierStorageService {
    /**
     * Creates an subscription
     *
     * @param session - The session
     * @param token - The device token
     * @param serviceId The serviceId of the push service e.g. gcm, apn, apn.macos
     * @param provider - The provider
     * @return the subscription
     */
    Subscription createSubscription(Session session, String token, String serviceId, MobileNotifierProviders provider) throws OXException;

    /**
     * Updates a token to a new token
     *
     * @param session - The session
     * @param token - The device token
     * @param serviceId - The serviceId of the push service e.g. gcm, apn, apn.macos
     * @param newToken - The new device token
     * @return <code>true</true> if the subscriptions could be successfully updated; <code>false</code> if not.
     * @throws OXException
     */
    boolean updateToken(Session session, String token, String serviceId, String newToken) throws OXException;

    /**
     * Updates a token to a new token
     *
     * @param contextId - The contextId
     * @param token - The old device token
     * @param serviceId - The serviceId of the push service e.g. gcm, apn, apn.macos
     * @param newToken - The new device token
     * @return <code>true</true> if the subscriptions could be successfully updated; <code>false</code> if not.
     */
    boolean updateToken(int contextId, String token, String serviceId, String newToken) throws OXException;

    /**
     * Deletes a subscription by the userId, token, serviceId and provider.
     *
     * @param session - The session
     * @param token - The device token
     * @param serviceId - The serviceId of the push service e.g. gcm, apn, apn.macos
     * @param provider - The provider
     * @return <code>true</true> if the subscriptions could be successfully removed; <code>false</code> if not.
     */
    boolean deleteSubscription(Session session, String token, String serviceId, MobileNotifierProviders provider) throws OXException;

    /**
     * Deletes a subscription by the userId, token, serviceId and provider.
     *
     * @param session - The session
     * @param token - The hardware token provided by the push provider
     * @param serviceId - The serviceId of the push service e.g. gcm, apn, apn.macos
     * @param provider - The provider
     * @return <code>true</true> if the subscriptions could be successfully removed; <code>false</code> if not.
     */
    boolean deleteSubscription(int contextId, String token, String serviceId, MobileNotifierProviders provider) throws OXException;

    /**
     * Deletes a subscription by the userId, token and serviceId.
     *
     * @param contextId - The context id
     * @param token - The hardware token provided by the push provider
     * @param serviceId - The serviceId of the push service e.g. gcm, apn, apn.macos
     * @return <code>true</true> if the subscriptions could be successfully removed; <code>false</code> if not.
     */
    boolean deleteSubscriptions(int contextId, String token, String serviceId) throws OXException;

    /**
     * Gets all subscriptions of the specified contextId, userId, serviceId and provider.
     *
     * @param userId The userId
     * @param contextId The contextId
     * @param serviceId The serviceId of the push service e.g. gcm, apn, apn.macos
     * @param provider The provider
     * @return a list of subscription
     */
    List<Subscription> getSubscription(int userId, int contextId, String serviceId, MobileNotifierProviders provider) throws OXException;

    /**
     * Gets all subscriptions for the specified provider
     *
     * @param provider The provider
     * @return A list of context / userIds of the specified provider
     * @throws OXException
     */
    List<ContextUsers> getAllSubscriptions(MobileNotifierProviders provider) throws OXException;

    /**
     * Gets all tokens for a provider from the subscription table for a list of context users.
     *
     * @param ContextUser a list of context / userIds
     * @param serviceId The serviceId of the push service e.g. gcm, apn, apn.macos
     * @param provider The provider
     * @return List of strings which contains the tokens
     * @throws OXException
     */
    List<String> getTokensFromSubscriptions(List<ContextUsers> contextUsers,  String serviceId, MobileNotifierProviders provider) throws OXException;

    /**
     * Updates the last login timestamp. The timestamp should be updated whenever the user is informed to login again.
     *
     * @param contextId the context id
     * @param userId the userId
     * @param serviceId The serviceId of the push service e.g. gcm, apn, apn.macos
     * @return <code>true</code> if the timestamp is successfully updated; <code>false</code> if not
     * @throws OXException
     */
    boolean updateLastLoginPush(int contextId, int userId, String serviceId) throws OXException;

    /**
     * @param contextUser
     * @param token
     * @param serviceId
     * @param newToken
     * @return
     * @throws OXException
     */
    boolean updateToken(List<ContextUsers> contextUser, String token, String serviceId, String newToken) throws OXException;

    /**
     * @param contextUsers
     * @param token
     * @param serviceId
     * @return
     * @throws OXException
     */
    boolean deleteSubscriptions(List<ContextUsers> contextUsers, String token, String serviceId) throws OXException;
}

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
     * Updates a token to a new token. All provider containing this token will be updated.
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
    boolean deleteSubscription(int contextId, String token, String serviceId) throws OXException;

    /**
     * Gets subscriptions of the specified contextId, userId, serviceId and provider.
     *
     * @param userId The userId
     * @param contextId The contextId
     * @param serviceId The serviceId of the push service e.g. gcm, apn, apn.macos
     * @param provider The provider
     * @return a list of subscription
     */
    List<Subscription> getSubscriptions(int userId, int contextId, String serviceId, MobileNotifierProviders provider) throws OXException;

    /**
     * Gets all subscriptions for the specified provider
     *
     * @param provider - The provider
     * @param isLoginPush <code>true</code> rejects a new push to if the time in database
     * field <code>blockLoginPushUntil</code> is not reached; <code>false</code> if field should not be evaluated.
     * @return list of ContextUsers
     * @throws OXException
     */
    List<ContextUsers> getSubscriptions(MobileNotifierProviders provider, boolean isLoginPush) throws OXException;

    /**
     * Updates the blockLoginPushUntil timestamp in the database. The user will get no push notification until the limit is reached.
     *
     * @param contextId the context id
     * @param userId the userId
     * @param serviceId The serviceId of the push service e.g. gcm, apn, apn.macos
     * @return <code>true</code> if the timestamp is successfully updated; <code>false</code> if not
     * @throws OXException
     */
    boolean blockLoginPush(List<ContextUsers> contextUser, long blockLoginPushUntil) throws OXException;

    /**
     * Gets a list of tokens for the specified list of context users, serviceId an provider
     *
     * @param contextUser
     * @param serviceId
     * @param provider
     * @return a list of tokens for the specified list of context users.
     * @throws OXException
     */
    List<String> getTokens(List<ContextUsers> contextUser, String serviceId, MobileNotifierProviders provider) throws OXException;
}

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

package com.openexchange.subscribe;

import java.util.Collection;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public interface SubscribeService {

    /**
     * Retrieves the {@link SubscriptionSource}
     *
     * @return
     */
    public SubscriptionSource getSubscriptionSource();

    /**
     * Checks whether this service handles subscriptions for the given module
     *
     * @param folderModule The module
     * @return true if it handles subscriptions for the given module, false otherwise
     */
    public boolean handles(int folderModule);

    /**
     * Add a new subscription
     *
     * @param subscription The subscription
     * @throws OXException
     */
    public void subscribe(Subscription subscription) throws OXException;

    /**
     * Gets all subscriptions within the given folder
     *
     * @param context The context
     * @param folderId The folder id
     * @param secret The secret
     * @return A collection of {@link Subscription}s
     * @throws OXException
     */
    public Collection<Subscription> loadSubscriptions(Context context, String folderId, String secret) throws OXException;

    /**
     * Gets all subscriptions for a given user
     *
     * @param context The context
     * @param userId The user id
     * @param secret The secret
     * @return A collection of {@link Subscription}s
     * @throws OXException
     */
    public Collection<Subscription> loadSubscriptions(Context context, int userId, String secret) throws OXException;

    /**
     * Gets a specific {@link Subscription}
     *
     * @param context The context
     * @param subscriptionId The id of the {@link Subscription}
     * @param secret The secret
     * @return The {@link Subscription}
     * @throws OXException
     */
    public Subscription loadSubscription(Context context, int subscriptionId, String secret) throws OXException;

    /**
     * Removes a {@link Subscription}
     *
     * @param subscription The {@link Subscription} to remove
     * @throws OXException
     */
    public void unsubscribe(Subscription subscription) throws OXException;

    /**
     * Updates a {@link Subscription}
     *
     * @param subscription The {@link Subscription} to update
     * @throws OXException
     */
    public void update(Subscription subscription) throws OXException;

    /**
     * Gets the content of the {@link Subscription}
     *
     * @param subscription The {@link Subscription}
     * @return A content collections. The type of the content depends on the {@link Subscription}
     * @throws OXException
     */
    public Collection<?> getContent(Subscription subscription) throws OXException;

    /**
     * Loads the contents of this subscription.
     *
     * @param subscription The subscription to load
     * @return A search iterator providing the subscription's content
     * @throws OXException
     */
    SearchIterator<?> loadContent(Subscription subscription) throws OXException;

    /**
     * Checks if a given subscription id is known to the service
     *
     * @param context The context
     * @param subscriptionId The id of the {@link Subscription}
     * @return true if it is known, false otherwise
     * @throws OXException
     */
    public boolean knows(Context context, int subscriptionId) throws OXException;

    /**
     * Migrates a new secret
     *
     * @param session The user session
     * @param oldSecret The old secret
     * @param newSecret The new secret
     * @throws OXException
     */
    public void migrateSecret(Session session, String oldSecret, String newSecret) throws OXException;

    /**
     * Checks if a given user has accounts
     *
     * @param context The context
     * @param user The user
     * @return true if the given user has an account for this service
     * @throws OXException
     */
    public boolean hasAccounts(Context context, User user) throws OXException;

    /**
     * Touches a subscription.
     *
     * @param context The context
     * @param subscriptionId The id of the subscription
     * @throws OXException
     */
    public void touch(Context context, int subscriptionId) throws OXException;

    /**
     * Cleans-up accounts that could no more be decrypted with given secret
     *
     * @param secret The current secret
     * @param session The session providing user information
     * @throws OXException If operation fails
     */
    public void cleanUp(String secret, Session session) throws OXException;

    /**
     * Removes unrecoverable items
     *
     * @param secret The secret
     * @param session The user session
     * @throws OXException
     */
    public void removeUnrecoverableItems(String secret, Session session) throws OXException;

    /**
     * Gets a value indicating whether creating new or modifying existing subscriptions is enabled or not.
     *
     * @return <code>true</code> if enabled, <code>false</code>, otherwise
     */
    boolean isCreateModifyEnabled();

}

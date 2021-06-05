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

package com.openexchange.subscribe;

import java.util.Collection;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.user.User;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public interface SubscribeService {

    /**
     * Retrieves the {@link SubscriptionSource}
     *
     * @return
     */
    SubscriptionSource getSubscriptionSource();

    /**
     * Checks whether this service handles subscriptions for the given module
     *
     * @param folderModule The module
     * @return true if it handles subscriptions for the given module, false otherwise
     */
    boolean handles(int folderModule);

    /**
     * Add a new subscription
     *
     * @param subscription The subscription
     * @throws OXException
     */
    void subscribe(Subscription subscription) throws OXException;

    /**
     * Gets all subscriptions within the given folder
     *
     * @param context The context
     * @param folderId The folder id
     * @param secret The secret
     * @return A collection of {@link Subscription}s
     * @throws OXException
     */
    Collection<Subscription> loadSubscriptions(Context context, String folderId, String secret) throws OXException;

    /**
     * Gets all subscriptions for a given user
     *
     * @param context The context
     * @param userId The user id
     * @param secret The secret
     * @return A collection of {@link Subscription}s
     * @throws OXException
     */
    Collection<Subscription> loadSubscriptions(Context context, int userId, String secret) throws OXException;

    /**
     * Gets a specific {@link Subscription}
     *
     * @param context The context
     * @param subscriptionId The id of the {@link Subscription}
     * @param secret The secret
     * @return The {@link Subscription}
     * @throws OXException
     */
    Subscription loadSubscription(Context context, int subscriptionId, String secret) throws OXException;

    /**
     * Removes a {@link Subscription}
     *
     * @param subscription The {@link Subscription} to remove
     * @throws OXException
     */
    void unsubscribe(Subscription subscription) throws OXException;

    /**
     * Updates a {@link Subscription}
     *
     * @param subscription The {@link Subscription} to update
     * @throws OXException
     */
    void update(Subscription subscription) throws OXException;

    /**
     * Gets the content of the {@link Subscription}
     *
     * @param subscription The {@link Subscription}
     * @return A content collections. The type of the content depends on the {@link Subscription}
     * @throws OXException
     */
    Collection<?> getContent(Subscription subscription) throws OXException;

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
    boolean knows(Context context, int subscriptionId) throws OXException;

    /**
     * Migrates a new secret
     *
     * @param session The user session
     * @param oldSecret The old secret
     * @param newSecret The new secret
     * @throws OXException
     */
    void migrateSecret(Session session, String oldSecret, String newSecret) throws OXException;

    /**
     * Checks if a given user has accounts
     *
     * @param context The context
     * @param user The user
     * @return true if the given user has an account for this service
     * @throws OXException
     */
    boolean hasAccounts(Context context, User user) throws OXException;

    /**
     * Touches a subscription.
     *
     * @param context The context
     * @param subscriptionId The id of the subscription
     * @throws OXException
     */
    void touch(Context context, int subscriptionId) throws OXException;

    /**
     * Cleans-up accounts that could no more be decrypted with given secret
     *
     * @param secret The current secret
     * @param session The session providing user information
     * @throws OXException If operation fails
     */
    void cleanUp(String secret, Session session) throws OXException;

    /**
     * Removes unrecoverable items
     *
     * @param secret The secret
     * @param session The user session
     * @throws OXException
     */
    void removeUnrecoverableItems(String secret, Session session) throws OXException;

    /**
     * Gets a value indicating whether creating new or modifying existing subscriptions is enabled or not.
     *
     * @return <code>true</code> if enabled, <code>false</code>, otherwise
     */
    boolean isCreateModifyEnabled();

    /**
     * Checks whether this service is enabled or not.
     *
     * @param session The session providing user data
     * @return <code>true</code> if enabled, <code>false</code>, otherwise
     * @throws OXException If check fails
     */
    default boolean isEnabled(Session session) throws OXException {
        if (session == null) {
            throw new IllegalArgumentException("Session must ot be null");
        }
        return isEnabled(session.getUserId(), session.getContextId());
    }

    /**
     * Checks whether this service is enabled for given user or not.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if enabled, <code>false</code>, otherwise
     * @throws OXException If check fails
     */
    default boolean isEnabled(int userId, int contextId) throws OXException {
        // Enabled by default
        return true;
    }

}

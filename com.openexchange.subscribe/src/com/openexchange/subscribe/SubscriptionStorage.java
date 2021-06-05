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

import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.secret.SecretEncryptionStrategy;
import com.openexchange.user.User;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public interface SubscriptionStorage extends SecretEncryptionStrategy<EncryptedField> {

    /**
     * Remembers a {@link Subscription}
     *
     * @param subscription The {@link Subscription} to remember
     * @throws OXException
     */
    public void rememberSubscription(Subscription subscription) throws OXException;

    /**
     * Forgets a {@link Subscription}
     *
     * @param subscription The {@link Subscription} to forget
     * @throws OXException
     */
    public void forgetSubscription(Subscription subscription) throws OXException;

    /**
     * Gets all {@link Subscription}s within a given folder
     *
     * @param ctx The {@link Context}
     * @param folderId The folder id
     * @return A list of {@link Subscription}s
     * @throws OXException
     */
    public List<Subscription> getSubscriptions(Context ctx, String folderId) throws OXException;

    /**
     * Gets a {@link Subscription}
     *
     * @param ctx The {@link Context}
     * @param id The id of the {@link Subscription}
     * @return The {@link Subscription}
     * @throws OXException
     */
    public Subscription getSubscription(Context ctx, int id) throws OXException;

    /**
     * Gets all {@link Subscription}s for a given user
     *
     * @param ctx The {@link Context}
     * @param userId The user id
     * @return A list of {@link Subscription}s
     * @throws OXException
     */
    public List<Subscription> getSubscriptionsOfUser(Context ctx, int userId) throws OXException;

    /**
     * Gets all {@link Subscription}s of a given source for a given user
     *
     * @param ctx The {@link Context}
     * @param userId The user id
     * @param sourceId The source id
     * @return A list of {@link Subscription}s
     * @throws OXException
     */
    public List<Subscription> getSubscriptionsOfUser(Context ctx, int userId, String sourceId) throws OXException;

    /**
     * Updates a {@link Subscription}
     *
     * @param subscription The {@link Subscription} to update
     * @throws OXException
     */
    public void updateSubscription(Subscription subscription) throws OXException;

    /**
     * Deletes all {@link Subscription}s for a given user
     * 
     * @param userId The user id
     * @param ctx The {@link Context}
     * @throws OXException
     */
    public void deleteAllSubscriptionsForUser(int userId, Context ctx) throws OXException;

    /**
     * Deletes all {@link Subscription}s for a given context
     * 
     * @param contextId The context id
     * @param ctx The {@link Context}
     * @throws OXException
     */
    public void deleteAllSubscriptionsInContext(int contextId, Context ctx) throws OXException;

    /**
     * Deletes all {@link Subscription}s which match a given config
     *
     * @param query The config
     * @param sourceId The source id
     * @param ctx The {@link Context}
     * @throws OXException
     */
    public void deleteAllSubscriptionsWhereConfigMatches(Map<String, Object> query, String sourceId, Context ctx) throws OXException;

    /**
     * Checks if the given folders contain {@link Subscription}s or not
     * 
     * @param ctx The context
     * @param folderIds A list of folder ids
     * @return A map with a folder id to boolean mapping.
     * @throws OXException
     */
    public Map<String, Boolean> hasSubscriptions(Context ctx, List<String> folderIds) throws OXException;

    /**
     * Checks if the given user has subscriptions or not.
     *
     * @param ctx The {@link Context}
     * @param user The {@link User}
     * @return true if the user has a {@link Subscription}, false otherwise
     * @throws OXException
     */
    public boolean hasSubscriptions(Context ctx, User user) throws OXException;

    /**
     * Touches a subscription
     *
     * @param ctx The {@link Context}
     * @param subscriptionId The subscription id
     * @param currentTimeMillis The current time in milliseconds
     * @throws OXException
     */
    public void touch(Context ctx, int subscriptionId, long currentTimeMillis) throws OXException;
}

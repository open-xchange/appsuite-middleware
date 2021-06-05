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

import java.sql.Connection;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;

/**
 * {@link AdministrativeSubscriptionStorage}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.0
 */
public interface AdministrativeSubscriptionStorage extends SubscriptionStorage {

    /**
     * Returns all {@link Subscription}s for the specified context
     *
     * @param ctx The {@link Context}
     * @return all {@link Subscription}s for the specified context
     * @throws OXException if an error is occurred
     */
    List<Subscription> getSubscriptionsForContext(Context ctx) throws OXException;

    /**
     * Returns all {@link Subscription}s for the sspecified context
     *
     * @param ctx The {@link Context}
     * @param sourceId The source id a.k.a. the provider id
     * @return all {@link Subscription}s for the specified context
     * @throws OXException if an error is occurred
     */
    List<Subscription> getSubscriptionsForContextAndProvider(Context ctx, String sourceId) throws OXException;

    /**
     * Gets all {@link Subscription}s of a given source for a given context
     *
     * @param ctx The {@link Context}
     * @param sourceId The source id
     * @param con The connection to use
     * @return A list of {@link Subscription}s
     * @throws OXException if an error is occurred
     */
    List<Subscription> getSubscriptionsForContext(Context ctx, String sourceId, Connection con) throws OXException;

    /**
     * Removes the specified subscription
     *
     * @param ctx The {@link Context}
     * @param userId the user identifier
     * @param id The subscription identifier
     * @return <code>true</code> if the subscription was successfully deleted; <code>false</code> otherwise
     * @throws OXException if an error is occurred
     */
    boolean deleteSubscription(Context ctx, int userId, int id) throws OXException;

    /**
     * Removes the specified subscription
     *
     * @param ctx The {@link Context}
     * @param userId the user identifier
     * @param id The subscription identifier
     * @param connection The writable connection
     * @return <code>true</code> if the subscription was successfully deleted; <code>false</code> otherwise
     * @throws OXException if an error is occurred
     */
    boolean deleteSubscription(Context ctx, int userId, int id, Connection connection) throws OXException;

    /**
     * Deletes all {@link Subscription}s for the given user
     * 
     * @param userId The user id
     * @param ctx The {@link Context}
     * @param connection The writable connection
     * @throws OXException if an error is occurred
     */
    void deleteAllSubscriptionsForUser(int userId, Context ctx, Connection connection) throws OXException;
}

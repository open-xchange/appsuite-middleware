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

package com.openexchange.pns;

import com.openexchange.exception.OXException;

/**
 * {@link PushSubscriptionProvider} - Provides subscriptions not held in storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public interface PushSubscriptionProvider {

    /**
     * Gets all subscriptions interested in specified topic belonging to certain users.
     *
     * @param userIds The user identifiers to get the subscriptions for
     * @param contextId The context identifier
     * @param topic The topic
     * @return All matching subscriptions for specified topic
     * @throws OXException If interested subscriptions cannot be returned
     */
    Hits getInterestedSubscriptions(String client, int[] userIds, int contextId, String topic) throws OXException;

    /**
     * Gets all subscriptions interested in specified topic belonging to certain users.
     *
     * @param userIds The user identifiers to get the subscriptions for
     * @param contextId The context identifier
     * @param topic The topic
     * @return All matching subscriptions for specified topic
     * @throws OXException If interested subscriptions cannot be returned
     */
    Hits getInterestedSubscriptions(int[] userIds, int contextId, String topic) throws OXException;

    // ------------------------------------------------------------------------------------------------------------------

    /**
     * Checks if there is any subscription interested in specified topic belonging to given user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param topic The topic
     * @return <code>true</code> if there is such a subscription; otherwise <code>false</code>
     * @throws OXException If interested subscriptions cannot be checked
     */
    boolean hasInterestedSubscriptions(int userId, int contextId, String topic) throws OXException;

    /**
     * Checks if there is any subscription interested in specified topic belonging to given client of specified user.
     *
     * @param client The client identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param topic The topic
     * @return <code>true</code> if there is such a subscription; otherwise <code>false</code>
     * @throws OXException If interested subscriptions cannot be checked
     */
    boolean hasInterestedSubscriptions(String client, int userId, int contextId, String topic) throws OXException;

}

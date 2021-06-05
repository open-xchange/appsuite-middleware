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

package com.openexchange.pns.subscription.storage.rdb.cache;

import java.util.List;
import java.util.concurrent.Callable;
import com.openexchange.exception.OXException;
import com.openexchange.pns.PushSubscription;
import com.openexchange.pns.subscription.storage.rdb.RdbPushSubscriptionRegistry;

/**
 * {@link LoadInMemoryPushSubscriptionCollectionCallable}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class LoadInMemoryPushSubscriptionCollectionCallable implements Callable<CachedPushSubscriptionCollection> {

    private final int userId;
    private final int contextId;
    private final RdbPushSubscriptionRegistry registry;

    /**
     * Initializes a new {@link LoadInMemoryPushSubscriptionCollectionCallable}.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param registry The registry to use
     */
    public LoadInMemoryPushSubscriptionCollectionCallable(int userId, int contextId, RdbPushSubscriptionRegistry registry) {
        super();
        this.userId = userId;
        this.contextId = contextId;
        this.registry = registry;
    }

    @Override
    public CachedPushSubscriptionCollection call() throws OXException {
        return loadCollectionFor(userId, contextId, registry);
    }

    /**
     * Loads the collection for specified user using given database-backed registry.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param registry The database-backed registry
     * @return The collection
     * @throws OXException If collection cannot be loaded
     */
    public static CachedPushSubscriptionCollection loadCollectionFor(int userId, int contextId, RdbPushSubscriptionRegistry registry) throws OXException {
        List<PushSubscription> subscriptions = registry.loadSubscriptionsFor(userId, contextId);
        CachedPushSubscriptionCollection collection = new CachedPushSubscriptionCollection(userId, contextId);
        collection.addSubscription(subscriptions);
        return collection;
    }

}

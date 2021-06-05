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

import static com.openexchange.java.Autoboxing.I;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.openexchange.caching.CacheService;
import com.openexchange.caching.events.CacheEvent;
import com.openexchange.caching.events.CacheEventService;
import com.openexchange.exception.OXException;
import com.openexchange.pns.PushSubscription;
import com.openexchange.pns.subscription.storage.rdb.RdbPushSubscriptionRegistry;
import com.openexchange.session.UserAndContext;
import com.openexchange.threadpool.ThreadPools;

/**
 * {@link RdbPushSubscriptionRegistryCache}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class RdbPushSubscriptionRegistryCache {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(RdbPushSubscriptionRegistryCache.class);

    /** The name of the cache region: <code>"RdbPushSubscriptionRegistry"</code> */
    public static final String REGION = RdbPushSubscriptionRegistry.class.getSimpleName();

    // ----------------------------------------------------------------------------------------------------------------

    private final CacheEventService cacheEventService;
    private final CacheService cacheService;
    private final LoadingCache<Integer, LoadingCache<UserAndContext, CachedPushSubscriptionCollection>> cache;

    /**
     * Initializes a new {@link RdbPushSubscriptionRegistryCache}.
     */
    public RdbPushSubscriptionRegistryCache(final RdbPushSubscriptionRegistry registry, CacheEventService cacheEventService, CacheService cacheService) {
        super();
        this.cacheEventService = cacheEventService;
        this.cacheService = cacheService;

        final CacheLoader<UserAndContext, CachedPushSubscriptionCollection> collectionLoader = new CacheLoader<UserAndContext, CachedPushSubscriptionCollection>() {

            @Override
            public CachedPushSubscriptionCollection load(UserAndContext userAndContext) throws Exception {
                return LoadInMemoryPushSubscriptionCollectionCallable.loadCollectionFor(userAndContext.getUserId(), userAndContext.getContextId(), registry);
            }
        };

        CacheLoader<Integer, LoadingCache<UserAndContext, CachedPushSubscriptionCollection>> userCacheLoader = new CacheLoader<Integer, LoadingCache<UserAndContext, CachedPushSubscriptionCollection>>() {

            @Override
            public LoadingCache<UserAndContext, CachedPushSubscriptionCollection> load(Integer contextId) throws Exception {
                return CacheBuilder.newBuilder().initialCapacity(16).expireAfterAccess(30, TimeUnit.MINUTES).build(collectionLoader);
            }
        };

        cache = CacheBuilder.newBuilder().initialCapacity(512).expireAfterAccess(30, TimeUnit.MINUTES).build(userCacheLoader);
    }

    /**
     * Clears all user-associated collections from this cache
     *
     * @param notify Whether to notify
     */
    public void clear(boolean notify) {
        cache.invalidateAll();
        if (notify) {
            fireInvalidateCacheEvent(0, 0);
        }
    }

    /**
     * Gets the in-memory collection for given user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The user-associated in-memory collection
     * @throws OXException If collection cannot be created
     */
    public CachedPushSubscriptionCollection getCollectionFor(int userId, int contextId) throws OXException {
        try {
            LoadingCache<UserAndContext, CachedPushSubscriptionCollection> userCache = cache.get(I(contextId));
            return userCache.get(UserAndContext.newInstance(userId, contextId));
        } catch (ExecutionException e) {
            throw ThreadPools.launderThrowable(e, OXException.class);
        }
    }

    /**
     * Adds specified subscription to appropriate collection (if any available).
     *
     * @param subscription The subscription to add
     */
    public void addAndInvalidateIfPresent(PushSubscription subscription) {
        int contextId = subscription.getContextId();
        LoadingCache<UserAndContext, CachedPushSubscriptionCollection> userCache = cache.getIfPresent(I(contextId));
        if (null == userCache) {
            return;
        }

        int userId = subscription.getUserId();
        CachedPushSubscriptionCollection collection = userCache.getIfPresent(UserAndContext.newInstance(userId, contextId));
        if (null == collection) {
            return;
        }

        collection.addSubscription(subscription);
        fireInvalidateCacheEvent(userId, contextId);
    }

    /**
     * Removes specified subscription from appropriate collection (if any available).
     *
     * @param subscription The subscription to remove
     */
    public void removeAndInvalidateIfPresent(PushSubscription subscription) {
        int contextId = subscription.getContextId();
        LoadingCache<UserAndContext, CachedPushSubscriptionCollection> userCache = cache.getIfPresent(I(contextId));
        if (null == userCache) {
            return;
        }

        int userId = subscription.getUserId();
        CachedPushSubscriptionCollection collection = userCache.getIfPresent(UserAndContext.newInstance(userId, contextId));
        if (null == collection) {
            return;
        }

        collection.removeSubscription(subscription);
        fireInvalidateCacheEvent(userId, contextId);
    }

    /**
     * Drops collection for given user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    public void dropFor(int userId, int contextId) {
        dropFor(userId, contextId, true);
    }

    /**
     * Drops collection for given user/context.
     *
     * @param userId The user identifier or <code>0</code> (zero) to drop for whole context
     * @param contextId The context identifier
     * @param notify Whether to notify
     */
    public void dropFor(int userId, int contextId, boolean notify) {
        if (contextId <= 0) {
            // Clear all
            cache.invalidateAll();
        } else {
            if (userId <= 0) {
                // Drop for whole context
                cache.invalidate(I(contextId));
            } else {
                LoadingCache<UserAndContext, CachedPushSubscriptionCollection> userCache = cache.getIfPresent(I(contextId));
                if (null != userCache) {
                    userCache.invalidate(UserAndContext.newInstance(userId, contextId));
                }
            }
        }

        if (notify) {
            fireInvalidateCacheEvent(userId, contextId);
        }
        LOG.debug("Cleaned user-sensitive subscription cache for user {} in context {}", Integer.valueOf(userId), Integer.valueOf(contextId));
    }

    private void fireInvalidateCacheEvent(int userId, int contextId) {
        if (null != cacheEventService) {
            CacheEvent event = newCacheEventFor(userId, contextId);
            if (null != event) {
                cacheEventService.notify(this, event, false);
            }
        }
    }

    /**
     * Creates a new cache event
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The cache event
     */
    private CacheEvent newCacheEventFor(int userId, int contextId) {
        CacheService service = cacheService;
        return null == service ? null : CacheEvent.INVALIDATE(REGION, Integer.toString(contextId), service.newCacheKey(contextId, userId));
    }

}

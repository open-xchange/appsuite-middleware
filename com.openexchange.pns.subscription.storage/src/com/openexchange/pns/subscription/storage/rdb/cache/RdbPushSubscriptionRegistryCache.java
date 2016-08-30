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

package com.openexchange.pns.subscription.storage.rdb.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import org.slf4j.Logger;
import com.openexchange.caching.CacheService;
import com.openexchange.caching.events.CacheEvent;
import com.openexchange.caching.events.CacheEventService;
import com.openexchange.exception.OXException;
import com.openexchange.pns.PushSubscription;
import com.openexchange.pns.subscription.storage.rdb.RdbPushSubscriptionRegistry;
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

    private final RdbPushSubscriptionRegistry registry;
    private final CacheEventService cacheEventService;
    private final CacheService cacheService;
    private final ConcurrentMap<Integer, ConcurrentMap<Integer, Future<InMemoryPushSubscriptionCollection>>> cache;

    /**
     * Initializes a new {@link RdbPushSubscriptionRegistryCache}.
     */
    public RdbPushSubscriptionRegistryCache(RdbPushSubscriptionRegistry registry, CacheEventService cacheEventService, CacheService cacheService) {
        super();
        this.registry = registry;
        this.cacheEventService = cacheEventService;
        this.cacheService = cacheService;
        cache = new ConcurrentHashMap<>(512);
    }

    /**
     * Clears all user-associated collections from this cache
     *
     * @param notify Whether to notify
     */
    public void clear(boolean notify) {
        cache.clear();
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
    public InMemoryPushSubscriptionCollection getCollectionFor(int userId, int contextId) throws OXException {
        ConcurrentMap<Integer, Future<InMemoryPushSubscriptionCollection>> userCache = cache.get(contextId);
        if (null == userCache) {
            ConcurrentMap<Integer, Future<InMemoryPushSubscriptionCollection>> newUserCache = new ConcurrentHashMap<>();
            userCache = cache.putIfAbsent(contextId, newUserCache);
            if (null == userCache) {
                userCache = newUserCache;
            }
        }

        Future<InMemoryPushSubscriptionCollection> f = userCache.get(userId);
        if (null == f) {
            FutureTask<InMemoryPushSubscriptionCollection> ft = new FutureTask<>(new LoadInMemoryPushSubscriptionCollectionCallable(userId, contextId, registry));
            f = userCache.putIfAbsent(userId, ft);
            if (null == f) {
                f = ft;
                ft.run();
            }
        }
        return ThreadPools.getFrom(f);
    }

    /**
     * Adds specified subscription to appropriate collection (if any available).
     *
     * @param subscription The subscription to add
     * @throws OXException If add attempt fails
     */
    public void addAndInvalidateIfPresent(PushSubscription subscription) throws OXException {
        int contextId = subscription.getContextId();
        ConcurrentMap<Integer, Future<InMemoryPushSubscriptionCollection>> userCache = cache.get(contextId);
        if (null == userCache) {
            return;
        }

        int userId = subscription.getUserId();
        Future<InMemoryPushSubscriptionCollection> f = userCache.get(userId);
        if (null == f) {
            return;
        }

        InMemoryPushSubscriptionCollection collection = ThreadPools.getFrom(f);
        collection.addSubscription(subscription);
        fireInvalidateCacheEvent(userId, contextId);
    }

    /**
     * Removes specified subscription from appropriate collection (if any available).
     *
     * @param subscription The subscription to remove
     * @throws OXException If remove attempt fails
     */
    public void removeAndInvalidateIfPresent(PushSubscription subscription) throws OXException {
        int contextId = subscription.getContextId();
        ConcurrentMap<Integer, Future<InMemoryPushSubscriptionCollection>> userCache = cache.get(contextId);
        if (null == userCache) {
            return;
        }

        int userId = subscription.getUserId();
        Future<InMemoryPushSubscriptionCollection> f = userCache.get(userId);
        if (null == f) {
            return;
        }

        InMemoryPushSubscriptionCollection collection = ThreadPools.getFrom(f);
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
            cache.clear();
        } else {
            if (userId <= 0) {
                // Drop for whole context
                cache.remove(contextId);
            } else {
                ConcurrentMap<Integer, Future<InMemoryPushSubscriptionCollection>> userCache = cache.get(contextId);
                if (null != userCache) {
                    userCache.remove(userId);
                }
            }
        }

        if (notify) {
            fireInvalidateCacheEvent(userId, contextId);
        }
        LOG.debug("Cleaned user-sensitive subscription cache for user {} in context {}", Integer.valueOf(userId), Integer.valueOf(contextId));
    }

    private void fireInvalidateCacheEvent(int userId, int contextId) {
        if (null != cacheEventService && cacheEventService.getConfiguration().remoteInvalidationForPersonalFolders()) {
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

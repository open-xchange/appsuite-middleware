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

package com.openexchange.groupware.userconfiguration;

import java.sql.Connection;
import java.util.concurrent.locks.Lock;
import com.openexchange.cache.registry.CacheAvailabilityListener;
import com.openexchange.cache.registry.CacheAvailabilityRegistry;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheService;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.lock.LockService;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.user.User;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;


/**
 * {@inheritDoc}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CachingUserPermissionBitsStorage extends UserPermissionBitsStorage {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CachingUserConfigurationStorage.class);

    private static final String CACHE_REGION_NAME = "UserPermissionBits";

    private final CacheAvailabilityListener cacheAvailabilityListener;

    private transient final UserPermissionBitsStorage delegateStorage;

    private volatile Cache cache;

    private volatile UserPermissionBitsStorage fallback;

    /**
     * Initializes a new {@link CachingUserPermissionBitsStorage}.
     * @param capabilities
     *
     * @throws OXException If an error occurs
     */
    public CachingUserPermissionBitsStorage() throws OXException {
        super();
        this.delegateStorage = new RdbUserPermissionBitsStorage();
        cacheAvailabilityListener = new CacheAvailabilityListener() {

            @Override
            public void handleAbsence() throws OXException {
                releaseCache();
            }

            @Override
            public void handleAvailability() throws OXException {
                initCache();
            }
        };
        initCache();
    }

    private UserPermissionBitsStorage getFallback() {
        UserPermissionBitsStorage fallback = this.fallback;
        if (null == fallback) {
            synchronized (this) {
                fallback = this.fallback;
                if (null == fallback) {
                    fallback = new RdbUserPermissionBitsStorage();
                    this.fallback = fallback;
                }
            }
        }
        return fallback;
    }

    @Override
    protected void startInternal() throws OXException {
        CacheAvailabilityRegistry reg = CacheAvailabilityRegistry.getInstance();
        if (null != reg && !reg.registerListener(cacheAvailabilityListener)) {
            LOG.error("Cache availability listener could not be registered", new Throwable());
        }
    }

    @Override
    protected void stopInternal() throws OXException {
        CacheAvailabilityRegistry reg = CacheAvailabilityRegistry.getInstance();
        if (null != reg) {
            reg.unregisterListener(cacheAvailabilityListener);
        }
        releaseCache();
    }

    private final static CacheKey getKey(int userId, Context ctx, Cache cache) {
        return cache.newCacheKey(ctx.getContextId(), userId);
    }

    private final static CacheKey getKey(int userId, int contextId, Cache cache) {
        return cache.newCacheKey(contextId, userId);
    }

    /**
     * Initializes cache reference
     *
     * @throws OXException If an error occurs
     */
    void initCache() throws OXException {
        Cache cache = this.cache;
        if (null == cache) {
            synchronized (this) {
                cache = this.cache;
                if (null == cache) {
                    try {
                        this.cache = ServerServiceRegistry.getInstance().getService(CacheService.class).getCache(CACHE_REGION_NAME);
                    } catch (RuntimeException e) {
                        throw UserConfigurationCodes.CACHE_INITIALIZATION_FAILED.create(e, CACHE_REGION_NAME);
                    }
                }
            }
        }
    }

    /**
     * Releases cache reference
     *
     * @throws OXException If an error occurs
     */
    void releaseCache() throws OXException {
        Cache cache = this.cache;
        if (cache == null) {
            return;
        }
        try {
            cache.clear();
            CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
            if (null != cacheService) {
                cacheService.freeCache(CACHE_REGION_NAME);
            }
        } catch (RuntimeException e) {
            throw UserConfigurationCodes.CACHE_INITIALIZATION_FAILED.create(e, CACHE_REGION_NAME);
        } finally {
            this.cache = null;
        }
    }

    @Override
    public UserPermissionBits getUserPermissionBits(int userId, int contextId) throws OXException {
        Cache cache = this.cache;
        if (cache == null) {
            return getFallback().getUserPermissionBits(userId, contextId);
        }
        return get(cache, contextId, userId);
    }

    @Override
    public UserPermissionBits getUserPermissionBits(int userId, Context ctx) throws OXException {
        Cache cache = this.cache;
        if (cache == null) {
            return getFallback().getUserPermissionBits(userId, ctx);
        }
        return get(cache, ctx, userId);
    }

    @Override
    public UserPermissionBits getUserPermissionBits(Connection con, int userId, Context ctx) throws OXException {
        final Cache cache = this.cache;
        if (cache == null) {
            return getFallback().getUserPermissionBits(con, userId, ctx);
        }
        return get(con, cache, ctx, userId);
    }

    @Override
    public UserPermissionBits[] getUserPermissionBits(Context ctx, User[] users) throws OXException {
        if (null == users || 0 == users.length) {
            return new UserPermissionBits[0];
        }
        Cache cache = this.cache;
        if (cache == null) {
            return getFallback().getUserPermissionBits(ctx, users);
        }
        int[] userIds = new int[users.length];
        for (int i = 0; i < users.length; i++) {
            userIds[i] = users[i].getId();
        }

        return get(cache, ctx, userIds);
    }

    @Override
    public UserPermissionBits[] getUserPermissionBits(Context ctx, int[] userIds) throws OXException {
        if (null == userIds || 0 == userIds.length) {
            return new UserPermissionBits[0];
        }
        Cache cache = this.cache;
        if (cache == null) {
            return getFallback().getUserPermissionBits(ctx, userIds);
        }
        return get(cache, ctx, userIds);
    }

    @Override
    public void clearStorage() throws OXException {
        Cache cache = this.cache;
        if (cache == null) {
            return;
        }
        try {
            cache.clear();
        } catch (RuntimeException rte) {
            LOG.warn("A runtime error occurred.", rte);
        }
    }

    @Override
    public void removeUserPermissionBits(int userId, Context ctx) throws OXException {
        Cache cache = this.cache;
        if (cache == null) {
            return;
        }
        cache.remove(getKey(userId, ctx, cache));
    }

    @Override
    public void saveUserPermissionBits(int permissionBits, int userId, Context ctx) throws OXException {
        delegateStorage.saveUserPermissionBits(permissionBits, userId, ctx);
        removeUserPermissionBits(userId, ctx);
    }

    @Override
    public void saveUserPermissionBits(Connection con, int permissionBits, int userId, Context ctx) throws OXException {
        delegateStorage.saveUserPermissionBits(con, permissionBits, userId, ctx);
        removeUserPermissionBits(userId, ctx);
    }

    private UserPermissionBits get(Cache cache, int contextId, int userId) throws OXException {
        CacheKey key = getKey(userId, contextId, cache);
        Object object = cache.get(key);
        if (object != null) {
            return ((UserPermissionBits) object).clone();
        }

        LockService lockService = ServerServiceRegistry.getInstance().getService(LockService.class);
        Lock lock = null == lockService ? LockService.EMPTY_LOCK : lockService.getSelfCleaningLockFor(new StringBuilder(32).append("UserPermissionBits-").append(contextId).append('-').append(userId).toString());
        lock.lock();
        try {
            object = cache.get(key);
            if (object != null) {
                return ((UserPermissionBits) object).clone();
            }

            ContextService contextService = ServerServiceRegistry.getInstance().getService(ContextService.class);
            if (null == contextService) {
                throw ServiceExceptionCode.absentService(ContextService.class);
            }
            return load(cache, contextService.getContext(contextId), userId);
        } finally {
            lock.unlock();
        }
    }

    private UserPermissionBits get(Cache cache, Context ctx, int userId) throws OXException {
        CacheKey key = getKey(userId, ctx, cache);
        Object object = cache.get(key);
        if (object != null) {
            return ((UserPermissionBits) object).clone();
        }

        LockService lockService = ServerServiceRegistry.getInstance().getService(LockService.class);
        Lock lock = null == lockService ? LockService.EMPTY_LOCK : lockService.getSelfCleaningLockFor(new StringBuilder(32).append("UserPermissionBits-").append(ctx.getContextId()).append('-').append(userId).toString());
        lock.lock();
        try {
            object = cache.get(key);
            if (object != null) {
                return ((UserPermissionBits) object).clone();
            }

            return load(cache, ctx, userId);
        } finally {
            lock.unlock();
        }
    }

    private UserPermissionBits get(final Connection con, final Cache cache, final Context ctx, final int userId) throws OXException {
        final CacheKey key = getKey(userId, ctx, cache);
        final Object object = cache.get(key);
        if (object != null) {
            return ((UserPermissionBits) object).clone();
        }

        return load(con, cache, ctx, userId);
    }

    private UserPermissionBits load(Cache cache, Context ctx, int userId) throws OXException {
        UserPermissionBits perm = delegateStorage.getUserPermissionBits(userId, ctx);
        cache.put(getKey(userId, ctx, cache), perm.clone(), false);
        return perm;
    }

    private UserPermissionBits load(final Connection con, final Cache cache, final Context ctx, final int userId) throws OXException {
        final UserPermissionBits perm = delegateStorage.getUserPermissionBits(con, userId, ctx);
        cache.put(getKey(userId, ctx, cache), perm.clone(), false);
        return perm;
    }

    private UserPermissionBits[] get(Cache cache, Context ctx, int[] userIds) throws OXException {
        if (userIds.length == 0) {
            return new UserPermissionBits[0];
        }

        TIntObjectMap<UserPermissionBits> map = new TIntObjectHashMap<UserPermissionBits>(userIds.length);
        TIntList toLoad = new TIntArrayList(userIds.length);
        for (int id : userIds) {
            CacheKey key = getKey(id, ctx, cache);
            Object object = cache.get(key);
            if (object == null) {
                if (false == toLoad.contains(id)) {
                    toLoad.add(id);
                }
            } else {
                map.put(id, ((UserPermissionBits) object).clone());
            }
        }

        if (!toLoad.isEmpty()) {
            for (UserPermissionBits bits : load(cache, ctx, toLoad)) {
                if (null != bits) {
                    map.put(bits.getUserId(), bits);
                }
            }
        }

        UserPermissionBits[] retval = new UserPermissionBits[userIds.length];
        for (int i = 0; i < userIds.length; i++) {
            retval[i] = map.get(userIds[i]);
        }
        return retval;
    }

    private UserPermissionBits[] load(Cache cache, Context ctx, TIntList userIds) throws OXException {
        UserPermissionBits[] perms = delegateStorage.getUserPermissionBits(ctx, userIds.toArray());
        for (UserPermissionBits bits : perms) {
            if (null != bits) {
                cache.put(getKey(bits.getUserId(), ctx, cache), bits.clone(), false);
            }
        }
        return perms;
    }

}

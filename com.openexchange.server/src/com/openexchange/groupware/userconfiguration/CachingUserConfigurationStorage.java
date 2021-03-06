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

import static com.openexchange.java.Autoboxing.I;
import java.util.ArrayList;
import java.util.List;
import com.openexchange.cache.registry.CacheAvailabilityListener;
import com.openexchange.cache.registry.CacheAvailabilityRegistry;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.user.User;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * {@link CachingUserConfigurationStorage} - A cache-based implementation of {@link UserConfigurationStorage} with a fallback to
 * {@link CapabilityUserConfigurationStorage}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class CachingUserConfigurationStorage extends UserConfigurationStorage {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CachingUserConfigurationStorage.class);

    private static final String CACHE_REGION_NAME = "UserConfiguration";

    private final CacheAvailabilityListener cacheAvailabilityListener;

    private transient final UserConfigurationStorage delegateStorage;

    private volatile Cache cache;

    private volatile UserConfigurationStorage fallback;

    /**
     * Initializes a new {@link CachingUserConfigurationStorage}.
     * @param capabilities
     *
     * @throws OXException If an error occurs
     */
    public CachingUserConfigurationStorage() throws OXException {
        super();
        this.delegateStorage = new CapabilityUserConfigurationStorage();
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

    private UserConfigurationStorage getFallback() {
        UserConfigurationStorage fallback = this.fallback;
        if (null == fallback) {
            synchronized (this) {
                fallback = this.fallback;
                if (null == fallback) {
                    fallback = new CapabilityUserConfigurationStorage();
                    this.fallback = fallback;
                }
            }
        }
        return fallback;
    }

    @Override
    protected void startInternal() throws OXException {
        final CacheAvailabilityRegistry reg = CacheAvailabilityRegistry.getInstance();
        if (null != reg && !reg.registerListener(cacheAvailabilityListener)) {
            LOG.error("Cache availability listener could not be registered", new Throwable());
        }
    }

    @Override
    protected void stopInternal() throws OXException {
        final CacheAvailabilityRegistry reg = CacheAvailabilityRegistry.getInstance();
        if (null != reg) {
            reg.unregisterListener(cacheAvailabilityListener);
        }
        releaseCache();
    }

    private final static CacheKey getKey(final int userId, final Context ctx, final Cache cache) {
        return cache.newCacheKey(ctx.getContextId(), userId);
    }

    private static final CacheKey getKey(Cache cache, Context ctx, int userId, boolean extendedPermissions) {
        return cache.newCacheKey(ctx.getContextId(), String.valueOf(userId), String.valueOf(extendedPermissions));
    }

    private final static CacheKey getKey(final int userId, final int contextId, final Cache cache) {
        return cache.newCacheKey(contextId, userId);
    }

    private static final CacheKey getKey(Cache cache, final int contextId, int userId, boolean extendedPermissions) {
        return cache.newCacheKey(contextId, String.valueOf(userId), String.valueOf(extendedPermissions));
    }

    /**
     * Initializes cache reference
     *
     * @throws OXException If an error occurs
     */
    void initCache() throws OXException {
        final Cache cache = this.cache;
        if (cache != null) {
            return;
        }
        try {
            this.cache = ServerServiceRegistry.getInstance().getService(CacheService.class).getCache(CACHE_REGION_NAME);
        } catch (RuntimeException e) {
            throw UserConfigurationCodes.CACHE_INITIALIZATION_FAILED.create(e, CACHE_REGION_NAME);
        }
    }

    /**
     * Releases cache reference
     *
     * @throws OXException If an error occurs
     */
    void releaseCache() throws OXException {
        final Cache cache = this.cache;
        if (cache == null) {
            return;
        }
        try {
            cache.clear();
            final CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
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
    public UserConfiguration getUserConfiguration(Session session) throws OXException {
        return getUserConfiguration(session, null);
    }

    @Override
    public UserConfiguration getUserConfiguration(Session session, int[] groups) throws OXException {
        int[] grps = null != groups && 0 != groups.length ? groups : UserStorage.getInstance().getUser(session.getUserId(), session.getContextId()).getGroups();
        Cache cache = this.cache;
        if (cache == null) {
            return getFallback().getUserConfiguration(session, grps);
        }

        return getUserConfiguration(cache, session, grps);
    }

    @Override
    public UserConfiguration getUserConfiguration(int userId, int[] groups, Context ctx) throws OXException {
        final int[] grps = null != groups && 0 != groups.length ? groups : UserStorage.getInstance().getUser(userId, ctx).getGroups();
        final Cache cache = this.cache;
        if (cache == null) {
            return getFallback().getUserConfiguration(userId, grps, ctx);
        }
        return getUserConfigurations(cache, ctx, new int[] { userId }, new int[][] { grps })[0];
    }

    @Override
    public UserConfiguration[] getUserConfiguration(Context ctx, User[] users) throws OXException {
        final Cache cache = this.cache;
        if (cache == null) {
            return getFallback().getUserConfiguration(ctx, users);
        }

        // Getting the groups from all users is an expensive operation because the Refresher of UserReloader gets synchronized on the cache
        // lock for large contexts. So we can not convert User[] to userIds[] and groups[] and use
        // method {@link #getUserConfiguration(Cache, Context, int[], int[][])}.
        int[] userIds = new int[users.length];
        for (int i = 0; i < users.length; i++) {
            userIds[i] = users[i].getId();
        }
        TIntObjectMap<UserConfiguration> map = getCachedUserConfiguration(cache, ctx, userIds, true);

        TIntList toLoad = new TIntArrayList(users.length - map.size());
        List<int[]> groupsToLoad = new ArrayList<int[]>(users.length - map.size());
        for (User user : users) {
            if (!map.containsKey(user.getId())) {
                toLoad.add(user.getId());
                groupsToLoad.add(user.getGroups());
            }
        }
        loadUserConfiguration(cache, map, ctx, toLoad.toArray(), groupsToLoad.toArray(new int[groupsToLoad.size()][]));
        return convert(map, userIds);
    }

    @Override
    public UserConfiguration[] getUserConfigurations(Context ctx, int[] userIds, int[][] groups) throws OXException {
        final Cache cache = this.cache;
        if (cache == null) {
            return getFallback().getUserConfigurations(ctx, userIds, groups);
        }
        return getUserConfigurations(cache, ctx, userIds, groups);
    }

    private static TIntObjectMap<UserConfiguration> getCachedUserConfiguration(Cache cache, Context ctx, int[] userIds, boolean extendedPermissions) {
        TIntObjectMap<UserConfiguration> map = new TIntObjectHashMap<UserConfiguration>(userIds.length, 1);
        for (int i = 0; i < userIds.length; i++) {
            CacheKey key;
            if (extendedPermissions) {
                key = getKey(userIds[i], ctx, cache);
            } else {
                key = getKey(cache, ctx, userIds[i], false);
            }
            UserConfiguration userConfig = (UserConfiguration) cache.get(key);
            if (null != userConfig) {
                map.put(userIds[i], userConfig.clone());
            }
        }
        return map;
    }

    private static UserConfiguration getCachedUserConfiguration(Cache cache, Session session, boolean extendedPermissions) {
        CacheKey key;
        if (extendedPermissions) {
            key = getKey(session.getUserId(), session.getContextId(), cache);
        } else {
            key = getKey(cache, session.getContextId(), session.getUserId(), false);
        }
        return (UserConfiguration) cache.get(key);
    }

    private static UserConfiguration[] convert(TIntObjectMap<UserConfiguration> map, int[] userIds) {
        List<UserConfiguration> retval = new ArrayList<UserConfiguration>(map.size());
        for (int userId : userIds) {
            final UserConfiguration userConfiguration = map.get(userId);
            if (null != userConfiguration) {
                retval.add(userConfiguration.clone());
            }
        }
        return retval.toArray(new UserConfiguration[map.size()]);
    }

    /**
     * Loads a {@link UserConfiguration} without initializing the extended permissions. Initialization of extended permissions needs the
     * ConfigCascade which itself needs again a {@link UserConfiguration} without extended permissions.
     * This method should cache those {@link UserConfiguration}s without extended permissions otherwise loading the {@link UserConfiguration}
     * with extended permissions does not scale well. See https://bugs.open-xchange.com/show_bug.cgi?id=25162#c4.
     */
    private UserConfiguration getUserConfiguration(Cache cache, Session session, int[] groups) throws OXException {
        UserConfiguration userConfiguration = getCachedUserConfiguration(cache, session, false);
        if (null == userConfiguration) {
            userConfiguration = delegateStorage.getUserConfiguration(session, groups);
            try {
                cache.put(getKey(cache, session.getContextId(), session.getUserId(), false), userConfiguration, false);
            } catch (RuntimeException e) {
                LOG.warn("Failed to add user configuration for context {} and user {} to cache.", Integer.valueOf(session.getContextId()), Integer.valueOf(session.getUserId()), e);
            }
        }
        return userConfiguration.clone();
    }

    /**
     * Loads a {@link UserConfiguration} without initializing the extended permissions. Initialization of extended permissions needs the
     * ConfigCascade which itself needs again a {@link UserConfiguration} without extended permissions.
     * This method should cache those {@link UserConfiguration}s without extended permissions otherwise loading the {@link UserConfiguration}
     * with extended permissions does not scale well. See https://bugs.open-xchange.com/show_bug.cgi?id=25162#c4.
     */
    private UserConfiguration[] getUserConfigurations(Cache cache, Context ctx, int[] userIds, int[][] groups) throws OXException {
        TIntObjectMap<UserConfiguration> map = getCachedUserConfiguration(cache, ctx, userIds, false);
        TIntList toLoad = new TIntArrayList(userIds.length - map.size());
        List<int[]> groupsToLoad = new ArrayList<int[]>(userIds.length - map.size());
        for (int i = 0; i < userIds.length; i++) {
            if (!map.containsKey(userIds[i])) {
                toLoad.add(userIds[i]);
                groupsToLoad.add(groups[i]);
            }
        }
        loadUserConfiguration(cache, map, ctx, toLoad.toArray(), groupsToLoad.toArray(new int[groupsToLoad.size()][]));
        return convert(map, userIds);
    }

    private void loadUserConfiguration(Cache cache, TIntObjectMap<UserConfiguration> map, Context ctx, int[] userIds, int[][] groups) throws OXException {
        if (null == userIds || 0 == userIds.length) {
            return;
        }
        final UserConfiguration[] loaded;
        loaded = delegateStorage.getUserConfigurations(ctx, userIds, groups);
        if (null == loaded) {
            return;
        }
        for (UserConfiguration userConfig : loaded) {
            int userId = userConfig.getUserId();
            CacheKey key;
            key = getKey(cache, ctx, userId, false);
            try {
                cache.put(key, userConfig, false);
            } catch (RuntimeException e) {
                LOG.warn("Failed to add user configuration for context {} and user {} to cache.", Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId), e);
            }
            map.put(userId, userConfig.clone());
        }
    }

    @Override
    public void clearStorage() throws OXException {
        final Cache cache = this.cache;
        if (cache == null) {
            return;
        }
        try {
            cache.clear();
        } catch (RuntimeException rte) {
            /*
             * Swallow
             */
            LOG.warn("A runtime error occurred.", rte);
        }
    }

    @Override
    public void invalidateCache(final int userId, final Context ctx) throws OXException {
        final Cache cache = this.cache;
        if (cache == null) {
            return;
        }
        CacheKey key = getKey(userId, ctx, cache);
        CacheKey keyWithoutExtended = getKey(cache, ctx, userId, false);
        try {
            cache.remove(key);
            cache.remove(keyWithoutExtended);
        } catch (RuntimeException e) {
            LOG.warn("Failed to remove user configuration for context {} and user {} to cache.", I(ctx.getContextId()), I(userId), e);
        }
        UserPermissionBitsStorage.getInstance().removeUserPermissionBits(userId, ctx);
        /*
         * invalidate capabilities caches, too (yes, ugly)
         */
        CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
        if (null != cacheService) {
            try {
                cacheService.getCache("Capabilities").removeFromGroup(I(userId), String.valueOf(ctx.getContextId()));
            } catch (Exception e) {
                LOG.warn("Error invalidating \"Capabilities\" cache for user {} in context {}: {}",
                    Integer.valueOf(userId), Integer.valueOf(ctx.getContextId()), e.getMessage(), e);
            }
            try {
                cacheService.getCache("CapabilitiesUser").removeFromGroup(I(userId), String.valueOf(ctx.getContextId()));
            } catch (Exception e) {
                LOG.warn("Error invalidating \"CapabilitiesUser\" cache for user {} in context {}: {}",
                    Integer.valueOf(userId), Integer.valueOf(ctx.getContextId()), e.getMessage(), e);
            }
        }
    }
}

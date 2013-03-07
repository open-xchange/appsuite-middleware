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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.groupware.userconfiguration;

import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.I;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import com.openexchange.cache.registry.CacheAvailabilityListener;
import com.openexchange.cache.registry.CacheAvailabilityRegistry;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link CachingUserConfigurationStorage} - A cache-based implementation of {@link UserConfigurationStorage} with a fallback to
 * {@link RdbUserConfigurationStorage}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class CachingUserConfigurationStorage extends UserConfigurationStorage {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(CachingUserConfigurationStorage.class));

    private static final String CACHE_REGION_NAME = "UserConfiguration";

    private final CacheAvailabilityListener cacheAvailabilityListener;

    private transient final UserConfigurationStorage delegateStorage;

    private final Lock cacheWriteLock;

    private volatile Cache cache;

    private volatile UserConfigurationStorage fallback;

    /**
     * Initializes a new {@link CachingUserConfigurationStorage}.
     *
     * @throws OXException If an error occurs
     */
    public CachingUserConfigurationStorage() throws OXException {
        super();
        cacheWriteLock = new ReentrantLock();
        this.delegateStorage = new RdbUserConfigurationStorage();
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
                    fallback = new RdbUserConfigurationStorage();
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
        return cache.newCacheKey(ctx.getContextId(), I(userId), B(extendedPermissions));
    }

    /**
     * Initializes cache reference
     *
     * @throws OXException If an error occurs
     */
    void initCache() throws OXException {
        if (cache != null) {
            return;
        }
        try {
            cache = ServerServiceRegistry.getInstance().getService(CacheService.class).getCache(CACHE_REGION_NAME);
        } catch (final RuntimeException e) {
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
        } catch (final RuntimeException e) {
            throw UserConfigurationCodes.CACHE_INITIALIZATION_FAILED.create(e, CACHE_REGION_NAME);
        } finally {
            this.cache = null;
        }
    }

    /* The ConfigCascade adds an additional layer to the UserConfiguration. It is possible to modify the permissions through the
     * ConfigCascade while the ConfigCascade itself needs the permissions from the database. To release this the ConfigCascade loads the
     * UserConfiguration without initializing the extended permissions (initExtendedPermissions == false). Those loaded UserConfigurations
     * have not been put into cache, because without extended permissions the UserConfiguration gives false answers.
     * Unfortunately this does not scale out, so we have to cache UserConfigurations without extended permissions. Otherwise we have always
     * an access to the database here.
     */
    @Override
    public UserConfiguration getUserConfiguration(final int userId, final int[] groups, final Context ctx, final boolean initExtendedPermissions) throws OXException {
        final Cache cache = this.cache;
        if (cache == null) {
            return getFallback().getUserConfiguration(userId, groups, ctx, initExtendedPermissions);
        }
        final UserConfiguration userConfig;
        if (initExtendedPermissions) {
            userConfig = getUserConfiguration(cache, ctx, userId, groups);
        } else {
            userConfig = getUserConfigurationWithoutExtended(cache, ctx, userId, groups);
        }
        return userConfig;
    }

    /* @see com.openexchange.groupware.userconfiguration.CachingUserConfigurationStorage.getUserConfiguration(int, int[], Context, boolean)
     */
    @Override
    public UserConfiguration[] getUserConfiguration(Context ctx, User[] users) throws OXException {
        final Cache cache = this.cache;
        if (cache == null) {
            return getFallback().getUserConfiguration(ctx, users);
        }
        int[] userIds = new int[users.length];
        int[][] groups = new int[users.length][];
        for (int i = 0; i < users.length; i++) {
            userIds[i] = users[i].getId();
            groups[i] = users[i].getGroups();
        }
        return getUserConfiguration(cache, ctx, userIds, groups);
    }

    @Override
    UserConfiguration[] getUserConfigurationWithoutExtended(Context ctx, int[] userIds, int[][] groups) throws OXException {
        final Cache cache = this.cache;
        if (cache == null) {
            return getFallback().getUserConfigurationWithoutExtended(ctx, userIds, groups);
        }
        return getUserConfigurationWithoutExtended(cache, ctx, userIds, groups);
    }

    /**
     * Convenience method for calling the single array style implementation.
     */
    private UserConfiguration getUserConfiguration(Cache cache, Context ctx, int userId, int[] groups) throws OXException {
        return getUserConfiguration(cache, ctx, new int[] { userId }, new int[][] { groups })[0];
    }

    /**
     * Convenience method for calling the single array style implementation.
     */
    private UserConfiguration getUserConfigurationWithoutExtended(Cache cache, Context ctx, int userId, int[] groups) throws OXException {
        return getUserConfigurationWithoutExtended(cache, ctx, new int[] { userId }, new int[][] { groups })[0];
    }

    /**
     * Loads a {@link UserConfiguration} without initializing the extended permissions. Initialization of extended permissions needs the
     * ConfigCascade which itself needs again a {@link UserConfiguration} without extended permissions.
     * This method should cache those {@link UserConfiguration}s without extended permissions otherwise loading the {@link UserConfiguration}
     * with extended permissions does not scale well. See https://bugs.open-xchange.com/show_bug.cgi?id=25162#c4.
     */
    private UserConfiguration[] getUserConfigurationWithoutExtended(Cache cache, Context ctx, int[] userIds, int[][] groups) throws OXException {
        TIntObjectMap<UserConfiguration> map = new TIntObjectHashMap<UserConfiguration>(userIds.length, 1);
        TIntList toLoad = new TIntArrayList(userIds.length);
        List<int[]> groupsToLoad = new ArrayList<int[]>(userIds.length);
        for (int i = 0; i < userIds.length; i++) {
            UserConfiguration userConfig = (UserConfiguration) cache.get(getKey(cache, ctx, userIds[i], false));
            if (null == userConfig) {
                toLoad.add(userIds[i]);
                groupsToLoad.add(groups[i]);
            } else {
                map.put(userIds[i], userConfig.clone());
            }
        }
        final UserConfiguration[] loaded;
        if (0 == toLoad.size()) {
            loaded = new UserConfiguration[0];
        } else {
            loaded = delegateStorage.getUserConfigurationWithoutExtended(ctx, toLoad.toArray(), groupsToLoad.toArray(new int[groupsToLoad.size()][]));
        }
        for (UserConfiguration userConfig : loaded) {
            int userId = userConfig.getUserId();
            CacheKey key = getKey(cache, ctx, userId, false);
            cacheWriteLock.lock();
            try {
                cache.put(key, userConfig, false);
            } catch (RuntimeException e) {
                LOG.warn("Failed to add user configuration for context " + ctx.getContextId() + " and user " + userId + " to cache.", e);
            } finally {
                cacheWriteLock.unlock();
            }
            map.put(userId, userConfig.clone());
        }
        List<UserConfiguration> retval = new ArrayList<UserConfiguration>(userIds.length);
        for (int userId : userIds) {
            retval.add(map.get(userId));
        }
        return retval.toArray(new UserConfiguration[retval.size()]);
    }

    /**
     * This method uses the {@link UserConfiguration} cached without extended permissions and adds to them the extended permissions.
     * Afterwards puts the fully initialized {@link UserConfiguration} into the normal cache.
     */
    private UserConfiguration[] getUserConfiguration(Cache cache, Context ctx, int[] userIds, int[][] groups) throws OXException {
        TIntObjectMap<UserConfiguration> map = new TIntObjectHashMap<UserConfiguration>(userIds.length, 1);
        TIntList toLoad = new TIntArrayList(userIds.length);
        List<int[]> groupsToLoad = new ArrayList<int[]>(userIds.length);
        for (int i = 0; i < userIds.length; i++) {
            UserConfiguration userConfig = (UserConfiguration) cache.get(getKey(userIds[i], ctx, cache));
            if (null == userConfig) {
                toLoad.add(userIds[i]);
                groupsToLoad.add(groups[i]);
            } else {
                map.put(userIds[i], userConfig.clone());
            }
        }
        // Load UserConfigurations without extended permissions and cache them.
        final UserConfiguration[] loaded;
        if (0 == toLoad.size()) {
            loaded = new UserConfiguration[0];
        } else {
            loaded = getUserConfigurationWithoutExtended(cache, ctx, toLoad.toArray(), groupsToLoad.toArray(new int[groupsToLoad.size()][]));
        }
        for (UserConfiguration userConfig : loaded) {
            int userId = userConfig.getUserId();
            // Calculate extended permissions. Reading UserConfiguration by ConfigCascade will be fast now, because UserConfigurations
            // without extended permissions are already cached.
            userConfig.setExtendedPermissions(userConfig.calcExtendedPermissions());
            CacheKey key = getKey(userId, ctx, cache);
            cacheWriteLock.lock();
            try {
                cache.put(key, userConfig, false);
            } catch (RuntimeException e) {
                LOG.warn("Failed to add user configuration for context " + ctx.getContextId() + " and user " + userId + " to cache.", e);
            } finally {
                cacheWriteLock.unlock();
            }
            map.put(userId, userConfig.clone());
        }
        List<UserConfiguration> retval = new ArrayList<UserConfiguration>(userIds.length);
        for (int userId : userIds) {
            retval.add(map.get(userId));
        }
        return retval.toArray(new UserConfiguration[retval.size()]);
    }

    @Override
    public void clearStorage() throws OXException {
        final Cache cache = this.cache;
        if (cache == null) {
            return;
        }
        cacheWriteLock.lock();
        try {
            cache.clear();
        } catch (final RuntimeException rte) {
            /*
             * Swallow
             */
            LOG.warn("A runtime error occurred.", rte);
        } finally {
            cacheWriteLock.unlock();
        }
    }

    @Override
    public void removeUserConfiguration(final int userId, final Context ctx) throws OXException {
        final Cache cache = this.cache;
        if (cache == null) {
            return;
        }
        CacheKey key = getKey(userId, ctx, cache);
        CacheKey keyWithoutExtended = getKey(cache, ctx, userId, false);
        cacheWriteLock.lock();
        try {
            cache.remove(key);
            cache.remove(keyWithoutExtended);
        } catch (RuntimeException e) {
            LOG.warn("Failed to remove user configuration for context " + ctx.getContextId() + " and user " + userId + " to cache.", e);
        } finally {
            cacheWriteLock.unlock();
        }
    }

    @Override
    public void saveUserConfiguration(final int permissionBits, final int userId, final Context ctx) throws OXException {
        delegateStorage.saveUserConfiguration(permissionBits, userId, ctx);
        removeUserConfiguration(userId, ctx);
    }
}

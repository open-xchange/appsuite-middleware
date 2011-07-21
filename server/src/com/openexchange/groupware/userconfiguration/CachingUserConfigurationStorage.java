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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import com.openexchange.cache.registry.CacheAvailabilityListener;
import com.openexchange.cache.registry.CacheAvailabilityRegistry;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheException;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheService;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.UserConfigurationException.UserConfigurationCode;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link CachingUserConfigurationStorage} - A cache-based implementation of {@link UserConfigurationStorage} with a fallback to
 * {@link RdbUserConfigurationStorage}.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class CachingUserConfigurationStorage extends UserConfigurationStorage {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(org.apache.commons.logging.LogFactory.getLog(CachingUserConfigurationStorage.class));

    private static final String CACHE_REGION_NAME = "UserConfiguration";

    private final CacheAvailabilityListener cacheAvailabilityListener;

    private transient final UserConfigurationStorage delegateStorage;

    private final Lock cacheWriteLock;

    private Cache cache;

    private UserConfigurationStorage fallback;

    /**
     * Initializes a new {@link CachingUserConfigurationStorage}.
     * 
     * @throws UserConfigurationException If an error occurs
     */
    public CachingUserConfigurationStorage() throws UserConfigurationException {
        super();
        cacheWriteLock = new ReentrantLock();
        this.delegateStorage = new RdbUserConfigurationStorage();
        cacheAvailabilityListener = new CacheAvailabilityListener() {

            public void handleAbsence() throws AbstractOXException {
                releaseCache();
            }

            public void handleAvailability() throws AbstractOXException {
                initCache();
            }
        };
        initCache();
    }

    private UserConfigurationStorage getFallback() {
        if (null == fallback) {
            fallback = new RdbUserConfigurationStorage();
        }
        return fallback;
    }

    @Override
    protected void startInternal() throws AbstractOXException {
        final CacheAvailabilityRegistry reg = CacheAvailabilityRegistry.getInstance();
        if (null != reg && !reg.registerListener(cacheAvailabilityListener)) {
            LOG.error("Cache availability listener could not be registered", new Throwable());
        }
    }

    @Override
    protected void stopInternal() throws AbstractOXException {
        final CacheAvailabilityRegistry reg = CacheAvailabilityRegistry.getInstance();
        if (null != reg) {
            reg.unregisterListener(cacheAvailabilityListener);
        }
        releaseCache();
    }

    private final CacheKey getKey(final int userId, final Context ctx) {
        return cache.newCacheKey(ctx.getContextId(), userId);
    }

    /**
     * Initializes cache reference
     * 
     * @throws UserConfigurationException If an error occurs
     */
    void initCache() throws UserConfigurationException {
        if (cache != null) {
            return;
        }
        try {
            cache = ServerServiceRegistry.getInstance().getService(CacheService.class).getCache(CACHE_REGION_NAME);
        } catch (final CacheException e) {
            throw new UserConfigurationException(UserConfigurationCode.CACHE_INITIALIZATION_FAILED, e, CACHE_REGION_NAME);
        } catch (final RuntimeException e) {
            throw new UserConfigurationException(UserConfigurationCode.CACHE_INITIALIZATION_FAILED, e, CACHE_REGION_NAME);
        }
    }

    /**
     * Releases cache reference
     * 
     * @throws UserConfigurationException If an error occurs
     */
    void releaseCache() throws UserConfigurationException {
        if (cache == null) {
            return;
        }
        try {
            cache.clear();
            final CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
            if (null != cacheService) {
                cacheService.freeCache(CACHE_REGION_NAME);
            }
        } catch (final CacheException e) {
            throw new UserConfigurationException(UserConfigurationCode.CACHE_INITIALIZATION_FAILED, e, CACHE_REGION_NAME);
        } catch (final RuntimeException e) {
            throw new UserConfigurationException(UserConfigurationCode.CACHE_INITIALIZATION_FAILED, e, CACHE_REGION_NAME);
        } finally {
            cache = null;
        }
    }

    @Override
    public UserConfiguration getUserConfiguration(final int userId, final int[] groups, final Context ctx) throws UserConfigurationException {
        if (cache == null) {
            return getFallback().getUserConfiguration(userId, groups, ctx);
        }
        final CacheKey key = getKey(userId, ctx);
        UserConfiguration userConfig = (UserConfiguration) cache.get(key);
        if (null == userConfig) {
            cacheWriteLock.lock();
            try {
                if (null == (userConfig = (UserConfiguration) cache.get(key))) {
                    userConfig = delegateStorage.getUserConfiguration(userId, groups, ctx);
                    cache.put(key, userConfig);
                }
            } catch (final CacheException e) {
                throw new UserConfigurationException(UserConfigurationCode.CACHE_PUT_ERROR, e, e.getMessage());
            } catch (final RuntimeException rte) {
                return getFallback().getUserConfiguration(userId, groups, ctx);
            } finally {
                cacheWriteLock.unlock();
            }
        }
        return (UserConfiguration) userConfig.clone();
    }

    @Override
    public UserConfiguration[] getUserConfiguration(final Context ctx, final User[] users) throws UserConfigurationException {
        if (cache == null) {
            return getFallback().getUserConfiguration(ctx, users);
        }
        final List<User> toLoad = new ArrayList<User>(users.length);
        final List<UserConfiguration> retval = new ArrayList<UserConfiguration>(users.length);
        for (final User user : users) {
            final UserConfiguration userConfig = (UserConfiguration) cache.get(getKey(user.getId(), ctx));
            if (null == userConfig) {
                toLoad.add(user);
            } else {
                retval.add((UserConfiguration) userConfig.clone());
            }
        }
        final UserConfiguration[] userConfigs = delegateStorage.getUserConfiguration(ctx, toLoad.toArray(new User[toLoad.size()]));
        for (final UserConfiguration userConfig : userConfigs) {
            cacheWriteLock.lock();
            try {
                cache.put(getKey(userConfig.getUserId(), ctx), userConfig);
            } catch (final CacheException e) {
                throw new UserConfigurationException(e);
            } catch (final RuntimeException rte) {
                return getFallback().getUserConfiguration(ctx, users);
            } finally {
                cacheWriteLock.unlock();
            }
            retval.add((UserConfiguration) userConfig.clone());
        }
        return retval.toArray(new UserConfiguration[retval.size()]);
    }

    @Override
    public void clearStorage() throws UserConfigurationException {
        if (cache == null) {
            return;
        }
        cacheWriteLock.lock();
        try {
            cache.clear();
        } catch (final CacheException e) {
            throw new UserConfigurationException(UserConfigurationCode.CACHE_CLEAR_ERROR, e, e.getMessage());
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
    public void removeUserConfiguration(final int userId, final Context ctx) throws UserConfigurationException {
        if (cache == null) {
            return;
        }
        cacheWriteLock.lock();
        try {
            cache.remove(getKey(userId, ctx));
        } catch (final CacheException e) {
            throw new UserConfigurationException(UserConfigurationCode.CACHE_REMOVE_ERROR, e, e.getMessage());
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
    public void saveUserConfiguration(final int permissionBits, final int userId, final Context ctx) throws UserConfigurationException {
        delegateStorage.saveUserConfiguration(permissionBits, userId, ctx);
        cacheWriteLock.lock();
        try {
            cache.remove(getKey(userId, ctx));
        } catch (final CacheException e) {
            throw new UserConfigurationException(UserConfigurationCode.CACHE_REMOVE_ERROR, e, e.getMessage());
        } catch (final RuntimeException rte) {
            /*
             * Swallow
             */
            LOG.warn("A runtime error occurred.", rte);
        } finally {
            cacheWriteLock.unlock();
        }
    }

}

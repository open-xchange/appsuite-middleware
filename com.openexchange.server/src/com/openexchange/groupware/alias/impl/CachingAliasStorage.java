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

package com.openexchange.groupware.alias.impl;

import java.sql.Connection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.alias.UserAliasStorage;
import com.openexchange.lock.LockService;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link CachingAliasStorage}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class CachingAliasStorage implements UserAliasStorage {

    private static final String REGION_NAME = "UserAlias";

    static CacheKey newCacheKey(CacheService cacheService, int userId, int contextId) {
        return cacheService.newCacheKey(contextId, userId);
    }

    // -----------------------------------------------------------------------------------------------------------------------------

    /** Proxy attribute for the object implementing the persistent methods. */
    private final RdbAliasStorage delegate;

    /**
     * Initializes a new {@link CachingAliasStorage}.
     */
    public CachingAliasStorage(RdbAliasStorage delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public void invalidateAliases(int contextId, int userId) throws OXException {
        CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
        if (null != cacheService) {
            Cache cache = cacheService.getCache(REGION_NAME);
            cache.remove(newCacheKey(cacheService, userId, contextId));
        }
    }

    @Override
    public Set<String> getAliases(int contextId) throws OXException {
        return delegate.getAliases(contextId);
    }

    @Override
    public Set<String> getAliases(int contextId, int userId) throws OXException {
        CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
        if (cacheService == null) {
            return delegate.getAliases(contextId, userId);
        }

        Cache cache = cacheService.getCache(REGION_NAME);
        CacheKey key = newCacheKey(cacheService, userId, contextId);
        Object object = cache.get(key);
        if (object instanceof Set) {
            return (Set<String>) object;
        }

        LockService lockService = ServerServiceRegistry.getInstance().getService(LockService.class);
        Lock lock = null == lockService ? LockService.EMPTY_LOCK : lockService.getSelfCleaningLockFor(new StringBuilder(32).append("loadaliases-").append(contextId).append('-').append(userId).toString());
        lock.lock();
        try {
            object = cache.get(key);
            if (object instanceof Set) {
                return (Set<String>) object;
            }

            HashSet<String> aliases = delegate.getAliases(contextId, userId);
            cache.put(key, aliases, false);
            return aliases;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int getUserId(int contextId, String alias) throws OXException {
        return delegate.getUserId(contextId, alias);
    }

    @Override
    public boolean createAlias(Connection con, int contextId, int userId, String alias) throws OXException {
        boolean success = delegate.createAlias(con, contextId, userId, alias);
        if (success) {
            invalidateAliases(contextId, userId);
        }
        return success;
    }

    @Override
    public boolean updateAlias(Connection con, int contextId, int userId, String oldAlias, String newAlias) throws OXException {
        boolean success = delegate.updateAlias(con, contextId, userId, oldAlias, newAlias);
        if (success) {
            invalidateAliases(contextId, userId);
        }
        return success;
    }

    @Override
    public boolean deleteAlias(Connection con, int contextId, int userId, String alias) throws OXException {
        boolean success = delegate.deleteAlias(con, contextId, userId, alias);
        if (success) {
            invalidateAliases(contextId, userId);
        }
        return success;
    }

    @Override
    public boolean deleteAliases(Connection con, int contextId, int userId) throws OXException {
        boolean success = delegate.deleteAliases(con, contextId, userId);
        if (success) {
            invalidateAliases(contextId, userId);
        }
        return success;
    }

    @Override
    public List<Integer> getUserIdsByAliasDomain(int contextId, String domain) throws OXException {
        return delegate.getUserIdsByAliasDomain(contextId, domain);
    }
}

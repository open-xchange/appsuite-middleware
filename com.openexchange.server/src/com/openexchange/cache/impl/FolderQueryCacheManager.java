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

package com.openexchange.cache.impl;

import static com.openexchange.java.Autoboxing.I;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheService;
import com.openexchange.exception.OXException;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;

/**
 * {@link FolderQueryCacheManager}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FolderQueryCacheManager {

    private static final String REGION_NAME = "OXFolderQueryCache";

    private static volatile FolderQueryCacheManager instance;

    private Cache folderQueryCache;

    private final Lock cacheLock;

    private FolderQueryCacheManager() throws OXException {
        super();
        cacheLock = new ReentrantLock(true);
        initCache();
    }

    public static boolean isInitialized() {
        return instance != null;
    }

    /**
     * Initializes the singleton instance of folder query cache {@link FolderQueryCacheManager manager}
     *
     * @throws OXException If initialization fails
     */
    public static void initInstance() throws OXException {
        getInstance();
    }

    /**
     * @return The singleton instance of {@link FolderQueryCacheManager}
     * @throws OXException if instance of {@link FolderQueryCacheManager} cannot be initialized
     */
    public static FolderQueryCacheManager getInstance() throws OXException {
        if (instance == null) {
            synchronized (FolderQueryCacheManager.class) {
                if (instance == null) {
                    instance = new FolderQueryCacheManager();
                }
            }
        }
        return instance;
    }

    /**
     * Initializes cache reference.
     *
     * @throws OXException If initializing the cache reference fails
     */
    public void initCache() throws OXException {
        if (folderQueryCache != null) {
            return;
        }
        folderQueryCache = ServerServiceRegistry.getInstance().getService(CacheService.class).getCache(REGION_NAME);
    }

    /**
     * Releases cache reference.
     *
     * @throws OXException If clearing cache fails
     */
    public void releaseCache() throws OXException {
        if (folderQueryCache == null) {
            return;
        }
        folderQueryCache.clear();
        folderQueryCache = null;
    }

    /**
     * Releases the singleton instance of {@link FolderQueryCacheManager} and frees its cache resources
     *
     * @throws OXException If cache cannot be freed
     */
    public static void releaseInstance() throws OXException {
        if (instance != null) {
            synchronized (FolderQueryCacheManager.class) {
                if (instance != null) {
                    instance = null;
                    final CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
                    if (null != cacheService) {
                        cacheService.freeCache(REGION_NAME);
                    }
                }
            }
        }
    }

    /**
     * Gets a query result from cache if present, otherwise <code>null</code> is returned
     *
     * @return query result if present, otherwise <code>null</code>
     */
    public LinkedList<Integer> getFolderQuery(final int queryNum, final Session session) {
        return getFolderQuery(queryNum, session.getUserId(), session.getContextId());
    }

    /**
     * Gets a query result from cache if present, otherwise <code>null</code> is returned
     *
     * @return query result if present, otherwise <code>null</code>
     */
    public LinkedList<Integer> getFolderQuery(final int queryNum, final int userId, final int cid) {
        if (null == folderQueryCache) {
            return null;
        }
        cacheLock.lock();
        try {
            final Object tmp = folderQueryCache.getFromGroup(createUserKey(userId), createContextKey(cid));
            if (null == tmp) {
                return null;
            }
            @SuppressWarnings("unchecked")
            final Map<CacheKey, LinkedList<Integer>> map = (Map<CacheKey, LinkedList<Integer>>) tmp;
            final LinkedList<Integer> q = map.get(createQueryKey(queryNum));
            if (null == q) {
                return null;
            }
            @SuppressWarnings("unchecked")
            final
            LinkedList<Integer> retval = (LinkedList<Integer>) q.clone();
            return retval;
        } finally {
            cacheLock.unlock();
        }
    }

    /**
     * Puts a query result into cache
     *
     * @throws OXException If a caching error occurs
     */
    public void putFolderQuery(final int queryNum, final LinkedList<Integer> q, final Session session) throws OXException {
        putFolderQuery(queryNum, q, session.getUserId(), session.getContextId());
    }

    /**
     * Puts a query result into cache
     *
     * @throws OXException If a caching error occurs
     */
    public void putFolderQuery(final int queryNum, final LinkedList<Integer> q, final int userId, final int cid) throws OXException {
        putFolderQuery(queryNum, q, userId, cid, false);
    }

    /**
     * Puts a query result into cache. If <code>append</code> is set and cache already contains a query result belonging to given
     * <code>queryNum</code>, given result is going to appended to existing one. Otherwise existing entries are replaced.
     *
     * @throws OXException If a caching error occurs
     */
    public void putFolderQuery(final int queryNum, final LinkedList<Integer> q, final Session session, final boolean append) throws OXException {
        putFolderQuery(queryNum, q, session.getUserId(), session.getContextId(), append);
    }

    /**
     * Puts a query result into cache. If <code>append</code> is set and cache already contains a query result belonging to given
     * <code>queryNum</code>, given result is going to appended to existing one. Otherwise existing entries are replaced.
     *
     * @throws OXException If a caching error occurs
     */
    public void putFolderQuery(final int queryNum, final LinkedList<Integer> q, final int userId, final int cid, final boolean append) throws OXException {
        if (null == folderQueryCache) {
            return;
        } else if (q == null) {
            return;
        }
        final CacheKey queryKey = createQueryKey(queryNum);
        cacheLock.lock();
        try {
            boolean insertMap = false;
            @SuppressWarnings("unchecked")
            Map<CacheKey, LinkedList<Integer>> map = (Map<CacheKey, LinkedList<Integer>>) folderQueryCache.getFromGroup(createUserKey(userId), createContextKey(cid));
            if (map == null) {
                map = new HashMap<CacheKey, LinkedList<Integer>>();
                insertMap = true;
            }
            final LinkedList<Integer> tmp = map.get(queryKey);
            if (tmp == null || !append) {
                @SuppressWarnings("unchecked")
                final
                LinkedList<Integer> clone = (LinkedList<Integer>) q.clone();
                map.put(queryKey, clone);
            } else {
                @SuppressWarnings("unchecked")
                final
                LinkedList<Integer> clone = (LinkedList<Integer>) q.clone();
                tmp.addAll(clone);
            }
            if (insertMap) {
                folderQueryCache.putInGroup(createUserKey(userId), createContextKey(cid), (Serializable) map);
            }
        } finally {
            cacheLock.unlock();
        }
    }

    /**
     * Clears all cache entries belonging to given session's context
     */
    public void invalidateContextQueries(final Session session) {
        invalidateContextQueries(session.getContextId());
    }

    /**
     * Clears all cache entries belonging to given context
     */
    public void invalidateContextQueries(final int cid) {
        if (null == folderQueryCache) {
            return;
        }
        cacheLock.lock();
        try {
            folderQueryCache.invalidateGroup(createContextKey(cid));
        } finally {
            cacheLock.unlock();
        }
    }

    private final static QueryCacheKey.Module MODULE = QueryCacheKey.Module.FOLDER;

    private CacheKey createQueryKey(final int queryNum) {
        return folderQueryCache.newCacheKey(MODULE.getNum(), queryNum);
    }

    private static Integer createUserKey(final int userId) {
        return I(userId);
    }

    private static String createContextKey(final int cid) {
        return Integer.toString(cid);
    }

}

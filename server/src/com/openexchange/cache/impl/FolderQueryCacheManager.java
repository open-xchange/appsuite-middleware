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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import com.openexchange.api2.OXException;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheException;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheService;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.oxfolder.OXFolderException;
import com.openexchange.tools.oxfolder.OXFolderException.FolderCode;

/**
 * {@link FolderQueryCacheManager}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FolderQueryCacheManager {

    private static final ConcurrentMap<Integer, ReadWriteLock> contextLocks = new ConcurrentHashMap<Integer, ReadWriteLock>();

    private static final String REGION_NAME = "OXFolderQueryCache";

    private static volatile FolderQueryCacheManager instance;

    private Cache folderQueryCache;

    private static ReadWriteLock getContextLock(final int cid) {
        final Integer key = Integer.valueOf(cid);
        ReadWriteLock l = contextLocks.get(key);
        if (l == null) {
            final ReadWriteLock tmp = new ReentrantReadWriteLock();
            l = contextLocks.putIfAbsent(key, tmp);
            if (null == l) {
                l = tmp;
            }
        }
        return l;
    }

    private FolderQueryCacheManager() throws OXException {
        super();
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
     * @throws OXFolderException If initializing the cache reference fails
     */
    public void initCache() throws OXFolderException {
        if (folderQueryCache != null) {
            return;
        }
        try {
            folderQueryCache = ServerServiceRegistry.getInstance().getService(CacheService.class).getCache(REGION_NAME);
        } catch (final CacheException e) {
            throw new OXFolderException(FolderCode.FOLDER_CACHE_INITIALIZATION_FAILED, e, REGION_NAME, e.getMessage());
        }
    }

    /**
     * Releases cache reference.
     * 
     * @throws OXFolderException If clearing cache fails
     */
    public void releaseCache() throws OXFolderException {
        if (folderQueryCache == null) {
            return;
        }
        try {
            folderQueryCache.clear();
        } catch (final CacheException e) {
            throw new OXFolderException(FolderCode.FOLDER_CACHE_INITIALIZATION_FAILED, e, REGION_NAME, e.getMessage());
        }
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
                        try {
                            cacheService.freeCache(REGION_NAME);
                        } catch (final CacheException e) {
                            throw new OXException(e);
                        }
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
    @SuppressWarnings("unchecked")
    public LinkedList<Integer> getFolderQuery(final int queryNum, final int userId, final int cid) {
        if (null == folderQueryCache) {
            return null;
        }
        final Lock ctxReadLock = getContextLock(cid).readLock();
        ctxReadLock.lock();
        try {
            final Map<CacheKey, LinkedList<Integer>> map = (Map<CacheKey, LinkedList<Integer>>) folderQueryCache.getFromGroup(
                createUserKey(userId),
                createContextKey(cid));
            final LinkedList<Integer> q;
            if (map == null || (q = map.get(createQueryKey(queryNum))) == null) {
                return null;
            }
            return (LinkedList<Integer>) q.clone();
        } finally {
            ctxReadLock.unlock();
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
    @SuppressWarnings("unchecked")
    public void putFolderQuery(final int queryNum, final LinkedList<Integer> q, final int userId, final int cid, final boolean append) throws OXException {
        if (null == folderQueryCache) {
            return;
        } else if (q == null) {
            return;
        }
        final Lock ctxWriteLock = getContextLock(cid).writeLock();
        ctxWriteLock.lock();
        try {
            boolean insertMap = false;
            Map<CacheKey, LinkedList<Integer>> map = (Map<CacheKey, LinkedList<Integer>>) folderQueryCache.getFromGroup(
                createUserKey(userId),
                createContextKey(cid));
            if (map == null) {
                map = new HashMap<CacheKey, LinkedList<Integer>>();
                insertMap = true;
            }
            final CacheKey queryKey = createQueryKey(queryNum);
            final LinkedList<Integer> tmp = map.get(queryKey);
            if (tmp == null || !append) {
                map.put(queryKey, (LinkedList<Integer>) q.clone());
            } else {
                tmp.addAll((LinkedList<Integer>) q.clone());
            }
            if (insertMap) {
                folderQueryCache.putInGroup(createUserKey(userId), createContextKey(cid), (Serializable) map);
            }
        } catch (final CacheException e) {
            throw new OXException(e);
        } finally {
            ctxWriteLock.unlock();
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
        final Lock ctxWriteLock = getContextLock(cid).writeLock();
        ctxWriteLock.lock();
        try {
            folderQueryCache.invalidateGroup(createContextKey(cid));
        } finally {
            ctxWriteLock.unlock();
        }
    }

    private final static QueryCacheKey.Module MODULE = QueryCacheKey.Module.FOLDER;

    private CacheKey createQueryKey(final int queryNum) {
        return folderQueryCache.newCacheKey(MODULE.getNum(), queryNum);
    }

    private static Integer createUserKey(final int userId) {
        return Integer.valueOf(userId);
    }

    private static String createContextKey(final int cid) {
        return String.valueOf(cid);
    }

}

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

import java.sql.Connection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import com.openexchange.ajax.fields.FolderFields;
import com.openexchange.api2.OXException;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheException;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheService;
import com.openexchange.caching.ElementAttributes;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.oxfolder.OXFolderException;
import com.openexchange.tools.oxfolder.OXFolderNotFoundException;
import com.openexchange.tools.oxfolder.OXFolderProperties;
import com.openexchange.tools.oxfolder.OXFolderException.FolderCode;

/**
 * {@link FolderCacheManager} - Holds a cache for instances of {@link FolderObject}
 * <p>
 * <b>NOTE:</b> Only cloned versions of {@link FolderObject} instances are put into or received from cache. That prevents the danger of
 * further working on and therefore changing cached instances.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FolderCacheManager {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(FolderCacheManager.class);

    private static volatile FolderCacheManager instance;

    private static final String FOLDER_CACHE_REGION_NAME = "OXFolderCache";

    private final ConcurrentMap<Integer, ReadWriteLock> contextLocks;

    private Cache folderCache;

    /**
     * Initializes a new {@link FolderCacheManager}.
     * 
     * @throws OXException If initialization fails
     */
    private FolderCacheManager() throws OXException {
        super();
        contextLocks = new ConcurrentHashMap<Integer, ReadWriteLock>();
        initCache();
    }

    /**
     * Checks if folder cache has been initialized.
     * 
     * @return <code>true</code> if folder cache has been initialized; otherwise <code>false</code>
     */
    public static boolean isInitialized() {
        return (instance != null);
    }

    /**
     * Checks if folder cache is enabled (through configuration).
     * 
     * @return <code>true</code> if folder cache is enabled; otherwise <code>false</code>
     */
    public static boolean isEnabled() {
        return OXFolderProperties.isEnableFolderCache();
    }

    /**
     * Initializes the singleton instance of folder cache {@link FolderCacheManager manager}.
     * 
     * @throws OXException If initialization fails
     */
    public static void initInstance() throws OXException {
        if (instance == null) {
            synchronized (FolderCacheManager.class) {
                if (instance == null) {
                    instance = new FolderCacheManager();
                }
            }
        }
    }

    /**
     * Gets the singleton instance of folder cache {@link FolderCacheManager manager}.
     * 
     * @return The singleton instance of folder cache {@link FolderCacheManager manager}.
     * @throws FolderCacheNotEnabledException If folder cache is explicitly disabled through
     *             {@link OXFolderProperties#isEnableFolderCache() property}
     * @throws OXException If initialization fails
     */
    public static FolderCacheManager getInstance() throws FolderCacheNotEnabledException, OXException {
        if (!OXFolderProperties.isEnableFolderCache()) {
            throw new FolderCacheNotEnabledException();
        }
        if (instance == null) {
            synchronized (FolderCacheManager.class) {
                if (instance == null) {
                    instance = new FolderCacheManager();
                }
            }
        }
        return instance;
    }

    /**
     * Releases the singleton instance of folder cache {@link FolderCacheManager manager}.
     */
    public static void releaseInstance() {
        if (instance != null) {
            synchronized (FolderCacheManager.class) {
                if (instance != null) {
                    instance = null;
                    final CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
                    if (null != cacheService) {
                        try {
                            cacheService.freeCache(FOLDER_CACHE_REGION_NAME);
                        } catch (final CacheException e) {
                            LOG.error(e.getMessage(), e);
                        }
                    }
                }
            }
        }
    }

    /**
     * Initializes cache reference.
     * 
     * @throws OXFolderException If initializing the cache reference fails
     */
    public void initCache() throws OXFolderException {
        if (folderCache != null) {
            return;
        }
        try {
            folderCache = ServerServiceRegistry.getInstance().getService(CacheService.class).getCache(FOLDER_CACHE_REGION_NAME);
        } catch (final CacheException e) {
            throw new OXFolderException(FolderCode.FOLDER_CACHE_INITIALIZATION_FAILED, e, FOLDER_CACHE_REGION_NAME, e.getMessage());
        }
    }

    /**
     * Releases cache reference.
     * 
     * @throws OXFolderException If clearing cache fails
     */
    public void releaseCache() throws OXFolderException {
        if (folderCache == null) {
            return;
        }
        try {
            folderCache.clear();
        } catch (final CacheException e) {
            throw new OXFolderException(FolderCode.FOLDER_CACHE_INITIALIZATION_FAILED, e, FOLDER_CACHE_REGION_NAME, e.getMessage());
        }
        folderCache = null;
    }

    private ReadWriteLock getContextLock(final Context ctx) {
        return getContextLock(ctx.getContextId());
    }

    private ReadWriteLock getContextLock(final int cid) {
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

    private CacheKey getCacheKey(final int cid, final int objectId) {
        return folderCache.newCacheKey(cid, objectId);
    }

    /**
     * Fetches <code>FolderObject</code> which matches given object id. If none found or <code>fromCache</code> is not set the folder will
     * be loaded from underlying database store and automatically put into cache.
     * <p>
     * <b>NOTE:</b> This method returns a clone of cached <code>FolderObject</code> instance. Thus any modifications made to the referenced
     * object will not affect cached version
     * 
     * @throws OXException If a caching error occurs
     */
    public FolderObject getFolderObject(final int objectId, final boolean fromCache, final Context ctx, final Connection readConArg) throws OXException {
        final Lock ctxReadLock = getContextLock(ctx).readLock();
        ctxReadLock.lock();
        try {
            FolderObject folderObj = null;
            if (fromCache && null != folderCache) {
                folderObj = (FolderObject) folderCache.get(getCacheKey(ctx.getContextId(), objectId));
            }
            /*
             * Either fromCache was false or folder object was not found.
             */
            if (folderObj == null) {
                /*
                 * Upgrade lock: unlock first to acquire write lock
                 */
                ctxReadLock.unlock();
                final Lock ctxWriteLock = getContextLock(ctx).writeLock();
                ctxWriteLock.lock();
                try {
                    folderObj = loadFolderObjectInternal(objectId, ctx, readConArg);
                } finally {
                    /*
                     * Downgrade lock: reacquire read without giving up write lock and...
                     */
                    ctxReadLock.lock();
                    /*
                     * ... unlock write.
                     */
                    ctxWriteLock.unlock();
                }
            }
            /*
             * Return a copy, NOT a reference
             */
            return (FolderObject) folderObj.clone();
        } finally {
            ctxReadLock.unlock();
        }
    }

    /**
     * <p>
     * Fetches <code>FolderObject</code> which matches given object id.
     * </p>
     * <p>
     * <b>NOTE:</b> This method returns a clone of cached <code>FolderObject</code> instance. Thus any modifications made to the referenced
     * object will not affect cached version
     * </p>
     * 
     * @return The matching <code>FolderObject</code> instance else <code>null</code>
     */
    public FolderObject getFolderObject(final int objectId, final Context ctx) {
        if (null == folderCache) {
            return null;
        }
        final Lock ctxReadLock = getContextLock(ctx).readLock();
        ctxReadLock.lock();
        try {
            final FolderObject retval = (FolderObject) folderCache.get(getCacheKey(ctx.getContextId(), objectId));
            return retval == null ? null : (FolderObject) retval.clone();
        } finally {
            ctxReadLock.unlock();
        }
    }

    /**
     * Loads the folder which matches given object id from underlying database store and puts it into cache.
     * <p>
     * <b>NOTE:</b> This method returns a clone of cached <code>FolderObject</code> instance. Thus any modifications made to the referenced
     * object will not affect cached version
     * 
     * @return The matching <code>FolderObject</code> instance fetched from storage else <code>null</code>
     * @throws OXException If a caching error occurs
     */
    public FolderObject loadFolderObject(final int folderId, final Context ctx, final Connection readCon) throws OXException {
        final Lock ctxWriteLock = getContextLock(ctx).writeLock();
        ctxWriteLock.lock();
        try {
            /*
             * Return a copy, NOT a reference
             */
            return (FolderObject) loadFolderObjectInternal(folderId, ctx, readCon).clone();
        } finally {
            ctxWriteLock.unlock();
        }
    }

    /**
     * Loads the folder object from underlying database storage whose id matches given parameter <code>folderId</code>.
     * <p>
     * The returned object references the actually cached entry
     * 
     * @param folderId The folder ID
     * @param ctx The context
     * @param readCon A readable connection (<b>optional</b>), pass <code>null</code> to fetch a new one from connection pool
     * @return The object referencing the actually cached entry
     * @throws OXException If folder object could not be loaded or a caching error occurs
     */
    private FolderObject loadFolderObjectInternal(final int folderId, final Context ctx, final Connection readCon) throws OXException {
        if (folderId <= 0) {
            throw new OXFolderNotFoundException(folderId, ctx);
        }
        final FolderObject folderObj;
        try {
            folderObj = FolderObject.loadFolderObjectFromDB(folderId, ctx, readCon);
            if (null != folderCache) {
                final CacheKey key = getCacheKey(ctx.getContextId(), folderId);
                /*
                 * Perform a remove-and-put cycle to ensure the folder is invalidated in lateral/remote caches
                 */
                folderCache.remove(key);
                /*
                 * Put folder into cache
                 */
                folderCache.put(key, folderObj);
            }
        } catch (final CacheException e) {
            throw new OXException(e);
        }
        /*
         * Return a reference to cached element
         */
        return folderObj;
    }

    /**
     * Simply puts given <code>FolderObject</code> into cache if object's id is different to zero.
     * <p>
     * <b>NOTE:</b> This method puts a clone of given <code>FolderObject</code> instance into cache. Thus any modifications made to the
     * referenced object will not affect cached version
     * 
     * @param folderObj The folder object
     * @param ctx The context
     * @throws OXException If a caching error occurs
     */
    public void putFolderObject(final FolderObject folderObj, final Context ctx) throws OXException {
        putFolderObject(folderObj, ctx, true, null);
    }

    /**
     * <p>
     * Simply puts given <code>FolderObject</code> into cache if object's id is different to zero. If flag <code>overwrite</code> is set to
     * <code>false</code> then this method returns immediately if cache already holds a matching entry.
     * </p>
     * <p>
     * <b>NOTE:</b> This method puts a clone of given <code>FolderObject</code> instance into cache. Thus any modifications made to the
     * referenced object will not affect cached version
     * </p>
     * 
     * @param folderObj The folder object
     * @param ctx The context
     * @param overwrite <code>true</code> to overwrite; otherwise <code>false</code>
     * @param elemAttribs The element's attributes (<b>optional</b>), pass <code>null</code> to use the default attributes
     * @throws OXException If a caching error occurs
     */
    public void putFolderObject(final FolderObject folderObj, final Context ctx, final boolean overwrite, final ElementAttributes elemAttribs) throws OXException {
        if (null == folderCache) {
            return;
        }
        if (!folderObj.containsObjectID()) {
            throw new OXFolderException(
                FolderCode.MISSING_FOLDER_ATTRIBUTE,
                FolderFields.ID,
                Integer.valueOf(-1),
                Integer.valueOf(ctx.getContextId()));
        }
        try {
            final CacheKey ck = getCacheKey(ctx.getContextId(), folderObj.getObjectID());
            if (overwrite) {
                /*
                 * Put clone of new object into cache. If there is currently an object associated with this key in the region it is
                 * replaced.
                 */
                final Lock ctxWriteLock = getContextLock(ctx).writeLock();
                ctxWriteLock.lock();
                try {
                    if (elemAttribs == null) {
                        /*
                         * Remove to distribute PUT as REMOVE
                         */
                        folderCache.remove(ck);
                        /*
                         * Put with default attributes
                         */
                        folderCache.put(ck, (FolderObject) folderObj.clone());
                    } else {
                        /*
                         * Remove to distribute PUT as REMOVE
                         */
                        folderCache.remove(ck);
                        /*
                         * Ensure isLateral is set to false
                         */
                        elemAttribs.setIsLateral(false);
                        folderCache.put(ck, (FolderObject) folderObj.clone(), elemAttribs);
                    }
                } finally {
                    ctxWriteLock.unlock();
                }
            } else {
                if (folderCache.get(ck) != null) {
                    return;
                }
                /*
                 * Wait for other threads that currently own PUT lock
                 */
                final Lock ctxWriteLock = getContextLock(ctx).writeLock();
                ctxWriteLock.lock();
                try {
                    if (folderCache.get(ck) != null) {
                        /*
                         * Another thread made a PUT in the meantime. Return cause we may not overwrite.
                         */
                        return;
                    }
                    /*
                     * Perform initial put
                     */
                    if (elemAttribs == null) {
                        /*
                         * Put with default attributes
                         */
                        folderCache.put(ck, (FolderObject) folderObj.clone());
                    } else {
                        /*
                         * Ensure isLateral is set to false
                         */
                        elemAttribs.setIsLateral(false);
                        folderCache.put(ck, (FolderObject) folderObj.clone(), elemAttribs);
                    }
                } finally {
                    ctxWriteLock.unlock();
                }
            }
        } catch (final CacheException e) {
            throw new OXException(e);
        }
    }

    /**
     * Removes matching folder object from cache
     * 
     * @param key The key
     * @param ctx The context
     * @throws OXException If a caching error occurs
     */
    public void removeFolderObject(final int key, final Context ctx) throws OXException {
        if (null == folderCache) {
            return;
        }
        /*
         * Remove object from cache if exist
         */
        if (key > 0) {
            final Lock ctxWriteLock = getContextLock(ctx).writeLock();
            ctxWriteLock.lock();
            try {
                folderCache.remove(getCacheKey(ctx.getContextId(), key));
            } catch (final CacheException e) {
                throw new OXException(e);
            } finally {
                ctxWriteLock.unlock();
            }
        }
    }

    /**
     * Removes matching folder objects from cache
     * 
     * @param keys The keys
     * @param ctx The context
     * @throws OXException If a caching error occurs
     */
    public void removeFolderObjects(final int[] keys, final Context ctx) throws OXException {
        if (null == folderCache) {
            return;
        } else if (keys == null || keys.length == 0) {
            return;
        }
        /*
         * Remove objects from cache
         */
        final Lock ctxWriteLock = getContextLock(ctx).writeLock();
        ctxWriteLock.lock();
        try {
            for (int i = 0; i < keys.length; i++) {
                final int key = keys[i];
                if (key > 0) {
                    folderCache.remove(getCacheKey(ctx.getContextId(), key));
                }
            }
        } catch (final CacheException e) {
            throw new OXException(e);
        } finally {
            ctxWriteLock.unlock();
        }
    }

    /**
     * Removes all folder objects from this cache
     * 
     * @throws OXException If folder cache cannot be cleared
     */
    public void clearAll() throws OXException {
        if (null == folderCache) {
            return;
        }
        try {
            folderCache.clear();
        } catch (final CacheException e) {
            throw new OXException(e);
        }
    }

    /**
     * Returns default element attributes for this cache
     * 
     * @return default element attributes for this cache or <code>null</code>
     * @throws CacheException If a caching error occurs
     */
    public ElementAttributes getDefaultFolderObjectAttributes() throws CacheException {
        if (null == folderCache) {
            return null;
        }
        /*
         * Returns a copy NOT a reference
         */
        return folderCache.getDefaultElementAttributes();
    }

}

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

package com.openexchange.cache.impl;

import static com.openexchange.java.Autoboxing.I;
import java.io.Serializable;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheService;
import com.openexchange.caching.ElementAttributes;
import com.openexchange.caching.dynamic.OXObjectFactory;
import com.openexchange.caching.dynamic.Refresher;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.oxfolder.OXFolderExceptionCode;
import com.openexchange.tools.oxfolder.OXFolderProperties;

/**
 * {@link FolderCacheManager} - Holds a cache for instances of {@link FolderObject}
 * <p>
 * <b>NOTE:</b> Only cloned versions of {@link FolderObject} instances are put into or received from cache. That prevents the danger of
 * further working on and therefore changing cached instances.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FolderCacheManager {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(FolderCacheManager.class));

    private static volatile FolderCacheManager instance;

    private static final String REGION_NAME = "OXFolderCache";

    private volatile Cache folderCache;

    private final Lock cacheLock;

    /**
     * Initializes a new {@link FolderCacheManager}.
     *
     * @throws OXException If initialization fails
     */
    private FolderCacheManager() throws OXException {
        super();
        cacheLock = new ReentrantLock(true);
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
     * @throws OXException If initialization fails
     */
    public static FolderCacheManager getInstance() throws OXException {
        if (!OXFolderProperties.isEnableFolderCache()) {
            throw OXFolderExceptionCode.CACHE_NOT_ENABLED.create();
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
                            cacheService.freeCache(REGION_NAME);
                        } catch (final OXException e) {
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
     * @throws OXException If initializing the cache reference fails
     */
    public void initCache() throws OXException {
        if (folderCache != null) {
            return;
        }
        folderCache = ServerServiceRegistry.getInstance().getService(CacheService.class).getCache(REGION_NAME);
    }

    /**
     * Releases cache reference.
     *
     * @throws OXException If clearing cache fails
     */
    public void releaseCache() throws OXException {
        final Cache folderCache = this.folderCache;
        if (folderCache == null) {
            return;
        }
        folderCache.clear();
        this.folderCache = null;
    }

    CacheKey getCacheKey(final int cid, final int objectId) {
        return folderCache.newCacheKey(cid, objectId);
    }

    Lock getCacheLock() {
        return cacheLock;
    }

    private class FolderFactory implements OXObjectFactory<FolderObject> {

        private final Context ctx;

        private final int folderId;

        FolderFactory(final Context ctx, final int folderId) {
            super();
            this.ctx = ctx;
            this.folderId = folderId;
        }

        @Override
        public Lock getCacheLock() {
            return FolderCacheManager.this.getCacheLock();
        }

        @Override
        public Serializable getKey() {
            return getCacheKey(ctx.getContextId(), folderId);
        }

        @Override
        public FolderObject load() throws OXException {
            return loadFolderObjectInternal(folderId, ctx, null);
        }
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
    public FolderObject getFolderObject(final int objectId, final boolean fromCache, final Context ctx, final Connection readCon) throws OXException {
        final Cache folderCache = this.folderCache;
        if (null == folderCache) {
            throw OXFolderExceptionCode.CACHE_NOT_ENABLED.create("foldercache.properties");
        }
        try {
            if (fromCache) {
                // Conditional put into cache: Put only if absent.
                if (null != readCon) {
                    putIfAbsentInternal(new LoadingFolderProvider(objectId, ctx, readCon), ctx, null);
                }
            } else {
                // Forced put into cache: Always put.
                putFolderObject(loadFolderObjectInternal(objectId, ctx, readCon), ctx, true, null);
            }
            // Return refreshable object
            return Refresher.refresh(REGION_NAME, folderCache, new FolderFactory(ctx, objectId), true).clone();
        } catch (final OXException e) {
            throw e;
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
        final Cache folderCache = this.folderCache;
        if (null == folderCache) {
            return null;
        }
        final FolderObject retval;
        cacheLock.lock();
        try {
            final Object tmp = folderCache.get(getCacheKey(ctx.getContextId(), objectId));
            // Refresher uses Condition objects to prevent multiple threads loading same folder.
            if (tmp instanceof FolderObject) {
                retval = ((FolderObject) tmp);
            } else {
                retval = null;
            }
        } finally {
            cacheLock.unlock();
        }
        return null == retval ? retval : retval.clone();
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
        final CacheKey key = getCacheKey(ctx.getContextId(), folderId);
        final Cache folderCache = this.folderCache;
        cacheLock.lock();
        try {
            final Object tmp = folderCache.get(key);
            if (tmp instanceof FolderObject) {
                folderCache.remove(key);
                // Dirty hack
                final CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
                if (null != cacheService) {
                    try {
                        final Cache globalCache = cacheService.getCache("GlobalFolderCache");
                        final CacheKey cacheKey = cacheService.newCacheKey(ctx.getContextId(), FolderStorage.REAL_TREE_ID, String.valueOf(key));
                        globalCache.remove(cacheKey);
                    } catch (final OXException e) {
                        LOG.warn(e.getMessage(), e);
                    }
                }
            }
        } finally {
            cacheLock.unlock();
        }
        if (null != readCon) {
            putIfAbsent(loadFolderObjectInternal(folderId, ctx, readCon), ctx, null);
        }
        return Refresher.refresh(REGION_NAME, folderCache, new FolderFactory(ctx, folderId), true).clone();
    }

    /**
     * Loads the folder object from underlying database storage whose id matches given parameter <code>folderId</code>.
     * <p>
     *
     * @param folderId The folder ID
     * @param ctx The context
     * @param readCon A readable connection (<b>optional</b>), pass <code>null</code> to fetch a new one from connection pool
     * @return The object loaded from DB.
     * @throws OXException If folder object could not be loaded or a caching error occurs
     */
    FolderObject loadFolderObjectInternal(final int folderId, final Context ctx, final Connection readCon) throws OXException {
        if (folderId <= 0) {
            throw OXFolderExceptionCode.NOT_EXISTS.create(folderId, ctx.getContextId());
        }
        return FolderObject.loadFolderObjectFromDB(folderId, ctx, readCon);
    }

    /**
     * If the specified folder object is not already in cache, it is put into cache.
     * <p>
     * <b>NOTE:</b> This method puts a clone of given <code>FolderObject</code> instance into cache. Thus any modifications made to the
     * referenced object will not affect cached version
     *
     * @param folderObj The folder object
     * @param ctx The context
     * @param elemAttribs The element's attributes (<b>optional</b>), pass <code>null</code> to use the default attributes
     * @return The previous folder object available in cache, or <tt>null</tt> if there was none
     * @throws OXException If put-if-absent operation fails
     */
    public FolderObject putIfAbsent(final FolderObject folderObj, final Context ctx, final ElementAttributes elemAttribs) throws OXException {
        if (null == folderCache) {
            throw OXFolderExceptionCode.CACHE_NOT_ENABLED.create();
        }
        if (!folderObj.containsObjectID()) {
            throw OXFolderExceptionCode.MISSING_FOLDER_ATTRIBUTE.create(DataFields.ID, I(-1), I(ctx.getContextId()));
        }
        return putIfAbsentInternal(new InstanceFolderProvider(folderObj.clone()), ctx, elemAttribs);
    }

    private FolderObject putIfAbsentInternal(final FolderProvider folderProvider, final Context ctx, final ElementAttributes elemAttribs) throws OXException {
        final CacheKey key = getCacheKey(ctx.getContextId(), folderProvider.getObjectID());
        final FolderObject retval;
        cacheLock.lock();
        try {
            final Cache folderCache = this.folderCache;
            final Object tmp = folderCache.get(key);
            if (tmp instanceof FolderObject) {
                // Already in cache
                retval = ((FolderObject) tmp);
            } else {
                Condition cond = null;
                if (tmp instanceof Condition) {
                    cond = (Condition) tmp;
                } else {
                    // Remove to distribute PUT as REMOVE
                    folderCache.remove(key);
                    // Dirty hack
                    final CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
                    if (null != cacheService) {
                        try {
                            final Cache globalCache = cacheService.getCache("GlobalFolderCache");
                            final CacheKey cacheKey = cacheService.newCacheKey(ctx.getContextId(), FolderStorage.REAL_TREE_ID, String.valueOf(key));
                            globalCache.remove(cacheKey);
                        } catch (final OXException e) {
                            LOG.warn(e.getMessage(), e);
                        }
                    }
                }
                if (elemAttribs == null || folderCache.isDistributed()) {
                    // Put with default attributes
                    folderCache.put(key, folderProvider.getFolderObject());
                } else {
                    folderCache.put(key, folderProvider.getFolderObject(), elemAttribs);
                }
                if (null != cond) {
                    cond.signalAll();
                }
                // Return null to indicate successful insertion
                retval = null;
            }
        } finally {
            cacheLock.unlock();
        }
        return null == retval ? retval : retval.clone();
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
        final Cache folderCache = this.folderCache;
        if (null == folderCache) {
            return;
        }
        if (!folderObj.containsObjectID()) {
            throw OXFolderExceptionCode.MISSING_FOLDER_ATTRIBUTE.create(DataFields.ID, I(-1), I(ctx.getContextId()));
        }
        if (null != elemAttribs) {
            /*
             * Ensure isLateral is set to false
             */
            elemAttribs.setIsLateral(false);
        }
        /*
         * Put clone of new object into cache.
         */
        final FolderObject clone = folderObj.clone();
        final CacheKey key = getCacheKey(ctx.getContextId(), folderObj.getObjectID());
        cacheLock.lock();
        try {
            final Object tmp = folderCache.get(key);
            // If there is currently an object associated with this key in the region it is replaced.
            Condition cond = null;
            if (overwrite) {
                if (tmp instanceof FolderObject) {
                    // Remove to distribute PUT as REMOVE
                    folderCache.remove(key);
                    // Dirty hack
                    final CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
                    if (null != cacheService) {
                        try {
                            final Cache globalCache = cacheService.getCache("GlobalFolderCache");
                            final CacheKey cacheKey = cacheService.newCacheKey(ctx.getContextId(), FolderStorage.REAL_TREE_ID, String.valueOf(key));
                            globalCache.remove(cacheKey);
                        } catch (final OXException e) {
                            LOG.warn(e.getMessage(), e);
                        }
                    }
                } else if (tmp instanceof Condition) {
                    cond = (Condition) tmp;
                }
            } else {
                // Another thread made a PUT in the meantime. Return cause we may not overwrite.
                if (tmp instanceof FolderObject) {
                    return;
                } else if (tmp instanceof Condition) {
                    cond = (Condition) tmp;
                }
            }
            if (elemAttribs == null || folderCache.isDistributed()) {
                // Put with default attributes
                folderCache.put(key, clone);
            } else {
                folderCache.put(key, clone, elemAttribs);
            }
            if (null != cond) {
                cond.signalAll();
            }
        } finally {
            cacheLock.unlock();
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
        final Cache folderCache = this.folderCache;
        if (null == folderCache) {
            return;
        }
        // Remove object from cache if exist
        if (key > 0) {
            final CacheKey cacheKey = getCacheKey(ctx.getContextId(), key);
            cacheLock.lock();
            try {
                final Object tmp = folderCache.get(cacheKey);
                if (!(tmp instanceof Condition)) {
                    folderCache.remove(cacheKey);
                }
            } finally {
                cacheLock.unlock();
            }
        }
        // Dirty hack
        final CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
        if (null != cacheService) {
            try {
                final Cache globalCache = cacheService.getCache("GlobalFolderCache");
                final CacheKey cacheKey = cacheService.newCacheKey(ctx.getContextId(), FolderStorage.REAL_TREE_ID, String.valueOf(key));
                globalCache.remove(cacheKey);
            } catch (final OXException e) {
                LOG.warn(e.getMessage(), e);
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
        final Cache folderCache = this.folderCache;
        if (null == folderCache) {
            return;
        } else if (keys == null || keys.length == 0) {
            return;
        }
        final List<CacheKey> cacheKeys = new ArrayList<CacheKey>();
        for (final int key : keys) {
            if (key > 0) {
                cacheKeys.add(getCacheKey(ctx.getContextId(), key));
            }
        }
        /*
         * Remove objects from cache
         */
        cacheLock.lock();
        try {
            for (final CacheKey cacheKey : cacheKeys) {
                final Object tmp = folderCache.get(cacheKey);
                if (!(tmp instanceof Condition)) {
                    folderCache.remove(cacheKey);
                }
            }
        } finally {
            cacheLock.unlock();
        }
        // Dirty hack
        final CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
        if (null != cacheService) {
            try {
                final Cache globalCache = cacheService.getCache("GlobalFolderCache");
                for (final int key : keys) {
                    final CacheKey cacheKey = cacheService.newCacheKey(ctx.getContextId(), FolderStorage.REAL_TREE_ID, String.valueOf(key));
                    globalCache.remove(cacheKey);
                }
            } catch (final OXException e) {
                LOG.warn(e.getMessage(), e);
            }
        }
    }

    /**
     * Removes all folder objects from this cache
     *
     * @throws OXException If folder cache cannot be cleared
     */
    public void clearAll() throws OXException {
        final Cache folderCache = this.folderCache;
        if (null == folderCache) {
            return;
        }
        folderCache.clear();
        // Dirty hack
        final CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
        if (null != cacheService) {
            try {
                final Cache globalCache = cacheService.getCache("GlobalFolderCache");
                globalCache.clear();
            } catch (final OXException e) {
                LOG.warn(e.getMessage(), e);
            }
        }
    }

    /**
     * Returns default element attributes for this cache
     *
     * @return default element attributes for this cache or <code>null</code>
     * @throws OXException If a caching error occurs
     */
    public ElementAttributes getDefaultFolderObjectAttributes() throws OXException {
        final Cache folderCache = this.folderCache;
        if (null == folderCache) {
            return null;
        }
        /*
         * Returns a copy NOT a reference
         */
        return folderCache.getDefaultElementAttributes();
    }

    private static interface FolderProvider {

        FolderObject getFolderObject() throws OXException;

        int getObjectID();
    }

    private static final class InstanceFolderProvider implements FolderProvider {

        private final FolderObject folderObject;

        public InstanceFolderProvider(final FolderObject folderObject) {
            super();
            this.folderObject = folderObject;
        }

        @Override
        public FolderObject getFolderObject() {
            return folderObject;
        }

        @Override
        public int getObjectID() {
            return folderObject.getObjectID();
        }

    }

    private static final class LoadingFolderProvider implements FolderProvider {

        private final Connection readCon;

        private final int folderId;

        private final Context ctx;

        public LoadingFolderProvider(final int folderId, final Context ctx, final Connection readCon) {
            super();
            this.folderId = folderId;
            this.ctx = ctx;
            this.readCon = readCon;
        }

        @Override
        public FolderObject getFolderObject() throws OXException {
            if (folderId <= 0) {
                throw OXFolderExceptionCode.NOT_EXISTS.create(folderId, ctx.getContextId());
            }
            return FolderObject.loadFolderObjectFromDB(folderId, ctx, readCon);
        }

        @Override
        public int getObjectID() {
            return folderId;
        }
    }
}

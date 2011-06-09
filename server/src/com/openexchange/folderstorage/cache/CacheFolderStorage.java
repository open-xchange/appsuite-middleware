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

package com.openexchange.folderstorage.cache;

import gnu.trove.TIntArrayList;
import gnu.trove.TObjectIntHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheException;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheService;
import com.openexchange.concurrent.CallerRunsCompletionService;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderException;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.FolderType;
import com.openexchange.folderstorage.SortableId;
import com.openexchange.folderstorage.StorageParameters;
import com.openexchange.folderstorage.StoragePriority;
import com.openexchange.folderstorage.StorageType;
import com.openexchange.folderstorage.Type;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.cache.memory.FolderMap;
import com.openexchange.folderstorage.internal.StorageParametersImpl;
import com.openexchange.folderstorage.internal.performers.ClearPerformer;
import com.openexchange.folderstorage.internal.performers.CreatePerformer;
import com.openexchange.folderstorage.internal.performers.DeletePerformer;
import com.openexchange.folderstorage.internal.performers.InstanceStorageParametersProvider;
import com.openexchange.folderstorage.internal.performers.PathPerformer;
import com.openexchange.folderstorage.internal.performers.SessionStorageParametersProvider;
import com.openexchange.folderstorage.internal.performers.StorageParametersProvider;
import com.openexchange.folderstorage.internal.performers.UpdatePerformer;
import com.openexchange.folderstorage.internal.performers.UpdatesPerformer;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.server.ServiceException;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.threadpool.ThreadPoolCompletionService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link CacheFolderStorage} - The cache folder storage.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CacheFolderStorage implements FolderStorage {

    private static final ThreadPools.ExpectedExceptionFactory<FolderException> FACTORY =
        new ThreadPools.ExpectedExceptionFactory<FolderException>() {

            public Class<FolderException> getType() {
                return FolderException.class;
            }

            public FolderException newUnexpectedError(final Throwable t) {
                return FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(t, t.getMessage());
            }
        };

    private final CacheFolderStorageRegistry registry;

    private volatile CacheService cacheService;

    private volatile Cache globalCache;

    // private Cache userCache;

    /**
     * Initializes a new {@link CacheFolderStorage}.
     */
    public CacheFolderStorage() {
        super();
        registry = CacheFolderStorageRegistry.getInstance();
    }

    /**
     * Clears this cache with respect to specified session.
     * 
     * @param session The session
     * @throws FolderException If clear operation fails
     */
    public void clear(final Session session) throws FolderException {
        final Cache cache = globalCache;
        if (null != cache) {
            cache.invalidateGroup(String.valueOf(session.getContextId()));
        }
        dropUserEntries(session.getUserId(), session.getContextId());
    }

    /**
     * Initializes this folder cache on available cache service.
     * 
     * @throws FolderException If initialization of this folder cache fails
     */
    public void onCacheAvailable() throws FolderException {
        try {
            cacheService = CacheServiceRegistry.getServiceRegistry().getService(CacheService.class, true);
            globalCache = cacheService.getCache("GlobalFolderCache");
            // userCache = cacheService.getCache("UserFolderCache");
        } catch (final ServiceException e) {
            throw new FolderException(e);
        } catch (final CacheException e) {
            throw new FolderException(e);
        }
    }

    /**
     * Disposes this folder cache on absent cache service.
     * 
     * @throws FolderException If disposal of this folder cache fails
     */
    public void onCacheAbsent() throws FolderException {
        final CacheService service = cacheService;
        final Cache cache = globalCache;
        if (cache != null) {
            try {
                cache.clear();
                if (null != service) {
                    service.freeCache("GlobalFolderCache");
                }
            } catch (final CacheException e) {
                throw new FolderException(e);
            } finally {
                globalCache = null;
            }
        }
        /*-
         * 
        if (userCache != null) {
            try {
                userCache.clear();
                if (null != cacheService) {
                    cacheService.freeCache("UserFolderCache");
                }
            } catch (final CacheException e) {
                throw new FolderException(e);
            } finally {
                userCache = null;
            }
        }
        */
        if (service != null) {
            cacheService = null;
        }
    }

    public void checkConsistency(final String treeId, final StorageParameters storageParameters) throws FolderException {
        for (final FolderStorage folderStorage : registry.getFolderStoragesForTreeID(treeId)) {
            final boolean started = folderStorage.startTransaction(storageParameters, true);
            try {
                folderStorage.checkConsistency(treeId, storageParameters);
                if (started) {
                    folderStorage.commitTransaction(storageParameters);
                }
            } catch (final FolderException e) {
                if (started) {
                    folderStorage.rollback(storageParameters);
                }
                throw e;
            } catch (final Exception e) {
                if (started) {
                    folderStorage.rollback(storageParameters);
                }
                throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }
    }

    public void restore(final String treeId, final String folderId, final StorageParameters storageParameters) throws FolderException {
        final FolderStorage storage = registry.getFolderStorage(treeId, folderId);
        if (null == storage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, folderId);
        }
        final boolean started = storage.startTransaction(storageParameters, false);
        try {
            storage.restore(treeId, folderId, storageParameters);
            if (started) {
                storage.commitTransaction(storageParameters);
            }
        } catch (final FolderException e) {
            if (started) {
                storage.rollback(storageParameters);
            }
            throw e;
        } catch (final Exception e) {
            if (started) {
                storage.rollback(storageParameters);
            }
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            clear(storageParameters.getSession());
        }
    }

    public Folder prepareFolder(final String treeId, final Folder folder, final StorageParameters storageParameters) throws FolderException {
        final String folderId = folder.getID();
        final FolderStorage storage = registry.getFolderStorage(treeId, folderId);
        if (null == storage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, folderId);
        }
        final boolean started = storage.startTransaction(storageParameters, false);
        try {
            final Folder preparedFolder = storage.prepareFolder(treeId, folder, storageParameters);
            if (started) {
                storage.commitTransaction(storageParameters);
            }
            if (preparedFolder.isCacheable() && preparedFolder.isGlobalID() != folder.isGlobalID()) {
                putFolder(preparedFolder, treeId, storageParameters);
            }
            return preparedFolder;
        } catch (final FolderException e) {
            if (started) {
                storage.rollback(storageParameters);
            }
            throw e;
        } catch (final Exception e) {
            if (started) {
                storage.rollback(storageParameters);
            }
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private PathPerformer newPathPerformer(final StorageParameters storageParameters) throws FolderException {
        final Session session = storageParameters.getSession();
        if (null == session) {
            return new PathPerformer(storageParameters.getUser(), storageParameters.getContext(), null, registry);
        }
        try {
            return new PathPerformer(new ServerSessionAdapter(session), null, registry);
        } catch (final ContextException e) {
            throw new FolderException(e);
        }
    }

    public ContentType getDefaultContentType() {
        return null;
    }

    public void commitTransaction(final StorageParameters params) throws FolderException {
        // Nothing to do
    }

    public void createFolder(final Folder folder, final StorageParameters storageParameters) throws FolderException {
        final Session session = storageParameters.getSession();
        /*
         * Perform create operation via non-cache storage
         */
        final String folderId;
        if (null == session) {
            folderId = new CreatePerformer(storageParameters.getUser(), storageParameters.getContext(), registry).doCreate(folder);
        } else {
            try {
                folderId = new CreatePerformer(new ServerSessionAdapter(session), registry).doCreate(folder);
            } catch (final ContextException e) {
                throw new FolderException(e);
            }
        }
        /*
         * Get folder from appropriate storage
         */
        final String treeId = folder.getTreeID();
        final FolderStorage storage = registry.getFolderStorage(treeId, folderId);
        if (null == storage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, folderId);
        }
        final Folder createdFolder = loadFolder(treeId, folderId, StorageType.WORKING, storageParameters);
        if (createdFolder.isCacheable()) {
            putFolder(createdFolder, treeId, storageParameters);
        }
        /*
         * Refresh parent
         */
        removeSingleFromCache(folder.getParentID(), treeId, storageParameters.getUserId(), storageParameters.getSession());
        final Folder parentFolder = loadFolder(treeId, folder.getParentID(), StorageType.WORKING, storageParameters);
        if (parentFolder.isCacheable()) {
            putFolder(parentFolder, treeId, storageParameters);
        }
    }

    private void putFolder(final Folder folder, final String treeId, final StorageParameters storageParameters) throws FolderException {
        /*
         * Put to cache
         */
        try {
            if (folder.isGlobalID()) {
                globalCache.putInGroup(newCacheKey(folder.getID(), treeId), String.valueOf(storageParameters.getContextId()), folder);
            } else {
                final FolderMap folderMap = getFolderMapFrom(storageParameters.getSession());
                if (null != folderMap) {
                    folderMap.put(treeId, folder);
                }
            }
        } catch (final CacheException e) {
            throw new FolderException(e);
        }
    }

    /**
     * Removes specified folder and all of its predecessor folders from cache.
     * 
     * @param id The folder identifier
     * @param treeId The tree identifier
     * @param singleOnly <code>true</code> if only specified folder should be removed; otherwise <code>false</code> for complete folder's path to root folder
     * @param session The session providing user information
     * @throws FolderException If removal fails
     */
    public void removeFromCache(final String id, final String treeId, final boolean singleOnly, final Session session) throws FolderException {
        if (singleOnly) {
            removeSingleFromCache(id, treeId, session.getUserId(), session);
        } else {
            try {
                removeFromCache(id, treeId, session, new PathPerformer(
                    new ServerSessionAdapter(session),
                    null,
                    registry));
            } catch (final ContextException e) {
                throw new FolderException(e);
            }
        }
    }

    private void removeFromCache(final String id, final String treeId, final Session session, final PathPerformer pathPerformer) throws FolderException {
        if (null == id) {
            return;
        }
        {
            List<String> ids;
            try {
                if (existsFolder(treeId, id, StorageType.WORKING, pathPerformer.getStorageParameters())) {
                    final UserizedFolder[] path = pathPerformer.doPath(treeId, id, true);
                    ids = new ArrayList<String>(path.length);
                    for (final UserizedFolder userizedFolder : path) {
                        ids.add(userizedFolder.getID());
                    }
                } else {
                    ids = Collections.singletonList(id);
                }
            } catch (final Exception e) {
                final org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(CacheFolderStorage.class);
                if (log.isDebugEnabled()) {
                    log.debug(e.getMessage(), e);
                }
                try {
                    ids = new ArrayList<String>(Arrays.asList(pathPerformer.doForcePath(treeId, id, true)));
                } catch (final Exception e1) {
                    if (log.isDebugEnabled()) {
                        log.debug(e1.getMessage(), e1);
                    }
                    ids = Collections.singletonList(id);
                }
            }
            final int contextId = session.getContextId();
            final FolderMap folderMap = optFolderMapFrom(session);
            final Cache cache = globalCache;
            if (FolderStorage.REAL_TREE_ID.equals(treeId)) {
                for (final String folderId : ids) {
                    cache.removeFromGroup(newCacheKey(folderId, treeId), String.valueOf(contextId));
                    if (null != folderMap) {
                        folderMap.remove(folderId, treeId);
                    }
                }
            } else {
                for (final String folderId : ids) {
                    cache.removeFromGroup(newCacheKey(folderId, treeId), String.valueOf(contextId));
                    if (null != folderMap) {
                        folderMap.remove(folderId, treeId);
                    }
                    // Now for real tree, too
                    cache.removeFromGroup(newCacheKey(folderId, FolderStorage.REAL_TREE_ID), String.valueOf(contextId));
                    if (null != folderMap) {
                        folderMap.remove(folderId, FolderStorage.REAL_TREE_ID);
                    }
                }
            }
        }
    }

    /**
     * Removes a single folder from cache.
     * 
     * @param id The folder identifier
     * @param treeId The tree identifier
     * @param session The session
     */
    public void removeSingleFromCache(final String id, final String treeId, final int userId, final Session session) {
        removeSingleFromCache(id, treeId, userId, session.getContextId(), session);
    }

    /**
     * Removes a single folder from cache.
     * 
     * @param id The folder identifier
     * @param treeId The tree identifier
     * @param contextId The context identifier
     * @param session The session
     */
    public void removeSingleFromCache(final String id, final String treeId, final int userId, final int contextId, final Session session) {
        final Cache cache = globalCache;
        cache.removeFromGroup(newCacheKey(id, treeId), String.valueOf(contextId));
        if (userId > 0) {
            final FolderMap folderMap = optFolderMapFrom(session);
            if (null != folderMap) {
                folderMap.remove(id, treeId);
            }
        }
        if (!FolderStorage.REAL_TREE_ID.equals(treeId)) {
            // Now for real tree, too
            cache.removeFromGroup(newCacheKey(id, FolderStorage.REAL_TREE_ID), String.valueOf(contextId));
            if (userId > 0) {
                final FolderMap folderMap = optFolderMapFrom(session);
                if (null != folderMap) {
                    folderMap.remove(id, FolderStorage.REAL_TREE_ID);
                }
            }
        }
    }

    public void clearFolder(final String treeId, final String folderId, final StorageParameters storageParameters) throws FolderException {
        final Session session = storageParameters.getSession();
        /*
         * Perform clear operation via non-cache storage
         */
        if (null == session) {
            new ClearPerformer(storageParameters.getUser(), storageParameters.getContext(), registry).doClear(treeId, folderId);
        } else {
            try {
                new ClearPerformer(new ServerSessionAdapter(session), registry).doClear(treeId, folderId);
            } catch (final ContextException e) {
                throw new FolderException(e);
            }
        }
    }

    public void deleteFolder(final String treeId, final String folderId, final StorageParameters storageParameters) throws FolderException {
        final String parentId;
        final String realParentId;
        final boolean cacheable;
        final boolean global;
        {
            final Folder deleteMe = getFolder(treeId, folderId, storageParameters);
            cacheable = deleteMe.isCacheable();
            global = deleteMe.isGlobalID();
            parentId = deleteMe.getParentID();
            if (!FolderStorage.REAL_TREE_ID.equals(treeId)) {
                final StorageParameters parameters = newStorageParameters(storageParameters);
                final FolderStorage folderStorage = registry.getFolderStorage(FolderStorage.REAL_TREE_ID, folderId);
                final boolean started = folderStorage.startTransaction(parameters, false);
                try {
                    realParentId = folderStorage.getFolder(FolderStorage.REAL_TREE_ID, folderId, parameters).getParentID();
                    if (started) {
                        folderStorage.commitTransaction(parameters);
                    }
                } catch (final FolderException e) {
                    if (started) {
                        folderStorage.rollback(parameters);
                    }
                    throw e;
                } catch (final Exception e) {
                    if (started) {
                        folderStorage.rollback(parameters);
                    }
                    throw FolderException.newUnexpectedException(e);
                }
            } else {
                realParentId = null;
            }
        }
        final Session session = storageParameters.getSession();
        /*
         * Perform delete
         */
        if (null == session) {
            new DeletePerformer(storageParameters.getUser(), storageParameters.getContext(), registry).doDelete(
                treeId,
                folderId,
                storageParameters.getTimeStamp());
        } else {
            try {
                new DeletePerformer(new ServerSessionAdapter(session), registry).doDelete(
                    treeId,
                    folderId,
                    storageParameters.getTimeStamp());
            } catch (final ContextException e) {
                throw new FolderException(e);
            }
        }
        if (cacheable) {
            /*
             * Delete from cache
             */
            if (global) {
                globalCache.removeFromGroup(newCacheKey(folderId, treeId), String.valueOf(storageParameters.getContextId()));
            } else {
                final FolderMap folderMap = optFolderMapFrom(session);
                if (null != folderMap) {
                    folderMap.remove(folderId, treeId);
                }
            }
        }
        /*
         * Refresh
         */
        if (null != realParentId && !FolderStorage.ROOT_ID.equals(realParentId)) {
            removeFromCache(realParentId, treeId, storageParameters.getSession(), newPathPerformer(storageParameters));
        }
        if (!FolderStorage.ROOT_ID.equals(parentId)) {
            removeFromCache(parentId, treeId, storageParameters.getSession(), newPathPerformer(storageParameters));
            final Folder parentFolder = loadFolder(treeId, parentId, StorageType.WORKING, storageParameters);
            if (parentFolder.isCacheable()) {
                putFolder(parentFolder, treeId, storageParameters);
            }
        }
    }

    public String getDefaultFolderID(final User user, final String treeId, final ContentType contentType, final Type type, final StorageParameters storageParameters) throws FolderException {
        final FolderStorage storage = registry.getFolderStorageByContentType(treeId, contentType);
        if (null == storage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_CT.create(treeId, contentType);
        }
        final String folderId;
        final boolean started = storage.startTransaction(storageParameters, false);
        try {
            folderId = storage.getDefaultFolderID(user, treeId, contentType, type, storageParameters);
            if (started) {
                storage.commitTransaction(storageParameters);
            }
        } catch (final FolderException e) {
            if (started) {
                storage.rollback(storageParameters);
            }
            throw e;
        } catch (final Exception e) {
            if (started) {
                storage.rollback(storageParameters);
            }
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
        return folderId;
    }

    public Type getTypeByParent(final User user, final String treeId, final String parentId, final StorageParameters storageParameters) throws FolderException {
        final FolderStorage storage = registry.getFolderStorage(treeId, parentId);
        if (null == storage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, parentId);
        }
        final Type type;
        final boolean started = storage.startTransaction(storageParameters, false);
        try {
            type = storage.getTypeByParent(user, treeId, parentId, storageParameters);
            if (started) {
                storage.commitTransaction(storageParameters);
            }
        } catch (final FolderException e) {
            if (started) {
                storage.rollback(storageParameters);
            }
            throw e;
        } catch (final Exception e) {
            if (started) {
                storage.rollback(storageParameters);
            }
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
        return type;
    }

    public boolean containsForeignObjects(final User user, final String treeId, final String folderId, final StorageParameters storageParameters) throws FolderException {
        /*
         * Get folder storage
         */
        final FolderStorage storage = registry.getFolderStorage(treeId, folderId);
        if (null == storage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, folderId);
        }
        final boolean started = storage.startTransaction(storageParameters, false);
        try {
            final boolean containsForeignObjects = storage.containsForeignObjects(user, treeId, folderId, storageParameters);
            if (started) {
                storage.commitTransaction(storageParameters);
            }
            return containsForeignObjects;
        } catch (final FolderException e) {
            if (started) {
                storage.rollback(storageParameters);
            }
            throw e;
        } catch (final Exception e) {
            if (started) {
                storage.rollback(storageParameters);
            }
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    public boolean isEmpty(final String treeId, final String folderId, final StorageParameters storageParameters) throws FolderException {
        /*
         * Get folder storage
         */
        final FolderStorage storage = registry.getFolderStorage(treeId, folderId);
        if (null == storage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, folderId);
        }
        final boolean started = storage.startTransaction(storageParameters, false);
        try {
            final boolean isEmpty = storage.isEmpty(treeId, folderId, storageParameters);
            if (started) {
                storage.commitTransaction(storageParameters);
            }
            return isEmpty;
        } catch (final FolderException e) {
            if (started) {
                storage.rollback(storageParameters);
            }
            throw e;
        } catch (final Exception e) {
            if (started) {
                storage.rollback(storageParameters);
            }
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    public void updateLastModified(final long lastModified, final String treeId, final String folderId, final StorageParameters storageParameters) throws FolderException {
        final FolderStorage storage = registry.getFolderStorage(treeId, folderId);
        if (null == storage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, folderId);
        }
        final boolean started = storage.startTransaction(storageParameters, false);
        try {
            storage.updateLastModified(lastModified, treeId, folderId, storageParameters);
            if (started) {
                storage.commitTransaction(storageParameters);
            }
        } catch (final FolderException e) {
            if (started) {
                storage.rollback(storageParameters);
            }
            throw e;
        } catch (final Exception e) {
            if (started) {
                storage.rollback(storageParameters);
            }
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
        /*
         * Invalidate cache entry
         */
        removeFromCache(folderId, treeId, storageParameters.getSession(), newPathPerformer(storageParameters));
    }

    public Folder getFolder(final String treeId, final String folderId, final StorageParameters storageParameters) throws FolderException {
        return getFolder(treeId, folderId, StorageType.WORKING, storageParameters);
    }

    public List<Folder> getFolders(final String treeId, final List<String> folderIds, final StorageParameters storageParameters) throws FolderException {
        return getFolders(treeId, folderIds, StorageType.WORKING, storageParameters);
    }

    public Folder getFolder(final String treeId, final String folderId, final StorageType storageType, final StorageParameters storageParameters) throws FolderException {
        /*
         * Try from cache
         */
        Folder folder = getCloneFromCache(treeId, folderId, storageParameters);
        if (null != folder) {
            return folder;
        }
        /*
         * Load folder from appropriate storage
         */
        folder = loadFolder(treeId, folderId, storageType, storageParameters);
        /*
         * Check if folder is cacheable
         */
        if (folder.isCacheable()) {
            /*
             * Put to cache and return a cloned version
             */
            putFolder(folder, treeId, storageParameters);
            return (Folder) folder.clone();
        }
        /*
         * Return as-is since not cached
         */
        return folder;
    }

    private Folder getCloneFromCache(final String treeId, final String folderId, final StorageParameters storageParameters) {
        final int contextId = storageParameters.getContextId();
        /*
         * Try global cache key
         */
        Folder folder = (Folder) globalCache.getFromGroup(newCacheKey(folderId, treeId), String.valueOf(contextId));
        if (null != folder) {
            /*
             * Return a cloned version from global cache
             */
            return (Folder) folder.clone();
        }
        /*
         * Try user cache key
         */
        final FolderMap folderMap = optFolderMapFrom(storageParameters.getSession());
        folder = null == folderMap ? null : folderMap.get(folderId, treeId);
        if (null != folder) {
            /*
             * Return a cloned version from user-bound cache
             */
            return (Folder) folder.clone();
        }
        /*
         * Cache miss
         */
        return null;
    }

    public List<Folder> getFolders(final String treeId, final List<String> folderIds, final StorageType storageType, final StorageParameters storageParameters) throws FolderException {
        final int size = folderIds.size();
        final Folder[] ret = new Folder[size];
        final TObjectIntHashMap<String> toLoad = new TObjectIntHashMap<String>(size);
        /*
         * Get the ones from cache
         */
        for (int i = 0; i < size; i++) {
            /*
             * Try from cache
             */
            final String folderId = folderIds.get(i);
            final Folder folder = getCloneFromCache(treeId, folderId, storageParameters);
            if (null == folder) {
                /*
                 * Cache miss; Load from storage
                 */
                toLoad.put(folderId, i);
            } else {
                /*
                 * Cache hit
                 */
                ret[i] = folder;
            }
        }
        /*
         * Load the ones from storage
         */
        final Map<String, Folder> fromStorage;
        if (toLoad.isEmpty()) {
            fromStorage = Collections.emptyMap();
        } else {
            fromStorage = loadFolders(treeId, Arrays.asList(toLoad.keys(new String[toLoad.size()])), storageType, storageParameters);
        }
        /*
         * Fill return value
         */
        for (final Entry<String, Folder> entry : fromStorage.entrySet()) {
            Folder folder = entry.getValue();
            final int index = toLoad.get(entry.getKey());
            /*
             * Put into cache
             */
            if (folder.isCacheable()) {
                /*
                 * Put to cache and create a cloned version
                 */
                putFolder(folder, treeId, storageParameters);
                folder = (Folder) folder.clone();
            }
            ret[index] = folder;
        }
        /*
         * Return
         */
        final List<Folder> l = new ArrayList<Folder>(ret.length);
        for (final Folder folder : ret) {
            if (null != folder) {
                l.add(folder);
            }
        }
        return l;
    }

    public FolderType getFolderType() {
        return CacheFolderType.getInstance();
    }

    public StoragePriority getStoragePriority() {
        return StoragePriority.HIGHEST;
    }

    public SortableId[] getVisibleFolders(final String treeId, final ContentType contentType, final Type type, final StorageParameters storageParameters) throws FolderException {
        
        final FolderStorage folderStorage = registry.getFolderStorageByContentType(treeId, contentType);
        if (null == folderStorage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_CT.create(treeId, contentType);
        }
        final boolean started = folderStorage.startTransaction(storageParameters, true);
        try {
            final SortableId[] ret = folderStorage.getVisibleFolders(treeId, contentType, type, storageParameters);
            if (started) {
                folderStorage.commitTransaction(storageParameters);
            }
            return ret;
        } catch (final FolderException e) {
            if (started) {
                folderStorage.rollback(storageParameters);
            }
            throw e;
        } catch (final Exception e) {
            if (started) {
                folderStorage.rollback(storageParameters);
            }
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    public SortableId[] getSubfolders(final String treeId, final String parentId, final StorageParameters storageParameters) throws FolderException {
        final Folder parent = getFolder(treeId, parentId, storageParameters);
        final String[] subfolders = parent.getSubfolderIDs();
        final SortableId[] ret;
        if (null == subfolders) {
            /*
             * Get needed storages
             */
            final FolderStorage[] neededStorages = registry.getFolderStoragesForParent(treeId, parentId);
            if (0 == neededStorages.length) {
                return new SortableId[0];
            }
            try {
                final java.util.List<SortableId> allSubfolderIds;
                if (1 == neededStorages.length) {
                    final FolderStorage neededStorage = neededStorages[0];
                    final boolean started = neededStorage.startTransaction(storageParameters, false);
                    try {
                        allSubfolderIds = Arrays.asList(neededStorage.getSubfolders(treeId, parentId, storageParameters));
                        if (started) {
                            neededStorage.commitTransaction(storageParameters);
                        }
                    } catch (final Exception e) {
                        if (started) {
                            neededStorage.rollback(storageParameters);
                        }
                        throw e;
                    }
                } else {
                    allSubfolderIds = new ArrayList<SortableId>(neededStorages.length * 8);
                    final CompletionService<java.util.List<SortableId>> completionService =
                        new ThreadPoolCompletionService<java.util.List<SortableId>>(CacheServiceRegistry.getServiceRegistry().getService(
                            ThreadPoolService.class));
                    /*
                     * Get all visible subfolders from each storage
                     */
                    for (final FolderStorage neededStorage : neededStorages) {
                        completionService.submit(new Callable<java.util.List<SortableId>>() {

                            public java.util.List<SortableId> call() throws Exception {
                                final StorageParameters newParameters = newStorageParameters(storageParameters);
                                final boolean started = neededStorage.startTransaction(newParameters, false);
                                try {
                                    final java.util.List<SortableId> l =
                                        Arrays.asList(neededStorage.getSubfolders(treeId, parentId, newParameters));
                                    if (started) {
                                        neededStorage.commitTransaction(newParameters);
                                    }
                                    return l;
                                } catch (final Exception e) {
                                    if (started) {
                                        neededStorage.rollback(newParameters);
                                    }
                                    throw e;
                                }
                            }
                        });
                    }
                    /*
                     * Wait for completion
                     */
                    final List<List<SortableId>> results =
                        ThreadPools.takeCompletionService(completionService, neededStorages.length, FACTORY);
                    for (final List<SortableId> result : results) {
                        allSubfolderIds.addAll(result);
                    }
                }
                /*
                 * Sort them
                 */
                Collections.sort(allSubfolderIds);
                ret = allSubfolderIds.toArray(new SortableId[allSubfolderIds.size()]);
            } catch (final FolderException e) {
                throw e;
            } catch (final Exception e) {
                throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        } else {
            ret = new SortableId[subfolders.length];
            for (int i = 0; i < ret.length; i++) {
                ret[i] = new CacheSortableId(subfolders[i], i, null);
            }
        }
        return ret;
    }

    public ContentType[] getSupportedContentTypes() {
        return new ContentType[0];
    }

    public void rollback(final StorageParameters params) {
        // Nothing to do
    }

    public boolean startTransaction(final StorageParameters parameters, final boolean modify) throws FolderException {
        return false;
    }

    public void updateFolder(final Folder folder, final StorageParameters storageParameters) throws FolderException {
        final Session session = storageParameters.getSession();
        /*
         * Perform update operation via non-cache storage
         */
        final String oldFolderId = folder.getID();
        final boolean isMove = null != folder.getParentID();
        final String oldParentId = isMove ? getFolder(folder.getTreeID(), oldFolderId, storageParameters).getParentID() : null;
        if (null == session) {
            new UpdatePerformer(storageParameters.getUser(), storageParameters.getContext(), registry).doUpdate(
                folder,
                storageParameters.getTimeStamp());
        } else {
            try {
                new UpdatePerformer(new ServerSessionAdapter(session), registry).doUpdate(folder, storageParameters.getTimeStamp());
            } catch (final ContextException e) {
                throw new FolderException(e);
            }
        }
        /*
         * Get folder from appropriate storage
         */
        final String newFolderId = folder.getID();
        final String treeId = folder.getTreeID();
        /*
         * Refresh/Invalidate folder
         */
        final int userId = storageParameters.getUserId();
        if (isMove) {
            removeSingleFromCache(oldFolderId, treeId, userId, session);
            removeFromCache(oldParentId, treeId, session, newPathPerformer(storageParameters));
        } else {
            removeFromCache(newFolderId, treeId, session, newPathPerformer(storageParameters));
        }
        /*
         * Put updated folder
         */
        final Folder updatedFolder = loadFolder(treeId, newFolderId, StorageType.WORKING, storageParameters);
        if (isMove) {
            /*
             * Invalidate new parent folder
             */
            final String newParentId = updatedFolder.getParentID();
            if (null != newParentId && !newParentId.equals(oldParentId)) {
                removeSingleFromCache(newParentId, treeId, userId, storageParameters.getSession());
            }
        }
        if (updatedFolder.isCacheable()) {
            putFolder(updatedFolder, treeId, storageParameters);
        }
    }

    public boolean containsFolder(final String treeId, final String folderId, final StorageParameters storageParameters) throws FolderException {
        return containsFolder(treeId, folderId, StorageType.WORKING, storageParameters);
    }

    public String[] getModifiedFolderIDs(final String treeId, final Date timeStamp, final ContentType[] includeContentTypes, final StorageParameters storageParameters) throws FolderException {
        return getChangedFolderIDs(0, treeId, timeStamp, includeContentTypes, storageParameters);
    }

    public String[] getDeletedFolderIDs(final String treeId, final Date timeStamp, final StorageParameters storageParameters) throws FolderException {
        return getChangedFolderIDs(1, treeId, timeStamp, null, storageParameters);
    }

    private String[] getChangedFolderIDs(final int index, final String treeId, final Date timeStamp, final ContentType[] includeContentTypes, final StorageParameters storageParameters) throws FolderException {
        final Session session = storageParameters.getSession();
        /*
         * Perform update operation via non-cache storage
         */
        final UserizedFolder[] folders;
        final boolean ignoreDelete = index == 0;
        if (null == session) {
            folders =
                new UpdatesPerformer(
                    storageParameters.getUser(),
                    storageParameters.getContext(),
                    storageParameters.getDecorator(),
                    registry).doUpdates(treeId, timeStamp, ignoreDelete, includeContentTypes)[index];
        } else {
            try {
                folders =
                    new UpdatesPerformer(new ServerSessionAdapter(session), storageParameters.getDecorator(), registry).doUpdates(
                        treeId,
                        timeStamp,
                        ignoreDelete,
                        includeContentTypes)[index];
            } catch (final ContextException e) {
                throw new FolderException(e);
            }
        }
        if (null == folders || folders.length == 0) {
            return new String[0];
        }
        final String[] ids = new String[folders.length];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = folders[i].getID();
        }
        return ids;
    }

    public boolean containsFolder(final String treeId, final String folderId, final StorageType storageType, final StorageParameters storageParameters) throws FolderException {
        final FolderStorage storage = registry.getFolderStorage(treeId, folderId);
        if (null == storage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, folderId);
        }
        final boolean started = storage.startTransaction(storageParameters, true);
        try {
            final boolean contains = storage.containsFolder(treeId, folderId, storageType, storageParameters);
            if (started) {
                storage.commitTransaction(storageParameters);
            }
            return contains;
        } catch (final FolderException e) {
            if (started) {
                storage.rollback(storageParameters);
            }
            throw e;
        } catch (final Exception e) {
            if (started) {
                storage.rollback(storageParameters);
            }
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /*-
     * ++++++++++++++++++++++++++++++++++++ ++ + HELPERS + ++ ++++++++++++++++++++++++++++++++++++
     */

    /**
     * Creates the cache key for specified folder ID and tree ID pair.
     * 
     * @param folderId The folder ID
     * @param treeId The tree ID
     * @return The cache key
     */
    private CacheKey newCacheKey(final String folderId, final String treeId) {
        return cacheService.newCacheKey(1, treeId, folderId);
    }

    /**
     * Creates a user-bound key.
     */
    private CacheKey newCacheKey(final String folderId, final String treeId, final int cid, final int user) {
        return cacheService.newCacheKey(cid, Integer.valueOf(user), treeId, folderId);
    }

    private boolean existsFolder(final String treeId, final String folderId, final StorageType storageType, final StorageParameters storageParameters) throws FolderException {
        final FolderStorage storage = registry.getFolderStorage(treeId, folderId);
        if (null == storage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, folderId);
        }
        final boolean started = storage.startTransaction(storageParameters, false);
        try {
            final boolean exists = storage.containsFolder(treeId, folderId, storageType, storageParameters);
            if (started) {
                storage.commitTransaction(storageParameters);
            }
            return exists;
        } catch (final FolderException e) {
            if (started) {
                storage.rollback(storageParameters);
            }
            throw e;
        } catch (final Exception e) {
            if (started) {
                storage.rollback(storageParameters);
            }
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private Folder loadFolder(final String treeId, final String folderId, final StorageType storageType, final StorageParameters storageParameters) throws FolderException {
        final FolderStorage storage = registry.getFolderStorage(treeId, folderId);
        if (null == storage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, folderId);
        }
        final boolean started = storage.startTransaction(storageParameters, false);
        try {
            final Folder folder = storage.getFolder(treeId, folderId, storageType, storageParameters);
            if (started) {
                storage.commitTransaction(storageParameters);
            }
            return folder;
        } catch (final FolderException e) {
            if (started) {
                storage.rollback(storageParameters);
            }
            throw e;
        } catch (final Exception e) {
            if (started) {
                storage.rollback(storageParameters);
            }
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private Map<String, Folder> loadFolders(final String treeId, final List<String> folderIds, final StorageType storageType, final StorageParameters storageParameters) throws FolderException {
        /*
         * Collect by folder storage
         */
        final int size = folderIds.size();
        final Map<FolderStorage, TIntArrayList> map = new HashMap<FolderStorage, TIntArrayList>(4);
        for (int i = 0; i < size; i++) {
            final String id = folderIds.get(i);
            final FolderStorage tmp = registry.getFolderStorage(treeId, id);
            if (null == tmp) {
                throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, id);
            }
            TIntArrayList list = map.get(tmp);
            if (null == list) {
                list = new TIntArrayList();
                map.put(tmp, list);
            }
            list.add(i);
        }
        /*
         * Process by folder storage
         */
        final CompletionService<Object> completionService;
        final StorageParametersProvider paramsProvider;
        if (1 == map.size()) {
            completionService = new CallerRunsCompletionService<Object>();
            paramsProvider = new InstanceStorageParametersProvider(storageParameters);
        } else {
            try {
                completionService = new ThreadPoolCompletionService<Object>(CacheServiceRegistry.getServiceRegistry().getService(ThreadPoolService.class, true));
            } catch (final ServiceException e) {
                throw new FolderException(e);
            }
            final Session session = storageParameters.getSession();
            paramsProvider = null == session ? new SessionStorageParametersProvider(storageParameters.getUser(), storageParameters.getContext()) : new SessionStorageParametersProvider((ServerSession) storageParameters.getSession());
        }
        /*
         * Create destination map
         */
        final Map<String, Folder> ret = new ConcurrentHashMap<String, Folder>(size);
        int taskCount = 0;
        for (final java.util.Map.Entry<FolderStorage, TIntArrayList> entry : map.entrySet()) {
            final FolderStorage tmp = entry.getKey();
            final int[] indexes = entry.getValue().toNativeArray();
            completionService.submit(new Callable<Object>() {

                public Object call() throws Exception {
                    final StorageParameters newParameters = paramsProvider.getStorageParameters();
                    final List<FolderStorage> openedStorages = new ArrayList<FolderStorage>(2);
                    if (tmp.startTransaction(newParameters, false)) {            
                        openedStorages.add(tmp);
                    }
                    try {
                        /*
                         * Create the list of IDs to load with current storage
                         */
                        final List<String> ids = new ArrayList<String>(indexes.length);
                        for (final int index : indexes) {
                            ids.add(folderIds.get(index));
                        }
                        /*
                         * Load them & commit
                         */
                        final List<Folder> folders = tmp.getFolders(treeId, ids, storageType, newParameters);
                        for (final FolderStorage fs : openedStorages) {
                            fs.commitTransaction(newParameters);
                        }
                        /*
                         * Fill into map
                         */
                        for (final Folder folder : folders) {
                            ret.put(folder.getID(), folder);
                        }
                        /*
                         * Return
                         */
                        return null;
                    } catch (final FolderException e) {
                        for (final FolderStorage fs : openedStorages) {
                            fs.rollback(newParameters);
                        }
                        throw e;
                    } catch (final Exception e) {
                        for (final FolderStorage fs : openedStorages) {
                            fs.rollback(newParameters);
                        }
                        throw FolderException.newUnexpectedException(e);
                    }
                }
            });
            taskCount++;
        }
        /*
         * Wait for completion
         */
        ThreadPools.takeCompletionService(completionService, taskCount, FACTORY);
        return ret;
    }

    /**
     * Creates a new storage parameter instance.
     * 
     * @return A new storage parameter instance.
     */
    static StorageParameters newStorageParameters(final StorageParameters source) {
        final Session session = source.getSession();
        if (null == session) {
            return new StorageParametersImpl(source.getUser(), source.getContext());
        }
        return new StorageParametersImpl((ServerSession) session);
    }

    private static FolderMap getFolderMapFrom(final Session session) {
        return getFolderMapFrom(session, true);
    }

    private static FolderMap optFolderMapFrom(final Session session) {
        return getFolderMapFrom(session, false);
    }

    private static final String PARAM_FOLDER_MAP = "com.openexchange.folderstorage.cache.folderMap";

    private static FolderMap getFolderMapFrom(final Session session, final boolean createIfAbsent) {
        if (null == session) {
            return null;
        }
        if (!createIfAbsent) {
            return (FolderMap) session.getParameter(PARAM_FOLDER_MAP);
        }
        FolderMap map = (FolderMap) session.getParameter(PARAM_FOLDER_MAP);
        if (null == map) {
            final Lock lock = (Lock) session.getParameter(Session.PARAM_LOCK);
            if (null == lock) {
                synchronized (session) {
                    map = (FolderMap) session.getParameter(PARAM_FOLDER_MAP);
                    if (null == map) {
                        map = new FolderMap(1024, 300, TimeUnit.SECONDS);
                        session.setParameter(PARAM_FOLDER_MAP, map);
                    }
                }
            } else {
                lock.lock();
                try {
                    map = (FolderMap) session.getParameter(PARAM_FOLDER_MAP);
                    if (null == map) {
                        map = new FolderMap(1024, 300, TimeUnit.SECONDS);
                        session.setParameter(PARAM_FOLDER_MAP, map);
                    }
                } finally {
                    lock.unlock();
                }
            }
        }
        return map;
    }

    /**
     * Drops entries associated with specified user in given context.
     * 
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    public static void dropUserEntries(final int userId, final int contextId) {
        final SessiondService service = ServerServiceRegistry.getInstance().getService(SessiondService.class);
        if (null != service) {
            for (final Session session : service.getSessions(userId, contextId)) {
                final Lock lock = (Lock) session.getParameter(Session.PARAM_LOCK);
                if (null == lock) {
                    synchronized (session) {
                        final FolderMap map = (FolderMap) session.getParameter(PARAM_FOLDER_MAP);
                        if (null != map) {
                            map.clear();
                        }
                    }
                } else {
                    lock.lock();
                    try {
                        final FolderMap map = (FolderMap) session.getParameter(PARAM_FOLDER_MAP);
                        if (null != map) {
                            map.clear();
                        }
                    } finally {
                        lock.unlock();
                    }
                }
            }
            
        }
    }

}

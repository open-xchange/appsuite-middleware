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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheException;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheService;
import com.openexchange.config.ConfigurationService;
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
import com.openexchange.folderstorage.internal.StorageParametersImpl;
import com.openexchange.folderstorage.internal.performers.ClearPerformer;
import com.openexchange.folderstorage.internal.performers.CreatePerformer;
import com.openexchange.folderstorage.internal.performers.DeletePerformer;
import com.openexchange.folderstorage.internal.performers.PathPerformer;
import com.openexchange.folderstorage.internal.performers.UpdatePerformer;
import com.openexchange.folderstorage.internal.performers.UpdatesPerformer;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.server.ServiceException;
import com.openexchange.session.Session;
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

    private CacheService cacheService;

    private Cache globalCache;

    private Cache userCache;

    /**
     * Initializes a new {@link CacheFolderStorage}.
     */
    public CacheFolderStorage() {
        super();
        registry = CacheFolderStorageRegistry.getInstance();
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
            userCache = cacheService.getCache("UserFolderCache");
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
        if (globalCache != null) {
            try {
                globalCache.clear();
                if (null != cacheService) {
                    cacheService.freeCache("GlobalFolderCache");
                }
            } catch (final CacheException e) {
                throw new FolderException(e);
            } finally {
                globalCache = null;
            }
        }
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
        if (cacheService != null) {
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
            removeFromCache(folderId, treeId, storageParameters.getUserId(), storageParameters.getContextId(), newPathPerformer(storageParameters));
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
        removeSingleFromCache(folder.getParentID(), treeId, storageParameters.getUserId(), storageParameters.getContextId());
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
            final CacheKey key;
            final String id = folder.getID();
            if (folder.isGlobalID()) {
                key = newCacheKey(id, treeId, storageParameters.getContextId());
                globalCache.put(key, folder);
            } else {
                key = newCacheKey(id, treeId, storageParameters.getContextId(), storageParameters.getUserId());
                userCache.put(key, folder);
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
            removeSingleFromCache(id, treeId, session.getUserId(), session.getContextId());
        } else {
            try {
                removeFromCache(id, treeId, session.getUserId(), session.getContextId(), new PathPerformer(
                    new ServerSessionAdapter(session),
                    null,
                    registry));
            } catch (final ContextException e) {
                throw new FolderException(e);
            }
        }
    }

    private void removeFromCache(final String id, final String treeId, final int userId, final int contextId, final PathPerformer pathPerformer) throws FolderException {
        if (null == id) {
            return;
        }
        try {
            final List<String> ids;
            try {
                final UserizedFolder[] path = pathPerformer.doPath(treeId, id, true);
                ids = new ArrayList<String>(path.length);
                for (final UserizedFolder userizedFolder : path) {
                    ids.add(userizedFolder.getID());
                }
            } catch (final Exception e) {
                removeSingleFromCache(id, treeId, userId, contextId);
                return;
            }
            if (FolderStorage.REAL_TREE_ID.equals(treeId)) {
                for (final String folderId : ids) {
                    globalCache.remove(newCacheKey(folderId, treeId, contextId));
                    userCache.remove(newCacheKey(folderId, treeId, contextId, userId));
                }
            } else {
                for (final String folderId : ids) {
                    globalCache.remove(newCacheKey(folderId, treeId, contextId));
                    userCache.remove(newCacheKey(folderId, treeId, contextId, userId));
                    // Now for real tree, too
                    globalCache.remove(newCacheKey(folderId, FolderStorage.REAL_TREE_ID, contextId));
                    userCache.remove(newCacheKey(folderId, FolderStorage.REAL_TREE_ID, contextId, userId));
                }
            }
        } catch (final CacheException e) {
            throw new FolderException(e);
        }
    }

    private void removeSingleFromCache(final String id, final String treeId, final int userId, final int contextId) throws FolderException {
        try {
            globalCache.remove(newCacheKey(id, treeId, contextId));
            userCache.remove(newCacheKey(id, treeId, contextId, userId));
            if (!FolderStorage.REAL_TREE_ID.equals(treeId)) {
                // Now for real tree, too
                globalCache.remove(newCacheKey(id, FolderStorage.REAL_TREE_ID, contextId));
                userCache.remove(newCacheKey(id, FolderStorage.REAL_TREE_ID, contextId, userId));
            }
        } catch (final CacheException e) {
            throw new FolderException(e);
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
            try {
                /*
                 * Delete from cache
                 */
                if (global) {
                    final CacheKey key = newCacheKey(folderId, treeId, storageParameters.getContextId());
                    globalCache.remove(key);
                } else {
                    final CacheKey key = newCacheKey(folderId, treeId, storageParameters.getContextId(), storageParameters.getUserId());
                    userCache.remove(key);
                }
            } catch (final CacheException e) {
                throw new FolderException(e);
            }
        }
        /*
         * Refresh
         */
        if (null != realParentId && !FolderStorage.ROOT_ID.equals(realParentId)) {
            removeFromCache(realParentId, treeId, storageParameters.getUserId(), storageParameters.getContextId(), newPathPerformer(storageParameters));
        }
        if (!FolderStorage.ROOT_ID.equals(parentId)) {
            removeFromCache(parentId, treeId, storageParameters.getUserId(), storageParameters.getContextId(), newPathPerformer(storageParameters));
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
        removeFromCache(folderId, treeId, storageParameters.getUserId(), storageParameters.getContextId(), newPathPerformer(storageParameters));
    }

    public Folder getFolder(final String treeId, final String folderId, final StorageParameters storageParameters) throws FolderException {
        return getFolder(treeId, folderId, StorageType.WORKING, storageParameters);
    }

    public Folder getFolder(final String treeId, final String folderId, final StorageType storageType, final StorageParameters storageParameters) throws FolderException {
        final int contextId = storageParameters.getContextId();
        /*
         * Try global cache key
         */
        Folder folder = (Folder) globalCache.get(newCacheKey(folderId, treeId, contextId));
        if (null != folder) {
            /*
             * Return a cloned version from global cache
             */
            return (Folder) folder.clone();
        }
        /*
         * Try user cache key
         */
        folder = (Folder) userCache.get(newCacheKey(folderId, treeId, contextId, storageParameters.getUserId()));
        if (null != folder) {
            /*
             * Return a cloned version from user-bound cache
             */
            return (Folder) folder.clone();
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
                        ThreadPools.pollCompletionService(completionService, neededStorages.length, getMaxRunningMillis(), FACTORY);
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
        final int contextId = storageParameters.getContextId();
        if (isMove) {
            removeSingleFromCache(oldFolderId, treeId, userId, contextId);
            removeFromCache(oldParentId, treeId, userId, contextId, newPathPerformer(storageParameters));
        } else {
            removeFromCache(newFolderId, treeId, userId, contextId, newPathPerformer(storageParameters));
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
                removeSingleFromCache(newParentId, treeId, userId, contextId);
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
     * Creates a key.
     */
    private CacheKey newCacheKey(final String folderId, final String treeId, final int cid) {
        return cacheService.newCacheKey(cid, treeId, folderId);
    }

    /**
     * Creates a user-bound key.
     */
    private CacheKey newCacheKey(final String folderId, final String treeId, final int cid, final int user) {
        return cacheService.newCacheKey(cid, Integer.valueOf(user), treeId, folderId);
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

    private static final int DEFAULT_MAX_RUNNING_MILLIS = 120000;

    private int getMaxRunningMillis() {
        final ConfigurationService confService = CacheServiceRegistry.getServiceRegistry().getService(ConfigurationService.class);
        if (null == confService) {
            // Default of 2 minutes
            return DEFAULT_MAX_RUNNING_MILLIS;
        }
        // 2 * AJP_WATCHER_MAX_RUNNING_TIME
        return confService.getIntProperty("AJP_WATCHER_MAX_RUNNING_TIME", DEFAULT_MAX_RUNNING_MILLIS) * 2;
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

}

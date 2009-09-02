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

package com.openexchange.folderstorage.cache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
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
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.internal.StorageParametersImpl;
import com.openexchange.folderstorage.internal.actions.Clear;
import com.openexchange.folderstorage.internal.actions.Create;
import com.openexchange.folderstorage.internal.actions.Delete;
import com.openexchange.folderstorage.internal.actions.Update;
import com.openexchange.folderstorage.internal.actions.Updates;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.server.ServiceException;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link CacheFolderStorage} - The cache folder storage.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CacheFolderStorage implements FolderStorage {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(CacheFolderStorage.class);

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
            folderId = new Create(storageParameters.getUser(), storageParameters.getContext(), registry).doCreate(folder);
        } else {
            try {
                folderId = new Create(new ServerSessionAdapter(session), registry).doCreate(folder);
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
        putFolder(createdFolder, treeId, storageParameters);
        /*
         * Refresh parent
         */
        removeFolder(folder.getParentID(), treeId, storageParameters);
        final Folder parentFolder = loadFolder(treeId, folder.getParentID(), StorageType.WORKING, storageParameters);
        putFolder(parentFolder, treeId, storageParameters);
    }

    private void putFolder(final Folder folder, final String treeId, final StorageParameters storageParameters) throws FolderException {
        if (folder.isCacheable()) {
            /*
             * Put to cache
             */
            try {
                final CacheKey key;
                final String id = folder.getID();
                if (folder.isGlobalID()) {
                    key = newCacheKey(id, treeId, storageParameters.getContext().getContextId());
                    globalCache.put(key, folder);
                } else {
                    key = newCacheKey(id, treeId, storageParameters.getContext().getContextId(), storageParameters.getUser().getId());
                    userCache.put(key, folder);
                }
            } catch (final CacheException e) {
                throw new FolderException(e);
            }
        }
    }

    private void removeFolder(final String id, final String treeId, final StorageParameters storageParameters) throws FolderException {
        try {
            final int contextId = storageParameters.getContext().getContextId();
            final int userId = storageParameters.getUser().getId();
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
            new Clear(storageParameters.getUser(), storageParameters.getContext(), registry).doClear(treeId, folderId);
        } else {
            try {
                new Clear(new ServerSessionAdapter(session), registry).doClear(treeId, folderId);
            } catch (final ContextException e) {
                throw new FolderException(e);
            }
        }
    }

    public void deleteFolder(final String treeId, final String folderId, final StorageParameters storageParameters) throws FolderException {
        final String parentId;
        final boolean cacheable;
        final boolean global;
        {
            final Folder deleteMe = getFolder(treeId, folderId, storageParameters);
            cacheable = deleteMe.isCacheable();
            global = deleteMe.isGlobalID();
            parentId = deleteMe.getParentID();
        }
        final Session session = storageParameters.getSession();
        /*
         * Perform delete
         */
        if (null == session) {
            new Delete(storageParameters.getUser(), storageParameters.getContext(), registry).doDelete(
                treeId,
                folderId,
                storageParameters.getTimeStamp());
        } else {
            try {
                new Delete(new ServerSessionAdapter(session), registry).doDelete(treeId, folderId, storageParameters.getTimeStamp());
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
                    final CacheKey key = newCacheKey(folderId, treeId, storageParameters.getContext().getContextId());
                    globalCache.remove(key);
                } else {
                    final CacheKey key = newCacheKey(
                        folderId,
                        treeId,
                        storageParameters.getContext().getContextId(),
                        storageParameters.getUser().getId());
                    userCache.remove(key);
                }
            } catch (final CacheException e) {
                throw new FolderException(e);
            }
        }
        /*
         * Refresh parent
         */
        if (!FolderStorage.ROOT_ID.equals(parentId)) {
            removeFolder(parentId, treeId, storageParameters);
            final Folder parentFolder = loadFolder(treeId, parentId, StorageType.WORKING, storageParameters);
            putFolder(parentFolder, treeId, storageParameters);
        }
    }

    public String getDefaultFolderID(final User user, final String treeId, final ContentType contentType, final StorageParameters storageParameters) throws FolderException {
        final FolderStorage storage = registry.getFolderStorageByContentType(treeId, contentType);
        if (null == storage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_CT.create(treeId, contentType);
        }
        final String folderId;
        storage.startTransaction(storageParameters, false);
        try {
            folderId = storage.getDefaultFolderID(user, treeId, contentType, storageParameters);
            storage.commitTransaction(storageParameters);
        } catch (final FolderException e) {
            storage.rollback(storageParameters);
            throw e;
        }
        return folderId;
    }

    public Folder getFolder(final String treeId, final String folderId, final StorageParameters storageParameters) throws FolderException {
        return getFolder(treeId, folderId, StorageType.WORKING, storageParameters);
    }

    public Folder getFolder(final String treeId, final String folderId, final StorageType storageType, final StorageParameters storageParameters) throws FolderException {
        final int contextId = storageParameters.getContext().getContextId();
        /*
         * Try global cache key
         */
        CacheKey key = newCacheKey(folderId, treeId, contextId);
        Folder folder = (Folder) globalCache.get(key);
        if (null != folder) {
            /*
             * Return a cloned version
             */
            return (Folder) folder.clone();
        }
        /*
         * Try user cache key
         */
        key = newCacheKey(folderId, treeId, contextId, storageParameters.getUser().getId());
        folder = (Folder) userCache.get(key);
        if (null == folder) {
            folder = loadFolder(treeId, folderId, storageType, storageParameters);
            putFolder(folder, treeId, storageParameters);
        }
        /*
         * Return a cloned version
         */
        return (Folder) folder.clone();
    }

    public FolderType getFolderType() {
        return CacheFolderType.getInstance();
    }

    public StoragePriority getStoragePriority() {
        return StoragePriority.HIGHEST;
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
            try {
                final java.util.List<SortableId> allSubfolderIds = new ArrayList<SortableId>(neededStorages.length * 8);
                {
                    final CompletionService<java.util.List<SortableId>> completionService = new ExecutorCompletionService<java.util.List<SortableId>>(
                        CacheServiceRegistry.getServiceRegistry().getService(com.openexchange.timer.TimerService.class).getExecutor());
                    /*
                     * Get all visible subfolders from each storage
                     */
                    for (final FolderStorage neededStorage : neededStorages) {
                        completionService.submit(new Callable<java.util.List<SortableId>>() {

                            public java.util.List<SortableId> call() throws Exception {
                                final StorageParameters newParameters = newStorageParameters(storageParameters);
                                neededStorage.startTransaction(newParameters, false);
                                try {
                                    final java.util.List<SortableId> l = Arrays.asList(neededStorage.getSubfolders(
                                        treeId,
                                        parentId,
                                        newParameters));
                                    neededStorage.commitTransaction(newParameters);
                                    return l;
                                } catch (final Exception e) {
                                    neededStorage.rollback(newParameters);
                                    throw e;
                                }
                            }
                        });
                    }
                    /*
                     * Wait for completion
                     */
                    final int maxRunningMillis = getMaxRunningMillis();
                    try {
                        for (int i = neededStorages.length; i > 0; i--) {
                            final Future<java.util.List<SortableId>> f = completionService.poll(maxRunningMillis, TimeUnit.MILLISECONDS);
                            if (null != f) {
                                allSubfolderIds.addAll(f.get());
                            } else if (LOG.isWarnEnabled()) {
                                LOG.warn("Completion service's task did not complete in a timely manner!");
                            }
                        }
                    } catch (final InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
                    } catch (final ExecutionException e) {
                        final Throwable t = e.getCause();
                        if (FolderException.class.isAssignableFrom(t.getClass())) {
                            throw (FolderException) t;
                        } else if (AbstractOXException.class.isAssignableFrom(t.getClass())) {
                            throw new FolderException((AbstractOXException) t);
                        } else if (t instanceof RuntimeException) {
                            throw (RuntimeException) t;
                        } else if (t instanceof Error) {
                            throw (Error) t;
                        } else {
                            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(t, t.getMessage());
                        }
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
                ret[i] = new CacheSortableId(subfolders[i], i);
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

    public StorageParameters startTransaction(final StorageParameters parameters, final boolean modify) throws FolderException {
        return parameters;
    }

    public void updateFolder(final Folder folder, final StorageParameters storageParameters) throws FolderException {
        final Session session = storageParameters.getSession();
        /*
         * Perform update operation via non-cache storage
         */
        final String oldParentId;
        if (null != folder.getParentID()) {
            // Move
            oldParentId = getFolder(folder.getTreeID(), folder.getID(), storageParameters).getParentID();
        } else {
            oldParentId = null;
        }
        if (null == session) {
            new Update(storageParameters.getUser(), storageParameters.getContext(), registry).doUpdate(
                folder,
                storageParameters.getTimeStamp());
        } else {
            try {
                new Update(new ServerSessionAdapter(session), registry).doUpdate(folder, storageParameters.getTimeStamp());
            } catch (final ContextException e) {
                throw new FolderException(e);
            }
        }
        /*
         * Get folder from appropriate storage
         */
        final String folderId = folder.getID();
        final String treeId = folder.getTreeID();
        final FolderStorage storage = registry.getFolderStorage(treeId, folderId);
        if (null == storage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, folderId);
        }
        /*
         * Refresh/Invalidate folder
         */
        removeFolder(folderId, treeId, storageParameters);
        final Folder updatedFolder = loadFolder(treeId, folderId, StorageType.WORKING, storageParameters);
        putFolder(updatedFolder, treeId, storageParameters);
        /*
         * Refresh/invalidate parent(s)
         */
        final String parentID = updatedFolder.getParentID();
        if (!FolderStorage.ROOT_ID.equals(parentID)) {
            removeFolder(parentID, treeId, storageParameters);
            final Folder parentFolder = loadFolder(treeId, parentID, StorageType.WORKING, storageParameters);
            putFolder(parentFolder, treeId, storageParameters);
        }
        if (null != oldParentId && !FolderStorage.ROOT_ID.equals(oldParentId)) {
            removeFolder(oldParentId, treeId, storageParameters);
            final Folder oldParentFolder = loadFolder(treeId, oldParentId, StorageType.WORKING, storageParameters);
            putFolder(oldParentFolder, treeId, storageParameters);
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
            folders = new Updates(storageParameters.getUser(), storageParameters.getContext(), registry).doUpdates(
                treeId,
                timeStamp,
                ignoreDelete,
                includeContentTypes)[index];
        } else {
            try {
                folders = new Updates(new ServerSessionAdapter(session), registry).doUpdates(
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
        storage.startTransaction(storageParameters, true);
        try {
            final boolean contains = storage.containsFolder(treeId, folderId, storageType, storageParameters);
            storage.commitTransaction(storageParameters);
            return contains;
        } catch (final FolderException e) {
            storage.rollback(storageParameters);
            throw e;
        } catch (final Exception e) {
            storage.rollback(storageParameters);
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
        storage.startTransaction(storageParameters, true);
        try {
            final Folder folder = storage.getFolder(treeId, folderId, storageType, storageParameters);
            storage.commitTransaction(storageParameters);
            return folder;
        } catch (final FolderException e) {
            storage.rollback(storageParameters);
            throw e;
        } catch (final Exception e) {
            storage.rollback(storageParameters);
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

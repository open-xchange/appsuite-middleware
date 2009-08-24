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

import java.util.Date;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheException;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheService;
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
import com.openexchange.groupware.ldap.User;
import com.openexchange.server.ServiceException;

/**
 * {@link CacheFolderStorage} - The cache folder storage.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CacheFolderStorage implements FolderStorage {

    private static final String GLOBAL_FOLDER_CACHE_REGION_NAME = "GlobalFolderCache";

    private static final String USER_FOLDER_CACHE_REGION_NAME = "UserFolderCache";

    private CacheService cacheService;

    private Cache globalCache;

    private Cache userCache;

    /**
     * Initializes a new {@link CacheFolderStorage}.
     */
    public CacheFolderStorage() {
        super();
    }

    /**
     * Initializes this folder cache on available cache service.
     * 
     * @throws FolderException If initialization of this folder cache fails
     */
    public void onCacheAvailable() throws FolderException {
        try {
            cacheService = CacheServiceRegistry.getServiceRegistry().getService(CacheService.class, true);
            // TODO: cacheService.loadConfiguration();
            globalCache = cacheService.getCache(GLOBAL_FOLDER_CACHE_REGION_NAME);
            userCache = cacheService.getCache(USER_FOLDER_CACHE_REGION_NAME);
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
                    cacheService.freeCache(GLOBAL_FOLDER_CACHE_REGION_NAME);
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
                    cacheService.freeCache(USER_FOLDER_CACHE_REGION_NAME);
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
        final String treeId = folder.getTreeID();
        final String parentId = folder.getParentID();
        final Folder createdFolder;
        {
            final FolderStorage storage = CacheFolderStorageRegistry.getInstance().getFolderStorage(treeId, parentId);
            if (null == storage) {
                throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, parentId);
            }
            storage.startTransaction(storageParameters, true);
            try {
                storage.createFolder(folder, storageParameters);
                createdFolder = storage.getFolder(treeId, folder.getID(), storageParameters);
                storage.commitTransaction(storageParameters);
            } catch (final FolderException e) {
                storage.rollback(storageParameters);
                throw e;
            }
        }
        if (createdFolder.isCacheable()) {
            /*
             * Put to cache
             */
            try {
                final CacheKey key;
                final String id = createdFolder.getID();
                if (createdFolder.isGlobalID()) {
                    key = newCacheKey(id, treeId, storageParameters.getContext().getContextId());
                    globalCache.put(key, createdFolder);
                } else {
                    key = newCacheKey(id, treeId, storageParameters.getContext().getContextId(), storageParameters.getUser().getId());
                    userCache.put(key, createdFolder);
                }
            } catch (final CacheException e) {
                throw new FolderException(e);
            }
        }
    }

    public void clearFolder(final String treeId, final String folderId, final StorageParameters storageParameters) throws FolderException {
        // TODO
    }

    public void deleteFolder(final String treeId, final String folderId, final StorageParameters storageParameters) throws FolderException {
        final FolderStorage storage = CacheFolderStorageRegistry.getInstance().getFolderStorage(treeId, folderId);
        if (null == storage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, folderId);
        }
        storage.startTransaction(storageParameters, true);
        try {
            storage.deleteFolder(treeId, folderId, storageParameters);
            storage.commitTransaction(storageParameters);
        } catch (final FolderException e) {
            storage.rollback(storageParameters);
            throw e;
        }
    }

    public String getDefaultFolderID(final User user, final String treeId, final ContentType contentType, final StorageParameters storageParameters) throws FolderException {
        final FolderStorage storage = CacheFolderStorageRegistry.getInstance().getFolderStorageByContentType(treeId, contentType);
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
        // Try global cache key
        final int contextId = storageParameters.getContext().getContextId();
        CacheKey key = newCacheKey(folderId, treeId, contextId);
        Folder folder = (Folder) globalCache.get(key);
        if (null == folder) {
            // Try user cache key
            key = newCacheKey(folderId, treeId, contextId, storageParameters.getUser().getId());
            folder = (Folder) userCache.get(key);
        }
        if (null == folder) {
            final FolderStorage storage = CacheFolderStorageRegistry.getInstance().getFolderStorage(treeId, folderId);
            if (null == storage) {
                throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, folderId);
            }
            storage.startTransaction(storageParameters, false);
            try {
                folder = storage.getFolder(treeId, folderId, storageParameters);
                storage.commitTransaction(storageParameters);
            } catch (final FolderException e) {
                storage.rollback(storageParameters);
                throw e;
            }
            if (folder.isCacheable()) {
                /*
                 * Put to cache
                 */
                try {
                    if (folder.isGlobalID()) {
                        key = newCacheKey(folderId, treeId, contextId);
                        globalCache.put(key, folder);
                    } else {
                        key = newCacheKey(folderId, treeId, contextId, storageParameters.getUser().getId());
                        userCache.put(key, folder);
                    }
                } catch (final CacheException e) {
                    throw new FolderException(e);
                }
            }
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
        // TODO Auto-generated method stub
        return null;
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
        final String treeId = folder.getTreeID();
        final String folderId = folder.getID();
        final Folder updatedFolder;
        {
            final FolderStorage storage = CacheFolderStorageRegistry.getInstance().getFolderStorage(treeId, folderId);
            if (null == storage) {
                throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, folder);
            }
            storage.startTransaction(storageParameters, true);
            try {
                storage.updateFolder(folder, storageParameters);
                updatedFolder = storage.getFolder(treeId, folderId, storageParameters);
                storage.commitTransaction(storageParameters);
            } catch (final FolderException e) {
                storage.rollback(storageParameters);
                throw e;
            }
        }
        if (updatedFolder.isCacheable()) {
            /*
             * Put to cache
             */
            try {
                final CacheKey key;
                final String id = updatedFolder.getID();
                if (updatedFolder.isGlobalID()) {
                    key = newCacheKey(id, treeId, storageParameters.getContext().getContextId());
                    globalCache.put(key, updatedFolder);
                } else {
                    key = newCacheKey(id, treeId, storageParameters.getContext().getContextId(), storageParameters.getUser().getId());
                    userCache.put(key, updatedFolder);
                }
            } catch (final CacheException e) {
                throw new FolderException(e);
            }
        }
    }

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

    public boolean containsFolder(final String treeId, final String folderId, final StorageParameters storageParameters) throws FolderException {
        // TODO Auto-generated method stub
        return false;
    }

    public String[] getModifiedFolderIDs(final Date timeStamp, final StorageParameters storageParameters) throws FolderException {
        // TODO Auto-generated method stub
        return null;
    }

    public String[] getDeletedFolderIDs(final Date timeStamp, final StorageParameters storageParameters) throws FolderException {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean containsFolder(final String treeId, final String folderId, final StorageType storageType, final StorageParameters storageParameters) throws FolderException {
        // TODO Auto-generated method stub
        return false;
    }

    public Folder getFolder(final String treeId, final String folderId, final StorageType storageType, final StorageParameters storageParameters) throws FolderException {
        // TODO Auto-generated method stub
        return null;
    }
}

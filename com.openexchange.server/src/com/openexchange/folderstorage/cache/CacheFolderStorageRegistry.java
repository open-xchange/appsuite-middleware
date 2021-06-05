/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.folderstorage.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.FolderStorageDiscoverer;
import com.openexchange.folderstorage.FolderType;
import com.openexchange.folderstorage.StoragePriority;

/**
 * {@link CacheFolderStorageRegistry} - Cache's registry for folder storages.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CacheFolderStorageRegistry implements FolderStorageDiscoverer {

    private static final CacheFolderStorageRegistry instance = new CacheFolderStorageRegistry();

    /**
     * Gets the {@link CacheFolderStorageRegistry} instance.
     *
     * @return The {@link CacheFolderStorageRegistry} instance
     */
    public static CacheFolderStorageRegistry getInstance() {
        return instance;
    }

    /*
     * Member section
     */

    private final ConcurrentMap<String, List<FolderStorage>> registry;

    private final Queue<FolderStorage> genStorages;

    /**
     * Initializes a new {@link CacheFolderStorageRegistry}.
     */
    private CacheFolderStorageRegistry() {
        super();
        registry = new ConcurrentHashMap<String, List<FolderStorage>>();
        genStorages = new ConcurrentLinkedQueue<FolderStorage>();
    }

    /**
     * Clears the caches from registered folder storages.
     */
    public void clearCaches(final int userId, final int contextId) {
        for (final FolderStorage fs : genStorages) {
            fs.clearCache(userId, contextId);
        }
        final Set<Entry<String, List<FolderStorage>>> entrySet = registry.entrySet();
        for (final Entry<String, List<FolderStorage>> entry : entrySet) {
            final List<FolderStorage> list = entry.getValue();
            for (final FolderStorage folderStorage : list) {
                folderStorage.clearCache(userId, contextId);
            }
        }
    }

    /**
     * Associates specified folder storage to given tree identifier.
     *
     * @param treeId The tree identifier
     * @param folderStorage The folder storage to add
     * @return <code>true</code> If registration was successful; otherwise <code>false</code>
     */
    public boolean addFolderStorage(final String treeId, final FolderStorage folderStorage) {
        if (StoragePriority.HIGHEST == folderStorage.getStoragePriority()) {
            return false;
        }
        // Register storage's content types
        final ContentType[] contentTypes = folderStorage.getSupportedContentTypes();
        if (null != contentTypes && contentTypes.length > 0) {
            boolean success = true;
            for (int i = 0; success && i < contentTypes.length; i++) {
                success = CacheContentTypeRegistry.getInstance().addContentType(treeId, contentTypes[i], folderStorage);
            }
            if (!success) {
                for (final ContentType contentType : contentTypes) {
                    CacheContentTypeRegistry.getInstance().removeContentType(treeId, contentType);
                }
                return false;
            }
        } else {
            if (!CacheContentTypeRegistry.getInstance().addGeneralContentType(treeId, folderStorage)) {
                return false;
            }
        }
        // Register by tree identifier
        if (FolderStorage.ALL_TREE_ID.equals(treeId)) {
            genStorages.add(folderStorage);
        } else {
            List<FolderStorage> storages = registry.get(treeId);
            if (null == storages) {
                final List<FolderStorage> tmp = new CopyOnWriteArrayList<FolderStorage>();
                storages = registry.putIfAbsent(treeId, tmp);
                if (null == storages) {
                    storages = tmp;
                }
            }
            storages.add(folderStorage);
        }
        return true;
    }

    @Override
    public FolderStorage getFolderStorage(final String treeId, final String folderId) {
        if (!genStorages.isEmpty()) {
            /*
             * Check general storages first
             */
            for (final FolderStorage folderStorage : genStorages) {
                if (folderStorage.getFolderType().servesTreeId(treeId)) {
                    return folderStorage;
                }
            }
        }
        /*
         * Obtain candidates by tree identifier
         */
        final List<FolderStorage> storages = registry.get(treeId);
        if (null == storages) {
            return null;
        }
        for (final FolderStorage folderStorage : storages) {
            if (folderStorage.getFolderType().servesFolderId(folderId)) {
                return folderStorage;
            }
        }
        return null;
    }

    @Override
    public FolderStorage[] getFolderStoragesForParent(final String treeId, final String parentId) {
        if (!genStorages.isEmpty()) {
            /*
             * Check general storages first
             */
            for (final FolderStorage folderStorage : genStorages) {
                final FolderType folderType = folderStorage.getFolderType();
                if (folderType.servesTreeId(treeId) && folderType.servesParentId(parentId)) {
                    return new FolderStorage[] { folderStorage };
                }
            }
        }
        /*
         * Obtain candidates by tree identifier
         */
        final List<FolderStorage> storages = registry.get(treeId);
        if (null == storages) {
            return new FolderStorage[0];
        }
        final List<FolderStorage> l = new ArrayList<FolderStorage>(4);
        for (final FolderStorage folderStorage : storages) {
            if (folderStorage.getFolderType().servesParentId(parentId)) {
                l.add(folderStorage);
            }
        }
        return l.toArray(new FolderStorage[l.size()]);
    }

    @Override
    public FolderStorage[] getFolderStoragesForTreeID(final String treeId) {
        if (!genStorages.isEmpty()) {
            /*
             * Check general storages first
             */
            for (final FolderStorage folderStorage : genStorages) {
                final FolderType folderType = folderStorage.getFolderType();
                if (folderType.servesTreeId(treeId)) {
                    return new FolderStorage[] { folderStorage };
                }
            }
        }
        /*
         * Obtain candidates by tree identifier
         */
        final List<FolderStorage> storages = registry.get(treeId);
        if (null == storages) {
            return new FolderStorage[0];
        }
        return storages.toArray(new FolderStorage[storages.size()]);
    }

    @Override
    public FolderStorage[] getTreeFolderStorages(final String treeId) {
        if (!genStorages.isEmpty()) {
            /*
             * Check general storages first
             */
            for (final FolderStorage folderStorage : genStorages) {
                if (!StoragePriority.HIGHEST.equals(folderStorage.getStoragePriority()) && folderStorage.getFolderType().servesTreeId(
                    treeId)) {
                    return new FolderStorage[] { folderStorage };
                }
            }
        }
        /*
         * Obtain candidates by tree identifier
         */
        final List<FolderStorage> storages = registry.get(treeId);
        if (null == storages) {
            return new FolderStorage[0];
        }
        return storages.toArray(new FolderStorage[storages.size()]);
    }

    @Override
    public FolderStorage getFolderStorageByContentType(final String treeId, final ContentType contentType) {
        return CacheContentTypeRegistry.getInstance().getFolderStorageByContentType(treeId, contentType);
    }

    /**
     * Removes specified folder storage bound to given tree identifier.
     *
     * @param treeId The tree identifier
     * @param folderStorage The folder storage to remove
     */
    public void removeFolderStorage(final String treeId, final FolderStorage folderStorage) {
        if (StoragePriority.HIGHEST == folderStorage.getStoragePriority()) {
            return;
        }
        if (FolderStorage.ALL_TREE_ID.equals(treeId)) {
            genStorages.remove(folderStorage);
        } else {
            final List<FolderStorage> storages = registry.get(treeId);
            if (null == storages) {
                return;
            }
            storages.remove(folderStorage);
        }
        // Delete from content type registry
        final ContentType[] contentTypes = folderStorage.getSupportedContentTypes();
        if (null != contentTypes && contentTypes.length > 0) {
            for (final ContentType contentType : contentTypes) {
                CacheContentTypeRegistry.getInstance().removeContentType(treeId, contentType);
            }
        } else {
            CacheContentTypeRegistry.getInstance().removeGeneralContentType(treeId, folderStorage);
        }
    }

}

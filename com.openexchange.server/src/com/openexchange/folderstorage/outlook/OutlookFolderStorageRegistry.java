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

package com.openexchange.folderstorage.outlook;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.FolderStorageDiscoverer;
import com.openexchange.folderstorage.FolderType;
import com.openexchange.folderstorage.StoragePriority;

/**
 * {@link OutlookFolderStorageRegistry} - MS Outlook storage's registry for real folder storages.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class OutlookFolderStorageRegistry implements FolderStorageDiscoverer {

    private static final OutlookFolderStorageRegistry instance = new OutlookFolderStorageRegistry();

    /**
     * Gets the {@link OutlookFolderStorageRegistry} instance.
     *
     * @return The {@link OutlookFolderStorageRegistry} instance
     */
    public static OutlookFolderStorageRegistry getInstance() {
        return instance;
    }

    /*
     * Member section
     */

    private final ConcurrentMap<String, Queue<FolderStorage>> registry;

    private final Queue<FolderStorage> genStorages;

    private final ConcurrentMap<ContentType, FolderStorage> contentTypes;

    /**
     * Initializes a new {@link OutlookFolderStorageRegistry}.
     */
    private OutlookFolderStorageRegistry() {
        super();
        registry = new ConcurrentHashMap<String, Queue<FolderStorage>>();
        genStorages = new ConcurrentLinkedQueue<FolderStorage>();
        contentTypes = new ConcurrentHashMap<ContentType, FolderStorage>();
    }

    /**
     * Associates specified folder storage to given tree identifier.
     *
     * @param treeId The tree identifier
     * @param folderStorage The folder storage to add
     * @return <code>true</code> If registration was successful; otherwise <code>false</code>
     */
    public boolean addFolderStorage(final String treeId, final FolderStorage folderStorage) {
        if ((!FolderStorage.ALL_TREE_ID.equals(treeId) || !StoragePriority.HIGHEST.equals(folderStorage.getStoragePriority())) && !FolderStorage.REAL_TREE_ID.equals(treeId)) {
            return false;
        }
        // Register storage's content types
        final ContentType[] contentTypes = folderStorage.getSupportedContentTypes();
        if (null != contentTypes && contentTypes.length > 0) {
            boolean success = true;
            final Set<ContentType> added = new HashSet<ContentType>(contentTypes.length);
            for (int j = 0; success && j < contentTypes.length; j++) {
                if (null == this.contentTypes.putIfAbsent(contentTypes[j], folderStorage)) {
                    // Yapp, added
                    added.add(contentTypes[j]);
                } else {
                    // No, already present
                    final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OutlookFolderStorageRegistry.class);
                    log.error("Duplicate folder storage for content type \"{}\"", contentTypes[j].toString(), new Throwable());
                    success = false;
                }
            }
            if (!success) {
                for (final ContentType contentType : added) {
                    this.contentTypes.remove(contentType);
                }
                return false;
            }
        }
        // Register by tree identifier
        if (FolderStorage.ALL_TREE_ID.equals(treeId)) {
            genStorages.add(folderStorage);
        } else {
            Queue<FolderStorage> storages = registry.get(treeId);
            if (null == storages) {
                final Queue<FolderStorage> tmp = new ConcurrentLinkedQueue<FolderStorage>();
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
        final Queue<FolderStorage> storages = registry.get(treeId);
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

    /**
     * Gets the dedicated folder storage for specified tree-folder-pair.
     *
     * @param treeId The tree identifier
     * @param folderId The folder identifier
     * @return The dedicated folder storage for specified tree-folder-pair or <code>null</code>
     */
    public FolderStorage getDedicatedFolderStorage(final String treeId, final String folderId) {
        /*
         * Obtain candidates by tree identifier
         */
        final Queue<FolderStorage> storages = registry.get(treeId);
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
        final Queue<FolderStorage> storages = registry.get(treeId);
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
        final Queue<FolderStorage> storages = registry.get(treeId);
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
        final Queue<FolderStorage> storages = registry.get(treeId);
        if (null == storages) {
            return new FolderStorage[0];
        }
        return storages.toArray(new FolderStorage[storages.size()]);
    }

    @Override
    public FolderStorage getFolderStorageByContentType(final String treeId, final ContentType contentType) {
        final FolderStorage folderStorage = contentTypes.get(contentType);
        if (null == folderStorage) {
            return null;
        }
        if (folderStorage.getFolderType().servesTreeId(treeId)) {
            return folderStorage;
        }
        return null;
    }

    /**
     * Removes specified folder storage bound to given tree identifier.
     *
     * @param treeId The tree identifier
     * @param folderStorage The folder storage to remove
     */
    public void removeFolderStorage(final String treeId, final FolderStorage folderStorage) {
        if ((!FolderStorage.ALL_TREE_ID.equals(treeId) || !StoragePriority.HIGHEST.equals(folderStorage.getStoragePriority())) && !FolderStorage.REAL_TREE_ID.equals(treeId)) {
            return;
        }
        // Unregister folder storage
        if (FolderStorage.ALL_TREE_ID.equals(treeId)) {
            genStorages.remove(folderStorage);
        } else {
            final Queue<FolderStorage> storages = registry.get(treeId);
            if (null == storages) {
                return;
            }
            storages.remove(folderStorage);
        }
        // Unregister storage's content types
        final ContentType[] contentTypes = folderStorage.getSupportedContentTypes();
        if (null != contentTypes && contentTypes.length > 0) {
            for (final ContentType contentType : contentTypes) {
                this.contentTypes.remove(contentType);
            }
        }
    }

}

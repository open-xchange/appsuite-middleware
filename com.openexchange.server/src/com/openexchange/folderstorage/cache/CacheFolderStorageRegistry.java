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

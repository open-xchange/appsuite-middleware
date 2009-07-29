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

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.StoragePriority;

/**
 * {@link CacheFolderStorageRegistry} - Cache's registry for folder storages.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CacheFolderStorageRegistry {

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

    private final List<FolderStorage> genStorages;

    /**
     * Initializes a new {@link CacheFolderStorageRegistry}.
     */
    private CacheFolderStorageRegistry() {
        super();
        registry = new ConcurrentHashMap<String, List<FolderStorage>>();
        genStorages = new CopyOnWriteArrayList<FolderStorage>();
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
                for (int i = 0; i < contentTypes.length; i++) {
                    CacheContentTypeRegistry.getInstance().removeContentType(treeId, contentTypes[i]);
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
                storages = registry.putIfAbsent(treeId, new CopyOnWriteArrayList<FolderStorage>());
                if (null == storages) {
                    storages = tmp;
                }
            }
            storages.add(folderStorage);
        }
        return true;
    }

    /**
     * Gets the folder storage for specified tree-folder-pair.
     * 
     * @param treeId The tree identifier
     * @param folderId The folder identifier
     * @return The folder storage for specified tree-folder-pair
     */
    public FolderStorage getFolderStorage(final String treeId, final String folderId) {
        if (!genStorages.isEmpty()) {
            /*
             * Check general storages first
             */
            for (final Iterator<FolderStorage> iterator = genStorages.iterator(); iterator.hasNext();) {
                final FolderStorage folderStorage = iterator.next();
                if (folderStorage.getFolderType().servesTreeId(treeId)) {
                    return null;
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

    /**
     * Gets the folder storage capable to handle given content type in specified tree.
     * 
     * @param treeId The tree identifier
     * @param contentType The content type
     * @return The folder storage capable to handle given content type in specified tree
     */
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

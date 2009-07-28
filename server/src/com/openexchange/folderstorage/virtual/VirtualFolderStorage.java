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

package com.openexchange.folderstorage.virtual;

import java.util.Locale;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderException;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.FolderStorageService;
import com.openexchange.folderstorage.FolderType;
import com.openexchange.folderstorage.SortableId;
import com.openexchange.folderstorage.StorageParameters;
import com.openexchange.folderstorage.StoragePriority;
import com.openexchange.folderstorage.virtual.sql.VirtualFolderStorageSQL;
import com.openexchange.server.ServiceException;

/**
 * {@link VirtualFolderStorage} - TODO Short description of this class' purpose.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class VirtualFolderStorage implements FolderStorage {

    private final int treeId;

    private final FolderType folderType;

    /**
     * Initializes a new {@link VirtualFolderStorage}.
     * 
     * @param treeId The tree identifier
     */
    public VirtualFolderStorage(final int treeId) {
        super();
        this.treeId = treeId;
        folderType = new VirtualFolderType(treeId);
    }

    public void commitTransaction(final StorageParameters params) throws FolderException {
        
    }

    public void createFolder(final Folder folder, final StorageParameters storageParameters) throws FolderException {
        VirtualFolderStorageSQL.insertFolder(
            storageParameters.getContext().getContextId(),
            treeId,
            storageParameters.getUser().getId(),
            folder);
    }

    public void deleteFolder(final String folderId, final StorageParameters storageParameters) throws FolderException {
        VirtualFolderStorageSQL.deleteFolder(
            storageParameters.getContext().getContextId(),
            treeId,
            storageParameters.getUser().getId(),
            folderId);
    }

    public Folder getDefaultFolder(final int entity, final ContentType contentType, final StorageParameters storageParameters) throws FolderException {
        final VirtualFolder virtualFolder;
        {
            // Get real folder
            final FolderStorageService storageService;
            try {
                storageService = VirtualServiceRegistry.getServiceRegistry().getService(FolderStorageService.class, true);
            } catch (final ServiceException e) {
                throw new FolderException(e);
            }
            final FolderStorage realFolderStorage = storageService.getFolderStorageByContentType(FolderStorage.REAL_TREE_ID, contentType);
            if (null == realFolderStorage) {
                throw FolderExceptionErrorMessage.NO_STORAGE_FOR_CT.create(FolderStorage.REAL_TREE_ID, contentType);
            }
            realFolderStorage.startTransaction(storageParameters, false);
            try {
                final Folder realFolder = realFolderStorage.getDefaultFolder(entity, contentType, storageParameters);
                virtualFolder = new VirtualFolder(realFolder);
                virtualFolder.setTreeID(String.valueOf(treeId));
                realFolderStorage.commitTransaction(storageParameters);
            } catch (final FolderException e) {
                realFolderStorage.rollback(storageParameters);
                throw e;
            }
        }
        // Load folder data from database
        VirtualFolderStorageSQL.fillFolder(
            storageParameters.getContext().getContextId(),
            treeId,
            storageParameters.getUser().getId(),
            virtualFolder);
        return virtualFolder;
    }

    public Folder getFolder(final String folderId, final StorageParameters storageParameters) throws FolderException {
        final VirtualFolder virtualFolder;
        {
            // Get real folder
            final FolderStorageService storageService;
            try {
                storageService = VirtualServiceRegistry.getServiceRegistry().getService(FolderStorageService.class, true);
            } catch (final ServiceException e) {
                throw new FolderException(e);
            }
            final FolderStorage[] realFolderStorages = storageService.getFolderStorages(FolderStorage.REAL_TREE_ID, folderId);
            if (null == realFolderStorages || realFolderStorages.length == 0) {
                throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(FolderStorage.REAL_TREE_ID, folderId);
            }
            // Select first storage to load folder
            final FolderStorage realFolderStorage = realFolderStorages[0];
            realFolderStorage.startTransaction(storageParameters, false);
            try {
                final Folder realFolder = realFolderStorage.getFolder(folderId, storageParameters);
                virtualFolder = new VirtualFolder(realFolder);
                virtualFolder.setTreeID(String.valueOf(treeId));
                realFolderStorage.commitTransaction(storageParameters);
            } catch (final FolderException e) {
                realFolderStorage.rollback(storageParameters);
                throw e;
            }
        }
        // Load folder data from database
        VirtualFolderStorageSQL.fillFolder(
            storageParameters.getContext().getContextId(),
            treeId,
            storageParameters.getUser().getId(),
            virtualFolder);
        return virtualFolder;
    }

    public FolderType getFolderType() {
        return folderType;
    }

    public StoragePriority getStoragePriority() {
        return StoragePriority.NORMAL;
    }

    public SortableId[] getSubfolders(final String parentId, final StorageParameters storageParameters) throws FolderException {
        final String[][] idNamePairs = VirtualFolderStorageSQL.getSubfolderIds(
            storageParameters.getContext().getContextId(),
            treeId,
            storageParameters.getUser().getId(),
            parentId);
        final SortableId[] ret = new SortableId[idNamePairs.length];
        final Locale locale = storageParameters.getUser().getLocale();
        for (int i = 0; i < idNamePairs.length; i++) {
            final String[] idNamePair = idNamePairs[i];
            ret[i] = new VirtualId(idNamePair[0], idNamePair[1], locale);
        }
        return ret;
    }

    public void rollback(final StorageParameters params) {
        
    }

    public StorageParameters startTransaction(final StorageParameters parameters, final boolean modify) throws FolderException {
        return parameters;
    }

    public void updateFolder(final Folder folder, final StorageParameters storageParameters) throws FolderException {
        VirtualFolderStorageSQL.updateFolder(
            storageParameters.getContext().getContextId(),
            treeId,
            storageParameters.getUser().getId(),
            folder);

    }

    public ContentType[] getSupportedContentTypes() {
        return new ContentType[0];
    }

}

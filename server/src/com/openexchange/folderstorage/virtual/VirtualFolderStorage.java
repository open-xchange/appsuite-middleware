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

package com.openexchange.folderstorage.virtual;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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
import com.openexchange.folderstorage.virtual.sql.Delete;
import com.openexchange.folderstorage.virtual.sql.Insert;
import com.openexchange.folderstorage.virtual.sql.Select;
import com.openexchange.folderstorage.virtual.sql.Update;
import com.openexchange.groupware.ldap.User;

/**
 * {@link VirtualFolderStorage} - The virtual folder storage.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class VirtualFolderStorage implements FolderStorage {

    private final FolderType folderType;

    /**
     * Initializes a new {@link VirtualFolderStorage}.
     */
    public VirtualFolderStorage() {
        super();
        folderType = new VirtualFolderType();
    }

    public Folder prepareFolder(final String treeId, final Folder folder, final StorageParameters storageParameters) throws FolderException {
        return folder;
    }

    public void checkConsistency(final String treeId, final StorageParameters storageParameters) throws FolderException {
        // TODO: Implement it
    }

    public void restore(final String treeId, final String folderId, final StorageParameters storageParameters) throws FolderException {
        // No real storage
    }

    public ContentType getDefaultContentType() {
        return null;
    }

    public void commitTransaction(final StorageParameters params) throws FolderException {

    }

    public void createFolder(final Folder folder, final StorageParameters storageParameters) throws FolderException {
        Insert.insertFolder(storageParameters.getContextId(), Integer.parseInt(folder.getTreeID()), storageParameters.getUserId(), folder);
    }

    public void clearFolder(final String treeId, final String folderId, final StorageParameters storageParameters) throws FolderException {
        // Nothing to do
    }

    public void deleteFolder(final String treeId, final String folderId, final StorageParameters storageParameters) throws FolderException {
        Delete.deleteFolder(storageParameters.getContextId(), Integer.parseInt(treeId), storageParameters.getUserId(), folderId, true);
    }

    public String getDefaultFolderID(final User user, final String treeId, final ContentType contentType, final Type type, final StorageParameters storageParameters) throws FolderException {
        // Get default folder
        final FolderStorage byContentType = VirtualFolderStorageRegistry.getInstance().getFolderStorageByContentType(treeId, contentType);
        if (null == byContentType) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_CT.create(treeId, contentType);
        }
        final boolean started = byContentType.startTransaction(storageParameters, false);
        try {
            final String defaultFolderID = byContentType.getDefaultFolderID(user, treeId, contentType, type, storageParameters);
            if (started) {
                byContentType.commitTransaction(storageParameters);
            }
            return defaultFolderID;
        } catch (final FolderException e) {
            if (started) {
                byContentType.rollback(storageParameters);
            }
            throw e;
        } catch (final Exception e) {
            if (started) {
                byContentType.rollback(storageParameters);
            }
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    public Type getTypeByParent(final User user, final String treeId, final String parentId, final StorageParameters storageParameters) throws FolderException {
        return null;
    }

    public boolean containsForeignObjects(final User user, final String treeId, final String folderId, final StorageParameters storageParameters) throws FolderException {
        /*
         * Get real folder storage
         */
        final FolderStorage realFolderStorage =
            VirtualFolderStorageRegistry.getInstance().getFolderStorage(FolderStorage.REAL_TREE_ID, folderId);
        if (null == realFolderStorage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(FolderStorage.REAL_TREE_ID, folderId);
        }
        final boolean started = realFolderStorage.startTransaction(storageParameters, false);
        try {
            final boolean containsForeignObjects = realFolderStorage.containsForeignObjects(user, treeId, folderId, storageParameters);
            if (started) {
                realFolderStorage.commitTransaction(storageParameters);
            }
            return containsForeignObjects;
        } catch (final FolderException e) {
            if (started) {
                realFolderStorage.rollback(storageParameters);
            }
            throw e;
        } catch (final Exception e) {
            if (started) {
                realFolderStorage.rollback(storageParameters);
            }
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    public boolean isEmpty(final String treeId, final String folderId, final StorageParameters storageParameters) throws FolderException {
        /*
         * Get real folder storage
         */
        final FolderStorage realFolderStorage =
            VirtualFolderStorageRegistry.getInstance().getFolderStorage(FolderStorage.REAL_TREE_ID, folderId);
        if (null == realFolderStorage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(FolderStorage.REAL_TREE_ID, folderId);
        }
        final boolean started = realFolderStorage.startTransaction(storageParameters, false);
        try {
            final boolean isEmpty = realFolderStorage.isEmpty(treeId, folderId, storageParameters);
            if (started) {
                realFolderStorage.commitTransaction(storageParameters);
            }
            return isEmpty;
        } catch (final FolderException e) {
            if (started) {
                realFolderStorage.rollback(storageParameters);
            }
            throw e;
        } catch (final Exception e) {
            if (started) {
                realFolderStorage.rollback(storageParameters);
            }
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    public void updateLastModified(final long lastModified, final String treeId, final String folderId, final StorageParameters storageParameters) throws FolderException {
        /*
         * Get real folder storage
         */
        final FolderStorage folderStorage =
            VirtualFolderStorageRegistry.getInstance().getFolderStorage(FolderStorage.REAL_TREE_ID, folderId);
        if (null == folderStorage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(FolderStorage.REAL_TREE_ID, folderId);
        }
        final boolean started = folderStorage.startTransaction(storageParameters, false);
        try {
            // Get folder
            folderStorage.updateLastModified(lastModified, FolderStorage.REAL_TREE_ID, folderId, storageParameters);
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

    public List<Folder> getFolders(final String treeId, final List<String> folderIds, final StorageParameters storageParameters) throws FolderException {
        return getFolders(treeId, folderIds, StorageType.WORKING, storageParameters);
    }

    public List<Folder> getFolders(final String treeId, final List<String> folderIds, final StorageType storageType, final StorageParameters storageParameters) throws FolderException {
        final List<Folder> ret = new ArrayList<Folder>(folderIds.size());
        for (final String folderId : folderIds) {
            ret.add(getFolder(treeId, folderId, storageType, storageParameters));
        }
        return ret;
    }

    public Folder getFolder(final String treeId, final String folderId, final StorageParameters storageParameters) throws FolderException {
        return getFolder(treeId, folderId, StorageType.WORKING, storageParameters);
    }

    public Folder getFolder(final String treeId, final String folderId, final StorageType storageType, final StorageParameters storageParameters) throws FolderException {
        final VirtualFolder virtualFolder;
        {
            final Folder realFolder;
            {
                /*
                 * Get real folder storage
                 */
                final FolderStorage realFolderStorage =
                    VirtualFolderStorageRegistry.getInstance().getFolderStorage(FolderStorage.REAL_TREE_ID, folderId);
                if (null == realFolderStorage) {
                    throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(FolderStorage.REAL_TREE_ID, folderId);
                }
                final boolean started = realFolderStorage.startTransaction(storageParameters, false);
                try {
                    realFolder = realFolderStorage.getFolder(FolderStorage.REAL_TREE_ID, folderId, storageParameters);
                    if (started) {
                        realFolderStorage.commitTransaction(storageParameters);
                    }
                } catch (final FolderException e) {
                    if (started) {
                        realFolderStorage.rollback(storageParameters);
                    }
                    throw e;
                } catch (final Exception e) {
                    if (started) {
                        realFolderStorage.rollback(storageParameters);
                    }
                    throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
                }
            }
            virtualFolder = new VirtualFolder(realFolder);
            virtualFolder.setTreeID(treeId);
        }
        // Load folder data from database
        final User user = storageParameters.getUser();
        Select.fillFolder(
            storageParameters.getContextId(),
            Integer.parseInt(treeId),
            user.getId(),
            user.getLocale(),
            virtualFolder,
            storageType);
        return virtualFolder;
    }

    public FolderType getFolderType() {
        return folderType;
    }

    public StoragePriority getStoragePriority() {
        return StoragePriority.NORMAL;
    }

    public SortableId[] getSubfolders(final String treeId, final String parentId, final StorageParameters storageParameters) throws FolderException {
        final User user = storageParameters.getUser();
        final Locale locale = user.getLocale();
        final String[] ids =
            Select.getSubfolderIds(
                storageParameters.getContextId(),
                Integer.parseInt(treeId),
                user.getId(),
                locale,
                parentId,
                StorageType.WORKING);
        final SortableId[] ret = new SortableId[ids.length];
        for (int i = 0; i < ids.length; i++) {
            ret[i] = new VirtualId(ids[i], i, null);
        }
        return ret;
    }

    public void rollback(final StorageParameters params) {

    }

    public boolean startTransaction(final StorageParameters parameters, final boolean modify) throws FolderException {
        return true;
    }

    public void updateFolder(final Folder folder, final StorageParameters storageParameters) throws FolderException {
        final Folder storageFolder = getFolder(folder.getTreeID(), folder.getID(), storageParameters);
        /*
         * Ensure all field set
         */

        if (null == folder.getParentID()) {
            folder.setParentID(storageFolder.getParentID());
        }

        if (null == folder.getPermissions()) {
            folder.setPermissions(storageFolder.getPermissions());
        }

        if (folder.getName() == null) {
            folder.setName(storageFolder.getName());
        }

        Update.updateFolder(storageParameters.getContextId(), Integer.parseInt(folder.getTreeID()), storageParameters.getUserId(), folder);

    }

    public ContentType[] getSupportedContentTypes() {
        return new ContentType[0];
    }

    public boolean containsFolder(final String treeId, final String folderId, final StorageParameters storageParameters) throws FolderException {
        return containsFolder(treeId, folderId, StorageType.WORKING, storageParameters);
    }

    public boolean containsFolder(final String treeId, final String folderId, final StorageType storageType, final StorageParameters storageParameters) throws FolderException {
        return Select.containsFolder(
            storageParameters.getContextId(),
            Integer.parseInt(treeId),
            storageParameters.getUserId(),
            folderId,
            storageType);
    }

    public String[] getDeletedFolderIDs(final String treeId, final Date timeStamp, final StorageParameters storageParameters) throws FolderException {
        return new String[0];
    }

    public String[] getModifiedFolderIDs(final String treeId, final Date timeStamp, final ContentType[] includeContentTypes, final StorageParameters storageParameters) throws FolderException {
        return new String[0];
    }

    public SortableId[] getVisibleFolders(final String treeId, final ContentType contentType, final Type type, final StorageParameters storageParameters) throws FolderException {
        throw new UnsupportedOperationException("VirtualFolderStorage.getVisibleSubfolders()");
    }

}

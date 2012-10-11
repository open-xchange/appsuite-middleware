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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.folderstorage.internal.performers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.FolderStorageDiscoverer;
import com.openexchange.folderstorage.FolderType;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.SortableId;
import com.openexchange.folderstorage.internal.CalculatePermission;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link DeletePerformer} - Serves the <code>DELETE</code> request.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DeletePerformer extends AbstractPerformer {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(DeletePerformer.class));

    private static final boolean DEBUG_ENABLED = LOG.isDebugEnabled();

    /**
     * Initializes a new {@link DeletePerformer}.
     *
     * @param session The session
     */
    public DeletePerformer(final ServerSession session) {
        super(session);
    }

    /**
     * Initializes a new {@link DeletePerformer}.
     *
     * @param user The user
     * @param context The context
     */
    public DeletePerformer(final User user, final Context context) {
        super(user, context);
    }

    /**
     * Initializes a new {@link DeletePerformer}.
     *
     * @param session The session
     * @param folderStorageDiscoverer The folder storage discoverer
     */
    public DeletePerformer(final ServerSession session, final FolderStorageDiscoverer folderStorageDiscoverer) {
        super(session, folderStorageDiscoverer);
    }

    /**
     * Initializes a new {@link DeletePerformer}.
     *
     * @param user The user
     * @param context The context
     * @param folderStorageDiscoverer The folder storage discoverer
     */
    public DeletePerformer(final User user, final Context context, final FolderStorageDiscoverer folderStorageDiscoverer) {
        super(user, context, folderStorageDiscoverer);
    }

    /**
     * Performs the <code>DELETE</code> request.
     *
     * @param treeId The tree identifier
     * @param folderId The folder identifier
     * @param timeStamp The requestor's last-modified time stamp
     * @throws OXException If an error occurs during deletion
     */
    public void doDelete(final String treeId, final String folderId, final Date timeStamp) throws OXException {
        if (!KNOWN_TREES.contains(treeId)) {
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create("Create not supported by tree " + treeId);
        }
        final FolderStorage folderStorage = folderStorageDiscoverer.getFolderStorage(treeId, folderId);
        if (null == folderStorage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, folderId);
        }
        final long start = DEBUG_ENABLED ? System.currentTimeMillis() : 0L;
        final List<FolderStorage> openedStorages = new ArrayList<FolderStorage>(4);
        checkOpenedStorage(folderStorage, openedStorages);
        if (null != timeStamp) {
            storageParameters.setTimeStamp(timeStamp);
        }
        try {
            if (FolderStorage.REAL_TREE_ID.equals(treeId)) {
                /*
                 * Real delete
                 */
                folderStorage.deleteFolder(treeId, folderId, storageParameters);
            } else {
                /*-
                 * Virtual delete:
                 *
                 * 1. Delete from virtual storage
                 * 2. Delete from real storage
                 */
                final FolderStorage realStorage = folderStorageDiscoverer.getFolderStorage(FolderStorage.REAL_TREE_ID, folderId);
                if (null == realStorage) {
                    throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(FolderStorage.REAL_TREE_ID, folderId);
                }
                if (folderStorage.equals(realStorage)) {
                    folderStorage.deleteFolder(treeId, folderId, storageParameters);
                } else {
                    /*
                     * Delete from virtual storage
                     */
                    deleteVirtualFolder(folderId, treeId, folderStorage, openedStorages);
                }
            }
            if (DEBUG_ENABLED) {
                final long duration = System.currentTimeMillis() - start;
                LOG.debug(new StringBuilder().append("Delete.doDelete() took ").append(duration).append("msec for folder: ").append(
                    folderId).toString());
            }
            /*
             * Commit
             */
            for (final FolderStorage fs : openedStorages) {
                fs.commitTransaction(storageParameters);
            }
        } catch (final OXException e) {
            for (final FolderStorage fs : openedStorages) {
                fs.rollback(storageParameters);
            }
            throw e;
        } catch (final Exception e) {
            for (final FolderStorage fs : openedStorages) {
                fs.rollback(storageParameters);
            }
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private void deleteVirtualFolder(final String folderId, final String treeId, final FolderStorage folderStorage, final List<FolderStorage> openedStorages) throws OXException {
        final Folder folder = folderStorage.getFolder(treeId, folderId, storageParameters);
        storageParameters.putParameter(FolderType.GLOBAL, "global", Boolean.valueOf(folder.isGlobalID()));
        {
            final Permission permission = CalculatePermission.calculate(folder, this, ALL_ALLOWED);
            if (!permission.isVisible()) {
                throw FolderExceptionErrorMessage.FOLDER_NOT_VISIBLE.create(
                    getFolderInfo4Error(folder),
                    getUserInfo4Error(),
                    getContextInfo4Error());
            }
            if (!permission.isAdmin()) {
                throw FolderExceptionErrorMessage.FOLDER_NOT_DELETEABLE.create(
                    getFolderInfo4Error(folder),
                    getUserInfo4Error(),
                    getContextInfo4Error());
            }
            /*
             * Delete permissions
             */
            if (!canDeleteAllObjects(permission, folderId, treeId, folderStorage)) {
                throw FolderExceptionErrorMessage.FOLDER_NOT_DELETEABLE.create(
                    getFolderInfo4Error(folder),
                    getUserInfo4Error(),
                    getContextInfo4Error());
            }
        }
        final SortableId[] subfolders = folderStorage.getSubfolders(treeId, folderId, storageParameters);
        for (final SortableId subfolder : subfolders) {
            final String id = subfolder.getId();
            final FolderStorage subfolderStorage;
            if (folderStorage.getFolderType().servesFolderId(id)) {
                subfolderStorage = folderStorage;
            } else {
                subfolderStorage = folderStorageDiscoverer.getFolderStorage(treeId, id);
                if (null == subfolderStorage) {
                    throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, id);
                }
                checkOpenedStorage(subfolderStorage, openedStorages);
            }
            /*
             * Delete subfolder
             */
            deleteVirtualFolder(id, treeId, subfolderStorage, openedStorages);
        }
        /*
         * Delete from real storage
         */
        final FolderStorage realStorage = folderStorageDiscoverer.getFolderStorage(FolderStorage.REAL_TREE_ID, folderId);
        if (null == realStorage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(FolderStorage.REAL_TREE_ID, folderId);
        }
        checkOpenedStorage(realStorage, openedStorages);
        realStorage.deleteFolder(FolderStorage.REAL_TREE_ID, folderId, storageParameters);
        /*
         * And now from virtual storage
         */
        folderStorage.deleteFolder(treeId, folderId, storageParameters);
    }

    private boolean canDeleteAllObjects(final Permission permission, final String folderId, final String treeId, final FolderStorage folderStorage) throws OXException {
        final int deletePermission = permission.getDeletePermission();
        if (deletePermission >= Permission.DELETE_ALL_OBJECTS) {
            /*
             * Can delete all objects
             */
            return true;
        }
        if (deletePermission >= Permission.DELETE_OWN_OBJECTS) {
            /*
             * User may only delete own objects. Check if folder contains foreign objects which must not be deleted.
             */
            return !folderStorage.containsForeignObjects(user, treeId, folderId, storageParameters);
        }
        /*
         * No delete permission: Return true if folder is empty
         */
        return folderStorage.isEmpty(treeId, folderId, storageParameters);
    }

    private void checkOpenedStorage(final FolderStorage storage, final List<FolderStorage> openedStorages) throws OXException {
        for (final FolderStorage openedStorage : openedStorages) {
            if (openedStorage.equals(storage)) {
                return;
            }
        }
        if (storage.startTransaction(storageParameters, true)) {
            openedStorages.add(storage);
        }
    }

}

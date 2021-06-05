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

package com.openexchange.folderstorage.internal.performers;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.CalculatePermission;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.FolderStorageDiscoverer;
import com.openexchange.folderstorage.FolderType;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.SortableId;
import com.openexchange.folderstorage.TrashAwareFolderStorage;
import com.openexchange.folderstorage.TrashResult;
import com.openexchange.folderstorage.tx.TransactionManager;
import com.openexchange.folderstorage.virtual.VirtualFolderStorage;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.User;

/**
 * {@link DeletePerformer} - Serves the <code>DELETE</code> request.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DeletePerformer extends AbstractUserizedFolderPerformer {

    /**
     * Initializes a new {@link DeletePerformer}.
     *
     * @param session The session
     * @param decorator The optional folder service decorator
     * @throws OXException If passed session is invalid
     */
    public DeletePerformer(final ServerSession session, FolderServiceDecorator decorator) throws OXException {
        super(session, decorator);
    }

    /**
     * Initializes a new {@link DeletePerformer}.
     *
     * @param user The user
     * @param context The context
     * @param decorator The optional folder service decorator
     */
    public DeletePerformer(final User user, final Context context, FolderServiceDecorator decorator) {
        super(user, context, decorator);
    }

    /**
     * Initializes a new {@link DeletePerformer}.
     *
     * @param session The session
     * @param decorator The optional folder service decorator
     * @param folderStorageDiscoverer The folder storage discoverer
     * @throws OXException If passed session is invalid
     */
    public DeletePerformer(final ServerSession session, FolderServiceDecorator decorator, final FolderStorageDiscoverer folderStorageDiscoverer) throws OXException {
        super(session, decorator, folderStorageDiscoverer);
    }

    /**
     * Initializes a new {@link DeletePerformer}.
     *
     * @param user The user
     * @param context The context
     * @param decorator The optional folder service decorator
     * @param folderStorageDiscoverer The folder storage discoverer
     */
    public DeletePerformer(final User user, final Context context, FolderServiceDecorator decorator, final FolderStorageDiscoverer folderStorageDiscoverer) {
        super(user, context, decorator, folderStorageDiscoverer);
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
        if (KNOWN_TREES.contains(treeId)) {
            FolderStorage folderStorage = folderStorageDiscoverer.getFolderStorage(treeId, folderId);
            if (null == folderStorage) {
                throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, folderId);
            }
            if (null != timeStamp) {
                storageParameters.setTimeStamp(timeStamp);
            }

            TransactionManager transactionManager = TransactionManager.initTransaction(storageParameters);
            boolean rollbackTransaction = true;
            /*
             * Throws an exception if someone tries to add an element. If this happens, you found a bug.
             * As long as a TransactionManager is present, every storage has to add itself to the
             * TransactionManager in FolderStorage.startTransaction() and must return false.
             */
            final List<FolderStorage> openedStorages = Collections.emptyList();
            checkOpenedStorage(folderStorage, openedStorages);
            try {
                if (FolderStorage.REAL_TREE_ID.equals(treeId)) {
                    /*
                     * Real delete
                     */
                    deleteRealFolder(folderId, folderStorage);
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
                        deleteRealFolder(folderId, realStorage);
                    } else {
                        /*
                         * Delete from virtual storage
                         */
                        deleteVirtualFolder(folderId, treeId, folderStorage, openedStorages, transactionManager);
                    }
                }
                /*
                 * Commit
                 */
                transactionManager.commit();
                rollbackTransaction = false;
            } catch (OXException e) {
                throw e;
            } catch (Exception e) {
                throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
            } finally {
                if (rollbackTransaction) {
                    transactionManager.rollback();
                }
            }
        } else if (VirtualFolderStorage.FOLDER_TREE_EAS.equals(treeId)) {
            FolderStorage folderStorage = folderStorageDiscoverer.getFolderStorage(treeId, folderId);
            if (null == folderStorage) {
                throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, folderId);
            }
            folderStorage.deleteFolder(treeId, folderId, storageParameters);
        } else {
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create("Delete not supported by tree " + treeId);
        }
    }

    /**
     * Performs the <code>TRASH</code> request.
     *
     * @param treeId The tree identifier
     * @param folderId The folder identifier
     * @param timeStamp The requestor's last-modified time stamp
     * @return
     * @throws OXException If an error occurs during deletion
     */
    public TrashResult doTrash(final String treeId, final String folderId, final Date timeStamp) throws OXException {
        TrashResult result = null;
        if (KNOWN_TREES.contains(treeId)) {
            FolderStorage folderStorage = folderStorageDiscoverer.getFolderStorage(treeId, folderId);
            if (null == folderStorage) {
                throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, folderId);
            }
            checkTrashAware(folderStorage);
            if (null != timeStamp) {
                storageParameters.setTimeStamp(timeStamp);
            }

            TransactionManager transactionManager = TransactionManager.initTransaction(storageParameters);
            boolean rollbackTransaction = true;
            /*
             * Throws an exception if someone tries to add an element. If this happens, you found a bug.
             * As long as a TransactionManager is present, every storage has to add itself to the
             * TransactionManager in FolderStorage.startTransaction() and must return false.
             */
            final List<FolderStorage> openedStorages = Collections.emptyList();
            checkOpenedStorage(folderStorage, openedStorages);
            try {
                if (FolderStorage.REAL_TREE_ID.equals(treeId)) {
                    /*
                     * Real delete
                     */
                    result = trashRealFolder(folderId, folderStorage);
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
                        result = trashRealFolder(folderId, folderStorage);
                    } else {
                        /*
                         * Delete from virtual storage
                         */
                        trashVirtualFolder(folderId, treeId, folderStorage, openedStorages, transactionManager);
                    }
                }
                /*
                 * Commit
                 */
                transactionManager.commit();
                rollbackTransaction = false;
                return result;
            } catch (OXException e) {
                throw e;
            } catch (Exception e) {
                throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
            } finally {
                if (rollbackTransaction) {
                    transactionManager.rollback();
                }
            }
        } else if (VirtualFolderStorage.FOLDER_TREE_EAS.equals(treeId)) {
            FolderStorage folderStorage = folderStorageDiscoverer.getFolderStorage(treeId, folderId);
            if (null == folderStorage) {
                throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, folderId);
            }
            checkTrashAware(folderStorage);

            if (!(folderStorage instanceof TrashAwareFolderStorage)) {
                throw FolderExceptionErrorMessage.UNSUPPORTED_OPERATION.create();
            }

            ((TrashAwareFolderStorage) folderStorage).trashFolder(treeId, folderId, storageParameters);
            return result;
        } else {
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create("Delete not supported by tree " + treeId);
        }
    }

    private void checkTrashAware(FolderStorage storage) throws OXException {
        if (!(storage instanceof TrashAwareFolderStorage)){
            throw FolderExceptionErrorMessage.UNSUPPORTED_OPERATION.create();
        }
    }

    private void deleteVirtualFolder(final String folderId, final String treeId, final FolderStorage folderStorage, final List<FolderStorage> openedStorages, TransactionManager transactionManager) throws OXException {
        final Folder folder = folderStorage.getFolder(treeId, folderId, storageParameters);
        storageParameters.putParameter(FolderType.GLOBAL, "global", Boolean.valueOf(folder.isGlobalID()));
        {
            Permission permission = CalculatePermission.calculate(folder, this, ALL_ALLOWED);
            if (!permission.isVisible()) {
                throw FolderExceptionErrorMessage.FOLDER_NOT_VISIBLE.create(getFolderInfo4Error(folder), getUserInfo4Error(), getContextInfo4Error());
            }
            if (!permission.isAdmin()) {
                throw FolderExceptionErrorMessage.FOLDER_NOT_DELETEABLE.create(getFolderInfo4Error(folder), getUserInfo4Error(), getContextInfo4Error());
            }
            /*
             * Delete permissions
             */
            if (!canDeleteAllObjects(permission, folderId, treeId, folderStorage)) {
                throw FolderExceptionErrorMessage.FOLDER_NOT_DELETEABLE.create(getFolderInfo4Error(folder), getUserInfo4Error(), getContextInfo4Error());
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
            deleteVirtualFolder(id, treeId, subfolderStorage, openedStorages, transactionManager);
        }
        /*
         * Delete from real storage
         */
        final FolderStorage realStorage = folderStorageDiscoverer.getFolderStorage(FolderStorage.REAL_TREE_ID, folderId);
        if (null == realStorage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(FolderStorage.REAL_TREE_ID, folderId);
        }
        checkOpenedStorage(realStorage, openedStorages);
        deleteRealFolder(folder, realStorage);
        /*
         * And now from virtual storage
         */
        folderStorage.deleteFolder(treeId, folderId, storageParameters);
    }

    private TrashResult trashVirtualFolder(final String folderId, final String treeId, final FolderStorage folderStorage, final List<FolderStorage> openedStorages, TransactionManager transactionManager) throws OXException {
        if (!(folderStorage instanceof TrashAwareFolderStorage)) {
            throw FolderExceptionErrorMessage.UNSUPPORTED_OPERATION.create();
        }
        final Folder folder = folderStorage.getFolder(treeId, folderId, storageParameters);
        storageParameters.putParameter(FolderType.GLOBAL, "global", Boolean.valueOf(folder.isGlobalID()));
        {
            Permission permission = CalculatePermission.calculate(folder, this, ALL_ALLOWED);
            if (!permission.isVisible()) {
                throw FolderExceptionErrorMessage.FOLDER_NOT_VISIBLE.create(getFolderInfo4Error(folder), getUserInfo4Error(), getContextInfo4Error());
            }
            if (!permission.isAdmin()) {
                throw FolderExceptionErrorMessage.FOLDER_NOT_DELETEABLE.create(getFolderInfo4Error(folder), getUserInfo4Error(), getContextInfo4Error());
            }
            /*
             * Delete permissions
             */
            if (!canDeleteAllObjects(permission, folderId, treeId, folderStorage)) {
                throw FolderExceptionErrorMessage.FOLDER_NOT_DELETEABLE.create(getFolderInfo4Error(folder), getUserInfo4Error(), getContextInfo4Error());
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
            trashVirtualFolder(id, treeId, subfolderStorage, openedStorages, transactionManager);
        }
        /*
         * Delete from real storage
         */
        final FolderStorage realStorage = folderStorageDiscoverer.getFolderStorage(FolderStorage.REAL_TREE_ID, folderId);
        if (null == realStorage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(FolderStorage.REAL_TREE_ID, folderId);
        }
        checkOpenedStorage(realStorage, openedStorages);
        trashRealFolder(folder, realStorage);
        /*
         * And now from virtual storage
         */
        return ((TrashAwareFolderStorage) folderStorage).trashFolder(treeId, folderId, storageParameters);
    }

    private void deleteRealFolder(String id, FolderStorage storage) throws OXException {
        deleteRealFolder(storage.getFolder(FolderStorage.REAL_TREE_ID, id, storageParameters), storage);
    }

    private TrashResult trashRealFolder(String id, FolderStorage storage) throws OXException {
        return trashRealFolder(storage.getFolder(FolderStorage.REAL_TREE_ID, id, storageParameters), storage);
    }

    private void deleteRealFolder(Folder folder, FolderStorage storage) throws OXException {
        /*
         * delete folder
         */
        storage.deleteFolder(FolderStorage.REAL_TREE_ID, folder.getID(), storageParameters);
    }

    private TrashResult trashRealFolder(Folder folder, FolderStorage storage) throws OXException {
        /*
         * trash folder
         */
        if (storage instanceof TrashAwareFolderStorage) {
            return ((TrashAwareFolderStorage) storage).trashFolder(FolderStorage.REAL_TREE_ID, folder.getID(), storageParameters);
        }
        throw FolderExceptionErrorMessage.UNSUPPORTED_OPERATION.create();
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

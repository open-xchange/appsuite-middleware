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

package com.openexchange.folderstorage.internal.actions;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderException;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.internal.FolderStorageRegistry;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link Update} - Serves the <code>UPDATE</code> request.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Update extends AbstractAction {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(Update.class);

    /**
     * Initializes a new {@link Update} from given session.
     * 
     * @param session The session
     */
    public Update(final ServerSession session) {
        super(session);
    }

    /**
     * Initializes a new {@link Update} from given user-context-pair.
     * 
     * @param user The user
     * @param context The context
     */
    public Update(final User user, final Context context) {
        super(user, context);
    }

    /**
     * Performs the <code>UPDATE</code> request.
     * 
     * @param folder The object which denotes the folder to update and provides the changes to perform
     * @throws FolderException If update fails
     */
    public void doUpdate(final Folder folder) throws FolderException {
        final String folderId = folder.getID();
        if (null == folderId) {
            throw FolderExceptionErrorMessage.MISSING_FOLDER_ID.create(new Object[0]);
        }
        final String treeId = folder.getTreeID();
        if (null == treeId) {
            throw FolderExceptionErrorMessage.MISSING_TREE_ID.create(new Object[0]);
        }
        final FolderStorage storage = FolderStorageRegistry.getInstance().getFolderStorage(treeId, folderId);
        if (null == storage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, folderId);
        }
        storage.startTransaction(storageParameters, true);
        final List<FolderStorage> openedStorages = new ArrayList<FolderStorage>(4);
        openedStorages.add(storage);

        try {
            /*
             * Load storage folder
             */
            final Folder storageFolder = storage.getFolder(treeId, folderId, storageParameters);
            final boolean move;
            final String oldParentId = storageFolder.getParentID();
            {
                final String newParentId = folder.getParentID();
                move = (null != newParentId && !newParentId.equals(oldParentId));
            }
            final boolean rename;
            {
                final String newName = folder.getName();
                rename = (null != newName && !newName.equals(storageFolder.getName()));
            }
            final boolean changePermissions;
            {
                final Permission[] newPerms = folder.getPermissions();
                if (null == newPerms) {
                    changePermissions = false;
                } else {
                    final Permission[] oldPerms = storageFolder.getPermissions();
                    if (newPerms.length != oldPerms.length) {
                        changePermissions = true;
                    } else {
                        boolean equals = true;
                        for (int i = 0; equals && i < oldPerms.length; i++) {
                            final Permission oldPerm = oldPerms[i];
                            if (0 == oldPerm.getSystem()) {
                                final int entity = oldPerm.getEntity();
                                Permission compareWith = null;
                                for (int j = 0; null == compareWith && j < newPerms.length; j++) {
                                    final Permission newPerm = newPerms[j];
                                    if (newPerm.getEntity() == entity) {
                                        compareWith = newPerm;
                                    }
                                }
                                equals = (null != compareWith && compareWith.equals(oldPerm));
                            }
                        }
                        changePermissions = !equals;
                    }
                }
            }
            /*
             * Do move?
             */
            if (move) {
                /*
                 * Move folder dependent on folder is virtual or not
                 */
                final String newParentId = folder.getParentID();
                final FolderStorage newRealParentStorage = FolderStorageRegistry.getInstance().getFolderStorage(
                    FolderStorage.REAL_TREE_ID,
                    newParentId);
                if (null == newRealParentStorage) {
                    throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(FolderStorage.REAL_TREE_ID, newParentId);
                }

                final String parentId = folder.getParentID();
                final FolderStorage realParentStorage = FolderStorageRegistry.getInstance().getFolderStorage(
                    FolderStorage.REAL_TREE_ID,
                    parentId);
                if (null == realParentStorage) {
                    throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(FolderStorage.REAL_TREE_ID, parentId);
                }

                /*
                 * Check for a folder with the same name below parent
                 */
                if (!rename) {
                    final String newName = folder.getName();
                    folder.setName(newName == null ? storageFolder.getName() : newName);
                }
                if (equallyNamedSibling(folder.getName(), treeId, newParentId)) {
                    throw FolderExceptionErrorMessage.EQUAL_NAME.create(folder.getName(), newParentId, treeId);
                }

                /*
                 * Perform move either in real or in virtual storage
                 */
                if (FolderStorage.REAL_TREE_ID.equals(folder.getTreeID())) {
                    doMoveReal(folder, storage, realParentStorage, newRealParentStorage);
                } else {
                    doMoveVirtual(folder, storage, realParentStorage, newRealParentStorage, openedStorages);
                }
            } else if (rename) {
                folder.setParentID(oldParentId);
                if (equallyNamedSibling(folder.getName(), treeId, oldParentId)) {
                    throw FolderExceptionErrorMessage.EQUAL_NAME.create(folder.getName(), oldParentId, treeId);
                }

                /*
                 * Perform rename either in real or in virtual storage
                 */
                if (FolderStorage.REAL_TREE_ID.equals(folder.getTreeID())) {
                    doRenameReal(folder, storage);
                } else {
                    doRenameVirtual(folder, storage, openedStorages);
                }
            } else if (changePermissions) {
                /*
                 * Change permissions either in real or in virtual storage
                 */
                if (FolderStorage.REAL_TREE_ID.equals(folder.getTreeID())) {
                    storage.updateFolder(folder, storageParameters);
                } else {
                    final FolderStorage realStorage = FolderStorageRegistry.getInstance().getFolderStorage(
                        FolderStorage.REAL_TREE_ID,
                        folder.getID());
                    if (null == realStorage) {
                        throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(FolderStorage.REAL_TREE_ID, folder.getID());
                    }
                    checkOpenedStorage(realStorage, openedStorages);
                    realStorage.updateFolder(folder, storageParameters);
                    storage.updateFolder(folder, storageParameters);
                }
            }

            /*
             * Commit
             */
            for (final FolderStorage fs : openedStorages) {
                fs.commitTransaction(storageParameters);
            }
        } catch (final FolderException e) {
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

    } // End of doUpdate()

    private void doMoveReal(final Folder folder, final FolderStorage folderStorage, final FolderStorage realParentStorage, final FolderStorage newRealParentStorage) throws FolderException {
        if (folderStorage.equals(realParentStorage) && newRealParentStorage.equals(realParentStorage)) {
            throw FolderExceptionErrorMessage.MOVE_NOT_PERMITTED.create(new Object[0]);
        }
        folderStorage.updateFolder(folder, storageParameters);
    }

    private void doMoveVirtual(final Folder folder, final FolderStorage virtualStorage, final FolderStorage realParentStorage, final FolderStorage newRealParentStorage, final List<FolderStorage> openedStorages) throws FolderException {
        final FolderStorage realStorage = FolderStorageRegistry.getInstance().getFolderStorage(FolderStorage.REAL_TREE_ID, folder.getID());
        if (null == realStorage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(FolderStorage.REAL_TREE_ID, folder.getID());
        }

        final boolean parentChildEquality = realStorage.equals(realParentStorage);
        final boolean parentEquality = newRealParentStorage.equals(realParentStorage);
        if (parentChildEquality && parentEquality) {
            checkOpenedStorage(realStorage, openedStorages);
            /*
             * Perform the move in real storage
             */
            final Folder clone4Real = (Folder) folder.clone();
            clone4Real.setName(nonExistingName(clone4Real.getName(), FolderStorage.REAL_TREE_ID, clone4Real.getParentID()));
            realStorage.updateFolder(clone4Real, storageParameters);
            /*
             * Perform the move in virtual storage
             */
            virtualStorage.updateFolder(folder, storageParameters);
        } else if (!parentChildEquality && parentEquality) {
            /*
             * No real action required in this case. Perform the move in virtual storage only.
             */
            virtualStorage.updateFolder(folder, storageParameters);
        } else if (parentChildEquality && !parentEquality) {
            /*
             * Move to default location in real storage
             */
            checkOpenedStorage(realStorage, openedStorages);
            final String defaultParentId = realStorage.getDefaultFolderID(
                user,
                FolderStorage.REAL_TREE_ID,
                realStorage.getDefaultContentType(),
                storageParameters);
            // TODO: Check permission for obtained default folder ID?
            final Folder clone4Real = (Folder) folder.clone();
            clone4Real.setParentID(defaultParentId);
            clone4Real.setName(nonExistingName(clone4Real.getName(), FolderStorage.REAL_TREE_ID, defaultParentId));
            realStorage.updateFolder(clone4Real, storageParameters);
            /*
             * Perform the move in virtual storage
             */
            virtualStorage.updateFolder(folder, storageParameters);
        } else {
            /*
             * (!parentChildEquality && !parentEquality) ?
             */
            throw FolderExceptionErrorMessage.MOVE_NOT_PERMITTED.create(new Object[0]);
        }
    }

    private void doRenameReal(final Folder folder, final FolderStorage realStorage) throws FolderException {
        realStorage.updateFolder(folder, storageParameters);
    }

    private void doRenameVirtual(final Folder folder, final FolderStorage virtualStorage, final List<FolderStorage> openedStorages) throws FolderException {
        // Update name in real tree
        final FolderStorage realStorage = FolderStorageRegistry.getInstance().getFolderStorage(FolderStorage.REAL_TREE_ID, folder.getID());
        if (null == realStorage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(FolderStorage.REAL_TREE_ID, folder.getID());
        }
        checkOpenedStorage(realStorage, openedStorages);
        final Folder realFolder = realStorage.getFolder(FolderStorage.REAL_TREE_ID, folder.getID(), storageParameters);
        final Folder clone4Real = (Folder) folder.clone();
        clone4Real.setName(nonExistingName(clone4Real.getName(), FolderStorage.REAL_TREE_ID, realFolder.getParentID()));
        realStorage.updateFolder(clone4Real, storageParameters);
        // Update name in virtual tree
        virtualStorage.updateFolder(folder, storageParameters);
    }

    private void checkOpenedStorage(final FolderStorage storage, final List<FolderStorage> openedStorages) throws FolderException {
        for (final FolderStorage openedStorage : openedStorages) {
            if (openedStorage.equals(storage)) {
                return;
            }
        }
        storage.startTransaction(storageParameters, true);
        openedStorages.add(storage);
    }

    private boolean equallyNamedSibling(final String name, final String treeId, final String parentId) throws FolderException {
        final com.openexchange.folderstorage.internal.actions.List listAction;
        if (null == session) {
            listAction = new com.openexchange.folderstorage.internal.actions.List(user, context);
        } else {
            listAction = new com.openexchange.folderstorage.internal.actions.List(session);
        }
        listAction.setStorageParameters(storageParameters);
        final UserizedFolder[] subfolders = listAction.doList(treeId, parentId, true);
        for (final UserizedFolder userizedFolder : subfolders) {
            if (name.equals(userizedFolder.getName())) {
                return true;
            }
        }
        return false;
    }

    private String nonExistingName(final String name, final String treeId, final String parentId) throws FolderException {
        final com.openexchange.folderstorage.internal.actions.List listAction;
        if (null == session) {
            listAction = new com.openexchange.folderstorage.internal.actions.List(user, context);
        } else {
            listAction = new com.openexchange.folderstorage.internal.actions.List(session);
        }
        listAction.setStorageParameters(storageParameters);
        final UserizedFolder[] subfolders = listAction.doList(treeId, parentId, true);
        final StringBuilder sb = new StringBuilder();
        String nonExistingName = name;
        int i = 0;
        int count = 0;
        while (i < subfolders.length) {
            if (nonExistingName.equals(subfolders[i].getName())) {
                sb.setLength(0);
                sb.append(name).append('_').append(String.valueOf(++count));
                nonExistingName = sb.toString();
                i = 0;
            } else {
                i++;
            }
        }
        return nonExistingName;
    }

}

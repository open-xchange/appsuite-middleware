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
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.FolderStorageDiscoverer;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link UpdatePerformer} - Serves the <code>UPDATE</code> request.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UpdatePerformer extends AbstractUserizedFolderPerformer {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(UpdatePerformer.class));

    private static final boolean DEBUG_ENABLED = LOG.isDebugEnabled();

    /**
     * Initializes a new {@link UpdatePerformer} from given session.
     *
     * @param session The session
     */
    public UpdatePerformer(final ServerSession session, final FolderServiceDecorator decorator) {
        super(session, decorator);
    }

    /**
     * Initializes a new {@link UpdatePerformer} from given user-context-pair.
     *
     * @param user The user
     * @param context The context
     */
    public UpdatePerformer(final User user, final Context context, final FolderServiceDecorator decorator) {
        super(user, context, decorator);
    }

    /**
     * Initializes a new {@link UpdatePerformer}.
     *
     * @param session The session
     * @param folderStorageDiscoverer The folder storage discoverer
     */
    public UpdatePerformer(final ServerSession session, final FolderServiceDecorator decorator, final FolderStorageDiscoverer folderStorageDiscoverer) {
        super(session, decorator, folderStorageDiscoverer);
    }

    /**
     * Initializes a new {@link UpdatePerformer}.
     *
     * @param user The user
     * @param context The context
     * @param folderStorageDiscoverer The folder storage discoverer
     */
    public UpdatePerformer(final User user, final Context context, final FolderServiceDecorator decorator, final FolderStorageDiscoverer folderStorageDiscoverer) {
        super(user, context, decorator, folderStorageDiscoverer);
    }

    /**
     * Performs the <code>UPDATE</code> request.
     *
     * @param folder The object which denotes the folder to update and provides the changes to perform
     * @param timeStamp The requestor's last-modified time stamp
     * @throws OXException If update fails
     */
    public void doUpdate(final Folder folder, final Date timeStamp) throws OXException {
        final String folderId = folder.getID();
        if (null == folderId) {
            throw FolderExceptionErrorMessage.MISSING_FOLDER_ID.create(new Object[0]);
        }
        final String treeId = folder.getTreeID();
        if (null == treeId) {
            throw FolderExceptionErrorMessage.MISSING_TREE_ID.create(new Object[0]);
        }
        final FolderStorage storage = folderStorageDiscoverer.getFolderStorage(treeId, folderId);
        if (null == storage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, folderId);
        }
        final long start = DEBUG_ENABLED ? System.currentTimeMillis() : 0L;
        if (null != timeStamp) {
            storageParameters.setTimeStamp(timeStamp);
        }
        final List<FolderStorage> openedStorages = new ArrayList<FolderStorage>(4);
        checkOpenedStorage(storage, openedStorages);

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
                if (move && !"infostore".equals(storageFolder.getContentType().toString())) {
                    /*
                     * Check for duplicate
                     */
                    CheckForDuplicateResult result = getCheckForDuplicateResult(folder.getName(), treeId, newParentId, openedStorages);
                    if (null != result) {
                        final boolean autoRename = AJAXRequestDataTools.parseBoolParameter(getDecoratorStringProperty("autorename"));
                        if (!autoRename) {
                            throw result.error;
                        }
                        final boolean useParenthesis = PARENTHESIS_CAPABLE.contains(storageFolder.getContentType().toString());
                        int count = 2;
                        final StringBuilder nameBuilder = new StringBuilder(folder.getName());
                        final int resetLen = nameBuilder.length();
                        do {
                            nameBuilder.setLength(resetLen);
                            if (useParenthesis) {
                                nameBuilder.append(" (").append(count++).append(')');
                            } else {
                                nameBuilder.append(" ").append(count++);
                            }
                            result = getCheckForDuplicateResult(nameBuilder.toString(), treeId, newParentId, openedStorages);
                        } while (null != result);
                        folder.setName(nameBuilder.toString());
                    }
                }
            }
            final boolean rename;
            {
                final String newName = folder.getName();
                rename = (null != newName && !newName.equals(storageFolder.getName()));
                if (rename && !"infostore".equals(storageFolder.getContentType().toString())) {
                    /*
                     * Check for duplicate
                     */
                    CheckForDuplicateResult result = getCheckForDuplicateResult(newName, treeId, storageFolder.getParentID(), openedStorages);
                    if (null != result) {
                        final boolean autoRename = AJAXRequestDataTools.parseBoolParameter(getDecoratorStringProperty("autorename"));
                        if (!autoRename) {
                            throw result.error;
                        }
                        final boolean useParenthesis = PARENTHESIS_CAPABLE.contains(storageFolder.getContentType().toString());
                        int count = 2;
                        final StringBuilder nameBuilder = new StringBuilder(folder.getName());
                        final int resetLen = nameBuilder.length();
                        do {
                            nameBuilder.setLength(resetLen);
                            if (useParenthesis) {
                                nameBuilder.append(" (").append(count++).append(')');
                            } else {
                                nameBuilder.append(" ").append(count++);
                            }
                            result = getCheckForDuplicateResult(nameBuilder.toString(), treeId, storageFolder.getParentID(), openedStorages);
                        } while (null != result);
                        folder.setName(nameBuilder.toString());
                    }
                }
            }
            final boolean changePermissions;
            {
                final Permission[] newPerms = stripSystemPermissions(folder.getPermissions());
                if (null == newPerms) {
                    changePermissions = false;
                } else {
                    final Permission[] oldPerms = stripSystemPermissions(storageFolder.getPermissions());
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
            final boolean changeSubscription;
            {
                changeSubscription = (storageFolder.isSubscribed() != folder.isSubscribed());
            }
            /*
             * Do move?
             */
            if (move) {
                /*
                 * Move folder dependent on folder is virtual or not
                 */
                final String newParentId = folder.getParentID();
                final FolderStorage newRealParentStorage =
                    folderStorageDiscoverer.getFolderStorage(FolderStorage.REAL_TREE_ID, newParentId);
                if (null == newRealParentStorage) {
                    throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(FolderStorage.REAL_TREE_ID, newParentId);
                }

                final FolderStorage realParentStorage = folderStorageDiscoverer.getFolderStorage(FolderStorage.REAL_TREE_ID, oldParentId);
                if (null == realParentStorage) {
                    throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(FolderStorage.REAL_TREE_ID, oldParentId);
                }

                /*
                 * Check for a folder with the same name below parent
                 */
                if (!rename) {
                    final String newName = folder.getName();
                    folder.setName(newName == null ? storageFolder.getName() : newName);
                }
                if (equallyNamedSibling(folder.getName(), treeId, newParentId, openedStorages)) {
                    throw FolderExceptionErrorMessage.EQUAL_NAME.create(folder.getName(), newParentId, treeId);
                }
                /*
                 * Check for forbidden public mail folder
                 */
                {
                    final boolean started2 = newRealParentStorage.startTransaction(storageParameters, true);
                    try {
                        final Folder newParent = newRealParentStorage.getFolder(FolderStorage.REAL_TREE_ID, newParentId, storageParameters);
                        if ((FolderStorage.PUBLIC_ID.equals(newParent.getID()) || PublicType.getInstance().equals(newParent.getType())) && "mail".equals(storageFolder.getContentType().toString())) {
                            throw FolderExceptionErrorMessage.NO_PUBLIC_MAIL_FOLDER.create();
                        }
                        if (started2) {
                            newRealParentStorage.commitTransaction(storageParameters);
                        }
                    } catch (final OXException e) {
                        if (started2) {
                            newRealParentStorage.rollback(storageParameters);
                        }
                        throw e;
                    } catch (final Exception e) {
                        if (started2) {
                            newRealParentStorage.rollback(storageParameters);
                        }
                        throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
                    }
                }

                /*
                 * Perform move either in real or in virtual storage
                 */
                final MovePerformer movePerformer = newMovePerformer();
                movePerformer.setStorageParameters(storageParameters);
                if (FolderStorage.REAL_TREE_ID.equals(folder.getTreeID())) {
                    movePerformer.doMoveReal(folder, storage, realParentStorage, newRealParentStorage);
                } else {
                    movePerformer.doMoveVirtual(folder, storage, realParentStorage, newRealParentStorage, storageFolder, openedStorages);
                }
            } else if (rename) {
                folder.setParentID(oldParentId);
                if (equallyNamedSibling(folder.getName(), treeId, oldParentId, openedStorages)) {
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
                    final FolderStorage realStorage = folderStorageDiscoverer.getFolderStorage(FolderStorage.REAL_TREE_ID, folder.getID());
                    if (null == realStorage) {
                        throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(FolderStorage.REAL_TREE_ID, folder.getID());
                    }
                    if (storage.equals(realStorage)) {
                        storage.updateFolder(folder, storageParameters);
                    } else {
                        checkOpenedStorage(realStorage, openedStorages);
                        realStorage.updateFolder(folder, storageParameters);
                        storage.updateFolder(folder, storageParameters);
                    }
                }
            } else if (changeSubscription) {
                /*
                 * Change subscription either in real or in virtual storage
                 */
                if (FolderStorage.REAL_TREE_ID.equals(folder.getTreeID())) {
                    storage.updateFolder(folder, storageParameters);
                } else {
                    final FolderStorage realStorage = folderStorageDiscoverer.getFolderStorage(FolderStorage.REAL_TREE_ID, folder.getID());
                    if (null == realStorage) {
                        throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(FolderStorage.REAL_TREE_ID, folder.getID());
                    }
                    if (storage.equals(realStorage)) {
                        storage.updateFolder(folder, storageParameters);
                    } else {
                        checkOpenedStorage(realStorage, openedStorages);
                        realStorage.updateFolder(folder, storageParameters);
                        storage.updateFolder(folder, storageParameters);
                    }
                }
            }
            /*
             * Commit
             */
            for (final FolderStorage fs : openedStorages) {
                fs.commitTransaction(storageParameters);
            }

            final Set<OXException> warnings = storageParameters.getWarnings();
            if (null != warnings) {
                for (final OXException warning : warnings) {
                    addWarning(warning);
                }
            }

            if (DEBUG_ENABLED) {
                final long duration = System.currentTimeMillis() - start;
                LOG.debug(new StringBuilder().append("UpdatePerformer.doUpdate() took ").append(duration).append("msec for folder: ").append(
                    folderId).toString());
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

    } // End of doUpdate()

    private MovePerformer newMovePerformer() {
        if (null == session) {
            return new MovePerformer(user, context, folderStorageDiscoverer);
        }
        return new MovePerformer(session, folderStorageDiscoverer);
    }

    private void doRenameReal(final Folder folder, final FolderStorage realStorage) throws OXException {
        realStorage.updateFolder(folder, storageParameters);
    }

    private void doRenameVirtual(final Folder folder, final FolderStorage virtualStorage, final List<FolderStorage> openedStorages) throws OXException {
        // Update name in real tree
        final FolderStorage realStorage = folderStorageDiscoverer.getFolderStorage(FolderStorage.REAL_TREE_ID, folder.getID());
        if (null == realStorage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(FolderStorage.REAL_TREE_ID, folder.getID());
        }
        if (virtualStorage.equals(realStorage)) {
            virtualStorage.updateFolder(folder, storageParameters);
        } else {
            checkOpenedStorage(realStorage, openedStorages);
            final Folder realFolder = realStorage.getFolder(FolderStorage.REAL_TREE_ID, folder.getID(), storageParameters);
            final Folder clone4Real = (Folder) folder.clone();
            clone4Real.setParentID(null);
            clone4Real.setName(nonExistingName(clone4Real.getName(), FolderStorage.REAL_TREE_ID, realFolder.getParentID(), openedStorages));
            realStorage.updateFolder(clone4Real, storageParameters);
            // Update name in virtual tree
            folder.setNewID(clone4Real.getID());
            virtualStorage.updateFolder(folder, storageParameters);
            folder.setID(clone4Real.getID());
        }
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

    private boolean equallyNamedSibling(final String name, final String treeId, final String parentId, final Collection<FolderStorage> openedStorages) throws OXException {
        final ListPerformer listPerformer;
        if (null == session) {
            listPerformer = new ListPerformer(user, context, null);
        } else {
            listPerformer = new ListPerformer(session, null);
        }
        listPerformer.setStorageParameters(storageParameters);
        final UserizedFolder[] subfolders = listPerformer.doList(treeId, parentId, true, openedStorages, false);
        for (final UserizedFolder userizedFolder : subfolders) {
            if (name.equals(userizedFolder.getName())) {
                return true;
            }
        }
        return false;
    }

    private String nonExistingName(final String name, final String treeId, final String parentId, final Collection<FolderStorage> openedStorages) throws OXException {
        final ListPerformer listPerformer;
        if (null == session) {
            listPerformer = new ListPerformer(user, context, null);
        } else {
            listPerformer = new ListPerformer(session, null);
        }
        listPerformer.setStorageParameters(storageParameters);
        final UserizedFolder[] subfolders = listPerformer.doList(treeId, parentId, true, openedStorages, false);
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

    private static Permission[] stripSystemPermissions(final Permission[] permissions) {
        if (null == permissions) {
            return null;
        }
        final int len = permissions.length;
        final List<Permission> list = new ArrayList<Permission>(len);
        for (int i = 0; i < len; i++) {
            final Permission permission = permissions[i];
            if (0 == permission.getSystem()) {
                list.add(permission);
            }
        }
        return list.toArray(new Permission[list.size()]);
    }

}

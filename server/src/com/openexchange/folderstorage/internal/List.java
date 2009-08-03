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

package com.openexchange.folderstorage.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderException;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.SortableId;
import com.openexchange.folderstorage.StorageParameters;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.type.PrivateType;
import com.openexchange.folderstorage.type.SharedType;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link List} - Serves the list request.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class List {

    private final ServerSession session;

    private final User user;

    private final Context context;

    private final StorageParameters storageParameters;

    /**
     * Initializes a new {@link List} from given session.
     * 
     * @param session The session
     */
    public List(final ServerSession session) {
        super();
        this.session = session;
        user = session.getUser();
        context = session.getContext();
        storageParameters = new StorageParametersImpl(session);
    }

    /**
     * Initializes a new {@link List} from given user-context-pair.
     * 
     * @param user The user
     * @param context The context
     */
    public List(final User user, final Context context) {
        super();
        session = null;
        this.user = user;
        this.context = context;
        storageParameters = new StorageParametersImpl(user, context);
    };

    public UserizedFolder[] doList(final String treeId, final String parentId, final boolean all) throws FolderException {
        final FolderStorage folderStorage = FolderStorageRegistry.getInstance().getFolderStorage(treeId, parentId);
        if (null == folderStorage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, parentId);
        }
        folderStorage.startTransaction(storageParameters, false);
        final UserizedFolder[] ret;
        try {
            final Folder parent = folderStorage.getFolder(treeId, parentId, storageParameters);
            {
                /*
                 * Check folder permission for parent folder
                 */
                final Permission parentPermission;
                if (null == session) {
                    parentPermission = CalculatePermission.calculate(parent, user, context);
                } else {
                    parentPermission = CalculatePermission.calculate(parent, session);
                }
                if (parentPermission.getFolderPermission() <= Permission.NO_PERMISSIONS) {
                    // TODO: Throw exception
                }
            }

            final String[] subfolderIds = parent.getSubfolderIDs();
            if (null == subfolderIds) {
                ret = null;
            } else {
                /*
                 * The subfolders can be completely fetched from parent's folder storage
                 */
                final java.util.List<UserizedFolder> subfolders = new ArrayList<UserizedFolder>(subfolderIds.length);
                for (int i = 0; i < subfolderIds.length; i++) {
                    final Folder subfolder = folderStorage.getFolder(treeId, subfolderIds[i], storageParameters);
                    /*
                     * Check for access rights and subscribed status dependent on parameter "all"
                     */
                    final Permission subfolderPermission;
                    if (null == session) {
                        subfolderPermission = CalculatePermission.calculate(subfolder, user, context);
                    } else {
                        subfolderPermission = CalculatePermission.calculate(subfolder, session);
                    }
                    if (subfolderPermission.getFolderPermission() > Permission.NO_PERMISSIONS && (all ? true : subfolder.isSubscribed())) {
                        final UserizedFolder userizedFolder = new UserizedFolderImpl(subfolder);
                        // Permissions
                        userizedFolder.setOwnPermission(subfolderPermission);
                        CalculatePermission.calculateUserPermissions(userizedFolder, context);
                        // Type 
                        if (userizedFolder.getCreatedBy() != user.getId() && PrivateType.getInstance().equals(userizedFolder.getType())) {
                            userizedFolder.setType(SharedType.getInstance());
                            userizedFolder.setSubfolderIDs(new String[0]);
                        }
                        
                        subfolders.add(userizedFolder);
                    }

                }
                /*
                 * TODO: Sort subfolder list or expect them in proper order?
                 */
                ret = subfolders.toArray(new UserizedFolder[subfolders.size()]);
            }
            folderStorage.commitTransaction(storageParameters);
        } catch (final FolderException e) {
            folderStorage.rollback(storageParameters);
            throw e;
        } catch (final Exception e) {
            folderStorage.rollback(storageParameters);
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
        if (null == ret) {
            return getSubfoldersFromMultipleStorages(treeId, parentId, all);
        }
        return ret;
    }

    private UserizedFolder[] getSubfoldersFromMultipleStorages(final String treeId, final String parentId, final boolean all) throws FolderException {
        final FolderStorage[] parentStorages = FolderStorageRegistry.getInstance().getFolderStoragesForParent(treeId, parentId);
        for (final FolderStorage ps : parentStorages) {
            ps.startTransaction(storageParameters, false);
        }
        final java.util.List<FolderStorage> additionalStorages = new ArrayList<FolderStorage>(4);
        try {
            final java.util.List<SortableId> allSubfolderIds = new ArrayList<SortableId>(parentStorages.length * 8);
            for (final FolderStorage ps : parentStorages) {
                allSubfolderIds.addAll(Arrays.asList(ps.getSubfolders(treeId, parentId, storageParameters)));
            }
            Collections.sort(allSubfolderIds);
            final java.util.List<UserizedFolder> subfolders = new ArrayList<UserizedFolder>(allSubfolderIds.size());
            for (final SortableId sortableId : allSubfolderIds) {
                final String id = sortableId.getId();
                /*
                 * Get folder storage to serve subfolder: First check already opened storages, then obtain foreign storage if none
                 * appropriate.
                 */
                FolderStorage tmp = null;
                for (final FolderStorage ps : parentStorages) {
                    if (ps.getFolderType().servesFolderId(id)) {
                        tmp = ps;
                    }
                }
                if (null == tmp) {
                    tmp = FolderStorageRegistry.getInstance().getFolderStorage(treeId, id);
                    if (null == tmp) {
                        throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, id);
                    }
                    additionalStorages.add(tmp);
                    tmp.startTransaction(storageParameters, false);
                }
                /*
                 * Get subfolder from appropriate storage
                 */
                final Folder subfolder = tmp.getFolder(treeId, id, storageParameters);
                /*
                 * Check for access rights and subscribed status dependent on parameter "all"
                 */
                final Permission subfolderPermission;
                if (null == session) {
                    subfolderPermission = CalculatePermission.calculate(subfolder, user, context);
                } else {
                    subfolderPermission = CalculatePermission.calculate(subfolder, session);
                }
                if (subfolderPermission.getFolderPermission() > Permission.NO_PERMISSIONS && (all ? true : subfolder.isSubscribed())) {
                    final UserizedFolder userizedFolder = new UserizedFolderImpl(subfolder);
                    userizedFolder.setOwnPermission(subfolderPermission);
                    CalculatePermission.calculateUserPermissions(userizedFolder, context);
                    subfolders.add(userizedFolder);
                }
            }
            /*
             * Commit all used storages
             */
            if (!additionalStorages.isEmpty()) {
                for (final FolderStorage as : additionalStorages) {
                    as.commitTransaction(storageParameters);
                }
            }
            for (final FolderStorage ps : parentStorages) {
                ps.commitTransaction(storageParameters);
            }
            return subfolders.toArray(new UserizedFolder[subfolders.size()]);
        } catch (final FolderException e) {
            if (!additionalStorages.isEmpty()) {
                for (final FolderStorage as : additionalStorages) {
                    as.rollback(storageParameters);
                }
            }
            for (final FolderStorage storage : parentStorages) {
                storage.rollback(storageParameters);
            }
            throw e;
        } catch (final Exception e) {
            if (!additionalStorages.isEmpty()) {
                for (final FolderStorage as : additionalStorages) {
                    as.rollback(storageParameters);
                }
            }
            for (final FolderStorage storage : parentStorages) {
                storage.rollback(storageParameters);
            }
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }
}

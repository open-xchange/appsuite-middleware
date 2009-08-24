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
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderException;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.SortableId;
import com.openexchange.folderstorage.StorageType;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.database.contentType.InfostoreContentType;
import com.openexchange.folderstorage.internal.CalculatePermission;
import com.openexchange.folderstorage.internal.FolderStorageRegistry;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link Updates} - Serves the <code>UPDATES</code> request.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Updates extends AbstractUserizedFolderAction {

    /**
     * Initializes a new {@link Updates}.
     * 
     * @param session The session
     */
    public Updates(final ServerSession session) {
        super(session);
    }

    /**
     * Initializes a new {@link Updates}.
     * 
     * @param user The user
     * @param context The context
     */
    public Updates(final User user, final Context context) {
        super(user, context);
    }

    /**
     * Performs the <code>UPDATES</code> request.
     * 
     * @param treeId The tree identifier
     * @param since The time stamp
     * @param ignoreDeleted <code>true</code> to ignore delete operations performed since given time stamp; otherwise <code>false</code> to
     *            include them
     * @return All updated/deleted folders since given time stamp.
     * @throws FolderException If updates request fails
     */
    public UserizedFolder[][] doUpdates(final String treeId, final Date since, final boolean ignoreDeleted) throws FolderException {
        final FolderStorage[] realFolderStorages = FolderStorageRegistry.getInstance().getFolderStoragesForTreeID(
            FolderStorage.REAL_TREE_ID);
        for (final FolderStorage folderStorage : realFolderStorages) {
            folderStorage.startTransaction(storageParameters, false);
        }
        try {
            final UserConfiguration userConfiguration;
            {
                final Session s = storageParameters.getSession();
                if (s instanceof ServerSession) {
                    userConfiguration = ((ServerSession) s).getUserConfiguration();
                } else {
                    userConfiguration = UserConfigurationStorage.getInstance().getUserConfiguration(
                        user.getId(),
                        storageParameters.getContext());
                }
            }
            final List<Folder> updatedList = new ArrayList<Folder>();
            final List<Folder> deletedList = ignoreDeleted ? null : new ArrayList<Folder>();
            boolean addSystemSharedFolder;
            boolean checkVirtualListFolders;
            {
                /*
                 * Iterate real folder and get new-and-modified folders
                 */
                final List<Folder> modifiedFolders = new ArrayList<Folder>();
                for (final FolderStorage folderStorage : realFolderStorages) {
                    final String[] modifiedFolderIDs = folderStorage.getModifiedFolderIDs(since, storageParameters);
                    for (int i = 0; i < modifiedFolderIDs.length; i++) {
                        modifiedFolders.add(folderStorage.getFolder(FolderStorage.REAL_TREE_ID, modifiedFolderIDs[i], storageParameters));
                    }
                }
                addSystemSharedFolder = false;
                checkVirtualListFolders = false;
                final int size = modifiedFolders.size();
                final Iterator<Folder> iter = modifiedFolders.iterator();
                for (int i = 0; i < size; i++) {
                    final Folder f = iter.next();
                    final Permission effectivePerm = getEffectivePermission(f);
                    if (effectivePerm.getFolderPermission() >= Permission.READ_FOLDER) {
                        if (f.getCreatedBy() != getUser().getId()) {
                            /*
                             * Shared
                             */
                            addSystemSharedFolder = true;
                        } else if (PublicType.getInstance().equals(f.getType())) {
                            /*
                             * Public
                             */
                            final Folder parent = getFolder(FolderStorage.REAL_TREE_ID, f.getParentID(), realFolderStorages);
                            final Permission parentPerm = getEffectivePermission(parent);
                            if (parentPerm.getFolderPermission() >= Permission.READ_FOLDER) {
                                /*
                                 * Parent is visible
                                 */
                                updatedList.add(parent);
                            } else {
                                /*
                                 * Parent NOT visible. Update superior system folder to let the newly visible folder appear underneath virtual
                                 * "Other XYZ folders"
                                 */
                                if (InfostoreContentType.getInstance().equals(f.getContentType())) {
                                    updatedList.add(getFolder(
                                        FolderStorage.REAL_TREE_ID,
                                        String.valueOf(FolderObject.SYSTEM_INFOSTORE_FOLDER_ID),
                                        realFolderStorages));
                                } else {
                                    updatedList.add(getFolder(
                                        FolderStorage.REAL_TREE_ID,
                                        String.valueOf(FolderObject.SYSTEM_PUBLIC_FOLDER_ID),
                                        realFolderStorages));
                                }
                            }
                        }
                        updatedList.add(f);
                    } else {
                        checkVirtualListFolders |= (PublicType.getInstance().equals(f.getType()));
                        if (deletedList != null) {
                            deletedList.add(f);
                        }
                    }
                }
            }
            /*
             * Check virtual list folders
             */
            if (checkVirtualListFolders && deletedList != null) {
                final Set<String> set = getPublicSubfolderIDs(FolderStorage.REAL_TREE_ID, realFolderStorages);
                {
                    final String vid = String.valueOf(FolderObject.VIRTUAL_LIST_TASK_FOLDER_ID);
                    if (userConfiguration.hasTask() && !set.contains(vid)) {
                        deletedList.add(getFolder(FolderStorage.REAL_TREE_ID, vid, realFolderStorages));
                    }
                }
                {
                    final String vid = String.valueOf(FolderObject.VIRTUAL_LIST_CALENDAR_FOLDER_ID);
                    if (userConfiguration.hasCalendar() && !set.contains(vid)) {
                        deletedList.add(getFolder(FolderStorage.REAL_TREE_ID, vid, realFolderStorages));
                    }
                }
                {
                    final String vid = String.valueOf(FolderObject.VIRTUAL_LIST_CONTACT_FOLDER_ID);
                    if (userConfiguration.hasContact() && !set.contains(vid)) {
                        deletedList.add(getFolder(FolderStorage.REAL_TREE_ID, vid, realFolderStorages));
                    }
                }
                {
                    final String vid = String.valueOf(FolderObject.VIRTUAL_LIST_INFOSTORE_FOLDER_ID);
                    if (userConfiguration.hasInfostore() && !set.contains(vid)) {
                        deletedList.add(getFolder(FolderStorage.REAL_TREE_ID, vid, realFolderStorages));
                    }
                }
            }
            /*
             * Check if shared folder must be updated, too
             */
            if (addSystemSharedFolder) {
                updatedList.add(getFolder(
                    FolderStorage.REAL_TREE_ID,
                    String.valueOf(FolderObject.SYSTEM_SHARED_FOLDER_ID),
                    realFolderStorages));
            }
            /*
             * Get deleted folders
             */
            if (!ignoreDeleted) {
                final List<Folder> deletedFolders = new ArrayList<Folder>();
                for (final FolderStorage folderStorage : realFolderStorages) {
                    final String[] deletedFolderIDs = folderStorage.getDeletedFolderIDs(since, storageParameters);
                    for (int i = 0; i < deletedFolderIDs.length; i++) {
                        // Pass storage type to fetch folder from backup tables
                        deletedFolders.add(folderStorage.getFolder(
                            FolderStorage.REAL_TREE_ID,
                            deletedFolderIDs[i],
                            StorageType.BACKUP,
                            storageParameters));
                    }
                }
                /*
                 * Add previously gathered "deleted" folders
                 */
                deletedFolders.addAll(deletedList);
            }
            /*
             * Check tree
             */
            if (false && !FolderStorage.REAL_TREE_ID.equals(treeId)) {
                /*
                 * Check if folders are contained in given tree ID
                 */
                final FolderStorage fs = FolderStorageRegistry.getInstance().getFolderStoragesForTreeID(treeId)[0];
                for (final Iterator<Folder> iterator = updatedList.iterator(); iterator.hasNext();) {
                    final Folder folder = iterator.next();
                    if (!fs.containsFolder(treeId, folder.getID(), storageParameters)) {
                        iterator.remove();
                    }
                }
                if (null != deletedList) {
                    for (final Iterator<Folder> iterator = deletedList.iterator(); iterator.hasNext();) {
                        final Folder folder = iterator.next();
                        if (!fs.containsFolder(treeId, folder.getID(), StorageType.BACKUP, storageParameters)) {
                            iterator.remove();
                        }
                    }
                }
            }
            final List<FolderStorage> openedStorages = new ArrayList<FolderStorage>(Arrays.asList(realFolderStorages));
            final UserizedFolder[] modified = new UserizedFolder[updatedList.size()];
            for (int i = 0; i < modified.length; i++) {
                final Folder folder = updatedList.get(i);
                modified[i] = getUserizedFolder(folder, getEffectivePermission(folder), treeId, true, true, openedStorages);
            }

            final UserizedFolder[] deleted;
            if (deletedList != null) {
                deleted = new UserizedFolder[deletedList.size()];
                for (int i = 0; i < deleted.length; i++) {
                    final Folder folder = deletedList.get(i);
                    deleted[i] = getUserizedFolder(folder, getEffectivePermission(folder), treeId, true, true, openedStorages);
                }
            } else {
                deleted = new UserizedFolder[0];
            }
            return new UserizedFolder[][] { modified, deleted };
        } catch (final FolderException e) {
            for (final FolderStorage folderStorage : realFolderStorages) {
                folderStorage.rollback(storageParameters);
            }
            throw e;
        } catch (final Exception e) {
            for (final FolderStorage folderStorage : realFolderStorages) {
                folderStorage.rollback(storageParameters);
            }
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private Permission getEffectivePermission(final Folder folder) throws FolderException {
        if (null == getSession()) {
            return CalculatePermission.calculate(folder, getUser(), getContext());
        }
        return CalculatePermission.calculate(folder, getSession());
    }

    private Folder getFolder(final String treeId, final String folderId, final FolderStorage[] storages) throws FolderException {
        for (final FolderStorage storage : storages) {
            if (storage.getFolderType().servesFolderId(folderId)) {
                return storage.getFolder(treeId, folderId, storageParameters);
            }
        }
        throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, folderId);
    }

    private Set<String> getPublicSubfolderIDs(final String treeId, final FolderStorage[] storages) throws FolderException {
        final String folderId = String.valueOf(FolderObject.SYSTEM_PUBLIC_FOLDER_ID);
        for (final FolderStorage storage : storages) {
            if (storage.getFolderType().servesFolderId(folderId)) {
                final SortableId[] tmp = storage.getSubfolders(treeId, folderId, storageParameters);
                final Set<String> set = new HashSet<String>(tmp.length);
                for (final SortableId id : tmp) {
                    set.add(id.getId());
                }
                return set;
            }
        }
        throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, folderId);
    }

}

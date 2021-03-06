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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.CalculatePermission;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.FolderStorageDiscoverer;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.SortableId;
import com.openexchange.folderstorage.StorageParameters;
import com.openexchange.folderstorage.StorageType;
import com.openexchange.folderstorage.Type;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.database.contentType.InfostoreContentType;
import com.openexchange.folderstorage.type.PrivateType;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.folderstorage.type.SharedType;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.modules.Module;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.groupware.userconfiguration.UserPermissionBitsStorage;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.User;

/**
 * {@link UpdatesPerformer} - Serves the <code>UPDATES</code> request.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UpdatesPerformer extends AbstractUserizedFolderPerformer {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(UpdatesPerformer.class);

    /**
     * Initializes a new {@link UpdatesPerformer}.
     *
     * @param session The session
     * @param decorator The optional folder service decorator
     * @throws OXException If passed session is invalid
     */
    public UpdatesPerformer(final ServerSession session, final FolderServiceDecorator decorator) throws OXException {
        super(session, decorator);
    }

    /**
     * Initializes a new {@link UpdatesPerformer}.
     *
     * @param user The user
     * @param context The context
     * @param decorator The optional folder service decorator
     */
    public UpdatesPerformer(final User user, final Context context, final FolderServiceDecorator decorator) {
        super(user, context, decorator);
    }

    /**
     * Initializes a new {@link UpdatesPerformer}.
     *
     * @param session The session
     * @param decorator The optional folder service decorator
     * @param folderStorageDiscoverer The folder storage discoverer
     * @throws OXException If passed session is invalid
     */
    public UpdatesPerformer(final ServerSession session, final FolderServiceDecorator decorator, final FolderStorageDiscoverer folderStorageDiscoverer) throws OXException {
        super(session, decorator, folderStorageDiscoverer);
    }

    /**
     * Initializes a new {@link UpdatesPerformer}.
     *
     * @param user The user
     * @param context The context
     * @param decorator The optional folder service decorator
     * @param folderStorageDiscoverer The folder storage discoverer
     */
    public UpdatesPerformer(final User user, final Context context, final FolderServiceDecorator decorator, final FolderStorageDiscoverer folderStorageDiscoverer) {
        super(user, context, decorator, folderStorageDiscoverer);
    }

    /**
     * Performs the <code>UPDATES</code> request.
     *
     * @param treeId The tree identifier
     * @param since The time stamp
     * @param ignoreDeleted <code>true</code> to ignore delete operations performed since given time stamp; otherwise <code>false</code> to
     *            include them
     * @param includeContentTypes The content types to include
     * @return All updated/deleted folders since given time stamp.
     * @throws OXException If updates request fails
     */
    public UserizedFolder[][] doUpdates(final String treeId, final Date since, final boolean ignoreDeleted, final ContentType[] includeContentTypes) throws OXException {
        final List<FolderStorage> realFolderStorages =
            new ArrayList<FolderStorage>(Arrays.asList(folderStorageDiscoverer.getTreeFolderStorages(FolderStorage.REAL_TREE_ID)));
        final List<FolderStorage> openedStorages = new ArrayList<FolderStorage>(realFolderStorages.size() + 1);
        for (final FolderStorage folderStorage : realFolderStorages) {
            checkOpenedStorage(folderStorage, false, openedStorages);
        }
        try {
            final UserPermissionBits userPermissionBits;
            {
                final Session s = storageParameters.getSession();
                if (s instanceof ServerSession) {
                    userPermissionBits = ((ServerSession) s).getUserPermissionBits();
                } else {
                    userPermissionBits =
                        UserPermissionBitsStorage.getInstance().getUserPermissionBits(user.getId(), storageParameters.getContext());
                }
            }
            final boolean isReal = FolderStorage.REAL_TREE_ID.equals(treeId);
            final TreeChecker treeChecker;
            if (isReal) {
                treeChecker = TRUST_ALL_CHECKER;
            } else {
                /*
                 * There's only one storage for non-real tree identifier
                 */
                final FolderStorage[] treeFolderStorages = folderStorageDiscoverer.getTreeFolderStorages(treeId);
                if (null == treeFolderStorages || treeFolderStorages.length == 0) {
                    throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, "*");
                }
                final FolderStorage treeStorage = treeFolderStorages[0];
                checkOpenedStorage(treeStorage, false, openedStorages);
                treeChecker = new TreeCheckerImpl(treeStorage, storageParameters);
            }

            /*-
             * ++++++++++++++++++++++++++++++++++++++++------------++++++++++++++++++++++++++++++++++++++++
             * ++++++++++++++++++++++++++++++++++++++++ HERE WE GO ++++++++++++++++++++++++++++++++++++++++
             * ++++++++++++++++++++++++++++++++++++++++------------++++++++++++++++++++++++++++++++++++++++
             */

            final List<Folder> updatedList = new ArrayList<Folder>();
            final List<Folder> deletedList = ignoreDeleted ? null : new ArrayList<Folder>();
            final Set<String> virtualSharedIDs = new HashSet<String>();
            boolean addSystemSharedFolder;
            boolean checkVirtualListFolders;
            {
                /*
                 * Iterate real folder and get new-and-modified folders
                 */
                final List<Folder> modifiedFolders = new ArrayList<Folder>();
                for (final FolderStorage folderStorage : realFolderStorages) {
                    final String[] modifiedFolderIDs =
                        folderStorage.getModifiedFolderIDs(treeId, since, includeContentTypes, storageParameters);
                    if (modifiedFolderIDs.length > 0) {
                        try {
                            modifiedFolders.addAll(folderStorage.getFolders(
                                FolderStorage.REAL_TREE_ID,
                                Arrays.asList(modifiedFolderIDs),
                                storageParameters));
                        } catch (OXException e) {
                            for (final String modifiedFolderID : modifiedFolderIDs) {
                                try {
                                    modifiedFolders.add(folderStorage.getFolder(
                                        FolderStorage.REAL_TREE_ID,
                                        modifiedFolderID,
                                        storageParameters));
                                } catch (OXException ee) {
                                    LOG.error("Updated folder \"{}\" could not be fetched from storage \"{}\":\n{}", modifiedFolderID, folderStorage.getClass().getName(), ee.getMessage(),
                                        ee);
                                }
                            }
                        }
                    }
                }
                if (!FolderStorage.REAL_TREE_ID.equals(treeId)) {
                    final FolderStorage[] storages = folderStorageDiscoverer.getTreeFolderStorages(treeId);
                    for (final FolderStorage storage : storages) {
                        final boolean started = storage.startTransaction(storageParameters, false);
                        try {
                            final String[] modifiedFolderIDs =
                                storage.getModifiedFolderIDs(treeId, since, includeContentTypes, storageParameters);
                            if (modifiedFolderIDs.length > 0) {
                                try {
                                    modifiedFolders.addAll(storage.getFolders(
                                        FolderStorage.REAL_TREE_ID,
                                        Arrays.asList(modifiedFolderIDs),
                                        storageParameters));
                                } catch (OXException e) {
                                    for (final String modifiedFolderID : modifiedFolderIDs) {
                                        try {
                                            modifiedFolders.add(storage.getFolder(
                                                FolderStorage.REAL_TREE_ID,
                                                modifiedFolderID,
                                                storageParameters));
                                        } catch (OXException ee) {
                                            LOG.error("Updated folder \"{}\" could not be fetched from storage \"{}\":\n{}", modifiedFolderID, storage.getClass().getName(), ee.getMessage(),
                                                ee);
                                        }
                                    }
                                }
                            }
                            if (started) {
                                storage.commitTransaction(storageParameters);
                            }
                        } catch (OXException e) {
                            if (started) {
                                storage.rollback(storageParameters);
                            }
                            throw e;
                        } catch (Exception e) {
                            if (started) {
                                storage.rollback(storageParameters);
                            }
                            FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
                        }
                    }
                }
                addSystemSharedFolder = false;
                checkVirtualListFolders = false;
                final boolean sharedFolderAccess = userPermissionBits.hasFullSharedFolderAccess();
                final int size = modifiedFolders.size();
                final Iterator<Folder> iter = modifiedFolders.iterator();
                for (int i = 0; i < size; i++) {
                    final Folder f = iter.next();
                    final Permission effectivePerm = getEffectivePermission(f);
                    if (effectivePerm.isVisible()) {
                        if (isShared(f, getUserId())) {
                            if (sharedFolderAccess) {
                                /*
                                 * Add display name of shared folder owner
                                 */
                                virtualSharedIDs.add(new StringBuilder(8).append(FolderObject.SHARED_PREFIX).append(f.getCreatedBy()).toString());
                                /*
                                 * Remember to include system shared folder
                                 */
                                addSystemSharedFolder = true;
                            } else {
                                if (deletedList != null) {
                                    if (treeChecker.containsVirtualFolder(f.getID(), treeId, StorageType.WORKING)) {
                                        deletedList.add(f);
                                    }
                                }
                            }
                        } else if (isPublicOrInfostore(f)) {
                            /*
                             * Public
                             */
                            Folder parentFolder;
                            try {
                                parentFolder = getFolder(FolderStorage.REAL_TREE_ID, f.getParentID(), realFolderStorages);
                            } catch (OXException e) {
                                if ("FLD-0008".equals(e.getErrorCode())) {
                                    parentFolder = null; // "Folder X does not exist in context Y"
                                } else {
                                    throw e;
                                }
                            }
                            if (null != parentFolder && getEffectivePermission(parentFolder).isVisible()) {
                                /*
                                 * Parent is visible
                                 */
                                if (treeChecker.containsVirtualFolder(parentFolder.getID(), treeId, StorageType.WORKING) && listContainsFolder(updatedList, parentFolder) == false) {
                                    updatedList.add(parentFolder);
                                }
                            } else {
                                /*
                                 * Parent NOT visible. Update superior system folder to let the newly visible folder appear underneath
                                 * virtual "Other XYZ folders"
                                 */
                                if (InfostoreContentType.getInstance().equals(f.getContentType())) {
                                    final String infostoreId = String.valueOf(FolderObject.SYSTEM_INFOSTORE_FOLDER_ID);
                                    if (treeChecker.containsVirtualFolder(infostoreId, treeId, StorageType.WORKING)) {
                                        updatedList.add(getFolder(FolderStorage.REAL_TREE_ID, infostoreId, realFolderStorages));
                                    }
                                } else {
                                    final String publicId = String.valueOf(FolderObject.SYSTEM_PUBLIC_FOLDER_ID);
                                    if (treeChecker.containsVirtualFolder(publicId, treeId, StorageType.WORKING)) {
                                        updatedList.add(getFolder(FolderStorage.REAL_TREE_ID, publicId, realFolderStorages));
                                    }
                                }
                            }
                        }
                        if (treeChecker.containsVirtualFolder(f.getID(), treeId, StorageType.WORKING)) {
                            updatedList.add(f);
                        }
                    } else {
                        checkVirtualListFolders |= (PublicType.getInstance().equals(f.getType()));
                        if (deletedList != null) {
                            if (treeChecker.containsVirtualFolder(f.getID(), treeId, StorageType.WORKING)) {
                                deletedList.add(f);
                            }
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
                    if (userPermissionBits.hasTask() && !set.contains(vid)) {
                        if (treeChecker.containsVirtualFolder(vid, treeId, StorageType.WORKING)) {
                            deletedList.add(getFolder(FolderStorage.REAL_TREE_ID, vid, realFolderStorages));
                        }
                    }
                }
                {
                    final String vid = String.valueOf(FolderObject.VIRTUAL_LIST_CALENDAR_FOLDER_ID);
                    if (userPermissionBits.hasCalendar() && !set.contains(vid)) {
                        if (treeChecker.containsVirtualFolder(vid, treeId, StorageType.WORKING)) {
                            deletedList.add(getFolder(FolderStorage.REAL_TREE_ID, vid, realFolderStorages));
                        }
                    }
                }
                {
                    final String vid = String.valueOf(FolderObject.VIRTUAL_LIST_CONTACT_FOLDER_ID);
                    if (userPermissionBits.hasContact() && !set.contains(vid)) {
                        if (treeChecker.containsVirtualFolder(vid, treeId, StorageType.WORKING)) {
                            deletedList.add(getFolder(FolderStorage.REAL_TREE_ID, vid, realFolderStorages));
                        }
                    }
                }
                {
                    final String vid = String.valueOf(FolderObject.VIRTUAL_LIST_INFOSTORE_FOLDER_ID);
                    if (userPermissionBits.hasInfostore() && !set.contains(vid)) {
                        if (treeChecker.containsVirtualFolder(vid, treeId, StorageType.WORKING)) {
                            deletedList.add(getFolder(FolderStorage.REAL_TREE_ID, vid, realFolderStorages));
                        }
                    }
                }
            }
            /*
             * Check if shared folder must be updated, too
             */
            if (addSystemSharedFolder) {
                final String sharedId = String.valueOf(FolderObject.SYSTEM_SHARED_FOLDER_ID);
                if (treeChecker.containsVirtualFolder(sharedId, treeId, StorageType.WORKING)) {
                    updatedList.add(getFolder(FolderStorage.REAL_TREE_ID, sharedId, realFolderStorages));
                }
                if (!virtualSharedIDs.isEmpty()) {
                    for (final String virtualSharedID : virtualSharedIDs) {
                        updatedList.add(getFolder(FolderStorage.REAL_TREE_ID, virtualSharedID, realFolderStorages));
                    }
                }
            }
            /*
             * Get deleted folders
             */
            if (!ignoreDeleted && deletedList != null) {
                final List<Folder> deletedFolders = new ArrayList<Folder>();
                for (final FolderStorage folderStorage : realFolderStorages) {
                    final String[] deletedFolderIDs = folderStorage.getDeletedFolderIDs(treeId, since, storageParameters);
                    for (final String folderId : deletedFolderIDs) {
                        if (treeChecker.containsVirtualFolder(folderId, treeId, StorageType.BACKUP)) {
                            deletedFolders.add(folderStorage.getFolder(
                                FolderStorage.REAL_TREE_ID,
                                folderId,
                                StorageType.BACKUP,
                                storageParameters));
                        }
                    }
                }
                /*
                 * Add previously gathered "deleted" folders
                 */
                deletedList.addAll(deletedFolders);
            }
            /*
             * Generate array of modified folders
             */
            final UserizedFolder[] modified = new UserizedFolder[updatedList.size()];
            for (int i = 0; i < modified.length; i++) {
                final Folder folder = updatedList.get(i);
                modified[i] = getUserizedFolder(folder, getEffectivePermission(folder), treeId, true, true, storageParameters, realFolderStorages);
            }
            /*
             * Generate array of deleted folders (if non-null)
             */
            final UserizedFolder[] deleted;
            if (deletedList == null) {
                deleted = new UserizedFolder[0];
            } else {
                deleted = new UserizedFolder[deletedList.size()];
                for (int i = 0; i < deleted.length; i++) {
                    final Folder folder = deletedList.get(i);
                    deleted[i] =
                        getUserizedFolder(folder, getEffectivePermission(folder), treeId, true, true, storageParameters, realFolderStorages);
                }
            }
           
            /*
             * Commit
             */
            for (final FolderStorage folderStorage : openedStorages) {
                folderStorage.commitTransaction(storageParameters);
            }
            /*
             * Return result
             */
            return new UserizedFolder[][] { modified, deleted };
        } catch (OXException e) {
            for (final FolderStorage folderStorage : openedStorages) {
                folderStorage.rollback(storageParameters);
            }
            throw e;
        } catch (Exception e) {
            for (final FolderStorage folderStorage : openedStorages) {
                folderStorage.rollback(storageParameters);
            }
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private static interface TreeChecker {

        boolean containsVirtualFolder(String folderId, String treeId, StorageType storageType) throws OXException;
    }

    private static final TreeChecker TRUST_ALL_CHECKER = new TreeChecker() {

        @Override
        public boolean containsVirtualFolder(final String folderId, final String treeId, final StorageType storageType) throws OXException {
            return true;
        }
    };

    private static final class TreeCheckerImpl implements TreeChecker {

        private final FolderStorage treeStorage;

        private final StorageParameters storageParameters;

        public TreeCheckerImpl(final FolderStorage treeStorage, final StorageParameters storageParameters) {
            super();
            this.treeStorage = treeStorage;
            this.storageParameters = storageParameters;
        }

        @Override
        public boolean containsVirtualFolder(final String folderId, final String treeId, final StorageType storageType) throws OXException {
            /*
             * Check if folders are contained in given tree ID
             */
            return treeStorage.containsFolder(treeId, folderId, storageType, storageParameters);
        }

    }

    private Permission getEffectivePermission(final Folder folder) throws OXException {
        if (null == getSession()) {
            return CalculatePermission.calculate(folder, getUser(), getContext(), getAllowedContentTypes());
        }
        return CalculatePermission.calculate(folder, getSession(), getAllowedContentTypes());
    }

    private Folder getFolder(final String treeId, final String folderId, final Collection<FolderStorage> storages) throws OXException {
        for (final FolderStorage storage : storages) {
            if (storage.getFolderType().servesFolderId(folderId)) {
                return storage.getFolder(treeId, folderId, storageParameters);
            }
        }
        throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, folderId);
    }

    private Set<String> getPublicSubfolderIDs(final String treeId, final Collection<FolderStorage> storages) throws OXException {
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

    private static boolean isShared(final Folder f, final int userId) {
        final Type type = f.getType();
        return SharedType.getInstance().equals(type) || PrivateType.getInstance().equals(type) && f.getCreatedBy() != userId;
    }

    private static boolean isPublicOrInfostore(final Folder f) {
        return PublicType.getInstance().equals(f.getType()) || Module.INFOSTORE.getFolderConstant() == f.getContentType().getModule();
    }

    private static boolean listContainsFolder(List<Folder> folderList, Folder folder) {
        for (Folder f : folderList) {
            if (f.getID().equals(folder.getID())) {
                return true;
            }
        }
        return false;
    }


}

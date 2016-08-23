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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.composition.FilenameValidationUtils;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.FolderStorageDiscoverer;
import com.openexchange.folderstorage.LockCleaningFolderStorage;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.SetterAwareFolder;
import com.openexchange.folderstorage.SortableId;
import com.openexchange.folderstorage.StorageParametersUtility;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.database.contentType.InfostoreContentType;
import com.openexchange.folderstorage.filestorage.contentType.FileStorageContentType;
import com.openexchange.folderstorage.internal.CalculatePermission;
import com.openexchange.folderstorage.mail.contentType.MailContentType;
import com.openexchange.folderstorage.osgi.FolderStorageServices;
import com.openexchange.folderstorage.tx.TransactionManager;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.java.Strings;
import com.openexchange.objectusecount.IncrementArguments;
import com.openexchange.objectusecount.ObjectUseCountService;
import com.openexchange.share.GuestInfo;
import com.openexchange.share.recipient.RecipientType;
import com.openexchange.tools.oxfolder.OXFolderExceptionCode;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link UpdatePerformer} - Serves the <code>UPDATE</code> request.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UpdatePerformer extends AbstractUserizedFolderPerformer {

    private static final String CONTENT_TYPE_MAIL = MailContentType.getInstance().toString();

    /**
     * Initializes a new {@link UpdatePerformer} from given session.
     *
     * @param session The session
     * @throws OXException If passed session is invalid
     */
    public UpdatePerformer(final ServerSession session, final FolderServiceDecorator decorator) throws OXException {
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
     * @throws OXException If passed session is invalid
     */
    public UpdatePerformer(final ServerSession session, final FolderServiceDecorator decorator, final FolderStorageDiscoverer folderStorageDiscoverer) throws OXException {
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
        checkOpenedStorage(storage, openedStorages);
        try {
            /*
             * Load storage folder
             */
            final Folder storageFolder = storage.getFolder(treeId, folderId, storageParameters);
            final String oldParentId = storageFolder.getParentID();

            final boolean move;
            {
                final String newParentId = folder.getParentID();
                move = (null != newParentId && !newParentId.equals(oldParentId));
                if (move) {
                    boolean checkForReservedName = true;
                    if (null == folder.getName()) {
                        folder.setName(storageFolder.getName());
                        checkForReservedName = false;
                    }
                    if (null != checkForEqualName(treeId, newParentId, folder, storageFolder.getContentType(), true)) {
                        throw FolderExceptionErrorMessage.EQUAL_NAME.create(folder.getName(), getFolderNameSave(storage, newParentId), treeId);
                    }
                    if (checkForReservedName && !folder.getName().equals(storageFolder.getName()) && null != checkForReservedName(treeId, newParentId, folder, storageFolder.getContentType(), true)) {
                        throw FolderExceptionErrorMessage.RESERVED_NAME.create(folder.getName());
                    }
                }
            }

            final boolean rename;
            {
                final String newName = folder.getName();
                rename = (null != newName && !newName.equals(storageFolder.getName()));
                if (rename && false == move) {
                    if (null != checkForEqualName(treeId, storageFolder.getParentID(), folder, storageFolder.getContentType(), false)) {
                        throw FolderExceptionErrorMessage.EQUAL_NAME.create(folder.getName(), getFolderNameSave(storage, storageFolder.getParentID()), treeId);
                    }
                    if (null != checkForReservedName(treeId, storageFolder.getParentID(), folder, storageFolder.getContentType(), false)) {
                        throw FolderExceptionErrorMessage.RESERVED_NAME.create(folder.getName());
                    }
                    if (InfostoreContentType.getInstance().equals(storageFolder.getContentType()) && Strings.isNotEmpty(newName)) {
                        FilenameValidationUtils.checkCharacters(newName);
                        FilenameValidationUtils.checkName(newName);
                    }
                }
            }
            boolean changeSubscription = false;
            {
                if (folder instanceof SetterAwareFolder) {
                    if (((SetterAwareFolder) folder).containsSubscribed()) {
                        changeSubscription = (storageFolder.isSubscribed() != folder.isSubscribed());
                    }
                } else {
                    changeSubscription = (storageFolder.isSubscribed() != folder.isSubscribed());
                }
            }
            final boolean changedMetaInfo;
            {
                Map<String, Object> meta = folder.getMeta();
                if (null == meta) {
                    changedMetaInfo = false;
                } else {
                    Map<String, Object> storageMeta = storageFolder.getMeta();
                    if (null == storageMeta) {
                        changedMetaInfo = true;
                    } else {
                        changedMetaInfo = false == meta.equals(storageMeta);
                    }
                }
            }

            ComparedFolderPermissions comparedPermissions = new ComparedFolderPermissions(session, folder, storageFolder);
            boolean addedDecorator = false;
            FolderServiceDecorator decorator = storageParameters.getDecorator();
            if (decorator == null) {
                decorator = new FolderServiceDecorator();
                storageParameters.setDecorator(decorator);
                addedDecorator = true;
            }
            final boolean cascadePermissions = decorator.getBoolProperty("cascadePermissions");

            boolean isRecursion = decorator.containsProperty(RECURSION_MARKER);
            if (!isRecursion) {
                decorator.put(RECURSION_MARKER, true);
            }
            try {
                /*
                 * Do move?
                 */
                if (move) {
                    /*
                     * Move folder dependent on folder is virtual or not
                     */
                    final String newParentId = folder.getParentID();
                    FolderStorage newRealParentStorage = folderStorageDiscoverer.getFolderStorage(FolderStorage.REAL_TREE_ID, newParentId);
                    if (null == newRealParentStorage) {
                        throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(FolderStorage.REAL_TREE_ID, newParentId);
                    }
                    FolderStorage realParentStorage = folderStorageDiscoverer.getFolderStorage(FolderStorage.REAL_TREE_ID, oldParentId);
                    if (null == realParentStorage) {
                        throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(FolderStorage.REAL_TREE_ID, oldParentId);
                    }
                    FolderStorage realStorage = folderStorageDiscoverer.getFolderStorage(FolderStorage.REAL_TREE_ID, folder.getID());
                    if (null == realStorage) {
                        throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(FolderStorage.REAL_TREE_ID, folder.getID());
                    }
                    /*
                     * ensure FileStorageFolderStorage is used for move operations to/from a file storage
                     */
                    if (FileStorageContentType.getInstance().equals(realParentStorage.getDefaultContentType())) {
                        newRealParentStorage = realParentStorage;
                        realStorage = realParentStorage;
                    } else if (FileStorageContentType.getInstance().equals(newRealParentStorage.getDefaultContentType())) {
                        realParentStorage = newRealParentStorage;
                        realStorage = newRealParentStorage;
                    }
                    /*
                     * Check for a folder with the same name below parent
                     */
                    if (equallyNamedSibling(folder.getName(), treeId, newParentId, openedStorages)) {
                        throw FolderExceptionErrorMessage.EQUAL_NAME.create(folder.getName(), getFolderNameSave(newRealParentStorage, newParentId), treeId);
                    }
                    /*
                     * Check for forbidden public mail folder
                     */
                    if (CONTENT_TYPE_MAIL.equals(storageFolder.getContentType().toString())) {
                        boolean started = newRealParentStorage.startTransaction(storageParameters, true);
                        boolean rollback = true;
                        try {
                            Folder newParent = newRealParentStorage.getFolder(FolderStorage.REAL_TREE_ID, newParentId, storageParameters);
                            if (isPublicPimFolder(newParent)) {
                                throw FolderExceptionErrorMessage.NO_PUBLIC_MAIL_FOLDER.create();
                            }
                            if (started) {
                                newRealParentStorage.commitTransaction(storageParameters);
                                rollback = false;
                            }
                        } catch (RuntimeException e) {
                            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
                        } finally {
                            if (started && rollback) {
                                newRealParentStorage.rollback(storageParameters);
                            }
                        }
                    }
                    /*
                     * Perform move either in real or in virtual storage
                     */
                    MovePerformer movePerformer = newMovePerformer();
                    movePerformer.setStorageParameters(storageParameters);
                    if (FolderStorage.REAL_TREE_ID.equals(folder.getTreeID())) {
                        movePerformer.doMoveReal(folder, storage, realParentStorage, newRealParentStorage);
                    } else {
                        movePerformer.doMoveVirtual(folder, storage, realStorage, realParentStorage, newRealParentStorage, storageFolder, openedStorages);
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
                } else if (comparedPermissions.hasChanges() || cascadePermissions) {

                    ObjectUseCountService useCountService = FolderStorageServices.requireService(ObjectUseCountService.class);
                    List<Integer> addedUsers = comparedPermissions.getAddedUsers();
                    if (null != useCountService && null != addedUsers && !addedUsers.isEmpty()) {
                        for (Integer i : addedUsers) {
                            IncrementArguments arguments = new IncrementArguments.Builder(i.intValue()).build();
                            useCountService.incrementObjectUseCount(session, arguments);
                        }
                    }

                    /*
                     * Check permissions of anonymous guest users
                     */
                    checkGuestPermissions(storageFolder, comparedPermissions);
                    /*
                     * prepare new shares for added guest permissions
                     */
                    if (!isRecursion && comparedPermissions.hasNewGuests()) {
                        processAddedGuestPermissions(folderId, storageFolder.getContentType(), comparedPermissions, transactionManager.getConnection());
                    }
                    if (cascadePermissions) {
                        /*
                         * Switch back to false before update due to the recursive nature of FolderStorage.updateFolder in some implementations
                         */
                        decorator.put("cascadePermissions", Boolean.FALSE);
                    }
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

                            if(comparedPermissions.hasRemovedUsers()){
                                if (realStorage instanceof LockCleaningFolderStorage) {
                                    List<Permission> removedPermissions = comparedPermissions.getRemovedUserPermissions();
                                    int[] removedUserPermissions = new int[removedPermissions.size()];
                                    int x=0;
                                    for(Permission perm: removedPermissions){
                                        removedUserPermissions[x++] = perm.getEntity();
                                    }
                                    ((LockCleaningFolderStorage)realStorage).cleanLocksFor(folder, removedUserPermissions, storageParameters);
                                }
                            }
                        }
                    }
                    /*
                     * Cascade folder permissions
                     */
                    if (cascadePermissions) {
                        boolean ignoreWarnings = StorageParametersUtility.getBoolParameter("ignoreWarnings", storageParameters);
                        checkOpenedStorage(storage, openedStorages);
                        List<String> subfolderIDs = new ArrayList<String>();
                        try {
                            gatherSubfolders(folder, storage, treeId, subfolderIDs, ignoreWarnings);
                            if (0 < subfolderIDs.size()) {
                                /*
                                 * prepare target permissions: remove any anonymous link permission entities
                                 */
                                List<Permission> permissions = new ArrayList<Permission>(folder.getPermissions().length);
                                for (Permission permission : folder.getPermissions()) {
                                    if (false == permission.isGroup()) {
                                        GuestInfo guest = comparedPermissions.getGuestInfo(permission.getEntity());
                                        if (null != guest && RecipientType.ANONYMOUS.equals(guest.getRecipientType())) {
                                            continue;
                                        }
                                    }
                                    permissions.add(permission);
                                }
                                updatePermissions(storage, treeId, subfolderIDs, permissions.toArray(new Permission[permissions.size()]));
                            }
                        } catch (OXException e) {
                            if (OXFolderExceptionCode.NO_ADMIN_ACCESS.equals(e)) {
                                addWarning(e);
                                return;
                            }
                            throw e;
                        }
                    }
                    /*
                     * delete existing shares for removed guest permissions
                     */
                    if (!isRecursion && comparedPermissions.hasRemovedGuests()) {
                        processRemovedGuestPermissions(comparedPermissions.getRemovedGuestPermissions());
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
                } else if (changedMetaInfo) {
                    /*
                     * Change meta either in real or in virtual storage
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
            } finally {
                if (!isRecursion) {
                    decorator.remove(RECURSION_MARKER);
                }

                if (addedDecorator) {
                    storageParameters.setDecorator(null);
                }
            }
            /*
             * Commit
             */
            transactionManager.commit();
            rollbackTransaction = false;

            final Set<OXException> warnings = storageParameters.getWarnings();
            if (null != warnings) {
                for (final OXException warning : warnings) {
                    addWarning(warning);
                }
            }

        } catch (final OXException e) {
            throw e;
        } catch (final Exception e) {
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (rollbackTransaction) {
                transactionManager.rollback();
            }
        }
    } // End of doUpdate()

    /**
     * Gather all sub-folders that the current user has administrative rights.
     *
     * @param folder The folder
     * @param storage The folder storage
     * @param treeId The tree identifier
     * @param ids The already gathered sub-folders
     * @param ignoreWarnings Whether or not the warnings are going to be ignored
     * @throws OXException if the current user does not have administrative rights.
     */
    private void gatherSubfolders(Folder folder, FolderStorage storage, String treeId, List<String> ids, final boolean ignoreWarnings) throws OXException {
        SortableId[] sortableIds = storage.getSubfolders(treeId, folder.getID(), storageParameters);
        for (SortableId id : sortableIds) {
            Folder f = storage.getFolder(treeId, id.getId(), storageParameters);
            Permission permission = CalculatePermission.calculate(f, this, ALL_ALLOWED);
            if (!permission.isAdmin()) {
                if (!ignoreWarnings) {
                    int contextId;
                    int userId;
                    if (session == null) {
                        contextId = context.getContextId();
                        userId = user.getId();
                    } else {
                        contextId = session.getContextId();
                        userId = session.getUserId();
                    }
                    throw OXFolderExceptionCode.NO_ADMIN_ACCESS.create(userId, f.getName(), Integer.valueOf(contextId));
                }
            } else {
                ids.add(f.getID());
                gatherSubfolders(f, storage, treeId, ids, ignoreWarnings);
            }
        }
    }

    /**
     * Updates the permissions for multiple folders.
     *
     * @param storage The folder storage
     * @param treeId The tree identifier
     * @param folderIDs The identifiers of the folders to update the permissions
     * @param permissions The target permissions to apply for all folders
     * @throws OXException If applying the permissions fails.
     */
    private void updatePermissions(FolderStorage storage, String treeId, List<String> folderIDs, Permission[] permissions) throws OXException {
        for (String id : folderIDs) {
            UpdateFolder toUpdate = new UpdateFolder();
            toUpdate.setTreeID(treeId);
            toUpdate.setID(id);
            toUpdate.setPermissions(permissions);
            storage.updateFolder(toUpdate, storageParameters);
        }
    }

    private MovePerformer newMovePerformer() throws OXException {
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

    /**
     * Tries to get a folder's name, ignoring any exceptions that might occur. Useful for exception messages.
     *
     * @param storage The storage
     * @param folderId The folder identifier
     * @return The folder name, falling back to the identifier
     */
    private String getFolderNameSave(FolderStorage storage, String folderId) {
        try {
            Folder folder = storage.getFolder(FolderStorage.REAL_TREE_ID, folderId, storageParameters);
            return folder.getName();
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(UpdatePerformer.class).debug("Error getting name for folder '{}'", folderId, e);
            return folderId;
        }
    }

}

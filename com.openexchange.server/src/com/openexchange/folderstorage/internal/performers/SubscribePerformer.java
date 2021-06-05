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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import com.google.common.collect.ImmutableSet;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.CalculatePermission;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.FolderStorageDiscoverer;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.User;

/**
 * {@link SubscribePerformer} - Serves the <code>SUBSCRIBE</code> action.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SubscribePerformer extends AbstractPerformer {

    /**
     * Initializes a new {@link SubscribePerformer}.
     *
     * @param session
     * @throws OXException If passed session is invalid
     */
    public SubscribePerformer(final ServerSession session) throws OXException {
        super(session);
    }

    /**
     * Initializes a new {@link SubscribePerformer}.
     *
     * @param user
     * @param context
     */
    public SubscribePerformer(final User user, final Context context) {
        super(user, context);
    }

    /**
     * Initializes a new {@link SubscribePerformer}.
     *
     * @param session The session
     * @param folderStorageDiscoverer The folder storage discoverer
     * @throws OXException If passed session is invalid
     */
    public SubscribePerformer(final ServerSession session, final FolderStorageDiscoverer folderStorageDiscoverer) throws OXException {
        super(session, folderStorageDiscoverer);
    }

    /**
     * Initializes a new {@link SubscribePerformer}.
     *
     * @param user The user
     * @param context The context
     * @param folderStorageDiscoverer The folder storage discoverer
     */
    public SubscribePerformer(final User user, final Context context, final FolderStorageDiscoverer folderStorageDiscoverer) {
        super(user, context, folderStorageDiscoverer);
    }

    private static final Set<String> SYSTEM_FOLDERS = ImmutableSet.of(
        FolderStorage.ROOT_ID,
        FolderStorage.PRIVATE_ID,
        FolderStorage.PUBLIC_ID,
        FolderStorage.SHARED_ID);

    private static final Set<String> VIRTUAL_IDS =  ImmutableSet.of(
        Integer.toString(FolderObject.VIRTUAL_LIST_TASK_FOLDER_ID), Integer.toString(FolderObject.VIRTUAL_LIST_CALENDAR_FOLDER_ID),
            Integer.toString(FolderObject.VIRTUAL_LIST_CONTACT_FOLDER_ID), Integer.toString(FolderObject.VIRTUAL_LIST_INFOSTORE_FOLDER_ID));

    /**
     * The prepared fullname.
     */
    private static final String PREPARED_FULLNAME_DEFAULT = MailFolderUtility.prepareFullname(MailAccount.DEFAULT_ID, MailFolder.ROOT_FOLDER_ID);

    private static boolean isSystemFolder(final String folderId) {
        if (null == folderId) {
            return false;
        }
        if (SYSTEM_FOLDERS.contains(folderId)) {
            return true;
        }
        if (folderId.startsWith(FolderObject.SHARED_PREFIX)) {
            return true;
        }
        if (VIRTUAL_IDS.contains(folderId)) {
            return true;
        }
        if (PREPARED_FULLNAME_DEFAULT.equals(folderId)) {
            return true;
        }
        return false;
    }

    /**
     * Performs the <code>SUBSCRIBE</code> action.
     *
     * @param sourceTreeId The source tree identifier
     * @param folderId The folder identifier
     * @param targetTreeId The target tree identifier
     * @param optTargetParentId The optional target parent identifier
     * @throws OXException If a folder error occurs
     */
    public void doSubscribe(final String sourceTreeId, final String folderId, final String targetTreeId, final String optTargetParentId) throws OXException {
        if (KNOWN_TREES.contains(targetTreeId)) {
            throw FolderExceptionErrorMessage.NO_REAL_SUBSCRIBE.create(targetTreeId);
        }
        final FolderStorage sourceStorage = folderStorageDiscoverer.getFolderStorage(sourceTreeId, folderId);
        if (null == sourceStorage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(sourceTreeId, folderId);
        }
        final List<FolderStorage> openedStorages = new ArrayList<FolderStorage>(4);
        if (sourceStorage.startTransaction(storageParameters, false)) {
            openedStorages.add(sourceStorage);
        }
        try {
            Folder sourceFolder = sourceStorage.getFolder(sourceTreeId, folderId, storageParameters);
            /*
             * Check folder permission
             */
            if (!CalculatePermission.calculate(sourceFolder, this, ALL_ALLOWED).isVisible()) {
                throw FolderExceptionErrorMessage.FOLDER_NOT_VISIBLE.create(getFolderInfo4Error(sourceFolder), getUserInfo4Error(), getContextInfo4Error());
            }
            /*
             * Determine parent in target tree
             */
            final String targetParentId = optTargetParentId == null ? sourceFolder.getParentID() : optTargetParentId;
            final FolderStorage targetStorage = getOpenedStorage(targetParentId, targetTreeId, storageParameters, openedStorages);
            /*
             * Check for equally named folder
             */
            if (targetStorage.containsFolder(targetTreeId, targetParentId, storageParameters)) {
                checkForSimilarNamed(sourceFolder, targetTreeId, targetParentId, openedStorages);
            }
            /*
             * List of folders
             */
            final LinkedList<Folder> folders = new LinkedList<Folder>();

            Folder virtualFolder = (Folder) sourceFolder.clone();
            virtualFolder.setParentID(targetParentId);
            virtualFolder.setTreeID(targetTreeId);
            virtualFolder.setSubfolderIDs(null);
            folders.add(virtualFolder);

            while (!isSystemFolder(virtualFolder.getParentID()) && !targetStorage.containsFolder(targetTreeId, virtualFolder.getParentID(), storageParameters)) {
                sourceFolder = sourceStorage.getFolder(sourceTreeId, virtualFolder.getParentID(), storageParameters);
                /*
                 * Check folder permission for parent folder
                 */
                final Permission parentPermission = CalculatePermission.calculate(sourceFolder, this, ALL_ALLOWED);
                if (parentPermission.isVisible()) {
                    virtualFolder = (Folder) sourceFolder.clone();
                    final String parentId = sourceFolder.getParentID();
                    virtualFolder.setParentID(isSystemFolder(parentId) ? FolderStorage.ROOT_ID : parentId);
                    virtualFolder.setTreeID(targetTreeId);
                    virtualFolder.setSubfolderIDs(null);
                    folders.addFirst(virtualFolder);
                } else {
                    virtualFolder.setParentID(FolderStorage.ROOT_ID);
                }
            }

            for (final Folder folder : folders) {
                targetStorage.createFolder(folder, storageParameters);
            }

            for (final FolderStorage fs : openedStorages) {
                fs.commitTransaction(storageParameters);
            }

            final Set<OXException> warnings = storageParameters.getWarnings();
            if (null != warnings) {
                for (final OXException warning : warnings) {
                    addWarning(warning);
                }
            }

        } catch (OXException e) {
            for (final FolderStorage fs : openedStorages) {
                fs.rollback(storageParameters);
            }
            throw e;
        } catch (Exception e) {
            for (final FolderStorage fs : openedStorages) {
                fs.rollback(storageParameters);
            }
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private void checkForSimilarNamed(final Folder sourceFolder, final String targetTreeId, final String targetParentId, final List<FolderStorage> openedStorages) throws OXException {
        /*
         * Check for equally named folder
         */
        final UserizedFolder[] subfolders =
            (session == null ? new ListPerformer(user, context, null) : new ListPerformer(session, null)).doList(
                targetTreeId,
                targetParentId,
                true,
                openedStorages,
                false);
        final String sourceId = sourceFolder.getID();
        final String sourceName = sourceFolder.getName();
        for (final UserizedFolder userizedFolder : subfolders) {
            if (!userizedFolder.getID().equals(sourceId) && userizedFolder.getName().equals(sourceName)) {
                throw FolderExceptionErrorMessage.EQUAL_NAME.create(sourceName, targetParentId, targetTreeId);
            }
        }
    }

}

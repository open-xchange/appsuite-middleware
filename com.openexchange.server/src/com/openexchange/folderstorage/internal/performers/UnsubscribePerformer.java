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
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.CalculatePermission;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.FolderStorageDiscoverer;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.SortableId;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.User;

/**
 * {@link UnsubscribePerformer} - Serves the <code>UNSUBSCRIBE</code> action.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UnsubscribePerformer extends AbstractPerformer {

    /**
     * Initializes a new {@link UnsubscribePerformer}.
     *
     * @param session
     * @throws OXException If passed session is invalid
     */
    public UnsubscribePerformer(final ServerSession session) throws OXException {
        super(session);
    }

    /**
     * Initializes a new {@link UnsubscribePerformer}.
     *
     * @param user
     * @param context
     */
    public UnsubscribePerformer(final User user, final Context context) {
        super(user, context);
    }

    /**
     * Initializes a new {@link UnsubscribePerformer}.
     *
     * @param session The session
     * @param folderStorageDiscoverer The folder storage discoverer
     * @throws OXException If passed session is invalid
     */
    public UnsubscribePerformer(final ServerSession session, final FolderStorageDiscoverer folderStorageDiscoverer) throws OXException {
        super(session, folderStorageDiscoverer);
    }

    /**
     * Initializes a new {@link UnsubscribePerformer}.
     *
     * @param user The user
     * @param context The context
     * @param folderStorageDiscoverer The folder storage discoverer
     */
    public UnsubscribePerformer(final User user, final Context context, final FolderStorageDiscoverer folderStorageDiscoverer) {
        super(user, context, folderStorageDiscoverer);
    }

    /**
     * Performs the <code>UNSUBSCRIBE</code> action.
     *
     * @param treeId The virtual tree identifier
     * @param folderId The folder identifier
     * @throws OXException If a folder error occurs
     */
    public void doUnsubscribe(final String treeId, final String folderId) throws OXException {
        if (KNOWN_TREES.contains(treeId)) {
            throw FolderExceptionErrorMessage.NO_REAL_UNSUBSCRIBE.create(treeId);
        }
        final FolderStorage virtualStorage = folderStorageDiscoverer.getFolderStorage(treeId, folderId);
        if (null == virtualStorage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, folderId);
        }
        final java.util.List<FolderStorage> openedStorages = new ArrayList<FolderStorage>(4);
        if (virtualStorage.startTransaction(storageParameters, false)) {
            openedStorages.add(virtualStorage);
        }
        try {
            unsubscribeFolder(treeId, folderId, virtualStorage, true);
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

    private void unsubscribeFolder(final String treeId, final String folderId, final FolderStorage virtualStorage, final boolean allowSubfolderUnsubscribe) throws OXException {
        if (!virtualStorage.containsFolder(treeId, folderId, storageParameters)) {
            return;
        }
        /*
         * Unsubscribe contained folder
         */
        Folder folder = virtualStorage.getFolder(treeId, folderId, storageParameters);
        /*
         * Check folder permission
         */
        Permission permission = CalculatePermission.calculate(folder, this, ALL_ALLOWED);
        if (!permission.isVisible()) {
            throw FolderExceptionErrorMessage.FOLDER_NOT_VISIBLE.create(getFolderInfo4Error(folder), getUserInfo4Error(), getContextInfo4Error());
        }
        /*
         * Check subfolders
         */
        {
            /*
             * No unsubscribe on a folder which has subfolders
             */
            final String[] ids = folder.getSubfolderIDs();
            if (null == ids) {
                final SortableId[] sortableIds = virtualStorage.getSubfolders(treeId, folderId, storageParameters);
                if (sortableIds.length > 0) {
                    if (!allowSubfolderUnsubscribe) {
                        throw FolderExceptionErrorMessage.NO_UNSUBSCRIBE.create(folderId, treeId);
                    }
                    for (final SortableId sortableId : sortableIds) {
                        unsubscribeFolder(treeId, sortableId.getId(), virtualStorage, allowSubfolderUnsubscribe);
                    }
                }
            } else {
                if (ids.length > 0) {
                    if (!allowSubfolderUnsubscribe) {
                        throw FolderExceptionErrorMessage.NO_UNSUBSCRIBE.create(folderId, treeId);
                    }
                    for (final String id : ids) {
                        unsubscribeFolder(treeId, id, virtualStorage, allowSubfolderUnsubscribe);
                    }
                }
            }
        }
        virtualStorage.deleteFolder(treeId, folderId, storageParameters);
    }

}

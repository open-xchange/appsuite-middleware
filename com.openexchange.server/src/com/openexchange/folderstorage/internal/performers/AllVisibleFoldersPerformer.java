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
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderFilter;
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.FolderStorageDiscoverer;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.User;

/**
 * {@link AllVisibleFoldersPerformer} - Serves the request to deliver all visible folders.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class AllVisibleFoldersPerformer extends AbstractUserizedFolderPerformer {

    /**
     * Initializes a new {@link AllVisibleFoldersPerformer}.
     *
     * @param session The session
     * @param decorator The optional folder service decorator
     * @throws OXException If passed session is invalid
     */
    public AllVisibleFoldersPerformer(final ServerSession session, final FolderServiceDecorator decorator) throws OXException {
        super(session, decorator);
    }

    /**
     * Initializes a new {@link AllVisibleFoldersPerformer}.
     *
     * @param user The user
     * @param context The context final
     * @param decorator The optional folder service decorator
     */
    public AllVisibleFoldersPerformer(final User user, final Context context, final FolderServiceDecorator decorator) {
        super(user, context, decorator);
    }

    /**
     * Initializes a new {@link AllVisibleFoldersPerformer}.
     *
     * @param session The session
     * @param decorator The optional folder service decorator
     * @param folderStorageDiscoverer The folder storage discoverer
     * @throws OXException If passed session is invalid
     */
    public AllVisibleFoldersPerformer(final ServerSession session, final FolderServiceDecorator decorator, final FolderStorageDiscoverer folderStorageDiscoverer) throws OXException {
        super(session, decorator, folderStorageDiscoverer);
    }

    /**
     * Initializes a new {@link AllVisibleFoldersPerformer}.
     *
     * @param user The user
     * @param context The context
     * @param decorator The optional folder service decorator
     * @param folderStorageDiscoverer The folder storage discoverer
     */
    public AllVisibleFoldersPerformer(final User user, final Context context, final FolderServiceDecorator decorator, final FolderStorageDiscoverer folderStorageDiscoverer) {
        super(user, context, decorator, folderStorageDiscoverer);
    }

    /**
     * Gets all visible folders
     *
     * @param treeId The tree identifier
     * @param filter The (optional) folder filter; set to <code>null</code> to filter none
     * @return All visible folders
     * @throws OXException If a folder error occurs
     */
    public UserizedFolder[] doAllVisibleFolders(final String treeId, final FolderFilter filter) throws OXException {
        final FolderStorage rootStorage = folderStorageDiscoverer.getFolderStorage(treeId, FolderStorage.ROOT_ID);
        if (null == rootStorage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, FolderStorage.ROOT_ID);
        }
        final List<FolderStorage> openedStorages = new ArrayList<FolderStorage>(4);
        if (rootStorage.startTransaction(storageParameters, false)) {
            openedStorages.add(rootStorage);
        }
        try {
            final List<UserizedFolder> visibleFolders = new ArrayList<UserizedFolder>();
            final ListPerformer listAction =
                null == session ? new ListPerformer(user, context, getDecorator()) : new ListPerformer(session, getDecorator());

            fillSubfolders(treeId, FolderStorage.ROOT_ID, filter, visibleFolders, listAction, openedStorages);

            final UserizedFolder[] ret = visibleFolders.toArray(new UserizedFolder[visibleFolders.size()]);

            for (final FolderStorage fs : openedStorages) {
                fs.commitTransaction(storageParameters);
            }

            return ret;
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

    private void fillSubfolders(final String treeId, final String parentId, final FolderFilter filter, final List<UserizedFolder> visibleFolders, final ListPerformer listAction, final Collection<FolderStorage> openedStorages) throws OXException {
        final UserizedFolder[] subfolders = getSubfolders(treeId, parentId, listAction, openedStorages);
        if (subfolders.length > 0) {
            if (null == filter) {
                /*
                 * No filter
                 */
                visibleFolders.addAll(Arrays.asList(subfolders));
                for (final UserizedFolder subfolder : subfolders) {
                    fillSubfolders(treeId, subfolder.getID(), filter, visibleFolders, listAction, openedStorages);
                }
            } else {
                /*
                 * With filter
                 */
                for (int i = 0; i < subfolders.length; i++) {
                    final UserizedFolder subfolder = subfolders[i];
                    if (filter.accept(subfolder)) {
                        visibleFolders.add(subfolder);
                    } else {
                        subfolders[i] = null;
                    }
                }
                for (final UserizedFolder subfolder : subfolders) {
                    if (null != subfolder) {
                        fillSubfolders(treeId, subfolder.getID(), filter, visibleFolders, listAction, openedStorages);
                    }
                }
            }
        }
    }

    private UserizedFolder[] getSubfolders(final String treeId, final String parentId, final ListPerformer listAction, final Collection<FolderStorage> openedStorages) throws OXException {
        return listAction.doList(treeId, parentId, true, openedStorages, false);
    }

}

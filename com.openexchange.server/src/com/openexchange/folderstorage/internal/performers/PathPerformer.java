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
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.CalculatePermission;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.FolderStorageDiscoverer;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.User;

/**
 * {@link PathPerformer} - Serves the <code>PATH</code> request.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class PathPerformer extends AbstractUserizedFolderPerformer {

    private static final int MAX_PATH_DEPTH = 256;

    /**
     * Initializes a new {@link PathPerformer} from given session.
     *
     * @param session The session
     * @param decorator The optional folder service decorator
     * @throws OXException If passed session is invalid
     */
    public PathPerformer(final ServerSession session, final FolderServiceDecorator decorator) throws OXException {
        super(session, decorator);
    }

    /**
     * Initializes a new {@link PathPerformer} from given user-context-pair.
     *
     * @param user The user
     * @param context The context
     * @param decorator The optional folder service decorator
     */
    public PathPerformer(final User user, final Context context, final FolderServiceDecorator decorator) {
        super(user, context, decorator);
    }

    /**
     * Initializes a new {@link PathPerformer}.
     *
     * @param session The session
     * @param decorator The optional folder service decorator
     * @param folderStorageDiscoverer The folder storage discoverer
     * @throws OXException If passed session is invalid
     */
    public PathPerformer(final ServerSession session, final FolderServiceDecorator decorator, final FolderStorageDiscoverer folderStorageDiscoverer) throws OXException {
        super(session, decorator, folderStorageDiscoverer);
    }

    /**
     * Initializes a new {@link PathPerformer}.
     *
     * @param user The user
     * @param context The context
     * @param decorator The optional folder service decoratorde
     * @param folderStorageDiscoverer The folder storage discoverer
     */
    public PathPerformer(final User user, final Context context, final FolderServiceDecorator decorator, final FolderStorageDiscoverer folderStorageDiscoverer) {
        super(user, context, decorator, folderStorageDiscoverer);
    }

    private static interface PermissionProvider {

        Permission getOwnPermission(Folder folder) throws OXException;
    }

    private static final class SessionPermissionProvider implements PermissionProvider {

        private final ServerSession session;

        private final java.util.List<ContentType> allowedContentTypes;

        public SessionPermissionProvider(final ServerSession session, final java.util.List<ContentType> allowedContentTypes) {
            super();
            this.session = session;
            this.allowedContentTypes = allowedContentTypes;
        }

        @Override
        public Permission getOwnPermission(final Folder folder) {
            return CalculatePermission.calculate(folder, session, allowedContentTypes);
        }

    }

    private static final class UserCtxPermissionProvider implements PermissionProvider {

        private final User user;

        private final Context ctx;

        private final java.util.List<ContentType> allowedContentTypes;

        public UserCtxPermissionProvider(final User user, final Context ctx, final java.util.List<ContentType> allowedContentTypes) {
            super();
            this.user = user;
            this.ctx = ctx;
            this.allowedContentTypes = allowedContentTypes;
        }

        @Override
        public Permission getOwnPermission(final Folder folder) throws OXException {
            return CalculatePermission.calculate(folder, user, ctx, allowedContentTypes);
        }

    }

    /**
     * Performs the <code>PATH</code> request.
     *
     * @param treeId The tree identifier
     * @param folderId The folder identifier
     * @param all <code>true</code> to get all subfolders regardless of their subscription status; otherwise <code>false</code> to only get
     *            subscribed ones
     * @return The user-sensitive folders describing the path to root folder
     * @throws OXException If a folder error occurs
     */
    public UserizedFolder[] doPath(final String treeId, final String folderId, final boolean all) throws OXException {
        if (FolderStorage.ROOT_ID.equals(folderId)) {
            return new UserizedFolder[0];
        }
        final FolderStorage folderStorage = folderStorageDiscoverer.getFolderStorage(treeId, folderId);
        if (null == folderStorage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, folderId);
        }
        final java.util.List<FolderStorage> openedStorages = new ArrayList<FolderStorage>(4);
        if (folderStorage.startTransaction(storageParameters, false)) {
            openedStorages.add(folderStorage);
        }
        final UserizedFolder[] ret;
        try {
            Folder folder = folderStorage.getFolder(treeId, folderId, storageParameters);
            /*
             * Check folder permission for parent folder
             */
            final PermissionProvider permissionProvider;
            if (null == session) {
                permissionProvider = new UserCtxPermissionProvider(user, context, getAllowedContentTypes());
            } else {
                permissionProvider = new SessionPermissionProvider(session, getAllowedContentTypes());
            }

            Permission ownPermission = permissionProvider.getOwnPermission(folder);
            if (!ownPermission.isVisible()) {
                throw FolderExceptionErrorMessage.FOLDER_NOT_VISIBLE.create(getFolderInfo4Error(folder), getUserInfo4Error(), getContextInfo4Error());
            }

            final List<UserizedFolder> path = new ArrayList<UserizedFolder>(8);
            UserizedFolder userizedFolder = getUserizedFolder(folder, ownPermission, treeId, all, true, storageParameters, openedStorages);
            path.add(userizedFolder);
            for (int max = MAX_PATH_DEPTH; max-- > 0 && !FolderStorage.ROOT_ID.equals(userizedFolder.getParentID()) && null != userizedFolder.getParentID();) {
                if (max == 0) {
                    throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create("Exceeded max. allowed path depth of " + MAX_PATH_DEPTH);
                }

                FolderStorage fs = getOpenedStorage(userizedFolder.getParentID(), treeId, storageParameters, openedStorages);
                folder = fs.getFolder(treeId, userizedFolder.getParentID(), storageParameters);
                ownPermission = permissionProvider.getOwnPermission(folder);
                if (!ownPermission.isVisible()) {
                    throw FolderExceptionErrorMessage.FOLDER_NOT_VISIBLE.create(getFolderInfo4Error(folder), getUserInfo4Error(), getContextInfo4Error());
                }
                userizedFolder = getUserizedFolder(folder, ownPermission, treeId, all, true, storageParameters, openedStorages);
                if (userizedFolder.getID().equals(userizedFolder.getParentID())) {
                    // Folder references itself as parent
                    throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create("Folder \"" + userizedFolder.getID() + "\" references itself as parent.");
                }
                path.add(userizedFolder);
            }

            ret = path.toArray(new UserizedFolder[path.size()]);

            for (final FolderStorage fs : openedStorages) {
                fs.commitTransaction(storageParameters);
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
        return ret;
    }

    /**
     * Performs the <code>PATH</code> request.
     *
     * @param treeId The tree identifier
     * @param folderId The folder identifier
     * @return The user-sensitive folders describing the path to root folder
     * @throws OXException If a folder error occurs
     */
    public String[] doForcePath(final String treeId, final String folderId) throws OXException {
        if (FolderStorage.ROOT_ID.equals(folderId)) {
            return new String[0];
        }
        final FolderStorage folderStorage = folderStorageDiscoverer.getFolderStorage(treeId, folderId);
        if (null == folderStorage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, folderId);
        }
        final java.util.List<FolderStorage> openedStorages = new ArrayList<FolderStorage>(4);
        if (folderStorage.startTransaction(storageParameters, false)) {
            openedStorages.add(folderStorage);
        }
        final String[] ret;
        try {
            Folder folder = folderStorage.getFolder(treeId, folderId, storageParameters);

            final List<String> path = new ArrayList<String>(8);
            path.add(folderId);
            while (!FolderStorage.ROOT_ID.equals(folder.getParentID())) {
                final FolderStorage fs = getOpenedStorage(folder.getParentID(), treeId, storageParameters, openedStorages);
                folder = fs.getFolder(treeId, folder.getParentID(), storageParameters);
                path.add(folder.getID());
            }

            ret = path.toArray(new String[path.size()]);

            for (final FolderStorage fs : openedStorages) {
                fs.commitTransaction(storageParameters);
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
        return ret;
    }

}

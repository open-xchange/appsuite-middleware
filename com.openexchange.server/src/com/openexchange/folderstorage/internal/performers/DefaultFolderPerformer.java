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
import com.openexchange.folderstorage.Type;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.internal.Tools;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.User;

/**
 * {@link DefaultFolderPerformer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class DefaultFolderPerformer extends AbstractUserizedFolderPerformer {

    /**
     * Initializes a new {@link DefaultFolderPerformer}.
     *
     * @param session The session
     * @param decorator The optional folder service decorator
     * @throws OXException If passed session is invalid
     */
    public DefaultFolderPerformer(ServerSession session, FolderServiceDecorator decorator) throws OXException {
        super(session, decorator);
    }

    /**
     * Initializes a new {@link DefaultFolderPerformer}.
     *
     * @param user The user
     * @param context The context
     * @param decorator The optional folder service decorator
     */
    public DefaultFolderPerformer(User user, Context context, FolderServiceDecorator decorator) {
        super(user, context, decorator);
    }

    /**
     * Initializes a new {@link DefaultFolderPerformer}.
     *
     * @param session The session
     * @param decorator The optional folder service decorator
     * @param folderStorageDiscoverer The folder storage discoverer
     * @throws OXException If passed session is invalid
     */
    public DefaultFolderPerformer(ServerSession session, FolderServiceDecorator decorator, FolderStorageDiscoverer folderStorageDiscoverer) throws OXException {
        super(session, decorator, folderStorageDiscoverer);
    }

    /**
     * Initializes a new {@link DefaultFolderPerformer}.
     *
     * @param user The user
     * @param context The context
     * @param decorator The optional folder service decorator
     * @param folderStorageDiscoverer The folder storage discoverer
     */
    public DefaultFolderPerformer(User user, Context context, FolderServiceDecorator decorator, FolderStorageDiscoverer folderStorageDiscoverer) {
        super(user, context, decorator, folderStorageDiscoverer);
    }

    /**
     * Gets this storage's default folder identifier for specified user for given content type.
     *
     * @param user The user whose default folder shall be returned
     * @param treeId The tree identifier
     * @param contentType The content type or the default folder
     * @param type The folder type
     * @return The default folder identifier for specified user for given content type
     */
    public UserizedFolder doGet(User user, String treeId, ContentType contentType, Type type) throws OXException {
        /*
         * prefer the default folder from config tree if possible
         */
        ServerSession session = getSession();
        if (null != session && session.getUserId() == user.getId()) {
            String folderId = Tools.getConfiguredDefaultFolder(session, contentType, type);
            if (null != folderId) {
                return new GetPerformer(session, getDecorator()).doGet(treeId, folderId);
            }
        }
        /*
         * get default folder from storage, otherwise
         */
        FolderStorage folderStorage = folderStorageDiscoverer.getFolderStorageByContentType(treeId, contentType);
        if (null == folderStorage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_CT.create(treeId, String.valueOf(contentType));
        }
        List<FolderStorage> openedStorages = new ArrayList<FolderStorage>(4);
        if (folderStorage.startTransaction(storageParameters, false)) {
            openedStorages.add(folderStorage);
        }
        boolean committed = false;
        try {
            String folderId = folderStorage.getDefaultFolderID(user, treeId, contentType, type, storageParameters);
            Folder folder = folderStorage.getFolder(treeId, folderId, storageParameters);
            /*
             * Check folder permission for folder
             */
            Permission ownPermission;
            if (null == session) {
                ownPermission = CalculatePermission.calculate(folder, getUser(), getContext(), getAllowedContentTypes());
            } else {
                ownPermission = CalculatePermission.calculate(folder, session, getAllowedContentTypes());
            }
            if (false == ownPermission.isVisible()) {
                throw FolderExceptionErrorMessage.FOLDER_NOT_VISIBLE.create(getFolderInfo4Error(folder), getUserInfo4Error(), getContextInfo4Error());
            }
            UserizedFolder userizedFolder = getUserizedFolder(folder, ownPermission, treeId, true, true, storageParameters, openedStorages);
            /*
             * Commit
             */
            for (FolderStorage openedStorage : openedStorages) {
                openedStorage.commitTransaction(storageParameters);
            }
            committed = true;
            return userizedFolder;
        } catch (Exception e) {
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (false == committed) {
                for (FolderStorage openedStorage : openedStorages) {
                    openedStorage.rollback(storageParameters);
                }
            }
        }
    }

}

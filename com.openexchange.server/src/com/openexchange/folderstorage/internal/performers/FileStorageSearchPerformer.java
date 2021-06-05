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
import java.util.Iterator;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.CalculatePermission;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.SearchableFileFolderNameFolderStorage;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link FileStorageSearchPerformer}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.5
 */
public class FileStorageSearchPerformer extends AbstractUserizedFolderPerformer {

    public FileStorageSearchPerformer(ServerSession session, FolderServiceDecorator decorator) throws OXException {
        super(session, decorator);
    }

    /**
     * Searches a folder below given folder identifier by folder name
     *
     * @param treeId The tree identifier
     * @param folderId The 'root' folder for search operation
     * @param query The query to search
     * @param date Time stamp to limit search result to folders that are newer
     * @param includeSubfolders Include all sub-folders below given folder identifier
     * @param all <code>true</code> to deliver all sub-folders regardless of their subscribed status; <code>false</code> to deliver
     *            subscribed folders only
     * @param start A start index (inclusive) for the search results. Useful for paging.
     * @param end An end index (exclusive) for the search results. Useful for paging.
     * @return {@link List} of {@link UserizedFolder} sorted by name
     * @throws OXException If search fails
     */
    public List<UserizedFolder> doSearch(String treeId, String folderId, String query, long date, boolean includeSubfolders, boolean all, int start, int end) throws OXException {
        FolderStorage folderStorage = folderStorageDiscoverer.getFolderStorage(treeId, folderId);
        if (null == folderStorage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, folderId);
        }
        if (!SearchableFileFolderNameFolderStorage.class.isInstance(folderStorage)) {
            throw FolderExceptionErrorMessage.NO_SEARCH_SUPPORT.create();
        }

        SearchableFileFolderNameFolderStorage searchableStorage = (SearchableFileFolderNameFolderStorage) folderStorage;
        List<FolderStorage> openedStorages = new ArrayList<FolderStorage>(4);
        if (searchableStorage.startTransaction(storageParameters, false)) {
            openedStorages.add(searchableStorage);
        }
        try {
            List<Folder> folders = searchableStorage.searchFileStorageFolders(treeId, folderId, query, date, includeSubfolders, start, end, storageParameters);
            List<UserizedFolder> result = new ArrayList<UserizedFolder>(folders.size());
            for (Folder folder : folders) {
                Permission ownPermission;
                if (null == getSession()) {
                    ownPermission = CalculatePermission.calculate(folder, getUser(), getContext(), getAllowedContentTypes());
                } else {
                    ownPermission = CalculatePermission.calculate(folder, getSession(), getAllowedContentTypes());
                }
                if (ownPermission.isVisible() && (all || folder.isSubscribed())) {
                    result.add(getUserizedFolder(folder, ownPermission, treeId, true, true, storageParameters, openedStorages));
                }
            }
            for (Iterator<FolderStorage> it = openedStorages.iterator(); it.hasNext();) {
                FolderStorage fs = it.next();
                fs.commitTransaction(storageParameters);
                it.remove(); // Successfully committed
            }
            return result;
        } catch (RuntimeException e) {
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            for (final FolderStorage fs : openedStorages) {
                fs.rollback(storageParameters);
            }
        }
    }

}

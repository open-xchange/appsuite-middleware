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
 *    trademarks of the OX Software GmbH. group of companies.
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
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.CalculatePermission;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.SearchableFolderNameFolderStorage;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link SearchPerformer}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.5
 */
public class SearchPerformer extends AbstractUserizedFolderPerformer {

    public SearchPerformer(ServerSession session, FolderServiceDecorator decorator) throws OXException {
        super(session, decorator);
    }

    /**
     * Searches a folder below given folder identifier by folder name
     *
     * @param treeId The tree identifier
     * @param folderId The 'root' folder for search operation
     * @param module The module identifier
     * @param query The query to search
     * @param date Timestamp to limit search result to folders that are newer
     * @param includeSubfolders Include all subfolders below given folder identifier
     * @param all <code>true</code> to deliver all subfolders regardless of their subscribed status; <code>false</code> to deliver
     *            subscribed folders only
     * @param start A start index (inclusive) for the search results. Useful for paging.
     * @param end An end index (exclusive) for the search results. Useful for paging.
     * @return {@link List} of {@link UserizedFolder} sorted by name
     * @throws OXException If search fails
     */
    public List<UserizedFolder> doSearch(String treeId, String folderId, ContentType module, String query, long date, boolean includeSubfolders, boolean all, int start, int end) throws OXException {
        FolderStorage folderStorage = folderStorageDiscoverer.getFolderStorage(treeId, folderId);
        if (null == folderStorage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, folderId);
        }
        if (!SearchableFolderNameFolderStorage.class.isInstance(folderStorage)) {
            throw FolderExceptionErrorMessage.NO_SEARCH_SUPPORT.create();
        }
        SearchableFolderNameFolderStorage searchableStorage = (SearchableFolderNameFolderStorage) folderStorage;
        List<FolderStorage> openedStorages = new ArrayList<FolderStorage>(4);
        if (searchableStorage.startTransaction(storageParameters, false)) {
            openedStorages.add(searchableStorage);
        }
        try {
            List<Folder> folders = searchableStorage.search(treeId, folderId, module, query, date, includeSubfolders, start, end, storageParameters);
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
            for (final FolderStorage fs : openedStorages) {
                fs.commitTransaction(storageParameters);
            }
            return result;
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

}

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

package com.openexchange.file.storage.infostore.internal;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFileStorageFolder;
import com.openexchange.file.storage.FileStorageCaseInsensitiveAccess;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.FileStorageRestoringFolderAccess;
import com.openexchange.file.storage.FolderStatsAware;
import com.openexchange.file.storage.MediaFolderAwareFolderAccess;
import com.openexchange.file.storage.infostore.folder.AbstractInfostoreFolderAccess;
import com.openexchange.file.storage.PermissionAware;
import com.openexchange.file.storage.SearchableFolderNameFolderAccess;
import com.openexchange.file.storage.infostore.folder.FolderWriter;
import com.openexchange.file.storage.infostore.osgi.Services;
import com.openexchange.folderstorage.FolderResponse;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.folderstorage.RestoringFolderService;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.database.contentType.InfostoreContentType;
import com.openexchange.folderstorage.type.DocumentsType;
import com.openexchange.folderstorage.type.MusicType;
import com.openexchange.folderstorage.type.PicturesType;
import com.openexchange.folderstorage.type.TemplatesType;
import com.openexchange.folderstorage.type.VideosType;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link InfostoreFolderAccess}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class InfostoreFolderAccess extends AbstractInfostoreFolderAccess implements FileStorageFolderAccess, MediaFolderAwareFolderAccess, PermissionAware, FolderStatsAware, FileStorageCaseInsensitiveAccess, FileStorageRestoringFolderAccess, SearchableFolderNameFolderAccess {

    private final InfostoreFacade infostore;

    /**
     * Initializes a new {@link InfostoreFolderAccess}.
     *
     * @param session The session
     * @param infostore A reference to the underlying infostore facade
     */
    public InfostoreFolderAccess(ServerSession session, InfostoreFacade infostore) {
        super(session);
        this.infostore = infostore;
    }

    @Override
    public Map<String, FileStorageFolder[]> restoreFolderFromTrash(List<String> folderIds, String defaultDestFolderId) throws OXException {
        FolderService folderService = getFolderService();
        if (false == (folderService instanceof RestoringFolderService)) {
            throw FileStorageExceptionCodes.NO_RESTORE_SUPPORT.create();
        }

        FolderServiceDecorator decorator = initDecorator();
        UserizedFolder destFolder = folderService.getFolder(TREE_ID, defaultDestFolderId, session, decorator);
        FolderResponse<Map<String, List<UserizedFolder>>> result = ((RestoringFolderService) folderService).restoreFolderFromTrash(TREE_ID, folderIds, destFolder, session, decorator);
        Map<String, List<UserizedFolder>> map = result.getResponse();

        Map<String, FileStorageFolder[]> retval = new LinkedHashMap<String, FileStorageFolder[]>(map.size());
        for (Map.Entry<String, List<UserizedFolder>> entry : map.entrySet()) {
            List<UserizedFolder> path = entry.getValue();
            FileStorageFolder[] folders = new FileStorageFolder[path.size()];
            int i = 0;
            for (UserizedFolder userizedFolder : path) {
                folders[i++] = getConverter().getStorageFolder(userizedFolder);
            }
            retval.put(entry.getKey(), folders);
        }
        return retval;
    }

    @Override
    public DefaultFileStorageFolder getPicturesFolder() throws OXException {
        return getDefaultFolder(PicturesType.getInstance());
    }

    @Override
    public DefaultFileStorageFolder getDocumentsFolder() throws OXException {
        return getDefaultFolder(DocumentsType.getInstance());
    }

    @Override
    public DefaultFileStorageFolder getTemplatesFolder() throws OXException {
        return getDefaultFolder(TemplatesType.getInstance());
    }

    @Override
    public DefaultFileStorageFolder getMusicFolder() throws OXException {
        return getDefaultFolder(MusicType.getInstance());
    }

    @Override
    public DefaultFileStorageFolder getVideosFolder() throws OXException {
        return getDefaultFolder(VideosType.getInstance());
    }

    @Override
    protected FolderService getFolderService() throws OXException {
        FolderService folderService = Services.getService(FolderService.class);
        if (null == folderService) {
            throw ServiceExceptionCode.absentService(FolderService.class);
        }
        return folderService;
    }

    @Override
    protected InfostoreFacade getInfostore() throws OXException {
        return infostore;
    }

    @Override
    public FileStorageFolder[] searchFolderByName(String query, String folderId, long date, boolean includeSubfolders, boolean all, int start, int end) throws OXException {
        FolderService folderService = getFolderService();
        FolderServiceDecorator decorator = initDecorator();
        FolderResponse<List<UserizedFolder>> result = folderService.searchFolderByName(TREE_ID, folderId, InfostoreContentType.getInstance(), query, date, includeSubfolders, all, start, end, session, decorator);
        List<UserizedFolder> userizedFolders = result.getResponse();
        return FolderWriter.writeFolders(userizedFolders.toArray(new UserizedFolder[userizedFolders.size()]));
    }

}

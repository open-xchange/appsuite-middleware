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

package com.openexchange.file.storage.dropbox.access;

import java.util.ArrayList;
import java.util.List;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.DeletedMetadata;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.GetMetadataErrorException;
import com.dropbox.core.v2.files.ListFolderErrorException;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.dropbox.DropboxConstants;
import com.openexchange.oauth.access.AbstractOAuthAccess;
import com.openexchange.session.Session;

/**
 * {@link AbstractDropboxAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
abstract class AbstractDropboxAccess {

    protected final AbstractOAuthAccess dropboxOAuthAccess;
    protected final Session session;
    protected final FileStorageAccount account;
    protected final DbxClientV2 client;

    /**
     * Initialises a new {@link AbstractDropboxAccess}.
     *
     * @throws OXException
     */
    AbstractDropboxAccess(AbstractOAuthAccess dropboxOAuthAccess, FileStorageAccount account, Session session) throws OXException {
        super();
        this.dropboxOAuthAccess = dropboxOAuthAccess;
        this.session = session;
        this.account = account;
        this.client = ((DropboxClient) dropboxOAuthAccess.getClient().client).dbxClient;
    }

    /**
     * Determines whether the specified folder identifier denotes the root folder
     *
     * @param folderId The folder identifier
     * @return true if the specified folder identifier denotes a root folder; false otherwise
     */
    boolean isRoot(String folderId) {
        folderId = folderId == null ? "" : folderId;
        return FileStorageFolder.ROOT_FULLNAME.equals(folderId) || "/".equals(folderId);
    }

    /**
     * Retrieves the parent folder path from the specified full path.
     *
     * @param path The path
     * @return The parent folder path
     */
    String getParent(String path) {
        String workingPath = path;
        // If the path ends with a '/' trim it
        if (path.endsWith("/")) {
            workingPath = path.substring(0, path.length() - 1);
        }
        int index = workingPath.lastIndexOf('/');
        return workingPath.substring(0, index);
    }

    /**
     * Gets the (dropbox-)path for specified folder identifier.
     *
     * @param folderId The folder identifier
     * @return The associated path
     */
    String toPath(String folderId) {
        return isRoot(folderId) ? "/" : folderId;
    }

    /**
     * Gets the (dropbox-)path for the specified folder- and file-identifier.
     *
     * @param folderId The folder identifier
     * @param fileId The file identifier
     * @return The associated path
     */
    String toPath(String folderId, String fileId) {
        String parentPath = isRoot(folderId) ? "/" : folderId;
        return parentPath.endsWith("/") ? parentPath + fileId : parentPath + '/' + fileId;
    }

    /**
     * Returns the metadata for a file ({@link FileMetadata}) or a folder ({@link FolderMetadata})
     *
     * @param id The resource identifier
     * @return The metadata of the file or folder
     * @throws OXException If the resource is not found
     * @throws GetMetadataErrorException If a metadata error is occurred
     * @throws DbxException if a generic Dropbox error is occurred
     */
    Metadata getMetadata(String id) throws OXException, GetMetadataErrorException, DbxException {
        Metadata metadata = client.files().getMetadata(id);
        if (metadata instanceof DeletedMetadata) {
            throw FileStorageExceptionCodes.NOT_FOUND.create(DropboxConstants.ID, id);
        }
        return metadata;
    }

    /**
     * Returns the {@link FileMetadata} of the specified file
     *
     * @param folderId The folder identifier
     * @param fileId The file identifier
     * @return The {@link FileMetadata}
     * @throws GetMetadataErrorException If a metadata error is occured
     * @throws OXException If the resource is not found
     * @throws DbxException if a generic Dropbox error is occured
     */
    FileMetadata getFileMetadata(String folderId, String fileId) throws GetMetadataErrorException, OXException, DbxException {
        String path = toPath(folderId, fileId);
        Metadata metadata = getMetadata(path);
        if (metadata instanceof FileMetadata) {
            return (FileMetadata) metadata;
        }
        throw FileStorageExceptionCodes.NOT_A_FILE.create(DropboxConstants.ID, path);
    }

    /**
     * Gets the {@link FolderMetadata} of the specified folder.
     *
     * @param folderId The folder identifier
     * @return The {@link FolderMetadata}
     * @throws GetMetadataErrorException If a metadata error is occurred
     * @throws DbxException If a generic Dropbox error is occurred
     * @throws OXException if the specified identifier does not denote a folder
     */
    FolderMetadata getFolderMetadata(String folderId) throws GetMetadataErrorException, DbxException, OXException {
        Metadata metadata = getMetadata(folderId);
        if (metadata instanceof FolderMetadata) {
            return (FolderMetadata) metadata;
        }
        throw FileStorageExceptionCodes.NOT_A_FOLDER.create(DropboxConstants.ID, folderId);
    }

    /**
     * Lists the contents of the specified folder and Returns a {@link List} with {@link Metadata}
     *
     * @param folderId The folder identifier
     * @return A {@link List} with {@link Metadata}
     * @throws ListFolderErrorException if a list folder error is occurred
     * @throws DbxException if a generic Dropbox error is occurred
     */
    List<Metadata> listFolder(String folderId) throws ListFolderErrorException, DbxException {
        List<Metadata> results = new ArrayList<Metadata>();
        boolean hasMore = false;
        ListFolderResult result = client.files().listFolder(folderId);
        do {
            hasMore = result.getHasMore();
            results.addAll(result.getEntries());
            if (hasMore) {
                String cursor = result.getCursor();
                result = client.files().listFolderContinue(cursor);
            }
        } while (hasMore);

        return results;
    }

    /**
     * Lists the contents of the specified folder and Returns a {@link List} with {@link Metadata}
     *
     * @param folderId The folder identifier
     * @return A {@link List} with {@link Metadata}
     * @throws ListFolderErrorException if a list folder error is occurred
     * @throws DbxException if a generic Dropbox error is occurred
     */
    List<String> listSubfolderIds(String folderId) throws ListFolderErrorException, DbxException {
        List<String> results = new ArrayList<String>();
        boolean hasMore = false;
        ListFolderResult result = client.files().listFolder(folderId);
        do {
            hasMore = result.getHasMore();
            for (Metadata m : result.getEntries()) {
                if (m instanceof FolderMetadata) {
                    results.add(m.getPathDisplay());
                }
            }

            if (hasMore) {
                String cursor = result.getCursor();
                result = client.files().listFolderContinue(cursor);
            }
        } while (hasMore);

        return results;
    }
}

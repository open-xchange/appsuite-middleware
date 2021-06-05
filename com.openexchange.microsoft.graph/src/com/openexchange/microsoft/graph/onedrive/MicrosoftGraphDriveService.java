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

package com.openexchange.microsoft.graph.onedrive;

import java.io.InputStream;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.Quota;

/**
 * {@link MicrosoftGraphDriveService}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public interface MicrosoftGraphDriveService {

    /**
     * Retrieves the identifier of the root folder
     * 
     * @param accessToken The oauth access token
     * @return the identifier of the root folder
     * @throws OXException if an error is occurred
     */
    String getRootFolderId(String accessToken) throws OXException;

    /**
     * Checks whether the folder with the specified identifier exists.
     * 
     * @param accessToken The oauth access token
     * @param folderId The folder identifier
     * @return <code>true</code> if the folder exists; <code>false</code> otherwise.
     * @throws OXException If an error is occurred
     */
    boolean existsFolder(String accessToken, String folderId) throws OXException;

    /**
     * Returns the root folder of the user's default drive account
     * 
     * @param userId The user identifier
     * @param accessToken The oauth access token
     * @return The root folder
     * @throws OXException If an error is occurred
     */
    OneDriveFolder getRootFolder(int userId, String accessToken) throws OXException;

    /**
     * Retrieves the folder with the specified identifier for the specified user
     * 
     * @param userId The user identifier
     * @param accessToken The oauth access token
     * @param folderId the folder identifier
     * @return The folder
     * @throws OXException if an error is occurred
     */
    OneDriveFolder getFolder(int userId, String accessToken, String folderId) throws OXException;

    /**
     * Returns all sub-folders of the specified folder
     * 
     * @param userId The user identifier
     * @param accessToken The oauth access token
     * @param folderId The folder identifier for which to retrieve all sub-folders
     * @return A {@link List} with all sub-folders
     * @throws OXException If an error is occurred
     */
    List<OneDriveFolder> getSubFolders(int userId, String accessToken, String folderId) throws OXException;

    /**
     * Creates the folder with the specified name under the parent with the specified id. If the <code>autorename</code>
     * flag is enabled, then if another folder with the same name under the same parent already exists, it will be
     * automatically renamed to something like 'Name (1)'. The autorename will happen on the remote end-point, i.e.
     * no auto-renaming in the middleware's premises.
     * 
     * @param userId The user identifier
     * @param accessToken the access token
     * @param folderName The folder's name
     * @param parentId The parent identifier
     * @param autorename whether an auto-rename will happen in case of a name clash
     * @return The created {@link OneDriveFolder}
     * @throws OXException if an error is occurred
     */
    OneDriveFolder createFolder(int userId, String accessToken, String folderName, String parentId, boolean autorename) throws OXException;

    /**
     * Deletes the folder with the specified identifier
     * 
     * @param accessToken the oauth access token
     * @param folderId the folder identifier
     * @throws OXException if an error is occurred
     */
    void deleteFolder(String accessToken, String folderId) throws OXException;

    /**
     * Empties and retains the folder
     * 
     * @param accessToken the oauth access token
     * @param folderId the folder identifier
     * @throws OXException if an error is occurred
     */
    void clearFolder(String accessToken, String folderId) throws OXException;

    /**
     * Renames the folder with the specified identifier
     * 
     * @param accessToken The access token
     * @param folderId the folder identifier
     * @param newName the new name for the folder
     * @throws OXException if an error is occurred
     */
    void renameFolder(String accessToken, String folderId, String newName) throws OXException;

    /**
     * Moves the folder with the specified identifier under the parent with the specified identifier. In case
     * of a name clashing no auto-rename will happen and an exception will be thrown.
     * 
     * @param accessToken The oauth access token
     * @param folderId The identifier of the folder to move
     * @param parentId The identifier of the new parent for the moved folder
     * @return The new folder identifier
     * @throws OXException if an error is occurred
     */
    String moveFolder(String accessToken, String folderId, String parentId) throws OXException;

    /**
     * Moves and renames the folder with the specified identifier under the parent with the specified identifier. In case
     * of a name clashing no auto-rename will happen and an exception will be thrown.
     * 
     * @param accessToken The oauth access token
     * @param folderId The identifier of the folder to move
     * @param parentId The identifier of the new parent for the moved folder
     * @param newName The new name of the folder
     * @return The new folder identifier
     * @throws OXException if an error is occurred
     */
    String moveFolder(String accessToken, String folderId, String parentId, String newName) throws OXException;

    /**
     * Deletes the file with the specified identifier
     * 
     * @param accessToken the oauth access token
     * @param fileId the file identifier
     * @throws OXException if an error is occurred
     */
    void deleteFile(String accessToken, String fileId) throws OXException;

    /**
     * Returns all files with in the specified folder
     * 
     * @param userId The user identifier
     * @param accessToken The oauth access token
     * @param folderId The folder identifier for which to retrieve all sub-folders
     * @return A {@link List} with all files in the specified folder
     * @throws OXException If an error is occurred
     */
    List<OneDriveFile> getFiles(int userId, String accessToken, String folderId) throws OXException;

    /**
     * Retrieves the file with the specified identifier
     * 
     * @param userId The user identifier
     * @param accessToken the oauth access token
     * @param itemId The file's identifier
     * @return The retrieved {@link OneDriveFile}
     * @throws OXException if an error is occurred
     */
    OneDriveFile getFile(int userId, String accessToken, String itemId) throws OXException;

    /**
     * Retrieves a {@link List} of files with the specified identifiers
     * 
     * @param userId The user identifier
     * @param accessToken the oauth access token
     * @param itemIds The files' identifiers
     * @return The {@link List} with the files, or an empty list if no files were found.
     * @throws OXException if an error is occurred
     */
    List<OneDriveFile> getFiles(int userId, String accessToken, List<String> itemIds) throws OXException;

    /**
     * Retrieves the contents of the file with the specified identifier
     * 
     * @param accessToken the oauth access token
     * @param fileId The file's identifier
     * @return The {@link InputStream} with the data
     * @throws OXException if an error is occurred
     */
    InputStream getFile(String accessToken, String fileId) throws OXException;

    /**
     * Moves the file with the specified identifier under the parent with the specified identifier
     * 
     * @param accessToken The oauth access token
     * @param fileId The file identifier to move
     * @param parentId The new parent
     * @return the new file identifier
     * @throws OXException if an error is occurred
     */
    String moveFile(String accessToken, String fileId, String parentId) throws OXException;

    /**
     * Updates the metadata of the specified {@link File} and optionally moves it under the parent
     * with the specified identifier
     * 
     * @param accessToken The oauth access token
     * @param file The file metadata
     * @param modifiedFields The modified fields
     * @param parentId The optional new parent identifier
     * @return The new file identifier
     * @throws OXException if an error is occurred
     */
    String updateFile(String accessToken, File file, List<Field> modifiedFields, String parentId) throws OXException;

    /**
     * Copy the file with the specified identifier under the parent with the specified identifier
     * 
     * @param accessToken The oauth access token
     * @param itemId the identifier
     * @param parentId the new parent identifier
     * @return the new file identifier
     * @throws OXException if an error is occurred
     */
    String copyFile(String accessToken, String itemId, String parentId) throws OXException;

    /**
     * Copy the file and update the metadata
     * 
     * @param accessToken The oauth access token
     * @param itemId the identifier
     * @param file The file metadata
     * @param modifiedFields The modified fields
     * @param parentId the new parent identifier
     * @return the new file identifier
     * @throws OXException if an error is occurred
     */
    String copyFile(String accessToken, String itemId, File file, List<Field> modifiedFields, String parentId) throws OXException;

    /**
     * Retrieves a thumbnail for the item with the specified identifier
     * 
     * @param accessToken the oauth access token
     * @param itemId the identifier of the item for which to retrieve the thumbnail
     * @return The stream with the thumbnail
     * @throws OXException if an error is occurred
     */
    InputStream getThumbnail(String accessToken, String itemId) throws OXException;

    /**
     * Retrieve the quota for the user's default OneDrive
     * 
     * @param accessToken The oauth access token
     * @return The {@link Quota}
     * @throws OXException if an error is occurred
     */
    Quota getQuota(String accessToken) throws OXException;

    /**
     * Performs a search with the specified <code>query</code> in the folder with the specified identifier. If the flag <code>includeSubfolders</code>
     * is enabled, then the search is performed on the subfolders of the specified folder.
     * 
     * @param userId The user identifer
     * @param accessToken the oauth access token
     * @param query The search query
     * @param folderId The folder identifier in which to perform the search
     * @param includeSubfolders Whether to consider sub-folders, i.e. go recursive
     * @return A {@link List} with the found files
     * @throws OXException if an error is occurred
     */
    List<OneDriveFile> searchFiles(int userId, String accessToken, String query, String folderId, boolean includeSubfolders) throws OXException;

    /**
     * Uploads the file
     * 
     * @param accessToken The oauth access token
     * @param file The file's metadata
     * @param inputStream The actual data to upload
     * @return the new file identifier
     * @throws OXException if an error is occurred
     */
    String upload(String accessToken, File file, InputStream inputStream) throws OXException;
}

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

package com.openexchange.folderstorage;

import java.util.Date;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.user.User;

/**
 * {@link FolderStorage} - A folder storage bound to a certain folder source (e.g database, email, etc).
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface FolderStorage {

    /**
     * The reserved tree identifier for real folder tree: <code>"0"</code>.
     */
    public static final String REAL_TREE_ID = "0".intern();

    /**
     * The reserved tree identifier for all folder trees: <code>"*"</code>.
     */
    public static final String ALL_TREE_ID = "*".intern();

    /**
     * The reserved identifier for root folder: <code>"0"</code>.
     */
    public static final String ROOT_ID = "0".intern();

    /**
     * The reserved identifier for private folder: <code>"1"</code>.
     */
    public static final String PRIVATE_ID = "1".intern();

    /**
     * The reserved identifier for public folder: <code>"2"</code>.
     */
    public static final String PUBLIC_ID = "2".intern();

    /**
     * The reserved identifier for shared folder: <code>"3"</code>.
     */
    public static final String SHARED_ID = "3".intern();

    /**
     * The reserved identifier for global address book folder: <code>"6"</code>.
     */
    public static final String GLOBAL_ADDRESS_BOOK_ID = "6".intern();

    /**
     * Clears the cache with respect to specified user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    void clearCache(int userId, int contextId);

    /**
     * Gets the content types supported by this folder storage.
     * <p>
     * A zero length array means this folder storage supports all content types for a certain tree identifier.
     *
     * @return The content types supported by this folder storage or a zero length array to indicate no content type limitations
     */
    ContentType[] getSupportedContentTypes();

    /**
     * Gets the storage's folder type.
     *
     * @return The storage's folder type
     */
    FolderType getFolderType();

    /**
     * Gets the storage's priority.
     *
     * @return The storage's priority
     */
    StoragePriority getStoragePriority();

    /**
     * Gets the default content type for this folder storage.
     *
     * @return The default content type or <code>null</code> if this folder storage has no default content type
     */
    ContentType getDefaultContentType();

    /**
     * Checks if denoted folder is empty.
     *
     * @param treeId The tree identifier
     * @param folderId The folder identifier
     * @param storageParameters The storage parameters
     * @return <code>true</code> if denoted folder is empty; otherwise <code>false</code>
     * @throws OXException If checking folder content fails
     */
    boolean isEmpty(String treeId, String folderId, StorageParameters storageParameters) throws OXException;

    /**
     * Checks if denoted folder contains user-foreign objects.
     *
     * @param user The user whose default folder shall be returned
     * @param treeId The tree identifier
     * @param folderId The folder identifier
     * @param storageParameters The storage parameters
     * @return <code>true</code> if folder contains user-foreign objects; otherwise <code>false</code>
     * @throws OXException
     */
    boolean containsForeignObjects(User user, String treeId, String folderId, StorageParameters storageParameters) throws OXException;

    /**
     * Checks if the folder denoted by specified folder ID exists in this folder storage.
     *
     * @param treeId The tree identifier
     * @param folderId The folder identifier
     * @param storageParameters The storage parameters
     * @return <code>true</code> if the folder denoted by specified folder ID exists in this folder storage; otherwise <code>false</code>
     * @throws OXException If the folder existence cannot be checked
     */
    boolean containsFolder(String treeId, String folderId, StorageParameters storageParameters) throws OXException;

    /**
     * Checks if the folder denoted by specified folder ID exists in this folder storage.
     *
     * @param treeId The tree identifier
     * @param folderId The folder identifier
     * @param storageType The storage type from which to get the folder
     * @param storageParameters The storage parameters
     * @return <code>true</code> if the folder denoted by specified folder ID exists in this folder storage; otherwise <code>false</code>
     * @throws OXException If the folder existence cannot be checked
     */
    boolean containsFolder(String treeId, String folderId, StorageType storageType, StorageParameters storageParameters) throws OXException;

    /**
     * Checks consistency of the tree.
     *
     * @param treeId The tree identifier
     * @param storageParameters The storage parameters
     * @throws OXException If consistency check fails
     */
    void checkConsistency(String treeId, StorageParameters storageParameters) throws OXException;

    /**
     * Restores the folder with given folder identifier
     *
     * @param treeId The tree identifier
     * @param folderId The folder identifier
     * @param storageParameters The storage parameters
     * @throws OXException If restore fails
     */
    void restore(String treeId, String folderId, StorageParameters storageParameters) throws OXException;

    /**
     * Prepares specified folder with user-sensitive informations if needed.
     *
     * @param treeId The tree identifier
     * @param folder The folder identifier
     * @param storageParameters The storage parameters
     * @return The prepared or unchanged folder
     * @throws OXException If preparation fails
     */
    Folder prepareFolder(final String treeId, final Folder folder, final StorageParameters storageParameters ) throws OXException;

    /**
     * Gets the folder denoted by specified folder ID.
     *
     * @param treeId The tree identifier
     * @param folderId The folder identifier
     * @param storageParameters The storage parameters
     * @return The folder
     * @throws OXException If the folder cannot be returned
     */
    Folder getFolder(String treeId, String folderId, StorageParameters storageParameters) throws OXException;

    /**
     * Gets the folders denoted by specified folder IDs.
     *
     * @param treeId The tree identifier
     * @param folderIds The folder identifiers
     * @param storageParameters The storage parameters
     * @return The folder
     * @throws OXException If the folder cannot be returned
     */
    List<Folder> getFolders(String treeId, List<String> folderIds, StorageParameters storageParameters) throws OXException;

    /**
     * Gets the folder denoted by specified folder ID.
     *
     * @param treeId The tree identifier
     * @param folderId The folder identifier
     * @param storageType The storage type from which to get the folder
     * @param storageParameters The storage parameters
     * @return The folder
     * @throws OXException If the folder cannot be returned
     */
    Folder getFolder(String treeId, String folderId, StorageType storageType, StorageParameters storageParameters) throws OXException;

    /**
     * Gets the folders denoted by specified folder IDs.
     *
     * @param treeId The tree identifier
     * @param folderIds The folder identifiers
     * @param storageType The storage type from which to get the folder
     * @param storageParameters The storage parameters
     * @return The folder
     * @throws OXException If the folder cannot be returned
     */
    List<Folder> getFolders(String treeId, List<String> folderIds, StorageType storageType, StorageParameters storageParameters) throws OXException;

    /**
     * Gets this storage's default folder identifier for specified user for given content type.
     *
     * @param user The user whose default folder shall be returned
     * @param treeId The tree identifier
     * @param contentType The content type or the default folder
     * @param type The folder type
     * @param storageParameters The storage parameters
     * @return The default folder identifier for specified user for given content type
     * @throws OXException If the default folder cannot be returned
     */
    String getDefaultFolderID(User user, String treeId, ContentType contentType, Type type, StorageParameters storageParameters) throws OXException;

    /**
     * Gets the type as indicated by specified parent.
     *
     * @param user The user
     * @param treeId The tree identifier
     * @param parentId The parent identifier
     * @param storageParameters The storage parameters
     * @return The type as indicated by specified parent
     * @throws OXException Id determining the type fails
     */
    Type getTypeByParent(User user, String treeId, String parentId, StorageParameters storageParameters) throws OXException;

    /**
     * Deletes the folder denoted by specified folder ID.
     * <p>
     * A {@link OXException} is thrown if denoted folder contains subfolders.
     *
     * @param treeId The tree identifier
     * @param folderId The folder ID
     * @param storageParameters The storage parameters
     * @throws OXException If deletion fails
     */
    void deleteFolder(String treeId, String folderId, StorageParameters storageParameters) throws OXException;

    /**
     * Clears the content of the folder denoted by specified folder ID.
     *
     * @param treeId The tree identifier
     * @param folderId The folder ID
     * @param storageParameters The storage parameters
     * @throws OXException If deletion fails
     */
    void clearFolder(String treeId, String folderId, StorageParameters storageParameters) throws OXException;

    /**
     * Updates the data of the given folder on the storage.
     *
     * @param folder object containing new folder data.
     * @param storageParameters The storage parameters
     * @throws OXException If changing the folder data fails.
     */
    void updateFolder(Folder folder, StorageParameters storageParameters) throws OXException;

    /**
     * Updates the last-modified time stamp of the given folder in the storage.
     *
     * @param lastModified The last-modified time stamp to set
     * @param treeId The tree identifier
     * @param folderId The folder ID
     * @param storageParameters The storage parameters
     * @throws OXException If updating last-modified time stamp fails
     */
    void updateLastModified(long lastModified, String treeId, String folderId, StorageParameters storageParameters) throws OXException;

    /**
     * Creates the given folder on the storage.
     * <p>
     * Implementation is expected to set the identifier of created folder in passed folder instance through {@link Folder#setID(String)}.
     *
     * @param folder The object containing the new folder data.
     * @param storageParameters The storage parameters
     * @throws OXException If creating the folder fails.
     */
    void createFolder(Folder folder, StorageParameters storageParameters) throws OXException;

    /**
     * Gets the folder identifiers for specified content type and type.
     *
     * @param treeId The tree identifier
     * @param contentType The content type
     * @param type The type
     * @param storageParameters The storage parameters
     * @return The folder identifiers for specified content type and type
     * @throws OXException If returning the folder identifiers fails
     */
    SortableId[] getVisibleFolders(final String treeId, final ContentType contentType, final Type type, final StorageParameters storageParameters) throws OXException;

    /**
     * Gets the folder identifiers for specified content type and type.
     *
     * @param rootFolderId The root folder identifier
     * @param treeId The tree identifier
     * @param contentType The content type
     * @param type The type
     * @param storageParameters The storage parameters
     * @return The folder identifiers for specified content type and type
     * @throws OXException If returning the folder identifiers fails
     */
    SortableId[] getVisibleFolders(String rootFolderId, String treeId, final ContentType contentType, final Type type, final StorageParameters storageParameters) throws OXException;

    /**
     * Gets the identifiers of all folders that are considered as "shared" by the user, i.e. those folders of the user that have been
     * shared to at least one other entity.
     *
     * @param treeId The tree identifier
     * @param contentType The content type
     * @param storageParameters The storage parameters
     * @return The identifiers of the shared folders, or <code>null</code> if there are none
     * @throws OXException If returning the folder identifiers fails
     */
    SortableId[] getUserSharedFolders(String treeId, ContentType contentType, StorageParameters storageParameters) throws OXException;

    /**
     * Gets the subfolder identifiers for specified parent which are visible to storage parameter's entity.
     *
     * @param treeId The tree identifier
     * @param parentId The parent identifier
     * @param storageParameters The storage parameters
     * @return The subfolder identifiers for specified parent or an empty array if parent identifier cannot be served
     * @throws OXException If returning the subfolder identifiers fails
     */
    SortableId[] getSubfolders(String treeId, String parentId, StorageParameters storageParameters) throws OXException;

    /**
     * Gets the identifiers of all new and modified folders from this storage since given time stamp.
     * <p>
     * If a certain implementation does not support this feature an empty array is supposed to be returned.
     *
     * @param treeId The tree identifier
     * @param timeStamp The time stamp
     * @param includeContentTypes The content types to include
     * @param storageParameters The storage parameters
     * @return The identifiers of all new and modified folders from this storage since given time stamp
     * @throws OXException If a folder error occurs
     */
    String[] getModifiedFolderIDs(String treeId, Date timeStamp, ContentType[] includeContentTypes, StorageParameters storageParameters) throws OXException;

    /**
     * Gets the identifiers of all deleted folders from this storage since given time stamp.
     * <p>
     * If a certain implementation does not support this feature an empty array is supposed to be returned.
     *
     * @param treeId The tree identifier
     * @param timeStamp The time stamp
     * @param storageParameters The storage parameters
     * @return The identifiers of all deleted folders from this storage since given time stamp
     * @throws OXException If a folder error occurs
     */
    String[] getDeletedFolderIDs(String treeId, Date timeStamp, StorageParameters storageParameters) throws OXException;

    /**
     * Starts a transaction on folder storage.
     *
     * @param parameters The parameters
     * @param modify <code>true</code> if started transaction is supposed to modify storage's content; otherwise <code>false</code>
     * @return <code>true</code> if call started the transaction; otherwise <code>false</code> if transaction has already been started
     *         before
     * @throws OXException If storage parameters cannot be returned
     */
    boolean startTransaction(StorageParameters parameters, boolean modify) throws OXException;

    /**
     * Publishes made changes on the storage.
     *
     * @param params The storage parameters
     * @throws OXException If committing the made changes fails.
     */
    void commitTransaction(StorageParameters params) throws OXException;

    /**
     * Discards made changes on the storage. This method does not throw an exception because a rollback should only be used if already some
     * problem or exception occurred. Problems occurred while executing this method should only be logged.
     *
     * @param params The storage parameters.
     */
    void rollback(StorageParameters params);

}

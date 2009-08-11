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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.folderstorage;

import com.openexchange.groupware.ldap.User;

/**
 * {@link FolderStorage} - A folder storage bound to a certain folder source (e.g database, email, etc).
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface FolderStorage {

    /**
     * The reserved tree identifier for real folder tree.
     */
    public static final String REAL_TREE_ID = "0";

    /**
     * The reserved tree identifier for all folder trees.
     */
    public static final String ALL_TREE_ID = "*";

    /**
     * The reserved identifier for root folder.
     */
    public static final String ROOT_ID = "0";

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
     * Checks if the folder denoted by specified folder ID exists in this folder storage.
     * 
     * @param treeId The tree identifier
     * @param folderId The folder identifier
     * @param storageParameters The storage parameters
     * @return <code>true</code> if the folder denoted by specified folder ID exists in this folder storage; otherwise <code>false</code>
     * @throws FolderException If the folder existence cannot be checked
     */
    boolean containsFolder(String treeId, String folderId, StorageParameters storageParameters) throws FolderException;

    /**
     * Gets the folder denoted by specified folder ID.
     * 
     * @param treeId The tree identifier
     * @param folderId The folder identifier
     * @param storageParameters The storage parameters
     * @return The folder
     * @throws FolderException If the folder cannot be returned
     */
    Folder getFolder(String treeId, String folderId, StorageParameters storageParameters) throws FolderException;

    /**
     * Gets this storage's default folder identifier for specified user for given content type.
     * 
     * @param user The user whose default folder shall be returned
     * @param treeId The tree identifier
     * @param contentType The content type or the default folder
     * @param storageParameters The storage parameters
     * @return The default folder identifier for specified user for given content type
     * @throws FolderException If the default folder cannot be returned
     */
    String getDefaultFolderID(User user, String treeId, ContentType contentType, StorageParameters storageParameters) throws FolderException;

    /**
     * Deletes the folder denoted by specified folder ID.
     * <p>
     * A {@link FolderException} is thrown if denoted folder contains subfolders.
     * 
     * @param treeId The tree identifier
     * @param folderId The folder ID
     * @param storageParameters The storage parameters
     * @throws FolderException If deletion fails
     */
    void deleteFolder(String treeId, String folderId, StorageParameters storageParameters) throws FolderException;

    /**
     * Clears the content of the folder denoted by specified folder ID.
     * 
     * @param treeId The tree identifier
     * @param folderId The folder ID
     * @param storageParameters The storage parameters
     * @throws FolderException If deletion fails
     */
    void clearFolder(String treeId, String folderId, StorageParameters storageParameters) throws FolderException;

    /**
     * Updates the data of the given folder on the storage.
     * 
     * @param folder object containing new folder data.
     * @param storageParameters The storage parameters
     * @throws FolderException If changing the folder data fails.
     */
    void updateFolder(Folder folder, StorageParameters storageParameters) throws FolderException;

    /**
     * Creates the given folder on the storage.
     * <p>
     * Implementation is expected to set the identifier of created folder in passed folder instance through {@link Folder#setID(String)}.
     * 
     * @param folder The object containing the new folder data.
     * @param storageParameters The storage parameters
     * @throws FolderException If creating the folder fails.
     */
    void createFolder(Folder folder, StorageParameters storageParameters) throws FolderException;

    /**
     * Gets the subfolder identifiers for specified parent which are visible to storage parameter's entity.
     * 
     * @param treeId The tree identifier
     * @param parentId The parent identifier
     * @param storageParameters The storage parameters
     * @return The subfolder identifiers for specified parent or an empty array if parent identifier cannot be served
     * @throws FolderException If returning the subfolder identifiers fails
     */
    SortableId[] getSubfolders(String treeId, String parentId, StorageParameters storageParameters) throws FolderException;

    /**
     * Starts a transaction on folder storage.
     * 
     * @param parameters The parameters
     * @param modify <code>true</code> if started transaction is supposed to modify storage's content; otherwise <code>false</code>
     * @return The parameters with storage-specific extensions
     * @throws FolderException If storage parameters cannot be returned
     */
    StorageParameters startTransaction(StorageParameters parameters, boolean modify) throws FolderException;

    /**
     * Publishes made changes on the storage.
     * 
     * @param params The storage parameters.
     * @throws FolderException If committing the made changes fails.
     */
    void commitTransaction(StorageParameters params) throws FolderException;

    /**
     * Discards made changes on the storage. This method does not throw an exception because a rollback should only be used if already some
     * problem or exception occurred. Problems occurred while executing this method should only be logged.
     * 
     * @param params The storage parameters.
     */
    void rollback(StorageParameters params);

}

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

package com.openexchange.folder;

/**
 * {@link FolderStorage} - Provides access to a folder storage
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public interface FolderStorage<K> {

	/**
	 * Checks if the folder exists whose ID matches specified ID.
	 * 
	 * @param folderId
	 *            The folder ID
	 * @return <code>true</code> if folder exists; otherwise
	 *         <code>false</code>
	 * @throws FolderException
	 *             If folder existence cannot be checked
	 */
	public boolean exists(K folderId) throws FolderException;

	/**
	 * Gets this storage's root folder ID
	 * 
	 * @return The root folder ID
	 */
	public K getRootFolderId();

	/**
	 * Gets the storage folder uniquely identified by specified folder ID
	 * explicitly from storage.
	 * 
	 * @param folderId
	 *            The folder ID
	 * @return The folder uniquely identified by specified folder ID.
	 * @throws FolderException
	 *             If folder cannot be loaded from storage
	 */
	public Folder<K> loadFromStorage(K folderId) throws FolderException;

	/**
	 * Fills given folder object with storage values. At least corresponding ID
	 * has to be set in folder object.
	 * 
	 * @param folder
	 *            The folder object to fill
	 * @param overwrite
	 *            <code>true</code> to overwrite even if value is already
	 *            present; <code>false</code> to only fill value if not
	 *            present
	 * @throws FolderException
	 *             If folder cannot be filled
	 */
	public void fill(Folder<K> folder, boolean overwrite) throws FolderException;

	/**
	 * Gets the storage folder uniquely identified by specified folder ID either
	 * from cache on a cache hit or from storage on a cache miss.
	 * 
	 * @param folderId
	 *            The folder ID
	 * @return The folder uniquely identified by specified folder ID.
	 * @throws FolderException
	 *             If folder cannot be returned
	 */
	public Folder<K> getFolder(K folderId) throws FolderException;

	/**
	 * Yields the effective permission for the specified user which is the max.
	 * permission resulting from its user/group permissions covered with user's
	 * configuration profile.
	 * 
	 * @param folderId
	 *            The folder ID
	 * @param user
	 *            The user ID
	 * @return The effective permission for the specified user
	 * @throws FolderException
	 *             If user's effective permission cannot be determined
	 */
	public FolderPermission<K> getEffectivePermission(K folderId, int user) throws FolderException;

	/**
	 * Gets all subfolders of the folder whose ID matches specified parent ID.
	 * <p>
	 * The optional timestamp argument (the number of milliseconds since January
	 * 1, 1970, 00:00:00 GMT) limits subfolders to return only those whose last
	 * modified timestamp is equal to or greater than specified timestamp. This
	 * argument is ignored if no timestamps are supported by folder storage.
	 * 
	 * @param parentId
	 *            The parent ID whose subfolder shall be determined
	 * @param timestamp
	 *            The optional timestamp; leave <code>null</code> to ignore
	 * @return The parent folder's subfolders as an array of {@link Folder}
	 * @throws FolderException
	 *             If subfolder of specified parent folder cannot be returned
	 */
	public Folder<K>[] getSubfolders(K parentId, Long timestamp) throws FolderException;

	/**
	 * Gets all user-visible subfolders of the folder whose ID matches specified
	 * parent ID.
	 * <p>
	 * The optional timestamp argument (the number of milliseconds since January
	 * 1, 1970, 00:00:00 GMT) limits subfolders to return only those whose last
	 * modified timestamp is equal to or greater than specified timestamp. This
	 * argument is ignored if no timestamps are supported by folder storage.
	 * 
	 * @param parentId
	 *            The parent ID whose subfolder shall be determined
	 * @param user
	 *            The user ID
	 * @param timestamp
	 *            The optional timestamp; leave <code>null</code> to ignore
	 * @return The parent folder's user-visible subfolders as an array of
	 *         {@link Folder}
	 * @throws FolderException
	 *             If subfolder of specified parent folder cannot be returned
	 */
	public Folder<K>[] getVisibleSubfolders(K parentId, int user, Long timestamp) throws FolderException;

	/**
	 * Gets all user-visible ancestor folders of the folder whose ID matches
	 * folder ID and which are located on folder's path to root folder.
	 * 
	 * @param folderId
	 *            The folder ID
	 * @param user
	 *            The user ID
	 * @return All user-visible ancestor folders on folder's path to root
	 *         folder.
	 */
	public Folder<K>[] getPath2Root(K folderId, int user);

	/**
	 * Inserts a new folder described by specified folder object below the
	 * folder whose ID matches the one obtained from
	 * {@link Folder#getParentFolderID()}. Especially the parent ID, name,
	 * module, and permissions needs to be provided by specified folder object.
	 * Folder type is determined by parent folder.
	 * <p>
	 * Given folder object is then filled with the values of the created folder
	 * to further work with the object.
	 * 
	 * @param folder
	 *            The folder object representing the folder to create
	 * @throws FolderException
	 *             If folder creation fails
	 */
	public void insert(Folder<K> folder) throws FolderException;

	/**
	 * Updates the folder from storage whose ID matches the one obtained from
	 * specified folder object by invoking {@link Folder#getFolderID()} with the
	 * values provided by given folder object.
	 * <p>
	 * The optional client last modified timestamp (the number of milliseconds
	 * since January 1, 1970, 00:00:00 GMT) ensures that the folder in question
	 * is only deleted if storage folder's last modified timestamp is not after
	 * client last modified timestamp. Thus a synchronization mechanism is
	 * supported, if multiple clients work on folder storage. This argument is
	 * ignored if no timestamps are supported by folder storage.
	 * 
	 * @param folder
	 *            The folder object providing the unique ID of the folder to
	 *            update and the fields to update
	 * @param clientLastModified
	 *            The optional client last modified timestamp; leave
	 *            <code>null</code> to ignore
	 * @throws FolderException
	 *             If folder update fails or client last modified timestamp is
	 *             after storage folder's last modified timestamp
	 */
	public void update(Folder<K> folder, Long clientLastModified) throws FolderException;

	/**
	 * Deletes the folder from storage whose ID matches the one obtained from
	 * specified folder object by invoking {@link Folder#getFolderID()}.
	 * <p>
	 * The optional client last modified timestamp (the number of milliseconds
	 * since January 1, 1970, 00:00:00 GMT) ensures that the folder in question
	 * is only deleted if storage folder's last modified timestamp is not after
	 * client last modified timestamp. Thus a synchronization mechanism is
	 * supported, if multiple clients work on folder storage. This argument is
	 * ignored if no timestamps are supported by folder storage.
	 * 
	 * @param folder
	 *            The folder object providing the unique ID of the folder to
	 *            delete
	 * @param clientLastModified
	 *            The optional client last modified timestamp; leave
	 *            <code>null</code> to ignore
	 * @throws FolderException
	 *             If folder deletion fails or client last modified timestamp is
	 *             after storage folder's last modified timestamp
	 */
	public void delete(Folder<K> folder, Long clientLastModified) throws FolderException;

	/**
	 * Gets all user-visible folders that have been modified (created or
	 * updated) since given timestamp which is the number of milliseconds since
	 * January 1, 1970, 00:00:00 GMT.
	 * 
	 * @param timestamp
	 *            The timestamp
	 * @param user
	 *            The user D
	 * @return All user-visible folders that have been modified since given
	 *         timestamp
	 * @throws FolderException
	 *             If all user-visible modified folders cannot be returned or
	 *             storage has no timestamp support
	 */
	public Folder<K>[] getModifiedFolders(long timestamp, int user) throws FolderException;

	/**
	 * Gets all user-visible folders that have been deleted since given
	 * timestamp which is the number of milliseconds since January 1, 1970,
	 * 00:00:00 GMT.
	 * 
	 * @param timestamp
	 *            The timestamp
	 * @param user
	 *            The user D
	 * @return All user-visible folders that have been modified since given
	 *         timestamp
	 * @throws FolderException
	 *             If all user-visible deleted folders cannot be returned or
	 *             storage has no timestamp support
	 */
	public Folder<K>[] getDeletedFolders(long timestamp, int user) throws FolderException;

	/**
	 * Gets all folders from this storage that has been modified since specified
	 * timestamp which is the number of milliseconds since January 1, 1970,
	 * 00:00:00 GMT.
	 * 
	 * @param timestamp
	 *            The timestamp
	 * @return All folders that has been modified since specified timestamp
	 * @throws FolderException -
	 *             If all modified folders cannot be returned or storage has no
	 *             timestamp support
	 */
	public Folder<K>[] getAllModifiedFolders(long timestamp) throws FolderException;
}

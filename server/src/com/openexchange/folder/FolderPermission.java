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
 * {@link FolderPermission} - A folder permission assigned to an entity (either
 * user or group)
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public interface FolderPermission<K extends Object> extends Cloneable {

	/**
	 * No permission: <code>0</code>
	 */
	public static final int NO_PERMISSION = 0;

	/**
	 * Max. permission: <code>128</code>
	 */
	public static final int MAX_PERMISSION = 128;

	/**
	 * Read folder: <code>2</code>
	 */
	public static final int READ_FOLDER = 2;

	/**
	 * Create objects in folder: <code>4</code>
	 */
	public static final int CREATE_OBJECTS_IN_FOLDER = 4;

	/**
	 * Create subfolders: <code>8</code>
	 */
	public static final int CREATE_SUB_FOLDERS = 8;

	/**
	 * Read own objects: <code>2</code>
	 */
	public static final int READ_OWN_OBJECTS = 2;

	/**
	 * Read all objects: <code>4</code>
	 */
	public static final int READ_ALL_OBJECTS = 4;

	/**
	 * Write own objects: <code>2</code>
	 */
	public static final int WRITE_OWN_OBJECTS = 2;

	/**
	 * Write all objects: <code>4</code>
	 */
	public static final int WRITE_ALL_OBJECTS = 4;

	/**
	 * Delete own objects: <code>2</code>
	 */
	public static final int DELETE_OWN_OBJECTS = 2;

	/**
	 * Delete all objects: <code>4</code>
	 */
	public static final int DELETE_ALL_OBJECTS = 4;

	/**
	 * Gets the folder ID to which this permission is assigned
	 * 
	 * @return The folder ID
	 */
	public K getFolderID();

	/**
	 * Sets the folder ID to which this permission is assigned
	 * 
	 * @param id
	 *            The folder ID
	 */
	public void setFolderID(K id);

	/**
	 * Gets the entity ID to which this permission is assigned
	 * 
	 * @return The entity ID
	 */
	public int getEntity();

	/**
	 * Sets the entity ID to which this permission is assigned
	 * 
	 * @param entity
	 *            The entity ID
	 */
	public void setEntity(int entity);

	/**
	 * Checks if this permission grants admin privileges to assigned entity
	 * 
	 * @return <code>true</code> if permission grants admin privileges;
	 *         otherwise <code>false</code>
	 */
	public boolean isAdmin();

	/**
	 * Sets if this permission grants admin privileges to assigned entity
	 * 
	 * @param admin
	 *            <code>true</code> to grant admin privileges; otherwise
	 *            <code>false</code>
	 */
	public void setAdmin(boolean admin);

	/**
	 * Checks if assigned entity is a group
	 * 
	 * @return <code>true</code> if assigned entity is a group; otherwise
	 *         <code>false</code>
	 */
	public boolean isGroup();

	/**
	 * Sets if assigned entity is a group
	 * 
	 * @param group
	 *            <code>true</code> to mark assigned entity as a group;
	 *            otherwise <code>false</code>
	 */
	public void setGroup(boolean group);

	/**
	 * Gets the folder permission
	 * 
	 * @return The folder permission
	 */
	public int getFolderPermission();

	/**
	 * Sets the folder permission
	 * 
	 * @param folderPermission
	 *            The folder permission
	 * @throws FolderException
	 *             If specified permission value is invalid
	 */
	public void setFolderPermission(int folderPermission) throws FolderException;

	/**
	 * Gets the (object) read permission
	 * 
	 * @return The (object) read permission
	 */
	public int getReadPermission();

	/**
	 * Sets the (object) read permission
	 * 
	 * @param readPermission
	 *            The (object) read permission
	 * @throws FolderException
	 *             If specified permission value is invalid
	 */
	public void setReadPermission(int readPermission) throws FolderException;

	/**
	 * Gets the (object) write permission
	 * 
	 * @return The (object) write permission
	 */
	public int getWritePermission();

	/**
	 * Sets the (object) write permission
	 * 
	 * @param writePermission
	 *            The (object) write permission
	 * @throws FolderException
	 *             If specified permission value is invalid
	 */
	public void setWritePermission(int writePermission) throws FolderException;

	/**
	 * Gets the (object) delete permission
	 * 
	 * @return The (object) delete permission
	 */
	public int getDeletePermission();

	/**
	 * Sets the (object) delete permission
	 * 
	 * @param deletePermission
	 *            The (object) delete permission
	 * @throws FolderException
	 *             If specified permission value is invalid
	 */
	public void setDeletePermission(int deletePermission) throws FolderException;

	/**
	 * Checks if folder is visible according to folder permission
	 * 
	 * @return <code>true</code> if folder is visible according to folder
	 *         permission; otherwise <code>false</code>
	 */
	public boolean isFolderVisible();

	/**
	 * Checks if objects may be created according to folder permission
	 * 
	 * @return <code>true</code> if objects may be created according to folder
	 *         permission; otherwise <code>false</code>
	 */
	public boolean canCreateObjects();

	/**
	 * Checks if subfolders may be created according to folder permission
	 * 
	 * @return <code>true</code> if subfolders may be created according to
	 *         folder permission; otherwise <code>false</code>
	 */
	public boolean canCreateSubfolders();

	/**
	 * Checks if own objects may be read according to read permission
	 * 
	 * @return <code>true</code> if own objects may be read according to read
	 *         permission; otherwise <code>false</code>
	 */
	public boolean canReadOwnObjects();

	/**
	 * Checks if all objects may be read according to read permission
	 * 
	 * @return <code>true</code> if all objects may be read according to read
	 *         permission; otherwise <code>false</code>
	 */
	public boolean canReadAllObjects();

	/**
	 * Checks if own objects may be written according to write permission
	 * 
	 * @return <code>true</code> if own objects may be written according to
	 *         write permission; otherwise <code>false</code>
	 */
	public boolean canWriteOwnObjects();

	/**
	 * Checks if all objects may be written according to write permission
	 * 
	 * @return <code>true</code> if all objects may be written according to
	 *         write permission; otherwise <code>false</code>
	 */
	public boolean canWriteAllObjects();

	/**
	 * Checks if own objects may be deleted according to delete permission
	 * 
	 * @return <code>true</code> if own objects may be deleted according to
	 *         delete permission; otherwise <code>false</code>
	 */
	public boolean canDeleteOwnObjects();

	/**
	 * Checks if all objects may be deleted according to delete permission
	 * 
	 * @return <code>true</code> if all objects may be deleted according to
	 *         delete permission; otherwise <code>false</code>
	 */
	public boolean canDeleteAllObjects();

	/**
	 * Creates and returns a copy of this object.
	 * 
	 * @return A clone of this instance.
	 */
	public Object clone();
}

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

import java.util.Date;

/**
 * {@link Folder} - A storage folder
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public interface Folder<K extends Object> extends Cloneable {

	/**
	 * Gets the folder ID
	 * 
	 * @return The folder ID
	 */
	public K getFolderID();

	/**
	 * Sets the folder ID
	 * 
	 * @param id
	 *            The folder ID
	 */
	public void setFolderID(K id);

	/**
	 * Gets the parent folder ID
	 * 
	 * @return The parent folder ID
	 */
	public K getParentFolderID();

	/**
	 * Sets the parent folder ID
	 * 
	 * @param id
	 *            The parent folder ID
	 */
	public void setParentFolderID(K id);

	/**
	 * Gets the user ID of the user who created this folder or <code>-1</code>
	 * if not supported
	 * 
	 * @return The user ID or <code>-1</code> if not supported
	 */
	public int getCreatedBy();

	/**
	 * Gets the user ID of the user who lastly modified this folder or
	 * <code>-1</code> if not supported
	 * 
	 * @return The user ID or <code>-1</code> if not supported
	 */
	public int getModifiedBy();

	/**
	 * Gets the creation date or <code>null</code> if not supported
	 * 
	 * @return The creation date or <code>null</code> if not supported
	 */
	public Date getCreationDate();

	/**
	 * Gets the last modified date or <code>null</code> if not supported
	 * 
	 * @return The last modified date or <code>null</code> if not supported
	 */
	public Date getLastModified();

	/**
	 * Sets the ID of the user who created this folder or does nothing if not
	 * supported
	 * 
	 * @param createdBy
	 *            The user ID
	 */
	public void setCreatedBy(int createdBy);

	/**
	 * Sets the ID of the user who lastly modified this folder or does nothing
	 * if not supported
	 * 
	 * @param modifiedBy
	 *            The user ID
	 */
	public void setModifiedBy(int modifiedBy);

	/**
	 * Sets this folder's creation date or does nothing if not supported
	 * 
	 * @param creationDate
	 *            The creation date
	 */
	public void setCreationDate(Date creationDate);

	/**
	 * Sets this folder's last modified date or does nothing if not supported
	 * 
	 * @param lastModified
	 *            The last modified date
	 */
	public void setLastModified(Date lastModified);

	/**
	 * Gets the folder name
	 * 
	 * @return The folder name
	 */
	public String getName();

	/**
	 * Sets the folder name
	 * 
	 * @param name
	 *            The folder name
	 */
	public void setName(String name);

	/**
	 * Gets this folder's module
	 * 
	 * @return The module
	 */
	public FolderModule getModule();

	/**
	 * Sets this folder's module
	 * 
	 * @param module
	 *            The module
	 */
	public void setModule(FolderModule module);

	/**
	 * Gets this folder's type
	 * 
	 * @return The type
	 */
	public FolderType getType();

	/**
	 * Determines folder type dependent on specified user ID.
	 * 
	 * @param user
	 *            The user ID
	 * @return The folder type
	 */
	public FolderType getType(int user);

	/**
	 * Sets this folder's type
	 * 
	 * @param module
	 *            The type
	 */
	public void setType(FolderType type);

	/**
	 * Gets a <b>cloned</b> version of folder's permissions
	 * 
	 * @return A <b>cloned</b> version of folder's permissions
	 */
	public FolderPermission<K>[] getPermissions();

	/**
	 * Sets folder's permissions
	 * 
	 * @param permissions
	 *            The permissions to set
	 */
	public void setPermissions(FolderPermission<K>[] permissions);

	/**
	 * Checks if this folder has subfolders
	 * 
	 * @return <code>true</code> if this folder has subfolders; otherwise
	 *         <code>false</code>
	 */
	public boolean hasSubfolders();

	/**
	 * Sets if this folder has subfolders
	 * 
	 * @param hasSubfolders
	 *            <code>true</code> to indicate subfolders; otherwise
	 *            <code>false</code>
	 */
	public void setHasSubfolder(boolean hasSubfolders);

	/**
	 * Indicates if folder's subfolders flag has been set
	 * 
	 * @return <code>true</code> if folder's subfolders flag has been set;
	 *         otherwise <code>false</code>
	 */
	public boolean containsHasSubfolder();

	/**
	 * Gets the permission status
	 * 
	 * @return The permission status
	 */
	public FolderPermissionStatus getPermissionStatus();

	/**
	 * Sets the permission status
	 * 
	 * @param permissionStatus
	 *            The permission status
	 */
	public void setPermissionStatus(FolderPermissionStatus permissionStatus);

	/**
	 * Checks if this folder is marked as a default folder
	 * 
	 * @return <code>true</code> if this folder is marked as a default folder;
	 *         otherwise <code>false</code>
	 */
	public boolean isDefault();

	/**
	 * Sets this folder's default flag
	 * 
	 * @param b
	 *            <code>true</code> to mark this folder as a default folder;
	 *            otherwise <code>false</code>
	 */
	public void setDefault(boolean b);

	/**
	 * Indicates if folder's default flag has been set
	 * 
	 * @return <code>true</code> if folder's default flag has been set;
	 *         otherwise <code>false</code>
	 */
	public boolean containsDefault();

	/**
	 * Resets this folder's members for re-usage
	 */
	public void reset();

	/**
	 * Creates and returns a copy of this object.
	 * 
	 * @return A clone of this instance.
	 */
	public Object clone();

	/**
	 * This method does not check permissions of given user on this folder,
	 * rather than checks if this folder is of type <code>PRIVATE</code> and
	 * user is not folder's creator.
	 * 
	 * @param user
	 *            The user ID
	 * 
	 * @return <code>true</code> if this folder is of type PRIVATE and user is
	 *         not folder's creator; otherwise <code>false</code>
	 */
	public boolean isShared(final int user);
}

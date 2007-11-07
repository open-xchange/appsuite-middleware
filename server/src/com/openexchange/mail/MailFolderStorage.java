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

package com.openexchange.mail;

import com.openexchange.mail.dataobjects.MailFolder;

/**
 * {@link MailFolderStorage}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public interface MailFolderStorage {

	/**
	 * Checks if a folder exists whose fullname matches given
	 * <code>fullname</code>
	 * 
	 * @param fullname
	 *            The fullname
	 * @return <code>true</code> if folder exists in mailbox; otherwise
	 *         <code>false</code>
	 * @throws MailException
	 *             If existence cannot be checked
	 */
	public boolean exists(final String fullname) throws MailException;

	/**
	 * Checks if a folder exists whose ID matches given <code>id</code>
	 * 
	 * @param id
	 *            The id
	 * @return <code>true</code> if folder exists in mailbox; otherwise
	 *         <code>false</code>
	 * @throws MailException
	 *             If existence cannot be checked
	 */
	public boolean exists(final long id) throws MailException;

	/**
	 * Gets the folder identified through given fullname
	 * 
	 * @param fullname
	 *            The fullname
	 * @return The corresponding instance of {@link MailFolder}
	 * @throws MailException
	 *             If folder could not be fetched
	 */
	public MailFolder getFolder(final String fullname) throws MailException;

	/**
	 * Gets the folder identified through given id
	 * 
	 * @param id
	 *            The id
	 * @return The corresponding instance of {@link MailFolder}
	 * @throws MailException
	 *             If folder could not be fetched
	 */
	public MailFolder getFolder(final long id) throws MailException;

	/**
	 * Gets the subfolders located below the folder whose fullname matches given
	 * parameter <code>parentFullname</code>
	 * 
	 * @param parentFullname
	 *            The parent fullname
	 * @param all
	 *            whether if all or only subscribed subfolders shall be returned
	 * @return An array of {@link MailFolder} representing the subfolders
	 * @throws MailException
	 *             If subfolders cannot be delivered
	 */
	public MailFolder[] getSubfolders(final String parentFullname, final boolean all) throws MailException;

	/**
	 * Gets the subfolders located below the folder whose id matches given
	 * parameter <code>parentId</code>
	 * 
	 * @param parentId
	 *            The parent ID
	 * @param all
	 *            whether if all or only subscribed subfolders shall be returned
	 * @return An array of {@link MailFolder} representing the subfolders
	 * @throws MailException
	 *             If subfolders cannot be delivered
	 */
	public MailFolder[] getSubfolders(final long parentId, final boolean all) throws MailException;

	/**
	 * Gets the mailbox's default folder
	 * 
	 * @return The mailbox's default folder
	 * @throws MailException
	 *             If mailbox's default folder cannot be delivered
	 */
	public MailFolder getRootFolder() throws MailException;

	/**
	 * Checks user's default folder as definded in user's mail settings and
	 * creates them if any is missing
	 * 
	 * @throws MailException
	 *             If user's default folder could not be checked
	 */
	public void checkDefaultFolders() throws MailException;

	/**
	 * Creates a new mail folder with attributes taken from given mail folder
	 * 
	 * @param toCreate
	 *            The mail folder to create
	 * @return Fullname of the created mail folder
	 * @throws MailException
	 *             If creation fails
	 */
	public String createFolder(MailFolder toCreate) throws MailException;

	/**
	 * Updates an existing mail folder identifed through given fullname. All
	 * attributes set in given mail folder parameter are applied.
	 * 
	 * @param fullname
	 *            The fullname of the mail folder to update
	 * @param toUpdate
	 *            The mail folder to update containing only the modified fields
	 * @return Fullname of the updated mail folder
	 * @throws MailException
	 */
	public String updateFolder(String fullname, MailFolder toUpdate) throws MailException;

	/**
	 * Updates an existing mail folder identifed through given ID. All
	 * attributes set in given mail folder parameter are applied.
	 * 
	 * @param long
	 *            The ID of the mail folder to update
	 * @param toUpdate
	 *            The mail folder to update containing only the modified fields
	 * @return Fullname of the updated mail folder
	 * @throws MailException
	 */
	public String updateFolder(long id, MailFolder toUpdate) throws MailException;

	/**
	 * Deletes an existing mail folder identifed through given fullname. If
	 * folder is not located below default trash folder it is moved (including
	 * subfolder tree) to default trash folder; otherwise it is deleted
	 * permanently.
	 * 
	 * @param fullname
	 *            The fullname of the mail folder to delete
	 * @return Fullname of the deleted mail folder
	 * @throws MailException
	 */
	public String deleteFolder(String fullname) throws MailException;

	/**
	 * Deletes an existing mail folder identifed through given ID. If folder is
	 * not located below default trash folder it is moved (including subfolder
	 * tree) to default trash folder; otherwise it is deleted permanently.
	 * 
	 * @param id
	 *            The ID of the mail folder to delete
	 * @return Fullname of the deleted mail folder
	 * @throws MailException
	 */
	public String deleteFolder(long id) throws MailException;

	/**
	 * Deletes the content of the folder identified through given fullname
	 * 
	 * @param fullname
	 *            The fullname of the mail folder whose content should be
	 *            cleared
	 * @throws MailException
	 *             If folder's content cannot be cleared
	 */
	public void clearFolder(String fullname) throws MailException;

	/**
	 * Deletes the content of the folder identified through given ID
	 * 
	 * @param id
	 *            The ID of the mail folder whose content should be cleared
	 * @throws MailException
	 *             If folder's content cannot be cleared
	 */
	public void clearFolder(long id) throws MailException;

	/**
	 * Gets the reverse path from the folder indentified through given fullname
	 * to parental default folder. All occuring folders on that path are
	 * contained in reverse order in returned array of {@link MailFolder}
	 * instances.
	 * 
	 * @param fullname
	 *            The folder fullname
	 * @return All occuring folders in reverse order as an array of
	 *         {@link MailFolder} instances.
	 * @throws MailException
	 *             If path cannot be determined
	 */
	public MailFolder[] getPath2DefaultFolder(String fullname) throws MailException;

	/**
	 * Gets the reverse path from the folder indentified through given ID to
	 * parental default folder. All occuring folders on that path are contained
	 * in reverse order in returned array of {@link MailFolder} instances.
	 * 
	 * @param id
	 *            The folder ID
	 * @return All occuring folders in reverse order as an array of
	 *         {@link MailFolder} instances.
	 * @throws MailException
	 *             If path cannot be determined
	 */
	public MailFolder[] getPath2DefaultFolder(long id) throws MailException;

	/**
	 * Gets the ID of default drafts folder
	 * 
	 * @return The ID of default drafts folder
	 * @throws MailException
	 */
	public String getConfirmedHamFolder() throws MailException;

	/**
	 * Gets the ID of default drafts folder
	 * 
	 * @return The ID of default drafts folder
	 * @throws MailException
	 */
	public String getConfirmedSpamFolder() throws MailException;

	/**
	 * Gets the ID of default drafts folder
	 * 
	 * @return The ID of default drafts folder
	 * @throws MailException
	 */
	public String getDraftsFolder() throws MailException;

	/**
	 * Gets the ID of default spam folder
	 * 
	 * @return The ID of default spam folder
	 * @throws MailException
	 */
	public String getSpamFolder() throws MailException;

	/**
	 * Gets the ID of default sent folder
	 * 
	 * @return The ID of default sent folder
	 * @throws MailException
	 */
	public String getSentFolder() throws MailException;

	/**
	 * Gets the ID of default trash folder
	 * 
	 * @return The ID of default trash folder
	 * @throws MailException
	 */
	public String getTrashFolder() throws MailException;

	/**
	 * Releases all used resources when closing parental {@link MailConnection}
	 * 
	 * @throws MailException
	 */
	public void releaseResources() throws MailException;

}

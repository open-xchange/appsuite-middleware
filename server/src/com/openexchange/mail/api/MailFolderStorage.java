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

package com.openexchange.mail.api;

import java.util.ArrayList;
import java.util.List;

import com.openexchange.mail.MailException;
import com.openexchange.mail.Quota;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailFolderDescription;

/**
 * {@link MailFolderStorage} - Offers basic access methods to mail folder(s)
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public abstract class MailFolderStorage {

	/**
	 * Constant which indicates unlimited quota
	 * 
	 * @value <code>-1</code>
	 */
	public static final int UNLIMITED_QUOTA = -1;

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
	public abstract boolean exists(final String fullname) throws MailException;

	/**
	 * Gets the folder identified through given fullname
	 * 
	 * @param fullname
	 *            The fullname
	 * @return The corresponding instance of {@link MailFolder}
	 * @throws MailException
	 *             If folder could not be fetched
	 */
	public abstract MailFolder getFolder(final String fullname) throws MailException;

	/**
	 * Gets the first level subfolders located below the folder whose fullname
	 * matches given parameter <code>parentFullname</code>
	 * 
	 * @param parentFullname
	 *            The parent fullname
	 * @param all
	 *            Whether all or only subscribed subfolders shall be returned.
	 *            If underlying mailing system does not support folder
	 *            subscription, this argument should always be treated as
	 *            <code>true</code>.
	 * @return An array of {@link MailFolder} representing the subfolders
	 * @throws MailException
	 *             If subfolders cannot be delivered
	 */
	public abstract MailFolder[] getSubfolders(final String parentFullname, final boolean all) throws MailException;

	/**
	 * Gets the mailbox's default folder
	 * 
	 * @return The mailbox's default folder
	 * @throws MailException
	 *             If mailbox's default folder cannot be delivered
	 */
	public abstract MailFolder getRootFolder() throws MailException;

	/**
	 * Checks user's default folder as defined in user's mail settings and
	 * creates them if any is missing
	 * 
	 * @throws MailException
	 *             If user's default folder could not be checked
	 */
	public abstract void checkDefaultFolders() throws MailException;

	/**
	 * Creates a new mail folder with attributes taken from given mail folder
	 * 
	 * @param toCreate
	 *            The mail folder to create
	 * @return The fullname of the created mail folder
	 * @throws MailException
	 *             If creation fails
	 */
	public abstract String createFolder(MailFolderDescription toCreate) throws MailException;

	/**
	 * Updates an existing mail folder identified through given fullname. All
	 * attributes set in given mail folder parameter are applied.
	 * <p>
	 * The currently known attributes that make sense being updated are:
	 * <ul>
	 * <li>permissions; meaning folder's permissions are updated if
	 * {@link MailFolderDescription#containsPermissions()} returns
	 * <code>true</code></li>
	 * <li>subscription; meaning a subscribe/unsubscribe operation is performed
	 * if {@link MailFolderDescription#containsSubscribed()} returns
	 * <code>true</code></li>
	 * </ul>
	 * Of course more folder attributes may be checked by implementation to
	 * enhance update operations. The programmer may extend the
	 * {@link MailFolderDescription} class to do so.
	 * 
	 * @param fullname
	 *            The fullname of the mail folder to update
	 * @param toUpdate
	 *            The mail folder to update containing only the modified fields
	 * @return The fullname of the updated mail folder
	 * @throws MailException
	 *             If folder cannot be updated
	 */
	public abstract String updateFolder(String fullname, MailFolderDescription toUpdate) throws MailException;

	/**
	 * Moves the folder identified through given fullname to the path specified
	 * through argument <code>newFullname</code>. Thus a rename can be
	 * implicitly performed.
	 * <p>
	 * E.g.:
	 * 
	 * <pre>
	 * my.path.to.folder -&gt; my.newpath.to.folder
	 * </pre>
	 * 
	 * @param fullname
	 *            The folder fullname
	 * @param newFullname
	 *            The new fullname to move to
	 * @return The new fullname where the folder has been moved
	 * @throws MailException
	 *             If folder cannot be moved
	 */
	public abstract String moveFolder(String fullname, String newFullname) throws MailException;

	/**
	 * Renames the folder identified through given fullname to the specified new
	 * name.
	 * <p>
	 * Since a rename is a move operation in the same (parent) folder, this is
	 * only a convenience method that may be overridden if necessary.
	 * <p>
	 * E.g.:
	 * 
	 * <pre>
	 * my.path.to.folder -&gt; my.path.to.newfolder
	 * </pre>
	 * 
	 * @param fullname
	 *            The folder fullname
	 * @param newName
	 *            The new name
	 * @return The new fullname
	 * @throws MailException
	 *             If folder cannot be renamed
	 */
	public String renameFolder(final String fullname, final String newName) throws MailException {
		final MailFolder folder = getFolder(fullname);
		final String newPath = new StringBuilder(folder.getParentFullname()).append(folder.getSeparator()).append(
				newName).toString();
		return moveFolder(fullname, newPath);
	}

	/**
	 * Deletes an existing mail folder identified through given fullname.
	 * <p>
	 * This is a convenience method that invokes
	 * {@link #deleteFolder(String, boolean)} with <code>hardDelete</code> set
	 * to <code>false</code>.
	 * 
	 * @param fullname
	 *            The fullname of the mail folder to delete
	 * @return The fullname of the deleted mail folder
	 * @throws MailException
	 *             If mail folder cannot be deleted
	 */
	public String deleteFolder(final String fullname) throws MailException {
		return deleteFolder(fullname, false);
	}

	/**
	 * Deletes an existing mail folder identified through given fullname.
	 * <p>
	 * If <code>hardDelete</code> is not set and folder is not located below
	 * default trash folder it is backed up (including subfolder tree) in
	 * default trash folder; otherwise it is deleted permanently.
	 * <p>
	 * While another backup folder with the same name already exists below
	 * default trash folder, an increasing serial number is appended to folder
	 * name until its name is unique inside default trash folder's subfolders.
	 * E.g.: If folder "DeleteMe" already exists below default trash folder, the
	 * next name would be "DeleteMe2". If again a folder "DeleteMe2" already
	 * exists below default trash folder, the next name would be "DeleteMe3",
	 * and so no.
	 * <p>
	 * If default trash folder cannot hold subfolders, the folder is either
	 * deleted permanently or an appropriate exception may be thrown.
	 * 
	 * @param fullname
	 *            The fullname of the mail folder to delete
	 * @param hardDelete
	 *            Whether to delete permanently or to backup into trash folder
	 * @return The fullname of the deleted mail folder
	 * @throws MailException
	 *             If mail folder cannot be deleted
	 */
	public abstract String deleteFolder(String fullname, boolean hardDelete) throws MailException;

	/**
	 * Deletes the content of the folder identified through given fullname
	 * 
	 * @param fullname
	 *            The fullname of the mail folder whose content should be
	 *            cleared
	 * @throws MailException
	 *             If folder's content cannot be cleared
	 */
	public void clearFolder(final String fullname) throws MailException {
		clearFolder(fullname, false);
	}

	/**
	 * Deletes the content of the folder identified through given fullname
	 * 
	 * @param fullname
	 *            The fullname of the mail folder whose content should be
	 *            cleared
	 * @param hardDelete
	 *            Whether to delete permanently or to backup into trash folder
	 * @throws MailException
	 *             If folder's content cannot be cleared
	 */
	public abstract void clearFolder(String fullname, boolean hardDelete) throws MailException;

	/**
	 * Gets the reverse path from the folder identified through given fullname
	 * to parental default folder. All occurring folders on that path are
	 * contained in reverse order in returned array of {@link MailFolder}
	 * instances.
	 * 
	 * @param fullname
	 *            The folder fullname
	 * @return All occurring folders in reverse order as an array of
	 *         {@link MailFolder} instances.
	 * @throws MailException
	 *             If path cannot be determined
	 */
	public MailFolder[] getPath2DefaultFolder(final String fullname) throws MailException {
		if (fullname.equals(MailFolder.DEFAULT_FOLDER_ID)) {
			return new MailFolder[0];
		}
		MailFolder f = getFolder(fullname);
		final List<MailFolder> list = new ArrayList<MailFolder>();
		do {
			list.add(f);
			f = getFolder(f.getParentFullname());
		} while (!f.getFullname().equals(MailFolder.DEFAULT_FOLDER_ID));
		return list.toArray(new MailFolder[list.size()]);
	}

	/**
	 * Detects both quota limit and quota usage on given mailbox folder's
	 * quotaroot.
	 * 
	 * @param folder
	 *            The folder fullname (if <code>null</code> <i>"INBOX"</i> is
	 *            used)
	 * @return The quota
	 * @throws MailException
	 *             If quota limit and/or quote usage cannot be determined
	 */
	public abstract Quota getQuota(String folder) throws MailException;

	/**
	 * Gets the fullname of default confirmed ham folder
	 * 
	 * @return The fullname of default confirmed ham folder
	 * @throws MailException
	 *             If confirmed ham folder's fullname cannot be returned
	 */
	public abstract String getConfirmedHamFolder() throws MailException;

	/**
	 * Gets the fullname of default confirmed spam folder
	 * 
	 * @return The fullname of default confirmed spam folder
	 * @throws MailException
	 *             If confirmed spam folder's fullname cannot be returned
	 */
	public abstract String getConfirmedSpamFolder() throws MailException;

	/**
	 * Gets the fullname of default drafts folder
	 * 
	 * @return The fullname of default drafts folder
	 * @throws MailException
	 *             If draft folder's fullname cannot be returned
	 */
	public abstract String getDraftsFolder() throws MailException;

	/**
	 * Gets the fullname of default spam folder
	 * 
	 * @return The fullname of default spam folder
	 * @throws MailException
	 *             If spam folder's fullname cannot be returned
	 */
	public abstract String getSpamFolder() throws MailException;

	/**
	 * Gets the fullname of default sent folder
	 * 
	 * @return The fullname of default sent folder
	 * @throws MailException
	 *             If sent folder's fullname cannot be returned
	 */
	public abstract String getSentFolder() throws MailException;

	/**
	 * Gets the fullname of default trash folder
	 * 
	 * @return The fullname of default trash folder
	 * @throws MailException
	 *             If trash folder's fullname cannot be returned
	 */
	public abstract String getTrashFolder() throws MailException;

	/**
	 * Releases all used resources when closing parental {@link MailAccess}
	 * 
	 * @throws MailException
	 *             If resources cannot be released
	 */
	public abstract void releaseResources() throws MailException;

}

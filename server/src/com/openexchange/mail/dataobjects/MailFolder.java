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

package com.openexchange.mail.dataobjects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.openexchange.mail.permission.DefaultMailPermission;
import com.openexchange.server.impl.OCLPermission;

/**
 * MailFolder - a data container object for a mail folder
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public class MailFolder implements Serializable {

	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = -8203697938992090309L;

	private String name;

	private boolean b_name;

	private String fullname;

	private boolean b_fullname;

	private String parentFullname;

	private boolean b_parentFullname;

	private boolean subscribed;

	private boolean b_subscribed;

	private boolean hasSubfolders;

	private boolean b_hasSubfolders;

	private boolean hasSubscribedSubfolders;

	private boolean b_hasSubscribedSubfolders;

	private boolean exists;

	private boolean b_exists;

	private boolean holdsMessages;

	private boolean b_holdsMessages;

	private boolean holdsFolders;

	private boolean b_holdsFolders;

	private int messageCount;

	private boolean b_messageCount;

	private int newMessageCount;

	private boolean b_newMessageCount;

	private int unreadMessageCount;

	private boolean b_unreadMessageCount;

	private int deletedMessageCount;

	private boolean b_deletedMessageCount;

	private char separator;

	private boolean b_separator;

	private OCLPermission ownPermission;

	private boolean b_ownPermission;

	private boolean supportsUserFlags;

	private boolean b_supportsUserFlags;

	private boolean rootFolder;

	private boolean b_rootFolder;

	private boolean defaultFolder;

	private boolean b_defaultFolder;

	private String summary;

	private boolean b_summary;

	private List<OCLPermission> permissions;

	private boolean b_permissions;

	/**
	 * Virtual name of mailbox's root folder
	 * 
	 * @value E-Mail
	 */
	public static final String DEFAULT_FOLDER_NAME = "E-Mail";

	/**
	 * Virtual fullname of mailbox's root folder
	 * 
	 * @value default
	 */
	public static final String DEFAULT_FOLDER_ID = "default";

	/**
	 * Default constructor
	 */
	public MailFolder() {
		super();
	}

	/**
	 * Gets the fullname
	 * 
	 * @return The fullname ({@link #DEFAULT_FOLDER_ID} if this mail folder
	 *         denotes the root folder)
	 */
	public String getFullname() {
		return fullname;
	}

	/**
	 * @return <code>true</code> if fullname is set; otherwise
	 *         <code>false</code>
	 */
	public boolean containsFullname() {
		return b_fullname;
	}

	/**
	 * Removes the fullname
	 */
	public void removeFullname() {
		fullname = null;
		b_fullname = false;
	}

	/**
	 * Sets this mail folder's fullname.
	 * <p>
	 * If this mail folder denotes the root folder, {@link #DEFAULT_FOLDER_ID}
	 * is supposed to be set as fullname.
	 * 
	 * @param fullname
	 *            the fullname to set
	 */
	public void setFullname(final String fullname) {
		this.fullname = fullname;
		b_fullname = true;
	}

	/**
	 * @return the hasSubfolders
	 */
	public boolean hasSubfolders() {
		return hasSubfolders;
	}

	/**
	 * @return <code>true</code> if hasSubfolders is set; otherwise
	 *         <code>false</code>
	 */
	public boolean containsSubfolders() {
		return b_hasSubfolders;
	}

	/**
	 * Removes hasSubfolders
	 */
	public void removeSubfolders() {
		hasSubfolders = false;
		b_hasSubfolders = false;
	}

	/**
	 * @param hasSubfolders
	 *            the hasSubfolders to set
	 */
	public void setSubfolders(final boolean hasSubfolders) {
		this.hasSubfolders = hasSubfolders;
		b_hasSubfolders = true;
	}

	/**
	 * @return the hasSubscribedSubfolders
	 */
	public boolean hasSubscribedSubfolders() {
		return hasSubscribedSubfolders;
	}

	/**
	 * @return <code>true</code> if hasSubscribedSubfolders is set; otherwise
	 *         <code>false</code>
	 */
	public boolean containsSubscribedSubfolders() {
		return b_hasSubscribedSubfolders;
	}

	/**
	 * Removes hasSubscribedSubfolders
	 */
	public void removeSubscribedSubfolders() {
		hasSubscribedSubfolders = false;
		b_hasSubscribedSubfolders = false;
	}

	/**
	 * @param hasSubscribedSubfolders
	 *            the hasSubscribedSubfolders to set
	 */
	public void setSubscribedSubfolders(final boolean hasSubscribedSubfolders) {
		this.hasSubscribedSubfolders = hasSubscribedSubfolders;
		b_hasSubscribedSubfolders = true;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return <code>true</code> if name is set; otherwise <code>false</code>
	 */
	public boolean containsName() {
		return b_name;
	}

	/**
	 * Removes the name
	 */
	public void removeName() {
		name = null;
		b_name = false;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(final String name) {
		this.name = name;
		b_name = true;
	}

	/**
	 * @return the subscribed
	 */
	public boolean isSubscribed() {
		return subscribed;
	}

	/**
	 * @return <code>true</code> if subscribed is set; otherwise
	 *         <code>false</code>
	 */
	public boolean containsSubscribed() {
		return b_subscribed;
	}

	/**
	 * Removes the subscribed
	 */
	public void removeSubscribed() {
		subscribed = false;
		b_subscribed = false;
	}

	/**
	 * @param subscribed
	 *            the subscribed to set
	 */
	public void setSubscribed(final boolean subscribed) {
		this.subscribed = subscribed;
		b_subscribed = true;
	}

	/**
	 * Gets the deletedMessageCount
	 * 
	 * @return the deletedMessageCount
	 */
	public int getDeletedMessageCount() {
		return deletedMessageCount;
	}

	/**
	 * @return <code>true</code> if deletedMessageCount is set; otherwise
	 *         <code>false</code>
	 */
	public boolean containsDeletedMessageCount() {
		return b_deletedMessageCount;
	}

	/**
	 * Removes the deletedMessageCount
	 */
	public void removeDeletedMessageCount() {
		deletedMessageCount = 0;
		b_deletedMessageCount = false;
	}

	/**
	 * Sets the deletedMessageCount
	 * 
	 * @param deletedMessageCount
	 *            the deletedMessageCount to set
	 */
	public void setDeletedMessageCount(final int deletedMessageCount) {
		this.deletedMessageCount = deletedMessageCount;
		b_deletedMessageCount = true;
	}

	/**
	 * Checks if this folder exists
	 * 
	 * @return <code>true</code> if folder exists in mailbox; otherwise
	 *         <code>false</code>
	 */
	public boolean exists() {
		return exists;
	}

	/**
	 * @return <code>true</code> if exists status is set; otherwise
	 *         <code>false</code>
	 */
	public boolean containsExists() {
		return b_exists;
	}

	/**
	 * Removes exists status
	 */
	public void removeExists() {
		exists = false;
		b_exists = false;
	}

	/**
	 * Sets the exists status
	 * 
	 * @param exists
	 *            <code>true</code> if folder exists in mailbox; otherwise
	 *            <code>false</code>
	 */
	public void setExists(final boolean exists) {
		this.exists = exists;
		b_exists = true;
	}

	/**
	 * Gets the messageCount
	 * 
	 * @return the messageCount
	 */
	public int getMessageCount() {
		return messageCount;
	}

	/**
	 * @return <code>true</code> if messageCount is set; otherwise
	 *         <code>false</code>
	 */
	public boolean containsMessageCount() {
		return b_messageCount;
	}

	/**
	 * Removes the messageCount
	 */
	public void removeMessageCount() {
		messageCount = 0;
		b_messageCount = false;
	}

	/**
	 * Sets the messageCount
	 * 
	 * @param messageCount
	 *            the messageCount to set
	 */
	public void setMessageCount(final int messageCount) {
		this.messageCount = messageCount;
		b_messageCount = true;
	}

	/**
	 * Gets the newMessageCount
	 * 
	 * @return the newMessageCount
	 */
	public int getNewMessageCount() {
		return newMessageCount;
	}

	/**
	 * @return <code>true</code> if newMessageCount is set; otherwise
	 *         <code>false</code>
	 */
	public boolean containsNewMessageCount() {
		return b_newMessageCount;
	}

	/**
	 * Removes the newMessageCount
	 */
	public void removeNewMessageCount() {
		newMessageCount = 0;
		b_newMessageCount = false;
	}

	/**
	 * Sets the newMessageCount
	 * 
	 * @param newMessageCount
	 *            the newMessageCount to set
	 */
	public void setNewMessageCount(final int newMessageCount) {
		this.newMessageCount = newMessageCount;
		b_newMessageCount = true;
	}

	/**
	 * Gets the unreadMessageCount
	 * 
	 * @return the unreadMessageCount
	 */
	public int getUnreadMessageCount() {
		return unreadMessageCount;
	}

	/**
	 * @return <code>true</code> if unreadMessageCount is set; otherwise
	 *         <code>false</code>
	 */
	public boolean containsUnreadMessageCount() {
		return b_unreadMessageCount;
	}

	/**
	 * Removes the unreadMessageCount
	 */
	public void removeUnreadMessageCount() {
		unreadMessageCount = 0;
		b_unreadMessageCount = false;
	}

	/**
	 * Sets the unreadMessageCount
	 * 
	 * @param unreadMessageCount
	 *            the unreadMessageCount to set
	 */
	public void setUnreadMessageCount(final int unreadMessageCount) {
		this.unreadMessageCount = unreadMessageCount;
		b_unreadMessageCount = true;
	}

	/**
	 * Gets the separator
	 * 
	 * @return the separator
	 */
	public char getSeparator() {
		return separator;
	}

	/**
	 * @return <code>true</code> if separator is set; otherwise
	 *         <code>false</code>
	 */
	public boolean containsSeparator() {
		return b_separator;
	}

	/**
	 * Removes the separator
	 */
	public void removeSeparator() {
		separator = '0';
		b_separator = false;
	}

	/**
	 * Sets the separator
	 * 
	 * @param separator
	 *            the separator to set
	 */
	public void setSeparator(final char separator) {
		this.separator = separator;
		b_separator = true;
	}

	/**
	 * Gets the parent fullname
	 * 
	 * @return The parent fullname or <code>null</code> if this mail folder
	 *         denotes the root folder
	 */
	public String getParentFullname() {
		return parentFullname;
	}

	/**
	 * @return <code>true</code> if parentFullname is set; otherwise
	 *         <code>false</code>
	 */
	public boolean containsParentFullname() {
		return b_parentFullname;
	}

	/**
	 * Removes the parentFullname
	 */
	public void removeParentFullname() {
		parentFullname = null;
		b_parentFullname = false;
	}

	/**
	 * Sets the parent fullname
	 * <p>
	 * If this mail folder denotes the root folder, <code>null</code> is
	 * supposed to be set.
	 * 
	 * @param parentFullname
	 *            the parent fullname to set
	 */
	public void setParentFullname(final String parentFullname) {
		this.parentFullname = parentFullname;
		b_parentFullname = true;
	}

	/**
	 * Checks if this folder holds messages
	 * 
	 * @return <code>true</code> if folder holds messages; otherwise
	 *         <code>false</code>
	 */
	public boolean isHoldsMessages() {
		return holdsMessages;
	}

	/**
	 * @return <code>true</code> if this folder holds messages; otherwise
	 *         <code>false</code>
	 */
	public boolean containsHoldsMessages() {
		return b_holdsMessages;
	}

	/**
	 * Removes the holds messages flag
	 */
	public void removeHoldsMessages() {
		holdsMessages = false;
		b_holdsMessages = false;
	}

	/**
	 * Sets if this folder holds messages
	 * 
	 * @param holdsMessages
	 *            <code>true</code> if folder holds messages; otherwise
	 *            <code>false</code>
	 */
	public void setHoldsMessages(final boolean holdsMessages) {
		this.holdsMessages = holdsMessages;
		b_holdsMessages = true;
	}

	/**
	 * Checks if this folder holds folders
	 * 
	 * @return <code>true</code> if folder holds folders; otherwise
	 *         <code>false</code>
	 */
	public boolean isHoldsFolders() {
		return holdsFolders;
	}

	/**
	 * @return <code>true</code> if this folder holds folders; otherwise
	 *         <code>false</code>
	 */
	public boolean containsHoldsFolders() {
		return b_holdsFolders;
	}

	/**
	 * Removes the holds folders flag
	 */
	public void removeHoldsFolders() {
		holdsFolders = false;
		b_holdsFolders = false;
	}

	/**
	 * Sets if this folder holds folders
	 * 
	 * @param holdsFolders
	 *            <code>true</code> if folder holds folders; otherwise
	 *            <code>false</code>
	 */
	public void setHoldsFolders(final boolean holdsFolders) {
		this.holdsFolders = holdsFolders;
		b_holdsFolders = true;
	}

	/**
	 * Gets the ownPermission
	 * 
	 * @return the ownPermission or <code>null</code> if this mail folder
	 *         denotes the root folder
	 */
	public OCLPermission getOwnPermission() {
		return ownPermission;
	}

	/**
	 * @return <code>true</code> if ownPermission is set; otherwise
	 *         <code>false</code>
	 */
	public boolean containsOwnPermission() {
		return b_ownPermission;
	}

	/**
	 * Removes the ownPermission
	 */
	public void removeOwnPermission() {
		ownPermission = null;
		b_ownPermission = false;
	}

	/**
	 * Sets own permission.
	 * <p>
	 * Apply an instance of {@link DefaultMailPermission} if mailing system does
	 * not support permissions, except if this mail folder denotes the root
	 * folder, then apply <code>null</code>.
	 * 
	 * @param ownPermission
	 *            the own permission to set
	 */
	public void setOwnPermission(final OCLPermission ownPermission) {
		this.ownPermission = ownPermission;
		b_ownPermission = true;
	}

	/**
	 * Gets the rootFolder
	 * 
	 * @return the rootFolder
	 */
	public boolean isRootFolder() {
		return rootFolder;
	}

	/**
	 * @return <code>true</code> if rootFolder is set; otherwise
	 *         <code>false</code>
	 */
	public boolean containsRootFolder() {
		return b_rootFolder;
	}

	/**
	 * Removes the rootFolder
	 */
	public void removeRootFolder() {
		rootFolder = false;
		b_rootFolder = false;
	}

	/**
	 * Sets the rootFolder
	 * 
	 * @param rootFolder
	 *            the rootFolder to set
	 */
	public void setRootFolder(final boolean rootFolder) {
		this.rootFolder = rootFolder;
		b_rootFolder = true;
	}

	/**
	 * Gets the default folder status
	 * 
	 * @return the default folder status
	 */
	public boolean isDefaultFolder() {
		return defaultFolder;
	}

	/**
	 * @return <code>true</code> if default folder is set; otherwise
	 *         <code>false</code>
	 */
	public boolean containsDefaultFolder() {
		return b_defaultFolder;
	}

	/**
	 * Removes the default folder status
	 */
	public void removeDefaultFolder() {
		defaultFolder = false;
		b_defaultFolder = false;
	}

	/**
	 * Sets the default folder status
	 * 
	 * @param defaultFolder
	 *            the default folder status to set
	 */
	public void setDefaultFolder(final boolean defaultFolder) {
		this.defaultFolder = defaultFolder;
		b_defaultFolder = true;
	}

	/**
	 * Gets the summary
	 * 
	 * @return The summary or <code>null</code> if this mail folder denotes
	 *         the root folder.
	 */
	public String getSummary() {
		return summary;
	}

	/**
	 * @return <code>true</code> if summary is set; otherwise
	 *         <code>false</code>
	 */
	public boolean containsSummary() {
		return b_summary;
	}

	/**
	 * Removes the summary
	 */
	public void removeSummary() {
		summary = null;
		b_summary = false;
	}

	/**
	 * Sets the summary
	 * <p>
	 * Set to <code>null</code> for root folder.
	 * 
	 * @param summary
	 *            the summary to set
	 */
	public void setSummary(final String summary) {
		this.summary = summary;
		b_summary = true;
	}

	/**
	 * Adds a permission
	 * 
	 * @param permission
	 *            The permission to add
	 */
	public void addPermission(final OCLPermission permission) {
		if (null == permission) {
			return;
		} else if (null == permissions) {
			permissions = new ArrayList<OCLPermission>();
			b_permissions = true;
		}
		permissions.add(permission);
	}

	/**
	 * Adds permissions
	 * 
	 * @param permissions
	 *            The permissions to add
	 */
	public void addPermissions(final OCLPermission[] permissions) {
		if (null == permissions || permissions.length == 0) {
			return;
		} else if (null == this.permissions) {
			this.permissions = new ArrayList<OCLPermission>(permissions.length);
			b_permissions = true;
		}
		this.permissions.addAll(Arrays.asList(permissions));
	}

	/**
	 * @return <code>true</code> if permissions are set; otherwise
	 *         <code>false</code>
	 */
	public boolean containsPermissions() {
		return b_permissions;
	}

	/**
	 * Removes the permissions
	 */
	public void removePermissions() {
		permissions = null;
		b_permissions = false;
	}

	private static final OCLPermission[] EMPTY_PERMS = new OCLPermission[0];

	/**
	 * @return the permissions as array of {@link OCLPermission}
	 */
	public OCLPermission[] getPermissions() {
		if (null == permissions) {
			return EMPTY_PERMS;
		}
		return permissions.toArray(new OCLPermission[permissions.size()]);
	}

	@Override
	public String toString() {
		return containsFullname() ? getFullname() : "[no fullname]";
	}

	/**
	 * Gets the supportsUserFlags
	 * 
	 * @return the supportsUserFlags
	 */
	public boolean isSupportsUserFlags() {
		return supportsUserFlags;
	}

	/**
	 * @return <code>true</code> if supportsUserFlags is set; otherwise
	 *         <code>false</code>
	 */
	public boolean containsSupportsUserFlags() {
		return b_supportsUserFlags;
	}

	/**
	 * Removes the supportsUserFlags
	 */
	public void removeSupportsUserFlags() {
		b_supportsUserFlags = false;
		b_supportsUserFlags = false;
	}

	/**
	 * Sets the supportsUserFlags
	 * 
	 * @param supportsUserFlags
	 *            the supportsUserFlags to set
	 */
	public void setSupportsUserFlags(final boolean supportsUserFlags) {
		this.supportsUserFlags = supportsUserFlags;
		b_supportsUserFlags = true;
	}

}

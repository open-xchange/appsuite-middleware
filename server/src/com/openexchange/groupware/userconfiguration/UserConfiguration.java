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

package com.openexchange.groupware.userconfiguration;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteFailedException;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.server.impl.DBPoolingException;
import com.openexchange.tools.Collections.SmartIntArray;

/**
 * {@link UserConfiguration} - Represents a user's module access configuration
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UserConfiguration implements Serializable, DeleteListener, Cloneable {

	private static final long serialVersionUID = -8277899698366715803L;

	private static final transient Log LOG = LogFactory.getLog(UserConfiguration.class);

	private static final int WEBMAIL = 1;

	private static final int CALENDAR = 1 << 1;

	private static final int CONTACTS = 1 << 2;

	private static final int TASKS = 1 << 3;

	private static final int INFOSTORE = 1 << 4;

	private static final int PROJECTS = 1 << 5;

	private static final int FORUM = 1 << 6;

	private static final int PINBOARD_WRITE_ACCESS = 1 << 7;

	private static final int WEBDAV_XML = 1 << 8;

	private static final int WEBDAV = 1 << 9;

	private static final int ICAL = 1 << 10;

	private static final int VCARD = 1 << 11;

	private static final int RSS_BOOKMARKS = 1 << 12;

	private static final int RSS_PORTAL = 1 << 13;

	private static final int MOBILITY = 1 << 14;

	private static final int EDIT_PUBLIC_FOLDERS = 1 << 15;

	private static final int READ_CREATE_SHARED_FOLDERS = 1 << 16;

	private static final int DELEGATE_TASKS = 1 << 17;

	/*
	 * Field members
	 */
	private int permissionBits;

	private final int userId;

	private int[] groups;

	private final transient Context ctx;

	private int[] accessibleModules;

	private boolean accessibleModulesComputed;

	/**
	 * Initializes a new {@link UserConfiguration}
	 */
	public UserConfiguration() {
		super();
		userId = -1;
		ctx = null;
		groups = null;
	}

	/**
	 * Initializes a new {@link UserConfiguration}
	 * 
	 * @param permissionBits
	 *            The permissions' bit mask
	 * @param userId
	 *            The user ID
	 * @param groups
	 *            The user's group IDs
	 * @param ctx
	 *            The context
	 */
	public UserConfiguration(final int permissionBits, final int userId, final int[] groups, final Context ctx) {
		super();
		this.permissionBits = permissionBits;
		this.userId = userId;
		this.groups = new int[groups.length];
		System.arraycopy(groups, 0, this.groups, 0, groups.length);
		this.ctx = ctx;
	}

	@Override
	public boolean equals(final Object other) {
		if (this == other) {
			return true;
		} else if ((other == null) || !(other instanceof UserConfiguration)) {
			return false;
		}
		final UserConfiguration uc = (UserConfiguration) other;
		if ((userId != uc.userId) || (permissionBits != uc.permissionBits)) {
			return false;
		}
		if (null != groups) {
			if (null == uc.groups) {
				return false;
			}
			Arrays.sort(groups);
			Arrays.sort(uc.groups);
			if (!Arrays.equals(groups, uc.groups)) {
				return false;
			}
		}
		if (null != uc.groups) {
			return false;
		}
		if (null != ctx) {
			if (null == uc.ctx) {
				return false;
			}
			return (ctx.getContextId() == uc.ctx.getContextId());
		}
		return (null == uc.ctx);
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 31 * hash + userId;
		hash = 31 * hash + permissionBits;
		if (null != groups) {
			Arrays.sort(groups);
			for (int i = 0; i < groups.length; i++) {
				hash = 31 * hash + groups[i];
			}
		}
		if (null != ctx) {
			hash = 31 * hash + ctx.getContextId();
		}
		return hash;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		try {
			final UserConfiguration clone = (UserConfiguration) super.clone();
			if (groups != null) {
				clone.groups = new int[groups.length];
				System.arraycopy(groups, 0, clone.groups, 0, groups.length);
			}
			if (accessibleModules != null) {
				clone.accessibleModules = new int[accessibleModules.length];
				System.arraycopy(accessibleModules, 0, clone.accessibleModules, 0, accessibleModules.length);
			}
			/*
			 * if (userSettingMail != null) { clone.userSettingMail =
			 * (UserSettingMail) userSettingMail.clone(); }
			 */
			return clone;
		} catch (final CloneNotSupportedException e) {
			LOG.error(e.getMessage(), e);
			throw new InternalError(e.getMessage());
		}
	}

	/**
	 * Gets this user configuration's bit pattern
	 * 
	 * @return the bit pattern as an <code>int</code>
	 */
	public int getPermissionBits() {
		return permissionBits;
	}

	/**
	 * Sets this user configuration's bit pattern
	 * 
	 * @param permissionBits -
	 *            the bit pattern
	 */
	public void setPermissionBits(final int permissionBits) {
		this.permissionBits = permissionBits;
		accessibleModulesComputed = false;
	}

	/**
	 * Detects if user configuration allows web mail access
	 * 
	 * @return <code>true</code> if enabled; otherwise <code>false</code>
	 */
	public boolean hasWebMail() {
		return hasPermission(WEBMAIL);
	}

	/**
	 * Enables/Disables web mail access in user configuration
	 * 
	 * @param enableWebMail
	 */
	public void setWebMail(final boolean enableWebMail) {
		setPermission(enableWebMail, WEBMAIL);
		accessibleModulesComputed = false;
	}

	/**
	 * Detects if user configuration allows calendar access
	 * 
	 * @return <code>true</code> if enabled; otherwise <code>false</code>
	 */
	public boolean hasCalendar() {
		return hasPermission(CALENDAR);
	}

	/**
	 * Enables/Disables calendar access in user configuration
	 * 
	 * @param enableCalender
	 */
	public void setCalendar(final boolean enableCalender) {
		setPermission(enableCalender, CALENDAR);
		accessibleModulesComputed = false;
	}

	/**
	 * Detects if user configuration allows contact access
	 * 
	 * @return <code>true</code> if enabled; otherwise <code>false</code>
	 */
	public boolean hasContact() {
		return hasPermission(CONTACTS);
	}

	/**
	 * Enables/Disables contact access in user configuration
	 * 
	 * @param enableContact
	 */
	public void setContact(final boolean enableContact) {
		setPermission(enableContact, CONTACTS);
		accessibleModulesComputed = false;
	}

	/**
	 * Detects if user configuration allows task access
	 * 
	 * @return <code>true</code> if enabled; otherwise <code>false</code>
	 */
	public boolean hasTask() {
		return hasPermission(TASKS);
	}

	/**
	 * Enables/Disables task access in user configuration
	 * 
	 * @param enableTask
	 */
	public void setTask(final boolean enableTask) {
		setPermission(enableTask, TASKS);
		accessibleModulesComputed = false;
	}

	/**
	 * Detects if user configuration allows infostore access
	 * 
	 * @return <code>true</code> if enabled; otherwise <code>false</code>
	 */
	public boolean hasInfostore() {
		return hasPermission(INFOSTORE);
	}

	/**
	 * Enables/Disables infostore access in user configuration
	 * 
	 * @param enableInfostore
	 */
	public void setInfostore(final boolean enableInfostore) {
		setPermission(enableInfostore, INFOSTORE);
		accessibleModulesComputed = false;
	}

	/**
	 * Detects if user configuration allows project access
	 * 
	 * @return <code>true</code> if enabled; otherwise <code>false</code>
	 */
	public boolean hasProject() {
		return hasPermission(PROJECTS);
	}

	/**
	 * Enables/Disables project access in user configuration
	 * 
	 * @param enableProject
	 */
	public void setProject(final boolean enableProject) {
		setPermission(enableProject, PROJECTS);
		accessibleModulesComputed = false;
	}

	/**
	 * Detects if user configuration allows forum access
	 * 
	 * @return <code>true</code> if enabled; otherwise <code>false</code>
	 */
	public boolean hasForum() {
		return hasPermission(FORUM);
	}

	/**
	 * Enables/Disables forum access in user configuration
	 * 
	 * @param enableForum
	 */
	public void setForum(final boolean enableForum) {
		setPermission(enableForum, FORUM);
	}

	/**
	 * Detects if user configuration allows pinboard write access
	 * 
	 * @return <code>true</code> if enabled; otherwise <code>false</code>
	 */
	public boolean hasPinboardWriteAccess() {
		return hasPermission(PINBOARD_WRITE_ACCESS);
	}

	/**
	 * Enables/Disables pinboard write access in user configuration
	 * 
	 * @param enablePinboardWriteAccess
	 */
	public void setPinboardWriteAccess(final boolean enablePinboardWriteAccess) {
		setPermission(enablePinboardWriteAccess, PINBOARD_WRITE_ACCESS);
	}

	/**
	 * Detects if user configuration allows WebDAV XML
	 * 
	 * @return <code>true</code> if enabled; otherwise <code>false</code>
	 */
	public boolean hasWebDAVXML() {
		return hasPermission(WEBDAV_XML);
	}

	/**
	 * Enables/Disables WebDAV XML access in user configuration
	 * 
	 * @param enableWebDAVXML
	 */
	public void setWebDAVXML(final boolean enableWebDAVXML) {
		setPermission(enableWebDAVXML, WEBDAV_XML);
	}

	/**
	 * Detects if user configuration allows WebDAV
	 * 
	 * @return <code>true</code> if enabled; otherwise <code>false</code>
	 */
	public boolean hasWebDAV() {
		return hasPermission(WEBDAV);
	}

	/**
	 * Enables/Disables WebDAV access in user configuration
	 * 
	 * @param enableWebDAV
	 */
	public void setWebDAV(final boolean enableWebDAV) {
		setPermission(enableWebDAV, WEBDAV);
	}

	/**
	 * Detects if user configuration allows ICalendar
	 * 
	 * @return <code>true</code> if enabled; otherwise <code>false</code>
	 */
	public boolean hasICal() {
		return hasPermission(ICAL);
	}

	/**
	 * Enables/Disables ICalendar access in user configuration
	 * 
	 * @param enableICal
	 */
	public void setICal(final boolean enableICal) {
		setPermission(enableICal, ICAL);
	}

	/**
	 * Detects if user configuration allows VCard
	 * 
	 * @return <code>true</code> if enabled; otherwise <code>false</code>
	 */
	public boolean hasVCard() {
		return hasPermission(VCARD);
	}

	/**
	 * Enables/Disables VCard access in user configuration
	 * 
	 * @param enableVCard
	 */
	public void setVCard(final boolean enableVCard) {
		setPermission(enableVCard, VCARD);
	}

	/**
	 * Detects if user configuration allows RSS bookmarks
	 * 
	 * @return <code>true</code> if enabled; otherwise <code>false</code>
	 */
	public boolean hasRSSBookmarks() {
		return hasPermission(RSS_BOOKMARKS);
	}

	/**
	 * Enables/Disables RSS bookmarks access in user configuration
	 * 
	 * @param enableRSSBookmarks
	 */
	public void setRSSBookmarks(final boolean enableRSSBookmarks) {
		setPermission(enableRSSBookmarks, RSS_BOOKMARKS);
	}

	/**
	 * Detects if user configuration allows RSS portal
	 * 
	 * @return <code>true</code> if enabled; otherwise <code>false</code>
	 */
	public boolean hasRSSPortal() {
		return hasPermission(RSS_PORTAL);
	}

	/**
	 * Enables/Disables RSS portal access in user configuration
	 * 
	 * @param enableRSSPortal
	 */
	public void setRSSPortal(final boolean enableRSSPortal) {
		setPermission(enableRSSPortal, RSS_PORTAL);
	}

	/**
	 * Detects if user configuration allows mobility functionality
	 * 
	 * @return <code>true</code> if enabled; otherwise <code>false</code>
	 */
	public boolean hasSyncML() {
		return hasPermission(MOBILITY);
	}

	/**
	 * Enables/Disables mobility access in user configuration
	 * 
	 * @param enableSyncML
	 */
	public void setSyncML(final boolean enableSyncML) {
		setPermission(enableSyncML, MOBILITY);
	}

	/**
	 * Detects if user configuration allows PIM functionality (Calendar,
	 * Contact, and Task)
	 * 
	 * @return <code>true</code> if PIM functionality (Calendar, Contact, and
	 *         Task) is allowed; otherwise <code>false</code>
	 */
	public boolean hasPIM() {
		return hasCalendar() && hasContact() && hasTask();
	}

	/**
	 * Detects if user configuration allows team view
	 * 
	 * @return <code>true</code> if team view is allowed;otherwise
	 *         <code>false</code>
	 */
	public boolean hasTeamView() {
		return hasCalendar() && hasInfostore() && hasFullSharedFolderAccess() && hasFullPublicFolderAccess();
	}

	/**
	 * Detects if user configuration allows free busy
	 * 
	 * @return <code>true</code> if free busy is allowed;otherwise
	 *         <code>false</code>
	 */
	public boolean hasFreeBusy() {
		return hasCalendar() && hasInfostore() && hasFullSharedFolderAccess() && hasFullPublicFolderAccess();
	}

	/**
	 * Detects if user configuration allows conflict handling
	 * 
	 * @return <code>true</code> if conflict handling is allowed;otherwise
	 *         <code>false</code>
	 */
	public boolean hasConflictHandling() {
		return hasCalendar() && hasInfostore() && hasFullSharedFolderAccess() && hasFullPublicFolderAccess();
	}

	/**
	 * Detects if user configuration allows portal page in GUI
	 * 
	 * @return <code>true</code> if portal page is allowed;otherwise
	 *         <code>false</code>
	 */
	public boolean hasPortal() {
		return hasCalendar() && hasContact() && hasTask();
	}

	/**
	 * Determines all accessible modules as defined in user configuration. The
	 * returned array of <code>int</code> is sorted according to
	 * <code>{@link Arrays#sort(int[])}</code>, thus
	 * <code>{@link Arrays#binarySearch(int[], int)}</code> can be used to
	 * detect if a user holds module access to a certain module (or invoke
	 * <code>{@link UserConfiguration#hasModuleAccess(int)}</code>).
	 * <p>
	 * The <code>int</code> values matches the constants
	 * <code>{@link FolderObject#TASK}</code>,
	 * <code>{@link FolderObject#CALENDAR}</code>,
	 * <code>{@link FolderObject#CONTACT}</code>,
	 * <code>{@link FolderObject#UNBOUND}</code>,
	 * <code>{@link FolderObject#SYSTEM_MODULE}</code>,
	 * <code>{@link FolderObject#PROJECT}</code>,
	 * <code>{@link FolderObject#MAIL}</code>,
	 * <code>{@link FolderObject#INFOSTORE}</code>
	 * 
	 * @return A sorted array of <code>int</code> carrying accessible module
	 *         integer constants
	 */
	public int[] getAccessibleModules() {
		if (accessibleModulesComputed) {
			return cloneAccessibleModules();
		}
		final SmartIntArray array = new SmartIntArray(7);
		if (hasTask()) {
			array.append(FolderObject.TASK);
		}
		if (hasCalendar()) {
			array.append(FolderObject.CALENDAR);
		}
		if (hasContact()) {
			array.append(FolderObject.CONTACT);
		}
		if (hasProject()) {
			array.append(FolderObject.PROJECT);
		}
		if (hasInfostore()) {
			array.append(FolderObject.INFOSTORE);
		}
		if (hasWebMail()) {
			array.append(FolderObject.MAIL);
		}
		array.append(FolderObject.SYSTEM_MODULE);
		array.append(FolderObject.UNBOUND);
		accessibleModulesComputed = true;
		accessibleModules = array.toArray();
		Arrays.sort(accessibleModules);
		return cloneAccessibleModules();
	}

	/**
	 * Checks if user has access to given module
	 * 
	 * @param module
	 *            The module carrying a value defined in constants
	 *            <code>{@link FolderObject#TASK}</code>,
	 *            <code>{@link FolderObject#CALENDAR}</code>,
	 *            <code>{@link FolderObject#CONTACT}</code>,
	 *            <code>{@link FolderObject#UNBOUND}</code>,
	 *            <code>{@link FolderObject#SYSTEM_MODULE}</code>,
	 *            <code>{@link FolderObject#PROJECT}</code>,
	 *            <code>{@link FolderObject#MAIL}</code>,
	 *            <code>{@link FolderObject#INFOSTORE}</code>
	 * @return <code>true</code> if user configuration permits access to given
	 *         module; otherwise <code>false</code>
	 */
	public boolean hasModuleAccess(final int module) {
		return Arrays.binarySearch(getAccessibleModules(), module) >= 0;
	}

	private int[] cloneAccessibleModules() {
		final int[] clone = new int[accessibleModules.length];
		System.arraycopy(accessibleModules, 0, clone, 0, clone.length);
		return clone;
	}

	/**
	 * If this permission is not granted, it is prohibited for the user to
	 * create or edit public folders. Existing public folders are visible in any
	 * case.
	 * 
	 * @return <code>true</code> full public folder access is granted;
	 *         otherwise <code>false</code>
	 */
	public boolean hasFullPublicFolderAccess() {
		return hasPermission(EDIT_PUBLIC_FOLDERS);
	}

	/**
	 * Set enableFullPublicFolderAccess
	 * 
	 * @param enableFullPublicFolderAccess
	 */
	public void setFullPublicFolderAccess(final boolean enableFullPublicFolderAccess) {
		setPermission(enableFullPublicFolderAccess, EDIT_PUBLIC_FOLDERS);
	}

	/**
	 * If this permission is not granted, neither folders are allowed to be
	 * shared nor shared folders are allowed to be seen by user. Existing
	 * permissions are not removed if user loses this right, but the display of
	 * shared folders is suppressed.
	 * 
	 * @return <code>true</code> full shared folder access is granted;
	 *         otherwise <code>false</code>
	 */
	public boolean hasFullSharedFolderAccess() {
		return hasPermission(READ_CREATE_SHARED_FOLDERS);
	}

	/**
	 * Set enableFullSharedFolderAccess
	 * 
	 * @param enableFullSharedFolderAccess
	 */
	public void setFullSharedFolderAccess(final boolean enableFullSharedFolderAccess) {
		setPermission(enableFullSharedFolderAccess, READ_CREATE_SHARED_FOLDERS);
	}

	/**
	 * @return <code>true</code> if user can delegate tasks; otherwise
	 *         <code>false</code>
	 */
	public boolean canDelegateTasks() {
		return hasPermission(DELEGATE_TASKS);
	}

	/**
	 * Sets enableDelegateTasks
	 * 
	 * @param enableDelegateTasks
	 */
	public void setDelegateTasks(final boolean enableDelegateTasks) {
		setPermission(enableDelegateTasks, DELEGATE_TASKS);
	}

	private boolean hasPermission(final int permission) {
		return (permissionBits & permission) == permission;
	}

	private void setPermission(final boolean enable, final int permission) {
		/*
		 * Set or unset specified permission
		 */
		permissionBits = enable ? (permissionBits | permission) : (permissionBits & ~permission);
	}

	/**
	 * @return The user ID
	 */
	public int getUserId() {
		return userId;
	}

	/**
	 * @return The group IDs
	 */
	public int[] getGroups() {
		if (null == groups) {
			return null;
		}
		final int[] clone = new int[groups.length];
		System.arraycopy(groups, 0, clone, 0, clone.length);
		return clone;
	}

	/**
	 * @return The context
	 */
	public Context getContext() {
		return ctx;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return new StringBuilder().append("UserConfiguration_").append(userId).append('@').append(
				Integer.toBinaryString(permissionBits)).toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.delete.DeleteListener#deletePerformed(com.openexchange.groupware.delete.DeleteEvent,
	 *      java.sql.Connection, java.sql.Connection)
	 */
	public void deletePerformed(final DeleteEvent delEvent, final Connection readConArg, final Connection writeConArg)
			throws DeleteFailedException {
		if (delEvent.getType() == DeleteEvent.TYPE_USER) {
			try {
				/*
				 * Delete user configuration
				 */
				RdbUserConfigurationStorage.deleteUserConfiguration(delEvent.getId(), writeConArg, delEvent
						.getContext());
			} catch (final SQLException e) {
				LOG.error(e.getMessage(), e);
				throw new DeleteFailedException(DeleteFailedException.Code.SQL_ERROR, e, e.getLocalizedMessage());
			} catch (final DBPoolingException e) {
				LOG.error(e.getMessage(), e);
				throw new DeleteFailedException(e);
			}
		}
	}
}

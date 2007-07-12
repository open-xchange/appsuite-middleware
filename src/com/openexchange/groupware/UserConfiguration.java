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

package com.openexchange.groupware;

import static com.openexchange.tools.oxfolder.OXFolderManagerImpl.getUserName;
import static com.openexchange.tools.oxfolder.OXFolderManagerImpl.folderModule2String;

import static com.openexchange.tools.sql.DBUtils.closeResources;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.ajax.spellcheck.AJAXUserDictionary;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.UserConfigurationException.UserConfigurationCode;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.ContextImpl;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteFailedException;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.groupware.imap.UserSettingMail;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.server.DBPool;
import com.openexchange.server.DBPoolingException;
import com.openexchange.tools.oxfolder.OXFolderException;
import com.openexchange.tools.oxfolder.OXFolderException.FolderCode;

/**
 * <ul>
 * <li>All members of a certain context MUST have the same version; neither
 * standard or premium</li>
 * <li>A user who obtained the standard version has no group functionality such
 * as participants, sharing of folders. Moreover he is not allowed to "see"
 * public folders and may only see the public root folder "public"</li>
 * <li>A user who loses premium version functionalties will only has read and
 * delete access to public folder's objects. Furthermore shared folders will no
 * longer be visible. <i>Note: NO data will be deleted through a downgrade!</i></li>
 * <li>A user with the premium version has no quota limitations</li>
 * </ul>
 * 
 * @author Thorben Betten
 */
public final class UserConfiguration implements Serializable, DeleteListener, Cloneable {

	private static final long serialVersionUID = -8277899698366715803L;

	private static final transient Log LOG = LogFactory.getLog(UserConfiguration.class);

	private static final String LOAD_USER_CONFIGURATION = "SELECT permissions FROM user_configuration WHERE cid = ? AND user = ?";

	private static final String INSERT_USER_CONFIGURATION = "INSERT INTO user_configuration (cid, user, permissions) VALUES (?, ?, ?)";

	private static final String UPDATE_USER_CONFIGURATION = "UPDATE user_configuration SET permissions = ? WHERE cid = ? AND user = ?";

	private static final String DELETE_USER_CONFIGURATION = "DELETE FROM user_configuration WHERE cid = ? AND user = ?";

	private static final String SELECT_MYINFOSTORE_PERMISSION = "SELECT ot.fuid, op.admin_flag FROM oxfolder_permissions AS op "
			+ "JOIN oxfolder_tree AS ot ON op.fuid = ot.fuid AND op.cid = ? AND ot.cid = ? "
			+ "WHERE ot.created_from = ? AND ot.module = ? AND ot.default_flag = ?";

	private static final String UPDATE_MYINFOSTORE_PERMISSION = "UPDATE oxfolder_permissions SET admin_flag = 1 "
			+ "WHERE cid = ? AND permission_id = ? AND fuid = ?";

	private static final int WEBMAIL = 1;

	private static final int CALENDAR = 2;

	private static final int CONTACTS = 4;

	private static final int TASKS = 8;

	private static final int INFOSTORE = 16;

	private static final int PROJECTS = 32;

	private static final int FORUM = 64;

	private static final int PINBOARD_WRITE_ACCESS = 128;

	private static final int WEBDAV_XML = 256;

	private static final int WEBDAV = 512;

	private static final int ICAL = 1024;

	private static final int VCARD = 2048;

	private static final int RSS_BOOKMARKS = 4096;

	private static final int RSS_PORTAL = 8192;

	private static final int MOBILITY = 16384;

	private static final int EDIT_PUBLIC_FOLDERS = 32768;

	private static final int READ_CREATE_SHARED_FOLDERS = 65536;

	private static final int DELEGATE_TASKS = 131072;

	/**
	 * Permission bits for standard users
	 */
	private static final int STANDARD_VERSION = 31;

	/**
	 * Permission bits for premium users
	 */
	private static final int PREMIUM_VERSION = Integer.MAX_VALUE;

	private int permissionBits;

	private final int userId;

	private int[] groups;

	private final transient Context ctx;

	private transient UserSettingMail userSettingMail;

	private transient AJAXUserDictionary userDictionary;

	private int[] accessibleModules;

	private boolean accessibleModulesComputed;

	public UserConfiguration() {
		super();
		userId = -1;
		ctx = null;
		groups = null;
	}

	public UserConfiguration(final int permissionBits, final int userId, final int[] groups, final Context ctx) {
		super();
		this.permissionBits = permissionBits;
		this.userId = userId;
		this.groups = new int[groups.length];
		System.arraycopy(groups, 0, this.groups, 0, groups.length);
		this.ctx = ctx;
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
			if (userSettingMail != null) {
				clone.userSettingMail = (UserSettingMail) userSettingMail.clone();
			}
			if (userDictionary != null) {
				clone.userDictionary = (AJAXUserDictionary) userDictionary.clone();
			}
			return clone;
		} catch (final CloneNotSupportedException e) {
			LOG.error(e.getMessage(), e);
			throw new InternalError(e.getMessage());
		}
	}

	public boolean isStandardVersion() {
		return permissionBits == STANDARD_VERSION;
	}

	public void setStandardVersion() {
		this.permissionBits = STANDARD_VERSION;
		downgradeUser();
	}

	public void setPremiumVersion() throws OXException {
		this.permissionBits = PREMIUM_VERSION;
		upgradeUser();
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
	 * @return a sorted array of <tt>int</tt> representing user's accessible
	 *         modules
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
		array.append(FolderObject.SYSTEM_MODULE);
		array.append(FolderObject.UNBOUND);
		accessibleModulesComputed = true;
		accessibleModules = array.toArray();
		Arrays.sort(accessibleModules);
		return cloneAccessibleModules();
	}

	private final int[] cloneAccessibleModules() {
		final int[] clone = new int[accessibleModules.length];
		System.arraycopy(accessibleModules, 0, clone, 0, clone.length);
		return clone;
	}

	/**
	 * If this permission is not granted, it's prohibited for the user to create
	 * or edit public folders. Existing public folders are visible in any case.
	 */
	public boolean hasFullPublicFolderAccess() {
		return hasPermission(EDIT_PUBLIC_FOLDERS);
	}

	public void setFullPublicFolderAccess(final boolean enableFullPublicFolderAccess) {
		setPermission(enableFullPublicFolderAccess, EDIT_PUBLIC_FOLDERS);
	}

	/**
	 * If this permission is not granted, neither folders are allowed to shared
	 * nor shared folders are allowed to be seen by user. Existing permissions
	 * are not removed if user loses this right, but the display of shared
	 * folders is suppressed.
	 * 
	 * @return
	 */
	public boolean hasFullSharedFolderAccess() {
		return hasPermission(READ_CREATE_SHARED_FOLDERS);
	}

	public void setFullSharedFolderAccess(final boolean enableFullSharedFolderAccess) {
		setPermission(enableFullSharedFolderAccess, READ_CREATE_SHARED_FOLDERS);
	}

	public boolean canDelegateTasks() {
		return hasPermission(DELEGATE_TASKS);
	}

	public void setDelegateTasks(final boolean enableDelegateTasks) {
		setPermission(enableDelegateTasks, DELEGATE_TASKS);
	}

	private boolean hasPermission(final int permission) {
		return (permissionBits & permission) == permission;
	}

	private void setPermission(final boolean enable, final int permission) {
		/*
		 * Bitwise OR if enable and permission not set, yet. Otherwise bitwise
		 * XOR if permission is already set and enable is false.
		 */
		permissionBits = enable && !hasPermission(permission) ? (permissionBits | permission) : (!enable
				&& hasPermission(permission) ? (permissionBits ^ permission) : permissionBits);
	}

	public int getUserId() {
		return userId;
	}

	public int[] getGroups() {
		if (null == groups) {
			return null;
		}
		final int[] clone = new int[groups.length];
		System.arraycopy(groups, 0, clone, 0, clone.length);
		return clone;
	}

	public Context getContext() {
		return ctx;
	}

	private static final transient Lock LOCK = new ReentrantLock();

	public UserSettingMail getUserSettingMail() {
		if (userSettingMail == null) {
			LOCK.lock();
			try {
				if (userSettingMail == null) {
					final UserSettingMail tmp = new UserSettingMail();
					try {
						tmp.loadUserSettingMail(userId, ctx);
					} catch (final OXException e) {
						LOG.error(e.getMessage(), e);
					}
					userSettingMail = tmp;
				}
			} finally {
				LOCK.unlock();
			}
		}
		return userSettingMail;
	}

	public void setUserSettingMail(final UserSettingMail imapUserSetting) {
		setUserSettingMail(imapUserSetting, false);
	}

	public void setUserSettingMail(final UserSettingMail imapUserSetting, final boolean save) {
		this.userSettingMail = imapUserSetting;
		if (save && this.userSettingMail != null) {
			try {
				this.userSettingMail.saveUserSettingMail(userId, ctx);
			} catch (final OXException e) {
				LOG.error(e.getMessage(), e);
			}
		}
	}

	public AJAXUserDictionary getUserDictionary() throws OXException {
		try {
			if (userDictionary == null) {
				userDictionary = new AJAXUserDictionary(userId, ctx);
				if (!userDictionary.loadUserDictionary()) {
					userDictionary.saveUserDictionary();
				}
			}
			return userDictionary;
		} catch (final DBPoolingException e) {
			throw new UserConfigurationException(UserConfigurationCode.DBPOOL_ERROR, e, new Object[0]);
		} catch (final SQLException e) {
			throw new UserConfigurationException(UserConfigurationCode.SQL_ERROR, e, new Object[0]);
		}
	}

	public void setUserDictionary(final AJAXUserDictionary userDictionary) {
		try {
			setUserDictionary(userDictionary, false);
		} catch (final OXException e) {
			/*
			 * Cannot occur since we call with save set to false
			 */
			LOG.error(e.getMessage(), e);
		}
	}

	public void setUserDictionary(final AJAXUserDictionary userDictionary, final boolean save) throws OXException {
		this.userDictionary = userDictionary;
		if (save && this.userDictionary != null) {
			try {
				this.userDictionary.saveUserDictionary();
			} catch (final SQLException e) {
				throw new UserConfigurationException(UserConfigurationCode.SQL_ERROR, e, new Object[0]);
			} catch (final DBPoolingException e) {
				throw new UserConfigurationException(UserConfigurationCode.DBPOOL_ERROR, e, new Object[0]);
			}
		}
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

	/**
	 * This method performs some necessary changes if a user is downgraded to a
	 * standard user.
	 */
	private void downgradeUser() {
		/*
		 * TODO: Some changes due to user's downgrade to standard version
		 */
	}

	/**
	 * This method performs some necessary changes if a user becomes a premium
	 * user.
	 */
	private void upgradeUser() throws OXException {
		Connection readCon = null;
		Connection writeCon = null;
		try {
			readCon = DBPool.pickup(ctx);
			writeCon = DBPool.pickupWriteable(ctx);
			/*
			 * Add admin permission to user's MyInfostore folder
			 */
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				stmt = readCon.prepareStatement(SELECT_MYINFOSTORE_PERMISSION);
				stmt.setInt(1, ctx.getContextId());
				stmt.setInt(2, ctx.getContextId());
				stmt.setInt(3, userId);
				stmt.setInt(4, FolderObject.INFOSTORE);
				stmt.setInt(5, 1);
				rs = stmt.executeQuery();
				if (rs.next()) {
					final int fuid = rs.getInt(1);
					final boolean adminFlag = (rs.getInt(2) > 0);
					if (!adminFlag) {
						rs.close();
						stmt.close();
						stmt = writeCon.prepareStatement(UPDATE_MYINFOSTORE_PERMISSION);
						stmt.setInt(1, ctx.getContextId());
						stmt.setInt(2, userId);
						stmt.setInt(3, fuid);
						stmt.executeUpdate();
					}
				} else {
					throw new OXFolderException(FolderCode.NO_DEFAULT_FOLDER_FOUND,
							folderModule2String(FolderObject.INFOSTORE), getUserName(userId, ctx), Integer.valueOf(ctx
									.getContextId()));
				}
			} finally {
				closeResources(rs, stmt, readCon, true, ctx);
				if (writeCon != null) {
					DBPool.pushWrite(ctx, writeCon);
					writeCon = null;
				}
			}
		} catch (final SQLException e) {
			throw new UserConfigurationException(UserConfigurationCode.SQL_ERROR, e, new Object[0]);
		} catch (final DBPoolingException e) {
			throw new UserConfigurationException(UserConfigurationCode.DBPOOL_ERROR, e, new Object[0]);
		}
	}

	private static final String SQL_SELECT = "SELECT user FROM user_configuration WHERE cid = ? AND user = ?";

	public static void saveUserConfiguration(final UserConfiguration userConfig) throws OXException {
		boolean insert = false;
		try {
			Connection readCon = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				readCon = DBPool.pickup(userConfig.getContext());
				stmt = readCon.prepareStatement(SQL_SELECT);
				stmt.setInt(1, userConfig.getContext().getContextId());
				stmt.setInt(2, userConfig.userId);
				rs = stmt.executeQuery();
				insert = !rs.next();
			} finally {
				closeResources(rs, stmt, readCon, true, userConfig.getContext());
			}
			saveUserConfiguration(userConfig, insert, userConfig.getContext(), null);
		} catch (final SQLException e) {
			throw new UserConfigurationException(UserConfigurationCode.SQL_ERROR, e, new Object[0]);
		} catch (final DBPoolingException e) {
			throw new UserConfigurationException(UserConfigurationCode.DBPOOL_ERROR, e, new Object[0]);
		}
	}

	public static void saveUserConfiguration(final UserConfiguration userConfig, final boolean insert,
			final Connection writeCon) throws SQLException, DBPoolingException {
		saveUserConfiguration(userConfig, insert, userConfig.getContext(), writeCon);
	}

	public static void saveUserConfiguration(final UserConfiguration userConfig, final boolean insert,
			final Context ctx, final Connection writeConArg) throws SQLException, DBPoolingException {
		Connection writeCon = writeConArg;
		boolean closeConnection = false;
		PreparedStatement stmt = null;
		try {
			if (writeCon == null) {
				writeCon = DBPool.pickupWriteable(ctx);
				closeConnection = true;
			}
			if (insert) {
				stmt = writeCon.prepareStatement(INSERT_USER_CONFIGURATION);
				stmt.setInt(1, userConfig.ctx.getContextId());
				stmt.setInt(2, userConfig.userId);
				stmt.setInt(3, userConfig.permissionBits);
			} else {
				stmt = writeCon.prepareStatement(UPDATE_USER_CONFIGURATION);
				stmt.setInt(1, userConfig.permissionBits);
				stmt.setInt(2, userConfig.ctx.getContextId());
				stmt.setInt(3, userConfig.userId);
			}
			stmt.executeUpdate();
			if (!insert) {
				try {
					UserConfigurationStorage.getInstance().removeUserConfiguration(userConfig.userId, userConfig.ctx);
				} catch (UserConfigurationException e) {
					LOG.warn("User Configuration could not be removed from cache", e);
				}
			}
		} finally {
			closeResources(null, stmt, closeConnection ? writeCon : null, false, ctx);
		}
	}

	public static UserConfiguration loadUserConfiguration(final int userId, final int[] groups, final int cid,
			final Connection readConArg) throws SQLException, DBPoolingException {
		final Context ctx = new ContextImpl(cid);
		Connection readCon = readConArg;
		boolean closeReadCon = false;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			if (readCon == null) {
				readCon = DBPool.pickup(ctx);
				closeReadCon = true;
			}
			stmt = readCon.prepareStatement(LOAD_USER_CONFIGURATION);
			stmt.setInt(1, ctx.getContextId());
			stmt.setInt(2, userId);
			rs = stmt.executeQuery();
			if (rs.next()) {
				return new UserConfiguration(rs.getInt(1), userId, groups, ctx);
			}
			return new UserConfiguration(0, userId, groups, ctx);
		} finally {
			closeResources(rs, stmt, closeReadCon ? readCon : null, true, ctx);
		}
	}

	public static UserConfiguration loadUserConfiguration(final int userId, final int[] groupsArg, final Context ctx,
			final Connection readConArg) throws SQLException, LdapException, DBPoolingException, OXException {
		int[] groups = groupsArg;
		Connection readCon = readConArg;
		boolean closeCon = false;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			if (readCon == null) {
				readCon = DBPool.pickup(ctx);
				closeCon = true;
			}
			stmt = readCon.prepareStatement(LOAD_USER_CONFIGURATION);
			stmt.setInt(1, ctx.getContextId());
			stmt.setInt(2, userId);
			rs = stmt.executeQuery();
			if (rs.next()) {
				if (groups == null) {
					final UserStorage uStorage = UserStorage.getInstance(ctx);
					groups = uStorage.getUser(userId).getGroups();
				}
				return new UserConfiguration(rs.getInt(1), userId, groups, ctx);
			}
			throw new UserConfigurationException(UserConfigurationCode.NOT_FOUND, Integer.valueOf(userId), Integer
					.valueOf(ctx.getContextId()));
		} finally {
			closeResources(rs, stmt, closeCon ? readCon : null, true, ctx);
		}
	}

	public static UserConfiguration loadUserConfiguration(final int userId, final int[] groups, final Context ctx)
			throws SQLException, LdapException, DBPoolingException, OXException {
		return loadUserConfiguration(userId, groups, ctx, null);
	}

	public static UserConfiguration loadUserConfiguration(final int userId, final Context ctx) throws SQLException,
			LdapException, DBPoolingException, OXException {
		return loadUserConfiguration(userId, null, ctx, null);
	}

	public static void deleteUserConfiguration(final int userId, final Context ctx) throws SQLException,
			DBPoolingException {
		deleteUserConfiguration(userId, null, ctx);
	}

	public static void deleteUserConfiguration(final int userId, final Connection writeConArg, final Context ctx)
			throws SQLException, DBPoolingException {
		Connection writeCon = writeConArg;
		boolean closeWriteCon = false;
		PreparedStatement stmt = null;
		try {
			if (writeCon == null) {
				writeCon = DBPool.pickupWriteable(ctx);
				closeWriteCon = true;
			}
			stmt = writeCon.prepareStatement(DELETE_USER_CONFIGURATION);
			stmt.setInt(1, ctx.getContextId());
			stmt.setInt(2, userId);
			stmt.executeUpdate();
			try {
				UserConfigurationStorage.getInstance().removeUserConfiguration(userId, ctx);
			} catch (UserConfigurationException e) {
				LOG.warn("User Configuration could not be removed from cache", e);
			}
		} finally {
			closeResources(null, stmt, closeWriteCon ? writeCon : null, false, ctx);
		}
	}

	private static class SmartIntArray {
		/**
		 * Pointer to keep track of position in the array
		 */
		private int pointer;

		private int[] array;

		private final int growthSize;

		public SmartIntArray() {
			this(1024);
		}

		public SmartIntArray(final int initialSize) {
			this(initialSize, (initialSize / 4));
		}

		public SmartIntArray(final int initialSize, final int growthSize) {
			this.growthSize = growthSize;
			array = new int[initialSize];
		}

		public SmartIntArray append(final int i) {
			if (pointer >= array.length) {
				/*
				 * time to grow!
				 */
				final int[] tmpArray = new int[array.length + growthSize];
				System.arraycopy(array, 0, tmpArray, 0, array.length);
				array = tmpArray;
			}
			array[pointer++] = i;
			return this;
		}

		public int[] toArray() {
			final int[] trimmedArray = new int[pointer];
			System.arraycopy(array, 0, trimmedArray, 0, trimmedArray.length);
			return trimmedArray;
		}
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
				final Context ctx = delEvent.getContext();
				final int userId = delEvent.getId();
				/*
				 * Delete user configuration
				 */
				deleteUserConfiguration(userId, writeConArg, ctx);
			} catch (final Exception e) {
				LOG.error(e.getMessage(), e);
				throw new DeleteFailedException(e);
			}
		}
	}
}

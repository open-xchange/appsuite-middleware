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

package com.openexchange.imap;

import static com.openexchange.mail.MailInterfaceImpl.mailInterfaceMonitor;
import static com.openexchange.mail.dataobjects.MailFolder.DEFAULT_FOLDER_ID;
import static com.openexchange.mail.utils.StorageUtility.getDefaultFolderNames;
import static com.openexchange.mail.utils.StorageUtility.prepareFullname;
import static com.openexchange.mail.utils.StorageUtility.prepareMailFolderParam;
import static java.util.regex.Matcher.quoteReplacement;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.MethodNotSupportedException;
import javax.mail.ReadOnlyFolderException;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.imap.cache.NamespaceFoldersCache;
import com.openexchange.imap.cache.RightsCache;
import com.openexchange.imap.cache.UserFlagsCache;
import com.openexchange.imap.command.CopyIMAPCommand;
import com.openexchange.imap.command.FlagsIMAPCommand;
import com.openexchange.imap.config.IMAPConfig;
import com.openexchange.imap.converters.IMAPFolderConverter;
import com.openexchange.imap.user2acl.User2ACL;
import com.openexchange.imap.user2acl.User2ACLArgs;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailFolderStorage;
import com.openexchange.mail.MailSessionParameterNames;
import com.openexchange.mail.config.MailConfigException;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.mail.utils.StorageUtility;
import com.openexchange.server.OCLPermission;
import com.openexchange.sessiond.impl.SessionObject;
import com.sun.mail.iap.CommandFailedException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.ACL;
import com.sun.mail.imap.DefaultFolder;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.Rights;

/**
 * {@link IMAPFolderStorage} - The IMAP folder storage implementation
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class IMAPFolderStorage implements MailFolderStorage, Serializable {

	private static final String ERR_IDS_NOT_SUPPORTED = "Numeric folder IDs not supported by IMAP";

	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = -7945316079728160239L;

	private static final transient org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(IMAPFolderStorage.class);

	private static final String STR_INBOX = "INBOX";

	private final transient IMAPStore imapStore;

	private final transient IMAPConnection imapMailConnection;

	private final transient SessionObject session;

	private final transient IMAPConfig imapConfig;

	public IMAPFolderStorage(final IMAPStore imapStore, final IMAPConnection imapMailConnection,
			final SessionObject session) throws MailException {
		super();
		this.imapStore = imapStore;
		this.imapMailConnection = imapMailConnection;
		this.session = session;
		this.imapConfig = (IMAPConfig) imapMailConnection.getMailConfig();
	}

	public boolean exists(final String fullnameArg) throws MailException {
		try {
			final String fullname = prepareMailFolderParam(fullnameArg);
			if (DEFAULT_FOLDER_ID.equals(fullname)) {
				return true;
			}
			if (imapStore.getFolder(fullname).exists()) {
				return true;
			}
			return (checkForNamespaceFolder(fullname) != null);
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapMailConnection);
		}
	}

	public boolean exists(final long id) throws IMAPException {
		throw new IllegalStateException(ERR_IDS_NOT_SUPPORTED);
	}

	public MailFolder getFolder(final String fullnameArg) throws MailException {
		try {
			final String fullname = prepareMailFolderParam(fullnameArg);
			if (DEFAULT_FOLDER_ID.equals(fullname)) {
				return IMAPFolderConverter
						.convertFolder((IMAPFolder) imapStore.getDefaultFolder(), session, imapConfig);
			}
			IMAPFolder f = (IMAPFolder) imapStore.getFolder(fullname);
			if (f.exists()) {
				return IMAPFolderConverter.convertFolder(f, session, imapConfig);
			}
			f = checkForNamespaceFolder(fullname);
			if (null == f) {
				throw new IMAPException(IMAPException.Code.FOLDER_NOT_FOUND, fullname);
			}
			return IMAPFolderConverter.convertFolder(f, session, imapConfig);
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapMailConnection);
		}
	}

	public MailFolder getFolder(final long id) {
		throw new IllegalStateException(ERR_IDS_NOT_SUPPORTED);
	}

	private static final String PATTERN_ALL = "%";

	public MailFolder[] getSubfolders(final String parentFullnameArg, final boolean all) throws MailException {
		try {
			final String parentFullname = prepareMailFolderParam(parentFullnameArg);
			IMAPFolder parent;
			if (DEFAULT_FOLDER_ID.equals(parentFullname)) {
				parent = (IMAPFolder) imapStore.getDefaultFolder();
				final boolean subscribed = (!IMAPConfig.isIgnoreSubscription() && !all);
				/*
				 * Request subfolders the usual way
				 */
				final List<Folder> subfolders = new ArrayList<Folder>();
				{
					final IMAPFolder[] childFolders;
					final long start = System.currentTimeMillis();
					if (subscribed) {
						childFolders = (IMAPFolder[]) parent.listSubscribed(PATTERN_ALL);
						mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
					} else {
						childFolders = (IMAPFolder[]) parent.list(PATTERN_ALL);
						mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
					}
					subfolders.addAll(Arrays.asList(childFolders));
				}
				/*
				 * Merge with namespace folders
				 */
				{
					final List<Folder> personalNs = new ArrayList<Folder>(Arrays.asList(NamespaceFoldersCache
							.getPersonalNamespaces(imapStore, true, session)));
					mergeWithNamespaceFolders(subfolders, personalNs, subscribed);
				}
				{
					final List<Folder> otherNs = new ArrayList<Folder>(Arrays.asList(NamespaceFoldersCache
							.getUserNamespaces(imapStore, true, session)));
					mergeWithNamespaceFolders(subfolders, otherNs, subscribed);
				}
				{
					final List<Folder> sharedNs = new ArrayList<Folder>(Arrays.asList(NamespaceFoldersCache
							.getSharedNamespaces(imapStore, true, session)));
					mergeWithNamespaceFolders(subfolders, sharedNs, subscribed);
				}
				/*
				 * Output subfolders
				 */
				final List<MailFolder> list = new ArrayList<MailFolder>(subfolders.size());
				for (Folder subfolder : subfolders) {
					final MailFolder mo = IMAPFolderConverter
							.convertFolder((IMAPFolder) subfolder, session, imapConfig);
					list.add(mo);
				}
				return list.toArray(new MailFolder[list.size()]);
			}
			parent = (IMAPFolder) imapStore.getFolder(parentFullname);
			if (parent.exists()) {
				/*
				 * Holds LOOK-UP right?
				 */
				if (imapConfig.isSupportsACLs() && ((parent.getType() & IMAPFolder.HOLDS_MESSAGES) > 0)) {
					try {
						if (!RightsCache.getCachedRights(parent, true, session).contains(Rights.Right.LOOKUP)) {
							throw new IMAPException(IMAPException.Code.NO_LOOKUP_ACCESS, parentFullname);
						}
					} catch (final MessagingException e) {
						throw new IMAPException(IMAPException.Code.NO_ACCESS, parentFullname);
					}
				}
				return getSubfolderArray(all, parent);
			}
			/*
			 * Check for namespace folder
			 */
			parent = checkForNamespaceFolder(parentFullname);
			if (null != parent) {
				return getSubfolderArray(all, parent);
			}
			return EMPTY_PATH;
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapMailConnection);
		}
	}

	private MailFolder[] getSubfolderArray(final boolean all, final IMAPFolder parent) throws MailConfigException,
			MessagingException, MailException {
		final Folder[] subfolders;
		if (IMAPConfig.isIgnoreSubscription() || all) {
			subfolders = parent.list(PATTERN_ALL);
		} else {
			subfolders = parent.listSubscribed(PATTERN_ALL);
		}
		final List<MailFolder> list = new ArrayList<MailFolder>(subfolders.length);
		for (int i = 0; i < subfolders.length; i++) {
			final MailFolder mo = IMAPFolderConverter.convertFolder((IMAPFolder) subfolders[i], session, imapConfig);
			if (mo.exists()) {
				list.add(mo);
			}
		}
		return list.toArray(new MailFolder[list.size()]);
	}

	private static void mergeWithNamespaceFolders(final List<Folder> subfolders, final List<Folder> namespaceFolders,
			final boolean subscribed) {
		NextNSFolder: for (final Iterator<Folder> iter = namespaceFolders.iterator(); iter.hasNext();) {
			final String nsFullname = iter.next().getFullName();
			if (nsFullname == null || nsFullname.length() == 0) {
				iter.remove();
				continue NextNSFolder;
			}
			for (Folder subfolder : subfolders) {
				if (nsFullname.equals(subfolder.getFullName())) {
					/*
					 * Namespace folder already contained in subfolder list
					 */
					iter.remove();
					continue NextNSFolder;
				}
			}
		}
		if (subscribed) {
			for (final Iterator<Folder> iter = namespaceFolders.iterator(); iter.hasNext();) {
				if (!iter.next().isSubscribed()) {
					iter.remove();
				}
			}
		}
		subfolders.addAll(namespaceFolders);
	}

	/**
	 * Checks if given fullname matches a namespace folder
	 * 
	 * @param fullname
	 *            The folder's fullname
	 * @return The corresponding namespace folder or <code>null</code>
	 * @throws MessagingException
	 */
	private IMAPFolder checkForNamespaceFolder(final String fullname) throws MessagingException {
		/*
		 * Check for namespace folder
		 */
		IMAPFolder retval = null;
		{
			final Folder[] personalFolders = NamespaceFoldersCache.getPersonalNamespaces(imapStore, true, session);
			for (int i = 0; i < personalFolders.length; i++) {
				if (personalFolders[i].getFullName().equals(fullname)) {
					retval = new NamespaceFolder(imapStore, fullname, personalFolders[i].getSeparator());
					break;
				}
			}
		}
		{
			final Folder[] userFolders = NamespaceFoldersCache.getUserNamespaces(imapStore, true, session);
			for (int i = 0; i < userFolders.length; i++) {
				if (userFolders[i].getFullName().equals(fullname)) {
					retval = new NamespaceFolder(imapStore, fullname, userFolders[i].getSeparator());
					break;
				}
			}
		}
		{
			final Folder[] sharedFolders = NamespaceFoldersCache.getSharedNamespaces(imapStore, true, session);
			for (int i = 0; i < sharedFolders.length; i++) {
				if (sharedFolders[i].getFullName().equals(fullname)) {
					retval = new NamespaceFolder(imapStore, fullname, sharedFolders[i].getSeparator());
					break;
				}
			}
		}
		return retval;
	}

	public MailFolder[] getSubfolders(final long parentId, final boolean all) {
		throw new IllegalStateException(ERR_IDS_NOT_SUPPORTED);
	}

	public MailFolder getRootFolder() throws MailException {
		try {
			return IMAPFolderConverter.convertFolder((IMAPFolder) imapStore.getDefaultFolder(), session, imapConfig);
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapMailConnection);
		}
	}

	private boolean isDefaultFoldersChecked() {
		final Boolean b = (Boolean) session.getParameter(MailSessionParameterNames.PARAM_DEF_FLD_FLAG);
		return b != null && b.booleanValue();
	}

	private void setDefaultFoldersChecked(final boolean checked) {
		session.setParameter(MailSessionParameterNames.PARAM_DEF_FLD_FLAG, Boolean.valueOf(checked));
	}

	private void setDefaultMailFolder(final int index, final String fullname) {
		String[] arr = (String[]) session.getParameter(MailSessionParameterNames.PARAM_DEF_FLD_ARR);
		if (null == arr) {
			arr = new String[6];
			session.setParameter(MailSessionParameterNames.PARAM_DEF_FLD_ARR, arr);
		}
		arr[index] = fullname;
	}

	public void checkDefaultFolders() throws MailException {
		if (!isDefaultFoldersChecked()) {
			synchronized (session) {
				try {
					if (isDefaultFoldersChecked()) {
						return;
					}
					/*
					 * Get INBOX folder
					 */
					final Folder inboxFolder = imapStore.getFolder(STR_INBOX);
					if (!inboxFolder.exists()) {
						throw new IMAPException(IMAPException.Code.FOLDER_NOT_FOUND, STR_INBOX);
					}
					if (!inboxFolder.isSubscribed()) {
						/*
						 * Subscribe INBOX folder
						 */
						inboxFolder.setSubscribed(true);
					}
					final boolean noInferiors = ((inboxFolder.getType() & Folder.HOLDS_FOLDERS) == 0);
					final StringBuilder tmp = new StringBuilder(128);
					/*
					 * Determine where to create default folders and store as a
					 * prefix for folder fullname
					 */
					if (!noInferiors
							&& (!isAltNamespaceEnabled(imapStore) || IMAPConfig
									.isAllowNestedDefaultFolderOnAltNamespace())) {
						/*
						 * Only allow default folder below INBOX if inferiors
						 * are permitted and either altNamespace is disabled or
						 * nested default folder are explicitly allowed
						 */
						tmp.append(inboxFolder.getFullName()).append(inboxFolder.getSeparator());
					}
					final String prefix = tmp.toString();
					tmp.setLength(0);
					final int type = (Folder.HOLDS_MESSAGES | Folder.HOLDS_FOLDERS);
					/*
					 * Check default folders
					 */
					final String[] defaultFolderNames = getDefaultFolderNames(UserSettingMailStorage.getInstance()
							.getUserSettingMail(session.getUserId(), session.getContext()));
					for (int i = 0; i < defaultFolderNames.length; i++) {
						setDefaultMailFolder(i, checkDefaultFolder(prefix, defaultFolderNames[i], type, tmp));
					}
					setDefaultFoldersChecked(true);
				} catch (final MessagingException e) {
					throw IMAPException.handleMessagingException(e, imapMailConnection);
				}
			}
		}
	}

	private static final int FOLDER_TYPE = (Folder.HOLDS_MESSAGES | Folder.HOLDS_FOLDERS);

	public String createFolder(final MailFolder toCreate) throws MailException {
		try {
			/*
			 * Insert
			 */
			final String parentFullname = prepareMailFolderParam(toCreate.getParentFullname());
			IMAPFolder parent = DEFAULT_FOLDER_ID.equals(parentFullname) ? (IMAPFolder) imapStore.getDefaultFolder()
					: (IMAPFolder) imapStore.getFolder(parentFullname);
			if (!parent.exists()) {
				parent = checkForNamespaceFolder(parentFullname);
				if (null == parent) {
					throw new IMAPException(IMAPException.Code.FOLDER_NOT_FOUND, parentFullname);
				}
			}
			if ((parent.getType() & Folder.HOLDS_FOLDERS) == 0) {
				throw new IMAPException(IMAPException.Code.FOLDER_DOES_NOT_HOLD_FOLDERS,
						parent instanceof DefaultFolder ? DEFAULT_FOLDER_ID : parentFullname);
			} else if (imapConfig.isSupportsACLs()) {
				try {
					if (!RightsCache.getCachedRights(parent, true, session).contains(Rights.Right.CREATE)) {
						throw new IMAPException(IMAPException.Code.NO_CREATE_ACCESS, parentFullname);
					}
				} catch (final MessagingException e) {
					throw new IMAPException(IMAPException.Code.NO_ACCESS, parentFullname);
				}
			}
			if (toCreate.getName().indexOf(parent.getSeparator()) != -1) {
				throw new IMAPException(IMAPException.Code.INVALID_FOLDER_NAME, Character
						.valueOf(parent.getSeparator()));
			}
			final IMAPFolder createMe = (IMAPFolder) imapStore.getFolder(new StringBuilder(parentFullname).append(
					parent.getSeparator()).append(toCreate.getName()).toString());
			if (createMe.exists()) {
				throw new IMAPException(IMAPException.Code.DUPLICATE_FOLDER, createMe.getFullName());
			}
			if (!createMe.create(FOLDER_TYPE)) {
				throw new IMAPException(IMAPException.Code.FOLDER_CREATION_FAILED, createMe.getFullName(),
						parent instanceof DefaultFolder ? DEFAULT_FOLDER_ID : parent.getFullName());
			}
			/*
			 * Subscribe
			 */
			IMAPCommandsCollection.forceSetSubscribed(imapStore, createMe.getFullName(), true);
			if (imapConfig.isSupportsACLs() && toCreate.containsPermissions()) {
				final ACL[] initialACLs = getACLSafe(createMe);
				if (initialACLs != null) {
					final ACL[] newACLs = permissions2ACL(toCreate.getPermissions(), createMe);
					if (!equals(initialACLs, newACLs)) {
						if (!createMe.myRights().contains(Rights.Right.ADMINISTER)) {
							throw new IMAPException(IMAPException.Code.NO_ADMINISTER_ACCESS_ON_INITIAL, createMe
									.getFullName());
						}
						boolean adminFound = false;
						for (int i = 0; i < newACLs.length && !adminFound; i++) {
							if (newACLs[i].getRights().contains(Rights.Right.ADMINISTER)) {
								adminFound = true;
							}
						}
						if (!adminFound) {
							throw new IMAPException(IMAPException.Code.NO_ADMIN_ACL, createMe.getFullName());
						}
						/*
						 * Apply new ACLs
						 */
						for (int i = 0; i < newACLs.length; i++) {
							createMe.addACL(newACLs[i]);
						}
						/*
						 * Remove other ACLs
						 */
						final ACL[] removedACLs = getRemovedACLs(newACLs, initialACLs);
						if (removedACLs.length > 0) {
							final UserStorage userStorage = UserStorage.getInstance(session.getContext());
							final User2ACL user2ACL = User2ACL.getInstance(UserStorage.getUser(session.getUserId(),
									session.getContext()));
							final User2ACLArgs user2ACLArgs = IMAPFolderConverter.getUser2AclArgs(session, createMe);
							for (int i = 0; i < removedACLs.length; i++) {
								if (isKnownEntity(removedACLs[i].getName(), user2ACL, userStorage, user2ACLArgs)) {
									createMe.removeACL(removedACLs[i].getName());
								}
							}
						}
					}
				}
			}
			return prepareFullname(createMe.getFullName(), createMe.getSeparator());
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapMailConnection);
		} catch (final AbstractOXException e) {
			throw new IMAPException(e);
		}
	}

	public String updateFolder(final String fullnameArg, final MailFolder toUpdate) throws MailException {
		try {
			final String fullname = prepareMailFolderParam(fullnameArg);
			IMAPFolder updateMe = (IMAPFolder) imapStore.getFolder(fullname);
			/*
			 * Check for move
			 */
			final String oldParent = updateMe.getParent().getFullName();
			final String newParent = prepareMailFolderParam(toUpdate.getParentFullname());
			final boolean move = toUpdate.containsParentFullname() && !newParent.equals(oldParent);
			/*
			 * Check for rename. Rename must not be performed if a move has
			 * already been done
			 */
			final String oldName = updateMe.getName();
			final String newName = toUpdate.getName();
			final boolean rename = (!move && toUpdate.containsName() && !newName.equalsIgnoreCase(oldName));
			if (move) {
				/*
				 * Perform move operation
				 */
				if (isDefaultFolder(updateMe.getFullName())) {
					throw new IMAPException(IMAPException.Code.NO_DEFAULT_FOLDER_UPDATE, updateMe.getFullName());
				}
				IMAPFolder destFolder = ((IMAPFolder) (DEFAULT_FOLDER_ID.equals(newParent) ? imapStore
						.getDefaultFolder() : imapStore.getFolder(newParent)));
				if (!destFolder.exists()) {
					destFolder = checkForNamespaceFolder(newParent);
					if (null == destFolder) {
						throw new IMAPException(IMAPException.Code.FOLDER_NOT_FOUND, newParent);
					}
				}
				if (destFolder instanceof DefaultFolder) {
					if ((destFolder.getType() & Folder.HOLDS_FOLDERS) == 0) {
						throw new IMAPException(IMAPException.Code.FOLDER_DOES_NOT_HOLD_FOLDERS, destFolder
								.getFullName());
					}
				} else if (imapConfig.isSupportsACLs() && (destFolder.getType() & Folder.HOLDS_MESSAGES) > 0) {
					try {
						if (!RightsCache.getCachedRights(destFolder, true, session).contains(Rights.Right.CREATE)) {
							throw new IMAPException(IMAPException.Code.NO_CREATE_ACCESS, newParent);
						}
					} catch (final MessagingException e) {
						LOG.error(e.getMessage(), e);
						throw new IMAPException(IMAPException.Code.NO_ACCESS, newParent);
					}
				}
				if (destFolder.getFullName().startsWith(updateMe.getFullName())) {
					throw new IMAPException(IMAPException.Code.NO_MOVE_TO_SUBFLD, updateMe.getName(), destFolder
							.getName());
				}
				updateMe = moveFolder(updateMe, destFolder, newName);
			}
			/*
			 * Is rename operation?
			 */
			if (rename) {
				/*
				 * Perform rename operation
				 */
				if (isDefaultFolder(updateMe.getFullName())) {
					throw new IMAPException(IMAPException.Code.NO_DEFAULT_FOLDER_UPDATE, updateMe.getFullName());
				} else if (imapConfig.isSupportsACLs() && ((updateMe.getType() & Folder.HOLDS_MESSAGES) > 0)) {
					try {
						if (!RightsCache.getCachedRights(updateMe, true, session).contains(Rights.Right.CREATE)) {
							throw new IMAPException(IMAPException.Code.NO_CREATE_ACCESS, updateMe.getFullName());
						}
					} catch (final MessagingException e) {
						throw new IMAPException(IMAPException.Code.NO_ACCESS, updateMe.getFullName());
					}
				}
				/*
				 * Rename can only be invoked on a closed folder
				 */
				if (updateMe.isOpen()) {
					updateMe.close(false);
				}
				final IMAPFolder renameFolder;
				{
					final String parentFullName = updateMe.getParent().getFullName();
					final StringBuilder tmp = new StringBuilder();
					if (parentFullName.length() > 0) {
						tmp.append(parentFullName).append(updateMe.getSeparator());
					}
					tmp.append(toUpdate.getName());
					renameFolder = (IMAPFolder) imapStore.getFolder(tmp.toString());
				}
				if (renameFolder.exists()) {
					throw new IMAPException(IMAPException.Code.DUPLICATE_FOLDER, renameFolder.getFullName());
				}
				/*
				 * Remember subscription status
				 */
				Map<String, Boolean> subscriptionStatus;
				final String newFullName = renameFolder.getFullName();
				final String oldFullName = updateMe.getFullName();
				try {
					subscriptionStatus = getSubscriptionStatus(updateMe, oldFullName, newFullName);
				} catch (final MessagingException e) {
					if (LOG.isWarnEnabled()) {
						LOG.warn(new StringBuilder(128).append("Subscription status of folder \"").append(
								updateMe.getFullName()).append(
								"\" and its subfolders could not be stored prior to rename operation"));
					}
					subscriptionStatus = null;
				}
				/*
				 * Rename
				 */
				boolean success = false;
				final long start = System.currentTimeMillis();
				success = updateMe.renameTo(renameFolder);
				mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
				/*
				 * Success?
				 */
				if (!success) {
					throw new IMAPException(IMAPException.Code.UPDATE_FAILED, updateMe.getFullName());
				}
				updateMe = (IMAPFolder) imapStore.getFolder(oldFullName);
				if (updateMe.exists()) {
					deleteFolder(updateMe);
				}
				updateMe = (IMAPFolder) imapStore.getFolder(newFullName);
				/*
				 * Apply remembered subscription status
				 */
				if (subscriptionStatus == null) {
					/*
					 * At least subscribe to renamed folder
					 */
					updateMe.setSubscribed(true);
				} else {
					applySubscriptionStatus(updateMe, subscriptionStatus);
				}
			}
			if (imapConfig.isSupportsACLs() && toUpdate.containsPermissions()) {
				final ACL[] oldACLs = getACLSafe(updateMe);
				if (oldACLs != null) {
					final ACL[] newACLs = permissions2ACL(toUpdate.getPermissions(), updateMe);
					if (!equals(oldACLs, newACLs)) {
						/*
						 * Default folder is affected, check if owner still
						 * holds full rights
						 */
						if (isDefaultFolder(updateMe.getFullName())
								&& !stillHoldsFullRights(updateMe, newACLs, session)) {
							throw new IMAPException(IMAPException.Code.NO_DEFAULT_FOLDER_UPDATE, updateMe.getFullName());
						} else if (!RightsCache.getCachedRights(updateMe, true, session).contains(
								Rights.Right.ADMINISTER)) {
							throw new IMAPException(IMAPException.Code.NO_ADMINISTER_ACCESS, updateMe.getFullName());
						}
						/*
						 * Check new ACLs
						 */
						if (newACLs.length == 0) {
							throw new IMAPException(IMAPException.Code.NO_ADMIN_ACL, updateMe.getFullName());
						}
						boolean adminFound = false;
						for (int i = 0; i < newACLs.length && !adminFound; i++) {
							if (newACLs[i].getRights().contains(Rights.Right.ADMINISTER)) {
								adminFound = true;
							}
						}
						if (!adminFound) {
							throw new IMAPException(IMAPException.Code.NO_ADMIN_ACL, updateMe.getFullName());
						}
						/*
						 * Remove deleted ACLs
						 */
						final ACL[] removedACLs = getRemovedACLs(newACLs, oldACLs);
						if (removedACLs.length > 0) {
							final UserStorage userStorage = UserStorage.getInstance(session.getContext());
							final User2ACL user2ACL = User2ACL.getInstance(UserStorage.getUser(session.getUserId(),
									session.getContext()));
							final User2ACLArgs user2ACLArgs = IMAPFolderConverter.getUser2AclArgs(session, updateMe);
							for (int i = 0; i < removedACLs.length; i++) {
								if (isKnownEntity(removedACLs[i].getName(), user2ACL, userStorage, user2ACLArgs)) {
									updateMe.removeACL(removedACLs[i].getName());
								}
							}
						}
						/*
						 * Change existing ACLs according to new ACLs
						 */
						for (int i = 0; i < newACLs.length; i++) {
							updateMe.addACL(newACLs[i]);
						}
						/*
						 * Since the ACLs have changed remove cached rights
						 */
						RightsCache.removeCachedRights(updateMe, session);
					}
				}
			}
			if (!IMAPConfig.isIgnoreSubscription() && toUpdate.containsSubscribed()) {
				updateMe.setSubscribed(toUpdate.isSubscribed());
				IMAPCommandsCollection.forceSetSubscribed(imapStore, updateMe.getFullName(), toUpdate.isSubscribed());
			}
			return prepareFullname(updateMe.getFullName(), updateMe.getSeparator());
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapMailConnection);
		} catch (final IMAPException e) {
			throw e;
		} catch (final AbstractOXException e) {
			throw new IMAPException(e);
		}

	}

	public String updateFolder(final long fullname, final MailFolder toUpdate) throws IMAPException {
		throw new IllegalStateException(ERR_IDS_NOT_SUPPORTED);
	}

	public String deleteFolder(final String fullnameArg) throws MailException {
		try {
			final String fullname = prepareMailFolderParam(fullnameArg);
			IMAPFolder deleteMe = (IMAPFolder) imapStore.getFolder(fullname);
			if (!deleteMe.exists()) {
				deleteMe = checkForNamespaceFolder(fullname);
				if (null == deleteMe) {
					throw new IMAPException(IMAPException.Code.FOLDER_NOT_FOUND, fullname);
				}
			}
			final String trashFullname = prepareMailFolderParam(getTrashFolder());
			if (deleteMe.getParent().getFullName().equals(trashFullname)) {
				/*
				 * Delete permanently
				 */
				deleteFolder(deleteMe);
			} else {
				/*
				 * Just move this folder to trash
				 */
				final String name = deleteMe.getName();
				int appendix = 1;
				final StringBuilder sb = new StringBuilder();
				IMAPFolder newFolder = (IMAPFolder) imapStore.getFolder(sb.append(trashFullname).append(
						deleteMe.getSeparator()).append(name).toString());
				while (newFolder.exists()) {
					/*
					 * A folder of the same name already exists. Append
					 * appropriate appendix to folder name and check existence
					 * again.
					 */
					sb.setLength(0);
					newFolder = (IMAPFolder) imapStore.getFolder(sb.append(trashFullname).append(
							deleteMe.getSeparator()).append(name).append('_').append(++appendix).toString());
				}
				moveFolder(deleteMe, (IMAPFolder) imapStore.getFolder(trashFullname), newFolder, false);
			}
			return fullnameArg;
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapMailConnection);
		}
	}

	public String deleteFolder(final long id) throws IMAPException {
		throw new IllegalStateException(ERR_IDS_NOT_SUPPORTED);
	}

	private static final Flags FLAGS_DELETED = new Flags(Flags.Flag.DELETED);

	public void clearFolder(final String fullnameArg) throws MailException {
		try {
			final String fullname = prepareMailFolderParam(fullnameArg);
			final IMAPFolder f = (IMAPFolder) imapStore.getFolder(fullname);
			try {
				if ((f.getType() & Folder.HOLDS_MESSAGES) == 0) {
					throw new IMAPException(IMAPException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, f.getFullName());
				} else if (imapConfig.isSupportsACLs()) {
					if (!RightsCache.getCachedRights(f, true, session).contains(Rights.Right.READ)) {
						throw new IMAPException(IMAPException.Code.NO_READ_ACCESS, f.getFullName());
					} else if (!RightsCache.getCachedRights(f, true, session).contains(Rights.Right.DELETE)) {
						throw new IMAPException(IMAPException.Code.NO_DELETE_ACCESS, f.getFullName());
					}
				}
			} catch (final MessagingException e) {
				throw new IMAPException(IMAPException.Code.NO_ACCESS, f.getFullName());
			}
			f.open(Folder.READ_WRITE);
			try {
				final String trashFullname = prepareMailFolderParam(getTrashFolder());
				if (!UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(), session.getContext())
						.isHardDeleteMsgs()
						&& !(f.getFullName().equals(trashFullname))) {
					final IMAPFolder trashFolder = (IMAPFolder) imapStore.getFolder(trashFullname);
					try {
						final long start = System.currentTimeMillis();
						new CopyIMAPCommand(f, trashFullname).doCommand();
						if (LOG.isInfoEnabled()) {
							LOG.info(new StringBuilder(100).append("\"Soft Clear\": All").append(
									" messages copied to default trash folder \"").append(trashFolder.getFullName())
									.append("\" in ").append((System.currentTimeMillis() - start)).append("msec")
									.toString());
						}
					} catch (final MessagingException e) {
						if (e.getNextException() instanceof CommandFailedException) {
							final CommandFailedException exc = (CommandFailedException) e.getNextException();
							if (exc.getMessage().indexOf("Over quota") > -1) {
								/*
								 * We face an Over-Quota-Exception
								 */
								throw new MailException(MailException.Code.DELETE_FAILED_OVER_QUOTA);
							}
						}
						throw new IMAPException(IMAPException.Code.MOVE_ON_DELETE_FAILED);
					}
				}
				/*
				 * Mark all messages as /DELETED
				 */
				new FlagsIMAPCommand(f, FLAGS_DELETED, true).doCommand();
				/*
				 * ... and perform EXPUNGE
				 */
				try {
					final long start = System.currentTimeMillis();
					IMAPCommandsCollection.fastExpunge(f);
					mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
					if (LOG.isInfoEnabled()) {
						LOG.info(new StringBuilder(100).append("Folder ").append(f.getFullName())
								.append(" cleared in ").append((System.currentTimeMillis() - start)).append("msec")
								.toString());
					}
				} catch (final ProtocolException pex) {
					throw new MessagingException(pex.getMessage(), pex);
				}
			} finally {
				f.close(false);
			}
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapMailConnection);
		}
	}

	public void clearFolder(final long id) throws IMAPException {
		throw new IllegalStateException(ERR_IDS_NOT_SUPPORTED);
	}

	private static final MailFolder[] EMPTY_PATH = new MailFolder[0];

	public MailFolder[] getPath2DefaultFolder(final String fullnameArg) throws MailException {
		try {
			final String fullname = prepareMailFolderParam(fullnameArg);
			if (fullname.equals(DEFAULT_FOLDER_ID)) {
				return EMPTY_PATH;
			}
			IMAPFolder f = (IMAPFolder) imapStore.getFolder(fullname);
			if (!f.exists()) {
				f = checkForNamespaceFolder(fullname);
				if (null == f) {
					throw new IMAPException(IMAPException.Code.FOLDER_NOT_FOUND, fullname);
				}
			}
			if (imapConfig.isSupportsACLs() && ((f.getType() & Folder.HOLDS_MESSAGES) > 0)) {
				try {
					if (!RightsCache.getCachedRights(f, true, session).contains(Rights.Right.LOOKUP)) {
						throw new IMAPException(IMAPException.Code.NO_LOOKUP_ACCESS, fullname);
					}
				} catch (final MessagingException e) {
					throw new IMAPException(IMAPException.Code.NO_ACCESS, fullname);
				}
			}
			final List<MailFolder> list = new ArrayList<MailFolder>();
			final String defaultFolder = imapStore.getDefaultFolder().getFullName();
			while (!f.getFullName().equals(defaultFolder)) {
				list.add(IMAPFolderConverter.convertFolder(f, session, imapConfig));
				f = (IMAPFolder) f.getParent();
			}
			return list.toArray(new MailFolder[list.size()]);
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapMailConnection);
		}
	}

	public MailFolder[] getPath2DefaultFolder(final long id) throws IMAPException {
		throw new IllegalStateException(ERR_IDS_NOT_SUPPORTED);
	}

	public String getConfirmedHamFolder() throws MailException {
		return getStdFolder(StorageUtility.INDEX_CONFIRMED_HAM);
	}

	public String getConfirmedSpamFolder() throws MailException {
		return getStdFolder(StorageUtility.INDEX_CONFIRMED_SPAM);
	}

	public String getDraftsFolder() throws MailException {
		return getStdFolder(StorageUtility.INDEX_DRAFTS);
	}

	public String getSentFolder() throws MailException {
		return getStdFolder(StorageUtility.INDEX_SENT);
	}

	public String getSpamFolder() throws MailException {
		return getStdFolder(StorageUtility.INDEX_SPAM);
	}

	public String getTrashFolder() throws MailException {
		return getStdFolder(StorageUtility.INDEX_TRASH);
	}

	public void releaseResources() throws IMAPException {
	}

	/*
	 * ++++++++++++++++++ Helper methods ++++++++++++++++++
	 */

	/**
	 * Get the ACL list of specified folder
	 * 
	 * @param imapFolder
	 *            The IMAP folder
	 * @return The ACL list or <code>null</code> if any error occured
	 */
	private static ACL[] getACLSafe(final IMAPFolder imapFolder) {
		try {
			return imapFolder.getACL();
		} catch (final MessagingException e) {
			if (LOG.isDebugEnabled()) {
				LOG.debug(e.getLocalizedMessage(), e);
			}
			return null;
		}
	}

	private void deleteFolder(final IMAPFolder deleteMe) throws MailException, MessagingException {
		if (isDefaultFolder(deleteMe.getFullName())) {
			throw new IMAPException(IMAPException.Code.NO_DEFAULT_FOLDER_DELETE, deleteMe.getFullName());
		} else if (!deleteMe.exists()) {
			throw new IMAPException(IMAPException.Code.FOLDER_NOT_FOUND, deleteMe.getFullName());
		}
		try {
			if (imapConfig.isSupportsACLs() && ((deleteMe.getType() & Folder.HOLDS_MESSAGES) > 0)
					&& !RightsCache.getCachedRights(deleteMe, true, session).contains(Rights.Right.CREATE)) {
				throw new IMAPException(IMAPException.Code.NO_CREATE_ACCESS, deleteMe.getFullName());
			}
		} catch (final MessagingException e) {
			throw new IMAPException(IMAPException.Code.NO_ACCESS, deleteMe.getFullName());
		}
		if (deleteMe.isOpen()) {
			deleteMe.close(false);
		}
		/*
		 * Unsubscribe prior to deletion
		 */
		IMAPCommandsCollection.forceSetSubscribed(imapStore, deleteMe.getFullName(), false);
		final long start = System.currentTimeMillis();
		if (!deleteMe.delete(true)) {
			throw new IMAPException(IMAPException.Code.DELETE_FAILED, deleteMe.getFullName());
		}
		mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
		/*
		 * Remove cache entries
		 */
		RightsCache.removeCachedRights(deleteMe, session);
		UserFlagsCache.removeUserFlags(deleteMe, session);
	}

	private static final transient Rights FULL_RIGHTS = new Rights("lrswipcda");

	private static boolean stillHoldsFullRights(final IMAPFolder defaultFolder, final ACL[] newACLs,
			final SessionObject session) throws AbstractOXException, MessagingException {
		/*
		 * Ensure that owner still holds full rights
		 */
		final String ownerACLName = User2ACL
				.getInstance(UserStorage.getUser(session.getUserId(), session.getContext())).getACLName(
						session.getUserId(), session.getContext(),
						IMAPFolderConverter.getUser2AclArgs(session, defaultFolder));
		for (int i = 0; i < newACLs.length; i++) {
			if (newACLs[i].getName().equals(ownerACLName) && newACLs[i].getRights().contains(FULL_RIGHTS)) {
				return true;
			}
		}
		return false;
	}

	private static Map<String, Boolean> getSubscriptionStatus(final IMAPFolder f, final String oldFullName,
			final String newFullName) throws MessagingException {
		final Map<String, Boolean> retval = new HashMap<String, Boolean>();
		getSubscriptionStatus(retval, f, oldFullName, newFullName);
		return retval;
	}

	private static void getSubscriptionStatus(final Map<String, Boolean> m, final IMAPFolder f,
			final String oldFullName, final String newFullName) throws MessagingException {
		if ((f.getType() & IMAPFolder.HOLDS_FOLDERS) > 0) {
			final Folder[] folders = f.list();
			for (int i = 0; i < folders.length; i++) {
				getSubscriptionStatus(m, (IMAPFolder) folders[i], oldFullName, newFullName);
			}
		}
		m.put(f.getFullName().replaceFirst(oldFullName, quoteReplacement(newFullName)), Boolean.valueOf(f
				.isSubscribed()));
	}

	private static void applySubscriptionStatus(final IMAPFolder f, final Map<String, Boolean> m)
			throws MessagingException {
		if ((f.getType() & IMAPFolder.HOLDS_FOLDERS) > 0) {
			final Folder[] folders = f.list();
			for (int i = 0; i < folders.length; i++) {
				applySubscriptionStatus((IMAPFolder) folders[i], m);
			}
		}
		Boolean b = m.get(f.getFullName());
		if (b == null) {
			if (LOG.isWarnEnabled()) {
				LOG.warn(new StringBuilder(128).append("No stored subscription status found for \"").append(
						f.getFullName()).append('"').toString());
			}
			b = Boolean.TRUE;
		}
		f.setSubscribed(b.booleanValue());
	}

	private IMAPFolder moveFolder(final IMAPFolder toMove, final IMAPFolder destFolder, final String folderName)
			throws MessagingException, MailException {
		String name = folderName;
		if (name == null) {
			name = toMove.getName();
		}
		return moveFolder(toMove, destFolder, name, true);
	}

	private IMAPFolder moveFolder(final IMAPFolder toMove, final IMAPFolder destFolder, final String folderName,
			final boolean checkForDuplicate) throws MessagingException, MailException {
		StringBuilder sb = new StringBuilder();
		if (destFolder.getFullName().length() > 0) {
			sb.append(destFolder.getFullName()).append(destFolder.getSeparator());
		}
		sb.append(folderName);
		final IMAPFolder newFolder = (IMAPFolder) imapStore.getFolder(sb.toString());
		sb = null;
		return moveFolder(toMove, destFolder, newFolder, checkForDuplicate);
	}

	private IMAPFolder moveFolder(final IMAPFolder toMove, final IMAPFolder destFolder, final IMAPFolder newFolder,
			final boolean checkForDuplicate) throws MessagingException, MailException {
		if ((destFolder.getType() & Folder.HOLDS_FOLDERS) == 0) {
			throw new IMAPException(IMAPException.Code.FOLDER_DOES_NOT_HOLD_FOLDERS, destFolder.getFullName());
		}
		final int toMoveType = toMove.getType();
		if (imapConfig.isSupportsACLs() && ((toMoveType & Folder.HOLDS_MESSAGES) > 0)) {
			try {
				if (!RightsCache.getCachedRights(toMove, true, session).contains(Rights.Right.READ)) {
					throw new IMAPException(IMAPException.Code.NO_READ_ACCESS, toMove.getFullName());
				} else if (!RightsCache.getCachedRights(toMove, true, session).contains(Rights.Right.CREATE)) {
					throw new IMAPException(IMAPException.Code.NO_CREATE_ACCESS, toMove.getFullName());
				}
			} catch (final MessagingException e) {
				throw new IMAPException(IMAPException.Code.NO_ACCESS, toMove.getFullName());
			}
		}
		/*
		 * Move by creating a new folder, copying all messages and deleting old
		 * folder
		 */
		if (checkForDuplicate && newFolder.exists()) {
			throw new IMAPException(IMAPException.Code.DUPLICATE_FOLDER, newFolder.getName());
		}
		/*
		 * Create new folder. NOTE: It's not possible to create a folder only
		 * with type set to HOLDS_FOLDERS, cause created folder is selectable
		 * anyway and therefore does not hold flag \NoSelect.
		 */
		if (!newFolder.create(toMoveType)) {
			throw new IMAPException(IMAPException.Code.FOLDER_CREATION_FAILED, newFolder.getFullName(),
					destFolder instanceof DefaultFolder ? DEFAULT_FOLDER_ID : destFolder.getFullName());
		}
		/*
		 * Apply original subscription status
		 */
		newFolder.setSubscribed(toMove.isSubscribed());
		if (imapConfig.isSupportsACLs()) {
			/*
			 * Copy ACLs
			 */
			try {
				newFolder.open(Folder.READ_WRITE);
				try {
					/*
					 * Copy ACLs
					 */
					final ACL[] acls = toMove.getACL();
					for (int i = 0; i < acls.length; i++) {
						newFolder.addACL(acls[i]);
					}
				} finally {
					newFolder.close(false);
				}
			} catch (final ReadOnlyFolderException e) {
				throw new IMAPException(IMAPException.Code.NO_WRITE_ACCESS, newFolder.getFullName());
			}
		}
		if ((toMoveType & Folder.HOLDS_MESSAGES) > 0) {
			/*
			 * Copy messages
			 */
			if (!toMove.isOpen()) {
				toMove.open(Folder.READ_ONLY);
			}
			try {
				final long start = System.currentTimeMillis();
				toMove.copyMessages(toMove.getMessages(), newFolder);
				mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
			} finally {
				toMove.close(false);
			}
		}
		/*
		 * Iterate subfolders
		 */
		final Folder[] subFolders = toMove.list();
		for (int i = 0; i < subFolders.length; i++) {
			moveFolder((IMAPFolder) subFolders[i], newFolder, subFolders[i].getName(), false);
		}
		/*
		 * Delete old folder
		 */
		IMAPCommandsCollection.forceSetSubscribed(imapStore, toMove.getFullName(), false);
		if (!toMove.delete(true) && LOG.isWarnEnabled()) {
			final IMAPException e = new IMAPException(IMAPException.Code.DELETE_FAILED, toMove.getFullName());
			LOG.warn(e.getMessage(), e);
		}
		/*
		 * Remove cache entries
		 */
		RightsCache.removeCachedRights(toMove, session);
		UserFlagsCache.removeUserFlags(toMove, session);
		return newFolder;
	}

	private boolean isDefaultFolder(final String folderFullName) throws MailException {
		boolean isDefaultFolder = false;
		isDefaultFolder = (folderFullName.equalsIgnoreCase(STR_INBOX));
		for (int index = 0; index < 6 && !isDefaultFolder; index++) {
			if (folderFullName.equalsIgnoreCase(prepareMailFolderParam(getStdFolder(index)))) {
				return true;
			}
		}
		return isDefaultFolder;
	}

	private String getStdFolder(final int index) throws MailException {
		try {
			if (StorageUtility.INDEX_INBOX == index) {
				final Folder inbox = imapStore.getFolder(STR_INBOX);
				return prepareFullname(inbox.getFullName(), inbox.getSeparator());
			}
			if (isDefaultFoldersChecked()) {
				return getDefaultMailFolder(index);
			}
			checkDefaultFolders();
			return getDefaultMailFolder(index);
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapMailConnection);
		}
	}

	private String getDefaultMailFolder(final int index) {
		final String[] arr = (String[]) session.getParameter(MailSessionParameterNames.PARAM_DEF_FLD_ARR);
		return arr == null ? null : arr[index];
	}

	private ACL[] permissions2ACL(final OCLPermission[] perms, final IMAPFolder imapFolder) throws AbstractOXException,
			MessagingException {
		final ACL[] acls = new ACL[perms.length];
		for (int i = 0; i < perms.length; i++) {
			acls[i] = ((ACLPermission) perms[i]).getPermissionACL(IMAPFolderConverter.getUser2AclArgs(session,
					imapFolder));
		}
		return acls;
	}

	private static ACL[] getRemovedACLs(final ACL[] newACLs, final ACL[] oldACLs) {
		final List<ACL> retval = new ArrayList<ACL>();
		for (final ACL oldACL : oldACLs) {
			boolean found = false;
			for (int i = 0; i < newACLs.length && !found; i++) {
				if (newACLs[i].getName().equals(oldACL.getName())) {
					found = true;
				}
			}
			if (!found) {
				retval.add(oldACL);
			}
		}
		return retval.toArray(new ACL[retval.size()]);
	}

	private static final boolean isKnownEntity(final String entity, final User2ACL user2ACL,
			final UserStorage userStorage, final User2ACLArgs user2ACLArgs) {
		try {
			return user2ACL.getUserID(entity, userStorage, user2ACLArgs) != -1;
		} catch (final AbstractOXException e) {
			return false;
		}
	}

	private static final String STR_PAT = "p|P";

	private static boolean equals(final ACL[] acls1, final ACL[] acls2) {
		if (acls1.length != acls2.length) {
			return false;
		}
		for (final ACL acl1 : acls1) {
			boolean found = false;
			Inner: for (final ACL acl2 : acls2) {
				if (acl1.getName().equals(acl2.getName())) {
					found = true;
					if (!acl1.getRights().toString().replaceAll(STR_PAT, "").equals(
							acl2.getRights().toString().replaceAll(STR_PAT, ""))) {
						return false;
					}
					break Inner;
				}
			}
			if (!found) {
				return false;
			}
		}
		return true;
	}

	private String checkDefaultFolder(final String prefix, final String name, final int type, final StringBuilder tmp)
			throws MessagingException {
		/*
		 * Check default folder
		 */
		boolean checkSubscribed = true;
		final Folder f = imapStore.getFolder(tmp.append(prefix).append(prepareMailFolderParam(name)).toString());
		tmp.setLength(0);
		if (!f.exists() && !f.create(type)) {
			final IMAPException oxme = new IMAPException(IMAPException.Code.NO_DEFAULT_FOLDER_CREATION, tmp.append(
					prefix).append(name).toString());
			tmp.setLength(0);
			LOG.error(oxme.getMessage(), oxme);
			checkSubscribed = false;
		}
		if (checkSubscribed && !f.isSubscribed()) {
			try {
				f.setSubscribed(true);
			} catch (final MethodNotSupportedException e) {
				LOG.error(e.getMessage(), e);
			} catch (final MessagingException e) {
				LOG.error(e.getMessage(), e);
			}
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug(tmp.append("Default folder \"").append(f.getFullName()).append("\" successfully checked")
					.toString());
			tmp.setLength(0);
		}
		return prepareFullname(f.getFullName(), f.getSeparator());
	}

	/**
	 * Determines if <i>altNamespace</i> is enabled for mailbox. If
	 * <i>altNamespace</i> is enabled all folder which are logically located
	 * below INBOX folder are represented as INBOX's siblings in IMAP folder
	 * tree. Dependent on IMAP server's implementation the INBOX folder is then
	 * marked with attribute <code>\NoInferiors</code> meaning it no longer
	 * allows subfolders.
	 * 
	 * @param imapStore -
	 *            the IMAP store (mailbox)
	 * @return <code>true</code> if altNamespace is enabled; otherwise
	 *         <code>false</code>
	 * @throws MessagingException -
	 *             if IMAP's NAMESPACE command fails
	 */
	private static boolean isAltNamespaceEnabled(final IMAPStore imapStore) throws MessagingException {
		boolean altnamespace = false;
		final Folder[] pn = imapStore.getPersonalNamespaces();
		if (pn.length != 0 && pn[0].getFullName().trim().length() == 0) {
			altnamespace = true;
		}
		return altnamespace;
	}

}

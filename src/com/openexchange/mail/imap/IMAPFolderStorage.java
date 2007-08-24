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

package com.openexchange.mail.imap;

import static com.openexchange.mail.imap.IMAPStorageUtils.DEFAULT_IMAP_FOLDER_ID;
import static com.openexchange.mail.imap.IMAPStorageUtils.prepareFullname;
import static com.openexchange.mail.imap.IMAPStorageUtils.prepareMailFolderParam;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.MethodNotSupportedException;
import javax.mail.ReadOnlyFolderException;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.imap.IMAPProperties;
import com.openexchange.imap.IMAPPropertyException;
import com.openexchange.imap.IMAPUtils;
import com.openexchange.imap.UserSettingMail;
import com.openexchange.imap.user2imap.User2IMAP;
import com.openexchange.mail.MailFolderStorage;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.imap.converters.IMAPFolderConverter;
import com.openexchange.server.IMAPPermission;
import com.openexchange.sessiond.SessionObject;
import com.sun.mail.imap.ACL;
import com.sun.mail.imap.DefaultFolder;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.Rights;

/**
 * IMAPFolderStorage
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

	private final transient IMAPMailConnection imapMailConnection;

	private final transient SessionObject session;

	/**
	 * @param imapStore
	 *            The IMAP store
	 */
	public IMAPFolderStorage(final IMAPStore imapStore, final IMAPMailConnection imapMailConnection,
			final SessionObject session) {
		super();
		this.imapStore = imapStore;
		this.imapMailConnection = imapMailConnection;
		this.session = session;
	}

	public MailFolder getFolder(final String fullnameArg) throws IMAPException {
		try {
			final String fullname = prepareMailFolderParam(fullnameArg);
			if (DEFAULT_IMAP_FOLDER_ID.equals(fullname)) {
				return IMAPFolderConverter.convertIMAPFolder((IMAPFolder) imapStore.getDefaultFolder(), session);
			}
			return IMAPFolderConverter.convertIMAPFolder((IMAPFolder) imapStore.getFolder(fullname), session);
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapMailConnection);
		}
	}

	public MailFolder getFolder(final long id) {
		throw new IllegalStateException(ERR_IDS_NOT_SUPPORTED);
	}

	private static final String PATTERN_ALL = "%";

	public MailFolder[] getSubfolders(final String parentFullnameArg, final boolean all) throws IMAPException {
		try {
			final String parentFullname = prepareMailFolderParam(parentFullnameArg);
			final IMAPFolder parent;
			if (DEFAULT_IMAP_FOLDER_ID.equals(parentFullname)) {
				parent = (IMAPFolder) imapStore.getDefaultFolder();
			} else {
				parent = (IMAPFolder) imapStore.getFolder(parentFullname);
				if (!parent.exists()) {
					throw new IMAPException(IMAPException.Code.FOLDER_NOT_FOUND, parentFullname);
				} else if (IMAPProperties.isSupportsACLs() && ((parent.getType() & IMAPFolder.HOLDS_MESSAGES) > 0)) {
					try {
						if (!session.getCachedRights(parent, true).contains(Rights.Right.LOOKUP)) {
							throw new IMAPException(IMAPException.Code.NO_LOOKUP_ACCESS, parentFullname);
						}
					} catch (final MessagingException e) {
						throw new IMAPException(IMAPException.Code.NO_ACCESS, parentFullname);
					}
				}
			}
			final Folder[] subfolders;
			if (IMAPProperties.isIgnoreSubscription() || all) {
				subfolders = parent.list(PATTERN_ALL);
			} else {
				subfolders = parent.listSubscribed(PATTERN_ALL);
			}
			final List<MailFolder> list = new ArrayList<MailFolder>(subfolders.length);
			for (int i = 0; i < subfolders.length; i++) {
				final MailFolder mo = IMAPFolderConverter.convertIMAPFolder((IMAPFolder) subfolders[i], session);
				if (mo.exists()) {
					list.add(mo);
				}
			}
			return list.toArray(new MailFolder[list.size()]);
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapMailConnection);
		} catch (final IMAPPropertyException e) {
			throw new IMAPException(e);
		}
	}

	public MailFolder[] getSubfolders(final long parentId, final boolean all) {
		throw new IllegalStateException(ERR_IDS_NOT_SUPPORTED);
	}

	public MailFolder getRootFolder() throws IMAPException {
		try {
			return IMAPFolderConverter.convertIMAPFolder((IMAPFolder) imapStore.getDefaultFolder(), session);
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapMailConnection);
		}
	}

	public void checkDefaultFolders() throws IMAPException {
		if (!session.isMailFldsChecked()) {
			final Lock mailFldLock = session.getMailFldsLock();
			mailFldLock.lock();
			try {
				if (session.isMailFldsChecked()) {
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
						&& (!isAltNamespaceEnabled(imapStore) || IMAPProperties
								.isAllowNestedDefaultFolderOnAltNamespace())) {
					/*
					 * Only allow default folder below INBOX if inferiors are
					 * permitted and either altNamespace is disabled or nested
					 * default folder are explicitely allowed
					 */
					tmp.append(inboxFolder.getFullName()).append(inboxFolder.getSeparator());
				}
				final String prefix = tmp.toString();
				tmp.setLength(0);
				final int type = (Folder.HOLDS_MESSAGES | Folder.HOLDS_FOLDERS);
				/*
				 * Check default folders
				 */
				final String[] defaultFolderNames = getDefaultFolderNames(session.getUserSettingMail());
				for (int i = 0; i < defaultFolderNames.length; i++) {
					session.setDefaultMailFolder(i, checkDefaultFolder(prefix, defaultFolderNames[i], type, tmp));
				}
				session.setMailFldsChecked(true);
			} catch (final MessagingException e) {
				throw IMAPException.handleMessagingException(e, imapMailConnection);
			} catch (final IMAPPropertyException e) {
				throw new IMAPException(e);
			} finally {
				mailFldLock.unlock();
			}
		}
	}

	public String createFolder(final MailFolder toCreate) throws IMAPException {
		try {
			/*
			 * Insert
			 */
			final String parentStr = prepareMailFolderParam(toCreate.getParentFullname());
			final IMAPFolder parent = DEFAULT_IMAP_FOLDER_ID.equals(parentStr) ? (IMAPFolder) imapStore
					.getDefaultFolder() : (IMAPFolder) imapStore.getFolder(parentStr);
			if (!parent.exists()) {
				throw new IMAPException(IMAPException.Code.FOLDER_NOT_FOUND, parentStr);
			} else if ((parent.getType() & Folder.HOLDS_FOLDERS) == 0) {
				throw new IMAPException(IMAPException.Code.FOLDER_DOES_NOT_HOLD_FOLDERS,
						parent instanceof DefaultFolder ? DEFAULT_IMAP_FOLDER_ID : parentStr);
			} else if (IMAPProperties.isSupportsACLs()) {
				try {
					if (!session.getCachedRights(parent, true).contains(Rights.Right.CREATE)) {
						throw new IMAPException(IMAPException.Code.NO_CREATE_ACCESS, parentStr);
					}
				} catch (final MessagingException e) {
					throw new IMAPException(IMAPException.Code.NO_ACCESS, parentStr);
				}
			}
			if (toCreate.getName().indexOf(parent.getSeparator()) != -1) {
				throw new IMAPException(IMAPException.Code.INVALID_FOLDER_NAME, Character
						.valueOf(parent.getSeparator()));
			}
			final IMAPFolder createMe = (IMAPFolder) imapStore.getFolder(new StringBuilder(parentStr).append(
					parent.getSeparator()).append(toCreate.getName()).toString());
			if (createMe.exists()) {
				throw new IMAPException(IMAPException.Code.DUPLICATE_FOLDER, createMe.getFullName());
			}
			if (!createMe.create(Folder.HOLDS_MESSAGES | Folder.HOLDS_FOLDERS)) {
				throw new IMAPException(IMAPException.Code.FOLDER_CREATION_FAILED, createMe.getFullName(),
						parent instanceof DefaultFolder ? DEFAULT_IMAP_FOLDER_ID : parent.getFullName());
			}
			/*
			 * Subscribe
			 */
			IMAPUtils.forceSetSubscribed(imapStore, createMe.getFullName(), true);
			if (IMAPProperties.isSupportsACLs() && toCreate.containsPermissions()) {
				final ACL[] initialACLs = createMe.getACL();
				final ACL[] newACLs = permissions2ACL((IMAPPermission[]) toCreate.getPermissions(), createMe);
				if (!equals(initialACLs, newACLs)) {
					boolean hasAdministerRight = false;
					for (int i = 0; i < initialACLs.length && !hasAdministerRight; i++) {
						if (session.getIMAPProperties().getImapLogin().equals(initialACLs[i].getName())
								&& initialACLs[i].getRights().contains(Rights.Right.ADMINISTER)) {
							hasAdministerRight = true;
						}
					}
					if (!hasAdministerRight) {
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
					for (int i = 0; i < removedACLs.length; i++) {
						createMe.removeACL(removedACLs[i].getName());
					}
				}
			}
			return prepareFullname(createMe.getFullName(), createMe.getSeparator());
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapMailConnection);
		} catch (final IMAPPropertyException e) {
			throw new IMAPException(e);
		} catch (final AbstractOXException e) {
			throw new IMAPException(e);
		}
	}

	public String updateFolder(final String fullnameArg, final MailFolder toUpdate) throws IMAPException {
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
				final IMAPFolder destFolder = ((IMAPFolder) (DEFAULT_IMAP_FOLDER_ID.equals(newParent) ? imapStore
						.getDefaultFolder() : imapStore.getFolder(newParent)));
				if (!destFolder.exists()) {
					throw new IMAPException(IMAPException.Code.FOLDER_NOT_FOUND, newParent);
				}
				if (destFolder instanceof DefaultFolder) {
					if ((destFolder.getType() & Folder.HOLDS_FOLDERS) == 0) {
						throw new IMAPException(IMAPException.Code.FOLDER_DOES_NOT_HOLD_FOLDERS, destFolder
								.getFullName());
					}
				} else if (IMAPProperties.isSupportsACLs() && (destFolder.getType() & Folder.HOLDS_MESSAGES) > 0) {
					try {
						if (!session.getCachedRights(destFolder, true).contains(Rights.Right.CREATE)) {
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
				} else if (IMAPProperties.isSupportsACLs() && ((updateMe.getType() & Folder.HOLDS_MESSAGES) > 0)) {
					try {
						if (!session.getCachedRights(updateMe, true).contains(Rights.Right.CREATE)) {
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
					// try {
					updateMe.close(false);
					// } finally {
					// mailInterfaceMonitor.changeNumActive(false);
					// }
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
				// final long start = System.currentTimeMillis();
				// try {
				success = updateMe.renameTo(renameFolder);
				// } finally {
				// mailInterfaceMonitor.addUseTime(System.currentTimeMillis() -
				// start);
				// }
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
			if (IMAPProperties.isSupportsACLs() && toUpdate.containsPermissions()) {
				final ACL[] oldACLs = updateMe.getACL();
				final ACL[] newACLs = permissions2ACL((IMAPPermission[]) toUpdate.getPermissions(), updateMe);
				if (!equals(oldACLs, newACLs)) {
					/*
					 * Default folder is affected, check if owner still holds
					 * full rights
					 */
					if (isDefaultFolder(updateMe.getFullName()) && !stillHoldsFullRights(updateMe, newACLs, session)) {
						throw new IMAPException(IMAPException.Code.NO_DEFAULT_FOLDER_UPDATE, updateMe.getFullName());
					} else if (!session.getCachedRights(updateMe, true).contains(Rights.Right.ADMINISTER)) {
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
					for (int i = 0; i < removedACLs.length; i++) {
						updateMe.removeACL(removedACLs[i].getName());
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
					session.removeCachedRights(updateMe);
				}
			}
			if (!IMAPProperties.isIgnoreSubscription() && toUpdate.containsSubscribed()) {
				updateMe.setSubscribed(toUpdate.isSubscribed());
				IMAPUtils.forceSetSubscribed(imapStore, updateMe.getFullName(), toUpdate.isSubscribed());
			}
			return prepareFullname(updateMe.getFullName(), updateMe.getSeparator());
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapMailConnection);
		} catch (final IMAPPropertyException e) {
			throw new IMAPException(e);
		} catch (final AbstractOXException e) {
			throw new IMAPException(e);
		}

	}

	public String updateFolder(final long fullname, final MailFolder toUpdate) throws IMAPException {
		throw new IllegalStateException(ERR_IDS_NOT_SUPPORTED);
	}

	public String deleteFolder(final String fullnameArg) throws IMAPException {
		try {
			final String fullname = prepareMailFolderParam(fullnameArg);
			final IMAPFolder deleteMe = (IMAPFolder) imapStore.getFolder(fullname);
			deleteFolder(deleteMe);
			return fullnameArg;
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapMailConnection);
		} catch (final IMAPPropertyException e) {
			throw new IMAPException(e);
		}
	}

	public String deleteFolder(final long id) throws IMAPException {
		throw new IllegalStateException(ERR_IDS_NOT_SUPPORTED);
	}

	public String getConfirmedHamFolder() throws IMAPException {
		return getStdFolder(IMAPStorageUtils.INDEX_CONFIRMED_HAM);
	}

	public String getConfirmedSpamFolder() throws IMAPException {
		return getStdFolder(IMAPStorageUtils.INDEX_CONFIRMED_SPAM);
	}

	public String getDraftsFolder() throws IMAPException {
		return getStdFolder(IMAPStorageUtils.INDEX_DRAFTS);
	}

	public String getSpamFolder() throws IMAPException {
		return getStdFolder(IMAPStorageUtils.INDEX_SPAM);
	}

	public String getTrashFolder() throws IMAPException {
		return getStdFolder(IMAPStorageUtils.INDEX_TRASH);
	}

	/*
	 * ++++++++++++++++++ Helper methods ++++++++++++++++++
	 */

	private void deleteFolder(final IMAPFolder deleteMe) throws IMAPException, MessagingException,
			IMAPPropertyException {
		if (isDefaultFolder(deleteMe.getFullName())) {
			throw new IMAPException(IMAPException.Code.NO_DEFAULT_FOLDER_DELETE, deleteMe.getFullName());
		} else if (!deleteMe.exists()) {
			throw new IMAPException(IMAPException.Code.FOLDER_NOT_FOUND, deleteMe.getFullName());
		}
		try {
			if (IMAPProperties.isSupportsACLs() && ((deleteMe.getType() & Folder.HOLDS_MESSAGES) > 0)
					&& !session.getCachedRights(deleteMe, true).contains(Rights.Right.CREATE)) {
				throw new IMAPException(IMAPException.Code.NO_CREATE_ACCESS, deleteMe.getFullName());
			}
		} catch (final MessagingException e) {
			throw new IMAPException(IMAPException.Code.NO_ACCESS, deleteMe.getFullName());
		}
		if (deleteMe.isOpen()) {
			// try {
			deleteMe.close(false);
			// } finally {
			// mailInterfaceMonitor.changeNumActive(false);
			// }
		}
		/*
		 * Unsubscribe prior to deletion
		 */
		IMAPUtils.forceSetSubscribed(imapStore, deleteMe.getFullName(), false);
		// final long start = System.currentTimeMillis();
		if (!deleteMe.delete(true)) {
			throw new IMAPException(IMAPException.Code.DELETE_FAILED, deleteMe.getFullName());
		}
		// mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
		/*
		 * Remove cache entries
		 */
		session.removeCachedRights(deleteMe);
		session.removeCachedUserFlags(deleteMe);
	}

	private static final transient Rights FULL_RIGHTS = new Rights("lrswipcda");

	private static boolean stillHoldsFullRights(final IMAPFolder defaultFolder, final ACL[] newACLs,
			final SessionObject session) throws AbstractOXException, MessagingException {
		/*
		 * Ensure that owner still holds full rights
		 */
		final String ownerACLName = User2IMAP.getInstance(session.getUserObject()).getACLName(
				session.getUserObject().getId(), session.getContext(),
				IMAPFolderConverter.getUser2IMAPInfo(session, defaultFolder));
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
		m.put(f.getFullName().replaceFirst(oldFullName, newFullName), Boolean.valueOf(f.isSubscribed()));
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
			throws MessagingException, IMAPException, IMAPPropertyException {
		String name = folderName;
		if (name == null) {
			name = toMove.getName();
		}
		return moveFolder(toMove, destFolder, name, true);
	}

	private IMAPFolder moveFolder(final IMAPFolder toMove, final IMAPFolder destFolder, final String folderName,
			final boolean checkForDuplicate) throws MessagingException, IMAPException, IMAPPropertyException {
		if ((destFolder.getType() & Folder.HOLDS_FOLDERS) == 0) {
			throw new IMAPException(IMAPException.Code.FOLDER_DOES_NOT_HOLD_FOLDERS, destFolder.getFullName());
		}
		final int toMoveType = toMove.getType();
		if (IMAPProperties.isSupportsACLs() && ((toMoveType & Folder.HOLDS_MESSAGES) > 0)) {
			try {
				if (!session.getCachedRights(toMove, true).contains(Rights.Right.READ)) {
					throw new IMAPException(IMAPException.Code.NO_READ_ACCESS, toMove.getFullName());
				} else if (!session.getCachedRights(toMove, true).contains(Rights.Right.CREATE)) {
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
		StringBuilder sb = new StringBuilder();
		if (destFolder.getFullName().length() > 0) {
			sb.append(destFolder.getFullName()).append(destFolder.getSeparator());
		}
		sb.append(folderName);
		final IMAPFolder newFolder = (IMAPFolder) imapStore.getFolder(sb.toString());
		sb = null;
		if (checkForDuplicate && newFolder.exists()) {
			throw new IMAPException(IMAPException.Code.DUPLICATE_FOLDER, folderName);
		}
		/*
		 * Create new folder. NOTE: It's not possible to create a folder only
		 * with type set to HOLDS_FOLDERS, cause created folder is selectable
		 * anyway and therefore does not hold flag \NoSelect.
		 */
		if (!newFolder.create(toMoveType)) {
			throw new IMAPException(IMAPException.Code.FOLDER_CREATION_FAILED, newFolder.getFullName(),
					destFolder instanceof DefaultFolder ? DEFAULT_IMAP_FOLDER_ID : destFolder.getFullName());
		}
		try {
			newFolder.open(Folder.READ_WRITE);
			// TODO: mailInterfaceMonitor.changeNumActive(true);
			try {
				newFolder.setSubscribed(toMove.isSubscribed());
				/*
				 * Copy ACLs
				 */
				final ACL[] acls = toMove.getACL();
				for (int i = 0; i < acls.length; i++) {
					newFolder.addACL(acls[i]);
				}
			} finally {
				try {
					newFolder.close(false);
				} finally {
					// TODO: mailInterfaceMonitor.changeNumActive(false);
				}
			}
		} catch (final ReadOnlyFolderException e) {
			throw new IMAPException(IMAPException.Code.NO_WRITE_ACCESS, newFolder.getFullName());
		}
		if ((toMoveType & Folder.HOLDS_MESSAGES) > 0) {
			/*
			 * Copy messages
			 */
			if (!toMove.isOpen()) {
				toMove.open(Folder.READ_ONLY);
				// TODO: mailInterfaceMonitor.changeNumActive(true);
			}
			try {
				// final long start = System.currentTimeMillis();
				toMove.copyMessages(toMove.getMessages(), newFolder);
				// TODO:
				// mailInterfaceMonitor.addUseTime(System.currentTimeMillis() -
				// start);
			} finally {
				// try {
				toMove.close(false);
				// } finally {
				// TODO: mailInterfaceMonitor.changeNumActive(false);
				// }
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
		IMAPUtils.forceSetSubscribed(imapStore, toMove.getFullName(), false);
		if (!toMove.delete(true) && LOG.isWarnEnabled()) {
			final IMAPException e = new IMAPException(IMAPException.Code.DELETE_FAILED, toMove.getFullName());
			LOG.warn(e.getMessage(), e);
		}
		/*
		 * Remove cache entries
		 */
		session.removeCachedRights(toMove);
		session.removeCachedUserFlags(toMove);
		return newFolder;
	}

	private boolean isDefaultFolder(final String folderFullName) throws IMAPException {
		boolean isDefaultFolder = false;
		isDefaultFolder = (folderFullName.equalsIgnoreCase(STR_INBOX));
		for (int index = 0; index < 6 && !isDefaultFolder; index++) {
			if (folderFullName.equalsIgnoreCase(prepareMailFolderParam(getStdFolder(index)))) {
				return true;
			}
		}
		return isDefaultFolder;
	}

	private String getStdFolder(final int index) throws IMAPException {
		try {
			if (IMAPStorageUtils.INDEX_INBOX == index) {
				final Folder inbox = imapStore.getFolder(STR_INBOX);
				return prepareFullname(inbox.getFullName(), inbox.getSeparator());
			}
			if (session.isMailFldsChecked()) {
				return session.getDefaultMailFolder(index);
			}
			checkDefaultFolders();
			return session.getDefaultMailFolder(index);
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapMailConnection);
		}
	}

	private ACL[] permissions2ACL(final IMAPPermission[] imapPermissions, final IMAPFolder imapFolder)
			throws AbstractOXException, MessagingException {
		final ACL[] acls = new ACL[imapPermissions.length];
		for (int i = 0; i < imapPermissions.length; i++) {
			acls[i] = imapPermissions[i].getPermissionACL(IMAPFolderConverter.getUser2IMAPInfo(session, imapFolder));
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

	private static final String SWITCH_DEFAULT_FOLDER = "Switching to default value %s";

	private static String[] getDefaultFolderNames(final UserSettingMail usm) throws IMAPPropertyException {
		final String[] names = new String[usm.isSpamEnabled() ? 6 : 4];
		if (usm.getStdDraftsName() == null || usm.getStdDraftsName().length() == 0) {
			if (LOG.isWarnEnabled()) {
				final IMAPException e = new IMAPException(IMAPException.Code.MISSING_DEFAULT_FOLDER_NAME,
						UserSettingMail.STD_DRAFTS);
				LOG.warn(String.format(SWITCH_DEFAULT_FOLDER, UserSettingMail.STD_DRAFTS), e);
			}
			names[IMAPStorageUtils.INDEX_DRAFTS] = UserSettingMail.STD_DRAFTS;
		} else {
			names[IMAPStorageUtils.INDEX_DRAFTS] = usm.getStdDraftsName();
		}
		if (usm.getStdSentName() == null || usm.getStdSentName().length() == 0) {
			if (LOG.isWarnEnabled()) {
				final IMAPException e = new IMAPException(IMAPException.Code.MISSING_DEFAULT_FOLDER_NAME,
						UserSettingMail.STD_SENT);
				LOG.warn(String.format(SWITCH_DEFAULT_FOLDER, UserSettingMail.STD_SENT), e);
			}
			names[IMAPStorageUtils.INDEX_SENT] = UserSettingMail.STD_SENT;
		} else {
			names[IMAPStorageUtils.INDEX_SENT] = usm.getStdSentName();
		}
		if (usm.getStdSpamName() == null || usm.getStdSpamName().length() == 0) {
			if (LOG.isWarnEnabled()) {
				final IMAPException e = new IMAPException(IMAPException.Code.MISSING_DEFAULT_FOLDER_NAME,
						UserSettingMail.STD_SPAM);
				LOG.warn(String.format(SWITCH_DEFAULT_FOLDER, UserSettingMail.STD_SPAM), e);
			}
			names[IMAPStorageUtils.INDEX_SPAM] = UserSettingMail.STD_SPAM;
		} else {
			names[IMAPStorageUtils.INDEX_SPAM] = usm.getStdSpamName();
		}
		if (usm.getStdTrashName() == null || usm.getStdTrashName().length() == 0) {
			if (LOG.isWarnEnabled()) {
				final IMAPException e = new IMAPException(IMAPException.Code.MISSING_DEFAULT_FOLDER_NAME,
						UserSettingMail.STD_TRASH);
				LOG.warn(String.format(SWITCH_DEFAULT_FOLDER, UserSettingMail.STD_TRASH), e);
			}
			names[IMAPStorageUtils.INDEX_TRASH] = UserSettingMail.STD_TRASH;
		} else {
			names[IMAPStorageUtils.INDEX_TRASH] = usm.getStdTrashName();
		}
		if (usm.isSpamEnabled()) {
			if (usm.getConfirmedSpam() == null || usm.getConfirmedSpam().length() == 0) {
				if (LOG.isWarnEnabled()) {
					final IMAPException e = new IMAPException(IMAPException.Code.MISSING_DEFAULT_FOLDER_NAME,
							UserSettingMail.STD_CONFIRMED_SPAM);
					LOG.warn(String.format(SWITCH_DEFAULT_FOLDER, UserSettingMail.STD_CONFIRMED_SPAM), e);
				}
				names[IMAPStorageUtils.INDEX_CONFIRMED_SPAM] = UserSettingMail.STD_CONFIRMED_SPAM;
			} else {
				names[IMAPStorageUtils.INDEX_CONFIRMED_SPAM] = usm.getConfirmedSpam();
			}
			if (usm.getConfirmedHam() == null || usm.getConfirmedHam().length() == 0) {
				if (LOG.isWarnEnabled()) {
					final IMAPException e = new IMAPException(IMAPException.Code.MISSING_DEFAULT_FOLDER_NAME,
							UserSettingMail.STD_CONFIRMED_HAM);
					LOG.warn(String.format(SWITCH_DEFAULT_FOLDER, UserSettingMail.STD_CONFIRMED_HAM), e);
				}
				names[IMAPStorageUtils.INDEX_CONFIRMED_HAM] = UserSettingMail.STD_CONFIRMED_HAM;
			} else {
				names[IMAPStorageUtils.INDEX_CONFIRMED_HAM] = usm.getConfirmedHam();
			}
		}
		return names;
	}

}

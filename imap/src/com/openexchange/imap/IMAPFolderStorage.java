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

import static com.openexchange.mail.MailServletInterface.mailInterfaceMonitor;
import static com.openexchange.mail.dataobjects.MailFolder.DEFAULT_FOLDER_ID;
import static com.openexchange.mail.utils.StorageUtility.getDefaultFolderNames;
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
import javax.mail.Quota;
import javax.mail.ReadOnlyFolderException;
import javax.mail.Quota.Resource;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
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
import com.openexchange.mail.MailSessionParameterNames;
import com.openexchange.mail.api.MailFolderStorage;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailFolderDescription;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.mail.utils.StorageUtility;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.session.Session;
import com.sun.mail.iap.CommandFailedException;
import com.sun.mail.iap.ConnectionException;
import com.sun.mail.iap.ParsingException;
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
public final class IMAPFolderStorage extends MailFolderStorage implements Serializable {

	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = -7945316079728160239L;

	private static final transient org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(IMAPFolderStorage.class);

	private static final String STR_INBOX = "INBOX";

	private final transient IMAPStore imapStore;

	private final transient IMAPAccess imapAccess;

	private final transient Session session;

	private final transient Context ctx;

	private final transient IMAPConfig imapConfig;

	/**
	 * Initializes a new {@link IMAPFolderStorage}
	 * 
	 * @param imapStore
	 *            The IMAP store
	 * @param imapAccess
	 *            The IMAP access
	 * @param session
	 *            The session providing needed user data
	 * @throws IMAPException
	 *             If context loading fails
	 */
	public IMAPFolderStorage(final IMAPStore imapStore, final IMAPAccess imapAccess, final Session session)
			throws IMAPException {
		super();
		this.imapStore = imapStore;
		this.imapAccess = imapAccess;
		this.session = session;
		try {
			this.ctx = ContextStorage.getStorageContext(session.getContextId());
		} catch (final ContextException e) {
			throw new IMAPException(e);
		}
		this.imapConfig = imapAccess.getIMAPConfig();
	}

	@Override
	public boolean exists(final String fullname) throws MailException {
		try {
			if (DEFAULT_FOLDER_ID.equals(fullname)) {
				return true;
			}
			if (imapStore.getFolder(fullname).exists()) {
				return true;
			}
			return (checkForNamespaceFolder(fullname) != null);
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapAccess);
		}
	}

	@Override
	public MailFolder getFolder(final String fullname) throws MailException {
		try {
			if (DEFAULT_FOLDER_ID.equals(fullname)) {
				return IMAPFolderConverter.convertFolder((IMAPFolder) imapStore.getDefaultFolder(), session,
						imapConfig, ctx);
			}
			IMAPFolder f = (IMAPFolder) imapStore.getFolder(fullname);
			if (f.exists()) {
				return IMAPFolderConverter.convertFolder(f, session, imapConfig, ctx);
			}
			f = checkForNamespaceFolder(fullname);
			if (null == f) {
				throw new IMAPException(IMAPException.Code.FOLDER_NOT_FOUND, fullname);
			}
			return IMAPFolderConverter.convertFolder(f, session, imapConfig, ctx);
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapAccess);
		}
	}

	private static final String PATTERN_ALL = "%";

	@Override
	public MailFolder[] getSubfolders(final String parentFullname, final boolean all) throws MailException {
		try {
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
				for (final Folder subfolder : subfolders) {
					list.add(IMAPFolderConverter.convertFolder((IMAPFolder) subfolder, session, imapConfig, ctx));
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
			throw IMAPException.handleMessagingException(e, imapAccess);
		}
	}

	private MailFolder[] getSubfolderArray(final boolean all, final IMAPFolder parent) throws MessagingException,
			MailException {
		final Folder[] subfolders;
		if (IMAPConfig.isIgnoreSubscription() || all) {
			subfolders = parent.list(PATTERN_ALL);
		} else {
			subfolders = parent.listSubscribed(PATTERN_ALL);
		}
		final List<MailFolder> list = new ArrayList<MailFolder>(subfolders.length);
		for (int i = 0; i < subfolders.length; i++) {
			final MailFolder mo = IMAPFolderConverter.convertFolder((IMAPFolder) subfolders[i], session, imapConfig,
					ctx);
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

	@Override
	public MailFolder getRootFolder() throws MailException {
		try {
			return IMAPFolderConverter.convertFolder((IMAPFolder) imapStore.getDefaultFolder(), session, imapConfig,
					ctx);
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapAccess);
		}
	}

	private boolean isDefaultFoldersChecked() {
		final Boolean b = (Boolean) session.getParameter(MailSessionParameterNames.PARAM_DEF_FLD_FLAG);
		return b != null && b.booleanValue();
	}

	private void setDefaultFoldersChecked(final boolean checked) {
		session.setParameter(MailSessionParameterNames.PARAM_DEF_FLD_FLAG, Boolean.valueOf(checked));
	}

	private void setSeparator(final char separator) {
		session.setParameter(MailSessionParameterNames.PARAM_SEPARATOR, Character.valueOf(separator));
	}

	private void setDefaultMailFolder(final int index, final String fullname) {
		String[] arr = (String[]) session.getParameter(MailSessionParameterNames.PARAM_DEF_FLD_ARR);
		if (null == arr) {
			arr = new String[6];
			session.setParameter(MailSessionParameterNames.PARAM_DEF_FLD_ARR, arr);
		}
		arr[index] = fullname;
	}

	@Override
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
					final int type;
					if (IMAPConfig.isMBoxEnabled()) {
						type = Folder.HOLDS_MESSAGES;
					} else {
						type = FOLDER_TYPE;
					}
					/*
					 * Check default folders
					 */
					final String[] defaultFolderNames = getDefaultFolderNames(UserSettingMailStorage.getInstance()
							.getUserSettingMail(session.getUserId(), ctx));
					for (int i = 0; i < defaultFolderNames.length; i++) {
						setDefaultMailFolder(i, checkDefaultFolder(prefix, defaultFolderNames[i], type, tmp));
					}
					setSeparator(inboxFolder.getSeparator());
					setDefaultFoldersChecked(true);
				} catch (final MessagingException e) {
					throw IMAPException.handleMessagingException(e, imapAccess);
				}
			}
		}
	}

	private static final int FOLDER_TYPE = (Folder.HOLDS_MESSAGES | Folder.HOLDS_FOLDERS);

	@Override
	public String createFolder(final MailFolderDescription toCreate) throws MailException {
		boolean created = false;
		IMAPFolder createMe = null;
		try {
			/*
			 * Insert
			 */
			String parentFullname = toCreate.getParentFullname();
			IMAPFolder parent;
			if (DEFAULT_FOLDER_ID.equals(parentFullname)) {
				parent = (IMAPFolder) imapStore.getDefaultFolder();
				parentFullname = parent.getFullName();
			} else {
				parent = (IMAPFolder) imapStore.getFolder(parentFullname);
			}
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
			if (!checkFolderNameValidity(toCreate.getName(), parent.getSeparator())) {
				throw new IMAPException(IMAPException.Code.INVALID_FOLDER_NAME, Character
						.valueOf(parent.getSeparator()));
			}
			if (parent.getFullName().length() == 0) {
				/*
				 * Below default folder
				 */
				createMe = (IMAPFolder) imapStore.getFolder(toCreate.getName());
			} else {
				createMe = (IMAPFolder) imapStore.getFolder(new StringBuilder(parent.getFullName()).append(
						parent.getSeparator()).append(toCreate.getName()).toString());
			}
			if (createMe.exists()) {
				throw new IMAPException(IMAPException.Code.DUPLICATE_FOLDER, createMe.getFullName());
			}
			final int ftype;
			if (IMAPConfig.isMBoxEnabled()) {
				/*
				 * Determine folder creation type dependent on folder name
				 */
				ftype = createMe.getName().endsWith(String.valueOf(parent.getSeparator())) ? Folder.HOLDS_FOLDERS
						: Folder.HOLDS_MESSAGES;
			} else {
				ftype = FOLDER_TYPE;
			}
			try {
				if (!(created = createMe.create(ftype))) {
					throw new IMAPException(IMAPException.Code.FOLDER_CREATION_FAILED, createMe.getFullName(),
							parent instanceof DefaultFolder ? DEFAULT_FOLDER_ID : parent.getFullName());
				}
			} catch (final MessagingException e) {
				if ("Unsupported type".equals(e.getMessage())) {
					if (LOG.isWarnEnabled()) {
						LOG.warn("IMAP folder creation failed due to unsupported type."
								+ " Going to retry with fallback type HOLDS-MESSAGES.", e);
					}
					if (!(created = createMe.create(Folder.HOLDS_MESSAGES))) {
						throw new IMAPException(IMAPException.Code.FOLDER_CREATION_FAILED, createMe.getFullName(),
								parent instanceof DefaultFolder ? DEFAULT_FOLDER_ID : parent.getFullName());
					}
					if (LOG.isInfoEnabled()) {
						LOG.info("IMAP folder created with fallback type HOLDS_MESSAGES");
					}
				} else {
					throw IMAPException.handleMessagingException(e, imapAccess);
				}
			}
			/*
			 * Subscribe
			 */
			if (!IMAPConfig.isSupportSubscription()) {
				IMAPCommandsCollection.forceSetSubscribed(imapStore, createMe.getFullName(), true);
			} else if (toCreate.containsSubscribed()) {
				IMAPCommandsCollection.forceSetSubscribed(imapStore, createMe.getFullName(), toCreate.isSubscribed());
			} else {
				IMAPCommandsCollection.forceSetSubscribed(imapStore, createMe.getFullName(), true);
			}
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
							final User2ACL user2ACL = User2ACL.getInstance(imapConfig);
							final User2ACLArgs user2ACLArgs = IMAPFolderConverter.getUser2AclArgs(session, createMe);
							for (int i = 0; i < removedACLs.length; i++) {
								if (isKnownEntity(removedACLs[i].getName(), user2ACL, ctx, user2ACLArgs)) {
									createMe.removeACL(removedACLs[i].getName());
								}
							}
						}
					}
				}
			}
			return createMe.getFullName();
		} catch (final MessagingException e) {
			if (created) {
				try {
					if (createMe.exists()) {
						createMe.delete(true);
					}
				} catch (final Throwable e2) {
					LOG.error(new StringBuilder().append("Temporary created IMAP folder \"").append(
							createMe.getFullName()).append("could not be deleted"), e2);
				}
			}
			throw IMAPException.handleMessagingException(e, imapAccess);
		} catch (final AbstractOXException e) {
			if (created) {
				try {
					if (createMe.exists()) {
						createMe.delete(true);
					}
				} catch (final Throwable e2) {
					LOG.error(new StringBuilder().append("Temporary created IMAP folder \"").append(
							createMe.getFullName()).append("could not be deleted"), e2);
				}
			}
			throw new IMAPException(e);
		}
	}

	@Override
	public String moveFolder(final String fullname, final String newFullname) throws MailException {
		if (DEFAULT_FOLDER_ID.equals(fullname) || DEFAULT_FOLDER_ID.equals(newFullname)) {
			throw new IMAPException(IMAPException.Code.NO_ROOT_MOVE);
		}
		try {
			IMAPFolder moveMe = (IMAPFolder) imapStore.getFolder(fullname);
			final char separator = moveMe.getSeparator();
			final String oldParent = moveMe.getParent().getFullName();
			final String newParent;
			final String newName;
			{
				final int pos = newFullname.lastIndexOf(separator);
				if (pos == -1) {
					newParent = "";
					newName = newFullname;
				} else {
					newParent = newFullname.substring(0, pos);
					newName = newFullname.substring(pos + 1);
				}
			}
			/*
			 * Check for move
			 */
			final boolean move = !newParent.equals(oldParent);
			/*
			 * Check for rename. Rename must not be performed if a move has
			 * already been done
			 */
			final boolean rename = (!move && !newName.equals(moveMe.getName()));
			if (move) {
				/*
				 * Perform move operation
				 */
				if (isDefaultFolder(moveMe.getFullName())) {
					throw new IMAPException(IMAPException.Code.NO_DEFAULT_FOLDER_UPDATE, moveMe.getFullName());
				}
				IMAPFolder destFolder = ((IMAPFolder) (newParent.length() == 0 ? imapStore.getDefaultFolder()
						: imapStore.getFolder(newParent)));
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
				if (!checkFolderNameValidity(newName, separator)) {
					throw new IMAPException(IMAPException.Code.INVALID_FOLDER_NAME, Character.valueOf(separator));
				}
				if (destFolder.getFullName().startsWith(moveMe.getFullName())) {
					throw new IMAPException(IMAPException.Code.NO_MOVE_TO_SUBFLD, moveMe.getName(), destFolder
							.getName());
				}
				try {
					moveMe = moveFolder(moveMe, destFolder, newName);
				} catch (final MailException e) {
					deleteTemporaryCreatedFolder(destFolder, newName);
					throw e;
				} catch (final MessagingException e) {
					deleteTemporaryCreatedFolder(destFolder, newName);
					throw e;
				}
			}
			/*
			 * Is rename operation?
			 */
			if (rename) {
				/*
				 * Perform rename operation
				 */
				if (isDefaultFolder(moveMe.getFullName())) {
					throw new IMAPException(IMAPException.Code.NO_DEFAULT_FOLDER_UPDATE, moveMe.getFullName());
				} else if (imapConfig.isSupportsACLs() && ((moveMe.getType() & Folder.HOLDS_MESSAGES) > 0)) {
					try {
						if (!RightsCache.getCachedRights(moveMe, true, session).contains(Rights.Right.CREATE)) {
							throw new IMAPException(IMAPException.Code.NO_CREATE_ACCESS, moveMe.getFullName());
						}
					} catch (final MessagingException e) {
						throw new IMAPException(IMAPException.Code.NO_ACCESS, moveMe.getFullName());
					}
				}
				if (!checkFolderNameValidity(newName, separator)) {
					throw new IMAPException(IMAPException.Code.INVALID_FOLDER_NAME, Character.valueOf(separator));
				}
				/*
				 * Rename can only be invoked on a closed folder
				 */
				if (moveMe.isOpen()) {
					moveMe.close(false);
				}
				final IMAPFolder renameFolder;
				{
					final String parentFullName = moveMe.getParent().getFullName();
					final StringBuilder tmp = new StringBuilder();
					if (parentFullName.length() > 0) {
						tmp.append(parentFullName).append(separator);
					}
					tmp.append(newName);
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
				final String oldFullName = moveMe.getFullName();
				try {
					subscriptionStatus = getSubscriptionStatus(moveMe, oldFullName, newFullName);
				} catch (final MessagingException e) {
					if (LOG.isWarnEnabled()) {
						LOG.warn(new StringBuilder(128).append("Subscription status of folder \"").append(
								moveMe.getFullName()).append(
								"\" and its subfolders could not be stored prior to rename operation"));
					}
					subscriptionStatus = null;
				}
				/*
				 * Rename
				 */
				boolean success = false;
				final long start = System.currentTimeMillis();
				success = moveMe.renameTo(renameFolder);
				mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
				/*
				 * Success?
				 */
				if (!success) {
					throw new IMAPException(IMAPException.Code.UPDATE_FAILED, moveMe.getFullName());
				}
				moveMe = (IMAPFolder) imapStore.getFolder(oldFullName);
				if (moveMe.exists()) {
					deleteFolder(moveMe);
				}
				moveMe = (IMAPFolder) imapStore.getFolder(newFullName);
				/*
				 * Apply remembered subscription status
				 */
				if (subscriptionStatus == null) {
					/*
					 * At least subscribe to renamed folder
					 */
					moveMe.setSubscribed(true);
				} else {
					applySubscriptionStatus(moveMe, subscriptionStatus);
				}
			}
			return moveMe.getFullName();
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapAccess);
		} catch (final IMAPException e) {
			throw e;
		} catch (final AbstractOXException e) {
			throw new IMAPException(e);
		}
	}

	@Override
	public String updateFolder(final String fullname, final MailFolderDescription toUpdate) throws MailException {
		try {
			final IMAPFolder updateMe = (IMAPFolder) imapStore.getFolder(fullname);
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
								&& !stillHoldsFullRights(updateMe, newACLs, imapConfig, session, ctx)) {
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
							final User2ACL user2ACL = User2ACL.getInstance(imapConfig);
							final User2ACLArgs user2ACLArgs = IMAPFolderConverter.getUser2AclArgs(session, updateMe);
							for (int i = 0; i < removedACLs.length; i++) {
								if (isKnownEntity(removedACLs[i].getName(), user2ACL, ctx, user2ACLArgs)) {
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
			return updateMe.getFullName();
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapAccess);
		} catch (final IMAPException e) {
			throw e;
		} catch (final AbstractOXException e) {
			throw new IMAPException(e);
		}

	}

	private void deleteTemporaryCreatedFolder(final IMAPFolder destFolder, final String name) throws MessagingException {
		/*
		 * Delete moved folder if operation failed
		 */
		final IMAPFolder tmp = (IMAPFolder) imapStore.getFolder(new StringBuilder(destFolder.getFullName()).append(
				destFolder.getSeparator()).append(name).toString());
		if (tmp.exists()) {
			try {
				tmp.delete(true);
			} catch (final MessagingException e1) {
				LOG.error("Temporary created folder could not be deleted: " + tmp.getFullName(), e1);
			}
		}
	}

	@Override
	public String deleteFolder(final String fullname, final boolean hardDelete) throws MailException {
		try {
			IMAPFolder deleteMe = (IMAPFolder) imapStore.getFolder(fullname);
			if (!deleteMe.exists()) {
				deleteMe = checkForNamespaceFolder(fullname);
				if (null == deleteMe) {
					throw new IMAPException(IMAPException.Code.FOLDER_NOT_FOUND, fullname);
				}
			}
			if (hardDelete) {
				/*
				 * Delete permanently
				 */
				deleteFolder(deleteMe);
				return fullname;
			}
			final IMAPFolder trashFolder = (IMAPFolder) imapStore.getFolder(getTrashFolder());
			if (deleteMe.getParent().getFullName().equals(trashFolder.getFullName())
					|| ((trashFolder.getType() & Folder.HOLDS_FOLDERS) == 0)) {
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
				IMAPFolder newFolder = (IMAPFolder) imapStore.getFolder(sb.append(trashFolder.getFullName()).append(
						deleteMe.getSeparator()).append(name).toString());
				while (newFolder.exists()) {
					/*
					 * A folder of the same name already exists. Append
					 * appropriate appendix to folder name and check existence
					 * again.
					 */
					sb.setLength(0);
					newFolder = (IMAPFolder) imapStore.getFolder(sb.append(trashFolder.getFullName()).append(
							deleteMe.getSeparator()).append(name).append('_').append(++appendix).toString());
				}
				try {
					moveFolder(deleteMe, trashFolder, newFolder, false);
				} catch (final MailException e) {
					deleteTemporaryCreatedFolder(trashFolder, newFolder.getName());
					throw e;
				} catch (final MessagingException e) {
					deleteTemporaryCreatedFolder(trashFolder, newFolder.getName());
					throw e;
				}
			}
			return fullname;
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapAccess);
		}
	}

	private static final Flags FLAGS_DELETED = new Flags(Flags.Flag.DELETED);

	private static final int INT_1 = 1;

	@Override
	public void clearFolder(final String fullname, final boolean hardDelete) throws MailException {
		try {
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
				final String trashFullname = getTrashFolder();
				final boolean backup = (!hardDelete
						&& !UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(), ctx)
								.isHardDeleteMsgs() && !(f.getFullName().equals(trashFullname)));
				int msgCount = f.getMessageCount();
				/*
				 * Block-wise deletion
				 */
				final long startClear = System.currentTimeMillis();
				final int blockSize = IMAPConfig.getBlockSize();
				final StringBuilder debug;
				if (LOG.isDebugEnabled()) {
					debug = new StringBuilder(128);
				} else {
					debug = null;
				}
				while (msgCount > blockSize) {
					/*
					 * Don't adapt sequence number since folder expunge already
					 * resets message numbering
					 */
					if (backup) {
						try {
							final long start = System.currentTimeMillis();
							new CopyIMAPCommand(f, INT_1, blockSize, trashFullname).doCommand();
							if (LOG.isDebugEnabled()) {
								debug.setLength(0);
								LOG.debug(debug.append("\"Soft Clear\": ").append(
										"Messages copied to default trash folder \"").append(trashFullname).append(
										"\" in ").append((System.currentTimeMillis() - start)).append("msec")
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
					 * Delete through storing \Deleted flag...
					 */
					new FlagsIMAPCommand(f, INT_1, blockSize, FLAGS_DELETED, true).doCommand();
					/*
					 * ... and perform EXPUNGE
					 */
					final long startExpunge = System.currentTimeMillis();
					try {
						IMAPCommandsCollection.fastExpunge(f);
						if (LOG.isDebugEnabled()) {
							debug.setLength(0);
							LOG.debug(debug.append("EXPUNGE command executed on \"").append(f.getFullName()).append(
									"\" in ").append((System.currentTimeMillis() - startExpunge)).append("msec")
									.toString());
						}
					} catch (final ConnectionException e) {
						/*
						 * Connection is broken. Not possible to retry.
						 */
						if (LOG.isDebugEnabled()) {
							debug.setLength(0);
							LOG.debug(debug.append("EXPUNGE command timed out in ").append(
									(System.currentTimeMillis() - startExpunge)).append("msec").toString());
						}
						throw new IMAPException(IMAPException.Code.CONNECTION_ERROR, e, imapAccess.getMailConfig()
								.getServer(), imapAccess.getMailConfig().getLogin());
					}
					/*
					 * Decrement
					 */
					msgCount -= blockSize;
				}
				if (backup) {
					try {
						final long start = System.currentTimeMillis();
						new CopyIMAPCommand(f, trashFullname).doCommand();
						if (LOG.isDebugEnabled()) {
							debug.setLength(0);
							LOG.debug(debug.append("\"Soft Clear\": ").append(
									"Messages copied to default trash folder \"").append(trashFullname)
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
				 * Delete through storing \Deleted flag...
				 */
				new FlagsIMAPCommand(f, FLAGS_DELETED, true).doCommand();
				/*
				 * ... and perform EXPUNGE
				 */
				try {
					final long start = System.currentTimeMillis();
					IMAPCommandsCollection.fastExpunge(f);
					mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
				} catch (final ProtocolException pex) {
					throw new MessagingException(pex.getMessage(), pex);
				}
				if (LOG.isDebugEnabled()) {
					debug.setLength(0);
					LOG.info(debug.append("Folder '").append(fullname).append("' cleared in ").append(
							System.currentTimeMillis() - startClear).append("msec"));
				}
			} finally {
				f.close(false);
			}
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapAccess);
		} catch (final ProtocolException e) {
			throw IMAPException.handleMessagingException(new MessagingException(e.getMessage(), e), imapAccess);
		} catch (final AbstractOXException e) {
			throw new IMAPException(e);
		}
	}

	private static final MailFolder[] EMPTY_PATH = new MailFolder[0];

	@Override
	public MailFolder[] getPath2DefaultFolder(final String fullname) throws MailException {
		try {
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
				list.add(IMAPFolderConverter.convertFolder(f, session, imapConfig, ctx));
				f = (IMAPFolder) f.getParent();
			}
			return list.toArray(new MailFolder[list.size()]);
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapAccess);
		}
	}

	@Override
	public String getConfirmedHamFolder() throws MailException {
		return getStdFolder(StorageUtility.INDEX_CONFIRMED_HAM);
	}

	@Override
	public String getConfirmedSpamFolder() throws MailException {
		return getStdFolder(StorageUtility.INDEX_CONFIRMED_SPAM);
	}

	@Override
	public String getDraftsFolder() throws MailException {
		return getStdFolder(StorageUtility.INDEX_DRAFTS);
	}

	@Override
	public String getSentFolder() throws MailException {
		return getStdFolder(StorageUtility.INDEX_SENT);
	}

	@Override
	public String getSpamFolder() throws MailException {
		return getStdFolder(StorageUtility.INDEX_SPAM);
	}

	@Override
	public String getTrashFolder() throws MailException {
		return getStdFolder(StorageUtility.INDEX_TRASH);
	}

	@Override
	public void releaseResources() throws IMAPException {
	}

	private static final String QUOTA_RES_STORAGE = "STORAGE";

	@Override
	public com.openexchange.mail.Quota getQuota(final String folder) throws MailException {
		try {
			final IMAPFolder f;
			{
				final String fullname = folder == null ? STR_INBOX : folder;
				final boolean isDefaultFolder = fullname.equals(DEFAULT_FOLDER_ID);
				f = (IMAPFolder) (isDefaultFolder ? imapStore.getDefaultFolder() : imapStore.getFolder(fullname));
				if (!isDefaultFolder && !f.exists()) {
					throw new IMAPException(IMAPException.Code.FOLDER_NOT_FOUND, fullname);
				}
				try {
					if ((f.getType() & Folder.HOLDS_MESSAGES) == 0) {
						throw new IMAPException(IMAPException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, fullname);
					} else if (imapConfig.isSupportsACLs()) {
						if (!RightsCache.getCachedRights(f, true, session).contains(Rights.Right.READ)) {
							throw new IMAPException(IMAPException.Code.NO_READ_ACCESS, fullname);
						} else if (!RightsCache.getCachedRights(f, true, session).contains(Rights.Right.DELETE)) {
							throw new IMAPException(IMAPException.Code.NO_DELETE_ACCESS, fullname);
						}
					}
				} catch (final MessagingException e) {
					throw new IMAPException(IMAPException.Code.NO_ACCESS, fullname);
				}
			}
			f.open(Folder.READ_ONLY);
			if (!imapConfig.getImapCapabilities().hasQuota()) {
				return com.openexchange.mail.Quota.UNLIMITED;
			}
			final Quota[] folderQuota;
			try {
				final long start = System.currentTimeMillis();
				folderQuota = f.getQuota();
				mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
			} catch (final MessagingException mexc) {
				if (mexc.getNextException() instanceof ParsingException) {
					return com.openexchange.mail.Quota.UNLIMITED;
				}
				throw mexc;
			}
			if (folderQuota.length == 0) {
				return com.openexchange.mail.Quota.UNLIMITED;
			}
			final Quota.Resource[] resources = folderQuota[0].resources;
			if (resources.length == 0) {
				return com.openexchange.mail.Quota.UNLIMITED;
			}
			Resource storageResource = null;
			for (int i = 0; i < resources.length; i++) {
				if (QUOTA_RES_STORAGE.equalsIgnoreCase(resources[i].name)) {
					storageResource = resources[i];
				}
			}
			if (null == storageResource) {
				/*
				 * No storage limitations
				 */
				if (LOG.isWarnEnabled()) {
					logUnsupportedQuotaResources(resources, 0);
				}
				return com.openexchange.mail.Quota.UNLIMITED;
			}
			if (resources.length > 1 && LOG.isWarnEnabled()) {
				logUnsupportedQuotaResources(resources, 1);
			}
			return new com.openexchange.mail.Quota(storageResource.limit, storageResource.usage);
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapAccess);
		}
	}

	/*
	 * ++++++++++++++++++ Helper methods ++++++++++++++++++
	 */

	/**
	 * Logs unsupported QUOTA resources
	 * 
	 * @param resources
	 *            The QUOTA resources
	 */
	private static void logUnsupportedQuotaResources(final Quota.Resource[] resources, final int start) {
		final StringBuilder sb = new StringBuilder(128)
				.append("Unsupported QUOTA resource(s) [<name> (<usage>/<limit>]:\n");
		sb.append(resources[start].name).append(" (").append(resources[start].usage).append('/').append(
				resources[start].limit).append(')');
		for (int i = start + 1; i < resources.length; i++) {
			sb.append(", ").append(resources[i].name).append(" (").append(resources[i].usage).append('/').append(
					resources[i].limit).append(')');

		}
		LOG.warn(sb.toString());
	}

	/**
	 * Get the QUOTA resource with the highest usage-per-limitation value
	 * 
	 * @param resources
	 *            The QUOTA resources
	 * @return The QUOTA resource with the highest usage to limitation relation
	 * 
	 * <pre>
	 * private static Resource getMaxUsageResource(final Quota.Resource[] resources) {
	 * 	final Resource maxUsageResource;
	 * 	{
	 * 		int index = 0;
	 * 		long maxUsage = resources[0].usage / resources[0].limit;
	 * 		for (int i = 1; i &lt; resources.length; i++) {
	 * 			final long tmp = resources[i].usage / resources[i].limit;
	 * 			if (tmp &gt; maxUsage) {
	 * 				maxUsage = tmp;
	 * 				index = i;
	 * 			}
	 * 		}
	 * 		maxUsageResource = resources[index];
	 * 	}
	 * 	return maxUsageResource;
	 * }
	 * </pre>
	 * 
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
			final IMAPConfig imapConfig, final Session session, final Context ctx) throws AbstractOXException,
			MessagingException {
		/*
		 * Ensure that owner still holds full rights
		 */
		final String ownerACLName = User2ACL.getInstance(imapConfig).getACLName(session.getUserId(), ctx,
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
			if (folderFullName.equalsIgnoreCase(getStdFolder(index))) {
				return true;
			}
		}
		return isDefaultFolder;
	}

	private String getStdFolder(final int index) throws MailException {
		if (!isDefaultFoldersChecked()) {
			checkDefaultFolders();
		}
		if (StorageUtility.INDEX_INBOX == index) {
			return STR_INBOX;
		}
		if (isDefaultFoldersChecked()) {
			return getDefaultMailFolder(index);
		}
		checkDefaultFolders();
		return getDefaultMailFolder(index);
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
					imapFolder), imapConfig, ctx);
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

	private static boolean isKnownEntity(final String entity, final User2ACL user2ACL, final Context ctx,
			final User2ACLArgs user2ACLArgs) {
		try {
			return user2ACL.getUserID(entity, ctx, user2ACLArgs) != -1;
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
		final Folder f = imapStore.getFolder(tmp.append(prefix).append(name).toString());
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
		return f.getFullName();
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

	/**
	 * Checks id specified folder name is allowed to be used on folder creation.
	 * The folder name is valid if the separator character does not appear or
	 * provided that mbox format is enabled may only appear at name's end.
	 * 
	 * @param name
	 *            The folder name to check.
	 * @param separator
	 *            The separator character.
	 * @return <code>true</code> if folder name is valid; otherwise
	 *         <code>false</code>
	 */
	private static boolean checkFolderNameValidity(final String name, final char separator) {
		final int pos = name.indexOf(separator);
		if (IMAPConfig.isMBoxEnabled()) {
			/*
			 * Allow trailing separator
			 */
			return (pos == -1) || (pos == name.length() - 1);
		}
		return (pos == -1);
	}
}
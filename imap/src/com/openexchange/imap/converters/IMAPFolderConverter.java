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

package com.openexchange.imap.converters;

import javax.mail.Folder;
import javax.mail.MessagingException;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.groupware.ldap.UserException;
import com.openexchange.imap.ACLPermission;
import com.openexchange.imap.IMAPException;
import com.openexchange.imap.NamespaceFolder;
import com.openexchange.imap.cache.NamespaceFoldersCache;
import com.openexchange.imap.cache.RightsCache;
import com.openexchange.imap.cache.UserFlagsCache;
import com.openexchange.imap.config.IMAPConfig;
import com.openexchange.imap.user2acl.IMAPServer;
import com.openexchange.imap.user2acl.User2ACLArgs;
import com.openexchange.imap.user2acl.User2ACLException;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailSessionParameterNames;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.mime.MIMEMailException;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.session.Session;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.ACL;
import com.sun.mail.imap.DefaultFolder;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.Rights;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.ListInfo;

/**
 * {@link IMAPFolderConverter} - Converts an instance of {@link IMAPFolder} to
 * an instance of {@link MailFolder}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class IMAPFolderConverter {

	private static final class _User2ACLArgs implements User2ACLArgs {

		private final int sessionUser;

		private final String fullname;

		private final char separator;

		/**
		 * Initializes a new {@link _User2ACLArgs}
		 * 
		 * @param sessionUser
		 *            The session user ID
		 * @param fullname
		 *            The IMAP folder's fullname
		 * @param separator
		 *            The separator character
		 */
		public _User2ACLArgs(final int sessionUser, final String fullname, final char separator) {
			this.sessionUser = sessionUser;
			this.fullname = fullname;
			this.separator = separator;
		}

		private static final Object[] EMPYT_ARGS = new Object[0];

		public Object[] getArguments(final IMAPServer imapServer) throws AbstractOXException {
			if (IMAPServer.CYRUS.equals(imapServer)) {
				return EMPYT_ARGS;
			} else if (IMAPServer.COURIER.equals(imapServer)) {
				return new Object[] { Integer.valueOf(sessionUser), fullname, Character.valueOf(separator) };
			}
			throw new User2ACLException(User2ACLException.Code.UNKNOWN_IMAP_SERVER, imapServer.getName());

		}
	}

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(IMAPFolderConverter.class);

	private static final Rights RIGHTS_EMPTY = new Rights();

	/**
	 * New mailbox attribute added by the "LIST-EXTENDED" extension
	 */
	private static final String ATTRIBUTE_NON_EXISTENT = "\\NonExistent";

	private static final String ATTRIBUTE_HAS_CHILDREN = "\\HasChildren";

	/**
	 * Prevent instantiation
	 */
	private IMAPFolderConverter() {
		super();
	}

	/**
	 * Creates an appropriate implementation of {@link User2ACLArgs}
	 * 
	 * @param session
	 *            The session
	 * @param imapFolder
	 *            The IMAP folder
	 * @return An appropriate implementation of {@link User2ACLArgs}
	 * @throws MessagingException
	 *             If IMAP folder's attributes cannot be accessed
	 */
	public static User2ACLArgs getUser2AclArgs(final Session session, final IMAPFolder imapFolder)
			throws MessagingException {
		return new _User2ACLArgs(session.getUserId(), imapFolder.getFullName(), imapFolder.getSeparator());
	}

	private static final String STR_INBOX = "INBOX";

	/**
	 * Creates a folder data object from given IMAP folder
	 * 
	 * @param imapFolder
	 *            The IMAP folder
	 * @param session
	 *            The session
	 * @param ctx
	 *            The context
	 * @return an instance of <code>{@link MailFolder}</code> containing the
	 *         attributes from given IMAP folder
	 * @throws MailException
	 *             If conversion fails
	 */
	public static MailFolder convertFolder(final IMAPFolder imapFolder, final Session session,
			final IMAPConfig imapConfig, final Context ctx) throws MailException {
		try {
			final MailFolder mailFolder = new MailFolder();
			mailFolder.setRootFolder(imapFolder instanceof DefaultFolder);
			mailFolder.setExists(imapFolder.exists());
			String[] attrs;
			try {
				attrs = imapFolder.getAttributes();
			} catch (final NullPointerException e) {
				/*
				 * No attributes available. Try to determine them manually.
				 */
				attrs = new String[0];
				ListInfo[] li = (ListInfo[]) imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {
					public Object doCommand(final IMAPProtocol protocol) throws ProtocolException {
						try {
							return protocol.list("", new StringBuilder().append(imapFolder.getFullName()).append(
									imapFolder.getSeparator()).append('%').toString());
						} catch (final MessagingException e) {
							LOG.error(e.getLocalizedMessage(), e);
							throw new ProtocolException(e.getLocalizedMessage());
						}
					}
				});
				mailFolder.setSubfolders(hasChildren(li));
				li = (ListInfo[]) imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {
					public Object doCommand(final IMAPProtocol protocol) throws ProtocolException {
						try {
							return protocol.lsub("", new StringBuilder().append(imapFolder.getFullName()).append(
									imapFolder.getSeparator()).append('%').toString());
						} catch (final MessagingException e) {
							LOG.error(e.getLocalizedMessage(), e);
							throw new ProtocolException(e.getLocalizedMessage());
						}
					}
				});
				mailFolder.setSubscribedSubfolders(hasChildren(li));
			}
			Attribs: for (final String attribute : attrs) {
				if (ATTRIBUTE_NON_EXISTENT.equalsIgnoreCase(attribute)) {
					mailFolder.setNonExistent(true);
					break Attribs;
				}
			}
			if (!mailFolder.containsNonExistent()) {
				mailFolder.setNonExistent(false);
			}
			mailFolder.setSeparator(imapFolder.getSeparator());
			if (mailFolder.isRootFolder()) {
				mailFolder.setFullname("");
			} else {
				mailFolder.setFullname(imapFolder.getFullName());
			}
			mailFolder.setName(imapFolder.getName());
			{
				final Folder parent = imapFolder.getParent();
				if (null == parent) {
					mailFolder.setParentFullname(null);
				} else {
					mailFolder.setParentFullname(parent instanceof DefaultFolder ? "" : parent.getFullName());
				}
			}
			/*
			 * Determine if subfolders exist
			 */
			if (mailFolder.exists() && ((imapFolder.getType() & javax.mail.Folder.HOLDS_FOLDERS) == 0)) {
				mailFolder.setSubfolders(false);
				mailFolder.setSubscribedSubfolders(false);
			} else {
				if (!mailFolder.containsSubfolders()) {
					mailFolder.setSubfolders(false);
					Attribs: for (final String attribute : attrs) {
						if (ATTRIBUTE_HAS_CHILDREN.equalsIgnoreCase(attribute)) {
							mailFolder.setSubfolders(true);
							break Attribs;
						}
					}
				}
				if (!mailFolder.containsSubscribedSubfolders()) {
					mailFolder.setSubscribedSubfolders(false);
					if (mailFolder.hasSubfolders()) {
						final ListInfo[] li = (ListInfo[]) imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {
							public Object doCommand(final IMAPProtocol protocol) throws ProtocolException {
								return protocol.lsub("", imapFolder.getFullName());
							}
						});
						if (null != li) {
							final String[] lsubAttrs = li[findName(li, imapFolder.getFullName())].attrs;
							Attribs: for (final String attribute : lsubAttrs) {
								if (ATTRIBUTE_HAS_CHILDREN.equalsIgnoreCase(attribute)) {
									mailFolder.setSubscribedSubfolders(true);
									break Attribs;
								}
							}
						}
					}
				}
			}
			mailFolder
					.setHoldsMessages(mailFolder.exists() ? ((imapFolder.getType() & javax.mail.Folder.HOLDS_MESSAGES) > 0)
							: false);
			mailFolder
					.setHoldsFolders(mailFolder.exists() ? ((imapFolder.getType() & javax.mail.Folder.HOLDS_FOLDERS) > 0)
							: false);
			final Rights ownRights;
			if (mailFolder.isRootFolder()) {
				/*
				 * Properly handled in
				 * com.openexchange.mail.json.writer.FolderWriter
				 */
				mailFolder.setOwnPermission(null);
				ownRights = (Rights) RIGHTS_EMPTY.clone();
			} else {
				final ACLPermission ownPermission = new ACLPermission();
				ownPermission.setEntity(session.getUserId());
				if (!mailFolder.exists() || mailFolder.isNonExistent()) {
					ownPermission.parseRights((ownRights = (Rights) RIGHTS_EMPTY.clone()));
				} else if (!mailFolder.isHoldsMessages()) {
					/*
					 * Distinguish between holds folders and none
					 */
					if (mailFolder.isHoldsFolders()) {
						/*
						 * This is the tricky case: Allow subfolder creation for
						 * a common imap folder but deny it for imap server's
						 * namespace folders
						 */
						if (checkForNamespaceFolder(imapFolder.getFullName(), (IMAPStore) imapFolder.getStore(),
								session)) {
							ownPermission.parseRights((ownRights = (Rights) RIGHTS_EMPTY.clone()));
						} else {
							ownPermission.setAllPermission(OCLPermission.CREATE_SUB_FOLDERS,
									OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS,
									OCLPermission.NO_PERMISSIONS);
							ownPermission.setFolderAdmin(true);
							ownRights = ACLPermission.permission2Rights(ownPermission);
						}
					} else {
						ownPermission.parseRights((ownRights = (Rights) RIGHTS_EMPTY.clone()));
					}
				} else {
					ownPermission.parseRights((ownRights = getOwnRightsInternal(imapFolder, session, imapConfig)));
				}
				/*
				 * Check own permission against folder type
				 */
				if (!mailFolder.isHoldsFolders() && ownPermission.canCreateSubfolders()) {
					ownPermission.setFolderPermission(OCLPermission.CREATE_OBJECTS_IN_FOLDER);
				}
				if (!mailFolder.isHoldsMessages()) {
					ownPermission.setReadObjectPermission(OCLPermission.NO_PERMISSIONS);
				}
				mailFolder.setOwnPermission(ownPermission);
			}
			if (!mailFolder.isRootFolder()) {
				/*
				 * Default folder
				 */
				if (STR_INBOX.equals(imapFolder.getFullName())) {
					mailFolder.setDefaulFolder(true);
				} else if (isDefaultFoldersChecked(session)) {
					final int len = UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(),
							ContextStorage.getStorageContext(session.getContextId())).isSpamEnabled() ? 6 : 4;
					for (int i = 0; (i < len) && !mailFolder.isDefaulFolder(); i++) {
						if (mailFolder.getFullname().equals(getDefaultMailFolder(i, session))) {
							mailFolder.setDefaulFolder(true);
						}
					}
					if (!mailFolder.containsDefaulFolder()) {
						mailFolder.setDefaulFolder(false);
					}
				}
			}
			if (mailFolder.isHoldsMessages() && ownRights.contains(Rights.Right.READ)) {
				mailFolder.setSummary(new StringBuilder().append('(').append(imapFolder.getMessageCount()).append('/')
						.append(imapFolder.getUnreadMessageCount()).append(')').toString());
				mailFolder.setMessageCount(imapFolder.getMessageCount());
				mailFolder.setNewMessageCount(imapFolder.getNewMessageCount());
				mailFolder.setUnreadMessageCount(imapFolder.getUnreadMessageCount());
				mailFolder.setDeletedMessageCount(imapFolder.getDeletedMessageCount());
			} else {
				mailFolder.setSummary(null);
				mailFolder.setMessageCount(-1);
				mailFolder.setNewMessageCount(-1);
				mailFolder.setUnreadMessageCount(-1);
				mailFolder.setDeletedMessageCount(-1);
			}
			mailFolder.setSubscribed(IMAPConfig.isSupportSubscription() ? imapFolder.isSubscribed() : true);
			if (imapConfig.isSupportsACLs()) {
				if (mailFolder.isHoldsMessages() && mailFolder.exists()
						&& (ownRights.contains(Rights.Right.READ) || ownRights.contains(Rights.Right.ADMINISTER))
						&& !(imapFolder instanceof DefaultFolder)) {
					try {
						applyACL2Permissions(imapFolder, session, imapConfig, mailFolder, ownRights, ctx);
					} catch (final AbstractOXException e) {
						if (LOG.isWarnEnabled()) {
							LOG.warn("ACLs could not be parsed", e);
						}
						mailFolder.removePermissions();
						addOwnACL(session.getUserId(), mailFolder, ownRights);
					}
				} else {
					addEmptyACL(session.getUserId(), mailFolder);
				}
			} else {
				addOwnACL(session.getUserId(), mailFolder, ownRights);
			}
			if (IMAPConfig.isUserFlagsEnabled() && mailFolder.exists() && mailFolder.isHoldsMessages()
					&& ownRights.contains(Rights.Right.READ)
					&& UserFlagsCache.supportsUserFlags(imapFolder, true, session)) {
				mailFolder.setSupportsUserFlags(true);
			} else {
				mailFolder.setSupportsUserFlags(false);
			}
			return mailFolder;
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e);
		} catch (final ContextException e) {
			throw new IMAPException(e);
		}
	}

	private static boolean isDefaultFoldersChecked(final Session session) {
		final Boolean b = (Boolean) session.getParameter(MailSessionParameterNames.PARAM_DEF_FLD_FLAG);
		return b != null && b.booleanValue();
	}

	private static String getDefaultMailFolder(final int index, final Session session) {
		final String[] arr = (String[]) session.getParameter(MailSessionParameterNames.PARAM_DEF_FLD_ARR);
		return arr == null ? null : arr[index];
	}

	/**
	 * Parses IMAP folder's ACLs to instances of {@link ACLPermission} and
	 * applies them to specified mail folder
	 * 
	 * @param imapFolder
	 *            The IMAP folder
	 * @param session
	 *            The session providing needed user data
	 * @param imapConfig
	 *            The user's IMAP configuration
	 * @param mailFolder
	 *            The mail folder
	 * @param ownRights
	 *            The rights granted to IMAP folder for session user
	 * @param ctx
	 *            The context
	 * @throws AbstractOXException
	 *             If ACLs cannot be mapped
	 */
	private static void applyACL2Permissions(final IMAPFolder imapFolder, final Session session,
			final IMAPConfig imapConfig, final MailFolder mailFolder, final Rights ownRights, final Context ctx)
			throws AbstractOXException {
		if (IMAPConfig.hasNewACLExt(imapConfig.getServer()) && !ownRights.contains(Rights.Right.ADMINISTER)) {
			addOwnACL(session.getUserId(), mailFolder, ownRights);
		} else {
			final ACL[] acls;
			try {
				acls = imapFolder.getACL();
			} catch (final MessagingException e) {
				if (!ownRights.contains(Rights.Right.ADMINISTER)) {
					if (LOG.isWarnEnabled()) {
						LOG.warn(new StringBuilder(256).append("ACLs could not be requested for folder ").append(
								imapFolder.getFullName()).append(
								". A newer ACL extension (RFC 4314) seems to be supported by IMAP server ").append(
								imapConfig.getServer()).append(
								", which denies GETACL command if no ADMINISTER right is granted."), e);
					}
					/*
					 * Remember newer IMAP server's ACL extension
					 */
					IMAPConfig.setNewACLExt(imapConfig.getServer(), true);
					addOwnACL(session.getUserId(), mailFolder, ownRights);
					return;
				}
				throw MIMEMailException.handleMessagingException(e);
			}
			try {
				final User2ACLArgs args = new _User2ACLArgs(session.getUserId(), imapFolder.getFullName(), imapFolder
						.getSeparator());
				final StringBuilder debugBuilder;
				if (LOG.isDebugEnabled()) {
					debugBuilder = new StringBuilder(128);
				} else {
					debugBuilder = null;
				}
				for (int j = 0; j < acls.length; j++) {
					final ACLPermission aclPerm = new ACLPermission();
					try {
						aclPerm.parseACL(acls[j], args, imapConfig, ctx);
						mailFolder.addPermission(aclPerm);
					} catch (final AbstractOXException e) {
						if (isUnknownEntityError(e)) {
							if (LOG.isDebugEnabled()) {
								debugBuilder.setLength(0);
								LOG.debug(debugBuilder.append("Cannot map ACL entity named \"").append(
										acls[j].getName()).append("\" to a system user").toString());
							}
						} else {
							throw e;
						}
					}
				}
			} catch (final MessagingException e) {
				throw MIMEMailException.handleMessagingException(e);
			}
		}
	}

	/**
	 * Adds current user's rights granted to IMAP folder as an ACL
	 * 
	 */
	private static void addOwnACL(final int sessionUser, final MailFolder mailFolder, final Rights ownRights) {
		final ACLPermission aclPerm = new ACLPermission();
		aclPerm.setEntity(sessionUser);
		aclPerm.parseRights(ownRights);
		mailFolder.addPermission(aclPerm);
	}

	/**
	 * Adds empty ACL to specified mail folder
	 * 
	 */
	private static void addEmptyACL(final int sessionUser, final MailFolder mailFolder) {
		final ACLPermission aclPerm = new ACLPermission();
		aclPerm.setEntity(sessionUser);
		aclPerm.setAllPermission(OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS,
				OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS);
		mailFolder.addPermission(aclPerm);
	}

	private static boolean isUnknownEntityError(final AbstractOXException e) {
		return Component.USER.equals(e.getComponent())
				&& (UserException.Code.USER_NOT_FOUND.getDetailNumber() == e.getDetailNumber() || LdapException.Code.USER_NOT_FOUND
						.getDetailNumber() == e.getDetailNumber());
	}

	private static boolean checkForNamespaceFolder(final String fullname, final IMAPStore imapStore,
			final Session session) throws MessagingException {
		/*
		 * Check for namespace folder
		 */
		{
			final Folder[] personalFolders = NamespaceFoldersCache.getPersonalNamespaces(imapStore, true, session);
			for (int i = 0; i < personalFolders.length; i++) {
				if (personalFolders[i].getFullName().startsWith(fullname)) {
					return true;
				}
			}
		}
		{
			final Folder[] userFolders = NamespaceFoldersCache.getUserNamespaces(imapStore, true, session);
			for (int i = 0; i < userFolders.length; i++) {
				if (userFolders[i].getFullName().startsWith(fullname)) {
					return true;
				}
				final NamespaceFolder nsf = new NamespaceFolder(imapStore, userFolders[i].getFullName(), userFolders[i]
						.getSeparator());
				final Folder[] subFolders = nsf.list();
				for (int j = 0; j < subFolders.length; j++) {
					if (subFolders[j].getFullName().startsWith(fullname)) {
						return true;
					}
				}
			}
		}
		{
			final Folder[] sharedFolders = NamespaceFoldersCache.getSharedNamespaces(imapStore, true, session);
			for (int i = 0; i < sharedFolders.length; i++) {
				if (sharedFolders[i].getFullName().startsWith(fullname)) {
					return true;
				}
			}
		}
		return false;
	}

	private static final String STR_MAILBOX_NOT_EXISTS = "NO Mailbox does not exist";

	private static final String STR_FULL_RIGHTS = "acdilprsw";

	private static Rights getOwnRightsInternal(final IMAPFolder folder, final Session session,
			final IMAPConfig imapConfig) {
		if (folder instanceof DefaultFolder) {
			return null;
		}
		final Rights retval;
		if (imapConfig.isSupportsACLs()) {
			try {
				retval = RightsCache.getCachedRights(folder, true, session);
			} catch (final MessagingException e) {
				if ((e.getNextException() instanceof com.sun.mail.iap.CommandFailedException)
						&& (e.getNextException().getMessage().indexOf(STR_MAILBOX_NOT_EXISTS) != -1)) {
					/*
					 * This occurs when requesting MYRIGHTS on a shared folder.
					 * Just log a warning!
					 */
					if (LOG.isWarnEnabled()) {
						LOG.warn(IMAPException.getFormattedMessage(IMAPException.Code.FOLDER_NOT_FOUND, folder
								.getFullName()), e);
					}
				} else {
					LOG.error(e.getMessage(), e);
				}
				/*
				 * Write empty string as rights. Nevertheless user may see
				 * folder!
				 */
				return (Rights) RIGHTS_EMPTY.clone();
			} catch (final Throwable t) {
				LOG.error(t.getMessage(), t);
				/*
				 * Write empty string as rights. Nevertheless user may see
				 * folder!
				 */
				return (Rights) RIGHTS_EMPTY.clone();
			}
		} else {
			/*
			 * No ACLs enabled. User has full access.
			 */
			retval = new Rights(STR_FULL_RIGHTS);
		}
		return retval;
	}

	/**
	 * Looks for attribute <code>\HasChildren</code> in provided attributes
	 * 
	 * @param li
	 *            The list info
	 * @return <code>true</code> if attribute <code>\HasChildren</code> is
	 *         present; otherwise <code>false</code>
	 */
	private static boolean hasChildren(final ListInfo[] li) {
		if (null != li) {
			for (int i = 0; i < li.length; i++) {
				final String[] tmpAttrs = li[i].attrs;
				for (final String attribute : tmpAttrs) {
					if (ATTRIBUTE_HAS_CHILDREN.equalsIgnoreCase(attribute)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Which entry in <code>li</code> matches <code>lname</code>? If the
	 * name contains wildcards, more than one entry may be returned.
	 */
	private static int findName(final ListInfo[] li, final String lname) {
		int i;
		/*
		 * If the name contains a wildcard, there might be more than one
		 */
		for (i = 0; i < li.length; i++) {
			if (li[i].name.equals(lname)) {
				break;
			}
		}
		if (i >= li.length) {
			/*
			 * Nothing matched exactly. Use first one.
			 */
			i = 0;
		}
		return i;
	}

}

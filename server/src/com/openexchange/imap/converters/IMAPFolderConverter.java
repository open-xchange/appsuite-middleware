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

import static com.openexchange.mail.dataobjects.MailFolder.DEFAULT_FOLDER_ID;

import javax.mail.MessagingException;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.groupware.ldap.UserException;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.imap.ACLPermission;
import com.openexchange.imap.IMAPException;
import com.openexchange.imap.cache.RightsCache;
import com.openexchange.imap.cache.UserFlagsCache;
import com.openexchange.imap.config.IMAPConfig;
import com.openexchange.imap.user2acl.User2ACL;
import com.openexchange.imap.user2acl.User2ACLArgs;
import com.openexchange.imap.user2acl.User2ACLInit.IMAPServer;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailSessionParameterNames;
import com.openexchange.mail.config.MailConfigException;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.mail.utils.StorageUtility;
import com.openexchange.sessiond.Session;
import com.openexchange.sessiond.impl.SessionObject;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.ACL;
import com.sun.mail.imap.DefaultFolder;
import com.sun.mail.imap.IMAPFolder;
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

	private static final class MyUser2ACLArgs implements User2ACLArgs {

		private final int sessionUser;

		private final String fullname;

		private final char separator;

		public MyUser2ACLArgs(final int sessionUser, final String fullname, final char separator) {
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
			throw new User2ACL.User2ACLException(User2ACL.User2ACLException.Code.UNKNOWN_IMAP_SERVER, imapServer
					.getName());

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
	 * @return An appropiate implementation of {@link User2ACLArgs}
	 * @throws MessagingException
	 *             If IMAP folder's attributes cannot be accessed
	 */
	public static User2ACLArgs getUser2AclArgs(final SessionObject session, final IMAPFolder imapFolder)
			throws MessagingException {
		return new MyUser2ACLArgs(session.getUserId(), imapFolder.getFullName(), imapFolder.getSeparator());
	}

	private static final String STR_INBOX = "INBOX";

	/**
	 * Creates a folder data object from given IMAP folder
	 * 
	 * @param imapFolder
	 *            The IMAP folder
	 * @param session
	 *            The session
	 * @return an instance of <code>{@link MailFolder}</code> containing the
	 *         attributes from given IMAP folder
	 * @throws MailException
	 *             If conversion fails
	 */
	public static MailFolder convertFolder(final IMAPFolder imapFolder, final SessionObject session,
			final IMAPConfig imapConfig) throws MailException {
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
			mailFolder.setSeparator(imapFolder.getSeparator());
			if (mailFolder.isRootFolder()) {
				mailFolder.setFullname(DEFAULT_FOLDER_ID);
			} else {
				mailFolder.setFullname(StorageUtility.prepareFullname(imapFolder.getFullName(), mailFolder
						.getSeparator()));
			}
			mailFolder.setName(imapFolder.getName());
			mailFolder.setParentFullname(prepareParentFullname(imapFolder.getParent()));
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
			final Rights ownRights = mailFolder.exists() && mailFolder.isHoldsMessages() ? getOwnRightsInternal(
					imapFolder, session, imapConfig) : (Rights) RIGHTS_EMPTY.clone();
			{
				final ACLPermission imapPermission = new ACLPermission(session);
				imapPermission.setEntity(session.getUserId());
				imapPermission.parseRights(ownRights);
				mailFolder.setOwnPermission(imapPermission);
			}
			if (!mailFolder.isRootFolder()) {
				/*
				 * Default folder
				 */
				if (STR_INBOX.equals(imapFolder.getFullName())) {
					mailFolder.setDefaulFolder(true);
				} else if (isDefaultFoldersChecked(session)) {
					final int len = UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(),
							session.getContext()).isSpamEnabled() ? 6 : 4;
					for (int i = 0; (i < len) && !mailFolder.isDefaulFolder(); i++) {
						if (mailFolder.getFullname().equals(getDefaultMailFolder(i, session))) {
							mailFolder.setDefaulFolder(true);
						}
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
			}
			mailFolder.setSubscribed(imapFolder.isSubscribed());
			if (imapConfig.isSupportsACLs() && mailFolder.isHoldsMessages() && mailFolder.exists()
					&& (ownRights.contains(Rights.Right.READ) || ownRights.contains(Rights.Right.ADMINISTER))
					&& !(imapFolder instanceof DefaultFolder)) {
				try {
					applyACL2Permissions(imapFolder, session, imapConfig, mailFolder, ownRights);
				} catch (final AbstractOXException e) {
					if (LOG.isWarnEnabled()) {
						LOG.warn("ACLs could not be parsed", e);
					}
					mailFolder.removePermissions();
				}
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
	 * @throws AbstractOXException
	 *             If ACLs cannot be mapped
	 */
	private static void applyACL2Permissions(final IMAPFolder imapFolder, final SessionObject session,
			final IMAPConfig imapConfig, final MailFolder mailFolder, final Rights ownRights)
			throws AbstractOXException {
		if (IMAPConfig.hasNewACLExt(imapConfig.getServer()) && !ownRights.contains(Rights.Right.ADMINISTER)) {
			addOwnACL(session, mailFolder, ownRights);
		} else {
			try {
				final ACL[] acls = imapFolder.getACL();
				final UserStorage userStorage = UserStorage.getInstance(session.getContext());
				final User2ACLArgs args = new MyUser2ACLArgs(session.getUserId(), imapFolder.getFullName(), imapFolder
						.getSeparator());
				for (int j = 0; j < acls.length; j++) {
					final ACLPermission aclPerm = new ACLPermission(session, userStorage);
					try {
						aclPerm.parseACL(acls[j], args);
						mailFolder.addPermission(aclPerm);
					} catch (final AbstractOXException e) {
						if (isUnknownEntityError(e)) {
							if (LOG.isDebugEnabled()) {
								LOG.debug(new StringBuilder(128).append("Cannot map ACL entity named \"").append(
										acls[j].getName()).append("\" to a system user").toString(), e);
							}
						} else {
							throw e;
						}
					}
				}
			} catch (final MessagingException e) {
				if (LOG.isWarnEnabled()) {
					LOG.warn(new StringBuilder(256).append("ACLs could not be requested for folder ").append(
							imapFolder.getFullName()).append(
							". A newer ACL extension (RFC 4314) seems to be supported by IMAP server ").append(
							imapConfig.getServer()).append(
							" which denies GETACL command if no ADMINISTER right is granted."), e);
				}
				/*
				 * Remember newer IMAP server's ACL extension
				 */
				IMAPConfig.setNewACLExt(imapConfig.getServer(), true);
				addOwnACL(session, mailFolder, ownRights);
			}
		}
	}

	/**
	 * Adds current user's rights granted to IMAP folder as an ACL
	 * 
	 * @param session
	 *            The session prividing needed user data
	 * @param mailFolder
	 *            The mail folder to whom the ACL should be applied
	 * @param ownRights
	 *            The rights to add as an ACL
	 */
	private static void addOwnACL(final SessionObject session, final MailFolder mailFolder, final Rights ownRights) {
		final ACLPermission aclPerm = new ACLPermission(session);
		aclPerm.setEntity(session.getUserId());
		aclPerm.parseRights(ownRights);
		mailFolder.addPermission(aclPerm);
	}

	private static boolean isUnknownEntityError(final AbstractOXException e) {
		return Component.USER.equals(e.getComponent())
				&& (UserException.Code.USER_NOT_FOUND.getDetailNumber() == e.getDetailNumber() || LdapException.Code.USER_NOT_FOUND
						.getDetailNumber() == e.getDetailNumber());
	}

	private static final String STR_MAILBOX_NOT_EXISTS = "NO Mailbox does not exist";

	private static final String STR_FULL_RIGHTS = "acdilprsw";

	private static Rights getOwnRightsInternal(final IMAPFolder folder, final SessionObject session,
			final IMAPConfig imapConfig) throws MessagingException, MailConfigException {
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
								.getFullName()));
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
		if ((folder.getType() & javax.mail.Folder.HOLDS_FOLDERS) == 0) {
			/*
			 * NoInferiors detected: No create access
			 */
			retval.remove(Rights.Right.CREATE);
		} else if ((folder.getType() & javax.mail.Folder.HOLDS_MESSAGES) == 0) {
			/*
			 * NoSelect detected: No read access
			 */
			retval.remove(Rights.Right.READ);
		}
		return retval;
	}

	private static String prepareParentFullname(final javax.mail.Folder parent) throws MessagingException {
		final StringBuilder sb = new StringBuilder(32).append(DEFAULT_FOLDER_ID);
		if (parent instanceof DefaultFolder) {
			return sb.toString();
		} else if (parent == null) {
			return null;
		}
		return sb.append(parent.getSeparator()).append(parent.getFullName()).toString();
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

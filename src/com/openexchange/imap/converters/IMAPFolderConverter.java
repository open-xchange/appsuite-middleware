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
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.imap.ACLPermission;
import com.openexchange.imap.IMAPException;
import com.openexchange.imap.cache.RightsCache;
import com.openexchange.imap.cache.UserFlagsCache;
import com.openexchange.imap.config.IMAPConfig;
import com.openexchange.imap.user2acl.User2ACL;
import com.openexchange.imap.user2acl.User2ACLArgs;
import com.openexchange.imap.user2acl.User2ACL.IMAPServer;
import com.openexchange.mail.MailException;
import com.openexchange.mail.config.MailConfigException;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.utils.StorageUtility;
import com.openexchange.sessiond.SessionObject;
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
		return new MyUser2ACLArgs(session.getUserObject().getId(), imapFolder.getFullName(), imapFolder.getSeparator());
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
	public static MailFolder convertFolder(final IMAPFolder imapFolder, final SessionObject session)
			throws MailException {
		try {
			final MailFolder mailFolder = new MailFolder();
			mailFolder.setRootFolder(imapFolder instanceof DefaultFolder);
			mailFolder.setExists(imapFolder.exists());
			final String[] attrs = imapFolder.getAttributes();
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
				mailFolder.setSubfolders(false);
				Attribs: for (final String attribute : attrs) {
					if (ATTRIBUTE_HAS_CHILDREN.equalsIgnoreCase(attribute)) {
						mailFolder.setSubfolders(true);
						break Attribs;
					}
				}
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
			mailFolder
					.setHoldsMessages(mailFolder.exists() ? ((imapFolder.getType() & javax.mail.Folder.HOLDS_MESSAGES) > 0)
							: false);
			final Rights ownRights = mailFolder.exists() && mailFolder.isHoldsMessages() ? getOwnRightsInternal(
					imapFolder, session) : (Rights) RIGHTS_EMPTY.clone();
			final UserStorage userStorage = UserStorage.getInstance(session.getContext());
			{
				final ACLPermission imapPermission = new ACLPermission(session, userStorage);
				imapPermission.setEntity(session.getUserObject().getId());
				imapPermission.parseRights(ownRights);
				mailFolder.setOwnPermission(imapPermission);
			}
			if (!mailFolder.isRootFolder()) {
				/*
				 * Default folder
				 */
				if (STR_INBOX.equals(imapFolder.getFullName())) {
					mailFolder.setDefaulFolder(true);
				} else if (session.isMailFldsChecked()) {
					final int len = session.getUserSettingMail().isSpamEnabled() ? 6 : 4;
					for (int i = 0; (i < len) && !mailFolder.isDefaulFolder(); i++) {
						if (mailFolder.getFullname().equals(session.getDefaultMailFolder(i))) {
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
			if (IMAPConfig.isSupportsACLs() && mailFolder.isHoldsMessages() && mailFolder.exists()
					&& (ownRights.contains(Rights.Right.READ) || ownRights.contains(Rights.Right.ADMINISTER))
					&& !(imapFolder instanceof DefaultFolder)) {
				try {
					final ACL[] acls = imapFolder.getACL();
					final User2ACLArgs info = new MyUser2ACLArgs(session.getUserObject().getId(), imapFolder
							.getFullName(), imapFolder.getSeparator());
					for (int j = 0; j < acls.length; j++) {
						final ACLPermission imapPerm = new ACLPermission(session, userStorage);
						imapPerm.parseACL(acls[j], info);
						mailFolder.addPermission(imapPerm);
					}
				} catch (final MessagingException e) {
					if (LOG.isWarnEnabled()) {
						LOG.warn(new StringBuilder("ACL could not be requested for folder ").append(imapFolder
								.getFullName()), e);
					}
					mailFolder.removePermissions();
				} catch (final AbstractOXException e) {
					if (LOG.isWarnEnabled()) {
						LOG.warn("ACL could not be parsed", e);
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
		} catch (final LdapException e) {
			throw new IMAPException(e);
		}
	}

	private static final String STR_MAILBOX_NOT_EXISTS = "NO Mailbox does not exist";

	private static final String STR_FULL_RIGHTS = "acdilprsw";

	private static Rights getOwnRightsInternal(final IMAPFolder folder, final SessionObject session)
			throws MessagingException, MailConfigException {
		if (folder instanceof DefaultFolder) {
			return null;
		}
		final Rights retval;
		if (IMAPConfig.isSupportsACLs()) {
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

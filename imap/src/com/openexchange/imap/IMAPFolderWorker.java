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

import static com.openexchange.mail.dataobjects.MailFolder.DEFAULT_FOLDER_ID;

import java.io.Serializable;
import java.util.Set;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.MessagingException;

import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.imap.cache.RightsCache;
import com.openexchange.imap.config.IMAPConfig;
import com.openexchange.mail.MailException;
import com.openexchange.mail.api.MailMessageStorage;
import com.openexchange.mail.mime.MIMESessionPropertyNames;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.session.Session;
import com.sun.mail.imap.DefaultFolder;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.Rights;
import com.sun.mail.imap.Rights.Right;

/**
 * {@link IMAPFolderWorker} - An abstract class that extends
 * {@link MailMessageStorage} by convenience methods for working on a certain
 * IMAP folder.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public abstract class IMAPFolderWorker extends MailMessageStorage implements Serializable {

	private static final long serialVersionUID = -3802840535524866003L;

	private static final transient org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(IMAPFolderWorker.class);

	protected static final String WARN_FLD_ALREADY_CLOSED = "Invoked close() on a closed folder";

	protected static final String STR_INBOX = "INBOX";

	protected static final String STR_FALSE = "false";

	protected static final Flags FLAGS_SEEN = new Flags(Flags.Flag.SEEN);

	/*
	 * Fields
	 */
	protected final transient IMAPStore imapStore;

	protected final transient Session session;

	protected final transient Context ctx;

	protected final IMAPAccess imapAccess;

	protected final UserSettingMail usm;

	protected final transient IMAPConfig imapConfig;

	protected transient IMAPFolder imapFolder;

	protected int holdsMessages = -1;

	/**
	 * Initializes a new {@link IMAPFolderWorker}
	 * 
	 * @param imapStore
	 *            The IMAP store
	 * @param imapAccess
	 *            The IMAP access
	 * @param session
	 *            The session providing needed user data
	 * @throws IMAPException
	 *             If context lading fails
	 */
	public IMAPFolderWorker(final IMAPStore imapStore, final IMAPAccess imapAccess, final Session session)
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
		this.usm = UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(), ctx);
		this.imapConfig = imapAccess.getIMAPConfig();
	}

	@Override
	public void releaseResources() throws MailException {
		if (null != imapFolder) {
			closeIMAPFolder();
		}
	}

	/**
	 * Reports a modification of the IMAP folder denoted by specified fullname.
	 * If stored IMAP folder's fullname equals specified fullname, it is closed
	 * quietly.
	 * 
	 * @param modifiedFullname
	 *            The fullname of the folder which has been modified
	 */
	public void notifyIMAPFolderModification(final String modifiedFullname) {
		if (null == imapFolder || !modifiedFullname.equals(imapFolder.getFullName())) {
			/*
			 * Modified folder did not affect remembered IMAP folder
			 */
			return;
		}
		try {
			closeIMAPFolder();
		} catch (final Exception e) {
			LOG.error(e.getMessage(), e);
		}

	}

	/**
	 * Reports a modification of the IMAP folders denoted by specified set of
	 * fullnames. If stored IMAP folder's fullname is contained in set of
	 * fullnames, it is closed quietly.
	 * 
	 * @param modifiedFullnames
	 *            The fullnames of the folders which have been modified
	 */
	public void notifyIMAPFolderModification(final Set<String> modifiedFullnames) {
		if (null == imapFolder || !modifiedFullnames.contains(imapFolder.getFullName())) {
			/*
			 * Modified folders did not affect remembered IMAP folder
			 */
			return;
		}
		try {
			closeIMAPFolder();
		} catch (final Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}

	private void closeIMAPFolder() throws MailException {
		try {
			imapFolder.close(false);
		} catch (final IllegalStateException e) {
			LOG.warn(WARN_FLD_ALREADY_CLOSED, e);
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapConfig);
		} finally {
			resetIMAPFolder();
		}
	}

	/**
	 * Resets the IMAP folder by setting field {@link #imapFolder} to
	 * <code>null</code> and field {@link #holdsMessages} to <code>-1</code>.
	 */
	protected void resetIMAPFolder() {
		holdsMessages = -1;
		imapFolder = null;
	}

	/**
	 * Determine if field {@link #imapFolder} indicates to hold messages.<br>
	 * <b>NOTE</b>: This method assumes that field {@link #imapFolder} is
	 * <b>not</b> <code>null</code>.
	 * 
	 * <pre>
	 * return ((imapFolder.getType() &amp; IMAPFolder.HOLDS_MESSAGES) == 1)
	 * </pre>
	 * 
	 * @return <code>true</code> if field {@link #imapFolder} indicates to
	 *         hold messages
	 * @throws MessagingException
	 *             If a messaging error occurs
	 */
	protected boolean holdsMessages() throws MessagingException {
		if (holdsMessages == -1) {
			holdsMessages = ((imapFolder.getType() & IMAPFolder.HOLDS_MESSAGES) == 0) ? 0 : 1;
		}
		return holdsMessages > 0;
	}

	/**
	 * Sets and opens (only if exists) the folder in a safe manner, checks if
	 * selectable and for right {@link Right#READ}
	 * 
	 * @param fullname
	 *            The folder fullname
	 * @param desiredMode
	 *            The desired opening mode (either {@link Folder#READ_ONLY} or
	 *            {@link Folder#READ_WRITE})
	 * @return The properly opened IMAP folder
	 * @throws MessagingException
	 *             If a messaging error occurs
	 * @throws IMAPException
	 *             If user does not hold sufficient rights to open the IMAP
	 *             folder in desired mode
	 */
	protected final IMAPFolder setAndOpenFolder(final String fullname, final int desiredMode)
			throws MessagingException, MailException {
		return setAndOpenFolder(null, fullname, desiredMode);
	}

	/**
	 * Sets and opens (only if exists) the folder in a safe manner, checks if
	 * selectable and for right {@link Right#READ}
	 * 
	 * @param imapFolder
	 *            The IMAP folder to check against
	 * @param fullname
	 *            The folder fullname
	 * @param desiredMode
	 *            The desired opening mode (either {@link Folder#READ_ONLY} or
	 *            {@link Folder#READ_WRITE})
	 * @return The properly opened IMAP folder
	 * @throws MessagingException
	 *             If a messaging error occurs
	 * @throws IMAPException
	 *             If user does not hold sufficient rights to open the IMAP
	 *             folder in desired mode
	 */
	protected final IMAPFolder setAndOpenFolder(final IMAPFolder imapFolder, final String fullname,
			final int desiredMode) throws MessagingException, MailException {
		final boolean isDefaultFolder = fullname.equals(DEFAULT_FOLDER_ID);
		final boolean isIdenticalFolder;
		if (isDefaultFolder) {
			isIdenticalFolder = (imapFolder == null ? false : imapFolder instanceof DefaultFolder);
		} else {
			isIdenticalFolder = (imapFolder == null ? false : imapFolder.getFullName().equals(fullname));
		}
		if (imapFolder != null) {
			IMAPCommandsCollection.forceNoopCommand(imapFolder);
			try {
				/*
				 * This call also checks if folder is opened
				 */
				final int mode = imapFolder.getMode();
				if (isIdenticalFolder && imapFolder.isOpen() && mode >= desiredMode) {
					/*
					 * Identical folder is already opened in an appropriate
					 * mode.
					 */
					// IMAPCommandsCollection.updateIMAPFolder(imapFolder,
					// mode);
					return imapFolder;
				}
				/*
				 * Folder is open, so close folder
				 */
				try {
					imapFolder.close(false);
				} finally {
					if (imapFolder == this.imapFolder) {
						resetIMAPFolder();
					}
				}
			} catch (final IllegalStateException e) {
				/*
				 * Folder not open
				 */
				LOG.warn(WARN_FLD_ALREADY_CLOSED, e);
			}
			/*
			 * Folder is closed here
			 */
			if (isIdenticalFolder) {
				try {
					if ((imapFolder.getType() & Folder.HOLDS_MESSAGES) == 0) { // NoSelect
						throw new IMAPException(IMAPException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, imapFolder
								.getFullName());
					} else if (imapConfig.isSupportsACLs()
							&& !RightsCache.getCachedRights(imapFolder, true, session).contains(Rights.Right.READ)) {
						throw new IMAPException(IMAPException.Code.NO_FOLDER_OPEN, imapFolder.getFullName());
					}
				} catch (final MessagingException e) { // No access
					throw new IMAPException(IMAPException.Code.NO_ACCESS, imapFolder.getFullName());
				}
				if (desiredMode == Folder.READ_WRITE
						&& ((imapFolder.getType() & Folder.HOLDS_MESSAGES) == 0)
						&& STR_FALSE.equalsIgnoreCase(imapAccess.getMailProperties().getProperty(
								MIMESessionPropertyNames.PROP_ALLOWREADONLYSELECT, STR_FALSE))
						&& IMAPCommandsCollection.isReadOnly(imapFolder)) {
					throw new IMAPException(IMAPException.Code.READ_ONLY_FOLDER, imapFolder.getFullName());
				}
				/*
				 * Open identical folder in right mode
				 */
				imapFolder.open(desiredMode);
				return imapFolder;
			}
		}
		final IMAPFolder retval = (isDefaultFolder ? (IMAPFolder) imapStore.getDefaultFolder() : (IMAPFolder) imapStore
				.getFolder(fullname));
		if (!isDefaultFolder && !retval.exists()) {
			throw new IMAPException(IMAPException.Code.FOLDER_NOT_FOUND, retval.getFullName());
		} else if (desiredMode != Folder.READ_ONLY && desiredMode != Folder.READ_WRITE) {
			throw new IMAPException(IMAPException.Code.UNKNOWN_FOLDER_MODE, Integer.valueOf(desiredMode));
		}
		try {
			if ((retval.getType() & Folder.HOLDS_MESSAGES) == 0) { // NoSelect
				throw new IMAPException(IMAPException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, retval.getFullName());
			} else if (imapConfig.isSupportsACLs()
					&& !RightsCache.getCachedRights(retval, true, session).contains(Rights.Right.READ)) {
				throw new IMAPException(IMAPException.Code.NO_FOLDER_OPEN, retval.getFullName());
			}
		} catch (final MessagingException e) {
			throw new IMAPException(IMAPException.Code.NO_ACCESS, retval.getFullName());
		}
		if (desiredMode == Folder.READ_WRITE
				&& ((retval.getType() & Folder.HOLDS_MESSAGES) == 0)
				&& STR_FALSE.equalsIgnoreCase(imapAccess.getMailProperties().getProperty(
						MIMESessionPropertyNames.PROP_ALLOWREADONLYSELECT, STR_FALSE))
				&& IMAPCommandsCollection.isReadOnly(retval)) {
			throw new IMAPException(IMAPException.Code.READ_ONLY_FOLDER, retval.getFullName());
		}
		retval.open(desiredMode);
		return retval;
	}
}

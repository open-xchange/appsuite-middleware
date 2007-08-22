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
import static com.openexchange.mail.imap.IMAPStorageUtils.prepareMailFolderParam;

import java.io.Serializable;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.ReadOnlyFolderException;

import com.openexchange.imap.IMAPProperties;
import com.openexchange.imap.IMAPPropertyException;
import com.openexchange.imap.IMAPUtils;
import com.openexchange.imap.OXMailException;
import com.openexchange.imap.OXMailException.MailCode;
import com.openexchange.imap.command.FlagsIMAPCommand;
import com.openexchange.mail.MailMessageStorage;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.imap.converters.IMAPMessageConverter;
import com.openexchange.mail.search.MailSearchTerm;
import com.openexchange.sessiond.SessionObject;
import com.sun.mail.imap.DefaultFolder;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.Rights;

/**
 * IMAPMessageStorage
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class IMAPMessageStorage extends MailMessageStorage implements Serializable {

	private static final transient org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(IMAPMessageStorage.class);

	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = 1467121647337217270L;

	/*
	 * String constants
	 */
	private static final String STR_INBOX = "INBOX";

	private static final String STR_FALSE = "false";

	private static final String WARN_FLD_ALREADY_CLOSED = "Invoked close() on a closed folder";

	private static final String ERROR_KEEP_SEEN = "/SEEN flag cannot be set: ";

	/*
	 * Flag constants
	 */
	private static final Flags FLAGS_SEEN = new Flags(Flags.Flag.SEEN);

	private static final Flags FLAGS_DRAFT = new Flags(Flags.Flag.DRAFT);

	private final transient IMAPStore imapStore;

	private final transient IMAPMailConnection imapMailConnection;

	private final transient SessionObject session;

	private transient IMAPFolder imapFolder;

	private int holdsMessages = -1;

	private transient Message markAsSeen;

	/**
	 * Constructor
	 * 
	 * @param imapStore
	 *            The <b>connected</b> IMAP store that provides access to IMAP
	 *            server
	 */
	public IMAPMessageStorage(final IMAPStore imapStore, final IMAPMailConnection imapMailConnection,
			final SessionObject session) {
		super();
		this.imapStore = imapStore;
		this.imapMailConnection = imapMailConnection;
		this.session = session;
	}

	private void resetIMAPFolder() {
		holdsMessages = -1;
		imapFolder = null;
	}

	private boolean holdsMessages() throws MessagingException {
		if (holdsMessages == -1) {
			holdsMessages = ((imapFolder.getType() & Folder.HOLDS_MESSAGES) == 0) ? 0 : 1;
		}
		return holdsMessages > 0;
	}

	private void keepSeen() {
		if (markAsSeen == null) {
			return;
		} else if (imapFolder == null) {
			return;
		}
		try {
			if (!holdsMessages()) {
				/*
				 * Folder is not selectable, further working working on this
				 * folder will result in an IMAP error telling "Mailbox does not
				 * exist".
				 */
				return;
			} else if (IMAPProperties.isSupportsACLs()) {
				/*
				 * Check \KEEP_SEEN right
				 */
				try {
					if (!session.getCachedRights(imapFolder, true).contains(Rights.Right.KEEP_SEEN)) {
						/*
						 * User has no \KEEP_SEEN right
						 */
						if (LOG.isWarnEnabled()) {
							LOG.warn(new StringBuilder(ERROR_KEEP_SEEN).append("Missing KEEP_SEEN right").toString());
						}
						return;
					}
				} catch (final MessagingException e) {
					if (LOG.isWarnEnabled()) {
						LOG.warn(new StringBuilder(ERROR_KEEP_SEEN).append(e.getMessage()).toString(), e);
					}
					return;
				}
			}
			try {
				if (imapFolder.getMode() == Folder.READ_ONLY) {
					boolean decrement = true;
					try {
						imapFolder.close(false);
					} catch (final IllegalStateException e) {
						LOG.warn(WARN_FLD_ALREADY_CLOSED, e);
						decrement = false;
					} finally {
						if (decrement) {
							// TODO:
							// mailInterfaceMonitor.changeNumActive(false);
						}
					}
					imapFolder.open(Folder.READ_WRITE);
					// TODO: mailInterfaceMonitor.changeNumActive(true);
				}
			} catch (final IllegalStateException e) {
				LOG.warn(WARN_FLD_ALREADY_CLOSED, e);
				/*
				 * Folder is closed
				 */
				try {
					imapFolder.open(Folder.READ_WRITE);
					// TODO: mailInterfaceMonitor.changeNumActive(true);
				} catch (final ReadOnlyFolderException e1) {
					LOG.error(new StringBuilder(ERROR_KEEP_SEEN).append(e1.getMessage()).toString(), e1);
					return;
				}
			}
			markAsSeen.setFlags(FLAGS_SEEN, true);
		} catch (final MessageRemovedException e) {
			if (LOG.isWarnEnabled()) {
				LOG.warn(new StringBuilder(ERROR_KEEP_SEEN).append(e.getMessage()).toString(), e);
			}
			return;
		} catch (final MessagingException e) {
			if (LOG.isErrorEnabled()) {
				LOG.error(new StringBuilder(ERROR_KEEP_SEEN).append(e.getMessage()).toString(), e);
			}
			return;
		} catch (final IMAPPropertyException e) {
			if (LOG.isErrorEnabled()) {
				LOG.error(new StringBuilder(ERROR_KEEP_SEEN).append(e.getMessage()).toString(), e);
			}
			return;
		} finally {
			markAsSeen = null;
		}
	}

	/**
	 * Sets and opens (only if exists) the folder in a safe manner
	 * 
	 * @param folderName
	 *            The folder fullname
	 * @param openMode
	 *            The opening mode (either {@link Folder#READ_ONLY} or
	 *            {@link Folder#READ_WRITE})
	 * @throws MessagingException
	 * @throws IMAPException
	 * @throws OXMailException
	 */
	private void setAndOpenFolder(final String folderName, final int openMode) throws MessagingException,
			IMAPException, OXMailException {
		final boolean isDefaultFolder = folderName.equals(DEFAULT_IMAP_FOLDER_ID);
		final boolean isIdenticalFolder;
		if (isDefaultFolder) {
			isIdenticalFolder = (imapFolder == null ? false : imapFolder instanceof DefaultFolder);
		} else {
			isIdenticalFolder = (imapFolder == null ? false : imapFolder.getFullName().equals(folderName));
		}
		if (imapFolder != null) {
			IMAPUtils.forceNoopCommand(imapFolder);
			try {
				/*
				 * This call also checks if folder is opened
				 */
				final int mode = imapFolder.getMode();
				if (isIdenticalFolder && imapFolder.isOpen()) {
					if (mode == openMode) {
						/*
						 * Identical folder is already opened in right mode
						 */
						return;
					} else if (mode == Folder.READ_WRITE && openMode == Folder.READ_ONLY) {
						/*
						 * Although folder is opened read-write instead of
						 * read-only, all operations allowed in read-only also
						 * work in read-write. Therefore return here.
						 */
						return;
					}
				}
				/*
				 * Folder is open, check \SEEN flag and close it
				 */
				if (markAsSeen != null) {
					/*
					 * Mark stored message as seen
					 */
					keepSeen();
				}
				try {
					imapFolder.close(false);
				} finally {
					// TODO: mailInterfaceMonitor.changeNumActive(false);
					resetIMAPFolder();
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
				if (openMode == Folder.READ_WRITE
						&& !holdsMessages() // NoSelect
						&& STR_FALSE.equalsIgnoreCase(imapMailConnection.getMailProperties().getProperty(
								IMAPPropertyNames.PROP_ALLOWREADONLYSELECT, STR_FALSE))
						&& IMAPUtils.isReadOnly(imapFolder)) {
					throw new IMAPException(IMAPException.Code.READ_ONLY_FOLDER, imapFolder.getFullName());
				}
				/*
				 * Open identical folder in right mode
				 */
				imapFolder.open(openMode);
				// TODO: mailInterfaceMonitor.changeNumActive(true);
				return;
			}
		}
		imapFolder = (isDefaultFolder ? (IMAPFolder) imapStore.getDefaultFolder() : (IMAPFolder) imapStore
				.getFolder(folderName));
		if (!isDefaultFolder && !imapFolder.exists()) {
			throw new IMAPException(IMAPException.Code.FOLDER_NOT_FOUND, imapFolder.getFullName());
		}
		if (openMode != Folder.READ_ONLY && openMode != Folder.READ_WRITE) {
			throw new OXMailException(MailCode.UNKNOWN_FOLDER_MODE, Integer.valueOf(openMode));
		} else if (openMode == Folder.READ_WRITE
				&& !holdsMessages() // NoSelect
				&& STR_FALSE.equalsIgnoreCase(imapMailConnection.getMailProperties().getProperty(
						IMAPPropertyNames.PROP_ALLOWREADONLYSELECT, STR_FALSE)) && IMAPUtils.isReadOnly(imapFolder)) {
			throw new IMAPException(IMAPException.Code.READ_ONLY_FOLDER, imapFolder.getFullName());
		}
		imapFolder.open(openMode);
		// TODO: mailInterfaceMonitor.changeNumActive(true);
	}

	@Override
	public MailMessage getMessage(final String folderArg, final long msgUID) throws IMAPException {
		try {
			final String folder = prepareMailFolderParam(folderArg);
			if (DEFAULT_IMAP_FOLDER_ID.equals(folder)) {
				throw new IMAPException(IMAPException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, DEFAULT_IMAP_FOLDER_ID);
			}
			if (imapFolder == null) {
				/*
				 * Not initialized
				 */
				imapFolder = (IMAPFolder) (folder == null ? imapStore.getFolder(STR_INBOX) : imapStore
						.getFolder(folder));
			} else if (!imapFolder.getFullName().equals(folder)) {
				/*
				 * Another folder than previous one
				 */
				if (imapFolder.isOpen()) {
					if (markAsSeen != null) {
						/*
						 * Mark stored message as seen
						 */
						keepSeen();
					}
					try {
						imapFolder.close(false);
					} finally {
						// mailInterfaceMonitor.changeNumActive(false);
						resetIMAPFolder();
					}
				}
				imapFolder = (IMAPFolder) (folder == null ? imapStore.getFolder(STR_INBOX) : imapStore
						.getFolder(folder));
			}
			/*
			 * Open
			 */
			if (!imapFolder.isOpen()) {
				imapFolder.open(Folder.READ_WRITE);
				// TODO: mailInterfaceMonitor.changeNumActive(true);
			} else if (markAsSeen != null) {
				/*
				 * Folder is already open, mark stored message as seen
				 */
				keepSeen();
			}
			// final long start = System.currentTimeMillis();
			final IMAPMessage msg;
			// try {
			msg = (IMAPMessage) imapFolder.getMessageByUID(msgUID);
			// } finally {
			// TODO:
			// mailInterfaceMonitor.addUseTime(System.currentTimeMillis() -
			// start);
			// }
			if (msg == null) {
				throw new IMAPException(IMAPException.Code.MESSAGE_NOT_FOUND, String.valueOf(msgUID), imapFolder
						.toString());
			}
			if (imapFolder.getMode() == Folder.READ_WRITE && !msg.isExpunged()) {
				/*
				 * Check for drafts folder. This is done here, cause sometimes
				 * copy operation does not properly add \Draft flag.
				 */
				final boolean isDraftFld = imapFolder.getFullName().equals(
						prepareMailFolderParam(imapMailConnection.getFolderStorage().getDraftsFolder()));
				final boolean isDraft = msg.getFlags().contains(Flags.Flag.DRAFT);
				if (isDraftFld && !isDraft) {
					try {
						msg.setFlags(FLAGS_DRAFT, true);
					} catch (final MessagingException e) {
						if (LOG.isWarnEnabled()) {
							LOG.warn(IMAPException.getFormattedMessage(IMAPException.Code.FLAG_FAILED,
									FlagsIMAPCommand.FLAG_DRAFT, String.valueOf(msg.getMessageNumber()), imapFolder
											.getFullName(), e.getMessage()), e);
						}
					}
				} else if (!isDraftFld && isDraft) {
					try {
						msg.setFlags(FLAGS_DRAFT, false);
					} catch (final MessagingException e) {
						if (LOG.isWarnEnabled()) {
							LOG.warn(IMAPException.getFormattedMessage(IMAPException.Code.FLAG_FAILED,
									FlagsIMAPCommand.FLAG_DRAFT, String.valueOf(msg.getMessageNumber()), imapFolder
											.getFullName(), e.getMessage()), e);
						}
					}
				}
			}
			markAsSeen = msg;
			return IMAPMessageConverter.convertIMAPMessage(msg);
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapMailConnection);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.MailMessageStorage#getUnreadMessages(java.lang.String)
	 */
	@Override
	public MailMessage[] getUnreadMessages(final String fullname) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.MailMessageStorage#searchMessages(java.lang.String)
	 */
	@Override
	public MailMessage[] searchMessages(final String fullname, final MailSearchTerm searchTerm) {
		// TODO Auto-generated method stub
		return null;
	}

}

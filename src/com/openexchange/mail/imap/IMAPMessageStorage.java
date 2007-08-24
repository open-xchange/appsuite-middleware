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
import java.util.Arrays;

import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.ReadOnlyFolderException;

import com.openexchange.api2.MailInterfaceImpl;
import com.openexchange.groupware.container.mail.MessageCacheObject;
import com.openexchange.imap.IMAPProperties;
import com.openexchange.imap.IMAPPropertyException;
import com.openexchange.imap.MessageHeaders;
import com.openexchange.imap.OXMailException;
import com.openexchange.imap.UserSettingMail;
import com.openexchange.imap.OXMailException.MailCode;
import com.openexchange.imap.command.CopyIMAPCommand;
import com.openexchange.imap.command.ExtractSpamMsgIMAPCommand;
import com.openexchange.imap.command.FetchIMAPCommand;
import com.openexchange.imap.command.FlagsIMAPCommand;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailListField;
import com.openexchange.mail.MailMessageStorage;
import com.openexchange.mail.MailStorageUtils.OrderDirection;
import com.openexchange.mail.dataobjects.MailContent;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.imap.converters.IMAPMessageConverter;
import com.openexchange.mail.imap.search.IMAPSearch;
import com.openexchange.mail.imap.sort.IMAPSort;
import com.openexchange.mail.parser.MailMessageParser;
import com.openexchange.mail.parser.handlers.AttachmentMessageHandler;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.Collections.SmartLongArray;
import com.sun.mail.iap.CommandFailedException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.AppendUID;
import com.sun.mail.imap.DefaultFolder;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.Rights;
import com.sun.mail.imap.protocol.BODYSTRUCTURE;

/**
 * {@link IMAPMessageStorage} - The IMAP specific implementation of
 * {@link MailMessageStorage}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class IMAPMessageStorage implements MailMessageStorage, Serializable {

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

	private static final Flags FLAGS_DELETED = new Flags(Flags.Flag.DELETED);

	/*
	 * Fields
	 */
	private final transient IMAPStore imapStore;

	private final transient IMAPMailConnection imapMailConnection;

	private final transient SessionObject session;

	private final transient UserSettingMail usm;

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
		this.usm = session.getUserSettingMail();
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
					try {
						imapFolder.close(false);
					} catch (final IllegalStateException e) {
						LOG.warn(WARN_FLD_ALREADY_CLOSED, e);
					}
					imapFolder.open(Folder.READ_WRITE);
				}
			} catch (final IllegalStateException e) {
				LOG.warn(WARN_FLD_ALREADY_CLOSED, e);
				/*
				 * Folder is closed
				 */
				try {
					imapFolder.open(Folder.READ_WRITE);
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
	 * @param fullname
	 *            The folder fullname
	 * @param desiredMode
	 *            The desired opening mode (either {@link Folder#READ_ONLY} or
	 *            {@link Folder#READ_WRITE})
	 * @return The properly opened IMAP folder
	 * @throws MessagingException
	 * @throws IMAPException
	 */
	private IMAPFolder setAndOpenFolder(final String fullname, final int desiredMode) throws MessagingException,
			IMAPException {
		return setAndOpenFolder(null, fullname, desiredMode);
	}

	/**
	 * Sets and opens (only if exists) the folder in a safe manner
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
	 * @throws IMAPException
	 */
	private IMAPFolder setAndOpenFolder(final IMAPFolder imapFolder, final String fullname, final int desiredMode)
			throws MessagingException, IMAPException {
		final boolean isDefaultFolder = fullname.equals(DEFAULT_IMAP_FOLDER_ID);
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
				if (isIdenticalFolder && imapFolder.isOpen()) {
					if (mode == desiredMode) {
						/*
						 * Identical folder is already opened in right mode
						 */
						return imapFolder;
					} else if (mode == Folder.READ_WRITE && desiredMode == Folder.READ_ONLY) {
						/*
						 * Although folder is opened read-write instead of
						 * read-only, all operations allowed in read-only also
						 * work in read-write. Therefore return here.
						 */
						return imapFolder;
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
				if (desiredMode == Folder.READ_WRITE
						&& !holdsMessages() // NoSelect
						&& STR_FALSE.equalsIgnoreCase(imapMailConnection.getMailProperties().getProperty(
								IMAPPropertyNames.PROP_ALLOWREADONLYSELECT, STR_FALSE))
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
		}
		if (desiredMode != Folder.READ_ONLY && desiredMode != Folder.READ_WRITE) {
			throw new IMAPException(IMAPException.Code.UNKNOWN_FOLDER_MODE, Integer.valueOf(desiredMode));
		} else if (desiredMode == Folder.READ_WRITE
				&& !holdsMessages() // NoSelect
				&& STR_FALSE.equalsIgnoreCase(imapMailConnection.getMailProperties().getProperty(
						IMAPPropertyNames.PROP_ALLOWREADONLYSELECT, STR_FALSE))
				&& IMAPCommandsCollection.isReadOnly(retval)) {
			throw new IMAPException(IMAPException.Code.READ_ONLY_FOLDER, retval.getFullName());
		}
		retval.open(desiredMode);
		return retval;
	}

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
			} else if (markAsSeen != null) {
				/*
				 * Folder is already open, mark stored message as seen
				 */
				keepSeen();
			}
			final long start = System.currentTimeMillis();
			final IMAPMessage msg = (IMAPMessage) imapFolder.getMessageByUID(msgUID);
			MailInterfaceImpl.mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
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

	private static final transient MailMessage[] EMPTY_RETVAL = new MailMessage[0];

	public MailMessage[] getMessages(final String folder, final int[] fromToIndices, final MailListField sortField,
			final OrderDirection order, final MailListField[] searchCols, final String[] searchPatterns,
			final boolean linkSearchTermsWithOR, final MailListField[] fields) throws IMAPException {
		try {
			imapFolder = setAndOpenFolder(imapFolder, prepareMailFolderParam(folder), Folder.READ_ONLY);
			try {
				if (!holdsMessages()) {
					throw new IMAPException(IMAPException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, imapFolder.getFullName());
				} else if (IMAPProperties.isSupportsACLs()
						&& !session.getCachedRights(imapFolder, true).contains(Rights.Right.READ)) {
					throw new IMAPException(IMAPException.Code.NO_READ_ACCESS, imapFolder.getFullName());
				}
			} catch (final MessagingException e) {
				throw new IMAPException(IMAPException.Code.NO_ACCESS, imapFolder.getFullName());
			}
			Message[] retval = null;
			/*
			 * Shall a search be performed?
			 */
			final boolean search = (searchCols != null && searchCols.length > 0 && searchPatterns != null && searchPatterns.length > 0);
			if (search) {
				/*
				 * Preselect message list according to given search pattern
				 */
				retval = IMAPSearch.searchMessages(imapFolder, searchCols, searchPatterns, linkSearchTermsWithOR,
						fields, sortField);
				if (retval == null || retval.length == 0) {
					return EMPTY_RETVAL;
				}
			}
			retval = IMAPSort.sortMessages(imapFolder, retval, fields, sortField, order, session.getLocale());
			if (fromToIndices != null && fromToIndices.length == 2) {
				final int fromIndex = fromToIndices[0];
				int toIndex = fromToIndices[1];
				if (retval == null || retval.length == 0) {
					return EMPTY_RETVAL;
				}
				if ((fromIndex) > retval.length) {
					/*
					 * Return empty iterator if start is out of range
					 */
					return EMPTY_RETVAL;
				}
				/*
				 * Reset end index if out of range
				 */
				if (toIndex > retval.length) {
					toIndex = retval.length;
				}
				final Message[] tmp = retval;
				final int retvalLength = toIndex - fromIndex + 1;
				retval = new Message[retvalLength];
				System.arraycopy(tmp, fromIndex, retval, 0, retvalLength);
			}
			return IMAPMessageConverter.convertIMAPMessages(retval, fields);
		} catch (final IMAPPropertyException e) {
			throw new IMAPException(e);
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapMailConnection);
		}
	}

	public MailMessage[] getUnreadMessages(final String folder, final MailListField sortField,
			final OrderDirection order, final MailListField[] fields, final int limit) throws IMAPException {
		try {
			imapFolder = setAndOpenFolder(imapFolder, prepareMailFolderParam(folder), Folder.READ_ONLY);
			try {
				if (!holdsMessages()) {
					throw new OXMailException(MailCode.FOLDER_DOES_NOT_HOLD_MESSAGES, imapFolder.getFullName());
				} else if (IMAPProperties.isSupportsACLs()
						&& !session.getCachedRights(imapFolder, true).contains(Rights.Right.READ)) {
					throw new OXMailException(MailCode.NO_READ_ACCESS, imapFolder.getFullName());
				}
			} catch (final MessagingException e) {
				throw new OXMailException(MailCode.NO_ACCESS, imapFolder.getFullName());
			}
			/*
			 * Get ( & fetch) new messages
			 */
			final long start = System.currentTimeMillis();
			final Message[] msgs = IMAPCommandsCollection.getNewMessages(imapFolder, fields, sortField, order, session
					.getLocale());
			MailInterfaceImpl.mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
			if (msgs == null) {
				return EMPTY_RETVAL;
			} else if (limit > 0) {
				final int newLength = Math.min(limit, msgs.length);
				final Message[] retval = new Message[newLength];
				System.arraycopy(msgs, 0, retval, 0, newLength);
				return IMAPMessageConverter.convertIMAPMessages(retval, fields);
			}
			return IMAPMessageConverter.convertIMAPMessages(msgs, fields);
		} catch (final OXMailException e) {
			throw new IMAPException(e);
		} catch (final IMAPPropertyException e) {
			throw new IMAPException(e);
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapMailConnection);
		}
	}

	public MailMessage[] searchMessages(final String folder, final MailListField[] searchCols,
			final String[] searchPatterns, final boolean linkWithOR, final MailListField[] fields) throws IMAPException {
		try {
			imapFolder = setAndOpenFolder(imapFolder, prepareMailFolderParam(folder), Folder.READ_ONLY);
			try {
				if (!holdsMessages()) {
					throw new IMAPException(IMAPException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, imapFolder.getFullName());
				} else if (IMAPProperties.isSupportsACLs()
						&& !session.getCachedRights(imapFolder, true).contains(Rights.Right.READ)) {
					throw new IMAPException(IMAPException.Code.NO_READ_ACCESS, imapFolder.getFullName());
				}
			} catch (final MessagingException e) {
				throw new IMAPException(IMAPException.Code.NO_ACCESS, imapFolder.getFullName());
			}
			final Message[] retval = IMAPSearch.searchMessages(imapFolder, searchCols, searchPatterns, linkWithOR,
					fields, MailListField.RECEIVED_DATE);
			if (retval == null || retval.length == 0) {
				return EMPTY_RETVAL;
			}
			return IMAPMessageConverter.convertIMAPMessages(retval, fields);
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapMailConnection);
		} catch (final IMAPPropertyException e) {
			throw new IMAPException(e);
		}
	}

	public boolean deleteMessages(final String folder, final long[] msgUIDs, final boolean hardDelete)
			throws IMAPException {
		try {
			imapFolder = setAndOpenFolder(imapFolder, prepareMailFolderParam(folder), Folder.READ_WRITE);
			try {
				if (!holdsMessages()) {
					throw new OXMailException(MailCode.FOLDER_DOES_NOT_HOLD_MESSAGES, imapFolder.getFullName());
				} else if (IMAPProperties.isSupportsACLs()
						&& !session.getCachedRights(imapFolder, true).contains(Rights.Right.DELETE)) {
					throw new IMAPException(IMAPException.Code.NO_DELETE_ACCESS, imapFolder.getFullName());
				}
			} catch (final MessagingException e) {
				throw new IMAPException(IMAPException.Code.NO_ACCESS, imapFolder.getFullName());
			}
			final String trashFullname = prepareMailFolderParam(imapMailConnection.getFolderStorage().getTrashFolder());
			/*
			 * TODO: This should be done in mail interface
			 */
			/*
			 * Perform "soft delete", means to copy message to default trash
			 * folder
			 */
			if (!usm.isHardDeleteMsgs() && !hardDelete && !(folder.equals(trashFullname))) {
				if (null == trashFullname) {
					// TODO: Bug#8992 -> What to do if trash folder is null
					if (LOG.isErrorEnabled()) {
						LOG.error("\n\tDefault trash folder is not set: aborting delete operation");
					}
					throw new IMAPException(IMAPException.Code.MISSING_DEFAULT_FOLDER_NAME, "trash");
				}
				/*
				 * Copy messages to folder "TRASH"
				 */
				try {
					final long start = System.currentTimeMillis();
					new CopyIMAPCommand(imapFolder, msgUIDs, trashFullname, false, true).doCommand();
					if (LOG.isInfoEnabled()) {
						LOG.info(new StringBuilder(100).append("\"Soft Delete\": ").append(msgUIDs.length).append(
								" messages copied to default trash folder \"").append(trashFullname).append("\" in ")
								.append((System.currentTimeMillis() - start)).append("msec").toString());
					}
				} catch (final MessagingException e) {
					if (e.getNextException() instanceof CommandFailedException) {
						final CommandFailedException exc = (CommandFailedException) e.getNextException();
						if (exc.getMessage().indexOf("Over quota") > -1) {
							/*
							 * We face an Over-Quota-Exception
							 */
							throw new IMAPException(IMAPException.Code.DELETE_FAILED_OVER_QUOTA);
						}
					}
					throw new IMAPException(IMAPException.Code.MOVE_ON_DELETE_FAILED);
				}

			}
			/*
			 * Mark messages as \DELETED
			 */
			long start = System.currentTimeMillis();
			new FlagsIMAPCommand(imapFolder, msgUIDs, FLAGS_DELETED, true, false).doCommand();
			if (LOG.isInfoEnabled()) {
				LOG.info(new StringBuilder(100).append(msgUIDs.length).append(
						" messages marked as deleted (through system flag \\DELETED) in ").append(
						(System.currentTimeMillis() - start)).append("msec").toString());
			}
			/*
			 * ... and perform EXPUNGE
			 */
			try {
				start = System.currentTimeMillis();
				IMAPCommandsCollection.fastExpunge(imapFolder);
				MailInterfaceImpl.mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
				if (LOG.isInfoEnabled()) {
					LOG.info(new StringBuilder(100).append("Folder ").append(imapFolder.getFullName()).append(
							" cleared in ").append((System.currentTimeMillis() - start)).append("msec").toString());
				}
			} catch (final ProtocolException pex) {
				throw new MessagingException(pex.getMessage(), pex);
			}
			/*
			 * Close folder to force JavaMail-internal message cache update
			 */
			imapFolder.close(false);
			resetIMAPFolder();
			return true;
		} catch (final OXMailException e) {
			throw new IMAPException(e);
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapMailConnection);
		} catch (final IMAPPropertyException e) {
			throw new IMAPException(e);
		}
	}

	private static final int SPAM_HAM = -1;

	private static final int SPAM_NOOP = 0;

	private static final int SPAM_SPAM = 1;

	public long[] copyMessages(final String sourceFolderArg, final String destFolderArg, final long[] msgUIDs,
			final boolean move) throws IMAPException {
		try {
			if (sourceFolderArg == null || sourceFolderArg.length() == 0) {
				throw new IMAPException(IMAPException.Code.MISSING_SOURCE_TARGET_FOLDER_ON_MOVE, "source");
			} else if (destFolderArg == null || destFolderArg.length() == 0) {
				throw new IMAPException(IMAPException.Code.MISSING_SOURCE_TARGET_FOLDER_ON_MOVE, "target");
			} else if (sourceFolderArg.equals(destFolderArg) && move) {
				throw new IMAPException(IMAPException.Code.NO_EQUAL_MOVE, prepareMailFolderParam(sourceFolderArg));
			}
			final String sourceFolder = prepareMailFolderParam(sourceFolderArg);
			final String destFolder = prepareMailFolderParam(destFolderArg);
			/*
			 * Open and check user rights on source folder
			 */
			imapFolder = setAndOpenFolder(imapFolder, sourceFolder, Folder.READ_WRITE);
			try {
				if (!holdsMessages()) {
					throw new IMAPException(IMAPException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, imapFolder.getFullName());
				} else if (move && IMAPProperties.isSupportsACLs()
						&& !session.getCachedRights(imapFolder, true).contains(Rights.Right.DELETE)) {
					throw new IMAPException(IMAPException.Code.NO_DELETE_ACCESS, imapFolder.getFullName());
				}
			} catch (final MessagingException e) {
				throw new IMAPException(IMAPException.Code.NO_ACCESS, imapFolder.getFullName());
			}
			/*
			 * Open and check user rights on destination folder
			 */
			final IMAPFolder tmpFolder = setAndOpenFolder(destFolder, Folder.READ_ONLY);
			try {
				if ((tmpFolder.getType() & Folder.HOLDS_MESSAGES) == 0) {
					throw new IMAPException(IMAPException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, tmpFolder.getFullName());
				} else if (IMAPProperties.isSupportsACLs()
						&& !session.getCachedRights(tmpFolder, true).contains(Rights.Right.INSERT)) {
					throw new IMAPException(IMAPException.Code.NO_INSERT_ACCESS, tmpFolder.getFullName());
				}
			} catch (final MessagingException e) {
				throw new IMAPException(IMAPException.Code.NO_ACCESS, tmpFolder.getFullName());
			}
			/*
			 * Copy operation needs UIDPLUS capability
			 */
			long start = System.currentTimeMillis();
			final long[] res = new CopyIMAPCommand(imapFolder, msgUIDs, destFolder, false, false).doCommand();
			if (LOG.isInfoEnabled()) {
				LOG.info(new StringBuilder(100).append(msgUIDs.length).append(" messages copied in ").append(
						(System.currentTimeMillis() - start)).append("msec").toString());
			}
			/*
			 * TODO: Should be moved to mail interface impl
			 */
			if (usm.isSpamEnabled()) {
				/*
				 * Spam related action
				 */
				final String spamFullName = prepareMailFolderParam(imapMailConnection.getFolderStorage()
						.getSpamFolder());
				final int spamAction = spamFullName.equals(imapFolder.getFullName()) ? SPAM_HAM : (spamFullName
						.equals(tmpFolder.getFullName()) ? SPAM_SPAM : SPAM_NOOP);
				if (spamAction != SPAM_NOOP) {
					try {
						handleSpamByUID(msgUIDs, spamAction == SPAM_SPAM, false);
					} catch (final MessagingException e) {
						if (LOG.isWarnEnabled()) {
							LOG.warn(e.getMessage(), e);
						}
					}
				}
			}
			if (move) {
				start = System.currentTimeMillis();
				new FlagsIMAPCommand(imapFolder, msgUIDs, FLAGS_DELETED, true, false).doCommand();
				if (LOG.isInfoEnabled()) {
					LOG.info(new StringBuilder(100).append(msgUIDs.length).append(
							" messages marked as expunged (through system flag \\DELETED) in ").append(
							(System.currentTimeMillis() - start)).append("msec").toString());
				}
				/*
				 * Expunge "moved" messages immediately
				 */
				try {
					start = System.currentTimeMillis();
					IMAPCommandsCollection.uidExpunge(imapFolder, msgUIDs);
					MailInterfaceImpl.mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
					if (LOG.isInfoEnabled()) {
						LOG.info(new StringBuilder(100).append(msgUIDs.length).append(" messages expunged in ").append(
								(System.currentTimeMillis() - start)).append("msec").toString());
					}
					/*
					 * Force folder cache update through a close
					 */
					imapFolder.close(false);
					resetIMAPFolder();
				} catch (final ProtocolException e) {
					throw new IMAPException(IMAPException.Code.MOVE_PARTIALLY_COMPLETED, e,
							com.openexchange.tools.oxfolder.OXFolderManagerImpl.getUserName(session), Arrays
									.toString(msgUIDs), imapFolder.getFullName(), e.getMessage());
				}
			}
			return res;
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapMailConnection);
		} catch (final IMAPPropertyException e) {
			throw new IMAPException(e);
		}
	}

	public MailContent getAttachment(final String fullname, final long msgUID, final String sequenceId, final boolean displayVersion)
			throws MailException {
		try {
			imapFolder = setAndOpenFolder(imapFolder, prepareMailFolderParam(fullname), Folder.READ_ONLY);
			final IMAPMessage msg = (IMAPMessage) imapFolder.getMessageByUID(msgUID);
			final MailMessage mailMessage = IMAPMessageConverter.convertIMAPMessage(msg);
			final AttachmentMessageHandler msgHandler = new AttachmentMessageHandler(sequenceId);
			new MailMessageParser().parseMailMessage(mailMessage, msgHandler);
			if (msgHandler.getAttachment() == null) {
				// TODO: throw an exception
			}
			return msgHandler.getAttachment();
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapMailConnection);
		} catch (IMAPPropertyException e) {
			throw new IMAPException(e);
		}
	}

	/*
	 * +++++++++++++++++ Helper methods +++++++++++++++++++
	 */

	private boolean handleSpamByUID(final long[] msgUIDs, final boolean isSpam, final boolean move)
			throws MessagingException, IMAPException, IMAPPropertyException {
		/*
		 * Check for spam handling
		 */
		if (usm.isSpamEnabled()) {
			final boolean locatedInSpamFolder = prepareMailFolderParam(
					imapMailConnection.getFolderStorage().getSpamFolder()).equals(imapFolder.getFullName());
			if (isSpam) {
				if (locatedInSpamFolder) {
					/*
					 * A message that already has been detected as spam should
					 * again be learned as spam: Abort.
					 */
					return true;
				}
				/*
				 * Copy to confirmed spam
				 */
				new CopyIMAPCommand(imapFolder, msgUIDs, prepareMailFolderParam(imapMailConnection.getFolderStorage()
						.getConfirmedSpamFolder()), false, true).doCommand();
				if (move) {
					/*
					 * Copy messages to spam folder
					 */
					new CopyIMAPCommand(imapFolder, msgUIDs, prepareMailFolderParam(imapMailConnection
							.getFolderStorage().getSpamFolder()), false, true).doCommand();
					/*
					 * Delete messages
					 */
					new FlagsIMAPCommand(imapFolder, msgUIDs, FLAGS_DELETED, true, false).doCommand();
					/*
					 * Expunge messages immediately
					 */
					try {
						imapFolder.getProtocol().uidexpunge(IMAPStorageUtils.toUIDSet(msgUIDs));
						/*
						 * Force folder cache update through a close
						 */
						imapFolder.close(false);
						resetIMAPFolder();
					} catch (final ProtocolException e) {
						throw new IMAPException(IMAPException.Code.MOVE_PARTIALLY_COMPLETED, e,
								com.openexchange.tools.oxfolder.OXFolderManagerImpl.getUserName(session), Arrays
										.toString(msgUIDs), imapFolder.getFullName(), e.getMessage());
					}
				}
				return true;
			}
			if (!locatedInSpamFolder) {
				/*
				 * A message that already has been detected as ham should again
				 * be learned as ham: Abort.
				 */
				return true;
			}
			/*
			 * Mark as ham. In contrast to mark as spam this is a very time
			 * sucking operation. In order to deal with the original messages
			 * that are wrapped inside a SpamAssassin-created message it must be
			 * extracted. Therefore we need to access message's content and
			 * cannot deal only with UIDs
			 */
			final FetchProfile fp = new FetchProfile();
			fp.add(MessageHeaders.HDR_X_SPAM_FLAG);
			fp.add(FetchProfile.Item.CONTENT_INFO);
			final MessageCacheObject[] msgs = (MessageCacheObject[]) new FetchIMAPCommand(imapFolder, msgUIDs, fp,
					false, false).doCommand();
			/*
			 * Seperate the plain from the nested messages inside spam folder
			 */
			SmartLongArray plainUIDs = new SmartLongArray(msgUIDs.length);
			SmartLongArray extractUIDs = new SmartLongArray(msgUIDs.length);
			for (int i = 0; i < msgs.length; i++) {
				final String[] spamHdr = msgs[i].getHeader(MessageHeaders.HDR_X_SPAM_FLAG);
				final BODYSTRUCTURE bodystructure = msgs[i].getBodystructure();
				if (spamHdr != null && "yes".regionMatches(true, 0, spamHdr[0], 0, 3) && bodystructure.isMulti()
						&& bodystructure.bodies[1].isNested()) {
					extractUIDs.append(msgUIDs[i]);
				} else {
					plainUIDs.append(msgUIDs[i]);
				}
			}
			final String confirmedHamFullname = prepareMailFolderParam(imapMailConnection.getFolderStorage()
					.getConfirmedHamFolder());
			/*
			 * Copy plain messages to confirmed ham and INBOX
			 */
			long[] plainUIDsArr = plainUIDs.toArray();
			plainUIDs = null;
			new CopyIMAPCommand(imapFolder, plainUIDsArr, confirmedHamFullname, false, true).doCommand();
			if (move) {
				new CopyIMAPCommand(imapFolder, plainUIDsArr, STR_INBOX, false, true).doCommand();
			}
			plainUIDsArr = null;
			/*
			 * Handle spam messages
			 */
			long[] spamArr = extractUIDs.toArray();
			extractUIDs = null;
			IMAPFolder confirmedHamFld = null;
			try {
				confirmedHamFld = (IMAPFolder) imapStore.getFolder(confirmedHamFullname);
				confirmedHamFld.open(Folder.READ_WRITE);
				/*
				 * Get nested spam messages
				 */
				long start = System.currentTimeMillis();
				Message[] nestedMsgs = new ExtractSpamMsgIMAPCommand(imapFolder, spamArr).doCommand();
				if (LOG.isInfoEnabled()) {
					LOG.info(new StringBuilder(100).append("Nested SPAM messages fetched in ").append(
							(System.currentTimeMillis() - start)).append("msec").toString());
				}
				spamArr = null;
				/*
				 * ... and append them to confirmed ham folder and - if move
				 * enabled - copy them to INBOX.
				 */
				start = System.currentTimeMillis();
				AppendUID[] appendUIDs = confirmedHamFld.appendUIDMessages(nestedMsgs);
				MailInterfaceImpl.mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
				if (LOG.isInfoEnabled()) {
					LOG.info(new StringBuilder(100).append("Nested SPAM messages appended to ").append(
							confirmedHamFullname).append(" in ").append((System.currentTimeMillis() - start)).append(
							"msec").toString());
				}
				nestedMsgs = null;
				if (move) { // Cannot be null
					start = System.currentTimeMillis();
					new CopyIMAPCommand(confirmedHamFld, appendUID2Long(appendUIDs), STR_INBOX, false, true)
							.doCommand();
					if (LOG.isInfoEnabled()) {
						LOG.info(new StringBuilder(100).append("Nested SPAM messages copied to ").append(STR_INBOX)
								.append(" in ").append((System.currentTimeMillis() - start)).append("msec").toString());
					}
				}
				appendUIDs = null;
				if (move) {
					/*
					 * Expunge messages
					 */
					new FlagsIMAPCommand(imapFolder, msgUIDs, FLAGS_DELETED, true, false).doCommand();
					start = System.currentTimeMillis();
					imapFolder.getProtocol().uidexpunge(IMAPStorageUtils.toUIDSet(msgUIDs));
					MailInterfaceImpl.mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
					if (LOG.isInfoEnabled()) {
						LOG.info(new StringBuilder(100).append("Original spam messages expunged in ").append(
								(System.currentTimeMillis() - start)).append("msec").toString());
					}
					/*
					 * Close folder to force JavaMail-internal message cache
					 * update
					 */
					imapFolder.close(false);
					resetIMAPFolder();
				}
			} catch (final ProtocolException e1) {
				throw new MessagingException(e1.getMessage(), e1);
			} finally {
				if (confirmedHamFld != null) {
					try {
						confirmedHamFld.close(false);
					} catch (final IllegalStateException e) {
						LOG.warn(WARN_FLD_ALREADY_CLOSED, e);
					}
				}
			}

		}
		return true;
	}

	private static long[] appendUID2Long(final AppendUID[] appendUIDs) {
		final long[] retval = new long[appendUIDs.length];
		for (int i = 0; i < retval.length; i++) {
			retval[i] = appendUIDs[i].uid;
		}
		return retval;
	}

}

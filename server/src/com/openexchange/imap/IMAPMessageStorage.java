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

import static com.openexchange.imap.utils.IMAPStorageUtility.getCacheFetchProfile;
import static com.openexchange.imap.utils.IMAPStorageUtility.getCacheFields;
import static com.openexchange.imap.utils.IMAPStorageUtility.getFetchProfile;
import static com.openexchange.mail.MailInterfaceImpl.mailInterfaceMonitor;
import static com.openexchange.mail.dataobjects.MailFolder.DEFAULT_FOLDER_ID;
import static com.openexchange.mail.utils.StorageUtility.prepareMailFolderParam;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;

import com.openexchange.cache.OXCachingException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.imap.cache.RightsCache;
import com.openexchange.imap.cache.UserFlagsCache;
import com.openexchange.imap.command.CopyIMAPCommand;
import com.openexchange.imap.command.FetchIMAPCommand;
import com.openexchange.imap.command.FlagsIMAPCommand;
import com.openexchange.imap.config.IMAPConfig;
import com.openexchange.imap.search.IMAPSearch;
import com.openexchange.imap.sort.IMAPSort;
import com.openexchange.imap.spam.SpamHandler;
import com.openexchange.imap.threadsort.ThreadSortUtil;
import com.openexchange.imap.threadsort.TreeNode;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailInterfaceImpl;
import com.openexchange.mail.MailListField;
import com.openexchange.mail.MailMessageStorage;
import com.openexchange.mail.MailStorageUtils.OrderDirection;
import com.openexchange.mail.cache.MailMessageCache;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.ContainerMessage;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.converters.MIMEMessageConverter;
import com.openexchange.mail.parser.MailMessageParser;
import com.openexchange.mail.parser.handlers.ImageMessageHandler;
import com.openexchange.mail.parser.handlers.MailPartHandler;
import com.openexchange.sessiond.impl.SessionObject;
import com.sun.mail.iap.CommandFailedException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.AppendUID;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.Rights;

/**
 * {@link IMAPMessageStorage} - The IMAP specific implementation of
 * {@link MailMessageStorage}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IMAPMessageStorage extends IMAPFolderWorker implements MailMessageStorage, Serializable {

	private static final transient org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(IMAPMessageStorage.class);

	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = 1467121647337217270L;

	/*
	 * Flag constants
	 */
	private static final Flags FLAGS_DRAFT = new Flags(Flags.Flag.DRAFT);

	private static final Flags FLAGS_DELETED = new Flags(Flags.Flag.DELETED);

	/*
	 * Fields
	 */
	private final transient int userId;

	private final transient Context ctx;

	/**
	 * Constructor
	 * 
	 * @param imapStore
	 *            The <b>connected</b> IMAP store that provides access to IMAP
	 *            server
	 * @throws MailException
	 */
	public IMAPMessageStorage(final IMAPStore imapStore, final IMAPConnection imapMailConnection,
			final SessionObject session) throws MailException {
		super(imapStore, imapMailConnection, session);
		userId = session.getUserId();
		ctx = session.getContext();
	}

	public MailMessage getMessage(final String folderArg, final long msgUID) throws MailException {
		try {
			final String folder = prepareMailFolderParam(folderArg);
			if (DEFAULT_FOLDER_ID.equals(folder)) {
				throw new IMAPException(IMAPException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, DEFAULT_FOLDER_ID);
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
				throw new MailException(MailException.Code.MAIL_NOT_FOUND, String.valueOf(msgUID), imapFolder
						.toString());
			}
			if (imapFolder.getMode() == Folder.READ_WRITE && !msg.isExpunged()) {
				/*
				 * Check for drafts folder. This is done here, cause sometimes
				 * copy operation does not properly add \Draft flag.
				 */
				final boolean isDraftFld = imapFolder.getFullName().equals(
						prepareMailFolderParam(imapConnection.getFolderStorage().getDraftsFolder()));
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
			return MIMEMessageConverter.convertMessage(msg);
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapConnection);
		}
	}

	public MailMessage[] getAllMessages(final String folder, final int[] fromToIndices, final MailListField sortField,
			final OrderDirection order, final MailListField[] fields) throws MailException {
		return getMessages(folder, fromToIndices, sortField, order, null, null, false, fields);
	}

	private static final transient MailMessage[] EMPTY_RETVAL = new MailMessage[0];

	public MailMessage[] getMessages(final String folder, final int[] fromToIndices, final MailListField sortField,
			final OrderDirection order, final MailListField[] searchFields, final String[] searchPatterns,
			final boolean linkSearchTermsWithOR, final MailListField[] fields) throws MailException {
		try {
			final String fullname = prepareMailFolderParam(folder);
			imapFolder = setAndOpenFolder(imapFolder, fullname, Folder.READ_ONLY);
			Message[] msgs = null;
			final Set<MailListField> usedFields = new HashSet<MailListField>();
			/*
			 * Shall a search be performed?
			 */
			final boolean search = (searchFields != null && searchFields.length > 0 && searchPatterns != null && searchPatterns.length > 0);
			if (search) {
				/*
				 * Preselect message list according to given search pattern
				 */
				msgs = IMAPSearch.searchMessages(imapFolder, searchFields, searchPatterns, linkSearchTermsWithOR,
						fields, sortField, usedFields, imapConfig);
				if (msgs == null || msgs.length == 0) {
					return EMPTY_RETVAL;
				}
			}
			msgs = IMAPSort.sortMessages(imapFolder, msgs, fields, sortField, order, UserStorage.getUser(
					session.getUserId(), session.getContext()).getLocale(), usedFields, imapConfig);
			if (fromToIndices != null && fromToIndices.length == 2) {
				final int fromIndex = fromToIndices[0];
				int toIndex = fromToIndices[1];
				if (msgs == null || msgs.length == 0) {
					return EMPTY_RETVAL;
				}
				if ((fromIndex) > msgs.length) {
					/*
					 * Return empty iterator if start is out of range
					 */
					return EMPTY_RETVAL;
				}
				/*
				 * Reset end index if out of range
				 */
				if (toIndex > msgs.length) {
					toIndex = msgs.length;
				}
				final Message[] tmp = msgs;
				final int retvalLength = toIndex - fromIndex + 1;
				msgs = new Message[retvalLength];
				System.arraycopy(tmp, fromIndex, msgs, 0, retvalLength);
			}
			final MailMessage[] mails = MIMEMessageConverter.convertMessages(msgs, usedFields
					.toArray(new MailListField[usedFields.size()]));
			try {
				/*
				 * Remove old user cache entries
				 */
				MailMessageCache.getInstance().removeFolderMessages(fullname, userId, ctx);
				if (mails != null && mails.length > 0 && mails.length < IMAPConfig.getMailFetchLimit()) {
					/*
					 * ... and put new ones
					 */
					MailMessageCache.getInstance().putMessages(mails, userId, ctx);
				}
			} catch (final OXCachingException e) {
				LOG.error(e.getLocalizedMessage(), e);
			}
			return mails;
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapConnection);
		}
	}

	@SuppressWarnings("null")
	public MailMessage[] getThreadSortedMessages(final String folder, final int[] fromToIndices,
			final MailListField[] searchFields, final String[] searchPatterns, final boolean linkSearchTermsWithOR,
			final MailListField[] fields) throws MailException {
		try {
			if (!imapConfig.getImapCapabilities().hasThreadReferences()) {
				throw new IMAPException(IMAPException.Code.THREAD_SORT_NOT_SUPPORTED);
			}
			final String fullname = prepareMailFolderParam(folder);
			imapFolder = setAndOpenFolder(imapFolder, fullname, Folder.READ_ONLY);
			Message[] msgs = null;
			final Set<MailListField> usedFields = new HashSet<MailListField>();
			/*
			 * Shall a search be performed?
			 */
			final boolean search = (searchFields != null && searchFields.length > 0 && searchPatterns != null && searchPatterns.length > 0);
			if (search) {
				/*
				 * Preselect message list according to given search pattern
				 */
				msgs = IMAPSearch.searchMessages(imapFolder, searchFields, searchPatterns, linkSearchTermsWithOR,
						fields, null, usedFields, imapConfig);
				if (msgs == null || msgs.length == 0) {
					return EMPTY_RETVAL;
				}
			}
			final List<TreeNode> threadList;
			{
				/*
				 * Sort messages by thread reference
				 */
				final StringBuilder sortRange;
				if (search) {
					/*
					 * Define sequence of valid message numbers: e.g.:
					 * 2,34,35,43,51
					 */
					sortRange = new StringBuilder(msgs.length * 2);
					sortRange.append(msgs[0].getMessageNumber());
					for (int i = 1; i < msgs.length; i++) {
						sortRange.append(msgs[i].getMessageNumber()).append(',');
					}
				} else {
					/*
					 * Select all messages
					 */
					sortRange = new StringBuilder(3).append("ALL");
				}
				final long start = System.currentTimeMillis();
				final String threadResp = ThreadSortUtil.getThreadResponse(imapFolder, sortRange);
				mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
				/*
				 * Parse THREAD response
				 */
				threadList = ThreadSortUtil.parseThreadResponse(threadResp);
				msgs = ThreadSortUtil.getMessagesFromThreadResponse(imapFolder.getFullName(),
						imapFolder.getSeparator(), threadResp);
			}
			/*
			 * Fetch messages
			 */
			final FetchProfile fetchProfile;
			if (msgs.length < IMAPConfig.getMailFetchLimit()) {
				fetchProfile = getCacheFetchProfile();
				usedFields.addAll(getCacheFields());
			} else {
				fetchProfile = getFetchProfile(fields, null);
				usedFields.addAll(Arrays.asList(fields));
			}
			msgs = new FetchIMAPCommand(imapFolder, msgs, fetchProfile, false, true).doCommand();
			/*
			 * Apply thread level
			 */
			createThreadSortMessages(threadList, 0, msgs, 0);
			/*
			 * ... and return
			 */
			if (fromToIndices != null && fromToIndices.length == 2) {
				final int fromIndex = fromToIndices[0];
				int toIndex = fromToIndices[1];
				if (msgs == null || msgs.length == 0) {
					return EMPTY_RETVAL;
				}
				if ((fromIndex) > msgs.length) {
					/*
					 * Return empty iterator if start is out of range
					 */
					return EMPTY_RETVAL;
				}
				/*
				 * Reset end index if out of range
				 */
				if (toIndex > msgs.length) {
					toIndex = msgs.length;
				}
				final Message[] tmp = msgs;
				final int retvalLength = toIndex - fromIndex + 1;
				msgs = new ContainerMessage[retvalLength];
				System.arraycopy(tmp, fromIndex, msgs, 0, retvalLength);
			}
			final MailMessage[] mails = MIMEMessageConverter.convertMessages(msgs, usedFields
					.toArray(new MailListField[usedFields.size()]));
			try {
				/*
				 * Remove old user cache entries
				 */
				MailMessageCache.getInstance().removeFolderMessages(fullname, userId, ctx);
				if (mails != null && mails.length > 0 && mails.length < IMAPConfig.getMailFetchLimit()) {
					/*
					 * ... and put new ones
					 */
					MailMessageCache.getInstance().putMessages(mails, userId, ctx);
				}
			} catch (final OXCachingException e) {
				LOG.error(e.getLocalizedMessage(), e);
			}
			return mails;
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapConnection);
		}
	}

	public MailMessage[] getMessagesByUID(final String folderArg, final long[] msgUIDs, final MailListField[] fields,
			final boolean tryFromCache) throws MailException {
		try {
			final String fullname = prepareMailFolderParam(folderArg);
			if (tryFromCache) {
				try {
					if (MailMessageCache.getInstance().containsFolderMessages(fullname, userId, ctx)) {
						return MailMessageCache.getInstance().getMessages(msgUIDs, fullname, userId, ctx);
					}
				} catch (final OXCachingException e) {
					LOG.error(e.getLocalizedMessage(), e);
				}
			}
			imapFolder = setAndOpenFolder(imapFolder, fullname, Folder.READ_ONLY);
			final Message[] msgs = new FetchIMAPCommand(imapFolder, msgUIDs, getFetchProfile(fields, null), false, true)
					.doCommand();
			return MIMEMessageConverter.convertMessages(msgs, fields);
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapConnection);
		}
	}

	public MailMessage[] getUnreadMessages(final String folder, final MailListField sortField,
			final OrderDirection order, final MailListField[] fields, final int limit) throws MailException {
		try {
			imapFolder = setAndOpenFolder(imapFolder, prepareMailFolderParam(folder), Folder.READ_ONLY);
			/*
			 * Get ( & fetch) new messages
			 */
			final long start = System.currentTimeMillis();
			final Message[] msgs = IMAPCommandsCollection.getUnreadMessages(imapFolder, fields, sortField, order,
					UserStorage.getUser(session.getUserId(), session.getContext()).getLocale());
			MailInterfaceImpl.mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
			if (msgs == null) {
				return EMPTY_RETVAL;
			} else if (limit > 0) {
				final int newLength = Math.min(limit, msgs.length);
				final Message[] retval = new Message[newLength];
				System.arraycopy(msgs, 0, retval, 0, newLength);
				return MIMEMessageConverter.convertMessages(retval, fields);
			}
			return MIMEMessageConverter.convertMessages(msgs, fields);
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapConnection);
		}
	}

	public boolean deleteMessages(final String folder, final long[] msgUIDs, final boolean hardDelete)
			throws MailException {
		try {
			final String fullname = prepareMailFolderParam(folder);
			imapFolder = setAndOpenFolder(imapFolder, fullname, Folder.READ_WRITE);
			try {
				if (!holdsMessages()) {
					throw new IMAPException(IMAPException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, imapFolder.getFullName());
				} else if (imapConfig.isSupportsACLs()
						&& !RightsCache.getCachedRights(imapFolder, true, session).contains(Rights.Right.DELETE)) {
					throw new IMAPException(IMAPException.Code.NO_DELETE_ACCESS, imapFolder.getFullName());
				}
			} catch (final MessagingException e) {
				throw new IMAPException(IMAPException.Code.NO_ACCESS, imapFolder.getFullName());
			}
			final String trashFullname = prepareMailFolderParam(imapConnection.getFolderStorage().getTrashFolder());
			/*
			 * Perform "soft delete", means to move message to default trash
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
							throw new MailException(MailException.Code.DELETE_FAILED_OVER_QUOTA);
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
							" expunged in ").append((System.currentTimeMillis() - start)).append("msec").toString());
				}
			} catch (final ProtocolException pex) {
				throw new MessagingException(pex.getMessage(), pex);
			}
			try {
				/*
				 * Update message cache
				 */
				MailMessageCache.getInstance().removeMessages(msgUIDs, fullname, userId, ctx);
			} catch (final OXCachingException e) {
				LOG.error(e.getLocalizedMessage(), e);
			}
			/*
			 * Close folder to force JavaMail-internal message cache update
			 */
			imapFolder.close(false);
			resetIMAPFolder();
			return true;
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapConnection);
		}
	}

	private static final int SPAM_HAM = -1;

	private static final int SPAM_NOOP = 0;

	private static final int SPAM_SPAM = 1;

	public long[] copyMessages(final String sourceFolderArg, final String destFolderArg, final long[] msgUIDs,
			final boolean move, final boolean fast) throws MailException {
		try {
			if (sourceFolderArg == null || sourceFolderArg.length() == 0) {
				throw new IMAPException(IMAPException.Code.MISSING_SOURCE_TARGET_FOLDER_ON_MOVE, "source");
			} else if (destFolderArg == null || destFolderArg.length() == 0) {
				throw new IMAPException(IMAPException.Code.MISSING_SOURCE_TARGET_FOLDER_ON_MOVE, "target");
			} else if (sourceFolderArg.equals(destFolderArg) && move) {
				throw new IMAPException(IMAPException.Code.NO_EQUAL_MOVE, prepareMailFolderParam(sourceFolderArg));
			}
			final String sourceFolder = prepareMailFolderParam(sourceFolderArg);
			final String destFullname = prepareMailFolderParam(destFolderArg);
			/*
			 * Open and check user rights on source folder
			 */
			imapFolder = setAndOpenFolder(imapFolder, sourceFolder, Folder.READ_WRITE);
			try {
				if (!holdsMessages()) {
					throw new IMAPException(IMAPException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, imapFolder.getFullName());
				} else if (move && imapConfig.isSupportsACLs()
						&& !RightsCache.getCachedRights(imapFolder, true, session).contains(Rights.Right.DELETE)) {
					throw new IMAPException(IMAPException.Code.NO_DELETE_ACCESS, imapFolder.getFullName());
				}
			} catch (final MessagingException e) {
				throw new IMAPException(IMAPException.Code.NO_ACCESS, imapFolder.getFullName());
			}
			{
				/*
				 * Open and check user rights on destination folder
				 */
				final IMAPFolder destFolder = setAndOpenFolder(destFullname, Folder.READ_ONLY);
				try {
					if ((destFolder.getType() & Folder.HOLDS_MESSAGES) == 0) {
						throw new IMAPException(IMAPException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, destFolder
								.getFullName());
					} else if (imapConfig.isSupportsACLs()
							&& !RightsCache.getCachedRights(destFolder, true, session).contains(Rights.Right.INSERT)) {
						throw new IMAPException(IMAPException.Code.NO_INSERT_ACCESS, destFolder.getFullName());
					}
				} catch (final MessagingException e) {
					throw new IMAPException(IMAPException.Code.NO_ACCESS, destFolder.getFullName());
				} finally {
					destFolder.close(false);
				}
			}
			/*
			 * Copy operation
			 */
			long start = System.currentTimeMillis();
			long[] res = new CopyIMAPCommand(imapFolder, msgUIDs, destFullname, false, fast).doCommand();
			if (LOG.isInfoEnabled()) {
				LOG.info(new StringBuilder(100).append(msgUIDs.length).append(" messages copied in ").append(
						(System.currentTimeMillis() - start)).append("msec").toString());
			}
			if (!fast && (res == null || noUIDsAssigned(res, msgUIDs.length))) {
				/*
				 * Invalid UIDs
				 */
				res = getDestinationUIDs(msgUIDs, destFullname);
			}
			if (usm.isSpamEnabled()) {
				/*
				 * Spam related action
				 */
				final String spamFullName = prepareMailFolderParam(imapConnection.getFolderStorage().getSpamFolder());
				final int spamAction = spamFullName.equals(imapFolder.getFullName()) ? SPAM_HAM : (spamFullName
						.equals(destFullname) ? SPAM_SPAM : SPAM_NOOP);
				if (spamAction != SPAM_NOOP) {
					try {
						handleSpamByUID(msgUIDs, spamAction == SPAM_SPAM, false, sourceFolder, Folder.READ_WRITE);
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
					if (LOG.isWarnEnabled()) {
						LOG.warn(new StringBuilder("UID EXPUNGE failed: ").append(e.getLocalizedMessage()).toString(),
								e);
					}
					/*
					 * UID EXPUNGE did not work; perform fallback actions
					 */
					try {
						final long[] excUIDs = IMAPCommandsCollection.getDeletedMessages(imapFolder, msgUIDs);
						if (excUIDs.length > 0) {
							/*
							 * Temporary remove flag \Deleted, perform expunge &
							 * restore flag \Deleted
							 */
							new FlagsIMAPCommand(imapFolder, excUIDs, FLAGS_DELETED, false, false).doCommand();
							IMAPCommandsCollection.fastExpunge(imapFolder);
							new FlagsIMAPCommand(imapFolder, excUIDs, FLAGS_DELETED, true, false).doCommand();
						} else {
							IMAPCommandsCollection.fastExpunge(imapFolder);
						}
						/*
						 * Force folder cache update through a close
						 */
						imapFolder.close(false);
						resetIMAPFolder();
					} catch (final ProtocolException e1) {
						throw new IMAPException(IMAPException.Code.MOVE_PARTIALLY_COMPLETED, e1,
								com.openexchange.tools.oxfolder.OXFolderManagerImpl.getUserName(session, UserStorage
										.getUser(session.getUserId(), session.getContext())), Arrays.toString(msgUIDs),
								imapFolder.getFullName(), e1.getMessage());
					}
				}
				try {
					/*
					 * Update message cache
					 */
					MailMessageCache.getInstance().removeMessages(msgUIDs, sourceFolder, userId, ctx);
				} catch (final OXCachingException e) {
					LOG.error(e.getLocalizedMessage(), e);
				}
			}
			return res;
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapConnection);
		}
	}

	public long[] appendMessages(final String destFolderArg, final MailMessage[] mailMessages) throws MailException {
		try {
			/*
			 * Open and check user rights on source folder
			 */
			final String destFolder = prepareMailFolderParam(destFolderArg);
			imapFolder = setAndOpenFolder(imapFolder, destFolder, Folder.READ_WRITE);
			try {
				if (!holdsMessages()) {
					throw new IMAPException(IMAPException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, imapFolder.getFullName());
				} else if (imapConfig.isSupportsACLs()
						&& !RightsCache.getCachedRights(imapFolder, true, session).contains(Rights.Right.INSERT)) {
					throw new IMAPException(IMAPException.Code.NO_INSERT_ACCESS, imapFolder.getFullName());
				}
			} catch (final MessagingException e) {
				throw new IMAPException(IMAPException.Code.NO_ACCESS, imapFolder.getFullName());
			}
			/*
			 * Convert messages to JavaMail message objects
			 */
			final Message[] msgs = MIMEMessageConverter.convertMailMessages(mailMessages);
			/*
			 * Mark first message for later lookup
			 */
			final String hash = plainStringToMD5(String.valueOf(System.currentTimeMillis()));
			msgs[0].setHeader(MessageHeaders.HDR_X_OX_MARKER, hash);
			/*
			 * ... and append them to folder
			 */
			try {
				/*
				 * TODO: Maybe better to append new messages to cache than
				 * deleting whole content?
				 */
				MailMessageCache.getInstance().removeFolderMessages(destFolder, userId, ctx);
			} catch (final OXCachingException e) {
				LOG.error(e.getLocalizedMessage(), e);
			}
			final AppendUID[] appendUIDs = imapFolder.appendUIDMessages(msgs);
			if (appendUIDs != null && appendUIDs.length > 0 && appendUIDs[0] != null) {
				/*
				 * Assume a proper APPENDUID response code
				 */
				return appendUID2Long(imapFolder.appendUIDMessages(msgs));
			}
			/*
			 * Missing APPENDUID
			 */
			if (LOG.isWarnEnabled()) {
				LOG.warn("Missing APPENDUID response code");
			}
			final long[] retval = new long[msgs.length];
			long uid = IMAPCommandsCollection.findMarker(hash, imapFolder);
			if (uid != -1) {
				for (int i = 0; i < retval.length; i++) {
					retval[i] = uid++;
				}
			} else {
				Arrays.fill(retval, -1L);
			}
			return retval;
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapConnection);
		}
	}

	public void updateMessageFlags(final String folder, final long[] msgUIDs, final int flagsArg, final boolean set)
			throws MailException {
		try {
			final String fullname = prepareMailFolderParam(folder);
			imapFolder = setAndOpenFolder(imapFolder, fullname, Folder.READ_WRITE);
			/*
			 * Remove non user-alterable system flags
			 */
			int flags = flagsArg;
			if (((flags & MailMessage.FLAG_RECENT) > 0)) {
				flags = flags ^ MailMessage.FLAG_RECENT;
			}
			if (((flags & MailMessage.FLAG_USER) > 0)) {
				flags = flags ^ MailMessage.FLAG_USER;
			}
			/*
			 * Set new flags...
			 */
			final Rights myRights = imapConfig.isSupportsACLs() ? RightsCache
					.getCachedRights(imapFolder, true, session) : null;
			final Flags affectedFlags = new Flags();
			if (((flags & MailMessage.FLAG_ANSWERED) > 0)) {
				if (imapConfig.isSupportsACLs() && !myRights.contains(Rights.Right.WRITE)) {
					throw new IMAPException(IMAPException.Code.NO_WRITE_ACCESS, imapFolder.getFullName());
				}
				affectedFlags.add(Flags.Flag.ANSWERED);
			}
			if (((flags & MailMessage.FLAG_DELETED) > 0)) {
				if (imapConfig.isSupportsACLs() && !myRights.contains(Rights.Right.DELETE)) {
					throw new IMAPException(IMAPException.Code.NO_DELETE_ACCESS, imapFolder.getFullName());
				}
				affectedFlags.add(Flags.Flag.DELETED);
			}
			if (((flags & MailMessage.FLAG_DRAFT) > 0)) {
				if (imapConfig.isSupportsACLs() && !myRights.contains(Rights.Right.WRITE)) {
					throw new IMAPException(IMAPException.Code.NO_WRITE_ACCESS, imapFolder.getFullName());
				}
				affectedFlags.add(Flags.Flag.DRAFT);
			}
			if (((flags & MailMessage.FLAG_FLAGGED) > 0)) {
				if (imapConfig.isSupportsACLs() && !myRights.contains(Rights.Right.WRITE)) {
					throw new IMAPException(IMAPException.Code.NO_WRITE_ACCESS, imapFolder.getFullName());
				}
				affectedFlags.add(Flags.Flag.FLAGGED);
			}
			if (((flags & MailMessage.FLAG_SEEN) > 0)) {
				if (imapConfig.isSupportsACLs() && !myRights.contains(Rights.Right.KEEP_SEEN)) {
					throw new IMAPException(IMAPException.Code.NO_KEEP_SEEN_ACCESS, imapFolder.getFullName());
				}
				affectedFlags.add(Flags.Flag.SEEN);
			}
			if (affectedFlags.getSystemFlags().length > 0) {
				final long start = System.currentTimeMillis();
				new FlagsIMAPCommand(imapFolder, msgUIDs, affectedFlags, set, false).doCommand();
				if (LOG.isInfoEnabled()) {
					LOG.info(new StringBuilder(100).append("System Flags applied to ").append(msgUIDs.length).append(
							" messages in ").append((System.currentTimeMillis() - start)).append("msec").toString());
				}
			}
			/*
			 * Check for spam action
			 */
			if (IMAPConfig.isSpamEnabled() && ((flags & MailMessage.FLAG_SPAM) > 0)) {
				handleSpamByUID(msgUIDs, set, true, fullname, Folder.READ_WRITE);
				/*
				 * Remove from cache
				 */
				try {
					if (MailMessageCache.getInstance().containsFolderMessages(fullname, userId, ctx)) {
						MailMessageCache.getInstance().removeMessages(msgUIDs, fullname, userId, ctx);

					}
				} catch (final OXCachingException e) {
					LOG.error(e.getLocalizedMessage(), e);
				}
			} else {
				try {
					if (MailMessageCache.getInstance().containsFolderMessages(fullname, userId, ctx)) {
						/*
						 * Update cache entries
						 */
						final long start = System.currentTimeMillis();
						MailMessageCache.getInstance().updateCachedMessages(msgUIDs, fullname, userId, ctx,
								FIELDS_FLAGS, new Object[] { Integer.valueOf(set ? flags : (flags * -1)) });
						if (LOG.isInfoEnabled()) {
							LOG.info(new StringBuilder(100).append(msgUIDs.length).append(
									" cached messages updated in ").append((System.currentTimeMillis() - start))
									.append("msec").toString());
						}

					}
				} catch (final OXCachingException e) {
					LOG.error(e.getLocalizedMessage(), e);
				}
				/*
				 * Force JavaMail's cache update through folder closure
				 */
				imapFolder.close(false);
				resetIMAPFolder();
			}
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapConnection);
		}
	}

	private static final MailListField[] FIELDS_COLOR_LABEL = new MailListField[] { MailListField.COLOR_LABEL };

	public void updateMessageColorLabel(final String folder, final long[] msgUIDs, final int colorLabel)
			throws MailException {
		try {
			if (!IMAPConfig.isUserFlagsEnabled()) {
				/*
				 * User flags are disabled
				 */
				if (LOG.isDebugEnabled()) {
					LOG.debug("User flags are disabled or not supported. Update of color flag ignored.");
				}
				return;
			}
			final String fullname = prepareMailFolderParam(folder);
			imapFolder = setAndOpenFolder(imapFolder, fullname, Folder.READ_WRITE);
			try {
				if (!holdsMessages()) {
					throw new IMAPException(IMAPException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, imapFolder.getFullName());
				} else if (imapConfig.isSupportsACLs()
						&& !RightsCache.getCachedRights(imapFolder, true, session).contains(Rights.Right.WRITE)) {
					throw new IMAPException(IMAPException.Code.NO_WRITE_ACCESS, imapFolder.getFullName());
				}
			} catch (final MessagingException e) {
				throw new IMAPException(IMAPException.Code.NO_ACCESS, imapFolder.getFullName());
			}
			if (!UserFlagsCache.supportsUserFlags(imapFolder, true, session)) {
				LOG.error(new StringBuilder().append("Folder \"").append(imapFolder.getFullName()).append(
						"\" does not support user-defined flags. Update of color flag ignored."));
				return;
			}
			/*
			 * Remove all old color label flag(s) and set new color label flag
			 */
			long start = System.currentTimeMillis();
			IMAPCommandsCollection.clearAllColorLabels(imapFolder, msgUIDs);
			MailInterfaceImpl.mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
			if (LOG.isInfoEnabled()) {
				LOG.info(new StringBuilder(100).append("All color flags cleared from ").append(msgUIDs.length).append(
						" messages in ").append((System.currentTimeMillis() - start)).append("msec").toString());
			}
			start = System.currentTimeMillis();
			IMAPCommandsCollection.setColorLabel(imapFolder, msgUIDs, MailMessage.getColorLabelStringValue(colorLabel));
			MailInterfaceImpl.mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
			if (LOG.isInfoEnabled()) {
				LOG.info(new StringBuilder(100).append("All color flags set in ").append(msgUIDs.length).append(
						" messages in ").append((System.currentTimeMillis() - start)).append("msec").toString());
			}
			try {
				if (MailMessageCache.getInstance().containsFolderMessages(fullname, userId, ctx)) {
					/*
					 * Update cache entries
					 */
					start = System.currentTimeMillis();
					MailMessageCache.getInstance().updateCachedMessages(msgUIDs, fullname, userId, ctx,
							FIELDS_COLOR_LABEL, new Object[] { Integer.valueOf(colorLabel) });
					if (LOG.isInfoEnabled()) {
						LOG.info(new StringBuilder(100).append(msgUIDs.length).append(" cached messages updated in ")
								.append((System.currentTimeMillis() - start)).append("msec").toString());
					}
				}
			} catch (final OXCachingException e) {
				LOG.error(e.getLocalizedMessage(), e);
			}
			/*
			 * Force JavaMail's cache update through folder closure
			 */
			imapFolder.close(false);
			resetIMAPFolder();
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapConnection);
		} catch (final ProtocolException e) {
			throw IMAPException.handleMessagingException(new MessagingException(e.getLocalizedMessage(), e),
					imapConnection);
		}
	}

	public MailPart getAttachment(final String fullname, final long msgUID, final String sequenceId,
			final boolean displayVersion) throws MailException {
		try {
			imapFolder = setAndOpenFolder(imapFolder, prepareMailFolderParam(fullname), Folder.READ_ONLY);
			final MailMessage mailMessage = MIMEMessageConverter.convertMessage((IMAPMessage) imapFolder
					.getMessageByUID(msgUID));
			final MailPartHandler handler = new MailPartHandler(sequenceId);
			new MailMessageParser().parseMailMessage(mailMessage, handler);
			if (handler.getMailPart() == null) {
				throw new IMAPException(IMAPException.Code.ATTACHMENT_NOT_FOUND, sequenceId, Long.valueOf(msgUID),
						fullname);
			}
			return handler.getMailPart();
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapConnection);
		}
	}

	public MailPart getImageAttachment(final String fullname, final long msgUID, final String cid) throws MailException {
		try {
			imapFolder = setAndOpenFolder(imapFolder, prepareMailFolderParam(fullname), Folder.READ_ONLY);
			final MailMessage mailMessage = MIMEMessageConverter.convertMessage((IMAPMessage) imapFolder
					.getMessageByUID(msgUID));
			final ImageMessageHandler handler = new ImageMessageHandler(cid);
			new MailMessageParser().parseMailMessage(mailMessage, handler);
			if (handler.getImagePart() == null) {
				throw new IMAPException(IMAPException.Code.ATTACHMENT_NOT_FOUND, cid, Long.valueOf(msgUID), fullname);
			}
			return handler.getImagePart();
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapConnection);
		}
	}

	public void releaseResources() throws IMAPException {
		if (null != imapFolder) {
			try {
				imapFolder.close(false);
				resetIMAPFolder();
			} catch (final IllegalStateException e) {
				LOG.warn(WARN_FLD_ALREADY_CLOSED, e);
			} catch (final MessagingException e) {
				LOG.error(e.getLocalizedMessage(), e);
			}
		}
	}

	/*
	 * +++++++++++++++++ Helper methods +++++++++++++++++++
	 */

	private static int createThreadSortMessages(final List<TreeNode> threadList, final int level, final Message[] msgs,
			final int indexArg) {
		int index = indexArg;
		final int threadListSize = threadList.size();
		final Iterator<TreeNode> iter = threadList.iterator();
		for (int i = 0; i < threadListSize; i++) {
			final TreeNode currentNode = iter.next();
			((ContainerMessage) msgs[index]).setThreadLevel(level);
			index++;
			index = createThreadSortMessages(currentNode.getChilds(), level + 1, msgs, index);
		}
		return index;
	}

	private static boolean noUIDsAssigned(final long[] arr, final int expectedLen) {
		final long[] tmp = new long[expectedLen];
		Arrays.fill(tmp, -1L);
		return Arrays.equals(arr, tmp);
	}

	/**
	 * Determines the corresponding UIDs in destination folder
	 * 
	 * @param msgUIDs
	 *            The UIDs in source folder
	 * @param destFullname
	 *            The destination folder's fullname
	 * @return The corresponding UIDs in destination folder
	 * @throws MessagingException
	 */
	private long[] getDestinationUIDs(final long[] msgUIDs, final String destFullname) throws MessagingException {
		/*
		 * No COPYUID present in response code. Since UIDs are assigned in
		 * strictly ascending order in the mailbox (refer to IMAPv4 rfc3501,
		 * section 2.3.1.1), we can discover corresponding UIDs by selecting the
		 * destination mailbox and detecting the location of messages placed in
		 * the destination mailbox by using FETCH and/or SEARCH commands (e.g.,
		 * for Message-ID or some unique marker placed in the message in an
		 * APPEND).
		 */
		final long[] retval = new long[msgUIDs.length];
		Arrays.fill(retval, -1L);
		final String messageId;
		{
			int minIndex = 0;
			long minVal = msgUIDs[0];
			for (int i = 1; i < msgUIDs.length; i++) {
				if (msgUIDs[i] < minVal) {
					minIndex = i;
					minVal = msgUIDs[i];
				}
			}
			messageId = ((IMAPMessage) (imapFolder.getMessageByUID(msgUIDs[minIndex]))).getMessageID();
		}
		if (messageId != null) {
			final IMAPFolder destFolder = (IMAPFolder) imapStore.getFolder(destFullname);
			destFolder.open(Folder.READ_ONLY);
			try {
				/*
				 * Find this message ID in destination folder
				 */
				long startUID = IMAPCommandsCollection.messageId2UID(messageId, destFolder);
				if (startUID != -1) {
					for (int i = 0; i < msgUIDs.length; i++) {
						retval[i] = startUID++;
					}
				}
			} finally {
				destFolder.close(false);
			}
		}
		return retval;
	}

	private void handleSpamByUID(final long[] msgUIDs, final boolean isSpam, final boolean move, final String fullname,
			final int desiredMode) throws MessagingException, MailException {
		/*
		 * Check for spam handling
		 */
		if (usm.isSpamEnabled()) {
			final boolean locatedInSpamFolder = prepareMailFolderParam(
					imapConnection.getFolderStorage().getSpamFolder()).equals(imapFolder.getFullName());
			if (isSpam) {
				if (locatedInSpamFolder) {
					/*
					 * A message that already has been detected as spam should
					 * again be learned as spam: Abort.
					 */
					return;
				}
				/*
				 * Handle spam
				 */
				try {
					SpamHandler.getInstance().handleSpam(imapFolder, msgUIDs, move, imapConnection);
					/*
					 * Close and reopen to force internal message cache update
					 */
					resetIMAPFolder();
					imapFolder = setAndOpenFolder(imapFolder, fullname, desiredMode);
				} catch (final MailException e) {
					throw new IMAPException(e);
				}
				return;
			}
			if (!locatedInSpamFolder) {
				/*
				 * A message that already has been detected as ham should again
				 * be learned as ham: Abort.
				 */
				return;
			}
			/*
			 * Handle ham.
			 */
			try {
				SpamHandler.getInstance().handleHam(imapFolder, msgUIDs, move, imapConnection, imapStore);
				/*
				 * Close and reopen to force internal message cache update
				 */
				resetIMAPFolder();
				imapFolder = setAndOpenFolder(imapFolder, fullname, desiredMode);
			} catch (final MailException e) {
				throw new IMAPException(e);
			}
		}
	}

	private static long[] appendUID2Long(final AppendUID[] appendUIDs) {
		final long[] retval = new long[appendUIDs.length];
		for (int i = 0; i < retval.length; i++) {
			retval[i] = appendUIDs[i].uid;
		}
		return retval;
	}

	private static final String ALG_MD5 = "MD5";

	private static String plainStringToMD5(final String input) {
		final MessageDigest md;
		try {
			/*
			 * Choose MD5 (SHA1 is also possible)
			 */
			md = MessageDigest.getInstance(ALG_MD5);
		} catch (final NoSuchAlgorithmException e) {
			LOG.error("Unable to generate file ID", e);
			return input;
		}
		/*
		 * Reset
		 */
		md.reset();
		/*
		 * Update the digest
		 */
		try {
			md.update(input.getBytes("UTF-8"));
		} catch (final UnsupportedEncodingException e) {
			/*
			 * Should not occur since utf-8 is a known encoding in jsdk
			 */
			LOG.error("Unable to generate file ID", e);
			return input;
		}
		/*
		 * Here comes the hash
		 */
		final byte[] byteHash = md.digest();
		final StringBuilder resultString = new StringBuilder();
		for (int i = 0; i < byteHash.length; i++) {
			resultString.append(Integer.toHexString(0xF0 & byteHash[i]).charAt(0));
		}
		return resultString.toString();
	}
}

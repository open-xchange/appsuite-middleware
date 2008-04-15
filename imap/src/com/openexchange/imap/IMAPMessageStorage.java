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
import static com.openexchange.mail.mime.utils.MIMEStorageUtility.getFetchProfile;

import java.io.IOException;
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
import javax.mail.internet.MimeMessage;

import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.imap.cache.RightsCache;
import com.openexchange.imap.cache.UserFlagsCache;
import com.openexchange.imap.command.CopyIMAPCommand;
import com.openexchange.imap.command.FetchIMAPCommand;
import com.openexchange.imap.command.FlagsIMAPCommand;
import com.openexchange.imap.config.IMAPConfig;
import com.openexchange.imap.search.IMAPSearch;
import com.openexchange.imap.sort.IMAPSort;
import com.openexchange.imap.threadsort.ThreadSortUtil;
import com.openexchange.imap.threadsort.TreeNode;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailListField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.api.MailMessageStorage;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.compose.ComposeType;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.dataobjects.compose.ReferencedMailPart;
import com.openexchange.mail.mime.ExtendedMimeMessage;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.converters.MIMEMessageConverter;
import com.openexchange.mail.mime.filler.MIMEMessageFiller;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.session.Session;
import com.sun.mail.iap.CommandFailedException;
import com.sun.mail.iap.ConnectionException;
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
public final class IMAPMessageStorage extends IMAPFolderWorker {

	private static final transient org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(IMAPMessageStorage.class);

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 1467121647337217270L;

	/*
	 * Flag constants
	 */
	private static final Flags FLAGS_DRAFT = new Flags(Flags.Flag.DRAFT);

	private static final Flags FLAGS_DELETED = new Flags(Flags.Flag.DELETED);

	/*
	 * Empty return value
	 */
	private static final transient MailMessage[] EMPTY_RETVAL = new MailMessage[0];

	/**
	 * Initializes a new {@link IMAPMessageStorage}
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
	public IMAPMessageStorage(final IMAPStore imapStore, final IMAPAccess imapAccess, final Session session)
			throws IMAPException {
		super(imapStore, imapAccess, session);
	}

	@Override
	public MailMessage[] getMessages(final String fullname, final long[] mailIds, final MailField[] fields)
			throws MailException {
		final boolean body;
		{
			final Set<MailField> fieldSet = new HashSet<MailField>(Arrays.asList(fields));
			/*
			 * Check for field FULL
			 */
			if (fieldSet.contains(MailField.FULL)) {
				final MailMessage[] mails = new MailMessage[mailIds.length];
				for (int j = 0; j < mails.length; j++) {
					mails[j] = getMessage(fullname, mailIds[j], true);
				}
				return mails;
			}
			body = fieldSet.contains(MailField.BODY) || fieldSet.contains(MailField.FULL);
		}
		try {
			imapFolder = setAndOpenFolder(imapFolder, fullname, Folder.READ_ONLY);
			final long start = System.currentTimeMillis();
			final Message[] msgs = new FetchIMAPCommand(imapFolder, mailIds, getFetchProfile(fields, IMAPConfig
					.isFastFetch()), false, true, body).doCommand();
			mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
			if (LOG.isDebugEnabled()) {
				LOG.debug(new StringBuilder(128).append("IMAP fetch for ").append(mailIds.length).append(
						" messages took ").append((System.currentTimeMillis() - start)).append("msec").toString());
			}
			if (msgs == null || msgs.length == 0) {
				return EMPTY_RETVAL;
			}
			return MIMEMessageConverter.convertMessages(msgs, fields, body);
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapAccess);
		}
	}

	@Override
	public MailMessage getMessage(final String fullname, final long msgUID, final boolean markSeen)
			throws MailException {
		try {
			imapFolder = setAndOpenFolder(imapFolder, fullname, Folder.READ_WRITE);
			final long start = System.currentTimeMillis();
			final IMAPMessage msg = (IMAPMessage) imapFolder.getMessageByUID(msgUID);
			mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
			if (msg == null) {
				throw new MailException(MailException.Code.MAIL_NOT_FOUND, String.valueOf(msgUID), imapFolder
						.toString());
			}
			final MailMessage mail = MIMEMessageConverter.convertMessage(msg);
			if (!mail.isSeen() && markSeen) {
				mail.setPrevSeen(false);
				if (imapConfig.isSupportsACLs()) {
					try {
						if (RightsCache.getCachedRights(imapFolder, true, session).contains(Rights.Right.KEEP_SEEN)) {
							/*
							 * User has \KEEP_SEEN right: Switch \Seen flag
							 */
							msg.setFlags(FLAGS_SEEN, true);
							mail.setFlag(MailMessage.FLAG_SEEN, true);
							mail.setUnreadMessages(mail.getUnreadMessages() <= 0 ? 0 : mail.getUnreadMessages() - 1);
						}
					} catch (final MessagingException e) {
						if (LOG.isWarnEnabled()) {
							LOG.warn(new StringBuilder("/SEEN flag could not be set on message #").append(
									mail.getMailId()).append(" in folder ").append(mail.getFolder()).toString(), e);
						}
					}
				} else {
					/*
					 * Switch \Seen flag
					 */
					msg.setFlags(FLAGS_SEEN, true);
					mail.setFlag(MailMessage.FLAG_SEEN, true);
					mail.setUnreadMessages(mail.getUnreadMessages() <= 0 ? 0 : mail.getUnreadMessages() - 1);
				}
			}
			return mail;
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapAccess);
		}
	}

	@Override
	public MailMessage[] searchMessages(final String fullname, final IndexRange indexRange,
			final MailListField sortField, final OrderDirection order, final SearchTerm<?> searchTerm,
			final MailField[] fields) throws MailException {
		try {
			imapFolder = setAndOpenFolder(imapFolder, fullname, Folder.READ_ONLY);
			/*
			 * Shall a search be performed?
			 */
			final int[] filter;
			if (null != searchTerm) {
				/*
				 * Preselect message list according to given search pattern
				 */
				filter = IMAPSearch.searchMessages(imapFolder, searchTerm, imapConfig);
				if (filter == null || filter.length == 0) {
					return EMPTY_RETVAL;
				}
			} else {
				filter = null;
			}
			final Set<MailField> usedFields = new HashSet<MailField>();
			Message[] msgs = IMAPSort.sortMessages(imapFolder, filter, fields, sortField, order, UserStorage
					.getStorageUser(session.getUserId(), ctx).getLocale(), usedFields, imapConfig);
			if (indexRange != null) {
				final int fromIndex = indexRange.start;
				int toIndex = indexRange.end;
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
			return MIMEMessageConverter.convertMessages(msgs, usedFields.toArray(new MailField[usedFields.size()]),
					usedFields.contains(MailField.BODY) || usedFields.contains(MailField.FULL));
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapAccess);
		}
	}

	@Override
	public MailMessage[] getThreadSortedMessages(final String fullname, final IndexRange indexRange,
			final SearchTerm<?> searchTerm, final MailField[] fields) throws MailException {
		try {
			if (!imapConfig.getImapCapabilities().hasThreadReferences()) {
				throw new IMAPException(IMAPException.Code.THREAD_SORT_NOT_SUPPORTED);
			}
			imapFolder = setAndOpenFolder(imapFolder, fullname, Folder.READ_ONLY);
			final Set<MailField> usedFields = new HashSet<MailField>();
			/*
			 * Shall a search be performed?
			 */
			final int[] filter;
			if (null != searchTerm) {
				/*
				 * Preselect message list according to given search pattern
				 */
				filter = IMAPSearch.searchMessages(imapFolder, searchTerm, imapConfig);
				if (filter == null || filter.length == 0) {
					return EMPTY_RETVAL;
				}
			} else {
				filter = null;
			}
			Message[] msgs = null;
			final List<TreeNode> threadList;
			{
				/*
				 * Sort messages by thread reference
				 */
				final StringBuilder sortRange;
				if (null != filter) {
					/*
					 * Define sequence of valid message numbers: e.g.:
					 * 2,34,35,43,51
					 */
					sortRange = new StringBuilder(filter.length * 2);
					sortRange.append(filter[0]);
					for (int i = 1; i < filter.length; i++) {
						sortRange.append(filter[i]).append(',');
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
			final FetchProfile fetchProfile = getFetchProfile(fields, null, IMAPConfig.isFastFetch());
			usedFields.addAll(Arrays.asList(fields));
			final boolean body = usedFields.contains(MailField.BODY) || usedFields.contains(MailField.FULL);
			msgs = new FetchIMAPCommand(imapFolder, msgs, fetchProfile, false, true, body).doCommand();
			/*
			 * Apply thread level
			 */
			createThreadSortMessages(threadList, 0, msgs, 0);
			/*
			 * ... and return
			 */
			if (indexRange != null) {
				final int fromIndex = indexRange.start;
				int toIndex = indexRange.end;
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
				msgs = new ExtendedMimeMessage[retvalLength];
				System.arraycopy(tmp, fromIndex, msgs, 0, retvalLength);
			}
			return MIMEMessageConverter.convertMessages(msgs, usedFields.toArray(new MailField[usedFields.size()]),
					body);
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapAccess);
		}
	}

	@Override
	public MailMessage[] getUnreadMessages(final String fullname, final MailListField sortField,
			final OrderDirection order, final MailField[] fields, final int limit) throws MailException {
		try {
			imapFolder = setAndOpenFolder(imapFolder, fullname, Folder.READ_ONLY);
			/*
			 * Get ( & fetch) new messages
			 */
			final long start = System.currentTimeMillis();
			final Message[] msgs = IMAPCommandsCollection.getUnreadMessages(imapFolder, fields, sortField, order,
					UserStorage.getStorageUser(session.getUserId(), ctx).getLocale());
			mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
			if (msgs == null || msgs.length == 0) {
				return EMPTY_RETVAL;
			} else if (limit > 0) {
				final int newLength = Math.min(limit, msgs.length);
				final Message[] retval = new Message[newLength];
				System.arraycopy(msgs, 0, retval, 0, newLength);
				return MIMEMessageConverter.convertMessages(retval, fields);
			}
			return MIMEMessageConverter.convertMessages(msgs, fields);
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapAccess);
		}
	}

	@Override
	public boolean deleteMessages(final String fullname, final long[] msgUIDs, final boolean hardDelete)
			throws MailException {
		try {
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
			final String trashFullname = imapAccess.getFolderStorage().getTrashFolder();
			if (null == trashFullname) {
				// TODO: Bug#8992 -> What to do if trash folder is null
				if (LOG.isErrorEnabled()) {
					LOG.error("\n\tDefault trash folder is not set: aborting delete operation");
				}
				throw new IMAPException(IMAPException.Code.MISSING_DEFAULT_FOLDER_NAME, "trash");
			}
			final boolean backup = (!usm.isHardDeleteMsgs() && !hardDelete && !(fullname.equals(trashFullname)));
			final StringBuilder debug;
			if (LOG.isDebugEnabled()) {
				debug = new StringBuilder(128);
			} else {
				debug = null;
			}
			final long[] remain;
			final int blockSize = IMAPConfig.getBlockSize();
			if (msgUIDs.length > blockSize) {
				/*
				 * Block-wise deletion
				 */
				int offset = 0;
				final long[] tmp = new long[blockSize];
				for (int len = msgUIDs.length; len > blockSize; len -= blockSize) {
					System.arraycopy(msgUIDs, offset, tmp, 0, tmp.length);
					offset += blockSize;
					deleteByUIDs(trashFullname, backup, tmp, debug);
				}
				remain = new long[msgUIDs.length - offset];
				System.arraycopy(msgUIDs, offset, remain, 0, remain.length);
			} else {
				remain = msgUIDs;
			}
			deleteByUIDs(trashFullname, backup, remain, debug);
			/*
			 * Close folder to force JavaMail-internal message cache update
			 */
			imapFolder.close(false);
			resetIMAPFolder();
			return true;
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapAccess);
		}
	}

	private void deleteByUIDs(final String trashFullname, final boolean backup, final long[] tmp, final StringBuilder sb)
			throws MailException, IMAPException, MessagingException {
		if (backup) {
			/*
			 * Copy messages to folder "TRASH"
			 */
			try {
				final long start = System.currentTimeMillis();
				new CopyIMAPCommand(imapFolder, tmp, trashFullname, false, true).doCommand();
				if (LOG.isDebugEnabled()) {
					sb.setLength(0);
					LOG.debug(sb.append("\"Soft Delete\": ").append(tmp.length).append(
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
		 * Mark messages as \DELETED...
		 */
		final long start = System.currentTimeMillis();
		new FlagsIMAPCommand(imapFolder, tmp, FLAGS_DELETED, true, false).doCommand();
		if (LOG.isDebugEnabled()) {
			sb.setLength(0);
			LOG.debug(sb.append(tmp.length).append(" messages marked as deleted (through system flag \\DELETED) in ")
					.append((System.currentTimeMillis() - start)).append("msec").toString());
		}
		/*
		 * ... and perform EXPUNGE
		 */
		try {
			IMAPCommandsCollection.uidExpungeWithFallback(imapFolder, tmp);
		} catch (final ConnectionException e) {
			/*
			 * Connection is broken. Not possible to retry.
			 */
			throw new IMAPException(IMAPException.Code.CONNECTION_ERROR, e, imapAccess.getMailConfig().getServer(),
					imapAccess.getMailConfig().getLogin());
		} catch (final ProtocolException e) {
			throw new IMAPException(IMAPException.Code.UID_EXPUNGE_FAILED, e, Arrays.toString(tmp), imapFolder
					.getFullName(), e.getMessage());
		}
	}

	@Override
	public long[] copyMessages(final String sourceFolder, final String destFolder, final long[] mailIds,
			final boolean fast) throws MailException {
		return copyOrMoveMessages(sourceFolder, destFolder, mailIds, false, fast);
	}

	@Override
	public long[] moveMessages(final String sourceFolder, final String destFolder, final long[] mailIds,
			final boolean fast) throws MailException {
		return copyOrMoveMessages(sourceFolder, destFolder, mailIds, true, fast);
	}

	private long[] copyOrMoveMessages(final String sourceFullname, final String destFullname, final long[] msgUIDs,
			final boolean move, final boolean fast) throws MailException {
		try {
			if (sourceFullname == null || sourceFullname.length() == 0) {
				throw new IMAPException(IMAPException.Code.MISSING_SOURCE_TARGET_FOLDER_ON_MOVE, "source");
			} else if (destFullname == null || destFullname.length() == 0) {
				throw new IMAPException(IMAPException.Code.MISSING_SOURCE_TARGET_FOLDER_ON_MOVE, "target");
			} else if (sourceFullname.equals(destFullname) && move) {
				throw new IMAPException(IMAPException.Code.NO_EQUAL_MOVE, sourceFullname);
			}
			/*
			 * Open and check user rights on source folder
			 */
			imapFolder = setAndOpenFolder(imapFolder, sourceFullname, Folder.READ_WRITE);
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
			final long[] result = new long[msgUIDs.length];
			final int blockSize = IMAPConfig.getBlockSize();
			final StringBuilder debug;
			if (LOG.isDebugEnabled()) {
				debug = new StringBuilder(128);
			} else {
				debug = null;
			}

			int offset = 0;
			final long[] remain;
			if (msgUIDs.length > blockSize) {
				/*
				 * Block-wise deletion
				 */
				final long[] tmp = new long[blockSize];
				for (int len = msgUIDs.length; len > blockSize; len -= blockSize) {
					System.arraycopy(msgUIDs, offset, tmp, 0, tmp.length);
					final long[] uids = copyOrMoveByUID(move, fast, destFullname, tmp, debug);
					/*
					 * Append UIDs
					 */
					System.arraycopy(uids, 0, result, offset, uids.length);
					offset += blockSize;
				}
				remain = new long[msgUIDs.length - offset];
				System.arraycopy(msgUIDs, offset, remain, 0, remain.length);
			} else {
				remain = msgUIDs;
			}
			final long[] uids = copyOrMoveByUID(move, fast, destFullname, remain, debug);
			System.arraycopy(uids, 0, result, offset, uids.length);
			if (move) {
				/*
				 * Force folder cache update through a close
				 */
				imapFolder.close(false);
				resetIMAPFolder();
			}
			final String draftFullname = imapAccess.getFolderStorage().getDraftsFolder();
			if (destFullname.equals(draftFullname)) {
				/*
				 * A copy/move to drafts folder. Ensure to set \Draft flag.
				 */
				final IMAPFolder destFolder = setAndOpenFolder(destFullname, Folder.READ_WRITE);
				try {
					final long start = System.currentTimeMillis();
					new FlagsIMAPCommand(destFolder, FLAGS_DRAFT, true).doCommand();
					if (LOG.isDebugEnabled()) {
						LOG.debug(new StringBuilder(128).append(
								"A copy/move to default drafts folder => All messages' \\Draft flag in ").append(
								destFullname).append(" set in ").append((System.currentTimeMillis() - start)).append(
								"msec").toString());
					}
				} finally {
					destFolder.close(false);
				}
			} else if (sourceFullname.equals(draftFullname)) {
				/*
				 * A copy/move from drafts folder. Ensure to unset \Draft flag.
				 */
				final IMAPFolder destFolder = setAndOpenFolder(destFullname, Folder.READ_WRITE);
				try {
					final long start = System.currentTimeMillis();
					new FlagsIMAPCommand(destFolder, FLAGS_DRAFT, false).doCommand();
					if (LOG.isDebugEnabled()) {
						LOG.debug(new StringBuilder(128).append(
								"A copy/move from default drafts folder => All messages' \\Draft flag in ").append(
								destFullname).append(" unset in ").append((System.currentTimeMillis() - start)).append(
								"msec").toString());
					}
				} finally {
					destFolder.close(false);
				}
			}
			return result;
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapAccess);
		}
	}

	private long[] copyOrMoveByUID(final boolean move, final boolean fast, final String destFullname, final long[] tmp,
			final StringBuilder sb) throws MessagingException, MailException, IMAPException {
		long start = System.currentTimeMillis();
		long[] uids = new CopyIMAPCommand(imapFolder, tmp, destFullname, false, fast).doCommand();
		if (LOG.isDebugEnabled()) {
			sb.setLength(0);
			LOG.debug(sb.append(tmp.length).append(" messages copied in ").append((System.currentTimeMillis() - start))
					.append("msec").toString());
		}
		if (!fast && (uids == null || noUIDsAssigned(uids, tmp.length))) {
			/*
			 * Invalid UIDs
			 */
			uids = getDestinationUIDs(tmp, destFullname);
		}
		if (move) {
			start = System.currentTimeMillis();
			new FlagsIMAPCommand(imapFolder, tmp, FLAGS_DELETED, true, false).doCommand();
			if (LOG.isDebugEnabled()) {
				sb.setLength(0);
				LOG.debug(sb.append(tmp.length).append(
						" messages marked as expunged (through system flag \\DELETED) in ").append(
						(System.currentTimeMillis() - start)).append("msec").toString());
			}
			try {
				IMAPCommandsCollection.uidExpungeWithFallback(imapFolder, tmp);
			} catch (final ConnectionException e) {
				/*
				 * Connection is broken. Not possible to retry.
				 */
				throw new IMAPException(IMAPException.Code.CONNECTION_ERROR, e, imapAccess.getMailConfig().getServer(),
						imapAccess.getMailConfig().getLogin());
			} catch (final ProtocolException e) {
				throw new IMAPException(IMAPException.Code.UID_EXPUNGE_FAILED, e, Arrays.toString(tmp), imapFolder
						.getFullName(), e.getMessage());
			}
		}
		return uids;
	}

	@Override
	public long[] appendMessages(final String destFullname, final MailMessage[] mailMessages) throws MailException {
		try {
			/*
			 * Open and check user rights on source folder
			 */
			imapFolder = setAndOpenFolder(imapFolder, destFullname, Folder.READ_WRITE);
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
			final AppendUID[] appendUIDs = imapFolder.appendUIDMessages(msgs);
			if (appendUIDs != null && appendUIDs.length > 0 && appendUIDs[0] != null) {
				/*
				 * Assume a proper APPENDUID response code
				 */
				return appendUID2Long(appendUIDs);
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
			throw IMAPException.handleMessagingException(e, imapAccess);
		}
	}

	@Override
	public void updateMessageFlags(final String fullname, final long[] msgUIDs, final int flagsArg, final boolean set)
			throws MailException {
		try {
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
				if (LOG.isDebugEnabled()) {
					LOG.debug(new StringBuilder(128).append("System Flags applied to ").append(msgUIDs.length).append(
							" messages in ").append((System.currentTimeMillis() - start)).append("msec").toString());
				}
			}
			/*
			 * Check for spam action
			 */
			if (usm.isSpamEnabled() && ((flags & MailMessage.FLAG_SPAM) > 0)) {
				handleSpamByUID(msgUIDs, set, true, fullname, Folder.READ_WRITE);
			} else {
				/*
				 * Force JavaMail's cache update through folder closure
				 */
				imapFolder.close(false);
				resetIMAPFolder();
			}
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapAccess);
		}
	}

	@Override
	public void updateMessageColorLabel(final String fullname, final long[] msgUIDs, final int colorLabel)
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
			mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
			if (LOG.isDebugEnabled()) {
				LOG.debug(new StringBuilder(128).append("All color flags cleared from ").append(msgUIDs.length).append(
						" messages in ").append((System.currentTimeMillis() - start)).append("msec").toString());
			}
			start = System.currentTimeMillis();
			IMAPCommandsCollection.setColorLabel(imapFolder, msgUIDs, MailMessage.getColorLabelStringValue(colorLabel));
			mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
			if (LOG.isDebugEnabled()) {
				LOG.debug(new StringBuilder(128).append("All color flags set in ").append(msgUIDs.length).append(
						" messages in ").append((System.currentTimeMillis() - start)).append("msec").toString());
			}
			/*
			 * Force JavaMail's cache update through folder closure
			 */
			imapFolder.close(false);
			resetIMAPFolder();
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapAccess);
		} catch (final ProtocolException e) {
			throw IMAPException
					.handleMessagingException(new MessagingException(e.getLocalizedMessage(), e), imapAccess);
		}
	}

	@Override
	public MailMessage saveDraft(final String draftFullname, final ComposedMailMessage composedMail)
			throws MailException {
		try {
			if (!composedMail.isDraft()) {
				composedMail.setFlag(MailMessage.FLAG_DRAFT, true);
			}
			final MimeMessage mimeMessage = new MimeMessage(imapAccess.getSession());
			mimeMessage.setFlag(Flags.Flag.DRAFT, true);
			/*
			 * Check for edit-draft operation
			 */
			final List<String> tempIds;
			if (composedMail.getMsgref() != null) {
				/*
				 * Load referenced mail parts from original message
				 */
				tempIds = ReferencedMailPart.loadReferencedParts(composedMail, session);
			} else {
				tempIds = null;
			}
			/*
			 * Fill message
			 */
			final long uid;
			final MIMEMessageFiller filler = new MIMEMessageFiller(session, ctx);
			try {
				/*
				 * Set headers
				 */
				filler.setMessageHeaders(composedMail, mimeMessage);
				/*
				 * Set common headers
				 */
				filler.setCommonHeaders(mimeMessage);
				/*
				 * Fill body
				 */
				filler.fillMailBody(composedMail, mimeMessage, ComposeType.NEW, null);
				mimeMessage.saveChanges();
				/*
				 * Append message to draft folder
				 */
				uid = appendMessages(draftFullname, new MailMessage[] { MIMEMessageConverter
						.convertMessage(mimeMessage) })[0];
			} finally {
				filler.deleteReferencedUploadFiles();
				if (null != tempIds) {
					for (final String id : tempIds) {
						session.removeUploadedFile(id);
					}
				}
			}
			/*
			 * Check for draft-edit operation: Delete old version
			 */
			final MailMessage refMail = composedMail.getReferencedMail();
			if (refMail != null) {
				if (refMail.isDraft()) {
					deleteMessages(refMail.getFolder(), new long[] { composedMail.getReferencedMail().getMailId() },
							true);
				}
				composedMail.setMsgref(null);
			}
			/*
			 * Return draft mail
			 */
			return getMessage(draftFullname, uid, true);
		} catch (final MessagingException e) {
			throw IMAPException.handleMessagingException(e, imapAccess);
		} catch (final IOException e) {
			throw new IMAPException(IMAPException.Code.IO_ERROR, e, e.getLocalizedMessage());
		}
	}

	@Override
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
			((ExtendedMimeMessage) msgs[index]).setThreadLevel(level);
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
			final boolean locatedInSpamFolder = imapAccess.getFolderStorage().getSpamFolder().equals(
					imapFolder.getFullName());
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
					IMAPProvider.getInstance().getSpamHandler().handleSpam(imapFolder.getFullName(), msgUIDs, move,
							imapAccess);
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
				IMAPProvider.getInstance().getSpamHandler().handleHam(imapFolder.getFullName(), msgUIDs, move,
						imapAccess);
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
			LOG.error("Unable to generate ID", e);
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
			 * Should not occur since utf-8 is a known encoding
			 */
			LOG.error("Unable to generate file ID", e);
			return input;
		}
		/*
		 * Here comes the hash
		 */
		final byte[] byteHash = md.digest();
		final StringBuilder resultString = new StringBuilder(16);
		for (int i = 0; i < byteHash.length; i++) {
			resultString.append(Integer.toHexString(0xF0 & byteHash[i]).charAt(0));
		}
		return resultString.toString();
	}

}

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
import static com.openexchange.mail.mime.utils.MIMEStorageUtility.getFetchProfile;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.StoreClosedException;
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
import com.openexchange.imap.threadsort.ThreadSortNode;
import com.openexchange.imap.threadsort.ThreadSortUtil;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailFields;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.compose.ComposeType;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.mime.ExtendedMimeMessage;
import com.openexchange.mail.mime.MIMEMailException;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.converters.MIMEMessageConverter;
import com.openexchange.mail.mime.filler.MIMEMessageFiller;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.session.Session;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.AppendUID;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.Rights;

/**
 * {@link IMAPMessageStorage} - The IMAP implementation of message storage.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IMAPMessageStorage extends IMAPFolderWorker {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(IMAPMessageStorage.class);

    /**
     * Serial version UID
     */
    private static final long serialVersionUID = 1467121647337217270L;

    /*-
     * Flag constants
     */

    private static final Flags FLAGS_DRAFT = new Flags(Flags.Flag.DRAFT);

    private static final Flags FLAGS_DELETED = new Flags(Flags.Flag.DELETED);

    /*-
     * String constants
     */

    private static final String STR_MSEC = "msec";

    /**
     * Initializes a new {@link IMAPMessageStorage}.
     * 
     * @param imapStore The IMAP store
     * @param imapAccess The IMAP access
     * @param session The session providing needed user data
     * @throws IMAPException If context loading fails
     */
    public IMAPMessageStorage(final IMAPStore imapStore, final IMAPAccess imapAccess, final Session session) throws IMAPException {
        super(imapStore, imapAccess, session);
    }

    @Override
    public MailMessage[] getMessages(final String fullname, final long[] mailIds, final MailField[] fields) throws MailException {
        if ((mailIds == null) || (mailIds.length == 0)) {
            return EMPTY_RETVAL;
        }
        final boolean body;
        {
            final MailFields fieldSet = new MailFields(fields);
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
            /*
             * Fetch desired messages by given UIDs. Turn UIDs to corresponding sequence numbers to maintain order cause some IMAP servers
             * ignore the order of UIDs provided in a "UID FETCH" command.
             */
            final int[] seqNums = IMAPCommandsCollection.uids2SeqNums(imapFolder, mailIds);
            final Message[] messages = new Message[seqNums.length];
            final FetchProfile fetchProfile = getFetchProfile(fields, IMAPConfig.isFastFetch());
            final boolean isRev1 = imapConfig.getImapCapabilities().hasIMAP4rev1();
            int lastPos = 0;
            int pos = 0;
            while (pos < seqNums.length) {
                if (seqNums[pos] <= 0) {
                    final int len = pos - lastPos;
                    if (len > 0) {
                        fetchValidSeqNums(lastPos, len, seqNums, messages, fetchProfile, isRev1, body);
                    }
                    // Determine next valid position
                    pos++;
                    while (pos < seqNums.length && -1 == seqNums[pos]) {
                        pos++;
                    }
                    lastPos = pos;
                } else {
                    pos++;
                }
            }
            if (lastPos < pos) {
                fetchValidSeqNums(lastPos, pos - lastPos, seqNums, messages, fetchProfile, isRev1, body);
            }
            return MIMEMessageConverter.convertMessages(messages, fields, body);
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e, imapConfig);
        }
    }

    private void fetchValidSeqNums(final int lastPos, final int len, final int[] seqNums, final Message[] messages, final FetchProfile fetchProfile, final boolean isRev1, final boolean body) throws MessagingException {
        final int[] subarr = new int[len];
        System.arraycopy(seqNums, lastPos, subarr, 0, len);
        final long start = System.currentTimeMillis();
        final Message[] submessages = new FetchIMAPCommand(imapFolder, isRev1, subarr, fetchProfile, false, true, body).doCommand();
        mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
        if (LOG.isDebugEnabled()) {
            LOG.debug(new StringBuilder(128).append("IMAP fetch for ").append(subarr.length).append(" messages took ").append(
                (System.currentTimeMillis() - start)).append(STR_MSEC).toString());
        }
        System.arraycopy(submessages, 0, messages, lastPos, submessages.length);
    }

    @Override
    public MailMessage getMessage(final String fullname, final long msgUID, final boolean markSeen) throws MailException {
        try {
            imapFolder = setAndOpenFolder(imapFolder, fullname, Folder.READ_WRITE);
            final IMAPMessage msg;
            {
                final long start = System.currentTimeMillis();
                msg = (IMAPMessage) imapFolder.getMessageByUID(msgUID);
                mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
            }
            if (msg == null) {
                // throw new MailException(MailException.Code.MAIL_NOT_FOUND,
                // String.valueOf(msgUID), imapFolder
                // .toString());
                return null;
            }
            final MailMessage mail;
            try {
                mail = MIMEMessageConverter.convertMessage(msg);
            } catch (final MIMEMailException e) {
                if (MIMEMailException.Code.MESSAGE_REMOVED.getNumber() == e.getDetailNumber()) {
                    /*
                     * Obviously message was removed in the meantime
                     */
                    return null;
                }
                throw e;
            }
            if (!mail.isSeen() && markSeen) {
                mail.setPrevSeen(false);
                if (imapConfig.isSupportsACLs()) {
                    try {
                        if (aclExtension.canKeepSeen(RightsCache.getCachedRights(imapFolder, true, session, accountId))) {
                            /*
                             * User has \KEEP_SEEN right: Switch \Seen flag
                             */
                            msg.setFlags(FLAGS_SEEN, true);
                            mail.setFlag(MailMessage.FLAG_SEEN, true);
                            mail.setUnreadMessages(mail.getUnreadMessages() <= 0 ? 0 : mail.getUnreadMessages() - 1);
                        }
                    } catch (final MessagingException e) {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn(new StringBuilder("/SEEN flag could not be set on message #").append(mail.getMailId()).append(
                                " in folder ").append(mail.getFolder()).toString(), e);
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
            throw MIMEMailException.handleMessagingException(e, imapConfig);
        }
    }

    @Override
    public MailMessage[] searchMessages(final String fullname, final IndexRange indexRange, final MailSortField sortField, final OrderDirection order, final SearchTerm<?> searchTerm, final MailField[] fields) throws MailException {
        try {
            imapFolder = setAndOpenFolder(imapFolder, fullname, Folder.READ_ONLY);
            if (imapFolder.getMessageCount() == 0) {
                return EMPTY_RETVAL;
            }
            /*
             * Shall a search be performed?
             */
            final int[] filter;
            if (null == searchTerm) {
                /*
                 * Check if an all-fetch can be performed to only obtain UIDs of all folder's messages: FETCH 1: (UID)
                 */
                if (MailSortField.RECEIVED_DATE.equals(sortField) && onlyFolderAndID(fields)) {
                    return performAllFetch(fullname, order, indexRange);
                }
                /*
                 * Proceed with common handling
                 */
                filter = null;
            } else {
                /*
                 * Preselect message list according to given search pattern
                 */
                filter = IMAPSearch.searchMessages(imapFolder, searchTerm, imapConfig);
                if ((filter == null) || (filter.length == 0)) {
                    return EMPTY_RETVAL;
                }
            }
            final MailFields usedFields = new MailFields();
            Message[] msgs = IMAPSort.sortMessages(imapFolder, filter, fields, sortField, order, UserStorage.getStorageUser(
                session.getUserId(),
                ctx).getLocale(), usedFields, imapConfig);
            if (indexRange != null) {
                final int fromIndex = indexRange.start;
                int toIndex = indexRange.end;
                if ((msgs == null) || (msgs.length == 0)) {
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
                if (toIndex >= msgs.length) {
                    toIndex = msgs.length;
                }
                final Message[] tmp = msgs;
                final int retvalLength = toIndex - fromIndex;
                msgs = new Message[retvalLength];
                System.arraycopy(tmp, fromIndex, msgs, 0, retvalLength);
            }
            return MIMEMessageConverter.convertMessages(
                msgs,
                usedFields.toArray(),
                usedFields.contains(MailField.BODY) || usedFields.contains(MailField.FULL));
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e, imapConfig);
        }
    }

    @Override
    public MailMessage[] getThreadSortedMessages(final String fullname, final IndexRange indexRange, final SearchTerm<?> searchTerm, final MailField[] fields) throws MailException {
        try {
            if (!imapConfig.getImapCapabilities().hasThreadReferences()) {
                throw new IMAPException(IMAPException.Code.THREAD_SORT_NOT_SUPPORTED);
            }
            imapFolder = setAndOpenFolder(imapFolder, fullname, Folder.READ_ONLY);
            final MailFields usedFields = new MailFields();
            /*
             * Shall a search be performed?
             */
            final int[] filter;
            if (null == searchTerm) {
                filter = null;
            } else {
                /*
                 * Preselect message list according to given search pattern
                 */
                filter = IMAPSearch.searchMessages(imapFolder, searchTerm, imapConfig);
                if ((filter == null) || (filter.length == 0)) {
                    return EMPTY_RETVAL;
                }
            }
            Message[] msgs = null;
            final List<ThreadSortNode> threadList;
            {
                /*
                 * Sort messages by thread reference
                 */
                final StringBuilder sortRange;
                if (null == filter) {
                    /*
                     * Select all messages
                     */
                    sortRange = new StringBuilder(3).append("ALL");
                } else {
                    /*
                     * Define sequence of valid message numbers: e.g.: 2,34,35,43,51
                     */
                    sortRange = new StringBuilder(filter.length << 1);
                    sortRange.append(filter[0]);
                    for (int i = 1; i < filter.length; i++) {
                        sortRange.append(filter[i]).append(',');
                    }
                }
                final long start = System.currentTimeMillis();
                final String threadResp = ThreadSortUtil.getThreadResponse(imapFolder, sortRange.toString());
                mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
                /*
                 * Parse THREAD response
                 */
                threadList = ThreadSortUtil.parseThreadResponse(threadResp);
                msgs = ThreadSortUtil.getMessagesFromThreadResponse(imapFolder.getFullName(), imapFolder.getSeparator(), threadResp);
            }
            /*
             * Fetch messages
             */
            final FetchProfile fetchProfile = getFetchProfile(fields, null, IMAPConfig.isFastFetch());
            usedFields.addAll(Arrays.asList(fields));
            final boolean body = usedFields.contains(MailField.BODY) || usedFields.contains(MailField.FULL);
            msgs = new FetchIMAPCommand(imapFolder, imapConfig.getImapCapabilities().hasIMAP4rev1(), msgs, fetchProfile, false, true, body).doCommand();
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
                if ((msgs == null) || (msgs.length == 0)) {
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
                if (toIndex >= msgs.length) {
                    toIndex = msgs.length;
                }
                final Message[] tmp = msgs;
                final int retvalLength = toIndex - fromIndex;
                msgs = new ExtendedMimeMessage[retvalLength];
                System.arraycopy(tmp, fromIndex, msgs, 0, retvalLength);
            }
            return MIMEMessageConverter.convertMessages(msgs, usedFields.toArray(), body);
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e, imapConfig);
        }
    }

    @Override
    public MailMessage[] getUnreadMessages(final String fullname, final MailSortField sortField, final OrderDirection order, final MailField[] fields, final int limit) throws MailException {
        try {
            imapFolder = setAndOpenFolder(imapFolder, fullname, Folder.READ_ONLY);
            /*
             * Get ( & fetch) new messages
             */
            final long start = System.currentTimeMillis();
            final Message[] msgs = IMAPCommandsCollection.getUnreadMessages(
                imapFolder,
                fields,
                sortField,
                order,
                UserStorage.getStorageUser(session.getUserId(), ctx).getLocale());
            mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
            if ((msgs == null) || (msgs.length == 0) || limit == 0) {
                return EMPTY_RETVAL;
            } else if (limit > 0) {
                final int newLength = ((limit <= msgs.length) ? limit : msgs.length);
                final Message[] retval = new Message[newLength];
                System.arraycopy(msgs, 0, retval, 0, newLength);
                return MIMEMessageConverter.convertMessages(retval, fields);
            }
            return MIMEMessageConverter.convertMessages(msgs, fields);
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e, imapConfig);
        }
    }

    @Override
    public void deleteMessages(final String fullname, final long[] msgUIDs, final boolean hardDelete) throws MailException {
        try {
            imapFolder = setAndOpenFolder(imapFolder, fullname, Folder.READ_WRITE);
            try {
                if (!holdsMessages()) {
                    throw new IMAPException(IMAPException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, imapFolder.getFullName());
                }
                if (imapConfig.isSupportsACLs() && !aclExtension.canDeleteMessages(RightsCache.getCachedRights(
                    imapFolder,
                    true,
                    session,
                    accountId))) {
                    throw new IMAPException(IMAPException.Code.NO_DELETE_ACCESS, imapFolder.getFullName());
                }
            } catch (final MessagingException e) {
                throw new IMAPException(IMAPException.Code.NO_ACCESS, e, imapFolder.getFullName());
            }
            if (hardDelete || usm.isHardDeleteMsgs()) {
                blockwiseDeletion(msgUIDs, false, null);
                return;
            }
            final String trashFullname = imapAccess.getFolderStorage().getTrashFolder();
            if (null == trashFullname) {
                // TODO: Bug#8992 -> What to do if trash folder is null
                if (LOG.isErrorEnabled()) {
                    LOG.error("\n\tDefault trash folder is not set: aborting delete operation");
                }
                throw new IMAPException(IMAPException.Code.MISSING_DEFAULT_FOLDER_NAME, "trash");
            }
            final boolean backup = (!(fullname.startsWith(trashFullname)));
            blockwiseDeletion(msgUIDs, backup, backup ? trashFullname : null);
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e, imapConfig);
        }
    }

    private void blockwiseDeletion(final long[] msgUIDs, final boolean backup, final String trashFullname) throws MailException, MessagingException {
        final StringBuilder debug = LOG.isDebugEnabled() ? new StringBuilder(128) : null;
        final long[] remain;
        final int blockSize = IMAPConfig.getBlockSize();
        if (blockSize > 0 && msgUIDs.length > blockSize) {
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
    }

    private void deleteByUIDs(final String trashFullname, final boolean backup, final long[] uids, final StringBuilder sb) throws MailException, MessagingException {
        if (backup) {
            /*
             * Copy messages to folder "TRASH"
             */
            try {
                final long start = System.currentTimeMillis();
                new CopyIMAPCommand(imapFolder, uids, trashFullname, false, true).doCommand();
                if (LOG.isDebugEnabled()) {
                    sb.setLength(0);
                    LOG.debug(sb.append("\"Soft Delete\": ").append(uids.length).append(" messages copied to default trash folder \"").append(
                        trashFullname).append("\" in ").append((System.currentTimeMillis() - start)).append(STR_MSEC).toString());
                }
            } catch (final MessagingException e) {
                if (e.getMessage().indexOf("Over quota") > -1) {
                    /*
                     * We face an Over-Quota-Exception
                     */
                    throw new MailException(MailException.Code.DELETE_FAILED_OVER_QUOTA, e, new Object[0]);
                }
                final Exception nestedExc = e.getNextException();
                if (nestedExc != null && nestedExc.getMessage().indexOf("Over quota") > -1) {
                    /*
                     * We face an Over-Quota-Exception
                     */
                    throw new MailException(MailException.Code.DELETE_FAILED_OVER_QUOTA, e, new Object[0]);
                }
                throw new IMAPException(IMAPException.Code.MOVE_ON_DELETE_FAILED, e, new Object[0]);
            }
        }
        /*
         * Mark messages as \DELETED...
         */
        final long start = System.currentTimeMillis();
        new FlagsIMAPCommand(imapFolder, uids, FLAGS_DELETED, true, true, false).doCommand();
        if (LOG.isDebugEnabled()) {
            sb.setLength(0);
            LOG.debug(sb.append(uids.length).append(" messages marked as deleted (through system flag \\DELETED) in ").append(
                (System.currentTimeMillis() - start)).append(STR_MSEC).toString());
        }
        /*
         * ... and perform EXPUNGE
         */
        try {
            IMAPCommandsCollection.uidExpungeWithFallback(imapFolder, uids, imapConfig.getImapCapabilities().hasUIDPlus());
        } catch (final FolderClosedException e) {
            /*
             * Not possible to retry since connection is broken
             */
            throw new IMAPException(
                IMAPException.Code.CONNECT_ERROR,
                e,
                imapAccess.getMailConfig().getServer(),
                imapAccess.getMailConfig().getLogin());
        } catch (final StoreClosedException e) {
            /*
             * Not possible to retry since connection is broken
             */
            throw new IMAPException(
                IMAPException.Code.CONNECT_ERROR,
                e,
                imapAccess.getMailConfig().getServer(),
                imapAccess.getMailConfig().getLogin());
        } catch (final MessagingException e) {
            throw new IMAPException(
                IMAPException.Code.UID_EXPUNGE_FAILED,
                e,
                Arrays.toString(uids),
                imapFolder.getFullName(),
                e.getMessage());
        }
    }

    @Override
    public long[] copyMessages(final String sourceFolder, final String destFolder, final long[] mailIds, final boolean fast) throws MailException {
        return copyOrMoveMessages(sourceFolder, destFolder, mailIds, false, fast);
    }

    @Override
    public long[] moveMessages(final String sourceFolder, final String destFolder, final long[] mailIds, final boolean fast) throws MailException {
        if (DEFAULT_FOLDER_ID.equals(destFolder)) {
            throw new IMAPException(IMAPException.Code.NO_ROOT_MOVE);
        }
        return copyOrMoveMessages(sourceFolder, destFolder, mailIds, true, fast);
    }

    private long[] copyOrMoveMessages(final String sourceFullname, final String destFullname, final long[] msgUIDs, final boolean move, final boolean fast) throws MailException {
        try {
            if ((sourceFullname == null) || (sourceFullname.length() == 0)) {
                throw new IMAPException(IMAPException.Code.MISSING_SOURCE_TARGET_FOLDER_ON_MOVE, "source");
            } else if ((destFullname == null) || (destFullname.length() == 0)) {
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
                }
                if (move && imapConfig.isSupportsACLs() && !aclExtension.canDeleteMessages(RightsCache.getCachedRights(
                    imapFolder,
                    true,
                    session,
                    accountId))) {
                    throw new IMAPException(IMAPException.Code.NO_DELETE_ACCESS, imapFolder.getFullName());
                }
            } catch (final MessagingException e) {
                throw new IMAPException(IMAPException.Code.NO_ACCESS, e, imapFolder.getFullName());
            }
            {
                /*
                 * Open and check user rights on destination folder
                 */
                final IMAPFolder destFolder = (IMAPFolder) imapStore.getFolder(destFullname);
                try {
                    if (!destFolder.exists()) {
                        throw new IMAPException(IMAPException.Code.FOLDER_NOT_FOUND, destFullname);
                    }
                    if ((destFolder.getType() & Folder.HOLDS_MESSAGES) == 0) {
                        throw new IMAPException(IMAPException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, destFullname);
                    }
                } catch (final MessagingException e) {
                    throw IMAPException.handleMessagingException(e, imapConfig);
                }
                try {
                    /*
                     * Check if COPY/APPEND is allowed on destination folder
                     */
                    if (imapConfig.isSupportsACLs() && !aclExtension.canInsert(RightsCache.getCachedRights(
                        destFolder,
                        true,
                        session,
                        accountId))) {
                        throw new IMAPException(IMAPException.Code.NO_INSERT_ACCESS, destFolder.getFullName());
                    }
                } catch (final MessagingException e) {
                    throw new IMAPException(IMAPException.Code.NO_ACCESS, e, destFolder.getFullName());
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
            if (blockSize > 0 && msgUIDs.length > blockSize) {
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
                    if (destFolder.getMessageCount() > 0) {
                        final long start = System.currentTimeMillis();
                        new FlagsIMAPCommand(destFolder, FLAGS_DRAFT, true, true).doCommand();
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(new StringBuilder(128).append(
                                "A copy/move to default drafts folder => All messages' \\Draft flag in ").append(destFullname).append(
                                " set in ").append((System.currentTimeMillis() - start)).append(STR_MSEC).toString());
                        }
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
                    new FlagsIMAPCommand(destFolder, FLAGS_DRAFT, false, true).doCommand();
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(new StringBuilder(128).append("A copy/move from default drafts folder => All messages' \\Draft flag in ").append(
                            destFullname).append(" unset in ").append((System.currentTimeMillis() - start)).append(STR_MSEC).toString());
                    }
                } finally {
                    destFolder.close(false);
                }
            }
            return result;
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e, imapConfig);
        }
    }

    private long[] copyOrMoveByUID(final boolean move, final boolean fast, final String destFullname, final long[] tmp, final StringBuilder sb) throws MessagingException, MailException, IMAPException {
        long start = System.currentTimeMillis();
        long[] uids = new CopyIMAPCommand(imapFolder, tmp, destFullname, false, fast).doCommand();
        if (LOG.isDebugEnabled()) {
            sb.setLength(0);
            LOG.debug(sb.append(tmp.length).append(" messages copied in ").append((System.currentTimeMillis() - start)).append(STR_MSEC).toString());
        }
        if (!fast && ((uids == null) || noUIDsAssigned(uids, tmp.length))) {
            /*
             * Invalid UIDs
             */
            uids = getDestinationUIDs(tmp, destFullname);
        }
        if (move) {
            start = System.currentTimeMillis();
            new FlagsIMAPCommand(imapFolder, tmp, FLAGS_DELETED, true, true, false).doCommand();
            if (LOG.isDebugEnabled()) {
                sb.setLength(0);
                LOG.debug(sb.append(tmp.length).append(" messages marked as expunged (through system flag \\DELETED) in ").append(
                    (System.currentTimeMillis() - start)).append(STR_MSEC).toString());
            }
            try {
                IMAPCommandsCollection.uidExpungeWithFallback(imapFolder, tmp, imapConfig.getImapCapabilities().hasUIDPlus());
            } catch (final FolderClosedException e) {
                /*
                 * Not possible to retry since connection is broken
                 */
                throw new IMAPException(
                    IMAPException.Code.CONNECT_ERROR,
                    e,
                    imapAccess.getMailConfig().getServer(),
                    imapAccess.getMailConfig().getLogin());
            } catch (final StoreClosedException e) {
                /*
                 * Not possible to retry since connection is broken
                 */
                throw new IMAPException(
                    IMAPException.Code.CONNECT_ERROR,
                    e,
                    imapAccess.getMailConfig().getServer(),
                    imapAccess.getMailConfig().getLogin());
            } catch (final MessagingException e) {
                if (e.getNextException() instanceof ProtocolException) {
                    final ProtocolException protocolException = (ProtocolException) e.getNextException();
                    final Response response = protocolException.getResponse();
                    if (response != null && response.isBYE()) {
                        /*
                         * The BYE response is always untagged, and indicates that the server is about to close the connection.
                         */
                        throw new IMAPException(
                            IMAPException.Code.CONNECT_ERROR,
                            e,
                            imapAccess.getMailConfig().getServer(),
                            imapAccess.getMailConfig().getLogin());
                    }
                    final Throwable cause = protocolException.getCause();
                    if (cause instanceof StoreClosedException) {
                        /*
                         * Connection is down. No retry.
                         */
                        throw new IMAPException(
                            IMAPException.Code.CONNECT_ERROR,
                            e,
                            imapAccess.getMailConfig().getServer(),
                            imapAccess.getMailConfig().getLogin());
                    } else if (cause instanceof FolderClosedException) {
                        /*
                         * Connection is down. No retry.
                         */
                        throw new IMAPException(
                            IMAPException.Code.CONNECT_ERROR,
                            e,
                            imapAccess.getMailConfig().getServer(),
                            imapAccess.getMailConfig().getLogin());
                    }
                }
                throw new IMAPException(
                    IMAPException.Code.UID_EXPUNGE_FAILED,
                    e,
                    Arrays.toString(tmp),
                    imapFolder.getFullName(),
                    e.getMessage());
            }
        }
        return uids;
    }

    @Override
    public long[] appendMessages(final String destFullname, final MailMessage[] mailMessages) throws MailException {
        if (null == mailMessages || mailMessages.length == 0) {
            return new long[0];
        }
        try {
            /*
             * Open and check user rights on source folder
             */
            imapFolder = setAndOpenFolder(imapFolder, destFullname, Folder.READ_WRITE);
            try {
                if (!holdsMessages()) {
                    throw new IMAPException(IMAPException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, imapFolder.getFullName());
                }
                if (imapConfig.isSupportsACLs() && !aclExtension.canInsert(RightsCache.getCachedRights(imapFolder, true, session, accountId))) {
                    throw new IMAPException(IMAPException.Code.NO_INSERT_ACCESS, imapFolder.getFullName());
                }
            } catch (final MessagingException e) {
                throw new IMAPException(IMAPException.Code.NO_ACCESS, e, imapFolder.getFullName());
            }
            /*
             * Convert messages to JavaMail message objects
             */
            final Message[] msgs = MIMEMessageConverter.convertMailMessages(mailMessages);
            /*
             * Check if destination folder supports user flags
             */
            final boolean supportsUserFlags = UserFlagsCache.supportsUserFlags(imapFolder, true, session, accountId);
            if (!supportsUserFlags) {
                /*
                 * Remove all user flags from messages before appending to folder
                 */
                for (final Message message : msgs) {
                    removeUserFlagsFromMessage(message);
                }
            }
            /*
             * Mark first message for later lookup
             */
            final String hash = randomUUID();
            msgs[0].setHeader(MessageHeaders.HDR_X_OX_MARKER, hash);
            /*
             * ... and append them to folder
             */
            long[] retval = checkAndConvertAppendUID(imapFolder.appendUIDMessages(msgs));
            if (retval.length > 0) {
                /*
                 * Close affected IMAP folder to ensure consistency regarding IMAFolder's internal cache.
                 */
                notifyIMAPFolderModification(destFullname);
                return retval;
            }
            /*
             * Missing UID information in APPENDUID response
             */
            if (LOG.isWarnEnabled()) {
                LOG.warn("Missing UID information in APPENDUID response");
            }
            retval = new long[msgs.length];
            long uid = IMAPCommandsCollection.findMarker(hash, imapFolder);
            if (uid == -1) {
                Arrays.fill(retval, -1L);
            } else {
                for (int i = 0; i < retval.length; i++) {
                    retval[i] = uid++;
                }
            }
            /*
             * Close affected IMAP folder to ensure consistency regarding IMAFolder's internal cache.
             */
            notifyIMAPFolderModification(destFullname);
            return retval;
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e, imapConfig);
        }
    }

    @Override
    public void updateMessageFlags(final String fullname, final long[] msgUIDs, final int flagsArg, final boolean set) throws MailException {
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
            final Rights myRights = imapConfig.isSupportsACLs() ? RightsCache.getCachedRights(imapFolder, true, session, accountId) : null;
            final Flags affectedFlags = new Flags();
            boolean applyFlags = false;
            if (((flags & MailMessage.FLAG_ANSWERED) > 0)) {
                if (imapConfig.isSupportsACLs() && !aclExtension.canWrite(myRights)) {
                    throw new IMAPException(IMAPException.Code.NO_WRITE_ACCESS, imapFolder.getFullName());
                }
                affectedFlags.add(Flags.Flag.ANSWERED);
                applyFlags = true;
            }
            if (((flags & MailMessage.FLAG_DELETED) > 0)) {
                if (imapConfig.isSupportsACLs() && !aclExtension.canDeleteMessages(myRights)) {
                    throw new IMAPException(IMAPException.Code.NO_DELETE_ACCESS, imapFolder.getFullName());
                }
                affectedFlags.add(Flags.Flag.DELETED);
                applyFlags = true;
            }
            if (((flags & MailMessage.FLAG_DRAFT) > 0)) {
                if (imapConfig.isSupportsACLs() && !aclExtension.canWrite(myRights)) {
                    throw new IMAPException(IMAPException.Code.NO_WRITE_ACCESS, imapFolder.getFullName());
                }
                affectedFlags.add(Flags.Flag.DRAFT);
                applyFlags = true;
            }
            if (((flags & MailMessage.FLAG_FLAGGED) > 0)) {
                if (imapConfig.isSupportsACLs() && !aclExtension.canWrite(myRights)) {
                    throw new IMAPException(IMAPException.Code.NO_WRITE_ACCESS, imapFolder.getFullName());
                }
                affectedFlags.add(Flags.Flag.FLAGGED);
                applyFlags = true;
            }
            if (((flags & MailMessage.FLAG_SEEN) > 0)) {
                if (imapConfig.isSupportsACLs() && !aclExtension.canKeepSeen(myRights)) {
                    throw new IMAPException(IMAPException.Code.NO_KEEP_SEEN_ACCESS, imapFolder.getFullName());
                }
                affectedFlags.add(Flags.Flag.SEEN);
                applyFlags = true;
            }
            /*
             * Check for forwarded flag (supported through user flags)
             */
            if (((flags & MailMessage.FLAG_FORWARDED) == MailMessage.FLAG_FORWARDED) && UserFlagsCache.supportsUserFlags(
                imapFolder,
                true,
                session,
                accountId)) {
                if (imapConfig.isSupportsACLs() && !aclExtension.canWrite(myRights)) {
                    throw new IMAPException(IMAPException.Code.NO_WRITE_ACCESS, imapFolder.getFullName());
                }
                affectedFlags.add(MailMessage.USER_FORWARDED);
                applyFlags = true;
            }
            /*
             * Check for read acknowledgment flag (supported through user flags)
             */
            if (((flags & MailMessage.FLAG_READ_ACK) == MailMessage.FLAG_READ_ACK) && UserFlagsCache.supportsUserFlags(
                imapFolder,
                true,
                session,
                accountId)) {
                if (imapConfig.isSupportsACLs() && !aclExtension.canWrite(myRights)) {
                    throw new IMAPException(IMAPException.Code.NO_WRITE_ACCESS, imapFolder.getFullName());
                }
                affectedFlags.add(MailMessage.USER_READ_ACK);
                applyFlags = true;
            }
            if (applyFlags) {
                final long start = System.currentTimeMillis();
                new FlagsIMAPCommand(imapFolder, msgUIDs, affectedFlags, set, true, false).doCommand();
                if (LOG.isDebugEnabled()) {
                    LOG.debug(new StringBuilder(128).append("Flags applied to ").append(msgUIDs.length).append(" messages in ").append(
                        (System.currentTimeMillis() - start)).append(STR_MSEC).toString());
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
            throw MIMEMailException.handleMessagingException(e, imapConfig);
        }
    }

    @Override
    public void updateMessageColorLabel(final String fullname, final long[] msgUIDs, final int colorLabel) throws MailException {
        try {
            if (!MailProperties.getInstance().isUserFlagsEnabled()) {
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
                }
                if (imapConfig.isSupportsACLs() && !aclExtension.canWrite(RightsCache.getCachedRights(imapFolder, true, session, accountId))) {
                    throw new IMAPException(IMAPException.Code.NO_WRITE_ACCESS, imapFolder.getFullName());
                }
            } catch (final MessagingException e) {
                throw new IMAPException(IMAPException.Code.NO_ACCESS, e, imapFolder.getFullName());
            }
            if (!UserFlagsCache.supportsUserFlags(imapFolder, true, session, accountId)) {
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
                LOG.debug(new StringBuilder(128).append("All color flags cleared from ").append(msgUIDs.length).append(" messages in ").append(
                    (System.currentTimeMillis() - start)).append(STR_MSEC).toString());
            }
            start = System.currentTimeMillis();
            IMAPCommandsCollection.setColorLabel(imapFolder, msgUIDs, MailMessage.getColorLabelStringValue(colorLabel));
            mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
            if (LOG.isDebugEnabled()) {
                LOG.debug(new StringBuilder(128).append("All color flags set in ").append(msgUIDs.length).append(" messages in ").append(
                    (System.currentTimeMillis() - start)).append(STR_MSEC).toString());
            }
            /*
             * Force JavaMail's cache update through folder closure
             */
            imapFolder.close(false);
            resetIMAPFolder();
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e, imapConfig);
        }
    }

    @Override
    public MailMessage saveDraft(final String draftFullname, final ComposedMailMessage composedMail) throws MailException {
        try {
            final MimeMessage mimeMessage = new MimeMessage(imapAccess.getSession());
            /*
             * Fill message
             */
            final long uid;
            final MIMEMessageFiller filler = new MIMEMessageFiller(session, ctx);
            composedMail.setFiller(filler);
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
                filler.fillMailBody(composedMail, mimeMessage, ComposeType.NEW);
                mimeMessage.setFlag(Flags.Flag.DRAFT, true);
                mimeMessage.saveChanges();
                /*
                 * Append message to draft folder
                 */
                uid = appendMessages(draftFullname, new MailMessage[] { MIMEMessageConverter.convertMessage(mimeMessage) })[0];
            } finally {
                composedMail.cleanUp();
            }
            /*
             * Check for draft-edit operation: Delete old version
             */
            final MailPath msgref = composedMail.getMsgref();
            if (msgref != null) {
                deleteMessages(msgref.getFolder(), new long[] { msgref.getUid() }, true);
                composedMail.setMsgref(null);
            }
            /*
             * Force folder update
             */
            notifyIMAPFolderModification(draftFullname);
            /*
             * Return draft mail
             */
            return getMessage(draftFullname, uid, true);
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e, imapConfig);
        } catch (final IOException e) {
            throw new IMAPException(IMAPException.Code.IO_ERROR, e, e.getMessage());
        }
    }

    /*-
     * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
     * +++++++++++++++++ Helper methods +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
     * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
     */

    /**
     * Performs the FETCH command on currently active IMAP folder on all messages using the 1:* sequence range argument.
     * 
     * @param fullname The IMAP folder's fullname
     * @param order The order direction (needed to possibly flip the results)
     * @return The fetched mail messages with only ID and folder ID set.
     * @throws MessagingException If a messaging error occurs
     */
    private MailMessage[] performAllFetch(final String fullname, final OrderDirection order, final IndexRange indexRange) throws MessagingException {
        /*
         * Perform simple fetch
         */
        final long start = System.currentTimeMillis();
        MailMessage[] retval = IMAPCommandsCollection.fetchAll(imapFolder, OrderDirection.ASC.equals(order));
        mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
        if (LOG.isDebugEnabled()) {
            LOG.debug(new StringBuilder(128).append(fullname).append(": IMAP all fetch >>>FETCH 1:* (UID INTERNALDATE)<<< took ").append(
                (System.currentTimeMillis() - start)).append(STR_MSEC).toString());
        }
        if (retval == null || retval.length == 0) {
            return EMPTY_RETVAL;
        }
        if (indexRange != null) {
            final int fromIndex = indexRange.start;
            int toIndex = indexRange.end;
            if ((fromIndex) > retval.length) {
                /*
                 * Return empty iterator if start is out of range
                 */
                return EMPTY_RETVAL;
            }
            /*
             * Reset end index if out of range
             */
            if (toIndex >= retval.length) {
                toIndex = retval.length;
            }
            final MailMessage[] tmp = retval;
            final int retvalLength = toIndex - fromIndex;
            retval = new MailMessage[retvalLength];
            System.arraycopy(tmp, fromIndex, retval, 0, retvalLength);
        }
        return retval;
    }

    private static boolean onlyFolderAndID(final MailField[] fields) {
        if (fields.length > 2) {
            return false;
        }
        for (final MailField mailField : fields) {
            if (!MailField.ID.equals(mailField) && !MailField.FOLDER_ID.equals(mailField)) {
                return false;
            }
        }
        return true;
    }

    private static int createThreadSortMessages(final List<ThreadSortNode> threadList, final int level, final Message[] msgs, final int index) {
        int idx = index;
        final int threadListSize = threadList.size();
        final Iterator<ThreadSortNode> iter = threadList.iterator();
        for (int i = 0; i < threadListSize; i++) {
            final ThreadSortNode currentNode = iter.next();
            ((ExtendedMimeMessage) msgs[idx]).setThreadLevel(level);
            idx++;
            idx = createThreadSortMessages(currentNode.getChilds(), level + 1, msgs, idx);
        }
        return idx;
    }

    private static boolean noUIDsAssigned(final long[] arr, final int expectedLen) {
        final long[] tmp = new long[expectedLen];
        Arrays.fill(tmp, -1L);
        return Arrays.equals(arr, tmp);
    }

    /**
     * Determines the corresponding UIDs in destination folder
     * 
     * @param msgUIDs The UIDs in source folder
     * @param destFullname The destination folder's fullname
     * @return The corresponding UIDs in destination folder
     * @throws MessagingException
     * @throws IMAPException
     */
    private long[] getDestinationUIDs(final long[] msgUIDs, final String destFullname) throws MessagingException, IMAPException {
        /*
         * No COPYUID present in response code. Since UIDs are assigned in strictly ascending order in the mailbox (refer to IMAPv4 rfc3501,
         * section 2.3.1.1), we can discover corresponding UIDs by selecting the destination mailbox and detecting the location of messages
         * placed in the destination mailbox by using FETCH and/or SEARCH commands (e.g., for Message-ID or some unique marker placed in the
         * message in an APPEND).
         */
        final long[] retval = new long[msgUIDs.length];
        Arrays.fill(retval, -1L);
        if (!IMAPCommandsCollection.canBeOpened(imapFolder, destFullname, Folder.READ_ONLY)) {
            // No look-up possible
            return retval;
        }
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
            final IMAPMessage imapMessage = (IMAPMessage) (imapFolder.getMessageByUID(msgUIDs[minIndex]));
            if (imapMessage == null) {
                /*
                 * No message found whose UID matches msgUIDs[minIndex]
                 */
                messageId = null;
            } else {
                messageId = imapMessage.getMessageID();
            }
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

    private void handleSpamByUID(final long[] msgUIDs, final boolean isSpam, final boolean move, final String fullname, final int desiredMode) throws MessagingException, MailException {
        /*
         * Check for spam handling
         */
        if (usm.isSpamEnabled()) {
            final boolean locatedInSpamFolder = imapAccess.getFolderStorage().getSpamFolder().equals(imapFolder.getFullName());
            if (isSpam) {
                if (locatedInSpamFolder) {
                    /*
                     * A message that already has been detected as spam should again be learned as spam: Abort.
                     */
                    return;
                }
                /*
                 * Handle spam
                 */
                try {
                    IMAPProvider.getInstance().getSpamHandler().handleSpam(imapFolder.getFullName(), msgUIDs, move, session);
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
                 * A message that already has been detected as ham should again be learned as ham: Abort.
                 */
                return;
            }
            /*
             * Handle ham.
             */
            try {
                IMAPProvider.getInstance().getSpamHandler().handleHam(imapFolder.getFullName(), msgUIDs, move, session);
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

    /**
     * Checks and converts specified APPENDUID response.
     * 
     * @param appendUIDs The APPENDUID response
     * @return An array of long for each valid {@link AppendUID} element or a zero size array of long if an invalid {@link AppendUID}
     *         element was detected.
     */
    private static long[] checkAndConvertAppendUID(final AppendUID[] appendUIDs) {
        if (appendUIDs == null || appendUIDs.length == 0) {
            return new long[0];
        }
        final long[] retval = new long[appendUIDs.length];
        for (int i = 0; i < appendUIDs.length; i++) {
            if (appendUIDs[i] == null) {
                /*
                 * A null element means the server didn't return UID information for the appended message.
                 */
                return new long[0];
            }
            retval[i] = appendUIDs[i].uid;
        }
        return retval;
    }

    /**
     * Removes all user flags from given message's flags
     * 
     * @param message The message whose user flags shall be removed
     * @throws MessagingException If removing user flags fails
     */
    private static void removeUserFlagsFromMessage(final Message message) throws MessagingException {
        final String[] userFlags = message.getFlags().getUserFlags();
        if (userFlags.length > 0) {
            /*
             * Create a new flags container necessary for later removal
             */
            final Flags remove = new Flags();
            for (final String userFlag : userFlags) {
                remove.add(userFlag);
            }
            /*
             * Remove gathered user flags from message's flags; flags which do not occur in flags object are unaffected.
             */
            message.setFlags(remove, false);
        }
    }

    /**
     * Generates a UUID using {@link UUID#randomUUID()}; e.g.:<br>
     * <i>a5aa65cb-6c7e-4089-9ce2-b107d21b9d15</i>
     * 
     * @return A UUID string
     */
    private static String randomUUID() {
        return UUID.randomUUID().toString();
    }

}

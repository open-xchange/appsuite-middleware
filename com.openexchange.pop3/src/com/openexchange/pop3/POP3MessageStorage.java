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

package com.openexchange.pop3;

import static com.openexchange.mail.MailServletInterface.mailInterfaceMonitor;
import static com.openexchange.mail.mime.converters.MIMEMessageConverter.convertMessages;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.UIDFolder;
import com.openexchange.database.Database;
import com.openexchange.groupware.ldap.UserException;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailFields;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.mime.MIMEMailException;
import com.openexchange.mail.mime.utils.MIMEStorageUtility;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.pop3.services.POP3ServiceRegistry;
import com.openexchange.pop3.sort.MailMessageComparator;
import com.openexchange.pop3.util.POP3StorageUtil;
import com.openexchange.pop3.util.UIDUtil;
import com.openexchange.server.ServiceException;
import com.openexchange.server.impl.DBPoolingException;
import com.openexchange.session.Session;
import com.openexchange.user.UserService;
import com.sun.mail.pop3.POP3Folder;
import com.sun.mail.pop3.POP3Store;

/**
 * {@link POP3MessageStorage} - The POP3 implementation of message storage.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class POP3MessageStorage extends POP3FolderWorker {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(POP3MessageStorage.class);

    /**
     * Serial version UID
     */
    private static final long serialVersionUID = 1467121647337217270L;

    /*-
     * Flag constants
     */

    private static final Flags FLAGS_DELETED = new Flags(Flags.Flag.DELETED);

    /*-
     * Members
     */

    private Locale locale;

    /**
     * Initializes a new {@link POP3MessageStorage}.
     * 
     * @param pop3Store The POP3 store
     * @param pop3Access The POP3 access
     * @param session The session providing needed user data
     * @throws POP3Exception If context loading fails
     */
    public POP3MessageStorage(final POP3Store pop3Store, final POP3Access pop3Access, final Session session) throws POP3Exception {
        super(pop3Store, pop3Access, session);
    }

    /**
     * Gets session user's locale
     * 
     * @return The session user's locale
     * @throws POP3Exception If retrieving user's locale fails
     */
    private Locale getLocale() throws POP3Exception {
        if (null == locale) {
            try {
                final UserService userService = POP3ServiceRegistry.getServiceRegistry().getService(UserService.class, true);
                locale = userService.getUser(session.getUserId(), ctx).getLocale();
            } catch (final ServiceException e) {
                throw new POP3Exception(e);
            } catch (final UserException e) {
                throw new POP3Exception(e);
            }
        }
        return locale;
    }

    @Override
    public MailMessage[] getMessages(final String fullname, final String[] mailIds, final MailField[] fields) throws MailException {
        if ((mailIds == null) || (mailIds.length == 0)) {
            return EMPTY_RETVAL;
        }
        final boolean body;
        {
            final MailFields fieldSet = new MailFields(fields);
            if (fieldSet.contains(MailField.FULL)) {
                final MailMessage[] mails = new MailMessage[mailIds.length];
                for (int j = 0; j < mails.length; j++) {
                    mails[j] = getMessage(fullname, mailIds[j], true);
                }
                return mails;
            }
            body = (fieldSet.contains(MailField.BODY));
        }
        try {
            pop3Folder = setAndOpenFolder(pop3Folder, fullname, Folder.READ_ONLY);
            if (!holdsMessages()) {
                throw new POP3Exception(POP3Exception.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, fullname);
            }
            final POP3Folder pop3Fld = (POP3Folder) pop3Folder;
            // Get matching messages by UID
            final Message[] msgs;
            {
                final Message[] allmsgs = pop3Fld.getMessages();
                final long start = System.currentTimeMillis();
                pop3Fld.fetch(allmsgs, MIMEStorageUtility.getUIDFetchProfile());
                mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
                syncMessages(allmsgs, pop3Fld);
                msgs = new Message[mailIds.length];
                for (int i = 0; i < mailIds.length; i++) {
                    final String mailId = mailIds[i];
                    if (mailId == null) {
                        msgs[i] = null;
                    } else {
                        for (int j = 0; j < allmsgs.length && msgs[i] == null; j++) {
                            final Message m = allmsgs[j];
                            if (mailId.equals(pop3Fld.getUID(m))) {
                                msgs[i] = m;
                            }
                        }
                    }
                }
            }
            // Fetch messages
            final FetchProfile fetchProfile = MIMEStorageUtility.getFetchProfile(fields, true);
            final MailMessage[] mails = fetch(fetchProfile, msgs, body, false);
            return mails;
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e, pop3Config);
        }
    }

    @Override
    public MailMessage[] searchMessages(final String fullname, final IndexRange indexRange, final MailSortField sortField, final OrderDirection order, final SearchTerm<?> searchTerm, final MailField[] fields) throws MailException {
        try {
            pop3Folder = setAndOpenFolder(pop3Folder, fullname, Folder.READ_ONLY);
            if (!holdsMessages()) {
                throw new POP3Exception(POP3Exception.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, fullname);
            }
            final POP3Folder pop3Fld = (POP3Folder) pop3Folder;
            if (pop3Fld.getMessageCount() == 0) {
                return EMPTY_RETVAL;
            }
            final FetchProfile fetchProfile;
            {
                final Set<MailField> searchFields = new HashSet<MailField>();
                searchTerm.addMailField(searchFields);
                fetchProfile = MIMEStorageUtility.getFetchProfile(
                    fields,
                    searchFields.toArray(new MailField[searchFields.size()]),
                    MailField.toField(sortField.getListField()),
                    true);
            }
            // Prefetch all messages
            final MailFields fieldSet = new MailFields(fields);
            final boolean body = (fieldSet.contains(MailField.FULL) || fieldSet.contains(MailField.BODY));
            final MailMessage[] mails = fetch(fetchProfile, pop3Fld.getMessages(), body, true);
            // Filter and sort them
            MailMessage[] msgs = null;
            {
                // Filter them
                final List<MailMessage> filteredMails = new ArrayList<MailMessage>(mails.length);
                for (int i = 0; i < mails.length; i++) {
                    final MailMessage mail = mails[i];
                    if (searchTerm.matches(mail)) {
                        filteredMails.add(mail);
                    }
                }
                // Sort them
                {
                    Collections.sort(filteredMails, new MailMessageComparator(OrderDirection.DESC.equals(order), getLocale()));
                }
                msgs = filteredMails.toArray(new MailMessage[filteredMails.size()]);
            }
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
                final MailMessage[] tmp = msgs;
                final int retvalLength = toIndex - fromIndex;
                msgs = new MailMessage[retvalLength];
                System.arraycopy(tmp, fromIndex, msgs, 0, retvalLength);
            }
            return msgs;
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e, pop3Config);
        }
    }

    @Override
    public MailMessage[] getUnreadMessages(final String fullname, final MailSortField sortField, final OrderDirection order, final MailField[] fields, final int limit) throws MailException {
        try {
            pop3Folder = setAndOpenFolder(pop3Folder, fullname, Folder.READ_ONLY);
            if (!holdsMessages()) {
                throw new POP3Exception(POP3Exception.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, fullname);
            }
            final POP3Folder pop3Fld = (POP3Folder) pop3Folder;
            Message[] msgs = null;
            {
                /*
                 * Get UIDs of unread messages
                 */
                long start = System.currentTimeMillis();
                final Message[] allMsgs = pop3Fld.getMessages();
                mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
                final Set<String> unreadUIDs = getUnreadMessages(allMsgs.length);
                /*
                 * Prefetch their UIDLs
                 */
                start = System.currentTimeMillis();
                pop3Fld.fetch(allMsgs, MIMEStorageUtility.getUIDFetchProfile());
                mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
                syncMessages(allMsgs, pop3Fld);
                /*
                 * Check which occur in unread UIDLs
                 */
                final List<Message> tmp = new ArrayList<Message>(allMsgs.length);
                for (int i = 0; i < allMsgs.length; i++) {
                    final Message cur = allMsgs[i];
                    if (unreadUIDs.contains(pop3Fld.getUID(cur))) {
                        tmp.add(cur);
                    }
                }
                msgs = tmp.toArray(new Message[tmp.size()]);
            }
            final MailFields fieldSet = new MailFields(fields);
            final boolean body = (fieldSet.contains(MailField.FULL) || fieldSet.contains(MailField.BODY));
            // Fetch messages
            final FetchProfile fetchProfile = MIMEStorageUtility.getFetchProfile(fields, true);
            final MailMessage[] mails = fetch(fetchProfile, msgs, body, false);
            return mails;
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e, pop3Config);
        }
    }

    @Override
    public void deleteMessages(final String fullname, final String[] msgUIDs, final boolean hardDelete) throws MailException {
        try {
            pop3Folder = setAndOpenFolder(pop3Folder, fullname, Folder.READ_WRITE);
            if (!holdsMessages()) {
                throw new POP3Exception(POP3Exception.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, fullname);
            }
            final POP3Folder pop3Fld = (POP3Folder) pop3Folder;
            final Message[] allMsgs = pop3Fld.getMessages();
            /*
             * Prefetch their UIDLs
             */
            long start = System.currentTimeMillis();
            pop3Fld.fetch(allMsgs, MIMEStorageUtility.getUIDFetchProfile());
            mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
            syncMessages(allMsgs, pop3Fld);
            final Set<String> uidls = new HashSet<String>(Arrays.asList(msgUIDs));
            for (int i = 0; i < allMsgs.length; i++) {
                final Message cur = allMsgs[i];
                if (uidls.contains(pop3Fld.getUID(cur))) {
                    cur.setFlags(FLAGS_DELETED, true);
                }
            }
            start = System.currentTimeMillis();
            pop3Fld.close(true);
            mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
            resetPOP3Folder();
            POP3StorageUtil.deleteMessagesFromTables(uidls, session.getUserId(), session.getContextId());
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e, pop3Config);
        }
    }

    @Override
    public String[] copyMessages(final String sourceFolder, final String destFolder, final String[] mailIds, final boolean fast) throws MailException {
        throw new POP3Exception(POP3Exception.Code.COPY_MSGS_DENIED);
    }

    @Override
    public String[] moveMessages(final String sourceFolder, final String destFolder, final String[] mailIds, final boolean fast) throws MailException {
        throw new POP3Exception(POP3Exception.Code.MOVE_MSGS_DENIED);
    }

    @Override
    public String[] appendMessages(final String destFullname, final MailMessage[] mailMessages) throws MailException {
        throw new POP3Exception(POP3Exception.Code.APPEND_MSGS_DENIED);
    }

    @Override
    public void updateMessageFlags(final String fullname, final String[] mailIds, final int flags, final boolean set) throws MailException {
        try {
            pop3Folder = setAndOpenFolder(pop3Folder, fullname, Folder.READ_WRITE);
            if (!holdsMessages()) {
                throw new POP3Exception(POP3Exception.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, fullname);
            }
            final POP3Folder pop3Fld = (POP3Folder) pop3Folder;
            // Fetch messages
            final MailMessage[] msgs;
            {
                final long start = System.currentTimeMillis();
                final Message[] allMsgs = pop3Fld.getMessages();
                mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
                final FetchProfile fetchProfile = MIMEStorageUtility.getFlagsFetchProfile();
                fetchProfile.add(UIDFolder.FetchProfileItem.UID);
                final MailMessage[] mails = fetch(fetchProfile, allMsgs, false, true);
                msgs = new MailMessage[mailIds.length];
                for (int i = 0; i < mailIds.length; i++) {
                    final String mailId = mailIds[i];
                    if (mailId == null) {
                        msgs[i] = null;
                    } else {
                        for (int j = 0; j < mails.length && msgs[i] == null; j++) {
                            final MailMessage m = mails[j];
                            if (mailId.equals(m.getMailId())) {
                                msgs[i] = m;
                            }
                        }
                    }
                }
            }
            // Update system flags
            for (final MailMessage m : msgs) {
                int newFlags = m.getFlags();
                if (((flags & MailMessage.FLAG_ANSWERED) > 0)) {
                    newFlags = set ? (newFlags | MailMessage.FLAG_ANSWERED) : (newFlags & ~MailMessage.FLAG_ANSWERED);
                }
                if (((flags & MailMessage.FLAG_DELETED) > 0)) {
                    newFlags = set ? (newFlags | MailMessage.FLAG_DELETED) : (newFlags & ~MailMessage.FLAG_DELETED);
                }
                if (((flags & MailMessage.FLAG_DRAFT) > 0)) {
                    newFlags = set ? (newFlags | MailMessage.FLAG_DRAFT) : (newFlags & ~MailMessage.FLAG_DRAFT);
                }
                if (((flags & MailMessage.FLAG_FLAGGED) > 0)) {
                    newFlags = set ? (newFlags | MailMessage.FLAG_FLAGGED) : (newFlags & ~MailMessage.FLAG_FLAGGED);
                }
                if (((flags & MailMessage.FLAG_SEEN) > 0)) {
                    newFlags = set ? (newFlags | MailMessage.FLAG_SEEN) : (newFlags & ~MailMessage.FLAG_SEEN);
                }
                if (((flags & MailMessage.FLAG_USER) > 0)) {
                    newFlags = set ? (newFlags | MailMessage.FLAG_USER) : (newFlags & ~MailMessage.FLAG_USER);
                }
                if (((flags & MailMessage.FLAG_SPAM) > 0)) {
                    newFlags = set ? (newFlags | MailMessage.FLAG_SPAM) : (newFlags & ~MailMessage.FLAG_SPAM);
                }
                if (((flags & MailMessage.FLAG_FORWARDED) > 0)) {
                    newFlags = set ? (newFlags | MailMessage.FLAG_FORWARDED) : (newFlags & ~MailMessage.FLAG_FORWARDED);
                }
                if (((flags & MailMessage.FLAG_READ_ACK) > 0)) {
                    newFlags = set ? (newFlags | MailMessage.FLAG_READ_ACK) : (newFlags & ~MailMessage.FLAG_READ_ACK);
                }
                // Apply new flags
                updateSystemFlags(m.getMailId(), newFlags);
            }
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e, pop3Config);
        }
    }

    @Override
    public void updateMessageColorLabel(final String fullname, final String[] mailIds, final int colorLabel) throws MailException {
        try {
            pop3Folder = setAndOpenFolder(pop3Folder, fullname, Folder.READ_WRITE);
            if (!holdsMessages()) {
                throw new POP3Exception(POP3Exception.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, fullname);
            }
            final POP3Folder pop3Fld = (POP3Folder) pop3Folder;
            // Fetch messages
            final MailMessage[] msgs;
            {
                final long start = System.currentTimeMillis();
                final Message[] allMsgs = pop3Fld.getMessages();
                mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
                final FetchProfile fetchProfile = MIMEStorageUtility.getFlagsFetchProfile();
                fetchProfile.add(UIDFolder.FetchProfileItem.UID);
                final MailMessage[] mails = fetch(fetchProfile, allMsgs, false, true);
                msgs = new MailMessage[mailIds.length];
                for (int i = 0; i < mailIds.length; i++) {
                    final String mailId = mailIds[i];
                    if (mailId == null) {
                        msgs[i] = null;
                    } else {
                        for (int j = 0; j < mails.length && msgs[i] == null; j++) {
                            final MailMessage m = mails[j];
                            if (mailId.equals(m.getMailId())) {
                                msgs[i] = m;
                            }
                        }
                    }
                }
            }
            // Update color flags
            for (final MailMessage m : msgs) {
                updateColorFlag(m.getMailId(), colorLabel);
            }
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e, pop3Config);
        }
    }

    @Override
    public MailMessage saveDraft(final String draftFullname, final ComposedMailMessage composedMail) throws MailException {
        throw new POP3Exception(POP3Exception.Code.DRAFTS_NOT_SUPPORTED);
    }

    /*-
     * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
     * +++++++++++++++++ Helper methods +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
     * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
     */

    private static void prepareFetchProfile(final FetchProfile fetchProfile, final boolean sync) {
        // Check if ENVELOPE fetch item needs to be added
        if ((fetchProfile.getHeaderNames().length > 0 || fetchProfile.contains(FetchProfile.Item.CONTENT_INFO)) && !fetchProfile.contains(FetchProfile.Item.ENVELOPE)) {
            fetchProfile.add(FetchProfile.Item.ENVELOPE);
        }
        if (sync && !fetchProfile.contains(UIDFolder.FetchProfileItem.UID)) {
            fetchProfile.add(UIDFolder.FetchProfileItem.UID);
        }
    }

    private MailMessage[] fetch(final FetchProfile fetchProfile, final Message[] messages, final boolean body, final boolean sync) throws MailException {
        prepareFetchProfile(fetchProfile, sync);
        // Fetches UID, all HEADERS, and SIZE
        final POP3Folder pop3Fld = (POP3Folder) pop3Folder;
        try {
            final long start = System.currentTimeMillis();
            pop3Fld.fetch(messages, fetchProfile);
            mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e, pop3Config);
        }
        if (sync) {
            syncMessages(messages, pop3Fld);
        }
        final EnumSet<MailField> set = EnumSet.noneOf(MailField.class);
        /*
         * Folder is always set
         */
        set.add(MailField.FOLDER_ID);
        if (fetchProfile.contains(FetchProfile.Item.ENVELOPE)) {
            /*
             * From, To, Cc, Bcc, ReplyTo, Subject and Date
             */
            set.add(MailField.FROM);
            set.add(MailField.TO);
            set.add(MailField.CC);
            set.add(MailField.BCC);
            set.add(MailField.SUBJECT);
            set.add(MailField.RECEIVED_DATE);
            set.add(MailField.SENT_DATE);
            set.add(MailField.SIZE);
            /*
             * Furthermore the ENVELOPE in POP3 is performed through a TOP command; meaning all headers are present though
             */
            set.add(MailField.CONTENT_TYPE);
            set.add(MailField.HEADERS);
        }
        if (fetchProfile.contains(UIDFolder.FetchProfileItem.UID)) {
            set.add(MailField.ID);
        }
        // Convert with fields pre-fetched through previous POP3 fetch
        final MailMessage[] mails = convertMessages(messages, pop3Fld, set.toArray(new MailField[set.size()]), body);
        // Check for flags
        if (fetchProfile.contains(FetchProfile.Item.FLAGS)) {
            for (int i = 0; i < mails.length; i++) {
                final MailMessage mm = mails[i];
                prefillFlags(mails[i], UIDUtil.uid2long(mm.getMailId()), session.getContextId(), session.getUserId());
            }
        }
        return mails;
    }

    private static final String SELECT_FLAGS = "SELECT flags, color_flag FROM user_pop3_data WHERE cid = ? AND user = ? AND uid = ?";

    private static final String SELECT_USER_FLAGS = "SELECT user_flag FROM user_pop3_user_flag WHERE cid = ? AND user = ? AND uid = ?";

    private static void prefillFlags(final MailMessage message, final long uid, final int cid, final int user) throws POP3Exception {
        final Connection con;
        try {
            con = Database.get(cid, false);
        } catch (final DBPoolingException e) {
            throw new POP3Exception(e);
        }
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(SELECT_FLAGS);
            int pos = 1;
            stmt.setLong(pos++, cid);
            stmt.setLong(pos++, user);
            stmt.setLong(pos++, uid);
            rs = stmt.executeQuery();
            if (rs.next()) {
                message.setFlags(rs.getInt(1));
                message.setColorLabel(rs.getInt(2));
            }
            rs.close();
            stmt.close();
            stmt = con.prepareStatement(SELECT_USER_FLAGS);
            pos = 1;
            stmt.setLong(pos++, cid);
            stmt.setLong(pos++, user);
            stmt.setLong(pos++, uid);
            rs = stmt.executeQuery();
            while (rs.next()) {
                message.addUserFlag(rs.getString(1));
            }
        } catch (final SQLException e) {
            throw new POP3Exception(POP3Exception.Code.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
            Database.back(cid, false, con);
        }
    }

    private static final String SQL_SELECT_UNREAD = "SELECT uidl FROM user_pop3_data WHERE cid = ? AND user = ? AND (flags & ?) = ?";

    private Set<String> getUnreadMessages(final int initialSize) throws POP3Exception {
        final int cid = session.getContextId();
        final Connection con;
        try {
            con = Database.get(cid, false);
        } catch (final DBPoolingException e) {
            throw new POP3Exception(e);
        }
        final Set<String> uidls = new HashSet<String>(initialSize);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(SQL_SELECT_UNREAD);
            int pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, session.getUserId());
            stmt.setInt(pos++, MailMessage.FLAG_SEEN);
            stmt.setInt(pos++, 0);
            rs = stmt.executeQuery();
            while (rs.next()) {
                uidls.add(rs.getString(1));
            }
        } catch (final SQLException e) {
            throw new POP3Exception(POP3Exception.Code.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
            Database.back(cid, false, con);
        }
        return uidls;
    }

    private static final String SQL_UPDATE_FLAGS = "UPDATE user_pop3_data SET flags = ? WHERE cid = ? AND user = ? AND uidl = ?";

    private void updateSystemFlags(final String uidl, final int newFlags) throws POP3Exception {
        final int cid = session.getContextId();
        final Connection con;
        try {
            con = Database.get(cid, true);
        } catch (final DBPoolingException e) {
            throw new POP3Exception(e);
        }
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(SQL_UPDATE_FLAGS);
            stmt.setInt(1, newFlags);
            stmt.setInt(2, session.getContextId());
            stmt.setInt(3, session.getUserId());
            stmt.setString(4, uidl);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw new POP3Exception(POP3Exception.Code.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(null, stmt);
            Database.back(cid, true, con);
        }
    }

    private static final String SQL_UPDATE_COLOR_FLAGS = "UPDATE user_pop3_data SET color_flag = ? WHERE cid = ? AND user = ? AND uidl = ?";

    private void updateColorFlag(final String uidl, final int colorFlag) throws POP3Exception {
        final int cid = session.getContextId();
        final Connection con;
        try {
            con = Database.get(cid, true);
        } catch (final DBPoolingException e) {
            throw new POP3Exception(e);
        }
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(SQL_UPDATE_COLOR_FLAGS);
            stmt.setInt(1, colorFlag);
            stmt.setInt(2, session.getContextId());
            stmt.setInt(3, session.getUserId());
            stmt.setString(4, uidl);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw new POP3Exception(POP3Exception.Code.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(null, stmt);
            Database.back(cid, true, con);
        }
    }

    private void syncMessages(final Message[] allMsgs, final POP3Folder pop3Fld) throws MailException {
        try {
            final String[] uidls = new String[allMsgs.length];
            for (int i = 0; i < uidls.length; i++) {
                uidls[i] = pop3Fld.getUID(allMsgs[i]);
            }
            syncMessages(uidls);
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e, pop3Config);
        }
    }

    private void syncMessages(final String[] uidls) throws POP3Exception {
        POP3StorageUtil.syncDBEntries(uidls, session.getUserId(), session.getContextId());
    }
}

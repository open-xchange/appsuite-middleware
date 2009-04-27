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

import static com.openexchange.mail.dataobjects.MailFolder.DEFAULT_FOLDER_ID;
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
import javax.mail.Message;
import javax.mail.UIDFolder;
import javax.mail.internet.MimeMessage;
import com.openexchange.context.ContextService;
import com.openexchange.database.Database;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.ldap.UserException;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailFields;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.api.MailMessageStorage;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.mime.MIMEMailException;
import com.openexchange.mail.mime.converters.MIMEMessageConverter;
import com.openexchange.mail.mime.utils.MIMEStorageUtility;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.mail.utils.MailMessageComparator;
import com.openexchange.pop3.services.POP3ServiceRegistry;
import com.openexchange.pop3.util.POP3StorageUtil;
import com.openexchange.pop3.util.UIDUtil;
import com.openexchange.server.ServiceException;
import com.openexchange.server.impl.DBPoolingException;
import com.openexchange.session.Session;
import com.openexchange.user.UserService;

/**
 * {@link POP3MessageStorage} - The POP3 message storage implementation.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class POP3MessageStorage extends MailMessageStorage {

    /**
     * Serial version UID
     */
    private static final long serialVersionUID = 1467121647337217270L;

    /*-
     * Members
     */

    private final Session session;

    private final Context ctx;

    private final POP3Access pop3Access;

    private Locale locale;

    /**
     * Initializes a new {@link POP3MessageStorage}.
     * 
     * @param pop3Access The POP3 access
     * @param session The session providing needed user data
     * @throws POP3Exception If context loading fails
     */
    public POP3MessageStorage(final POP3Access pop3Access, final Session session) throws POP3Exception {
        super();
        this.pop3Access = pop3Access;
        this.session = session;
        try {
            final ContextService contextService = POP3ServiceRegistry.getServiceRegistry().getService(ContextService.class, true);
            ctx = contextService.getContext(session.getContextId());
        } catch (final ContextException e) {
            throw new POP3Exception(e);
        } catch (final ServiceException e) {
            throw new POP3Exception(e);
        }
    }

    @Override
    public void releaseResources() throws MailException {
        // Nothing to release
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
            body = (fieldSet.contains(MailField.FULL) || fieldSet.contains(MailField.BODY));
        }
        if (DEFAULT_FOLDER_ID.equals(fullname)) {
            throw new POP3Exception(POP3Exception.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, fullname);
        }
        final POP3InboxFolder pop3Fld = pop3Access.getInboxFolder();
        // Get matching messages by UID
        final Message[] msgs;
        {
            final Message[] allmsgs = pop3Fld.getMessages();
            final String[] uidls = pop3Fld.getUIDLs();
            msgs = new Message[mailIds.length];
            for (int i = 0; i < mailIds.length; i++) {
                final String mailId = mailIds[i];
                if (mailId == null) {
                    msgs[i] = null;
                } else {
                    for (int j = 0; j < uidls.length && msgs[i] == null; j++) {
                        if (mailId.equals(uidls[j])) {
                            msgs[i] = allmsgs[j];
                        }
                    }
                }
            }
        }
        // Fetch messages
        final FetchProfile fetchProfile = MIMEStorageUtility.getFetchProfile(fields, true);
        final MailMessage[] mails = fetch(fetchProfile, msgs, body);
        return mails;
    }

    @Override
    public MailMessage getMessage(final String fullname, final String mailId, final boolean markSeen) throws MailException {
        if (DEFAULT_FOLDER_ID.equals(fullname)) {
            throw new POP3Exception(POP3Exception.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, fullname);
        }
        final POP3InboxFolder pop3Fld = pop3Access.getInboxFolder();
        // Get matching message by UID
        final MimeMessage msg = (MimeMessage) pop3Fld.getMessage(mailId);
        if (null == msg) {
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
        // Set system flags
        mail.setFlags(POP3StorageUtil.getSystemFlags(mailId, session.getUserId(), session.getContextId()));
        // Set color flag
        mail.setColorLabel(POP3StorageUtil.getColorFlag(mailId, session.getUserId(), session.getContextId()));
        // Set user flags
        final String[] userFlags = POP3StorageUtil.getUserFlags(mailId, session.getUserId(), session.getContextId());
        if (userFlags != null) {
            /*
             * Mark message to contain user flags
             */
            mail.addUserFlags(new String[0]);
            for (final String userFlag : userFlags) {
                if (MailMessage.isColorLabel(userFlag)) {
                    mail.setColorLabel(MailMessage.getColorLabelIntValue(userFlag));
                } else if (MailMessage.USER_FORWARDED.equalsIgnoreCase(userFlag)) {
                    mail.setFlags(mail.getFlags() | MailMessage.FLAG_FORWARDED);
                } else if (MailMessage.USER_READ_ACK.equalsIgnoreCase(userFlag)) {
                    mail.setFlags(mail.getFlags() | MailMessage.FLAG_READ_ACK);
                } else {
                    mail.addUserFlag(userFlag);
                }
            }
        }
        // Mark message as seen
        if (!mail.isSeen() && markSeen) {
            mail.setPrevSeen(false);
            mail.setFlag(MailMessage.FLAG_SEEN, true);
            mail.setUnreadMessages(mail.getUnreadMessages() <= 0 ? 0 : mail.getUnreadMessages() - 1);
            // Apply seen flag
            updateSystemFlags(mailId, mail.getFlags());
        }
        return mail;
    }

    @Override
    public MailMessage[] searchMessages(final String fullname, final IndexRange indexRange, final MailSortField sortField, final OrderDirection order, final SearchTerm<?> searchTerm, final MailField[] fields) throws MailException {
        if (DEFAULT_FOLDER_ID.equals(fullname)) {
            throw new POP3Exception(POP3Exception.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, fullname);
        }
        final POP3InboxFolder pop3Fld = pop3Access.getInboxFolder();
        if (pop3Fld.getMessageCount() == 0) {
            return EMPTY_RETVAL;
        }
        final FetchProfile fetchProfile;
        if (null == searchTerm) {
            fetchProfile = MIMEStorageUtility.getFetchProfile(fields, MailField.toField(sortField.getListField()), true);
        } else {
            // Add fields addressed by search term as well
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
        final MailMessage[] mails = fetch(fetchProfile, pop3Fld.getMessages(), body);
        // Filter and sort them
        MailMessage[] msgs = null;
        if (null == searchTerm) {
            // Sort them
            final List<MailMessage> tmp = new ArrayList<MailMessage>(Arrays.asList(mails));
            Collections.sort(tmp, new MailMessageComparator(sortField, OrderDirection.DESC.equals(order), getLocale()));
            msgs = tmp.toArray(new MailMessage[tmp.size()]);
        } else {
            // Filter them
            final List<MailMessage> filteredMails = new ArrayList<MailMessage>(mails.length);
            for (int i = 0; i < mails.length; i++) {
                final MailMessage mail = mails[i];
                if (searchTerm.matches(mail)) {
                    filteredMails.add(mail);
                }
            }
            // Sort them
            Collections.sort(filteredMails, new MailMessageComparator(sortField, OrderDirection.DESC.equals(order), getLocale()));
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
    }

    @Override
    public MailMessage[] getUnreadMessages(final String fullname, final MailSortField sortField, final OrderDirection order, final MailField[] fields, final int limit) throws MailException {
        if (DEFAULT_FOLDER_ID.equals(fullname)) {
            throw new POP3Exception(POP3Exception.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, fullname);
        }
        final POP3InboxFolder pop3Fld = pop3Access.getInboxFolder();
        Message[] msgs = null;
        {
            /*
             * Get UIDs of unread messages
             */
            final Message[] allMsgs = pop3Fld.getMessages();
            final Set<String> unreadUIDs = getUnreadMessages(allMsgs.length);
            /*
             * Get UIDLs
             */
            final String[] uidls = pop3Fld.getUIDLs();
            /*
             * Check which occur in unread UIDLs
             */
            final List<Message> tmp = new ArrayList<Message>(uidls.length);
            for (int i = 0; i < uidls.length; i++) {
                if (unreadUIDs.contains(uidls[i])) {
                    tmp.add(allMsgs[i]);
                }
            }
            msgs = tmp.toArray(new Message[tmp.size()]);
        }
        final MailFields fieldSet = new MailFields(fields);
        final boolean body = (fieldSet.contains(MailField.FULL) || fieldSet.contains(MailField.BODY));
        // Fetch messages
        final FetchProfile fetchProfile = MIMEStorageUtility.getFetchProfile(fields, true);
        final MailMessage[] mails = fetch(fetchProfile, msgs, body);
        return mails;
    }

    @Override
    public void deleteMessages(final String fullname, final String[] msgUIDs, final boolean hardDelete) throws MailException {
        if (DEFAULT_FOLDER_ID.equals(fullname)) {
            throw new POP3Exception(POP3Exception.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, fullname);
        }
        final POP3InboxFolder pop3Fld = pop3Access.getInboxFolder();
        pop3Fld.deleteByUIDLs(Arrays.asList(msgUIDs));
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
        if (DEFAULT_FOLDER_ID.equals(fullname)) {
            throw new POP3Exception(POP3Exception.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, fullname);
        }
        final POP3InboxFolder pop3Fld = pop3Access.getInboxFolder();
        // Get matching messages by UID
        final MailMessage[] msgs;
        {
            final MailMessage[] mails = fetch(MIMEStorageUtility.getFlagsFetchProfile(), pop3Fld.getMessages(), false);
            final String[] uidls = pop3Fld.getUIDLs();
            msgs = new MailMessage[mailIds.length];
            for (int i = 0; i < mailIds.length; i++) {
                final String mailId = mailIds[i];
                if (mailId == null) {
                    msgs[i] = null;
                } else {
                    for (int j = 0; j < uidls.length && msgs[i] == null; j++) {
                        if (mailId.equals(uidls[j])) {
                            msgs[i] = mails[j];
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
    }

    @Override
    public void updateMessageColorLabel(final String fullname, final String[] mailIds, final int colorLabel) throws MailException {
        if (DEFAULT_FOLDER_ID.equals(fullname)) {
            throw new POP3Exception(POP3Exception.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, fullname);
        }
        final POP3InboxFolder pop3Fld = pop3Access.getInboxFolder();
        // Get matching messages by UID
        final MailMessage[] msgs;
        {
            final MailMessage[] mails = fetch(MIMEStorageUtility.getFlagsFetchProfile(), pop3Fld.getMessages(), false);
            final String[] uidls = pop3Fld.getUIDLs();
            msgs = new MailMessage[mailIds.length];
            for (int i = 0; i < mailIds.length; i++) {
                final String mailId = mailIds[i];
                if (mailId == null) {
                    msgs[i] = null;
                } else {
                    for (int j = 0; j < uidls.length && msgs[i] == null; j++) {
                        if (mailId.equals(uidls[j])) {
                            msgs[i] = mails[j];
                        }
                    }
                }
            }
        }
        // Update color flags
        for (final MailMessage m : msgs) {
            updateColorFlag(m.getMailId(), colorLabel);
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

    private static void prepareFetchProfile(final FetchProfile fetchProfile) {
        // Check if ENVELOPE fetch item needs to be added
        if ((fetchProfile.getHeaderNames().length > 0 || fetchProfile.contains(FetchProfile.Item.CONTENT_INFO)) && !fetchProfile.contains(FetchProfile.Item.ENVELOPE)) {
            fetchProfile.add(FetchProfile.Item.ENVELOPE);
        }
    }

    private MailMessage[] fetch(final FetchProfile fetchProfile, final Message[] messages, final boolean body) throws MailException {
        prepareFetchProfile(fetchProfile);
        // Fetches UID, all HEADERS, and SIZE
        final POP3InboxFolder pop3Fld = pop3Access.getInboxFolder();
        pop3Fld.fetch(messages, fetchProfile);
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
        // Add UID to fetch profile
        if (!fetchProfile.contains(UIDFolder.FetchProfileItem.UID)) {
            fetchProfile.add(UIDFolder.FetchProfileItem.UID);
        }
        set.add(MailField.ID);
        // Convert with fields pre-fetched through previous POP3 fetch
        final MailMessage[] mails = convertMessages(messages, pop3Fld.getPOP3InboxFolder(), set.toArray(new MailField[set.size()]), body);
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

}

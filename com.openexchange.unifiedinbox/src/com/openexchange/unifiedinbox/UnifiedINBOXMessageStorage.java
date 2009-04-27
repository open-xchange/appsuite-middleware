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

package com.openexchange.unifiedinbox;

import static com.openexchange.mail.dataobjects.MailFolder.DEFAULT_FOLDER_ID;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.openexchange.context.ContextService;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailFields;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailMessageStorage;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountException;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.server.ServiceException;
import com.openexchange.session.Session;
import com.openexchange.unifiedinbox.services.UnifiedINBOXServiceRegistry;
import com.openexchange.unifiedinbox.utility.UnifiedINBOXUtility;

/**
 * {@link UnifiedINBOXMessageStorage} - The Unified INBOX message storage implementation.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UnifiedINBOXMessageStorage extends MailMessageStorage {

    /**
     * Serial version UID
     */
    private static final long serialVersionUID = 1467121647337217270L;

    /*-
     * Members
     */

    private final Session session;

    private final int user;

    private final int cid;

    private final Context ctx;

    private final UnifiedINBOXAccess access;

    /**
     * Initializes a new {@link UnifiedINBOXMessageStorage}.
     * 
     * @param access The Unified INBOX access
     * @param session The session providing needed user data
     * @throws UnifiedINBOXException If context loading fails
     */
    public UnifiedINBOXMessageStorage(final UnifiedINBOXAccess access, final Session session) throws UnifiedINBOXException {
        super();
        this.access = access;
        this.session = session;
        cid = session.getContextId();
        try {
            final ContextService contextService = UnifiedINBOXServiceRegistry.getServiceRegistry().getService(ContextService.class, true);
            ctx = contextService.getContext(cid);
        } catch (final ContextException e) {
            throw new UnifiedINBOXException(e);
        } catch (final ServiceException e) {
            throw new UnifiedINBOXException(e);
        }
        user = session.getUserId();
    }

    @Override
    public void releaseResources() throws MailException {
        // Nothing to release
    }

    @Override
    public MailMessage[] getMessages(final String fullname, final String[] mailIds, final MailField[] fields) throws MailException {
        if ((mailIds == null) || (mailIds.length == 0)) {
            return EMPTY_RETVAL;
        }
        {
            final MailFields fieldSet = new MailFields(fields);
            if (fieldSet.contains(MailField.FULL)) {
                final MailMessage[] mails = new MailMessage[mailIds.length];
                for (int j = 0; j < mails.length; j++) {
                    mails[j] = getMessage(fullname, mailIds[j], true);
                }
                return mails;
            }
        }
        if (DEFAULT_FOLDER_ID.equals(fullname)) {
            throw new UnifiedINBOXException(UnifiedINBOXException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, fullname);
        }
        final MailMessage[] messages = new MailMessage[mailIds.length];
        // Parse mail IDs
        final Map<Integer, Map<String, List<String>>> parsed = UnifiedINBOXUtility.parseMailIDs(mailIds);
        final int size = parsed.size();
        final Iterator<Map.Entry<Integer, Map<String, List<String>>>> iter = parsed.entrySet().iterator();
        for (int i = 0; i < size; i++) {
            final Map.Entry<Integer, Map<String, List<String>>> accountMapEntry = iter.next();
            final int accountId = accountMapEntry.getKey().intValue();
            // Get account's mail access
            final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session, accountId);
            boolean close = false;
            try {
                mailAccess.connect();
                close = true;
                final Map<String, List<String>> folderUIDMap = accountMapEntry.getValue();
                final int innersize = folderUIDMap.size();
                final Iterator<Map.Entry<String, List<String>>> inneriter = folderUIDMap.entrySet().iterator();
                for (int j = 0; j < innersize; j++) {
                    final Map.Entry<String, List<String>> e = inneriter.next();
                    final String folder = e.getKey();
                    final List<String> uids = e.getValue();
                    final MailMessage[] mails = mailAccess.getMessageStorage().getMessages(
                        folder,
                        uids.toArray(new String[uids.size()]),
                        fields);
                    // Now insert mails at proper position
                    insertMessage(mailIds, messages, accountId, folder, mails);
                }
            } finally {
                if (close) {
                    mailAccess.close(true);
                }
            }
        }
        return messages;
    }

    @Override
    public MailMessage getMessage(final String fullname, final String mailId, final boolean markSeen) throws MailException {
        if (DEFAULT_FOLDER_ID.equals(fullname)) {
            throw new UnifiedINBOXException(UnifiedINBOXException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, fullname);
        }
        final UnifiedINBOXUID uid = new UnifiedINBOXUID(mailId);
        final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session, uid.getAccountId());
        boolean close = false;
        try {
            mailAccess.connect();
            close = true;
            return mailAccess.getMessageStorage().getMessage(uid.getFullname(), uid.getId(), markSeen);
        } finally {
            if (close) {
                mailAccess.close(true);
            }
        }
    }

    @Override
    public MailMessage[] searchMessages(final String fullname, final IndexRange indexRange, final MailSortField sortField, final OrderDirection order, final SearchTerm<?> searchTerm, final MailField[] fields) throws MailException {
        if (DEFAULT_FOLDER_ID.equals(fullname)) {
            throw new UnifiedINBOXException(UnifiedINBOXException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, fullname);
        }
        final MailAccount[] accounts;
        try {
            final MailAccountStorageService storageService = UnifiedINBOXServiceRegistry.getServiceRegistry().getService(
                MailAccountStorageService.class,
                true);
            accounts = storageService.getUserMailAccounts(user, cid);
        } catch (final ServiceException e) {
            throw new UnifiedINBOXException(e);
        } catch (final MailAccountException e) {
            throw new UnifiedINBOXException(e);
        }
        final List<MailMessage> messages = new ArrayList<MailMessage>();
        final StringBuilder helper = new StringBuilder(32);
        for (final MailAccount mailAccount : accounts) {
            // Ignore the mail account denoting this Unified INBOX account
            if (access.getAccountId() != mailAccount.getId()) {
                final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session, mailAccount.getId());
                boolean close = false;
                try {
                    mailAccess.connect();
                    close = true;
                    // Get account's messages
                    final MailMessage[] accountMails = mailAccess.getMessageStorage().searchMessages(
                        UnifiedINBOXAccess.INBOX,
                        indexRange,
                        sortField,
                        order,
                        searchTerm,
                        fields);
                    for (int i = 0; i < accountMails.length; i++) {
                        final MailMessage accountMail = accountMails[i];
                        helper.append(MailFolderUtility.prepareFullname(mailAccount.getId(), UnifiedINBOXAccess.INBOX));
                        helper.append(MailPath.SEPERATOR).append(accountMail.getMailId());
                        accountMail.setMailId(helper.toString());
                        helper.setLength(0);
                        messages.add(accountMail);
                    }
                } finally {
                    if (close) {
                        mailAccess.close(true);
                    }
                }
            }
        }
        return messages.toArray(new MailMessage[messages.size()]);
    }

    @Override
    public MailMessage[] getUnreadMessages(final String fullname, final MailSortField sortField, final OrderDirection order, final MailField[] fields, final int limit) throws MailException {
        if (DEFAULT_FOLDER_ID.equals(fullname)) {
            throw new UnifiedINBOXException(UnifiedINBOXException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, fullname);
        }
        final MailAccount[] accounts;
        try {
            final MailAccountStorageService storageService = UnifiedINBOXServiceRegistry.getServiceRegistry().getService(
                MailAccountStorageService.class,
                true);
            accounts = storageService.getUserMailAccounts(user, cid);
        } catch (final ServiceException e) {
            throw new UnifiedINBOXException(e);
        } catch (final MailAccountException e) {
            throw new UnifiedINBOXException(e);
        }
        final List<MailMessage> messages = new ArrayList<MailMessage>();
        final StringBuilder helper = new StringBuilder(32);
        for (final MailAccount mailAccount : accounts) {
            // Ignore the mail account denoting this Unified INBOX account
            if (access.getAccountId() != mailAccount.getId()) {
                final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session, mailAccount.getId());
                boolean close = false;
                try {
                    mailAccess.connect();
                    close = true;
                    // Get account's unread messages
                    final MailMessage[] accountMails = mailAccess.getMessageStorage().getUnreadMessages(
                        UnifiedINBOXAccess.INBOX,
                        sortField,
                        order,
                        fields,
                        limit);
                    for (int i = 0; i < accountMails.length; i++) {
                        final MailMessage accountMail = accountMails[i];
                        helper.append(MailFolderUtility.prepareFullname(mailAccount.getId(), UnifiedINBOXAccess.INBOX));
                        helper.append(MailPath.SEPERATOR).append(accountMail.getMailId());
                        accountMail.setMailId(helper.toString());
                        helper.setLength(0);
                        messages.add(accountMail);
                    }
                } finally {
                    if (close) {
                        mailAccess.close(true);
                    }
                }
            }
        }
        return messages.toArray(new MailMessage[messages.size()]);
    }

    @Override
    public void deleteMessages(final String fullname, final String[] mailIds, final boolean hardDelete) throws MailException {
        if (DEFAULT_FOLDER_ID.equals(fullname)) {
            throw new UnifiedINBOXException(UnifiedINBOXException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, fullname);
        }
        // Parse mail IDs
        final Map<Integer, Map<String, List<String>>> parsed = UnifiedINBOXUtility.parseMailIDs(mailIds);
        final int size = parsed.size();
        final Iterator<Map.Entry<Integer, Map<String, List<String>>>> iter = parsed.entrySet().iterator();
        for (int i = 0; i < size; i++) {
            final Map.Entry<Integer, Map<String, List<String>>> accountMapEntry = iter.next();
            final int accountId = accountMapEntry.getKey().intValue();
            // Get account's mail access
            final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session, accountId);
            boolean close = false;
            try {
                mailAccess.connect();
                close = true;
                final Map<String, List<String>> folderUIDMap = accountMapEntry.getValue();
                final int innersize = folderUIDMap.size();
                final Iterator<Map.Entry<String, List<String>>> inneriter = folderUIDMap.entrySet().iterator();
                for (int j = 0; j < innersize; j++) {
                    final Map.Entry<String, List<String>> e = inneriter.next();
                    final String folder = e.getKey();
                    final List<String> uids = e.getValue();
                    // Delete messages
                    mailAccess.getMessageStorage().deleteMessages(folder, uids.toArray(new String[uids.size()]), hardDelete);
                }
            } finally {
                if (close) {
                    mailAccess.close(true);
                }
            }
        }
    }

    @Override
    public String[] copyMessages(final String sourceFolder, final String destFolder, final String[] mailIds, final boolean fast) throws MailException {
        throw new UnifiedINBOXException(UnifiedINBOXException.Code.COPY_MSGS_DENIED);
    }

    @Override
    public String[] moveMessages(final String sourceFolder, final String destFolder, final String[] mailIds, final boolean fast) throws MailException {
        throw new UnifiedINBOXException(UnifiedINBOXException.Code.MOVE_MSGS_DENIED);
    }

    @Override
    public String[] appendMessages(final String destFullname, final MailMessage[] mailMessages) throws MailException {
        throw new UnifiedINBOXException(UnifiedINBOXException.Code.APPEND_MSGS_DENIED);
    }

    @Override
    public void updateMessageFlags(final String fullname, final String[] mailIds, final int flags, final boolean set) throws MailException {
        if (DEFAULT_FOLDER_ID.equals(fullname)) {
            throw new UnifiedINBOXException(UnifiedINBOXException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, fullname);
        }
        // Parse mail IDs
        final Map<Integer, Map<String, List<String>>> parsed = UnifiedINBOXUtility.parseMailIDs(mailIds);
        final int size = parsed.size();
        final Iterator<Map.Entry<Integer, Map<String, List<String>>>> iter = parsed.entrySet().iterator();
        for (int i = 0; i < size; i++) {
            final Map.Entry<Integer, Map<String, List<String>>> accountMapEntry = iter.next();
            final int accountId = accountMapEntry.getKey().intValue();
            // Get account's mail access
            final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session, accountId);
            boolean close = false;
            try {
                mailAccess.connect();
                close = true;
                final Map<String, List<String>> folderUIDMap = accountMapEntry.getValue();
                final int innersize = folderUIDMap.size();
                final Iterator<Map.Entry<String, List<String>>> inneriter = folderUIDMap.entrySet().iterator();
                for (int j = 0; j < innersize; j++) {
                    final Map.Entry<String, List<String>> e = inneriter.next();
                    final String folder = e.getKey();
                    final List<String> uids = e.getValue();
                    // Update flags
                    mailAccess.getMessageStorage().updateMessageFlags(folder, uids.toArray(new String[uids.size()]), flags, set);
                }
            } finally {
                if (close) {
                    mailAccess.close(true);
                }
            }
        }
    }

    @Override
    public void updateMessageColorLabel(final String fullname, final String[] mailIds, final int colorLabel) throws MailException {
        if (DEFAULT_FOLDER_ID.equals(fullname)) {
            throw new UnifiedINBOXException(UnifiedINBOXException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, fullname);
        }
        // Parse mail IDs
        final Map<Integer, Map<String, List<String>>> parsed = UnifiedINBOXUtility.parseMailIDs(mailIds);
        final int size = parsed.size();
        final Iterator<Map.Entry<Integer, Map<String, List<String>>>> iter = parsed.entrySet().iterator();
        for (int i = 0; i < size; i++) {
            final Map.Entry<Integer, Map<String, List<String>>> accountMapEntry = iter.next();
            final int accountId = accountMapEntry.getKey().intValue();
            // Get account's mail access
            final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session, accountId);
            boolean close = false;
            try {
                mailAccess.connect();
                close = true;
                final Map<String, List<String>> folderUIDMap = accountMapEntry.getValue();
                final int innersize = folderUIDMap.size();
                final Iterator<Map.Entry<String, List<String>>> inneriter = folderUIDMap.entrySet().iterator();
                for (int j = 0; j < innersize; j++) {
                    final Map.Entry<String, List<String>> e = inneriter.next();
                    final String folder = e.getKey();
                    final List<String> uids = e.getValue();
                    // Update flags
                    mailAccess.getMessageStorage().updateMessageColorLabel(folder, uids.toArray(new String[uids.size()]), colorLabel);
                }
            } finally {
                if (close) {
                    mailAccess.close(true);
                }
            }
        }
    }

    @Override
    public MailMessage saveDraft(final String draftFullname, final ComposedMailMessage composedMail) throws MailException {
        throw new UnifiedINBOXException(UnifiedINBOXException.Code.DRAFTS_NOT_SUPPORTED);
    }

    /*-
     * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
     * +++++++++++++++++ Helper methods +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
     * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
     */

    private static void insertMessage(final String[] mailIds, final MailMessage[] toFill, final int accountId, final String folder, final MailMessage[] mails) {
        for (int k = 0; k < mails.length; k++) {
            final String lookFor = new UnifiedINBOXUID(accountId, folder, mails[k].getMailId()).toString();
            int pos = -1;
            for (int l = 0; l < mailIds.length && pos == -1; l++) {
                final String mailId = mailIds[l];
                if (lookFor.equals(mailId)) {
                    pos = l;
                }
            }
            if (pos != -1) {
                toFill[pos] = mails[k];
            }
        }
    }
}

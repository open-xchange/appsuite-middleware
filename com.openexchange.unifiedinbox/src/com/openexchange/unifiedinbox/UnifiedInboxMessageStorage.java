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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.TIntObjectMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import javax.mail.MessagingException;
import com.openexchange.context.ContextService;
import com.openexchange.continuation.ContinuationExceptionCodes;
import com.openexchange.continuation.ContinuationRegistryService;
import com.openexchange.continuation.ContinuationResponse;
import com.openexchange.continuation.ExecutorContinuation;
import com.openexchange.continuation.ExecutorContinuation.ContinuationResponseGenerator;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailFields;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.IMailMessageStorageExt;
import com.openexchange.mail.api.ISimplifiedThreadStructure;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.api.MailMessageStorage;
import com.openexchange.mail.api.unified.UnifiedFullName;
import com.openexchange.mail.api.unified.UnifiedViewService;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.MimeMailExceptionCode;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.mail.threader.Conversation;
import com.openexchange.mail.threader.Conversations;
import com.openexchange.mail.utils.MailMessageComparator;
import com.openexchange.mail.utils.StorageUtility;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.UnifiedInboxManagement;
import com.openexchange.mailaccount.UnifiedInboxUID;
import com.openexchange.session.Session;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.Task;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.unifiedinbox.copy.UnifiedInboxMessageCopier;
import com.openexchange.unifiedinbox.dataobjects.UnifiedMailMessage;
import com.openexchange.unifiedinbox.services.Services;
import com.openexchange.unifiedinbox.utility.LoggingCallable;
import com.openexchange.unifiedinbox.utility.TrackingCompletionService;
import com.openexchange.unifiedinbox.utility.UnifiedInboxCompletionService;
import com.openexchange.unifiedinbox.utility.UnifiedInboxUtility;
import com.openexchange.user.UserService;

/**
 * {@link UnifiedInboxMessageStorage} - The Unified Mail message storage implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UnifiedInboxMessageStorage extends MailMessageStorage implements IMailMessageStorageExt, ISimplifiedThreadStructure, UnifiedViewService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(UnifiedInboxMessageStorage.class);

    /*-
     * Members
     */

    private final Session session;
    private final int user;
    private final int cid;
    private final Context ctx;
    private final UnifiedInboxAccess access;
    private Locale locale;
    private UnifiedInboxMessageCopier copier;

    /**
     * Initializes a new {@link UnifiedInboxMessageStorage}.
     *
     * @param access The Unified Mail access
     * @param session The session providing needed user data
     * @throws OXException If context loading fails
     */
    public UnifiedInboxMessageStorage(UnifiedInboxAccess access, Session session) throws OXException {
        super();
        this.access = access;
        this.session = session;
        cid = session.getContextId();
        {
            ContextService contextService = Services.getService(ContextService.class);
            ctx = contextService.getContext(cid);
        }
        user = session.getUserId();
    }

    /**
     * Initializes a new stateless {@link UnifiedInboxMessageStorage}.
     */
    public UnifiedInboxMessageStorage() {
        super();
        access = null;
        session = null;
        user = 0;
        cid = 0;
        ctx = null;
    }

    private static MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> getStoredMailAccessFor(int accountId, Session session, UnifiedInboxAccess access) throws OXException {
        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = access.getOpenedMailAccess(accountId);
        if (null == mailAccess) {
            mailAccess = MailAccess.getInstance(session, accountId);
            mailAccess.connect();
            access.storeOpenedMailAccessIfAbsent(accountId, mailAccess);
        }
        return mailAccess;
    }

    protected static void closeSafe(MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) {
        if (null == mailAccess) {
            return;
        }
        mailAccess.close(true);
    }

    /**
     * Gets session user's locale
     *
     * @return The session user's locale
     * @throws OXException If retrieving user's locale fails
     */
    protected Locale getLocale() throws OXException {
        if (null == locale) {
            UserService userService = Services.getService(UserService.class);
            locale = userService.getUser(session.getUserId(), ctx).getLocale();
        }
        return locale;
    }

    private UnifiedInboxMessageCopier getCopier() {
        if (null == copier) {
            copier = new UnifiedInboxMessageCopier(session, access);
        }
        return copier;
    }

    private List<MailAccount> getAccounts() throws OXException {
        return getAccounts(true, access.getAccountId(), user, cid);
    }

    private static List<MailAccount> getAccounts(boolean onlyEnabled, int unifiedMailAccountId, int userId, int contextId) throws OXException {
        MailAccount[] tmp = Services.getService(MailAccountStorageService.class).getUserMailAccounts(userId, contextId);
        List<MailAccount> accounts = new ArrayList<>(tmp.length);
        int thisAccountId = unifiedMailAccountId;

        for (MailAccount mailAccount : tmp) {
            if (thisAccountId != mailAccount.getId() && (!onlyEnabled || mailAccount.isUnifiedINBOXEnabled())) {
                accounts.add(mailAccount);
            }
        }

        return accounts;
    }

    private MailAccount getAccount(int accountId) throws OXException {
        MailAccountStorageService srv = Services.getService(MailAccountStorageService.class);
        return srv.getMailAccount(accountId, user, cid);
    }

    @Override
    public void releaseResources() {
        // Nothing to release
    }

    @Override
    public MailMessage[] getMessagesByMessageID(String... messageIDs) throws OXException {
        if (null == messageIDs || messageIDs.length <= 0) {
            return new MailMessage[0];
        }

        throw MailExceptionCode.UNSUPPORTED_OPERATION.create();
    }

    @Override
    public MailMessage[] getMessages(String fullName, String[] mailIds, MailField[] fields) throws OXException {
        return getMessages(fullName, mailIds, fields, null);
    }

    @Override
    public MailMessage[] getMessages(String fullName, String[] mailIds, MailField[] fields, String[] headerNames) throws OXException {
        return getMessages(fullName, mailIds, fields, headerNames, session, access);
    }

    @Override
    public MailMessage[] getMessages(UnifiedFullName fullName, String[] mailIds, MailField[] fields, Session session) throws OXException {
        int unifiedAccountId = Services.getService(UnifiedInboxManagement.class).getUnifiedINBOXAccountID(session);

        UnifiedInboxAccess access = new UnifiedInboxAccess(session, unifiedAccountId);
        access.connectInternal();

        return getMessages(fullName.getFullName(), mailIds, fields, null, session, access);
    }

    private static MailMessage[] getMessages(String fullName, String[] mailIds, final MailField[] fields, final String[] headerNames, Session session, UnifiedInboxAccess access) throws OXException {
        if ((mailIds == null) || (mailIds.length == 0)) {
            return EMPTY_RETVAL;
        }
        {
            MailFields fieldSet = new MailFields(fields);
            if (fieldSet.contains(MailField.FULL) || fieldSet.contains(MailField.BODY)) {
                MailMessage[] mails = new MailMessage[mailIds.length];
                for (int j = 0; j < mails.length; j++) {
                    mails[j] = getMessage(fullName, mailIds[j], true, session, access);
                }
                return mails;
            }
        }
        if (DEFAULT_FOLDER_ID.equals(fullName)) {
            throw UnifiedInboxException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES.create(fullName);
        }
        if (UnifiedInboxAccess.KNOWN_FOLDERS.contains(fullName)) {
            final MailMessage[] messages = new MailMessage[mailIds.length];
            // Parse mail IDs
            TIntObjectMap<Map<String, List<String>>> parsed = UnifiedInboxUtility.parseMailIDs(mailIds);
            // Create completion service for simultaneous access
            Executor executor = ThreadPools.getThreadPool().getExecutor();
            TrackingCompletionService<GetMessagesResult> completionService = new UnifiedInboxCompletionService<>(executor);
            // Iterate parsed map and submit a task for each iteration
            int numTasks = 0;
            TIntObjectIterator<Map<String, List<String>>> iter = parsed.iterator();
            for (int i = parsed.size(); i-- > 0;) {
                iter.advance();
                final int accountId = iter.key();
                final Map<String, List<String>> folderUIDMap = iter.value();
                numTasks++;
                completionService.submit(new LoggingCallable<GetMessagesResult>(session) {

                    @Override
                    public GetMessagesResult call() throws OXException {
                        // Get account's mail access
                        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
                        try {
                            mailAccess = MailAccess.getInstance(getSession(), accountId);
                            mailAccess.connect();
                            for (Iterator<Map.Entry<String, List<String>>> inneriter = folderUIDMap.entrySet().iterator(); inneriter.hasNext();) {
                                Map.Entry<String, List<String>> e = inneriter.next();
                                String folder = e.getKey();
                                List<String> uids = e.getValue();
                                try {
                                    MailMessage[] mails;
                                    if (null == headerNames || headerNames.length <= 0) {
                                        mails = mailAccess.getMessageStorage().getMessages(folder, uids.toArray(new String[uids.size()]), fields);
                                    } else {
                                        IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
                                        if (messageStorage instanceof IMailMessageStorageExt) {
                                            mails = ((IMailMessageStorageExt) messageStorage).getMessages(folder, uids.toArray(new String[uids.size()]), fields, headerNames);
                                        } else {
                                            MailField[] fields2 = MailFields.addIfAbsent(fields, MailField.ID);
                                            mails = messageStorage.getMessages(folder, uids.toArray(new String[uids.size()]), fields2);
                                            if (null == mails || mails.length <= 0) {
                                                return new GetMessagesResult(accountId, folder, mails);
                                            }

                                            int length = mails.length;
                                            MailMessage[] headers;
                                            {
                                                String[] ids = new String[length];
                                                for (int i = ids.length; i-- > 0;) {
                                                    MailMessage m = mails[i];
                                                    ids[i] = null == m ? null : m.getMailId();
                                                }
                                                headers = messageStorage.getMessages(folder, ids, MailFields.toArray(MailField.HEADERS));
                                            }

                                            for (int i = length; i-- > 0;) {
                                                MailMessage mailMessage = mails[i];
                                                if (null != mailMessage) {
                                                    MailMessage header = headers[i];
                                                    if (null != header) {
                                                        for (String headerName : headerNames) {
                                                            String[] values = header.getHeader(headerName);
                                                            if (null != values) {
                                                                for (String value : values) {
                                                                    mailMessage.addHeader(headerName, value);
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    for (MailMessage mail : messages) {
                                        if (null != mail) {
                                            mail.setAccountId(accountId);
                                        }
                                    }
                                    return new GetMessagesResult(accountId, folder, mails);
                                } catch (OXException me) {
                                    MailConfig config = mailAccess.getMailConfig();
                                    StringBuilder tmp = new StringBuilder(128);
                                    tmp.append("Couldn't get messages from folder \"");
                                    tmp.append((null == folder ? "<unknown>" : folder)).append("\" from server \"").append(
                                        config.getServer());
                                    tmp.append("\" for login \"").append(config.getLogin()).append("\".");
                                    getLogger().warn(tmp.toString(), me);
                                    return GetMessagesResult.EMPTY_RESULT;
                                } catch (RuntimeException rte) {
                                    MailConfig config = mailAccess.getMailConfig();
                                    StringBuilder tmp = new StringBuilder(128);
                                    tmp.append("Couldn't get messages from folder \"");
                                    tmp.append((null == folder ? "<unknown>" : folder)).append("\" from server \"").append(
                                        config.getServer());
                                    tmp.append("\" for login \"").append(config.getLogin()).append("\".");
                                    getLogger().warn(tmp.toString(), rte);
                                    return GetMessagesResult.EMPTY_RESULT;
                                }
                            }
                        } catch (OXException e) {
                            getLogger().debug("", e);
                            return GetMessagesResult.EMPTY_RESULT;
                        } finally {
                            closeSafe(mailAccess);
                        }
                        // Return dummy object
                        return GetMessagesResult.EMPTY_RESULT;
                    }
                });
                /*-
                 *
                Map.Entry<Integer, Map<String, List<String>>> accountMapEntry = iter.next();
                int accountId = accountMapEntry.getKey().intValue();
                // Get account's mail access
                MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = MailAccess.getInstance(session, accountId);
                boolean close = false;
                try {
                    mailAccess.connect();
                    close = true;
                    Map<String, List<String>> folderUIDMap = accountMapEntry.getValue();
                    int innersize = folderUIDMap.size();
                    Iterator<Map.Entry<String, List<String>>> inneriter = folderUIDMap.entrySet().iterator();
                    for (int j = 0; j < innersize; j++) {
                        Map.Entry<String, List<String>> e = inneriter.next();
                        String folder = e.getKey();
                        List<String> uids = e.getValue();
                        MailMessage[] mails = mailAccess.getMessageStorage().getMessages(
                            folder,
                            uids.toArray(new String[uids.size()]),
                            fields);
                        // Now insert mails at proper position
                        insertMessage(mailIds, messages, accountId, folder, mails);
                    }
                } finally {
                    if (close) {
                        closeSafe(mailAccess);
                    }
                }
                 */
            }
            // Wait for completion of each submitted task
            int undelegatedAccountId = access.getAccountId();
            try {
                for (int i = 0; i < numTasks; i++) {
                    GetMessagesResult result = completionService.take().get();
                    insertMessage(mailIds, messages, result.accountId, result.folder, result.mails, fullName, undelegatedAccountId);
                }
                LOG.debug("Retrieval of {} messages from folder \"{}\" took {}msec.", mailIds.length, fullName, completionService.getDuration());

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw MailExceptionCode.INTERRUPT_ERROR.create(e);
            } catch (ExecutionException e) {
                throw ThreadPools.launderThrowable(e, OXException.class);
            }
            // Return properly filled array
            return messages;
        }
        FullnameArgument fa = UnifiedInboxUtility.parseNestedFullName(fullName);
        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
        try {
            int accountId = fa.getAccountId();
            mailAccess = MailAccess.getInstance(session, accountId);
            mailAccess.connect();
            // Get messages
            MailMessage[] mails;
            if (null == headerNames || headerNames.length <= 0) {
                mails = mailAccess.getMessageStorage().getMessages(fa.getFullname(), mailIds, fields);
            } else {
                IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
                if (messageStorage instanceof IMailMessageStorageExt) {
                    mails = ((IMailMessageStorageExt) messageStorage).getMessages(fa.getFullname(), mailIds, fields, headerNames);
                } else {
                    MailField[] fields2 = MailFields.addIfAbsent(fields, MailField.ID);
                    mails = messageStorage.getMessages(fa.getFullname(), mailIds, fields2);
                    if (null == mails || mails.length <= 0) {
                        return mails;
                    }

                    int length = mails.length;
                    MailMessage[] headers;
                    {
                        String[] ids = new String[length];
                        for (int i = ids.length; i-- > 0;) {
                            MailMessage m = mails[i];
                            ids[i] = null == m ? null : m.getMailId();
                        }
                        headers = messageStorage.getMessages(fa.getFullname(), ids, MailFields.toArray(MailField.HEADERS));
                    }

                    for (int i = length; i-- > 0;) {
                        MailMessage mailMessage = mails[i];
                        if (null != mailMessage) {
                            MailMessage header = headers[i];
                            if (null != header) {
                                for (String headerName : headerNames) {
                                    String[] values = header.getHeader(headerName);
                                    if (null != values) {
                                        for (String value : values) {
                                            mailMessage.addHeader(headerName, value);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            int unifiedAccountId = access.getAccountId();
            for (MailMessage mail : mails) {
                if (null != mail) {
                    mail.setFolder(fullName);
                    mail.setAccountId(unifiedAccountId);
                }
            }
            return mails;
        } finally {
            closeSafe(mailAccess);
        }
    }

    @Override
    public MailPart getImageAttachment(String fullName, String mailId, String contentId) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(fullName)) {
            throw UnifiedInboxException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES.create(fullName);
        }
        if (UnifiedInboxAccess.KNOWN_FOLDERS.contains(fullName)) {
            UnifiedInboxUID uid = new UnifiedInboxUID(mailId);
            MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
            try {
                // Get stored or newly connect MailAccess instance
                mailAccess = getStoredMailAccessFor(uid.getAccountId(), session, access);

                // Get part
                MailPart part = mailAccess.getMessageStorage().getImageAttachment(uid.getFullName(), uid.getId(), contentId);
                if (null == part) {
                    return null;
                }
                return part;
            } finally {
                // Nothing
            }
        }
        FullnameArgument fa = UnifiedInboxUtility.parseNestedFullName(fullName);
        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
        try {
            // Get stored or newly connect MailAccess instance
            mailAccess = getStoredMailAccessFor(fa.getAccountId(), session, access);

            // Get part
            MailPart part = mailAccess.getMessageStorage().getImageAttachment(fa.getFullname(), mailId, contentId);
            if (null == part) {
                return null;
            }
            return part;
        } finally {
            // Nothing
        }
    }

    @Override
    public MailPart getAttachment(String fullName, String mailId, String sequenceId) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(fullName)) {
            throw UnifiedInboxException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES.create(fullName);
        }
        if (UnifiedInboxAccess.KNOWN_FOLDERS.contains(fullName)) {
            UnifiedInboxUID uid = new UnifiedInboxUID(mailId);
            MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
            try {
                // Get stored or newly connect MailAccess instance
                mailAccess = getStoredMailAccessFor(uid.getAccountId(), session, access);

                // Get part
                MailPart part = mailAccess.getMessageStorage().getAttachment(uid.getFullName(), uid.getId(), sequenceId);
                if (null == part) {
                    return null;
                }
                return part;
            } finally {
                // Nothing
            }
        }
        FullnameArgument fa = UnifiedInboxUtility.parseNestedFullName(fullName);
        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
        try {
            // Get stored or newly connect MailAccess instance
            mailAccess = getStoredMailAccessFor(fa.getAccountId(), session, access);

            // Get part
            MailPart part = mailAccess.getMessageStorage().getAttachment(fa.getFullname(), mailId, sequenceId);
            if (null == part) {
                return null;
            }
            return part;
        } finally {
            // Nothing
        }
    }

    @Override
    public MailMessage getMessage(String fullName, String mailId, boolean markSeen) throws OXException {
        return getMessage(fullName, mailId, markSeen, session, access);
    }

    @Override
    public MailMessage getMessage(UnifiedFullName fullName, String mailId, boolean markSeen, Session session) throws OXException {
        int unifiedAccountId = Services.getService(UnifiedInboxManagement.class).getUnifiedINBOXAccountID(session);

        UnifiedInboxAccess access = new UnifiedInboxAccess(session, unifiedAccountId);
        access.connectInternal();

        return getMessage(fullName.getFullName(), mailId, markSeen, session, access);
    }

    private static MailMessage getMessage(final String fullName, String mailId, boolean markSeen, Session session, final UnifiedInboxAccess access) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(fullName)) {
            throw UnifiedInboxException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES.create(fullName);
        }
        if (UnifiedInboxAccess.KNOWN_FOLDERS.contains(fullName)) {
            UnifiedInboxUID uid = new UnifiedInboxUID(mailId);
            MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
            try {
                // Get stored or newly connect MailAccess instance
                int accountId = uid.getAccountId();
                mailAccess = getStoredMailAccessFor(accountId, session, access);

                // Get the message
                MailMessage mail = mailAccess.getMessageStorage().getMessage(uid.getFullName(), uid.getId(), markSeen);
                if (null == mail) {
                    return null;
                }

                // Determine unread count
                boolean wasUnseen = markSeen && mail.containsPrevSeen() && !mail.isPrevSeen();
                Future<Integer> future = null;
                if (wasUnseen) {
                    future = ThreadPools.getThreadPool().submit(new AbstractTask<Integer>() {

                        @Override
                        public Integer call() throws OXException {
                            return Integer.valueOf(access.getFolderStorage().getUnreadCounter(fullName));
                        }
                    });
                }

                // Convert to Unified Mail message
                mail = new UnifiedMailMessage(mail, access.getAccountId());
                mail.setMailId(mailId);
                mail.setFolder(fullName);
                mail.setAccountId(accountId);
                if (null != future) {
                    try {
                        mail.setUnreadMessages(future.get().intValue());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw MailExceptionCode.INTERRUPT_ERROR.create(e, e.getMessage());
                    } catch (ExecutionException e) {
                        throw ThreadPools.launderThrowable(e, OXException.class);
                    }
                }
                return mail;
            } finally {
                // Nothing
            }
        }
        FullnameArgument fa = UnifiedInboxUtility.parseNestedFullName(fullName);
        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
        try {
            // Get stored or newly connect MailAccess instance
            mailAccess = getStoredMailAccessFor(fa.getAccountId(), session, access);

            // Get message
            MailMessage mail = mailAccess.getMessageStorage().getMessage(fa.getFullname(), mailId, markSeen);
            if (null == mail) {
                return null;
            }

            // Prepare it
            int unifiedAccountId = access.getAccountId();
            // mail.loadContent();
            mail.setFolder(fullName);
            mail.setAccountId(unifiedAccountId);
            return mail;
        } finally {
            // Nothing
        }
    }

    static final MailMessageComparator COMPARATOR = new MailMessageComparator(MailSortField.RECEIVED_DATE, true, null);

    @Override
    public List<List<MailMessage>> getThreadSortedMessages(final String fullName, final boolean includeSent, boolean cache, IndexRange indexRange, final long max, final MailSortField sortField, final OrderDirection order, final MailField[] mailFields, final String searchTerm) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(fullName)) {
            throw UnifiedInboxException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES.create(fullName);
        }
        if (UnifiedInboxAccess.KNOWN_FOLDERS.contains(fullName)) {
            List<MailAccount> accounts = getAccounts();
            final int undelegatedAccountId = access.getAccountId();
            boolean descending = OrderDirection.DESC.equals(order);
            MailSortField effectiveSortField = null == sortField ? MailSortField.RECEIVED_DATE :  sortField;
            MailFields mfs = new MailFields(mailFields);
            mfs.add(MailField.getField(effectiveSortField.getField()));
            final MailField[] checkedFields = mfs.toArray();
            // Create completion service for simultaneous access
            int length = accounts.size();
            Executor executor = ThreadPools.getThreadPool().getExecutor();
            TrackingCompletionService<List<List<MailMessage>>> completionService = new UnifiedInboxCompletionService<>(executor);
            final IndexRange applicableRange = null == indexRange ? null : new IndexRange(0, indexRange.end);
            for (final MailAccount mailAccount : accounts) {
                Session session = this.session;
                completionService.submit(new LoggingCallable<List<List<MailMessage>>>(session) {

                    @Override
                    public List<List<MailMessage>> call() {
                        int accountId = mailAccount.getId();
                        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
                        String fn = null;
                        try {
                            mailAccess = MailAccess.getInstance(getSession(), accountId);
                            mailAccess.connect();
                            // Get real full name
                            fn = UnifiedInboxUtility.determineAccountFullName(mailAccess, fullName);
                            // Check if denoted account has such a default folder
                            if (fn == null) {
                                return Collections.emptyList();
                            }
                            // Get account's messages
                            IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
                            if (messageStorage instanceof ISimplifiedThreadStructure) {
                                try {
                                    List<List<MailMessage>> list = ((ISimplifiedThreadStructure) messageStorage).getThreadSortedMessages(fn, includeSent, false, applicableRange, max, sortField, order, checkedFields, searchTerm);
                                    List<List<MailMessage>> ret = new ArrayList<>(list.size());
                                    UnifiedInboxUID helper = new UnifiedInboxUID();
                                    for (List<MailMessage> list2 : list) {
                                        List<MailMessage> messages = new ArrayList<>(list2.size());
                                        for (MailMessage accountMail : list2) {
                                            UnifiedMailMessage umm = new UnifiedMailMessage(accountMail, undelegatedAccountId);
                                            String accountMailFolder = accountMail.getFolder();
                                            umm.setMailId(helper.setUID(accountId, accountMailFolder, accountMail.getMailId()).toString());
                                            umm.setFolder(fn.equals(accountMailFolder) ? fullName : UnifiedInboxAccess.SENT);
                                            umm.setAccountId(accountId);
                                            messages.add(umm);
                                        }
                                        ret.add(messages);
                                    }
                                    return ret;
                                } catch (OXException e) {
                                    if (!MailExceptionCode.UNSUPPORTED_OPERATION.equals(e)) {
                                        throw e;
                                    }
                                    // Use fall-back mechanism
                                }
                            }
                            /*-
                             * 1. Send 'all' request with id, folder_id, level, and received_date - you need all that data.
                             *
                             * 2. Whenever level equals 0, a new thread starts (new array)
                             *
                             * 3. Add all objects (id, folder_id, received_date) to that list until level !== 0.
                             *
                             * 4. Order by received_date (ignore the internal level structure), so that the newest mails show up first.
                             *
                             * 5. Generate the real list of all threads. This must be again ordered by received_date, so that the most recent threads show up
                             *    first. id and folder_id refer to the most recent mail.
                             */
                            MailMessage[] msgArr;
                            try {
                                int allSort = MailSortField.RECEIVED_DATE.getField();
                                int allOrder = OrderDirection.DESC.getOrder();
                                msgArr = messageStorage.getThreadSortedMessages(fn, applicableRange, sortField, order, null, checkedFields);
                            } catch (OXException e) {
                                msgArr = messageStorage.getAllMessages(fn, applicableRange, sortField, order, checkedFields);
                            }
                            List<List<MailMessage>> list = new LinkedList<>();
                            List<MailMessage> current = new LinkedList<>();
                            // Here we go
                            int size = msgArr.length;
                            for (int i = 0; i < size; i++) {
                                MailMessage mail = msgArr[i];
                                if (null != mail) {
                                    int threadLevel = mail.getThreadLevel();
                                    if (0 == threadLevel) {
                                        list.add(current);
                                        current = new LinkedList<>();
                                    }
                                    current.add(mail);
                                }
                            }
                            list.add(current);
                            /*
                             * Sort empty ones
                             */
                            for (Iterator<List<MailMessage>> iterator = list.iterator(); iterator.hasNext();) {
                                List<MailMessage> mails = iterator.next();
                                if (null == mails || mails.isEmpty()) {
                                    iterator.remove();
                                } else {
                                    Collections.sort(mails, COMPARATOR);
                                }
                            }
                            /*
                             * Sort root elements
                             */
                            boolean descending = OrderDirection.DESC.equals(order);
                            MailSortField effectiveSortField = null == sortField ? MailSortField.RECEIVED_DATE :  sortField;
                            if (null == effectiveSortField) {
                                effectiveSortField = MailSortField.RECEIVED_DATE;
                            }
                            final MailMessageComparator comparator = new MailMessageComparator(effectiveSortField, descending, null);
                            Comparator<List<MailMessage>> listComparator = new Comparator<List<MailMessage>>() {

                                @Override
                                public int compare(List<MailMessage> o1, List<MailMessage> o2) {
                                    return comparator.compare(o1.get(0), o2.get(0));
                                }
                            };
                            Collections.sort(list, listComparator);
                            List<List<MailMessage>> ret = new ArrayList<>(list.size());
                            UnifiedInboxUID helper = new UnifiedInboxUID();
                            for (List<MailMessage> list2 : list) {
                                List<MailMessage> messages = new ArrayList<>(list2.size());
                                for (MailMessage accountMail : list2) {
                                    UnifiedMailMessage umm = new UnifiedMailMessage(accountMail, undelegatedAccountId);
                                    String accountMailFolder = accountMail.getFolder();
                                    umm.setMailId(helper.setUID(accountId, accountMailFolder, accountMail.getMailId()).toString());
                                    umm.setFolder(fn.equals(accountMailFolder) ? fullName : UnifiedInboxAccess.SENT);
                                    umm.setAccountId(accountId);
                                    messages.add(umm);
                                }
                                ret.add(messages);
                            }
                            return ret;
                        } catch (OXException e) {
                            StringBuilder tmp = new StringBuilder(128);
                            tmp.append("Couldn't get messages from folder \"");
                            tmp.append((null == fn ? "<unknown>" : fn)).append("\" from server \"").append(mailAccount.getMailServer());
                            tmp.append("\" for login \"").append(mailAccount.getLogin()).append("\".");
                            getLogger().warn(tmp.toString(), e);
                            return Collections.emptyList();
                        } catch (RuntimeException e) {
                            StringBuilder tmp = new StringBuilder(128);
                            tmp.append("Couldn't get messages from folder \"");
                            tmp.append((null == fn ? "<unknown>" : fn)).append("\" from server \"").append(mailAccount.getMailServer());
                            tmp.append("\" for login \"").append(mailAccount.getLogin()).append("\".");
                            getLogger().warn(tmp.toString(), e);
                            return Collections.emptyList();
                        } finally {
                            closeSafe(mailAccess);
                        }
                    }
                });
            }
            // Wait for completion of each submitted task
            try {
                List<List<MailMessage>> messages = new ArrayList<>(length << 2);
                for (int i = 0; i < length; i++) {
                    messages.addAll(completionService.take().get());
                }
                LOG.debug("getThreadSortedMessages from folder \"{}\" took {}msec.", fullName, completionService.getDuration());

                // Sort them
                final MailMessageComparator comparator = new MailMessageComparator(effectiveSortField, descending, null);
                Comparator<List<MailMessage>> listComparator = new Comparator<List<MailMessage>>() {

                    @Override
                    public int compare(List<MailMessage> o1, List<MailMessage> o2) {
                        return comparator.compare(o1.get(0), o2.get(0));
                    }
                };
                Collections.sort(messages, listComparator);
                // Return as array
                if (null == indexRange) {
                    return messages;
                }
                // Apply index range
                int fromIndex = indexRange.start;
                int toIndex = indexRange.end;
                if (fromIndex > messages.size()) {
                    /*
                     * Return empty iterator if start is out of range
                     */
                    return Collections.emptyList();
                }
                /*
                 * Reset end index if out of range
                 */
                if (toIndex >= messages.size()) {
                    if (fromIndex == 0) {
                        return messages;
                    }
                    toIndex = messages.size();
                }
                messages = messages.subList(fromIndex, toIndex);
                return messages;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw MailExceptionCode.INTERRUPT_ERROR.create(e);
            } catch (ExecutionException e) {
                throw ThreadPools.launderThrowable(e, OXException.class);
            }
        }
        /*
         * Certain account's folder
         */
        FullnameArgument fa = UnifiedInboxUtility.parseNestedFullName(fullName);
        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
        try {
            int accountId = fa.getAccountId();
            mailAccess = MailAccess.getInstance(session, accountId);
            mailAccess.connect();
            // Get account's messages
            final IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
            if ((messageStorage instanceof ISimplifiedThreadStructure)) {
                try {
                    return ((ISimplifiedThreadStructure) messageStorage).getThreadSortedMessages(fa.getFullname(), includeSent, false, indexRange, max, sortField, order, mailFields, searchTerm);
                } catch (OXException e) {
                    if (!MailExceptionCode.UNSUPPORTED_OPERATION.equals(e)) {
                        throw e;
                    }
                    // Use fall-back mechanism
                }
            }
            /*-
             * --------------------------------------------------------------------------------------------------------------------------
             *
             * Manually do thread-sort
             *
             * Sort by references
             */
            String realFullName = fa.getFullname();
            boolean mergeWithSent = includeSent && !mailAccess.getFolderStorage().getSentFolder().equals(realFullName);
            Future<List<MailMessage>> messagesFromSentFolder;
            if (mergeWithSent) {
                final String sentFolder = mailAccess.getFolderStorage().getSentFolder();
                messagesFromSentFolder = ThreadPools.getThreadPool().submit(new AbstractTask<List<MailMessage>>() {

                    @Override
                    public List<MailMessage> call() throws Exception {
                        return Conversations.messagesFor(sentFolder, (int) max, new MailFields(mailFields), messageStorage);
                    }
                });
            } else {
                messagesFromSentFolder = null;
            }
            // For actual folder
            List<Conversation> conversations = Conversations.conversationsFor(realFullName, (int) max, new MailFields(mailFields), messageStorage);
            // Retrieve from sent folder
            if (null != messagesFromSentFolder) {
                List<MailMessage> sentMessages = getFrom(messagesFromSentFolder);
                for (Conversation conversation : conversations) {
                    for (MailMessage sentMessage : sentMessages) {
                        if (conversation.referencesOrIsReferencedBy(sentMessage)) {
                            conversation.addMessage(sentMessage);
                        }
                    }
                }
            }
            // Fold it
            Conversations.fold(conversations);
            // Comparator
            MailMessageComparator threadComparator = COMPARATOR;
            // Sort
            List<List<MailMessage>> list = new ArrayList<>(conversations.size());
            for (Conversation conversation : conversations) {
                list.add(conversation.getMessages(threadComparator));
            }
            // Sort root elements
            {
                MailSortField effectiveSortField = null == sortField ? MailSortField.RECEIVED_DATE :  sortField;
                Comparator<List<MailMessage>> listComparator = getListComparator(effectiveSortField, order, getLocale());
                Collections.sort(list, listComparator);
            }
            // Check for index range
            list = sliceMessages(list, indexRange);
            /*
             * Apply account identifier
             */
            setAccountInfo2(list, getAccount(fa.getAccountId()));
            // Return list
            return list;
        } finally {
            closeSafe(mailAccess);
        }
    }

    private static List<List<MailMessage>> sliceMessages(List<List<MailMessage>> listOfConversations, IndexRange indexRange) {
        List<List<MailMessage>> list = listOfConversations;
        // Check for index range
        if (null != indexRange) {
            int fromIndex = indexRange.start;
            int toIndex = indexRange.end;
            int size = list.size();
            if ((fromIndex) > size) {
                // Return empty iterator if start is out of range
                return Collections.emptyList();
            }
            // Reset end index if out of range
            if (toIndex >= size) {
                if (fromIndex == 0) {
                    return list;
                }
                toIndex = size;
            }
            list = list.subList(fromIndex, toIndex);
        }
        // Return list
        return list;
    }

    private static <T> T getFrom(Future<T> f) throws OXException {
        if (null == f) {
            return null;
        }
        try {
            return f.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Keep interrupted state
            throw MailExceptionCode.INTERRUPT_ERROR.create(e, e.getMessage());
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof MessagingException) {
                throw MimeMailException.handleMessagingException((MessagingException) cause);
            }
            throw ThreadPools.launderThrowable(e, OXException.class);
        }

    }

    private Comparator<List<MailMessage>> getListComparator(final MailSortField sortField, final OrderDirection order, Locale locale) {
        final MailMessageComparator comparator = new MailMessageComparator(sortField, OrderDirection.DESC.equals(order), locale);
        Comparator<List<MailMessage>> listComparator = new Comparator<List<MailMessage>>() {

            @Override
            public int compare(List<MailMessage> o1, List<MailMessage> o2) {
                int result = comparator.compare(o1.get(0), o2.get(0));
                if ((0 != result) || (MailSortField.RECEIVED_DATE != sortField)) {
                    return result;
                }
                // Zero as comparison result AND primarily sorted by received-date
                MailMessage msg1 = o1.get(0);
                MailMessage msg2 = o2.get(0);
                String inReplyTo1 = msg1.getInReplyTo();
                String inReplyTo2 = msg2.getInReplyTo();
                if (null == inReplyTo1) {
                    result = null == inReplyTo2 ? 0 : -1;
                } else {
                    result = null == inReplyTo2 ? 1 : 0;
                }
                return 0 == result ? new MailMessageComparator(MailSortField.SENT_DATE, OrderDirection.DESC.equals(order), null).compare(msg1, msg2) : result;
            }
        };
        return listComparator;
    }

    /**
     * Sets account ID and name in given instances of {@link MailMessage}.
     *
     * @param mailMessages The {@link MailMessage} instances
     * @return The given instances of {@link MailMessage} each with account ID and name set
     * @throws OXException If mail account cannot be obtained
     */
    private <C extends Collection<MailMessage>, W extends Collection<C>> W setAccountInfo2(W col, MailAccount account) throws OXException {
        String name = account.getName();
        int id = account.getId();
        for (C mailMessages : col) {
            for (MailMessage mailMessage : mailMessages) {
                if (null != mailMessage) {
                    mailMessage.setAccountId(id);
                    mailMessage.setAccountName(name);
                }
            }
        }
        return col;
    }

    @Override
    public MailMessage[] getThreadSortedMessages(final String fullName, IndexRange indexRange, MailSortField sortField, OrderDirection order, final SearchTerm<?> searchTerm, MailField[] fields) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(fullName)) {
            throw UnifiedInboxException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES.create(fullName);
        }
        if (UnifiedInboxAccess.KNOWN_FOLDERS.contains(fullName)) {
            List<MailAccount> accounts = getAccounts();
            MailFields mfs = new MailFields(fields);
            mfs.add(MailField.getField(sortField.getField()));
            final MailField[] checkedFields = mfs.toArray();
            // Create completion service for simultaneous access
            int length = accounts.size();
            final int undelegatedAccountId = access.getAccountId();
            Executor executor = ThreadPools.getThreadPool().getExecutor();
            TrackingCompletionService<List<MailMessage>> completionService =
                new UnifiedInboxCompletionService<>(executor);
            for (final MailAccount mailAccount : accounts) {
                Session session  = this.session;
                completionService.submit(new LoggingCallable<List<MailMessage>>(session) {

                    @Override
                    public List<MailMessage> call() {
                        int accountId = mailAccount.getId();
                        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
                        String fn = null;
                        try {
                            mailAccess = MailAccess.getInstance(getSession(), accountId);
                            mailAccess.connect();
                            // Get real full name
                            fn = UnifiedInboxUtility.determineAccountFullName(mailAccess, fullName);
                            // Check if denoted account has such a default folder
                            if (fn == null) {
                                return Collections.emptyList();
                            }
                            // Get account's messages
                            MailMessage[] accountMails =  mailAccess.getMessageStorage().getThreadSortedMessages(fn, null, MailSortField.RECEIVED_DATE, OrderDirection.DESC, searchTerm, checkedFields);
                            List<MailMessage> messages = new ArrayList<>(accountMails.length);
                            UnifiedInboxUID helper = new UnifiedInboxUID();
                            for (MailMessage accountMail : accountMails) {
                                if (null != accountMail) {
                                    UnifiedMailMessage umm = new UnifiedMailMessage(accountMail, undelegatedAccountId);
                                    umm.setMailId(helper.setUID(accountId, fn, accountMail.getMailId()).toString());
                                    umm.setFolder(fullName);
                                    umm.setAccountId(accountId);
                                    messages.add(umm);
                                }
                            }
                            return messages;
                        } catch (OXException e) {
                            StringBuilder tmp = new StringBuilder(128);
                            tmp.append("Couldn't get messages from folder \"");
                            tmp.append((null == fn ? "<unknown>" : fn)).append("\" from server \"").append(mailAccount.getMailServer());
                            tmp.append("\" for login \"").append(mailAccount.getLogin()).append("\".");
                            getLogger().warn(tmp.toString(), e);
                            return Collections.emptyList();
                        } catch (RuntimeException e) {
                            StringBuilder tmp = new StringBuilder(128);
                            tmp.append("Couldn't get messages from folder \"");
                            tmp.append((null == fn ? "<unknown>" : fn)).append("\" from server \"").append(mailAccount.getMailServer());
                            tmp.append("\" for login \"").append(mailAccount.getLogin()).append("\".");
                            getLogger().warn(tmp.toString(), e);
                            return Collections.emptyList();
                        } finally {
                            closeSafe(mailAccess);
                        }
                    }
                });
            }
            // Wait for completion of each submitted task
            try {
                List<MailMessage> messages = new ArrayList<>(length << 2);
                for (int i = 0; i < length; i++) {
                    messages.addAll(completionService.take().get());
                }
                LOG.debug("Searching messages from folder \"{}\" took {}msec.", fullName, completionService.getDuration());

                // Sort them
                MailMessageComparator c = new MailMessageComparator(sortField, OrderDirection.DESC.equals(order), getLocale());
                Collections.sort(messages, c);
                // Return as array
                if (null == indexRange) {
                    return messages.toArray(new MailMessage[messages.size()]);
                }
                // Apply index range
                int fromIndex = indexRange.start;
                int toIndex = indexRange.end;
                if (fromIndex > messages.size()) {
                    /*
                     * Return empty iterator if start is out of range
                     */
                    return EMPTY_RETVAL;
                }
                /*
                 * Reset end index if out of range
                 */
                if (toIndex >= messages.size()) {
                    if (fromIndex == 0) {
                        return messages.toArray(new MailMessage[messages.size()]);
                    }
                    toIndex = messages.size();
                }
                messages = messages.subList(fromIndex, toIndex);
                return messages.toArray(new MailMessage[messages.size()]);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw MailExceptionCode.INTERRUPT_ERROR.create(e);
            } catch (ExecutionException e) {
                throw ThreadPools.launderThrowable(e, OXException.class);
            }
        }
        FullnameArgument fa = UnifiedInboxUtility.parseNestedFullName(fullName);
        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
        try {
            int accountId = fa.getAccountId();
            mailAccess = MailAccess.getInstance(session, accountId);
            mailAccess.connect();
            // Get account's messages
            MailMessage[] mails = mailAccess.getMessageStorage().getThreadSortedMessages(fa.getFullname(), indexRange, sortField, order, searchTerm, fields);
            int unifiedAccountId = this.access.getAccountId();
            for (MailMessage mail : mails) {
                if (null != mail) {
                    mail.setFolder(fullName);
                    mail.setAccountId(unifiedAccountId);
                }
            }
            return mails;
        } finally {
                closeSafe(mailAccess);
        }
    }

    @Override
    public void clearCache() throws OXException {
        int unifiedAccountId = Services.getService(UnifiedInboxManagement.class).getUnifiedINBOXAccountID(session);
        List<MailAccount> accounts = getAccounts(true, unifiedAccountId, session.getUserId(), session.getContextId());
        for (MailAccount mailAccount : accounts) {
            MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
            try {
                mailAccess = MailAccess.getInstance(session, mailAccount.getId());
                mailAccess.connect();

                IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
                if (messageStorage instanceof IMailMessageStorageExt) {
                    ((IMailMessageStorageExt) messageStorage).clearCache();
                }
            } finally {
                closeSafe(mailAccess);
            }
        }
    }

    @Override
    public int getUnreadCount(final String folder, final SearchTerm<?> searchTerm) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(folder)) {
            throw UnifiedInboxException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES.create(folder);
        }
        int unifiedMailAccountId = Services.getService(UnifiedInboxManagement.class).getUnifiedINBOXAccountID(session);

        if (UnifiedInboxAccess.KNOWN_FOLDERS.contains(folder)) {
            List<MailAccount> accounts = getAccounts(true, unifiedMailAccountId, session.getUserId(), session.getContextId());
            int length = accounts.size();
            Executor executor = ThreadPools.getThreadPool().getExecutor();

            TrackingCompletionService<Integer> completionService = new UnifiedInboxCompletionService<>(executor);
            for (final MailAccount mailAccount : accounts) {
                completionService.submit(new LoggingCallable<Integer>(session) {

                    @Override
                    public Integer call() {
                        int accountId = mailAccount.getId();
                        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
                        String fn = null;
                        try {
                            mailAccess = MailAccess.getInstance(getSession(), accountId);
                            mailAccess.connect();
                            // Get real full name
                            fn = UnifiedInboxUtility.determineAccountFullName(mailAccess, folder);
                            // Check if denoted account has such a default folder
                            if (fn == null) {
                                return 0;
                            }
                            IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
                            return messageStorage.getUnreadCount(folder, searchTerm);
                        } catch (OXException e) {
                            if (MailExceptionCode.ACCOUNT_DOES_NOT_EXIST.equals(e) || MimeMailExceptionCode.LOGIN_FAILED.equals(e)) {
                                getLogger().debug("Couldn't get unread count from folder \"{}\" from server \"{}\" for login \"{}\".", (null == fn ? "<unknown>" : fn), mailAccount.getMailServer(), mailAccount.getLogin(), e);
                            } else {
                                getLogger().warn("Couldn't get unread count from folder \"{}\" from server \"{}\" for login \"{}\".", (null == fn ? "<unknown>" : fn), mailAccount.getMailServer(), mailAccount.getLogin(), e);
                            }
                            return 0;
                        } catch (RuntimeException e) {
                            getLogger().warn("Couldn't get unread count from folder \"{}\" from server \"{}\" for login \"{}\".", (null == fn ? "<unknown>" : fn), mailAccount.getMailServer(), mailAccount.getLogin(), e);
                            return 0;
                        } finally {
                            closeSafe(mailAccess);
                        }
                    }
                });
            }
            // Wait for completion of each submitted task
            try {
                int result = 0;
                for (int i = 0; i < length; i++) {
                    result += completionService.take().get();
                }

                return result;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw MailExceptionCode.INTERRUPT_ERROR.create(e);
            } catch (ExecutionException e) {
                throw ThreadPools.launderThrowable(e, OXException.class);
            }
        }

        FullnameArgument fa = UnifiedInboxUtility.parseNestedFullName(folder);
        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
        try {
            int accountId = fa.getAccountId();
            mailAccess = MailAccess.getInstance(session, accountId);
            mailAccess.connect();
            IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
            return messageStorage.getUnreadCount(folder, searchTerm);

        } finally {
            closeSafe(mailAccess);
        }
    }

    @Override
    public MailMessage[] searchMessages(String fullName, IndexRange indexRange, MailSortField sortField, OrderDirection order, SearchTerm<?> searchTerm, MailField[] fields) throws OXException {
        return searchMessages(fullName, indexRange, sortField, order, searchTerm, fields, null);
    }

    @Override
    public MailMessage[] searchMessages(String fullName, IndexRange indexRange, MailSortField sortField, OrderDirection order, SearchTerm<?> searchTerm, MailField[] fields, String[] headerNames) throws OXException {
        return searchMessages(fullName, indexRange, sortField, order, searchTerm, fields, headerNames, session, true, access.getAccountId(), getLocale());
    }

    @Override
    public MailMessage[] searchMessages(UnifiedFullName fullName, IndexRange indexRange, MailSortField sortField, OrderDirection order, SearchTerm<?> searchTerm, MailField[] fields, Session session) throws OXException {
        int unifiedAccountId = Services.getService(UnifiedInboxManagement.class).getUnifiedINBOXAccountID(session);

        ContextService contextService = Services.getService(ContextService.class);
        UserService userService = Services.getService(UserService.class);
        Locale locale = userService.getUser(session.getUserId(), contextService.getContext(session.getContextId())).getLocale();

        return searchMessages(fullName.getFullName(), indexRange, sortField, order, searchTerm, fields, null, session, false, unifiedAccountId, locale);
    }

    @Override
    public MailMessage[] allMessages(UnifiedFullName fullName, MailField[] fields, Session session) throws OXException {
        int unifiedAccountId = Services.getService(UnifiedInboxManagement.class).getUnifiedINBOXAccountID(session);

        ContextService contextService = Services.getService(ContextService.class);
        UserService userService = Services.getService(UserService.class);
        Locale locale = userService.getUser(session.getUserId(), contextService.getContext(session.getContextId())).getLocale();

        return searchMessages(fullName.getFullName(), null, MailSortField.RECEIVED_DATE, OrderDirection.DESC, null, fields, null, session, false, unifiedAccountId, locale);
    }

    private static MailMessage[] searchMessages(final String fullName, final IndexRange indexRange, MailSortField sortField, final OrderDirection order, final SearchTerm<?> searchTerm, MailField[] fields, final String[] headerNames, Session session, boolean onlyEnabled, int unifiedMailAccountId, final Locale locale) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(fullName)) {
            throw UnifiedInboxException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES.create(fullName);
        }
        final MailSortField effectiveSortField = determineSortFieldForSearch(fullName, sortField);
        if (UnifiedInboxAccess.KNOWN_FOLDERS.contains(fullName)) {
            List<MailAccount> accounts = getAccounts(onlyEnabled, unifiedMailAccountId, session.getUserId(), session.getContextId());
            MailFields mfs = StorageUtility.prepareMailFieldsForSearch(fields, effectiveSortField);
            final MailField[] checkedFields = mfs.toArray();
            // Create completion service for simultaneous access
            int length = accounts.size();
            final int undelegatedAccountId = unifiedMailAccountId;
            Executor executor = ThreadPools.getThreadPool().getExecutor();
            // Check for continuation service
            ContinuationRegistryService continuationRegistry = Services.optService(ContinuationRegistryService.class);
            if (null != continuationRegistry && mfs.contains(MailField.SUPPORTS_CONTINUATION) && !mfs.contains(MailField.FULL) && !mfs.contains(MailField.BODY)) {
                ExecutorContinuation<MailMessage> executorContinuation;
                {
                    ContinuationResponseGenerator<MailMessage> responseGenerator = new ContinuationResponseGenerator<MailMessage>() {

                        @Override
                        public ContinuationResponse<Collection<MailMessage>> responseFor(List<MailMessage> messages, boolean completed) throws OXException {
                            // Sort them
                            MailMessageComparator c = new MailMessageComparator(effectiveSortField, OrderDirection.DESC.equals(order), locale);
                            Collections.sort(messages, c);
                            // Return as array
                            if (null == indexRange) {
                                return new ContinuationResponse<Collection<MailMessage>>(messages, null, "mail", completed);
                            }
                            // Apply index range
                            int fromIndex = indexRange.start;
                            int toIndex = indexRange.end;
                            if (fromIndex > messages.size()) {
                                /*
                                 * Return empty iterator if start is out of range
                                 */
                                return new ContinuationResponse<Collection<MailMessage>>(Collections.<MailMessage> emptyList(), null, "mail", completed);
                            }
                            /*
                             * Reset end index if out of range
                             */
                            if (toIndex >= messages.size()) {
                                toIndex = messages.size();
                            }
                            return new ContinuationResponse<Collection<MailMessage>>(messages.subList(fromIndex, toIndex), null, "mail", completed);
                        }
                    };
                    executorContinuation = ExecutorContinuation.newContinuation(executor, responseGenerator);
                }
                // Submit tasks
                final IndexRange applicableRange = null == indexRange ? null : new IndexRange(0, indexRange.end);
                for (final MailAccount mailAccount : accounts) {
                    executorContinuation.submit(new LoggingCallable<Collection<MailMessage>>(session) {

                        @Override
                        public List<MailMessage> call() {
                            int accountId = mailAccount.getId();
                            MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
                            String fn = null;
                            try {
                                mailAccess = MailAccess.getInstance(getSession(), accountId);
                                mailAccess.connect();
                                // Get real full name
                                fn = UnifiedInboxUtility.determineAccountFullName(mailAccess, fullName);
                                // Check if denoted account has such a default folder
                                if (fn == null) {
                                    return Collections.emptyList();
                                }
                                // Determine sort option
                                MailSortField sortField = MailSortField.RECEIVED_DATE;
                                OrderDirection orderDir = OrderDirection.DESC;
                                if (null != indexRange) {
                                    // Apply proper sort option
                                    sortField = effectiveSortField;
                                    orderDir = order;
                                }
                                // Get account's messages
                                MailMessage[] accountMails;
                                if (null == headerNames || headerNames.length <= 0) {
                                    accountMails = mailAccess.getMessageStorage().searchMessages(fn, null, sortField, orderDir, searchTerm, checkedFields);
                                } else {
                                    IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
                                    if (messageStorage instanceof IMailMessageStorageExt) {
                                        accountMails = ((IMailMessageStorageExt) messageStorage).searchMessages(fn, applicableRange, sortField, orderDir, searchTerm, checkedFields, headerNames);
                                    } else {
                                        MailField[] checkedFields2 = MailFields.addIfAbsent(checkedFields, MailField.ID);
                                        accountMails = messageStorage.searchMessages(fn, applicableRange, sortField, orderDir, searchTerm, checkedFields2);

                                        if (null == accountMails || accountMails.length <= 0) {
                                            return Collections.emptyList();
                                        }

                                        int length = accountMails.length;
                                        MailMessage[] headers;
                                        {
                                            String[] ids = new String[length];
                                            for (int i = ids.length; i-- > 0;) {
                                                MailMessage m = accountMails[i];
                                                ids[i] = null == m ? null : m.getMailId();
                                            }
                                            headers = messageStorage.getMessages(fn, ids, MailFields.toArray(MailField.HEADERS));
                                        }

                                        for (int i = length; i-- > 0;) {
                                            MailMessage mailMessage = accountMails[i];
                                            if (null != mailMessage) {
                                                MailMessage header = headers[i];
                                                if (null != header) {
                                                    for (String headerName : headerNames) {
                                                        String[] values = header.getHeader(headerName);
                                                        if (null != values) {
                                                            for (String value : values) {
                                                                mailMessage.addHeader(headerName, value);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                List<MailMessage> messages = new ArrayList<>(accountMails.length);
                                UnifiedInboxUID helper = new UnifiedInboxUID();
                                String name = mailAccount.getName();
                                for (MailMessage accountMail : accountMails) {
                                    if (null != accountMail) {
                                        UnifiedMailMessage umm = new UnifiedMailMessage(accountMail, undelegatedAccountId);
                                        umm.setMailId(helper.setUID(accountId, fn, accountMail.getMailId()).toString());
                                        umm.setFolder(fullName);
                                        umm.setAccountId(accountId);
                                        umm.setAccountName(name);
                                        messages.add(umm);
                                    }
                                }
                                return messages;
                            } catch (OXException e) {
                                getLogger().warn("Couldn't get messages from folder \"{}\" from server \"{}\" for login \"{}\".", (null == fn ? "<unknown>" : fn), mailAccount.getMailServer(), mailAccount.getLogin(), e);
                                return Collections.emptyList();
                            } catch (RuntimeException e) {
                                getLogger().warn("Couldn't get messages from folder \"{}\" from server \"{}\" for login \"{}\".", (null == fn ? "<unknown>" : fn), mailAccount.getMailServer(), mailAccount.getLogin(), e);
                                return Collections.emptyList();
                            } finally {
                                closeSafe(mailAccess);
                            }
                        }
                    });
                }
                // Add to registry
                continuationRegistry.putContinuation(executorContinuation, session);
                // Await first...
                try {
                    executorContinuation.awaitFirstResponse();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw MailExceptionCode.INTERRUPT_ERROR.create(e);
                }
                // Signal schedule to continuation
                throw ContinuationExceptionCodes.scheduledForContinuation(executorContinuation);
            }

            // The old way
            TrackingCompletionService<List<MailMessage>> completionService = new UnifiedInboxCompletionService<>(executor);
            for (final MailAccount mailAccount : accounts) {
                completionService.submit(new LoggingCallable<List<MailMessage>>(session) {

                    @Override
                    public List<MailMessage> call() {
                        int accountId = mailAccount.getId();
                        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
                        String fn = null;
                        try {
                            mailAccess = MailAccess.getInstance(getSession(), accountId);
                            mailAccess.connect();
                            // Get real full name
                            fn = UnifiedInboxUtility.determineAccountFullName(mailAccess, fullName);
                            // Check if denoted account has such a default folder
                            if (fn == null) {
                                return Collections.emptyList();
                            }
                            // Determine sort option
                            MailSortField sortField = MailSortField.RECEIVED_DATE;
                            OrderDirection orderDir = OrderDirection.DESC;
                            if (null != indexRange) {
                                // Apply proper sort option
                                sortField = effectiveSortField;
                                orderDir = order;
                            }
                            // Get account's messages
                            MailMessage[] accountMails;
                            if (null == headerNames || headerNames.length <= 0) {
                                accountMails = mailAccess.getMessageStorage().searchMessages(fn, indexRange, sortField, orderDir, searchTerm, checkedFields);
                            } else {
                                IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
                                if (messageStorage instanceof IMailMessageStorageExt) {
                                    accountMails = ((IMailMessageStorageExt) messageStorage).searchMessages(fn, indexRange, sortField, orderDir, searchTerm, checkedFields, headerNames);
                                } else {
                                    MailField[] checkedFields2 = MailFields.addIfAbsent(checkedFields, MailField.ID);
                                    accountMails = messageStorage.searchMessages(fn, indexRange, sortField, orderDir, searchTerm, checkedFields2);

                                    if (null == accountMails || accountMails.length <= 0) {
                                        return Collections.emptyList();
                                    }

                                    int length = accountMails.length;
                                    MailMessage[] headers;
                                    {
                                        String[] ids = new String[length];
                                        for (int i = ids.length; i-- > 0;) {
                                            MailMessage m = accountMails[i];
                                            ids[i] = null == m ? null : m.getMailId();
                                        }
                                        headers = messageStorage.getMessages(fn, ids, MailFields.toArray(MailField.HEADERS));
                                    }

                                    for (int i = length; i-- > 0;) {
                                        MailMessage mailMessage = accountMails[i];
                                        if (null != mailMessage) {
                                            MailMessage header = headers[i];
                                            if (null != header) {
                                                for (String headerName : headerNames) {
                                                    String[] values = header.getHeader(headerName);
                                                    if (null != values) {
                                                        for (String value : values) {
                                                            mailMessage.addHeader(headerName, value);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            List<MailMessage> messages = new ArrayList<>(accountMails.length);
                            UnifiedInboxUID helper = new UnifiedInboxUID();
                            String name = mailAccount.getName();
                            for (MailMessage accountMail : accountMails) {
                                if (null != accountMail) {
                                    UnifiedMailMessage umm = new UnifiedMailMessage(accountMail, undelegatedAccountId);
                                    umm.setMailId(helper.setUID(accountId, fn, accountMail.getMailId()).toString());
                                    umm.setFolder(fullName);
                                    umm.setAccountId(accountId);
                                    umm.setAccountName(name);
                                    messages.add(umm);
                                }
                            }
                            return messages;
                        } catch (OXException e) {
                            if (MailExceptionCode.ACCOUNT_DOES_NOT_EXIST.equals(e) || MimeMailExceptionCode.LOGIN_FAILED.equals(e)) {
                                getLogger().debug("Couldn't get messages from folder \"{}\" from server \"{}\" for login \"{}\".", (null == fn ? "<unknown>" : fn), mailAccount.getMailServer(), mailAccount.getLogin(), e);
                            } else {
                                getLogger().warn("Couldn't get messages from folder \"{}\" from server \"{}\" for login \"{}\".", (null == fn ? "<unknown>" : fn), mailAccount.getMailServer(), mailAccount.getLogin(), e);
                            }
                            return Collections.emptyList();
                        } catch (RuntimeException e) {
                            getLogger().warn("Couldn't get messages from folder \"{}\" from server \"{}\" for login \"{}\".", (null == fn ? "<unknown>" : fn), mailAccount.getMailServer(), mailAccount.getLogin(), e);
                            return Collections.emptyList();
                        } finally {
                            closeSafe(mailAccess);
                        }
                    }
                });
            }
            // Wait for completion of each submitted task
            try {
                List<MailMessage> messages = new ArrayList<>(length << 2);
                for (int i = 0; i < length; i++) {
                    messages.addAll(completionService.take().get());
                }
                LOG.debug("Searching messages from folder \"{}\" took {}msec.", fullName, completionService.getDuration());
                // Sort them
                MailMessageComparator c = new MailMessageComparator(effectiveSortField, OrderDirection.DESC.equals(order), locale);
                Collections.sort(messages, c);
                // Return as array
                if (null == indexRange) {
                    return messages.toArray(new MailMessage[messages.size()]);
                }
                // Apply index range
                int fromIndex = indexRange.start;
                int toIndex = indexRange.end;
                if (fromIndex > messages.size()) {
                    /*
                     * Return empty iterator if start is out of range
                     */
                    return EMPTY_RETVAL;
                }
                /*
                 * Reset end index if out of range
                 */
                if (toIndex >= messages.size()) {
                    if (fromIndex == 0) {
                        return messages.toArray(new MailMessage[messages.size()]);
                    }
                    toIndex = messages.size();
                }
                messages = messages.subList(fromIndex, toIndex);
                return messages.toArray(new MailMessage[messages.size()]);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw MailExceptionCode.INTERRUPT_ERROR.create(e);
            } catch (ExecutionException e) {
                throw ThreadPools.launderThrowable(e, OXException.class);
            }
        }
        FullnameArgument fa = UnifiedInboxUtility.parseNestedFullName(fullName);
        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
        try {
            int accountId = fa.getAccountId();
            mailAccess = MailAccess.getInstance(session, accountId);
            mailAccess.connect();
            // Get account's messages
            MailMessage[] mails;
            if (null == headerNames || headerNames.length <= 0) {
                mails = mailAccess.getMessageStorage().searchMessages(fa.getFullname(), indexRange, effectiveSortField, order, searchTerm, fields);
            } else {
                IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
                if (messageStorage instanceof IMailMessageStorageExt) {
                    mails = ((IMailMessageStorageExt) messageStorage).searchMessages(fa.getFullname(), indexRange, effectiveSortField, order, searchTerm, fields, headerNames);
                } else {
                    MailField[] checkedFields2 = MailFields.addIfAbsent(fields, MailField.ID);
                    mails = messageStorage.searchMessages(fa.getFullname(), indexRange, effectiveSortField, order, searchTerm, checkedFields2);

                    if (null == mails || mails.length <= 0) {
                        return mails;
                    }

                    int length = mails.length;
                    MailMessage[] headers;
                    {
                        String[] ids = new String[length];
                        for (int i = ids.length; i-- > 0;) {
                            MailMessage m = mails[i];
                            ids[i] = null == m ? null : m.getMailId();
                        }
                        headers = messageStorage.getMessages(fa.getFullname(), ids, MailFields.toArray(MailField.HEADERS));
                    }

                    for (int i = length; i-- > 0;) {
                        MailMessage mailMessage = mails[i];
                        if (null != mailMessage) {
                            MailMessage header = headers[i];
                            if (null != header) {
                                for (String headerName : headerNames) {
                                    String[] values = header.getHeader(headerName);
                                    if (null != values) {
                                        for (String value : values) {
                                            mailMessage.addHeader(headerName, value);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            for (MailMessage mail : mails) {
                if (null != mail) {
                    mail.setFolder(fullName);
                    mail.setAccountId(unifiedMailAccountId);
                }
            }
            return mails;
        } finally {
            closeSafe(mailAccess);
        }
    }

    private static MailSortField determineSortFieldForSearch(String fullName, MailSortField requestedSortField) {
        MailSortField effectiveSortField;
        if (null == requestedSortField) {
            effectiveSortField = MailSortField.RECEIVED_DATE;
        } else {
            if (MailSortField.SENT_DATE.equals(requestedSortField)) {
                String draftsFullname = UnifiedInboxAccess.DRAFTS;
                if (fullName.equals(draftsFullname)) {
                    effectiveSortField = MailSortField.RECEIVED_DATE;
                } else {
                    effectiveSortField = requestedSortField;
                }
            } else {
                effectiveSortField = requestedSortField;
            }
        }

        return effectiveSortField;
    }

    @Override
    public MailMessage[] getUnreadMessages(final String fullName, final MailSortField sortField, final OrderDirection order, final MailField[] fields, final int limit) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(fullName)) {
            throw UnifiedInboxException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES.create(fullName);
        }
        if (UnifiedInboxAccess.KNOWN_FOLDERS.contains(fullName)) {
            List<MailAccount> accounts = getAccounts();
            int length = accounts.size();
            final int undelegatedAccountId = access.getAccountId();
            Executor executor = ThreadPools.getThreadPool().getExecutor();
            TrackingCompletionService<List<MailMessage>> completionService =
                new UnifiedInboxCompletionService<>(executor);
            for (final MailAccount mailAccount : accounts) {
                Session session  = this.session;
                completionService.submit(new LoggingCallable<List<MailMessage>>(session) {

                    @Override
                    public List<MailMessage> call() throws Exception {
                        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
                        try {
                            int accountId = mailAccount.getId();
                            mailAccess = MailAccess.getInstance(getSession(), accountId);
                            mailAccess.connect();
                            // Get real full name
                            String fn = UnifiedInboxUtility.determineAccountFullName(mailAccess, fullName);
                            // Check if denoted account has such a default folder
                            if (fn == null) {
                                return Collections.emptyList();
                            }
                            // Get account's unread messages
                            MailMessage[] accountMails = mailAccess.getMessageStorage().getUnreadMessages(fn, sortField, order, fields, limit);
                            UnifiedInboxUID helper = new UnifiedInboxUID();
                            List<MailMessage> messages = new ArrayList<>(accountMails.length);
                            for (MailMessage accountMail : accountMails) {
                                if (null != accountMail) {
                                    UnifiedMailMessage umm = new UnifiedMailMessage(accountMail, undelegatedAccountId);
                                    umm.setMailId(helper.setUID(accountId, fn, accountMail.getMailId()).toString());
                                    umm.setFolder(fullName);
                                    umm.setAccountId(accountId);
                                    messages.add(umm);
                                }
                            }
                            return messages;
                        } catch (OXException e) {
                            getLogger().debug("", e);
                            return Collections.emptyList();
                        } finally {
                            closeSafe(mailAccess);
                        }
                    }
                });
            }
            // Wait for completion of each submitted task
            try {
                List<MailMessage> messages = new ArrayList<>(length << 2);
                for (int i = 0; i < length; i++) {
                    messages.addAll(completionService.take().get());
                }
                LOG.debug("Retrieving unread messages from folder \"{}\" took {}msec.", fullName, completionService.getDuration());

                // Sort them
                Collections.sort(messages, new MailMessageComparator(sortField, OrderDirection.DESC.equals(order), getLocale()));
                // Return as array
                return messages.toArray(new MailMessage[messages.size()]);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw MailExceptionCode.INTERRUPT_ERROR.create(e);
            } catch (ExecutionException e) {
                throw ThreadPools.launderThrowable(e, OXException.class);
            }
        }
        FullnameArgument fa = UnifiedInboxUtility.parseNestedFullName(fullName);
        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
        try {
            int accountId = fa.getAccountId();
            mailAccess = MailAccess.getInstance(session, accountId);
            mailAccess.connect();
            // Get account's messages
            MailMessage[] mails = mailAccess.getMessageStorage().getUnreadMessages(fa.getFullname(), sortField, order, fields, limit);
            int unifiedAccountId = this.access.getAccountId();
            for (MailMessage mail : mails) {
                if (null != mail) {
                    mail.setFolder(fullName);
                    mail.setAccountId(unifiedAccountId);
                }
            }
            return mails;
        } finally {
                closeSafe(mailAccess);
        }
    }

    @Override
    public void deleteMessages(String fullName, String[] mailIds, final boolean hardDelete) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(fullName)) {
            throw UnifiedInboxException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES.create(fullName);
        }
        if (UnifiedInboxAccess.KNOWN_FOLDERS.contains(fullName)) {
            // Parse mail IDs
            TIntObjectMap<Map<String, List<String>>> parsed = UnifiedInboxUtility.parseMailIDs(mailIds);
            int size = parsed.size();
            TIntObjectIterator<Map<String, List<String>>> iter = parsed.iterator();
            // Collection of Callables
            Collection<Task<Object>> collection = new ArrayList<>(size);
            for (int i = size; i-- > 0;) {
                iter.advance();
                final int accountId = iter.key();
                final Map<String, List<String>> folderUIDMap = iter.value();
                collection.add(new LoggingCallable<Object>(session) {

                    @Override
                    public Object call() throws Exception {
                        // Get account's mail access
                        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
                        try {
                            mailAccess = MailAccess.getInstance(getSession(), accountId);
                            mailAccess.connect();
                            int innersize = folderUIDMap.size();
                            Iterator<Map.Entry<String, List<String>>> inneriter = folderUIDMap.entrySet().iterator();
                            for (int j = 0; j < innersize; j++) {
                                Map.Entry<String, List<String>> e = inneriter.next();
                                String folder = e.getKey();
                                List<String> uids = e.getValue();
                                // Delete messages
                                mailAccess.getMessageStorage().deleteMessages(folder, uids.toArray(new String[uids.size()]), hardDelete);
                            }
                        } catch (OXException e) {
                            getLogger().debug("", e);
                            return null;
                        } finally {
                            closeSafe(mailAccess);
                        }
                        return null;
                    }
                });
            }
            ThreadPoolService executor = ThreadPools.getThreadPool();
            try {
                // Invoke all and wait for being executed
                executor.invokeAll(collection);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw MailExceptionCode.INTERRUPT_ERROR.create(e);
            }
        } else {
            FullnameArgument fa = UnifiedInboxUtility.parseNestedFullName(fullName);
            MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
            try {
                mailAccess = MailAccess.getInstance(session, fa.getAccountId());
                mailAccess.connect();
                mailAccess.getMessageStorage().deleteMessages(fa.getFullname(), mailIds, hardDelete);
            } finally {
                    closeSafe(mailAccess);
            }
        }
    }

    @Override
    public String[] copyMessages(String sourceFolder, String destFolder, String[] mailIds, boolean fast) throws OXException {
        return getCopier().doCopy(sourceFolder, destFolder, mailIds, fast, false);
    }

    @Override
    public String[] moveMessages(String sourceFolder, String destFolder, String[] mailIds, boolean fast) throws OXException {
        return getCopier().doCopy(sourceFolder, destFolder, mailIds, fast, true);
    }

    @Override
    public String[] appendMessages(String destFullname, MailMessage[] mailMessages) throws OXException {
        if (UnifiedInboxAccess.KNOWN_FOLDERS.contains(destFullname)) {
            // TODO: Error code OR default account?!
            throw UnifiedInboxException.Code.INVALID_DESTINATION_FOLDER.create(new Object[0]);
        }
        // Parse destination folder
        FullnameArgument destFullnameArgument = UnifiedInboxUtility.parseNestedFullName(destFullname);
        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
        try {
            mailAccess = MailAccess.getInstance(session, destFullnameArgument.getAccountId());
            mailAccess.connect();
            return mailAccess.getMessageStorage().appendMessages(destFullnameArgument.getFullname(), mailMessages);
        } finally {
            closeSafe(mailAccess);
        }
    }

    private static final String[] EMPTY_FLAGS = new String[0];

    @Override
    public void updateMessageFlags(String fullName, String[] mailIds, final int flags, final boolean set) throws OXException {
        updateMessageFlags(fullName, mailIds, flags, EMPTY_FLAGS, set);
    }

    @Override
    public void updateMessageFlags(String fullName, String[] mailIds, final int flags, final String[] userFlags, final boolean set) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(fullName)) {
            throw UnifiedInboxException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES.create(fullName);
        }
        if (UnifiedInboxAccess.KNOWN_FOLDERS.contains(fullName)) {
            // Parse mail IDs
            TIntObjectMap<Map<String, List<String>>> parsed = UnifiedInboxUtility.parseMailIDs(mailIds);
            int size = parsed.size();
            TIntObjectIterator<Map<String, List<String>>> iter = parsed.iterator();
            // Collection of Callables
            Collection<Task<Object>> collection = new ArrayList<>(size);
            for (int i = size; i-- > 0;) {
                iter.advance();
                final int accountId = iter.key();
                final Map<String, List<String>> folderUIDMap = iter.value();
                collection.add(new LoggingCallable<Object>(session) {

                    @Override
                    public Object call() throws Exception {
                        // Get account's mail access
                        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
                        try {
                            mailAccess = MailAccess.getInstance(getSession(), accountId);
                            mailAccess.connect();
                            int innersize = folderUIDMap.size();
                            Iterator<Map.Entry<String, List<String>>> inneriter = folderUIDMap.entrySet().iterator();
                            for (int j = 0; j < innersize; j++) {
                                Map.Entry<String, List<String>> e = inneriter.next();
                                String folder = e.getKey();
                                List<String> uids = e.getValue();
                                // Update flags
                                mailAccess.getMessageStorage().updateMessageFlags(folder, uids.toArray(new String[uids.size()]), flags, userFlags, set);
                            }
                        } catch (OXException e) {
                            getLogger().debug("", e);
                            return null;
                        } finally {
                            closeSafe(mailAccess);
                        }
                        return null;
                    }
                });
            }
            ThreadPoolService executor = ThreadPools.getThreadPool();
            try {
                // Invoke all and wait for being executed
                executor.invokeAll(collection);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw MailExceptionCode.INTERRUPT_ERROR.create(e);
            }
        } else {
            FullnameArgument fa = UnifiedInboxUtility.parseNestedFullName(fullName);
            MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
            try {
                mailAccess = MailAccess.getInstance(session, fa.getAccountId());
                mailAccess.connect();
                mailAccess.getMessageStorage().updateMessageFlags(fa.getFullname(), mailIds, flags, set);
            } finally {
                closeSafe(mailAccess);
            }
        }
    }

    @Override
    public void updateMessageUserFlags(String fullName, String[] mailIds, final String[] flags, final boolean set) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(fullName)) {
            throw UnifiedInboxException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES.create(fullName);
        }
        if (UnifiedInboxAccess.KNOWN_FOLDERS.contains(fullName)) {
            // Parse mail IDs
            TIntObjectMap<Map<String, List<String>>> parsed = UnifiedInboxUtility.parseMailIDs(mailIds);
            int size = parsed.size();
            TIntObjectIterator<Map<String, List<String>>> iter = parsed.iterator();
            // Collection of Callables
            Collection<Task<Object>> collection = new ArrayList<>(size);
            for (int i = size; i-- > 0;) {
                iter.advance();
                final int accountId = iter.key();
                final Map<String, List<String>> folderUIDMap = iter.value();
                collection.add(new LoggingCallable<Object>(session) {

                    @Override
                    public Object call() throws Exception {
                        // Get account's mail access
                        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
                        try {
                            mailAccess = MailAccess.getInstance(getSession(), accountId);
                            mailAccess.connect();
                            int innersize = folderUIDMap.size();
                            Iterator<Map.Entry<String, List<String>>> inneriter = folderUIDMap.entrySet().iterator();
                            for (int j = 0; j < innersize; j++) {
                                Map.Entry<String, List<String>> e = inneriter.next();
                                String folder = e.getKey();
                                List<String> uids = e.getValue();
                                // Update flags
                                mailAccess.getMessageStorage().updateMessageUserFlags(folder, uids.toArray(new String[uids.size()]), flags, set);
                            }
                        } catch (OXException e) {
                            getLogger().debug("", e);
                            return null;
                        } finally {
                            closeSafe(mailAccess);
                        }
                        return null;
                    }
                });
            }
            ThreadPoolService executor = ThreadPools.getThreadPool();
            try {
                // Invoke all and wait for being executed
                executor.invokeAll(collection);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw MailExceptionCode.INTERRUPT_ERROR.create(e);
            }
        } else {
            FullnameArgument fa = UnifiedInboxUtility.parseNestedFullName(fullName);
            MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
            try {
                mailAccess = MailAccess.getInstance(session, fa.getAccountId());
                mailAccess.connect();
                mailAccess.getMessageStorage().updateMessageUserFlags(fa.getFullname(), mailIds, flags, set);
            } finally {
                closeSafe(mailAccess);
            }
        }
    }

    @Override
    public void updateMessageColorLabel(String fullName, String[] mailIds, final int colorLabel) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(fullName)) {
            throw UnifiedInboxException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES.create(fullName);
        }
        if (UnifiedInboxAccess.KNOWN_FOLDERS.contains(fullName)) {
            // Parse mail IDs
            TIntObjectMap<Map<String, List<String>>> parsed = UnifiedInboxUtility.parseMailIDs(mailIds);
            int size = parsed.size();
            TIntObjectIterator<Map<String, List<String>>> iter = parsed.iterator();
            // Collection of Callables
            Collection<Task<Object>> collection = new ArrayList<>(size);
            for (int i = size; i-- > 0;) {
                iter.advance();
                final int accountId = iter.key();
                final Map<String, List<String>> folderUIDMap = iter.value();
                collection.add(new LoggingCallable<Object>(session) {

                    @Override
                    public Object call() throws Exception {
                        // Get account's mail access
                        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
                        try {
                            mailAccess = MailAccess.getInstance(getSession(), accountId);
                            mailAccess.connect();
                            int innersize = folderUIDMap.size();
                            Iterator<Map.Entry<String, List<String>>> inneriter = folderUIDMap.entrySet().iterator();
                            for (int j = 0; j < innersize; j++) {
                                Map.Entry<String, List<String>> e = inneriter.next();
                                String folder = e.getKey();
                                List<String> uids = e.getValue();
                                // Update flags
                                mailAccess.getMessageStorage().updateMessageColorLabel(folder, uids.toArray(new String[uids.size()]), colorLabel);
                            }
                        } catch (OXException e) {
                            getLogger().debug("", e);
                            return null;
                        } finally {
                            closeSafe(mailAccess);
                        }
                        return null;
                    }
                });
            }
            ThreadPoolService executor = ThreadPools.getThreadPool();
            try {
                // Invoke all and wait for being executed
                executor.invokeAll(collection);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw MailExceptionCode.INTERRUPT_ERROR.create(e);
            }
        } else {
            FullnameArgument fa = UnifiedInboxUtility.parseNestedFullName(fullName);
            MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
            try {
                mailAccess = MailAccess.getInstance(session, fa.getAccountId());
                mailAccess.connect();
                mailAccess.getMessageStorage().updateMessageColorLabel(fa.getFullname(), mailIds, colorLabel);
            } finally {
                closeSafe(mailAccess);
            }
        }
    }

    @Override
    public MailMessage saveDraft(String draftFullName, ComposedMailMessage composedMail) throws OXException {
        throw UnifiedInboxException.Code.DRAFTS_NOT_SUPPORTED.create();
    }

    /*-
     * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
     * +++++++++++++++++ Helper methods +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
     * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
     */

    private static void insertMessage(String[] mailIds, MailMessage[] toFill, int accountId, String folder, MailMessage[] mails, String uiFullname, int undelegatedAccountId) {
        UnifiedInboxUID helper = new UnifiedInboxUID();
        for (MailMessage mail : mails) {
            if (null != mail) {
                String lookFor = helper.setUID(accountId, folder, mail.getMailId()).toString();
                int pos = -1;
                for (int l = 0; l < mailIds.length && pos == -1; l++) {
                    if (lookFor.equals(mailIds[l])) {
                        pos = l;
                    }
                }
                if (pos != -1) {
                    UnifiedMailMessage umm = new UnifiedMailMessage(mail, undelegatedAccountId);
                    toFill[pos] = umm;
                    umm.setMailId(mailIds[pos]);
                    umm.setFolder(uiFullname);
                    umm.setAccountId(accountId);
                }
            }
        }
    }

    private static class GetMessagesResult {

        static final GetMessagesResult EMPTY_RESULT = new GetMessagesResult(-1, null, new MailMessage[0]);

        MailMessage[] mails;
        String folder;
        int accountId;

        public GetMessagesResult(int accountId, String folder, MailMessage[] mails) {
            super();
            this.mails = mails;
            this.folder = folder;
            this.accountId = accountId;
        }

    }

    @Override
    public MailMessage[] getMessagesByMessageIDByFolder(String fullName, String... messageIDs) throws OXException {
        if (null == messageIDs || messageIDs.length <= 0) {
            return new MailMessage[0];
        }

        throw MailExceptionCode.UNSUPPORTED_OPERATION.create();
    }

}

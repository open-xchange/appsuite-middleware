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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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
import org.apache.commons.logging.Log;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailFields;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.ISimplifiedThreadStructure;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.api.MailMessageStorage;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.mail.utils.MailMessageComparator;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.session.Session;
import com.openexchange.threadpool.Task;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.unifiedinbox.copy.UnifiedInboxMessageCopier;
import com.openexchange.unifiedinbox.dataobjects.UnifiedMailMessage;
import com.openexchange.unifiedinbox.services.UnifiedInboxServiceRegistry;
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
public final class UnifiedInboxMessageStorage extends MailMessageStorage implements ISimplifiedThreadStructure {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(UnifiedInboxMessageStorage.class);
    private static final boolean DEBUG = LOG.isDebugEnabled();

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
    public UnifiedInboxMessageStorage(final UnifiedInboxAccess access, final Session session) throws OXException {
        super();
        this.access = access;
        this.session = session;
        cid = session.getContextId();
        {
            final ContextService contextService = UnifiedInboxServiceRegistry.getServiceRegistry().getService(ContextService.class, true);
            ctx = contextService.getContext(cid);
        }
        user = session.getUserId();
    }

    protected static void closeSafe(final MailAccess<?, ?> mailAccess) {
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
    private Locale getLocale() throws OXException {
        if (null == locale) {
            final UserService userService = UnifiedInboxServiceRegistry.getServiceRegistry().getService(UserService.class, true);
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
        final MailAccountStorageService srv = UnifiedInboxServiceRegistry.getServiceRegistry().getService(MailAccountStorageService.class, true);
        final MailAccount[] tmp = srv.getUserMailAccounts(user, cid);
        final List<MailAccount> accounts = new ArrayList<MailAccount>(tmp.length);
        final int thisAccountId = access.getAccountId();
        for (final MailAccount mailAccount : tmp) {
            if (mailAccount.isUnifiedINBOXEnabled() && thisAccountId != mailAccount.getId()) {
                accounts.add(mailAccount);
            }
        }
        return accounts;
    }

    @Override
    public void releaseResources() throws OXException {
        // Nothing to release
    }

    @Override
    public MailMessage[] getMessages(final String fullName, final String[] mailIds, final MailField[] fields) throws OXException {
        if ((mailIds == null) || (mailIds.length == 0)) {
            return EMPTY_RETVAL;
        }
        {
            final MailFields fieldSet = new MailFields(fields);
            if (fieldSet.contains(MailField.FULL) || fieldSet.contains(MailField.BODY)) {
                final MailMessage[] mails = new MailMessage[mailIds.length];
                for (int j = 0; j < mails.length; j++) {
                    mails[j] = getMessage(fullName, mailIds[j], true);
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
            final Map<Integer, Map<String, List<String>>> parsed = UnifiedInboxUtility.parseMailIDs(mailIds);
            // Create completion service for simultaneous access
            final Executor executor = ThreadPools.getThreadPool().getExecutor();
            final TrackingCompletionService<GetMessagesResult> completionService =
                new UnifiedInboxCompletionService<GetMessagesResult>(executor);
            // Iterate parsed map and submit a task for each iteration
            int numTasks = 0;
            for (final Iterator<Map.Entry<Integer, Map<String, List<String>>>> iter = parsed.entrySet().iterator(); iter.hasNext();) {
                final Map.Entry<Integer, Map<String, List<String>>> accountMapEntry = iter.next();
                numTasks++;
                completionService.submit(new LoggingCallable<GetMessagesResult>(session) {

                    @Override
                    public GetMessagesResult call() throws OXException {
                        final int accountId = accountMapEntry.getKey().intValue();
                        // Get account's mail access
                        MailAccess<?, ?> mailAccess = null;
                        try {
                            mailAccess = MailAccess.getInstance(getSession(), accountId);
                            mailAccess.connect();
                            final Map<String, List<String>> folderUIDMap = accountMapEntry.getValue();
                            for (final Iterator<Map.Entry<String, List<String>>> inneriter = folderUIDMap.entrySet().iterator(); inneriter.hasNext();) {
                                final Map.Entry<String, List<String>> e = inneriter.next();
                                final String folder = e.getKey();
                                final List<String> uids = e.getValue();
                                try {
                                    final MailMessage[] mails =
                                        mailAccess.getMessageStorage().getMessages(folder, uids.toArray(new String[uids.size()]), fields);
                                    for (final MailMessage mail : messages) {
                                        if (null != mail) {
                                            mail.setAccountId(accountId);
                                        }
                                    }
                                    return new GetMessagesResult(accountId, folder, mails);
                                } catch (final OXException me) {
                                    final MailConfig config = mailAccess.getMailConfig();
                                    final StringBuilder tmp = new StringBuilder(128);
                                    tmp.append("Couldn't get messages from folder \"");
                                    tmp.append((null == folder ? "<unknown>" : folder)).append("\" from server \"").append(
                                        config.getServer());
                                    tmp.append("\" for login \"").append(config.getLogin()).append("\".");
                                    getLogger().warn(tmp.toString(), me);
                                    return GetMessagesResult.EMPTY_RESULT;
                                } catch (final RuntimeException rte) {
                                    final MailConfig config = mailAccess.getMailConfig();
                                    final StringBuilder tmp = new StringBuilder(128);
                                    tmp.append("Couldn't get messages from folder \"");
                                    tmp.append((null == folder ? "<unknown>" : folder)).append("\" from server \"").append(
                                        config.getServer());
                                    tmp.append("\" for login \"").append(config.getLogin()).append("\".");
                                    getLogger().warn(tmp.toString(), rte);
                                    return GetMessagesResult.EMPTY_RESULT;
                                }
                            }
                        } catch (final OXException e) {
                            getLogger().debug(e.getMessage(), e);
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
                        closeSafe(mailAccess);
                    }
                }
                 */
            }
            // Wait for completion of each submitted task
            final int undelegatedAccountId = access.getAccountId();
            try {
                for (int i = 0; i < numTasks; i++) {
                    final GetMessagesResult result = completionService.take().get();
                    insertMessage(mailIds, messages, result.accountId, result.folder, result.mails, fullName, undelegatedAccountId);
                }
                if (DEBUG) {
                    LOG.debug(new StringBuilder(64).append("Retrieving ").append(mailIds.length).append(" messages from folder \"").append(
                        fullName).append("\" took ").append(completionService.getDuration()).append("msec."));
                }
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw MailExceptionCode.INTERRUPT_ERROR.create(e);
            } catch (final ExecutionException e) {
                throw ThreadPools.launderThrowable(e, OXException.class);
            }
            // Return properly filled array
            return messages;
        }
        final FullnameArgument fa = UnifiedInboxUtility.parseNestedFullname(fullName);
        MailAccess<?, ?> mailAccess = null;
        try {
            final int accountId = fa.getAccountId();
            mailAccess = MailAccess.getInstance(session, accountId);
            mailAccess.connect();
            // Get messages
            final MailMessage[] mails = mailAccess.getMessageStorage().getMessages(fa.getFullname(), mailIds, fields);
            final int unifiedAccountId = this.access.getAccountId();
            for (final MailMessage mail : mails) {
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
    public MailMessage getMessage(final String fullName, final String mailId, final boolean markSeen) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(fullName)) {
            throw UnifiedInboxException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES.create(fullName);
        }
        if (UnifiedInboxAccess.KNOWN_FOLDERS.contains(fullName)) {
            final UnifiedInboxUID uid = new UnifiedInboxUID(mailId);
            MailAccess<?, ?> mailAccess = null;
            try {
                mailAccess = MailAccess.getInstance(session, uid.getAccountId());
                mailAccess.connect();
                MailMessage mail = mailAccess.getMessageStorage().getMessage(uid.getFullName(), uid.getId(), markSeen);
                if (null == mail) {
                    return null;
                }
                mail = new UnifiedMailMessage(mail, access.getAccountId());
                mail.loadContent();
                mail.setMailId(mailId);
                mail.setFolder(fullName);
                mail.setAccountId(uid.getAccountId());
                return mail;
            } finally {
                closeSafe(mailAccess);
            }
        }
        final FullnameArgument fa = UnifiedInboxUtility.parseNestedFullname(fullName);
        MailAccess<?, ?> mailAccess = null;
        try {
            mailAccess = MailAccess.getInstance(session, fa.getAccountId());
            mailAccess.connect();
            // Get message
            final MailMessage mail = mailAccess.getMessageStorage().getMessage(fa.getFullname(), mailId, markSeen);
            if (null == mail) {
                return null;
            }
            final int unifiedAccountId = this.access.getAccountId();
            mail.loadContent();
            mail.setFolder(fullName);
            mail.setAccountId(unifiedAccountId);
            return mail;
        } finally {
                closeSafe(mailAccess);
        }
    }

    static final MailMessageComparator COMPARATOR = new MailMessageComparator(MailSortField.RECEIVED_DATE, true, null);

    @Override
    public List<List<MailMessage>> getThreadSortedMessages(final String fullName, final boolean includeSent, final boolean cache, final IndexRange indexRange, final long max, final MailSortField sortField, final OrderDirection order, final MailField[] mailFields) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(fullName)) {
            throw UnifiedInboxException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES.create(fullName);
        }
        if (UnifiedInboxAccess.KNOWN_FOLDERS.contains(fullName)) {
            final List<MailAccount> accounts = getAccounts();
            final int undelegatedAccountId = access.getAccountId();
            final boolean descending = OrderDirection.DESC.equals(order);
            final MailSortField effectiveSortField = null == sortField ? MailSortField.RECEIVED_DATE :  sortField;
            final MailFields mfs = new MailFields(mailFields);
            mfs.add(MailField.getField(effectiveSortField.getField()));
            final MailField[] checkedFields = mfs.toArray();
            // Create completion service for simultaneous access
            final int length = accounts.size();
            final Executor executor = ThreadPools.getThreadPool().getExecutor();
            final TrackingCompletionService<List<List<MailMessage>>> completionService =
                new UnifiedInboxCompletionService<List<List<MailMessage>>>(executor);
            for (final MailAccount mailAccount : accounts) {
                completionService.submit(new LoggingCallable<List<List<MailMessage>>>(session) {

                    @Override
                    public List<List<MailMessage>> call() {
                        final int accountId = mailAccount.getId();
                        MailAccess<?, ?> mailAccess = null;
                        String fn = null;
                        try {
                            mailAccess = MailAccess.getInstance(getSession(), accountId);
                            mailAccess.connect();
                            // Get real full name
                            fn = UnifiedInboxUtility.determineAccountFullname(mailAccess, fullName);
                            // Check if denoted account has such a default folder
                            if (fn == null) {
                                return Collections.emptyList();
                            }
                            // Get account's messages
                            final IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
                            if (messageStorage instanceof ISimplifiedThreadStructure) {
                                try {
                                    final List<List<MailMessage>> list = ((ISimplifiedThreadStructure) messageStorage).getThreadSortedMessages(fn, includeSent, false, null, max, sortField, order, checkedFields);
                                    final List<List<MailMessage>> ret = new ArrayList<List<MailMessage>>(list.size());
                                    final UnifiedInboxUID helper = new UnifiedInboxUID();
                                    for (final List<MailMessage> list2 : list) {
                                        final List<MailMessage> messages = new ArrayList<MailMessage>(list2.size());
                                        for (final MailMessage accountMail : list2) {
                                            final UnifiedMailMessage umm = new UnifiedMailMessage(accountMail, undelegatedAccountId);
                                            final String accountMailFolder = accountMail.getFolder();
                                            umm.setMailId(helper.setUID(accountId, accountMailFolder, accountMail.getMailId()).toString());
                                            umm.setFolder(fn.equals(accountMailFolder) ? fullName : UnifiedInboxAccess.SENT);
                                            umm.setAccountId(accountId);
                                            messages.add(umm);
                                        }
                                        ret.add(messages);
                                    }
                                    return ret;
                                } catch (final OXException e) {
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
                                final int allSort = MailSortField.RECEIVED_DATE.getField();
                                final int allOrder = OrderDirection.DESC.getOrder();
                                msgArr = messageStorage.getThreadSortedMessages(fn, null, sortField, order, null, checkedFields);
                            } catch (final OXException e) {
                                msgArr = messageStorage.getAllMessages(fn, null, sortField, order, checkedFields);
                            }
                            final List<List<MailMessage>> list = new LinkedList<List<MailMessage>>();
                            List<MailMessage> current = new LinkedList<MailMessage>();
                            // Here we go
                            final int size = msgArr.length;
                            for (int i = 0; i < size; i++) {
                                final MailMessage mail = msgArr[i];
                                final int threadLevel = mail.getThreadLevel();
                                if (0 == threadLevel) {
                                    list.add(current);
                                    current = new LinkedList<MailMessage>();
                                }
                                current.add(mail);
                            }
                            list.add(current);
                            /*
                             * Sort empty ones
                             */
                            for (final Iterator<List<MailMessage>> iterator = list.iterator(); iterator.hasNext();) {
                                final List<MailMessage> mails = iterator.next();
                                if (null == mails || mails.isEmpty()) {
                                    iterator.remove();
                                } else {
                                    Collections.sort(mails, COMPARATOR);
                                }
                            }
                            /*
                             * Sort root elements
                             */
                            final boolean descending = OrderDirection.DESC.equals(order);
                            MailSortField effectiveSortField = null == sortField ? MailSortField.RECEIVED_DATE :  sortField;
                            if (null == effectiveSortField) {
                                effectiveSortField = MailSortField.RECEIVED_DATE;
                            }
                            final MailMessageComparator comparator = new MailMessageComparator(effectiveSortField, descending, null);
                            final Comparator<List<MailMessage>> listComparator = new Comparator<List<MailMessage>>() {

                                @Override
                                public int compare(final List<MailMessage> o1, final List<MailMessage> o2) {
                                    return comparator.compare(o1.get(0), o2.get(0));
                                }
                            };
                            Collections.sort(list, listComparator);
                            final List<List<MailMessage>> ret = new ArrayList<List<MailMessage>>(list.size());
                            final UnifiedInboxUID helper = new UnifiedInboxUID();
                            for (final List<MailMessage> list2 : list) {
                                final List<MailMessage> messages = new ArrayList<MailMessage>(list2.size());
                                for (final MailMessage accountMail : list2) {
                                    final UnifiedMailMessage umm = new UnifiedMailMessage(accountMail, undelegatedAccountId);
                                    final String accountMailFolder = accountMail.getFolder();
                                    umm.setMailId(helper.setUID(accountId, accountMailFolder, accountMail.getMailId()).toString());
                                    umm.setFolder(fn.equals(accountMailFolder) ? fullName : UnifiedInboxAccess.SENT);
                                    umm.setAccountId(accountId);
                                    messages.add(umm);
                                }
                                ret.add(messages);
                            }
                            return ret;
                        } catch (final OXException e) {
                            final StringBuilder tmp = new StringBuilder(128);
                            tmp.append("Couldn't get messages from folder \"");
                            tmp.append((null == fn ? "<unknown>" : fn)).append("\" from server \"").append(mailAccount.getMailServer());
                            tmp.append("\" for login \"").append(mailAccount.getLogin()).append("\".");
                            getLogger().warn(tmp.toString(), e);
                            return Collections.emptyList();
                        } catch (final RuntimeException e) {
                            final StringBuilder tmp = new StringBuilder(128);
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
                List<List<MailMessage>> messages = new ArrayList<List<MailMessage>>(length << 2);
                for (int i = 0; i < length; i++) {
                    messages.addAll(completionService.take().get());
                }
                if (DEBUG) {
                    LOG.debug(new StringBuilder(64).append("getThreadSortedMessages from folder \"").append(fullName).append("\" took ").append(
                        completionService.getDuration()).append("msec."));
                }
                // Sort them
                final MailMessageComparator comparator = new MailMessageComparator(effectiveSortField, descending, null);
                final Comparator<List<MailMessage>> listComparator = new Comparator<List<MailMessage>>() {

                    @Override
                    public int compare(final List<MailMessage> o1, final List<MailMessage> o2) {
                        return comparator.compare(o1.get(0), o2.get(0));
                    }
                };
                Collections.sort(messages, listComparator);
                // Return as array
                if (null == indexRange) {
                    return messages;
                }
                // Apply index range
                final int fromIndex = indexRange.start;
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
                    toIndex = messages.size();
                }
                messages = messages.subList(fromIndex, toIndex);
                return messages;
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw MailExceptionCode.INTERRUPT_ERROR.create(e);
            } catch (final ExecutionException e) {
                throw ThreadPools.launderThrowable(e, OXException.class);
            }
        }
        /*
         * Certain account's folder
         */
        final FullnameArgument fa = UnifiedInboxUtility.parseNestedFullname(fullName);
        MailAccess<?, ?> mailAccess = null;
        try {
            final int accountId = fa.getAccountId();
            mailAccess = MailAccess.getInstance(session, accountId);
            mailAccess.connect();
            // Get account's messages
            final IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
            if (messageStorage instanceof ISimplifiedThreadStructure) {
                try {
                    return ((ISimplifiedThreadStructure) messageStorage).getThreadSortedMessages(fa.getFullname(), includeSent, false, indexRange, max, sortField, order, mailFields);
                } catch (final OXException e) {
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
                final int allSort = MailSortField.RECEIVED_DATE.getField();
                final int allOrder = OrderDirection.DESC.getOrder();
                msgArr = messageStorage.getThreadSortedMessages(fullName, indexRange, sortField, order, null, mailFields);
            } catch (final OXException e) {
                msgArr = messageStorage.getAllMessages(fullName, indexRange, sortField, order, mailFields);
            }
            List<List<MailMessage>> list = new LinkedList<List<MailMessage>>();
            List<MailMessage> current = new LinkedList<MailMessage>();
            // Here we go
            final int size = msgArr.length;
            for (int i = 0; i < size; i++) {
                final MailMessage mail = msgArr[i];
                final int threadLevel = mail.getThreadLevel();
                if (0 == threadLevel) {
                    list.add(current);
                    current = new LinkedList<MailMessage>();
                }
                current.add(mail);
            }
            list.add(current);
            /*
             * Sort empty ones
             */
            for (final Iterator<List<MailMessage>> iterator = list.iterator(); iterator.hasNext();) {
                final List<MailMessage> mails = iterator.next();
                if (null == mails || mails.isEmpty()) {
                    iterator.remove();
                } else {
                    Collections.sort(mails, COMPARATOR);
                }
            }
            /*
             * Sort root elements
             */
            final boolean descending = OrderDirection.DESC.equals(order);
            MailSortField effectiveSortField = null == sortField ? MailSortField.RECEIVED_DATE :  sortField;
            if (null == effectiveSortField) {
                effectiveSortField = MailSortField.RECEIVED_DATE;
            }
            final MailMessageComparator comparator = new MailMessageComparator(effectiveSortField, descending, null);
            final Comparator<List<MailMessage>> listComparator = new Comparator<List<MailMessage>>() {

                @Override
                public int compare(final List<MailMessage> o1, final List<MailMessage> o2) {
                    return comparator.compare(o1.get(0), o2.get(0));
                }
            };
            Collections.sort(list, listComparator);
            if (null != indexRange) {
                final int fromIndex = indexRange.start;
                int toIndex = indexRange.end;
                final int lsize = list.size();
                if ((fromIndex) > lsize) {
                    /*
                     * Return empty iterator if start is out of range
                     */
                    return Collections.emptyList();
                }
                /*
                 * Reset end index if out of range
                 */
                if (toIndex >= lsize) {
                    toIndex = lsize;
                }
                list = list.subList(fromIndex, toIndex);
            }
            /*
             * Finally return
             */
            return list;
        } finally {
            closeSafe(mailAccess);
        }
    }

    @Override
    public MailMessage[] getThreadSortedMessages(final String fullName, final IndexRange indexRange, final MailSortField sortField, final OrderDirection order, final SearchTerm<?> searchTerm, final MailField[] fields) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(fullName)) {
            throw UnifiedInboxException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES.create(fullName);
        }
        if (UnifiedInboxAccess.KNOWN_FOLDERS.contains(fullName)) {
            final List<MailAccount> accounts = getAccounts();
            final MailFields mfs = new MailFields(fields);
            mfs.add(MailField.getField(sortField.getField()));
            final MailField[] checkedFields = mfs.toArray();
            // Create completion service for simultaneous access
            final int length = accounts.size();
            final int undelegatedAccountId = access.getAccountId();
            final Executor executor = ThreadPools.getThreadPool().getExecutor();
            final TrackingCompletionService<List<MailMessage>> completionService =
                new UnifiedInboxCompletionService<List<MailMessage>>(executor);
            for (final MailAccount mailAccount : accounts) {
                completionService.submit(new LoggingCallable<List<MailMessage>>(session) {

                    @Override
                    public List<MailMessage> call() {
                        final int accountId = mailAccount.getId();
                        MailAccess<?, ?> mailAccess = null;
                        String fn = null;
                        try {
                            mailAccess = MailAccess.getInstance(getSession(), accountId);
                            mailAccess.connect();
                            // Get real full name
                            fn = UnifiedInboxUtility.determineAccountFullname(mailAccess, fullName);
                            // Check if denoted account has such a default folder
                            if (fn == null) {
                                return Collections.emptyList();
                            }
                            // Get account's messages
                            final MailMessage[] accountMails =
                                mailAccess.getMessageStorage().getThreadSortedMessages(fn, null, MailSortField.RECEIVED_DATE, OrderDirection.DESC, searchTerm, checkedFields);
                            final List<MailMessage> messages = new ArrayList<MailMessage>(accountMails.length);
                            final UnifiedInboxUID helper = new UnifiedInboxUID();
                            for (final MailMessage accountMail : accountMails) {
                                final UnifiedMailMessage umm = new UnifiedMailMessage(accountMail, undelegatedAccountId);
                                umm.setMailId(helper.setUID(accountId, fn, accountMail.getMailId()).toString());
                                umm.setFolder(fullName);
                                umm.setAccountId(accountId);
                                messages.add(umm);
                            }
                            return messages;
                        } catch (final OXException e) {
                            final StringBuilder tmp = new StringBuilder(128);
                            tmp.append("Couldn't get messages from folder \"");
                            tmp.append((null == fn ? "<unknown>" : fn)).append("\" from server \"").append(mailAccount.getMailServer());
                            tmp.append("\" for login \"").append(mailAccount.getLogin()).append("\".");
                            getLogger().warn(tmp.toString(), e);
                            return Collections.emptyList();
                        } catch (final RuntimeException e) {
                            final StringBuilder tmp = new StringBuilder(128);
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
                List<MailMessage> messages = new ArrayList<MailMessage>(length << 2);
                for (int i = 0; i < length; i++) {
                    messages.addAll(completionService.take().get());
                }
                if (DEBUG) {
                    LOG.debug(new StringBuilder(64).append("Searching messages from folder \"").append(fullName).append("\" took ").append(
                        completionService.getDuration()).append("msec."));
                }
                // Sort them
                final MailMessageComparator c = new MailMessageComparator(sortField, OrderDirection.DESC.equals(order), getLocale());
                Collections.sort(messages, c);
                // Return as array
                if (null == indexRange) {
                    return messages.toArray(new MailMessage[messages.size()]);
                }
                // Apply index range
                final int fromIndex = indexRange.start;
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
                    toIndex = messages.size();
                }
                messages = messages.subList(fromIndex, toIndex);
                return messages.toArray(new MailMessage[messages.size()]);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw MailExceptionCode.INTERRUPT_ERROR.create(e);
            } catch (final ExecutionException e) {
                throw ThreadPools.launderThrowable(e, OXException.class);
            }
        }
        final FullnameArgument fa = UnifiedInboxUtility.parseNestedFullname(fullName);
        MailAccess<?, ?> mailAccess = null;
        try {
            final int accountId = fa.getAccountId();
            mailAccess = MailAccess.getInstance(session, accountId);
            mailAccess.connect();
            // Get account's messages
            final MailMessage[] mails =
                mailAccess.getMessageStorage().getThreadSortedMessages(fa.getFullname(), indexRange, sortField, order, searchTerm, fields);
            final int unifiedAccountId = this.access.getAccountId();
            for (final MailMessage mail : mails) {
                mail.setFolder(fullName);
                mail.setAccountId(unifiedAccountId);
            }
            return mails;
        } finally {
                closeSafe(mailAccess);
        }
    }

    @Override
    public MailMessage[] searchMessages(final String fullName, final IndexRange indexRange, final MailSortField sortField, final OrderDirection order, final SearchTerm<?> searchTerm, final MailField[] fields) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(fullName)) {
            throw UnifiedInboxException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES.create(fullName);
        }
        if (UnifiedInboxAccess.KNOWN_FOLDERS.contains(fullName)) {
            final List<MailAccount> accounts = getAccounts();
            final MailFields mfs = new MailFields(fields);
            mfs.add(MailField.getField(sortField.getField()));
            final MailField[] checkedFields = mfs.toArray();
            // Create completion service for simultaneous access
            final int length = accounts.size();
            final int undelegatedAccountId = access.getAccountId();
            final Executor executor = ThreadPools.getThreadPool().getExecutor();
            final TrackingCompletionService<List<MailMessage>> completionService =
                new UnifiedInboxCompletionService<List<MailMessage>>(executor);
            for (final MailAccount mailAccount : accounts) {
                completionService.submit(new LoggingCallable<List<MailMessage>>(session) {

                    @Override
                    public List<MailMessage> call() {
                        final int accountId = mailAccount.getId();
                        MailAccess<?, ?> mailAccess = null;
                        String fn = null;
                        try {
                            mailAccess = MailAccess.getInstance(getSession(), accountId);
                            mailAccess.connect();
                            // Get real full name
                            fn = UnifiedInboxUtility.determineAccountFullname(mailAccess, fullName);
                            // Check if denoted account has such a default folder
                            if (fn == null) {
                                return Collections.emptyList();
                            }
                            // Get account's messages
                            final MailMessage[] accountMails =
                                mailAccess.getMessageStorage().searchMessages(fn, null, MailSortField.RECEIVED_DATE, OrderDirection.DESC, searchTerm, checkedFields);
                            final List<MailMessage> messages = new ArrayList<MailMessage>(accountMails.length);
                            final UnifiedInboxUID helper = new UnifiedInboxUID();
                            for (final MailMessage accountMail : accountMails) {
                                final UnifiedMailMessage umm = new UnifiedMailMessage(accountMail, undelegatedAccountId);
                                umm.setMailId(helper.setUID(accountId, fn, accountMail.getMailId()).toString());
                                umm.setFolder(fullName);
                                umm.setAccountId(accountId);
                                umm.setAccountName(mailAccount.getName());
                                messages.add(umm);
                            }
                            return messages;
                        } catch (final OXException e) {
                            final StringBuilder tmp = new StringBuilder(128);
                            tmp.append("Couldn't get messages from folder \"");
                            tmp.append((null == fn ? "<unknown>" : fn)).append("\" from server \"").append(mailAccount.getMailServer());
                            tmp.append("\" for login \"").append(mailAccount.getLogin()).append("\".");
                            getLogger().warn(tmp.toString(), e);
                            return Collections.emptyList();
                        } catch (final RuntimeException e) {
                            final StringBuilder tmp = new StringBuilder(128);
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
                List<MailMessage> messages = new ArrayList<MailMessage>(length << 2);
                for (int i = 0; i < length; i++) {
                    messages.addAll(completionService.take().get());
                }
                if (DEBUG) {
                    LOG.debug(new StringBuilder(64).append("Searching messages from folder \"").append(fullName).append("\" took ").append(
                        completionService.getDuration()).append("msec."));
                }
                // Sort them
                final MailMessageComparator c = new MailMessageComparator(sortField, OrderDirection.DESC.equals(order), getLocale());
                Collections.sort(messages, c);
                // Return as array
                if (null == indexRange) {
                    return messages.toArray(new MailMessage[messages.size()]);
                }
                // Apply index range
                final int fromIndex = indexRange.start;
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
                    toIndex = messages.size();
                }
                messages = messages.subList(fromIndex, toIndex);
                return messages.toArray(new MailMessage[messages.size()]);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw MailExceptionCode.INTERRUPT_ERROR.create(e);
            } catch (final ExecutionException e) {
                throw ThreadPools.launderThrowable(e, OXException.class);
            }
        }
        final FullnameArgument fa = UnifiedInboxUtility.parseNestedFullname(fullName);
        MailAccess<?, ?> mailAccess = null;
        try {
            final int accountId = fa.getAccountId();
            mailAccess = MailAccess.getInstance(session, accountId);
            mailAccess.connect();
            // Get account's messages
            final MailMessage[] mails =
                mailAccess.getMessageStorage().searchMessages(fa.getFullname(), indexRange, sortField, order, searchTerm, fields);
            final int unifiedAccountId = this.access.getAccountId();
            for (final MailMessage mail : mails) {
                mail.setFolder(fullName);
                mail.setAccountId(unifiedAccountId);
            }
            return mails;
        } finally {
            closeSafe(mailAccess);
        }
    }

    @Override
    public MailMessage[] getUnreadMessages(final String fullName, final MailSortField sortField, final OrderDirection order, final MailField[] fields, final int limit) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(fullName)) {
            throw UnifiedInboxException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES.create(fullName);
        }
        if (UnifiedInboxAccess.KNOWN_FOLDERS.contains(fullName)) {
            final List<MailAccount> accounts = getAccounts();
            final int length = accounts.size();
            final int undelegatedAccountId = access.getAccountId();
            final Executor executor = ThreadPools.getThreadPool().getExecutor();
            final TrackingCompletionService<List<MailMessage>> completionService =
                new UnifiedInboxCompletionService<List<MailMessage>>(executor);
            for (final MailAccount mailAccount : accounts) {
                completionService.submit(new LoggingCallable<List<MailMessage>>(session) {

                    @Override
                    public List<MailMessage> call() throws Exception {
                        MailAccess<?, ?> mailAccess = null;
                        try {
                            final int accountId = mailAccount.getId();
                            mailAccess = MailAccess.getInstance(getSession(), accountId);
                            mailAccess.connect();
                            // Get real full name
                            final String fn = UnifiedInboxUtility.determineAccountFullname(mailAccess, fullName);
                            // Check if denoted account has such a default folder
                            if (fn == null) {
                                return Collections.emptyList();
                            }
                            // Get account's unread messages
                            final MailMessage[] accountMails =
                                mailAccess.getMessageStorage().getUnreadMessages(fn, sortField, order, fields, limit);
                            final UnifiedInboxUID helper = new UnifiedInboxUID();
                            final List<MailMessage> messages = new ArrayList<MailMessage>(accountMails.length);
                            for (final MailMessage accountMail : accountMails) {
                                final UnifiedMailMessage umm = new UnifiedMailMessage(accountMail, undelegatedAccountId);
                                umm.setMailId(helper.setUID(accountId, fn, accountMail.getMailId()).toString());
                                umm.setFolder(fullName);
                                umm.setAccountId(accountId);
                                messages.add(umm);
                            }
                            return messages;
                        } catch (final OXException e) {
                            getLogger().debug(e.getMessage(), e);
                            return Collections.emptyList();
                        } finally {
                            closeSafe(mailAccess);
                        }
                    }
                });
            }
            // Wait for completion of each submitted task
            try {
                final List<MailMessage> messages = new ArrayList<MailMessage>(length << 2);
                for (int i = 0; i < length; i++) {
                    messages.addAll(completionService.take().get());
                }
                if (DEBUG) {
                    LOG.debug(new StringBuilder(64).append("Retrieving unread messages from folder \"").append(fullName).append("\" took ").append(
                        completionService.getDuration()).append("msec."));
                }
                // Sort them
                Collections.sort(messages, new MailMessageComparator(sortField, OrderDirection.DESC.equals(order), getLocale()));
                // Return as array
                return messages.toArray(new MailMessage[messages.size()]);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw MailExceptionCode.INTERRUPT_ERROR.create(e);
            } catch (final ExecutionException e) {
                throw ThreadPools.launderThrowable(e, OXException.class);
            }
        }
        final FullnameArgument fa = UnifiedInboxUtility.parseNestedFullname(fullName);
        MailAccess<?, ?> mailAccess = null;
        try {
            final int accountId = fa.getAccountId();
            mailAccess = MailAccess.getInstance(session, accountId);
            mailAccess.connect();
            // Get account's messages
            final MailMessage[] mails = mailAccess.getMessageStorage().getUnreadMessages(fa.getFullname(), sortField, order, fields, limit);
            final int unifiedAccountId = this.access.getAccountId();
            for (final MailMessage mail : mails) {
                mail.setFolder(fullName);
                mail.setAccountId(unifiedAccountId);
            }
            return mails;
        } finally {
                closeSafe(mailAccess);
        }
    }

    @Override
    public void deleteMessages(final String fullName, final String[] mailIds, final boolean hardDelete) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(fullName)) {
            throw UnifiedInboxException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES.create(fullName);
        }
        if (UnifiedInboxAccess.KNOWN_FOLDERS.contains(fullName)) {
            // Parse mail IDs
            final Map<Integer, Map<String, List<String>>> parsed = UnifiedInboxUtility.parseMailIDs(mailIds);
            final int size = parsed.size();
            final Iterator<Map.Entry<Integer, Map<String, List<String>>>> iter = parsed.entrySet().iterator();
            // Collection of Callables
            final Collection<Task<Object>> collection = new ArrayList<Task<Object>>(size);
            for (int i = 0; i < size; i++) {
                final Map.Entry<Integer, Map<String, List<String>>> accountMapEntry = iter.next();
                collection.add(new LoggingCallable<Object>(session) {

                    @Override
                    public Object call() throws Exception {
                        final int accountId = accountMapEntry.getKey().intValue();
                        // Get account's mail access
                        MailAccess<?, ?> mailAccess = null;
                        try {
                            mailAccess = MailAccess.getInstance(getSession(), accountId);
                            mailAccess.connect();
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
                        } catch (final OXException e) {
                            getLogger().debug(e.getMessage(), e);
                            return null;
                        } finally {
                            closeSafe(mailAccess);
                        }
                        return null;
                    }
                });
            }
            final ThreadPoolService executor = ThreadPools.getThreadPool();
            try {
                // Invoke all and wait for being executed
                if (DEBUG) {
                    final long start = System.currentTimeMillis();
                    executor.invokeAll(collection);
                    final long dur = System.currentTimeMillis() - start;
                    LOG.debug(new StringBuilder(64).append("Deleting ").append(mailIds.length).append(" messages in folder \"").append(
                        fullName).append(" took ").append(dur).append("msec."));
                } else {
                    executor.invokeAll(collection);
                }
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw MailExceptionCode.INTERRUPT_ERROR.create(e);
            }
        } else {
            final FullnameArgument fa = UnifiedInboxUtility.parseNestedFullname(fullName);
            MailAccess<?, ?> mailAccess = null;
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
    public String[] copyMessages(final String sourceFolder, final String destFolder, final String[] mailIds, final boolean fast) throws OXException {
        return getCopier().doCopy(sourceFolder, destFolder, mailIds, fast, false);
    }

    @Override
    public String[] moveMessages(final String sourceFolder, final String destFolder, final String[] mailIds, final boolean fast) throws OXException {
        return getCopier().doCopy(sourceFolder, destFolder, mailIds, fast, true);
    }

    @Override
    public String[] appendMessages(final String destFullname, final MailMessage[] mailMessages) throws OXException {
        if (UnifiedInboxAccess.KNOWN_FOLDERS.contains(destFullname)) {
            // TODO: Error code OR default account?!
            throw UnifiedInboxException.Code.INVALID_DESTINATION_FOLDER.create(new Object[0]);
        }
        // Parse destination folder
        final FullnameArgument destFullnameArgument = UnifiedInboxUtility.parseNestedFullname(destFullname);
        MailAccess<?, ?> mailAccess = null;
        try {
            mailAccess = MailAccess.getInstance(session, destFullnameArgument.getAccountId());
            mailAccess.connect();
            return mailAccess.getMessageStorage().appendMessages(destFullnameArgument.getFullname(), mailMessages);
        } finally {
            closeSafe(mailAccess);
        }
    }

    @Override
    public void updateMessageFlags(final String fullName, final String[] mailIds, final int flags, final boolean set) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(fullName)) {
            throw UnifiedInboxException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES.create(fullName);
        }
        if (UnifiedInboxAccess.KNOWN_FOLDERS.contains(fullName)) {
            // Parse mail IDs
            final Map<Integer, Map<String, List<String>>> parsed = UnifiedInboxUtility.parseMailIDs(mailIds);
            final int size = parsed.size();
            final Iterator<Map.Entry<Integer, Map<String, List<String>>>> iter = parsed.entrySet().iterator();
            // Collection of Callables
            final Collection<Task<Object>> collection = new ArrayList<Task<Object>>(size);
            for (int i = 0; i < size; i++) {
                final Map.Entry<Integer, Map<String, List<String>>> accountMapEntry = iter.next();
                collection.add(new LoggingCallable<Object>(session) {

                    @Override
                    public Object call() throws Exception {
                        final int accountId = accountMapEntry.getKey().intValue();
                        // Get account's mail access
                        MailAccess<?, ?> mailAccess = null;
                        try {
                            mailAccess = MailAccess.getInstance(getSession(), accountId);
                            mailAccess.connect();
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
                        } catch (final OXException e) {
                            getLogger().debug(e.getMessage(), e);
                            return null;
                        } finally {
                            closeSafe(mailAccess);
                        }
                        return null;
                    }
                });
            }
            final ThreadPoolService executor = ThreadPools.getThreadPool();
            try {
                // Invoke all and wait for being executed
                if (DEBUG) {
                    final long start = System.currentTimeMillis();
                    executor.invokeAll(collection);
                    final long dur = System.currentTimeMillis() - start;
                    LOG.debug(new StringBuilder(64).append("Updating system/user flags of ").append(mailIds.length).append(
                        " messages took ").append(dur).append("msec."));
                } else {
                    executor.invokeAll(collection);
                }
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw MailExceptionCode.INTERRUPT_ERROR.create(e);
            }
        } else {
            final FullnameArgument fa = UnifiedInboxUtility.parseNestedFullname(fullName);
            MailAccess<?, ?> mailAccess = null;
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
    public void updateMessageColorLabel(final String fullName, final String[] mailIds, final int colorLabel) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(fullName)) {
            throw UnifiedInboxException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES.create(fullName);
        }
        if (UnifiedInboxAccess.KNOWN_FOLDERS.contains(fullName)) {
            // Parse mail IDs
            final Map<Integer, Map<String, List<String>>> parsed = UnifiedInboxUtility.parseMailIDs(mailIds);
            final int size = parsed.size();
            final Iterator<Map.Entry<Integer, Map<String, List<String>>>> iter = parsed.entrySet().iterator();
            // Collection of Callables
            final Collection<Task<Object>> collection = new ArrayList<Task<Object>>(size);
            for (int i = 0; i < size; i++) {
                final Map.Entry<Integer, Map<String, List<String>>> accountMapEntry = iter.next();
                collection.add(new LoggingCallable<Object>(session) {

                    @Override
                    public Object call() throws Exception {
                        final int accountId = accountMapEntry.getKey().intValue();
                        // Get account's mail access
                        MailAccess<?, ?> mailAccess = null;
                        try {
                            mailAccess = MailAccess.getInstance(getSession(), accountId);
                            mailAccess.connect();
                            final Map<String, List<String>> folderUIDMap = accountMapEntry.getValue();
                            final int innersize = folderUIDMap.size();
                            final Iterator<Map.Entry<String, List<String>>> inneriter = folderUIDMap.entrySet().iterator();
                            for (int j = 0; j < innersize; j++) {
                                final Map.Entry<String, List<String>> e = inneriter.next();
                                final String folder = e.getKey();
                                final List<String> uids = e.getValue();
                                // Update flags
                                mailAccess.getMessageStorage().updateMessageColorLabel(
                                    folder,
                                    uids.toArray(new String[uids.size()]),
                                    colorLabel);
                            }
                        } catch (final OXException e) {
                            getLogger().debug(e.getMessage(), e);
                            return null;
                        } finally {
                            closeSafe(mailAccess);
                        }
                        return null;
                    }
                });
            }
            final ThreadPoolService executor = ThreadPools.getThreadPool();
            try {
                // Invoke all and wait for being executed
                if (DEBUG) {
                    final long start = System.currentTimeMillis();
                    executor.invokeAll(collection);
                    final long dur = System.currentTimeMillis() - start;
                    LOG.debug(new StringBuilder(64).append("Updating color flag of ").append(mailIds.length).append(" messages took ").append(
                        dur).append("msec."));
                } else {
                    executor.invokeAll(collection);
                }
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw MailExceptionCode.INTERRUPT_ERROR.create(e);
            }
        } else {
            final FullnameArgument fa = UnifiedInboxUtility.parseNestedFullname(fullName);
            MailAccess<?, ?> mailAccess = null;
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
    public MailMessage saveDraft(final String draftFullName, final ComposedMailMessage composedMail) throws OXException {
        throw UnifiedInboxException.Code.DRAFTS_NOT_SUPPORTED.create();
    }

    /*-
     * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
     * +++++++++++++++++ Helper methods +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
     * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
     */

    private static void insertMessage(final String[] mailIds, final MailMessage[] toFill, final int accountId, final String folder, final MailMessage[] mails, final String uiFullname, final int undelegatedAccountId) {
        final UnifiedInboxUID helper = new UnifiedInboxUID();
        for (final MailMessage mail : mails) {
            if (null != mail) {
                final String lookFor = helper.setUID(accountId, folder, mail.getMailId()).toString();
                int pos = -1;
                for (int l = 0; l < mailIds.length && pos == -1; l++) {
                    if (lookFor.equals(mailIds[l])) {
                        pos = l;
                    }
                }
                if (pos != -1) {
                    final UnifiedMailMessage umm = new UnifiedMailMessage(mail, undelegatedAccountId);
                    toFill[pos] = umm;
                    umm.setMailId(mailIds[pos]);
                    umm.setFolder(uiFullname);
                    umm.setAccountId(accountId);
                }
            }
        }
    }

    private static class GetMessagesResult {

        public static final GetMessagesResult EMPTY_RESULT = new GetMessagesResult(-1, null, new MailMessage[0]);

        public final MailMessage[] mails;

        public final String folder;

        public final int accountId;

        public GetMessagesResult(final int accountId, final String folder, final MailMessage[] mails) {
            super();
            this.mails = mails;
            this.folder = folder;
            this.accountId = accountId;
        }

    }

}

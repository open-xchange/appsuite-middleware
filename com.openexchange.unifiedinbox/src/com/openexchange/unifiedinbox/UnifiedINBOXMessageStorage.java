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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
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
import com.openexchange.unifiedinbox.copy.UnifiedINBOXMessageCopier;
import com.openexchange.unifiedinbox.dataobjects.UnifiedMailMessage;
import com.openexchange.unifiedinbox.services.UnifiedINBOXServiceRegistry;
import com.openexchange.unifiedinbox.utility.LoggingCallable;
import com.openexchange.unifiedinbox.utility.TrackingCompletionService;
import com.openexchange.unifiedinbox.utility.UnifiedINBOXCompletionService;
import com.openexchange.unifiedinbox.utility.UnifiedINBOXUtility;
import com.openexchange.user.UserService;

/**
 * {@link UnifiedINBOXMessageStorage} - The Unified INBOX message storage implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UnifiedINBOXMessageStorage extends MailMessageStorage {

    private static final org.apache.commons.logging.Log LOG =
        com.openexchange.log.Log.valueOf(org.apache.commons.logging.LogFactory.getLog(UnifiedINBOXMessageStorage.class));

    private static final boolean DEBUG = LOG.isDebugEnabled();

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

    private Locale locale;

    private UnifiedINBOXMessageCopier copier;

    /**
     * Initializes a new {@link UnifiedINBOXMessageStorage}.
     *
     * @param access The Unified INBOX access
     * @param session The session providing needed user data
     * @throws OXException If context loading fails
     */
    public UnifiedINBOXMessageStorage(final UnifiedINBOXAccess access, final Session session) throws OXException {
        super();
        this.access = access;
        this.session = session;
        cid = session.getContextId();
        {
            final ContextService contextService = UnifiedINBOXServiceRegistry.getServiceRegistry().getService(ContextService.class, true);
            ctx = contextService.getContext(cid);
        }
        user = session.getUserId();
    }

    /**
     * Gets session user's locale
     *
     * @return The session user's locale
     * @throws OXException If retrieving user's locale fails
     */
    private Locale getLocale() throws OXException {
        if (null == locale) {
            final UserService userService = UnifiedINBOXServiceRegistry.getServiceRegistry().getService(UserService.class, true);
            locale = userService.getUser(session.getUserId(), ctx).getLocale();
        }
        return locale;
    }

    private UnifiedINBOXMessageCopier getCopier() {
        if (null == copier) {
            copier = new UnifiedINBOXMessageCopier(session, access);
        }
        return copier;
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
            throw UnifiedINBOXException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES.create(fullName);
        }
        if (UnifiedINBOXAccess.KNOWN_FOLDERS.contains(fullName)) {
            final MailMessage[] messages = new MailMessage[mailIds.length];
            // Parse mail IDs
            final Map<Integer, Map<String, List<String>>> parsed = UnifiedINBOXUtility.parseMailIDs(mailIds);
            final int size = parsed.size();
            // Create completion service for simultaneous access
            final Executor executor = ThreadPools.getThreadPool().getExecutor();
            final TrackingCompletionService<GetMessagesResult> completionService =
                new UnifiedINBOXCompletionService<GetMessagesResult>(executor);
            // Iterate parsed map and submit a task for each iteration
            final Iterator<Map.Entry<Integer, Map<String, List<String>>>> iter = parsed.entrySet().iterator();
            for (int i = 0; i < size; i++) {
                completionService.submit(new LoggingCallable<GetMessagesResult>(session) {

                    public GetMessagesResult call() throws OXException {
                        final Map.Entry<Integer, Map<String, List<String>>> accountMapEntry = iter.next();
                        final int accountId = accountMapEntry.getKey().intValue();
                        // Get account's mail access
                        final MailAccess<?, ?> mailAccess;
                        try {
                            mailAccess = MailAccess.getInstance(getSession(), accountId);
                            mailAccess.connect();
                        } catch (final OXException e) {
                            getLogger().debug(e.getMessage(), e);
                            return GetMessagesResult.EMPTY_RESULT;
                        }
                        try {
                            final Map<String, List<String>> folderUIDMap = accountMapEntry.getValue();
                            for (final Iterator<Map.Entry<String, List<String>>> inneriter = folderUIDMap.entrySet().iterator(); inneriter.hasNext();) {
                                final Map.Entry<String, List<String>> e = inneriter.next();
                                final String folder = e.getKey();
                                final List<String> uids = e.getValue();
                                try {
                                    return new GetMessagesResult(accountId, folder, mailAccess.getMessageStorage().getMessages(
                                        folder,
                                        uids.toArray(new String[uids.size()]),
                                        fields));
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
                        } finally {
                            mailAccess.close(true);
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
                        mailAccess.close(true);
                    }
                }
                 */
            }
            // Wait for completion of each submitted task
            try {
                for (int i = 0; i < size; i++) {
                    final GetMessagesResult result = completionService.take().get();
                    insertMessage(mailIds, messages, result.accountId, result.folder, result.mails, fullName);
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
        final FullnameArgument fa = UnifiedINBOXUtility.parseNestedFullname(fullName);
        final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session, fa.getAccountId());
        boolean close = false;
        try {
            mailAccess.connect();
            close = true;
            // Get messages
            final MailMessage[] mails = mailAccess.getMessageStorage().getMessages(fa.getFullname(), mailIds, fields);
            for (final MailMessage mail : mails) {
                if (null != mail) {
                    mail.setFolder(fullName);
                }
            }
            return mails;
        } finally {
            if (close) {
                mailAccess.close(true);
            }
        }
    }

    @Override
    public MailMessage getMessage(final String fullName, final String mailId, final boolean markSeen) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(fullName)) {
            throw UnifiedINBOXException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES.create(fullName);
        }
        if (UnifiedINBOXAccess.KNOWN_FOLDERS.contains(fullName)) {
            final UnifiedINBOXUID uid = new UnifiedINBOXUID(mailId);
            final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session, uid.getAccountId());
            boolean close = false;
            try {
                mailAccess.connect();
                close = true;
                MailMessage mail = mailAccess.getMessageStorage().getMessage(uid.getFullName(), uid.getId(), markSeen);
                if (null == mail) {
                    return null;
                }
                mail = new UnifiedMailMessage(mail);
                mail.loadContent();
                mail.setMailId(mailId);
                mail.setFolder(fullName);
                return mail;
            } finally {
                if (close) {
                    mailAccess.close(true);
                }
            }
        }
        final FullnameArgument fa = UnifiedINBOXUtility.parseNestedFullname(fullName);
        final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session, fa.getAccountId());
        boolean close = false;
        try {
            mailAccess.connect();
            close = true;
            // Get message
            final MailMessage mail = mailAccess.getMessageStorage().getMessage(fa.getFullname(), mailId, markSeen);
            if (null == mail) {
                return null;
            }
            mail.loadContent();
            mail.setFolder(fullName);
            return mail;
        } finally {
            if (close) {
                mailAccess.close(true);
            }
        }
    }

    @Override
    public MailMessage[] searchMessages(final String fullName, final IndexRange indexRange, final MailSortField sortField, final OrderDirection order, final SearchTerm<?> searchTerm, final MailField[] fields) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(fullName)) {
            throw UnifiedINBOXException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES.create(fullName);
        }
        if (UnifiedINBOXAccess.KNOWN_FOLDERS.contains(fullName)) {
            final List<MailAccount> accounts;
            {
                final MailAccountStorageService storageService =
                    UnifiedINBOXServiceRegistry.getServiceRegistry().getService(MailAccountStorageService.class, true);
                final MailAccount[] tmp = storageService.getUserMailAccounts(user, cid);
                accounts = new ArrayList<MailAccount>(tmp.length);
                for (final MailAccount mailAccount : tmp) {
                    if (access.getAccountId() != mailAccount.getId() && mailAccount.isUnifiedINBOXEnabled()) {
                        accounts.add(mailAccount);
                    }
                }
            }
            final MailFields mfs = new MailFields(fields);
            mfs.add(MailField.getField(sortField.getField()));
            final MailField[] checkedFields = mfs.toArray();
            // Create completion service for simultaneous access
            final int length = accounts.size();
            final Executor executor = ThreadPools.getThreadPool().getExecutor();
            final TrackingCompletionService<List<MailMessage>> completionService =
                new UnifiedINBOXCompletionService<List<MailMessage>>(executor);
            for (final MailAccount mailAccount : accounts) {
                completionService.submit(new LoggingCallable<List<MailMessage>>(session) {

                    public List<MailMessage> call() {
                        final int accountId = mailAccount.getId();
                        final MailAccess<?, ?> mailAccess;
                        try {
                            mailAccess = MailAccess.getInstance(getSession(), accountId);
                            mailAccess.connect();
                        } catch (final OXException e) {
                            getLogger().debug(e.getMessage(), e);
                            return Collections.emptyList();
                        }
                        String fn = null;
                        try {
                            // Get real full name
                            fn = UnifiedINBOXUtility.determineAccountFullname(mailAccess, fullName);
                            // Check if denoted account has such a default folder
                            if (fn == null) {
                                return Collections.emptyList();
                            }
                            // Get account's messages
                            final MailMessage[] accountMails =
                                mailAccess.getMessageStorage().searchMessages(fn, indexRange, MailSortField.RECEIVED_DATE, OrderDirection.DESC, searchTerm, checkedFields);
                            final List<MailMessage> messages = new ArrayList<MailMessage>(accountMails.length);
                            final UnifiedINBOXUID helper = new UnifiedINBOXUID();
                            for (final MailMessage accountMail : accountMails) {
                                final UnifiedMailMessage umm = new UnifiedMailMessage(accountMail);
                                umm.setMailId(helper.setUID(accountId, fn, accountMail.getMailId()).toString());
                                umm.setFolder(fullName);
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
                            mailAccess.close(true);
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
                    LOG.debug(new StringBuilder(64).append("Searching messages from folder \"").append(fullName).append("\" took ").append(
                        completionService.getDuration()).append("msec."));
                }
                // Sort them
                final MailMessageComparator c = new MailMessageComparator(sortField, OrderDirection.DESC.equals(order), getLocale());
                Collections.sort(messages, c);
                // Return as array
                return messages.toArray(new MailMessage[messages.size()]);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw MailExceptionCode.INTERRUPT_ERROR.create(e);
            } catch (final ExecutionException e) {
                throw ThreadPools.launderThrowable(e, OXException.class);
            }
        }
        final FullnameArgument fa = UnifiedINBOXUtility.parseNestedFullname(fullName);
        final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session, fa.getAccountId());
        boolean close = false;
        try {
            mailAccess.connect();
            close = true;
            // Get account's messages
            final MailMessage[] mails =
                mailAccess.getMessageStorage().searchMessages(fa.getFullname(), indexRange, sortField, order, searchTerm, fields);
            for (final MailMessage mail : mails) {
                mail.setFolder(fullName);
            }
            return mails;
        } finally {
            if (close) {
                mailAccess.close(true);
            }
        }
    }

    @Override
    public MailMessage[] getUnreadMessages(final String fullName, final MailSortField sortField, final OrderDirection order, final MailField[] fields, final int limit) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(fullName)) {
            throw UnifiedINBOXException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES.create(fullName);
        }
        if (UnifiedINBOXAccess.KNOWN_FOLDERS.contains(fullName)) {
            final MailAccount[] accounts;
            {
                final MailAccountStorageService storageService =
                    UnifiedINBOXServiceRegistry.getServiceRegistry().getService(MailAccountStorageService.class, true);
                final MailAccount[] tmp = storageService.getUserMailAccounts(user, cid);
                final List<MailAccount> l = new ArrayList<MailAccount>(tmp.length);
                for (final MailAccount mailAccount : tmp) {
                    if (access.getAccountId() != mailAccount.getId() && mailAccount.isUnifiedINBOXEnabled()) {
                        l.add(mailAccount);
                    }
                }
                accounts = l.toArray(new MailAccount[l.size()]);
            }
            final int length = accounts.length;
            final Executor executor = ThreadPools.getThreadPool().getExecutor();
            final TrackingCompletionService<List<MailMessage>> completionService =
                new UnifiedINBOXCompletionService<List<MailMessage>>(executor);
            for (final MailAccount mailAccount : accounts) {
                completionService.submit(new LoggingCallable<List<MailMessage>>(session) {

                    public List<MailMessage> call() throws Exception {
                        final MailAccess<?, ?> mailAccess;
                        try {
                            mailAccess = MailAccess.getInstance(getSession(), mailAccount.getId());
                            mailAccess.connect();
                        } catch (final OXException e) {
                            getLogger().debug(e.getMessage(), e);
                            return Collections.emptyList();
                        }
                        try {
                            // Get real fullname
                            final String fn = UnifiedINBOXUtility.determineAccountFullname(mailAccess, fullName);
                            // Check if denoted account has such a default folder
                            if (fn == null) {
                                return Collections.emptyList();
                            }
                            // Get account's unread messages
                            final MailMessage[] accountMails =
                                mailAccess.getMessageStorage().getUnreadMessages(fn, sortField, order, fields, limit);
                            final UnifiedINBOXUID helper = new UnifiedINBOXUID();
                            final List<MailMessage> messages = new ArrayList<MailMessage>(accountMails.length);
                            for (final MailMessage accountMail : accountMails) {
                                final UnifiedMailMessage umm = new UnifiedMailMessage(accountMail);
                                umm.setMailId(helper.setUID(mailAccount.getId(), fn, accountMail.getMailId()).toString());
                                umm.setFolder(fullName);
                                messages.add(umm);
                            }
                            return messages;
                        } finally {
                            mailAccess.close(true);
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
        final FullnameArgument fa = UnifiedINBOXUtility.parseNestedFullname(fullName);
        final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session, fa.getAccountId());
        boolean close = false;
        try {
            mailAccess.connect();
            close = true;
            // Get account's messages
            final MailMessage[] mails = mailAccess.getMessageStorage().getUnreadMessages(fa.getFullname(), sortField, order, fields, limit);
            for (final MailMessage mail : mails) {
                mail.setFolder(fullName);
            }
            return mails;
        } finally {
            if (close) {
                mailAccess.close(true);
            }
        }
    }

    @Override
    public void deleteMessages(final String fullName, final String[] mailIds, final boolean hardDelete) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(fullName)) {
            throw UnifiedINBOXException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES.create(fullName);
        }
        if (UnifiedINBOXAccess.KNOWN_FOLDERS.contains(fullName)) {
            // Parse mail IDs
            final Map<Integer, Map<String, List<String>>> parsed = UnifiedINBOXUtility.parseMailIDs(mailIds);
            final int size = parsed.size();
            final Iterator<Map.Entry<Integer, Map<String, List<String>>>> iter = parsed.entrySet().iterator();
            // Collection of Callables
            final Collection<Task<Object>> collection = new ArrayList<Task<Object>>(size);
            for (int i = 0; i < size; i++) {

                collection.add(new LoggingCallable<Object>(session) {

                    public Object call() throws Exception {
                        final Map.Entry<Integer, Map<String, List<String>>> accountMapEntry = iter.next();
                        final int accountId = accountMapEntry.getKey().intValue();
                        // Get account's mail access
                        final MailAccess<?, ?> mailAccess;
                        try {
                            mailAccess = MailAccess.getInstance(getSession(), accountId);
                            mailAccess.connect();
                        } catch (final OXException e) {
                            getLogger().debug(e.getMessage(), e);
                            return null;
                        }
                        try {
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
                            mailAccess.close(true);
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
            final FullnameArgument fa = UnifiedINBOXUtility.parseNestedFullname(fullName);
            final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session, fa.getAccountId());
            boolean close = false;
            try {
                mailAccess.connect();
                close = true;
                mailAccess.getMessageStorage().deleteMessages(fa.getFullname(), mailIds, hardDelete);
            } finally {
                if (close) {
                    mailAccess.close(true);
                }
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
        if (UnifiedINBOXAccess.KNOWN_FOLDERS.contains(destFullname)) {
            // TODO: Error code OR default account?!
            throw UnifiedINBOXException.Code.INVALID_DESTINATION_FOLDER.create(new Object[0]);
        }
        // Parse destination folder
        final FullnameArgument destFullnameArgument = UnifiedINBOXUtility.parseNestedFullname(destFullname);
        final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session, destFullnameArgument.getAccountId());
        mailAccess.connect();
        try {
            return mailAccess.getMessageStorage().appendMessages(destFullnameArgument.getFullname(), mailMessages);
        } finally {
            mailAccess.close(true);
        }
    }

    @Override
    public void updateMessageFlags(final String fullName, final String[] mailIds, final int flags, final boolean set) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(fullName)) {
            throw UnifiedINBOXException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES.create(fullName);
        }
        if (UnifiedINBOXAccess.KNOWN_FOLDERS.contains(fullName)) {
            // Parse mail IDs
            final Map<Integer, Map<String, List<String>>> parsed = UnifiedINBOXUtility.parseMailIDs(mailIds);
            final int size = parsed.size();
            final Iterator<Map.Entry<Integer, Map<String, List<String>>>> iter = parsed.entrySet().iterator();
            // Collection of Callables
            final Collection<Task<Object>> collection = new ArrayList<Task<Object>>(size);
            for (int i = 0; i < size; i++) {
                collection.add(new LoggingCallable<Object>(session) {

                    public Object call() throws Exception {
                        final Map.Entry<Integer, Map<String, List<String>>> accountMapEntry = iter.next();
                        final int accountId = accountMapEntry.getKey().intValue();
                        // Get account's mail access
                        final MailAccess<?, ?> mailAccess;
                        try {
                            mailAccess = MailAccess.getInstance(getSession(), accountId);
                            mailAccess.connect();
                        } catch (final OXException e) {
                            getLogger().debug(e.getMessage(), e);
                            return null;
                        }
                        try {
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
                            mailAccess.close(true);
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
            final FullnameArgument fa = UnifiedINBOXUtility.parseNestedFullname(fullName);
            final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session, fa.getAccountId());
            boolean close = false;
            try {
                mailAccess.connect();
                close = true;
                mailAccess.getMessageStorage().updateMessageFlags(fa.getFullname(), mailIds, flags, set);
            } finally {
                if (close) {
                    mailAccess.close(true);
                }
            }
        }
    }

    @Override
    public void updateMessageColorLabel(final String fullName, final String[] mailIds, final int colorLabel) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(fullName)) {
            throw UnifiedINBOXException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES.create(fullName);
        }
        if (UnifiedINBOXAccess.KNOWN_FOLDERS.contains(fullName)) {
            // Parse mail IDs
            final Map<Integer, Map<String, List<String>>> parsed = UnifiedINBOXUtility.parseMailIDs(mailIds);
            final int size = parsed.size();
            final Iterator<Map.Entry<Integer, Map<String, List<String>>>> iter = parsed.entrySet().iterator();
            // Collection of Callables
            final Collection<Task<Object>> collection = new ArrayList<Task<Object>>(size);
            for (int i = 0; i < size; i++) {
                collection.add(new LoggingCallable<Object>(session) {

                    public Object call() throws Exception {
                        final Map.Entry<Integer, Map<String, List<String>>> accountMapEntry = iter.next();
                        final int accountId = accountMapEntry.getKey().intValue();
                        // Get account's mail access
                        final MailAccess<?, ?> mailAccess;
                        try {
                            mailAccess = MailAccess.getInstance(getSession(), accountId);
                            mailAccess.connect();
                        } catch (final OXException e) {
                            getLogger().debug(e.getMessage(), e);
                            return null;
                        }
                        try {
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
                        } finally {
                            mailAccess.close(true);
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
            final FullnameArgument fa = UnifiedINBOXUtility.parseNestedFullname(fullName);
            final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session, fa.getAccountId());
            boolean close = false;
            try {
                mailAccess.connect();
                close = true;
                mailAccess.getMessageStorage().updateMessageColorLabel(fa.getFullname(), mailIds, colorLabel);
            } finally {
                if (close) {
                    mailAccess.close(true);
                }
            }
        }
    }

    @Override
    public MailMessage saveDraft(final String draftFullName, final ComposedMailMessage composedMail) throws OXException {
        throw UnifiedINBOXException.Code.DRAFTS_NOT_SUPPORTED.create();
    }

    /*-
     * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
     * +++++++++++++++++ Helper methods +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
     * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
     */

    private static void insertMessage(final String[] mailIds, final MailMessage[] toFill, final int accountId, final String folder, final MailMessage[] mails, final String uiFullname) {
        final UnifiedINBOXUID helper = new UnifiedINBOXUID();
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
                    final UnifiedMailMessage umm = new UnifiedMailMessage(mail);
                    toFill[pos] = umm;
                    umm.setMailId(mailIds[pos]);
                    umm.setFolder(uiFullname);
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

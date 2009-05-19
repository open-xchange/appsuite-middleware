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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import com.openexchange.context.ContextService;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.ldap.UserException;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailFields;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailMessageStorage;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.mail.utils.MailMessageComparator;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountException;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.server.ServiceException;
import com.openexchange.session.Session;
import com.openexchange.unifiedinbox.services.UnifiedINBOXServiceRegistry;
import com.openexchange.unifiedinbox.utility.LoggingCallable;
import com.openexchange.unifiedinbox.utility.UnifiedINBOXThreadFactory;
import com.openexchange.unifiedinbox.utility.UnifiedINBOXUtility;
import com.openexchange.user.UserService;

/**
 * {@link UnifiedINBOXMessageStorage} - The Unified INBOX message storage implementation.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UnifiedINBOXMessageStorage extends MailMessageStorage {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(UnifiedINBOXMessageStorage.class);

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

    /**
     * Gets session user's locale
     * 
     * @return The session user's locale
     * @throws UnifiedINBOXException If retrieving user's locale fails
     */
    private Locale getLocale() throws UnifiedINBOXException {
        if (null == locale) {
            try {
                final UserService userService = UnifiedINBOXServiceRegistry.getServiceRegistry().getService(UserService.class, true);
                locale = userService.getUser(session.getUserId(), ctx).getLocale();
            } catch (final ServiceException e) {
                throw new UnifiedINBOXException(e);
            } catch (final UserException e) {
                throw new UnifiedINBOXException(e);
            }
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
            if (fieldSet.contains(MailField.FULL) || fieldSet.contains(MailField.BODY)) {
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
        if (UnifiedINBOXAccess.KNOWN_FOLDERS.contains(fullname)) {
            final MailMessage[] messages = new MailMessage[mailIds.length];
            // Parse mail IDs
            final Map<Integer, Map<String, List<String>>> parsed = UnifiedINBOXUtility.parseMailIDs(mailIds);
            final int size = parsed.size();
            // Create completion service for simultaneous access
            final ExecutorService executor = Executors.newFixedThreadPool(size, new UnifiedINBOXThreadFactory());
            final CompletionService<Object> completionService = new ExecutorCompletionService<Object>(executor);
            // Iterate parsed map and submit a task for each iteration
            final Iterator<Map.Entry<Integer, Map<String, List<String>>>> iter = parsed.entrySet().iterator();
            for (int i = 0; i < size; i++) {
                completionService.submit(new LoggingCallable<Object>(session) {

                    public MailMessage[] call() throws Exception {
                        final Map.Entry<Integer, Map<String, List<String>>> accountMapEntry = iter.next();
                        final int accountId = accountMapEntry.getKey().intValue();
                        // Get account's mail access
                        final MailAccess<?, ?> mailAccess = MailAccess.getInstance(getSession(), accountId);
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
                                insertMessage(mailIds, messages, accountId, folder, mails, fullname);
                            }
                        } finally {
                            if (close) {
                                mailAccess.close(true);
                            }
                        }
                        // Return dummy object
                        return EMPTY_RETVAL;
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
                    completionService.take().get();
                }
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new MailException(MailException.Code.INTERRUPT_ERROR, e);
            } catch (final ExecutionException e) {
                final Throwable t = e.getCause();
                if (MailException.class.isAssignableFrom(t.getClass())) {
                    throw (MailException) t;
                } else if (t instanceof RuntimeException) {
                    throw (RuntimeException) t;
                } else if (t instanceof Error) {
                    throw (Error) t;
                } else {
                    throw new IllegalStateException("Not unchecked", t);
                }
            } finally {
                try {
                    executor.shutdownNow();
                    executor.awaitTermination(10000L, TimeUnit.MILLISECONDS);
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            // Return properly filled array
            return messages;
        }
        final FullnameArgument fa = UnifiedINBOXUtility.parseNestedFullname(fullname);
        final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session, fa.getAccountId());
        boolean close = false;
        try {
            mailAccess.connect();
            close = true;
            // Get messages
            final MailMessage[] mails = mailAccess.getMessageStorage().getMessages(fa.getFullname(), mailIds, fields);
            for (int i = 0; i < mails.length; i++) {
                mails[i].setFolder(fullname);
            }
            return mails;
        } finally {
            if (close) {
                mailAccess.close(true);
            }
        }
    }

    @Override
    public MailMessage getMessage(final String fullname, final String mailId, final boolean markSeen) throws MailException {
        if (DEFAULT_FOLDER_ID.equals(fullname)) {
            throw new UnifiedINBOXException(UnifiedINBOXException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, fullname);
        }
        if (UnifiedINBOXAccess.KNOWN_FOLDERS.contains(fullname)) {
            final UnifiedINBOXUID uid = new UnifiedINBOXUID(mailId);
            final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session, uid.getAccountId());
            boolean close = false;
            try {
                mailAccess.connect();
                close = true;
                final MailMessage mail = mailAccess.getMessageStorage().getMessage(uid.getFullname(), uid.getId(), markSeen);
                mail.setMailId(mailId);
                mail.setFolder(fullname);
                mail.loadContent();
                return mail;
            } finally {
                if (close) {
                    mailAccess.close(true);
                }
            }
        }
        final FullnameArgument fa = UnifiedINBOXUtility.parseNestedFullname(fullname);
        final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session, fa.getAccountId());
        boolean close = false;
        try {
            mailAccess.connect();
            close = true;
            // Get message
            final MailMessage mail = mailAccess.getMessageStorage().getMessage(fa.getFullname(), mailId, markSeen);
            mail.loadContent();
            mail.setFolder(fullname);
            return mail;
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
        if (UnifiedINBOXAccess.KNOWN_FOLDERS.contains(fullname)) {
            final MailAccount[] accounts;
            try {
                final MailAccountStorageService storageService = UnifiedINBOXServiceRegistry.getServiceRegistry().getService(
                    MailAccountStorageService.class,
                    true);
                final MailAccount[] tmp = storageService.getUserMailAccounts(user, cid);
                final List<MailAccount> l = new ArrayList<MailAccount>(tmp.length);
                for (int i = 0; i < tmp.length; i++) {
                    final MailAccount mailAccount = tmp[i];
                    if (access.getAccountId() != mailAccount.getId() && mailAccount.isUnifiedINBOXEnabled()) {
                        l.add(mailAccount);
                    }
                }
                accounts = l.toArray(new MailAccount[l.size()]);
            } catch (final ServiceException e) {
                throw new UnifiedINBOXException(e);
            } catch (final MailAccountException e) {
                throw new UnifiedINBOXException(e);
            }
            // Create completion service for simultaneous access
            final int length = accounts.length;
            final ExecutorService executor = Executors.newFixedThreadPool(length, new UnifiedINBOXThreadFactory());
            final CompletionService<List<MailMessage>> completionService = new ExecutorCompletionService<List<MailMessage>>(executor);
            for (final MailAccount mailAccount : accounts) {
                completionService.submit(new LoggingCallable<List<MailMessage>>(session) {

                    public List<MailMessage> call() throws Exception {
                        final MailAccess<?, ?> mailAccess;
                        try {
                            mailAccess = MailAccess.getInstance(getSession(), mailAccount.getId());
                            mailAccess.connect();
                        } catch (final MailException e) {
                            getLogger().error(e.getMessage(), e);
                            return Collections.emptyList();
                        }
                        try {
                            // Get real fullname
                            final String fn = UnifiedINBOXUtility.determineAccountFullname(mailAccess, fullname);
                            // Check if denoted account has such a default folder
                            if (fn == null) {
                                return Collections.emptyList();
                            }
                            // Get account's messages
                            final MailMessage[] accountMails = mailAccess.getMessageStorage().searchMessages(
                                fn,
                                indexRange,
                                sortField,
                                order,
                                searchTerm,
                                fields);
                            final List<MailMessage> messages = new ArrayList<MailMessage>(accountMails.length);
                            final UnifiedINBOXUID helper = new UnifiedINBOXUID();
                            for (int i = 0; i < accountMails.length; i++) {
                                final MailMessage accountMail = accountMails[i];
                                accountMail.setMailId(helper.setUID(mailAccount.getId(), fn, accountMail.getMailId()).toString());
                                accountMail.setFolder(fullname);
                                messages.add(accountMail);
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
                // Sort them
                Collections.sort(messages, new MailMessageComparator(sortField, OrderDirection.DESC.equals(order), getLocale()));
                // Return as array
                return messages.toArray(new MailMessage[messages.size()]);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new MailException(MailException.Code.INTERRUPT_ERROR, e);
            } catch (final ExecutionException e) {
                final Throwable t = e.getCause();
                if (MailException.class.isAssignableFrom(t.getClass())) {
                    throw (MailException) t;
                } else if (t instanceof RuntimeException) {
                    throw (RuntimeException) t;
                } else if (t instanceof Error) {
                    throw (Error) t;
                } else {
                    throw new IllegalStateException("Not unchecked", t);
                }
            } finally {
                try {
                    executor.shutdownNow();
                    executor.awaitTermination(10000L, TimeUnit.MILLISECONDS);
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        final FullnameArgument fa = UnifiedINBOXUtility.parseNestedFullname(fullname);
        final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session, fa.getAccountId());
        boolean close = false;
        try {
            mailAccess.connect();
            close = true;
            // Get account's messages
            final MailMessage[] mails = mailAccess.getMessageStorage().searchMessages(
                fa.getFullname(),
                indexRange,
                sortField,
                order,
                searchTerm,
                fields);
            for (int i = 0; i < mails.length; i++) {
                mails[i].setFolder(fullname);
            }
            return mails;
        } finally {
            if (close) {
                mailAccess.close(true);
            }
        }
    }

    @Override
    public MailMessage[] getUnreadMessages(final String fullname, final MailSortField sortField, final OrderDirection order, final MailField[] fields, final int limit) throws MailException {
        if (DEFAULT_FOLDER_ID.equals(fullname)) {
            throw new UnifiedINBOXException(UnifiedINBOXException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, fullname);
        }
        if (UnifiedINBOXAccess.KNOWN_FOLDERS.contains(fullname)) {
            final MailAccount[] accounts;
            try {
                final MailAccountStorageService storageService = UnifiedINBOXServiceRegistry.getServiceRegistry().getService(
                    MailAccountStorageService.class,
                    true);
                final MailAccount[] tmp = storageService.getUserMailAccounts(user, cid);
                final List<MailAccount> l = new ArrayList<MailAccount>(tmp.length);
                for (int i = 0; i < tmp.length; i++) {
                    final MailAccount mailAccount = tmp[i];
                    if (access.getAccountId() != mailAccount.getId() && mailAccount.isUnifiedINBOXEnabled()) {
                        l.add(mailAccount);
                    }
                }
                accounts = l.toArray(new MailAccount[l.size()]);
            } catch (final ServiceException e) {
                throw new UnifiedINBOXException(e);
            } catch (final MailAccountException e) {
                throw new UnifiedINBOXException(e);
            }
            final int length = accounts.length;
            final ExecutorService executor = Executors.newFixedThreadPool(length, new UnifiedINBOXThreadFactory());
            final CompletionService<List<MailMessage>> completionService = new ExecutorCompletionService<List<MailMessage>>(executor);
            for (final MailAccount mailAccount : accounts) {
                completionService.submit(new LoggingCallable<List<MailMessage>>(session) {

                    public List<MailMessage> call() throws Exception {
                        final MailAccess<?, ?> mailAccess;
                        try {
                            mailAccess = MailAccess.getInstance(getSession(), mailAccount.getId());
                            mailAccess.connect();
                        } catch (final MailException e) {
                            getLogger().error(e.getMessage(), e);
                            return Collections.emptyList();
                        }
                        try {
                            // Get real fullname
                            final String fn = UnifiedINBOXUtility.determineAccountFullname(mailAccess, fullname);
                            // Check if denoted account has such a default folder
                            if (fn == null) {
                                return Collections.emptyList();
                            }
                            // Get account's unread messages
                            final MailMessage[] accountMails = mailAccess.getMessageStorage().getUnreadMessages(
                                fn,
                                sortField,
                                order,
                                fields,
                                limit);
                            final UnifiedINBOXUID helper = new UnifiedINBOXUID();
                            final List<MailMessage> messages = new ArrayList<MailMessage>(accountMails.length);
                            for (int i = 0; i < accountMails.length; i++) {
                                final MailMessage accountMail = accountMails[i];
                                accountMail.setMailId(helper.setUID(mailAccount.getId(), fn, accountMail.getMailId()).toString());
                                accountMail.setFolder(fullname);
                                messages.add(accountMail);
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
                // Sort them
                Collections.sort(messages, new MailMessageComparator(sortField, OrderDirection.DESC.equals(order), getLocale()));
                // Return as array
                return messages.toArray(new MailMessage[messages.size()]);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new MailException(MailException.Code.INTERRUPT_ERROR, e);
            } catch (final ExecutionException e) {
                final Throwable t = e.getCause();
                if (MailException.class.isAssignableFrom(t.getClass())) {
                    throw (MailException) t;
                } else if (t instanceof RuntimeException) {
                    throw (RuntimeException) t;
                } else if (t instanceof Error) {
                    throw (Error) t;
                } else {
                    throw new IllegalStateException("Not unchecked", t);
                }
            } finally {
                try {
                    executor.shutdownNow();
                    executor.awaitTermination(10000L, TimeUnit.MILLISECONDS);
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        final FullnameArgument fa = UnifiedINBOXUtility.parseNestedFullname(fullname);
        final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session, fa.getAccountId());
        boolean close = false;
        try {
            mailAccess.connect();
            close = true;
            // Get account's messages
            final MailMessage[] mails = mailAccess.getMessageStorage().getUnreadMessages(fa.getFullname(), sortField, order, fields, limit);
            for (int i = 0; i < mails.length; i++) {
                mails[i].setFolder(fullname);
            }
            return mails;
        } finally {
            if (close) {
                mailAccess.close(true);
            }
        }
    }

    @Override
    public void deleteMessages(final String fullname, final String[] mailIds, final boolean hardDelete) throws MailException {
        if (DEFAULT_FOLDER_ID.equals(fullname)) {
            throw new UnifiedINBOXException(UnifiedINBOXException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, fullname);
        }
        if (UnifiedINBOXAccess.KNOWN_FOLDERS.contains(fullname)) {
            // Parse mail IDs
            final Map<Integer, Map<String, List<String>>> parsed = UnifiedINBOXUtility.parseMailIDs(mailIds);
            final int size = parsed.size();
            final Iterator<Map.Entry<Integer, Map<String, List<String>>>> iter = parsed.entrySet().iterator();
            // Collection of Callables
            final Collection<Callable<Object>> collection = new ArrayList<Callable<Object>>(size);
            for (int i = 0; i < size; i++) {

                collection.add(new LoggingCallable<Object>(session) {

                    public Object call() throws Exception {
                        final Map.Entry<Integer, Map<String, List<String>>> accountMapEntry = iter.next();
                        final int accountId = accountMapEntry.getKey().intValue();
                        // Get account's mail access
                        final MailAccess<?, ?> mailAccess = MailAccess.getInstance(getSession(), accountId);
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
                        return null;
                    }
                });
            }
            final ExecutorService executor = Executors.newFixedThreadPool(size, new UnifiedINBOXThreadFactory());
            try {
                // Invoke all and wait for being executed
                executor.invokeAll(collection);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new MailException(MailException.Code.INTERRUPT_ERROR, e);
            } finally {
                try {
                    executor.shutdownNow();
                    executor.awaitTermination(10000L, TimeUnit.MILLISECONDS);
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            return;
        }
        final FullnameArgument fa = UnifiedINBOXUtility.parseNestedFullname(fullname);
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

    @Override
    public String[] copyMessages(final String sourceFolder, final String destFolder, final String[] mailIds, final boolean fast) throws MailException {
        return getCopier().doCopy(sourceFolder, destFolder, mailIds, fast, false);
    }

    @Override
    public String[] moveMessages(final String sourceFolder, final String destFolder, final String[] mailIds, final boolean fast) throws MailException {
        return getCopier().doCopy(sourceFolder, destFolder, mailIds, fast, true);
    }

    @Override
    public String[] appendMessages(final String destFullname, final MailMessage[] mailMessages) throws MailException {
        if (UnifiedINBOXAccess.KNOWN_FOLDERS.contains(destFullname)) {
            // TODO: Error code OR default account?!
            throw new IllegalArgumentException("Invalid destination folder. Don't know where to append the mails.");
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
    public void updateMessageFlags(final String fullname, final String[] mailIds, final int flags, final boolean set) throws MailException {
        if (DEFAULT_FOLDER_ID.equals(fullname)) {
            throw new UnifiedINBOXException(UnifiedINBOXException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, fullname);
        }
        if (UnifiedINBOXAccess.KNOWN_FOLDERS.contains(fullname)) {
            // Parse mail IDs
            final Map<Integer, Map<String, List<String>>> parsed = UnifiedINBOXUtility.parseMailIDs(mailIds);
            final int size = parsed.size();
            final Iterator<Map.Entry<Integer, Map<String, List<String>>>> iter = parsed.entrySet().iterator();
            // Collection of Callables
            final Collection<Callable<Object>> collection = new ArrayList<Callable<Object>>(size);
            for (int i = 0; i < size; i++) {
                collection.add(new LoggingCallable<Object>(session) {

                    public Object call() throws Exception {
                        final Map.Entry<Integer, Map<String, List<String>>> accountMapEntry = iter.next();
                        final int accountId = accountMapEntry.getKey().intValue();
                        // Get account's mail access
                        final MailAccess<?, ?> mailAccess = MailAccess.getInstance(getSession(), accountId);
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
                        return null;
                    }
                });
            }
            final ExecutorService executor = Executors.newFixedThreadPool(size, new UnifiedINBOXThreadFactory());
            try {
                // Invoke all and wait for being executed
                executor.invokeAll(collection);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new MailException(MailException.Code.INTERRUPT_ERROR, e);
            } finally {
                try {
                    executor.shutdownNow();
                    executor.awaitTermination(10000L, TimeUnit.MILLISECONDS);
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            return;
        }
        final FullnameArgument fa = UnifiedINBOXUtility.parseNestedFullname(fullname);
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

    @Override
    public void updateMessageColorLabel(final String fullname, final String[] mailIds, final int colorLabel) throws MailException {
        if (DEFAULT_FOLDER_ID.equals(fullname)) {
            throw new UnifiedINBOXException(UnifiedINBOXException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, fullname);
        }
        if (UnifiedINBOXAccess.KNOWN_FOLDERS.contains(fullname)) {
            // Parse mail IDs
            final Map<Integer, Map<String, List<String>>> parsed = UnifiedINBOXUtility.parseMailIDs(mailIds);
            final int size = parsed.size();
            final Iterator<Map.Entry<Integer, Map<String, List<String>>>> iter = parsed.entrySet().iterator();
            // Collection of Callables
            final Collection<Callable<Object>> collection = new ArrayList<Callable<Object>>(size);
            for (int i = 0; i < size; i++) {
                collection.add(new LoggingCallable<Object>(session) {

                    public Object call() throws Exception {
                        final Map.Entry<Integer, Map<String, List<String>>> accountMapEntry = iter.next();
                        final int accountId = accountMapEntry.getKey().intValue();
                        // Get account's mail access
                        final MailAccess<?, ?> mailAccess = MailAccess.getInstance(getSession(), accountId);
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
                                mailAccess.getMessageStorage().updateMessageColorLabel(
                                    folder,
                                    uids.toArray(new String[uids.size()]),
                                    colorLabel);
                            }
                        } finally {
                            if (close) {
                                mailAccess.close(true);
                            }
                        }
                        return null;
                    }
                });
            }
            final ExecutorService executor = Executors.newFixedThreadPool(size, new UnifiedINBOXThreadFactory());
            try {
                // Invoke all and wait for being executed
                executor.invokeAll(collection);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new MailException(MailException.Code.INTERRUPT_ERROR, e);
            } finally {
                try {
                    executor.shutdownNow();
                    executor.awaitTermination(10000L, TimeUnit.MILLISECONDS);
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            return;
        }
        final FullnameArgument fa = UnifiedINBOXUtility.parseNestedFullname(fullname);
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

    @Override
    public MailMessage saveDraft(final String draftFullname, final ComposedMailMessage composedMail) throws MailException {
        throw new UnifiedINBOXException(UnifiedINBOXException.Code.DRAFTS_NOT_SUPPORTED);
    }

    /*-
     * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
     * +++++++++++++++++ Helper methods +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
     * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
     */

    private static void insertMessage(final String[] mailIds, final MailMessage[] toFill, final int accountId, final String folder, final MailMessage[] mails, final String uiFullname) {
        final UnifiedINBOXUID helper = new UnifiedINBOXUID();
        for (int k = 0; k < mails.length; k++) {
            final String lookFor = helper.setUID(accountId, folder, mails[k].getMailId()).toString();
            int pos = -1;
            for (int l = 0; l < mailIds.length && pos == -1; l++) {
                final String mailId = mailIds[l];
                if (lookFor.equals(mailId)) {
                    pos = l;
                }
            }
            if (pos != -1) {
                toFill[pos] = mails[k];
                mails[k].setMailId(mailIds[pos]);
                mails[k].setFolder(uiFullname);
            }
        }
    }

}

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
import static com.openexchange.unifiedinbox.utility.UnifiedInboxUtility.generateNestedFullName;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.java.Collators;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailFolderStorageEnhanced;
import com.openexchange.mail.api.IMailFolderStorageEnhanced2;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailFolderStorage;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailFolder.DefaultFolderType;
import com.openexchange.mail.dataobjects.MailFolderDescription;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.search.FlagTerm;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.session.Session;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.unifiedinbox.converters.UnifiedInboxFolderConverter;
import com.openexchange.unifiedinbox.services.Services;
import com.openexchange.unifiedinbox.utility.LoggingCallable;
import com.openexchange.unifiedinbox.utility.TrackingCompletionService;
import com.openexchange.unifiedinbox.utility.UnifiedInboxCompletionService;
import com.openexchange.unifiedinbox.utility.UnifiedInboxUtility;
import com.openexchange.user.UserService;

/**
 * {@link UnifiedInboxFolderStorage} - The Unified Mail folder storage implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UnifiedInboxFolderStorage extends MailFolderStorage implements IMailFolderStorageEnhanced2 {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(UnifiedInboxFolderStorage.class);

    // private final UnifiedINBOXAccess access;

    private final int unifiedInboxId;
    final Session session;
    private final Context ctx;
    private Locale locale;

    /**
     * Initializes a new {@link UnifiedInboxFolderStorage}
     *
     * @param access The Unified Mail access
     * @param session The session providing needed user data
     * @throws OXException If context loading fails
     */
    public UnifiedInboxFolderStorage(final UnifiedInboxAccess access, final Session session) throws OXException {
        super();
        // this.access = access;
        unifiedInboxId = access.getAccountId();
        this.session = session;
        ctx = ContextStorage.getStorageContext(session.getContextId());
    }

    private List<MailAccount> getAccounts() throws OXException {
        final MailAccountStorageService srv = Services.getService(MailAccountStorageService.class);
        final MailAccount[] tmp = srv.getUserMailAccounts(session.getUserId(), session.getContextId());
        final List<MailAccount> accounts = new ArrayList<MailAccount>(tmp.length);
        final int thisAccountId = unifiedInboxId;
        for (final MailAccount mailAccount : tmp) {
            if (thisAccountId != mailAccount.getId() && mailAccount.isUnifiedINBOXEnabled()) {
                accounts.add(mailAccount);
            }
        }
        return accounts;
    }

    @Override
    public void expungeFolder(final String fullName) throws OXException {
        expungeFolder(fullName, false);
    }

    @Override
    public void expungeFolder(final String fullName, final boolean hardDelete) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(fullName)) {
            return;
        }
        if (UnifiedInboxAccess.KNOWN_FOLDERS.contains(fullName)) {
            final List<MailAccount> accounts = getAccounts();
            final int length = accounts.size();
            final Executor executor = ThreadPools.getThreadPool().getExecutor();
            final TrackingCompletionService<Void> completionService = new UnifiedInboxCompletionService<Void>(executor);
            for (final MailAccount mailAccount : accounts) {
                completionService.submit(new LoggingCallable<Void>(session) {

                    @Override
                    public Void call() throws Exception {
                        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
                        try {
                            final int accountId = mailAccount.getId();
                            mailAccess = MailAccess.getInstance(getSession(), accountId);
                            mailAccess.connect();
                            final String fn = UnifiedInboxUtility.determineAccountFullName(mailAccess, fullName);
                            // Check if denoted account has such a default folder
                            if (fn == null) {
                                return null;
                            }
                            final IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
                            if (messageStorage instanceof IMailFolderStorageEnhanced) {
                                ((IMailFolderStorageEnhanced) messageStorage).expungeFolder(fn, hardDelete);
                                return null;
                            }
                            final MailField[] fields = new MailField[] { MailField.ID };
                            final FlagTerm term = new FlagTerm(MailMessage.FLAG_DELETED, true);
                            final MailMessage[] messages = messageStorage.searchMessages(fn, IndexRange.NULL, MailSortField.RECEIVED_DATE, OrderDirection.ASC, term, fields);
                            final List<String> mailIds = new ArrayList<String>(messages.length);
                            for (int i = 0; i < messages.length; i++) {
                                final MailMessage mailMessage = messages[i];
                                if (null != mailMessage) {
                                    mailIds.add(mailMessage.getMailId());
                                }
                            }
                            if (hardDelete) {
                                messageStorage.deleteMessages(fn, mailIds.toArray(new String[0]), true);
                            } else {
                                final String trashFolder = mailAccess.getFolderStorage().getTrashFolder();
                                if (fn.equals(trashFolder)) {
                                    // Also perform hard-delete when compacting trash folder
                                    messageStorage.deleteMessages(fn, mailIds.toArray(new String[0]), true);
                                } else {
                                    messageStorage.moveMessages(fn, trashFolder, mailIds.toArray(new String[0]), true);
                                }
                            }
                            return null;
                        } catch (final OXException e) {
                            getLogger().debug("", e);
                            return null;
                        } finally {
                            closeSafe(mailAccess);
                        }
                    }
                });
            }
            // Wait for completion of each submitted task
            try {
                for (int i = 0; i < length; i++) {
                    completionService.take().get();
                }
                LOG.debug("Expunging folder \"{}\" took {}msec.", fullName, completionService.getDuration());
                // Return
                return;
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw MailExceptionCode.INTERRUPT_ERROR.create(e);
            } catch (final ExecutionException e) {
                throw ThreadPools.launderThrowable(e, OXException.class);
            }
        }
        final FullnameArgument fa = UnifiedInboxUtility.parseNestedFullName(fullName);
        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
        try {
            final int accountId = fa.getAccountId();
            mailAccess = MailAccess.getInstance(session, accountId);
            mailAccess.connect();
            // Get account's messages
            final IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
            final String fn = fa.getFullname();
            if (messageStorage instanceof IMailFolderStorageEnhanced) {
                ((IMailFolderStorageEnhanced) messageStorage).expungeFolder(fn, hardDelete);
                return;
            }
            final MailField[] fields = new MailField[] { MailField.ID };
            final FlagTerm term = new FlagTerm(MailMessage.FLAG_DELETED, true);
            final MailMessage[] messages = messageStorage.searchMessages(fn, IndexRange.NULL, MailSortField.RECEIVED_DATE, OrderDirection.ASC, term, fields);
            final List<String> mailIds = new ArrayList<String>(messages.length);
            for (int i = 0; i < messages.length; i++) {
                final MailMessage mailMessage = messages[i];
                if (null != mailMessage) {
                    mailIds.add(mailMessage.getMailId());
                }
            }
            if (hardDelete) {
                messageStorage.deleteMessages(fn, mailIds.toArray(new String[0]), true);
            } else {
                final String trashFolder = mailAccess.getFolderStorage().getTrashFolder();
                if (fn.equals(trashFolder)) {
                    // Also perform hard-delete when compacting trash folder
                    messageStorage.deleteMessages(fn, mailIds.toArray(new String[0]), true);
                } else {
                    messageStorage.moveMessages(fn, trashFolder, mailIds.toArray(new String[0]), true);
                }
            }
        } finally {
            closeSafe(mailAccess);
        }
    }

    @Override
    public int[] getTotalAndUnreadCounter(final String fullName) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(fullName)) {
            return new int[] {0,0};
        }
        if (UnifiedInboxAccess.KNOWN_FOLDERS.contains(fullName)) {
            final List<MailAccount> accounts = getAccounts();
            final int length = accounts.size();
            final Executor executor = ThreadPools.getThreadPool().getExecutor();
            final TrackingCompletionService<int[]> completionService = new UnifiedInboxCompletionService<int[]>(executor);
            for (final MailAccount mailAccount : accounts) {
                completionService.submit(new LoggingCallable<int[]>(session) {

                    @Override
                    public int[] call() throws Exception {
                        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
                        try {
                            final int accountId = mailAccount.getId();
                            mailAccess = MailAccess.getInstance(getSession(), accountId);
                            mailAccess.connect();
                            final String fn = UnifiedInboxUtility.determineAccountFullName(mailAccess, fullName);
                            // Check if denoted account has such a default folder
                            if (fn == null) {
                                return new int[] {0,0};
                            }
                            IMailFolderStorage folderStorage = mailAccess.getFolderStorage();
                            if (folderStorage instanceof IMailFolderStorageEnhanced2) {
                                return ((IMailFolderStorageEnhanced2) folderStorage).getTotalAndUnreadCounter(fn);
                            }
                            if (folderStorage instanceof IMailFolderStorageEnhanced) {
                                final IMailFolderStorageEnhanced enhanced = (IMailFolderStorageEnhanced) folderStorage;
                                return new int[] { enhanced.getTotalCounter(fn), enhanced.getUnreadCounter(fn) };
                            }
                            final IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
                            final MailField[] fields = new MailField[] { MailField.ID };
                            final int count = messageStorage.searchMessages(fn, null, MailSortField.RECEIVED_DATE, OrderDirection.ASC, null, fields).length;
                            final MailMessage[] unreadMessages = messageStorage.getUnreadMessages(fn, MailSortField.RECEIVED_DATE, OrderDirection.ASC, fields, -1);
                            final int unreadCount = unreadMessages.length;
                            return new int[] { count, unreadCount };
                        } catch (final OXException e) {
                            getLogger().debug("", e);
                            return new int[] {0,0};
                        } finally {
                            closeSafe(mailAccess);
                        }
                    }
                });
            }
            // Wait for completion of each submitted task
            try {
                int count = 0;
                int unreadCount = 0;
                for (int i = 0; i < length; i++) {
                    final int[] arr = completionService.take().get();
                    count += arr[0];
                    unreadCount += arr[1];
                }
                LOG.debug("Retrieving total message count from folder \"{}\" took {}msec.", fullName, completionService.getDuration());
                // Return
                return new int[] { count, unreadCount };
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw MailExceptionCode.INTERRUPT_ERROR.create(e);
            } catch (final ExecutionException e) {
                throw ThreadPools.launderThrowable(e, OXException.class);
            }
        }
        /*
         * Subfolder
         */
        final FullnameArgument fa = UnifiedInboxUtility.parseNestedFullName(fullName);
        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
        try {
            final int accountId = fa.getAccountId();
            mailAccess = MailAccess.getInstance(session, accountId);
            mailAccess.connect();
            // Get account's messages
            IMailFolderStorage folderStorage = mailAccess.getFolderStorage();
            if (folderStorage instanceof IMailFolderStorageEnhanced2) {
                return ((IMailFolderStorageEnhanced2) folderStorage).getTotalAndUnreadCounter(fa.getFullname());
            }
            if (folderStorage instanceof IMailFolderStorageEnhanced) {
                final IMailFolderStorageEnhanced enhanced = (IMailFolderStorageEnhanced) folderStorage;
                final int count = enhanced.getTotalCounter(fa.getFullname());
                final int unreadCount = enhanced.getUnreadCounter(fa.getFullname());
                return new int[] { count, unreadCount };
            }
            final MailField[] fields = new MailField[] { MailField.ID };
            final IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
            final int count = messageStorage.searchMessages(fa.getFullname(), null, MailSortField.RECEIVED_DATE, OrderDirection.ASC, null, fields).length;
            final MailMessage[] unreadMessages = messageStorage.getUnreadMessages(fa.getFullname(), MailSortField.RECEIVED_DATE, OrderDirection.ASC, fields, -1);
            final int unreadCount = unreadMessages.length;
            return new int[] { count, unreadCount };
        } finally {
            closeSafe(mailAccess);
        }
    }

    @Override
    public int getTotalCounter(final String fullName) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(fullName)) {
            return 0;
        }
        if (UnifiedInboxAccess.KNOWN_FOLDERS.contains(fullName)) {
            final List<MailAccount> accounts = getAccounts();
            final int length = accounts.size();
            final Executor executor = ThreadPools.getThreadPool().getExecutor();
            final TrackingCompletionService<Integer> completionService = new UnifiedInboxCompletionService<Integer>(executor);
            for (final MailAccount mailAccount : accounts) {
                completionService.submit(new LoggingCallable<Integer>(session) {

                    @Override
                    public Integer call() throws Exception {
                        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
                        try {
                            final int accountId = mailAccount.getId();
                            mailAccess = MailAccess.getInstance(getSession(), accountId);
                            mailAccess.connect();
                            final String fn = UnifiedInboxUtility.determineAccountFullName(mailAccess, fullName);
                            // Check if denoted account has such a default folder
                            if (fn == null) {
                                return Integer.valueOf(0);
                            }
                            final IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
                            if (messageStorage instanceof IMailFolderStorageEnhanced) {
                                return Integer.valueOf(((IMailFolderStorageEnhanced) messageStorage).getTotalCounter(fn));
                            }
                            final MailField[] fields = new MailField[] { MailField.ID };
                            final int count = messageStorage.searchMessages(fn, null, MailSortField.RECEIVED_DATE, OrderDirection.ASC, null, fields).length;
                            return Integer.valueOf(count);
                        } catch (final OXException e) {
                            getLogger().debug("", e);
                            return Integer.valueOf(0);
                        } finally {
                            closeSafe(mailAccess);
                        }
                    }
                });
            }
            // Wait for completion of each submitted task
            try {
                int count = 0;
                for (int i = 0; i < length; i++) {
                    count += (completionService.take().get()).intValue();
                }
                LOG.debug("Retrieving total message count from folder \"{}\" took {}msec.", fullName, completionService.getDuration());
                // Return
                return count;
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw MailExceptionCode.INTERRUPT_ERROR.create(e);
            } catch (final ExecutionException e) {
                throw ThreadPools.launderThrowable(e, OXException.class);
            }
        }
        final FullnameArgument fa = UnifiedInboxUtility.parseNestedFullName(fullName);
        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
        try {
            final int accountId = fa.getAccountId();
            mailAccess = MailAccess.getInstance(session, accountId);
            mailAccess.connect();
            // Get account's messages
            final IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
            if (messageStorage instanceof IMailFolderStorageEnhanced) {
                return ((IMailFolderStorageEnhanced) messageStorage).getTotalCounter(fa.getFullname());
            }
            final MailField[] fields = new MailField[] { MailField.ID };
            final int count = messageStorage.searchMessages(fa.getFullname(), null, MailSortField.RECEIVED_DATE, OrderDirection.ASC, null, fields).length;
            return count;
        } finally {
            closeSafe(mailAccess);
        }
    }

    @Override
    public int getNewCounter(final String fullName) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(fullName)) {
            return 0;
        }
        if (UnifiedInboxAccess.KNOWN_FOLDERS.contains(fullName)) {
            final List<MailAccount> accounts = getAccounts();
            final int length = accounts.size();
            final Executor executor = ThreadPools.getThreadPool().getExecutor();
            final TrackingCompletionService<Integer> completionService = new UnifiedInboxCompletionService<Integer>(executor);
            for (final MailAccount mailAccount : accounts) {
                completionService.submit(new LoggingCallable<Integer>(session) {

                    @Override
                    public Integer call() throws Exception {
                        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
                        try {
                            final int accountId = mailAccount.getId();
                            mailAccess = MailAccess.getInstance(getSession(), accountId);
                            mailAccess.connect();
                            final String fn = UnifiedInboxUtility.determineAccountFullName(mailAccess, fullName);
                            // Check if denoted account has such a default folder
                            if (fn == null) {
                                return Integer.valueOf(0);
                            }
                            final IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
                            if (messageStorage instanceof IMailFolderStorageEnhanced) {
                                return Integer.valueOf(((IMailFolderStorageEnhanced) messageStorage).getNewCounter(fn));
                            }
                            final MailField[] fields = new MailField[] { MailField.ID };
                            final SearchTerm<?> term = new FlagTerm(MailMessage.FLAG_RECENT, true);
                            final int count = messageStorage.searchMessages(fn, null, MailSortField.RECEIVED_DATE, OrderDirection.ASC, term, fields).length;
                            return Integer.valueOf(count);
                        } catch (final OXException e) {
                            getLogger().debug("", e);
                            return Integer.valueOf(0);
                        } finally {
                            closeSafe(mailAccess);
                        }
                    }
                });
            }
            // Wait for completion of each submitted task
            try {
                int count = 0;
                for (int i = 0; i < length; i++) {
                    count += (completionService.take().get()).intValue();
                }
                LOG.debug("Retrieving new message count from folder \"{}\" took {}msec.", fullName, completionService.getDuration());

                // Return
                return count;
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw MailExceptionCode.INTERRUPT_ERROR.create(e);
            } catch (final ExecutionException e) {
                throw ThreadPools.launderThrowable(e, OXException.class);
            }
        }
        final FullnameArgument fa = UnifiedInboxUtility.parseNestedFullName(fullName);
        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
        try {
            final int accountId = fa.getAccountId();
            mailAccess = MailAccess.getInstance(session, accountId);
            mailAccess.connect();
            // Get account's messages
            final IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
            if (messageStorage instanceof IMailFolderStorageEnhanced) {
                return ((IMailFolderStorageEnhanced) messageStorage).getUnreadCounter(fa.getFullname());
            }
            final MailField[] fields = new MailField[] { MailField.ID };
            final SearchTerm<?> term = new FlagTerm(MailMessage.FLAG_RECENT, true);
            final int count = messageStorage.searchMessages(fa.getFullname(), null, MailSortField.RECEIVED_DATE, OrderDirection.ASC, term, fields).length;
            return count;
        } finally {
            closeSafe(mailAccess);
        }
    }

    @Override
    public int getUnreadCounter(final String fullName) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(fullName)) {
            return 0;
        }
        if (UnifiedInboxAccess.KNOWN_FOLDERS.contains(fullName)) {
            final List<MailAccount> accounts = getAccounts();
            final int length = accounts.size();
            final Executor executor = ThreadPools.getThreadPool().getExecutor();
            final TrackingCompletionService<Integer> completionService = new UnifiedInboxCompletionService<Integer>(executor);
            for (final MailAccount mailAccount : accounts) {
                completionService.submit(new LoggingCallable<Integer>(session) {

                    @Override
                    public Integer call() throws Exception {
                        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
                        try {
                            final int accountId = mailAccount.getId();
                            mailAccess = MailAccess.getInstance(getSession(), accountId);
                            mailAccess.connect();
                            final String fn = UnifiedInboxUtility.determineAccountFullName(mailAccess, fullName);
                            // Check if denoted account has such a default folder
                            if (fn == null) {
                                return Integer.valueOf(0);
                            }
                            final IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
                            if (messageStorage instanceof IMailFolderStorageEnhanced) {
                                return Integer.valueOf(((IMailFolderStorageEnhanced) messageStorage).getUnreadCounter(fn));
                            }
                            final MailField[] fields = new MailField[] { MailField.ID };
                            final MailMessage[] unreadMessages =
                                messageStorage.getUnreadMessages(fn, MailSortField.RECEIVED_DATE, OrderDirection.ASC, fields, -1);
                            return Integer.valueOf(unreadMessages.length);
                        } catch (final OXException e) {
                            getLogger().debug("", e);
                            return Integer.valueOf(0);
                        } finally {
                            closeSafe(mailAccess);
                        }
                    }
                });
            }
            // Wait for completion of each submitted task
            try {
                int count = 0;
                for (int i = 0; i < length; i++) {
                    count += (completionService.take().get()).intValue();
                }
                LOG.debug("Retrieving unread message count from folder \"{}\" took {}msec.", fullName, completionService.getDuration());

                // Return
                return count;
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw MailExceptionCode.INTERRUPT_ERROR.create(e);
            } catch (final ExecutionException e) {
                throw ThreadPools.launderThrowable(e, OXException.class);
            }
        }
        final FullnameArgument fa = UnifiedInboxUtility.parseNestedFullName(fullName);
        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
        try {
            final int accountId = fa.getAccountId();
            mailAccess = MailAccess.getInstance(session, accountId);
            mailAccess.connect();
            // Get account's messages
            final IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
            if (messageStorage instanceof IMailFolderStorageEnhanced) {
                return ((IMailFolderStorageEnhanced) messageStorage).getUnreadCounter(fa.getFullname());
            }
            final MailField[] fields = new MailField[] { MailField.ID };
            final MailMessage[] unreadMessages =
                messageStorage.getUnreadMessages(fa.getFullname(), MailSortField.RECEIVED_DATE, OrderDirection.ASC, fields, -1);
            return unreadMessages.length;
        } finally {
            closeSafe(mailAccess);
        }
    }

    @Override
    public boolean exists(final String fullname) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(fullname)) {
            return true;
        }
        if (UnifiedInboxAccess.KNOWN_FOLDERS.contains(fullname)) {
            return true;
        }
        final String fn = startsWithKnownFullname(fullname);
        if (null == fn) {
            return false;
        }
        final FullnameArgument fa = MailFolderUtility.prepareMailFolderParam(fn);
        if (!isMailAccountEnabled(fa.getAccountId())) {
            return false;
        }
        MailAccess<?, ?> mailAccess = null;
        try {
            mailAccess = MailAccess.getInstance(session, fa.getAccountId());
            mailAccess.connect();
            return mailAccess.getFolderStorage().exists(fa.getFullname());
        } finally {
            if (null != mailAccess) {
                mailAccess.close(true);
            }
        }
    }

    @Override
    public MailFolder getFolder(final String fullname) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(fullname)) {
            return UnifiedInboxFolderConverter.getRootFolder();
        }
        if (UnifiedInboxAccess.KNOWN_FOLDERS.contains(fullname)) {
            return UnifiedInboxFolderConverter.getUnifiedINBOXFolder(unifiedInboxId, session, fullname, getLocalizedName(fullname));
        }
        final String fn = startsWithKnownFullname(fullname);
        if (null != fn) {
            final FullnameArgument fa = MailFolderUtility.prepareMailFolderParam(fn);
            final int nestedAccountId = fa.getAccountId();
            if (!isMailAccountEnabled(nestedAccountId)) {
                throw UnifiedInboxException.Code.FOLDER_NOT_FOUND.create(fullname);
            }
            MailAccess<?, ?> mailAccess = null;
            try {
                mailAccess = MailAccess.getInstance(session, nestedAccountId);
                mailAccess.connect();
                final String nestedFullname = fa.getFullname();
                final MailFolder mailFolder = mailAccess.getFolderStorage().getFolder(nestedFullname);
                final String startingKnownFullname = getStartingKnownFullname(fullname);
                mailFolder.setFullname(generateNestedFullName(unifiedInboxId, startingKnownFullname, nestedAccountId, nestedFullname));
                mailFolder.setParentFullname(generateNestedFullName(unifiedInboxId, startingKnownFullname, nestedAccountId, null));
                mailFolder.setName(getMailAccountName(nestedAccountId));
                mailFolder.setSubfolders(false);
                mailFolder.setSubscribedSubfolders(false);
                mailFolder.setDefaultFolder(false);
                mailFolder.setDefaultFolderType(DefaultFolderType.NONE);
                UnifiedInboxFolderConverter.setPermissions(mailFolder, session.getUserId());
                return mailFolder;
            } finally {
                if (null != mailAccess) {
                    mailAccess.close(true);
                }
            }
        }
        throw UnifiedInboxException.Code.FOLDER_NOT_FOUND.create(fullname);
    }

    @Override
    public MailFolder[] getSubfolders(final String parentFullname, final boolean all) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(parentFullname)) {
            return getRootSubfolders(false);
        }
        if (UnifiedInboxAccess.KNOWN_FOLDERS.contains(parentFullname)) {
            return getKnownFolderSubfolders(parentFullname);
        }
        final String fn = startsWithKnownFullname(parentFullname);
        if (null != fn) {
            final FullnameArgument fa = MailFolderUtility.prepareMailFolderParam(fn);
            final int nestedAccountId = fa.getAccountId();
            if (!isMailAccountEnabled(nestedAccountId)) {
                throw UnifiedInboxException.Code.FOLDER_NOT_FOUND.create(parentFullname);
            }
            /*
             * Return empty array since mapped default folders have no subfolders in Unified Mail account
             */
            return EMPTY_PATH;
        }
        throw UnifiedInboxException.Code.FOLDER_NOT_FOUND.create(parentFullname);
    }

    static final String[] FULLNAMES = {
        UnifiedInboxAccess.INBOX, UnifiedInboxAccess.DRAFTS, UnifiedInboxAccess.SENT, UnifiedInboxAccess.SPAM, UnifiedInboxAccess.TRASH };

    private MailFolder[] getRootSubfolders(final boolean byAccount) throws OXException {
        if (byAccount) {
            return getRootSubfoldersByAccount();
        }
        return getRootSubfoldersByFolder();
    }

    private MailFolder[] getRootSubfoldersByAccount() throws OXException {
        // Determine accounts
        final int unifiedINBOXAccountId = unifiedInboxId;
        final MailAccount[] accounts;
        {
            final MailAccountStorageService storageService = Services.getService(MailAccountStorageService.class);
            final MailAccount[] arr = storageService.getUserMailAccounts(session.getUserId(), session.getContextId());
            final List<MailAccount> l = new ArrayList<MailAccount>(arr.length);
            for (final MailAccount mailAccount : arr) {
                if (unifiedINBOXAccountId != mailAccount.getId() && mailAccount.isUnifiedINBOXEnabled()) {
                    l.add(mailAccount);
                }
            }
            accounts = l.toArray(new MailAccount[l.size()]);
        }
        final int nAccounts = accounts.length;
        final Executor executor = ThreadPools.getThreadPool().getExecutor();
        final TrackingCompletionService<int[][]> completionService = new UnifiedInboxCompletionService<int[][]>(executor);
        // Create a task for each account
        for (int i = 0; i < nAccounts; i++) {
            final int accountId = accounts[i].getId();
            completionService.submit(new LoggingCallable<int[][]>(session, unifiedINBOXAccountId) {

                @Override
                public int[][] call() throws Exception {
                    return UnifiedInboxFolderConverter.getAccountDefaultFolders(accountId, session, FULLNAMES);
                }
            });
        }
        // Wait for completion
        final List<int[][]> list = new ArrayList<int[][]>(nAccounts);
        try {
            int completed = 0;
            while (completed < nAccounts) {
                // No timeout
                list.add(completionService.take().get());
                completed++;
            }
            LOG.debug("Retrieving root's subfolders took {}msec.", completionService.getDuration());
            // Merge them
            final String[] names = new String[5];
            names[0] = getLocalizedName(UnifiedInboxAccess.INBOX);
            names[1] = getLocalizedName(UnifiedInboxAccess.DRAFTS);
            names[2] = getLocalizedName(UnifiedInboxAccess.SENT);
            names[3] = getLocalizedName(UnifiedInboxAccess.SPAM);
            names[4] = getLocalizedName(UnifiedInboxAccess.TRASH);
            return UnifiedInboxFolderConverter.mergeAccountDefaultFolders(list, FULLNAMES, names);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw MailExceptionCode.INTERRUPT_ERROR.create(e);
        } catch (final ExecutionException e) {
            throw ThreadPools.launderThrowable(e, OXException.class);
        }
    }

    private MailFolder[] getRootSubfoldersByFolder() throws OXException {
        final MailFolder[] retval = new MailFolder[5];
        final Executor executor = ThreadPools.getThreadPool().getExecutor();
        final TrackingCompletionService<Retval> completionService = new UnifiedInboxCompletionService<Retval>(executor);
        // Init names
        final String[][] names = new String[5][];
        names[0] = new String[] { UnifiedInboxAccess.INBOX, getLocalizedName(UnifiedInboxAccess.INBOX) };
        names[1] = new String[] { UnifiedInboxAccess.DRAFTS, getLocalizedName(UnifiedInboxAccess.DRAFTS) };
        names[2] = new String[] { UnifiedInboxAccess.SENT, getLocalizedName(UnifiedInboxAccess.SENT) };
        names[3] = new String[] { UnifiedInboxAccess.SPAM, getLocalizedName(UnifiedInboxAccess.SPAM) };
        names[4] = new String[] { UnifiedInboxAccess.TRASH, getLocalizedName(UnifiedInboxAccess.TRASH) };
        // Create a Callable for each known subfolder
        for (int i = 0; i < retval.length; i++) {
            final int index = i;
            final String[] tmp = names[index];
            completionService.submit(new LoggingCallable<Retval>(session, unifiedInboxId) {

                @Override
                public Retval call() throws Exception {
                    return new Retval(UnifiedInboxFolderConverter.getUnifiedINBOXFolder(
                        getAccountId(),
                        getSession(),
                        tmp[0],
                        tmp[1]), index);
                }
            });
        }
        // Wait for completion of each submitted task
        try {
            int completed = 0;
            while (completed < retval.length) {
                // No timeout
                final Future<Retval> future = completionService.poll(UnifiedInboxUtility.getMaxRunningMillis(), TimeUnit.MILLISECONDS);
                completed++;
                if (null != future) {
                    final Retval r = future.get();
                    if (null != r) {
                        retval[r.index] = r.mailFolder;
                    }
                }
            }
            LOG.debug("Retrieving root's subfolders took {}msec.", completionService.getDuration());
            // Return them
            return retval;
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw MailExceptionCode.INTERRUPT_ERROR.create(e);
        } catch (final ExecutionException e) {
            throw ThreadPools.launderThrowable(e, OXException.class);
        }
    }

    private MailFolder[] getKnownFolderSubfolders(final String parentFullName) throws OXException {
        final MailAccount[] accounts;
        {
            final MailAccountStorageService storageService = Services.getService(MailAccountStorageService.class);
            final MailAccount[] tmp = storageService.getUserMailAccounts(session.getUserId(), session.getContextId());
            final List<MailAccount> l = new ArrayList<MailAccount>(tmp.length);
            for (final MailAccount mailAccount : tmp) {
                if (unifiedInboxId != mailAccount.getId() && mailAccount.isUnifiedINBOXEnabled()) {
                    l.add(mailAccount);
                }
            }
            accounts = l.toArray(new MailAccount[l.size()]);
        }
        final Session s = session;
        final int unifiedInboxAccountId = unifiedInboxId;
        final int length = accounts.length;
        final Executor executor = ThreadPools.getThreadPool().getExecutor();
        final TrackingCompletionService<MailFolder> completionService = new UnifiedInboxCompletionService<MailFolder>(executor);
        for (final MailAccount mailAccount : accounts) {
            completionService.submit(new LoggingCallable<MailFolder>(session) {

                @Override
                public MailFolder call() throws Exception {
                    MailAccess<?, ?> mailAccess = null;
                    try {
                        mailAccess = MailAccess.getInstance(getSession(), mailAccount.getId());
                        mailAccess.connect();
                        final String accountFullname = UnifiedInboxUtility.determineAccountFullName(mailAccess, parentFullName);
                        // Check if account fullname is not null
                        if (null == accountFullname) {
                            return null;
                        }
                        // Get mail folder
                        final MailFolder mailFolder = mailAccess.getFolderStorage().getFolder(accountFullname);
                        mailFolder.setFullname(generateNestedFullName(unifiedInboxAccountId, parentFullName, mailAccount.getId(), mailFolder.getFullname()));
                        mailFolder.setParentFullname(generateNestedFullName(unifiedInboxAccountId, parentFullName, mailAccount.getId(), null));
                        UnifiedInboxFolderConverter.setPermissions(mailFolder, s.getUserId());
                        mailFolder.setSubfolders(false);
                        mailFolder.setSubscribedSubfolders(false);
                        mailFolder.setName(mailAccount.getName());
                        mailFolder.setDefaultFolder(false);
                        mailFolder.setDefaultFolderType(DefaultFolderType.NONE);
                        return mailFolder;
                    } catch (final OXException e) {
                        getLogger().debug("", e);
                        return null;
                    } finally {
                        if (null != mailAccess) {
                            mailAccess.close(true);
                        }
                    }
                }
            });
        }
        // Wait for completion of each submitted task
        try {
            final List<MailFolder> folders = new ArrayList<MailFolder>(length << 2);
            for (int i = 0; i < length; i++) {
                final MailFolder f = completionService.take().get();
                if (null != f) {
                    folders.add(f);
                }
            }
            LOG.debug("Retrieving subfolders of \"{}\" took {}msec.", parentFullName, completionService.getDuration());
            // Sort them
            Collections.sort(folders, new MailFolderNameComparator(getLocale()));
            // Return as array
            return folders.toArray(new MailFolder[folders.size()]);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw MailExceptionCode.INTERRUPT_ERROR.create(e);
        } catch (final ExecutionException e) {
            throw ThreadPools.launderThrowable(e, OXException.class);
        }
    }

    @Override
    public MailFolder getRootFolder() throws OXException {
        return UnifiedInboxFolderConverter.getRootFolder();
    }

    @Override
    public void checkDefaultFolders() throws OXException {
        // Nothing to do
    }

    @Override
    public String createFolder(final MailFolderDescription toCreate) throws OXException {
        throw UnifiedInboxException.Code.FOLDER_CREATION_FAILED.create();
    }

    @Override
    public String moveFolder(final String fullName, final String newFullname) throws OXException {
        throw UnifiedInboxException.Code.MOVE_DENIED.create();
    }

    @Override
    public String updateFolder(final String fullName, final MailFolderDescription toUpdate) throws OXException {
        throw UnifiedInboxException.Code.UPDATE_DENIED.create();
    }

    @Override
    public String deleteFolder(final String fullName, final boolean hardDelete) throws OXException {
        throw UnifiedInboxException.Code.DELETE_DENIED.create();
    }

    @Override
    public void clearFolder(final String fullName, final boolean hardDelete) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(fullName)) {
            throw MailExceptionCode.NO_ROOT_FOLDER_MODIFY_DELETE.create();
        }
        if (UnifiedInboxAccess.KNOWN_FOLDERS.contains(fullName)) {
            /*-
             *
            if (!UnifiedInboxAccess.TRASH.equals(fullName)) {
                // Shall we support clear() for all folders ? ? ?
                throw UnifiedInboxException.Code.CLEAR_NOT_SUPPORTED.create();
            }
             *
             */

            // Clear Unified Mail folder
            final List<MailAccount> accounts = getAccounts();
            final int length = accounts.size();
            final Executor executor = ThreadPools.getThreadPool().getExecutor();
            final TrackingCompletionService<Void> completionService = new UnifiedInboxCompletionService<Void>(executor);
            for (final MailAccount mailAccount : accounts) {
                completionService.submit(new LoggingCallable<Void>(session) {

                    @Override
                    public Void call() {
                        final int accountId = mailAccount.getId();
                        MailAccess<?, ?> mailAccess = null;
                        String fn = null;
                        try {
                            mailAccess = MailAccess.getInstance(getSession(), accountId);
                            mailAccess.connect();
                            // Get real full name
                            fn = UnifiedInboxUtility.determineAccountFullName(mailAccess, fullName);
                            // Check if denoted account has such a default folder
                            if (fn == null) {
                                return null;
                            }
                            // Clear folder
                            mailAccess.getFolderStorage().clearFolder(fn, hardDelete);
                        } catch (final OXException e) {
                            getLogger().warn("Couldn't clear folder \"{}\" from server \"{}\" for login \"{}\".", (null == fn ? "<unknown>" : fn), mailAccount.getMailServer(), mailAccount.getLogin(), e);
                        } catch (final RuntimeException e) {
                            getLogger().warn("Couldn't clear folder \"{}\" from server \"{}\" for login \"{}\".", (null == fn ? "<unknown>" : fn), mailAccount.getMailServer(), mailAccount.getLogin(), e);
                        } finally {
                            closeSafe(mailAccess);
                        }
                        return null;
                    }
                });
            }
            // Wait for completion of each submitted task
            try {
                for (int i = 0; i < length; i++) {
                    completionService.take().get();
                }
                LOG.debug("Clearing messages from folder \"{}\" took {}msec.", fullName, Long.toString(completionService.getDuration()));
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw MailExceptionCode.INTERRUPT_ERROR.create(e);
            } catch (final ExecutionException e) {
                throw ThreadPools.launderThrowable(e, OXException.class);
            }
            // Leave...
            return;
        }

        // Clear subfolder
        final FullnameArgument fa = UnifiedInboxUtility.parseNestedFullName(fullName);
        MailAccess<?, ?> mailAccess = null;
        try {
            mailAccess = MailAccess.getInstance(session, fa.getAccountId());
            mailAccess.connect();
            // Clear folder
            mailAccess.getFolderStorage().clearFolder(fullName, hardDelete);
        } finally {
            closeSafe(mailAccess);
        }
    }

    @Override
    public MailFolder[] getPath2DefaultFolder(final String fullName) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(fullName)) {
            return EMPTY_PATH;
        }
        if (!UnifiedInboxAccess.KNOWN_FOLDERS.contains(fullName)) {
            throw UnifiedInboxException.Code.FOLDER_NOT_FOUND.create(fullName);
        }
        return new MailFolder[] {
            UnifiedInboxFolderConverter.getUnifiedINBOXFolder(unifiedInboxId, session, fullName, getLocalizedName(fullName)),
            UnifiedInboxFolderConverter.getRootFolder() };
    }

    @Override
    public String getConfirmedHamFolder() throws OXException {
        return null;
    }

    @Override
    public String getConfirmedSpamFolder() throws OXException {
        return null;
    }

    @Override
    public String getDraftsFolder() throws OXException {
        return UnifiedInboxAccess.DRAFTS;
    }

    @Override
    public String getSentFolder() throws OXException {
        return UnifiedInboxAccess.SENT;
    }

    @Override
    public String getSpamFolder() throws OXException {
        return UnifiedInboxAccess.SPAM;
    }

    @Override
    public String getTrashFolder() throws OXException {
        return UnifiedInboxAccess.TRASH;
    }

    @Override
    public void releaseResources() throws UnifiedInboxException {
        // Nothing to release
    }

    @Override
    public com.openexchange.mail.Quota[] getQuotas(final String folder, final com.openexchange.mail.Quota.Type[] types) throws OXException {
        return com.openexchange.mail.Quota.getUnlimitedQuotas(types);
    }

    /**
     * Gets session user's locale.
     *
     * @return The session user's locale
     * @throws OXException If retrieving user's locale fails
     */
    private Locale getLocale() throws OXException {
        if (null == locale) {
            final UserService userService = Services.getService(UserService.class);
            locale = userService.getUser(session.getUserId(), ctx).getLocale();
        }
        return locale;
    }

    private String getLocalizedName(final String fullname) throws OXException {
        if (UnifiedInboxAccess.INBOX.equals(fullname)) {
            return I18n.getInstance().translate(getLocale(), NameStrings.NAME_INBOX);
        }
        if (UnifiedInboxAccess.DRAFTS.equals(fullname)) {
            return I18n.getInstance().translate(getLocale(), NameStrings.NAME_DRAFTS);
        }
        if (UnifiedInboxAccess.SENT.equals(fullname)) {
            return I18n.getInstance().translate(getLocale(), NameStrings.NAME_SENT);
        }
        if (UnifiedInboxAccess.SPAM.equals(fullname)) {
            return I18n.getInstance().translate(getLocale(), NameStrings.NAME_SPAM);
        }
        if (UnifiedInboxAccess.TRASH.equals(fullname)) {
            return I18n.getInstance().translate(getLocale(), NameStrings.NAME_TRASH);
        }
        throw UnifiedInboxException.Code.UNKNOWN_DEFAULT_FOLDER_INDEX.create(fullname);
    }

    private static String startsWithKnownFullname(final String fullname) {
        for (final String knownFullname : UnifiedInboxAccess.KNOWN_FOLDERS) {
            if (fullname.startsWith(knownFullname)) {
                // Cut off starting known fullname AND separator character
                return fullname.substring(knownFullname.length() + 1);
            }
        }
        return null;
    }

    private static String getStartingKnownFullname(final String fullname) {
        for (final String knownFullname : UnifiedInboxAccess.KNOWN_FOLDERS) {
            if (fullname.startsWith(knownFullname)) {
                return knownFullname;
            }
        }
        return null;
    }

    private String getMailAccountName(final int accountId) throws OXException {
        final MailAccountStorageService storageService = Services.getService(MailAccountStorageService.class);
        return storageService.getMailAccount(accountId, session.getUserId(), session.getContextId()).getName();
    }

    private boolean isMailAccountEnabled(final int accountId) throws OXException {
        final MailAccountStorageService storageService = Services.getService(MailAccountStorageService.class);
        return storageService.getMailAccount(accountId, session.getUserId(), session.getContextId()).isUnifiedINBOXEnabled();
    }

    private static class MailFolderNameComparator implements Comparator<MailFolder> {

        private final Collator collator;

        public MailFolderNameComparator(final Locale locale) {
            super();
            collator = Collators.getSecondaryInstance(locale);
        }

        @Override
        public int compare(final MailFolder o1, final MailFolder o2) {
            final String name1 = o1.getName();
            final String name2 = o2.getName();
            return collator.compare(name1 == null ? "" : name1, name2 == null ? "" : name2);
        }

    }

    protected static void closeSafe(final MailAccess<?, ?> mailAccess) {
        if (null == mailAccess) {
            return;
        }
        mailAccess.close(true);
    }

    /**
     * Tiny helper class.
     */
    private static final class Retval {

        final MailFolder mailFolder;

        final int index;

        public Retval(final MailFolder mailFolder, final int index) {
            super();
            this.index = index;
            this.mailFolder = mailFolder;
        }
    } // End of Retval

}

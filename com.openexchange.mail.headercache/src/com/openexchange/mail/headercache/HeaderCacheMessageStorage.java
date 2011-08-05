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

package com.openexchange.mail.headercache;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import com.openexchange.exception.OXException;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailFields;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.headercache.database.DatabaseAccess;
import com.openexchange.mail.headercache.database.DatabaseAccess.FolderSetterApplier;
import com.openexchange.mail.headercache.database.DatabaseAccess.SetterApplier;
import com.openexchange.mail.headercache.services.HeaderCacheServiceRegistry;
import com.openexchange.mail.headercache.sync.SynchronizerCallable;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.session.Session;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.threadpool.behavior.CallerRunsBehavior;

/**
 * {@link HeaderCacheMessageStorage} - The header cache message storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class HeaderCacheMessageStorage implements IMailMessageStorage {

    /**
     * All mail fields covered by header data.
     */
    private static final MailFields ALL_COVERED_MAIL_FIELDS = Constants.getCoveredMailFields();

    /**
     * The map to control concurrent invocations if synchronize method.
     */
    private static final ConcurrentMap<Key, Future<Object>> SYNCHRONIZER_MAP = new ConcurrentHashMap<Key, Future<Object>>();

    /**
     * Fields to request mail ID and folder fullname.
     */
    private static final MailField[] FIELDS_ID = { MailField.ID, MailField.FOLDER_ID };

    /**
     * The mail access.
     */
    private final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess;

    /**
     * The delegate message storage.
     */
    private final IMailMessageStorage delegatee;

    /**
     * The session.
     */
    private final Session session;

    /**
     * The user identifier.
     */
    private final int user;

    /**
     * The context identifier.
     */
    private final int contextId;

    /**
     * The account identifier.
     */
    private final int accountId;

    /**
     * Initializes a new {@link HeaderCacheMessageStorage}.
     *
     * @param session The session
     * @param mailAccess The mail access
     * @throws OXException If initialization fails
     */
    public HeaderCacheMessageStorage(final Session session, final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) throws OXException {
        super();
        this.session = session;
        this.mailAccess = mailAccess;
        this.delegatee = mailAccess.getMessageStorage();
        user = session.getUserId();
        contextId = session.getContextId();
        accountId = mailAccess.getAccountId();
    }

    @Override
    public String[] appendMessages(final String destFolder, final MailMessage[] msgs) throws OXException {
        final String[] ids = delegatee.appendMessages(destFolder, msgs);
        synchronizeFolder(destFolder, true, true);
        return ids;
    }

    @Override
    public String[] copyMessages(final String sourceFolder, final String destFolder, final String[] mailIds, final boolean fast) throws OXException {
        final String[] ids = delegatee.copyMessages(sourceFolder, destFolder, mailIds, fast);
        synchronizeFolder(destFolder, true, true);
        return ids;
    }

    @Override
    public void deleteMessages(final String folder, final String[] mailIds, final boolean hardDelete) throws OXException {
        delegatee.deleteMessages(folder, mailIds, hardDelete);
        synchronizeFolder(folder, true, true);
    }

    @Override
    public MailMessage[] getAllMessages(final String folder, final IndexRange indexRange, final MailSortField sortField, final OrderDirection order, final MailField[] fields) throws OXException {
        return searchMessages(folder, indexRange, sortField, order, null, fields);
    }

    @Override
    public MailPart getAttachment(final String folder, final String mailId, final String sequenceId) throws OXException {
        return delegatee.getAttachment(folder, mailId, sequenceId);
    }

    @Override
    public MailMessage[] getDeletedMessages(final String folder, final MailField[] fields) throws OXException {
        return delegatee.getDeletedMessages(folder, fields);
    }

    @Override
    public MailPart getImageAttachment(final String folder, final String mailId, final String contentId) throws OXException {
        return delegatee.getImageAttachment(folder, mailId, contentId);
    }

    @Override
    public MailMessage getMessage(final String folder, final String mailId, final boolean markSeen) throws OXException {
        /*
         * Synchronize since we access the message storage
         */
        synchronize(folder, false);
        /*
         * Return fully filled message
         */
        return delegatee.getMessage(folder, mailId, markSeen);
    }

    @Override
    public MailMessage[] getMessages(final String folder, final String[] mailIds, final MailField[] fields) throws OXException {
        /*
         * Synchronize since we access the message storage
         */
        synchronize(folder, false);
        /*
         * Check fields
         */
        final MailFields originalMailFields = new MailFields(fields);
        /*
         * Check if all requested fields can be served from header cache
         */
        final MailFields requestMailFields = new MailFields(originalMailFields);
        final boolean containsAnyCoveredField = requestMailFields.removeAll(ALL_COVERED_MAIL_FIELDS);
        /*
         * Request mails
         */
        final MailMessage[] mails =
            delegatee.getMessages(
                folder,
                mailIds,
                (requestMailFields.isEmpty()) ? FIELDS_ID : requestMailFields.add(MailField.ID).toArray());
        if (containsAnyCoveredField && (mails.length > 0)) {
            fillMails(folder, originalMailFields, mails);
        }
        return mails;
    }

    @Override
    public MailMessage[] getNewAndModifiedMessages(final String folder, final MailField[] fields) throws OXException {
        return delegatee.getNewAndModifiedMessages(folder, fields);
    }

    @Override
    public MailMessage[] getThreadSortedMessages(final String folder, final IndexRange indexRange, final MailSortField sortField, final OrderDirection order, final SearchTerm<?> searchTerm, final MailField[] fields) throws OXException {
        /*
         * Synchronize since we access the message storage
         */
        synchronize(folder, false);
        /*
         * Check fields
         */
        final MailFields originalMailFields = new MailFields(fields);
        /*
         * Check if all requested fields can be served from header cache
         */
        final MailFields requestMailFields = new MailFields(originalMailFields);
        final boolean containsAnyCoveredField = requestMailFields.removeAll(ALL_COVERED_MAIL_FIELDS);
        /*
         * Request mails
         */
        final MailMessage[] mails =
            delegatee.getThreadSortedMessages(
                folder,
                indexRange,
                sortField,
                order,
                searchTerm,
                (requestMailFields.isEmpty()) ? FIELDS_ID : requestMailFields.add(MailField.ID).toArray());
        if (containsAnyCoveredField && (mails.length > 0)) {
            fillMails(folder, originalMailFields, mails);
        }
        return mails;
    }

    @Override
    public MailMessage[] getUnreadMessages(final String folder, final MailSortField sortField, final OrderDirection order, final MailField[] fields, final int limit) throws OXException {
        /*
         * Synchronize since we access the message storage
         */
        synchronize(folder, false);
        /*
         * Check fields
         */
        final MailFields originalMailFields = new MailFields(fields);
        /*
         * Check if all requested fields can be served from header cache
         */
        final MailFields requestMailFields = new MailFields(originalMailFields);
        final boolean containsAnyCoveredField = requestMailFields.removeAll(ALL_COVERED_MAIL_FIELDS);
        /*
         * Request mails
         */
        final MailMessage[] unreadMails =
            delegatee.getUnreadMessages(folder, sortField, order, (requestMailFields.isEmpty()) ? FIELDS_ID : requestMailFields.add(
                MailField.ID).toArray(), limit);
        if (containsAnyCoveredField && (unreadMails.length > 0)) {
            fillMails(folder, originalMailFields, unreadMails);
        }
        return unreadMails;
    }

    @Override
    public String[] moveMessages(final String sourceFolder, final String destFolder, final String[] mailIds, final boolean fast) throws OXException {
        final String[] ids = delegatee.moveMessages(sourceFolder, destFolder, mailIds, fast);
        synchronizeFolder(sourceFolder, true, true);
        synchronizeFolder(destFolder, true, true);
        return ids;
    }

    @Override
    public void releaseResources() throws OXException {
        delegatee.releaseResources();
    }

    @Override
    public MailMessage saveDraft(final String draftFullname, final ComposedMailMessage draftMail) throws OXException {
        final MailMessage draft = delegatee.saveDraft(draftFullname, draftMail);
        synchronizeFolder(draftFullname, true, true);
        return draft;
    }

    @Override
    public MailMessage[] searchMessages(final String folder, final IndexRange indexRange, final MailSortField sortField, final OrderDirection order, final SearchTerm<?> searchTerm, final MailField[] fields) throws OXException {
        /*
         * Synchronize since we access the message storage
         */
        synchronize(folder, false);
        /*
         * Check fields
         */
        final MailFields originalMailFields = new MailFields(fields);
        /*
         * Check if all requested fields can be served from header cache
         */
        final MailFields requestMailFields = new MailFields(originalMailFields);
        final boolean containsAnyCoveredField = requestMailFields.removeAll(ALL_COVERED_MAIL_FIELDS);
        /*
         * Request mails
         */
        final MailMessage[] mails =
            delegatee.searchMessages(
                folder,
                indexRange,
                sortField,
                order,
                searchTerm,
                (requestMailFields.isEmpty()) ? FIELDS_ID : requestMailFields.add(MailField.ID).toArray());
        if (containsAnyCoveredField && (mails.length > 0)) {
            fillMails(folder, originalMailFields, mails);
        }
        return mails;
    }

    @Override
    public void updateMessageColorLabel(final String folder, final String[] mailIds, final int colorLabel) throws OXException {
        delegatee.updateMessageColorLabel(folder, mailIds, colorLabel);
        synchronizeFolder(folder, true, true);
    }

    @Override
    public void updateMessageFlags(final String folder, final String[] mailIds, final int flags, final boolean set) throws OXException {
        delegatee.updateMessageFlags(folder, mailIds, flags, set);
        synchronizeFolder(folder, true, true);
    }

    /*-
     * ####################################################################################################################################
     * #############################################         HELPER METHODS         #######################################################
     * ####################################################################################################################################
     */

    private void fillMails(final String folder, final MailFields originalMailFields, final MailMessage[] mails) throws OXException {
        /*
         * Fill messages with cache-available data
         */
        final MailFields coveredMailFields = new MailFields(originalMailFields);
        coveredMailFields.retainAll(ALL_COVERED_MAIL_FIELDS);
        final List<SetterApplier> list = DatabaseAccess.getSetterApplierList(coveredMailFields);
        if (!list.isEmpty()) {
            /*
             * Fill available header data
             */
            if (originalMailFields.contains(MailField.FOLDER_ID)) {
                list.add(new FolderSetterApplier(folder));
            }
            DatabaseAccess.newInstance(folder, accountId, user, contextId).fillMails(mails, list);
        } else if (originalMailFields.contains(MailField.FOLDER_ID)) {
            /*
             * Apply folder fullname to each mail
             */
            for (final MailMessage mail : mails) {
                if (null != mail) {
                    mail.setFolder(folder);
                }
            }
        }
        // TODO: Replace with constant
    }

    /**
     * Synchronizes folder either with calling thread or with a separate thread.
     */
    private void synchronizeFolder(final String folder, final boolean separateThread, final boolean enforce) throws OXException {
        if (separateThread) {
            /*
             * Delegate as task to thread pool
             */
            final ThreadPoolService threadPool = HeaderCacheServiceRegistry.getServiceRegistry().getService(ThreadPoolService.class);
            if (null != threadPool) {
                threadPool.submit(
                    ThreadPools.task(new SyncExtCallable(folder, accountId, session, enforce)),
                    CallerRunsBehavior.getInstance());
                return;
            }
        }
        /*
         * Synchronize with calling thread
         */
        synchronize(folder, mailAccess, session, enforce);
    }

    /**
     * Synchronizes folder with calling thread.
     */
    private void synchronize(final String folder, final boolean enforce) throws OXException {
        synchronize(folder, mailAccess, session, enforce);
    }

    /**
     * Dedicated for being performed by a separate thread.
     */
    static void synchronizeInExternalThread(final String folder, final int accountId, final Session session, final boolean enforce) throws OXException {
        final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess =
            MailAccess.getInstance(session, accountId);
        mailAccess.connect();
        try {
            synchronize(folder, mailAccess, session, enforce);
        } finally {
            mailAccess.close(true);
        }
    }

    private static void synchronize(final String folder, final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess, final Session session, final boolean enforce) throws OXException {
        /*
         * Sync headers
         */
        final Key key = new Key(session.getContextId(), session.getUserId(), mailAccess.getAccountId(), folder);
        Future<Object> f;
        if (enforce) {
            /*
             * Non-blocking wait for free slot in synchronizer map
             */
            while (SYNCHRONIZER_MAP.containsKey(key)) {
                ;
            }
            /*
             * If we are here, any started task contains the changes to propagate
             */
            f = startOrWait(folder, mailAccess, session, true, key);
        } else {
            /*
             * Start own task or wait for present task being executed
             */
            f = SYNCHRONIZER_MAP.get(key);
            if (null == f) {
                f = startOrWait(folder, mailAccess, session, false, key);
            }
        }
        /*
         * Check obtained future
         */
        if (null != f) {
            /*
             * Get future's result
             */
            getFutureResult(f);
        }
    }

    /**
     * Returns a non-<code>null</code> {@link Future} to wait; otherwise <code>null</code>
     */
    private static Future<Object> startOrWait(final String folder, final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess, final Session session, final boolean enforce, final Key key) {
        final FutureTask<Object> ft = new FutureTask<Object>(new SynchronizerCallable(folder, session, mailAccess, enforce));
        final Future<Object> f = SYNCHRONIZER_MAP.putIfAbsent(key, ft);
        if (null != f) {
            return f;
        }
        // f = ft;
        ft.run();
        /*-
         * No need to get future's result since SynchronizerCallable returns null; therefore:
         * => Remove & leave
         */
        SYNCHRONIZER_MAP.remove(key);
        return null;
    }

    private static <V> V getFutureResult(final Future<V> future) throws OXException {
        try {
            return future.get();
        } catch (final InterruptedException e) {
            // Keep interrupted status
            Thread.currentThread().interrupt();
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final CancellationException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final ExecutionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof OXException) {
                throw ((OXException) cause);
            }
            if (cause instanceof RuntimeException) {
                throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw new IllegalStateException("Not unchecked", cause);
        }
    }

    private static final class SyncExtCallable implements Callable<Object> {

        private final String folder;

        private final int accountId;

        private final Session session;

        private final boolean enforce;

        SyncExtCallable(final String folder, final int accountId, final Session session, final boolean enforce) {
            super();
            this.folder = folder;
            this.accountId = accountId;
            this.session = session;
            this.enforce = enforce;
        }

        @Override
        public Object call() throws Exception {
            synchronizeInExternalThread(folder, accountId, session, enforce);
            return null;
        }
    }

    private static final class Key {

        private final int cid;

        private final int user;

        private final int account;

        private final String fullname;

        private final int hash;

        Key(final int cid, final int user, final int account, final String fullname) {
            super();
            this.cid = cid;
            this.user = user;
            this.account = account;
            this.fullname = fullname;

            final int prime = 31;
            int result = 1;
            result = prime * result + account;
            result = prime * result + cid;
            result = prime * result + ((fullname == null) ? 0 : fullname.hashCode());
            result = prime * result + user;
            hash = result;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Key)) {
                return false;
            }
            final Key other = (Key) obj;
            if (account != other.account) {
                return false;
            }
            if (cid != other.cid) {
                return false;
            }
            if (user != other.user) {
                return false;
            }
            if (fullname == null) {
                if (other.fullname != null) {
                    return false;
                }
            } else if (!fullname.equals(other.fullname)) {
                return false;
            }
            return true;
        }

    } // End of Key class

}

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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.mail.smal;

import static com.openexchange.mail.smal.SMALServiceLookup.getServiceStatic;
import static java.util.Arrays.asList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import com.openexchange.exception.OXException;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailFields;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.IMailMessageStorageBatch;
import com.openexchange.mail.api.IMailMessageStorageExt;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.mail.smal.adapter.IndexAdapter;
import com.openexchange.mail.smal.jobqueue.JobQueue;
import com.openexchange.mail.smal.jobqueue.jobs.AdderByIDsJob;
import com.openexchange.mail.smal.jobqueue.jobs.ChangeByIDsJob;
import com.openexchange.mail.smal.jobqueue.jobs.FlagsObserverJob;
import com.openexchange.mail.smal.jobqueue.jobs.FolderJob;
import com.openexchange.mail.smal.jobqueue.jobs.RemoveByIDsJob;
import com.openexchange.session.Session;
import com.openexchange.threadpool.CancelableCompletionService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;

/**
 * {@link SMALMessageStorage} - The message storage for SMAL which either delegates calls to delegating message storage or serves them from
 * index storage.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SMALMessageStorage extends AbstractSMALStorage implements IMailMessageStorage, IMailMessageStorageExt, IMailMessageStorageBatch {

    protected static final org.apache.commons.logging.Log LOG =
        com.openexchange.log.Log.valueOf(org.apache.commons.logging.LogFactory.getLog(SMALMessageStorage.class));

    private static enum MailResultType {
        STORAGE, INDEX;
    }

    private static final class MailResult<V> {

        protected static <V> MailResult<V> newStorageResult(final V result) {
            return new MailResult<V>(MailResultType.STORAGE, result);
        }

        protected static <V> MailResult<V> newIndexResult(final V result) {
            return new MailResult<V>(MailResultType.INDEX, result);
        }

        protected final V result;

        protected final MailResultType resultType;

        private MailResult(final MailResultType resultType, final V result) {
            super();
            this.result = result;
            this.resultType = resultType;
        }

    }

    /**
     * Takes the next completed task from specified completion service.
     * 
     * @param completionService The completion service to take from
     * @return The next completed task
     * @throws OXException If taking next completed task failsF
     */
    protected static <V> MailResult<V> takeNextFrom(final CompletionService<MailResult<V>> completionService) throws OXException {
        try {
            final Future<MailResult<V>> future = completionService.take();
            return getFrom(future);
        } catch (final InterruptedException e) {
            // Keep interrupted state
            Thread.currentThread().interrupt();
            throw MailExceptionCode.INTERRUPT_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Polls the next completed task from specified completion service.
     * 
     * @param completionService The completion service to take from
     * @return The next completed task or <code>null</code>
     * @throws OXException If taking next completed task failsF
     */
    protected static <V> MailResult<V> pollNextFrom(final CompletionService<MailResult<V>> completionService, final long millis) throws OXException {
        final Future<MailResult<V>> future;
        try {
            future = millis > 0 ? completionService.poll(millis, TimeUnit.MILLISECONDS) : completionService.poll();
        } catch (final InterruptedException e) {
            // Keep interrupted state
            Thread.currentThread().interrupt();
            throw MailExceptionCode.INTERRUPT_ERROR.create(e, e.getMessage());
        }
        if (null == future) {
            return null;
        }
        return getFrom(future);
    }

    /**
     * Cancels all tasks of passed completion service.
     * 
     * @param completionService The completion service to cancel
     */
    protected static <V> void cancelRemaining(final CancelableCompletionService<MailResult<V>> completionService) {
        if (null != completionService) {
            try {
                completionService.cancel(true);
            } catch (final RuntimeException e) {
                LOG.warn("Failed canceling remaining tasks.", e);
            }
        }
    }

    private static <V> MailResult<V> getFrom(final Future<MailResult<V>> future) throws OXException {
        try {
            return future.get();
        } catch (final InterruptedException e) {
            // Keep interrupted state
            Thread.currentThread().interrupt();
            throw MailExceptionCode.INTERRUPT_ERROR.create(e, e.getMessage());
        } catch (final ExecutionException e) {
            try {
                throw ThreadPools.launderThrowable(e, OXException.class);
            } catch (final RuntimeException rte) {
                throw MailExceptionCode.UNEXPECTED_ERROR.create(rte, rte.getMessage());
            }
        }
    }

    /*-
     * --------------------------------------------- Member stuff -----------------------------------------------
     */

    private final IMailMessageStorage messageStorage;

    /**
     * Initializes a new {@link SMALMessageStorage}.
     * 
     * @throws OXException If initialization fails
     */
    public SMALMessageStorage(final Session session, final int accountId, final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> delegateMailAccess) throws OXException {
        super(session, accountId, delegateMailAccess);
        messageStorage = delegateMailAccess.getMessageStorage();
    }

    @Override
    public String[] appendMessages(final String destFolder, final MailMessage[] msgs) throws OXException {
        final String[] newIds = messageStorage.appendMessages(destFolder, msgs);
        /*
         * Enqueue adder job
         */
        final AdderByIDsJob adderJob = new AdderByIDsJob(destFolder, accountId, userId, contextId);
        JobQueue.getInstance().addJob(adderJob.setMailIds(asList(newIds)).setRanking(10));
        return newIds;
    }

    @Override
    public String[] copyMessages(final String sourceFolder, final String destFolder, final String[] mailIds, final boolean fast) throws OXException {
        final String[] newIds = messageStorage.copyMessages(sourceFolder, destFolder, mailIds, false);
        /*
         * Enqueue adder job
         */
        final AdderByIDsJob adderJob = new AdderByIDsJob(destFolder, accountId, userId, contextId);
        JobQueue.getInstance().addJob(adderJob.setMailIds(asList(newIds)).setRanking(10));
        return fast ? new String[0] : newIds;
    }

    @Override
    public void deleteMessages(final String folder, final String[] mailIds, final boolean hardDelete) throws OXException {
        messageStorage.deleteMessages(folder, mailIds, hardDelete);
        /*
         * Enqueue remover job
         */
        final RemoveByIDsJob removerJob = new RemoveByIDsJob(folder, accountId, userId, contextId);
        JobQueue.getInstance().addJob(removerJob.setMailIds(asList(mailIds)).setRanking(10));
    }

    @Override
    public MailMessage[] getMessages(final String folder, final String[] mailIds, final MailField[] fields) throws OXException {
        final IndexAdapter indexAdapter = getIndexAdapter();
        if (null == indexAdapter) {
            return messageStorage.getMessages(folder, mailIds, fields);
        }
        final MailFields mfs = new MailFields(fields);
        if (!indexAdapter.getIndexableFields().containsAll(mfs)) {
            return messageStorage.getMessages(folder, mailIds, fields);
        }
        try {
            /*
             * Concurrently fetch from index and mail storage and serve request with whichever comes first
             */
            final IMailMessageStorage messageStorage = this.messageStorage;
            final CancelableCompletionService<MailResult<List<MailMessage>>> completionService = newCompletionService();
            completionService.submit(new Callable<MailResult<List<MailMessage>>>() {

                @Override
                public MailResult<List<MailMessage>> call() throws Exception {
                    return MailResult.newIndexResult(indexAdapter.getMessages(mailIds, folder, null, null, fields, accountId, session));
                }
            });
            completionService.submit(new Callable<MailResult<List<MailMessage>>>() {

                @Override
                public MailResult<List<MailMessage>> call() throws Exception {
                    return MailResult.newStorageResult(asList(messageStorage.getMessages(folder, mailIds, fields)));
                }
            });
            final MailResult<List<MailMessage>> result = takeNextFrom(completionService);
            switch (result.resultType) {
            case INDEX:
                if (mfs.contains(MailField.FLAGS)) {
                    // Index result came first
                    awaitStorageGetResult(folder, completionService);
                }
                break;
            case STORAGE:
                // Storage result came first
                cancelRemaining(completionService);
            }
            final List<MailMessage> mails = result.result;
            return mails.toArray(new MailMessage[mails.size()]);
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    /**
     * Await the completion of the task which returns storage's content. Trigger an appropriate {@link FlagsObserverJob}.
     */
    private void awaitStorageGetResult(final String folder, final CompletionService<MailResult<List<MailMessage>>> completionService) {
        final Runnable task = new Runnable() {

            @Override
            public void run() {
                try {
                    final List<MailMessage> mails = takeNextFrom(completionService).result;
                    final FlagsObserverJob job = new FlagsObserverJob(folder, accountId, userId, contextId);
                    JobQueue.getInstance().addJob(job.setRanking(1).setStorageMails(mails));
                } catch (final OXException oxe) {
                    LOG.warn("Retrieving mails from mail storage failed.", oxe);
                } catch (final RuntimeException rte) {
                    LOG.warn("Retrieving mails from mail storage failed.", rte);
                }
            }
        };
        getServiceStatic(ThreadPoolService.class).submit(ThreadPools.task(task));
    }

    @Override
    public MailMessage[] searchMessages(final String folder, final IndexRange indexRange, final MailSortField sortField, final OrderDirection order, final SearchTerm<?> searchTerm, final MailField[] fields) throws OXException {
        System.out.println("SMALMessageStorage.searchMessages()...");
        final IndexAdapter indexAdapter = getIndexAdapter();
        if (null == indexAdapter) {
            return messageStorage.searchMessages(folder, indexRange, sortField, order, searchTerm, fields);
        }
        final MailFields mfs = new MailFields(fields);
        if (!indexAdapter.getIndexableFields().containsAll(mfs)) {
            return messageStorage.searchMessages(folder, indexRange, sortField, order, searchTerm, fields);
        }
        final long st = System.currentTimeMillis();
        try {
            /*
             * Concurrently fetch from index and mail storage and serve request with whichever comes first
             */
            final IMailMessageStorage ms = this.messageStorage;
            final CancelableCompletionService<MailResult<List<MailMessage>>> completionService = newCompletionService();
            completionService.submit(new Callable<MailResult<List<MailMessage>>>() {

                @Override
                public MailResult<List<MailMessage>> call() throws Exception {
                    try {
                        return MailResult.newIndexResult(indexAdapter.search(
                            folder,
                            searchTerm,
                            sortField,
                            order,
                            fields,
                            accountId,
                            session));
                    } catch (final InterruptedException e) {
                        LOG.debug("Index search request interrupted: " + e.getMessage());

                        System.out.println("Index search request interrupted: " + e.getMessage());

                        throw e;
                    }
                }
            });
            completionService.submit(new Callable<MailResult<List<MailMessage>>>() {

                @Override
                public MailResult<List<MailMessage>> call() throws Exception {
                    return MailResult.newStorageResult(asList(ms.searchMessages(folder, indexRange, sortField, order, searchTerm, fields)));
                }
            });
            final MailResult<List<MailMessage>> result = takeNextFrom(completionService);
            switch (result.resultType) {
            case INDEX:
                // Index result came first: Await storage task completion separately & schedule sync job
                // Ignore deleted if results are filtered by a search term

                System.out.println(">>>INDEX<<< request completed first for " + folder + ". Awaiting storage result separately & schedule sync job");

                awaitStorageSearchResult(folder, completionService, (null != searchTerm));
                break;
            case STORAGE:
                // Storage result came first: Cancel remaining index task and schedule sync job

                System.out.println(">>>STORAGE<<< request completed first for " + folder + "...");

                awaitIndexSearchResult(folder, completionService, result, (null != searchTerm));
                break;
            }
            final List<MailMessage> mails = result.result;
            return mails.toArray(new MailMessage[mails.size()]);
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        } finally {
            final long dur = System.currentTimeMillis() - st;
            System.out.println("\tSMALMessageStorage.searchMessages() took " + dur + "msec.");
        }
    }

    /**
     * Await the completion of the task which returns storage's content. Trigger an appropriate {@link FolderJob}.
     */
    private void awaitIndexSearchResult(final String folder, final CancelableCompletionService<MailResult<List<MailMessage>>> completionService, final MailResult<List<MailMessage>> result, final boolean ignoreDeleted) {
        final Runnable task = new Runnable() {

            @Override
            public void run() {
                try {
                    final MailResult<List<MailMessage>> delayedResult = pollNextFrom(completionService, 1000);
                    if (delayedResult == null) {
                        System.out.println("... Canceling remaining index request & schedule sync job.");
                        cancelRemaining(completionService);
                        scheduleFolderJob(folder, result.result, null, ignoreDeleted);
                    } else {
                        System.out.println("... Delayed result available & schedule sync job.");
                        // Delayed result available
                        scheduleFolderJob(folder, result.result, delayedResult.result, ignoreDeleted);
                    }
                } catch (final OXException oxe) {
                    LOG.warn("Retrieving mails from mail storage failed.", oxe);
                } catch (final RuntimeException rte) {
                    LOG.warn("Retrieving mails from mail storage failed.", rte);
                }
            }
        };
        getServiceStatic(ThreadPoolService.class).submit(ThreadPools.task(task));
    }

    /**
     * Await the completion of the task which returns storage's content. Trigger an appropriate {@link FolderJob}.
     */
    private void awaitStorageSearchResult(final String folder, final CompletionService<MailResult<List<MailMessage>>> completionService, final boolean ignoreDeleted) {
        final Runnable task = new Runnable() {

            @Override
            public void run() {
                try {
                    final List<MailMessage> mails = takeNextFrom(completionService).result;
                    scheduleFolderJob(folder, mails, null, ignoreDeleted);
                } catch (final OXException oxe) {
                    LOG.warn("Retrieving mails from mail storage failed.", oxe);
                } catch (final RuntimeException rte) {
                    LOG.warn("Retrieving mails from mail storage failed.", rte);
                }
            }
        };
        getServiceStatic(ThreadPoolService.class).submit(ThreadPools.task(task));
    }

    /**
     * Schedules a new folder job.
     * 
     * @param fullName The folder full name
     * @param optStorageMails The optional storage mails
     * @param optIndexMails The optional index mails
     * @param ignoreDeleted Whether to ignore deleted mails during sync operation
     */
    protected void scheduleFolderJob(final String fullName, final List<MailMessage> optStorageMails, final List<MailMessage> optIndexMails, final boolean ignoreDeleted) {
        final FolderJob folderJob = new FolderJob(fullName, accountId, userId, contextId);
        if (null != optStorageMails) {
            folderJob.setStorageMails(optStorageMails); // Assign storage's mail
        }
        if (null != optIndexMails) {
            folderJob.setIndexMails(optIndexMails); // Assign index' mail
        }
        folderJob.setSpan(-1); // Immediate run
        folderJob.setRanking(10); // Replace this job with similar jobs possibly contained in queue
        folderJob.setIgnoreDeleted(ignoreDeleted); // Whether to delete (may only be done if all mails requested)
        JobQueue.getInstance().addJob(folderJob);
    }

    @Override
    public void updateMessageFlags(final String folder, final String[] mailIds, final int flags, final boolean set) throws OXException {
        messageStorage.updateMessageFlags(folder, mailIds, flags, set);
        /*
         * Enqueue change job
         */
        final ChangeByIDsJob job = new ChangeByIDsJob(folder, accountId, userId, contextId);
        JobQueue.getInstance().addJob(job.setRanking(10).setMailIds(asList(mailIds)));
    }

    @Override
    public MailMessage[] getAllMessages(final String folder, final IndexRange indexRange, final MailSortField sortField, final OrderDirection order, final MailField[] fields) throws OXException {
        return searchMessages(folder, indexRange, sortField, order, null, fields);
    }

    @Override
    public MailPart getAttachment(final String folder, final String mailId, final String sequenceId) throws OXException {
        return messageStorage.getAttachment(folder, mailId, sequenceId);
    }

    @Override
    public MailPart getImageAttachment(final String folder, final String mailId, final String contentId) throws OXException {
        return messageStorage.getImageAttachment(folder, mailId, contentId);
    }

    @Override
    public MailMessage getMessage(final String folder, final String mailId, final boolean markSeen) throws OXException {
        final IndexAdapter indexAdapter = getIndexAdapter();
        if (null == indexAdapter) {
            return messageStorage.getMessage(folder, mailId, markSeen);
        }
        final MailMessage mail = messageStorage.getMessage(folder, mailId, markSeen);
        mail.setAccountId(accountId);
        indexAdapter.addContent(mail, session);
        return mail;
    }

    @Override
    public MailMessage[] getThreadSortedMessages(final String folder, final IndexRange indexRange, final MailSortField sortField, final OrderDirection order, final SearchTerm<?> searchTerm, final MailField[] fields) throws OXException {
        return messageStorage.getThreadSortedMessages(folder, indexRange, sortField, order, searchTerm, fields);
    }

    @Override
    public MailMessage[] getUnreadMessages(final String folder, final MailSortField sortField, final OrderDirection order, final MailField[] fields, final int limit) throws OXException {
        return messageStorage.getUnreadMessages(folder, sortField, order, fields, limit);
    }

    @Override
    public String[] moveMessages(final String sourceFolder, final String destFolder, final String[] mailIds, final boolean fast) throws OXException {
        final String[] newIds = messageStorage.moveMessages(sourceFolder, destFolder, mailIds, false);
        /*
         * Adder job
         */
        final AdderByIDsJob adderJob = new AdderByIDsJob(destFolder, accountId, userId, contextId);
        JobQueue.getInstance().addJob(adderJob.setMailIds(asList(newIds)).setRanking(10));
        /*
         * Remover job
         */
        final RemoveByIDsJob removerJob = new RemoveByIDsJob(sourceFolder, accountId, userId, contextId);
        JobQueue.getInstance().addJob(removerJob.setMailIds(asList(mailIds)).setRanking(10));
        /*
         * Return depending on "fast" parameter
         */
        return fast ? new String[0] : newIds;
    }

    @Override
    public MailMessage saveDraft(final String draftFullname, final ComposedMailMessage draftMail) throws OXException {
        return messageStorage.saveDraft(draftFullname, draftMail);
    }

    @Override
    public void updateMessageColorLabel(final String folder, final String[] mailIds, final int colorLabel) throws OXException {
        messageStorage.updateMessageColorLabel(folder, mailIds, colorLabel);
        /*
         * Enqueue change job.
         */
        final ChangeByIDsJob job = new ChangeByIDsJob(folder, accountId, userId, contextId);
        JobQueue.getInstance().addJob(job.setRanking(10).setMailIds(asList(mailIds)));
    }

    @Override
    public MailMessage[] getNewAndModifiedMessages(final String folder, final MailField[] fields) throws OXException {
        final Long timestamp = (Long) session.getParameter("smal.Timestamp");

        return messageStorage.getNewAndModifiedMessages(folder, fields);
    }

    @Override
    public MailMessage[] getDeletedMessages(final String folder, final MailField[] fields) throws OXException {
        final Long timestamp = (Long) session.getParameter("smal.Timestamp");

        return messageStorage.getDeletedMessages(folder, fields);
    }

    @Override
    public void releaseResources() throws OXException {
        messageStorage.releaseResources();
    }

    /**
     * The fields containing only the mail identifier.
     */
    protected static final MailField[] FIELDS_ID_AND_FOLDER = new MailField[] { MailField.ID, MailField.FOLDER_ID };

    @Override
    public MailMessage[] getMessagesByMessageID(final String... messageIDs) throws OXException {
        if (messageStorage instanceof IMailMessageStorageExt) {
            final IMailMessageStorageExt messageStorageExt = (IMailMessageStorageExt) messageStorage;
            return messageStorageExt.getMessagesByMessageID(messageIDs);
        }
        final SearchTerm<?> searchTerm;
        if (1 == messageIDs.length) {
            searchTerm = new com.openexchange.mail.search.HeaderTerm("Message-ID", messageIDs[0]);
        } else {
            return EMPTY_RETVAL;
        }
        return messageStorage.searchMessages(
            "INBOX",
            IndexRange.NULL,
            MailSortField.RECEIVED_DATE,
            OrderDirection.ASC,
            searchTerm,
            FIELDS_ID_AND_FOLDER);
    }

    private static final MailField[] FIELDS_HEADERS = { MailField.ID, MailField.HEADERS };

    @Override
    public MailMessage[] getMessages(final String fullName, final String[] mailIds, final MailField[] fields, final String[] headerNames) throws OXException {
        if (messageStorage instanceof IMailMessageStorageExt) {
            final IMailMessageStorageExt messageStorageExt = (IMailMessageStorageExt) messageStorage;
            return messageStorageExt.getMessages(fullName, mailIds, fields, headerNames);
        }
        return messageStorage.getMessages(fullName, mailIds, FIELDS_HEADERS);
    }

    @Override
    public void updateMessageColorLabel(final String fullName, final int colorLabel) throws OXException {
        if (messageStorage instanceof IMailMessageStorageBatch) {
            final IMailMessageStorageBatch batch = (IMailMessageStorageBatch) messageStorage;
            batch.updateMessageColorLabel(fullName, colorLabel);
        } else {
            final String[] ids = getAllIdentifiersOf(fullName);
            messageStorage.updateMessageColorLabel(fullName, ids, colorLabel);
        }
    }

    private String[] getAllIdentifiersOf(final String fullName) throws OXException {
        final MailMessage[] messages =
            searchMessages(fullName, IndexRange.NULL, MailSortField.RECEIVED_DATE, OrderDirection.ASC, null, FIELDS_ID);
        final String[] ids = new String[messages.length];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = messages[i].getMailId();
        }
        return ids;
    }

    @Override
    public void updateMessageFlags(final String fullName, final int flags, final boolean set) throws OXException {
        if (messageStorage instanceof IMailMessageStorageBatch) {
            final IMailMessageStorageBatch batch = (IMailMessageStorageBatch) messageStorage;
            batch.updateMessageFlags(fullName, flags, set);
        } else {
            final String[] ids = getAllIdentifiersOf(fullName);
            messageStorage.updateMessageFlags(fullName, ids, flags, set);
        }
    }

    @Override
    public String[] getPrimaryContents(final String folder, final String[] mailIds) throws OXException {
        return messageStorage.getPrimaryContents(folder, mailIds);
    }

}

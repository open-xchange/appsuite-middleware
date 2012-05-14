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

package com.openexchange.mail.smal.impl;

import static java.util.Arrays.asList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.commons.logging.Log;
import com.openexchange.exception.OXException;
import com.openexchange.index.solr.mail.SolrMailUtility;
import com.openexchange.log.LogFactory;
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
import com.openexchange.mail.api.MailProvider;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.mail.smal.impl.index.IndexAccessAdapter;
import com.openexchange.mail.smal.impl.processor.ProcessingProgress;
import com.openexchange.service.indexing.IndexingService;
import com.openexchange.service.indexing.mail.job.AddByIDsJob;
import com.openexchange.service.indexing.mail.job.ChangeByIDsJob;
import com.openexchange.service.indexing.mail.job.ChangeByMessagesJob;
import com.openexchange.service.indexing.mail.job.RemoveByIDsJob;
import com.openexchange.session.Session;
import com.openexchange.threadpool.CancelableCompletionService;
import com.openexchange.threadpool.ThreadPools;

/**
 * {@link SmalMessageStorage} - The message storage for SMAL which either delegates calls to delegating message storage or serves them from
 * index storage.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SmalMessageStorage extends AbstractSMALStorage implements IMailMessageStorage, IMailMessageStorageExt, IMailMessageStorageBatch {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(SmalMessageStorage.class));

    private static final boolean DEBUG = LOG.isDebugEnabled();

    private static enum MailResultType {
        STORAGE, INDEX;
    }

    private static final class MailResult<V> {

        /**
         * The empty result.
         */
        protected static final MailResult<Object> EMPTY_RESULT = new MailResult<Object>(null, null);

        @SuppressWarnings("unchecked")
        protected static <V> MailResult<V> emptyResult() {
            return (MailResult<V>) EMPTY_RESULT;
        }

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
     * Takes the next completed task from specified completion service (waiting if none are yet present).
     * 
     * @param completionService The completion service to take from
     * @return The next completed task
     * @throws OXException If taking next completed task fails
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
     * Tries to take the next completed task from specified completion service if one is immediately available.
     * 
     * @param completionService The completion service to take from
     * @return The next completed task or <code>null</code> if none immediately available
     * @throws OXException If taking next completed task fails
     */
    protected static <V> MailResult<V> tryTakeNextFrom(final CompletionService<MailResult<V>> completionService) throws OXException {
        final Future<MailResult<V>> future = completionService.poll();
        if (null == future) {
            return null;
        }
        return getFrom(future);
    }

    /**
     * Polls the next completed task from specified completion service.
     * 
     * @param completionService The completion service to take from
     * @param millis The max. number of milliseconds to wait
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

    private static <V> V getFrom(final Future<V> future) throws OXException {
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

    private static final MailFields MAIL_FIELDS_FLAGS = new MailFields(MailField.FLAGS, MailField.COLOR_LABEL);

    /*-
     * --------------------------------------------- Member stuff -----------------------------------------------
     */

    private final IMailMessageStorage messageStorage;

    private final IMailFolderStorage folderStorage;

    /**
     * Initializes a new {@link SmalMessageStorage}.
     * 
     * @throws OXException If initialization fails
     */
    public SmalMessageStorage(final Session session, final int accountId, final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> delegateMailAccess) throws OXException {
        super(session, accountId, delegateMailAccess);
        messageStorage = delegateMailAccess.getMessageStorage();
        folderStorage = delegateMailAccess.getFolderStorage();
    }

    @Override
    public String[] appendMessages(final String destFolder, final MailMessage[] msgs) throws OXException {
        final String[] newIds = messageStorage.appendMessages(destFolder, msgs);
        /*
         * Enqueue adder job
         */
        final AddByIDsJob adderJob = new AddByIDsJob(destFolder, createJobInfo());
        adderJob.setMails(Arrays.asList(msgs));
        adderJob.setPriority(9);
        submitJob(adderJob);
        return newIds;
    }

    @Override
    public String[] copyMessages(final String sourceFolder, final String destFolder, final String[] mailIds, final boolean fast) throws OXException {
        final String[] newIds = messageStorage.copyMessages(sourceFolder, destFolder, mailIds, false);
        /*
         * Enqueue adder job
         */
        final AddByIDsJob adderJob = new AddByIDsJob(destFolder, createJobInfo());
        adderJob.setMailIds(Arrays.asList(mailIds));
        adderJob.setPriority(9);
        submitJob(adderJob);
        return fast ? new String[0] : newIds;
    }

    @Override
    public void deleteMessages(final String folder, final String[] mailIds, final boolean hardDelete) throws OXException {
        messageStorage.deleteMessages(folder, mailIds, hardDelete);
        /*
         * Enqueue remover job
         */
        final RemoveByIDsJob removerJob = new RemoveByIDsJob(folder, createJobInfo());
        removerJob.setMailIds(Arrays.asList(mailIds));
        removerJob.setPriority(9);
        submitJob(removerJob);
    }

    @Override
    public MailMessage[] getMessages(final String folder, final String[] mailIds, final MailField[] fields) throws OXException {
        if (null == getIndexFacadeService()) {
            return messageStorage.getMessages(folder, mailIds, fields);
        }
        final MailFields mfs = new MailFields(fields);
        if (!SolrMailUtility.getIndexableFields().containsAll(mfs)) {
            return messageStorage.getMessages(folder, mailIds, fields);
        }
        try {
            /*
             * Obtain folder
             */
            final MailFolder mailFolder = folderStorage.getFolder(folder);
            if (!mailFolder.isHoldsMessages() || 0 == mailFolder.getMessageCount()) {
                // Folder has no messages
                return EMPTY_RETVAL;
            }
            /*
             * Process folder
             */
            /*final ProcessingProgress processingProgress = */processFolder(mailFolder);
            /*
             * Concurrently fetch from index and mail storage and serve request with whichever comes first
             */
            final IMailMessageStorage messageStorage = this.messageStorage;
            final CancelableCompletionService<MailResult<List<MailMessage>>> completionService = newCompletionService();
            completionService.submit(new Callable<MailResult<List<MailMessage>>>() {

                @Override
                public MailResult<List<MailMessage>> call() throws Exception {
                    // No need to await completion because getMessages() is called immediately after searchMessages()
                    // processingProgress.awaitCompletion();
                    return MailResult.newIndexResult(IndexAccessAdapter.getInstance().getMessages(accountId, folder, session, null, null));
//                    return MailResult.newIndexResult(IndexAccessAdapter.getInstance().getMessages(accountId, folder, mailIds, null, null, fields, session));
                }
            });
            completionService.submit(new Callable<MailResult<List<MailMessage>>>() {

                @Override
                public MailResult<List<MailMessage>> call() throws Exception {
                    return MailResult.newStorageResult(asList(messageStorage.getMessages(folder, mailIds, fields)));
                }
            });
            MailResult<List<MailMessage>> result = takeNextFrom(completionService);
            if (MailResult.EMPTY_RESULT.equals(result)) {
                result = takeNextFrom(completionService);
            }
            switch (result.resultType) {
            case INDEX:
                if (mfs.containsAny(MAIL_FIELDS_FLAGS)) {
                    // Index result came first
                    asyncScheduleChangeJob(folder, completionService);
                }
                if (DEBUG) {
                    LOG.debug("SmalMessageStorage.getMessages(): Index result came first for \"" + folder + "\" " + new DebugInfo(delegateMailAccess));
                }
                break;
            case STORAGE:
                // Storage result came first
                if (DEBUG) {
                    final MailProvider provider = delegateMailAccess.getProvider();
                    LOG.debug("SmalMessageStorage.getMessages(): "+(null == provider ? "Storage" : provider.getProtocol().getName())+" result came first for \"" + folder + "\" " + new DebugInfo(delegateMailAccess));
                }
                // TODO: cancelRemaining(completionService);
                break;
            }
            final List<MailMessage> mails = result.result;
            return mails.toArray(new MailMessage[mails.size()]);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw MailExceptionCode.INTERRUPT_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }
    
    
    @Override
    public MailMessage[] searchMessages(final String folder, final IndexRange indexRange, final MailSortField sortField, final OrderDirection order, final SearchTerm<?> searchTerm, final MailField[] fields) throws OXException {
    	if (null == getIndexFacadeService()) {
            return messageStorage.searchMessages(folder, indexRange, sortField, order, searchTerm, fields);
        }
        final MailFields mfs = new MailFields(fields);
        if (!SolrMailUtility.getIndexableFields().containsAll(mfs)) {
            return messageStorage.searchMessages(folder, indexRange, sortField, order, searchTerm, fields);
        }
        try {
            /*
             * Obtain folder
             */
            final MailFolder mailFolder = folderStorage.getFolder(folder);
            if (!mailFolder.isHoldsMessages() || 0 == mailFolder.getMessageCount()) {
                // Folder has no messages
                return EMPTY_RETVAL;
            }
            /*
             * Process folder
             */
            final ProcessingProgress processingProgress = processFolder(mailFolder);
            /*
             * Concurrently fetch from index and mail storage and serve request with whichever comes first
             */
            final IMailMessageStorage ms = this.messageStorage;
            final CancelableCompletionService<MailResult<List<MailMessage>>> completionService = newCompletionService();
            completionService.submit(new Callable<MailResult<List<MailMessage>>>() {

                @Override
                public MailResult<List<MailMessage>> call() throws Exception {
                    try {
                        if (processingProgress.isFirstTime()) {
                            processingProgress.awaitCompletion();
                        }
                        return MailResult.newIndexResult(IndexAccessAdapter.getInstance().search(accountId, folder, searchTerm, sortField, order, fields, indexRange, session));
                    } catch (final OXException e) {
                        if (!MailExceptionCode.FOLDER_NOT_FOUND.equals(e)) {
                            LOG.error(e.getMessage(), e);
                            throw e;
                        }
                        return MailResult.emptyResult();
                    } catch (final Exception e) {
                        LOG.error(e.getMessage(), e);
                        throw e;
                    }
                }
            });
            completionService.submit(new Callable<MailResult<List<MailMessage>>>() {

                @Override
                public MailResult<List<MailMessage>> call() throws Exception {
                    try {
                        return MailResult.newStorageResult(asList(ms.searchMessages(
                            folder,
                            indexRange,
                            sortField,
                            order,
                            searchTerm,
                            fields)));
                    } catch (final Exception e) {
                        LOG.error(e.getMessage(), e);
                        throw e;
                    }
                }
            });
            MailResult<List<MailMessage>> result = takeNextFrom(completionService);
            if (MailResult.EMPTY_RESULT.equals(result)) {
                result = takeNextFrom(completionService);
            }
            switch (result.resultType) {
            case INDEX:
                /*
                 * Index result came first
                 */
                if (DEBUG) {
                    LOG.debug("SmalMessageStorage.searchMessages(): Index result came first for \"" + folder + "\" " + new DebugInfo(delegateMailAccess));
                }
                if (processingProgress.asJob()) {
                    /*
                     * Processed as job: indexed results not immediately available
                     */
                    if (processingProgress.isFirstTime()/* && processingProgress.isHasHighAttention()*/) {
                        // Actively await result from storage
                        result = takeNextFrom(completionService);
                    }
                } else {
                    if (new MailFields(fields).containsAny(MAIL_FIELDS_FLAGS)) {
                        // Submit change job
                        asyncScheduleChangeJob(folder, completionService);
                    }
                }
                break;
            case STORAGE:
                /*
                 * Storage result came first: Cancel remaining index task
                 */
                if (DEBUG) {
                    final MailProvider provider = delegateMailAccess.getProvider();
                    LOG.debug("SmalMessageStorage.searchMessages(): "+(null == provider ? "Storage" : provider.getProtocol().getName())+" result came first for \"" + folder + "\" " + new DebugInfo(delegateMailAccess));
                }
                // TODO cancelRemaining(completionService);
                if (!processingProgress.asJob() && new MailFields(fields).containsAny(MAIL_FIELDS_FLAGS)) {
                    scheduleChangeJob(folder, result);
                }
                break;
            }
            final List<MailMessage> mails = result.result;
            return mails.toArray(new MailMessage[mails.size()]);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw MailExceptionCode.INTERRUPT_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    /**
     * Asynchronously await the completion of the task which returns storage's content. Trigger an appropriate change job
     */
    private void asyncScheduleChangeJob(final String folder, final CancelableCompletionService<MailResult<List<MailMessage>>> completionService) {
        final Runnable task = new Runnable() {

            @Override
            public void run() {
                try {
                    final MailResult<List<MailMessage>> delayedResult = pollNextFrom(completionService, 1000);
                    if (delayedResult == null) {
                        cancelRemaining(completionService);
                    } else {
                        // Delayed result available
                        final IndexingService indexingService = SmalServiceLookup.getServiceStatic(IndexingService.class);
                        if (null != indexingService) {
                            final ChangeByMessagesJob changeJob = new ChangeByMessagesJob(folder, createJobInfo());
                            changeJob.setMailIds(delayedResult.result);
                            indexingService.addJob(changeJob);
                        }
                    }
                } catch (final OXException oxe) {
                    LOG.warn("Retrieving mails from mail storage failed.", oxe);
                } catch (final RuntimeException rte) {
                    LOG.warn("Retrieving mails from mail storage failed.", rte);
                }
            }
        };
        ThreadPools.getThreadPool().submit(ThreadPools.task(task));
    }

    /**
     * Schedule a change job
     */
    private void scheduleChangeJob(final String folder, final MailResult<List<MailMessage>> result) {
        if (null == result) {
            return;
        }
        final Runnable task = new Runnable() {

            @Override
            public void run() {
                try {
                    // Delayed result available
                    final IndexingService indexingService = SmalServiceLookup.getServiceStatic(IndexingService.class);
                    if (null != indexingService) {
                        final ChangeByMessagesJob changeJob = new ChangeByMessagesJob(folder, createJobInfo());
                        changeJob.setMailIds(result.result);
                        indexingService.addJob(changeJob);
                    } 
                } catch (final OXException oxe) {
                    LOG.warn("Retrieving mails from mail storage failed.", oxe);
                } catch (final RuntimeException rte) {
                    LOG.warn("Retrieving mails from mail storage failed.", rte);
                }
            }
        };
        ThreadPools.getThreadPool().submit(ThreadPools.task(task));
    }

    @Override
    public void updateMessageFlags(final String folder, final String[] mailIds, final int flags, final boolean set) throws OXException {
        messageStorage.updateMessageFlags(folder, mailIds, flags, set);
        /*
         * Enqueue change job
         */
        final ChangeByIDsJob job = new ChangeByIDsJob(folder, createJobInfo());
        job.setMailIds(Arrays.asList(mailIds));
        job.setPriority(9);
        submitJob(job);
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
        if (null == getIndexFacadeService()) {
            return messageStorage.getMessage(folder, mailId, markSeen);
        }
        final MailMessage mail = messageStorage.getMessage(folder, mailId, markSeen);
        mail.setAccountId(accountId);
        try {
            IndexAccessAdapter.getInstance().addContent(mail, session);
        } catch (final Exception e) {
            // Ignore failed adding to index
            LOG.warn("Adding message's content to index failed.", e);
        }
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
        final AddByIDsJob adderJob = new AddByIDsJob(destFolder, createJobInfo());
        adderJob.setMailIds(asList(newIds));
        adderJob.setPriority(9);
        submitJob(adderJob);
        /*
         * Remover job
         */
        final RemoveByIDsJob removerJob = new RemoveByIDsJob(sourceFolder, createJobInfo());
        adderJob.setMailIds(asList(mailIds));
        adderJob.setPriority(9);
        submitJob(removerJob);
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
        final ChangeByIDsJob job = new ChangeByIDsJob(folder, createJobInfo());
        job.setMailIds(asList(mailIds));
        job.setPriority(9);
        submitJob(job);
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

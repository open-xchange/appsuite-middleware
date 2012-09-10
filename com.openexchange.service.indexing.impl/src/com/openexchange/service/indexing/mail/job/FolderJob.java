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

package com.openexchange.service.indexing.mail.job;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import org.apache.commons.logging.Log;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.index.IndexAccess;
import com.openexchange.index.IndexDocument;
import com.openexchange.index.IndexExceptionCodes;
import com.openexchange.index.QueryParameters.Builder;
import com.openexchange.index.SearchHandler;
import com.openexchange.index.mail.MailUUID;
import com.openexchange.index.mail.MailUtility;
import com.openexchange.index.solr.IndexFolderManager;
import com.openexchange.log.LogFactory;
import com.openexchange.mail.MailFields;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.IMailMessageStorageExt;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.ContentAwareMailMessage;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.service.indexing.IndexingJob;
import com.openexchange.service.indexing.IndexingService;
import com.openexchange.service.indexing.impl.Services;
import com.openexchange.service.indexing.internal.mail.MailJobInfo;
import com.openexchange.service.indexing.mail.Constants;

/**
 * {@link FolderJob}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FolderJob extends AbstractMailJob {

    private static final long serialVersionUID = 2093998857641164982L;

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(FolderJob.class));

    private static final String SIMPLE_NAME = FolderJob.class.getSimpleName();

    protected final String fullName;

    private final InsertType insertType;
    
    private final boolean forced;

    private volatile boolean ignoreDeleted;

    private volatile long span;

    private volatile List<MailMessage> storageMails;

    private volatile List<MailMessage> indexMails;

    /**
     * Initializes a new {@link FolderJob} with default span.
     * <p>
     * This job is performed is span is exceeded and if able to exclusively set sync flag.
     * 
     * @param fullName The folder full name
     * @param info The job information
     */
    public FolderJob(final String fullName, final MailJobInfo info) {
        this(fullName, info, null);
    }
    
    /**
     * Initializes a new {@link FolderJob} with default span.
     * <p>
     * This job is performed is span is exceeded and if able to exclusively set sync flag.
     * 
     * @param fullName The folder full name
     * @param info The job information
     * @param forced Force this job to be executed?
     */
    public FolderJob(final String fullName, final MailJobInfo info, boolean forced) {
        this(fullName, info, null, forced);
    }

    /**
     * Initializes a new {@link FolderJob} with default span.
     * <p>
     * This job is performed is span is exceeded and if able to exclusively set sync flag.
     * 
     * @param fullName The folder full name
     * @param info The job information
     * @param insertType The insert type for new mails; {@link InsertType#ATTACHMENTS} is default
     */
    public FolderJob(final String fullName, final MailJobInfo info, final InsertType insertType) {
        this(fullName, info, insertType, false);
    }
    
    /**
     * Initializes a new {@link FolderJob} with default span.
     * <p>
     * This job is performed is span is exceeded and if able to exclusively set sync flag.
     * 
     * @param fullName The folder full name
     * @param info The job information
     * @param insertType The insert type for new mails; {@link InsertType#ATTACHMENTS} is default
     * @param forced Force this job to be executed?
     */
    public FolderJob(final String fullName, final MailJobInfo info, final InsertType insertType, boolean forced) {
        super(info);
        this.fullName = fullName;
        span = com.openexchange.service.indexing.mail.Constants.DEFAULT_MILLIS;
        this.insertType = null == insertType ? InsertType.ATTACHMENTS : insertType;
        this.forced = forced;
    }

    private static int getBlockSize() {
        return Constants.CHUNK_SIZE;
    }

    private static boolean scheduleJobs() {
        return false;
    }

    /**
     * Sets the ignore-deleted flag.
     * 
     * @param ignoreDeleted The ignore-deleted flag
     * @return This folder job with new behavior applied
     */
    public FolderJob setIgnoreDeleted(final boolean ignoreDeleted) {
        this.ignoreDeleted = ignoreDeleted;
        return this;
    }

    /**
     * Sets the span; a negative span enforces this job to run if able to exclusively set sync flag
     * 
     * @param span The span to set
     * @return This folder job with specified span applied
     */
    public FolderJob setSpan(final long span) {
        this.span = span;
        return this;
    }

    /**
     * Sets the storage mails
     * 
     * @param storageMails The storage mails to set
     * @return This folder job with specified storage mails applied
     */
    public FolderJob setStorageMails(final List<MailMessage> storageMails) {
        this.storageMails = storageMails;
        return this;
    }

    /**
     * Sets the index mails
     * 
     * @param indexMail The index mails to set
     * @return This folder job with specified index mails applied
     */
    public FolderJob setIndexMails(final List<MailMessage> indexMail) {
        indexMails = indexMail;
        return this;
    }
    
    private boolean checkAndSync(long then) throws OXException, InterruptedException {
        boolean unlock = true;
        boolean indexed = IndexFolderManager.isIndexed(contextId, userId, Types.EMAIL, String.valueOf(accountId), fullName);
        long now = System.currentTimeMillis();

        if (forced || !indexed || (now - then) > span) {
            unlock = syncFolder();
            if (!indexed) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Initially indexed folder " + fullName + ". Info: " + info);
                }
                IndexFolderManager.setIndexed(contextId, userId, Types.EMAIL, String.valueOf(accountId), fullName);
            }
        }
        
        return unlock;
    }

    @Override
    protected void performMailJob() throws OXException, InterruptedException {
        if (fullName == null) {
            LOG.warn("Folder name was null. Aborting folder job.");
            return;
        }

        boolean unlock = false;
        final long then = IndexFolderManager.getTimestamp(contextId, userId, Types.EMAIL, String.valueOf(accountId), fullName);
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug(Thread.currentThread().getName() + " performs folder job for folder " + fullName + ". " + info);
            }
            if (IndexFolderManager.lock(contextId, userId, Types.EMAIL, String.valueOf(accountId), fullName)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(Thread.currentThread().getName() + " got the lock.");
                }
                unlock = checkAndSync(then);
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(Thread.currentThread().getName() + " did not get the lock.");
                }
                if (IndexFolderManager.isLocked(contextId, userId, Types.EMAIL, String.valueOf(accountId), fullName) && (System.currentTimeMillis() - then) > (Constants.HOUR_MILLIS * 6)) {
                    LOG.warn("Found a possible unreleased folder lock for " + info + " on folder " + fullName + ". Unlocking now.");
                    IndexFolderManager.unlock(contextId, userId, Types.EMAIL, String.valueOf(accountId), fullName);
                    
                    if (IndexFolderManager.lock(contextId, userId, Types.EMAIL, String.valueOf(accountId), fullName)) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(Thread.currentThread().getName() + " got the retry-lock.");
                        }
                        unlock = checkAndSync(then);
                    }
                }
            }
        } catch (final InterruptedException e) {
            resetTimestamp(then);
            LOG.warn(SIMPLE_NAME + " failed: " + info, e);
            throw e;
        } catch (final RuntimeException e) {
            resetTimestamp(then);
            LOG.warn(SIMPLE_NAME + " failed: " + info, e);
            throw IndexExceptionCodes.UNEXPECTED_ERROR.create(e.getMessage(), e);
        } catch (final OXException e) {
            resetTimestamp(then);
            LOG.warn(SIMPLE_NAME + " failed: " + info, e);
            throw e;
        } finally {
            if (unlock) {
                try {
                    IndexFolderManager.unlock(contextId, userId, Types.EMAIL, String.valueOf(accountId), fullName);
                } catch (final OXException e) {
                    // SQLException. Retry
                    IndexFolderManager.unlock(contextId, userId, Types.EMAIL, String.valueOf(accountId), fullName);
                }
            }
        }
    }
    
    private void resetTimestamp(final long then) throws OXException {
        IndexFolderManager.setTimestamp(contextId, userId, Types.EMAIL, String.valueOf(accountId), fullName, then);
    }

    private boolean syncFolder() throws OXException, InterruptedException {
        final long st = LOG.isDebugEnabled() ? System.currentTimeMillis() : 0L;
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Starting sync: " + info);
            }
            final IndexAccess<MailMessage> indexAccess = storageAccess.getIndexAccess();
            /*
             * Get the mails from storage
             */
            final Map<String, MailMessage> storageMap;
            {
                final List<MailMessage> mails;
                MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
                try {
                    mailAccess = storageAccess.mailAccessFor();
                    /*
                     * At first check existence of denoted folder
                     */
                    if (!mailAccess.getFolderStorage().exists(fullName)) {
                        /*
                         * Drop entry from database and return
                         */
                        deleteDBEntry();
                        final Map<String, Object> params = new HashMap<String, Object>();
                        params.put("accountId", Integer.valueOf(accountId));
                        final Builder queryBuilder = new Builder(params);
                        indexAccess.deleteByQuery(queryBuilder.setHandler(SearchHandler.ALL_REQUEST).setFolders(Collections.singleton(fullName)).build());
                        return false;
                    }
                    if (null == storageMails) {
                        /*
                         * Fetch mails
                         */
                        mails = storageAccess.allMailsFromStorage(fullName);
                    } else {
                        mails = storageMails;
                    }
                } finally {
                    storageAccess.releaseMailAccess();
                    mailAccess = null;
                }
                if (mails.isEmpty()) {
                    storageMap = Collections.emptyMap();
                } else {
                    storageMap = new HashMap<String, MailMessage>(mails.size());
                    for (final MailMessage mailMessage : mails) {
                        storageMap.put(mailMessage.getMailId(), mailMessage);
                    }
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug(storageMap.size() + " mails from storage; folder job: " + info);
                }
            }
            /*
             * Get the mails from index
             */
            final Map<String, MailMessage> indexMap;
            {
                List<MailMessage> indexedMails = indexMails;
                if (null == indexedMails) {
                    indexedMails = storageAccess.allMailsFromIndex(fullName);
                }
                if (indexedMails.isEmpty()) {
                    indexMap = Collections.emptyMap();
                } else {
                    indexMap = new HashMap<String, MailMessage>(indexedMails.size());
                    for (final MailMessage mailMessage : indexedMails) {
                        indexMap.put(mailMessage.getMailId(), mailMessage);
                    }
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug(indexMap.size() + " mails from index; folder job: " + info);
                }
            }
            /*
             * New ones
             */
            Set<String> newIds = new HashSet<String>(storageMap.keySet());
            newIds.removeAll(indexMap.keySet());
            /*
             * Removed ones
             */
            Set<String> deletedIds;
            if (ignoreDeleted) {
                deletedIds = Collections.emptySet();
            } else {
                deletedIds = new HashSet<String>(indexMap.keySet());
                deletedIds.removeAll(storageMap.keySet());
            }
            /*
             * Changed ones
             */
            List<MailMessage> changedMails;
            {
                final Set<String> changedIds = new HashSet<String>(indexMap.keySet());
                changedIds.removeAll(deletedIds);
                changedMails = new ArrayList<MailMessage>(changedIds.size());
                for (final String mailId : changedIds) {
                    final MailMessage storageMail = storageMap.get(mailId);
                    if (isDifferent(storageMail, indexMap.get(mailId))) {
                        storageMail.setAccountId(accountId);
                        storageMail.setFolder(fullName);
                        storageMail.setMailId(mailId);
                        changedMails.add(storageMail);
                    }
                }
            }
            /*
             * Delete
             */
            if (!deletedIds.isEmpty()) {
                // Iterate identifiers
                for (final String id : deletedIds) {
                    final MailUUID uuid = new MailUUID(contextId, userId, accountId, fullName, id);
                    indexAccess.deleteById(uuid.getUUID());
                }
                setTimestamp(fullName, System.currentTimeMillis());
                if (LOG.isDebugEnabled()) {
                    LOG.debug(deletedIds.size() + " mails deleted from index; folder job: " + info);
                }
            }
            deletedIds = null;
            /*
             * Change flags
             */
            if (!changedMails.isEmpty()) {
                final int configuredBlockSize = getBlockSize();
                if (configuredBlockSize <= 0) {
                    indexAccess.change(toDocuments(changedMails), null);
                    setTimestamp(fullName, System.currentTimeMillis());
                } else {
                    final int size = changedMails.size();
                    int start = 0;
                    while (start < size) {
                        int end = start + configuredBlockSize;
                        if (end > size) {
                            end = size;
                        }
                        /*
                         * Change chunk
                         */
                        indexAccess.change(toDocuments(changedMails.subList(start, end)), null);
                        start = end;
                        setTimestamp(fullName, System.currentTimeMillis());
                    }
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug(changedMails.size() + " mails changed (flags) in index; folder job: " + info);
                }
            }
            changedMails = null;
            /*-
             * Add
             * 
             * http://www.mozgoweb.com/posts/how-to-parse-mime-message-using-mime4j-library/
             */
            if (!newIds.isEmpty()) {
                final List<String> ids = new ArrayList<String>(newIds);
                newIds = null;
                final long st1 = LOG.isDebugEnabled() ? System.currentTimeMillis() : 0l;
                final int configuredBlockSize = getBlockSize();
                if (configuredBlockSize <= 0) {
                    add2Index(ids, fullName, indexAccess);
                    if (LOG.isDebugEnabled()) {
                        final long dur = System.currentTimeMillis() - st1;
                        LOG.debug("Folder job \"" + info + "\" inserted " + ids.size() + " messages in " + dur + "msec in folder " + fullName + " in account " + accountId);
                    }
                } else {
                    // Positive chunk size configured
                    final int size = ids.size();
                    int start = 0;
                    if (scheduleJobs()) {
                        final List<List<String>> lists = new LinkedList<List<String>>();
                        while (start < size) {
                            int end = start + configuredBlockSize;
                            if (end > size) {
                                end = size;
                            }
                            lists.add(ids.subList(start, end));
                        }
                        final IndexingService indexingService = Services.getService(IndexingService.class);
                        final CountDownLatch latch = new CountDownLatch(lists.size());
                        for (final List<String> subIds : lists) {
                            final AddByIDsJob addJob = new AddByIDsJob(fullName, info, insertType).setMailIds(subIds);
                            addJob.setBehavior(behavior);
                            addJob.setPriority(priority);
                            indexingService.addJob(new LatchedIndexingJob(addJob, latch));
                            if (LOG.isDebugEnabled()) {
                                final long dur = System.currentTimeMillis() - st1;
                                LOG.debug("Folder job \"" + info + "\" scheduled adding of " + subIds.size() + " messages in " + dur + "msec in folder " + fullName + " in account " + accountId);
                            }
                        }
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("\tFolder job \"" + info + "\" awaits completion of scheduled Add-Jobs...");
                        }
                        latch.await();
                        if (LOG.isDebugEnabled()) {
                            final long dur = System.currentTimeMillis() - st1;
                            LOG.debug("\tFolder job \"" + info + "\" completed after " + dur + " ms.");
                        }
                    } else {
                        while (start < size) {
                            int end = start + configuredBlockSize;
                            if (end > size) {
                                end = size;
                            }
                            /*
                             * Add chunk
                             */
                            add2Index(ids.subList(start, end), fullName, indexAccess);
                            if (LOG.isDebugEnabled()) {
                                final long dur = System.currentTimeMillis() - st1;
                                LOG.debug("Folder job \"" + info + "\" inserted " + end + " of " + size + " messages in " + dur + "msec in folder " + fullName + " in account " + accountId);
                            }
                            start = end;
                        }
                    }
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Folder job \"" + info + "\" added " + size + " messages.");
                    }
                }
            } else if (LOG.isDebugEnabled()) {
                LOG.debug("Folder job \"" + info + "\" detected no NEW messages in folder " + fullName + " in account " + accountId);
            }

            setTimestamp(fullName, System.currentTimeMillis());
            return true;
        } finally {
            if (LOG.isDebugEnabled()) {
                final long dur = System.currentTimeMillis() - st;
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Folder job \"" + info + "\" took " + dur + "msec for folder " + fullName + " in account " + accountId);
                }
            }
        }
    }

    private static int getContentRetrievalChunkSize() {
        return 0;
    }

    private void add2Index(final List<String> ids, final String fullName, final IndexAccess<MailMessage> indexAccess) throws OXException {
        List<IndexDocument<MailMessage>> documents = null;
        try {
            final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = storageAccess.mailAccessFor();
            /*
             * Specify fields
             */
            final MailFields fields = MailUtility.getIndexableFields(indexAccess);
            final List<MailMessage> mails = Arrays.asList(mailAccess.getMessageStorage().getMessages(
                fullName,
                ids.toArray(new String[ids.size()]),
                fields.toArray()));
            // Read primary content
            final IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
            if (messageStorage instanceof IMailMessageStorageExt) {
                // Message storage overrides getPrimaryContents()
                final int chunk = getContentRetrievalChunkSize();
                final int size = mails.size();
                if (chunk > 0) {
                    int start = 0;
                    while (start < size) {
                        int end = start + chunk;
                        if (end > size) {
                            end = size;
                        }
                        final String[] mailIds = new String[end - start];
                        int index = 0;
                        for (int i = start; i < end; i++) {
                            mailIds[index++] = mails.get(i).getMailId();
                        }
                        final String[] primaryContents = messageStorage.getPrimaryContents(fullName, mailIds);
                        index = 0;
                        for (int i = start; i < end; i++) {
                            mails.set(i, new ContentAwareMailMessage(primaryContents[index++], mails.get(i)));
                        }
                        start = end;
                    }
                } else {
                    final String[] mailIds = new String[1];
                    for (int i = 0; i < size; i++) {
                        final MailMessage message = mails.get(i);
                        mailIds[0] = message.getMailId();
                        final String[] primaryContents = messageStorage.getPrimaryContents(fullName, mailIds);
                        mails.set(i, new ContentAwareMailMessage(primaryContents[0], message));
                    }
                }
            }
            // Convert to IndexDocument
            documents = toDocuments(mails);
            switch (insertType) {
                case ENVELOPE:
                    indexAccess.addEnvelopeData(documents);
                    break;
                case BODY:
                    indexAccess.addContent(documents, true);
                    break;
                default:
                    indexAccess.addAttachments(documents, true);
                    break;
            }
            setTimestamp(fullName, System.currentTimeMillis());
        } catch (final OXException e) {
            if (null != documents) {
                storageAccess.releaseAccess();
                add2IndexNonBatch(ids, fullName, indexAccess);
            }
        } finally {
            storageAccess.releaseMailAccess();
        }
    }

    private void add2IndexNonBatch(final List<String> ids, final String fullName, final IndexAccess<MailMessage> indexAccess) throws OXException {
        List<IndexDocument<MailMessage>> documents = null;
        try {
            final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = storageAccess.mailAccessFor();
            /*
             * Specify fields
             */
            final MailFields fields = MailUtility.getIndexableFields(indexAccess);
            final List<MailMessage> mails = Arrays.asList(mailAccess.getMessageStorage().getMessages(
                fullName,
                ids.toArray(new String[ids.size()]),
                fields.toArray()));
            // Read primary content
            final IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
            if (messageStorage instanceof IMailMessageStorageExt) {
                // Message storage overrides getPrimaryContents()
                final int chunk = getContentRetrievalChunkSize();
                final int size = mails.size();
                if (chunk > 0) {
                    int start = 0;
                    while (start < size) {
                        int end = start + chunk;
                        if (end > size) {
                            end = size;
                        }
                        final String[] mailIds = new String[end - start];
                        int index = 0;
                        for (int i = start; i < end; i++) {
                            mailIds[index++] = mails.get(i).getMailId();
                        }
                        final String[] primaryContents = messageStorage.getPrimaryContents(fullName, mailIds);
                        index = 0;
                        for (int i = start; i < end; i++) {
                            mails.set(i, new ContentAwareMailMessage(primaryContents[index++], mails.get(i)));
                        }
                        start = end;
                    }
                } else {
                    final String[] mailIds = new String[1];
                    for (int i = 0; i < size; i++) {
                        final MailMessage message = mails.get(i);
                        mailIds[0] = message.getMailId();
                        final String[] primaryContents = messageStorage.getPrimaryContents(fullName, mailIds);
                        mails.set(i, new ContentAwareMailMessage(primaryContents[0], message));
                    }
                }
            }
            // Convert to IndexDocument
            documents = toDocuments(mails);
            int count = 0;
            for (final IndexDocument<MailMessage> document : documents) {
                try {
                    switch (insertType) {
                        case ENVELOPE:
                            indexAccess.addEnvelopeData(document);
                            break;
                        case BODY:
                            indexAccess.addContent(document, true);
                            break;
                        default:
                            indexAccess.addAttachments(document, true);
                            break;
                    }
                    if ((++count % 100) == 0) {
                        setTimestamp(fullName, System.currentTimeMillis());
                    }
                } catch (final Exception inner) {
                    final MailMessage mail = document.getObject();
                    LOG.warn(
                        "Mail " + mail.getMailId() + " from folder " + mail.getFolder() + " of account " + accountId + " could not be added to index.",
                        inner);
                }
            }
        } catch (final OXException e) {
            // One-by-one add failed, too
        } finally {
            storageAccess.releaseMailAccess();
        }
    }

    private boolean deleteDBEntry() throws OXException {
        return IndexFolderManager.deleteFolderEntry(contextId, userId, Types.EMAIL, String.valueOf(accountId), fullName);
    }

    /**
     * Checks if specified {@link MailMessage} instances are different by system flags, color flag, or user flags.
     * 
     * @param storageMail The mail fetched from storage
     * @param indexMail The mail fetched from index
     * @return <code>true</code> if provided mails are considered different; otherwise <code>false</code>
     */
    public static boolean isDifferent(final MailMessage storageMail, final MailMessage indexMail) {
        if (null == storageMail || null == indexMail) {
            return false;
        }
        /*
         * Check system flags
         */
        if (storageMail.getFlags() != indexMail.getFlags()) {
            return true;
        }
        /*
         * Check color label
         */
        if (storageMail.getColorLabel() != indexMail.getColorLabel()) {
            return true;
        }
        /*
         * Check user flags
         */
        final Set<String> storageUserFlags;
        {
            final String[] stoUserFlags = storageMail.getUserFlags();
            storageUserFlags = null == stoUserFlags ? Collections.<String> emptySet() : new HashSet<String>(Arrays.asList(stoUserFlags));
        }
        final Set<String> indexUserFlags;
        {
            final String[] idxUserFlags = indexMail.getUserFlags();
            indexUserFlags = null == idxUserFlags ? Collections.<String> emptySet() : new HashSet<String>(Arrays.asList(idxUserFlags));
        }
        return (!storageUserFlags.equals(indexUserFlags));
    }

    private static final class LatchedIndexingJob implements IndexingJob {

        private static final long serialVersionUID = -9073396036107129988L;

        private final IndexingJob delegate;

        private final CountDownLatch latch;

        protected LatchedIndexingJob(final IndexingJob job, final CountDownLatch latch) {
            super();
            delegate = job;
            this.latch = latch;
        }

        @Override
        public Map<String, ?> getProperties() {
            return delegate.getProperties();
        }

        @Override
        public Class<?>[] getNeededServices() {
            return delegate.getNeededServices();
        }

        @Override
        public void performJob() throws OXException, InterruptedException {
            delegate.performJob();
        }

        @Override
        public boolean isDurable() {
            return delegate.isDurable();
        }

        @Override
        public int getPriority() {
            return delegate.getPriority();
        }

        @Override
        public void setPriority(final int priority) {
            delegate.setPriority(priority);
        }

        @Override
        public long getTimeStamp() {
            return delegate.getTimeStamp();
        }

        @Override
        public Origin getOrigin() {
            return Origin.PASSIVE;
        }

        @Override
        public Behavior getBehavior() {
            return delegate.getBehavior();
        }

        @Override
        public void beforeExecute() {
            delegate.beforeExecute();
        }

        @Override
        public void afterExecute(final Throwable t) {
            try {
                delegate.afterExecute(t);
                latch.countDown();
            } catch (final RuntimeException rte) {
                latch.countDown();
                throw rte;
            }
        }
    }

}

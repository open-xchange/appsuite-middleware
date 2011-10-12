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

package com.openexchange.mail.smal.jobqueue.jobs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import com.openexchange.database.DatabaseService;
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
import com.openexchange.mail.smal.SMALMailAccess;
import com.openexchange.mail.smal.SMALServiceLookup;
import com.openexchange.mail.smal.adapter.IndexAdapter;
import com.openexchange.mail.smal.jobqueue.Constants;
import com.openexchange.mail.smal.jobqueue.Job;
import com.openexchange.mail.smal.jobqueue.JobCompletionService;
import com.openexchange.mail.smal.jobqueue.JobQueue;
import com.openexchange.mail.smal.jobqueue.Jobs;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link FolderJob}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FolderJob extends AbstractMailSyncJob {

    private static final long serialVersionUID = -4811521171077091128L;

    private static final String SIMPLE_NAME = FolderJob.class.getSimpleName();

    protected static final org.apache.commons.logging.Log LOG =
        com.openexchange.log.Log.valueOf(org.apache.commons.logging.LogFactory.getLog(FolderJob.class));

    protected static final boolean DEBUG = LOG.isDebugEnabled();

    private static final int GATE_PERFORM = -1;

    private static final int GATE_OPEN = 0;

    private static final int GATE_REPLACE = 1;

    protected final String fullName;

    protected final String identifier;

    private final AtomicInteger gate;

    private volatile boolean ignoreDeleted;

    private volatile int ranking;

    private volatile boolean reEnqueued;

    private volatile boolean error;

    private volatile long span;

    private volatile List<MailMessage> storageMails;

    private volatile List<MailMessage> indexMails;

    /**
     * Initializes a new {@link FolderJob} with default span.
     * <p>
     * This job is performed is span is exceeded and if able to exclusively set sync flag.
     * 
     * @param fullName The folder full name
     * @param accountId The account ID
     * @param userId The user ID
     * @param contextId The context ID
     */
    public FolderJob(final String fullName, final int accountId, final int userId, final int contextId) {
        super(accountId, userId, contextId);
        gate = new AtomicInteger(0);
        ranking = 0;
        this.fullName = fullName;
        identifier =
            new StringBuilder(SIMPLE_NAME).append('@').append(contextId).append('@').append(userId).append('@').append(accountId).append(
                '@').append(fullName).toString();
        span = Constants.DEFAULT_MILLIS;
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
     * Sets the ranking
     *
     * @param ranking The ranking to set
     * @return This folder job with specified ranking applied
     */
    public FolderJob setRanking(final int ranking) {
        this.ranking = ranking;
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
        this.indexMails = indexMail;
        return this;
    }

    @Override
    public void replaceWith(final Job anotherJob) {
        if (!identifier.equals(anotherJob.getIdentifier())) {
            return;
        }
        int state;
        do {
            state = gate.get();
            if (GATE_PERFORM == state) {
                // Already performed
                return;
            }
        } while (state != GATE_OPEN || !gate.compareAndSet(state, GATE_REPLACE));
        final FolderJob anotherFolderJob = (FolderJob) anotherJob;
        this.ranking = anotherFolderJob.ranking;
        this.span = anotherFolderJob.span;
        this.storageMails = anotherFolderJob.storageMails;
        this.indexMails = anotherFolderJob.indexMails;
        gate.set(0);
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public int getRanking() {
        return ranking;
    }

    private static final MailField[] FIELDS = new MailField[] { MailField.ID, MailField.FLAGS };

    @Override
    public void perform() {
        if (error) {
            cancel();
            return;
        }
        int state;
        do {
            state = gate.get();
            if (GATE_PERFORM == state) {
                // Already performed?
                return;
            }
        } while (state != GATE_OPEN || !gate.compareAndSet(state, GATE_PERFORM));
        try {
            if (reEnqueued) {
                reEnqueued = false;
            } else {
                final long now = System.currentTimeMillis();
                try {
                    if ((span > 0 ? !shouldSync(fullName, now, span) : false) || !wasAbleToSetSyncFlag(fullName, now)) {
                        return;
                    }
                } catch (final OXException e) {
                    LOG.error("Couldn't look-up database.", e);
                }
            }
            /*
             * Sync mails with index...
             */
            final long st = DEBUG ? System.currentTimeMillis() : 0L;
            boolean unset = true;
            try {
                final IndexAdapter indexAdapter = getAdapter();
                final List<MailMessage> mails;
                final Session session;
                if (null == storageMails) {
                    MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
                    try {
                        mailAccess = SMALMailAccess.getUnwrappedInstance(userId, contextId, accountId);
                        session = mailAccess.getSession();
                        /*
                         * Get the mails from mail storage
                         */
                        mailAccess.connect(true);
                        /*
                         * At first check existence of denoted folder
                         */
                        if (!mailAccess.getFolderStorage().exists(fullName)) {
                            /*
                             * Drop entry from database and return
                             */
                            deleteDBEntry();
                            unset = false;
                            return;
                        }
                        /*
                         * Fetch mails
                         */
                        mails =
                            Arrays.asList(mailAccess.getMessageStorage().searchMessages(
                                fullName,
                                IndexRange.NULL,
                                MailSortField.RECEIVED_DATE,
                                OrderDirection.ASC,
                                null,
                                FIELDS));
                    } finally {
                        SMALMailAccess.closeUnwrappedInstance(mailAccess);
                        mailAccess = null;
                    }
                } else {
                    mails = storageMails;
                    session = SMALServiceLookup.getServiceStatic(SessiondService.class).getAnyActiveSessionForUser(userId, contextId);
                }
                final Map<String, MailMessage> storageMap;
                if (mails.isEmpty()) {
                    storageMap = Collections.emptyMap();
                } else {
                    storageMap = new HashMap<String, MailMessage>(mails.size());
                    for (final MailMessage mailMessage : mails) {
                        storageMap.put(mailMessage.getMailId(), mailMessage);
                    }
                }
                /*
                 * Get the mails from index
                 */
                List<MailMessage> indexedMails = this.indexMails;
                if (null == indexedMails) {
                    indexedMails = indexAdapter.search(fullName, null, null, null, FIELDS, accountId, session);
                }
                final Map<String, MailMessage> indexMap;
                if (indexedMails.isEmpty()) {
                    indexMap = Collections.emptyMap();
                } else {
                    indexMap = new HashMap<String, MailMessage>(indexedMails.size());
                    for (final MailMessage mailMessage : indexedMails) {
                        indexMap.put(mailMessage.getMailId(), mailMessage);
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
                Set<String> changedIds = new HashSet<String>(indexMap.keySet());
                List<MailMessage> changedMails = new ArrayList<MailMessage>(changedIds.size());
                changedIds.removeAll(deletedIds);
                for (final Iterator<String> iterator = changedIds.iterator(); iterator.hasNext();) {
                    final String mailId = iterator.next();
                    final MailMessage storageMail = storageMap.get(mailId);
                    final MailMessage indexMail = indexMap.get(mailId);
                    boolean different = false;
                    if (storageMail.getFlags() != indexMail.getFlags()) {
                        storageMail.setAccountId(accountId);
                        storageMail.setFolder(fullName);
                        storageMail.setMailId(mailId);
                        changedMails.add(storageMail);
                        different = true;
                    }
                    if (storageMail.getFlags() != indexMail.getFlags()) {
                        storageMail.setAccountId(accountId);
                        storageMail.setFolder(fullName);
                        storageMail.setMailId(mailId);
                        changedMails.add(storageMail);
                        different = true;
                    }
                    if (different) {
                        iterator.remove();
                    }
                }
                changedIds = null;
                /*
                 * Delete
                 */
                indexAdapter.deleteMessages(deletedIds, fullName, accountId, session);
                deletedIds = null;
                /*
                 * Change flags
                 */
                indexAdapter.change(changedMails, session);
                changedMails = null;
                /*
                 * Add
                 */
                if (!newIds.isEmpty()) {
                    final int blockSize;
                    final int size = newIds.size();
                    {
                        final int configuredBlockSize = Constants.CHUNK_SIZE;
                        blockSize = configuredBlockSize > size ? size : configuredBlockSize;
                    }
                    final List<String> ids = new ArrayList<String>(newIds);
                    newIds = null;
                    int start = 0;
                    try {
                        final JobQueue queue = JobQueue.getInstance();
                        final long now = System.currentTimeMillis();
                        int cnt = 0;
                        final JobCompletionService completionService = new JobCompletionService(0);
	                    while (start < size) {
	                    	final int end;
	                    	{
	                    	    int tmp = start + blockSize;
	                            if (tmp > size) {
	                                tmp = size;
	                            }
	                            end = tmp;
	                    	}
	                        /*
	                         * A new job
	                         */
	                    	final int strt = start;
	                        final Callable<Object> callable = new Callable<Object>() {

                                @Override
                                public Object call() throws Exception {
                                    // Add chunk to index
                                    add2Index(ids.subList(strt, end), fullName, indexAdapter);
                                    if (DEBUG) {
                                        final long dur = System.currentTimeMillis() - st;
                                        LOG.debug("Folder job \"" + identifier + "\" inserted " + end + " of " + size + " messages in " + dur + "msec in folder " + fullName + " in account " + accountId);
                                    }
                                    return null;
                                }
                            };
                            final Job adderJob = Jobs.jobFor(callable, identifier + '@' + String.valueOf(now+(cnt+1)), ranking);
                            if(completionService.addJob(adderJob)) {
                                cnt++;
                            } else {
                                // Add chunk to index
                                add2Index(ids.subList(strt, end), fullName, indexAdapter);
                                if (DEBUG) {
                                    final long dur = System.currentTimeMillis() - st;
                                    LOG.debug("Folder job \"" + identifier + "\" inserted " + end + " of " + size + " messages in " + dur + "msec in folder " + fullName + " in account " + accountId);
                                }
                            }
                            if (queue.hasHigherRankedJobInQueue(getRanking())) {
                                if (DEBUG) {
                                    LOG.debug("Folder job \"" + identifier + "\" aborted temporarily because a higher-ranked job is available in job queue.");
                                }
                                break;
                            }
                            start = end;
	                    }
	                    for (int i = 0; i < cnt; i++) {
                            completionService.take();
                        }
	                    if (DEBUG) {
                            LOG.debug("Folder job \"" + identifier + "\" added " + size + " messages.");
                        }
                    } finally {
                        if (DEBUG) {
                            LOG.debug("Folder job \"" + identifier + "\" triggers to add messages' content.");
                        }
                    	indexAdapter.addContents();
                    }
                    reEnqueued = (start < size);
                } else if (DEBUG) {
                    LOG.debug("Folder job \"" + identifier + "\" detected no new messages in folder " + fullName + " in account " + accountId);
                }
                setTimestampAndUnsetSyncFlag(fullName, System.currentTimeMillis());
                unset = false;
            } finally {
                if (unset) {
                    // Unset sync flag
                    unsetSyncFlag(fullName);
                }
                if (DEBUG) {
                    final long dur = System.currentTimeMillis() - st;
                    if (DEBUG) {
                        LOG.debug("Folder job \"" + identifier + "\" took " + dur + "msec for folder " + fullName + " in account " + accountId);
                    }
                }
            }
            if (reEnqueued) {
                reset();
                JobQueue.getInstance().addJob(this);
            }
        } catch (final InterruptedException e) {
            // Keep interrupted state
            Thread.currentThread().interrupt();
            error = true;
            cancel();
            LOG.error("Folder job \"" + identifier + "\" failed.", e);
        } catch (final Exception e) {
            error = true;
            cancel();
            LOG.error("Folder job \"" + identifier + "\" failed.", e);
        }
    }

    @Override
    public void cancel() {
        try {
            dropFolderEntry(fullName);
        } catch (final OXException e) {
            // Entry could not be removed
            LOG.warn("Entry for failed folder job \"" + identifier + "\" could not be removed.", e);
        } finally {
            super.cancel();
        }
    }

    protected void add2Index(final List<String> ids, final String fullName, final IndexAdapter indexAdapter) throws OXException {
        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
        try {
            mailAccess = SMALMailAccess.getUnwrappedInstance(userId, contextId, accountId);
            final Session session = mailAccess.getSession();
            mailAccess.connect(true);
            /*
             * Specify fields
             */
            final MailFields fields = new MailFields(indexAdapter.getIndexableFields());
            fields.removeMailField(MailField.BODY);
            fields.removeMailField(MailField.FULL);
            final MailMessage[] mails = mailAccess.getMessageStorage().getMessages(
                fullName,
                ids.toArray(new String[ids.size()]),
                fields.toArray());
            try {
                indexAdapter.add(Arrays.asList(mails), session);
            } catch (final OXException e) {
                // Batch add failed; retry one-by-one
                for (final MailMessage mail : mails) {
                    try {
                        indexAdapter.add(mail, session);
                    } catch (final Exception inner) {
                        LOG.warn(
                            "Mail " + mail.getMailId() + " from folder " + mail.getFolder() + " of account " + accountId + " could not be added to index.",
                            inner);
                    }
                }
            } catch (final InterruptedException e) {
                // Keep interrupted state
                Thread.currentThread().interrupt();
                throw MailExceptionCode.INTERRUPT_ERROR.create(e, e.getMessage());
            }
        } finally {
            SMALMailAccess.closeUnwrappedInstance(mailAccess);
        }
    }

    private boolean deleteDBEntry() throws OXException {
        final DatabaseService databaseService = SMALServiceLookup.getServiceStatic(DatabaseService.class);
        if (null == databaseService) {
            return false;
        }
        final Connection con = databaseService.getWritable(contextId);
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("DELETE FROM mailSync WHERE cid = ? AND user = ? AND accountId = ? AND fullName = ?");
            int pos = 1;
            stmt.setLong(pos++, contextId);
            stmt.setLong(pos++, userId);
            stmt.setLong(pos++, accountId);
            stmt.setString(pos, fullName);
            return stmt.executeUpdate() > 0;
        } catch (final SQLException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
            databaseService.backWritable(contextId, con);
        }
    }

}

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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import com.openexchange.service.indexing.mail.Constants;
import com.openexchange.service.indexing.mail.MailJobInfo;
import com.openexchange.session.Session;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link FolderJob}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FolderJob extends AbstractMailJob {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(FolderJob.class));

    private static final boolean DEBUG = LOG.isDebugEnabled();

    private static final long serialVersionUID = -4811521171077091128L;

    private final class Add2IndexRunnable implements Runnable {

        private final IndexAdapter indexAdapter;

        private final List<String> ids;

        private final Session session;

        protected Add2IndexRunnable(final List<String> ids, final IndexAdapter indexAdapter, final Session session) {
            super();
            this.indexAdapter = indexAdapter;
            this.ids = ids;
            this.session = session;
        }

        @Override
        public void run() {
            try {
                add2Index(ids, fullName, indexAdapter, session);
            } catch (final OXException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static final String SIMPLE_NAME = FolderJob.class.getSimpleName();

    protected final String fullName;

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
        super(info);
        this.fullName = fullName;
        span = com.openexchange.service.indexing.mail.Constants.DEFAULT_MILLIS;
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
        this.indexMails = indexMail;
        return this;
    }

    private static final MailField[] FIELDS = new MailField[] { MailField.ID, MailField.FLAGS, MailField.COLOR_LABEL };

    @Override
    public void performJob() throws OXException, InterruptedException {
        final boolean debug = LOG.isDebugEnabled();
        try {
            final long now = System.currentTimeMillis();
            try {
                if ((span > 0 ? !shouldSync(fullName, now, span) : false) || !wasAbleToSetSyncFlag(fullName, now)) {
                    if (debug) {
                        LOG.debug("Folder job should not yet be performed or wasn't able to acquire 'sync' flag: " + info);
                    }
                    return;
                }
            } catch (final OXException e) {
                LOG.error("Couldn't look-up database.", e);
            }
            /*
             * Sync mails with index...
             */
            final long st = DEBUG ? System.currentTimeMillis() : 0L;
            boolean unset = true;
            try {
                if (debug) {
                    LOG.debug("Starting folder job: " + info);
                }
                /*
                 * Get the mails from storage
                 */
                final Session session;
                final Map<String, MailMessage> storageMap;
                {
                    final List<MailMessage> mails;
                    MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
                    try {
                        mailAccess = mailAccessFor();
                        mailAccess.connect(true);
                        session = mailAccess.getSession();
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
                        if (null == storageMails) {
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
                        } else {
                            mails = storageMails;
                        }
                    } finally {
                        SMALMailAccess.closeUnwrappedInstance(mailAccess);
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
                    if (debug) {
                        LOG.debug(storageMap.size() + " mails from storage; folder job: " + info);
                    }
                }
                /*
                 * Get the mails from index
                 */
                final IndexAdapter indexAdapter = getAdapter();
                final Map<String, MailMessage> indexMap;
                {
                    List<MailMessage> indexedMails = this.indexMails;
                    if (null == indexedMails) {
                        indexedMails = indexAdapter.all(fullName, accountId, session);
                    }
                    if (indexedMails.isEmpty()) {
                        indexMap = Collections.emptyMap();
                    } else {
                        indexMap = new HashMap<String, MailMessage>(indexedMails.size());
                        for (final MailMessage mailMessage : indexedMails) {
                            indexMap.put(mailMessage.getMailId(), mailMessage);
                        }
                    }
                    if (debug) {
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
                    changedMails = new ArrayList<MailMessage>(changedIds.size());
                    changedIds.removeAll(deletedIds);
                    for (final Iterator<String> iterator = changedIds.iterator(); iterator.hasNext();) {
                        final String mailId = iterator.next();
                        final MailMessage storageMail = storageMap.get(mailId);
                        if (null == storageMail) {
                            iterator.remove();
                        } else {
                            final MailMessage indexMail = indexMap.get(mailId);
                            boolean different = false;
                            if (storageMail.getFlags() != indexMail.getFlags()) { // Different flags
                                storageMail.setAccountId(accountId);
                                storageMail.setFolder(fullName);
                                storageMail.setMailId(mailId);
                                changedMails.add(storageMail);
                                different = true;
                            }
                            if (storageMail.getColorLabel() != indexMail.getColorLabel()) { // Different color label
                                storageMail.setAccountId(accountId);
                                storageMail.setFolder(fullName);
                                storageMail.setMailId(mailId);
                                changedMails.add(storageMail);
                                different = true;
                            }
                            final String[] stoUserFlags = storageMail.getUserFlags();
                            final Set<String> storageUserFlags =
                                null == stoUserFlags ? Collections.<String> emptySet() : new HashSet<String>(Arrays.asList(stoUserFlags));
                            final String[] idxUserFlags = indexMail.getUserFlags();
                            final Set<String> indexUserFlags =
                                null == idxUserFlags ? Collections.<String> emptySet() : new HashSet<String>(Arrays.asList(idxUserFlags));
                            if (!storageUserFlags.equals(indexUserFlags)) { // Different user flags
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
                    }
                }
                /*
                 * Delete
                 */
                if (!deletedIds.isEmpty()) {
                    indexAdapter.deleteMessages(deletedIds, fullName, accountId, session);
                    if (debug) {
                        LOG.debug(deletedIds.size() + " mails deleted from index; folder job: " + info);
                    }
                }
                deletedIds = null;
                /*
                 * Change flags
                 */
                if (!changedMails.isEmpty()) {
                    indexAdapter.change(changedMails, session);
                    if (debug) {
                        LOG.debug(changedMails.size() + " mails changed (flags) in index; folder job: " + info);
                    }
                }
                changedMails = null;
                /*
                 * Add
                 */
                if (!newIds.isEmpty()) {
                    final List<String> ids = new ArrayList<String>(newIds);
                    newIds = null;
                    try {
                        chunkedAddWithoutJobs(ids, indexAdapter, session);
                    } finally {
                        if (DEBUG) {
                            LOG.debug("Folder job \"" + info + "\" triggers to add messages' content.");
                        }
                        indexAdapter.addContents();
                    }
                } else if (DEBUG) {
                    LOG.debug("Folder job \"" + info + "\" detected no NEW messages in folder " + fullName + " in account " + accountId);
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
                        LOG.debug("Folder job \"" + info + "\" took " + dur + "msec for folder " + fullName + " in account " + accountId);
                    }
                }
            }
        } catch (final RuntimeException e) {
            LOG.warn(SIMPLE_NAME + " failed: " + info, e);
        }
    }

    private void chunkedAddWithoutJobs(final List<String> ids, final IndexAdapter indexAdapter, final Session session) throws OXException {
        final long st = DEBUG ? System.currentTimeMillis() : 0l;
        final int configuredBlockSize = Constants.CHUNK_SIZE;
        if (configuredBlockSize <= 0) {
            add2Index(ids, fullName, indexAdapter, session);
            if (DEBUG) {
                final long dur = System.currentTimeMillis() - st;
                LOG.debug("Folder job \"" + info + "\" inserted " + ids.size() + " messages in " + dur + "msec in folder " + fullName + " in account " + accountId);
            }
            return;
        }
        // Positive chunk size configured
        final int size = ids.size();
        int start = 0;
        while (start < size) {
            int end = start + configuredBlockSize;
            if (end > size) {
                end = size;
            }
            /*
             * Add chunk
             */
            add2Index(ids.subList(start, end), fullName, indexAdapter, session);
            if (DEBUG) {
                final long dur = System.currentTimeMillis() - st;
                LOG.debug("Folder job \"" + info + "\" inserted " + end + " of " + size + " messages in " + dur + "msec in folder " + fullName + " in account " + accountId);
            }
            start = end;
        }
        if (DEBUG) {
            LOG.debug("Folder job \"" + info + "\" added " + size + " messages.");
        }
    }

    protected void add2Index(final List<String> ids, final String fullName, final IndexAdapter indexAdapter, final Session session) throws OXException {
        final MailMessage[] mails = loadMessages(ids, fullName, indexAdapter);
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
    }

    private MailMessage[] loadMessages(final List<String> ids, final String fullName, final IndexAdapter indexAdapter) throws OXException {
        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
        try {
            mailAccess = mailAccessFor();
            mailAccess.connect(true);
            /*
             * Specify fields
             */
            final MailFields fields = new MailFields(indexAdapter.getIndexableFields());
            fields.removeMailField(MailField.BODY);
            fields.removeMailField(MailField.FULL);
            return mailAccess.getMessageStorage().getMessages(fullName, ids.toArray(new String[ids.size()]), fields.toArray());
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

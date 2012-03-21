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
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.index.IndexAccess;
import com.openexchange.index.IndexDocument;
import com.openexchange.index.IndexResult;
import com.openexchange.index.QueryParameters;
import com.openexchange.index.solr.mail.SolrMailUtility;
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
import com.openexchange.service.indexing.impl.Services;
import com.openexchange.service.indexing.mail.Constants;
import com.openexchange.service.indexing.mail.MailJobInfo;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link FolderJob}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FolderJob extends AbstractMailJob {

    private static final long serialVersionUID = 2093998857641164982L;

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(FolderJob.class));

    private static final boolean DEBUG = LOG.isDebugEnabled();

    private static final String SIMPLE_NAME = FolderJob.class.getSimpleName();

    protected final String fullName;

    private final InsertType insertType;

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
     * @param insertType The insert type for new mails; {@link InsertType#ATTACHMENTS} is default
     */
    public FolderJob(final String fullName, final MailJobInfo info, final InsertType insertType) {
        super(info);
        this.fullName = fullName;
        span = com.openexchange.service.indexing.mail.Constants.DEFAULT_MILLIS;
        this.insertType = null == insertType ? InsertType.ATTACHMENTS : insertType;
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

    /**
     * ID, FLAGS, and COLOR_LABEL
     */
    private static final MailField[] FIELDS = new MailField[] { MailField.ID, MailField.FLAGS, MailField.COLOR_LABEL };

    @Override
    public void performJob() throws OXException, InterruptedException {
        final boolean debug = LOG.isDebugEnabled();
        IndexAccess<MailMessage> indexAccess = null;
        try {
            /*
             * Check against table entry if allowed to be run
             */
            try {
                final long now = System.currentTimeMillis();
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
                final Map<String, MailMessage> storageMap;
                {
                    final List<MailMessage> mails;
                    MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
                    try {
                        mailAccess = mailAccessFor();
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
                        getSmalAccessService().closeUnwrappedInstance(mailAccess);
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
                indexAccess = getIndexAccess();
                final Map<String, MailMessage> indexMap;
                {
                    List<MailMessage> indexedMails = this.indexMails;
                    if (null == indexedMails) {
                        final String queryString;
                        {
                            final StringBuilder queryBuilder = new StringBuilder(128);
                            queryBuilder.append('(').append(FIELD_USER).append(':').append(userId).append(')');
                            queryBuilder.append(" AND (").append(FIELD_CONTEXT).append(':').append(contextId).append(')');
                            queryBuilder.append(" AND (").append(FIELD_ACCOUNT).append(':').append(accountId).append(')');
                            queryBuilder.append(" AND (").append(FIELD_FULL_NAME).append(":\"").append(fullName).append("\")");
                            queryString = queryBuilder.toString();
                        }
                        final Map<String, Object> params = new HashMap<String, Object>(4);
                        // TODO: params.put("fields", mailFields);
                        params.put("sort", FIELD_RECEIVED_DATE);
                        params.put("order", "desc");
                        final QueryParameters queryParameter =
                            new QueryParameters.Builder(queryString).setOffset(0).setLength(Integer.MAX_VALUE).setType(
                                IndexDocument.Type.MAIL).setParameters(params).build();
                        final IndexResult<MailMessage> indexResult = indexAccess.query(queryParameter);
                        if (0 >= indexResult.getNumFound()) {
                            indexedMails = Collections.emptyList();
                        } else {
                            final List<IndexDocument<MailMessage>> results = indexResult.getResults();
                            final List<MailMessage> mails = new ArrayList<MailMessage>(results.size());
                            for (final IndexDocument<MailMessage> indexDocument : results) {
                                mails.add(indexDocument.getObject());
                            }
                            indexedMails = mails;
                        }
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
                    final StringBuilder queryBuilder = new StringBuilder(128);
                    queryBuilder.append('(').append(FIELD_USER).append(':').append(userId).append(')');
                    queryBuilder.append(" AND (").append(FIELD_CONTEXT).append(':').append(contextId).append(')');
                    queryBuilder.append(" AND (").append(FIELD_ACCOUNT).append(':').append(accountId).append(')');
                    queryBuilder.append(" AND (").append(FIELD_FULL_NAME).append(":\"").append(fullName).append("\")");
                    final int resLen = queryBuilder.length();
                    // Iterate identifiers
                    for (final String id : deletedIds) {
                        queryBuilder.setLength(resLen);
                        queryBuilder.append(" AND (").append(FIELD_ID).append(":\"").append(id).append("\")");
                        indexAccess.deleteByQuery(queryBuilder.toString());
                    }
                    if (debug) {
                        LOG.debug(deletedIds.size() + " mails deleted from index; folder job: " + info);
                    }
                }
                deletedIds = null;
                /*
                 * Change flags
                 */
                if (!changedMails.isEmpty()) {
                    indexAccess.change(toDocuments(changedMails), IndexAccess.ALL_FIELDS);
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
                        chunkedAdd(ids, indexAccess);
                    } finally {
                        if (DEBUG) {
                            LOG.debug("Folder job \"" + info + "\" triggers to add messages' content.");
                        }
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
        } finally {
            releaseAccess(indexAccess);
        }
    }

    private void chunkedAdd(final List<String> ids, final IndexAccess<MailMessage> indexAccess) throws OXException {
        final long st = DEBUG ? System.currentTimeMillis() : 0l;
        final int configuredBlockSize = Constants.CHUNK_SIZE;
        if (configuredBlockSize <= 0) {
            add2Index(ids, fullName, indexAccess);
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
            add2Index(ids.subList(start, end), fullName, indexAccess);
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

    protected void add2Index(final List<String> ids, final String fullName, final IndexAccess<MailMessage> indexAccess) throws OXException {
        final List<MailMessage> mails = loadMessages(ids, fullName);
        final List<IndexDocument<MailMessage>> documents = toDocuments(mails);
        try {
            switch (insertType) {
            case ENVELOPE:
                indexAccess.addEnvelopeData(documents);
                break;
            case BODY:
                indexAccess.addContent(documents);
                break;
            default:
                indexAccess.addAttachments(documents);
                break;
            }
        } catch (final OXException e) {
            // Batch add failed; retry one-by-one
            for (final IndexDocument<MailMessage> document : documents) {
                try {
                    indexAccess.addAttachments(document);
                } catch (final Exception inner) {
                    final MailMessage mail = document.getObject();
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

    private List<MailMessage> loadMessages(final List<String> ids, final String fullName) throws OXException {
        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
        try {
            mailAccess = mailAccessFor();
            mailAccess.connect(true);
            /*
             * Specify fields
             */
            final MailFields fields = new MailFields(SolrMailUtility.getIndexableFields());
            fields.removeMailField(MailField.BODY);
            fields.removeMailField(MailField.FULL);
            return Arrays.asList(mailAccess.getMessageStorage().getMessages(fullName, ids.toArray(new String[ids.size()]), fields.toArray()));
        } finally {
            getSmalAccessService().closeUnwrappedInstance(mailAccess);
        }
    }

    private boolean deleteDBEntry() throws OXException {
        final DatabaseService databaseService = Services.getService(DatabaseService.class);
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
            storageUserFlags =
                null == stoUserFlags ? Collections.<String> emptySet() : new HashSet<String>(Arrays.asList(stoUserFlags));
        }
        final Set<String> indexUserFlags;
        {
            final String[] idxUserFlags = indexMail.getUserFlags();
            indexUserFlags =
                null == idxUserFlags ? Collections.<String> emptySet() : new HashSet<String>(Arrays.asList(idxUserFlags));
        }
        return (!storageUserFlags.equals(indexUserFlags));
    }
    
}

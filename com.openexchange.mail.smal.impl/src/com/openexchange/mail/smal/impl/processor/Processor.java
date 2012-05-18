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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.mail.smal.impl.processor;

import static com.openexchange.index.solr.mail.SolrMailUtility.releaseAccess;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import org.apache.commons.logging.Log;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.index.IndexAccess;
import com.openexchange.index.IndexDocument;
import com.openexchange.index.IndexFacadeService;
import com.openexchange.log.LogFactory;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailFolderStorageEnhanced;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.smal.impl.DebugInfo;
import com.openexchange.mail.smal.impl.SmalExceptionCodes;
import com.openexchange.mail.smal.impl.SmalMailAccess;
import com.openexchange.mail.smal.impl.SmalServiceLookup;
import com.openexchange.mail.smal.impl.index.IndexAccessAdapter;
import com.openexchange.mail.smal.impl.index.IndexDocumentHelper;
import com.openexchange.service.indexing.IndexingService;
import com.openexchange.service.indexing.mail.MailJobInfo;
import com.openexchange.service.indexing.mail.MailJobInfo.Builder;
import com.openexchange.service.indexing.mail.job.FolderJob;
import com.openexchange.session.Session;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.threadpool.behavior.CallerRunsBehavior;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link Processor} - Processes a given mail folder for its content being indexed.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Processor {

    /**
     * The logger constant.
     */
    protected static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(Processor.class));

    /**
     * Whether debug logging is enabled for this class.
     */
    protected static final boolean DEBUG = LOG.isDebugEnabled();

    /**
     * The singleton instance.
     */
    private static final Processor INSTANCE = new Processor();

    /**
     * Gets the default instance.
     * 
     * @return The default instance
     */
    public static Processor getInstance() {
        return INSTANCE;
    }

    /**
     * The strategy to follow.
     */
    private final ProcessorStrategy strategy;

    /**
     * Initializes a new {@link Processor}.
     */
    private Processor() {
        this(DefaultProcessorStrategy.getInstance());
    }

    /**
     * Initializes a new {@link Processor}.
     * 
     * @param strategy The strategy to lookup high attention folders
     */
    public Processor(final ProcessorStrategy strategy) {
        super();
        assert null != strategy;
        this.strategy = strategy;
    }

    /**
     * (Asynchronously) Processes specified mail folder for its content being indexed.
     * <p>
     * In case of immediate processing, the following steps were performed:
     * <ul>
     * <li>Adds missing documents to index</li>
     * <li>Deletes removed documents from index</li>
     * </ul>
     * 
     * @param mailFolder The mail folder
     * @param mailAccess The (opened) mail access
     * @param params The optional parameters
     * @return The processing result or {@link ProcessingProgress#EMPTY_RESULT an empty result} if processing has been aborted
     * @throws OXException If an error occurs
     * @throws InterruptedException If processing is interrupted
     */
    public ProcessingProgress processFolder(final MailFolder mailFolder, final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess, final Map<String, Object> params) throws OXException, InterruptedException {
        if (mailFolder.isHoldsMessages() && mailFolder.getMessageCount() < 0) {
            final IMailFolderStorage folderStorage = mailAccess.getFolderStorage();
            if (folderStorage instanceof IMailFolderStorageEnhanced) {
                final IMailFolderStorageEnhanced storageEnhanced = (IMailFolderStorageEnhanced) folderStorage;
                final String fullName = mailFolder.getFullname();
                return processFolder(
                    new MailFolderInfo(fullName, storageEnhanced.getTotalCounter(fullName)),
                    mailAccess.getAccountId(),
                    mailAccess.getSession(),
                    params);
            }
        }
        return processFolder(mailFolder, mailAccess.getAccountId(), mailAccess.getSession(), params);
    }

    /**
     * (Asynchronously) Processes specified mail folder for its content being indexed.
     * <p>
     * In case of immediate processing, the following steps were performed:
     * <ul>
     * <li>Adds missing documents to index</li>
     * <li>Deletes removed documents from index</li>
     * </ul>
     * 
     * @param folder The mail folder
     * @param accountId The account identifier
     * @param session The associated session
     * @param params Optional parameters
     * @return The processing result or {@link ProcessingProgress#EMPTY_RESULT an empty result} if processing has been aborted
     * @throws OXException If an error occurs
     * @throws InterruptedException If processing is interrupted
     */
    public ProcessingProgress processFolder(final MailFolder folder, final int accountId, final Session session, final Map<String, Object> params) throws OXException, InterruptedException {
        return processFolder(new MailFolderInfo(folder), accountId, session, params);
    }

    /**
     * FULL
     */
    protected static final MailField[] FIELDS_FULL = new MailField[] { MailField.FULL };

    /**
     * ID, FOLDER_ID, CONTENT_TYPE, FROM, TO, CC, BCC, SUBJECT, SIZE, SENT_DATE, RECEIVED_DATE, FLAGS, THREAD_LEVEL,
     * DISPOSITION_NOTIFICATION_TO, PRIORITY, COLOR_LABEL
     */
    protected static final MailField[] FIELDS_LOW_COST = MailField.FIELDS_LOW_COST;

    /**
     * (Asynchronously) Processes specified mail folder for its content being indexed.
     * <p>
     * In case of immediate processing, the following steps were performed:
     * <ul>
     * <li>Adds missing documents to index</li>
     * <li>Deletes removed documents from index</li>
     * </ul>
     * 
     * @param folderInfo The mail folder information
     * @param accountId The account identifier
     * @param session The associated session
     * @param params Optional parameters
     * @return The processing result or {@link ProcessingProgress#EMPTY_RESULT an empty result} if processing has been aborted
     * @throws OXException If an error occurs
     * @throws InterruptedException If processing is interrupted
     */
    public ProcessingProgress processFolder(final MailFolderInfo folderInfo, final int accountId, final Session session, final Map<String, Object> params) throws OXException, InterruptedException {
        final int messageCount = folderInfo.getMessageCount();
        if (messageCount <= 0) {
            return ProcessingProgress.EMPTY_RESULT;
        }
        /*
         * Decide...
         */
        final IndexFacadeService facade = SmalServiceLookup.getServiceStatic(IndexFacadeService.class);
        if (null == facade) {
            // Index service missing
            return ProcessingProgress.EMPTY_RESULT;
        }
        final String fullName = folderInfo.getFullName();
        final int userId = session.getUserId();
        final int contextId = session.getContextId();
        {
            /*
             * Proceed
             */
            final ProcessingProgress processingProgress = new ProcessingProgress();
            processingProgress.setHasHighAttention(strategy.hasHighAttention(folderInfo));
            /*
             * Create task
             */
            final Callable<Object> task = new Callable<Object>() {

                @Override
                public Object call() throws Exception {
                    /*
                     * Acquire exclusive flag
                     */
                    try {
                        if (!acquire(fullName, accountId, userId, contextId)) {
                            // Another thread already running
                            processingProgress.setFirstTime(false).setProcessType(ProcessType.NONE);
                            if (DEBUG) {
                                LOG.debug("\tAnother thread processes \"" + fullName + "\" " + new DebugInfo(accountId, userId, contextId));
                            }
                            return null;
                        }
                        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
                        IndexAccess<MailMessage> indexAccess = null;
                        try {
                            indexAccess = facade.acquireIndexAccess(Types.EMAIL, session);
                            final boolean initial = !containsFolder(accountId, fullName, indexAccess, session);
                            mailAccess = SmalMailAccess.getUnwrappedInstance(session, accountId);
                            mailAccess.connect(false);
                            if (initial) {
                                /*-
                                 * 
                                 * Denoted folder has not been added to index before
                                 * 
                                 */
                                processingProgress.setFirstTime(true);
                                final Collection<MailMessage> storageMails = getParameter("processor.storageMails", params);
                                final Collection<MailMessage> indexMails = getParameter("processor.indexMails", params);
                                process(
                                    new MailFolderInfo(fullName, messageCount),
                                    processingProgress,
                                    mailAccess,
                                    indexAccess,
                                    storageMails,
                                    indexMails);
                            } else {
                                /*-
                                 * 
                                 * Denoted folder has already been added to index before
                                 * 
                                 */
                                processingProgress.setProcessType(ProcessType.JOB);
                                submitAsJob(folderInfo, mailAccess, Collections.<String, Object>emptyMap());
                                if (DEBUG) {
                                    LOG.debug("Scheduled new job for \"" + folderInfo.getFullName() + "\" " + new DebugInfo(mailAccess));
                                }
                            }
                            return null;
                        } finally {
                            if (processingProgress.countDown) {
                                processingProgress.latch.countDown();
                                processingProgress.countDown = false;
                            }
                            SmalMailAccess.closeUnwrappedInstance(mailAccess);
                            releaseAccess(facade, indexAccess);
                            release(fullName, accountId, userId, contextId);
                        }
                    } catch (final Exception e) {
                        LOG.error(e.getMessage(), e);
                        throw e;
                    }
                }

            };
            /*
             * Submit task & assign future
             */
            processingProgress.setFuture(ThreadPools.getThreadPool().submit(ThreadPools.task(task), CallerRunsBehavior.getInstance()));
            /*
             * Return when result is initialized appropriately
             */
            processingProgress.latch.await();
            return processingProgress;
        }
    }

    /**
     * Process specified folder information.
     * 
     * @param folderInfo The folder information
     * @param processingProgress The processing progress to initialize
     * @param mailAccess The associated {@link MailAccess} instance
     * @param indexAccess The associated {@link IndexAccess} instance
     * @param storageMails The storage messages or <code>null</code>
     * @param indexMails The index messages or <code>null</code>
     * @throws OXException If processing fails
     * @throws InterruptedException If processing is interrupted
     */
    protected void process(final MailFolderInfo folderInfo, final ProcessingProgress processingProgress, final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess, final IndexAccess<MailMessage> indexAccess, final Collection<MailMessage> storageMails, final Collection<MailMessage> indexMails) throws OXException, InterruptedException {
        final int accountId = mailAccess.getAccountId();
        final int messageCount = folderInfo.getMessageCount();
        if (strategy.addFull(messageCount, folderInfo)) { // headers, content + attachments
            processingProgress.setProcessType(ProcessType.FULL);
            final MailMessage[] messages =
                mailAccess.getMessageStorage().getAllMessages(
                    folderInfo.getFullName(),
                    IndexRange.NULL,
                    MailSortField.RECEIVED_DATE,
                    OrderDirection.DESC,
                    FIELDS_FULL);
            final List<IndexDocument<MailMessage>> documents = new ArrayList<IndexDocument<MailMessage>>(messages.length);
            for (final MailMessage message : messages) {
                documents.add(IndexDocumentHelper.documentFor(message, accountId));
            }
            if (DEBUG) {
                LOG.debug("Starting addAttachments() for " + documents.size() + " documents from \"" + folderInfo.getFullName() + "\" " + new DebugInfo(mailAccess));
                final long st = System.currentTimeMillis();
                indexAccess.addAttachments(documents, true);
                final long dur = System.currentTimeMillis() - st;
                LOG.debug("Performed addAttachments() for " + documents.size() + " documents from \"" + folderInfo.getFullName() + "\" in " + dur + "msec. " + new DebugInfo(
                    mailAccess));
            } else {
                indexAccess.addAttachments(documents, true);
            }
        } else if (strategy.addHeadersAndContent(messageCount, folderInfo)) { // headers + content
            processingProgress.setProcessType(ProcessType.HEADERS_AND_CONTENT);
            final MailMessage[] messages =
                mailAccess.getMessageStorage().getAllMessages(
                    folderInfo.getFullName(),
                    IndexRange.NULL,
                    MailSortField.RECEIVED_DATE,
                    OrderDirection.DESC,
                    FIELDS_FULL);
            final List<IndexDocument<MailMessage>> documents = new ArrayList<IndexDocument<MailMessage>>(messages.length);
            for (final MailMessage message : messages) {
                documents.add(IndexDocumentHelper.documentFor(message, accountId));
            }
            if (DEBUG) {
                LOG.debug("Starting addContent() for " + documents.size() + " documents from \"" + folderInfo.getFullName() + "\" " + new DebugInfo(mailAccess));
                final long st = System.currentTimeMillis();
                indexAccess.addContent(documents, true);
                final long dur = System.currentTimeMillis() - st;
                LOG.debug("Performed addContent() for " + documents.size() + " documents from \"" + folderInfo.getFullName() + "\" in " + dur + "msec. " + new DebugInfo(
                    mailAccess));
            } else {
                indexAccess.addContent(documents, true);
            }
        } else if (strategy.addHeadersOnly(messageCount, folderInfo)) { // headers only
            processingProgress.setProcessType(ProcessType.HEADERS_ONLY);
            final MailMessage[] messages =
                mailAccess.getMessageStorage().getAllMessages(
                    folderInfo.getFullName(),
                    null,
                    MailSortField.RECEIVED_DATE,
                    OrderDirection.DESC,
                    FIELDS_LOW_COST);
            final List<IndexDocument<MailMessage>> documents = new ArrayList<IndexDocument<MailMessage>>(messages.length);
            for (final MailMessage message : messages) {
                documents.add(IndexDocumentHelper.documentFor(message, accountId));
            }
            if (DEBUG) {
                LOG.debug("Starting addEnvelopeData() for " + documents.size() + " documents from \"" + folderInfo.getFullName() + "\" " + new DebugInfo(mailAccess));
                final long st = System.currentTimeMillis();
                indexAccess.addEnvelopeData(documents);
                final long dur = System.currentTimeMillis() - st;
                LOG.debug("Performed addEnvelopeData() for " + documents.size() + " documents from \"" + folderInfo.getFullName() + "\" in " + dur + "msec. " + new DebugInfo(
                    mailAccess));
            } else {
                indexAccess.addEnvelopeData(documents);
            }
        } else {
            processingProgress.setProcessType(ProcessType.JOB);
            submitAsJob(folderInfo, mailAccess, storageMails, indexMails);
            if (DEBUG) {
                LOG.debug("Scheduled new job for \"" + folderInfo.getFullName() + "\" " + new DebugInfo(mailAccess));
            }
        }
    }

    protected boolean containsFolder(final int accountId, final String fullName, final IndexAccess<MailMessage> indexAccess, final Session session) throws OXException, InterruptedException {
        if (null == fullName || accountId < 0) {
            return false;
        }
        
        return IndexAccessAdapter.getInstance().containsFolder(accountId, fullName, session);
    }

    private static final MailField[] FIELDS_ID = new MailField[] { MailField.ID };

    protected static List<Map<String, MailMessage>> getNewIds(final String fullName, final IndexAccess<MailMessage> indexAccess, final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) throws OXException, InterruptedException {
        /*
         * Get the mails from storage
         */
        final Map<String, MailMessage> storageMap;
        {
            /*
             * Fetch mails
             */
            final List<MailMessage> mails =
                Arrays.asList(mailAccess.getMessageStorage().searchMessages(
                    fullName,
                    IndexRange.NULL,
                    MailSortField.RECEIVED_DATE,
                    OrderDirection.ASC,
                    null,
                    FIELDS_ID));
            if (mails.isEmpty()) {
                storageMap = Collections.emptyMap();
            } else {
                storageMap = new HashMap<String, MailMessage>(mails.size());
                for (final MailMessage mailMessage : mails) {
                    storageMap.put(mailMessage.getMailId(), mailMessage);
                }
            }
        }
        /*
         * Get the mails from index
         */
        final Map<String, MailMessage> indexMap;
        {
//            final String queryString;
//            {
//                final Session session = mailAccess.getSession();
//                final StringBuilder queryBuilder = new StringBuilder(128);
//                queryBuilder.append('(').append(FIELD_USER).append(':').append(session.getUserId()).append(')');
//                queryBuilder.append(" AND (").append(FIELD_CONTEXT).append(':').append(session.getContextId()).append(')');
//                queryBuilder.append(" AND (").append(FIELD_ACCOUNT).append(':').append(mailAccess.getAccountId()).append(')');
//                queryBuilder.append(" AND (").append(FIELD_FULL_NAME).append(":\"").append(fullName).append("\")");
//                queryString = queryBuilder.toString();
//            }
//            final Map<String, Object> params = new HashMap<String, Object>(4);
//            // TODO: params.put("fields", mailFields);
//            params.put("sort", FIELD_RECEIVED_DATE);
//            params.put("order", "desc");
//            params.put("fields", FIELD_ID);
//            final QueryParameters queryParameter =
//                new QueryParameters.Builder(queryString).setOffset(0).setLength(Integer.MAX_VALUE).setType(IndexDocument.Type.MAIL).setParameters(
//                    params).build();
//            final IndexResult<MailMessage> indexResult = indexAccess.query(queryParameter);
//            final List<MailMessage> indexedMails;
//            if (0 >= indexResult.getNumFound()) {
//                indexedMails = Collections.emptyList();
//            } else {
//                final List<IndexDocument<MailMessage>> results = indexResult.getResults();
//                final List<MailMessage> mails = new ArrayList<MailMessage>(results.size());
//                for (final IndexDocument<MailMessage> indexDocument : results) {
//                    mails.add(indexDocument.getObject());
//                }
//                indexedMails = mails;
//            }
            
            final List<MailMessage> indexedMails = IndexAccessAdapter.getInstance().getMessages(mailAccess.getAccountId(), fullName, mailAccess.getSession(), MailSortField.RECEIVED_DATE, OrderDirection.DESC);
            if (indexedMails.isEmpty()) {
                indexMap = Collections.emptyMap();
            } else {
                indexMap = new HashMap<String, MailMessage>(indexedMails.size());
                for (final MailMessage mailMessage : indexedMails) {
                    indexMap.put(mailMessage.getMailId(), mailMessage);
                }
            }
        }
        /*
         * Return as list
         */
        final List<Map<String, MailMessage>> retval = new ArrayList<Map<String, MailMessage>>(2);
        retval.add(storageMap);
        retval.add(indexMap);
        return retval;
    }

    protected void submitAsJob(final MailFolderInfo folderInfo, final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess, final Map<String, Object> params) throws OXException {
        final Collection<MailMessage> storageMails = getParameter("processor.storageMails", params);
        final Collection<MailMessage> indexMails = getParameter("processor.indexMails", params);
        submitAsJob(folderInfo, mailAccess, storageMails, indexMails);
    }

    protected void submitAsJob(final MailFolderInfo folderInfo, final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess, final Collection<MailMessage> storageMails, final Collection<MailMessage> indexMails) throws OXException {
        final IndexingService indexingService = SmalServiceLookup.getServiceStatic(IndexingService.class);
        if (null == indexingService) {
            return;
        }
        final Session session = mailAccess.getSession();
        final MailConfig mailConfig = mailAccess.getMailConfig();
        final Builder jobInfoBuilder =
            new MailJobInfo.Builder(session.getUserId(), session.getContextId()).accountId(mailAccess.getAccountId()).login(
                mailConfig.getLogin()).password(mailConfig.getPassword()).server(mailConfig.getServer()).port(mailConfig.getPort()).secure(
                mailConfig.isSecure()).primaryPassword(session.getPassword());
        final FolderJob folderJob = new FolderJob(folderInfo.getFullName(), jobInfoBuilder.build());
        folderJob.setIndexMails(null == indexMails ? null : new ArrayList<MailMessage>(indexMails));
        folderJob.setStorageMails(null == storageMails ? null : new ArrayList<MailMessage>(storageMails));
        indexingService.addJob(folderJob);
    }

    @SuppressWarnings("unchecked")
    protected static <V> V getParameter(final String name, final Map<String, Object> params) {
        return (V) ((null == name) || (null == params) ? null : params.get(name));
    }

    protected static boolean acquire(final String fullName, final int accountId, final int userId, final int contextId) throws OXException {
        final DatabaseService databaseService = SmalServiceLookup.getServiceStatic(DatabaseService.class);
        final Connection con = databaseService.getWritable(contextId);
        try {
            return update(fullName, accountId, userId, contextId, true, con);
        } finally {
            databaseService.backWritable(contextId, con);
        }
    }

    protected static void release(final String fullName, final int accountId, final int userId, final int contextId) throws OXException {
        final DatabaseService databaseService = SmalServiceLookup.getServiceStatic(DatabaseService.class);
        final Connection con = databaseService.getWritable(contextId);
        try {
            forceUpdate(fullName, accountId, userId, contextId, false, con);
        } finally {
            databaseService.backWritable(contextId, con);
        }
    }

    private static void forceUpdate(final String fullName, final int accountId, final int userId, final int contextId, final boolean update, final Connection con) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("UPDATE mailSync SET sync=? WHERE cid=? AND user=? AND accountId=? AND fullName=?");
            int pos = 1;
            stmt.setInt(pos++, update ? 1 : 0);
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, userId);
            stmt.setInt(pos++, accountId);
            stmt.setString(pos, fullName);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw SmalExceptionCodes.UNEXPECTED_ERROR.create(e);
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    private static boolean update(final String fullName, final int accountId, final int userId, final int contextId, final boolean update, final Connection con) throws OXException {
        try {
            // Try to perform a compare-and-set UPDATE
            final int cur = performSelect(fullName, accountId, userId, contextId, con);
            if (cur < 0) {
                if (performInsert(fullName, accountId, userId, contextId, update, con)) {
                    return true;
                }
            } else if (update && (cur > 0)) {
                // Another thread holds 'sync' flag
                return false;
            }
            return compareAndSet(fullName, accountId, userId, contextId, update, con);
        } catch (final SQLException e) {
            throw SmalExceptionCodes.UNEXPECTED_ERROR.create(e);
        }
    }

    private static boolean compareAndSet(final String fullName, final int accountId, final int userId, final int contextId, final boolean update, final Connection con) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("UPDATE mailSync SET sync=? WHERE cid=? AND user=? AND accountId=? AND fullName=? AND sync=?");
            int pos = 1;
            stmt.setInt(pos++, update ? 1 : 0);
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, userId);
            stmt.setInt(pos++, accountId);
            stmt.setString(pos++, fullName);
            stmt.setInt(pos, update ? 0 : 1); // Opposite value
            final int result = stmt.executeUpdate();
            return (result > 0);
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    private static boolean performInsert(final String fullName, final int accountId, final int userId, final int contextId, final boolean update, final Connection con) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("INSERT INTO mailSync (cid, user, accountId, fullName, sync, timestamp) VALUES (?,?,?,?,?,?)");
            int pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, userId);
            stmt.setInt(pos++, accountId);
            stmt.setString(pos++, fullName);
            stmt.setInt(pos++, update ? 1 : 0);
            stmt.setLong(pos, 0L);
            try {
                final int result = stmt.executeUpdate();
                return (result > 0);
            } catch (final SQLException e) {
                return false;
            }
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    private static int performSelect(final String fullName, final int accountId, final int userId, final int contextId, final Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement("SELECT sync FROM mailSync WHERE cid=? AND user=? AND accountId=? AND fullName=? FOR UPDATE");
            int pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, userId);
            stmt.setInt(pos++, accountId);
            stmt.setString(pos, fullName);
            result = stmt.executeQuery();
            if (result.next()) {
                return result.getInt(1);
            }
            return -1;
        } catch (final SQLException e) {
            throw SmalExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
    }

}

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.index.IndexAccess;
import com.openexchange.index.IndexDocument;
import com.openexchange.index.IndexFacadeService;
import com.openexchange.index.IndexResult;
import com.openexchange.index.QueryParameters;
import com.openexchange.index.solr.SolrMailConstants;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.smal.impl.SmalServiceLookup;
import com.openexchange.service.indexing.IndexingService;
import com.openexchange.service.indexing.mail.MailJobInfo;
import com.openexchange.service.indexing.mail.MailJobInfo.Builder;
import com.openexchange.service.indexing.mail.job.FolderJob;
import com.openexchange.session.Session;

/**
 * {@link Processor} - Processes a given mail folder for its content being indexed.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Processor implements SolrMailConstants {

    private static final Processor INSTANCE = new Processor();

    /**
     * Gets the default instance.
     * 
     * @return The default instance
     */
    public static Processor getInstance() {
        return INSTANCE;
    }

    private final IProcessorStrategy strategy;

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
    public Processor(final IProcessorStrategy strategy) {
        super();
        assert null != strategy;
        this.strategy = strategy;
    }

    /**
     * Processes specified mail folder for its content being indexed.
     * 
     * @param folder The mail folder
     * @param mailAccess The mail access
     * @return The processing result or {@link ProcessingResult#EMPTY_RESULT an empty result} if processing has been aborted
     * @throws OXException If indexing fails for any reason
     * @throws InterruptedException If processing is interrupted
     */
    public ProcessingResult processFolder(final MailFolder folder, final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) throws OXException, InterruptedException {
        return processFolder(new MailFolderInfo(folder), mailAccess);
    }

    private static final MailField[] FIELDS_FULL = new MailField[] { MailField.FULL };

    private static final MailField[] FIELDS_LOW_COST = MailField.FIELDS_LOW_COST;

    /**
     * Processes specified mail folder for its content being indexed.
     * 
     * @param folderInfo The mail folder information
     * @param mailAccess The mail access
     * @return The processing result or {@link ProcessingResult#EMPTY_RESULT an empty result} if processing has been aborted
     * @throws OXException If indexing fails for any reason
     * @throws InterruptedException If processing is interrupted
     */
    public ProcessingResult processFolder(final MailFolderInfo folderInfo, final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) throws OXException, InterruptedException {
        final int messageCount = folderInfo.getMessageCount();
        if (0 <= messageCount) {
            return ProcessingResult.EMPTY_RESULT;
        }
        /*
         * Decide...
         */
        final IndexFacadeService facade = SmalServiceLookup.getServiceStatic(IndexFacadeService.class);
        if (null == facade) {
            // Index service missing
            return ProcessingResult.EMPTY_RESULT;
        }
        IndexAccess<MailMessage> indexAccess = null;
        try {
            indexAccess = facade.acquireIndexAccess(Types.EMAIL, mailAccess.getSession());
            final int accountId = mailAccess.getAccountId();
            final String fullName = folderInfo.getFullName();
            final boolean initial = !containsFolder(accountId, fullName, indexAccess);
            final ProcessingResult processingResult;
            if (initial) {
                /*-
                 * 
                 * Denoted folder has not been added to index before
                 * 
                 */
                if (strategy.addFull(messageCount, folderInfo)) { // headers, content + attachments
                    final MailMessage[] messages =
                        mailAccess.getMessageStorage().getAllMessages(
                            fullName,
                            null,
                            MailSortField.RECEIVED_DATE,
                            OrderDirection.DESC,
                            FIELDS_FULL);
                    final List<IndexDocument<MailMessage>> documents = new ArrayList<IndexDocument<MailMessage>>(messages.length);
                    for (final MailMessage message : messages) {
                        documents.add(IndexDocumentHelper.documentFor(message, accountId));
                    }
                    indexAccess.addAttachments(documents);
                    /*
                     * Assign appropriate result
                     */
                    processingResult = new ProcessingResult(ProcessType.FULL, strategy.hasHighAttention(folderInfo));
                } else if (strategy.addHeadersAndContent(messageCount, folderInfo)) { // headers + content
                    final MailMessage[] messages =
                        mailAccess.getMessageStorage().getAllMessages(
                            fullName,
                            null,
                            MailSortField.RECEIVED_DATE,
                            OrderDirection.DESC,
                            FIELDS_FULL);
                    final List<IndexDocument<MailMessage>> documents = new ArrayList<IndexDocument<MailMessage>>(messages.length);
                    for (final MailMessage message : messages) {
                        documents.add(IndexDocumentHelper.documentFor(message, accountId));
                    }
                    indexAccess.addContent(documents);
                    /*
                     * Assign appropriate result
                     */
                    processingResult = new ProcessingResult(ProcessType.HEADERS_AND_CONTENT, strategy.hasHighAttention(folderInfo));
                } else if (strategy.addHeadersOnly(messageCount, folderInfo)) { // headers only
                    final MailMessage[] messages =
                        mailAccess.getMessageStorage().getAllMessages(
                            fullName,
                            null,
                            MailSortField.RECEIVED_DATE,
                            OrderDirection.DESC,
                            FIELDS_LOW_COST);
                    final List<IndexDocument<MailMessage>> documents = new ArrayList<IndexDocument<MailMessage>>(messages.length);
                    for (final MailMessage message : messages) {
                        documents.add(IndexDocumentHelper.documentFor(message, accountId));
                    }
                    indexAccess.addEnvelopeData(documents);
                    /*
                     * Assign appropriate result
                     */
                    processingResult = new ProcessingResult(ProcessType.HEADERS_ONLY, strategy.hasHighAttention(folderInfo));
                } else {
                    submitAsJob(folderInfo, mailAccess);
                    /*
                     * Assign appropriate result
                     */
                    processingResult = new ProcessingResult(ProcessType.JOB, strategy.hasHighAttention(folderInfo));
                }
            } else {
                /*-
                 * 
                 * Denoted folder has already been added to index before
                 * 
                 */
                final Map<String, MailMessage> storageMap;
                final Map<String, MailMessage> indexMap;
                {
                    final List<Map<String, MailMessage>> tmp = getNewIds(fullName, indexAccess, mailAccess);
                    storageMap = tmp.get(0);
                    indexMap = tmp.get(1);
                }
                /*
                 * New ones
                 */
                final Set<String> newIds = new HashSet<String>(storageMap.keySet());
                newIds.removeAll(indexMap.keySet());
                if (newIds.isEmpty()) {
                    return ProcessingResult.EMPTY_RESULT;
                }
                final int size = newIds.size();
                if (strategy.addFull(size, folderInfo)) { // headers, content + attachments
                    final MailMessage[] messages =
                        mailAccess.getMessageStorage().getAllMessages(
                            fullName,
                            null,
                            MailSortField.RECEIVED_DATE,
                            OrderDirection.DESC,
                            FIELDS_FULL);
                    final List<IndexDocument<MailMessage>> documents = new ArrayList<IndexDocument<MailMessage>>(messages.length);
                    for (final MailMessage message : messages) {
                        documents.add(IndexDocumentHelper.documentFor(message, accountId));
                    }
                    indexAccess.addAttachments(documents);
                    /*
                     * Assign appropriate result
                     */
                    processingResult = new ProcessingResult(ProcessType.FULL, strategy.hasHighAttention(folderInfo));
                } else if (strategy.addHeadersAndContent(size, folderInfo)) { // headers + content
                    final MailMessage[] messages =
                        mailAccess.getMessageStorage().getAllMessages(
                            fullName,
                            null,
                            MailSortField.RECEIVED_DATE,
                            OrderDirection.DESC,
                            FIELDS_FULL);
                    final List<IndexDocument<MailMessage>> documents = new ArrayList<IndexDocument<MailMessage>>(messages.length);
                    for (final MailMessage message : messages) {
                        documents.add(IndexDocumentHelper.documentFor(message, accountId));
                    }
                    indexAccess.addContent(documents);
                    /*
                     * Assign appropriate result
                     */
                    processingResult = new ProcessingResult(ProcessType.HEADERS_AND_CONTENT, strategy.hasHighAttention(folderInfo));
                } else if (strategy.addHeadersOnly(size, folderInfo)) { // headers only
                    final MailMessage[] messages =
                        mailAccess.getMessageStorage().getAllMessages(
                            fullName,
                            null,
                            MailSortField.RECEIVED_DATE,
                            OrderDirection.DESC,
                            FIELDS_LOW_COST);
                    final List<IndexDocument<MailMessage>> documents = new ArrayList<IndexDocument<MailMessage>>(messages.length);
                    for (final MailMessage message : messages) {
                        documents.add(IndexDocumentHelper.documentFor(message, accountId));
                    }
                    indexAccess.addEnvelopeData(documents);
                    /*
                     * Assign appropriate result
                     */
                    processingResult = new ProcessingResult(ProcessType.HEADERS_ONLY, strategy.hasHighAttention(folderInfo));
                } else {
                    submitAsJob(folderInfo, mailAccess, storageMap.values(), indexMap.values());
                    /*
                     * Assign appropriate result
                     */
                    processingResult = new ProcessingResult(ProcessType.JOB, strategy.hasHighAttention(folderInfo));
                }
            }
            /*
             * Return result
             */
            return processingResult;
        } finally {
            releaseAccess(facade, indexAccess);
        }
    }

    private boolean containsFolder(final int accountId, final String fullName, final IndexAccess<MailMessage> indexAccess) throws OXException, InterruptedException {
        if (null == fullName || accountId < 0) {
            return false;
        }
        // Compose query string
        final StringBuilder queryBuilder = new StringBuilder(128);
        queryBuilder.append('(').append(FIELD_ACCOUNT).append(':').append(accountId).append(')');
        queryBuilder.append(" AND (").append(FIELD_FULL_NAME).append(":\"").append(fullName).append("\")");
        // Query parameters
        final Map<String, Object> params = new HashMap<String, Object>(2);
        params.put("sort", FIELD_RECEIVED_DATE);
        params.put("order", "desc");
        final QueryParameters queryParameters =
            new QueryParameters.Builder(queryBuilder.toString()).setLength(1).setOffset(0).setType(IndexDocument.Type.MAIL).setParameters(
                params).build();
        final IndexResult<MailMessage> result = indexAccess.query(queryParameters);
        return result.getNumFound() > 0;
    }

    private static final MailField[] FIELDS_ID = new MailField[] { MailField.ID };

    private static List<Map<String, MailMessage>> getNewIds(final String fullName, final IndexAccess<MailMessage> indexAccess, final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) throws OXException, InterruptedException {
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
            final String queryString;
            {
                final Session session = mailAccess.getSession();
                final StringBuilder queryBuilder = new StringBuilder(128);
                queryBuilder.append('(').append(FIELD_USER).append(':').append(session.getUserId()).append(')');
                queryBuilder.append(" AND (").append(FIELD_CONTEXT).append(':').append(session.getContextId()).append(')');
                queryBuilder.append(" AND (").append(FIELD_ACCOUNT).append(':').append(mailAccess.getAccountId()).append(')');
                queryBuilder.append(" AND (").append(FIELD_FULL_NAME).append(":\"").append(fullName).append("\")");
                queryString = queryBuilder.toString();
            }
            final Map<String, Object> params = new HashMap<String, Object>(4);
            // TODO: params.put("fields", mailFields);
            params.put("sort", FIELD_RECEIVED_DATE);
            params.put("order", "desc");
            final QueryParameters queryParameter =
                new QueryParameters.Builder(queryString).setOffset(0).setLength(Integer.MAX_VALUE).setType(IndexDocument.Type.MAIL).setParameters(
                    params).build();
            final IndexResult<MailMessage> indexResult = indexAccess.query(queryParameter);
            final List<MailMessage> indexedMails;
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

    private void submitAsJob(final MailFolderInfo folderInfo, final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) throws OXException {
        submitAsJob(folderInfo, mailAccess, null, null);
    }

    private void submitAsJob(final MailFolderInfo folderInfo, final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess, final Collection<MailMessage> storageMails, final Collection<MailMessage> indexMails) throws OXException {
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
        folderJob.setIndexMails(new ArrayList<MailMessage>(indexMails));
        folderJob.setStorageMails(new ArrayList<MailMessage>(storageMails));
        indexingService.addJob(folderJob);
    }

    private static void releaseAccess(final IndexFacadeService facade, final IndexAccess<MailMessage> indexAccess) {
        if (null != indexAccess) {
            try {
                facade.releaseIndexAccess(indexAccess);
            } catch (final Exception e) {
                // Ignore
            }
        }
    }

}

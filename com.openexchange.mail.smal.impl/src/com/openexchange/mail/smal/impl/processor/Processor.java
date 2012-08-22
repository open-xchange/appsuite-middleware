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

import static com.openexchange.index.mail.MailUtility.releaseAccess;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
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
import com.openexchange.threadpool.behavior.DiscardBehavior;

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
     * FULL
     */
    protected static final MailField[] FIELDS_FULL = new MailField[] { MailField.FULL };

    /**
     * ID, FOLDER_ID, CONTENT_TYPE, FROM, TO, CC, BCC, SUBJECT, SIZE, SENT_DATE, RECEIVED_DATE, FLAGS, THREAD_LEVEL,
     * DISPOSITION_NOTIFICATION_TO, PRIORITY, COLOR_LABEL
     */
    protected static final MailField[] FIELDS_LOW_COST = MailField.FIELDS_LOW_COST;
    
    public void processFolderAsync(final MailFolderInfo folderInfo, final int accountId, final Session session, final Map<String, Object> params) throws OXException {
        final Performer performer = new Performer(folderInfo, accountId, session, params, strategy);
        ThreadPools.getThreadPool().submit(ThreadPools.task(performer), DiscardBehavior.getInstance());
    }
    
    public void processFolderAsync(final MailFolder mailFolder, final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess, final Map<String, Object> params) throws OXException {
        if (mailFolder.isHoldsMessages() && mailFolder.getMessageCount() < 0) {
            final IMailFolderStorage folderStorage = mailAccess.getFolderStorage();
            if (folderStorage instanceof IMailFolderStorageEnhanced) {
                final IMailFolderStorageEnhanced storageEnhanced = (IMailFolderStorageEnhanced) folderStorage;
                final String fullName = mailFolder.getFullname();
                processFolderAsync(
                    new MailFolderInfo(fullName, storageEnhanced.getTotalCounter(fullName)),
                    mailAccess.getAccountId(),
                    mailAccess.getSession(),
                    params);
            }
        }
        
        processFolderAsync(mailFolder, mailAccess.getAccountId(), mailAccess.getSession(), params);
    }
    
    public void processFolderAsync(final MailFolder folder, final int accountId, final Session session, final Map<String, Object> params) throws OXException {
        processFolderAsync(new MailFolderInfo(folder), accountId, session, params);
    }
    
    private static final class Performer implements Runnable {
        
        private final MailFolderInfo folderInfo;
        private final int accountId;
        private final Session session;
        private final Map<String, Object> params;
        private final ProcessorStrategy strategy;
               

        public Performer(final MailFolderInfo folderInfo, final int accountId, final Session session, final Map<String, Object> params, final ProcessorStrategy strategy) {
            super();
            this.folderInfo = folderInfo;
            this.accountId = accountId;
            this.session = session;
            this.params = params;
            this.strategy = strategy;
        }

        @Override
        public void run() {
            final int messageCount = folderInfo.getMessageCount();
            if (messageCount <= 0) {
                return;
            }
            
            final IndexFacadeService facade = SmalServiceLookup.getServiceStatic(IndexFacadeService.class);
            if (null == facade) {
                return;
            }

            MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
            IndexAccess<MailMessage> indexAccess = null;
            try {
                indexAccess = facade.acquireIndexAccess(Types.EMAIL, session);
                mailAccess = SmalMailAccess.getUnwrappedInstance(session, accountId);
                mailAccess.connect(false);
                if (indexAccess.isIndexed(String.valueOf(accountId), folderInfo.getFullName())) {                    
                    process(mailAccess, indexAccess);
                } else {
                    submitJob(mailAccess);
                }                
            } catch (final Exception e) {
                LOG.error(e.getMessage(), e);
            } finally {
                SmalMailAccess.closeUnwrappedInstance(mailAccess);
                releaseAccess(facade, indexAccess);
            }
        }
        
        private void process(final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess, final IndexAccess<MailMessage> indexAccess) throws OXException {
            final int accountId = mailAccess.getAccountId();
            final int messageCount = folderInfo.getMessageCount();
            if (strategy.addFull(messageCount, folderInfo)) { // headers, content + attachments
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
                if (LOG.isDebugEnabled()) {
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
                if (LOG.isDebugEnabled()) {
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
                if (LOG.isDebugEnabled()) {
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
                 submitJob(mailAccess);           
            }
        }
        
        private void submitJob(final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) throws OXException {
            final Collection<MailMessage> storageMails = getParameter("processor.storageMails", params);
            final Collection<MailMessage> indexMails = getParameter("processor.indexMails", params);
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
            if (LOG.isDebugEnabled()) {
                LOG.debug("Scheduled new job for \"" + folderInfo.getFullName() + "\" " + new DebugInfo(mailAccess));
            }
        }
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

    @SuppressWarnings("unchecked")
    protected static <V> V getParameter(final String name, final Map<String, Object> params) {
        return (V) ((null == name) || (null == params) ? null : params.get(name));
    }
}

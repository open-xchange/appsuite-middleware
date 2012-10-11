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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import com.openexchange.exception.OXException;
import com.openexchange.index.IndexAccess;
import com.openexchange.index.IndexConstants;
import com.openexchange.index.IndexDocument;
import com.openexchange.index.IndexResult;
import com.openexchange.index.QueryParameters;
import com.openexchange.index.QueryParameters.Order;
import com.openexchange.index.SearchHandler;
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
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.index.MailIndexField;
import com.openexchange.mail.index.MailUtility;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.mail.smal.impl.index.IndexAccessAdapter;
import com.openexchange.mail.smal.impl.index.IndexDocumentHelper;
import com.openexchange.mail.utils.MailPasswordUtil;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.service.indexing.IndexingJob;
import com.openexchange.service.indexing.IndexingService;
import com.openexchange.service.indexing.JobInfo;
import com.openexchange.service.indexing.impl.mail.AddByIdsJob;
import com.openexchange.service.indexing.impl.mail.ChangeByIdsJob;
import com.openexchange.service.indexing.impl.mail.MailJobInfo;
import com.openexchange.service.indexing.impl.mail.MailJobInfo.Builder;
import com.openexchange.service.indexing.impl.mail.RemoveByIdsJob;
import com.openexchange.session.Session;

/**
 * {@link SmalMessageStorage} - The message storage for SMAL which either delegates calls to delegating message storage or serves them from
 * index storage.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SmalMessageStorage extends AbstractSMALStorage implements IMailMessageStorage, IMailMessageStorageExt, IMailMessageStorageBatch {

    protected static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(SmalMessageStorage.class));
    
    private final IMailMessageStorage messageStorage;


    /**
     * Initializes a new {@link SmalMessageStorage}.
     * 
     * @throws OXException If initialization fails
     */
    public SmalMessageStorage(final Session session, final int accountId, final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> delegateMailAccess) throws OXException {
        super(session, accountId, delegateMailAccess);
        messageStorage = delegateMailAccess.getMessageStorage();
    }

    @Override
    public String[] appendMessages(final String destFolder, final MailMessage[] msgs) throws OXException {
        final String[] newIds = messageStorage.appendMessages(destFolder, msgs);        
        /*
         * Enqueue adder job
         */
        try {
            Builder builder = prepareJobBuilder(AddByIdsJob.class);
            builder.folder(destFolder);
            builder.addProperty(AddByIdsJob.IDS, newIds);        
            submitJob(builder.build());
        } catch (Exception e) {
            LOG.warn("Could not schedule indexing job.", e);
        }

        return newIds;
    }

    

    @Override
    public String[] copyMessages(final String sourceFolder, final String destFolder, final String[] mailIds, final boolean fast) throws OXException {
        final String[] newIds = messageStorage.copyMessages(sourceFolder, destFolder, mailIds, false);
        /*
         * Enqueue adder job
         */
        try {
            Builder builder = prepareJobBuilder(AddByIdsJob.class);
            builder.folder(destFolder);
            builder.addProperty(AddByIdsJob.IDS, newIds);        
            submitJob(builder.build());
        } catch (Exception e) {
            LOG.warn("Could not schedule indexing job.", e);
        }
        return fast ? new String[0] : newIds;
    }

    @Override
    public void deleteMessages(final String folder, final String[] mailIds, final boolean hardDelete) throws OXException {
        messageStorage.deleteMessages(folder, mailIds, hardDelete);
        /*
         * Enqueue remover job
         */
        try {
            Builder builder = prepareJobBuilder(RemoveByIdsJob.class);
            builder.folder(folder);
            builder.addProperty(RemoveByIdsJob.IDS, mailIds);        
            submitJob(builder.build());
        } catch (Exception e) {
            LOG.warn("Could not schedule indexing job.", e);
        }
    }
    
    @Override
    public MailMessage[] getMessages(final String folder, final String[] mailIds, final MailField[] fields) throws OXException {
        return messageStorage.getMessages(folder, mailIds, fields);
    }    
    
    @Override
    public MailMessage[] searchMessages(final String folder, final IndexRange indexRange, final MailSortField sortField, final OrderDirection order, final SearchTerm<?> searchTerm, final MailField[] fields) throws OXException {
    	if (getIndexFacadeService() == null) {
            return messageStorage.searchMessages(folder, indexRange, sortField, order, searchTerm, fields);
        }
    	
        final MailFields mfs = new MailFields(fields);
        IndexAccess<MailMessage> indexAccess = null;
        try {
            indexAccess = IndexAccessAdapter.getInstance().getIndexAccess(session);
            boolean isIndexed = indexAccess.isIndexed(String.valueOf(accountId), folder);
            if (!isIndexed) {
                try {
                    submitFolderJob(folder);
                } catch (OXException e) {
                    LOG.error("Could not schedule folder job.", e);
                }
                
                return messageStorage.searchMessages(folder, indexRange, sortField, order, searchTerm, fields);
            } else if (searchTerm == null || !MailUtility.getIndexableFields(indexAccess).containsAll(mfs)) {
                return messageStorage.searchMessages(folder, indexRange, sortField, order, searchTerm, fields);
            }
            
            final Map<String, Object> params = new HashMap<String, Object>(1);
            params.put(IndexConstants.ACCOUNT, accountId);
            final QueryParameters.Builder builder = new QueryParameters.Builder(params)
                                                    .setOffset(0)
                                                    .setLength(Integer.MAX_VALUE)
                                                    .setFolders(Collections.singleton(folder));
            
            
            if (null != sortField) {
                final MailField field = MailField.getField(sortField.getField());
                final MailIndexField indexSortField = MailIndexField.getFor(field);
                if (indexSortField != null) {
                    builder.setSortField(indexSortField);
                    builder.setOrder(OrderDirection.DESC.equals(order) ? Order.DESC : Order.ASC);
                }
            }
            
            final QueryParameters parameters;
            final SimpleSearchTermVisitor visitor = new SimpleSearchTermVisitor();
            searchTerm.accept(visitor);
            if (visitor.simple) {
                parameters = builder.setHandler(SearchHandler.SIMPLE).setPattern(searchTerm.getPattern().toString()).build();
            } else {
                parameters = builder.setHandler(SearchHandler.CUSTOM).setSearchTerm(searchTerm).build();
            }

            long start = System.currentTimeMillis();
            final IndexResult<MailMessage> result = indexAccess.query(parameters, MailIndexField.getFor(fields));
            if (LOG.isDebugEnabled()) {
                long diff = System.currentTimeMillis() - start;
                LOG.debug("Index Query lasted " + diff + "ms.");
            }
            
            List<IndexDocument<MailMessage>> documents = result.getResults();
            List<MailMessage> mails;
            if (indexRange != null) {
                final int fromIndex = indexRange.start;
                int toIndex = indexRange.end;
                if ((documents == null) || documents.isEmpty()) {
                    mails = Collections.emptyList();
                }
                if ((fromIndex) > documents.size()) {
                    /*
                     * Return empty iterator if start is out of range
                     */
                    mails = Collections.emptyList();
                }
                /*
                 * Reset end index if out of range
                 */
                if (toIndex >= documents.size()) {
                    toIndex = documents.size();
                }
                documents = documents.subList(fromIndex, toIndex);                
            }
            
            mails = IndexDocumentHelper.messagesFrom(documents);
            return mails.toArray(new MailMessage[mails.size()]);
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        } finally {
            IndexAccessAdapter.getInstance().releaseIndexAccess(indexAccess);
        }
    }

    @Override
    public void updateMessageFlags(final String folder, final String[] mailIds, final int flags, final boolean set) throws OXException {
        messageStorage.updateMessageFlags(folder, mailIds, flags, set);
        /*
         * Enqueue change job
         */
        try {
            Builder builder = prepareJobBuilder(ChangeByIdsJob.class);
            builder.folder(folder);
            builder.addProperty(ChangeByIdsJob.IDS, mailIds);        
            submitJob(builder.build());
        } catch (Exception e) {
            LOG.warn("Could not schedule indexing job.", e);
        }
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
        final MailMessage mail = messageStorage.getMessage(folder, mailId, markSeen);
        if (mail == null)  {
            throw MailExceptionCode.MAIL_NOT_FOUN_BY_MESSAGE_ID.create(folder, mailId);
        }
        
        mail.setAccountId(accountId);        
        // TODO: this may be critical to performance.
//        try {
//            if (!mail.isPrevSeen()) {
//                Builder builder = prepareJobBuilder(AddByIdsJob.class);
//                builder.folder(folder);
//                builder.addProperty(AddByIdsJob.IDS, new String[] { mail.getMailId() });        
//                submitJob(builder.build());
//            }            
//        } catch (Exception e) {
//            LOG.warn("Could not schedule indexing job.", e);
//        }
        
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
        String[] retval = null;
        if (fast) {
            messageStorage.moveMessages(sourceFolder, destFolder, mailIds, true);            
            retval = new String[0];
        } else {
            retval = messageStorage.moveMessages(sourceFolder, destFolder, mailIds, false);
        }        
        
        try {
            Builder deleteBuilder = prepareJobBuilder(RemoveByIdsJob.class);
            deleteBuilder.folder(sourceFolder);
            deleteBuilder.addProperty(RemoveByIdsJob.IDS, mailIds);        
            submitJob(deleteBuilder.build());
            
            Builder createBuilder = prepareJobBuilder(AddByIdsJob.class);
            createBuilder.folder(destFolder);
            createBuilder.addProperty(AddByIdsJob.IDS, mailIds);        
            submitJob(createBuilder.build());
        } catch (Exception e) {
            LOG.warn("Could not schedule indexing job.", e);
        }
        
        return retval;
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
        try {
            Builder builder = prepareJobBuilder(ChangeByIdsJob.class);
            builder.folder(folder);
            builder.addProperty(ChangeByIdsJob.IDS, mailIds);        
            submitJob(builder.build());
        } catch (Exception e) {
            LOG.warn("Could not schedule indexing job.", e);
        }
    }

    @Override
    public MailMessage[] getNewAndModifiedMessages(final String folder, final MailField[] fields) throws OXException {
        return messageStorage.getNewAndModifiedMessages(folder, fields);
    }

    @Override
    public MailMessage[] getDeletedMessages(final String folder, final MailField[] fields) throws OXException {
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
    
    private void submitJob(JobInfo jobInfo) throws OXException {        
        IndexingService indexingService = SmalServiceLookup.getServiceStatic(IndexingService.class);
        if (indexingService == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(IndexingService.class);
        }

        indexingService.scheduleJob(jobInfo, null, -1L, IndexingService.DEFAULT_PRIORITY);    
    }
    
    private Builder prepareJobBuilder(Class<? extends IndexingJob> clazz) throws OXException {
        MailAccountStorageService storageService = SmalServiceLookup.getServiceStatic(MailAccountStorageService.class);
        if (storageService == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(MailAccountStorageService.class);
        }

        MailAccount account = storageService.getMailAccount(accountId, userId, contextId);
        String decryptedPW = account.getPassword() == null ? session.getPassword() : MailPasswordUtil.decrypt(account.getPassword(), 
            session, 
            accountId, 
            account.getLogin(), 
            account.getMailServer());
        
        Builder builder = MailJobInfo.newBuilder(clazz)
            .login(account.getLogin())
            .accountId(account.getId())
            .contextId(contextId)
            .userId(userId)
            .primaryPassword(session.getPassword())
            .password(decryptedPW);
        
        return builder;        
    }

}

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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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
import java.util.List;
import javax.mail.Message;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.index.AccountFolders;
import com.openexchange.index.IndexAccess;
import com.openexchange.index.IndexDocument;
import com.openexchange.index.IndexFacadeService;
import com.openexchange.index.IndexResult;
import com.openexchange.index.QueryParameters;
import com.openexchange.index.QueryParameters.Order;
import com.openexchange.index.SearchHandlers;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailFields;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.IMailMessageStorageBatch;
import com.openexchange.mail.api.IMailMessageStorageDelegator;
import com.openexchange.mail.api.IMailMessageStorageExt;
import com.openexchange.mail.api.IMailMessageStorageMimeSupport;
import com.openexchange.mail.api.ISimplifiedThreadStructure;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.index.MailIndexField;
import com.openexchange.mail.index.MailUtility;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.mail.smal.impl.index.FakeSession;
import com.openexchange.mail.smal.impl.index.IndexDocumentHelper;
import com.openexchange.mail.smal.impl.index.jobs.AddByIdsJob;
import com.openexchange.mail.smal.impl.index.jobs.ChangeByIdsJob;
import com.openexchange.mail.smal.impl.index.jobs.MailJobInfo;
import com.openexchange.mail.smal.impl.index.jobs.MailJobInfo.Builder;
import com.openexchange.mail.smal.impl.index.jobs.RemoveByIdsJob;
import com.openexchange.mail.utils.MailPasswordUtil;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.osgi.ExceptionUtils;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.service.indexing.IndexingJob;
import com.openexchange.service.indexing.IndexingService;
import com.openexchange.service.indexing.JobInfo;
import com.openexchange.session.Session;

/**
 * {@link SmalMessageStorage} - The message storage for SMAL which either delegates calls to delegating message storage or serves them from
 * index storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SmalMessageStorage extends AbstractSMALStorage implements IMailMessageStorage, IMailMessageStorageDelegator, IMailMessageStorageExt, IMailMessageStorageBatch, ISimplifiedThreadStructure, IMailMessageStorageMimeSupport {

    /**
     * Initializes a new {@link SmalMessageStorage}.
     *
     * @throws OXException If initialization fails
     */
    public SmalMessageStorage(final Session session, final int accountId, final SmalMailAccess smalMailAccess) throws OXException {
        super(session, accountId, smalMailAccess);
    }

    @Override
    public IMailMessageStorage getDelegateMessageStorage() throws OXException {
        return smalMailAccess.getDelegateMailAccess().getMessageStorage();
    }

    @Override
    public void clearCache() throws OXException {
        final IMailMessageStorage messageStorage = smalMailAccess.getDelegateMailAccess().getMessageStorage();
        if (messageStorage instanceof IMailMessageStorageExt) {
            ((IMailMessageStorageExt) messageStorage).clearCache();
        }
    }

    @Override
    public List<List<MailMessage>> getThreadSortedMessages(final String folder, final boolean includeSent, final boolean cache, final IndexRange indexRange, final long max, final MailSortField sortField, final OrderDirection order, final MailField[] fields) throws OXException {
        final IMailMessageStorage messageStorage = smalMailAccess.getDelegateMailAccess().getMessageStorage();
        if (messageStorage instanceof ISimplifiedThreadStructure) {
            return ((ISimplifiedThreadStructure) messageStorage).getThreadSortedMessages(folder, includeSent, cache, indexRange, max, sortField, order, fields);
        }
        throw MailExceptionCode.UNSUPPORTED_OPERATION.create();
    }

    @Override
    public boolean isMimeSupported() throws OXException {
        final IMailMessageStorage messageStorage = smalMailAccess.getDelegateMailAccess().getMessageStorage();
        return (messageStorage instanceof IMailMessageStorageMimeSupport) && ((IMailMessageStorageMimeSupport) messageStorage).isMimeSupported();
    }

    @Override
    public String[] appendMimeMessages(String destFolder, Message[] msgs) throws OXException {
        final IMailMessageStorage messageStorage = smalMailAccess.getDelegateMailAccess().getMessageStorage();
        if (messageStorage instanceof IMailMessageStorageMimeSupport) {
            final IMailMessageStorageMimeSupport streamSupport = (IMailMessageStorageMimeSupport) messageStorage;
            if (streamSupport.isMimeSupported()) {
                return streamSupport.appendMimeMessages(destFolder, msgs);
            }
        }
        throw MailExceptionCode.UNSUPPORTED_OPERATION.create();
    }

    @Override
    public Message getMimeMessage(String fullName, String id, boolean markSeen) throws OXException {
        final IMailMessageStorage messageStorage = smalMailAccess.getDelegateMailAccess().getMessageStorage();
        if (messageStorage instanceof IMailMessageStorageMimeSupport) {
            final IMailMessageStorageMimeSupport streamSupport = (IMailMessageStorageMimeSupport) messageStorage;
            if (streamSupport.isMimeSupported()) {
                return streamSupport.getMimeMessage(fullName, id, markSeen);
            }
        }
        throw MailExceptionCode.UNSUPPORTED_OPERATION.create();
    }

    @Override
    public String[] appendMessages(final String destFolder, final MailMessage[] msgs) throws OXException {
        final String[] newIds = smalMailAccess.getDelegateMailAccess().getMessageStorage().appendMessages(destFolder, msgs);
        /*
         * Enqueue adder job
         */
        try {
            final Builder builder = prepareJobBuilder(AddByIdsJob.class);
            builder.folder(destFolder);
            builder.addProperty(AddByIdsJob.IDS, newIds);
            submitJob(builder.build());
        } catch (final Exception e) {
            LOG.warn("Could not schedule indexing job.", e);
        }

        return newIds;
    }

    @Override
    public String[] copyMessages(final String sourceFolder, final String destFolder, final String[] mailIds, final boolean fast) throws OXException {
        final String[] newIds = smalMailAccess.getDelegateMailAccess().getMessageStorage().copyMessages(sourceFolder, destFolder, mailIds, false);
        /*
         * Enqueue adder job
         */
        try {
            final Builder builder = prepareJobBuilder(AddByIdsJob.class);
            builder.folder(destFolder);
            builder.addProperty(AddByIdsJob.IDS, newIds);
            submitJob(builder.build());
        } catch (final Exception e) {
            LOG.warn("Could not schedule indexing job.", e);
        }
        return fast ? new String[0] : newIds;
    }

    @Override
    public void deleteMessages(final String folder, final String[] mailIds, final boolean hardDelete) throws OXException {
        final IMailMessageStorage messageStorage = smalMailAccess.getDelegateMailAccess().getMessageStorage();
        messageStorage.deleteMessages(folder, mailIds, hardDelete);
        /*
         * Enqueue remover job
         */
        try {
            final Builder builder = prepareJobBuilder(RemoveByIdsJob.class);
            builder.folder(folder);
            builder.addProperty(RemoveByIdsJob.IDS, mailIds);
            submitJob(builder.build());
        } catch (final Exception e) {
            LOG.warn("Could not schedule indexing job.", e);
        }
    }

    @Override
    public MailMessage[] getMessages(final String folder, final String[] mailIds, final MailField[] fields) throws OXException {
        return smalMailAccess.getDelegateMailAccess().getMessageStorage().getMessages(folder, mailIds, fields);
    }

    @Override
    public MailMessage[] searchMessages(final String folder, final IndexRange indexRange, final MailSortField sortField, final OrderDirection order, final SearchTerm<?> searchTerm, final MailField[] fields) throws OXException {
        final IndexFacadeService indexFacade = getIndexFacadeService();
        if (searchTerm == null || indexFacade == null || isBlacklisted() || !isIndexingAllowed()) {
            return smalMailAccess.getDelegateMailAccess().getMessageStorage().searchMessages(folder, indexRange, sortField, order, searchTerm, fields);
        }

        // Close for the time accessing the index
        smalMailAccess.closeDelegateMailAccess();
        // Access index
        IndexAccess<MailMessage> indexAccess = null;
        try {
            final MailFields mfs = new MailFields(fields);
            indexAccess = indexFacade.acquireIndexAccess(Types.EMAIL, session);
            final boolean isIndexed = indexAccess.isIndexed(String.valueOf(accountId), folder);
            if (isIndexed && MailUtility.getIndexableFields(indexAccess).containsAll(mfs)) {
                final AccountFolders accountFolders = new AccountFolders(String.valueOf(accountId), Collections.singleton(folder));
                final QueryParameters.Builder builder = new QueryParameters.Builder().setAccountFolders(Collections.singleton(accountFolders));

                if (sortField != null) {
                    final MailField field = MailField.getField(sortField.getField());
                    final MailIndexField indexSortField = MailIndexField.getFor(field);
                    if (indexSortField != null) {
                        builder.setSortField(indexSortField);
                    }

                    if (order != null) {
                        builder.setOrder(order == OrderDirection.ASC ? Order.ASC : Order.DESC);
                    }
                }

                final QueryParameters parameters = builder.setHandler(SearchHandlers.CUSTOM).setSearchTerm(searchTerm).build();
                final IndexResult<MailMessage> result = indexAccess.query(parameters, MailIndexField.getFor(fields));

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
            }
        } catch (final Throwable t) {
            ExceptionUtils.handleThrowable(t);
            LOG.warn("Index search failed. Falling back to message storage.", t);
        } finally {
            if (indexAccess != null) {
                indexFacade.releaseIndexAccess(indexAccess);
            }

            try {
                submitFolderJob(folder);
            } catch (final OXException e) {
                LOG.warn("Could not schedule folder job for folder {}.", folder, e);
            }
        }

        // Fallback to message storage
        return smalMailAccess.getDelegateMailAccess().getMessageStorage().searchMessages(folder, indexRange, sortField, order, searchTerm, fields);
    }

    @Override
    public void updateMessageFlags(final String folder, final String[] mailIds, final int flags, final boolean set) throws OXException {
        smalMailAccess.getDelegateMailAccess().getMessageStorage().updateMessageFlags(folder, mailIds, flags, set);
        /*
         * Enqueue change job
         */
        try {
            final Builder builder = prepareJobBuilder(ChangeByIdsJob.class);
            builder.folder(folder);
            builder.addProperty(ChangeByIdsJob.IDS, mailIds);
            submitJob(builder.build());
        } catch (final Exception e) {
            LOG.warn("Could not schedule indexing job.", e);
        }
    }

    @Override
    public MailMessage[] getAllMessages(final String folder, final IndexRange indexRange, final MailSortField sortField, final OrderDirection order, final MailField[] fields) throws OXException {
        return searchMessages(folder, indexRange, sortField, order, null, fields);
    }

    @Override
    public MailPart getAttachment(final String folder, final String mailId, final String sequenceId) throws OXException {
        return smalMailAccess.getDelegateMailAccess().getMessageStorage().getAttachment(folder, mailId, sequenceId);
    }

    @Override
    public MailPart getImageAttachment(final String folder, final String mailId, final String contentId) throws OXException {
        return smalMailAccess.getDelegateMailAccess().getMessageStorage().getImageAttachment(folder, mailId, contentId);
    }

    @Override
    public MailMessage getMessage(final String folder, final String mailId, final boolean markSeen) throws OXException {
        final MailMessage mail = smalMailAccess.getDelegateMailAccess().getMessageStorage().getMessage(folder, mailId, markSeen);
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
        final MailMessage[] messages = smalMailAccess.getDelegateMailAccess().getMessageStorage().getThreadSortedMessages(folder, indexRange, sortField, order, searchTerm, fields);
        try {
            submitFolderJob(folder);
        } catch (final OXException e) {
            LOG.warn("Could not schedule folder job for folder {}.", folder, e);
        }

        return messages;
    }

    @Override
    public MailMessage[] getUnreadMessages(final String folder, final MailSortField sortField, final OrderDirection order, final MailField[] fields, final int limit) throws OXException {
        return smalMailAccess.getDelegateMailAccess().getMessageStorage().getUnreadMessages(folder, sortField, order, fields, limit);
    }

    @Override
    public String[] moveMessages(final String sourceFolder, final String destFolder, final String[] mailIds, final boolean fast) throws OXException {
        String[] retval = null;
        if (fast) {
            smalMailAccess.getDelegateMailAccess().getMessageStorage().moveMessages(sourceFolder, destFolder, mailIds, true);
            retval = new String[0];
        } else {
            retval = smalMailAccess.getDelegateMailAccess().getMessageStorage().moveMessages(sourceFolder, destFolder, mailIds, false);
        }

        try {
            final Builder deleteBuilder = prepareJobBuilder(RemoveByIdsJob.class);
            deleteBuilder.folder(sourceFolder);
            deleteBuilder.addProperty(RemoveByIdsJob.IDS, mailIds);
            submitJob(deleteBuilder.build());

            final Builder createBuilder = prepareJobBuilder(AddByIdsJob.class);
            createBuilder.folder(destFolder);
            createBuilder.addProperty(AddByIdsJob.IDS, mailIds);
            submitJob(createBuilder.build());
        } catch (final Exception e) {
            LOG.warn("Could not schedule indexing job.", e);
        }

        return retval;
    }

    @Override
    public MailMessage saveDraft(final String draftFullname, final ComposedMailMessage draftMail) throws OXException {
        return smalMailAccess.getDelegateMailAccess().getMessageStorage().saveDraft(draftFullname, draftMail);
    }

    @Override
    public void updateMessageUserFlags(String folder, String[] mailIds, String[] flags, boolean set) throws OXException {
        smalMailAccess.getDelegateMailAccess().getMessageStorage().updateMessageUserFlags(folder, mailIds, flags, set);
        /*
         * Enqueue change job.
         */
        try {
            final Builder builder = prepareJobBuilder(ChangeByIdsJob.class);
            builder.folder(folder);
            builder.addProperty(ChangeByIdsJob.IDS, mailIds);
            submitJob(builder.build());
        } catch (final Exception e) {
            LOG.warn("Could not schedule indexing job.", e);
        }
    }

    @Override
    public void updateMessageColorLabel(final String folder, final String[] mailIds, final int colorLabel) throws OXException {
        smalMailAccess.getDelegateMailAccess().getMessageStorage().updateMessageColorLabel(folder, mailIds, colorLabel);
        /*
         * Enqueue change job.
         */
        try {
            final Builder builder = prepareJobBuilder(ChangeByIdsJob.class);
            builder.folder(folder);
            builder.addProperty(ChangeByIdsJob.IDS, mailIds);
            submitJob(builder.build());
        } catch (final Exception e) {
            LOG.warn("Could not schedule indexing job.", e);
        }
    }

    @Override
    public MailMessage[] getNewAndModifiedMessages(final String folder, final MailField[] fields) throws OXException {
        return smalMailAccess.getDelegateMailAccess().getMessageStorage().getNewAndModifiedMessages(folder, fields);
    }

    @Override
    public MailMessage[] getDeletedMessages(final String folder, final MailField[] fields) throws OXException {
        return smalMailAccess.getDelegateMailAccess().getMessageStorage().getDeletedMessages(folder, fields);
    }

    @Override
    public void releaseResources() throws OXException {
        smalMailAccess.getDelegateMailAccess().getMessageStorage().releaseResources();
    }

    /**
     * The fields containing only the mail identifier.
     */
    protected static final MailField[] FIELDS_ID_AND_FOLDER = new MailField[] { MailField.ID, MailField.FOLDER_ID };

    @Override
    public MailMessage[] getMessagesByMessageID(final String... messageIDs) throws OXException {
        final IMailMessageStorage messageStorage = smalMailAccess.getDelegateMailAccess().getMessageStorage();
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
        final IMailMessageStorage messageStorage = smalMailAccess.getDelegateMailAccess().getMessageStorage();
        if (messageStorage instanceof IMailMessageStorageExt) {
            final IMailMessageStorageExt messageStorageExt = (IMailMessageStorageExt) messageStorage;
            return messageStorageExt.getMessages(fullName, mailIds, fields, headerNames);
        }
        return messageStorage.getMessages(fullName, mailIds, FIELDS_HEADERS);
    }

    @Override
    public void updateMessageColorLabel(final String fullName, final int colorLabel) throws OXException {
        final IMailMessageStorage messageStorage = smalMailAccess.getDelegateMailAccess().getMessageStorage();
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
        final IMailMessageStorage messageStorage = smalMailAccess.getDelegateMailAccess().getMessageStorage();
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
        return smalMailAccess.getDelegateMailAccess().getMessageStorage().getPrimaryContents(folder, mailIds);
    }

    private void submitJob(final JobInfo jobInfo) throws OXException {
        if (session instanceof FakeSession) {
            LOG.debug("Session is a fake session. Job will not be submitted...");
            // FIXME: This is done to prevent loops here and needs a much better solution!
            return;
        }

        if (!isIndexingAllowed() || isBlacklisted()) {
            return;
        }

        final IndexingService indexingService = SmalServiceLookup.getServiceStatic(IndexingService.class);
        if (indexingService == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(IndexingService.class);
        }

        indexingService.scheduleJob(true, jobInfo, null, -1L, IndexingService.DEFAULT_PRIORITY);
    }

    private Builder prepareJobBuilder(final Class<? extends IndexingJob> clazz) throws OXException {
        final MailAccountStorageService storageService = SmalServiceLookup.getServiceStatic(MailAccountStorageService.class);
        if (storageService == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(MailAccountStorageService.class);
        }

        final MailAccount account = storageService.getMailAccount(accountId, userId, contextId);
        final String decryptedPW = account.getPassword() == null ? session.getPassword() : MailPasswordUtil.decrypt(account.getPassword(),
            session,
            accountId,
            account.getLogin(),
            account.getMailServer());

        final Builder builder = MailJobInfo.newBuilder(clazz)
            .login(account.getLogin())
            .accountId(account.getId())
            .contextId(contextId)
            .userId(userId)
            .primaryPassword(session.getPassword())
            .password(decryptedPW);

        return builder;
    }

}

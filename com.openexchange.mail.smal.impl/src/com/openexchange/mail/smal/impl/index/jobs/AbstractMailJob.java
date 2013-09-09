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

package com.openexchange.mail.smal.impl.index.jobs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.logging.Log;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.attach.index.Attachment;
import com.openexchange.groupware.attach.index.ORTerm;
import com.openexchange.groupware.attach.index.ObjectIdTerm;
import com.openexchange.groupware.attach.index.SearchTerm;
import com.openexchange.groupware.tools.chunk.ChunkPerformer;
import com.openexchange.groupware.tools.chunk.ListPerformable;
import com.openexchange.index.AccountFolders;
import com.openexchange.index.IndexAccess;
import com.openexchange.index.IndexDocument;
import com.openexchange.index.IndexFacadeService;
import com.openexchange.index.QueryParameters;
import com.openexchange.index.SearchHandlers;
import com.openexchange.index.StandardIndexDocument;
import com.openexchange.mail.MailField;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.IMailMessageStorageExt;
import com.openexchange.mail.dataobjects.ContentAwareMailMessage;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.index.MailUUID;
import com.openexchange.mail.parser.MailMessageParser;
import com.openexchange.mail.smal.impl.SmalServiceLookup;
import com.openexchange.osgi.ExceptionUtils;
import com.openexchange.service.indexing.IndexingJob;


/**
 * {@link AbstractMailJob}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public abstract class AbstractMailJob implements IndexingJob {

    public static final String IDS = "ids";

    protected static final Log LOG = com.openexchange.log.Log.loggerFor(AbstractMailJob.class);

    protected static final int CHUNK_SIZE = 100;


    protected AbstractMailJob() {
        super();
    }

    protected void addMails(final MailJobInfo info, final List<String> idsToAdd, final IMailMessageStorage messageStorage, final IndexAccess<MailMessage> mailIndex, final IndexAccess<Attachment> attachmentIndex) throws OXException {
        if (idsToAdd.isEmpty()) {
            return;
        }

        final MailMessageParser parser = new MailMessageParser();
        ChunkPerformer.perform(idsToAdd, 0, CHUNK_SIZE, new ListPerformable<String>() {
            @Override
            public void perform(List<String> subList) throws OXException {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Adding a chunk of mails of folder " + info.folder + ": " + info.toString());
                }

                List<IndexDocument<MailMessage>> documents = new ArrayList<IndexDocument<MailMessage>>();
                List<IndexDocument<Attachment>> attachments = new ArrayList<IndexDocument<Attachment>>();
                MailMessage[] messages = messageStorage.getMessages(
                    info.folder,
                    subList.toArray(new String[subList.size()]),
                    MailField.values());

                /*
                 * Avoid memory leak in java mail implementation
                 */
                if (messageStorage instanceof IMailMessageStorageExt) {
                    LOG.trace("Cleaning mail cache of com.sun.mail.imap.IMAPFolder...");
                    ((IMailMessageStorageExt) messageStorage).clearCache();
                }

                String[] mailIds = new String[messages.length];
                for (int i = 0; i < messages.length; i++) {
                    MailMessage mailMessage = messages[i];
                    if (mailMessage != null) {
                        mailIds[i] = (mailMessage.getMailId());
                    }
                }

                String[] primaryContents = messageStorage.getPrimaryContents(info.folder, mailIds);
                for (int i = 0; i < messages.length; i++) {
                    MailMessage message = messages[i];
                    if (message != null) {
                        ContentAwareMailMessage contentAwareMessage = new ContentAwareMailMessage(primaryContents[i], message);
                        documents.add(new StandardIndexDocument<MailMessage>(contentAwareMessage));

                        try {
                            IndexMailHandler handler = new IndexMailHandler(String.valueOf(info.accountId), info.folder, message.getMailId());
                            parser.parseMailMessage(message, handler);
                            attachments.addAll(handler.getAttachments());
                        } catch (Throwable t) {
                            ExceptionUtils.handleThrowable(t);
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Attachments will be skipped for mail " + message.getMailId() + ".\nCause: " + t.getMessage() + "\n" + info.toString());
                            }
                        }
                    }
                }

                if (!documents.isEmpty()) {
                    mailIndex.addDocuments(documents);
                }

                if (!attachments.isEmpty()) {
                    attachmentIndex.addDocuments(attachments);
                }
            }
        });
    }

    protected void deleteMails(final MailJobInfo info, final List<String> idsToDelete, final IndexAccess<MailMessage> mailIndex, final IndexAccess<Attachment> attachmentIndex) throws OXException {
        if (idsToDelete.isEmpty()) {
            return;
        }

        ChunkPerformer.perform(idsToDelete, 0, CHUNK_SIZE, new ListPerformable<String>() {
            @Override
            public void perform(List<String> subList) throws OXException {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Deleting a chunk of mails in folder " + info.folder + ": " + info.toString());
                }

                SearchTerm<?>[] idTerms = new SearchTerm<?>[subList.size()];
                Set<String> mailUuids = new HashSet<String>(subList.size());
                int i = 0;
                for (String id : subList) {
                    mailUuids.add(MailUUID.newUUID(info.contextId, info.userId, info.accountId, info.folder, id).toString());
                    idTerms[i] = new ObjectIdTerm(id);
                    ++i;
                }

                QueryParameters deleteMailsQuery = new QueryParameters.Builder()
                    .setHandler(SearchHandlers.GET_REQUEST)
                    .setIndexIds(mailUuids)
                    .build();
                mailIndex.deleteByQuery(deleteMailsQuery);

                SearchTerm<?> orTerm = new ORTerm(idTerms);
                AccountFolders accountFolders = new AccountFolders(String.valueOf(info.accountId), Collections.singleton(String.valueOf(info.folder)));
                QueryParameters deleteAttachmentsQuery = new QueryParameters.Builder()
                    .setHandler(SearchHandlers.CUSTOM)
                    .setSearchTerm(orTerm)
                    .setModule(Types.EMAIL)
                    .setAccountFolders(Collections.singleton(accountFolders))
                    .build();
                attachmentIndex.deleteByQuery(deleteAttachmentsQuery);
            }
        });
    }

    protected void changeMails(final MailJobInfo info, final List<String> changedMails, final IMailMessageStorage messageStorage, final IndexAccess<MailMessage> mailIndex, final IndexAccess<Attachment> attachmentIndex) throws OXException {
        if (changedMails.isEmpty()) {
            return;
        }

        ChunkPerformer.perform(changedMails, 0, CHUNK_SIZE, new ListPerformable<String>() {
            @Override
            public void perform(List<String> subList) throws OXException {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Adding a chunk of mails of folder " + info.folder + ": " + info.toString());
                }

                List<IndexDocument<MailMessage>> documents = new ArrayList<IndexDocument<MailMessage>>();
                MailMessage[]  messages = messageStorage.getMessages(
                    info.folder,
                    subList.toArray(new String[subList.size()]),
                    MailField.values());

                String[] mailIds = new String[messages.length];
                for (int i = 0; i < messages.length; i++) {
                    MailMessage mailMessage = messages[i];
                    if (mailMessage != null) {
                        mailIds[i] = (mailMessage.getMailId());
                    }
                }

                String[] primaryContents = messageStorage.getPrimaryContents(info.folder, mailIds);
                for (int i = 0; i < messages.length; i++) {
                    MailMessage message = messages[i];
                    if (message != null) {
                        ContentAwareMailMessage contentAwareMessage = new ContentAwareMailMessage(primaryContents[i], message);
                        documents.add(new StandardIndexDocument<MailMessage>(contentAwareMessage));
                    }
                }

                if (!documents.isEmpty()) {
                    mailIndex.addDocuments(documents);
                }
            }
        });
    }

    protected void closeIndexAccess(IndexAccess<?> indexAccess) throws OXException {
        if (indexAccess != null) {
            IndexFacadeService indexFacade = SmalServiceLookup.getServiceStatic(IndexFacadeService.class);
            indexFacade.releaseIndexAccess(indexAccess);
        }
    }
}

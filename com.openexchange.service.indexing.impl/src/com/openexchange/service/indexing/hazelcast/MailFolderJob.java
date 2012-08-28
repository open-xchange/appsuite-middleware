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

package com.openexchange.service.indexing.hazelcast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.tools.chunk.ChunkPerformer;
import com.openexchange.groupware.tools.chunk.Performable;
import com.openexchange.index.IndexAccess;
import com.openexchange.index.IndexConstants;
import com.openexchange.index.IndexDocument;
import com.openexchange.index.IndexFacadeService;
import com.openexchange.index.IndexResult;
import com.openexchange.index.QueryParameters;
import com.openexchange.index.QueryParameters.Builder;
import com.openexchange.index.QueryParameters.Order;
import com.openexchange.index.SearchHandler;
import com.openexchange.index.StandardIndexDocument;
import com.openexchange.index.attachments.Attachment;
import com.openexchange.index.attachments.ORTerm;
import com.openexchange.index.attachments.ObjectIdTerm;
import com.openexchange.index.attachments.SearchTerm;
import com.openexchange.index.mail.MailIndexField;
import com.openexchange.index.mail.MailUUID;
import com.openexchange.index.solr.IndexFolderManager;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.dataobjects.ContentAwareMailMessage;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.parser.MailMessageParser;
import com.openexchange.mail.smal.SmalAccessService;
import com.openexchange.service.indexing.IndexingJob;
import com.openexchange.service.indexing.impl.Services;
import com.openexchange.service.indexing.mail.FakeSession;
import com.openexchange.service.indexing.mail.MailJobInfo;
import com.openexchange.session.Session;


/**
 * {@link MailFolderJob}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class MailFolderJob implements IndexingJob {
    
    private static final Log LOG = com.openexchange.log.Log.loggerFor(MailFolderJob.class);

    private static final long serialVersionUID = -900105721652425254L;
    
    private static final int CHUNK_SIZE = 100;
    
    private final MailJobInfo info;

    private String folder;
    
    
    public MailFolderJob(String folder, MailJobInfo info) {
        super();
        this.info = info;
        this.folder = folder;
    }

    @Override
    public Class<?>[] getNeededServices() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void performJob() throws OXException, InterruptedException {
        long start = System.currentTimeMillis();
        if (LOG.isDebugEnabled()) {
            LOG.debug(this.getClass().getSimpleName() + " started performing. " + info.toString());
        }
        
        checkJobInfo();
        IndexFacadeService indexFacade = Services.getService(IndexFacadeService.class);
        IndexAccess<MailMessage> mailIndex = indexFacade.acquireIndexAccess(Types.EMAIL, info.userId, info.contextId);
        IndexAccess<Attachment> attachmentIndex = indexFacade.acquireIndexAccess(Types.ATTACHMENT, info.userId, info.contextId);
        MailAccess<? extends IMailFolderStorage,? extends IMailMessageStorage> mailAccess = getMailAccess();
        try {
            IMailFolderStorage folderStorage = mailAccess.getFolderStorage();
            Map<String, Object> params = new HashMap<String, Object>();
            params.put(IndexConstants.ACCOUNT, Integer.valueOf(info.accountId));
            Builder queryBuilder = new Builder(params);
            QueryParameters mailAllQuery = queryBuilder.setHandler(SearchHandler.ALL_REQUEST)
                .setFolders(Collections.singleton(folder))
                .setSortField(MailIndexField.RECEIVED_DATE)
                .setOrder(Order.DESC)
                .build();            
            if (folderStorage.exists(folder)) {
                IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
                Set<MailIndexField> fields = Collections.singleton(MailIndexField.ID);                
                IndexResult<MailMessage> indexResult = mailIndex.query(mailAllQuery, fields);
                MailMessage[] storageMails = messageStorage.searchMessages(
                    folder,
                    IndexRange.NULL,
                    MailSortField.RECEIVED_DATE,
                    OrderDirection.DESC,
                    null,
                    new MailField[] { MailField.ID });
                
                Set<String> indexIds = new HashSet<String>();
                for (IndexDocument<MailMessage> document : indexResult.getResults()) {
                    indexIds.add(document.getObject().getMailId());
                }                
                Set<String> storageIds = new HashSet<String>();
                for (MailMessage msg : storageMails) {
                    storageIds.add(msg.getMailId());
                }
                
                deleteObsoleteMails(indexIds, storageIds, mailIndex, attachmentIndex);
                addNewMails(indexIds, storageIds, mailIndex, attachmentIndex, messageStorage);
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Deleting folder from index: " + info.toString());
                }
                
                IndexFolderManager.deleteFolderEntry(info.contextId, info.userId, Types.EMAIL, String.valueOf(info.accountId), folder);                
                mailIndex.deleteByQuery(mailAllQuery);
                Map<String, Object> attachmentAllParams = new HashMap<String, Object>();
                params.put(IndexConstants.MODULE, new Integer(Types.EMAIL));
                params.put(IndexConstants.ACCOUNT, Integer.toString(info.accountId));                
                QueryParameters attachmentAllQuery = new Builder(attachmentAllParams)
                    .setHandler(SearchHandler.ALL_REQUEST)
                    .setFolders(Collections.singleton(folder))
                    .build();
                attachmentIndex.deleteByQuery(attachmentAllQuery);                
            }
        } finally {
            closeMailAccess(mailAccess);
            closeIndexAccess(mailIndex);
            closeIndexAccess(attachmentIndex);
            
            if (LOG.isDebugEnabled()) {
                long diff = System.currentTimeMillis() - start;
                LOG.debug(this.getClass().getSimpleName() + " lasted " + diff + "ms. " + info.toString());
            }
        }        
    }

    private void addNewMails(Set<String> indexIds, Set<String> storageIds, final IndexAccess<MailMessage> mailIndex, final IndexAccess<Attachment> attachmentIndex, final IMailMessageStorage messageStorage) throws OXException {
        final List<String> toAdd = new ArrayList<String>(storageIds);
        toAdd.removeAll(indexIds);
        if (toAdd.isEmpty()) {
            return;
        }
        
        final MailMessageParser parser = new MailMessageParser();
        ChunkPerformer.perform(new Performable() {
            @Override
            public int perform(int off, int len) throws OXException {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Adding a chunk of mails of folder " + folder + ": " + info.toString());
                }
                
                List<String> subList = toAdd.subList(off, len);
                MailMessage[] messages = messageStorage.getMessages(
                    folder, 
                    subList.toArray(new String[subList.size()]), 
                    MailField.values());
                
                String[] mailIds = new String[messages.length];
                for (int i = 0; i < messages.length; i++) {
                    MailMessage mailMessage = messages[i];
                    if (mailMessage != null) {
                        mailIds[i] = (mailMessage.getMailId());
                    }
                }
                
                List<IndexDocument<MailMessage>> documents = new ArrayList<IndexDocument<MailMessage>>(messages.length);
                List<IndexDocument<Attachment>> attachments = new ArrayList<IndexDocument<Attachment>>();
                String[] primaryContents = messageStorage.getPrimaryContents(folder, mailIds);                
                for (int i = 0; i < messages.length; i++) {
                    MailMessage message = messages[i];
                    if (message != null) {
                        ContentAwareMailMessage contentAwareMessage = new ContentAwareMailMessage(primaryContents[i], message);
                        documents.add(new StandardIndexDocument<MailMessage>(contentAwareMessage));
                        IndexMailHandler handler = new IndexMailHandler(String.valueOf(info.accountId), folder, message.getMailId());
                        parser.parseMailMessage(message, handler);
                        attachments.addAll(handler.getAttachments());                        
                    }
                }

                if (!documents.isEmpty()) {
                    mailIndex.addContent(documents, true);
                }
                
                if (!attachments.isEmpty()) {
                    attachmentIndex.addContent(attachments, true);
                }
                
                return subList.size();
            }

            @Override
            public int getChunkSize() {
                return CHUNK_SIZE;
            }

            @Override
            public int getLength() {
                return toAdd.size();
            }

            @Override
            public int getInitialOffset() {
                return 0;
            }         
        });
    }

    private void deleteObsoleteMails(Set<String> indexIds, Set<String> storageIds, final IndexAccess<MailMessage> mailIndex, final IndexAccess<Attachment> attachmentIndex) throws OXException {
        final List<String> toDelete = new ArrayList<String>(indexIds);
        toDelete.removeAll(storageIds);
        if (toDelete.isEmpty()) {
            return;
        }
        
        ChunkPerformer.perform(new Performable() {
            @Override
            public int perform(int off, int len) throws OXException {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Deleting a chunk of mails in folder " + folder + ": " + info.toString());
                }
                
                List<String> subList = toDelete.subList(off, len);
                SearchTerm<?>[] idTerms = new SearchTerm<?>[subList.size()];
                String[] mailUuids = new String[subList.size()];
                int i = 0;        
                for (String id : subList) {
                    mailUuids[i] = new MailUUID(info.contextId, info.userId, info.accountId, folder, id).getUUID();
                    idTerms[i] = new ObjectIdTerm(id);
                    ++i;
                }                    
                
                Map<String, Object> deleteMailsParams = new HashMap<String, Object>();
                deleteMailsParams.put(IndexConstants.IDS, mailUuids);
                QueryParameters deleteMailsQuery = new QueryParameters.Builder(deleteMailsParams)
                    .setHandler(SearchHandler.GET_REQUEST)
                    .build();
                mailIndex.deleteByQuery(deleteMailsQuery);
                
                SearchTerm<?> orTerm = new ORTerm(idTerms);
                Map<String, Object> deleteAttachmentsParams = new HashMap<String, Object>();
                deleteAttachmentsParams.put(IndexConstants.MODULE, new Integer(Types.EMAIL));
                deleteAttachmentsParams.put(IndexConstants.ACCOUNT, Integer.valueOf(info.accountId));
                QueryParameters deleteAttachmentsQuery = new QueryParameters.Builder(deleteAttachmentsParams)
                    .setHandler(SearchHandler.CUSTOM)
                    .setSearchTerm(orTerm)
                    .setFolders(Collections.singleton(folder))
                    .build();
                attachmentIndex.deleteByQuery(deleteAttachmentsQuery);
                
                return subList.size();
            }

            @Override
            public int getChunkSize() {
                return CHUNK_SIZE;
            }

            @Override
            public int getLength() {
                return toDelete.size();
            }

            @Override
            public int getInitialOffset() {
                return 0;
            }
        });
    }
    
    private MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> getMailAccess() throws OXException {
        Session session = new FakeSession(info.primaryPassword, info.userId, info.contextId);
        session.setParameter("com.openexchange.mail.lookupMailAccessCache", Boolean.FALSE);
        SmalAccessService smalService = Services.getService(SmalAccessService.class);
        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> tmp = smalService.getUnwrappedInstance(session, info.accountId);
        /*
         * Safety close & not cacheable
         */
        tmp.close(true);
        tmp.setCacheable(false);
        /*
         * Parameterize configuration
         */
        MailConfig mailConfig = tmp.getMailConfig();
        mailConfig.setLogin(info.login);
        mailConfig.setPassword(info.password);
        mailConfig.setServer(info.server);
        mailConfig.setPort(info.port);
        mailConfig.setSecure(info.secure);
        tmp.connect(true);
        
        return tmp;
    }
    
    private void closeMailAccess(MailAccess<? extends IMailFolderStorage,? extends IMailMessageStorage> mailAccess) {
        if (mailAccess != null) {
            SmalAccessService smalService = Services.getService(SmalAccessService.class);
            smalService.closeUnwrappedInstance(mailAccess);
        }        
    }
    
    private void closeIndexAccess(IndexAccess<?> indexAccess) throws OXException {
        if (indexAccess != null) {
            IndexFacadeService indexFacade = Services.getService(IndexFacadeService.class);
            indexFacade.releaseIndexAccess(indexAccess);
        }        
    }
    
    private void checkJobInfo() throws OXException {
        // TODO: implement
    }

    @Override
    public boolean isDurable() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int getPriority() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setPriority(int priority) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public long getTimeStamp() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Origin getOrigin() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Behavior getBehavior() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void beforeExecute() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void afterExecute(Throwable t) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Map<String, ?> getProperties() {
        // TODO Auto-generated method stub
        return null;
    }

}

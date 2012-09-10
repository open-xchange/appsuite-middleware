package com.openexchange.service.indexing.internal.mail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
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
import com.openexchange.mail.dataobjects.ContentAwareMailMessage;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.parser.MailMessageParser;
import com.openexchange.mail.service.MailService;
import com.openexchange.service.indexing.internal.Services;
import com.openexchange.service.indexing.mail.MailJobInfo;

public class MailFolderCallable implements Callable<Object>, Serializable {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(MailFolderCallable.class);
    
    private static transient MailField[] CHANGEABLE_FIELDS = new MailField[] { 
        MailField.ID,
        MailField.FLAGS,
        MailField.COLOR_LABEL };

    private static final long serialVersionUID = -900105721652425254L;
    
    private static final int CHUNK_SIZE = 100;
    
    private final MailJobInfo info;
    
    
    public MailFolderCallable(MailJobInfo info) {
        super();
        this.info = info;
    }


    public void performJob() throws OXException, InterruptedException {
        if (CHANGEABLE_FIELDS == null) {
            CHANGEABLE_FIELDS = new MailField[] { 
                    MailField.ID,
                    MailField.FLAGS,
                    MailField.COLOR_LABEL };
        }
        
        long start = System.currentTimeMillis();
        if (LOG.isDebugEnabled()) {
            LOG.debug(this.getClass().getSimpleName() + " started performing. " + info.toString());
        }
        
        checkJobInfo();
        IndexFacadeService indexFacade = Services.getService(IndexFacadeService.class);
        IndexAccess<MailMessage> mailIndex = indexFacade.acquireIndexAccess(Types.EMAIL, info.userId, info.contextId);
        IndexAccess<Attachment> attachmentIndex = indexFacade.acquireIndexAccess(Types.ATTACHMENT, info.userId, info.contextId);
        MailService mailService = Services.getService(MailService.class);
        MailAccess<? extends IMailFolderStorage,? extends IMailMessageStorage> mailAccess = mailService.getMailAccess(info.userId, info.contextId, info.accountId);
        mailAccess.connect(false);
        try {            
            Map<String, Object> params = new HashMap<String, Object>();
            params.put(IndexConstants.ACCOUNT, Integer.valueOf(info.accountId));
            Builder queryBuilder = new Builder(params);
            QueryParameters mailAllQuery = queryBuilder.setHandler(SearchHandler.ALL_REQUEST)
                .setFolders(Collections.singleton(info.folder))
                .setSortField(MailIndexField.RECEIVED_DATE)
                .setOrder(Order.DESC)
                .build();
            
            IMailFolderStorage folderStorage = mailAccess.getFolderStorage();
            if (folderStorage.exists(info.folder)) {
                IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
                MailMessage[] storageResult = messageStorage.searchMessages(
                    info.folder,
                    IndexRange.NULL,
                    MailSortField.RECEIVED_DATE,
                    OrderDirection.DESC,
                    null,
                    CHANGEABLE_FIELDS);                
                Map<String, MailMessage> storageMails = new HashMap<String, MailMessage>();
                for (MailMessage msg : storageResult) {
                    storageMails.put(msg.getMailId(), msg);
                }
                
                if (IndexFolderManager.isIndexed(info.contextId, info.userId, Types.EMAIL, String.valueOf(info.accountId), info.folder)) {         
                    IndexResult<MailMessage> indexResult = mailIndex.query(mailAllQuery, MailIndexField.getFor(CHANGEABLE_FIELDS));                    
                    Map<String, MailMessage> indexMails = new HashMap<String, MailMessage>();
                    for (IndexDocument<MailMessage> document : indexResult.getResults()) {
                        MailMessage msg = document.getObject();
                        indexMails.put(msg.getMailId(), msg);
                    }                
                    
                    deleteMails(indexMails.keySet(), storageMails.keySet(), mailIndex, attachmentIndex);
                    addMails(indexMails.keySet(), storageMails.keySet(), mailIndex, attachmentIndex, messageStorage);
                    changeMails(indexMails, storageMails, mailIndex, attachmentIndex, messageStorage);              
                } else {
                    addMails(Collections.<String> emptySet(), storageMails.keySet(), mailIndex, attachmentIndex, messageStorage);
                    IndexFolderManager.setIndexed(info.contextId, info.userId, Types.EMAIL, String.valueOf(info.accountId), info.folder);
                }                
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Deleting info.folder from index: " + info.toString());
                }
                
                IndexFolderManager.deleteFolderEntry(info.contextId, info.userId, Types.EMAIL, String.valueOf(info.accountId), info.folder);
                mailIndex.deleteByQuery(mailAllQuery);
                Map<String, Object> attachmentAllParams = new HashMap<String, Object>();
                params.put(IndexConstants.MODULE, new Integer(Types.EMAIL));
                params.put(IndexConstants.ACCOUNT, Integer.toString(info.accountId));                
                QueryParameters attachmentAllQuery = new Builder(attachmentAllParams)
                    .setHandler(SearchHandler.ALL_REQUEST)
                    .setFolders(Collections.singleton(info.folder))
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
    
    private void changeMails(Map<String, MailMessage> indexMails, Map<String, MailMessage> storageMails, final IndexAccess<MailMessage> mailIndex, final IndexAccess<Attachment> attachmentIndex, final IMailMessageStorage messageStorage) throws OXException {
        Set<String> toRemove = new HashSet<String>(indexMails.keySet());
        toRemove.removeAll(storageMails.keySet());
        
        Set<String> toCompare = new HashSet<String>(indexMails.keySet());
        toCompare.removeAll(toRemove);
        
        final List<String> changedMails = new ArrayList<String>();
        for (String id : toCompare) {
            MailMessage storageMail = storageMails.get(id);
            MailMessage indexMail = indexMails.get(id);
            if (isDifferent(storageMail, indexMail)) {
                changedMails.add(storageMail.getMailId());
            }
        }
        
        if (changedMails.isEmpty()) {
            return;
        }
        ChunkPerformer.perform(new Performable() {            
            @Override
            public int perform(int off, int len) throws OXException {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Adding a chunk of mails of info.folder " + info.folder + ": " + info.toString());
                }
                
                List<String> subList = changedMails.subList(off, len);                
                MailMessage[] messages = messageStorage.getMessages(
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
                
                List<IndexDocument<MailMessage>> documents = new ArrayList<IndexDocument<MailMessage>>(messages.length);
                String[] primaryContents = messageStorage.getPrimaryContents(info.folder, mailIds);                
                for (int i = 0; i < messages.length; i++) {
                    MailMessage message = messages[i];
                    if (message != null) {
                        ContentAwareMailMessage contentAwareMessage = new ContentAwareMailMessage(primaryContents[i], message);
                        documents.add(new StandardIndexDocument<MailMessage>(contentAwareMessage));           
                    }
                }

                if (!documents.isEmpty()) {
                    mailIndex.addContent(documents, true);
                }
                
                return subList.size();
            }

            @Override
            public int getChunkSize() {
                return CHUNK_SIZE;
            }

            @Override
            public int getLength() {
                return changedMails.size();
            }

            @Override
            public int getInitialOffset() {
                return 0;
            }         
        });
    }

    private void addMails(Set<String> indexIds, Set<String> storageIds, final IndexAccess<MailMessage> mailIndex, final IndexAccess<Attachment> attachmentIndex, final IMailMessageStorage messageStorage) throws OXException {
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
                    LOG.debug("Adding a chunk of mails of info.folder " + info.folder + ": " + info.toString());
                }
                
                List<String> subList = toAdd.subList(off, len);
                MailMessage[] messages = messageStorage.getMessages(
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
                
                List<IndexDocument<MailMessage>> documents = new ArrayList<IndexDocument<MailMessage>>(messages.length);
                List<IndexDocument<Attachment>> attachments = new ArrayList<IndexDocument<Attachment>>();
                String[] primaryContents = messageStorage.getPrimaryContents(info.folder, mailIds);                
                for (int i = 0; i < messages.length; i++) {
                    MailMessage message = messages[i];
                    if (message != null) {
                        ContentAwareMailMessage contentAwareMessage = new ContentAwareMailMessage(primaryContents[i], message);
                        documents.add(new StandardIndexDocument<MailMessage>(contentAwareMessage));
                        IndexMailHandler handler = new IndexMailHandler(String.valueOf(info.accountId), info.folder, message.getMailId());
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

    private void deleteMails(Set<String> indexIds, Set<String> storageIds, final IndexAccess<MailMessage> mailIndex, final IndexAccess<Attachment> attachmentIndex) throws OXException {
        final List<String> toDelete = new ArrayList<String>(indexIds);
        toDelete.removeAll(storageIds);
        if (toDelete.isEmpty()) {
            return;
        }
        
        ChunkPerformer.perform(new Performable() {
            @Override
            public int perform(int off, int len) throws OXException {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Deleting a chunk of mails in info.folder " + info.folder + ": " + info.toString());
                }
                
                List<String> subList = toDelete.subList(off, len);
                SearchTerm<?>[] idTerms = new SearchTerm<?>[subList.size()];
                String[] mailUuids = new String[subList.size()];
                int i = 0;        
                for (String id : subList) {
                    mailUuids[i] = new MailUUID(info.contextId, info.userId, info.accountId, info.folder, id).getUUID();
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
                    .setFolders(Collections.singleton(info.folder))
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
    
    private boolean isDifferent(final MailMessage storageMail, final MailMessage indexMail) {
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
            storageUserFlags = null == stoUserFlags ? Collections.<String> emptySet() : new HashSet<String>(Arrays.asList(stoUserFlags));
        }
        final Set<String> indexUserFlags;
        {
            final String[] idxUserFlags = indexMail.getUserFlags();
            indexUserFlags = null == idxUserFlags ? Collections.<String> emptySet() : new HashSet<String>(Arrays.asList(idxUserFlags));
        }
        return (!storageUserFlags.equals(indexUserFlags));
    }
    
//    private MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> getMailAccess() throws OXException {
//        Session session = new FakeSession(info.primaryPassword, info.userId, info.contextId);
//        session.setParameter("com.openexchange.mail.lookupMailAccessCache", Boolean.FALSE);
//        MailService mailService = Services.getService(MailService.class);
//        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> tmp = mailService.getMailAccess(info.userId, info.contextId, info.accountId);
//        /*
//         * Safety close & not cacheable
//         */
//        tmp.close(true);
//        tmp.setCacheable(false);
//        /*
//         * Parameterize configuration
//         */
//        MailConfig mailConfig = tmp.getMailConfig();
//        mailConfig.setLogin(info.login);
//        mailConfig.setPassword(info.password);
//        mailConfig.setServer(info.server);
//        mailConfig.setPort(info.port);
//        mailConfig.setSecure(info.secure);
//        tmp.connect(true);
//        
//        return tmp;
//    }
//    
    private void closeMailAccess(MailAccess<? extends IMailFolderStorage,? extends IMailMessageStorage> mailAccess) {
        if (mailAccess != null) {            
            mailAccess.close(false);
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
    public Object call() throws Exception {
        performJob();
        return null;
    }
}

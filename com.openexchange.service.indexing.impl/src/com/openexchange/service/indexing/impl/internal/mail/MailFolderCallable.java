package com.openexchange.service.indexing.impl.internal.mail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.attach.index.Attachment;
import com.openexchange.imap.IMAPException;
import com.openexchange.index.IndexAccess;
import com.openexchange.index.IndexConstants;
import com.openexchange.index.IndexDocument;
import com.openexchange.index.IndexFacadeService;
import com.openexchange.index.IndexResult;
import com.openexchange.index.QueryParameters;
import com.openexchange.index.QueryParameters.Builder;
import com.openexchange.index.QueryParameters.Order;
import com.openexchange.index.SearchHandler;
import com.openexchange.index.solr.IndexFolderManager;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.index.MailIndexField;
import com.openexchange.mail.service.MailService;
import com.openexchange.service.indexing.IndexingService;
import com.openexchange.service.indexing.impl.internal.Services;
import com.openexchange.service.indexing.impl.mail.AbstractMailCallable;
import com.openexchange.service.indexing.impl.mail.MailJobInfo;

public class MailFolderCallable extends AbstractMailCallable {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(MailFolderCallable.class);
    
    private static transient MailField[] CHANGEABLE_FIELDS = new MailField[] { 
        MailField.ID,
        MailField.FLAGS,
        MailField.COLOR_LABEL };

    private static final long serialVersionUID = -900105721652425254L;
    
    
    public MailFolderCallable(MailJobInfo info) {
        super(info);
    }

    @Override
    public Object call() throws OXException, InterruptedException {
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
        try {
            mailAccess.connect();
            Map<String, Object> params = new HashMap<String, Object>();
            params.put(IndexConstants.ACCOUNT, String.valueOf(info.accountId));
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
                
                if (!info.force && IndexFolderManager.isIndexed(info.contextId, info.userId, Types.EMAIL, String.valueOf(info.accountId), info.folder)) {         
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
                    LOG.debug("Deleting folder from index: " + info.toString());
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
        } catch (OXException e) {
            // If connect to mail access failed, reschedule this job
            if (e.getCategory().equals(IMAPException.IMAPCode.CONNECTION_UNAVAILABLE.getCategory())
                && e.getCode() == IMAPException.IMAPCode.CONNECTION_UNAVAILABLE.getNumber()) {
                IndexingService indexingService = Services.getService(IndexingService.class);
                indexingService.scheduleJob(info, null, -1L, IndexingService.DEFAULT_PRIORITY);
            }
            throw e;
        } finally {
            closeMailAccess(mailAccess);
            closeIndexAccess(mailIndex);
            closeIndexAccess(attachmentIndex);
            
            if (LOG.isDebugEnabled()) {
                long diff = System.currentTimeMillis() - start;
                LOG.debug(this.getClass().getSimpleName() + " lasted " + diff + "ms. " + info.toString());
            }
        }
        
        return null;        
    }
    
    private void checkJobInfo() throws OXException {
        // TODO: implement
    }

    private void addMails(Set<String> indexIds, Set<String> storageIds, final IndexAccess<MailMessage> mailIndex, final IndexAccess<Attachment> attachmentIndex, final IMailMessageStorage messageStorage) throws OXException {
        final List<String> toAdd = new ArrayList<String>(storageIds);
        toAdd.removeAll(indexIds);        
        addMails(toAdd, messageStorage, mailIndex, attachmentIndex);
    }

    private void deleteMails(Set<String> indexIds, Set<String> storageIds, final IndexAccess<MailMessage> mailIndex, final IndexAccess<Attachment> attachmentIndex) throws OXException {
        final List<String> toDelete = new ArrayList<String>(indexIds);
        toDelete.removeAll(storageIds);
        deleteMails(toDelete, mailIndex, attachmentIndex);        
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
        
        changeMails(changedMails, messageStorage, mailIndex, attachmentIndex);
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
}

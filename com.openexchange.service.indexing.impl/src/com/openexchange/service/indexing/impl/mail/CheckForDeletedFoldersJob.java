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

package com.openexchange.service.indexing.impl.mail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import org.apache.commons.logging.Log;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.attach.index.Attachment;
import com.openexchange.groupware.attach.index.ORTerm;
import com.openexchange.groupware.attach.index.ObjectIdTerm;
import com.openexchange.groupware.attach.index.SearchTerm;
import com.openexchange.index.IndexAccess;
import com.openexchange.index.IndexConstants;
import com.openexchange.index.IndexDocument;
import com.openexchange.index.IndexFacadeService;
import com.openexchange.index.IndexResult;
import com.openexchange.index.QueryParameters;
import com.openexchange.index.SearchHandler;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.index.MailIndexField;
import com.openexchange.mail.index.MailUUID;
import com.openexchange.service.indexing.JobInfo;
import com.openexchange.service.indexing.impl.AbstractIndexingJob;
import com.openexchange.service.indexing.impl.internal.Services;


/**
 * {@link CheckForDeletedFoldersJob}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class CheckForDeletedFoldersJob extends AbstractIndexingJob {
    
    public static final String ALL_FOLDERS = "allFolders";
    

    @Override
    public void execute(JobInfo jobInfo) throws OXException {
        try {
            if (!(jobInfo instanceof MailJobInfo)) {
                throw new IllegalArgumentException("Job info must be an instance of MailJobInfo.");
            }
            
            MailJobInfo mailJobInfo = (MailJobInfo) jobInfo;
            Callable<Object> callable = new CheckForDeletedFoldersCallable(mailJobInfo);
            submitCallable(Types.EMAIL, mailJobInfo, callable);
        } catch (Exception e) {
            throw new OXException(e);
        }
    }
    
    public static final class CheckForDeletedFoldersCallable extends AbstractMailCallable {
        
        private static final long serialVersionUID = -8320606542261340360L;
        
        private static final Log LOG = com.openexchange.log.Log.loggerFor(CheckForDeletedFoldersCallable.class);
        
        
        /**
         * Initializes a new {@link CheckForDeletedFoldersCallable}.
         * @param info
         */
        protected CheckForDeletedFoldersCallable(MailJobInfo info) {
            super(info);
        }

        @Override
        public Object call() throws Exception {
            long start = System.currentTimeMillis();
            if (LOG.isDebugEnabled()) {
                LOG.debug(this.getClass().getSimpleName() + " started performing. " + info.toString());
            }
            
            checkJobInfo();
            IndexFacadeService indexFacade = Services.getService(IndexFacadeService.class);
            final IndexAccess<MailMessage> mailIndex = indexFacade.acquireIndexAccess(Types.EMAIL, info.userId, info.contextId);
            final IndexAccess<Attachment> attachmentIndex = indexFacade.acquireIndexAccess(Types.ATTACHMENT, info.userId, info.contextId);
            Set<String> allFolders = (Set<String>) info.getProperty(ALL_FOLDERS);
            try {            
                Map<String, Object> params = new HashMap<String, Object>(2);
                params.put(IndexConstants.ACCOUNT, info.accountId);
                QueryParameters withFolders = new QueryParameters.Builder(params)
                    .setFolders(allFolders)
                    .setHandler(SearchHandler.ALL_REQUEST)
                    .build();
                
                QueryParameters withoutFolders = new QueryParameters.Builder(params)
                    .setHandler(SearchHandler.ALL_REQUEST)
                    .build();
                
                Set<MailIndexField> fields = EnumSet.noneOf(MailIndexField.class);
                Collections.addAll(fields, MailIndexField.ID, MailIndexField.ACCOUNT, MailIndexField.FULL_NAME);
                IndexResult<MailMessage> mailsInFolders = mailIndex.query(withFolders, fields);
                IndexResult<MailMessage> allMails = mailIndex.query(withoutFolders, fields);
                
                Set<MailUUID> uuidsInFolders = new HashSet<MailUUID>();
                for (IndexDocument<MailMessage> document : mailsInFolders.getResults()) {
                    MailMessage message = document.getObject();
                    MailUUID uuid = MailUUID.newUUID(info.contextId, info.userId, message);
                    uuidsInFolders.add(uuid);
                }
                
                Set<MailUUID> allUUIDs = new HashSet<MailUUID>();
                for (IndexDocument<MailMessage> document : allMails.getResults()) {
                    MailMessage message = document.getObject();
                    MailUUID uuid = MailUUID.newUUID(info.contextId, info.userId, message);
                    allUUIDs.add(uuid);
                }
                
                if (allUUIDs.removeAll(uuidsInFolders)) {
                    if (allUUIDs.isEmpty()) {
                        return null;
                    }
                    
                    MailUUID[] uuidArray = allUUIDs.toArray(new MailUUID[allUUIDs.size()]);
                    String[] mailUUIDs = new String[allUUIDs.size()];
                    Map<String, List<String>> deletedFullNames = new HashMap<String, List<String>>();
                    for (int i = 0; i < uuidArray.length; i++) {
                        MailUUID uuid = uuidArray[i];   
                        mailUUIDs[i] = uuid.toString();                        
                        
                        List<String> mails = deletedFullNames.get(uuid.getFullName());
                        if (mails == null) {
                            mails = new ArrayList<String>();
                            deletedFullNames.put(uuid.getFullName(), mails);
                        }
                        
                        mails.add(uuid.getMailId());                        
                    }
                    
                    /*
                     * Delete mails from mail index
                     */
                    params.put(IndexConstants.IDS, mailUUIDs);
                    QueryParameters deleteQuery = new QueryParameters.Builder(params)
                        .setHandler(SearchHandler.GET_REQUEST)
                        .build();
                
                    mailIndex.deleteByQuery(deleteQuery);
                    
                    /*
                     * Delete attachments
                     */
                    for (String folder : deletedFullNames.keySet()) {
                        List<String> objectIds = deletedFullNames.get(folder);
                        SearchTerm<?>[] idTerms = new SearchTerm<?>[objectIds.size()];
                        for (int i = 0; i < objectIds.size(); i++) {
                            String objectId = objectIds.get(i);
                            idTerms[i] = new ObjectIdTerm(objectId);
                        }
                        
                        SearchTerm<?> orTerm = new ORTerm(idTerms);
                        Map<String, Object> deleteAttachmentsParams = new HashMap<String, Object>();
                        deleteAttachmentsParams.put(IndexConstants.MODULE, new Integer(Types.EMAIL));
                        deleteAttachmentsParams.put(IndexConstants.ACCOUNT, String.valueOf(info.accountId));
                        QueryParameters deleteAttachmentsQuery = new QueryParameters.Builder(deleteAttachmentsParams)
                            .setHandler(SearchHandler.CUSTOM)
                            .setSearchTerm(orTerm)
                            .setFolders(Collections.singleton(folder))
                            .build();
                        attachmentIndex.deleteByQuery(deleteAttachmentsQuery);
                    }                    
                }
            } finally {
                closeIndexAccess(mailIndex);
                closeIndexAccess(attachmentIndex);
                
                if (LOG.isDebugEnabled()) {
                    long diff = System.currentTimeMillis() - start;
                    LOG.debug(this.getClass().getSimpleName() + " lasted " + diff + "ms. " + info.toString());
                }
            }
            
            return null;
        }

        private void checkJobInfo() {
            // TODO Auto-generated method stub
            
        }
        
    }

}

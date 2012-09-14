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

package com.openexchange.mail.smal.impl.index;

import java.util.Date;
import org.apache.commons.logging.Log;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import com.openexchange.exception.OXException;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.permission.MailPermission;
import com.openexchange.mail.smal.impl.SmalMailAccess;
import com.openexchange.mail.smal.impl.SmalServiceLookup;
import com.openexchange.mail.utils.MailPasswordUtil;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.service.indexing.IndexingService;
import com.openexchange.service.indexing.JobInfo;
import com.openexchange.service.indexing.impl.mail.MailFolderJob;
import com.openexchange.service.indexing.impl.mail.MailJobInfo;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondEventConstants;


/**
 * {@link SMALSessionEventHandler}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class SMALSessionEventHandler implements EventHandler {
    
    private static final Log LOG = com.openexchange.log.Log.loggerFor(SMALSessionEventHandler.class);
    

    @Override
    public void handleEvent(Event event) {
        try {
            String topic = event.getTopic();
            if (SessiondEventConstants.TOPIC_ADD_SESSION.equals(topic) || SessiondEventConstants.TOPIC_REACTIVATE_SESSION.equals(topic)) {
                Session session = (Session) event.getProperty(SessiondEventConstants.PROP_SESSION);
                MailAccountStorageService storageService = SmalServiceLookup.getServiceStatic(MailAccountStorageService.class);
                if (storageService == null) {
                    OXException e = ServiceExceptionCode.SERVICE_UNAVAILABLE.create(MailAccountStorageService.class.getName());
                    LOG.warn("Could not trigger mail indexing jobs.", e);
                    return;
                }
                
                IndexingService indexingService = SmalServiceLookup.getServiceStatic(IndexingService.class);
                if (indexingService == null) {
                    OXException e = ServiceExceptionCode.SERVICE_UNAVAILABLE.create(IndexingService.class.getName());
                    LOG.warn("Could not trigger mail indexing jobs.", e);
                    return;
                }
                
                int userId = session.getUserId();
                int contextId = session.getContextId();
                // TODO: Check if external accounts are allowed to be indexed
                for (MailAccount account : storageService.getUserMailAccounts(userId, contextId)) {              
                    int accountId = account.getId();
                    String decryptedPW = account.getPassword() == null ? session.getPassword() : MailPasswordUtil.decrypt(account.getPassword(), 
                        session, 
                        accountId, 
                        account.getLogin(), 
                        account.getMailServer());
                    
                    MailAccess<? extends IMailFolderStorage,? extends IMailMessageStorage> mailAccess = SmalMailAccess.getUnwrappedInstance(session, accountId);
                    try {
                        mailAccess.connect();
                        IMailFolderStorage folderStorage = mailAccess.getFolderStorage();
                        
                        // TODO: This is an ugly workaround to not index shared imap folders. Maybe the folder storage has to be extended here.
                        MailFolder rootFolder = folderStorage.getFolder("INBOX");
                        MailFolder draftsFolder = folderStorage.getFolder(folderStorage.getDraftsFolder());
                        MailFolder sentFolder = folderStorage.getFolder(folderStorage.getSentFolder());
                        MailFolder trashFolder = folderStorage.getFolder(folderStorage.getTrashFolder());                        
                        scheduleFolderJobsRecursive(indexingService, 
                            account, 
                            folderStorage, 
                            contextId, 
                            userId, 
                            session.getPassword(), 
                            decryptedPW, 
                            new MailFolder[] { rootFolder, draftsFolder, sentFolder, trashFolder });
                    } finally {
                        SmalMailAccess.closeUnwrappedInstance(mailAccess);
                    }
                }
            }
        } catch (Exception e) {
            LOG.warn("Error while triggering mail indexing jobs.", e);
        }
    }
    
    private final void scheduleFolderJobsRecursive(IndexingService indexingService, MailAccount account, IMailFolderStorage folderStorage, int contextId, int userId, String primaryPassword, String password, MailFolder[] subfolders) throws OXException {        
        for (MailFolder folder : subfolders) {
            if (!folder.exists() || (folder.containsShared() && folder.isShared()) || folder.isSpam() || folder.isConfirmedSpam()) {
                continue;
            }
            
            MailPermission ownPermission = folder.getOwnPermission();            
            String fullName = folder.getFullname();
            if (ownPermission.isFolderVisible() && ownPermission.canReadAllObjects() && folder.isHoldsMessages()) {
                int priority;
                if (account.isDefaultAccount() && folder.isInbox()) {
                    priority = 15;
                } else if (folder.isInbox()) {
                    priority = 10;
                } else if (folder.isTrash()) {
                    priority = 1;
                } else {
                    priority = 5;
                }

                JobInfo jobInfo = MailJobInfo.newBuilder(MailFolderJob.class)
                    .login(account.getLogin())
                    .accountId(account.getId())
                    .contextId(contextId)
                    .userId(userId)
                    .primaryPassword(primaryPassword)
                    .password(password)
                    .folder(fullName)
                    .build();                                
                indexingService.scheduleJob(jobInfo, new Date(), -1, priority);
            }
            
            if (folder.isHoldsFolders()) {
                MailFolder[] subsubfolders = folderStorage.getSubfolders(fullName, true);
                if (subsubfolders != null && subsubfolders.length > 0 && subsubfolders != IMailFolderStorage.EMPTY_PATH) {
                    scheduleFolderJobsRecursive(indexingService, 
                        account, 
                        folderStorage, 
                        contextId, 
                        userId, 
                        primaryPassword,
                        password,
                        subsubfolders);
                } 
            }                           
        }
    }
    
}

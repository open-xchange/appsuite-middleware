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

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
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
import com.openexchange.service.indexing.impl.mail.CheckForDeletedFoldersJob;
import com.openexchange.service.indexing.impl.mail.MailFolderJob;
import com.openexchange.service.indexing.impl.mail.MailJobInfo;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondEventConstants;


/**
 * {@link SmalSessionEventHandler}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class SmalSessionEventHandler implements EventHandler {
    
    private static final Log LOG = com.openexchange.log.Log.loggerFor(SmalSessionEventHandler.class);
    
    private static final long FOLDER_INTERVAL = 60000L * 60;

    private static final boolean ONLY_PRIMARY = true;
    

    @Override
    public void handleEvent(Event event) {
        try {            
            IndexingService indexingService = SmalServiceLookup.getServiceStatic(IndexingService.class);
            if (indexingService == null) {
                OXException e = ServiceExceptionCode.SERVICE_UNAVAILABLE.create(IndexingService.class.getName());
                LOG.warn("Could not handle session event.", e);
                return;
            }  
            
            String topic = event.getTopic();            
            if (SessiondEventConstants.TOPIC_ADD_SESSION.equals(topic) || SessiondEventConstants.TOPIC_REACTIVATE_SESSION.equals(topic)) {                
                Session session = (Session) event.getProperty(SessiondEventConstants.PROP_SESSION);
                int contextId = session.getContextId();
                int userId = session.getUserId();                
                UserContextKey userContextKey = new UserContextKey(contextId, userId);
                MailAccountStorageService storageService = SmalServiceLookup.getServiceStatic(MailAccountStorageService.class);
                if (storageService == null) {
                    OXException e = ServiceExceptionCode.SERVICE_UNAVAILABLE.create(MailAccountStorageService.class.getName());
                    LOG.warn("Could not handle session event.", e);
                    return;
                }
                
                IMap<UserContextKey,Integer> sessionMap = getSessionMap();
                sessionMap.lock(userContextKey);
                boolean goOn = true;
                try {
                    Integer sessionCount = sessionMap.get(userContextKey);
                    if (sessionCount == null) {
                        sessionCount = new Integer(0);
                    }
                    
                    sessionMap.put(userContextKey, new Integer(sessionCount.intValue() + 1));
                    if (sessionCount.intValue() > 0) {
                        goOn = false;
                    }
                } finally {
                    sessionMap.unlock(userContextKey);
                }
                
                if (goOn) {
                    Map<Integer, Set<MailFolder>> allFolders = calculateMailFolders(session, storageService);
                    scheduleFolderJobs(session, allFolders, storageService, indexingService);
                }
            } else if (SessiondEventConstants.TOPIC_REMOVE_SESSION.equals(topic)) {
                Session session = (Session) event.getProperty(SessiondEventConstants.PROP_SESSION);
                unschedule(session, indexingService);
            } else if (SessiondEventConstants.TOPIC_REMOVE_DATA.equals(topic)) {
                Map<String, Session> sessions = (Map<String, Session>) event.getProperty(SessiondEventConstants.PROP_CONTAINER);
                for (Session session : sessions.values()) {
                    unschedule(session, indexingService);
                }
            }
        } catch (Exception e) {
            LOG.warn("Error while triggering mail indexing jobs.", e);
        }
    }
    
    private void unschedule(Session session, IndexingService indexingService) throws OXException {
        UserContextKey userContextKey = new UserContextKey(session.getContextId(), session.getUserId());
        IMap<UserContextKey,Integer> sessionMap = getSessionMap();
        sessionMap.lock(userContextKey);
        boolean goOn = false;
        try {
            Integer sessionCount = sessionMap.get(userContextKey);
            if (sessionCount == null) {
                return;
            }
            
            if (sessionCount.intValue() == 1) {
                sessionMap.remove(userContextKey);
                goOn = true;
            } else {
                sessionMap.put(userContextKey, new Integer(sessionCount.intValue() - 1));
            }
        } finally {
            sessionMap.unlock(userContextKey);
        }
        
        if (goOn) {
            int userId = session.getUserId();
            int contextId = session.getContextId();
            indexingService.unscheduleAllForUser(contextId, userId);
        }
    }
    
    private void scheduleFolderJobs(Session session, Map<Integer, Set<MailFolder>> allFolders, MailAccountStorageService storageService, IndexingService indexingService) throws OXException {
        int contextId = session.getContextId();
        int userId = session.getUserId();        
        for (Integer accountId : allFolders.keySet()) {
            MailAccount account = storageService.getMailAccount(accountId.intValue(), userId, contextId);
            Set<MailFolder> folders = allFolders.get(accountId);
            String decryptedPW = account.getPassword() == null ? session.getPassword() : MailPasswordUtil.decrypt(
                account.getPassword(),
                session,
                accountId.intValue(),
                account.getLogin(),
                account.getMailServer());
            
            Set<String> fullNames = new HashSet<String>();
            for (MailFolder folder : folders) {         
                fullNames.add(folder.getFullname());
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
                    .primaryPassword(session.getPassword())
                    .password(decryptedPW)
                    .folder(folder.getFullname())
                    .build();                                
                indexingService.scheduleJob(jobInfo, IndexingService.NOW, FOLDER_INTERVAL, priority);
            }
            
            JobInfo checkDeletedJobInfo = MailJobInfo.newBuilder(CheckForDeletedFoldersJob.class)
                .accountId(account.getId())
                .contextId(contextId)
                .userId(userId)
                .addProperty(CheckForDeletedFoldersJob.ALL_FOLDERS, fullNames)
                .build();                                
            indexingService.scheduleJob(checkDeletedJobInfo, IndexingService.NOW, FOLDER_INTERVAL, IndexingService.DEFAULT_PRIORITY); 
        }
    }

//    private void scheduleFolderJobs(Session session, IndexingService indexingService, MailAccountStorageService storageService) throws OXException {
//        // TODO: Check if accounts are allowed to be indexed
//        int userId = session.getUserId();
//        int contextId = session.getContextId();
//        for (MailAccount account : storageService.getUserMailAccounts(userId, contextId)) {
//            int accountId = account.getId();
//            String decryptedPW = account.getPassword() == null ? session.getPassword() : MailPasswordUtil.decrypt(
//                account.getPassword(),
//                session,
//                accountId,
//                account.getLogin(),
//                account.getMailServer());
//
//            MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = SmalMailAccess.getUnwrappedInstance(
//                session,
//                accountId);
//            try {
//                mailAccess.connect();
//                IMailFolderStorage folderStorage = mailAccess.getFolderStorage();
//                MailFolder rootFolder = folderStorage.getRootFolder();
//                MailFolder[] subfolders = folderStorage.getSubfolders(rootFolder.getFullname(), true);
//                scheduleFolderJobsRecursive(
//                    indexingService,
//                    account,
//                    folderStorage,
//                    contextId,
//                    userId,
//                    session.getPassword(),
//                    decryptedPW,
//                    subfolders);
//            } finally {
//                SmalMailAccess.closeUnwrappedInstance(mailAccess);
//            }
//        }
//    }
    
    private Map<Integer, Set<MailFolder>> calculateMailFolders(Session session, MailAccountStorageService storageService) throws OXException {
        int userId = session.getUserId();
        int contextId = session.getContextId();
        MailAccount[] mailAccounts;
        if (ONLY_PRIMARY) {
            mailAccounts = new MailAccount[] { storageService.getDefaultMailAccount(userId, contextId) };
        } else {
            mailAccounts = storageService.getUserMailAccounts(userId, contextId);
        }
        
        Map<Integer, Set<MailFolder>> folderMap = new HashMap<Integer, Set<MailFolder>>();
        for (MailAccount account : mailAccounts) {
            Set<MailFolder> allFolders = new HashSet<MailFolder>();
            int accountId = account.getId();
            MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = SmalMailAccess.getUnwrappedInstance(
                session,
                accountId);
            try {
                mailAccess.connect();
                IMailFolderStorage folderStorage = mailAccess.getFolderStorage();
                MailFolder rootFolder = folderStorage.getRootFolder();
                MailFolder[] subfolders = folderStorage.getSubfolders(rootFolder.getFullname(), true);
                addFoldersRecursive(subfolders, allFolders, folderStorage);
                folderMap.put(new Integer(accountId), allFolders);
            } finally {
                SmalMailAccess.closeUnwrappedInstance(mailAccess);
            }   
        }
        
        return folderMap;
    }
    
    private void addFoldersRecursive(MailFolder[] subfolders, Set<MailFolder> allFolders, IMailFolderStorage folderStorage) throws OXException {
        for (MailFolder folder : subfolders) {
            if (!folder.exists() || folder.isSpam() || folder.isConfirmedSpam()) {                
                continue;
            }
            
            boolean index = true;
            if ((folder.containsShared() && folder.isShared()) || (folder.containsPublic() && folder.isPublic())) {
                index = false;
            }
            
            MailPermission ownPermission = folder.getOwnPermission();            
            if (index && ownPermission.isFolderVisible() && ownPermission.canReadAllObjects() && folder.isHoldsMessages()) {
                allFolders.add(folder);
            }
            
            if (folder.isHoldsFolders()) {
                MailFolder[] subsubfolders = folderStorage.getSubfolders(folder.getFullname(), true);
                if (subsubfolders != null && subsubfolders.length > 0 && subsubfolders != IMailFolderStorage.EMPTY_PATH) {
                    addFoldersRecursive(subsubfolders, allFolders, folderStorage);
                }
            }
        }
    }
    
//    private void scheduleFolderJobsRecursive(IndexingService indexingService, MailAccount account, IMailFolderStorage folderStorage, int contextId, int userId, String primaryPassword, String password, MailFolder[] subfolders) throws OXException {        
//        for (MailFolder folder : subfolders) {
//            if (!folder.exists() || folder.isSpam() || folder.isConfirmedSpam()) {                
//                continue;
//            }
//            
//            boolean index = true;
//            if ((folder.containsShared() && folder.isShared()) || (folder.containsPublic() && folder.isPublic())) {
//                index = false;
//            }
//            
//            MailPermission ownPermission = folder.getOwnPermission();            
//            if (index && ownPermission.isFolderVisible() && ownPermission.canReadAllObjects() && folder.isHoldsMessages()) {
//                int priority;
//                if (account.isDefaultAccount() && folder.isInbox()) {
//                    priority = 15;
//                } else if (folder.isInbox()) {
//                    priority = 10;
//                } else if (folder.isTrash()) {
//                    priority = 1;
//                } else {
//                    priority = 5;
//                }
//
//                JobInfo jobInfo = MailJobInfo.newBuilder(MailFolderJob.class)
//                    .login(account.getLogin())
//                    .accountId(account.getId())
//                    .contextId(contextId)
//                    .userId(userId)
//                    .primaryPassword(primaryPassword)
//                    .password(password)
//                    .folder(folder.getFullname())
//                    .build();                                
//                indexingService.scheduleJob(jobInfo, null, FOLDER_INTERVAL, priority);
//            }
//            
//            if (folder.isHoldsFolders()) {
//                MailFolder[] subsubfolders = folderStorage.getSubfolders(folder.getFullname(), true);
//                if (subsubfolders != null && subsubfolders.length > 0 && subsubfolders != IMailFolderStorage.EMPTY_PATH) {
//                    scheduleFolderJobsRecursive(indexingService, 
//                        account, 
//                        folderStorage, 
//                        contextId, 
//                        userId, 
//                        primaryPassword,
//                        password,
//                        subsubfolders);
//                } 
//            }                           
//        }
//    }
    
    private IMap<UserContextKey, Integer> getSessionMap() throws OXException {
        HazelcastInstance hazelcast = SmalServiceLookup.getServiceStatic(HazelcastInstance.class);
        if (hazelcast == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(HazelcastInstance.class.getName());
        }
        
        IMap<UserContextKey, Integer> sessionMap = hazelcast.getMap(SmalSessionEventHandler.class.getSimpleName() + ".SessionsForUser");
        return sessionMap;
    }
    
    public static final class UserContextKey implements Serializable {

        private static final long serialVersionUID = -3719681448183194849L;

        private final int contextId;
        
        private final int userId;
        

        /**
         * Initializes a new {@link UserContextKey}.
         * @param contextId
         * @param userId
         */
        public UserContextKey(int contextId, int userId) {
            super();
            this.contextId = contextId;
            this.userId = userId;
        }
        
        /**
         * Gets the contextId
         *
         * @return The contextId
         */
        public final int getContextId() {
            return contextId;
        }
        
        /**
         * Gets the userId
         *
         * @return The userId
         */
        public final int getUserId() {
            return userId;
        }        
        
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + contextId;
            result = prime * result + userId;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            UserContextKey other = (UserContextKey) obj;
            if (contextId != other.contextId)
                return false;
            if (userId != other.userId)
                return false;
            return true;
        }
    }
    
}

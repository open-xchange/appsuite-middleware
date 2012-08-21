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

package com.openexchange.service.indexing.infostore;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.logging.Log;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderFilter;
import com.openexchange.folderstorage.FolderResponse;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.database.contentType.InfostoreContentType;
import com.openexchange.folderstorage.filestorage.contentType.FileStorageContentType;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.index.IndexAccess;
import com.openexchange.index.IndexConstants;
import com.openexchange.index.IndexFacadeService;
import com.openexchange.index.StandardIndexDocument;
import com.openexchange.index.attachments.Attachment;
import com.openexchange.index.filestore.FileUUID;
import com.openexchange.service.indexing.IndexingJob;
import com.openexchange.service.indexing.impl.Services;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;


/**
 * {@link InfostoreAccountJob}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class InfostoreAccountJob implements IndexingJob {
    
    private static final Log LOG = com.openexchange.log.Log.loggerFor(InfostoreAccountJob.class);

    private static final long serialVersionUID = 5282863207520957796L;
    
    private int priority;
    
    private long timestamp;

    private ConcurrentHashMap<String, Object> properties;

    private Session session;
    
    
    public InfostoreAccountJob(Session session) {
        super();
        this.session = session;
        priority = 4;
        timestamp = System.currentTimeMillis();
        properties = new ConcurrentHashMap<String, Object>();
    }

    @Override
    public Class<?>[] getNeededServices() {
        return new Class<?>[] { IDBasedFileAccessFactory.class, FolderService.class, IndexFacadeService.class };
    }

    @Override
    public void performJob() throws OXException, InterruptedException {
        /*
         *  FIXME: This f... f... does not work at all. 
         *  We need a reliable concept where to store documents based on their permissions.
         */
        if (true) {
            return;
        }
        
        long start = System.currentTimeMillis();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting InfostoreAccountJob for user " + session.getUserId() + " in context " + session.getContextId() + ".");
        }
        
        if (session == null) {
            throw new IllegalStateException("Session must not be null!");
        }
        
        FolderService folderService = Services.getService(FolderService.class);
        List<ContentType> contentTypes = new ArrayList<ContentType>(1);
        contentTypes.add(FileStorageContentType.getInstance());
        contentTypes.add(InfostoreContentType.getInstance());
        FolderResponse<UserizedFolder[]> subfolders = folderService.getAllVisibleFolders(
            FolderStorage.REAL_TREE_ID, 
            new FolderFilter() {
                @Override
                public boolean accept(Folder folder) {
                    boolean isInfostoreFolder = folder.getContentType().equals(InfostoreContentType.getInstance())
                                             || folder.getContentType().equals(FileStorageContentType.getInstance());
                    boolean isFolderOwner = folder.getCreatedBy() == session.getUserId();                    
                    if (isInfostoreFolder && isFolderOwner) {
                        return true;
                    }
                    
                    return false;
                }                
            }, 
            session,
            new FolderServiceDecorator().setAllowedContentTypes(contentTypes));
        
        IndexFacadeService indexFacade = Services.getService(IndexFacadeService.class);        
        IDBasedFileAccessFactory fileAccessFactory = Services.getService(IDBasedFileAccessFactory.class);
        IDBasedFileAccess fileAccess = fileAccessFactory.createAccess(session);
        for (UserizedFolder folder : subfolders.getResponse()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Found folder " + folder.getID());
            }
            
            indexFolder(indexFacade, fileAccess, folder);
        }        
        
        if (LOG.isDebugEnabled()) {
            long diff = System.currentTimeMillis() - start;
            LOG.debug("InfostoreAccountJob lasted " + diff + "ms.");
        }
    }
    
    private void indexFolder(IndexFacadeService indexFacade, IDBasedFileAccess fileAccess, UserizedFolder folder) throws OXException {
        TimedResult<File> documents = fileAccess.getDocuments(folder.getID());
        SearchIterator<File> iterator = documents.results();
        while (iterator.hasNext()) {
            File fileMetadata = iterator.next();
            FileID fileID = new FileID(fileMetadata.getId());
            FolderID folderID = new FolderID(fileMetadata.getFolderId());
            String service = fileID.getService();
            String account = fileID.getAccountId();
            
            IndexAccess<File> infostoreIndex = indexFacade.acquireIndexAccess(Types.INFOSTORE, session);
            IndexAccess<Attachment> attachmentIndex = indexFacade.acquireIndexAccess(Types.ATTACHMENT, session);
            StandardIndexDocument<File> document = new StandardIndexDocument<File>(fileMetadata);
            document.addProperty(IndexConstants.SERVICE, service);
            document.addProperty(IndexConstants.ACCOUNT, account);
            infostoreIndex.addContent(document, true);
            
            if (hasAttachment(fileMetadata)) {
                InputStream is = fileAccess.getDocument(fileID.toUniqueID(), FileStorageFileAccess.CURRENT_VERSION);
                Attachment attachment = new Attachment();
                attachment.setModule(Types.INFOSTORE);
                attachment.setService(service);
                attachment.setAccount(account);
                attachment.setFolder(folderID.getFolderId());
                attachment.setObjectId(fileID.getFileId());
                attachment.setAttachmentId(IndexConstants.DEFAULT_ATTACHMENT);
                attachment.setFileName(fileMetadata.getFileName());
                attachment.setFileSize(fileMetadata.getFileSize());
                attachment.setMimeType(fileMetadata.getFileMIMEType());
                attachment.setMd5Sum(fileMetadata.getFileMD5Sum());
                attachment.setContent(is);
                      
                attachmentIndex.addContent(new StandardIndexDocument<Attachment>(attachment), true);
            }
            
            if (LOG.isDebugEnabled()) {
                LOG.debug("Found file " + FileUUID.newUUID(fileID.getService(), fileID.getAccountId(), folderID.getFolderId(), fileID.getFileId()));
            }
        }
    }
    
    private boolean hasAttachment(File file) {
        return file.getFileName() != null && !file.getFileName().isEmpty() && file.getFileSize() > 0;
    }

    @Override
    public boolean isDurable() {
        return false;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public long getTimeStamp() {
        return timestamp;
    }

    @Override
    public Origin getOrigin() {
        return Origin.PASSIVE;
    }

    @Override
    public Behavior getBehavior() {
        return Behavior.CONSUMER_RUNS;
    }

    @Override
    public void beforeExecute() {
    }

    @Override
    public void afterExecute(Throwable t) {
    }

    @Override
    public Map<String, ?> getProperties() {
        return properties;
    }

}

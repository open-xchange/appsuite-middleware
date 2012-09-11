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

package com.openexchange.index.solr.groupware;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;


/**
 * {@link SolrFilestoreEventHandler}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class SolrFilestoreEventHandler implements EventHandler {
    
    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(SolrFilestoreEventHandler.class));

    // FIXME: remove class
    @Override
    public void handleEvent(Event event) {
        // TODO Auto-generated method stub
        
    }
    

//    @Override
//    public void handleEvent(Event event) {
//        if (FileStorageEventHelper.isInfostoreEvent(event)) {
//            try {
//                Session session = FileStorageEventHelper.extractSession(event);
//                IDBasedFileAccessFactory accessFactory = Services.getService(IDBasedFileAccessFactory.class);
//                IndexFacadeService indexService = Services.getService(IndexFacadeService.class);                    
//                IDBasedFileAccess access = accessFactory.createAccess(session);
//                IndexAccess<File> filestoreIndexAccess = indexService.acquireIndexAccess(Types.INFOSTORE, session);
//                IndexAccess<Attachment> attachmentIndexAccess = indexService.acquireIndexAccess(Types.ATTACHMENT, session);
//                String service = FileStorageEventHelper.extractService(event);
//                String accountId = FileStorageEventHelper.extractAccountId(event);                    
//                String id = FileStorageEventHelper.extractObjectId(event);
//                if (FileStorageEventHelper.isCreateEvent(event)) {
//                    indexFile(access, filestoreIndexAccess, attachmentIndexAccess, session, id, service, accountId);
//                } else if (FileStorageEventHelper.isUpdateEvent(event)) {
//                    // Just reindex
//                    indexFile(access, filestoreIndexAccess, attachmentIndexAccess, session, id, service, accountId);
//                } else if (FileStorageEventHelper.isDeleteEvent(event)) {
//                    String folderId = FileStorageEventHelper.extractFolderId(event);
//                    InfostoreUUID uuid = InfostoreUUID.newUUID(service, accountId, folderId, id);
//                    filestoreIndexAccess.deleteById(uuid.toString()); 
//                    attachmentIndexAccess.deleteById(AttachmentUUID.newUUID(Types.INFOSTORE, service, accountId, folderId, id, IndexConstants.DEFAULT_ATTACHMENT).toString());
//                    if (access.exists(id, FileStorageFileAccess.CURRENT_VERSION)) {
//                        // One or more versions have been deleted. 
//                        // We have to reindex the current one.                                           
//                        indexFile(access, filestoreIndexAccess, attachmentIndexAccess, session, id, service, accountId);
//                    }
//                }
//            } catch (OXException e) {
//                LOG.error(e.getMessage(), e);
//            } catch (Throwable e) {
//                LOG.error(e.getMessage(), e);
//            }
//        }        
//    }
//    
//    private void indexFile(IDBasedFileAccess access, IndexAccess<File> filestoreIndexAccess, IndexAccess<Attachment> attachmentIndexAccess, Session session, String id, String service, String accountId) throws OXException {        
//        File fileMetadata = access.getFileMetadata(id, FileStorageFileAccess.CURRENT_VERSION);
//        StandardIndexDocument<File> document = new StandardIndexDocument<File>(fileMetadata);
//        document.addProperty(IndexConstants.SERVICE, service);
//        document.addProperty(IndexConstants.ACCOUNT, accountId);    
//        filestoreIndexAccess.addContent(document, true);
//        
//        if (hasAttachment(fileMetadata)) {
//            InputStream is = access.getDocument(id, FileStorageFileAccess.CURRENT_VERSION);
//            Attachment attachment = new Attachment();
//            attachment.setModule(Types.INFOSTORE);
//            attachment.setService(service);
//            attachment.setAccount(accountId);
//            attachment.setFolder(fileMetadata.getFolderId());
//            attachment.setObjectId(id);
//            attachment.setAttachmentId(IndexConstants.DEFAULT_ATTACHMENT);
//            attachment.setFileName(fileMetadata.getFileName());
//            attachment.setFileSize(fileMetadata.getFileSize());
//            attachment.setMimeType(fileMetadata.getFileMIMEType());
//            attachment.setMd5Sum(fileMetadata.getFileMD5Sum());
//            attachment.setContent(is);
//                  
//            attachmentIndexAccess.addContent(new StandardIndexDocument<Attachment>(attachment), true);
//        }
//    }
//    
//    private boolean hasAttachment(File file) {
//        return file.getFileName() != null && !file.getFileName().isEmpty() && file.getFileSize() > 0;
//    }

}

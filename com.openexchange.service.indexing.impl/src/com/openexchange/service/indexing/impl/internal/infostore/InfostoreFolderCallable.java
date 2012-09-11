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

package com.openexchange.service.indexing.impl.internal.infostore;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import org.apache.commons.logging.Log;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.groupware.tools.chunk.ChunkPerformer;
import com.openexchange.groupware.tools.chunk.Performable;
import com.openexchange.index.IndexAccess;
import com.openexchange.index.IndexDocument;
import com.openexchange.index.IndexFacadeService;
import com.openexchange.index.StandardIndexDocument;
import com.openexchange.index.attachments.Attachment;
import com.openexchange.service.indexing.impl.infostore.InfostoreJobInfo;
import com.openexchange.service.indexing.impl.internal.FakeSession;
import com.openexchange.service.indexing.impl.internal.Services;
import com.openexchange.tools.iterator.SearchIterator;


/**
 * {@link InfostoreFolderCallable}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class InfostoreFolderCallable implements Callable<Object>, Serializable {

    private static final int CHUNK_SIZE = 100;

    private static final long serialVersionUID = -8114344313753991788L;
    
    private static final Log LOG = com.openexchange.log.Log.loggerFor(InfostoreFolderCallable.class);
    
    private final InfostoreJobInfo info;
    
    
    public InfostoreFolderCallable(InfostoreJobInfo info) {
        super();
        this.info = info;
    }

    @Override
    public Object call() throws Exception {
        long start = System.currentTimeMillis();
        if (LOG.isDebugEnabled()) {
            LOG.debug(this.getClass().getSimpleName() + " started performing. " + info.toString());
        }
        
        checkJobInfo();
        IndexFacadeService indexFacade = Services.getService(IndexFacadeService.class);
        final IndexAccess<File> infostoreIndex = indexFacade.acquireIndexAccess(Types.INFOSTORE, info.userId, info.contextId);
        final IndexAccess<Attachment> attachmentIndex = indexFacade.acquireIndexAccess(Types.ATTACHMENT, info.userId, info.contextId);
        try {
            IDBasedFileAccessFactory fileAccessFactory = Services.getService(IDBasedFileAccessFactory.class);
            IDBasedFileAccess fileAccess = fileAccessFactory.createAccess(new FakeSession(null, info.userId, info.contextId));
            FolderID folderID = new FolderID(info.service, info.account, info.folder);
            TimedResult<File> documents = fileAccess.getDocuments(folderID.toUniqueID());
            SearchIterator<File> it = documents.results();
            final List<IndexDocument<File>> indexDocuments = new ArrayList<IndexDocument<File>>();
            final List<IndexDocument<Attachment>> attachments = new ArrayList<IndexDocument<Attachment>>();
            while (it.hasNext()) {
                File file = it.next();
                StandardIndexDocument<File> indexDocument = new StandardIndexDocument<File>(file);
                indexDocuments.add(indexDocument);
                
                if (file.getFileName() != null || file.getFileSize() > 0) {
                    try {
                        InputStream document = fileAccess.getDocument(file.getId(), FileStorageFileAccess.CURRENT_VERSION);
                        if (document != null) {
                            Attachment attachment = new Attachment();
                            attachment.setModule(Types.INFOSTORE);
                            attachment.setService(info.service);
                            attachment.setAccount(info.account);
                            attachment.setAttachmentId(String.valueOf(file.getVersion()));
                            attachment.setObjectId(new FileID(file.getId()).getFileId());
                            attachment.setFolder(folderID.getFolderId());
                            attachment.setFileName(file.getFileName());
                            attachment.setFileSize(file.getFileSize());
                            attachment.setMimeType(file.getFileMIMEType());
                            attachment.setMd5Sum(file.getFileMD5Sum());
                            attachment.setContent(document);
                            
                            attachments.add(new StandardIndexDocument<Attachment>(attachment));
                        }
                    } catch (OXException e) {
                        LOG.warn("Could not get attachment input stream for infostore document.", e);
                    }
                }
            }
            
            ChunkPerformer.perform(new Performable() {
                @Override
                public int perform(int off, int len) throws OXException {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Adding a chunk of files to the index.");
                    }
                    
                    List<IndexDocument<File>> subList = indexDocuments.subList(off, len);
                    infostoreIndex.addContent(subList, true);
                    
                    return subList.size();
                }

                @Override
                public int getChunkSize() {
                    return CHUNK_SIZE;
                }

                @Override
                public int getLength() {
                    return indexDocuments.size();
                }

                @Override
                public int getInitialOffset() {
                    return 0;
                }
            });
            
            ChunkPerformer.perform(new Performable() {
                @Override
                public int perform(int off, int len) throws OXException {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Adding a chunk of attachments to the index.");
                    }
                    
                    List<IndexDocument<Attachment>> subList = attachments.subList(off, len);
                    attachmentIndex.addContent(subList, true);
                    
                    return subList.size();
                }

                @Override
                public int getChunkSize() {
                    return CHUNK_SIZE;
                }

                @Override
                public int getLength() {
                    return indexDocuments.size();
                }

                @Override
                public int getInitialOffset() {
                    return 0;
                }
            });
        } finally {
            closeIndexAccess(infostoreIndex);
            closeIndexAccess(attachmentIndex);
            
            if (LOG.isDebugEnabled()) {
                long diff = System.currentTimeMillis() - start;
                LOG.debug(this.getClass().getSimpleName() + " lasted " + diff + "ms. " + info.toString());
            }
        }
        
        return null;
    }
    
    private void closeIndexAccess(IndexAccess<?> indexAccess) throws OXException {
        if (indexAccess != null) {
            IndexFacadeService indexFacade = Services.getService(IndexFacadeService.class);
            indexFacade.releaseIndexAccess(indexAccess);
        }    
    }

    private void checkJobInfo() {
        // TODO Auto-generated method stub
        
    }

}

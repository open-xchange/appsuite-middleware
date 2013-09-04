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

package com.openexchange.service.indexing.impl.infostore;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.logging.Log;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.attach.index.Attachment;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.groupware.tools.chunk.ChunkPerformer;
import com.openexchange.groupware.tools.chunk.ListPerformable;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.index.AccountFolders;
import com.openexchange.index.IndexAccess;
import com.openexchange.index.IndexDocument;
import com.openexchange.index.IndexFacadeService;
import com.openexchange.index.QueryParameters;
import com.openexchange.index.QueryParameters.Builder;
import com.openexchange.index.SearchHandlers;
import com.openexchange.index.StandardIndexDocument;
import com.openexchange.index.solr.IndexFolderManager;
import com.openexchange.service.indexing.IndexingJob;
import com.openexchange.service.indexing.JobInfo;
import com.openexchange.service.indexing.impl.internal.Services;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.user.UserService;
import com.openexchange.userconf.UserPermissionService;


/**
 * {@link InfostoreFolderJob}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class InfostoreFolderJob implements IndexingJob {

    private static final int CHUNK_SIZE = 100;

    private static final Log LOG = com.openexchange.log.Log.loggerFor(InfostoreFolderJob.class);


    public InfostoreFolderJob() {
        super();
    }

    @Override
    public void execute(JobInfo jobInfo) throws OXException {
        if (!(jobInfo instanceof InfostoreJobInfo)) {
            throw new IllegalArgumentException("Job info must be an instance of InfostoreJobInfo.");
        }

        InfostoreJobInfo info = (InfostoreJobInfo) jobInfo;
        long start = System.currentTimeMillis();
        if (LOG.isDebugEnabled()) {
            LOG.debug(this.getClass().getSimpleName() + " started performing. " + info.toString());
        }

        checkJobInfo();
        if (info.force || !IndexFolderManager.isIndexed(
            info.contextId,
            info.userId,
            Types.INFOSTORE,
            info.account,
            String.valueOf(info.folder))) {

            ContextService contextService = Services.getService(ContextService.class);
            UserService userService = Services.getService(UserService.class);
            UserPermissionService userConfigurationService = Services.getService(UserPermissionService.class);
            Context context = contextService.getContext(info.contextId);
            User user = userService.getUser(info.userId, context);
            UserPermissionBits permissionBits = userConfigurationService.getUserPermissionBits(info.userId, context);
            IndexFacadeService indexFacade = Services.getService(IndexFacadeService.class);
            final IndexAccess<DocumentMetadata> infostoreIndex = indexFacade.acquireIndexAccess(
                Types.INFOSTORE,
                info.userId,
                info.contextId);
            final IndexAccess<Attachment> attachmentIndex = indexFacade.acquireIndexAccess(Types.ATTACHMENT, info.userId, info.contextId);
            try {
                if (info.deleteFolder) {
                    deleteFromIndex(info, infostoreIndex, attachmentIndex);
                } else {
                    indexFolder(info, context, user, permissionBits, infostoreIndex, attachmentIndex);
                }
            } finally {
                closeIndexAccess(infostoreIndex);
                closeIndexAccess(attachmentIndex);

                if (LOG.isDebugEnabled()) {
                    long diff = System.currentTimeMillis() - start;
                    LOG.debug(this.getClass().getSimpleName() + " lasted " + diff + "ms. " + info.toString());
                }
            }
        }
    }

    private void deleteFromIndex(InfostoreJobInfo info, IndexAccess<DocumentMetadata> infostoreIndex, IndexAccess<Attachment> attachmentIndex) throws OXException {
        IndexFolderManager.deleteFolderEntry(info.contextId, info.userId, Types.INFOSTORE, info.account, String.valueOf(info.folder));
        AccountFolders accountFolders = new AccountFolders(info.account, Collections.singleton(String.valueOf(info.folder)));
        Builder queryBuilder = new Builder();
        QueryParameters infostoreAllQuery = queryBuilder.setHandler(SearchHandlers.ALL_REQUEST)
            .setAccountFolders(Collections.singleton(accountFolders))
            .build();
        infostoreIndex.deleteByQuery(infostoreAllQuery);

        QueryParameters attachmentAllQuery = new Builder()
            .setHandler(SearchHandlers.ALL_REQUEST)
            .setAccountFolders(Collections.singleton(accountFolders))
            .setModule(Types.INFOSTORE)
            .build();
        attachmentIndex.deleteByQuery(attachmentAllQuery);
    }

    private void indexFolder(InfostoreJobInfo info, Context context, User user, UserPermissionBits permissionBits, final IndexAccess<DocumentMetadata> infostoreIndex, final IndexAccess<Attachment> attachmentIndex) throws OXException {
        InfostoreFacade infostoreFacade = Services.getService(InfostoreFacade.class);
        TimedResult<DocumentMetadata> documents = infostoreFacade.getDocuments(info.folder, context, user, permissionBits);
        final List<IndexDocument<DocumentMetadata>> indexDocuments = new ArrayList<IndexDocument<DocumentMetadata>>();
        final List<IndexDocument<Attachment>> attachments = new ArrayList<IndexDocument<Attachment>>();
        SearchIterator<DocumentMetadata> it = documents.results();
        while (it.hasNext()) {
            DocumentMetadata file = it.next();
            StandardIndexDocument<DocumentMetadata> indexDocument = new StandardIndexDocument<DocumentMetadata>(file);
            indexDocuments.add(indexDocument);
            if (file.getFilestoreLocation() != null) {
                try {
                    InputStream document = infostoreFacade.getDocument(
                        file.getId(),
                        InfostoreFacade.CURRENT_VERSION,
                        context,
                        user,
                        permissionBits);

                    if (document != null) {
                        Attachment attachment = new Attachment();
                        attachment.setModule(Types.INFOSTORE);
                        attachment.setAccount(info.account);
                        attachment.setAttachmentId(String.valueOf(file.getVersion()));
                        attachment.setObjectId(String.valueOf(file.getId()));
                        attachment.setFolder(String.valueOf(file.getFolderId()));
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

        ChunkPerformer.perform(indexDocuments, 0, CHUNK_SIZE, new ListPerformable<IndexDocument<DocumentMetadata>>() {
            @Override
            public void perform(List<IndexDocument<DocumentMetadata>> subList) throws OXException {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Adding a chunk of files to the index.");
                }

                infostoreIndex.addDocuments(subList);
            }
        });

        ChunkPerformer.perform(attachments, 0, CHUNK_SIZE, new ListPerformable<IndexDocument<Attachment>>() {
            @Override
            public void perform(List<IndexDocument<Attachment>> subList) throws OXException {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Adding a chunk of attachments to the index.");
                }

                attachmentIndex.addDocuments(subList);
            }
        });

        IndexFolderManager.setIndexed(info.contextId, info.userId, Types.INFOSTORE, info.account, String.valueOf(info.folder));
    }

    private void closeIndexAccess(IndexAccess<?> indexAccess) throws OXException {
        if (indexAccess != null) {
            IndexFacadeService indexFacade = Services.getService(IndexFacadeService.class);
            indexFacade.releaseIndexAccess(indexAccess);
        }
    }

    private void checkJobInfo() {
        // Nothing to do

    }

}

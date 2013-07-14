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

package com.openexchange.mail.smal.impl.index.jobs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.attach.index.Attachment;
import com.openexchange.groupware.attach.index.ORTerm;
import com.openexchange.groupware.attach.index.ObjectIdTerm;
import com.openexchange.groupware.attach.index.SearchTerm;
import com.openexchange.groupware.tools.chunk.ChunkPerformer;
import com.openexchange.groupware.tools.chunk.Performable;
import com.openexchange.index.AccountFolders;
import com.openexchange.index.IndexAccess;
import com.openexchange.index.IndexDocument;
import com.openexchange.index.IndexFacadeService;
import com.openexchange.index.IndexResult;
import com.openexchange.index.QueryParameters;
import com.openexchange.index.SearchHandlers;
import com.openexchange.index.solr.IndexFolderManager;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.index.MailIndexField;
import com.openexchange.mail.index.MailUUID;
import com.openexchange.mail.smal.impl.SmalServiceLookup;
import com.openexchange.mail.smal.impl.index.FakeSession;
import com.openexchange.mail.smal.impl.index.IndexableFoldersCalculator;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.service.indexing.IndexingService;
import com.openexchange.service.indexing.JobInfo;


/**
 * {@link CheckForDeletedFoldersJob}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class CheckForDeletedFoldersJob extends AbstractMailJob {

    @Override
    public void execute(JobInfo jobInfo) throws OXException {
        try {
            if (!(jobInfo instanceof MailJobInfo)) {
                throw new IllegalArgumentException("Job info must be an instance of MailJobInfo.");
            }

            final MailJobInfo info = (MailJobInfo) jobInfo;
            long start = System.currentTimeMillis();
            if (LOG.isDebugEnabled()) {
                LOG.debug(this.getClass().getSimpleName() + " started performing. " + info.toString());
            }

            checkJobInfo();
            IndexFacadeService indexFacade = SmalServiceLookup.getServiceStatic(IndexFacadeService.class);
            final IndexAccess<MailMessage> mailIndex = indexFacade.acquireIndexAccess(Types.EMAIL, info.userId, info.contextId);
            final IndexAccess<Attachment> attachmentIndex = indexFacade.acquireIndexAccess(Types.ATTACHMENT, info.userId, info.contextId);
            FakeSession fakeSession = new FakeSession(info.primaryPassword, info.userId, info.contextId);
            MailAccountStorageService storageService = SmalServiceLookup.getServiceStatic(MailAccountStorageService.class);
            try {
                Set<MailFolder> allFolders = IndexableFoldersCalculator.calculatePrivateMailFolders(fakeSession, storageService, info.accountId);
                Set<String> fullNames = new HashSet<String>();
                for (MailFolder folder : allFolders) {
                    fullNames.add(folder.getFullname());
                }

                Set<MailUUID> uuidsInFolders = getMailUUIDs(mailIndex, info.contextId, info.userId, new AccountFolders(String.valueOf(info.accountId), fullNames));
                Set<MailUUID> allUUIDs = getMailUUIDs(mailIndex, info.contextId, info.userId, new AccountFolders(String.valueOf(info.accountId)));
                if (allUUIDs.removeAll(uuidsInFolders)) {
                    if (allUUIDs.isEmpty()) {
                        return;
                    }

                    final List<MailUUID> idsToDelete = new ArrayList<MailUUID>(allUUIDs);
                    ChunkPerformer.perform(new Performable() {
                        @Override
                        public int perform(int off, int len) throws OXException {
                            if (LOG.isTraceEnabled()) {
                                LOG.trace("Deleting a chunk of mails in folder " + info.folder + ": " + info.toString());
                            }

                            List<MailUUID> subList = idsToDelete.subList(off, len);
                            Set<String> uuidStrings = new HashSet<String>();
                            Map<String, List<String>> deletedFullNames = new HashMap<String, List<String>>();
                            for (MailUUID uuid : subList) {
                                uuidStrings.add(uuid.toString());

                                List<String> mails = deletedFullNames.get(uuid.getFullName());
                                if (mails == null) {
                                    mails = new ArrayList<String>();
                                    deletedFullNames.put(uuid.getFullName(), mails);
                                }

                                mails.add(uuid.getMailId());
                            }

                            QueryParameters deleteMailsQuery = new QueryParameters.Builder()
                                .setHandler(SearchHandlers.GET_REQUEST)
                                .setIndexIds(uuidStrings)
                                .build();
                            mailIndex.deleteByQuery(deleteMailsQuery);

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
                                QueryParameters deleteAttachmentsQuery = new QueryParameters.Builder()
                                    .setHandler(SearchHandlers.CUSTOM)
                                    .setSearchTerm(orTerm)
                                    .setAccountFolders(Collections.singleton(new AccountFolders(String.valueOf(info.accountId), Collections.singleton(folder))))
                                    .setModule(Types.EMAIL)
                                    .build();
                                attachmentIndex.deleteByQuery(deleteAttachmentsQuery);
                            }

                            return subList.size();
                        }

                        @Override
                        public int getChunkSize() {
                            return CHUNK_SIZE;
                        }

                        @Override
                        public int getLength() {
                            return idsToDelete.size();
                        }

                        @Override
                        public int getInitialOffset() {
                            return 0;
                        }
                    });
                }

                /*
                 * Delete folders from DB
                 */
                Map<String, Boolean> indexedFolders = IndexFolderManager.getIndexedFolders(info.contextId, info.userId, Types.EMAIL, String.valueOf(info.accountId));
                for (String fullName : fullNames) {
                    indexedFolders.remove(fullName);
                }

                for (String fullName : indexedFolders.keySet()) {
                    IndexFolderManager.deleteFolderEntry(info.contextId, info.userId, Types.EMAIL, String.valueOf(info.accountId), fullName);
                }
            } catch (OXException e) {
                /*
                 * If connect to mail access failed, reschedule this job
                 * FIXME: This is just a workaround! We need to fix the mail implementation for this.
                 * The priority for acquiring a mail connection must be lower than for interactive connections.
                 * Jobs should not fail because of missing connections and jobs must not block connections
                 * for interactive uses (user activities).
                 */
                if (e.getCategory().equals(Category.CATEGORY_TRY_AGAIN)
                    && e.getCode() == 2058) {
                    LOG.warn("Could not connect mail access for job " + info + ". Rescheduling job to run again in 60 seconds.");
                    IndexingService indexingService = SmalServiceLookup.getServiceStatic(IndexingService.class);
                    indexingService.scheduleJob(false, info, new Date(System.currentTimeMillis() + 60000), -1L, IndexingService.DEFAULT_PRIORITY);
                    return;
                }

                throw e;
            } finally {
                closeIndexAccess(mailIndex);
                closeIndexAccess(attachmentIndex);

                if (LOG.isDebugEnabled()) {
                    long diff = System.currentTimeMillis() - start;
                    LOG.debug(this.getClass().getSimpleName() + " lasted " + diff + "ms. " + info.toString());
                }
            }
        } catch (Exception e) {
            throw new OXException(e);
        }
    }

    private Set<MailUUID> getMailUUIDs(final IndexAccess<MailMessage> mailIndex, final int contextId, final int userId, final AccountFolders accountFolders) throws OXException {
        final QueryParameters.Builder countBuilder = new QueryParameters.Builder()
            .setAccountFolders(Collections.singleton(accountFolders))
            .setHandler(SearchHandlers.ALL_REQUEST)
            .setOffset(0)
            .setLength(0);

        final Set<MailIndexField> fields = EnumSet.noneOf(MailIndexField.class);
        Collections.addAll(fields, MailIndexField.ID, MailIndexField.ACCOUNT, MailIndexField.FULL_NAME);
        final long numFound = mailIndex.query(countBuilder.build(), fields).getNumFound();
        final Set<MailUUID> uuids = new HashSet<MailUUID>();
        ChunkPerformer.perform(new Performable() {
            @Override
            public int perform(int off, int len) throws OXException {
                QueryParameters params = countBuilder
                    .setOffset(off)
                    .setLength(len)
                    .build();

                IndexResult<MailMessage> mailsInFolders = mailIndex.query(params, fields);
                List<IndexDocument<MailMessage>> results = mailsInFolders.getResults();
                for (IndexDocument<MailMessage> document : results) {
                    MailMessage message = document.getObject();
                    MailUUID uuid = MailUUID.newUUID(contextId, userId, message);
                    uuids.add(uuid);
                }

                return results.size();
            }

            @Override
            public int getLength() {
                return (int) numFound;
            }

            @Override
            public int getInitialOffset() {
                return 0;
            }

            @Override
            public int getChunkSize() {
                return CHUNK_SIZE;
            }
        });

        return uuids;
    }

    private void checkJobInfo() {
        // Nothing to do

    }
}

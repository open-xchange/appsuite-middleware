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
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.attach.index.Attachment;
import com.openexchange.index.AccountFolders;
import com.openexchange.index.IndexAccess;
import com.openexchange.index.IndexDocument;
import com.openexchange.index.IndexFacadeService;
import com.openexchange.index.IndexResult;
import com.openexchange.index.QueryParameters;
import com.openexchange.index.QueryParameters.Builder;
import com.openexchange.index.QueryParameters.Order;
import com.openexchange.index.SearchHandlers;
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
import com.openexchange.mail.smal.impl.SmalMailAccess;
import com.openexchange.mail.smal.impl.SmalServiceLookup;
import com.openexchange.mail.smal.impl.index.FakeSession;
import com.openexchange.service.indexing.IndexingService;
import com.openexchange.service.indexing.JobInfo;


/**
 * {@link MailFolderJob}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class MailFolderJob extends AbstractMailJob {

    public MailFolderJob() {
        super();
    }

    @Override
    public void execute(JobInfo jobInfo) throws OXException {
        try {
            if (!(jobInfo instanceof MailJobInfo)) {
                throw new IllegalArgumentException("Job info must be an instance of MailJobInfo.");
            }

            MailJobInfo info = (MailJobInfo) jobInfo;
            long start = System.currentTimeMillis();
            if (LOG.isDebugEnabled()) {
                LOG.debug(this.getClass().getSimpleName() + " started performing. " + info.toString());
            }

            checkJobInfo();
            MailField[] fields = new MailField[] {
                MailField.ID,
                MailField.FLAGS,
                MailField.COLOR_LABEL };
            IndexFacadeService indexFacade = SmalServiceLookup.getServiceStatic(IndexFacadeService.class);
            IndexAccess<MailMessage> mailIndex = indexFacade.acquireIndexAccess(Types.EMAIL, info.userId, info.contextId);
            IndexAccess<Attachment> attachmentIndex = indexFacade.acquireIndexAccess(Types.ATTACHMENT, info.userId, info.contextId);
            FakeSession fakeSession = new FakeSession(info.primaryPassword, info.userId, info.contextId);
            MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = SmalMailAccess.getUnwrappedInstance(fakeSession, info.accountId);
            try {
                mailAccess.connect();
                AccountFolders accountFolders = new AccountFolders(String.valueOf(info.accountId), Collections.singleton(info.folder));
                Builder queryBuilder = new Builder();
                QueryParameters mailAllQuery = queryBuilder.setHandler(SearchHandlers.ALL_REQUEST)
                    .setAccountFolders(Collections.singleton(accountFolders))
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
                        fields);

                    Map<String, MailMessage> storageMails = new HashMap<String, MailMessage>();
                    for (MailMessage msg : storageResult) {
                        storageMails.put(msg.getMailId(), msg);
                    }

                    if (!info.force && IndexFolderManager.isIndexed(info.contextId, info.userId, Types.EMAIL, String.valueOf(info.accountId), info.folder)) {
                        long lastCompletion = IndexFolderManager.getTimestamp(info.contextId, info.userId, Types.EMAIL, String.valueOf(info.accountId), info.folder);
                        if (System.currentTimeMillis() - lastCompletion < 60000L * 15) {
                            LOG.debug("Skipping job because it already ran in the last 15 minutes.");
                            return;
                        }

                        IndexResult<MailMessage> indexResult = mailIndex.query(mailAllQuery, MailIndexField.getFor(fields));
                        Map<String, MailMessage> indexMails = new HashMap<String, MailMessage>();
                        for (IndexDocument<MailMessage> document : indexResult.getResults()) {
                            MailMessage msg = document.getObject();
                            indexMails.put(msg.getMailId(), msg);
                        }

                        if (LOG.isDebugEnabled()) {
                            long diff = System.currentTimeMillis() - start;
                            LOG.debug(info.toString() + " Preparation lasted " + diff + "ms.");
                        }
                        deleteMails(info, indexMails.keySet(), storageMails.keySet(), mailIndex, attachmentIndex);
                        addMails(info, indexMails.keySet(), storageMails.keySet(), mailIndex, attachmentIndex, messageStorage);
                        changeMails(info, indexMails, storageMails, mailIndex, attachmentIndex, messageStorage);
                        IndexFolderManager.setTimestamp(info.contextId, info.userId, Types.EMAIL, String.valueOf(info.accountId), info.folder, System.currentTimeMillis());
                    } else {
                        if (LOG.isDebugEnabled()) {
                            long diff = System.currentTimeMillis() - start;
                            LOG.debug(info.toString() + " Preparation lasted " + diff + "ms.");
                        }
                        addMails(info, Collections.<String> emptySet(), storageMails.keySet(), mailIndex, attachmentIndex, messageStorage);
                        IndexFolderManager.setIndexed(info.contextId, info.userId, Types.EMAIL, String.valueOf(info.accountId), info.folder);
                        IndexFolderManager.setTimestamp(info.contextId, info.userId, Types.EMAIL, String.valueOf(info.accountId), info.folder, System.currentTimeMillis());
                    }
                } else {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Deleting folder from index: " + info.toString());
                    }

                    IndexFolderManager.deleteFolderEntry(info.contextId, info.userId, Types.EMAIL, String.valueOf(info.accountId), info.folder);
                    mailIndex.deleteByQuery(mailAllQuery);
                    QueryParameters attachmentAllQuery = new Builder()
                        .setHandler(SearchHandlers.ALL_REQUEST)
                        .setAccountFolders(Collections.singleton(accountFolders))
                        .setModule(Types.EMAIL)
                        .build();
                    attachmentIndex.deleteByQuery(attachmentAllQuery);
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
                SmalMailAccess.closeUnwrappedInstance(mailAccess);
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

    private void addMails(MailJobInfo info, Set<String> indexIds, Set<String> storageIds, final IndexAccess<MailMessage> mailIndex, final IndexAccess<Attachment> attachmentIndex, final IMailMessageStorage messageStorage) throws OXException {
        final List<String> toAdd = new ArrayList<String>(storageIds);
        toAdd.removeAll(indexIds);
        addMails(info, toAdd, messageStorage, mailIndex, attachmentIndex);
    }

    private void deleteMails(MailJobInfo info, Set<String> indexIds, Set<String> storageIds, final IndexAccess<MailMessage> mailIndex, final IndexAccess<Attachment> attachmentIndex) throws OXException {
        final List<String> toDelete = new ArrayList<String>(indexIds);
        toDelete.removeAll(storageIds);
        deleteMails(info, toDelete, mailIndex, attachmentIndex);
    }

    private void changeMails(MailJobInfo info, Map<String, MailMessage> indexMails, Map<String, MailMessage> storageMails, final IndexAccess<MailMessage> mailIndex, final IndexAccess<Attachment> attachmentIndex, final IMailMessageStorage messageStorage) throws OXException {
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

        changeMails(info, changedMails, messageStorage, mailIndex, attachmentIndex);
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

    private void checkJobInfo() {
        // Nothing to do

    }

}

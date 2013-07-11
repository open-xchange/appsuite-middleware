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
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.index.IndexExceptionCodes;
import com.openexchange.index.IndexProperties;
import com.openexchange.index.solr.ModuleSet;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.smal.impl.SmalServiceLookup;
import com.openexchange.mail.smal.impl.index.jobs.CheckForDeletedFoldersJob;
import com.openexchange.mail.smal.impl.index.jobs.MailFolderJob;
import com.openexchange.mail.smal.impl.index.jobs.MailJobInfo;
import com.openexchange.mail.utils.MailPasswordUtil;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.service.indexing.IndexingService;
import com.openexchange.service.indexing.JobInfo;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondEventConstants;

/**
 * {@link SmalSessionEventHandler}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class SmalSessionEventHandler implements EventHandler {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(SmalSessionEventHandler.class);

    private static final int MAX_OFFSET = 60000 * 5;

    private static final long JOB_TIMEOUT = 7 * 24 * 60 * 60000;

    private static final int PROGRESSION_RATE = 10;

    private static final long START_INTERVAL = 60000 * 60;

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
            boolean isReactivation = SessiondEventConstants.TOPIC_REACTIVATE_SESSION.equals(topic);
            if (SessiondEventConstants.TOPIC_ADD_SESSION.equals(topic) || isReactivation) {
                Session session = (Session) event.getProperty(SessiondEventConstants.PROP_SESSION);
                if (session.isTransient()) {
                    return;
                }

                int contextId = session.getContextId();
                int userId = session.getUserId();
                if (!isIndexingPermitted(contextId, userId)) {
                    if (LOG.isDebugEnabled()) {
                        OXException e = IndexExceptionCodes.INDEXING_NOT_ENABLED.create(Types.EMAIL, userId, contextId);
                        LOG.debug("Skipping event handling execution because: " + e.getMessage());
                    }
                    return;
                }

                MailAccountStorageService storageService = SmalServiceLookup.getServiceStatic(MailAccountStorageService.class);
                if (storageService == null) {
                    OXException e = ServiceExceptionCode.SERVICE_UNAVAILABLE.create(MailAccountStorageService.class.getName());
                    LOG.warn("Could not handle session event.", e);
                    return;
                }

                Map<Integer, Set<MailFolder>> allFolders = IndexableFoldersCalculator.calculatePrivateMailFolders(
                    session,
                    storageService);
                scheduleFolderJobs(session, allFolders, storageService, indexingService, isReactivation);
            }
        } catch (Exception e) {
            LOG.warn("Error while triggering mail indexing jobs.", e);
        }
    }

    private boolean isIndexingPermitted(int contextId, int userId) throws OXException {
        ConfigViewFactory config = SmalServiceLookup.getServiceStatic(ConfigViewFactory.class);
        ConfigView view = config.getView(userId, contextId);
        String moduleStr = view.get(IndexProperties.ALLOWED_MODULES, String.class);
        ModuleSet modules = new ModuleSet(moduleStr);
        return modules.containsModule(Types.EMAIL);
    }

    private void scheduleFolderJobs(Session session, Map<Integer, Set<MailFolder>> allFolders, MailAccountStorageService storageService, IndexingService indexingService, boolean updateOnly) throws OXException {
        int contextId = session.getContextId();
        int userId = session.getUserId();
        Random random = new Random();
        ConfigurationService configurationService = SmalServiceLookup.getServiceStatic(ConfigurationService.class);
        boolean useOffset = configurationService.getBoolProperty("com.openexchange.mail.smal.useOffset", true);
        for (Integer accountId : allFolders.keySet()) {
            MailAccount account = storageService.getMailAccount(accountId.intValue(), userId, contextId);
            Set<MailFolder> folders = allFolders.get(accountId);
            String decryptedPW = account.getPassword() == null ? session.getPassword() : MailPasswordUtil.decrypt(
                account.getPassword(),
                session,
                accountId.intValue(),
                account.getLogin(),
                account.getMailServer());

            for (MailFolder folder : folders) {
                int offset = 0;
                if (useOffset) {
                    offset = random.nextInt(MAX_OFFSET);
                }

                int priority;
                if (account.isDefaultAccount() && folder.isInbox()) {
                    priority = 15;
                    offset = 0;
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

                Date startDate = new Date(System.currentTimeMillis() + offset);
                indexingService.scheduleJobWithProgressiveInterval(jobInfo, startDate, JOB_TIMEOUT, START_INTERVAL, PROGRESSION_RATE, priority, updateOnly);
            }

            int offset = 0;
            if (useOffset) {
                offset = random.nextInt(MAX_OFFSET);
            }
            Date startDate = new Date(System.currentTimeMillis() + offset);
            JobInfo checkDeletedJobInfo = MailJobInfo.newBuilder(CheckForDeletedFoldersJob.class)
                .accountId(account.getId())
                .contextId(contextId)
                .userId(userId)
                .primaryPassword(session.getPassword())
                .password(decryptedPW)
                .build();
            indexingService.scheduleJobWithProgressiveInterval(checkDeletedJobInfo, startDate, JOB_TIMEOUT, START_INTERVAL, PROGRESSION_RATE, IndexingService.DEFAULT_PRIORITY, updateOnly);
        }
    }
}

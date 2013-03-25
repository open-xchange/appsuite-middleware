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

package com.openexchange.service.indexing.impl.internal.nonclustered;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import org.apache.commons.logging.Log;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import com.hazelcast.core.DistributedTask;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.index.IndexAccess;
import com.openexchange.index.IndexExceptionCodes;
import com.openexchange.index.IndexFacadeService;
import com.openexchange.index.IndexManagementService;
import com.openexchange.index.IndexProperties;
import com.openexchange.index.QueryParameters;
import com.openexchange.index.SearchHandlers;
import com.openexchange.index.solr.ModuleSet;
import com.openexchange.service.indexing.IndexingJob;
import com.openexchange.service.indexing.IndexingService;
import com.openexchange.service.indexing.JobInfo;
import com.openexchange.service.indexing.impl.internal.JobConstants;
import com.openexchange.service.indexing.impl.internal.Services;
import com.openexchange.service.indexing.impl.internal.Tools;
import com.openexchange.solr.SolrCoreIdentifier;

/**
 * {@link RunOrRescheduleAtTargetJob}
 * 
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
@DisallowConcurrentExecution
public class RunOrRescheduleAtTargetJob implements Job {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(RunOrRescheduleAtTargetJob.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap mergedData = context.getMergedJobDataMap();
        Object infoObject = mergedData.get(JobConstants.JOB_INFO);
        if (infoObject == null || !(infoObject instanceof JobInfo)) {
            String msg = "JobDataMap did not contain valid JobInfo instance.";
            LOG.error(msg);
            throw new JobExecutionException(msg, false);
        }

        JobInfo jobInfo = (JobInfo) infoObject;
        if (LOG.isDebugEnabled()) {
            LOG.debug("Started execution of job " + jobInfo.toString() + ". Trigger: " + context.getTrigger().getKey());
        }

        try {
            SolrCoreIdentifier identifier = new SolrCoreIdentifier(jobInfo.contextId, jobInfo.userId, jobInfo.getModule());
            HazelcastInstance hazelcast = Services.getService(HazelcastInstance.class);
            // FIXME: This core handling stuff has to be centralized and hidden by a transparent layer.
            IMap<String, String> solrCores = hazelcast.getMap("solrCoreMap");
            String owner = solrCores.get(identifier.toString());
            if (owner == null) {
                startUpIndex(jobInfo);
                owner = solrCores.get(identifier.toString());
            }
            if (owner == null) {
                LOG.error("Did not find a node holding this index.");
                // TODO: exception?
                return;
            }
            Member executor = null;
            for (Member member : hazelcast.getCluster().getMembers()) {
                if (owner.equals(Tools.resolveSocketAddress(member.getInetSocketAddress()))) {
                    executor = member;
                    break;
                }
            }

            if (executor == null) {
                LOG.error("Could not find a member to execute this job.");
            } else {
                if (executor.equals(hazelcast.getCluster().getLocalMember())) {
                    ConfigViewFactory config = Services.getService(ConfigViewFactory.class);
                    ConfigView view = config.getView(jobInfo.userId, jobInfo.contextId);
                    String moduleStr = view.get(IndexProperties.ALLOWED_MODULES, String.class);
                    ModuleSet modules = new ModuleSet(moduleStr);
                    if (!modules.containsModule(jobInfo.getModule())) {
                        if (LOG.isDebugEnabled()) {
                            OXException e = IndexExceptionCodes.INDEXING_NOT_ENABLED.create(jobInfo.getModule(), jobInfo.userId, jobInfo.contextId);
                            LOG.debug("Skipping job execution because: " + e.getMessage());
                        }

                        return;
                    }

                    IndexManagementService managementService = Services.getService(IndexManagementService.class);
                    if (managementService.isLocked(jobInfo.contextId, jobInfo.userId, jobInfo.getModule())) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Skipping job execution because corresponding index is locked. " + jobInfo.toString());
                        }

                        return;
                    }
                    
                    executeJob(jobInfo);
                } else {
                    long interval = 0L;
                    Object intervalObj = mergedData.get(JobConstants.INTERVAL);
                    if (intervalObj instanceof Long) {
                        interval = (Long) intervalObj;
                    }

                    int priority = IndexingService.DEFAULT_PRIORITY;
                    Object prioObj = mergedData.get(JobConstants.PRIORITY);
                    if (prioObj instanceof Integer) {
                        priority = (Integer) prioObj;
                    }

                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Rescheduling job " + jobInfo.toString() + " at member " + owner + ".");
                    }
                    FutureTask<Object> task = new DistributedTask<Object>(
                        new ScheduleJobCallable(jobInfo, new Date(), interval, priority),
                        executor);
                    ExecutorService executorService = hazelcast.getExecutorService();
                    executorService.submit(task);

                    context.getScheduler().unscheduleJob(context.getTrigger().getKey());
                }
            }
        } catch (Throwable t) {
            LOG.error(t.getMessage(), t);
        }
    }

    private void executeJob(JobInfo jobInfo) {
        Class<? extends IndexingJob> jobClass = jobInfo.jobClass;
        if (jobClass == null) {
            String msg = "JobInfo did not contain valid job class. " + jobInfo.toString();
            LOG.error(msg);
            return;
        }

        IndexingJob indexingJob;
        try {
            indexingJob = jobClass.newInstance();
        } catch (Throwable t) {
            String msg = "Could not instantiate IndexingJob from class object. " + jobInfo.toString();
            LOG.error(msg, t);
            return;
        }

        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Executing job " + jobInfo.toString());
            }
            indexingJob.execute(jobInfo);
        } catch (Throwable t) {
            String msg = "Error during IndexingJob execution. " + jobInfo.toString();
            LOG.error(msg, t);
            return;
        }
    }

    private void startUpIndex(JobInfo jobInfo) throws OXException {
        IndexFacadeService indexFacade = Services.getService(IndexFacadeService.class);
        IndexAccess<?> indexAccess = indexFacade.acquireIndexAccess(jobInfo.getModule(), jobInfo.userId, jobInfo.contextId);
        QueryParameters queryParameters = new QueryParameters.Builder().setLength(0).setHandler(SearchHandlers.SIMPLE).setSearchTerm(
            "something").build();
        indexAccess.query(queryParameters, null);
        indexFacade.releaseIndexAccess(indexAccess);
    }

}

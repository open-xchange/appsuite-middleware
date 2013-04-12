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
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
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
import com.openexchange.service.indexing.JobInfo;
import com.openexchange.service.indexing.impl.internal.Services;
import com.openexchange.service.indexing.impl.internal.Tools;
import com.openexchange.solr.SolrCoreIdentifier;


/**
 * <p>
 * {@link ProgressiveRecurringJob} implements a {@link Job} that re-schedules itself with progressive intervals.
 * For correct behavior only one trigger must be responsible for this job. Additionally the trigger must
 * not have any recurrence configured.
 * </p>
 * <p>
 * The next fire time is calculated based on the last interval and a progression rate. The used progression function is:<br>
 * <br>
 * <code>newInterval = oldInterval + (oldInterval * progressionRate / 100)</code><br>
 * <code>nextFireTime = now + newInterval</code><br>
 * </p>
 * <p>
 * Additionally a timeout is configured to terminate this job based on the time of the last update of the given job info.
 * If the actual fire time exceeds <code>lastUpdate + timeout</code> the job exists.
 * </p>
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
@DisallowConcurrentExecution
public class ProgressiveRecurringJob implements Job {
    
    private static final Log LOG = com.openexchange.log.Log.loggerFor(ProgressiveRecurringJob.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDetail jobDetail = context.getJobDetail();
        String jobId = jobDetail.getKey().toString();
        JobInfoWrapper infoWrapper = RecurringJobsManager.getJob(jobId);
        if (infoWrapper == null) {
            /*
             * Remove this trigger?
             */
            LOG.info("JobInfoWrapper was null. Exiting...");
            return;
        } else {
            long jobTimeout = infoWrapper.getJobTimeout();
            long lastUpdate = infoWrapper.getLastUpdate();
            if (System.currentTimeMillis() > (lastUpdate + jobTimeout)) {
                LOG.info("Recurring job will be removed because of timeout: " + infoWrapper.getJobInfo().toString());
                return;
            }
            
            long interval = infoWrapper.getInterval();
            if (interval <= 0) {
                LOG.error("Interval was < 0. Exiting...");
            }
            
            /*
             * TODO: Maybe implement some intelligent error handling.
             * Could be something like "if this job fails for the third time in succession
             * and was not updated since X, then remove it...".
             */
            try {
                if (perform(context, infoWrapper) && isProgressive(infoWrapper)) {
                    long newInterval = infoWrapper.increaseInterval();
                    infoWrapper.updateLastRun();
                    long nextStartTime = infoWrapper.getLastRun() + newInterval;
                    Trigger trigger = context.getTrigger();
                    TriggerBuilder<? extends Trigger> triggerBuilder = trigger.getTriggerBuilder();
                    triggerBuilder.startAt(new Date(nextStartTime));
                    try {
                        Scheduler scheduler = context.getScheduler();
                        scheduler.rescheduleJob(trigger.getKey(), triggerBuilder.build());
                        RecurringJobsManager.addOrUpdateJob(jobId, infoWrapper);
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Job was re-scheduled: " + infoWrapper.getJobInfo().toString() + ", nextStartTime: " +
                                nextStartTime + ", newInterval: " + newInterval);
                        }
                    } catch (SchedulerException e) {
                        LOG.error("Could not re-schedule job: " + infoWrapper.getJobInfo().toString(), e);
                    }
                }
            } catch (OXException e) {
                LOG.error("Error during job execution.", e);
            }
        }
    }
    
    protected boolean perform(JobExecutionContext context, JobInfoWrapper infoWrapper) throws OXException {
        HazelcastInstance hazelcast = Services.getService(HazelcastInstance.class);
        JobInfo jobInfo = infoWrapper.getJobInfo();
        Trigger trigger = context.getTrigger();
        Member executor = chooseExecutor(hazelcast, jobInfo);
        if (executor == null) {
            LOG.error("Could not find a member for execution.");
            return false;
        }
        
        if (executor.equals(hazelcast.getCluster().getLocalMember())) {
            if (!isExecutionAllowed(jobInfo)) {
                LOG.info("Execution of job " + jobInfo.toString() + " was not allowed. Skipping...");
                return false;
            }
            
            executeJob(jobInfo);
            return true;
        }
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Rescheduling job " + jobInfo.toString() + " at member " + executor.toString() + ".");
        }
        
        FutureTask<Object> task = new DistributedTask<Object>(new ScheduleProgressiveRecurringJobCallable(jobInfo, trigger.getPriority()), executor);
        ExecutorService executorService = hazelcast.getExecutorService();
        executorService.submit(task);
        try {
            context.getScheduler().unscheduleJob(trigger.getKey());
        } catch (SchedulerException e) {
            LOG.error("Could not remove trigger from local scheduler.", e);
        }
        return false;
    }
    
    private boolean isExecutionAllowed(JobInfo jobInfo) throws OXException {
        ConfigViewFactory config = Services.getService(ConfigViewFactory.class);
        ConfigView view = config.getView(jobInfo.userId, jobInfo.contextId);
        String moduleStr = view.get(IndexProperties.ALLOWED_MODULES, String.class);
        ModuleSet modules = new ModuleSet(moduleStr);
        if (!modules.containsModule(jobInfo.getModule())) {
            if (LOG.isDebugEnabled()) {
                OXException e = IndexExceptionCodes.INDEXING_NOT_ENABLED.create(jobInfo.getModule(), jobInfo.userId, jobInfo.contextId);
                LOG.debug("Skipping job execution because: " + e.getMessage());
            }

            return false;
        }

        IndexManagementService managementService = Services.getService(IndexManagementService.class);
        if (managementService.isLocked(jobInfo.contextId, jobInfo.userId, jobInfo.getModule())) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Skipping job execution because corresponding index is locked. " + jobInfo.toString());
            }

            return false;
        }
        
        return true;
    }
    
    private Member chooseExecutor(HazelcastInstance hazelcast, JobInfo jobInfo) throws OXException {
        SolrCoreIdentifier identifier = new SolrCoreIdentifier(jobInfo.contextId, jobInfo.userId, jobInfo.getModule());
        
        // FIXME: This core handling stuff has to be centralized and hidden by a transparent layer.
        IMap<String, String> solrCores = hazelcast.getMap("solrCoreMap");
        String owner = solrCores.get(identifier.toString());
        if (owner == null) {
            startUpIndex(jobInfo);
            owner = solrCores.get(identifier.toString());
        }
        
        if (owner == null) {
            LOG.error("Did not find a node holding this index.");
            return null;
        }
        Member executor = null;
        for (Member member : hazelcast.getCluster().getMembers()) {
            if (owner.equals(Tools.resolveSocketAddress(member.getInetSocketAddress()))) {
                executor = member;
                break;
            }
        }
        
        return executor;
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
    
    private boolean isProgressive(JobInfoWrapper infoWrapper) {
        return infoWrapper.getInterval() > 0 && infoWrapper.getProgressionRate() > 0;
    }

}

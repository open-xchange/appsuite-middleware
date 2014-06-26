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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.TriggerBuilder;
import org.quartz.service.QuartzService;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.ExceptionUtils;
import com.openexchange.service.indexing.IndexingService;
import com.openexchange.service.indexing.JobInfo;
import com.openexchange.service.indexing.impl.internal.JobConstants;
import com.openexchange.service.indexing.impl.internal.SchedulerConfig;
import com.openexchange.service.indexing.impl.internal.Services;
import com.openexchange.service.indexing.impl.internal.Tools;
import com.openexchange.threadpool.ThreadPoolService;

/**
 * {@link IndexingServiceImpl}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class IndexingServiceImpl implements IndexingService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IndexingServiceImpl.class);

    @Override
    public void scheduleJobWithProgressiveInterval(JobInfo info, Date startDate, long timeout, long initialInterval, int progressionRate, int priority, boolean onlyResetProgression) throws OXException {
        if (startDate == null) {
            startDate = new Date();
        }

        LOG.trace("Scheduling job {} at {}.\n    Initial interval: {}\n    Progression rate: {}\n    Timeout: {}\n    Priority: {}", info, startDate, initialInterval, progressionRate, timeout, priority);

        if (timeout <= 0) {
            throw new IllegalArgumentException("Parameter 'timeout' must be > 0.");
        }

        if (initialInterval <= 0) {
            throw new IllegalArgumentException("Parameter 'initialInterval' must be > 0.");
        }

        if (progressionRate <= 0) {
            throw new IllegalArgumentException("Parameter 'progressionRate' must be > 0.");
        }

        JobInfoWrapper infoWrapper = new JobInfoWrapper(info, timeout, initialInterval, progressionRate);
        JobKey jobKey = Tools.generateJobKey(info, null);
        JobInfoWrapper old = RecurringJobsManager.addOrUpdateJob(jobKey.toString(), infoWrapper);

        boolean scheduleJob = false;
        if (old == null) {
            scheduleJob = true;
        } else {
            if (onlyResetProgression) {
                /*
                 * If a node dies, all locally scheduled triggers are lost.
                 * Despite the jobInfo stays in the distributed collection.
                 * In those cases the next scheduled run of a job doesn't get fired.
                 * We can detect such a "misfire" and restore the according trigger.
                 */
                scheduleJob = wasMisfired(old);
            } else {
                scheduleJob = true;
            }
        }

        if (scheduleJob) {
            scheduleProgressiveJob(jobKey, info, startDate, priority);
        }
    }

    private boolean wasMisfired(JobInfoWrapper old) {
        long lastRun = old.getLastRun();
        long interval = old.getInterval();
        long now = System.currentTimeMillis();
        long diff = now - (lastRun + interval);
        if (diff > 0) {
            LOG.debug("Job {} was misfired for {}ms. Re-adding trigger and job on session reactivation.", old.getJobInfo(), diff);
            return true;
        }

        return false;
    }

    private void scheduleProgressiveJob(JobKey jobKey, JobInfo info, Date startDate, int priority) throws OXException {
        QuartzService quartzService = Services.getService(QuartzService.class);
        Scheduler scheduler = quartzService.getScheduler(
            SchedulerConfig.getSchedulerName(),
            SchedulerConfig.start(),
            SchedulerConfig.getThreadCount());

        JobDetail jobDetail = JobBuilder.newJob(ProgressiveRecurringJob.class)
            .withIdentity(jobKey)
            .build();

        String triggerName = info.toUniqueId() + "/withProgressiveInterval";
        SimpleTrigger trigger = TriggerBuilder.newTrigger()
            .forJob(jobDetail.getKey())
            .withIdentity(triggerName, Tools.generateTriggerGroup(info.contextId, info.userId))
            .startAt(startDate)
            .withPriority(priority)
            .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
            .build();

        try {
            scheduler.addJob(jobDetail, true);
            if (scheduler.checkExists(trigger.getKey())) {
                scheduler.rescheduleJob(trigger.getKey(), trigger);
            } else {
                scheduler.scheduleJob(trigger);
            }
        } catch (SchedulerException e) {
            throw new OXException(e);
        }
    }

    @Override
    public void scheduleJob(boolean async, final JobInfo info, final Date startDate, final long repeatInterval, final int priority) throws OXException {
        LOG.trace("Scheduling job {} at {} with interval {} and priority {}.", info, startDate == null ? "now" : startDate.toString(), repeatInterval, priority);

        Runnable task = new ScheduleJobRunnable(info, startDate, repeatInterval, priority);
        try {
            if (async) {
                ThreadPoolService threadPoolService = getThreadPoolService();
                threadPoolService.getExecutor().submit(task);
            } else {
                task.run();
            }
        } catch (Throwable t) {
            ExceptionUtils.handleThrowable(t);
            LOG.error("", t);
        }
    }

    @Override
    public void unscheduleAllForUser(boolean async, int contextId, int userId) throws OXException {
        LOG.trace("Unscheduling all jobs for user {} in context {}.", userId, contextId);

        HazelcastInstance hazelcast = Services.getService(HazelcastInstance.class);
        IExecutorService executorService = hazelcast.getExecutorService(JobConstants.HZ_EXECUTOR);
        executorService.executeOnAllMembers(new UnscheduleAllJobsRunnable(contextId, userId));
    }

    @Override
    public void unscheduleAllForContext(boolean async, int contextId) throws OXException {
        LOG.trace("Unscheduling all jobs for context {}.", contextId);

        HazelcastInstance hazelcast = Services.getService(HazelcastInstance.class);
        IExecutorService executorService = hazelcast.getExecutorService(JobConstants.HZ_EXECUTOR);
        executorService.executeOnAllMembers(new UnscheduleAllJobsRunnable(contextId, -1));
    }

    ThreadPoolService getThreadPoolService() throws OXException {
        ThreadPoolService threadPoolService = Services.getService(ThreadPoolService.class);
        return threadPoolService;
    }
}

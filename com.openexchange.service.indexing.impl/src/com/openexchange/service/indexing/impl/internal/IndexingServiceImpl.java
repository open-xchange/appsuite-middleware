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

package com.openexchange.service.indexing.impl.internal;

import java.util.ArrayList;
import java.util.Date;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.ObjectAlreadyExistsException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.service.QuartzService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.service.indexing.IndexingService;
import com.openexchange.service.indexing.JobInfo;
import com.openexchange.solr.SolrProperties;
import com.openexchange.threadpool.Task;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadRenamer;


/**
 * {@link IndexingServiceImpl}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class IndexingServiceImpl implements IndexingService {

    private static final String SCHEDULER_NAME = "com.openexchange.service.indexing";

    private static final Log LOG = com.openexchange.log.Log.loggerFor(IndexingServiceImpl.class);

    private final Scheduler scheduler;


    public IndexingServiceImpl() throws OXException {
        super();
        ConfigurationService config = Services.getService(ConfigurationService.class);
        boolean isSolrNode = config.getBoolProperty(SolrProperties.IS_NODE, false);
        int threads = config.getIntProperty(IndexingProperties.WORKER_THREADS, 5);
        QuartzService quartzService = Services.getService(QuartzService.class);
        scheduler = null;
    }

    @Override
    public void scheduleJob(final boolean async, final JobInfo info, final Date startDates, final long repeatInterval, final int priority) throws OXException {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Date tmpDate = startDates;
                    if (tmpDate == null) {
                        tmpDate = new Date();
                    }
                    JobDataMap jobData = new JobDataMap();
                    jobData.put(JobConstants.JOB_INFO, info);
                    JobDetail jobDetail = JobBuilder.newJob(QuartzIndexingJob.class).withIdentity(generateJobKey(info)).usingJobData(
                        jobData).build();
                    TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger()
                        .forJob(jobDetail)
                        .withIdentity(generateTriggerKey(info, tmpDate, repeatInterval))
                        .startAt(tmpDate)
                        .withPriority(priority);
                    if (repeatInterval > 0L) {
                        triggerBuilder.withSchedule(
                            SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInMilliseconds(repeatInterval)
                            .repeatForever()
                            .withMisfireHandlingInstructionIgnoreMisfires());
                    } else {
                        triggerBuilder.withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionIgnoreMisfires());
                    }
                    Trigger trigger = triggerBuilder.build();
                    Scheduler scheduler = getScheduler();
                    try {
                        if (LOG.isDebugEnabled()) {
                            Exception exception = new Exception();
                            LOG.debug("Scheduling job: " + info.toString() + ".", exception);
                        }

                        scheduler.scheduleJob(jobDetail, trigger);
                    } catch (SchedulerException e) {
                        if (e instanceof ObjectAlreadyExistsException) {
                            LOG.debug("Job already exists within JobStore: " + info.toString() + ". Trying to store trigger only.");
                            try {
                                scheduler.scheduleJob(trigger);
                            } catch (SchedulerException f) {
                                LOG.debug("Could not schedule trigger.", f);
                            }
                        } else {
                            throw new OXException(e);
                        }
                    }
                } catch (Throwable t) {
                    LOG.warn(t.getMessage(), t);
                }
            }
        };

        if (async) {
            ThreadPoolService threadPoolService = getThreadPoolService();
            threadPoolService.submit(new TaskAdapter(runnable));
        } else {
            runnable.run();
        }
    }

    @Override
    public void unscheduleJob(final boolean async, final JobInfo info) throws OXException {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Scheduler scheduler = getScheduler();
                    try {
                        scheduler.deleteJob(generateJobKey(info));
                    } catch (SchedulerException e) {
                        throw new OXException(e);
                    }
                } catch (Throwable t) {
                    LOG.warn(t.getMessage(), t);
                }
            }
        };

        if (async) {
            ThreadPoolService threadPoolService = getThreadPoolService();
            threadPoolService.submit(new TaskAdapter(runnable));
        } else {
            runnable.run();
        }
    }

    @Override
    public void unscheduleAllForUser(final boolean async, final int contextId, final int userId) throws OXException {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Scheduler scheduler = getScheduler();
                    String jobGroup = generateJobGroup(contextId, userId);
                    try {
                        Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(jobGroup));
                        scheduler.deleteJobs(new ArrayList<JobKey>(jobKeys));
                    } catch (SchedulerException e) {
                        throw new OXException(e);
                    }
                } catch (Throwable t) {
                    LOG.warn(t.getMessage(), t);
                }
            }
        };

        if (async) {
            ThreadPoolService threadPoolService = getThreadPoolService();
            threadPoolService.submit(new TaskAdapter(runnable));
        } else {
            runnable.run();
        }
    }

    @Override
    public void unscheduleAllForContext(final boolean async, final int contextId) throws OXException {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Scheduler scheduler = getScheduler();
                    String jobGroup = generateJobGroup(contextId);
                    try {
                        Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.jobGroupStartsWith(jobGroup));
                        scheduler.deleteJobs(new ArrayList<JobKey>(jobKeys));
                    } catch (SchedulerException e) {
                        throw new OXException(e);
                    }
                } catch (Throwable t) {
                    LOG.warn(t.getMessage(), t);
                }
            }
        };

        if (async) {
            ThreadPoolService threadPoolService = getThreadPoolService();
            threadPoolService.submit(new TaskAdapter(runnable));
        } else {
            runnable.run();
        }
    }

    JobKey generateJobKey(JobInfo info) {
        JobKey key = new JobKey(info.toUniqueId(), generateJobGroup(info.contextId, info.userId));
        return key;
    }

    TriggerKey generateTriggerKey(JobInfo info, Date startDate, long repeatInterval) {
        TriggerKey key = new TriggerKey(generateTriggerName(info, startDate, repeatInterval), generateTriggerGroup(info.contextId, info.userId));
        return key;
    }

    String generateJobGroup(int contextId) {
        return "indexingJobs/" + contextId;
    }

    String generateJobGroup(int contextId, int userId) {
        return "indexingJobs/" + contextId + '/' + userId;
    }

    String generateTriggerGroup(int contextId, int userId) {
        return "indexingTriggers/" + contextId + '/' + userId;
    }

    String generateTriggerName(JobInfo info, Date startDate, long repeatInterval) {
        StringBuilder sb = new StringBuilder(info.toUniqueId());
        sb.append('/');
        if (repeatInterval > 0L) {
            sb.append("withInterval/");
            sb.append(repeatInterval);
        } else {
            /*
             * Two one shot triggers within the same quarter of an hour have the same trigger key.
             * This avoids triggering jobs too often.
             */
            sb.append("oneShot/");
            long now = startDate.getTime();
            long millisSinceLastFullHour = now % (60000L * 60);
            long lastFullHourInMillis = now - millisSinceLastFullHour;
            long quarters = millisSinceLastFullHour / 60000L / 15;
            long time = lastFullHourInMillis + (quarters * 15 * 60000L);
            sb.append(time);
        }

        return sb.toString();
    }

    Scheduler getScheduler() throws OXException {
        return scheduler;
    }

    ThreadPoolService getThreadPoolService() throws OXException {
        ThreadPoolService threadPoolService = Services.getService(ThreadPoolService.class);
        return threadPoolService;
    }

    public void shutdown() {
        QuartzService quartzService = Services.getService(QuartzService.class);
//        quartzService.releaseClusteredScheduler(SCHEDULER_NAME);
    }

    private static final class TaskAdapter implements Task<Void> {

        private final Runnable runnable;

        public TaskAdapter(Runnable runnable) {
            super();
            this.runnable = runnable;
        }

        @Override
        public Void call() throws Exception {
            runnable.run();
            return null;
        }

        @Override
        public void setThreadName(ThreadRenamer threadRenamer) {
            return;
        }

        @Override
        public void beforeExecute(Thread t) {
            return;
        }

        @Override
        public void afterExecute(Throwable t) {
           return;
        }

    }

    @Override
    public void scheduleJobWithProgressiveInterval(JobInfo info, Date startDate, long timeout, long initialInterval, int progressionRate, int priority, boolean b) throws OXException {
        // TODO Auto-generated method stub
        
    }
}

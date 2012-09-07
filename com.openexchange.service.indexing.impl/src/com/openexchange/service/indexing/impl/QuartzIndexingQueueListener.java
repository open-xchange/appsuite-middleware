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

package com.openexchange.service.indexing.impl;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import org.apache.commons.logging.Log;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.service.QuartzService;
import com.openexchange.java.Java7ConcurrentLinkedQueue;
import com.openexchange.log.LogFactory;
import com.openexchange.mq.queue.MQQueueListener;
import com.openexchange.service.indexing.IndexingJob;
import com.openexchange.service.indexing.mail.Constants;

/**
 * {@link QuartzIndexingQueueListener} - The {@link MQQueueListener listener} that delegates incoming messages to {@link QuartzService
 * Quartz service}.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class QuartzIndexingQueueListener implements MQQueueListener {

    private static final String GROUP = Constants.MAIL_JOB_SCHEDULER_GROUP_ID;

    /**
     * The logger
     */
    static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(QuartzIndexingQueueListener.class));

    /**
     * Simple container class.
     */
    static final class JobResc {

        final JobDetail job;

        final Trigger trigger;

        protected JobResc(final JobDetail job, final Trigger trigger) {
            super();
            this.job = job;
            this.trigger = trigger;
        }
    }

    /**
     * Tracks number of concurrently executed <tt>QuartzIndexingJob</tt>s.
     */
    static final AtomicLong EXECUTION_TRACKER = new AtomicLong();

    /**
     * The threshold.
     */
    static final AtomicInteger THRESHOLD = new AtomicInteger(-1);

    /**
     * The queue used for re-scheduling jobs.
     */
    static final Queue<JobResc> RESCHEDULE_JOBS = new Java7ConcurrentLinkedQueue<JobResc>();

    private final AtomicLong counter;

    private final QuartzService quartzService;

    /**
     * Initializes a new {@link QuartzIndexingQueueListener}.
     * 
     * @throws SchedulerException If initialization fails
     */
    public QuartzIndexingQueueListener(final int maxConcurrentJobs, final QuartzService executor) throws SchedulerException {
        super();
        counter = new AtomicLong();
        quartzService = executor;
        THRESHOLD.set(maxConcurrentJobs);
        // The special job used for re-scheduling Quartz indexing jobs
        final JobDetail job = newJob(ReschedulerJob.class).withIdentity("rescheduler", GROUP).withDescription("The rescheduling job.").build();
        // Periodic execution; forever for every minute
        final TriggerBuilder<Trigger> triggerBuilder = newTrigger().withIdentity("rescheduler", GROUP).withDescription("The rescheduling trigger.").forJob(job.getKey());
        triggerBuilder.startNow().withSchedule(simpleSchedule().repeatForever().withIntervalInSeconds(15));
        // Schedule
        quartzService.getLocalScheduler().scheduleJob(job, triggerBuilder.build());
    }

    @Override
    public void close() {
        // Nope
    }

    @Override
    public void onText(final String text) {
        LOG.warn("Invalid indexing message type: text");
    }

    @Override
    public void onObjectMessage(final ObjectMessage objectMessage) {
        try {
            final IndexingJob indexingJob = (IndexingJob) objectMessage.getObject();
            final Map<String, Object> map = new HashMap<String, Object>(1);
            map.put("indexingJob", indexingJob);
            final String sCount = Long.toString(counter.incrementAndGet());
            /*
             * Define the job and tie it to our WrapperJob class
             */
            String jobKey = (String) indexingJob.getProperties().get(QuartzService.PROPERTY_JOB_KEY);
            if (null == jobKey) {
                jobKey = "job" + sCount;
            }
            final JobDetail job = newJob(QuartzIndexingJob.class).withIdentity(jobKey, GROUP).usingJobData(new JobDataMap(map)).build();
            /*
             * Trigger the job
             */
            final Trigger trigger = buildTrigger(indexingJob, job.getKey(), sCount);
            /*
             * Tell quartz to schedule the job using our trigger
             */
            quartzService.getLocalScheduler().scheduleJob(job, trigger);
        } catch (final JMSException e) {
            LOG.warn("A JMS error occurred: " + e.getMessage(), e);
        } catch (final SchedulerException e) {
            LOG.warn("A scheduler error occurred: " + e.getMessage(), e);
        } catch (final RuntimeException e) {
            LOG.warn("A runtime error occurred: " + e.getMessage(), e);
        }
    }

    @Override
    public void onBytes(final byte[] bytes) {
        try {
            final IndexingJob indexingJob = SerializableHelper.<IndexingJob> readObject(bytes);
            final Map<String, Object> map = new HashMap<String, Object>(1);
            map.put("indexingJob", indexingJob);
            final String sCount = Long.toString(counter.incrementAndGet());
            /*
             * Define the job and tie it to our WrapperJob class
             */
            String jobKey = (String) indexingJob.getProperties().get(QuartzService.PROPERTY_JOB_KEY);
            if (null == jobKey) {
                jobKey = "job" + sCount;
            }
            final JobDetail job = newJob(QuartzIndexingJob.class).withIdentity(jobKey, GROUP).usingJobData(new JobDataMap(map)).build();
            /*
             * Trigger the job to run now
             */
            final Trigger trigger = buildTrigger(indexingJob, job.getKey(), sCount);
            /*
             * Tell quartz to schedule the job using our trigger
             */
            quartzService.getLocalScheduler().scheduleJob(job, trigger);
        } catch (final ClassNotFoundException e) {
            LOG.warn("Invalid Java object in indexing message: " + e.getMessage(), e);
        } catch (final IOException e) {
            LOG.warn("Deserialization failed: " + e.getMessage(), e);
        } catch (final SchedulerException e) {
            LOG.warn("A scheduler error occurred: " + e.getMessage(), e);
        } catch (final RuntimeException e) {
            LOG.warn("A runtime error occurred: " + e.getMessage(), e);
        }
    }

    private Trigger buildTrigger(final IndexingJob indexingJob, final JobKey jobKey, final String sCount) {
        final TriggerBuilder<Trigger> triggerBuilder = newTrigger().withIdentity("trigger" + sCount, GROUP);
        if (null != jobKey) {
            triggerBuilder.forJob(jobKey);
        }
        final Map<String, ?> properties = indexingJob.getProperties();
        final String cronDesc = (String) properties.get(QuartzService.PROPERTY_CRON_EXPRESSION);
        if (null == cronDesc) {
            /*
             * A one-time or repeated job, starting either now or at a given start time.
             */
            Object tmp = properties.get(QuartzService.PROPERTY_START_TIME);
            if (null == tmp) {
                triggerBuilder.startNow();
            } else {
                final long startTime = longFrom(tmp);
                triggerBuilder.startAt(new Date(startTime));
            }
            tmp = properties.get(QuartzService.PROPERTY_REPEAT_COUNT);
            if (null == tmp) {
                /*
                 * No repeat count... forever?
                 */
                tmp = properties.get(QuartzService.PROPERTY_REPEAT_INTERVAL);
                if (null != tmp) {
                    /*
                     * Forever...
                     */
                    final long interval = longFrom(tmp);
                    triggerBuilder.withSchedule(simpleSchedule().repeatForever().withIntervalInMilliseconds(interval));
                }
            } else {
                final int repeatCount = intFrom(tmp);
                tmp = properties.get(QuartzService.PROPERTY_REPEAT_INTERVAL);
                if (null == tmp) {
                    /*
                     * With default interval (5 minutes)
                     */
                    triggerBuilder.withSchedule(simpleSchedule().withRepeatCount(repeatCount).withIntervalInMinutes(5));
                } else {
                    /*
                     * Interval
                     */
                    final long interval = longFrom(tmp);
                    triggerBuilder.withSchedule(simpleSchedule().withRepeatCount(repeatCount).withIntervalInMilliseconds(interval));
                }
            }
        } else {
            /*
             * A Cron job
             */
            triggerBuilder.withSchedule(cronSchedule(cronDesc));
        }
        return triggerBuilder.build();
    }

    private int intFrom(final Object tmp) {
        return tmp instanceof Integer ? ((Integer) tmp).intValue() : Integer.parseInt(tmp.toString().trim());
    }

    private long longFrom(final Object tmp) {
        return tmp instanceof Long ? ((Long) tmp).longValue() : Long.parseLong(tmp.toString().trim());
    }

    public static final class ReschedulerJob implements Job {

        private final Scheduler scheduler;

        /**
         * Initializes a new {@link ReschedulerJob}.
         */
        public ReschedulerJob(final Scheduler scheduler) {
            super();
            this.scheduler = scheduler;
        }

        @Override
        public void execute(final JobExecutionContext context) throws JobExecutionException {
            final long current = EXECUTION_TRACKER.get();
            final int max = THRESHOLD.get();
            if (max > 0 && current > max) {
                return;
            }
            final JobResc delayedJob = RESCHEDULE_JOBS.poll();
            if (null != delayedJob) {
                try {
                    final Trigger trigger = delayedJob.trigger;

                    /*-
                     * 
                    if (trigger instanceof CronTrigger) {
                        // A Cron job
                        scheduler.rescheduleJob(trigger.getKey(), trigger);
                    } else if (trigger instanceof SimpleTrigger) {
                        // Simple job
                        scheduler.rescheduleJob(trigger.getKey(), trigger);
                    } else {
                        // Any other job
                        scheduler.rescheduleJob(trigger.getKey(), trigger);
                    }
                     */

                    scheduler.rescheduleJob(trigger.getKey(), trigger);
                } catch (final SchedulerException e) {
                    throw new JobExecutionException(e);
                }
            }
        }

    }

    /**
     * A wrapper for an {@link IndexingJob} instance.
     */
    public static final class QuartzIndexingJob implements Job {

        /**
         * Initializes a new {@link QuartzIndexingJob}.
         */
        public QuartzIndexingJob() {
            super();
        }

        @Override
        public void execute(final JobExecutionContext context) throws JobExecutionException {
            final JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
            final IndexingJob indexingJob = (IndexingJob) jobDataMap.get("indexingJob");
            if (null == indexingJob) {
                // Huh?
                return;
            }
            final long current = EXECUTION_TRACKER.incrementAndGet();
            try {
                final int max = THRESHOLD.get();
                if (max <= 0 || current <= max || !reschedulable(context)) {
                    // Either no threshold specified, within threshold boundary or not reschedule-able (queue full, etc.)
                    performJob(indexingJob);
                }
            } finally {
                EXECUTION_TRACKER.decrementAndGet();
            }
        }

        private boolean reschedulable(final JobExecutionContext context) {
            final Trigger trigger = context.getTrigger();
            final JobDetail jobDetail = context.getJobDetail();
            if (trigger instanceof CronTrigger) {
                // A Cron job cannot be re-scheduled as-is
                final TriggerKey key = trigger.getKey();
                String triggerName = key.getName();
                if (!triggerName.startsWith("re-")) {
                    triggerName = "re-" + triggerName;
                }
                final TriggerBuilder<Trigger> triggerBuilder = newTrigger().withIdentity(triggerName, key.getGroup());
                triggerBuilder.forJob(jobDetail.getKey());
                triggerBuilder.startNow(); // Immediate start as soon as triggered
                return RESCHEDULE_JOBS.offer(new JobResc(jobDetail, triggerBuilder.build()));
            } else if (trigger instanceof SimpleTrigger) {
                // Simple job
                final SimpleTrigger simpleTrigger = (SimpleTrigger) trigger;
                if (simpleTrigger.getRepeatCount() > 0) {
                    // A repeated job cannot be re-scheduled as-is
                    final TriggerKey key = trigger.getKey();
                    String triggerName = key.getName();
                    if (!triggerName.startsWith("re-")) {
                        triggerName = "re-" + triggerName;
                    }
                    final TriggerBuilder<Trigger> triggerBuilder = newTrigger().withIdentity(triggerName, key.getGroup());
                    triggerBuilder.forJob(jobDetail.getKey());
                    triggerBuilder.startNow(); // Immediate start as soon as triggered
                    return RESCHEDULE_JOBS.offer(new JobResc(jobDetail, triggerBuilder.build()));
                }
                // Can be re-scheduled as-is
                return RESCHEDULE_JOBS.offer(new JobResc(jobDetail, trigger));
            } else {
                // Any other job... re-schedule as one-time execution for the sake of safety
                final TriggerKey key = trigger.getKey();
                String triggerName = key.getName();
                if (!triggerName.startsWith("re-")) {
                    triggerName = "re-" + triggerName;
                }
                final TriggerBuilder<Trigger> triggerBuilder = newTrigger().withIdentity(triggerName, key.getGroup());
                triggerBuilder.forJob(jobDetail.getKey());
                triggerBuilder.startNow(); // Immediate start as soon as triggered
                return RESCHEDULE_JOBS.offer(new JobResc(jobDetail, triggerBuilder.build()));
            }
        }

        private void performJob(final IndexingJob indexingJob) throws JobExecutionException {
            boolean ran = false;
            indexingJob.beforeExecute();
            try {
                indexingJob.performJob();
                ran = true;
                indexingJob.afterExecute(null);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                if (!ran) {
                    indexingJob.afterExecute(e);
                }
                LOG.warn("Indexing job failed with unchecked error.", e);
                throw new JobExecutionException(e.getMessage(), e);
            } catch (final Exception e) {
                if (!ran) {
                    indexingJob.afterExecute(e);
                }
                LOG.warn("Indexing job failed with unchecked error.", e);
                throw new JobExecutionException(e.getMessage(), e);
            }
        }

    }

}

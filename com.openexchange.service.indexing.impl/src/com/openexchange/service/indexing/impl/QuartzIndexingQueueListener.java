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
import static org.quartz.TriggerBuilder.newTrigger;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import org.apache.commons.logging.Log;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.service.QuartzService;
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

    static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(QuartzIndexingQueueListener.class));

    private final AtomicLong counter;

    private final QuartzService jobExecutor;

    /**
     * Initializes a new {@link QuartzIndexingQueueListener}.
     */
    public QuartzIndexingQueueListener(final QuartzService executor) {
        super();
        counter = new AtomicLong();
        jobExecutor = executor;
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
            final String name = Long.toString(counter.incrementAndGet());
            /*
             * Define the job and tie it to our WrapperJob class
             */
            final JobDetail job =
                newJob(WrapperJob.class).withIdentity("job"+name, GROUP).usingJobData(new JobDataMap(map)).build();
            /*
             * Trigger the job
             */
            final TriggerBuilder<Trigger> triggerBuilder = newTrigger().withIdentity("trigger"+name, GROUP);
            final String cronDesc = (String) indexingJob.getProperties().get(QuartzService.PROPERTY_CRON_EXPRESSION);
            if (null == cronDesc) {
                triggerBuilder.startNow();
            } else {
                triggerBuilder.withSchedule(cronSchedule(cronDesc));
            }
            final Trigger trigger = triggerBuilder.build();
            /*
             * Tell quartz to schedule the job using our trigger
             */
            jobExecutor.getScheduler().scheduleJob(job, trigger);
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
            final String name = Long.toString(counter.incrementAndGet());
            /*
             * Define the job and tie it to our WrapperJob class
             */
            final JobDetail job =
                newJob(WrapperJob.class).withIdentity("job"+name, GROUP).usingJobData(new JobDataMap(map)).build();
            /*
             * Trigger the job to run now
             */
            final TriggerBuilder<Trigger> triggerBuilder = newTrigger().withIdentity("trigger"+name, GROUP);
            final String cronDesc = (String) indexingJob.getProperties().get(QuartzService.PROPERTY_CRON_EXPRESSION);
            if (null == cronDesc) {
                triggerBuilder.startNow();
            } else {
                triggerBuilder.withSchedule(cronSchedule(cronDesc));
            }
            final Trigger trigger = triggerBuilder.build();
            /*
             * Tell quartz to schedule the job using our trigger
             */
            jobExecutor.getScheduler().scheduleJob(job, trigger);
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

    private static final class WrapperJob implements Job {

        public WrapperJob() {
            super();
        }

        @Override
        public void execute(final JobExecutionContext context) throws JobExecutionException {
            final JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
            final IndexingJob indexingJob = (IndexingJob) jobDataMap.get("indexingJob");
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

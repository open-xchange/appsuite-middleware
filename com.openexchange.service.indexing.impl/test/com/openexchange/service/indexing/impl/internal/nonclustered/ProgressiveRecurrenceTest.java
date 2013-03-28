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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import junit.framework.Assert;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.service.QuartzService;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.openexchange.exception.OXException;
import com.openexchange.service.indexing.IndexingJob;
import com.openexchange.service.indexing.JobInfo;
import com.openexchange.service.indexing.impl.internal.Services;
import com.openexchange.service.indexing.impl.internal.Tools;

/**
 * {@link ProgressiveRecurrenceTest}
 * 
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class ProgressiveRecurrenceTest {

    private static final String SYNCHRONIZER = "synchronizer";

    @Before
    public void before() {
        Services.setServiceLookup(new MockServiceLookup());
    }

    @After
    public void after() {
        Services.setServiceLookup(null);
    }

    @Test
    public void testProgressiveReschedulingAndHazelcastMapEviction() throws Exception {
        /*
         * This job runs 3 times. At the 4th run, the timeout is already exceeded.
         * The job should not be executed on that run and should be evicted shortly after it.
         * 
         * According progression table:
         * Run        Next Interval        Time since Add
         *  0              10000                   0
         *  1              15000               10000
         *  2              22500               25000
         *  3              33750               47500
         *  4              50625               81250
         */
        JobInfo info = new TestJobInfo.Builder(InnerJob.class).contextId(1).userId(2).setId("testJob1").build();
        JobInfoWrapper infoWrapper = new JobInfoWrapper(info, 60000L, 10000L, 50);
        QuartzService quartzService = Services.getService(QuartzService.class);
        Scheduler scheduler = quartzService.getScheduler("testScheduler", false, 2);
        scheduler.start();
        try {
            CountDownLatch jobLatch = new CountDownLatch(4);
            JobDataMap jobData = new JobDataMap();
            jobData.put(SYNCHRONIZER, jobLatch);
            JobDetail jobDetail = JobBuilder.newJob(TestJob.class)
                .withIdentity(info.toUniqueId(), "jobGroup1")
                .usingJobData(jobData)
                .build();
            String triggerName = info.toUniqueId() + "/withProgressiveInterval";
            SimpleTrigger trigger = TriggerBuilder.newTrigger()
                .forJob(jobDetail.getKey())
                .withIdentity(triggerName, Tools.generateTriggerGroup(info.contextId, info.userId))
                .startAt(new Date())
                .withPriority(5)
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
                .build();

            final CountDownLatch evictionLatch = new CountDownLatch(1);
            HazelcastInstance hazelcast = Services.getService(HazelcastInstance.class);
            IMap<String, JobInfoWrapper> jobs = hazelcast.getMap(RecurringJobsManager.JOB_MAP);
            jobs.addEntryListener(new EntryListener<String, JobInfoWrapper>() {

                @Override
                public void entryUpdated(EntryEvent<String, JobInfoWrapper> event) {
                }

                @Override
                public void entryRemoved(EntryEvent<String, JobInfoWrapper> event) {
                }

                @Override
                public void entryEvicted(EntryEvent<String, JobInfoWrapper> event) {
                    System.out.println(String.format("Evicting job %s at %tT", event.getValue().getJobInfo().toString(), new Date()));
                    evictionLatch.countDown();
                }

                @Override
                public void entryAdded(EntryEvent<String, JobInfoWrapper> event) {
                }
            }, true);
            
            Assert.assertTrue("Job already existed", RecurringJobsManager.addOrUpdateJob(jobDetail.getKey().toString(), infoWrapper));
            Assert.assertTrue("Wrong job count in RecurringJobsManager", RecurringJobsManager.getJobCount() == 1);
            scheduler.scheduleJob(jobDetail, trigger);

            /*
             * Timeout is reached after 81250ms, see progression table above. Give it some play...
             */
            Assert.assertTrue("jobLatch exceeded timeout", jobLatch.await(90000L, TimeUnit.MILLISECONDS));

            /*
             * Eviction is always delayed a bit. About 25s in local tests. So we give it a minute to be sure...
             */
            Assert.assertTrue("evictionLatch exceeded timeout", evictionLatch.await(60000L, TimeUnit.MILLISECONDS));
            Assert.assertTrue("Wrong job count in RecurringJobsManager", RecurringJobsManager.getJobCount() == 0);
            Assert.assertTrue(
                "Wrong job count in scheduler",
                scheduler.getJobKeys(GroupMatcher.jobGroupEquals(jobDetail.getKey().getGroup())).size() == 0);
            Assert.assertTrue(
                "Wrong trigger count in scheduler",
                scheduler.getTriggerKeys(GroupMatcher.triggerGroupEquals(trigger.getKey().getGroup())).size() == 0);
        } finally {
            scheduler.shutdown();
        }
    }

    @Test
    public void testProgressiveReschedulingWithTimeoutReset() throws Exception {
        /*
         * This time we run 2 times, reset the job and let it run for another 4 times.
         * The job should not be executed on that 6th run and should be evicted shortly after it.
         * 
         * According progression table:
         * Run        Next Interval        Time since Add
         *  0              10000                   0
         *  1              15000               10000
         *  2              22500               25000
         *  3              33750               47500
         *  4              50625               81250
         */
        JobInfo info = new TestJobInfo.Builder(InnerJob.class).contextId(1).userId(2).setId("testJob1").build();
        JobInfoWrapper infoWrapper = new JobInfoWrapper(info, 60000L, 10000L, 50);
        QuartzService quartzService = Services.getService(QuartzService.class);
        Scheduler scheduler = quartzService.getScheduler("testScheduler", false, 2);
        scheduler.start();
        try {
            CountDownLatch jobLatch = new CountDownLatch(6);
            JobDataMap jobData = new JobDataMap();
            jobData.put(SYNCHRONIZER, jobLatch);
            JobDetail jobDetail = JobBuilder.newJob(TestJob.class)
                .withIdentity(info.toUniqueId(), "jobGroup1")
                .usingJobData(jobData)
                .build();
            String triggerName = info.toUniqueId() + "/withProgressiveInterval";
            SimpleTrigger trigger = TriggerBuilder.newTrigger()
                .forJob(jobDetail.getKey())
                .withIdentity(triggerName, Tools.generateTriggerGroup(info.contextId, info.userId))
                .startAt(new Date())
                .withPriority(5)
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
                .build();

            final CountDownLatch evictionLatch = new CountDownLatch(1);
            HazelcastInstance hazelcast = Services.getService(HazelcastInstance.class);
            IMap<String, JobInfoWrapper> jobs = hazelcast.getMap(RecurringJobsManager.JOB_MAP);
            jobs.addEntryListener(new EntryListener<String, JobInfoWrapper>() {

                @Override
                public void entryUpdated(EntryEvent<String, JobInfoWrapper> event) {
                }

                @Override
                public void entryRemoved(EntryEvent<String, JobInfoWrapper> event) {
                }

                @Override
                public void entryEvicted(EntryEvent<String, JobInfoWrapper> event) {
                    System.out.println(String.format("Evicting job %s at %tT", event.getValue().getJobInfo().toString(), new Date()));
                    evictionLatch.countDown();
                }

                @Override
                public void entryAdded(EntryEvent<String, JobInfoWrapper> event) {
                }
            }, true);
            
            Assert.assertTrue("Job already existed", RecurringJobsManager.addOrUpdateJob(jobDetail.getKey().toString(), infoWrapper));
            Assert.assertTrue("Wrong job count in RecurringJobsManager", RecurringJobsManager.getJobCount() == 1);
            scheduler.scheduleJob(jobDetail, trigger);

            /*
             * We should get here after 2 runs, which is approximately after 25000ms. Give it 5 seconds play...
             */
            Thread.sleep(30000L);
            Assert.assertTrue("Wrong count on jobLatch", jobLatch.getCount() == 4);
            
            /*
             * Reset timeout and interval
             */
            infoWrapper.touch();
            RecurringJobsManager.addOrUpdateJob(jobDetail.getKey().toString(), infoWrapper);
            
            /*
             * Timeout is reached after 81250ms, see progression table above. Give it some play...
             */
            Assert.assertTrue("jobLatch exceeded timeout", jobLatch.await(90000L, TimeUnit.MILLISECONDS));

            /*
             * Eviction is always delayed a bit. About 25s in local tests. So we give it a minute to be sure...
             */
            Assert.assertTrue("evictionLatch exceeded timeout", evictionLatch.await(60000L, TimeUnit.MILLISECONDS));
            Assert.assertTrue("Wrong job count in RecurringJobsManager", RecurringJobsManager.getJobCount() == 0);
            Assert.assertTrue(
                "Wrong job count in scheduler",
                scheduler.getJobKeys(GroupMatcher.jobGroupEquals(jobDetail.getKey().getGroup())).size() == 0);
            Assert.assertTrue(
                "Wrong trigger count in scheduler",
                scheduler.getTriggerKeys(GroupMatcher.triggerGroupEquals(trigger.getKey().getGroup())).size() == 0);
        } finally {
            scheduler.shutdown();
        }
    }

    @DisallowConcurrentExecution
    public static final class TestJob extends ProgressiveRecurringJob {

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            CountDownLatch latch = (CountDownLatch) context.getMergedJobDataMap().get(SYNCHRONIZER);
            latch.countDown();
            super.execute(context);
        }

        @Override
        protected boolean perform(JobExecutionContext context, JobInfoWrapper infoWrapper) throws OXException {
            Date lastUpdate = new Date(infoWrapper.getLastUpdate());
            Date timeout = new Date(infoWrapper.getJobTimeout() + lastUpdate.getTime());
            Date now = new Date();
            Date nextExecution = new Date(
                now.getTime() + (infoWrapper.getInterval() + (infoWrapper.getInterval() * infoWrapper.getProgressionRate() / 100)));
            System.out.println(String.format(
                "lastUpdate: %tT, timeout: %tT, now: %tT, nextExecution: %tT",
                lastUpdate,
                timeout,
                now,
                nextExecution));

            return true;
        }
    }

    public static final class InnerJob implements IndexingJob {

        @Override
        public void execute(JobInfo jobInfo) throws OXException {
            return;
        }

    }

}

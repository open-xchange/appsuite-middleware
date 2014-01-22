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

package com.openexchange.quartz.hazelcast;

import java.util.Date;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

import org.junit.Assert;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;


/**
 * {@link TestJobs}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class TestJobs {

    @DisallowConcurrentExecution
    public static class TestJob implements Job {

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            try {
                @SuppressWarnings("unchecked") List<Date> jobExecDates = (List<Date>) context.getScheduler().getContext().get(DisallowConcurrentExecutionJobTest.DATE_STAMPS);
                long firedAt = System.currentTimeMillis();
                jobExecDates.add(new Date(firedAt));
                long sleepTill = firedAt + DisallowConcurrentExecutionJobTest.JOB_BLOCK_TIME;
                for (long sleepFor = sleepTill - System.currentTimeMillis(); sleepFor > 0; sleepFor = sleepTill - System.currentTimeMillis()) {
                    Thread.sleep(sleepFor);
                }
            } catch (InterruptedException e) {
                throw new JobExecutionException("Failed to pause job for testing.");
            } catch (SchedulerException e) {
                throw new JobExecutionException("Failed to lookup datestamp collection.");
            }
        }
    }

    @DisallowConcurrentExecution
    public static class SleepingTestJob implements Job {

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            try {
                System.out.println("Executing job.");
                Thread.sleep(2000L);
            } catch (InterruptedException e) {
                throw new JobExecutionException("Failed to pause job for testing.");
            }
        }
    }

    @DisallowConcurrentExecution
    public static class ReschedulingTestJob implements Job {

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            try {
                System.out.println("Started. Scheduler: " + context.getScheduler().getSchedulerName() + ". Job: " + context.getJobDetail().getKey().toString() + ". Trigger: " + context.getTrigger().getKey().toString());
                Scheduler scheduler1 = context.getScheduler();
                scheduler1.scheduleJob(TriggerBuilder.newTrigger()
                    .forJob(context.getJobDetail())
                    .withIdentity("someTrigger/1/2/3/ABC", "testTriggers/1/2/oneShot/" + System.currentTimeMillis())
                    .startAt(new Date(System.currentTimeMillis()))
                    .withPriority(5)
                    .build());
                Thread.sleep(2000L);
                System.out.println("Finished. Scheduler: " + context.getScheduler().getSchedulerName() + ". Job: " + context.getJobDetail().getKey().toString() + ". Trigger: " + context.getTrigger().getKey().toString());
            } catch (InterruptedException e) {
                throw new JobExecutionException("Failed to pause job for testing.");
            } catch (SchedulerException e) {
                throw new JobExecutionException("Failed to trigger job for testing.");
            }
        }
    }

    @DisallowConcurrentExecution
    public static class LongRunningJob implements Job {

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            try {
                System.out.println("execute. Scheduler: " + context.getScheduler().getSchedulerName() + ". Job: " + context.getJobDetail().getKey().toString() + ". Trigger: " + context.getTrigger().getKey().toString());
                CyclicBarrier barrier = (CyclicBarrier) context.getScheduler().getContext().get(DisallowConcurrentExecutionJobTest.BARRIER);
                barrier.await();
                Thread.sleep(30000L);
            } catch (SchedulerException e) {
                throw new JobExecutionException("Error", e);
            } catch (InterruptedException e) {
                throw new JobExecutionException("Error", e);
            } catch (BrokenBarrierException e) {
                throw new JobExecutionException("Error", e);
            }
        }
    }

    @DisallowConcurrentExecution
    @PersistJobDataAfterExecution
    public static class ChangingDetailJob implements Job {

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            try {
                System.out.println("Executing job.");
                JobDetail jobDetail = context.getJobDetail();
                JobDataMap dataMap = jobDetail.getJobDataMap();
                CountDownLatch latch = (CountDownLatch) context.getScheduler().getContext().get(DisallowConcurrentExecutionJobTest.BARRIER);
                if (latch.getCount() == 1L) {
                    String value = (String) dataMap.get("new");
                    Assert.assertNotNull("Value was null", value);
                    Assert.assertEquals("Wrong value", "test", "test");
                } else {
                    dataMap.put("new", "test");
                }

                latch.countDown();
            } catch (Throwable e) {
                throw new JobExecutionException("Error", e);
            }
        }
    }

}

/* 
 * Copyright 2001-2011 Terracotta, Inc. 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not 
 * use this file except in compliance with the License. You may obtain a copy 
 * of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
 * License for the specific language governing permissions and limitations 
 * under the License.
 */

package com.openexchange.service.indexing.hazelcast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Assert;
import org.junit.Test;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.DirectSchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.listeners.JobListenerSupport;
import org.quartz.service.internal.HazelcastJobStore;
import org.quartz.simpl.RAMJobStore;
import org.quartz.simpl.SimpleThreadPool;

/**
 * Integration test for using DisallowConcurrentExecution annot.
 * 
 * @author Zemian Deng <saltnlight5@gmail.com>
 */
public class DisallowConcurrentExecutionJobTest {

    private static final String JOB_STORE = "com.openexchange.service.indexing.hazelcast.TestableHazelcastJobStore";

//     private static final String JOB_STORE = "org.quartz.simpl.RAMJobStore";

    private static final long JOB_BLOCK_TIME = 300L;

    private static final String BARRIER = "BARRIER";

    private static final String DATE_STAMPS = "DATE_STAMPS";

    @DisallowConcurrentExecution
    public static class TestJob implements Job {

        public void execute(JobExecutionContext context) throws JobExecutionException {
            try {
                @SuppressWarnings("unchecked") List<Date> jobExecDates = (List<Date>) context.getScheduler().getContext().get(DATE_STAMPS);
                long firedAt = System.currentTimeMillis();
                jobExecDates.add(new Date(firedAt));
                long sleepTill = firedAt + JOB_BLOCK_TIME;
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

        public void execute(JobExecutionContext context) throws JobExecutionException {
            try {
                System.out.println("execute. Scheduler: " + context.getScheduler().getSchedulerName() + ". Job: " + context.getJobDetail().getKey().toString() + ". Trigger: " + context.getTrigger().getKey().toString());
                Scheduler scheduler1 = context.getScheduler();
                scheduler1.scheduleJob(TriggerBuilder.newTrigger()
                    .forJob(context.getJobDetail())
                    .withIdentity("someTrigger/1/2/3/ABC", "testTriggers/1/2/oneShot/" + System.currentTimeMillis())
                    .startAt(new Date(System.currentTimeMillis()))
                    .withPriority(5)
                    .build());
                Thread.sleep(2000L);                
            } catch (InterruptedException e) {
                throw new JobExecutionException("Failed to pause job for testing.");
            } catch (SchedulerException e) {
                throw new JobExecutionException("Failed to trigger job for testing.");
            }
        }
    }

    public static class TestJobListener extends JobListenerSupport {

        private final AtomicInteger jobExCount = new AtomicInteger(0);

        private final int jobExecutionCountToSyncAfter;

        public TestJobListener(int jobExecutionCountToSyncAfter) {
            this.jobExecutionCountToSyncAfter = jobExecutionCountToSyncAfter;
        }

        public String getName() {
            return "TestJobListener";
        }

        @Override
        public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
            if (jobExCount.incrementAndGet() == jobExecutionCountToSyncAfter) {
                try {
                    CyclicBarrier barrier = (CyclicBarrier) context.getScheduler().getContext().get(BARRIER);
                    barrier.await(125, TimeUnit.SECONDS);
                } catch (Throwable e) {
                    e.printStackTrace();
                    throw new AssertionError("Await on barrier was interrupted: " + e.toString());
                }
            }
        }
    }
    
    public static class SleepingJobListener extends JobListenerSupport {
        
        private AtomicInteger count = new AtomicInteger(0);

        
        @Override
        public String getName() {
            return "SleepingJobListener";
        }
        
        @Override
        public synchronized void jobToBeExecuted(JobExecutionContext context) {            
            try {
                System.out.println("ToBeExecuted. Scheduler: " + context.getScheduler().getSchedulerName() + ". Job: " + context.getJobDetail().getKey().toString() + ". Trigger: " + context.getTrigger().getKey().toString());
            } catch (SchedulerException e) {
                throw new AssertionError(e.getMessage());
            }
            
            if (!count.compareAndSet(0, 1)) {
                throw new AssertionError("Concurrent job count was not 0 but " + count.get());
            }
        }
        
        @Override
        public synchronized void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
            try {
                System.out.println("WasExecuted. Scheduler: " + context.getScheduler().getSchedulerName() + ". Job: " + context.getJobDetail().getKey().toString() + ". Trigger: " + context.getTrigger().getKey().toString());
                CountDownLatch latch = (CountDownLatch) context.getScheduler().getContext().get(BARRIER);
                latch.countDown();
                if (!count.compareAndSet(1, 0)) {
                    throw new AssertionError("Concurrent job count was not 1 but " + count.get());
                }
            } catch (SchedulerException e) {
                throw new AssertionError("Error while counting down the latch.");
            }            
        }        
    }
    
    @Test
    public void testClusterSchedulerConcurrency() throws Exception {
        HazelcastJobStore jobStore = new TestableHazelcastJobStore();
//        RAMJobStore jobStore = new RAMJobStore();
        DirectSchedulerFactory.getInstance().createScheduler("sched1", "1", new SimpleThreadPool(4, 1), jobStore, null, 0, 10, -1);
        DirectSchedulerFactory.getInstance().createScheduler("sched2", "2", new SimpleThreadPool(4, 1), jobStore, null, 0, 10, -1);    
        Scheduler scheduler1 = DirectSchedulerFactory.getInstance().getScheduler("sched1");
        Scheduler scheduler2 = DirectSchedulerFactory.getInstance().getScheduler("sched2");
        CountDownLatch latch = new CountDownLatch(9);
        SleepingJobListener listener = new SleepingJobListener();
        scheduler1.getContext().put(BARRIER, latch);
        scheduler1.getListenerManager().addJobListener(listener);
        scheduler2.getContext().put(BARRIER, latch);
        scheduler2.getListenerManager().addJobListener(listener);
        scheduler1.start();
        scheduler2.start();

        JobDetail job1 = JobBuilder.newJob(ReschedulingTestJob.class)
            .withIdentity("TestJob/1/2/3/ABC", "testJobs/1/2")
            .build();        
        
        SimpleTrigger trigger1 = TriggerBuilder.newTrigger()
            .forJob(job1)
            .withIdentity("someTrigger/1/2/3/ABC", "testTriggers/1/2/withInterval/5000")
            .startAt(new Date(System.currentTimeMillis()))
            .withPriority(5)
            .withSchedule(SimpleScheduleBuilder.simpleSchedule()
            .withIntervalInMilliseconds(5000)
            .repeatForever()
            .withMisfireHandlingInstructionFireNow())
            .build();
        
        scheduler1.scheduleJob(job1, trigger1);
        
        Thread.sleep(10);
        scheduler2.scheduleJob(TriggerBuilder.newTrigger()
            .forJob(job1)
            .withIdentity("someTrigger/1/2/3/ABC", "testTriggers/1/2/oneShot/" + System.currentTimeMillis())
            .startAt(new Date(System.currentTimeMillis()))
            .withPriority(5)
            .build());
        
        Thread.sleep(10);
        scheduler1.scheduleJob(TriggerBuilder.newTrigger()
            .forJob(job1)
            .withIdentity("someTrigger/1/2/3/ABC", "testTriggers/1/2/oneShot/" + System.currentTimeMillis())
            .startAt(new Date(System.currentTimeMillis()))
            .withPriority(5)
            .build());
        
        Thread.sleep(10);
        scheduler2.scheduleJob(TriggerBuilder.newTrigger()
            .forJob(job1)
            .withIdentity("someTrigger/1/2/3/ABC", "testTriggers/1/2/oneShot/" + System.currentTimeMillis())
            .startAt(new Date(System.currentTimeMillis()))
            .withPriority(5)
            .build());
        
        latch.await(30, TimeUnit.SECONDS);
        if (latch.getCount() > 0L) {
            Assert.fail("Count was " + latch.getCount());
        }

        scheduler1.shutdown(true);
        scheduler2.shutdown(true);
    }
    
    @Test
    public void testSomething() throws Exception {
        CountDownLatch latch = new CountDownLatch(2);
        Properties props = new Properties();
        props.setProperty("org.quartz.scheduler.idleWaitTime", "1500");
        props.setProperty("org.quartz.threadPool.threadCount", "3");
        props.setProperty("org.quartz.jobStore.class", JOB_STORE);
        Scheduler scheduler = new StdSchedulerFactory(props).getScheduler();
        scheduler.getContext().put(BARRIER, latch);
        scheduler.getListenerManager().addJobListener(new SleepingJobListener());
        
        Date startTime = new Date(System.currentTimeMillis() + 100);
        JobDetail job1 = JobBuilder.newJob(SleepingTestJob.class).withIdentity("job1").build();
        Trigger trigger1 = TriggerBuilder.newTrigger()
            .withSchedule(SimpleScheduleBuilder.simpleSchedule())
            .startAt(startTime)
            .build();

        Trigger trigger2 = TriggerBuilder.newTrigger()
            .withSchedule(SimpleScheduleBuilder.simpleSchedule())
            .startAt(new Date(startTime.getTime() + 100))
            .forJob(job1.getKey())
            .build();
        
        scheduler.scheduleJob(job1, trigger1);
        scheduler.scheduleJob(trigger2);
        scheduler.start();
        
        latch.await(125, TimeUnit.SECONDS);
        scheduler.shutdown(true);
    }

    @Test
    public void testNoConcurrentExecOnSameJob() throws Exception {
        List<Date> jobExecDates = Collections.synchronizedList(new ArrayList<Date>());
        CyclicBarrier barrier = new CyclicBarrier(2);

        Properties props = new Properties();
        props.setProperty("org.quartz.scheduler.idleWaitTime", "1500");
        props.setProperty("org.quartz.threadPool.threadCount", "2");
        props.setProperty("org.quartz.jobStore.class", JOB_STORE);
        Scheduler scheduler = new StdSchedulerFactory(props).getScheduler();
        scheduler.getContext().put(BARRIER, barrier);
        scheduler.getContext().put(DATE_STAMPS, jobExecDates);
        scheduler.getListenerManager().addJobListener(new TestJobListener(2));

        Date startTime = new Date(System.currentTimeMillis() + 100); // make the triggers fire at the same time.

        JobDetail job1 = JobBuilder.newJob(TestJob.class).withIdentity("job1").build();
        Trigger trigger1 = TriggerBuilder.newTrigger().withSchedule(SimpleScheduleBuilder.simpleSchedule()).startAt(startTime).build();

        Trigger trigger2 = TriggerBuilder.newTrigger().withSchedule(SimpleScheduleBuilder.simpleSchedule()).startAt(startTime).forJob(
            job1.getKey()).build();
        scheduler.scheduleJob(job1, trigger1);
        scheduler.scheduleJob(trigger2);
        scheduler.start();

        barrier.await(125, TimeUnit.SECONDS);

        scheduler.shutdown(true);

        Assert.assertTrue(jobExecDates.size() == 2);
        long fireTimeTrigger1 = jobExecDates.get(0).getTime();
        long fireTimeTrigger2 = jobExecDates.get(1).getTime();
        Assert.assertTrue((fireTimeTrigger2 - fireTimeTrigger1) >= JOB_BLOCK_TIME);
    }

    /** QTZ-202 */
    @Test
    public void testNoConcurrentExecOnSameJobWithBatching() throws Exception {
        List<Date> jobExecDates = Collections.synchronizedList(new ArrayList<Date>());
        CyclicBarrier barrier = new CyclicBarrier(2);

        Date startTime = new Date(System.currentTimeMillis() + 100); // make the triggers fire at the same time.

        JobDetail job1 = JobBuilder.newJob(TestJob.class).withIdentity("job1").build();
        Trigger trigger1 = TriggerBuilder.newTrigger().withSchedule(SimpleScheduleBuilder.simpleSchedule()).startAt(startTime).build();

        Trigger trigger2 = TriggerBuilder.newTrigger().withSchedule(SimpleScheduleBuilder.simpleSchedule()).startAt(startTime).forJob(
            job1.getKey()).build();

        Properties props = new Properties();
        props.setProperty("org.quartz.scheduler.idleWaitTime", "1500");
        props.setProperty("org.quartz.scheduler.batchTriggerAcquisitionMaxCount", "2");
        props.setProperty("org.quartz.threadPool.threadCount", "2");
        props.setProperty("org.quartz.jobStore.class", JOB_STORE);
        Scheduler scheduler = new StdSchedulerFactory(props).getScheduler();
        scheduler.getContext().put(BARRIER, barrier);
        scheduler.getContext().put(DATE_STAMPS, jobExecDates);
        scheduler.getListenerManager().addJobListener(new TestJobListener(2));
        scheduler.scheduleJob(job1, trigger1);
        scheduler.scheduleJob(trigger2);
        scheduler.start();

        barrier.await(125, TimeUnit.SECONDS);

        scheduler.shutdown(true);

        Assert.assertTrue(jobExecDates.size() == 2);
        long fireTimeTrigger1 = jobExecDates.get(0).getTime();
        long fireTimeTrigger2 = jobExecDates.get(1).getTime();
        Assert.assertTrue((fireTimeTrigger2 - fireTimeTrigger1) >= JOB_BLOCK_TIME);
    }
}

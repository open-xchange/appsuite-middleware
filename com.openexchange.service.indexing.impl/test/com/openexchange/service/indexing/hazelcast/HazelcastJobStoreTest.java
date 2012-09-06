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

package com.openexchange.service.indexing.hazelcast;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.quartz.AbstractJobStoreTest.SampleSignaler;
import org.quartz.DateBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.JobPersistenceException;
import org.quartz.ObjectAlreadyExistsException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.Trigger.TriggerState;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.impl.triggers.SimpleTriggerImpl;
import org.quartz.jobs.NoOpJob;
import org.quartz.simpl.CascadingClassLoadHelper;
import org.quartz.spi.ClassLoadHelper;
import org.quartz.spi.JobStore;
import org.quartz.spi.OperableTrigger;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Instance;


/**
 * {@link HazelcastJobStoreTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class HazelcastJobStoreTest {
    
    protected JobStore jobStore = null;
    
    protected SampleSignaler signaler = null;

    private JobDetail jobDetail;

    private Date startDate;
    
    
    @Before
    public void setUp() throws Exception {        
        signaler = new SampleSignaler();
        ClassLoadHelper loadHelper = new CascadingClassLoadHelper();
        loadHelper.initialize();
        jobStore = new TestableHazelcastJobStore();
        jobStore.initialize(loadHelper, signaler);
        jobStore.schedulerStarted();
        
        startDate = DateBuilder.evenMinuteDateAfterNow();
        jobDetail = new MutableJobDetail("testJob1", "testJobGroup1", true);
        jobStore.storeJob(jobDetail, false);
    }
    
    @After
    public void tearDown() throws Exception {
        jobStore.shutdown();      
        jobStore = null;
    }
    
//    @Test
//    public void testNotEqualPredicate() throws Exception {
//        HazelcastInstance hazelcast = getHazelcastInstance();
//        IMap<String, MapValue> map = hazelcast.getMap("mapName");
//        map.addIndex("value", false);
//        
//        map.put("key1", new MapValue(1));
//        
//        EntryObject entryObject = new PredicateBuilder().getEntryObject().get("value");
//        PredicateBuilder notEqualPredicate = entryObject.notEqual(5);
//        Collection<MapValue> values = map.values(notEqualPredicate);
//        assertTrue(!values.isEmpty());
//    }
//    
//    private HazelcastInstance getHazelcastInstance() throws JobPersistenceException {
//        return jobStore.getHazelcast();
//    }
//    
//    private static final class MapValue implements Serializable {
//        
//        private final int value;
//        
//        public MapValue(int value) {
//            super();
//            this.value = value;            
//        }
//        
//        public int getValue() {
//            return value;
//        }
//    }
    
    @Test
    public void testConcurrentExecution() throws Exception {
        MutableJobDetail jobDetail = (MutableJobDetail) this.jobDetail.clone();
        jobDetail.setConcurrentExecution(false);
        jobStore.storeJob(jobDetail, true);
        
        SimpleTriggerImpl trigger1 = (SimpleTriggerImpl) SimpleScheduleBuilder.repeatSecondlyForever(5).build();
        trigger1.setKey(new TriggerKey("testTrigger1", "testTriggerGroup1"));
        trigger1.setJobKey(jobDetail.getKey());
        trigger1.setStartTime(startDate);
        trigger1.computeFirstFireTime(null);
        jobStore.storeTrigger(trigger1, false);
        
        SimpleTriggerImpl trigger2 = (SimpleTriggerImpl) SimpleScheduleBuilder.repeatSecondlyForever(5).build();
        trigger2.setKey(new TriggerKey("testTrigger2", "testTriggerGroup1"));
        trigger2.setJobKey(jobDetail.getKey());
        trigger2.setStartTime(startDate);
        trigger2.computeFirstFireTime(null);
        jobStore.storeTrigger(trigger2, false);
        
        long firstFireTime = new Date(trigger1.getNextFireTime().getTime()).getTime();
        List<OperableTrigger> nextTriggers = jobStore.acquireNextTriggers(firstFireTime, 2, 0L);        
        Assert.assertTrue(nextTriggers.size() == 1);
        
        nextTriggers = jobStore.acquireNextTriggers(firstFireTime, 2, 0L);        
        Assert.assertTrue(nextTriggers.size() == 0);
        
    }
    
    /*
     * Tests from quartz sources
     */
    @Test
    public void testAcquireNextTrigger() throws Exception {
        Date baseFireTimeDate = DateBuilder.evenMinuteDateAfterNow();
        long baseFireTime = baseFireTimeDate.getTime();
        
        OperableTrigger trigger1 = 
            new SimpleTriggerImpl("trigger1", "triggerGroup1", jobDetail.getKey().getName(), 
                    jobDetail.getKey().getGroup(), new Date(baseFireTime + 200000), 
                    new Date(baseFireTime + 200000), 2, 2000);
        OperableTrigger trigger2 = 
            new SimpleTriggerImpl("trigger2", "triggerGroup1", jobDetail.getKey().getName(), 
                    jobDetail.getKey().getGroup(), new Date(baseFireTime +  50000),
                    new Date(baseFireTime + 200000), 2, 2000);
        OperableTrigger trigger3 = 
            new SimpleTriggerImpl("trigger1", "triggerGroup2", jobDetail.getKey().getName(), 
                    jobDetail.getKey().getGroup(), new Date(baseFireTime + 100000), 
                    new Date(baseFireTime + 200000), 2, 2000);

        trigger1.computeFirstFireTime(null);
        trigger2.computeFirstFireTime(null);
        trigger3.computeFirstFireTime(null);
        jobStore.storeTrigger(trigger1, false);
        jobStore.storeTrigger(trigger2, false);
        jobStore.storeTrigger(trigger3, false);
        
        long firstFireTime = new Date(trigger1.getNextFireTime().getTime()).getTime();

        assertTrue(jobStore.acquireNextTriggers(10, 1, 0L).isEmpty());
        assertEquals(
            trigger2.getKey(), 
            jobStore.acquireNextTriggers(firstFireTime + 10000, 1, 0L).get(0).getKey());
        assertEquals(
            trigger3.getKey(), 
            jobStore.acquireNextTriggers(firstFireTime + 10000, 1, 0L).get(0).getKey());
        assertEquals(
            trigger1.getKey(), 
            jobStore.acquireNextTriggers(firstFireTime + 10000, 1, 0L).get(0).getKey());
        assertTrue(
            jobStore.acquireNextTriggers(firstFireTime + 10000, 1, 0L).isEmpty());


        // release trigger3
        jobStore.releaseAcquiredTrigger(trigger3);
        assertEquals(
            trigger3, 
            jobStore.acquireNextTriggers(new Date(trigger1.getNextFireTime().getTime()).getTime() + 10000, 1, 1L).get(0));
    }

    @Test
    public void testAcquireNextTriggerBatch() throws Exception {
        
        Date baseFireTimeDate = DateBuilder.evenMinuteDateAfterNow();
        long baseFireTime = baseFireTimeDate.getTime();
        
        OperableTrigger trigger1 =
            new SimpleTriggerImpl("trigger1", "triggerGroup1", jobDetail.getKey().getName(),
                    jobDetail.getKey().getGroup(), new Date(baseFireTime + 200000),
                    new Date(baseFireTime + 200005), 2, 2000);
        OperableTrigger trigger2 =
            new SimpleTriggerImpl("trigger2", "triggerGroup1", jobDetail.getKey().getName(),
                    jobDetail.getKey().getGroup(), new Date(baseFireTime + 200100),
                    new Date(baseFireTime + 200105), 2, 2000);
        OperableTrigger trigger3 =
            new SimpleTriggerImpl("trigger3", "triggerGroup1", jobDetail.getKey().getName(),
                    jobDetail.getKey().getGroup(), new Date(baseFireTime + 200200),
                    new Date(baseFireTime + 200205), 2, 2000);
        OperableTrigger trigger4 =
            new SimpleTriggerImpl("trigger4", "triggerGroup1", jobDetail.getKey().getName(),
                    jobDetail.getKey().getGroup(), new Date(baseFireTime + 200300),
                    new Date(baseFireTime + 200305), 2, 2000);

        OperableTrigger trigger10 =
            new SimpleTriggerImpl("trigger10", "triggerGroup2", jobDetail.getKey().getName(),
                    jobDetail.getKey().getGroup(), new Date(baseFireTime + 500000),
                    new Date(baseFireTime + 700000), 2, 2000);

        trigger1.computeFirstFireTime(null);
        trigger2.computeFirstFireTime(null);
        trigger3.computeFirstFireTime(null);
        trigger4.computeFirstFireTime(null);
        trigger10.computeFirstFireTime(null);
        jobStore.storeTrigger(trigger1, false);
        jobStore.storeTrigger(trigger2, false);
        jobStore.storeTrigger(trigger3, false);
        jobStore.storeTrigger(trigger4, false);
        jobStore.storeTrigger(trigger10, false);
        
        long firstFireTime = new Date(trigger1.getNextFireTime().getTime()).getTime();

        List<OperableTrigger> acquiredTriggers = jobStore.acquireNextTriggers(firstFireTime + 10000, 3, 1000L);
        assertEquals(3, acquiredTriggers.size());
        assertEquals(trigger1.getKey(), acquiredTriggers.get(0).getKey());
        assertEquals(trigger2.getKey(), acquiredTriggers.get(1).getKey());
        assertEquals(trigger3.getKey(), acquiredTriggers.get(2).getKey());
        jobStore.releaseAcquiredTrigger(trigger1);
        jobStore.releaseAcquiredTrigger(trigger2);
        jobStore.releaseAcquiredTrigger(trigger3);

        acquiredTriggers = jobStore.acquireNextTriggers(firstFireTime + 10000, 4, 1000L);
        assertEquals(4, acquiredTriggers.size());
        assertEquals(trigger1.getKey(), acquiredTriggers.get(0).getKey());
        assertEquals(trigger2.getKey(), acquiredTriggers.get(1).getKey());
        assertEquals(trigger3.getKey(), acquiredTriggers.get(2).getKey());
        assertEquals(trigger4.getKey(), acquiredTriggers.get(3).getKey());
        jobStore.releaseAcquiredTrigger(trigger1);
        jobStore.releaseAcquiredTrigger(trigger2);
        jobStore.releaseAcquiredTrigger(trigger3);
        jobStore.releaseAcquiredTrigger(trigger4);

        acquiredTriggers = jobStore.acquireNextTriggers(firstFireTime + 10000, 5, 1000L);
        assertEquals(4, acquiredTriggers.size());
        assertEquals(trigger1.getKey(), acquiredTriggers.get(0).getKey());
        assertEquals(trigger2.getKey(), acquiredTriggers.get(1).getKey());
        assertEquals(trigger3.getKey(), acquiredTriggers.get(2).getKey());
        assertEquals(trigger4.getKey(), acquiredTriggers.get(3).getKey());
        jobStore.releaseAcquiredTrigger(trigger1);
        jobStore.releaseAcquiredTrigger(trigger2);
        jobStore.releaseAcquiredTrigger(trigger3);
        jobStore.releaseAcquiredTrigger(trigger4);

        assertEquals(1, jobStore.acquireNextTriggers(firstFireTime + 1, 5, 0L).size());
        jobStore.releaseAcquiredTrigger(trigger1);

        assertEquals(2, jobStore.acquireNextTriggers(firstFireTime + 250, 5, 199L).size());
        jobStore.releaseAcquiredTrigger(trigger1);
        jobStore.releaseAcquiredTrigger(trigger2);
        
        assertEquals(1, jobStore.acquireNextTriggers(firstFireTime + 150, 5, 50L).size());
        jobStore.releaseAcquiredTrigger(trigger1);
        jobStore.releaseAcquiredTrigger(trigger2);
    }

    @Test
    public void testTriggerStates() throws Exception {
        OperableTrigger trigger = 
            new SimpleTriggerImpl("trigger1", "triggerGroup1", jobDetail.getKey().getName(), jobDetail.getKey().getGroup(), 
                    new Date(System.currentTimeMillis() + 100000), new Date(System.currentTimeMillis() + 200000), 2, 2000);
        trigger.computeFirstFireTime(null);
        assertEquals(TriggerState.NONE, jobStore.getTriggerState(trigger.getKey()));
        jobStore.storeTrigger(trigger, false);
        assertEquals(TriggerState.NORMAL, jobStore.getTriggerState(trigger.getKey()));
    
        jobStore.pauseTrigger(trigger.getKey());
        assertEquals(TriggerState.PAUSED, jobStore.getTriggerState(trigger.getKey()));
    
        jobStore.resumeTrigger(trigger.getKey());
        assertEquals(TriggerState.NORMAL, jobStore.getTriggerState(trigger.getKey()));
    
        trigger = jobStore.acquireNextTriggers(
                new Date(trigger.getNextFireTime().getTime()).getTime() + 10000, 1, 1L).get(0);
        assertNotNull(trigger);
        jobStore.releaseAcquiredTrigger(trigger);
        trigger=jobStore.acquireNextTriggers(
                new Date(trigger.getNextFireTime().getTime()).getTime() + 10000, 1, 1L).get(0);
        assertNotNull(trigger);
        assertTrue(jobStore.acquireNextTriggers(
                new Date(trigger.getNextFireTime().getTime()).getTime() + 10000, 1, 1L).isEmpty());
    }

    // See: http://jira.opensymphony.com/browse/QUARTZ-606
    @Test
    public void testStoreTriggerReplacesTrigger() throws Exception {

        String jobName = "StoreTriggerReplacesTrigger";
        String jobGroup = "StoreTriggerReplacesTriggerGroup";
        JobDetailImpl detail = new JobDetailImpl(jobName, jobGroup, NoOpJob.class);
        jobStore.storeJob(detail, false);
 
        String trName = "StoreTriggerReplacesTrigger";
        String trGroup = "StoreTriggerReplacesTriggerGroup";
        OperableTrigger tr = new SimpleTriggerImpl(trName ,trGroup, new Date());
        tr.setJobKey(new JobKey(jobName, jobGroup));
        tr.setCalendarName(null);
 
        jobStore.storeTrigger(tr, false);
        assertEquals(tr,jobStore.retrieveTrigger(tr.getKey()));
 
        try {
            jobStore.storeTrigger(tr, false);
            fail("an attempt to store duplicate trigger succeeded");
        } catch(ObjectAlreadyExistsException oaee) {
            // expected
        }

        tr.setCalendarName("QQ");
        jobStore.storeTrigger(tr, true); //fails here
        assertEquals(tr, jobStore.retrieveTrigger(tr.getKey()));
        assertEquals( "StoreJob doesn't replace triggers", "QQ", jobStore.retrieveTrigger(tr.getKey()).getCalendarName());
    }

    @Test
    public void testPauseJobGroupPausesNewJob() throws Exception
    {
        
        final String jobName1 = "PauseJobGroupPausesNewJob";
        final String jobName2 = "PauseJobGroupPausesNewJob2";
        final String jobGroup = "PauseJobGroupPausesNewJobGroup";
    
        JobDetailImpl detail = new JobDetailImpl(jobName1, jobGroup, NoOpJob.class);
        detail.setDurability(true);
        jobStore.storeJob(detail, false);
        jobStore.pauseJobs(GroupMatcher.jobGroupEquals(jobGroup));
    
        detail = new JobDetailImpl(jobName2, jobGroup, NoOpJob.class);
        detail.setDurability(true);
        jobStore.storeJob(detail, false);
    
        String trName = "PauseJobGroupPausesNewJobTrigger";
        String trGroup = "PauseJobGroupPausesNewJobTriggerGroup";
        OperableTrigger tr = new SimpleTriggerImpl(trName, trGroup, new Date());
        tr.setJobKey(new JobKey(jobName2, jobGroup));
        jobStore.storeTrigger(tr, false);
        assertEquals(TriggerState.PAUSED, jobStore.getTriggerState(tr.getKey()));
    }    

    @Test
    public void testStoreAndRetrieveJobs() throws Exception {
        // Store jobs.
        for (int i=0; i < 10; i++) {
            JobDetail job = JobBuilder.newJob(NoOpJob.class).withIdentity("job" + i).build();
            jobStore.storeJob(job, false);
        }
        // Retrieve jobs.
        for (int i=0; i < 10; i++) {
            JobKey jobKey = JobKey.jobKey("job" + i);
            JobDetail storedJob = jobStore.retrieveJob(jobKey);
            Assert.assertEquals(jobKey, storedJob.getKey());
        }
    }
    
    @Test
    public void testStoreAndRetriveTriggers() throws Exception {        
        // Store jobs and triggers.
        for (int i=0; i < 10; i++) {
            JobDetail job = JobBuilder.newJob(NoOpJob.class).withIdentity("job" + i).build();
            jobStore.storeJob(job, true);
            SimpleScheduleBuilder schedule = SimpleScheduleBuilder.simpleSchedule();
            Trigger trigger = TriggerBuilder.newTrigger().withIdentity("job" + i).withSchedule(schedule).forJob(job).build();
            jobStore.storeTrigger((OperableTrigger)trigger, true);
        }
        // Retrieve job and trigger.
        for (int i=0; i < 10; i++) {
            JobKey jobKey = JobKey.jobKey("job" + i);
            JobDetail storedJob = jobStore.retrieveJob(jobKey);
            Assert.assertEquals(jobKey, storedJob.getKey());
            
            TriggerKey triggerKey = TriggerKey.triggerKey("job" + i);
            Trigger storedTrigger = jobStore.retrieveTrigger(triggerKey);
            Assert.assertEquals(triggerKey, storedTrigger.getKey());
        }
    }
    
    @Test
    public void testAcquireTriggers() throws Exception {       
        // Setup: Store jobs and triggers.
        long MIN = 60 * 1000L;
        Date startTime0 = new Date(System.currentTimeMillis() + MIN); // a min from now.
        for (int i=0; i < 10; i++) {
            Date startTime = new Date(startTime0.getTime() + i * MIN); // a min apart
            JobDetail job = JobBuilder.newJob(NoOpJob.class).withIdentity("job" + i).build();
            SimpleScheduleBuilder schedule = SimpleScheduleBuilder.repeatMinutelyForever(2);
            OperableTrigger trigger = (OperableTrigger)TriggerBuilder.newTrigger().withIdentity("job" + i).withSchedule(schedule).forJob(job).startAt(startTime).build();
            
            // Manually trigger the first fire time computation that scheduler would do. Otherwise 
            // the store.acquireNextTriggers() will not work properly.
            Date fireTime = trigger.computeFirstFireTime(null);
            Assert.assertEquals(true, fireTime != null);
            
            jobStore.storeJobAndTrigger(job, trigger);
        }
        
        // Test acquire one trigger at a time
        for (int i=0; i < 10; i++) {
            long noLaterThan = (startTime0.getTime() + i * MIN);
            int maxCount = 1;
            long timeWindow = 0;
            List<OperableTrigger> triggers = jobStore.acquireNextTriggers(noLaterThan, maxCount, timeWindow);
            Assert.assertEquals(1, triggers.size());
            Assert.assertEquals("job" + i, triggers.get(0).getKey().getName());
            
            // Let's remove the trigger now.
            jobStore.removeJob(triggers.get(0).getJobKey());
        }
    }
    
    @Test
    public void testAcquireTriggersInBatch() throws Exception {
        // Setup: Store jobs and triggers.
        long MIN = 60 * 1000L;
        Date startTime0 = new Date(System.currentTimeMillis() + MIN); // a min from now.
        for (int i=0; i < 10; i++) {
            Date startTime = new Date(startTime0.getTime() + i * MIN); // a min apart
            JobDetail job = JobBuilder.newJob(NoOpJob.class).withIdentity("job" + i).build();
            SimpleScheduleBuilder schedule = SimpleScheduleBuilder.repeatMinutelyForever(2);
            OperableTrigger trigger = (OperableTrigger)TriggerBuilder.newTrigger().withIdentity("job" + i).withSchedule(schedule).forJob(job).startAt(startTime).build();
            
            // Manually trigger the first fire time computation that scheduler would do. Otherwise 
            // the store.acquireNextTriggers() will not work properly.
            Date fireTime = trigger.computeFirstFireTime(null);
            Assert.assertEquals(true, fireTime != null);
            
            jobStore.storeJobAndTrigger(job, trigger);
        }
        
        // Test acquire batch of triggers at a time
        long noLaterThan = startTime0.getTime() + 10 * MIN;
        int maxCount = 7;
        // time window needs to be big to be able to pick up multiple triggers when they are a minute apart
        long timeWindow = 8 * MIN; 
        List<OperableTrigger> triggers = jobStore.acquireNextTriggers(noLaterThan, maxCount, timeWindow);
        Assert.assertEquals(7, triggers.size());
        for (int i=0; i < 7; i++) {
            Assert.assertEquals("job" + i, triggers.get(i).getKey().getName());
        }
    }
    
    private static final class MutableJobDetail implements JobDetail {

        private static final long serialVersionUID = 7192922204710163161L;

        private JobKey key;
        
        private boolean concurrentExecution;
        
        
        public MutableJobDetail(String group, String name, boolean concurrentExecution) {
            super();
            key = new JobKey(group, name);
            this.concurrentExecution = concurrentExecution;
        }
        
        public MutableJobDetail(MutableJobDetail jobDetail) {
            super();
            key = jobDetail.getKey();
            concurrentExecution = !jobDetail.isConcurrentExectionDisallowed();
        }
        
        public void setKey(JobKey key) {
            this.key = key;
        }
        
        public void setConcurrentExecution(boolean concurrentExecution) {
            this.concurrentExecution = concurrentExecution;
        }

        @Override
        public JobKey getKey() {
            return key;
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public Class<? extends Job> getJobClass() {
            return null;
        }

        @Override
        public JobDataMap getJobDataMap() {
            return null;
        }

        @Override
        public boolean isDurable() {
            return false;
        }

        @Override
        public boolean isPersistJobDataAfterExecution() {
            return false;
        }

        @Override
        public boolean isConcurrentExectionDisallowed() {
            return !concurrentExecution;
        }

        @Override
        public boolean requestsRecovery() {
            return false;
        }

        @Override
        public JobBuilder getJobBuilder() {
            return null;
        }
        
        @Override
        public Object clone() {
            return new MutableJobDetail(this);
        }
    }
    
    private static final class TestableHazelcastJobStore extends HazelcastJobStore {
        
        private HazelcastInstance hazelcast = null;
        
        @Override
        public void shutdown() {
            Collection<Instance> instances = hazelcast.getInstances();
            for (Instance instance : instances) {
                instance.destroy();
            }
        }
        
        @Override
        protected HazelcastInstance getHazelcast() throws JobPersistenceException {
            if (hazelcast == null) {
                hazelcast = Hazelcast.getDefaultInstance();
            }
            
            return hazelcast;
        }
    }

}

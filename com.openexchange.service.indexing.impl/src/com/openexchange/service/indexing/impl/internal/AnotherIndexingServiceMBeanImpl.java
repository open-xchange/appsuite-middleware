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
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.management.MBeanException;
import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import org.apache.commons.logging.Log;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerListener;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.Trigger.CompletedExecutionInstruction;
import org.quartz.Trigger.TriggerState;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.TriggerListener;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.service.QuartzService;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import com.hazelcast.core.MultiMap;
import com.openexchange.service.indexing.IndexingServiceMBean;

/**
 * {@link AnotherIndexingServiceMBeanImpl}
 * 
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class AnotherIndexingServiceMBeanImpl extends StandardMBean implements IndexingServiceMBean, SchedulerListener, TriggerListener {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(AnotherIndexingServiceMBeanImpl.class);

    private final String monitoringMapName;
    
    public AnotherIndexingServiceMBeanImpl(String monitoringMapName) throws NotCompliantMBeanException {
        super(IndexingServiceMBean.class);
        this.monitoringMapName = monitoringMapName;
        try {
            QuartzService quartzService = Services.getService(QuartzService.class);
            Scheduler scheduler = quartzService.getScheduler(SchedulerConfig.getSchedulerName(), SchedulerConfig.start(), SchedulerConfig.getThreadCount());
            scheduler.getListenerManager().addSchedulerListener(this);
            scheduler.getListenerManager().addTriggerListener(this);
            
            /*
             * Clean stale jobs that may exist from a possible crash and schedule a consistency job
             */
            MultiMap<String, String> jobs = getClusterWideJobMap();
            String nodeKey = getNodeKey();
            jobs.remove(nodeKey);
            
            JobDetail consistencyJob = JobBuilder.newJob(MonitoringMapConsistencyJob.class)
                .withIdentity(MonitoringMapConsistencyJob.class.getName())
                .usingJobData(MonitoringMapConsistencyJob.JOB_MAP, monitoringMapName)
                .usingJobData(MonitoringMapConsistencyJob.NODE_NAME, getNodeKey())
                .build();
            
            Trigger consistencyTrigger = TriggerBuilder.newTrigger()
                .forJob(consistencyJob)
                .withIdentity(MonitoringMapConsistencyJob.class.getName())
                .withSchedule(SimpleScheduleBuilder.repeatHourlyForever().withMisfireHandlingInstructionFireNow())
                .build();
            
            try {
                scheduler.addJob(consistencyJob, true);
                scheduler.scheduleJob(consistencyTrigger);
            } catch (SchedulerException e) {
                LOG.warn("Could not schedule monitoring consistency job.", e);
            }
        } catch (Throwable t) {
            LOG.warn("Could not register scheduler listener. Monitoring will return incorrect values!", t);
        }
    }

    @Override
    public List<String> getAllLocalRunningJobs() throws MBeanException {
        QuartzService quartzService = Services.getService(QuartzService.class);
        try {
            Scheduler scheduler = quartzService.getScheduler(SchedulerConfig.getSchedulerName(), SchedulerConfig.start(), SchedulerConfig.getThreadCount());
            List<JobExecutionContext> jobs = scheduler.getCurrentlyExecutingJobs();
            List<String> jobNames = new ArrayList<String>();
            for (JobExecutionContext context : jobs) {
                JobKey key = context.getJobDetail().getKey();
                jobNames.add(key.toString());
            }

            return jobNames;
        } catch (Exception e) {
            LOG.info(e.getMessage(), e);
            throw new MBeanException(e);
        }
    }

    @Override
    public List<String> getLocalRunningJobs(int contextId, int userId) throws MBeanException {
        try {
            QuartzService quartzService = Services.getService(QuartzService.class);
            Scheduler scheduler = quartzService.getScheduler(SchedulerConfig.getSchedulerName(), SchedulerConfig.start(), SchedulerConfig.getThreadCount());
            List<String> names = new ArrayList<String>();
            List<JobExecutionContext> currentlyExecutingJobs = scheduler.getCurrentlyExecutingJobs();
            for (JobExecutionContext job : currentlyExecutingJobs) {
                JobKey key = job.getJobDetail().getKey();
                if (key.getGroup().equals(Tools.generateJobGroup(contextId, userId))) {
                    names.add(key.getName());
                }
            }

            return names;
        } catch (Exception e) {
            LOG.info(e.getMessage(), e);
            throw new MBeanException(e);
        }
    }

    @Override
    public List<String> getAllScheduledJobs() throws MBeanException {
        try {
            /*
             * We iterate only through all entries of which we know their cluster member.
             * This allows entries of removed cluster nodes to be evicted after some time.
             */
            List<String> jobNames = new ArrayList<String>();
            HazelcastInstance hazelcast = Services.getService(HazelcastInstance.class);
            Set<Member> members = hazelcast.getCluster().getMembers();
            MultiMap<String, String> jobs = getClusterWideJobMap();
            for (Member member : members) {
                String nodeName = Tools.resolveSocketAddress(member.getInetSocketAddress());
                Collection<String> forNode = jobs.get(nodeName);
                if (forNode != null && !forNode.isEmpty()) {
                    for (String job : forNode) {
                        jobNames.add(nodeName + ": " + job);
                    }
                }
            }

            return jobNames;
        } catch (Exception e) {
            LOG.info(e.getMessage(), e);
            throw new MBeanException(e);
        }
    }

    @Override
    public List<String> getLocalScheduledJobs() throws MBeanException {
        try {
            List<String> names = new ArrayList<String>();
            QuartzService quartzService = Services.getService(QuartzService.class);
            Scheduler scheduler = quartzService.getScheduler(SchedulerConfig.getSchedulerName(), SchedulerConfig.start(), SchedulerConfig.getThreadCount());
            List<String> groups = scheduler.getTriggerGroupNames();
            for (String group : groups) {
                Set<TriggerKey> triggerKeys = scheduler.getTriggerKeys(GroupMatcher.triggerGroupEquals(group));
                for (TriggerKey k : triggerKeys) {
                    names.add(k.toString());
                }
            }

            return names;
        } catch (Exception e) {
            LOG.info(e.getMessage(), e);
            throw new MBeanException(e);
        }
    }

    @Override
    public List<String> getScheduledJobs(int contextId, int userId) throws MBeanException {
        try {
            String triggerGroup = Tools.generateTriggerGroup(contextId, userId);
            List<String> allJobs = getAllScheduledJobs();
            List<String> jobNames = new ArrayList<String>();
            for (String job : allJobs) {
                if (job.contains(triggerGroup)) {
                    jobNames.add(job);
                }
            }

            return jobNames;
        } catch (Exception e) {
            LOG.info(e.getMessage(), e);
            throw new MBeanException(e);
        }
    }

    @Override
    public List<String> getTriggerStatesForJob(String jobGroup, String jobName) throws MBeanException {
        try {
            QuartzService quartzService = Services.getService(QuartzService.class);
            Scheduler scheduler = quartzService.getScheduler(SchedulerConfig.getSchedulerName(), SchedulerConfig.start(), SchedulerConfig.getThreadCount());
            List<String> states = new ArrayList<String>();
            List<? extends Trigger> triggersOfJob = scheduler.getTriggersOfJob(new JobKey(jobName, jobGroup));
            for (Trigger t : triggersOfJob) {
                TriggerKey key = t.getKey();
                Date nextFireTime = t.getNextFireTime();
                Date previousFireTime = t.getPreviousFireTime();
                Date startTime = t.getStartTime();
                TriggerState triggerState = scheduler.getTriggerState(key);
                states.add(key + " (start: " + startTime + ", previous: " + previousFireTime + ", next: " + nextFireTime + "): " + triggerState.toString());
            }

            return states;
        } catch (Exception e) {
            LOG.info(e.getMessage(), e);
            throw new MBeanException(e);
        }
    }

    /*
     * SchedulerListener Implementation
     */
    @Override
    public void jobScheduled(Trigger trigger) {
        try {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Trigger was added: " + trigger.getKey().toString());
            }

            MultiMap<String, String> jobs = getClusterWideJobMap();
            String nodeKey = getNodeKey();
            jobs.put(nodeKey, trigger.getKey().toString());
        } catch (Throwable t) {
            LOG.warn(t.getMessage(), t);
        }
    }

    @Override
    public void jobUnscheduled(TriggerKey triggerKey) {
        try {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Trigger was removed: " + triggerKey.toString());
            }

            MultiMap<String, String> jobs = getClusterWideJobMap();
            String nodeKey = getNodeKey();
            jobs.remove(nodeKey, triggerKey.toString());
        } catch (Throwable t) {
            LOG.warn(t.getMessage(), t);
        }
    }

    @Override
    public void triggerFinalized(Trigger trigger) {
        try {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Trigger finalized: " + trigger.getKey().toString());
            }

            MultiMap<String, String> jobs = getClusterWideJobMap();
            String nodeKey = getNodeKey();
            jobs.remove(nodeKey, trigger.getKey().toString());
        } catch (Throwable t) {
            LOG.warn(t.getMessage(), t);
        }
    }

    @Override
    public void triggerPaused(TriggerKey triggerKey) {}

    @Override
    public void triggersPaused(String triggerGroup) {}

    @Override
    public void triggerResumed(TriggerKey triggerKey) {}

    @Override
    public void triggersResumed(String triggerGroup) {}

    @Override
    public void jobAdded(JobDetail jobDetail) {}

    @Override
    public void jobDeleted(JobKey jobKey) {}

    @Override
    public void jobPaused(JobKey jobKey) {}

    @Override
    public void jobsPaused(String jobGroup) {}

    @Override
    public void jobResumed(JobKey jobKey) {}

    @Override
    public void jobsResumed(String jobGroup) {}

    @Override
    public void schedulerError(String msg, SchedulerException cause) {}

    @Override
    public void schedulerInStandbyMode() {}

    @Override
    public void schedulerStarted() {}

    @Override
    public void schedulerShutdown() {}

    @Override
    public void schedulerShuttingdown() {}

    @Override
    public void schedulingDataCleared() {}
    
    /*
     * TriggerListener Implementation
     */
    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public void triggerFired(Trigger trigger, JobExecutionContext context) {
        /*
         * We touch this nodes entry to prevent it from being evicted
         */
        String nodeKey = getNodeKey();
        MultiMap<String,String> jobs = getClusterWideJobMap();
        jobs.containsKey(nodeKey);
    }

    @Override
    public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
        return false;
    }

    @Override
    public void triggerMisfired(Trigger trigger) {}

    @Override
    public void triggerComplete(Trigger trigger, JobExecutionContext context, CompletedExecutionInstruction triggerInstructionCode) {}
    
    private MultiMap<String, String> getClusterWideJobMap() {
        HazelcastInstance hazelcast = Services.getService(HazelcastInstance.class);
        return hazelcast.getMultiMap(monitoringMapName);
    }

    private String getNodeKey() {
        HazelcastInstance hazelcast = Services.getService(HazelcastInstance.class);
        Member member = hazelcast.getCluster().getLocalMember();
        return Tools.resolveSocketAddress(member.getInetSocketAddress());
    }

}

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
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import javax.management.MBeanException;
import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import org.apache.commons.logging.Log;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerListener;
import org.quartz.Trigger;
import org.quartz.Trigger.TriggerState;
import org.quartz.TriggerKey;
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
public class AnotherIndexingServiceMBeanImpl extends StandardMBean implements IndexingServiceMBean, SchedulerListener {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(AnotherIndexingServiceMBeanImpl.class);

    public AnotherIndexingServiceMBeanImpl() throws NotCompliantMBeanException {
        super(IndexingServiceMBean.class);
        try {
            QuartzService quartzService = Services.getService(QuartzService.class);
            Scheduler scheduler = quartzService.getLocalScheduler();
            scheduler.getListenerManager().addSchedulerListener(this);
        } catch (Throwable t) {
            LOG.warn("Could not register scheduler listener. Monitoring will return incorrect values!", t);
        }
    }

    @Override
    public List<String> getAllLocalRunningJobs() throws MBeanException {
        QuartzService quartzService = Services.getService(QuartzService.class);
        try {
            Scheduler scheduler = quartzService.getLocalScheduler();
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
            Scheduler scheduler = quartzService.getLocalScheduler();
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
            List<String> jobNames = new ArrayList<String>();
            MultiMap<String, String> jobs = getClusterWideJobMap();
            for (Entry<String, String> entry : jobs.entrySet()) {
                jobNames.add(entry.getValue());
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
            Scheduler scheduler = quartzService.getLocalScheduler();
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
                if (job.startsWith(triggerGroup)) {
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
            Scheduler scheduler = quartzService.getLocalScheduler();
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
    public void triggerPaused(TriggerKey triggerKey) {
        // TODO Auto-generated method stub

    }

    @Override
    public void triggersPaused(String triggerGroup) {
        // TODO Auto-generated method stub

    }

    @Override
    public void triggerResumed(TriggerKey triggerKey) {
        // TODO Auto-generated method stub

    }

    @Override
    public void triggersResumed(String triggerGroup) {
        // TODO Auto-generated method stub

    }

    @Override
    public void jobAdded(JobDetail jobDetail) {
        // TODO Auto-generated method stub

    }

    @Override
    public void jobDeleted(JobKey jobKey) {
        // TODO Auto-generated method stub

    }

    @Override
    public void jobPaused(JobKey jobKey) {
        // TODO Auto-generated method stub

    }

    @Override
    public void jobsPaused(String jobGroup) {
        // TODO Auto-generated method stub

    }

    @Override
    public void jobResumed(JobKey jobKey) {
        // TODO Auto-generated method stub

    }

    @Override
    public void jobsResumed(String jobGroup) {
        // TODO Auto-generated method stub

    }

    @Override
    public void schedulerError(String msg, SchedulerException cause) {
        // TODO Auto-generated method stub

    }

    @Override
    public void schedulerInStandbyMode() {
        // TODO Auto-generated method stub

    }

    @Override
    public void schedulerStarted() {
        // TODO Auto-generated method stub

    }

    @Override
    public void schedulerShutdown() {
        // TODO Auto-generated method stub

    }

    @Override
    public void schedulerShuttingdown() {
        // TODO Auto-generated method stub

    }

    @Override
    public void schedulingDataCleared() {
        // TODO Auto-generated method stub

    }

    private MultiMap<String, String> getClusterWideJobMap() {
        HazelcastInstance hazelcast = Services.getService(HazelcastInstance.class);
        return hazelcast.getMultiMap("indexingServiceMonitoring-0");
    }

    private String getNodeKey() {
        HazelcastInstance hazelcast = Services.getService(HazelcastInstance.class);
        Member member = hazelcast.getCluster().getLocalMember();
        return Tools.resolveSocketAddress(member.getInetSocketAddress());
    }

}

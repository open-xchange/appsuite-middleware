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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.management.MBeanException;
import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import com.openexchange.service.indexing.JobMonitoringMBean;


/**
 * {@link JobMonitoringMBeanImpl}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class JobMonitoringMBeanImpl extends StandardMBean implements JobMonitoringMBean {
    
    private final Scheduler scheduler;

    /**
     * Initializes a new {@link JobMonitoringMBeanImpl}.
     * @param mbeanInterface
     * @throws NotCompliantMBeanException
     */
    public JobMonitoringMBeanImpl(Scheduler scheduler) throws NotCompliantMBeanException {
        super(JobMonitoringMBean.class);
        this.scheduler = scheduler;
    }
    
    @Override
    public int countStoredJobInfos() throws MBeanException {
        return RecurringJobsManager.getJobCount();
    }
    
    @Override
    public List<String> getStoredJobInfos() throws MBeanException {
        return RecurringJobsManager.getJobIds();
    }
    
    @Override
    public int countLocalTriggers() throws MBeanException {
        try {
            int count = 0;
            List<String> groups = scheduler.getTriggerGroupNames();
            for (String group : groups) {
                Set<TriggerKey> triggerKeys = scheduler.getTriggerKeys(GroupMatcher.triggerGroupEquals(group));
                count += triggerKeys.size();
            }
            return count;
        } catch (Exception e) {
            throw new MBeanException(e);
        }
    }
    
    @Override
    public List<String> getLocalTriggers() throws MBeanException {
        try {
            List<String> names = new ArrayList<String>();
            List<String> groups = scheduler.getTriggerGroupNames();
            for (String group : groups) {
                Set<TriggerKey> triggerKeys = scheduler.getTriggerKeys(GroupMatcher.triggerGroupEquals(group));
                for (TriggerKey k : triggerKeys) {
                    names.add(k.toString());
                }
            }
            return names;
        } catch (Exception e) {
            throw new MBeanException(e);
        }
    }
    
    @Override
    public int countRunningJobs() throws MBeanException {
        try {
            int count = scheduler.getCurrentlyExecutingJobs().size();
            return count;
        } catch (Exception e) {
            throw new MBeanException(e);
        }
    }
    
    @Override
    public Map<String, String> getRunningJobs() throws MBeanException {
        try {
            Map<String, String> names = new HashMap<String, String>();
            List<JobExecutionContext> executingJobs = scheduler.getCurrentlyExecutingJobs();
            for (JobExecutionContext context : executingJobs) {
                JobKey key = context.getJobDetail().getKey();
                TriggerKey triggerKey = context.getTrigger().getKey();
                names.put(triggerKey.toString(), key.toString());
            }
            
            return names;
        } catch (Exception e) {
            throw new MBeanException(e);
        }
    }
    
    @Override
    public String getRecurrenceDetails(String jobKey) throws MBeanException {
        JobInfoWrapper job = RecurringJobsManager.getJob(jobKey);
        if (job == null) {
            return null;
        }
        
        long lastUpdate = job.getLastUpdate();
        long lastRun = job.getLastRun();
        long interval = job.getInterval();
        StringBuilder sb = new StringBuilder(job.getJobInfo().toString())
            .append("\n    Last Update:      ")
            .append(new Date(lastUpdate))
            .append("\n    Timeout:          ")
            .append(new Date(lastUpdate + job.getJobTimeout()))
            .append("\n    Last Run:         ")
            .append(new Date(lastRun))
            .append("\n    Next Run:         ")
            .append(new Date(lastRun + interval))
            .append("\n    Interval:         ")
            .append(interval)
            .append("\n    Progression Rate: ")
            .append(job.getProgressionRate());
        
        return sb.toString();
    }

    @Override
    public String getTriggerDetails(String triggerName) throws MBeanException {
        try {
            int index = triggerName.indexOf('.');
            Trigger trigger = scheduler.getTrigger(new TriggerKey(triggerName.substring(index + 1, triggerName.length()), triggerName.substring(0, index)));
            if (trigger == null) {
                return null;
            }
            
            StringBuilder sb = new StringBuilder("Trigger: ")
                .append(triggerName)
                .append("\n    For Job:        ")
                .append(trigger.getJobKey().toString())
                .append("\n    Next Fire Time: ")
                .append(trigger.getNextFireTime());
            
            return sb.toString();
        } catch (Exception e) {
            throw new MBeanException(e);
        }
    }
}

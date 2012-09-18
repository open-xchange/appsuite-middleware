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
import com.openexchange.exception.OXException;
import com.openexchange.service.indexing.IndexingService;
import com.openexchange.service.indexing.JobInfo;


/**
 * {@link IndexingServiceImpl}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class IndexingServiceImpl implements IndexingService {
    
    private static final Log LOG = com.openexchange.log.Log.loggerFor(IndexingServiceImpl.class);
    

    @Override
    public void scheduleJob(JobInfo info, Date startDate, long repeatInterval, int priority) throws OXException {
        if (startDate == null) {
            startDate = new Date();
        }
        
        JobDataMap jobData = new JobDataMap();
        jobData.put(JobConstants.JOB_INFO, info);
        JobDetail jobDetail = JobBuilder.newJob(QuartzIndexingJob.class)
            .withIdentity(generateJobKey(info))
            .usingJobData(jobData)
            .build();        
        
        TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger()
            .forJob(jobDetail)
            .withIdentity(generateTriggerKey(info, startDate, repeatInterval))
            .startAt(startDate)
            .withPriority(priority);
        
        if (repeatInterval > 0L) {
            triggerBuilder.withSchedule(SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInMilliseconds(repeatInterval)
                .repeatForever()
                .withMisfireHandlingInstructionFireNow());            
        }        
          
        Trigger trigger = triggerBuilder.build(); 
        Scheduler scheduler = getScheduler();
        try {
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            if (e instanceof ObjectAlreadyExistsException) {
                LOG.debug("Job already exists within JobStore: " + info.toString() + ". Trying to store trigger only.");
                try {
                    scheduler.scheduleJob(trigger);
                } catch (SchedulerException f) {
                    LOG.warn("Could not schedule trigger.", f);
                }
            } else {
                throw new OXException(e);
            }                
        }
    }
    
    @Override
    public void unscheduleJob(JobInfo info) throws OXException {
        Scheduler scheduler = getScheduler();
        try {
            scheduler.deleteJob(generateJobKey(info));
        } catch (SchedulerException e) {
            throw new OXException(e);
        }
    }
    
    @Override
    public void unscheduleAllForUser(int contextId, int userId) throws OXException {
        Scheduler scheduler = getScheduler();
        String jobGroup = generateJobGroup(contextId, userId);        
        try {
            Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(jobGroup));
            scheduler.deleteJobs(new ArrayList<JobKey>(jobKeys));
        } catch (SchedulerException e) {
            throw new OXException(e);
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
            sb.append("oneShot/");
            sb.append(startDate.getTime());
        }
        
        return sb.toString();
    }

    Scheduler getScheduler() {
        QuartzService quartzService = Services.getService(QuartzService.class);
        Scheduler scheduler = quartzService.getClusteredScheduler();
        return scheduler;
    }
    
}

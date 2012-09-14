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

import java.util.Date;
import org.apache.commons.logging.Log;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.ObjectAlreadyExistsException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
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
        JobDataMap jobData = new JobDataMap();
        jobData.put(JobConstants.JOB_INFO, info);

        JobDetail jobDetail = JobBuilder.newJob(QuartzIndexingJob.class)
            .withIdentity(info.toUniqueId(), "indexingJobs/" + info.contextId + '/' + info.userId)
            .usingJobData(jobData)
            .build();
        
        TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger()
            .forJob(jobDetail)
            .withIdentity(info.toUniqueId(), "indexingTriggers/" + info.contextId + '/' + info.userId);
        
        if (startDate == null) {
            triggerBuilder.startNow();
        } else {
            triggerBuilder.startAt(startDate);
        }
        
        if (repeatInterval > 0) {
            triggerBuilder.withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInMilliseconds(repeatInterval));
        }

        triggerBuilder.withPriority(priority);
        Trigger trigger = triggerBuilder.build();        
        try {
            scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            if (e instanceof ObjectAlreadyExistsException) {
                LOG.debug("Job already exists within JobStore: " + info.toString());
            } else {
                throw new OXException(e);
            }                
        }
    }
    
    private void scheduleJob(JobDetail jobDetail, Trigger trigger) throws SchedulerException {
        QuartzService quartzService = Services.getService(QuartzService.class);
        Scheduler scheduler = quartzService.getClusteredScheduler();
        scheduler.scheduleJob(jobDetail, trigger);
    }

}

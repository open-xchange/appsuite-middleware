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

import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.Callable;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.TriggerBuilder;
import org.quartz.service.QuartzService;
import com.openexchange.exception.OXException;
import com.openexchange.service.indexing.JobInfo;
import com.openexchange.service.indexing.impl.internal.SchedulerConfig;
import com.openexchange.service.indexing.impl.internal.Services;
import com.openexchange.service.indexing.impl.internal.Tools;

/**
 * {@link ScheduleProgressiveRecurringJobCallable}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class ScheduleProgressiveRecurringJobCallable implements Callable<Object>, Serializable {

    private static final long serialVersionUID = 7614918425842919875L;

    private final JobInfo jobInfo;
    
    private final int priority;

    /**
     * Initializes a new {@link ScheduleProgressiveRecurringJobCallable}.
     * @param jobInfo
     * @param trigger
     */
    public ScheduleProgressiveRecurringJobCallable(JobInfo jobInfo, int priority) {
        this.jobInfo = jobInfo;
        this.priority = priority;
    }

    @Override
    public Object call() throws Exception {
        JobDetail jobDetail = JobBuilder.newJob(ProgressiveRecurringJob.class)
            .withIdentity(Tools.generateJobKey(jobInfo))
            .build();
        
        String triggerName = jobInfo.toUniqueId() + "/withProgressiveInterval";
        SimpleTrigger newTrigger = TriggerBuilder.newTrigger()
            .forJob(jobDetail.getKey())
            .withIdentity(triggerName, Tools.generateTriggerGroup(jobInfo.contextId, jobInfo.userId))
            .startAt(new Date())
            .withPriority(priority)
            .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
            .build();
        
        QuartzService quartzService = Services.getService(QuartzService.class);
        Scheduler scheduler = quartzService.getScheduler(SchedulerConfig.getSchedulerName(), SchedulerConfig.start(), SchedulerConfig.getThreadCount());
        try {
            scheduler.addJob(jobDetail, true);
            if (scheduler.checkExists(newTrigger.getKey())) {
                scheduler.rescheduleJob(newTrigger.getKey(), newTrigger);
            } else {
                scheduler.scheduleJob(newTrigger);
            }
        } catch (SchedulerException e) {
            throw new OXException(e);
        }
        
        return null;
    }
}
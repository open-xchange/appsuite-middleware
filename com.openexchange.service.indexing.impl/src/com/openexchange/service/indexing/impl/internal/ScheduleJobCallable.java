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

import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.Callable;
import org.apache.commons.logging.Log;
import org.quartz.JobDetail;
import org.quartz.ObjectAlreadyExistsException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.service.QuartzService;
import com.openexchange.service.indexing.JobInfo;


/**
 * {@link ScheduleJobCallable}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class ScheduleJobCallable implements Callable<Object>, Serializable {
    
    private static final Log LOG = com.openexchange.log.Log.loggerFor(ScheduleJobCallable.class);

    private static final long serialVersionUID = 5900667348491833307L;
    
    private final JobInfo jobInfo;

    private final Date startDate;

    private final long interval;

    private final int priority;
    
    public ScheduleJobCallable(JobInfo jobInfo, Date startDate, long interval, int priority) {
        super();
        this.jobInfo = jobInfo;
        this.startDate = startDate;
        this.interval = interval;
        this.priority = priority;
    }

    @Override
    public Object call() throws Exception {
        if (LOG.isDebugEnabled()) {
            if (LOG.isTraceEnabled()) {
                Exception exception = new Exception();
                LOG.trace("Scheduling job: " + jobInfo.toString() + ".", exception);
            } else {
                LOG.debug("Scheduling job: " + jobInfo.toString() + ".");
            }
        }
        
        try {
            JobDetail jobDetail = Tools.buildJob(jobInfo, RunOrRescheduleAtTargetJob.class);
            Trigger targetTrigger = Tools.buildTrigger(jobDetail.getKey(), jobInfo, startDate, interval, priority);
            QuartzService quartzService = Services.getService(QuartzService.class);
            Scheduler scheduler = quartzService.getScheduler(SchedulerConfig.getSchedulerName(), SchedulerConfig.start(), SchedulerConfig.getThreadCount());
            scheduler.addJob(jobDetail, true);
            try {
                scheduler.scheduleJob(targetTrigger);
            } catch (SchedulerException e) {
                if (e instanceof ObjectAlreadyExistsException) {
                    LOG.info("Could not schedule trigger " + targetTrigger.getKey() + ". It already exists.");
                } else {
                    throw e;
                }
            }
        } catch (Throwable t) {
            LOG.error(t.getMessage(), t);
        }
        return null;
    }

}

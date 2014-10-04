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

package com.openexchange.service.indexing.impl.internal.nonclustered;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.service.QuartzService;
import com.openexchange.osgi.ExceptionUtils;
import com.openexchange.service.indexing.impl.internal.SchedulerConfig;
import com.openexchange.service.indexing.impl.internal.Services;
import com.openexchange.service.indexing.impl.internal.Tools;


/**
 * {@link UnscheduleAllJobsRunnable}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class UnscheduleAllJobsRunnable implements Runnable, Serializable {

    private static final long serialVersionUID = -3020268885605197578L;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(UnscheduleAllJobsRunnable.class);

    private final int contextId;

    private final int userId;

    public UnscheduleAllJobsRunnable(int contextId, int userId) {
        super();
        this.contextId = contextId;
        this.userId = userId;
    }

    @Override
    public void run() {
        try {
            QuartzService quartzService = Services.getService(QuartzService.class);
            Scheduler scheduler = quartzService.getScheduler(SchedulerConfig.getSchedulerName(), SchedulerConfig.start(), SchedulerConfig.getThreadCount());
            if (contextId > 0 && userId > 0) {
                if (userId > 0) {
                    String jobGroup = Tools.generateJobGroup(contextId, userId);
                    Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(jobGroup));
                    int count = 0;
                    for (JobKey jobKey : jobKeys) {
                        List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
                        count += triggers.size();
                        for (Trigger trigger : triggers) {
                            scheduler.unscheduleJob(trigger.getKey());
                        }
                    }

                    LOG.debug("Unscheduled {} triggers for {} jobs for user {} in context {}.", count, jobKeys.size(), userId, contextId);
                } else {
                    String jobGroup = Tools.generateJobGroup(contextId);
                    Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.jobGroupStartsWith(jobGroup));
                    int count = 0;
                    for (JobKey jobKey : jobKeys) {
                        List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
                        count += triggers.size();
                        for (Trigger trigger : triggers) {
                            scheduler.unscheduleJob(trigger.getKey());
                        }
                    }

                    LOG.debug("Unscheduled {} triggers for {} jobs for user {} in context {}.", count, jobKeys.size(), userId, contextId);
                }
            }
        } catch (Throwable t) {
            ExceptionUtils.handleThrowable(t);
            LOG.error("", t);
        }
    }

}

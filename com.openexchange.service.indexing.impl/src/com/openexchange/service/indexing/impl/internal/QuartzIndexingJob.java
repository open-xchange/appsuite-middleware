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

import org.apache.commons.logging.Log;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import com.openexchange.service.indexing.IndexingJob;
import com.openexchange.service.indexing.JobInfo;


/**
 * {@link QuartzIndexingJob}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class QuartzIndexingJob implements Job {
    
    private static final Log LOG = com.openexchange.log.Log.loggerFor(QuartzIndexingJob.class);
    
    
    public QuartzIndexingJob() {
        super();
    }    

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobData = context.getMergedJobDataMap();
        Object infoObject = jobData.get(JobConstants.JOB_INFO);
        if (infoObject == null || !(infoObject instanceof JobInfo)) {
            String msg = "JobDataMap did not contain valid JobInfo instance.";
            LOG.error(msg);
            throw new JobExecutionException(msg, false);
        }
        
        JobInfo jobInfo = (JobInfo) infoObject;   
        Class<? extends IndexingJob> jobClass = jobInfo.jobClass;
        if (jobClass == null) {
            String msg = "JobInfo did not contain valid job class. " + jobInfo.toString();
            LOG.error(msg);
            throw new JobExecutionException(msg, false);
        }
            
        IndexingJob indexingJob;
        try {
            indexingJob = jobClass.newInstance();
        } catch (Throwable t) {
            String msg = "Could not instantiate IndexingJob from class object. " + jobInfo.toString();
            LOG.error(msg, t);
            throw new JobExecutionException(msg, t, false);
        }
        
        try {
            indexingJob.execute(jobInfo);
        } catch (Throwable t) {
            String msg = "Error during IndexingJob execution. " + jobInfo.toString();
            LOG.error(msg, t);
            throw new JobExecutionException(msg, t, false);
        }
    }

}

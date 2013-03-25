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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MultiMap;
import com.openexchange.service.indexing.impl.internal.Services;


/**
 * {@link MonitoringMapConsistencyJob}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class MonitoringMapConsistencyJob implements Job {
    
    private static final Log LOG = com.openexchange.log.Log.loggerFor(MonitoringMapConsistencyJob.class);
    
    public static final String NODE_NAME = "nodeName";
    
    public static final String JOB_MAP = "jobMap";

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Started run of MonitoringMapConsistencyJob");
            }
            
            JobDataMap data = context.getMergedJobDataMap();
            String nodeName = data.getString(NODE_NAME);
            String jobMap = data.getString(JOB_MAP);
            
            Scheduler scheduler = context.getScheduler();
            Set<String> localJobs = new HashSet<String>();
            List<String> groups = scheduler.getTriggerGroupNames();
            for (String group : groups) {
                Set<TriggerKey> triggerKeys = scheduler.getTriggerKeys(GroupMatcher.triggerGroupEquals(group));
                for (TriggerKey k : triggerKeys) {
                    localJobs.add(k.toString());
                }
            }
            
            HazelcastInstance hazelcast = Services.getService(HazelcastInstance.class);
            MultiMap<String, String> monitoredJobs = hazelcast.getMultiMap(jobMap);
            Collection<String> collection = monitoredJobs.get(nodeName);
            int removed = 0;
            if (collection != null && !collection.isEmpty()) {
                Set<String> clusteredJobs = new HashSet<String>(collection);
                clusteredJobs.remove(localJobs);
                removed = clusteredJobs.size();
                for (String job : clusteredJobs) {
                    monitoredJobs.remove(nodeName, job);
                }
            }
            
            for (String job : localJobs) {
                monitoredJobs.put(nodeName, job);
            }
            
            if (LOG.isDebugEnabled()) {
                LOG.debug("Removed " + removed + " jobs from monitoring map.");
            }
        } catch (Throwable t) {
            LOG.warn("MonitoringMapConsistencyJob failed.", t);
        }
    }

}

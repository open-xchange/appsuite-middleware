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
import java.net.InetSocketAddress;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import org.apache.commons.logging.Log;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import com.hazelcast.core.DistributedTask;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;
import com.openexchange.exception.OXException;
import com.openexchange.index.IndexAccess;
import com.openexchange.index.IndexFacadeService;
import com.openexchange.index.QueryParameters;
import com.openexchange.index.SearchHandler;
import com.openexchange.service.indexing.IndexingJob;
import com.openexchange.service.indexing.JobInfo;
import com.openexchange.solr.SolrCoreIdentifier;


/**
 * {@link QuartzIndexingJob}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
@DisallowConcurrentExecution
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
        if (LOG.isDebugEnabled()) {
            LOG.debug("Started execution of job " + jobInfo.toString() + ". Trigger: " + context.getTrigger().getKey());
        }
        
        try {
            submitCallable(jobInfo);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new JobExecutionException(e);
        }
    }
    
    protected void submitCallable(JobInfo jobInfo) throws Exception {
        SolrCoreIdentifier identifier = new SolrCoreIdentifier(jobInfo.contextId, jobInfo.userId, jobInfo.getModule());
        HazelcastInstance hazelcast = Services.getService(HazelcastInstance.class);
        // FIXME: This core handling stuff has to be centralized and hidden by a transparent layer.
        IMap<String, String> solrCores = hazelcast.getMap("solrCoreMap");            
        String owner = solrCores.get(identifier.toString());
        if (owner == null) {
            startUpIndex(jobInfo);
            owner = solrCores.get(identifier.toString());
        }
        
        if (owner == null) {
            LOG.error("Did not find a node holding this index.");
            // TODO: exception
            return;
        }
        
        Member executor = null;            
        for (Member member : hazelcast.getCluster().getMembers()) {
            if (owner.equals(resolveSocketAddress(member.getInetSocketAddress()))) {
                executor = member;
                break;
            }
        }
        
        if (executor == null) {
            LOG.error("Could not find a member to execute this job.");
        } else {
            Callable<Object> callable = new IndexingJobCallable(jobInfo);
            if (executor.equals(hazelcast.getCluster().getLocalMember())) {
                callable.call();
            } else {
                FutureTask<Object> task = new DistributedTask<Object>(callable, executor);
                ExecutorService executorService = hazelcast.getExecutorService();
                executorService.submit(task);
                task.get();
            }           
        }
    }

    private void startUpIndex(JobInfo jobInfo) throws OXException {
        IndexFacadeService indexFacade = Services.getService(IndexFacadeService.class);
        IndexAccess<?> indexAccess = indexFacade.acquireIndexAccess(jobInfo.getModule(), jobInfo.userId, jobInfo.contextId);
        QueryParameters queryParameters = new QueryParameters.Builder("Something").setLength(0).setHandler(SearchHandler.SIMPLE).build();
        indexAccess.query(queryParameters, null);
        indexFacade.releaseIndexAccess(indexAccess);
    }
    
    private String resolveSocketAddress(InetSocketAddress addr) {
        if (addr.isUnresolved()) {
            return addr.getHostName();
        } else {
            return addr.getAddress().getHostAddress();
        }
    }
    
    public static final class IndexingJobCallable implements Callable<Object>, Serializable {

        private static final long serialVersionUID = -4925442107711497341L;
        
        private final JobInfo jobInfo;
        
        
        public IndexingJobCallable(final JobInfo jobInfo) {
            super();
            this.jobInfo = jobInfo;
        }

        @Override
        public Object call() throws Exception {
            // TODO: define exception or proper return type            
            Class<? extends IndexingJob> jobClass = jobInfo.jobClass;
            if (jobClass == null) {
                String msg = "JobInfo did not contain valid job class. " + jobInfo.toString();
                LOG.error(msg);
                return null;
//                throw new JobExecutionException(msg, false);
            }
            
            
            IndexingJob indexingJob;
            try {
                indexingJob = jobClass.newInstance();
            } catch (Throwable t) {
                String msg = "Could not instantiate IndexingJob from class object. " + jobInfo.toString();
                LOG.error(msg, t);
//                throw new JobExecutionException(msg, t, false);
                return null;
            }
            
            try {
                indexingJob.execute(jobInfo);
            } catch (Throwable t) {
                String msg = "Error during IndexingJob execution. " + jobInfo.toString();
                LOG.error(msg, t);
                return null;
//                throw new JobExecutionException(msg, t, false);
            }
            
            return null;
        }
        
    }

}

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

package com.openexchange.service.indexing.impl;

import java.net.InetSocketAddress;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import org.apache.commons.logging.Log;
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
import com.openexchange.service.indexing.impl.internal.Services;
import com.openexchange.solr.SolrCoreIdentifier;


/**
 * {@link AbstractIndexingJob}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public abstract class AbstractIndexingJob implements IndexingJob {
    
    private static final Log LOG = com.openexchange.log.Log.loggerFor(AbstractIndexingJob.class);
    
    
    protected void submitCallable(int module, JobInfo jobInfo, Callable<Object> callable) throws Exception {
        SolrCoreIdentifier identifier = new SolrCoreIdentifier(jobInfo.contextId, jobInfo.userId, module);
        HazelcastInstance hazelcast = Services.getService(HazelcastInstance.class);
        // FIXME: This core handling stuff has to be centralized and hidden by a transparent layer.
        IMap<String, String> solrCores = hazelcast.getMap("solrCoreMap");            
        String owner = solrCores.get(identifier.toString());
        if (owner == null) {
            startUpIndex(module, jobInfo);
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
        } else if (executor.equals(hazelcast.getCluster().getLocalMember())) {
            callable.call();
        } else {
            FutureTask<Object> task = new DistributedTask<Object>(callable, executor);
            ExecutorService executorService = hazelcast.getExecutorService();
            executorService.submit(task);
            task.get();
        }
    }

    private void startUpIndex(int module, JobInfo jobInfo) throws OXException {
        IndexFacadeService indexFacade = Services.getService(IndexFacadeService.class);
        IndexAccess<?> indexAccess = indexFacade.acquireIndexAccess(module, jobInfo.userId, jobInfo.contextId);
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

}

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

package com.openexchange.service.indexing.mail;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import com.hazelcast.core.DistributedTask;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.index.IndexAccess;
import com.openexchange.index.IndexFacadeService;
import com.openexchange.index.QueryParameters;
import com.openexchange.index.SearchHandler;
import com.openexchange.index.mail.MailIndexField;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.service.indexing.IndexingJob;
import com.openexchange.service.indexing.JobInfo;
import com.openexchange.service.indexing.internal.Services;
import com.openexchange.service.indexing.internal.mail.MailFolderCallable;
import com.openexchange.solr.SolrCoreIdentifier;


/**
 * {@link MailFolderJob}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class MailFolderJob implements IndexingJob {
    
    public MailFolderJob() {
        super();
    }

    @Override
    public void execute(JobInfo jobInfo) throws OXException {
        try {
            if (!(jobInfo instanceof MailJobInfo)) {
                throw new IllegalArgumentException("Job info must be an instance of MailJobInfo.");
            }

            MailJobInfo mailJobInfo = (MailJobInfo) jobInfo;
            IndexFacadeService indexFacade = Services.getService(IndexFacadeService.class);
            IndexAccess<MailMessage> indexAccess = indexFacade.acquireIndexAccess(Types.EMAIL, jobInfo.userId, jobInfo.contextId);
            QueryParameters queryParameters = new QueryParameters.Builder("Something").setLength(0).setHandler(SearchHandler.SIMPLE).build();
            indexAccess.query(queryParameters, Collections.singleton(MailIndexField.ID));
            indexFacade.releaseIndexAccess(indexAccess);
            
            SolrCoreIdentifier identifier = new SolrCoreIdentifier(jobInfo.contextId, jobInfo.userId, Types.EMAIL);
            HazelcastInstance hazelcast = Services.getService(HazelcastInstance.class);
            // FIXME: This core handling stuff has to be centralized and hidden by a transparent layer.
            IMap<String, String> solrCores = hazelcast.getMap("solrCoreMap");            
            String owner = solrCores.get(identifier.toString());
            if (owner == null) {
//                throw new OXException("Index " + identifier.toString() + " was not active.");
            }
            
            Member executor = null;            
            for (Member member : hazelcast.getCluster().getMembers()) {
                if (owner.equals(resolveSocketAddress(member.getInetSocketAddress()))) {
                    executor = member;
                    break;
                }
            }
            
            Callable<Object> mailFolderCallable = new MailFolderCallable(mailJobInfo);
            if (executor == null) {
                //TODO:exception
            } else if (executor.equals(hazelcast.getCluster().getLocalMember())) {
                mailFolderCallable.call();
            } else {
                FutureTask<Object> task = new DistributedTask<Object>(mailFolderCallable, executor);
                ExecutorService executorService = hazelcast.getExecutorService();
                executorService.submit(task);
                task.get();
            }
        } catch (Exception e) {
            throw new OXException(e);
        }
    }
    
    private String resolveSocketAddress(InetSocketAddress addr) {
        if (addr.isUnresolved()) {
            return addr.getHostName();
        } else {
            return addr.getAddress().getHostAddress();
        }
    }

}

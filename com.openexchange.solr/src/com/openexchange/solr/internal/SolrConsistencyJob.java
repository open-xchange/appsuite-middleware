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

package com.openexchange.solr.internal;

import java.util.HashSet;
import java.util.Set;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;
import com.hazelcast.query.SqlPredicate;
import com.openexchange.solr.SolrAccessService;
import com.openexchange.solr.SolrCoreIdentifier;


/**
 * {@link SolrConsistencyJob}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
@DisallowConcurrentExecution
public class SolrConsistencyJob implements Job {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SolrConsistencyJob.class);

    public SolrConsistencyJob() {
        super();
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            DelegationSolrAccessImpl accessService = (DelegationSolrAccessImpl) Services.getService(SolrAccessService.class);
            EmbeddedSolrAccessImpl embeddedAccess = accessService.getEmbeddedServerAccess();
            HazelcastInstance hazelcast = Services.getService(HazelcastInstance.class);

            Member member = hazelcast.getCluster().getLocalMember();
            String host = SolrCoreTools.resolveSocketAddress(member.getInetSocketAddress());
            SqlPredicate predicate = new SqlPredicate("this = " + host);
            IMap<String, String> solrCores = hazelcast.getMap(SolrCoreTools.SOLR_CORE_MAP);
            Set<String> coresInHazelcast = solrCores.keySet(predicate);
            Set<String> activeCores = new HashSet<String>(embeddedAccess.getActiveCores());
            if (activeCores.removeAll(coresInHazelcast)) {
                /*
                 * Shutdown cores that can not be reached anymore
                 */
                for (String core : activeCores) {
                    embeddedAccess.freeResources(new SolrCoreIdentifier(core));
                }
            }

            String hostAddress = member.getInetSocketAddress().getAddress().getHostAddress();
            IMap<String, Integer> solrNodes = hazelcast.getMap(SolrCoreTools.SOLR_NODE_MAP);
            solrNodes.put(hostAddress, coresInHazelcast.size());
        } catch (Throwable t) {
            LOG.warn("Error during consistency job.", t);
        }
    }

}

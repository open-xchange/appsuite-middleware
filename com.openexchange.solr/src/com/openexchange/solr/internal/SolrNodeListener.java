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

import java.util.Set;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;
import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;
import com.hazelcast.query.SqlPredicate;


/**
 * {@link SolrNodeListener}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class SolrNodeListener implements MembershipListener {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SolrNodeListener.class);

    private final HazelcastInstance hazelcast;


    public SolrNodeListener(HazelcastInstance hazelcast) {
        super();
        this.hazelcast = hazelcast;
    }

    @Override
    public void memberAdded(MembershipEvent membershipEvent) {
        // nothing to do
    }

    @Override
    public void memberRemoved(MembershipEvent membershipEvent) {
        Member member = membershipEvent.getMember();
        String hostAddress = member.getSocketAddress().getAddress().getHostAddress();
        IMap<String, Integer> solrNodes = hazelcast.getMap(SolrCoreTools.SOLR_NODE_MAP);
        solrNodes.remove(hostAddress);

        String host = SolrCoreTools.resolveSocketAddress(member.getSocketAddress());
        SqlPredicate predicate = new SqlPredicate("this = " + host);
        IMap<String, String> solrCores = hazelcast.getMap(SolrCoreTools.SOLR_CORE_MAP);
        if (LOG.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder("Solr cores:\n");
            for (String key : solrCores.keySet()) {
                sb.append("    ").append(key).append(": ").append(solrCores.get(key));
            }
            LOG.debug(sb.toString());
        }
        Set<String> cores = solrCores.keySet(predicate);
        for (String core : cores) {
            solrCores.remove(core, host);
        }
    }

    @Override
    public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {
        // nothing to do
    }

}

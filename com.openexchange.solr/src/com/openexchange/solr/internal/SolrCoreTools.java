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

import java.net.InetSocketAddress;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;


/**
 * {@link SolrCoreTools}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class SolrCoreTools {

    public static final String SOLR_NODE_MAP = "solrNodeMap";

    public static final String SOLR_CORE_MAP = "solrCoreMap";

    public static void incrementCoreCount(HazelcastInstance hazelcast, Member member) {
        IMap<String, Integer> solrNodes = hazelcast.getMap(SOLR_NODE_MAP);
        String localAddress = member.getInetSocketAddress().getAddress().getHostAddress();
        solrNodes.lock(localAddress);
        try {
            Integer integer = solrNodes.get(localAddress);
            solrNodes.put(localAddress, Integer.valueOf(null == integer ? 1 : integer.intValue() + 1));
        } finally {
            solrNodes.unlock(localAddress);
        }
    }

    public static void decrementCoreCount(HazelcastInstance hazelcast, Member member) {
        IMap<String, Integer> solrNodes = hazelcast.getMap(SOLR_NODE_MAP);
        String localAddress = member.getInetSocketAddress().getAddress().getHostAddress();
        solrNodes.lock(localAddress);
        try {
            Integer integer = solrNodes.get(localAddress);
            if (null != integer) {
                solrNodes.put(localAddress, Integer.valueOf(integer.intValue() - 1));
            }
        } finally {
            solrNodes.unlock(localAddress);
        }
    }

    public static String resolveSocketAddress(InetSocketAddress addr) {
        return addr.isUnresolved() ? addr.getHostName() : addr.getAddress().getHostAddress();
    }

}

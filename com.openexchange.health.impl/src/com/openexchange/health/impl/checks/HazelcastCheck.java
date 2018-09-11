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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.health.impl.checks;

import java.util.HashMap;
import java.util.Map;
import com.hazelcast.cluster.ClusterState;
import com.hazelcast.core.Cluster;
import com.hazelcast.core.HazelcastInstance;
import com.openexchange.health.NodeHealthCheck;
import com.openexchange.health.NodeHealthCheckResponse;
import com.openexchange.health.impl.NodeHealthCheckResponseImpl;
import com.openexchange.server.ServiceLookup;


/**
 * {@link HazelcastCheck}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.1
 */
public class HazelcastCheck implements NodeHealthCheck {

    private final static String NAME = "hazelcast";

    private final ServiceLookup services;

    public HazelcastCheck(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public NodeHealthCheckResponse call() {
        HazelcastInstance hzInstance = services.getOptionalService(HazelcastInstance.class);
        if (null == hzInstance) {
            return new NodeHealthCheckResponseImpl(NAME, null, true);
        }
        boolean status = true;
        Map<String, Object> data = new HashMap<>(5);
        Cluster cluster = hzInstance.getCluster();
        if (null != cluster) {
            data.put("memberCount", String.valueOf(cluster.getMembers().size()));
            ClusterState clusterState = cluster.getClusterState();
            if (!ClusterState.ACTIVE.equals(clusterState)) {
                status = false;
            }
            data.put("clusterState", clusterState.name());
            data.put("clusterVersion", cluster.getClusterVersion().toString());
            data.put("memberVersion", cluster.getLocalMember().getVersion().toString());
            data.put("isLiteMember", String.valueOf(cluster.getLocalMember().isLiteMember()));
        }

        return new NodeHealthCheckResponseImpl(NAME, data, status);
    }

}

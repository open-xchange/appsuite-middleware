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

package com.openexchange.health.check;

import com.hazelcast.cluster.ClusterState;
import com.hazelcast.core.Cluster;
import com.hazelcast.core.HazelcastInstance;
import com.openexchange.health.DefaultHealthCheck;
import com.openexchange.health.DefaultHealthCheckResponse;
import com.openexchange.health.DefaultHealthCheckResponseBuilder;
import com.openexchange.server.ServiceLookup;


/**
 * {@link HazelcastHealthCheck}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.1
 */
public class HazelcastHealthCheck extends DefaultHealthCheck {

    private final static String NAME = "hazelcast";

    private final ServiceLookup services;

    public HazelcastHealthCheck(ServiceLookup services) {
        super(NAME);
        this.services = services;
    }

    @Override
    public DefaultHealthCheckResponse execute() {
        DefaultHealthCheckResponseBuilder builder = new DefaultHealthCheckResponseBuilder();
        builder.name(NAME);
        HazelcastInstance hzInstance = services.getOptionalService(HazelcastInstance.class);
        if (null == hzInstance) {
            return builder.up().build();
        }
        boolean status = true;
        Cluster cluster = hzInstance.getCluster();
        if (null != cluster) {
            builder.withData("memberCount", cluster.getMembers().size());
            ClusterState clusterState = cluster.getClusterState();
            if (!ClusterState.ACTIVE.equals(clusterState)) {
                status = false;
            }
            builder.withData("clusterState", clusterState.name());
            builder.withData("clusterVersion", cluster.getClusterVersion().toString());
            builder.withData("memberVersion", cluster.getLocalMember().getVersion().toString());
            builder.withData("isLiteMember", cluster.getLocalMember().isLiteMember());
        }
        builder.state(status);
        return builder.build();
    }

}

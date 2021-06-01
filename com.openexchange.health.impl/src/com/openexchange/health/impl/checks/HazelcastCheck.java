/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.health.impl.checks;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import com.hazelcast.cluster.Cluster;
import com.hazelcast.cluster.ClusterState;
import com.hazelcast.core.HazelcastInstance;
import com.openexchange.health.AbstractCachingMWHealthCheck;
import com.openexchange.health.MWHealthCheckResponse;
import com.openexchange.health.impl.MWHealthCheckResponseImpl;
import com.openexchange.server.ServiceLookup;


/**
 * {@link HazelcastCheck}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.1
 */
public class HazelcastCheck extends AbstractCachingMWHealthCheck {

    private final static String NAME = "hazelcast";
    private final static long TIMEOUT = 15000L;

    private final ServiceLookup services;

    public HazelcastCheck(ServiceLookup services) {
        super(5000);
        this.services = services;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public long getTimeout() {
        return TIMEOUT;
    }

    @Override
    protected MWHealthCheckResponse doCall() {
        HazelcastInstance hzInstance = services.getOptionalService(HazelcastInstance.class);
        if (null == hzInstance) {
            return new MWHealthCheckResponseImpl(NAME, null, true);
        }

        boolean status = true;
        Cluster cluster = hzInstance.getCluster();
        if (null == cluster) {
            return new MWHealthCheckResponseImpl(NAME, Collections.emptyMap(), status);
        }

        Map<String, Object> data = new HashMap<>(5);
        data.put("memberCount", String.valueOf(cluster.getMembers().size()));
        ClusterState clusterState = cluster.getClusterState();
        if (!ClusterState.ACTIVE.equals(clusterState)) {
            status = false;
        }
        data.put("clusterState", clusterState.name());
        data.put("clusterVersion", cluster.getClusterVersion().toString());
        data.put("memberVersion", cluster.getLocalMember().getVersion().toString());
        data.put("isLiteMember", String.valueOf(cluster.getLocalMember().isLiteMember()));
        return new MWHealthCheckResponseImpl(NAME, data, status);
    }

}

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

package com.openexchange.nosql.cassandra.mbean.impl;

import javax.management.NotCompliantMBeanException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.HostDistance;
import com.datastax.driver.core.PoolingOptions;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Session.State;
import com.datastax.driver.core.policies.LoadBalancingPolicy;
import com.openexchange.exception.OXException;
import com.openexchange.management.AnnotatedDynamicStandardMBean;
import com.openexchange.nosql.cassandra.CassandraService;
import com.openexchange.nosql.cassandra.impl.CassandraServiceImpl;
import com.openexchange.nosql.cassandra.mbean.CassandraNodeMBean;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;

/**
 * {@link CassandraNodeMBeanImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class CassandraNodeMBeanImpl extends AnnotatedDynamicStandardMBean implements CassandraNodeMBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(CassandraNodeMBeanImpl.class);

    private final Host host;
    private int connections;
    private int inFlightQueries;
    private int maxLoad;
    private String hostState;
    private String cassandraVersion;
    private int trashedConnections;

    /**
     * Initialises a new {@link CassandraNodeMBeanImpl}.
     *
     * @throws NotCompliantMBeanException
     */
    public CassandraNodeMBeanImpl(ServiceLookup services, Host host) throws NotCompliantMBeanException {
        super(services, CassandraNodeMBean.NAME, CassandraNodeMBean.class);
        this.host = host;
    }

    @Override
    protected void refresh() {
        try {
            CassandraService cassandraService = getService(CassandraService.class);
            if (cassandraService == null) {
                throw ServiceExceptionCode.absentService(CassandraService.class);
            }
            Cluster cluster = cassandraService.getCluster();
            LoadBalancingPolicy loadBalancingPolicy = cluster.getConfiguration().getPolicies().getLoadBalancingPolicy();
            PoolingOptions poolingOptions = cluster.getConfiguration().getPoolingOptions();

            Session session = ((CassandraServiceImpl) cassandraService).getSession();
            State state = session.getState();
            HostDistance distance = loadBalancingPolicy.distance(host);

            connections = state.getOpenConnections(host);
            trashedConnections = state.getTrashedConnections(host);
            inFlightQueries = state.getInFlightQueries(host);
            maxLoad = connections * poolingOptions.getMaxRequestsPerConnection(distance);
            hostState = host.getState();
            cassandraVersion = host.getCassandraVersion().toString();
        } catch (OXException e) {
            LOGGER.error("Could not refresh the statistics for the Cassandra node '{}' in datacenter '{}' in rack '{}'.", host.getAddress().getHostName(), host.getDatacenter(), host.getRack(), e);
        }
    }

    @Override
    public String getNodeName() {
        return host.getAddress().getHostName();
    }

    @Override
    public int getConnections() {
        return connections;
    }

    @Override
    public int getInFlightQueries() {
        return inFlightQueries;
    }

    @Override
    public int getMaxLoad() {
        return maxLoad;
    }

    @Override
    public String getState() {
        return hostState;
    }

    @Override
    public String getCassandraVersion() {
        return cassandraVersion;
    }

    @Override
    public int getTrashedConnections() {
        return trashedConnections;
    }
}

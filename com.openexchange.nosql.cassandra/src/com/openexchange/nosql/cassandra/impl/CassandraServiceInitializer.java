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

package com.openexchange.nosql.cassandra.impl;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import com.datastax.driver.core.Cluster.Initializer;
import com.datastax.driver.core.Configuration;
import com.datastax.driver.core.Host.StateListener;
import com.datastax.driver.core.HostDistance;
import com.datastax.driver.core.PoolingOptions;
import com.datastax.driver.core.SocketOptions;
import com.datastax.driver.core.policies.LoadBalancingPolicy;
import com.datastax.driver.core.policies.Policies;
import com.datastax.driver.core.policies.RetryPolicy;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;

/**
 * {@link CassandraServiceInitializer}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
class CassandraServiceInitializer implements Initializer {

    private final LeanConfigurationService leanConfigurationService;
    private final ServiceLookup services;

    /**
     * Initialises a new {@link CassandraServiceInitializer}.
     *
     * @param services The {@link ServiceLookup} instance
     */
    public CassandraServiceInitializer(ServiceLookup services) {
        super();
        this.services = services;
        leanConfigurationService = services.getService(LeanConfigurationService.class);
    }

    @Override
    public String getClusterName() {
        return leanConfigurationService.getProperty(CassandraProperty.clusterName);
    }

    @Override
    public List<InetSocketAddress> getContactPoints() {
        int port = leanConfigurationService.getIntProperty(CassandraProperty.port);
        String cps = leanConfigurationService.getProperty(CassandraProperty.clusterContactPoints);
        String[] cpsSplit = Strings.splitByComma(cps);

        List<InetSocketAddress> contactPoints = new ArrayList<>();
        for (String contactPoint : cpsSplit) {
            contactPoints.add(new InetSocketAddress(contactPoint, port));
        }
        return contactPoints;
    }

    @Override
    public Configuration getConfiguration() {
        // Retry Policies
        String rp = leanConfigurationService.getProperty(CassandraProperty.retryPolicy);
        boolean logRetryPolicy = leanConfigurationService.getBooleanProperty(CassandraProperty.logRetryPolicy);
        RetryPolicy retryPolicy = logRetryPolicy ? CassandraRetryPolicy.valueOf(rp).getLoggingRetryPolicy() : CassandraRetryPolicy.valueOf(rp).getRetryPolicy();

        // Load Balancing Policies
        String lbPolicy = leanConfigurationService.getProperty(CassandraProperty.loadBalancingPolicy);
        LoadBalancingPolicy loadBalancingPolicy = CassandraLoadBalancingPolicy.createLoadBalancingPolicy(lbPolicy);

        // Build policies
        Policies policies = Policies.builder().withRetryPolicy(retryPolicy).withLoadBalancingPolicy(loadBalancingPolicy).build();

        // Build pooling options
        PoolingOptions poolingOptions = new PoolingOptions();
        int heartbeatInterval = leanConfigurationService.getIntProperty(CassandraProperty.poolingHeartbeat);
        int minLocal = leanConfigurationService.getIntProperty(CassandraProperty.minimumLocalConnectionsPerNode);
        int maxLocal = leanConfigurationService.getIntProperty(CassandraProperty.maximumLocalConnectionsPerNode);
        int minRemote = leanConfigurationService.getIntProperty(CassandraProperty.minimumRemoteConnectionsPerNode);
        int maxRemote = leanConfigurationService.getIntProperty(CassandraProperty.maximumRemoteConnectionsPerNode);
        int idleTimeoutSeconds = leanConfigurationService.getIntProperty(CassandraProperty.idleConnectionTrashTimeout);
        int localMaxRequests = leanConfigurationService.getIntProperty(CassandraProperty.maximumRequestsPerLocalConnection);
        int remoteMaxRequests = leanConfigurationService.getIntProperty(CassandraProperty.maximumRequestsPerRemoteConnection);
        int maxQueueSize = leanConfigurationService.getIntProperty(CassandraProperty.acquisitionQueueMaxSize);
        poolingOptions.setHeartbeatIntervalSeconds(heartbeatInterval);
        poolingOptions.setConnectionsPerHost(HostDistance.LOCAL, minLocal, maxLocal);
        poolingOptions.setConnectionsPerHost(HostDistance.REMOTE, minRemote, maxRemote);
        poolingOptions.setIdleTimeoutSeconds(idleTimeoutSeconds);
        poolingOptions.setMaxRequestsPerConnection(HostDistance.LOCAL, localMaxRequests);
        poolingOptions.setMaxRequestsPerConnection(HostDistance.REMOTE, remoteMaxRequests);
        poolingOptions.setMaxQueueSize(maxQueueSize);

        // Build socket options
        SocketOptions socketOptions = new SocketOptions();
        {
            int connectTimeout = leanConfigurationService.getIntProperty(CassandraProperty.connectTimeout);
            socketOptions.setConnectTimeoutMillis(connectTimeout);
        }
        {
            int readTimeout = leanConfigurationService.getIntProperty(CassandraProperty.readTimeout);
            socketOptions.setConnectTimeoutMillis(readTimeout);
        }

        // Build configuration
        return Configuration.builder().withPolicies(policies).withPoolingOptions(poolingOptions).withSocketOptions(socketOptions).build();
    }

    @Override
    public Collection<StateListener> getInitialListeners() {
        StateListener sl = new MBeanHostStateListener(services);
        return Collections.singletonList(sl);
    }
}

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

import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import com.datastax.driver.core.policies.LoadBalancingPolicy;
import com.datastax.driver.core.policies.RoundRobinPolicy;
import com.datastax.driver.core.policies.TokenAwarePolicy;

/**
 * {@link CassandraLoadBalancingPolicy}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public enum CassandraLoadBalancingPolicy {
    /**
     * Same as the {@link #DCAwareRoundRobin} load balancing policy but with
     * added token awareness.
     */
    DCTokenAwareRoundRobin,
    /**
     * A data-center aware Round-robin load balancing policy.
     * <p/>
     * This policy provides round-robin queries over the node of the local
     * data center. It also includes in the query plans returned a configurable
     * number of hosts in the remote data centers, but those are always tried
     * after the local nodes. In other words, this policy guarantees that no
     * host in a remote data center will be queried unless no host in the local
     * data center can be reached.
     * <p/>
     * If used with a single data center, this policy is equivalent to the
     * {@link #RoundRobin}, but its DC awareness incurs a slight overhead
     * so the latter should be preferred to this policy in that case.
     */
    DCAwareRoundRobin,
    /**
     * A Round-robin load balancing policy.
     * <p/>
     * This policy queries nodes in a round-robin fashion. For a given query,
     * if an host fail, the next one (following the round-robin order) is
     * tried, until all hosts have been tried.
     * <p/>
     * This policy is not datacenter aware and will include every known
     * Cassandra host in its round robin algorithm. If you use multiple
     * datacenter this will be inefficient and you will want to use the
     * {@link #DCAwareRoundRobin} load balancing policy instead.
     */
    RoundRobin,
    ;

    /**
     * Creates a {@link LoadBalancingPolicy} from the specified policy. If
     * the specified policy is unknown, then the {@link #DCTokenAwareRoundRobin}
     * is returned as a fall-back.
     * 
     * @param policy The policy
     * @return The {@link LoadBalancingPolicy}
     */
    public static LoadBalancingPolicy createLoadBalancingPolicy(String policy) {
        CassandraLoadBalancingPolicy p;
        try {
            p = CassandraLoadBalancingPolicy.valueOf(policy);
        } catch (IllegalArgumentException e) {
            p = CassandraLoadBalancingPolicy.DCTokenAwareRoundRobin;
        }
        switch (p) {
            case DCAwareRoundRobin:
                return DCAwareRoundRobinPolicy.builder().build();
            case DCTokenAwareRoundRobin:
                return new TokenAwarePolicy(DCAwareRoundRobinPolicy.builder().build());
            case RoundRobin:
                return new RoundRobinPolicy();
            default:
                throw new IllegalArgumentException("Unknown policy '" + policy + "'");
        }
    }
}

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

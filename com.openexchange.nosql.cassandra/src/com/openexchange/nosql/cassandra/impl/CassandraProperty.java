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

/**
 * {@link CassandraProperty}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public enum CassandraProperty {
    /**
     * Defines the name of the Cassandra cluster. Technically this name does not correlate
     * with the name configured in the real Cassandra cluster, but it's rather used to distinguish
     * exposed JMX metrics when multiple Cluster instances live in the same JVM
     */
    clusterName("ox"),
    /**
     * Defines the Cassandra seed node(s) as a comma separated list
     */
    clusterContactPoints("127.0.0.1"),
    /**
     * Defines the port on which the Cassandra server is running.
     * <p/>
     * Defaults to <code>9042</code>
     */
    port(9042),
    /**
     * Defines load balancing policy to use for the cluster. There are three
     * load balancing policies to choose from:
     * <ul>
     * <li>{@link CassandraLoadBalancingPolicy#RoundRobin}</li>
     * <li>{@link CassandraLoadBalancingPolicy#DCAwareRoundRobin}</li>
     * <li>{@link CassandraLoadBalancingPolicy#DCTokenAwareRoundRobin}</li>
     * </ul>
     * <p/>
     * Defaults to {@link CassandraLoadBalancingPolicy#RoundRobin}
     */
    loadBalancingPolicy(CassandraLoadBalancingPolicy.RoundRobin.name()),
    /**
     * A policy that defines a default behaviour to adopt when a request fails. There are three
     * retry policies to choose from:
     * <ul>
     * <li>{@link CassandraRetryPolicy#defaultRetryPolicy}</li>
     * <li>{@link CassandraRetryPolicy#downgradingConsistencyRetryPolicy}</li>
     * <li>{@link CassandraRetryPolicy#fallthroughRetryPolicy}</li>
     * </ul>
     * <p/>
     * 
     * Defaults to {@link CassandraRetryPolicy#defaultRetryPolicy}
     */
    retryPolicy(CassandraRetryPolicy.defaultRetryPolicy.name()),
    /**
     * Logs the retry decision of the policy.
     * <p/>
     * Defaults to <code>false</code>
     */
    logRetryPolicy(false),
    /**
     * Enables the query logger which logs all executed statements
     * <p/>
     * Defatuls to <code>false</code>
     */
    enableQueryLogger(false),
    /**
     * Defines the latency threshold in milliseconds beyond which queries are considered 'slow'
     * and logged as such by the Cassandra service. Used in conjunction with the 'enableQueryLogger'
     * property.
     * <p/>
     * Defaults to <code>5000</code> msec.
     */
    queryLatencyThreshold(5000),
    /**
     * Defines the amount of time (in seconds) for connection keepalive in the form of a heartbeat.
     * When a connection has been idle for the given amount of time, the Cassandra service will
     * simulate activity by writing a dummy request to it (by sending an <code>OPTIONS</code> message).
     * 
     * Defaults to 30 seconds
     */
    poolingHeartbeat(30),
    ;

    private Object defaultValue;

    private static final String PREFIX = "com.openexchange.nosql.cassandra.";

    /**
     * Initialises a new {@link CassandraProperty}.
     */
    private CassandraProperty(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Returns the fully qualified name for the property
     * 
     * @return the fully qualified name for the property
     */
    public String getName() {
        return PREFIX + name();
    }

    /**
     * Returns the default value of this property
     *
     * @return the default value of this property
     */
    public <T extends Object> T getDefaultValue(Class<T> cls) {
        if (defaultValue.getClass().isAssignableFrom(cls)) {
            return cls.cast(defaultValue);
        } else {
            throw new IllegalArgumentException("The object cannot be converted to the specified type '" + cls.getCanonicalName() + "'");
        }
    }
}

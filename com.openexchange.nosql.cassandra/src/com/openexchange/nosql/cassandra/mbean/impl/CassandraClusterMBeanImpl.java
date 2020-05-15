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

package com.openexchange.nosql.cassandra.mbean.impl;

import java.util.HashSet;
import java.util.Set;
import javax.management.NotCompliantMBeanException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.Metrics;
import com.datastax.driver.core.Metrics.Errors;
import com.datastax.driver.core.TokenRange;
import com.openexchange.exception.OXException;
import com.openexchange.management.AnnotatedDynamicStandardMBean;
import com.openexchange.nosql.cassandra.CassandraService;
import com.openexchange.nosql.cassandra.mbean.CassandraClusterMBean;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;

/**
 * {@link CassandraClusterMBeanImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class CassandraClusterMBeanImpl extends AnnotatedDynamicStandardMBean implements CassandraClusterMBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(CassandraClusterMBeanImpl.class);

    private Metrics metrics;
    private Errors errors;
    private String clusterName;
    private Cluster cluster;

    /**
     * Initialises a new {@link CassandraClusterMBeanImpl}.
     *
     * @param services
     * @param description
     * @param mbeanInterface
     * @throws NotCompliantMBeanException
     */
    public CassandraClusterMBeanImpl(ServiceLookup services) throws NotCompliantMBeanException {
        super(services, CassandraClusterMBean.NAME, CassandraClusterMBean.class);
    }

    @Override
    protected void refresh() {
        try {
            CassandraService cassandraService = getService(CassandraService.class);
            if (cassandraService == null) {
                throw ServiceExceptionCode.absentService(CassandraService.class);
            }
            cluster = cassandraService.getCluster();
            metrics = cluster.getMetrics();
            errors = metrics.getErrorMetrics();
            clusterName = cluster.getClusterName();
        } catch (OXException e) {
            LOGGER.error("Could not refresh the statistics for the Cassandra cluster .", e);
        }
    }

    @Override
    public Set<String> getNodes() {
        Set<String> hosts = new HashSet<>();
        for (Host host : cluster.getMetadata().getAllHosts()) {
            hosts.add(host.getAddress().getHostAddress());
        }
        return hosts;
    }

    @Override
    public Set<String> getTokenRanges() {
        Set<TokenRange> tokenRanges = cluster.getMetadata().getTokenRanges();
        Set<String> tr = new HashSet<>(tokenRanges.size());
        StringBuilder sb = new StringBuilder();
        for (TokenRange tokenRange : tokenRanges) {
            sb.append("DataType: ").append(tokenRange.getStart().getType().getName());
            sb.append(" [");
            sb.append(tokenRange.getStart().getValue());
            sb.append(" - ");
            sb.append(tokenRange.getEnd().getValue());
            sb.append("]");
            tr.add(sb.toString());
            sb.setLength(0);
        }

        return tr;
    }

    @Override
    public String getClusterName() {
        return clusterName;
    }

    @Override
    public int getOpenConnections() {
        return metrics.getOpenConnections().getValue().intValue();
    }

    @Override
    public int getTrashedConnections() {
        return metrics.getTrashedConnections().getValue().intValue();
    }

    @Override
    public int getQueuedTasks() {
        return metrics.getExecutorQueueDepth().getValue().intValue();
    }

    @Override
    public int getBlockingExecutorQueueTasks() {
        return metrics.getBlockingExecutorQueueDepth().getValue().intValue();
    }

    @Override
    public int getSchedulerQueueSize() {
        return metrics.getTaskSchedulerQueueSize().getValue().intValue();
    }

    @Override
    public int getReconnectionSchedulerQueueSize() {
        return metrics.getReconnectionSchedulerQueueSize().getValue().intValue();
    }

    @Override
    public int getConnectedNodes() {
        return metrics.getConnectedToHosts().getValue().intValue();
    }

    @Override
    public long getAuthenticationErrors() {
        return errors.getAuthenticationErrors().getCount();
    }

    @Override
    public long getClientTimeouts() {
        return errors.getClientTimeouts().getCount();
    }

    @Override
    public long getConnectionErrors() {
        return errors.getConnectionErrors().getCount();
    }

    @Override
    public long getIgnores() {
        return errors.getIgnores().getCount();
    }

    @Override
    public long getIgnoresOnClientTimeout() {
        return errors.getIgnoresOnClientTimeout().getCount();
    }

    @Override
    public long getIgnoresOnConnectionError() {
        return errors.getIgnoresOnConnectionError().getCount();
    }

    @Override
    public long getIgnoresOnOtherErrors() {
        return errors.getIgnoresOnOtherErrors().getCount();
    }

    @Override
    public long getIgnoresOnReadTimeout() {
        return errors.getIgnoresOnReadTimeout().getCount();
    }

    @Override
    public long getIgnoresOnUnavailable() {
        return errors.getIgnoresOnUnavailable().getCount();
    }

    @Override
    public long getIgnoresOnWriteTimeout() {
        return errors.getIgnoresOnWriteTimeout().getCount();
    }

    @Override
    public long getOthers() {
        return errors.getOthers().getCount();
    }

    @Override
    public long getReadTimeouts() {
        return errors.getReadTimeouts().getCount();
    }

    @Override
    public long getRetries() {
        return errors.getRetries().getCount();
    }

    @Override
    public long getRetriesOnClientTimeout() {
        return errors.getRetriesOnClientTimeout().getCount();
    }

    @Override
    public long getRetriesOnConnectionError() {
        return errors.getRetriesOnConnectionError().getCount();
    }

    @Override
    public long getRetriesOnOtherErrors() {
        return errors.getRetriesOnOtherErrors().getCount();
    }

    @Override
    public long getRetriesOnReadTimeout() {
        return errors.getRetriesOnReadTimeout().getCount();
    }

    @Override
    public long getRetriesOnUnavailable() {
        return errors.getRetriesOnUnavailable().getCount();
    }

    @Override
    public long getRetriesOnWriteTimeout() {
        return errors.getRetriesOnWriteTimeout().getCount();
    }

    @Override
    public long getSpeculativeExecutions() {
        return errors.getSpeculativeExecutions().getCount();
    }

    @Override
    public long getUnavailables() {
        return errors.getUnavailables().getCount();
    }

    @Override
    public long getWriteTimeouts() {
        return errors.getWriteTimeouts().getCount();
    }

}

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

package com.openexchange.nosql.cassandra.impl.state;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.LatencyTracker;
import com.datastax.driver.core.Statement;

/**
 * {@link LatencyTrackerListener}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class LatencyTrackerListener implements LatencyTracker {

    private static final Logger LOGGER = LoggerFactory.getLogger(LatencyTrackerListener.class);

    /**
     * Initialises a new {@link LatencyTrackerListener}.
     */
    public LatencyTrackerListener() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.datastax.driver.core.LatencyTracker#update(com.datastax.driver.core.Host, com.datastax.driver.core.Statement, java.lang.Exception, long)
     */
    @Override
    public void update(Host host, Statement statement, Exception exception, long newLatencyNanos) {
        if (exception != null) {
            LOGGER.error("The statement '{}' failed to execute on host '{}'. Reason: {}", statement.toString(), host.getAddress().getHostAddress(), exception.getMessage(), exception);
        } else {
            LOGGER.debug("The statement '{}' that was performed on host '{}' was executed in {} msec.", newLatencyNanos * 1000);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.datastax.driver.core.LatencyTracker#onRegister(com.datastax.driver.core.Cluster)
     */
    @Override
    public void onRegister(Cluster cluster) {
        LOGGER.info("This node is registered with the cluster '{}'", cluster.getClusterName());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.datastax.driver.core.LatencyTracker#onUnregister(com.datastax.driver.core.Cluster)
     */
    @Override
    public void onUnregister(Cluster cluster) {
        LOGGER.info("This node was unregistered from the cluster '{}'", cluster.getClusterName());
    }

}

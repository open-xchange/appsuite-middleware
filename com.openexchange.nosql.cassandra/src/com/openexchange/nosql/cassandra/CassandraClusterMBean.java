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

package com.openexchange.nosql.cassandra;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * {@link CassandraClusterMBean}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public interface CassandraClusterMBean {
    
    static final String DOMAIN = "com.openxchange.nosql.cassandra";
    static final String NAME = "Cassandra Cluster Monitoring Bean";

    /**
     * Returns the name of the cluster
     * 
     * @return the name of the cluster
     */
    String getClusterName();

    /**
     * Returns the total amount of connections that this OX node has to the Cassandra cluster
     * 
     * @return the total amount of connections that this OX node has to the Cassandra cluster
     */
    int getOpenConnections();

    /**
     * Returns the total amount of trashed connections
     * 
     * @return the total amount of trashed connections
     */
    int getTrashedConnections();

    /**
     * Returns the number of queued up tasks in the internal {@link CassandraService} executor
     * 
     * @return the number of queued up tasks in the internal CassandraService executor, or <code>-1</code>
     *         if the internal executor is not accessible or not an instance of {@link ThreadPoolExecutor}
     */
    int getQueuedTasks();

    /**
     * Returns the number of queued up tasks in the internal blocking executor of the {@link CassandraService}
     * 
     * @return the number of queued up tasks in the internal blocking executor of the {@link CassandraService}
     *         ,or <code>-1</code> if the internal executor is not accessible or not an instance of
     *         {@link ThreadPoolExecutor}
     */
    int getBlockingExecutorQueueTasks();
}

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

import com.openexchange.management.MBeanMethodAnnotation;

/**
 * {@link CassandraNodeMBean}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public interface CassandraNodeMBean {

    static final String DOMAIN = "com.openxchange.nosql.cassandra";
    static final String NAME = "Cassandra Node Monitoring Bean";

    /**
     * Returns the Cassandra node's full qualified name
     * 
     * @return the Cassandra node's full qualified name
     */
    @MBeanMethodAnnotation(description = "Returns the Cassandra node's full qualified name", parameters = {}, parameterDescriptions = {})
    String getNodeName();

    /**
     * Returns the amount of active connections to the node
     * 
     * @return the amount of active connections to the node
     */
    @MBeanMethodAnnotation(description = "Returns the amount of active connections to the node", parameters = {}, parameterDescriptions = {})
    int getConnections();

    /**
     * Returns the amount of trashed connections for the node
     * 
     * @return the amount of trashed connections for the node
     */
    @MBeanMethodAnnotation(description = "Returns the amount of trashed connections for the node", parameters = {}, parameterDescriptions = {})

    int getTrashedConnections();

    /**
     * Returns the amount of in flight queries, i.e. the amount of queries
     * that are written to the connection and are still being processed by
     * the cluster
     * 
     * @return the amount of the in flight queries
     */
    @MBeanMethodAnnotation(description = "Returns the amount of in flight queries, i.e. the amount of queries that are written to the connection and are still being processed by the cluster", parameters = {}, parameterDescriptions = {})
    int getInFlightQueries();

    /**
     * Returns the maximum connection load for this node
     * 
     * @return the maximum connection load for this node
     */
    @MBeanMethodAnnotation(description = "Returns the maximum connection load for this node", parameters = {}, parameterDescriptions = {})
    int getMaxLoad();

    /**
     * Returns the node's state. Possible return values: UP, DOWN, ADDED
     * 
     * @return the node's state
     */
    @MBeanMethodAnnotation(description = "Returns the node's state. Possible return values: UP, DOWN, ADDED", parameters = {}, parameterDescriptions = {})
    String getState();

    /**
     * Returns the Cassandra version for the specific node
     * 
     * @return the Cassandra version for the specific node
     */
    @MBeanMethodAnnotation(description = "Returns the Cassandra version for the specific node", parameters = {}, parameterDescriptions = {})
    String getCassandraVersion();
}

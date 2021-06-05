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

package com.openexchange.nosql.cassandra.mbean;

import com.openexchange.management.MBeanMethodAnnotation;

/**
 * {@link CassandraNodeMBean} - Provides monitoring information for a Cassandra node
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public interface CassandraNodeMBean extends CassandraMBean {

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

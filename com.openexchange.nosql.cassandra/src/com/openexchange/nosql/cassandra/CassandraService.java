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

package com.openexchange.nosql.cassandra;

import java.util.concurrent.Future;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.MappingManager;
import com.openexchange.exception.OXException;

/**
 * {@link CassandraService} - The service providing access to the Cassandra cluster and its keyspace-bound sessions.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public interface CassandraService {

    /**
     * Gets the Cassandra {@link Cluster} instance
     *
     * @return The Cassandra {@link Cluster} instance or <code>null</code> if the service was
     *         not initialised yet.
     * @throws OXException if the Cassandra {@link Cluster} cannot be returned or any other error is occurred
     */
    Cluster getCluster() throws OXException;

    /**
     * Gets a Cassandra {@link Session} for the specified keyspace
     *
     * @param keyspace The keyspace name
     * @return a Cassandra {@link Session} for the Cassandra {@link Cluster} with the specified keyspace or <code>null</code> if the service was
     *         not initialised yet.
     * @throws OXException if there is no such Cassandra keyspace or if the Cassandra {@link Session} cannot be returned
     * @see CassandraServices#executeQuery(String, Session)
     */
    Session getSession(String keyspace) throws OXException;

    /**
     * Gets a Cassandra {@link Session} for the Cassandra {@link Cluster}. Note that the returned
     * {@link Session} is not bound to any keyspace, meaning that all tables in the CQL queries
     * performed with that {@link Session} will have to be prefixed with the keyspace name.
     *
     * @return a Cassandra {@link Session} for the Cassandra {@link Cluster} or <code>null</code> if the service was
     *         not initialised yet.
     * @throws OXException if the Cassandra {@link Session} cannot be returned
     * @see CassandraServices#executeQuery(String, Session)
     */
    Session getSession() throws OXException;

    /**
     * Gets a Cassandra {@link Session} for an asynchronous query for the specified keyspace
     *
     * @param keyspace The keyspace name
     * @return The Cassandra {@link Session} encapsulated in a {@link Future} object or <code>null</code> if the service was
     *         not initialised yet.
     * @throws OXException If the Cassandra {@link Session} cannot be returned
     */
    Future<Session> getSessionForAsynchronousExecution(String keyspace) throws OXException;

    /**
     * Gets a {@link MappingManager} for the specified keyspace.
     *
     * @param keyspace The keyspace for which to get the {@link MappingManager}
     * @return The {@link MappingManager} bound to the specified keyspace
     * @throws OXException if the {@link MappingManager} cannot be returned
     */
    MappingManager getMappingManager(String keyspace) throws OXException;
}

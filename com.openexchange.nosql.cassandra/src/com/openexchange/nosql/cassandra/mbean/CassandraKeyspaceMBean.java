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

import java.util.Map;
import java.util.Set;

/**
 * {@link CassandraKeyspaceMBean}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public interface CassandraKeyspaceMBean extends CassandraMBean {

    static final String NAME = "Cassandra Keyspace Monitoring Tool";

    /**
     * Returns the tables defined in this keyspace.
     * 
     * @return the tables defined in this keyspace.
     */
    Set<String> getTables();

    /**
     * Returns the replication options for this keyspace.
     * 
     * @return the replication options for this keyspace.
     */
    Map<String, String> getReplicationOptions();

    /**
     * Returns the user types defined in this keyspace.
     * 
     * @return the user types defined in this keyspace.
     */
    Set<String> getUserTypes();

    /**
     * Returns the functions defined in this keyspace.
     * 
     * @return the functions defined in this keyspace.
     */
    Set<String> getFunctions();
}

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

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.MappingManager;
import com.google.common.util.concurrent.ListenableFuture;
import com.openexchange.exception.OXException;

/**
 * {@link CassandraService}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public interface CassandraService {

    /**
     * Returns the Cassandra {@link Cluster} instance with the specified name
     * 
     * @return The Cassandra {@link Cluster} instance
     * @throws OXException if there is no such Cassandra {@link Cluster} or any other error is occurred
     */
    Cluster getCluster() throws OXException;

    /**
     * Returns a Cassandra {@link Session} for the Cassandra {@link Cluster}. Since the keyspace name is not specified
     * during the {@link Session} creation, it will have to be present in the CQL statements that will use the returned
     * {@link Session} to perform queries in the {@link Cluster}
     * 
     * @return a Cassandra {@link Session} for the Cassandra {@link Cluster}
     * @throws OXException if there is no such Cassandra {@link Cluster} or if the Cassandra {@link Session} cannot be returned
     */
    Session getSession() throws OXException;

    /**
     * Returns a Cassandra {@link Session} for the Cassandra {@link Cluster} with the specified name and for the specified keyspace
     * 
     * @param keyspace The keyspace name
     * @return a Cassandra {@link Session} for the Cassandra {@link Cluster} with the specified name and for the specified keyspace
     * @throws OXException if there is no such Cassandra {@link Cluster} or keyspace or if the Cassandra {@link Session} cannot be returned
     */
    Session getSession(String keyspace) throws OXException;

    /**
     * Returns a Cassandra {@link Session} for an asynchronous query. Since the keyspace name is not specified
     * during the {@link Session} creation, it will have to be present in the CQL statements that will use the returned
     * {@link Session} to perform queries in the {@link Cluster}
     * 
     * @return The Cassandra {@link Session} encapsulated in a {@link ListenableFuture} object
     * @throws OXException If the Cassandra {@link Session} cannot be returned
     */
    ListenableFuture<Session> getSessionForAsynchronousExecution() throws OXException;

    /**
     * Returns a Cassandra {@link Session} for an asynchronous query
     * 
     * @param keyspace The keyspace name
     * @return The Cassandra {@link Session} encapsulated in a {@link ListenableFuture} object
     * @throws OXException If the Cassandra {@link Session} cannot be returned
     */
    ListenableFuture<Session> getSessionForAsynchronousExecution(String keyspace) throws OXException;

    /**
     * Returns a {@link MappingManager} for the specified Cassandra {@link Session}.
     * 
     * @param session The Cassandra {@link Session}
     * @return The {@link MappingManager} bound to the specified Cassandra {@link Session}
     * @throws OXException if the {@link MappingManager} cannot be returned
     */
    //TODO: Document/Experiment what happens if the specified Session is bound to a specific keyspace and
    //      the MappingManager manages entities/tables that are not present in the specified keyspace. 
    MappingManager getMapping(Session session) throws OXException;

    /**
     * Returns a {@link MappingManager} for the specified Cassandra {@link Session}
     * 
     * @param keyspace The keyspace name
     * @return The {@link MappingManager} bound to the specified Cassandra {@link Session}
     * @throws OXException if the {@link MappingManager} cannot be returned
     */
    MappingManager getMapping(String keyspace) throws OXException;
}

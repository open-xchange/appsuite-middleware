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

import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.AuthenticationException;
import com.datastax.driver.core.exceptions.NoHostAvailableException;
import com.datastax.driver.mapping.MappingManager;
import com.google.common.util.concurrent.ListenableFuture;
import com.openexchange.exception.OXException;
import com.openexchange.nosql.cassandra.CassandraService;
import com.openexchange.nosql.cassandra.exceptions.CassandraServiceExceptionCodes;
import com.openexchange.server.ServiceLookup;

/**
 * {@link CassandraServiceImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class CassandraServiceImpl implements CassandraService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CassandraServiceImpl.class);

    /**
     * The Cassandra {@link Cluster} instance
     */
    private final Cluster cluster;

    /**
     * Local Cassandra {@link Session}s cache; one per keyspace
     */
    private final ConcurrentMap<String, Session> synchronousSessions;

    /**
     * Local Cassandra asynchronous {@link Session}s cache; one per keyspace
     */
    private final ConcurrentMap<String, ListenableFuture<Session>> asynchronousSessions;

    /**
     * Initialises a new {@link CassandraServiceImpl}.
     * 
     * @param services The {@link ServiceLookup} instance
     * @throws OXException
     */
    public CassandraServiceImpl(ServiceLookup services) throws OXException {
        super();

        // Build the Cluster
        CassandraServiceInitializer initializer = new CassandraServiceInitializer(services);
        cluster = Cluster.buildFrom(initializer);
        try {
            // Initialise the cluster
            cluster.init();
        } catch (NoHostAvailableException e) {
            throw CassandraServiceExceptionCodes.CONTACT_POINTS_NOT_REACHABLE.create(e, initializer.getContactPoints());
        } catch (AuthenticationException e) {
            throw CassandraServiceExceptionCodes.AUTHENTICATION_ERROR.create(e, initializer.getContactPoints());
        }
        // Initialise the sessions cache
        synchronousSessions = new ConcurrentHashMap<>();
        asynchronousSessions = new ConcurrentHashMap<>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.nosql.cassandra.CassandraService#getCluster()
     */
    @Override
    public Cluster getCluster() throws OXException {
        return cluster;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.nosql.cassandra.CassandraService#getSession(java.lang.String)
     */
    @Override
    public Session getSession(String keyspace) throws OXException {
        // Fetch a connection from cache
        Session session = synchronousSessions.get(keyspace);
        if (session != null) {
            return session;
        }

        // If none exists, connect
        Session newSession = cluster.connect(keyspace);
        // Cache the new session
        session = synchronousSessions.putIfAbsent(keyspace, newSession);
        if (session != null) {
            // A session was already initialised by another thread, thus we close the new session
            newSession.close();
        } else {
            session = newSession;
        }
        return session;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.nosql.cassandra.CassandraService#getSessionForAsynchronousExecution(java.lang.String)
     */
    @Override
    public ListenableFuture<Session> getSessionForAsynchronousExecution(String keyspace) throws OXException {
        // Fetch a connection from cache
        ListenableFuture<Session> session = asynchronousSessions.get(keyspace);
        if (session != null) {
            return session;
        }

        // If none exists, connect
        ListenableFuture<Session> newSession = cluster.connectAsync(keyspace);
        // Cache the new session
        session = asynchronousSessions.putIfAbsent(keyspace, newSession);
        if (session != null) {
            // A session was already initialised by another thread, thus we close the new session
            try {
                newSession.get().close();
            } catch (InterruptedException e) {
                throw CassandraServiceExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            } catch (ExecutionException e) {
                throw CassandraServiceExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            } catch (CancellationException e) {
                throw CassandraServiceExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        } else {
            session = newSession;
        }

        return session;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.nosql.cassandra.CassandraService#getMapping(com.datastax.driver.core.Session)
     */
    @Override
    public MappingManager getMapping(Session session) throws OXException {
        return new MappingManager(session);
    }

    /**
     * Shutdown the service
     */
    public void shutdown() {
        if (cluster != null && !cluster.isClosed()) {
            // Close synchronous session
            for (Session session : synchronousSessions.values()) {
                session.close();
            }
            // Close asynchronous sessions
            for (ListenableFuture<Session> session : asynchronousSessions.values()) {
                Session futureSession = null;
                try {
                    boolean canceled = session.cancel(false);
                    if (canceled) {
                        futureSession = session.get();
                    }
                } catch (InterruptedException e) {
                    LOGGER.warn("{}", e.getMessage(), e);
                } catch (ExecutionException e) {
                    LOGGER.warn("{}", e.getMessage(), e);
                } catch (CancellationException e) {
                    LOGGER.warn("{}", e.getMessage(), e);
                }
                if (futureSession != null) {
                    futureSession.close();
                }
            }
            // Close the cluster
            cluster.close();
        }
    }
}

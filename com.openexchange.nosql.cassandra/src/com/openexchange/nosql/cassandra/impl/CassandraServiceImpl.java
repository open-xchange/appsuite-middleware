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

import java.io.Closeable;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.QueryLogger;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.AuthenticationException;
import com.datastax.driver.core.exceptions.InvalidQueryException;
import com.datastax.driver.core.exceptions.NoHostAvailableException;
import com.datastax.driver.mapping.MappingManager;
import com.openexchange.config.lean.LeanConfigurationService;
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
    private final Map<String, CassandraSession> synchronousSessions;

    /**
     * Local Cassandra asynchronous {@link Session}s cache; one per keyspace
     */
    private final ConcurrentMap<String, Future<Session>> asynchronousSessions;
    /**
     * A multi-purpose global keyspace-less Cassandra {@link Session}. Used for
     * retrieving statistics for the {@link Cluster}
     */
    private Session globalSession;

    private final ServiceLookup services;

    private final CassandraServiceInitializer initializer;

    /**
     * Initialises a new {@link CassandraServiceImpl}.
     *
     * @param services The {@link ServiceLookup} instance
     * @throws OXException
     */
    public CassandraServiceImpl(ServiceLookup services) {
        super();
        this.services = services;

        // Build the Cluster
        initializer = new CassandraServiceInitializer(services);
        cluster = Cluster.buildFrom(initializer);

        // Initialise the sessions cache
        synchronousSessions = new ConcurrentHashMap<>();
        asynchronousSessions = new ConcurrentHashMap<>();
    }

    /**
     * Initialises the cluster connection
     * 
     * @throws OXException if initialisation fails
     */
    public void init() throws OXException {
        try {
            // Initialise cluster
            cluster.init();
            // Register the query logger
            LeanConfigurationService leanConfigurationService = services.getService(LeanConfigurationService.class);
            boolean enableQueryLogger = leanConfigurationService.getBooleanProperty(CassandraProperty.enableQueryLogger);
            if (enableQueryLogger) {
                int slowQueryLatencyThresholdMillis = leanConfigurationService.getIntProperty(CassandraProperty.queryLatencyThreshold);
                QueryLogger queryLogger = QueryLogger.builder().withConstantThreshold(slowQueryLatencyThresholdMillis).build();
                cluster.register(queryLogger);
            }
            // Register the MBean schema change listener
            cluster.register(new MBeanSchemaChangeListener(services));
            //Initialise the global session
            globalSession = cluster.connect();
        } catch (NoHostAvailableException e) {
            throw CassandraServiceExceptionCodes.CONTACT_POINTS_NOT_REACHABLE.create(e, initializer.getContactPoints());
        } catch (AuthenticationException e) {
            throw CassandraServiceExceptionCodes.AUTHENTICATION_ERROR.create(e, initializer.getContactPoints());
        }
    }

    @Override
    public Cluster getCluster() throws OXException {
        return cluster;
    }

    @Override
    public Session getSession(String keyspace) throws OXException {
        return getCassandraSession(keyspace).getSession();
    }

    @Override
    public Future<Session> getSessionForAsynchronousExecution(String keyspace) throws OXException {
        // Fetch a connection from cache
        Future<Session> session = asynchronousSessions.get(keyspace);
        if (session != null) {
            return session;
        }

        synchronized (this) {
            session = asynchronousSessions.get(keyspace);
            if (session != null) {
                return session;
            }

            try {
                // If none exists, connect
                Future<Session> newSession = cluster.connectAsync(keyspace);
                // Cache the new session
                asynchronousSessions.put(keyspace, newSession);
                return newSession;
            } catch (NoHostAvailableException e) {
                throw CassandraServiceExceptionCodes.CONTACT_POINTS_NOT_REACHABLE.create(e, initializer.getContactPoints());
            } catch (IllegalStateException e) {
                throw CassandraServiceExceptionCodes.CANNOT_INITIALISE_CLUSTER.create(e, e.getMessage());
            } catch (RuntimeException e) {
                throw CassandraServiceExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }
    }

    @Override
    public MappingManager getMappingManager(String keyspace) throws OXException {
        return getCassandraSession(keyspace).getMappingManager();
    }

    @Override
    public Session getSession() throws OXException {
        return globalSession;
    }

    //////////////////////////////////// HELPERS //////////////////////////////////////////////////

    /**
     * Shutdown the service
     */
    public void shutdown() {
        LOGGER.info("Cassandra Service is shutting down");
        if (cluster.isClosed()) {
            return;
        }

        // Close synchronous session
        LOGGER.info("Closing synchronous session");
        for (CassandraSession session : synchronousSessions.values()) {
            closeSafe(session.getSession());
        }
        synchronousSessions.clear();

        // Close asynchronous sessions
        LOGGER.info("Closing asynchronous session");
        for (Future<Session> session : asynchronousSessions.values()) {
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
            closeSafe(futureSession);
        }
        asynchronousSessions.clear();

        // Close the global session
        LOGGER.info("Closing global session");
        if (globalSession != null && !globalSession.isClosed()) {
            closeSafe(globalSession);
        }

        // Close the cluster
        LOGGER.info("Closing cluster connection");
        closeSafe(cluster);
    }

    private void closeSafe(Closeable closeable) {
        if (null != closeable) {
            try {
                closeable.close();
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    /**
     * Helper method for retrieving a {@link CassandraSession} from the local cache. If no session exists
     * one will be created and cached for future use.
     *
     * @param keyspace The keyspace
     * @return A {@link CassandraSession}
     * @throws OXException if the cluster instance cannot be initialised, or if the specified keyspace does not exist,
     *             or if any other error is occurred
     */
    private CassandraSession getCassandraSession(String keyspace) throws OXException {
        // Fetch a connection from cache
        CassandraSession session = synchronousSessions.get(keyspace);
        if (session != null) {
            return session;
        }

        synchronized (this) {
            session = synchronousSessions.get(keyspace);
            if (session != null) {
                return session;
            }

            try {
                // If none exists, connect
                Session newSession = cluster.connect(keyspace);
                MappingManager mappingManager = new MappingManager(newSession);
                CassandraSession newCassandraSession = new CassandraSession(newSession, mappingManager);
                // Cache the new session
                synchronousSessions.put(keyspace, newCassandraSession);
                return newCassandraSession;
            } catch (NoHostAvailableException e) {
                throw CassandraServiceExceptionCodes.CONTACT_POINTS_NOT_REACHABLE.create(e, initializer.getContactPoints());
            } catch (AuthenticationException e) {
                throw CassandraServiceExceptionCodes.AUTHENTICATION_ERROR.create(e, initializer.getContactPoints());
            } catch (InvalidQueryException e) {
                throw CassandraServiceExceptionCodes.KEYSPACE_DOES_NOT_EXIST.create(e, keyspace);
            } catch (IllegalStateException e) {
                throw CassandraServiceExceptionCodes.CANNOT_INITIALISE_CLUSTER.create(e, e.getMessage());
            } catch (RuntimeException e) {
                throw CassandraServiceExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }
    }
}

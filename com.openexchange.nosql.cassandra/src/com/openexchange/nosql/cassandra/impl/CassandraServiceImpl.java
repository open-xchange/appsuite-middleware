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

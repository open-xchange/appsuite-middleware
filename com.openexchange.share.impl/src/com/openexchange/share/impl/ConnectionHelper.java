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

package com.openexchange.share.impl;

import java.sql.Connection;
import java.sql.SQLException;
import org.slf4j.Logger;
import com.openexchange.caching.ThreadLocalConditionHolder;
import com.openexchange.caching.events.DefaultCondition;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.share.ShareExceptionCodes;

/**
 * {@link ConnectionHelper}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ConnectionHelper {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ConnectionHelper.class);
    }

    /** Whether a <code>ConnectionHelper</code> instance owns a connection or not */
    private static enum ConnectionOwnership {
        /** Connection is <b>not(!)</b> owned by <code>ConnectionHelper</code> instance */
        NONE,
        /** Connection is owned by <code>ConnectionHelper</code> instance in read-only mode */
        OWNED_READABLE,
        /** Connection is owned by <code>ConnectionHelper</code> instance in read-write mode */
        OWNED_WRITEABLE;
    }

    /**
     * <pre>
     * Connection.class.getName()) + '@' + Thread.currentThread().getId()
     * </pre>
     */
    private static String getConnectionSessionParameterName() {
        return new StringBuilder(Connection.class.getName()).append('@').append(Thread.currentThread().getId()).toString();
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final ServiceLookup services;
    private final ConnectionOwnership ownership;
    private final Connection connection;
    private final int contextID;
    private final Session session;
    private final boolean sessionParameterSet;
    private final DefaultCondition cacheCondition;

    private boolean committed;

    /**
     * Initializes a new {@link ConnectionHelper}.
     *
     * @param contextID The context ID
     * @param services The service lookup
     * @param needsWritable <code>true</code> if a writable connection is required, <code>false</code>, otherwise
     * @throws OXException In case connection can't be obtained
     */
    public ConnectionHelper(int contextID, ServiceLookup services, boolean needsWritable) throws OXException {
        super();
        this.contextID = contextID;
        this.services = services;
        DatabaseService dbService = services.getService(DatabaseService.class);
        if (needsWritable) {
            connection = dbService.getWritable(contextID);
            DefaultCondition condition = new DefaultCondition();
            cacheCondition = condition;
            ThreadLocalConditionHolder.getInstance().setCondition(condition);
            ownership = ConnectionOwnership.OWNED_WRITEABLE;
        } else {
            connection = dbService.getReadOnly(contextID);
            cacheCondition = null;
            ownership = ConnectionOwnership.OWNED_READABLE;
        }
        session = null;
        sessionParameterSet = false;
    }

    /**
     * Initializes a new {@link ConnectionHelper}.
     *
     * @param session The session
     * @param services The service lookup
     * @param needsWritable <code>true</code> if a writable connection is required, <code>false</code>, otherwise
     * @throws OXException In case connection can't be obtained
     */
    public ConnectionHelper(Session session, ServiceLookup services, boolean needsWritable) throws OXException {
        super();
        this.session = session;
        this.contextID = session.getContextId();
        this.services = services;
        Connection connection = (Connection) session.getParameter(getConnectionSessionParameterName());
        try {
            if (null == connection) {
                DatabaseService dbService = services.getService(DatabaseService.class);
                if (needsWritable) {
                    connection = dbService.getWritable(contextID);
                    connection.setAutoCommit(false);
                    DefaultCondition condition = new DefaultCondition();
                    cacheCondition = condition;
                    ThreadLocalConditionHolder.getInstance().setCondition(condition);
                    ownership = ConnectionOwnership.OWNED_WRITEABLE;
                } else {
                    connection = dbService.getReadOnly(contextID);
                    cacheCondition = null;
                    ownership = ConnectionOwnership.OWNED_READABLE;
                }
                session.setParameter(getConnectionSessionParameterName(), connection);
                sessionParameterSet = true;
            } else if (needsWritable && connection.isReadOnly()) {
                connection = services.getService(DatabaseService.class).getWritable(contextID);
                connection.setAutoCommit(false);
                ownership = ConnectionOwnership.OWNED_WRITEABLE;
                DefaultCondition condition = new DefaultCondition();
                cacheCondition = condition;
                ThreadLocalConditionHolder.getInstance().setCondition(condition);
                sessionParameterSet = false;
            } else {
                ownership = ConnectionOwnership.NONE;
                cacheCondition = null;
                sessionParameterSet = false;
            }
            this.connection = connection;
        } catch (SQLException e) {
            throw ShareExceptionCodes.DB_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Gets the underlying connection.
     *
     * @return The connection
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Gets the context ID.
     *
     * @return The context ID
     */
    public int getContextID() {
        return contextID;
    }

    /**
     * Starts the transaction on the underlying connection in case the connection is owned by this instance.
     * 
     * @throws OXException In case of an SQL error
     */
    public void start() throws OXException {
        if (ConnectionOwnership.OWNED_WRITEABLE == ownership) {
            try {
                Databases.startTransaction(connection);
            } catch (SQLException e) {
                throw ShareExceptionCodes.DB_ERROR.create(e, e.getMessage());
            }
        }
    }

    /**
     * Commits the transaction on the underlying connection in case the connection is owned by this instance.
     * 
     * @throws OXException In case of an SQL error
     */
    public void commit() throws OXException {
        if (ConnectionOwnership.OWNED_WRITEABLE == ownership) {
            try {
                connection.commit();
            } catch (SQLException e) {
                throw ShareExceptionCodes.DB_ERROR.create(e, e.getMessage());
            }
            committed = true;
        }
    }

    /**
     * Backs the underlying connection in case the connection is owned by this instance, rolling back automatically if not yet committed.
     */
    public void finish() {
        if (ConnectionOwnership.NONE != ownership && null != connection) {
            if (ConnectionOwnership.OWNED_WRITEABLE == ownership) {
                if (committed) {
                    // Committed... Send out possible cache invalidation events
                    DefaultCondition condition = this.cacheCondition;
                    if (null != condition) {
                        condition.set(true);
                        ThreadLocalConditionHolder.getInstance().clear();
                    }
                } else {
                    // Don't send out possible cache invalidation events
                    DefaultCondition condition = this.cacheCondition;
                    if (null != condition) {
                        condition.set(false);
                        ThreadLocalConditionHolder.getInstance().clear();
                    }
                    // Roll-back the connection
                    Databases.rollback(connection);
                }
                // Restore auto-commit mode
                Databases.autocommit(connection);
            }
            // Push back connection into pool
            try {
                if (connection.isReadOnly()) {
                    services.getService(DatabaseService.class).backReadOnly(contextID, connection);
                } else {
                    services.getService(DatabaseService.class).backWritable(contextID, connection);
                }
            } catch (SQLException e) {
                LoggerHolder.LOGGER.warn("Error pushing back connection into pool", e);
            }
        }
        if (sessionParameterSet && null != session) {
            session.setParameter(getConnectionSessionParameterName(), null);
        }
    }

    /**
     * Gets a value indicating whether this connection has been committed or not
     *
     * @return <code>true</code> if the connection has been committed, <code>false</code> otherwise
     */
    public boolean isCommitted() {
        return committed;
    }

}

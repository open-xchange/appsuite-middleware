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

}

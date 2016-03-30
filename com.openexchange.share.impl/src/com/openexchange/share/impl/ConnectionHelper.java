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

    private final ServiceLookup services;
    private final boolean ownsConnection;
    private final Connection connection;
    private final int contextID;
    private final Session session;
    private final boolean sessionParameterSet;

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
        connection = needsWritable ? dbService.getWritable(contextID) : dbService.getReadOnly(contextID);
        ownsConnection = true;
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
        Connection connection = (Connection) session.getParameter(Connection.class.getName() + '@' + Thread.currentThread().getId());
        try {
            if (null == connection) {
                DatabaseService dbService = services.getService(DatabaseService.class);
                connection = needsWritable ? dbService.getWritable(contextID) : dbService.getReadOnly(contextID);
                connection.setAutoCommit(false);
                ownsConnection = true;
                session.setParameter(Connection.class.getName() + '@' + Thread.currentThread().getId(), connection);
                sessionParameterSet = true;
            } else if (needsWritable && connection.isReadOnly()) {
                connection = services.getService(DatabaseService.class).getWritable(contextID);
                connection.setAutoCommit(false);
                ownsConnection = true;
                sessionParameterSet = false;
            } else {
                ownsConnection = false;
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
        if (ownsConnection) {
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
        if (ownsConnection) {
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
        if (ownsConnection && null != connection) {
            if (false == committed) {
                Databases.rollback(connection);
            }
            Databases.autocommit(connection);
            try {
                if (connection.isReadOnly()) {
                    services.getService(DatabaseService.class).backReadOnly(contextID, connection);
                } else {
                    services.getService(DatabaseService.class).backWritable(contextID, connection);
                }
            } catch (SQLException e) {
                org.slf4j.LoggerFactory.getLogger(ConnectionHelper.class).warn("Error backing connection", e);
            }
        }
        if (sessionParameterSet && null != session) {
            session.setParameter(Connection.class.getName() + '@' + Thread.currentThread().getId(), null);
        }
    }

}

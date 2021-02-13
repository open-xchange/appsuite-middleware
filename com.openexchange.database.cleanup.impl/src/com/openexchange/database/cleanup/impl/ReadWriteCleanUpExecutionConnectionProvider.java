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

package com.openexchange.database.cleanup.impl;

import java.sql.Connection;
import java.sql.SQLException;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.database.cleanup.CleanUpExecutionConnectionProvider;
import com.openexchange.database.cleanup.DatabaseCleanUpExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;


/**
 * {@link ReadWriteCleanUpExecutionConnectionProvider}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v8.0.0
 */
public class ReadWriteCleanUpExecutionConnectionProvider implements CleanUpExecutionConnectionProvider, AutoCloseable {

    private final ServiceLookup services;
    private final int representativeContextId;
    private final boolean noTimeout;

    private Connection connection;
    private DatabaseService databaseService;

    /**
     * Initializes a new {@link ReadWriteCleanUpExecutionConnectionProvider}.
     *
     * @param representativeContextId The identifier of a representative context in that schema
     * @param noTimeout <code>true</code> to obtain a database connection w/o timeout, <code>false</code>, otherwise
     * @param services The service look-up
     */
    public ReadWriteCleanUpExecutionConnectionProvider(int representativeContextId, boolean noTimeout, ServiceLookup services) {
        super();
        this.services = services;
        this.representativeContextId = representativeContextId;
        this.noTimeout = noTimeout;
    }

    @Override
    public synchronized Connection getConnection() throws OXException {
        Connection connection = this.connection;
        if (connection == null) {
            // Acquire database service
            DatabaseService databaseService = services.getServiceSafe(DatabaseService.class);
            this.databaseService = databaseService;

            // Fetch connection & start transaction on it
            boolean error = true;
            connection = noTimeout ? databaseService.getForUpdateTask(representativeContextId) : databaseService.getWritable(representativeContextId);
            try {
                Databases.startTransaction(connection);
                this.connection = connection;
                error = false;
            } catch (SQLException e) {
                throw DatabaseCleanUpExceptionCode.SQL_ERROR.create(e, e.getMessage());
            } finally {
                if (error) {
                    if (noTimeout) {
                        databaseService.backForUpdateTaskAfterReading(representativeContextId, connection);
                    } else {
                        databaseService.backWritableAfterReading(representativeContextId, connection);
                    }
                }
            }
        }
        return connection;
    }

    /**
     * Commits connection (if any) and puts it back to pool.
     *
     * @throws SQLException If commit fails
     */
    synchronized void commitAfterSuccess() throws SQLException {
        Connection connection = this.connection;
        if (connection != null) {
            connection.commit();
            this.connection = null;
            Databases.autocommit(connection);
            if (noTimeout) {
                databaseService.backForUpdateTask(representativeContextId, connection);
            } else {
                databaseService.backWritable(representativeContextId, connection);
            }
        }
    }

    @Override
    public synchronized void close() {
        Connection connection = this.connection;
        if (connection != null) {
            this.connection = null;
            Databases.rollback(connection);
            Databases.autocommit(connection);
            if (noTimeout) {
                databaseService.backForUpdateTask(representativeContextId, connection);
            } else {
                databaseService.backWritable(representativeContextId, connection);
            }
        }
    }

}

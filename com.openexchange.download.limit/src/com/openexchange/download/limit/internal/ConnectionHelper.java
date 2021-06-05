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

package com.openexchange.download.limit.internal;

import java.sql.Connection;
import java.sql.SQLException;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.download.limit.exceptions.LimitExceptionCodes;
import com.openexchange.exception.OXException;

/**
 * {@link ConnectionHelper}
 *
 * Provides read-only- and writable connections for accessing the database.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.2
 */
public class ConnectionHelper {

    private final DatabaseService databaseService;
    private final int contextId;

    private Connection readOnlyConnection = null;
    private boolean backReadOnly;
    private Connection writableConnection = null;
    private boolean backWritable;
    private boolean committed;

    /**
     * Initializes a new {@link ConnectionHelper}.
     */
    public ConnectionHelper(int contextId) throws OXException {
        super();
        this.contextId = contextId;
        this.databaseService = Services.getService(DatabaseService.class, true);
    }

    /**
     * Gets a read-only database connection for the current session.
     *
     * @return A read-only connection
     * @throws OXException
     */
    public Connection getReadOnly() throws OXException {
        if (null == readOnlyConnection) {
            readOnlyConnection = databaseService.getReadOnly(contextId);
            backReadOnly = true;
        }
        return readOnlyConnection;
    }

    /**
     * Gets a writable database connection for the current session. Auto-commit is set to <code>false</code> implicitly.
     *
     * @return A writable connection
     * @throws OXException
     */
    public Connection getWritable() throws OXException {
        if (null == writableConnection) {
            writableConnection = databaseService.getWritable(contextId);
            try {
                writableConnection.setAutoCommit(false);
            } catch (SQLException e) {
                throw LimitExceptionCodes.SQL_ERROR.create(e, e.getMessage());
            }
            backWritable = true;
        }
        return writableConnection;
    }

    /**
     * Commits the transaction on the underlying connection in case the connection is owned by this instance.
     */
    public void commit() throws OXException {
        try {
            writableConnection.commit();
        } catch (SQLException e) {
            throw LimitExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        }
        committed = true;
    }

    /**
     * Backs all acquired database connections to the pool if needed.
     */
    public void back() {
        backReadOnly();
        backWritable();
    }

    /**
     * Backs an acquired read-only connection to the pool if needed.
     */
    public void backReadOnly() {
        if (null != readOnlyConnection && backReadOnly) {
            databaseService.backReadOnly(contextId, readOnlyConnection);
            readOnlyConnection = null;
        }
    }

    /**
     * Backs an acquired writable connection to the pool if needed, rolling back the transaction automatically if not yet committed.
     */
    public void backWritable() {
        if (null != writableConnection) {
            if (false == committed) {
                Databases.rollback(writableConnection);
            }
            if (backWritable) {
                Databases.autocommit(writableConnection);
                databaseService.backWritable(contextId, writableConnection);
                writableConnection = null;
            }
        }
    }

    /**
     * Backs an acquired writable connection to the pool if needed, rolling back the transaction automatically if not yet committed.
     */
    public void backWritableAfterReading() {
        if (null != writableConnection) {
            if (false == committed) {
                Databases.rollback(writableConnection);
            }
            if (backWritable) {
                Databases.autocommit(writableConnection);
                databaseService.backWritableAfterReading(contextId, writableConnection);
                writableConnection = null;
            }
        }
    }
}

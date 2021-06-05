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

package com.openexchange.contact.storage.rdb.internal;

import java.sql.Connection;
import java.sql.SQLException;
import com.openexchange.contact.ContactSessionParameterNames;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactExceptionCodes;
import com.openexchange.session.Session;

/**
 * {@link ConnectionHelper}
 *
 * Provides read-only- and writable connections for accessing the database.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ConnectionHelper {

    private final DatabaseService databaseService;
    private final Session session;

    private Connection readOnlyConnection = null;
    private boolean backReadOnly;
    private Connection writableConnection = null;
    private boolean backWritable;
    private boolean committed;

    /**
     * Initializes a new {@link ConnectionHelper}.
     */
    public ConnectionHelper(Session session) throws OXException {
    	super();
    	this.databaseService = RdbServiceLookup.getService(DatabaseService.class, true);
        this.session = session;
    }

    /**
     * Gets a read-only database connection for the current session.
     *
     * @return A read-only connection
     * @throws OXException
     */
    public Connection getReadOnly() throws OXException {
        if (null == readOnlyConnection) {
            Object sessionConnection = session.getParameter(ContactSessionParameterNames.getParamReadOnlyConnection());
            if (null != sessionConnection && Connection.class.isInstance(sessionConnection)) {
                readOnlyConnection = (Connection)sessionConnection;
                backReadOnly = false;
            } else {
                readOnlyConnection = databaseService.getReadOnly(session.getContextId());
                backReadOnly = true;
            }
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
            Object sessionConnection = session.getParameter(ContactSessionParameterNames.getParamWritableConnection());
            if (null != sessionConnection && Connection.class.isInstance(sessionConnection)) {
                writableConnection = (Connection)sessionConnection;
                backWritable = false;
            } else {
                writableConnection = databaseService.getWritable(session.getContextId());
                try {
                    writableConnection.setAutoCommit(false);
                } catch (SQLException e) {
                    throw ContactExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
                }
                backWritable = true;
            }
        }
        return writableConnection;
    }

    public void commit() throws SQLException {
        if (null != writableConnection) {
            writableConnection.commit();
            committed = true;
        }
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
            databaseService.backReadOnly(session.getContextId(), readOnlyConnection);
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
                databaseService.backWritable(session.getContextId(), writableConnection);
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
                databaseService.backWritableAfterReading(session.getContextId(), writableConnection);
                writableConnection = null;
            }
        }
    }

}


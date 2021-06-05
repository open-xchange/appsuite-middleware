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

package com.openexchange.guest.impl.internal;

import java.sql.Connection;
import java.sql.SQLException;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.guest.GuestExceptionCodes;
import com.openexchange.server.ServiceLookup;


/**
 * {@link AbstractConnectionHelper}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public abstract class AbstractConnectionHelper {

    protected final ServiceLookup services;
    protected Connection connection;
    protected boolean committed;
    protected final boolean writableConnection;

    /**
     * Initializes a new {@link AbstractConnectionHelper}.
     * 
     * @param services The service lookup
     * @param needsWritable <code>true</code> if connections used by this helper needs to be writable, <code>false</code> for read only
     */
    public AbstractConnectionHelper(ServiceLookup services, boolean needsWritable) {
        this.services = services;
        this.writableConnection = needsWritable;
        this.committed = false;
    }

    /**
     * Gets the underlying connection.
     *
     * @return the connection
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Starts the transaction on the underlying connection in case the connection is owned by this instance.
     *
     * @throws OXException In case of an SQL error
     */
    public void start() throws OXException {
        acquireConnection();

        try {
            Databases.startTransaction(connection);
        } catch (SQLException e) {
            throw GuestExceptionCodes.DB_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Commits the transaction on the underlying connection in case the connection is owned by this instance.
     *
     * @throws OXException In case of an SQL error
     */
    public void commit() throws OXException {
        try {
            connection.commit();
        } catch (SQLException e) {
            throw GuestExceptionCodes.DB_ERROR.create(e, e.getMessage());
        }
        committed = true;
    }

    /**
     * Gets a value indicating whether this connection has been committed or not
     *
     * @return <code>true</code> if the connection has been committed, <code>false</code> otherwise
     */
    public boolean isCommitted() {
        return committed;
    }

    /**
     * Handles finishing the transaction and returning connections for the given implementation
     */
    public abstract void finish();

    /**
     * Acquires a connection.
     *
     * @throws OXException In case connection can't be acquired 
     */
    public abstract void acquireConnection() throws OXException;
}

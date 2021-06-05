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

package com.openexchange.chronos.impl;

import static com.openexchange.chronos.service.CalendarParameters.PARAMETER_CONNECTION;
import java.sql.Connection;
import java.sql.SQLException;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.impl.osgi.Services;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.SimpleDBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link AbstractStorageOperation}
 *
 * @param <S> The storage class
 * @param <T> The return type of the operation
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.0
 */
public abstract class AbstractStorageOperation<S, T> implements StorageOperation<T> {

    protected final CalendarSession session;
    protected final Context context;

    /**
     * Initializes a new {@link StorageOperation}.
     *
     * @param session The calendar session
     */
    public AbstractStorageOperation(CalendarSession session) throws OXException {
        super();
        this.session = session;
        this.context = ServerSessionAdapter.valueOf(session.getSession()).getContext();
    }

    @Override
    public T executeQuery() throws OXException {
        DatabaseService dbService = Services.getService(DatabaseService.class);
        Connection readConnection = null;
        try {
            readConnection = dbService.getReadOnly(context);
            session.set(PARAMETER_CONNECTION(), readConnection);
            return execute(session, initStorage(new SimpleDBProvider(readConnection, null)));
        } finally {
            session.set(PARAMETER_CONNECTION(), null);
            if (null != readConnection) {
                dbService.backReadOnly(context, readConnection);
            }
        }
    }

    @Override
    public T executeUpdate() throws OXException {
        boolean committed = false;
        DatabaseService dbService = Services.getService(DatabaseService.class);
        Connection writeConnection = null;
        try {
            writeConnection = dbService.getWritable(context);
            writeConnection.setAutoCommit(false);
            session.set(PARAMETER_CONNECTION(), writeConnection);
            T result = execute(session, initStorage(new SimpleDBProvider(writeConnection, writeConnection)));
            writeConnection.commit();
            committed = true;
            return result;
        } catch (SQLException e) {
            if (DBUtils.isTransactionRollbackException(e)) {
                throw CalendarExceptionCodes.DB_ERROR_TRY_AGAIN.create(e, e.getMessage());
            }
            throw CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            session.set(PARAMETER_CONNECTION(), null);
            if (null != writeConnection) {
                if (false == committed) {
                    Databases.rollback(writeConnection);
                    Databases.autocommit(writeConnection);
                    dbService.backWritableAfterReading(context, writeConnection);
                } else {
                    Databases.autocommit(writeConnection);
                    dbService.backWritable(context, writeConnection);
                }
            }
        }
    }

    /**
     * Executes the storage operation.
     *
     * @param session The calendar session
     * @param storage The initialised calendar availability storage to use
     * @return The result
     */
    protected abstract T execute(CalendarSession session, S storage) throws OXException;

    /**
     * Initialises the storage
     *
     * @param dbProvider The database provider
     * @return The storage instance
     * @throws OXException if the storage instance cannot be initialised
     */
    protected abstract S initStorage(DBProvider dbProvider) throws OXException;
}

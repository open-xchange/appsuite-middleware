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

package com.openexchange.multifactor.storage.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.multifactor.exceptions.MultifactorExceptionCodes;

/**
 * {@link MultifactorStorageCommon} contains shared code in multifactor storage classes
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.2
 */
public abstract class MultifactorStorageCommon {

    /**
     * Removes all multifactor devices for a given user
     *
     * @param dbs Database service to use
     * @param sql SQL statement to use
     * @param userId Id of the user
     * @param contextId Id of the context
     * @return <code>true</code> if items removed
     * @throws OXException
     */
    protected boolean deleteAllForUser(DatabaseService dbs, String sql, int userId, int contextId) throws OXException {
        final Connection connection = dbs.getWritable(contextId);
        PreparedStatement statement = null;
        int rows = 0;
        try {
            statement = connection.prepareStatement(sql);
            int param = 1;
            statement.setInt(param++, contextId);
            statement.setInt(param++, userId);
            rows = statement.executeUpdate();
            return rows > 0 ? true : false;
        } catch (SQLException e) {
            throw MultifactorExceptionCodes.SQL_EXCEPTION.create(e.getMessage(), e);
        } finally {
            Databases.closeSQLStuff(statement);
            if (connection != null) {
                if (rows > 0) {
                    dbs.backWritable(connection);
                } else {
                    dbs.backWritableAfterReading(connection);
                }
            }
        }
    }

    /**
     * Removes all multifactor devices for a given context
     *
     * @param dbs Database service to use
     * @param sql SQL statement to use
     * @param contextId Id of the context
     * @return <code>true</code> if rows affected
     * @throws OXException
     */
    protected boolean deleteAllForContext(DatabaseService dbs, String sql, int contextId) throws OXException {
        final Connection connection = dbs.getWritable(contextId);
        PreparedStatement statement = null;
        int rows = 0;
        try {
            statement = connection.prepareStatement(sql);
            int param = 1;
            statement.setInt(param++, contextId);
            rows = statement.executeUpdate();
            return rows > 0 ? true : false;
        } catch (SQLException e) {
            throw MultifactorExceptionCodes.SQL_EXCEPTION.create(e.getMessage(), e);
        } finally {
            Databases.closeSQLStuff(statement);
            if (connection != null) {
                if (rows > 0) {
                    dbs.backWritable(connection);
                } else {
                    dbs.backWritableAfterReading(connection);
                }
            }
        }
    }

}

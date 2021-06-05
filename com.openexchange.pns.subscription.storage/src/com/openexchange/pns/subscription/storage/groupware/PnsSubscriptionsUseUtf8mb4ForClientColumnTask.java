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

package com.openexchange.pns.subscription.storage.groupware;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;

/**
 * {@link PnsSubscriptionsUseUtf8mb4ForClientColumnTask}
 *
 * Adds the 'expires' column to 'pns_subscription' table.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class PnsSubscriptionsUseUtf8mb4ForClientColumnTask extends UpdateTaskAdapter {

    /**
     * Initializes a new {@link PnsSubscriptionsUseUtf8mb4ForClientColumnTask}.
     */
    public PnsSubscriptionsUseUtf8mb4ForClientColumnTask() {
        super();
    }

    @Override
    public String[] getDependencies() {
        return new String[] { PnsSubscriptionTablesUtf8Mb4UpdateTask.class.getName() };
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        Connection connection = params.getConnection();
        int rollback = 0;
        try {
            if (needsCharsetChange(params.getSchema().getSchema(), connection)) {
                connection.setAutoCommit(false);
                rollback = 1;

                changeCharset(connection);

                connection.commit();
                rollback = 2;
            }
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(connection);
                }
                Databases.autocommit(connection);
            }
        }
    }

    private void changeCharset(Connection connection) throws SQLException {
        PreparedStatement alterStmt = null;
        try {
            alterStmt = connection.prepareStatement("ALTER TABLE pns_subscription MODIFY COLUMN client VARCHAR(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL");
            alterStmt.execute();
        } finally {
            Databases.closeSQLStuff(alterStmt);
        }
    }

    private boolean needsCharsetChange(String schema, Connection connection) throws SQLException {
        PreparedStatement columnStmt = null;
        ResultSet columnRs = null;
        try {
            columnStmt = connection.prepareStatement("SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE table_schema = ? AND CHARACTER_SET_NAME = ? AND TABLE_NAME = ? AND COLUMN_NAME = ?");
            columnStmt.setString(1, schema);
            columnStmt.setString(2, "latin1");
            columnStmt.setString(3, "pns_subscription");
            columnStmt.setString(4, "client");
            columnRs = columnStmt.executeQuery();
            return columnRs.next();
        } finally {
            Databases.closeSQLStuff(columnRs, columnStmt);
        }
    }

}

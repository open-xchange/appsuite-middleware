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

package com.openexchange.drive.events.subscribe.rdb;

import static com.openexchange.database.Databases.autocommit;
import static com.openexchange.database.Databases.closeSQLStuff;
import static com.openexchange.database.Databases.rollback;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.java.util.UUIDs;
import com.openexchange.tools.update.Column;
import com.openexchange.tools.update.Tools;

/**
 * {@link DriveEventSubscriptionsMakeUuidPrimaryTask}
 *
 * Changes the column defintion for <code>uuid</code> to <code>uuid BINARY(16) NOT NULL</code> in the
 * <code>driveEventSubscriptions</code> table, fills it with random values, then changes the primary key to
 * <code>(cid,uuid)</code>. Also, an additional index for <code>(cid,service,token)</code> is added.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.2
 */
public class DriveEventSubscriptionsMakeUuidPrimaryTask extends UpdateTaskAdapter {

    @Override
    public String[] getDependencies() {
        return new String[] { DriveEventSubscriptionsAddUuidColumnTask.class.getName() };
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        Connection connection = params.getConnection();
        int rollback = 0;
        try {
            connection.setAutoCommit(false);
            rollback = 1;

            if (false == Tools.columnExists(connection, "driveEventSubscriptions", "uuid")) {
                throw UpdateExceptionCodes.COLUMN_NOT_FOUND.create("uuid", "driveEventSubscriptions");
            }
            /*
             * fill empty uuid values, make uuid column "not null" & adjust primary key afterwards
             */
            fillUUIDs(connection);
            Tools.modifyColumns(connection, "driveEventSubscriptions", new Column("uuid", "BINARY(16) NOT NULL"));
            Tools.createPrimaryKeyIfAbsent(connection, "driveEventSubscriptions", new String[] { "cid", "uuid" });
            Tools.createIndex(connection, "driveEventSubscriptions", new String[] { "cid", "service", "token" });

            connection.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback==1) {
                    rollback(connection);
                }
                autocommit(connection);
            }
        }
    }

    private static int[] fillUUIDs(Connection connection) throws SQLException {
        PreparedStatement selectStatement = null;
        PreparedStatement updateStatement = null;
        ResultSet resultSet = null;
        try {
            selectStatement = connection.prepareStatement("SELECT cid,service,token FROM driveEventSubscriptions WHERE uuid IS NULL;");
            updateStatement = connection.prepareStatement("UPDATE driveEventSubscriptions SET uuid=? WHERE cid=? AND service=? AND token=?;");
            resultSet = selectStatement.executeQuery();
            while (resultSet.next()) {
                updateStatement.setBytes(1, UUIDs.toByteArray(UUID.randomUUID()));
                updateStatement.setInt(2, resultSet.getInt(1));
                updateStatement.setString(3, resultSet.getString(2));
                updateStatement.setString(4, resultSet.getString(3));
                updateStatement.addBatch();
            }
            return updateStatement.executeBatch();
        } finally {
            closeSQLStuff(resultSet, selectStatement);
            closeSQLStuff(updateStatement);
        }
    }

}

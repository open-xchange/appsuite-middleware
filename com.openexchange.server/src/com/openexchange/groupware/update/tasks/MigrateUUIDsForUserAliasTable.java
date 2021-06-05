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

package com.openexchange.groupware.update.tasks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;
import java.util.UUID;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.java.util.UUIDs;
import com.openexchange.tools.update.Column;
import com.openexchange.tools.update.Tools;

/**
 * {@link MigrateUUIDsForUserAliasTable}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class MigrateUUIDsForUserAliasTable extends AbstractUserAliasTableUpdateTask {

    /**
     * Initialises a new {@link MigrateUUIDsForUserAliasTable}.
     */
    public MigrateUUIDsForUserAliasTable() {
        super();
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        Connection connection = params.getConnection();
        int rollback = 0;
        try {
            Databases.startTransaction(connection);
            rollback = 1;

            if (!Tools.columnExists(connection, "user_alias", "uuid")) {
                // Create the 'uuid' column
                Tools.addColumns(connection, "user_alias", new Column("uuid", "BINARY(16) DEFAULT NULL"));
            }
            // Get the aliases
            Set<Alias> aliases = getAllAliasesInUserAttributes(connection);
            // Migrate the UUIDs
            if (!aliases.isEmpty()) {
                migrateUUIDs(connection, aliases);
            }
            // Generate random UUIDs for those aliases that have none
            insertUUIDs(connection);
            // Commit changes
            connection.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(connection);
                }
                Databases.autocommit(connection);
            }
        }

    }

    @Override
    public String[] getDependencies() {
        return new String[0];
    }

    /**
     * Insert random UUIDs to the 'uuid' column of the 'user_alias' table for all aliases that have none
     *
     * @param connection The writable connection
     * @throws SQLException If an SQL error occurs
     */
    private void insertUUIDs(Connection connection) throws SQLException {
        Statement statement = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatment = null;
        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT cid, user, alias FROM user_alias WHERE uuid IS NULL");
            preparedStatment = connection.prepareStatement("UPDATE user_alias SET uuid = ? WHERE cid = ? AND user = ? AND alias = ?");
            while (resultSet.next()) {
                addBatch(preparedStatment, resultSet.getInt(1), resultSet.getInt(2), resultSet.getString(3), UUIDs.toByteArray(UUID.randomUUID()));
            }
            preparedStatment.executeBatch();
        } finally {
            Databases.closeSQLStuff(resultSet, statement);
            Databases.closeSQLStuff(preparedStatment);
        }
    }

    /**
     * Migrate the existing UUIDs from the 'user_attribute' table to the 'user_alias' table only if not previously migrated
     *
     * @param connection The writable connection
     * @param aliases The set of a
     * @throws SQLException
     */
    private void migrateUUIDs(Connection connection, Set<Alias> aliases) throws SQLException {
        PreparedStatement preparedStatment = null;
        try {
            preparedStatment = connection.prepareStatement("UPDATE user_alias SET uuid = ? WHERE cid = ? AND user = ? AND alias = ? AND uuid IS NULL");
            for (Alias alias : aliases) {
                addBatch(preparedStatment, alias.getCid(), alias.getUserId(), alias.getAlias(), UUIDs.toByteArray(alias.getUuid()));
            }
            preparedStatment.executeBatch();
        } finally {
            Databases.closeSQLStuff(preparedStatment);
        }
    }

    /**
     * Add a batch to the prepared statement
     *
     * @param preparedStatement The prepared statement
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param alias The alias
     * @param uuid The UUID as byte array
     * @throws SQLException if an SQL error is occurred
     */
    private void addBatch(PreparedStatement preparedStatement, int contextId, int userId, String alias, byte[] uuid) throws SQLException {
        int columnIndex = 0;
        preparedStatement.setBytes(++columnIndex, uuid);
        preparedStatement.setInt(++columnIndex, contextId);
        preparedStatement.setInt(++columnIndex, userId);
        preparedStatement.setString(++columnIndex, alias);
        preparedStatement.addBatch();
    }
}
